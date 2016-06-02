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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Stream;

/**
 * Utilities for working with instances of {@link IOStream}.
 * <p>
 * Where a function duplicates a capability that is on the {@code IOStream} interface, prefer to use that version
 * instead - specific implementations may be more efficient.
 */
public final class IOStreams
{
    private static final EmptyIOStream EMPTY = new EmptyIOStream();

    /**
     * Casts all of the items in the stream.
     * <p>
     * Beware that if any of the items cannot be cast, the stream will throw a {@link ClassCastException} when it
     * attempts to read those items.
     * <p>
     * To safely change the type, use {@link #map(IOStream, IOStreamTransform)} to transform the items.
     *
     * @param stream
     *     The stream to transform. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     * @param <R>
     *     The type to cast the items to.
     *
     * @return A new stream containing the same elements, but with the items cast to the new type.
     */
    public static <T, R> IOStream<R> cast(final IOStream<T> stream)
    {
        return IOStreams.map(
            stream, new AbstractIOStreamTransform<T, R>()
            {
                @Override
                @SuppressWarnings("unchecked")
                protected R transform(final T item)
                {
                    return (R) item;
                }
            }
        );
    }

    /**
     * Concatenates a series of streams into a single stream.
     * <p>
     * Each stream will be closed as it is used. If the concatenated stream is closed, any unused streams will remain
     * unclosed. If unused streams must be closed, they should be closed by the container iterator as a part of its own
     * close method (if it implements {@link AutoCloseable}), or by some other process.
     *
     * @param streams
     *     A stream of streams. Must not be null.
     * @param <T>
     *     The type of the items in the streams.
     *
     * @return A view of the streams as a single stream.
     */
    public static <T> IOStream<T> concat(final Iterator<? extends IOStream<? extends T>> streams)
    {
        return IOStreams.concat(IOStreams.fromIterator(streams));
    }

    /**
     * Concatenates a series of streams into a single stream.
     * <p>
     * Each stream will be closed as it is used. If the concatenated stream is closed, any unused streams will remain
     * unclosed. If unused streams must be closed, they should be closed by the container stream as a part of its own
     * close method.
     *
     * @param streams
     *     A stream of streams. Must not be null.
     * @param <T>
     *     The type of the items in the streams.
     *
     * @return A view of the streams as a single stream.
     */
    public static <T> IOStream<T> concat(final IOStream<? extends IOStream<? extends T>> streams)
    {
        Objects.requireNonNull(streams, "The stream of streams cannot be null.");
        return new AbstractIOStream<T>()
        {
            private IOStream<? extends T> current = null;

            @Override
            protected void end() throws Exception
            {
                try (
                    final IOStream<? extends IOStream<? extends T>> autoCloseStreams = streams;
                    final IOStream<? extends T> autoCloseCurrent = current
                )
                {
                    // Auto close resources
                }
            }

            @Override
            protected T find() throws Exception
            {
                while (current != null)
                {
                    if (Thread.interrupted())
                    {
                        throw new InterruptedException("The thread was interrupted.");
                    }
                    if (current.hasNext())
                    {
                        return current.next();
                    }
                    else
                    {
                        current.close();
                        current = streams.hasNext() ? Objects.requireNonNull(
                            streams.next(),
                            "One of the concatenated streams was null."
                        ) : null;
                    }
                }
                return terminate();
            }

            @Override
            protected void open() throws Exception
            {
                if (streams.hasNext())
                {
                    current = Objects.requireNonNull(streams.next(), "The first concatenated stream was null");
                }
            }
        };
    }

    /**
     * Concatenates a series of streams into a single stream.
     * <p>
     * Each stream will be closed as it is used. Unlike {@link #concat(IOStream)}, any unused streams will be closed
     * when the concatenated stream is closed.
     *
     * @param streams
     *     A stream of streams. Must not be null.
     * @param <T>
     *     The type of the items in the streams.
     *
     * @return A view of the streams as a single stream.
     */
    @SafeVarargs
    public static <T> IOStream<T> concat(final IOStream<? extends T>... streams)
    {
        Objects.requireNonNull(streams);

        return new AbstractIOStream<T>()
        {
            private int index = 0;

            @Override
            protected void end() throws Exception
            {
                Exception lastException = null;
                boolean caughtRuntimeException = false;
                for (; index < streams.length; index++)
                {
                    if (streams[index] == null)
                    {
                        continue;
                    }
                    try
                    {
                        streams[index].close();
                    }
                    catch (final RuntimeException ex)
                    {
                        caughtRuntimeException = true;
                        if (lastException != null)
                        {
                            ex.addSuppressed(lastException);
                        }
                        lastException = ex;
                    }
                    catch (final IOStreamException ex)
                    {
                        if (lastException != null)
                        {
                            ex.getCause()
                                .addSuppressed(lastException);
                        }
                        lastException = ex.getCause();
                    }
                }
                if (lastException != null)
                {
                    if (caughtRuntimeException)
                    {
                        if (lastException instanceof RuntimeException)
                        {
                            throw (RuntimeException) lastException;
                        }
                        else
                        {
                            throw new RuntimeException(
                                "Suppressed a runtime exception with a checked exception.",
                                lastException
                            );
                        }
                    }
                    else
                    {
                        if (lastException instanceof RuntimeException)
                        {
                            throw (RuntimeException) lastException;
                        }
                        else
                        {
                            throw new IOStreamCloseException(lastException);
                        }
                    }
                }
            }

            @Override
            protected T find() throws Exception
            {
                while (index < streams.length)
                {
                    final IOStream<? extends T> stream = Objects.requireNonNull(
                        streams[index],
                        "One of the concatenated streams was null."
                    );
                    if (stream.hasNext())
                    {
                        return stream.next();
                    }
                    else
                    {
                        stream.close();
                        index += 1;
                    }
                }
                return terminate();
            }
        };
    }

