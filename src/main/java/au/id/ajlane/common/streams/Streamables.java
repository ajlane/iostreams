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

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Utilities for working with instances of {@link Streamable}.
 */
public final class Streamables
{
    private static final Streamable<?> EMPTY = new Streamable<Object>()
    {
        @Override
        public boolean equals(final Object obj)
        {
            return obj.getClass().equals(this.getClass()) && obj == this;
        }

        @Override
        public int hashCode()
        {
            return 1;
        }

        @Override
        public Stream<Object> stream()
        {
            return Streams.empty();
        }

        @Override
        public String toString()
        {
            return "{}";
        }
    };

    /**
     * Changes the type of the items in the {@link Streamable} by lazily casting each item.
     *
     * @param streamable
     *         The {@link Streamable} to transform. Must not be {@code null}.
     * @param <T>
     *         The original type of the items.
     * @param <R>
     *         The new type of of the items.
     * @return A {@link Streamable} which is a transformed view of the given {@code Streamable}.
     * @see Streams#cast(Stream)
     */
    public static <T, R> Streamable<R> cast(final Streamable<T> streamable)
    {
        return new Streamable<R>()
        {
            @Override
            public Stream<R> stream()
            {
                return Streams.cast(streamable.stream());
            }

            @Override
            public String toString()
            {
                return streamable.toString();
            }
        };
    }

