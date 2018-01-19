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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Utilities for working with instances of streamable resource.
 * <p>
 * Where a function duplicates a capability that is on the streamable resource interface, prefer to use that version
 * instead - specific implementations may be more efficient.
 */
public final class IOStreamables
{
    private static final IOStreamable<?> EMPTY = new IOStreamable<Object>()
    {
        @Override
        public boolean equals(final Object obj)
        {
            return obj.getClass()
                .equals(this.getClass()) && obj == this;
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
     * Changes the type of the items in the streamable resource by lazily casting each item.
     *
     * @param streamable
     *     The streamable resource to transform. Must not be {@code null}.
     * @param <T>
     *     The original type of the items.
     * @param <R>
     *     The new type of of the items.
     *
     * @return An streamable resource which is a transformed view of the given streamable resource.
     *
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
     * Concatenates the items in a series of streamable resources.
     * <p>
     * The series itself is provided by another streamable resource.
     *
     * @param streamables
     *     The series of streamable resources to concatenate. Must not be {@code null}.
     * @param <T>
     *     The type of the items in the streamable resources.
     *
     * @return An streamable resource which is a concatenated view of the given streamable resource instances.
     */
    public static <T> IOStreamable<T> concat(final IOStreamable<? extends IOStreamable<? extends T>> streamables)
    {
        Objects.requireNonNull(streamables, "The streamable cannot be null.");
        return new IOStreamable<T>()
        {
            @Override
            public IOStream<T> stream()
            {
                return new AbstractIOStream<T>()
                {
                    private final IOStream<? extends IOStreamable<? extends T>> streamablesStream =
                        streamables.stream();
                    private IOStream<? extends T> current = null;

                    @Override
                    protected void end() throws Exception
                    {
                        if (current != null)
                        {
                            current.close();
                        }
                    }

                    @Override
                    protected T find() throws Exception
                    {
                        while (current != null)
                        {
                            if (current.hasNext())
                            {
                                return current.next();
                            }
                            else
                            {
                                current.close();
                                current = streamablesStream.hasNext() ?
                                    streamablesStream.next()
                                        .stream() :
                                    null;
                            }
                        }
                        return super.find();
                    }

                    @Override
                    protected void open() throws Exception
                    {
                        if (streamablesStream.hasNext())
                        {
                            current = streamablesStream.next()
                                .stream();
                        }
                    }
                };
            }
        };
    }

    /**
     * Concatenates the items provided by a series of streamable resources.
     *
     * @param streamables
     *     The series of streamable resources to concatenate. Must not be {@code null}.
     * @param <T>
     *     The type of the items in the streamable resources.
     *
     * @return An streamable resource which is a concatenated view of the given streamable resource instances.
     */
    @SafeVarargs
    public static <T> IOStreamable<T> concat(final IOStreamable<T>... streamables)
    {
        Objects.requireNonNull(streamables, "The array cannot be null.");
        if (streamables.length == 0)
        {
            return IOStreamables.empty();
        }
        return new IOStreamable<T>()
        {
            @Override
            public IOStream<T> stream()
            {
                return new AbstractIOStream<T>()
                {
                    private IOStream<? extends T> current = null;
                    private int index = 0;

                    @Override
                    protected void end() throws IOStreamCloseException, InterruptedException
                    {
                        if (current != null)
                        {
                            current.close();
                        }
                    }

                    @Override
                    protected T find() throws Exception
                    {
                        while (current != null)
                        {
                            if (current.hasNext())
                            {
                                return current.next();
                            }
                            else
                            {
                                current.close();
                                index += 1;
                                current = index < streamables.length ? streamables[index].stream() : null;
                            }
                        }
                        return super.find();
                    }

                    @Override
                    protected void open()
                    {
                        if (index < streamables.length)
                        {
                            current = streamables[index].stream();
                        }
                    }
                };
            }
        };
    }

    /**
     * Consumes the stream of a resource.
     *
     * @param streamable
     *     The streamable resource to consume.
     * @param consumer
     *     A consumer to receive the items in the stream.
     * @param <T>
     *     The type of the items in the streamable resource.
     *
     * @throws IOStreamReadException
     *     If there were any problems in reading the stream.
     * @throws IOStreamCloseException
     *     If there were any problems in closing the stream.
     * @throws InterruptedException
     *     If the thread was interrupted.
     */
    public static <T> void consume(
        final IOStreamable<T> streamable,
        final Supplier<? extends IOStreamConsumer<? super T>> consumer
    )
        throws IOStreamReadException, IOStreamCloseException, InterruptedException
    {
        IOStreams.consume(streamable.stream(), consumer.get());
    }

    /**
     * Consumes the stream of a resource, triggering any side-effects while discarding the items.
     *
     * @param streamable
     *     The streamable resource to consume.
     * @param <T>
     *     The type of the items in the streamable resource.
     *
     * @throws IOStreamReadException
     *     If there were any problems in reading the stream.
     * @throws IOStreamCloseException
     *     If there were any problems in closing the stream.
     * @throws InterruptedException
     *     If the thread was interrupted.
     */
    public static <T> void consume(final IOStreamable<T> streamable)
        throws IOStreamReadException, IOStreamCloseException, InterruptedException
    {
        IOStreams.consume(streamable.stream());
    }

    /**
     * Provides an empty streamable resource.
     *
     * @param <T>
     *     The type of the items in the streamable resource.
     *
     * @return An streamable resource.
     */
    @SuppressWarnings("unchecked")
    public static <T> IOStreamable<T> empty()
    {
        return (IOStreamable<T>) IOStreamables.EMPTY;
    }

    /**
     * Filters certain items out of a streamable resource.
     *
     * @param <T>
     *     The type of the items in the streamable resource.
     * @param streamable
     *     The streamable resource to filter. Must not be {@code null}.
     * @param filter
     *     The filter to apply. Must not be {@code null}.
     *
     * @return An streamable resource which is a filtered view of the given streamable resource.
     */
    public static <T> IOStreamable<T> filter(
        final IOStreamable<T> streamable,
        final Supplier<? extends IOStreamFilter<? super T>> filter
    )
    {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        Objects.requireNonNull(filter, "The filter cannot be null.");
        return () -> IOStreams.filter(streamable.stream(), filter.get());
    }

    /**
     * Converts a single streamable resource into a series of streamable resource instances and concatenates all of
     * their values.
     *
     * @param <T>
     *     The type of the items in the original streamable resource.
     * @param <R>
     *     The type of the items in the transformed streamable resource.
     * @param streamable
     *     The streamable resource which will be transformed.
     * @param transform
     *     The {@link IOStreamTransform} which will expand the original stream.
     *
     * @return An expanded streamable resource with the new type.
     */
    public static <T, R> IOStreamable<R> flatMap(
        final IOStreamable<T> streamable,
        final Supplier<? extends IOStreamTransform<? super T, ? extends IOStreamable<? extends R>>> transform
    )
    {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        Objects.requireNonNull(transform, "The transform cannot be null.");
        return concat(streamable.map(transform));
    }

    /**
     * Converts a series of arrays into a single streamable resource.
     * <p>
     * The arrays are lazily concatenated in the order provided by the given streamable resource. Must not be {@code
     * null}.
     *
     * @param streamable
     *     The streamable resource which will provide the arrays. Must not be {@code null}.
     * @param <T>
     *     The type of the items in the arrays.
     *
     * @return An streamable resource which is a concatenated view of the arrays provided by the given {@code
     * Streamable}.
     */
    public static <T> IOStreamable<T> flattenArrays(final IOStreamable<T[]> streamable)
    {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        return () -> IOStreams.flattenArrays(streamable.stream());
    }

    /**
     * Converts a series of {@link Iterable} instances into a single the streamable resource.
     * <p>
     * The {@code Iterable} instances are lazily concatenated in the order provided by the given streamable resource.
     *
     * @param streamable
     *     The streamable resource which will provide the {@code Iterable} instances. Must not be {@code null}.
     * @param <T>
     *     The type of the items in the {@link Iterable} instances.
     *
     * @return An streamable resource which is a concatenated view of the {@link Iterable} instances provided by the
     * given streamable resource.
     */
    public static <T> IOStreamable<T> flattenIterables(final IOStreamable<? extends Iterable<? extends T>> streamable)
    {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        return () -> IOStreams.flattenIterables(streamable.stream());
    }

    /**
     * Converts a series of streamable resources into a single streamable resource.
     * <p>
     * The streamable resources are lazily concatenated in the order provided by the given {@code IOStreamable}. Must
     * not be {@code null}.
     *
     * @param streamable
     *     The streamable resource which will provide the series of streamable resource instances. Must not be {@code
     *     null}.
     * @param <T>
     *     The type of the items in the streamable resources.
     *
     * @return An streamable resource which is a concatenated view of the streamable resource instances provided by the
     * given streamable resource.
     */
    public static <T> IOStreamable<T> flattenStreamables(final IOStreamable<? extends IOStreamable<T>> streamable)
    {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        return () -> streamable.stream()
            .flatMap(IOStreamable::stream);
    }

    /**
     * Creates a new streamable resource containing the values of an array.
     *
     * @param values
     *     The array which will underlie the streamable resource. Must not be {@code null}.
     * @param <T>
     *     The type of the items in the array.
     *
     * @return An streamable resource which is a view of the given array.
     */
    @SafeVarargs
    public static <T> IOStreamable<T> fromArray(final T... values)
    {
        Objects.requireNonNull(values, "The array cannot be null.");
        if (values.length == 0)
        {
            return IOStreamables.empty();
        }
        return () -> IOStreams.fromArray(values);
    }

    /**
     * Creates a new streamable resource containing the values of an {@link Iterable}.
     *
     * @param iterable
     *     The {@link Iterable} which will underlie the streamable resource. Must not be {@code null}.
     * @param <T>
     *     The type of the items in the {@link Iterable}.
     *
     * @return A streamable resource which is a view of the given {@link Iterable}.
     */
    public static <T> IOStreamable<T> fromIterable(final Iterable<T> iterable)
    {
        Objects.requireNonNull(iterable, "The iterable cannot be null.");
        return () -> IOStreams.fromIterator(iterable.iterator());
    }

    /**
     * Filters the items in a streamable resource to include only items which match the predicate.
     *
     * @param streamable
     *     The streamable resource to filter.
     * @param predicate
     *     A supplier to provide the predicate.
     * @param <T>
     *     The type of the items in the streamable resource.
     *
     * @return A filtered view of the streamable resource.
     */
    public static <T> IOStreamable<T> keep(
        final IOStreamable<T> streamable,
        final Supplier<? extends IOStreamPredicate<? super T>> predicate
    )
    {
        return () -> streamable.stream()
            .keep(predicate.get());
    }

    /**
     * Transforms the items in a streamable resource.
     * <p>
     * Transforming the items in a streamable resource does not change the number of items it provides. To remove items,
     * use {@link #filter(IOStreamable, Supplier)}.
     *
     * @param <T>
     *     The type of the items in the original streamable resource.
     * @param <R>
     *     The type of the items in the new streamable resource.
     * @param streamable
     *     The streamable resource to map.
     * @param transform
     *     The map to apply to each item in the streamable resource.
     *
     * @return A streamable resource which is a transformed view of the given streamable resource.
     */
    public static <T, R> IOStreamable<R> map(
        final IOStreamable<T> streamable,
        final Supplier<? extends IOStreamTransform<? super T, ? extends R>> transform
    )
    {
        return () -> IOStreams.map(streamable.stream(), transform.get());
    }

    /**
     * Creates a new streamable resource containing only a single item.
     *
     * @param item
     *     The item which will be provided by the streamable resource.
     * @param <T>
     *     The type of the item.
     *
     * @return A streamable resource which only provides the given value.
     */
    public static <T> IOStreamable<T> singleton(final T item)
    {
        return () -> IOStreams.singleton(item);
    }

    /**
     * Filters the items in a streamable resource to exclude any items which match the predicate.
     *
     * @param streamable
     *     The streamable resource to filter.
     * @param predicate
     *     A supplier to provide the predicate.
     * @param <T>
     *     The type of the items in the streamable resource.
     *
     * @return A filtered view of the streamable resource.
     */
    public static <T> IOStreamable<T> skip(
        final IOStreamable<T> streamable,
        final Supplier<? extends IOStreamPredicate<? super T>> predicate
    )
    {
        return () -> streamable.stream()
            .skip(predicate.get());
    }

    /**
     * Reads a streamable resource and copies its values to an array.
     *
     * @param streamable
     *     The streamable resource to copy.
     * @param supplier
     *     A function which will supply the array to fill. You may pass the array constructor e.g. {@code String[]::new}
     *     to create a new array.
     * @param <T>
     *     The type of the items in the array.
     *
     * @return An array.
     *
     * @throws IOStreamReadException
     *     If there was any problem in reading the stream.
     * @throws IOStreamCloseException
     *     If there was any problem in closing the stream.
     * @throws InterruptedException
     *     If the thread was interrupted.
     */
    public static <T> T[] toArray(final IOStreamable<T> streamable, final IntFunction<T[]> supplier)
        throws IOStreamReadException, IOStreamCloseException, InterruptedException
    {
        return streamable.stream()
            .toArray(supplier);
    }

    /**
     * Reads a streamable resource and copies its values to a {@link List}.
     *
     * @param streamable
     *     The streamable resource to copy.
     * @param <T>
     *     The type of the items in the {@link List}
     *
     * @return An immutable {@link List}.
     *
     * @throws IOStreamReadException
     *     If there was any problem in reading the stream.
     * @throws IOStreamCloseException
     *     If there was any problem in closing the stream.
     * @throws InterruptedException
     *     If the thread was interrupted.
     */
    public static <T> List<? extends T> toList(final IOStreamable<T> streamable)
        throws IOStreamReadException, IOStreamCloseException, InterruptedException
    {
        return IOStreams.toList(streamable.stream());
    }

    /**
     * Reads a streamable resource and copies its values to a {2link Set}.
     * <p>
     * Duplicate values in the streamable resource will be discarded.
     *
     * @param streamable
     *     The streamable resource to copy.
     * @param <T>
     *     The type of the items in the {@link Set}.
     *
     * @return An immutable {@link Set}.
     *
     * @throws IOStreamReadException
     *     If there was any problem in reading the resource's stream.
     * @throws IOStreamCloseException
     *     If there was any problem in closing the resource's stream.
     * @throws InterruptedException
     *     If the thread was interrupted.
     */
    public static <T> Set<T> toSet(final IOStreamable<T> streamable)
        throws IOStreamReadException, IOStreamCloseException, InterruptedException
    {
        return IOStreams.toSet(streamable.stream());
    }

    private IOStreamables() throws InstantiationException
    {
        throw new InstantiationException("This class cannot be instantiated.");
    }
}