    /**
     * Consumes a stream by discarding the items.
     * <p>
     * Useful for triggering any side effects from processing the stream, where the actual items are not required.
     *
     * @param stream
     *     The stream to consume. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @throws IOStreamReadException
     *     If there was a problem while reading the stream.
     * @throws IOStreamCloseException
     *     If there was a problem while closing the stream.
     */
    public static <T> void consume(final IOStream<T> stream)
        throws IOStreamReadException, IOStreamCloseException
    {
        try (final IOStream<T> outer = stream)
        {
            while (stream.hasNext())
            {
                stream.next();
            }
        }
    }

    /**
     * Consumes the items in a stream.
     *
     * @param stream
     *     The stream to consume. Must not be null.
     * @param consumer
     *     A function to receive each item in the stream. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @throws IOStreamReadException
     *     If there was a problem while reading the stream.
     * @throws IOStreamCloseException
     *     If there was a problem while closing the stream.
     */
    public static <T> void consume(final IOStream<T> stream, final IOStreamConsumer<? super T> consumer)
        throws IOStreamReadException, IOStreamCloseException
    {
        try (
            final IOStream<T> autoCloseStream = stream;
            final IOStreamConsumer<? super T> autoCloseConsumer = consumer
        )
        {
            while (stream.hasNext())
            {
                try
                {
                    consumer.accept(stream.next());
                }
                catch (final RuntimeException ex)
                {
                    throw ex;
                }
                catch (final Exception ex)
                {
                    throw new IOStreamReadException(ex);
                }
            }
        }
        catch (final RuntimeException ex)
        {
            throw ex;
        }
        catch (final Exception ex)
        {
            throw new IOStreamCloseException(ex);
        }
    }

    /**
     * Counts the number of items in the stream.
     * <p>
     * Consumes the stream in order to count every item. To keep a running total of items as they are being consumed,
     * consider using a stateful consumer with {@link #observe(IOStream, IOStreamConsumer)}.
     *
     * @param stream
     *     The stream to consume to count the values. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return The total number of items in the stream, or {@link Long#MAX_VALUE} if there are too many items to
     * represent with a long.
     *
     * @throws IOStreamReadException
     *     If there was a problem with reading the stream.
     * @throws IOStreamCloseException
     *     If there was a problem with closing the stream.
     */
    public static <T> long count(final IOStream<T> stream)
        throws IOStreamReadException, IOStreamCloseException
    {
        try (final IOStream<T> autoCloseStream = stream)
        {
            long count = 0;
            while (stream.hasNext())
            {
                stream.next();
                if (count < Long.MAX_VALUE)
                {
                    count++;
                }
            }
            return count;
        }
    }

    /**
     * Gets an immutable, empty stream.
     * <p>
     * For efficiency, the empty stream can be reused.
     *
     * @param <T>
     *     The type of the items in the stream, if it had any.
     *
     * @return An empty stream.
     */
    @SuppressWarnings("unchecked")
    public static <T> IOStream<T> empty()
    {
        return IOStreams.EMPTY;
    }

