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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilities for working with instances of {@link IOStreamFilter}.
 */
public final class IOStreamFilters
{

    private static final IOStreamFilter<?> ALL = new AbstractIOStreamFilter<Object>()
    {
        @Override
        protected boolean keep(final Object item)
        {
            return true;
        }
    };
    private static final IOStreamFilter<?> NONE = new AbstractIOStreamFilter<Object>()
    {
        @Override
        protected boolean keep(final Object item)
        {
            return this.terminate(false);
        }
    };

    /**
     * Provides a {@link IOStreamFilter} that keeps all items.
     *
     * @param <T>
     *     The type of the items in the {@code IOStream}.
     *
     * @return An instance of {@code IOStreamFilter}.
     */
    @SuppressWarnings("unchecked")
    public static <T> IOStreamFilter<T> all()
    {
        return (IOStreamFilter<T>) IOStreamFilters.ALL;
    }

    /**
     * Provides a {@link IOStreamFilter} which skips the given items.
     *
     * @param values
     *     The values to black-list.
     * @param <T>
     *     The type of the values in the {@code IOStream}.
     *
     * @return An instance of {@code IOStreamFilter}.
     */
    @SafeVarargs
    public static <T> IOStreamFilter<T> blacklist(final T... values)
    {
        final Set<T> set = new HashSet<>(values.length);
        Collections.addAll(set, values);
        return IOStreamFilters.blacklist(set);
    }

    /**
     * Provides a {@link IOStreamFilter} which skips the given items.
     *
     * @param values
     *     The values to black-list.
     * @param <T>
     *     The type of the values in the {@code IOStream}.
     *
     * @return An instance of {@code IOStreamFilter}.
     */
    public static <T> IOStreamFilter<T> blacklist(final Iterable<? extends T> values)
    {
        if (values instanceof Set<?>)
        {
            @SuppressWarnings("unchecked")
            final Set<? extends T> set = (Set<? extends T>) values;
            return IOStreamFilters.blacklist(set);
        }
        final Set<T> set = new HashSet<>();
        for (final T value : values)
        {
            set.add(value);
        }
        return IOStreamFilters.blacklist(set);
    }

    /**
     * Provides a {@link IOStreamFilter} which skips the given items.
     *
     * @param values
     *     The values to black-list.
     * @param <T>
     *     The type of the values in the {@code IOStream}.
     *
     * @return An instance of {@code IOStreamFilter}.
     */
    public static <T> IOStreamFilter<T> blacklist(final Collection<? extends T> values)
    {
        if (values instanceof Set<?>)
        {
            @SuppressWarnings("unchecked")
            final Set<? extends T> set = (Set<? extends T>) values;
            return IOStreamFilters.blacklist(set);
        }
        final Set<? extends T> set = new HashSet<>(values);
        return IOStreamFilters.blacklist(set);
    }

    /**
     * Provides a {@link IOStreamFilter} which skips the given items.
     *
     * @param values
     *     The values to black-list.
     * @param <T>
     *     The type of the values in the {@code IOStream}.
     *
     * @return An instance of {@code IOStreamFilter}.
     */
    public static <T> IOStreamFilter<T> blacklist(final Set<? extends T> values)
    {
        return new AbstractIOStreamFilter<T>()
        {
            @Override
            protected boolean keep(final T item)
            {
                return !values.contains(item);
            }
        };
    }

    /**
     * Provides a {@link IOStreamFilter} which keeps the opposite set of values to another filter.
     * <p>
     * If the original filter advises termination, it will be ignored. To invert the keep decision without changing the
     * termination condition, use {@link #invert(IOStreamFilter, boolean)}.
     *
     * @param filter
     *     The original filter to invert.
     * @param <T>
     *     The type of the items in the {@code IOStream}.
     *
     * @return An instance of {@code IOStreamFilter}.
     */
    public static <T> IOStreamFilter<T> invert(final IOStreamFilter<T> filter)
    {
        return filter.invert(false);
    }

    /**
     * Provides a {@link IOStreamFilter} which keeps the opposite set of values to another filter.
     *
     * @param filter
     *     The original filter to invert.
     * @param honourTermination
     *     {@code true} if the inverted filter should honour the termination condition set by the original filter. If
     *     {@code false}, the entire {@code IOStream} will be processed.
     * @param <T>
     *     The type of the items in the {@code IOStream}.
     *
     * @return An instance of {@code IOStreamFilter}.
     */
    public static <T> IOStreamFilter<T> invert(final IOStreamFilter<T> filter, final boolean honourTermination)
    {
        return new AbstractIOStreamFilter<T>()
        {
            @Override
            public void close() throws Exception
            {
                filter.close();
            }

            @Override
            protected boolean keep(final T item) throws Exception
            {
                final FilterDecision decision = filter.apply(item);
                switch (decision)
                {
                    case KEEP_AND_CONTINUE:
                        return false;
                    case KEEP_AND_TERMINATE:
                        return honourTermination && this.terminate(false);
                    case SKIP_AND_CONTINUE:
                        return true;
                    case SKIP_AND_TERMINATE:
                        return !honourTermination || this.terminate(true);
                    default:
                        throw new IllegalStateException("Unrecognised decision: " + decision.name());
                }
            }
        };
    }

