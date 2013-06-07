package au.id.ajlane.common.streams;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class StreamFilters {

    public static <T> StreamFilter<T> all() {
        return new AbstractStreamFilter<T>() {
        };
    }

    @SafeVarargs
    public static <T> StreamFilter<T> blacklist(final T... values) {
        final HashSet<T> set = new HashSet<>();
        for (final T value : values) set.add(value);
        return blacklist((Set<? super T>) set);
    }

    public static <T> StreamFilter<T> blacklist(final Iterable<T> values) {
        if (values instanceof Set<?>) {
            return blacklist((Set<? super T>) values);
        }
        final HashSet<T> set = new HashSet<>();
        for (final T value : values) set.add(value);
        return blacklist((Set<? super T>) set);
    }

    public static <T> StreamFilter<T> blacklist(final Collection<T> values) {
        if (values instanceof Set<?>) {
            return blacklist((Set<? super T>) values);
        }
        return blacklist((Set<? super T>) new HashSet<>(values));
    }

    public static <T> StreamFilter<T> blacklist(final Set<? super T> values) {
        return new AbstractStreamFilter<T>() {
            @Override
            protected boolean keep(final T item) {
                return !values.contains(item);
            }
        };
    }

    public static <T> StreamFilter<T> invert(final StreamFilter<? super T> filter) {
        return new AbstractStreamFilter<T>() {
            @Override
            public void close() throws StreamCloseException {
                filter.close();
            }

            @Override
            protected boolean keep(final T item) throws StreamFilterException {
                switch (filter.apply(item)) {
                    case KEEP_AND_CONTINUE:
                        return terminate(false);
                    case KEEP_AND_TERMINATE:
                        return false;
                    case SKIP_AND_CONTINUE:
                        return terminate(true);
                    case SKIP_AND_TERMINATE:
                    default:
                        return true;
                }
            }
        };
    }

    public static <T> StreamFilter<T> none() {
        return new AbstractStreamFilter<T>() {
            @Override
            protected boolean keep(final T item) {
                return terminate(false);
            }
        };
    }

    @SafeVarargs
    public static <T> StreamFilter<T> pipe(final StreamFilter<? super T>... filters) {
        return pipe(Arrays.asList(filters));
    }

    public static <T> StreamFilter<T> pipe(final Iterable<StreamFilter<? super T>> filters) {
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

    @SafeVarargs
    public static <T> StreamFilter<T> whitelist(final T... values) {
        final HashSet<T> set = new HashSet<>();
        for (final T value : values) set.add(value);
        return whitelist((Set<? super T>) set);
    }

    public static <T> StreamFilter<T> whitelist(final Iterable<T> values) {
        if (values instanceof Set<?>) {
            return whitelist((Set<? super T>) values);
        }
        final HashSet<T> set = new HashSet<>();
        for (final T value : values) set.add(value);
        return whitelist((Set<? super T>) set);
    }

    public static <T> StreamFilter<T> whitelist(final Collection<T> values) {
        if (values instanceof Set<?>) {
            return whitelist((Set<? super T>) values);
        }
        return whitelist((Set<? super T>) new HashSet<>(values));
    }

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