    /**
     * Applies a filter to the items in a stream.
     *
     * @param stream
     *     The stream to filter. Must not be null.
     * @param filter
     *     The filter to apply. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return A filtered view of the stream.
     */
    public static <T> IOStream<T> filter(final IOStream<? extends T> stream, final IOStreamFilter<? super T> filter)
    {
        Objects.requireNonNull(stream, "The stream cannot be null.");
        Objects.requireNonNull(filter, "The filter cannot be null.");
        return new AbstractIOStream<T>()
        {
            private volatile boolean terminate = false;

            @Override
            protected void end() throws Exception
            {
                try (
                    final IOStream<? extends T> autoCloseStream = stream;
                    final IOStreamFilter<? super T> autoCloseFilter = filter
                )
                {
                    // Auto close resources
                }
            }

            @Override
            protected T find() throws Exception
            {
                while (!terminate && stream.hasNext())
                {
                    if (Thread.interrupted())
                    {
                        throw new InterruptedException("The thread was interrupted.");
                    }
                    final T next = stream.next();
                    final FilterDecision decision = filter.apply(next);
                    switch (decision)
                    {
                        case KEEP_AND_CONTINUE:
                            return next;
                        case SKIP_AND_CONTINUE:
                            continue;
                        case KEEP_AND_TERMINATE:
                            this.terminate = true;
                            return next;
                        case SKIP_AND_TERMINATE:
                            return terminate();
                        default:
                            throw new IllegalStateException("Unrecognised decision: " + decision);
                    }
                }
                return terminate();
            }
        };
    }

    /**
     * Transforms each item in the stream into zero or more items.
     *
     * @param stream
     *     The stream to transform. Must not be null.
     * @param transform
     *     The transform to apply. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     * @param <R>
     *     The type of the items in the transformed stream.
     *
     * @return A transformed view of the stream.
     */
    public static <T, R> IOStream<R> flatMap(
        final IOStream<? extends T> stream,
        final IOStreamTransform<? super T, ? extends IOStream<? extends R>> transform
    )
    {
        return IOStreams.concat(IOStreams.map(stream, transform));
    }

    /**
     * Transforms a stream of arrays into a stream of items.
     *
     * @param stream
     *     The stream of arrays. Must not be null.
     * @param <T>
     *     The type of the items.
     *
     * @return A single view of the items in the arrays.
     */
    public static <T> IOStream<T> flattenArrays(final IOStream<? extends T[]> stream)
    {
        return IOStreams.flatMap(stream, IOStreams::fromArray);
    }

    /**
     * Transforms a stream of {@link Iterable} instances into a stream of items.
     *
     * @param stream
     *     The stream of {@link Iterable} instances. Must not be null.
     * @param <T>
     *     The type of the items.
     *
     * @return A single view of the items in the {@link Iterable} instances.
     */
    public static <T> IOStream<T> flattenIterables(final IOStream<? extends Iterable<? extends T>> stream)
    {
        return IOStreams.flatMap(stream, IOStreams::fromIterable);
    }

    /**
     * Transforms a stream of iterators into a stream of items.
     *
     * @param stream
     *     The stream of iterators. Must not be null.
     * @param <T>
     *     The type of the items.
     *
     * @return A single view of the items provided by the iterators.
     */
    public static <T> IOStream<T> flattenIterators(final IOStream<? extends Iterator<? extends T>> stream)
    {
        return IOStreams.flatMap(stream, IOStreams::fromIterator);
    }

    /**
     * Transform a stream of {@link Stream} instances into a stream of items.
     *
     * @param stream
     *     The stream of {@link Stream} instances. Must not be null.
     * @param <T>
     *     The type of the items.
     *
     * @return A single view of the items provided by the {@link Stream} instances.
     */
    public static <T> IOStream<T> flattenStreams(final IOStream<? extends Stream<? extends T>> stream)
    {
        return IOStreams.flatMap(stream, IOStreams::fromStream);
    }

    /**
     * Consumes the stream, accumulating all of the items into a single result.
     *
     * @param stream
     *     The stream to consume. Must not be null.
     * @param initial
     *     An initial value to provide to the accumulator.
     * @param accumulator
     *     A function which adds an item to the result. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
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
    public static <T, R> R fold(
        final IOStream<T> stream,
        final R initial,
        final IOStreamAccumulator<R, ? super T> accumulator
    )
        throws IOStreamReadException, IOStreamCloseException
    {
        Objects.requireNonNull(stream, "The stream must not be null.");
        Objects.requireNonNull(accumulator, "The accumulator must not be null.");
        try (
            final IOStream<T> autoCloseStream = stream;
            final IOStreamAccumulator<R, ? super T> autoCloseAccumulator = accumulator
        )
        {
            R result = initial;
            while (stream.hasNext())
            {
                result = accumulator.add(result, stream.next());
            }
            return result;
        }
        catch (final RuntimeException ex)
        {
            throw ex;
        }
        catch (final Exception ex)
        {
            throw new IOStreamReadException(ex);
        }
    }

    /**
     * Creates a stream from an array of items.
     *
     * @param values
     *     The array of items. May be empty, but must not be null.
     * @param <T>
     *     The type of the items.
     *
     * @return A view of the array as a stream.
     */
    @SafeVarargs
    public static <T> IOStream<T> fromArray(final T... values)
    {
        Objects.requireNonNull(values, "The array cannot be null. Use an empty array instead.");
        return new IOStream<T>()
        {
            private int index = 0;

            @Override
            public void close()
            {
                index = values.length;
            }

            @Override
            public boolean hasNext()
            {
                return index < values.length;
            }

            @Override
            public T next()
            {
                if (index < values.length)
                {
                    final T next = values[index];
                    index += 1;
                    return next;
                }
                throw new NoSuchElementException("There is not next item in the stream.");
            }
        };
    }