    /**
     * Provides a filter which removes all values from the {@code IOStream}.
     *
     * @param <T>
     *     The type of the items in the {@code IOStream}.
     *
     * @return An instance of {@code IOStreamFilter}.
     */
    @SuppressWarnings("unchecked")
    public static <T> IOStreamFilter<T> none()
    {
        return (IOStreamFilter<T>) IOStreamFilters.NONE;
    }

    /**
     * Chains several filters together.
     * <p>
     * Filters will be applied in the order given. If an item is skipped by an earlier filter, the subsequent filters
     * will not be checked.
     * <p>
     * Any filter can terminate the {@code IOStream} early if it is called, but termination will not occur until
     * <i>after</i> the pipeline has decided whether to keep/skip the item.
     *
     * @param filters
     *     The filters to apply, in order.
     * @param <T>
     *     The type of the items in the {@code IOStream}.
     *
     * @return An instance of @{link StreamFilter}.
     */
    @SafeVarargs
    public static <T> IOStreamFilter<T> pipe(final IOStreamFilter<? super T>... filters)
    {
        return IOStreamFilters.pipe(Arrays.asList(filters));
    }

    /**
     * Chains several filters together.
     * <p>
     * Filters will be applied in the order given. If an item is skipped by an earlier filter, the subsequent filters
     * will not be checked.
     * <p>
     * Any filter can terminate the {@code IOStream} early if it is called, but termination will not occur until
     * <i>after</i> the pipeline has decided whether to keep/skip the item.
     *
     * @param filters
     *     The filters to apply, in order.
     * @param <T>
     *     The type of the items in the {@code IOStream}.
     *
     * @return An instance of @{link StreamFilter}.
     */
    public static <T> IOStreamFilter<T> pipe(final Iterable<? extends IOStreamFilter<? super T>> filters)
    {
        return new AbstractIOStreamFilter<T>()
        {
            @Override
            public void close() throws IOStreamCloseException
            {
                Exception lastException = null;
                boolean runtimeException = false;
                for (final IOStreamFilter<? super T> filter : filters)
                {
                    try
                    {
                        filter.close();
                    }
                    catch (final RuntimeException ex)
                    {
                        runtimeException = true;
                        if (lastException != null)
                        {
                            ex.addSuppressed(lastException);
                        }
                        lastException = ex;
                    }
                    catch (final Exception ex)
                    {
                        if (lastException != null)
                        {
                            ex.addSuppressed(lastException);
                        }
                        lastException = ex;
                    }
                }
                if (lastException != null)
                {
                    if (runtimeException)
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
                        throw new IOStreamCloseException(lastException);
                    }
                }
            }

            @Override
            protected boolean keep(final T item) throws Exception
            {
                boolean terminate = false;

                for (final IOStreamFilter<? super T> filter : filters)
                {
                    final FilterDecision decision = filter.apply(item);
                    switch (decision)
                    {
                        case SKIP_AND_TERMINATE:
                            return this.terminate(false);
                        case SKIP_AND_CONTINUE:
                            return terminate && this.terminate(false);
                        case KEEP_AND_TERMINATE:
                            terminate = true;
                            continue;
                        case KEEP_AND_CONTINUE:
                            continue;
                        default:
                            throw new IllegalStateException(decision.name());
                    }
                }

                return !terminate || this.terminate(true);
            }
        };
    }

    /**
     * Provides a {@link IOStreamFilter} which keeps only items in the given set.
     *
     * @param values
     *     The values to white-list.
     * @param <T>
     *     The type of the values in the {@code IOStream}.
     *
     * @return An instance of {@code IOStreamFilter}.
     */
    @SafeVarargs
    public static <T> IOStreamFilter<T> whitelist(final T... values)
    {
        final Collection<T> set = new HashSet<>(values.length);
        Collections.addAll(set, values);
        return IOStreamFilters.whitelist(set);
    }

    /**
     * Provides a {@link IOStreamFilter} which keeps only items in the given set.
     *
     * @param values
     *     The values to white-list.
     * @param <T>
     *     The type of the values in the {@code IOStream}.
     *
     * @return An instance of {@code IOStreamFilter}.
     */
    public static <T> IOStreamFilter<T> whitelist(final Iterable<? extends T> values)
    {
        if (values instanceof Set<?>)
        {
            @SuppressWarnings("unchecked")
            final Set<? extends T> set = (Set<? extends T>) values;
            return IOStreamFilters.whitelist(set);
        }
        final Set<T> set = new HashSet<>();
        for (final T value : values)
        {
            set.add(value);
        }
        return IOStreamFilters.whitelist(set);
    }

    /**
     * Provides a {@link IOStreamFilter} which keeps only items in the given set.
     *
     * @param values
     *     The values to white-list.
     * @param <T>
     *     The type of the values in the {@code IOStream}.
     *
     * @return An instance of {@code IOStreamFilter}.
     */
    public static <T> IOStreamFilter<T> whitelist(final Collection<? extends T> values)
    {
        if (values instanceof Set<?>)
        {
            @SuppressWarnings("unchecked")
            final Set<? extends T> set = (Set<? extends T>) values;
            return IOStreamFilters.whitelist(set);
        }
        final Set<? extends T> set = new HashSet<>(values);
        return IOStreamFilters.whitelist(set);
    }

    /**
     * Provides a {@link IOStreamFilter} which keeps only items in the given set.
     *
     * @param values
     *     The values to white-list.
     * @param <T>
     *     The type of the values in the {@code IOStream}.
     *
     * @return An instance of {@code IOStreamFilter}.
     */
    public static <T> IOStreamFilter<T> whitelist(final Set<? extends T> values)
    {
        return new AbstractIOStreamFilter<T>()
        {
            @Override
            protected boolean keep(final T item)
            {
                return values.contains(item);
            }
        };
    }

    /**
     * Creates a filter from a predicate.
     *
     * @param keep
     *     The predicate which will determine which items to keep.
     * @param <T>
     *     The type of the items.
     *
     * @return An instance of {@code IOStreamFilter}.
     */
    public static <T> IOStreamFilter<T> fromPredicate(final IOStreamPredicate<? super T> keep)
    {
        return new AbstractIOStreamFilter<T>()
        {
            @Override
            public void close() throws Exception
            {
                keep.close();
            }

            @Override
            protected boolean keep(final T item) throws Exception
            {
                return keep.test(item);
            }
        };
    }

    /**
     * Creates a filter which will terminate after it has kept a fixed number of items.
     *
     * @param size
     *     The maximum number of items to keep.
     * @param <T>
     *     The type of the items.
     *
     * @return An instance of {@code IOStreamFilter}.
     */
    public static <T> IOStreamFilter<T> limit(final long size)
    {
        if (size <= 0)
        {
            return IOStreamFilters.none();
        }

        return new IOStreamFilter<T>()
        {
            private long count = 0;

            @Override
            public FilterDecision apply(T item)
            {
                if (count > size)
                {
                    return FilterDecision.SKIP_AND_TERMINATE;
                }
                count++;
                if (count >= size)
                {
                    return FilterDecision.KEEP_AND_TERMINATE;
                }
                return FilterDecision.KEEP_AND_CONTINUE;
            }
        };
    }

    /**
     * Creates a filter which will terminate the stream when it encounters a matching item.
     *
     * @param predicate
     *     A predicate to test each item. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return A filter which may cause the stream to terminate early.
     */
    public static <T> IOStreamFilter<T> keepUntil(final IOStreamPredicate<? super T> predicate)
    {
        if (predicate == null)
        {
            throw new NullPointerException("The predicate must be non-null.");
        }
        return t -> predicate.test(t) ? FilterDecision.SKIP_AND_TERMINATE : FilterDecision.KEEP_AND_CONTINUE;
    }

    /**
     * Creates a filter which will terminate the stream when it encounters a non-matching item.
     *
     * @param predicate
     *     A predicate to test each item. Must not be null.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return A filter which may cause the stream to terminate early.
     */
    public static <T> IOStreamFilter<T> keepWhile(final IOStreamPredicate<? super T> predicate)
    {
        if (predicate == null)
        {
            throw new NullPointerException("The predicate must be non-null.");
        }
        return t -> predicate.test(t) ? FilterDecision.KEEP_AND_CONTINUE : FilterDecision.SKIP_AND_TERMINATE;
    }

    /**
     * Creates a filter which will skip the first n items.
     *
     * @param n
     *     The number of items to skip.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return A filter.
     */
    public static <T> IOStreamFilter<? super T> skip(final long n)
    {
        if (n <= 0)
        {
            return all();
        }
        return new IOStreamFilter<T>()
        {
            private volatile long count = 0;

            @Override
            public FilterDecision apply(final T item) throws Exception
            {
                if (count < n)
                {
                    count++;
                    return FilterDecision.SKIP_AND_CONTINUE;
                }
                return FilterDecision.KEEP_AND_CONTINUE;
            }
        };
    }

    private IOStreamFilters() throws InstantiationException
    {
        throw new InstantiationException("This class cannot be instantiated.");
    }
}
