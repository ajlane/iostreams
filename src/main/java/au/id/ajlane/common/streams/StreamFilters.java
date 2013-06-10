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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility functions for working with instances of {@link StreamFilter}.
 */
public abstract class StreamFilters {

    private static StreamFilter<?> ALL = new AbstractStreamFilter<Object>() {
    };
    private static StreamFilter<?> NONE = new AbstractStreamFilter<Object>() {
        @Override
        protected boolean keep(final Object item) {
            return terminate(false);
        }
    };

    /**
     * Provides a {@link StreamFilter} that keeps all items.
     *
     * @param <T>
     *         The type of the items in the {@code Stream}.
     * @return An instance of {@code StreamFilter}.
     */
    @SuppressWarnings("unchecked")
    public static <T> StreamFilter<T> all() {
        return (StreamFilter<T>) ALL;
    }

    /**
     * Provides a {@link StreamFilter} which skips the given items.
     *
     * @param values
     *         The values to black-list.
     * @param <T>
     *         The type of the values in the {@code Stream}.
     * @return An instance of {@code StreamFilter}.
     */
    @SafeVarargs
    public static <T> StreamFilter<T> blacklist(final T... values) {
        final HashSet<T> set = new HashSet<>();
        for (final T value : values) set.add(value);
        return blacklist((Set<? super T>) set);
    }

    /**
     * Provides a {@link StreamFilter} which skips the given items.
     *
     * @param values
     *         The values to black-list.
     * @param <T>
     *         The type of the values in the {@code Stream}.
     * @return An instance of {@code StreamFilter}.
     */
    public static <T> StreamFilter<T> blacklist(final Iterable<T> values) {
        if (values instanceof Set<?>) {
            return blacklist((Set<? super T>) values);
        }
        final HashSet<T> set = new HashSet<>();
        for (final T value : values) set.add(value);
        return blacklist((Set<? super T>) set);
    }

    /**
     * Provides a {@link StreamFilter} which skips the given items.
     *
     * @param values
     *         The values to black-list.
     * @param <T>
     *         The type of the values in the {@code Stream}.
     * @return An instance of {@code StreamFilter}.
     */
    public static <T> StreamFilter<T> blacklist(final Collection<T> values) {
        if (values instanceof Set<?>) {
            return blacklist((Set<? super T>) values);
        }
        return blacklist((Set<? super T>) new HashSet<>(values));
    }

    /**
     * Provides a {@link StreamFilter} which skips the given items.
     *
     * @param values
     *         The values to black-list.
     * @param <T>
     *         The type of the values in the {@code Stream}.
     * @return An instance of {@code StreamFilter}.
     */
    public static <T> StreamFilter<T> blacklist(final Set<? super T> values) {
        return new AbstractStreamFilter<T>() {
            @Override
            protected boolean keep(final T item) {
                return !values.contains(item);
            }
        };
    }

    /**
     * Provides a {@link StreamFilter} which keeps the opposite set of values to another filter.
     * <p/>
     * If the original filter advises termination, it will be ignored. To invert the keep decision without changing the
     * termination condition, use {@link #invert(StreamFilter, boolean)}.
     *
     * @param filter
     *         The original filter to invert.
     * @param <T>
     *         The type of the items in the {@code Stream}.
     * @return An instance of {@code StreamFilter}.
     */
    public static <T> StreamFilter<T> invert(final StreamFilter<? super T> filter) {
        return invert(filter, false);
    }

    /**
     * Provides a {@link StreamFilter} which keeps the opposite set of values to another filter.
     *
     * @param filter
     *         The original filter to invert.
     * @param honourTermination
     *         {@code true} if the inverted filter should honour the termination condition set by the original filter.
     *         If {@code false}, the entire {@code Stream} will be processed.
     * @param <T>
     *         The type of the items in the {@code Stream}.
     * @return An instance of {@code StreamFilter}.
     */
    public static <T> StreamFilter<T> invert(final StreamFilter<? super T> filter, final boolean honourTermination) {
        return new AbstractStreamFilter<T>() {
            @Override
            public void close() throws StreamCloseException {
                filter.close();
            }

            @Override
            protected boolean keep(final T item) throws StreamFilterException {
                switch (filter.apply(item)) {
                    case KEEP_AND_CONTINUE:
                        return false;
                    case KEEP_AND_TERMINATE:
                        return honourTermination ? terminate(false) : false;
                    case SKIP_AND_CONTINUE:
                        return true;
                    case SKIP_AND_TERMINATE:
                    default:
                        return honourTermination ? terminate(true) : true;
                }
            }
        };
    }

