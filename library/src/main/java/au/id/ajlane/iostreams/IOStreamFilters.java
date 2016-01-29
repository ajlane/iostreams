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

import java.util.*;
import java.util.function.Predicate;

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
     *         The type of the items in the {@code IOStream}.
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
     *         The values to black-list.
     * @param <T>
     *         The type of the values in the {@code IOStream}.
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
     *         The values to black-list.
     * @param <T>
     *         The type of the values in the {@code IOStream}.
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
        for (final T value : values) set.add(value);
        return IOStreamFilters.blacklist(set);
    }

    /**
     * Provides a {@link IOStreamFilter} which skips the given items.
     *
     * @param values
     *         The values to black-list.
     * @param <T>
     *         The type of the values in the {@code IOStream}.
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
     *         The values to black-list.
     * @param <T>
     *         The type of the values in the {@code IOStream}.
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
     *         The original filter to invert.
     * @param <T>
     *         The type of the items in the {@code IOStream}.
     * @return An instance of {@code IOStreamFilter}.
     */
    public static <T> IOStreamFilter<T> invert(final IOStreamFilter<? super T> filter)
    {
        return IOStreamFilters.invert(filter, false);
    }

    /**
     * Provides a {@link IOStreamFilter} which keeps the opposite set of values to another filter.
     *
     * @param filter
     *         The original filter to invert.
     * @param honourTermination
     *         {@code true} if the inverted filter should honour the termination condition set by the original filter.
     *         If {@code false}, the entire {@code IOStream} will be processed.
     * @param <T>
     *         The type of the items in the {@code IOStream}.
     * @return An instance of {@code IOStreamFilter}.
     */
    public static <T> IOStreamFilter<T> invert(final IOStreamFilter<? super T> filter, final boolean honourTermination)
    {
        return new AbstractIOStreamFilter<T>()
        {
            @Override
            public void close() throws IOStreamCloseException
            {
                filter.close();
            }

            @Override
            protected boolean keep(final T item) throws IOStreamFilterException
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
                        throw new IllegalStateException(decision.name());
                }
            }
        };
    }

    /**
     * Provides a filter which removes all values from the {@code IOStream}.
     *
     * @param <T>
     *         The type of the items in the {@code IOStream}.
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
     *         The filters to apply, in order.
     * @param <T>
     *         The type of the items in the {@code IOStream}.
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
     *         The filters to apply, in order.
     * @param <T>
     *         The type of the items in the {@code IOStream}.
     * @return An instance of @{link StreamFilter}.
     */
    public static <T> IOStreamFilter<T> pipe(final Iterable<? extends IOStreamFilter<? super T>> filters)
    {
        return new AbstractIOStreamFilter<T>()
        {

            @Override
            public void close() throws IOStreamCloseException
            {
                for (final IOStreamFilter<? super T> filter : filters)
                {
                    filter.close();
                }
            }

            @Override
            protected boolean keep(final T item) throws IOStreamFilterException
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
     *         The values to white-list.
     * @param <T>
     *         The type of the values in the {@code IOStream}.
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
     *         The values to white-list.
     * @param <T>
     *         The type of the values in the {@code IOStream}.
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
        for (final T value : values) set.add(value);
        return IOStreamFilters.whitelist(set);
    }

    /**
     * Provides a {@link IOStreamFilter} which keeps only items in the given set.
     *
     * @param values
     *         The values to white-list.
     * @param <T>
     *         The type of the values in the {@code IOStream}.
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
     *         The values to white-list.
     * @param <T>
     *         The type of the values in the {@code IOStream}.
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

    private IOStreamFilters() throws InstantiationException
    {
        throw new InstantiationException("This class cannot be instantiated.");
    }

    public static <T> IOStreamFilter<T> fromPredicate(final Predicate<? super T> filter) {
        return new AbstractIOStreamFilter<T>() {
            @Override
            protected boolean keep(T item) throws IOStreamFilterException {
                return filter.test(item);
            }
        };
    }
}
