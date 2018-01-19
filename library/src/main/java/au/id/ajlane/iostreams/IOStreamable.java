/*
 * Copyright 2016 Aaron Lane
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package au.id.ajlane.iostreams;

import java.util.function.Supplier;

/**
 * Provides reusable access to a stream of items.
 * <p>
 * For once-off streams, prefer to use {@link IOStream} directly.
 * <p>
 * Utility methods on {@link IOStreamables} can make working with {@code IOStreamable}s easier: Use {@link
 * IOStreamables#map} to modify the items in the provided {@code IOStream}s, or {@link IOStreamables#filter} to remove
 * particular items. Join the {@code IOStreams}s from multiple {@code IOStreamable}s together with {@link
 * IOStreamables#concat}.
 *
 * @param <T>
 *     The type of the items in the {@code IOStreamable}.
 */
@FunctionalInterface
public interface IOStreamable<T>
{
    /**
     * Consumes the stream of the resource, triggering any side-effects while discarding the items.
     *
     * @throws IOStreamReadException
     *     If there were any problems in reading the stream.
     * @throws IOStreamCloseException
     *     If there were any problems in closing the stream.
     * @throws InterruptedException
     *     If the thread was interrupted.
     */
    default void consume() throws IOStreamReadException, IOStreamCloseException, InterruptedException
    {
        IOStreamables.consume(this);
    }

    /**
     * Consumes the stream of the resource.
     *
     * @param consumer
     *     A consumer to receive the items in the stream.
     *
     * @throws IOStreamReadException
     *     If there were any problems in reading the stream.
     * @throws IOStreamCloseException
     *     If there were any problems in closing the stream.
     * @throws InterruptedException
     *     If the thread was interrupted.
     */
    default void consume(final Supplier<? extends IOStreamConsumer<? super T>> consumer) throws IOStreamReadException,
        IOStreamCloseException, InterruptedException
    {
        IOStreamables.consume(this, consumer);
    }

    /**
     * Filters certain items out of a streamable resource.
     *
     * @param filter
     *     The filter to apply. Must not be {@code null}.
     *
     * @return An streamable resource which is a filtered view of the given streamable resource.
     */
    default IOStreamable<T> filter(final Supplier<? extends IOStreamFilter<? super T>> filter)
    {
        return IOStreamables.filter(this, filter);
    }

    /**
     * Converts the streamable resource into a series of streamable resource instances and concatenates all of their
     * values.
     *
     * @param <R>
     *     The type of the items in the transformed streamable resource.
     * @param transform
     *     The {@link IOStreamTransform} which will expand the original stream.
     *
     * @return An expanded streamable resource with the new type.
     */
    default <R> IOStreamable<R> flatMap(
        final Supplier<? extends IOStreamTransform<? super T, ? extends IOStreamable<? extends R>>>
            transform
    )
    {
        return IOStreamables.flatMap(this, transform);
    }

    /**
     * Filters the items in the streamable resource to include only items which match the predicate.
     *
     * @param predicate
     *     A supplier to provide the predicate.
     *
     * @return A filtered view of the streamable resource.
     */
    default IOStreamable<T> keep(final Supplier<? extends IOStreamPredicate<? super T>> predicate)
    {
        return IOStreamables.keep(this, predicate);
    }

    /**
     * Transforms the items in a streamable resource.
     * <p>
     * Transforming the items in a streamable resource does not change the number of items it provides. To remove items,
     * use {@link #filter(Supplier)}.
     *
     * @param <R>
     *     The type of the items in the new streamable resource.
     * @param transform
     *     The map to apply to each item in the streamable resource.
     *
     * @return A streamable resource which is a transformed view of the given streamable resource.
     */
    default <R> IOStreamable<R> map(final Supplier<? extends IOStreamTransform<? super T, ? extends R>> transform)
    {
        return IOStreamables.map(this, transform);
    }

    /**
     * Filters the items in the streamable resource to exclude any items which match the predicate.
     *
     * @param predicate
     *     A supplier to provide the predicate.
     *
     * @return A filtered view of the streamable resource.
     */
    default IOStreamable<T> skip(final Supplier<? extends IOStreamPredicate<? super T>> predicate)
    {
        return IOStreamables.skip(this, predicate);
    }

    /**
     * Provides a ready {@link IOStream} to iterate over the items in this streamable resource.
     *
     * @return An instance of {@link IOStream}.
     */
    IOStream<T> stream();
}
