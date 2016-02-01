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

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Utilities for working with instances of {@link IOStreamable}.
 */
public final class IOStreamables
{
    private static final IOStreamable<?> EMPTY = new IOStreamable<Object>()
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
        public IOStream<Object> stream()
        {
            return IOStreams.empty();
        }

        @Override
        public String toString()
        {
            return "{}";
        }
    };

    /**
     * Changes the type of the items in the {@link IOStreamable} by lazily casting each item.
     *
     * @param streamable
     *         The {@link IOStreamable} to transform. Must not be {@code null}.
     * @param <T>
     *         The original type of the items.
     * @param <R>
     *         The new type of of the items.
     * @return A {@link IOStreamable} which is a transformed view of the given {@code IOStreamable}.
     * @see IOStreams#cast(IOStream)
     */
    public static <T, R> IOStreamable<R> cast(final IOStreamable<T> streamable)
    {
        return new IOStreamable<R>()
        {
            @Override
            public IOStream<R> stream()
            {
                return IOStreams.cast(streamable.stream());
            }

            @Override
            public String toString()
            {
                return streamable.toString();
            }
        };
    }

    /**
     * Concatenates the items in a series of {@link IOStreamable} instances.
     * <p>
     * The series itself is provided by another {@link IOStreamable}.
     *
     * @param streamables
     *         The series of {@link IOStreamable} instances to concatenate. Must not be {@code null}.
     * @param <T>
     *         The type of the items in the {@link IOStreamable} instances.
     * @return A {@link IOStreamable} which is a concatenated view of the given {@code IOStreamable} instances.
     */
    public static <T> IOStreamable<T> concat(final IOStreamable<? extends IOStreamable<T>> streamables)
    {
        Objects.requireNonNull(streamables, "The streamable cannot be null.");
        return new IOStreamable<T>()
        {
            @Override
            public IOStream<T> stream()
            {
                return new AbstractIOStream<T>()
                {
                    private IOStream<? extends T> current = null;

                    private final IOStream<? extends IOStreamable<T>> streamablesStream = streamables.stream();

                    @Override
                    protected void open() throws IOStreamReadException
                    {
                        if (streamablesStream.hasNext())
                        {
                            current = streamablesStream.next().stream();
                        }
                    }

                    @Override
                    protected T find() throws IOStreamReadException
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
                                catch (final IOStreamCloseException ex)
                                {
                                    throw new IOStreamReadException(
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
                    protected void end() throws IOStreamCloseException
                    {
                        if (current != null) current.close();
                    }
                };
            }
        };
    }

    /**
     * Concatenates the items in a series of {@link IOStreamable} instances.
     *
     * @param streamables
     *         The series of {@link IOStreamable} instances to concatenate. Must not be {@code null}.
     * @param <T>
     *         The type of the items in the {@link IOStreamable} instances.
     * @return A {@link IOStreamable} which is a concatenated view of the given {@code IOStreamable} instances.
     */
    @SafeVarargs
    public static <T> IOStreamable<T> concat(final IOStreamable<T>... streamables)
    {
        Objects.requireNonNull(streamables, "The array cannot be null.");
        if (streamables.length == 0) return IOStreamables.empty();
        return new IOStreamable<T>()
        {
            @Override
            public IOStream<T> stream()
            {
                return new AbstractIOStream<T>()
                {
                    private int index = 0;
                    private IOStream<? extends T> current = null;

                    @Override
                    protected void open()
                    {
                        if (index < streamables.length)
                        {
                            current = streamables[index].stream();
                        }
                    }

                    @Override
                    protected T find() throws IOStreamReadException
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
                                catch (final IOStreamCloseException ex)
                                {
                                    throw new IOStreamReadException(
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
                    protected void end() throws IOStreamCloseException
                    {
                        if (current != null) current.close();
                    }
                };
            }
        };
    }

    /**
     * Provides an empty {@link IOStreamable}.
     *
     * @param <T>
     *         The type of the items in the {@link IOStreamable}.
     * @return A {@link IOStreamable}.
     */
    @SuppressWarnings("unchecked")
    public static <T> IOStreamable<T> empty()
    {
        return (IOStreamable<T>) IOStreamables.EMPTY;
    }

    /**
     * Filters certain items out of a {@link IOStreamable}.
     *
     * @param streamable
     *         The {@link IOStreamable} to filter. Must not be {@code null}.
     * @param filter
     *         The filter to apply. Must not be {@code null}.
     * @param <T>
     *         The type of the items in the {@link IOStreamable}.
     * @return A {@link IOStreamable} which is a filtered view of the given {@code IOStreamable}.
     */
    public static <T> IOStreamable<T> filter(final IOStreamable<T> streamable, final IOStreamFilter<? super T> filter)
    {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        Objects.requireNonNull(filter, "The filter cannot be null.");
        return () -> IOStreams.filter(streamable.stream(), filter);
    }

    /**
     * Converts a series of arrays into a single {@link IOStreamable}.
     * <p>
     * The arrays are lazily concatenated in the order provided by the given {@code IOStreamable}. Must not be {@code
     * null}.
     *
     * @param streamable
     *         The {@link IOStreamable} which will provide the arrays. Must not be {@code null}.
     * @param <T>
     *         The type of the items in the arrays.
     * @return A {@link IOStreamable} which is a concatenated view of the arrays provided by the given {@code
     *         Streamable}.
     */
    public static <T> IOStreamable<T> flattenArrays(final IOStreamable<T[]> streamable)
    {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        return () -> IOStreams.flattenArrays(streamable.stream());
    }

    /**
     * Converts a series of {@link Iterable} instances into a single the {@link IOStreamable}.
     * <p>
     * The {@code Iterable} instances are lazily concatenated in the order provided by the given {@code IOStreamable}.
     *
     * @param streamable
     *         The {@link IOStreamable} which will provide the {@code Iterable} instances. Must not be {@code null}.
     * @param <T>
     *         The type of the items in the {@link Iterable} instances.
     * @return A {@link IOStreamable} which is a concatenated view of the {@link Iterable} instances provided by the given
     *         {@code IOStreamable}.
     */
    public static <T> IOStreamable<T> flattenIterables(final IOStreamable<? extends Iterable<? extends T>> streamable)
    {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        return () -> IOStreams.flattenIterables(streamable.stream());
    }

    /**
     * Converts a series of {@link IOStreamable} instances into a single {@link IOStreamable}.
     * <p>
     * The {@link IOStreamable} instances are lazily concatenated in the order provided by the given {@code IOStreamable}.
     * Must not be {@code null}.
     *
     * @param streamable
     *         The {@link IOStreamable} which will provide the series of {@code IOStreamable} instances. Must not be {@code
     *         null}.
     * @param <T>
     *         The type of the items in the {@link IOStreamable} instances.
     * @return A {@link IOStreamable} which is a concatenated view of the {@code IOStreamable} instances provided by the
     *         given {@code IOStreamable}.
     */
    public static <T> IOStreamable<T> flattenStreamables(final IOStreamable<? extends IOStreamable<T>> streamable)
    {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        return () -> IOStreams.flatMap(streamable.stream(), IOStreamable::stream);
    }

    /**
     * Creates a new {@link IOStreamable} containing the values of an array.
     *
     * @param values
     *         The array which will underlie the {@link IOStreamable}. Must not be {@code null}.
     * @param <T>
     *         The type of the items in the array.
     * @return A {@link IOStreamable} which is a view of the given array.
     */
    @SafeVarargs
    public static <T> IOStreamable<T> fromArray(final T... values)
    {
        Objects.requireNonNull(values, "The array cannot be null.");
        if (values.length == 0) return IOStreamables.empty();
        return () -> IOStreams.fromArray(values);
    }

    /**
     * Creates a new {@link IOStreamable} containing the values of an {@link Iterable}.
     *
     * @param iterable
     *         The {@link Iterable} which will underlie the {@link IOStreamable}. Must not be {@code null}.
     * @param <T>
     *         The type of the items in the {@link Iterable}.
     * @return A {@link IOStreamable} which is a view of the given {@link Iterable}.
     */
    public static <T> IOStreamable<T> fromIterable(final Iterable<T> iterable)
    {
        Objects.requireNonNull(iterable, "The iterable cannot be null.");
        return () -> IOStreams.fromIterator(iterable.iterator());
    }

    /**
     * Creates a new {@link IOStreamable} containing only a single item.
     *
     * @param item
     *         The item which will be provided by the {@link IOStreamable}.
     * @param <T>
     *         The type of the item.
     * @return A {@link IOStreamable} which only provides the given value.
     */
    public static <T> IOStreamable<T> singleton(final T item)
    {
        return () -> IOStreams.singleton(item);
    }

    /**
     * Reads a {@link IOStreamable} and copies its values to an array.
     *
     * @param streamable
     *         The {@link IOStreamable} to copy.
     * @param <T>
     *         The type of the items in the array.
     * @return An array.
     * @throws IOStreamException
     *         If there was any problem in reading or closing the {@link IOStreamable}'s {@link IOStream}.
     */
    public static <T> T[] toArray(final IOStreamable<T> streamable) throws IOStreamException
    {
        return IOStreams.toArray(streamable.stream());
    }

    /**
     * Reads a {@link IOStreamable} and copies its values to a {@link List}.
     *
     * @param streamable
     *         The {@link IOStreamable} to copy.
     * @param <T>
     *         The type of the items in the {@link List}
     * @return An immutable {@link List}.
     * @throws IOStreamException
     *         If there was any problem in reading or closing the {@link IOStreamable}'s {@link IOStream}.
     */
    public static <T> List<? extends T> toList(final IOStreamable<T> streamable) throws IOStreamException
    {
        return IOStreams.toList(streamable.stream());
    }

    /**
     * Reads a {@link IOStreamable} and copies its values to a {2link Set}.
     * <p>
     * Duplicate values in the {@code IOStreamable} will be discarded.
     *
     * @param streamable
     *         The {@link IOStreamable} to copy.
     * @param <T>
     *         The type of the items in the {@link Set}.
     * @return An immutable {@link Set}.
     * @throws IOStreamException
     *         If there was any problem in reading or closing the {@link IOStreamable}'s {@link IOStream}.
     */
    public static <T> Set<T> toSet(final IOStreamable<T> streamable) throws IOStreamException
    {
        return IOStreams.toSet(streamable.stream());
    }

    /**
     * Transforms the items in a {@link IOStreamable}.
     * <p>
     * Transforming the items in a {@link IOStreamable} does not change the number of items it provides. To remove items,
     * use {@link #filter(IOStreamable, IOStreamFilter)}.
     *
     * @param streamable
     *         The {@link IOStreamable} to map.
     * @param transform
     *         The map to apply to each item in the {@link IOStreamable}.
     * @param <T>
     *         The type of the items in the original {@link IOStreamable}.
     * @param <R>
     *         The type of the items in the new {@link IOStreamable}.
     * @return A {@link IOStreamable} which is a transformed view of the given {@code IOStreamable}.
     */
    public static <T, R> IOStreamable<R> transform(final IOStreamable<T> streamable, final IOStreamTransform<? super T, ? extends R> transform)
    {
        return () -> IOStreams.map(streamable.stream(), transform);
    }

    private IOStreamables() throws InstantiationException
    {
        throw new InstantiationException("This class cannot be instantiated.");
    }
}
