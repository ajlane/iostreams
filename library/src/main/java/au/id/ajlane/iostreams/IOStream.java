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

import java.io.Closeable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * An iterator over a stream of items.
 * <p>
 * Unlike {@link Iterator} or {@link Stream}, this class accommodates streams which are backed by heavy-weight resources
 * (sockets, databases, etc.), perform blocking calculations, or require clean-up when the consumer has finished
 * iterating.
 * <p>
 * Consider the easier-to-use {@link AbstractIOStream} when implementing a new {@code IOStream}.
 * <p>
 * Utility methods on {@link IOStreams} can make working with {@code IOStream}s easier: Use {@link IOStreams#map} to
 * modify the items in a {@code IOStream}, or {@link IOStreams#filter} to remove particular items. Join multiple {@code
 * Stream}s together with {@link IOStreams#concat}.
 * <p>
 * When defining public interfaces, consider carefully whether you require a {@code IOStream} or an {@code IOStreamable}.
 *
 * @param <T>
 *         The type of the items in the {@code IOStream}
 */
public interface IOStream<T> extends Closeable
{
    /**
     * Releases any resources held by the {@code IOStream}.
     * <p>
     * Successive calls to {@code close()} should have no further effect.
     * <p>
     * The behaviour of a {@code IOStream} after its {@code close} method has been called is undefined. Typically, such a
     * {@code IOStream} would behave as if it contained no more items (by returning {@code false} from {@link #hasNext}).
     *
     * @throws IOStreamCloseException
     *         If the {@code IOStream} could not be closed for some reason. The {@code IOStream} may not release all
     *         resources if this is the case.
     */
    @Override
    void close() throws IOStreamCloseException;

    default void consume() throws IOStreamReadException, IOStreamCloseException {
        IOStreams.consume(this);
    }

    default void consume(final IOStreamConsumer<? super T> consumer)
        throws IOStreamReadException, IOStreamCloseException {
        IOStreams.consume(this, consumer);
    }

    default IOStream<T> filter(final IOStreamFilter<? super T> filter){
        return IOStreams.filter(this, filter);
    }

    default <R> IOStream<R> flatMap(final IOStreamTransform<? super T, ? extends IOStream<? extends R>> transform){
        return IOStreams.flatMap(this, transform);
    }

    default <R> R fold(final R initial, final IOStreamAccumulator<R, T> accumulator)
        throws IOStreamReadException, IOStreamCloseException {
        return IOStreams.fold(this, initial, accumulator);
    }

    default IOStream<IOStream<T>> grouped(final int size){
        return IOStreams.group(this, size);
    }

    /**
     * Checks if there are any more items in the {@code IOStream}.
     * <p>
     * It is not uncommon for significant work to be necessary in order to calculate {@code hasNext}. Typically,
     * implementations not be able to determine if there is a next item without fetching and buffering it.
     * <p>
     * If the thread is interrupted before this method returns, implementations may choose to throw a {@link
     * IOStreamReadException} with a {@link InterruptedException} as the cause.
     *
     * @return {@code true} if a subsequent call to {@link #next} will succeed. {@code false} otherwise.
     * @throws IOStreamReadException
     *         If there was any problem in reading from the underlying resource.
     */
    boolean hasNext() throws IOStreamReadException;

    default IOStream<T> keep(final IOStreamPredicate<? super T> predicate){
        return IOStreams.keep(this, predicate);
    }

    default IOStream<T> limit(final int size){
        return IOStreams.limit(this, size);
    }

    default <R> IOStream<R> map(final IOStreamTransform<? super T, ? extends R> transform){
        return IOStreams.map(this, transform);
    }

    default <R> IOStream<R> map(final IOStreamTransform<? super T, ? extends R> transform, final IOStreamTransformExceptionHandler<? super T> exceptionHandler){
        return IOStreams.map(this, transform, exceptionHandler);
    }

    /**
     * Returns the next item in the {@code IOStream}.
     * <p>
     * If the thread is interrupted before this method returns, implementations may choose to throw a {@link
     * IOStreamReadException} with a {@link InterruptedException} as the cause.
     *
     * @return The next item in the {@code IOStream}. {@code null} is a valid item, although discouraged.
     * @throws NoSuchElementException
     *         If there is no next item (calling {@link #hasNext} before this method would have returned {@code
     *         false}).
     * @throws IOStreamReadException
     *         If there was any problem in reading from the underlying resource.
     */
    T next() throws IOStreamReadException;

    default IOStream<T> observe(final IOStreamConsumer<? super T> observer){
        return IOStreams.observe(this, observer);
    }

    default <R> R reduce(final IOStreamTransform<? super IOStream<T>, R> reducer)
        throws IOStreamReadException, IOStreamCloseException {
        return IOStreams.reduce(this, reducer);
    }

    default IOStream<T> skip(final IOStreamPredicate<? super T> predicate){
        return IOStreams.skip(this, predicate);
    }

    default IOStream<IOStream<T>> split(final IOStreamBiPredicate<? super T, ? super T> predicate){
        return IOStreams.split(this, predicate);
    }

    default List<T> toList() throws IOStreamReadException, IOStreamCloseException {
        return IOStreams.toList(this);
    }
}