    /**
     * Creates a stream from an {@link Iterable} instance.
     *
     * @param iterable
     *     The iterable set of items. May be empty, but must not be null.
     * @param <T>
     *     The type of the items.
     *
     * @return A view of the {@link Iterable} instance as a stream.
     */
    public static <T> IOStream<T> fromIterable(final Iterable<? extends T> iterable)
    {
        Objects.requireNonNull(iterable, "The iterable cannot be null.");
        return IOStreams.fromIterator(iterable.iterator());
    }

    /**
     * Adapts an iterator as a stream.
     * <p>
     * If the iterator is also {@link AutoCloseable}, the stream will close the iterator when it closes.
     *
     * @param iterator
     *     The iterator to adapt. May be empty, but must not be null.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return A view of the iterator as a stream.
     */
    public static <T> IOStream<T> fromIterator(final Iterator<? extends T> iterator)
    {
        Objects.requireNonNull(iterator, "The iterator cannot be null.");
        return new IOStream<T>()
        {
            @Override
            public void close() throws IOStreamCloseException
            {
                if (iterator instanceof AutoCloseable)
                {
                    try
                    {
                        ((AutoCloseable) iterator).close();
                    }
                    catch (final RuntimeException ex)
                    {
                        throw ex;
                    }
                    catch (final Exception ex)
                    {
                        throw new IOStreamCloseException(ex);
                    }
                }
            }

            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public T next()
            {
                return iterator.next();
            }
        };
    }

    /**
     * Adapts an instance of {@link Stream} as a stream.
     *
     * @param stream
     *     The instance of {@link Stream}. May be empty, but must not be null.
     * @param <T>
     *     The type of the items.
     *
     * @return A view of the {@link Stream} instance as a stream.
     */
    public static <T> IOStream<T> fromStream(final Stream<? extends T> stream)
    {
        Objects.requireNonNull(stream, "The stream cannot be null.");
        return new IOStream<T>()
        {
            final Iterator<? extends T> iterator = stream.iterator();

            @Override
            public void close() throws IOStreamCloseException
            {
                stream.close();
            }

            @Override
            public boolean hasNext() throws IOStreamReadException
            {
                return iterator.hasNext();
            }

            @Override
            public T next() throws IOStreamReadException
            {
                return iterator.next();
            }
        };
    }

    /**
     * Groups adjacent items in the stream together, into groups with a fixed maximum size.
     *
     * @param stream
     *     The stream containing the items. Must not be null.
     * @param size
     *     The maximum size of the groups. Must be positive and non-zero.
     * @param <T>
     *     The type of the items.
     *
     * @return A view of the stream in which adjacent items are grouped into sub-streams.
     */
    public static <T> IOStream<IOStream<T>> group(final IOStream<T> stream, int size)
    {
        Objects.requireNonNull(stream, "The stream cannot be null.");
        if (size <= 0)
        {
            throw new IllegalArgumentException("The size must be positive and non-zero.");
        }
        return new AbstractIOStream<IOStream<T>>()
        {
            @Override
            public void end() throws Exception
            {
                stream.close();
            }

            @Override
            public IOStream<T> find() throws Exception
            {
                if (stream.hasNext())
                {
                    return new IOStream<T>()
                    {
                        private int count = 0;

                        @Override
                        public void close()
                        {
                            // Ignore - we'll close the underlying stream in the parent
                        }

                        @Override
                        public boolean hasNext() throws IOStreamReadException
                        {
                            return count < size && stream.hasNext();
                        }

                        @Override
                        public T next() throws IOStreamReadException
                        {
                            if (count >= size)
                            {
                                throw new NoSuchElementException();
                            }
                            count++;
                            return stream.next();
                        }
                    };
                }
                return terminate();
            }
        };
    }

