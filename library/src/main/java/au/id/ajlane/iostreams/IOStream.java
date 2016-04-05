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

import java.io.Closeable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
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
 * When defining public interfaces, consider carefully whether you require a {@code IOStream} or an {@code
 * IOStreamable}.
 *
 * @param <T>
 *     The type of the items in the {@code IOStream}
 */
public interface IOStream<T> extends Closeable
{
    /**
     * Releases any resources held by the {@code IOStream}.
     * <p>
     * Successive calls to {@code close()} should have no further effect.
     * <p>
     * The behaviour of a {@code IOStream} after its {@code close} method has been called is undefined. Typically, an
     * {@code IOStream} would behave as if it contained no more items (by returning {@code false} from {@link
     * #hasNext}).
     *
     * @throws IOStreamCloseException
     *     If the stream could not be closed for some reason. The stream may not release all resources if this is the
     *     case.
     */
    @Override
    void close() throws IOStreamCloseException;

    /**
     * Consumes the stream while discarding the items. <p>Useful for triggering any side-effects from processing the
     * stream where the output is not needed.</p> <p>Closes the stream when done.</p>
     *
     * @throws IOStreamReadException
     *     If any item in the stream could not be read for some reason.
     * @throws IOStreamCloseException
     *     If the stream could not be closed for some reason. The stream may not release all resources if this is the
     *     case.
     */
    default void consume() throws IOStreamReadException, IOStreamCloseException
    {
        IOStreams.consume(this);
    }

    /**
     * Consumes the stream by passing each item to a consumer. <p>Closes the stream when done.</p>
     *
     * @param consumer
     *     A function to receive the items in the stream.
     *
     * @throws IOStreamReadException
     *     If any item in the stream could not be read for some reason.
     * @throws IOStreamCloseException
     *     If the stream could not be closed for some reason. The stream may not release all resources if this is the
     *     case.
     */
    default void consume(final IOStreamConsumer<? super T> consumer)
        throws IOStreamReadException, IOStreamCloseException
    {
        IOStreams.consume(this, consumer);
    }

    /**
     * Counts the number of items in the stream.
     * <p>
     * Consumes the stream in order to count all of the items.
     *
     * @return The number of items in the stream, or {@link Long#MAX_VALUE} if there are too many items to count.
     * @throws IOStreamReadException
     *     If there was a problem in reading the stream.
     * @throws IOStreamCloseException
     *     If there was a problem in closing the stream.
     */
    default long count() throws IOStreamReadException, IOStreamCloseException
    {
        return IOStreams.count(this);
    }

    /**
     * Applies a filter to the items in the stream.
     *
     * @param filter
     *     The filter to apply. Must not be null.
     *
     * @return A filtered view of the stream.
     */
    default IOStream<T> filter(final IOStreamFilter<? super T> filter)
    {
        return IOStreams.filter(this, filter);
    }

    /**
     * Transforms each item in the stream into zero or more items.
     *
     * @param transform
     *     The transform to apply. Must not be null.
     * @param <R>
     *     The type of the items in the transformed stream.
     *
     * @return A transformed view of the stream.
     */
    default <R> IOStream<R> flatMap(final IOStreamTransform<? super T, ? extends IOStream<? extends R>> transform)
    {
        return IOStreams.flatMap(this, transform);
    }

    /**
     * Consumes the stream, accumulating all of the items into a single result.
     *
     * @param initial
     *     An initial value to provide to the accumulator.
     * @param accumulator
     *     A function which adds an item to the result. Must not be null.
     * @param <R>
     *     The type of the result.
     *
     * @return The result of the accumulator, after it has consumed all of the items in the stream.
     *
     * @throws IOStreamReadException
     *     If there was a problem with reading the stream.
     * @throws IOStreamCloseException
     *     If there was a problem with closing the stream.
     */
    default <R> R fold(final R initial, final IOStreamAccumulator<R, ? super T> accumulator)
        throws IOStreamReadException, IOStreamCloseException
    {
        return IOStreams.fold(this, initial, accumulator);
    }

    /**
     * Groups adjacent items in the stream together, into groups with a fixed maximum size.
     *
     * @param size
     *     The maximum size of the groups. Must be positive and non-zero.
     *
     * @return A view of the stream in which adjacent items are grouped into sub-streams.
     */
    default IOStream<? extends IOStream<T>> group(final int size)
    {
        return IOStreams.group(this, size);
    }

