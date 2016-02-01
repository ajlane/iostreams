/*
 * Copyright 2013 Aaron Lane
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
 *         The type of the items in the {@code IOStreamable}.
 */
@FunctionalInterface
public interface IOStreamable<T>
{
    /**
     * Provides a ready {@link IOStream} to iterate over the items in this {@code IOStreamable}.
     *
     * @return An instance of {@link IOStream}.
     */
    IOStream<T> stream();

    default <R> IOStreamable<R> map(final IOStreamTransform<? super T,? extends R> transform){
        return IOStreamables.map(this, transform);
    }

    default <R> IOStreamable<R> flatMap(final IOStreamTransform<? super T, ? extends IOStreamable<? extends R>> transform){
        return IOStreamables.flatMap(this, transform);
    }

    default IOStreamable<T> filter(final IOStreamFilter<? super T> filter){
        return IOStreamables.filter(this, filter);
    }

    default void foreach(final IOStreamConsumer<? super T> consumer) throws IOStreamReadException, IOStreamCloseException {
        IOStreamables.foreach(this, consumer);
    }
}