    /**
     * Groups adjacent items in the stream together.
     * <p>
     * This is the dual of {@link #split(IOStream, IOStreamBiPredicate)}
     *
     * @param stream
     *     The stream containing the items. Must not be null.
     * @param predicate
     *     A predicate which determines whether two items are in the same group. Must not be null.
     * @param <T>
     *     The type of the items.
     *
     * @return A view of the {@link IOStream} in which adjacent items are grouped into sub-streams.
     */
    public static <T> IOStream<IOStream<T>> group(
        final IOStream<T> stream,
        final IOStreamBiPredicate<? super T, ? super T> predicate
    )
    {
        Objects.requireNonNull(stream, "The stream cannot be null.");
        Objects.requireNonNull(predicate, "The predicate cannot be null.");

        return new AbstractIOStream<IOStream<T>>()
        {
            private boolean hasNext = false;
            private boolean hasPrevious = false;
            private T next;
            private T previous;

            @Override
            public void end() throws Exception
            {
                try (
                    final IOStream<T> autoCloseStream = stream;
                    final IOStreamBiPredicate<? super T, ? super T> autoClosePredicate = predicate
                )
                {
                    // Auto close resources
                }
                catch (RuntimeException ex)
                {
                    throw ex;
                }
                catch (Exception ex)
                {
                    throw new IOStreamCloseException(ex);
                }
            }

            @Override
            public IOStream<T> find() throws Exception
            {
                if (!hasPrevious)
                {
                    if (stream.hasNext())
                    {
                        previous = stream.next();
                        hasPrevious = true;
                    }
                    else
                    {
                        return terminate();
                    }
                }
                if (!hasNext)
                {
                    if (stream.hasNext())
                    {
                        next = stream.next();
                        hasNext = true;
                    }
                    else
                    {
                        hasPrevious = false;
                        return IOStreams.singleton(previous);
                    }
                }
                return new AbstractIOStream<T>()
                {
                    private boolean partitionEnd = false;

                    @Override
                    public void end() throws Exception
                    {
                        // Ignore - we'll close the underlying stream in the parent
                    }

                    @Override
                    protected T find() throws Exception
                    {
                        if (partitionEnd)
                        {
                            return terminate();
                        }
                        if (!hasPrevious)
                        {
                            if (stream.hasNext())
                            {
                                previous = stream.next();
                                hasPrevious = true;
                            }
                            else
                            {
                                return terminate();
                            }
                        }
                        if (!hasNext)
                        {
                            if (stream.hasNext())
                            {
                                next = stream.next();
                                hasNext = true;
                            }
                        }
                        final T result = previous;
                        partitionEnd = !hasNext || hasPrevious && !predicate.test(previous, next);
                        hasPrevious = hasNext;
                        previous = next;
                        hasNext = false;
                        return result;
                    }
                };
            }
        };
    }

    /**
     * Filters a stream to retain only items which are matched by the predicate.
     *
     * @param stream
     *     The stream to filter. Must not be null.
     * @param predicate
     *     The predicate to test items with. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return A filtered view of the stream.
     */
    public static <T> IOStream<T> keep(
        final IOStream<? extends T> stream,
        final IOStreamPredicate<? super T> predicate
    )
    {
        return filter(stream, IOStreamFilters.fromPredicate(predicate));
    }

    /**
     * Limits a stream to provide no more than a given number of items.
     *
     * @param stream
     *     The stream to limit. Must not be null.
     * @param size
     *     The maximum number of items the limited stream should provide. Must be non-negative.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return A limited view of the stream.
     */
    public static <T> IOStream<T> limit(final IOStream<T> stream, final int size)
    {
        return stream.filter(IOStreamFilters.limit(size));
    }

    /**
     * Transforms each item in a stream.
     *
     * @param stream
     *     The stream containing the items. Must not be null.
     * @param transform
     *     A transform which will be used to map the items. Must not be null.
     * @param <T>
     *     The type of the original items.
     * @param <R>
     *     The type of the transformed items.
     *
     * @return A transformed view of the stream.
     */
    public static <T, R> IOStream<R> map(
        final IOStream<? extends T> stream,
        final IOStreamTransform<? super T, ? extends R> transform
    )
    {
        Objects.requireNonNull(stream, "The stream cannot be null.");
        Objects.requireNonNull(transform, "The transform cannot be null.");
        return new IOStream<R>()
        {
            @Override
            public void close() throws IOStreamCloseException
            {
                try (
                    final IOStream<? extends T> autoCloseStream = stream;
                    final IOStreamTransform<? super T, ? extends R> autoCloseTransform = transform
                )
                {
                    // Auto close resources
                }
                catch (final RuntimeException ex)
                {
                    throw ex;
                }
                catch (final Exception ex)
                {
                    throw new IOStreamCloseException(ex);
                }
            }

            @Override
            public boolean hasNext() throws IOStreamReadException
            {
                return stream.hasNext();
            }

            @Override
            public R next() throws IOStreamReadException
            {
                try
                {
                    return transform.apply(stream.next());
                }
                catch (final RuntimeException ex)
                {
                    throw ex;
                }
                catch (final Exception ex)
                {
                    throw new IOStreamReadException(ex);
                }
            }
        };
    }