    /**
     * Groups adjacent items in the stream together.
     * <p>
     * This is the dual of {@link #split(IOStreamBiPredicate)}.
     *
     * @param predicate
     *     A predicate which determine whether two items are in the same group. Must not be null.
     *
     * @return A view of the stream in which adjacent items are grouped into sub-streams.
     */
    default IOStream<? extends IOStream<T>> group(final IOStreamBiPredicate<? super T, ? super T> predicate)
    {
        return IOStreams.group(this, predicate);
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
     *
     * @throws IOStreamReadException
     *     If there was any problem in reading from the underlying resource.
     */
    boolean hasNext() throws IOStreamReadException;

    /**
     * Filters the stream to retain only items which are matched by the predicate.
     *
     * @param predicate
     *     The predicate to test items with. Must not be null.
     *
     * @return A filtered view of the stream.
     */
    default IOStream<T> keep(final IOStreamPredicate<? super T> predicate)
    {
        return IOStreams.keep(this, predicate);
    }

    /**
     * Limits the stream to provide no more than a given number of items.
     *
     * @param size
     *     The maximum number of items the limited stream should provide. Must be non-negative.
     *
     * @return A limited view of the stream.
     */
    default IOStream<T> limit(final int size)
    {
        return IOStreams.limit(this, size);
    }

    /**
     * Transforms each item in the stream.
     *
     * @param transform
     *     The function to apply to transform each item.
     * @param <R>
     *     The type of the transformed items.
     *
     * @return A new stream which will apply the function as each item is read.
     */
    default <R> IOStream<R> map(final IOStreamTransform<? super T, ? extends R> transform)
    {
        return IOStreams.map(this, transform);
    }

    /**
     * Transforms each item in the stream.
     * <p>
     * Allows exceptions to be handled during the transform, rather than terminating the stream. If the exception
     * handler returns {@link FilterDecision#KEEP_AND_CONTINUE} or {@link FilterDecision#KEEP_AND_TERMINATE}, the stream
     * will terminate with the original exception. Otherwise, the stream will skip the item and either continue ({@link
     * FilterDecision#SKIP_AND_CONTINUE}) or terminate quietly ({@link FilterDecision#SKIP_AND_TERMINATE}).
     * <p>
     * The exception handler will only handle checked exceptions. Runtime exceptions will continue to propagate. To
     * handle runtime exceptions, catch them in the transform.
     *
     * @param transform
     *     The function to apply to transform each item.
     * @param exceptionHandler
     *     The function to handle any exceptions from the transform and retroactively filter problematic items.
     * @param <R>
     *     The type of the transformed items.
     *
     * @return A new stream which will apply the function as each item is read.
     */
    default <R> IOStream<R> map(
        final IOStreamTransform<? super T, ? extends R> transform,
        final IOStreamTransformExceptionHandler<? super T> exceptionHandler
    )
    {
        return IOStreams.map(this, transform, exceptionHandler);
    }


    /**
     * Finds the maximum value in the stream.
     * <p>
     * Consumes the stream.
     *
     * @param comparator
     *     A comparator to use to compare items in the stream. Must not be null.
     * @return The maximum value in the stream, or an empty value if the stream is empty or if the maximum is null.
     * @throws IOStreamReadException
     *     If there was a problem in reading the stream.
     * @throws IOStreamCloseException
     *     If there was a problem in closing the stream.
     */
    default Optional<T> max(final Comparator<T> comparator) throws IOStreamReadException, IOStreamCloseException
    {
        return IOStreams.max(this, comparator);
    }

    /**
     * Finds the minimum value in the stream.
     * <p>
     * Consumes the stream.
     *
     * @param comparator
     *     A comparator to use to compare items in the stream. Must not be null.
     * @return The minimum value in the stream, or an empty value if the stream is empty or if the minimum is null.
     * @throws IOStreamReadException
     *     If there was a problem in reading the stream.
     * @throws IOStreamCloseException
     *     If there was a problem in closing the stream.
     */
    default Optional<T> min(final Comparator<T> comparator) throws IOStreamReadException, IOStreamCloseException
    {
        return IOStreams.min(this, comparator);
    }

    /**
     * Returns the next item in the {@code IOStream}.
     * <p>
     * If the thread is interrupted before this method returns, implementations may choose to throw a {@link
     * IOStreamReadException} with a {@link InterruptedException} as the cause.
     *
     * @return The next item in the {@code IOStream}. {@code null} is a valid item, although discouraged.
     *
     * @throws NoSuchElementException
     *     If there is no next item (calling {@link #hasNext} before this method would have returned {@code false}).
     * @throws IOStreamReadException
     *     If there was any problem in reading from the underlying resource.
     */
    T next() throws IOStreamReadException;

    /**
     * Registers a function to observe values as they are consumed.
     * <p>
     * The observer will be closed when the returned stream is closed.
     *
     * @param observer
     *     The function which will observe the values. Must not be null.
     *
     * @return A view of the stream which will call the observer function.
     */
    default IOStream<T> observe(final IOStreamConsumer<? super T> observer)
    {
        return IOStreams.observe(this, observer);
    }

    /**
     * Creates a peekable view of the stream.
     *
     * @return A peekable view of the stream.
     */
    default PeekableIOStream<T> peekable()
    {
        return IOStreams.peekable(this);
    }

    /**
     * Consumes the stream, reducing all of its values into a single result.
     *
     * @param reducer
     *     A function to read the items and produce the result. Must not be null.
     * @param <R>
     *     The type of the result.
     *
     * @return The result calculated by the reducer.
     *
     * @throws IOStreamReadException
     *     If there was a problem in reading the stream.
     * @throws IOStreamCloseException
     *     If there was a problem in closing the stream.
     */
    default <R> R reduce(final IOStreamTransform<? super IOStream<T>, R> reducer)
        throws IOStreamReadException, IOStreamCloseException
    {
        return IOStreams.reduce(this, reducer);
    }

    /**
     * Filters the stream by skipping items that match a predicate.
     *
     * @param predicate
     *     A predicate to determine whether to skip an item.
     *
     * @return A view of the stream, containing only items that are not matched by the predicate.
     */
    default IOStream<T> skip(final IOStreamPredicate<? super T> predicate)
    {
        return IOStreams.skip(this, predicate);
    }

    /**
     * Splits the stream into groups of items.
     * <p>
     * This is the dual of {@link #group(IOStreamBiPredicate)}.
     *
     * @param predicate
     *     A predicate to determine if two items should be in different groups.
     *
     * @return A view of the stream as a stream of groups.
     */
    default IOStream<? extends IOStream<T>> split(final IOStreamBiPredicate<? super T, ? super T> predicate)
    {
        return IOStreams.split(this, predicate);
    }

    /**
     * Consumes the stream by collecting all of the items into an array.
     *
     * @param supplier
     *     A function to provide the array. Use a reference to an array constructor (e.g. {@code String[]::new}) to
     *     create a new array. Must not be null
     *
     * @return An array containing the items from the stream.
     *
     * @throws IOStreamReadException
     *     If there was a problem in reading the stream.
     * @throws IOStreamCloseException
     *     If there was a problem in closing the stream.
     */
    default T[] toArray(final IntFunction<T[]> supplier) throws IOStreamReadException, IOStreamCloseException
    {
        return IOStreams.toArray(this, supplier);
    }

    /**
     * Consumes the stream by collecting all of the items into a list.
     *
     * @return A list containing the items from the stream.
     *
     * @throws IOStreamReadException
     *     If there was a problem in reading the stream.
     * @throws IOStreamCloseException
     *     If there was a problem in closing the stream.
     */
    default List<T> toList() throws IOStreamReadException, IOStreamCloseException
    {
        return IOStreams.toList(this);
    }

    /**
     * Consumes the stream by collecting all of the items into a set.
     *
     * @return A set containing the unique items from the stream.
     *
     * @throws IOStreamReadException
     *     If there was a problem in reading the stream.
     * @throws IOStreamCloseException
     *     If there was a problem in closing the stream.
     */
    default Set<T> toSet() throws IOStreamReadException, IOStreamCloseException
    {
        return IOStreams.toSet(this);
    }

    /**
     * Terminates the stream when one of the items matches. <p> The matching item will not be kept.
     *
     * @param predicate
     *     The predicate to test each item. Must not be null.
     *
     * @return A view of the stream which may terminate early.
     */
    default IOStream<T> until(final IOStreamPredicate<? super T> predicate)
    {
        return IOStreams.until(this, predicate);
    }
}
