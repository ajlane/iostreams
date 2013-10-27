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

package au.id.ajlane.common.streams;

import java.util.*;

public final class Streams
{

    private static final Stream<?> EMPTY = new Stream<Object>()
    {
        @Override
        public void close()
        {
        }

        @Override
        public boolean hasNext()
        {
            return false;
        }

        @Override
        public Object next()
        {
            throw new NoSuchElementException("There is no next item in the stream.");
        }
    };

    public static <T, TCollection extends Collection<T>> TCollection addToCollection(final TCollection collection, final Stream<T> stream)
            throws StreamException
    {
        Objects.requireNonNull(collection, "The collection cannot be null.");
        Objects.requireNonNull(stream, "The stream cannot be null.");
        try
        {
            while (stream.hasNext())
            {
                collection.add(stream.next());
            }
        }
        finally
        {
            stream.close();
        }
        return collection;
    }

    public static <T, R> Stream<R> cast(final Stream<T> stream)
    {
        return Streams.transform(
                stream, new AbstractStreamTransform<T, R>()
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

    public static <T> Stream<T> concat(final Iterable<? extends Stream<? extends T>> streams)
    {
        return Streams.concat(Streams.fromIterable(streams));
    }

    public static <T> Stream<T> concat(final Iterator<? extends Stream<? extends T>> streams)
    {
        return Streams.concat(Streams.fromIterator(streams));
    }

    public static <T> Stream<T> concat(final Streamable<? extends Stream<? extends T>> streams)
    {
        Objects.requireNonNull(streams, "The streamable cannot be null.");
        return Streams.concat(streams.stream());
    }

    public static <T> Stream<T> concat(final Stream<? extends Stream<? extends T>> streams)
    {
        Objects.requireNonNull(streams, "The stream of streams cannot be null.");
        return new AbstractStream<T>()
        {
            private Stream<? extends T> current = null;

            @Override
            protected void open() throws StreamReadException
            {
                if (streams.hasNext())
                {
                    current = Objects.requireNonNull(streams.next(), "The first concatenated stream was null");
                }
            }

            @Override
            protected T find() throws StreamReadException
            {
                while (current != null)
                {
                    if (current.hasNext())
                    {
                        return current.next();
                    }
                    else
                    {
                        try
                        {
                            current.close();
                        }
                        catch (final StreamCloseException ex)
                        {
                            throw new StreamReadException("Could not close one of the concatenated streams.", ex);
                        }
                        current = streams.hasNext() ? Objects.requireNonNull(
                                streams.next(),
                                "One of the concatenated streams was null."
                        ) : null;
                    }
                }
                return super.find();
            }

            @Override
            protected void end() throws StreamCloseException
            {
                if (current != null) current.close();
                streams.close();
            }
        };
    }

    @SafeVarargs
    public static <T> Stream<T> concat(final Stream<? extends T>... streams)
    {
        Objects.requireNonNull(streams);

        return new AbstractStream<T>()
        {
            private int index = 0;

            @Override
            protected T find() throws StreamReadException
            {
                while (index < streams.length)
                {
                    final Stream<? extends T> stream = Objects.requireNonNull(
                            streams[index],
                            "One of the concatenated streams was null."
                    );
                    if (stream.hasNext())
                    {
                        return stream.next();
                    }
                    else
                    {
                        try
                        {
                            stream.close();
                        }
                        catch (final StreamCloseException ex)
                        {
                            throw new StreamReadException("Could not close one of the concatenated streams.", ex);
                        }
                        index += 1;
                    }
                }
                return super.find();
            }

            @Override
            protected void end() throws StreamCloseException
            {
                if (index < streams.length)
                {
                    if (streams[index] != null) streams[index].close();
                }
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> empty()
    {
        return (Stream<T>) Streams.EMPTY;
    }

    public static <T> Stream<T> filter(final Stream<? extends T> stream, final StreamFilter<? super T> filter)
    {
        Objects.requireNonNull(stream, "The stream cannot be null.");
        Objects.requireNonNull(filter, "The filter cannot be null.");
        return new AbstractStream<T>()
        {
            @Override
            protected void end() throws StreamCloseException
            {
                filter.close();
            }

            @SuppressWarnings("BooleanVariableAlwaysNegated")
            private boolean terminate = false;

            @Override
            protected T find() throws StreamReadException
            {
                while (!terminate && stream.hasNext())
                {
                    if (Thread.interrupted())
                    {
                        throw new StreamReadException(
                                "The thread was interrupted while filtering the stream.",
                                new InterruptedException("The thread was interrupted.")
                        );
                    }
                    final T next = stream.next();
                    switch (filter.apply(next))
                    {
                        case KEEP_AND_CONTINUE:
                            return next;
                        case SKIP_AND_CONTINUE:
                            continue;
                        case KEEP_AND_TERMINATE:
                            this.terminate = true;
                            return next;
                        case SKIP_AND_TERMINATE:
                        default:
                            break;
                    }
                }
                return super.find();
            }
        };
    }

    public static <T, R> Stream<R> flatten(final Stream<? extends T> stream, final StreamTransform<? super T, ? extends Stream<R>> transform)
    {
        return Streams.concat(Streams.transform(stream, transform));
    }

    public static <T> Stream<T> flattenArrays(final Stream<? extends T[]> stream)
    {
        return Streams.flatten(
                stream, new AbstractStreamTransform<T[], Stream<T>>()
        {
            @SafeVarargs
            @Override
            public final Stream<T> transform(final T... item)
            {
                return Streams.fromArray(item);
            }
        }
        );
    }

    public static <T> Stream<T> flattenIterables(final Stream<? extends Iterable<? extends T>> stream)
    {
        return Streams.flatten(
                stream, new AbstractStreamTransform<Iterable<? extends T>, Stream<T>>()
        {
            @Override
            public Stream<T> transform(final Iterable<? extends T> item)
            {
                return Streams.fromIterable(item);
            }
        }
        );
    }

    public static <T> Stream<T> flattenIterators(final Stream<? extends Iterator<? extends T>> stream)
    {
        return Streams.flatten(
                stream, new AbstractStreamTransform<Iterator<? extends T>, Stream<T>>()
        {
            @Override
            public Stream<T> transform(final Iterator<? extends T> item)
            {
                return Streams.fromIterator(item);
            }
        }
        );
    }

    public static <T> Stream<T> flattenStreams(final Stream<? extends Stream<? extends T>> stream)
    {
        return Streams.concat(stream);
    }

    @SafeVarargs
    public static <T> Stream<T> fromArray(final T... values)
    {
        Objects.requireNonNull(values, "The array cannot be null. Use an empty array instead.");
        return new Stream<T>()
        {
            private int index = 0;

            @Override
            public void close()
            {
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

    public static <T> Stream<T> fromIterable(final Iterable<? extends T> iterable)
    {
        Objects.requireNonNull(iterable, "The iterable cannot be null.");
        return Streams.fromIterator(iterable.iterator());
    }

    public static <T> Stream<T> fromIterator(final Iterator<? extends T> iterator)
    {
        Objects.requireNonNull(iterator, "The iterator cannot be null.");
        return new Stream<T>()
        {
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

            @Override
            public void close() throws StreamCloseException
            {
                if (iterator instanceof AutoCloseable)
                {
                    try
                    {
                        ((AutoCloseable) iterator).close();
                    }
                    catch (final Exception ex)
                    {
                        throw new StreamCloseException("Could not close underlying iterator.", ex);
                    }
                }
            }
        };
    }

    public static <T> Stream<T> singleton(final T item)
    {
        return new Stream<T>()
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

    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(final Stream<T> stream) throws StreamException
    {
        Objects.requireNonNull(stream, "The stream cannot be null.");
        final List<T> list = new ArrayList<>();
        return (T[]) Streams.addToCollection(list, stream).toArray();
    }

    public static <T> List<T> toList(final Stream<T> stream) throws StreamException
    {
        Objects.requireNonNull("The stream cannot be null.");
        final List<T> list = new ArrayList<>();
        return Streams.addToCollection(list, stream);
    }

    public static <T> Set<T> toSet(final Stream<T> stream) throws StreamException
    {
        Objects.requireNonNull("The stream cannot be null.");
        final Set<T> set = new HashSet<>();
        return Streams.addToCollection(set, stream);
    }

    public static <T, R> Stream<R> transform(final Stream<? extends T> stream, final StreamTransform<? super T, ? extends R> transform)
    {
        Objects.requireNonNull(stream, "The stream cannot be null.");
        Objects.requireNonNull(transform, "The transform cannot be null.");
        return new Stream<R>()
        {
            @Override
            public void close() throws StreamCloseException
            {
                try
                {
                    transform.close();
                }
                finally
                {
                    stream.close();
                }
            }

            @Override
            public boolean hasNext() throws StreamReadException
            {
                return stream.hasNext();
            }

            @Override
            public R next() throws StreamReadException
            {
                return transform.apply(stream.next());
            }
        };
    }

    private Streams() throws InstantiationException
    {
        throw new InstantiationException("This class cannot be instantiated.");
    }
}