    /**
     * Transforms each item in a stream.
     * <p>
     * If an item cannot be transformed (i.e. the transform throws a checked exception), an exception handler can
     * retroactively apply a filter to skip the item or terminate the stream.
     * <p>
     * The transform and the exception handler will be closed when the returned stream is closed.
     *
     * @param stream
     *     The stream containing the items. Must not be null.
     * @param transform
     *     A transform which will be used to map the items. Must not be null.
     * @param exceptionHandler
     *     A filter to apply if an item cannot be transformed.
     * @param <T>
     *     The type of the original items.
     * @param <R>
     *     The type of the transformed items.
     *
     * @return A transformed view of the stream.
     */
    public static <T, R> IOStream<R> map(
        final IOStream<? extends T> stream,
        final IOStreamTransform<? super T, ? extends R> transform,
        final IOStreamTransformExceptionHandler<? super T> exceptionHandler
    )
    {
        Objects.requireNonNull(stream, "The stream cannot be null.");
        Objects.requireNonNull(transform, "The transform cannot be null.");
        Objects.requireNonNull(exceptionHandler, "The exception handler cannot be null.");
        return new AbstractIOStream<R>()
        {
            private volatile boolean terminate = false;

            @Override
            public void end() throws Exception
            {
                try (
                    final IOStream<? extends T> autoCloseStream = stream;
                    final IOStreamTransform<? super T, ? extends R> autoCloseTransform = transform;
                    final IOStreamTransformExceptionHandler<? super T> autoCloseExceptionHandler = exceptionHandler
                )
                {
                    // Auto close resources
                }
            }

            @Override
            public R find() throws Exception
            {
                while (!terminate && stream.hasNext())
                {
                    if (Thread.interrupted())
                    {
                        throw new InterruptedException("The thread was interrupted.");
                    }
                    final T item = stream.next();
                    try
                    {
                        return transform.apply(item);
                    }
                    catch (final RuntimeException ex)
                    {
                        throw ex;
                    }
                    catch (final Exception transformThrown)
                    {
                        final Exception transformFailure = transformThrown instanceof IOStreamException ?
                            ((IOStreamException) transformThrown).getCause() :
                            transformThrown;
                        final FilterDecision decision;
                        try
                        {
                            decision = exceptionHandler.handle(item, transformFailure);
                        }
                        catch (final RuntimeException handlerThrown)
                        {
                            handlerThrown.addSuppressed(transformFailure);
                            throw handlerThrown;
                        }
                        catch (final IOStreamException handlerThrown)
                        {
                            final Exception handlerFailure = handlerThrown.getCause();
                            handlerFailure.addSuppressed(transformFailure);
                            throw handlerFailure;
                        }
                        catch (final Exception handlerThrown)
                        {
                            handlerThrown.addSuppressed(transformFailure);
                            throw handlerThrown;
                        }
                        if (decision == null)
                        {
                            final NullPointerException handlerFailure =
                                new NullPointerException("The filter decision was null.");
                            handlerFailure.addSuppressed(transformFailure);
                            throw handlerFailure;
                        }
                        switch (decision)
                        {
                            case KEEP_AND_CONTINUE:
                            case KEEP_AND_TERMINATE:
                                throw transformFailure;
                            case SKIP_AND_CONTINUE:
                                continue;
                            case SKIP_AND_TERMINATE:
                                this.terminate = true;
                                continue;
                            default:
                                final UnsupportedOperationException handlerFailure =
                                    new UnsupportedOperationException("Unrecognised decision: " + decision);
                                handlerFailure.addSuppressed(transformFailure);
                                throw handlerFailure;
                        }
                    }
                }
                return terminate();
            }
        };
    }

    /**
     * Finds the maximum value in the stream.
     *
     * @param stream
     *     The stream to consume to find the maximum value. Must not be null.
     * @param comparator
     *     The comparator to use to compare items in the stream. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return The maximum value, or an empty value if the stream is empty or if the maximum is null.
     *
     * @throws IOStreamReadException
     *     If there was problem with reading the stream.
     * @throws IOStreamCloseException
     *     If there was a problem with closing the stream.
     */
    public static <T> Optional<T> max(final IOStream<T> stream, Comparator<T> comparator)
        throws IOStreamReadException, IOStreamCloseException
    {
        Optional<T> max = Optional.empty();
        try (final IOStream<T> autoCloseStream = stream)
        {
            while (stream.hasNext())
            {
                final T item = stream.next();
                if (!max.isPresent() || comparator.compare(item, max.get()) > 0)
                {
                    max = Optional.ofNullable(item);
                }
            }
        }

        return max;
    }

    /**
     * Finds the minimum value in the stream.
     *
     * @param stream
     *     The stream to consume to find the minimum value. Must not be null.
     * @param comparator
     *     The comparator to use to compare items in the stream. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return The minimum value, or an empty value if the stream is empty or if the minimum is null.
     *
     * @throws IOStreamReadException
     *     If there was a problem with reading the stream.
     * @throws IOStreamCloseException
     *     If there was a problem with closing the stream.
     */
    public static <T> Optional<T> min(final IOStream<T> stream, Comparator<T> comparator)
        throws IOStreamReadException, IOStreamCloseException
    {
        return max(stream, comparator.reversed());
    }