    /**
     * Concatenates the items in a series of {@link Streamable} instances.
     * <p/>
     * The series itself is provided by another {@link Streamable}.
     *
     * @param streamables
     *         The series of {@link Streamable} instances to concatenate. Must not be {@code null}.
     * @param <T>
     *         The type of the items in the {@link Streamable} instances.
     * @return A {@link Streamable} which is a concatenated view of the given {@code Streamable} instances.
     */
    public static <T> Streamable<T> concat(final Streamable<? extends Streamable<T>> streamables)
    {
        Objects.requireNonNull(streamables, "The streamable cannot be null.");
        return new Streamable<T>()
        {
            @Override
            public Stream<T> stream()
            {
                return new AbstractStream<T>()
                {
                    private Stream<? extends T> current = null;

                    private final Stream<? extends Streamable<T>> streamablesStream = streamables.stream();

                    @Override
                    protected void open() throws StreamReadException
                    {
                        if (streamablesStream.hasNext())
                        {
                            current = streamablesStream.next().stream();
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
                                    throw new StreamReadException(
                                            "Could not close one of the concatenated streams.",
                                            ex
                                    );
                                }
                                current = streamablesStream.hasNext() ? streamablesStream.next().stream() : null;
                            }
                        }
                        return super.find();
                    }

                    @Override
                    protected void end() throws StreamCloseException
                    {
                        if (current != null) current.close();
                    }
                };
            }
        };
    }

    /**
     * Concatenates the items in a series of {@link Streamable} instances.
     *
     * @param streamables
     *         The series of {@link Streamable} instances to concatenate. Must not be {@code null}.
     * @param <T>
     *         The type of the items in the {@link Streamable} instances.
     * @return A {@link Streamable} which is a concatenated view of the given {@code Streamable} instances.
     */
    @SafeVarargs
    public static <T> Streamable<T> concat(final Streamable<T>... streamables)
    {
        Objects.requireNonNull(streamables, "The array cannot be null.");
        if (streamables.length == 0) return Streamables.empty();
        return new Streamable<T>()
        {
            @Override
            public Stream<T> stream()
            {
                return new AbstractStream<T>()
                {
                    private int index = 0;
                    private Stream<? extends T> current = null;

                    @Override
                    protected void open()
                    {
                        if (index < streamables.length)
                        {
                            current = streamables[index].stream();
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
                                    throw new StreamReadException(
                                            "Could not close one of the concatenated streams.",
                                            ex
                                    );
                                }
                                index += 1;
                                current = index < streamables.length ? streamables[index].stream() : null;
                            }
                        }
                        return super.find();
                    }

                    @Override
                    protected void end() throws StreamCloseException
                    {
                        if (current != null) current.close();
                    }
                };
            }
        };
    }

    /**
     * Provides an empty {@link Streamable}.
     *
     * @param <T>
     *         The type of the items in the {@link Streamable}.
     * @return A {@link Streamable}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Streamable<T> empty()
    {
        return (Streamable<T>) Streamables.EMPTY;
    }

    /**
     * Filters certain items out of a {@link Streamable}.
     *
     * @param streamable
     *         The {@link Streamable} to filter. Must not be {@code null}.
     * @param filter
     *         The filter to apply. Must not be {@code null}.
     * @param <T>
     *         The type of the items in the {@link Streamable}.
     * @return A {@link Streamable} which is a filtered view of the given {@code Streamable}.
     */
    public static <T> Streamable<T> filter(final Streamable<T> streamable, final StreamFilter<? super T> filter)
    {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        Objects.requireNonNull(filter, "The filter cannot be null.");
        return new Streamable<T>()
        {
            @Override
            public Stream<T> stream()
            {
                return Streams.filter(streamable.stream(), filter);
            }
        };
    }

    /**
     * Converts a series of arrays into a single {@link Streamable}.
     * <p/>
     * The arrays are lazily concatenated in the order provided by the given {@code Streamable}. Must not be {@code
     * null}.
     *
     * @param streamable
     *         The {@link Streamable} which will provide the arrays. Must not be {@code null}.
     * @param <T>
     *         The type of the items in the arrays.
     * @return A {@link Streamable} which is a concatenated view of the arrays provided by the given {@code
     *         Streamable}.
     */
    public static <T> Streamable<T> flattenArrays(final Streamable<T[]> streamable)
    {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        return new Streamable<T>()
        {
            @Override
            public Stream<T> stream()
            {
                return Streams.flattenArrays(streamable.stream());
            }
        };
    }

    /**
     * Converts a series of {@link Iterable} instances into a single the {@link Streamable}.
     * <p/>
     * The {@code Iterable} instances are lazily concatenated in the order provided by the given {@code Streamable}.
     *
     * @param streamable
     *         The {@link Streamable} which will provide the {@code Iterable} instances. Must not be {@code null}.
     * @param <T>
     *         The type of the items in the {@link Iterable} instances.
     * @return A {@link Streamable} which is a concatenated view of the {@link Iterable} instances provided by the given
     *         {@code Streamable}.
     */
    public static <T> Streamable<T> flattenIterables(final Streamable<? extends Iterable<? extends T>> streamable)
    {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        return new Streamable<T>()
        {
            @Override
            public Stream<T> stream()
            {
                return Streams.flattenIterables(streamable.stream());
            }
        };
    }

    /**
     * Converts a series of {@link Streamable} instances into a single {@link Streamable}.
     * <p/>
     * The {@link Streamable} instances are lazily concatenated in the order provided by the given {@code Streamable}.
     * Must not be {@code null}.
     *
     * @param streamable
     *         The {@link Streamable} which will provide the series of {@code Streamable} instances. Must not be {@code
     *         null}.
     * @param <T>
     *         The type of the items in the {@link Streamable} instances.
     * @return A {@link Streamable} which is a concatenated view of the {@code Streamable} instances provided by the
     *         given {@code Streamable}.
     */
    public static <T> Streamable<T> flattenStreamables(final Streamable<? extends Streamable<T>> streamable)
    {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        return new Streamable<T>()
        {
            @Override
            public Stream<T> stream()
            {
                return Streams.flatten(
                        streamable.stream(), new AbstractStreamTransform<Streamable<T>, Stream<T>>()
                {
                    @Override
                    protected Stream<T> transform(final Streamable<T> item)
                    {
                        return item.stream();
                    }
                }
                );
            }
        };
    }

    /**
     * Creates a new {@link Streamable} containing the values of an array.
     *
     * @param values
     *         The array which will underlie the {@link Streamable}. Must not be {@code null}.
     * @param <T>
     *         The type of the items in the array.
     * @return A {@link Streamable} which is a view of the given array.
     */
    @SafeVarargs
    public static <T> Streamable<T> fromArray(final T... values)
    {
        Objects.requireNonNull(values, "The array cannot be null.");
        if (values.length == 0) return Streamables.empty();
        return new Streamable<T>()
        {
            @Override
            public Stream<T> stream()
            {
                return Streams.fromArray(values);
            }
        };
    }

    /**
     * Creates a new {@link Streamable} containing the values of an {@link Iterable}.
     *
     * @param iterable
     *         The {@link Iterable} which will underlie the {@link Streamable}. Must not be {@code null}.
     * @param <T>
     *         The type of the items in the {@link Iterable}.
     * @return A {@link Streamable} which is a view of the given {@link Iterable}.
     */
    public static <T> Streamable<T> fromIterable(final Iterable<T> iterable)
    {
        Objects.requireNonNull(iterable, "The iterable cannot be null.");
        return new Streamable<T>()
        {
            @Override
            public Stream<T> stream()
            {
                return Streams.fromIterator(iterable.iterator());
            }
        };
    }

    /**
     * Creates a new {@link Streamable} containing only a single item.
     *
     * @param item
     *         The item which will be provided by the {@link Streamable}.
     * @param <T>
     *         The type of the item.
     * @return A {@link Streamable} which only provides the given value.
     */
    public static <T> Streamable<T> singleton(final T item)
    {
        return new Streamable<T>()
        {
            @Override
            public Stream<T> stream()
            {
                return Streams.singleton(item);
            }
        };
    }

    /**
     * Reads a {@link Streamable} and copies its values to an array.
     *
     * @param streamable
     *         The {@link Streamable} to copy.
     * @param <T>
     *         The type of the items in the array.
     * @return An array.
     * @throws StreamException
     *         If there was any problem in reading or closing the {@link Streamable}'s {@link Stream}.
     */
    public static <T> T[] toArray(final Streamable<T> streamable) throws StreamException
    {
        return Streams.toArray(streamable.stream());
    }

    /**
     * Reads a {@link Streamable} and copies its values to a {@link List}.
     *
     * @param streamable
     *         The {@link Streamable} to copy.
     * @param <T>
     *         The type of the items in the {@link List}
     * @return An immutable {@link List}.
     * @throws StreamException
     *         If there was any problem in reading or closing the {@link Streamable}'s {@link Stream}.
     */
    public static <T> List<? extends T> toList(final Streamable<T> streamable) throws StreamException
    {
        return Streams.toList(streamable.stream());
    }

    /**
     * Reads a {@link Streamable} and copies its values to a {2link Set}.
     * <p/>
     * Duplicate values in the {@code Streamable} will be discarded.
     *
     * @param streamable
     *         The {@link Streamable} to copy.
     * @param <T>
     *         The type of the items in the {@link Set}.
     * @return An immutable {@link Set}.
     * @throws StreamException
     *         If there was any problem in reading or closing the {@link Streamable}'s {@link Stream}.
     */
    public static <T> Set<T> toSet(final Streamable<T> streamable) throws StreamException
    {
        return Streams.toSet(streamable.stream());
    }

    /**
     * Transforms the items in a {@link Streamable}.
     * <p/>
     * Transforming the items in a {@link Streamable} does not change the number of items it provides. To remove items,
     * use {@link #filter(Streamable, StreamFilter)}.
     *
     * @param streamable
     *         The {@link Streamable} to transform.
     * @param transform
     *         The transform to apply to each item in the {@link Streamable}.
     * @param <T>
     *         The type of the items in the original {@link Streamable}.
     * @param <R>
     *         The type of the items in the new {@link Streamable}.
     * @return A {@link Streamable} which is a transformed view of the given {@code Streamable}.
     */
    public static <T, R> Streamable<R> transform(final Streamable<T> streamable, final StreamTransform<? super T, ? extends R> transform)
    {
        return new Streamable<R>()
        {
            @Override
            public Stream<R> stream()
            {
                return Streams.transform(streamable.stream(), transform);
            }
        };
    }

    private Streamables() throws InstantiationException
    {
        throw new InstantiationException("This class cannot be instantiated.");
    }
}