    /**
     * Provides a filter which removes all values from the {@code Stream}.
     *
     * @param <T>
     *         The type of the items in the {@code Stream}.
     * @return An instance of {@code StreamFilter}.
     */
    @SuppressWarnings("unchecked")
    public static <T> StreamFilter<T> none() {
        return (StreamFilter<T>) NONE;
    }

    /**
     * Chains several filters together.
     * <p/>
     * Filters will be applied in the order given. If an item is skipped by an earlier filter, the subsequent filters
     * will not be checked.
     * <p/>
     * Any filter can terminate the {@code Stream} early if it is called, but termination will not occur until
     * <i>after</i> the pipeline has decided whether to keep/skip the item.
     *
     * @param filters
     *         The filters to apply, in order.
     * @param <T>
     *         The type of the items in the {@code Stream}.
     * @return An instance of @{link StreamFilter}.
     */
    @SafeVarargs
    public static <T> StreamFilter<T> pipe(final StreamFilter<? super T>... filters) {
        return pipe(Arrays.asList(filters));
    }

    /**
     * Chains several filters together.
     * <p/>
     * Filters will be applied in the order given. If an item is skipped by an earlier filter, the subsequent filters
     * will not be checked.
     * <p/>
     * Any filter can terminate the {@code Stream} early if it is called, but termination will not occur until
     * <i>after</i> the pipeline has decided whether to keep/skip the item.
     *
     * @param filters
     *         The filters to apply, in order.
     * @param <T>
     *         The type of the items in the {@code Stream}.
     * @return An instance of @{link StreamFilter}.
     */
    public static <T> StreamFilter<T> pipe(final Iterable<? extends StreamFilter<? super T>> filters) {
        return new AbstractStreamFilter<T>() {
            @Override
            public void close() throws StreamCloseException {
                for (final StreamFilter<? super T> filter : filters) {
                    filter.close();
                }
            }

            @Override
            protected boolean keep(final T item) throws StreamFilterException {
                boolean terminate = false;

                for (final StreamFilter<? super T> filter : filters) {
                    switch (filter.apply(item)) {
                        case SKIP_AND_TERMINATE:
                            return terminate(false);
                        case SKIP_AND_CONTINUE:
                            return terminate ? terminate(false) : false;
                        case KEEP_AND_TERMINATE:
                            terminate = true;
                            continue;
                        case KEEP_AND_CONTINUE:
                        default:
                            continue;
                    }
                }

                return terminate ? terminate(true) : true;
            }
        };
    }

    /**
     * Provides a {@link StreamFilter} which keeps only items in the given set.
     *
     * @param values
     *         The values to white-list.
     * @param <T>
     *         The type of the values in the {@code Stream}.
     * @return An instance of {@code StreamFilter}.
     */
    @SafeVarargs
    public static <T> StreamFilter<T> whitelist(final T... values) {
        final HashSet<T> set = new HashSet<>();
        for (final T value : values) set.add(value);
        return whitelist((Set<? super T>) set);
    }

    /**
     * Provides a {@link StreamFilter} which keeps only items in the given set.
     *
     * @param values
     *         The values to white-list.
     * @param <T>
     *         The type of the values in the {@code Stream}.
     * @return An instance of {@code StreamFilter}.
     */
    public static <T> StreamFilter<T> whitelist(final Iterable<T> values) {
        if (values instanceof Set<?>) {
            return whitelist((Set<? super T>) values);
        }
        final HashSet<T> set = new HashSet<>();
        for (final T value : values) set.add(value);
        return whitelist((Set<? super T>) set);
    }

    /**
     * Provides a {@link StreamFilter} which keeps only items in the given set.
     *
     * @param values
     *         The values to white-list.
     * @param <T>
     *         The type of the values in the {@code Stream}.
     * @return An instance of {@code StreamFilter}.
     */
    public static <T> StreamFilter<T> whitelist(final Collection<T> values) {
        if (values instanceof Set<?>) {
            return whitelist((Set<? super T>) values);
        }
        return whitelist((Set<? super T>) new HashSet<>(values));
    }

    /**
     * Provides a {@link StreamFilter} which keeps only items in the given set.
     *
     * @param values
     *         The values to white-list.
     * @param <T>
     *         The type of the values in the {@code Stream}.
     * @return An instance of {@code StreamFilter}.
     */
    public static <T> StreamFilter<T> whitelist(final Set<? super T> values) {
        return new AbstractStreamFilter<T>() {
            @Override
            protected boolean keep(final T item) {
                return values.contains(item);
            }
        };
    }

    private StreamFilters() throws InstantiationException {
        throw new InstantiationException();
    }
}