    /**
     * Registers a function to observe values as they are consumed.
     * <p>
     * The observer will be closed when the returned stream is closed.
     *
     * @param stream
     *     The stream to observe. Must not be null.
     * @param observer
     *     The function which will observe the values. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return A view of the stream which will call the observer function.
     */
    public static <T> IOStream<T> observe(final IOStream<T> stream, final IOStreamConsumer<? super T> observer)
    {
        Objects.requireNonNull(stream, "The stream cannot be null.");
        Objects.requireNonNull(observer, "The observer cannot be null.");
        return new IOStream<T>()
        {
            @Override
            public void close() throws IOStreamCloseException
            {
                try (
                    final IOStream<T> autoCloseStream = stream;
                    final IOStreamConsumer<? super T> autoCloseObserver = observer
                )
                {
                    // Auto close resources
                }
                catch (RuntimeException ex)
                {
                    throw ex;
                }
                catch (Exception ex)
                {
                    throw new IOStreamCloseException(ex);
                }
            }

            @Override
            public boolean hasNext() throws IOStreamReadException
            {
                return stream.hasNext();
            }

            @Override
            public T next() throws IOStreamReadException
            {
                final T next = stream.next();
                try
                {
                    observer.accept(next);
                }
                catch (RuntimeException ex)
                {
                    throw ex;
                }
                catch (Exception ex)
                {
                    throw new IOStreamReadException(ex);
                }
                return next;
            }
        };
    }

    /**
     * Creates a peekable view of a stream.
     * <p>
     * Peeking at the items in the stream will cause them to be buffered. The buffer will not be cleared until the
     * stream moves beyond the items in the buffer, or until the stream is closed.
     *
     * @param stream
     *     The stream to peek at. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return A peekable view of the stream.
     */
    public static <T> PeekableIOStream<T> peekable(final IOStream<T> stream)
    {
        Objects.requireNonNull(stream, "The stream cannot be null.");
        if (stream instanceof PeekableIOStream<?>)
        {
            return (PeekableIOStream<T>) stream;
        }
        return new PeekableIOStream<T>()
        {
            private final LinkedList<T> buffer = new LinkedList<>();

            @Override
            public void close() throws IOStreamCloseException
            {
                try (final IOStream<T> autoCloseStream = stream)
                {
                    buffer.clear();
                }
            }

            @Override
            public boolean hasNext() throws IOStreamReadException
            {
                return buffer.size() > 0 || stream.hasNext();
            }

            @Override
            public T next() throws IOStreamReadException
            {
                if (buffer.size() > 0)
                {
                    return buffer.removeFirst();
                }
                else
                {
                    buffer.clear();
                    return stream.next();
                }
            }

            @Override
            public Iterable<T> peek(int n) throws IOStreamReadException
            {
                int extra = n - buffer.size();
                for (int i = 0; i < extra && stream.hasNext(); i++)
                {
                    buffer.add(stream.next());
                }
                return buffer.subList(0, Integer.min(n, buffer.size()));
            }

            @Override
            public PeekableIOStream<T> peekable()
            {
                return this;
            }
        };
    }

    /**
     * Consumes the stream, reducing all of its values into a single result.
     *
     * @param stream
     *     The stream to reduce. Must not be null.
     * @param reducer
     *     A function to read the items and produce the result. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
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
    public static <R, T> R reduce(final IOStream<T> stream, final IOStreamTransform<? super IOStream<T>, R> reducer)
        throws IOStreamReadException, IOStreamCloseException
    {
        try (
            final IOStream<T> autoCloseStream = stream;
            final IOStreamTransform<? super IOStream<T>, R> autoCloseReducer = reducer
        )
        {
            try
            {
                return reducer.apply(stream);
            }
            catch (RuntimeException ex)
            {
                throw ex;
            }
            catch (Exception ex)
            {
                throw new IOStreamReadException(ex);
            }
        }
        catch (final RuntimeException ex)
        {
            throw ex;
        }
        catch (final Exception ex)
        {
            throw new IOStreamCloseException(ex);
        }
    }

    /**
     * Creates a stream which repeatedly takes values from a supplier.
     *
     * @param supplier
     *     The supplier to take values from. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return An infinite stream.
     */
    public static <T> IOStream<T> repeat(final IOStreamSupplier<? extends T> supplier)
    {
        if (supplier == null)
        {
            throw new NullPointerException("The supplier must be non-null.");
        }
        return new AbstractIOStream<T>()
        {
            private volatile boolean closed = false;

            @Override
            public void end() throws Exception
            {
                try (final IOStreamSupplier<? extends T> autoCloseSupplier = supplier)
                {
                    // Auto close resources
                }
                finally
                {
                    closed = true;
                }
            }

            @Override
            public T find() throws Exception
            {
                if (closed)
                {
                    return terminate();
                }
                return supplier.get();
            }
        };
    }

    /**
     * Creates a stream which contains only a single item.
     *
     * @param item
     *     The single item.
     * @param <T>
     *     The type of the item.
     *
     * @return A stream containing only the given item.
     */
    public static <T> IOStream<T> singleton(final T item)
    {
        return new IOStream<T>()
        {
            private boolean hasNext = true;

            @Override
            public void close()
            {
            }

            @Override
            public boolean hasNext()
            {
                return hasNext;
            }

            @Override
            public T next()
            {
                if (hasNext)
                {
                    hasNext = false;
                    return item;
                }
                throw new NoSuchElementException("There is no next item in the stream.");
            }
        };
    }

    /**
     * Filters a stream by skipping items that match a predicate.
     *
     * @param stream
     *     The stream to filter. Must not be null.
     * @param predicate
     *     A predicate to determine whether to skip an item.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return A view of the stream, containing only items that are not matched by the predicate.
     */
    public static <T> IOStream<T> skip(
        final IOStream<? extends T> stream,
        final IOStreamPredicate<? super T> predicate
    )
    {
        return filter(
            stream,
            IOStreamFilters.fromPredicate(predicate)
                .invert()
        );
    }

    /**
     * Splits a stream into groups of items.
     * <p>
     * This is the dual of {@link #group(IOStream, IOStreamBiPredicate)}.
     *
     * @param stream
     *     The stream to split. Must not be null.
     * @param predicate
     *     A predicate to determine if two items should be in different groups.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return A view of the stream as a stream of groups.
     */
    public static <T> IOStream<? extends IOStream<T>> split(
        final IOStream<T> stream,
        final IOStreamBiPredicate<? super T, ? super T> predicate
    )
    {
        return stream.group(predicate.invert());
    }

    /**
     * Consumes the stream by collecting all of the items into an array.
     *
     * @param stream
     *     The stream to consume. Must not be null.
     * @param supplier
     *     A function to provide the array. Use a reference to an array constructor (e.g. {@code String[]::new}) to
     *     create a new array. Must not be null
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return An array containing the items from the stream.
     *
     * @throws IOStreamReadException
     *     If there was a problem in reading the stream.
     * @throws IOStreamCloseException
     *     If there was a problem in closing the stream.
     */
    public static <T> T[] toArray(final IOStream<T> stream, final IntFunction<T[]> supplier)
        throws IOStreamReadException, IOStreamCloseException
    {
        Objects.requireNonNull(stream, "The stream cannot be null.");
        final List<T> list = new ArrayList<>();
        stream.consume(list::add);
        return list.toArray(supplier.apply(list.size()));
    }

    /**
     * Consumes the stream by collecting all of the items into a list.
     *
     * @param stream
     *     The stream to consume. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return A list containing the items from the stream.
     *
     * @throws IOStreamReadException
     *     If there was a problem in reading the stream.
     * @throws IOStreamCloseException
     *     If there was a problem in closing the stream.
     */
    public static <T> List<T> toList(final IOStream<T> stream) throws IOStreamReadException, IOStreamCloseException
    {
        Objects.requireNonNull("The stream cannot be null.");
        final List<T> list = new ArrayList<>();
        stream.consume(list::add);
        return list;
    }

    /**
     * Consumes the stream by collecting all of the items into a set.
     *
     * @param stream
     *     The stream to consume. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return A set containing the unique items from the stream.
     *
     * @throws IOStreamReadException
     *     If there was a problem in reading the stream.
     * @throws IOStreamCloseException
     *     If there was a problem in closing the stream.
     */
    public static <T> Set<T> toSet(final IOStream<T> stream)
        throws IOStreamReadException, IOStreamCloseException
    {
        Objects.requireNonNull("The stream cannot be null.");
        final Set<T> set = new HashSet<>();
        stream.consume(set::add);
        return set;
    }

    /**
     * Terminates the stream when one of the items matches.
     * <p>
     * The matching item will not be kept.
     *
     * @param stream
     *     The stream to filter. Must not be null.
     * @param predicate
     *     The predicate to test each item. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return A view of the stream which may terminate early.
     */
    public static <T> IOStream<T> until(final IOStream<T> stream, final IOStreamPredicate<? super T> predicate)
    {
        return stream.filter(IOStreamFilters.keepUntil(predicate));
    }

    private IOStreams() throws InstantiationException
    {
        throw new InstantiationException("This class cannot be instantiated.");
    }
}
