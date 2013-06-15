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

public abstract class Streamables {

    private static final Streamable<?> EMPTY = new Streamable<Object>() {
        @Override
        public Stream<Object> stream() {
            return Streams.empty();
        }
    };

    public static <T, R> Streamable<R> cast(final Streamable<T> streamable) {
        return new Streamable<R>() {
            @Override
            public Stream<R> stream() {
                return Streams.cast(streamable.stream());
            }
        };
    }

    public static <T> Streamable<T> concat(final Streamable<? extends Streamable<T>> streamables) {
        Objects.requireNonNull(streamables, "The streamable cannot be null.");
        return new Streamable<T>() {
            @Override
            public Stream<T> stream() {
                return new AbstractStream<T>() {

                    private Stream<? extends T> current = null;

                    private Stream<? extends Streamable<T>> streamablesStream = streamables.stream();

                    @Override
                    protected void open() throws StreamReadException {
                        if (streamablesStream.hasNext()) {
                            current = streamablesStream.next().stream();
                        }
                    }

                    @Override
                    protected T find() throws StreamReadException {
                        while (current != null) {
                            if (current.hasNext()) {
                                return current.next();
                            } else {
                                try {
                                    current.close();
                                } catch (StreamCloseException ex) {
                                    throw new StreamReadException("Could not close one of the concatenated streams.", ex);
                                }
                                if (streamablesStream.hasNext()) {
                                    current = streamablesStream.next().stream();
                                } else {
                                    current = null;
                                }
                            }
                        }
                        return terminate();
                    }

                    @Override
                    protected void end() throws StreamCloseException {
                        if (current != null) current.close();
                    }
                };
            }
        };
    }

    @SafeVarargs
    public static <T> Streamable<T> concat(final Streamable<T>... streamables) {
        Objects.requireNonNull(streamables, "The array cannot be null.");
        return new Streamable<T>() {
            @Override
            public Stream<T> stream() {
                return new AbstractStream<T>() {

                    private int i = 0;
                    private Stream<? extends T> current = null;

                    @Override
                    protected void open() {
                        if (i < streamables.length) {
                            current = streamables[i].stream();
                        }
                    }

                    @Override
                    protected T find() throws StreamReadException {
                        while (current != null) {
                            if (current.hasNext()) {
                                return current.next();
                            } else {
                                try {
                                    current.close();
                                } catch (StreamCloseException ex) {
                                    throw new StreamReadException("Could not close one of the concatenated streams.", ex);
                                }
                                i++;
                                if (i < streamables.length) {
                                    current = streamables[i].stream();
                                } else {
                                    current = null;
                                }
                            }
                        }
                        return terminate();
                    }

                    @Override
                    protected void end() throws StreamCloseException {
                        if (current != null) current.close();
                    }
                };
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> Streamable<T> empty() {
        return (Streamable<T>) EMPTY;
    }

    public static <T> Streamable<T> filter(final Streamable<T> streamable, final StreamFilter<? super T> filter) {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        Objects.requireNonNull(filter, "The filter cannot be null.");
        return new Streamable<T>() {
            @Override
            public Stream<T> stream() {
                return Streams.filter(streamable.stream(), filter);
            }
        };
    }

    public static <T> Streamable<T> flattenArrays(final Streamable<T[]> streamable) {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        return new Streamable<T>() {
            @Override
            public Stream<T> stream() {
                return Streams.flattenArrays(streamable.stream());
            }
        };
    }

    public static <T> Streamable<T> flattenIterables(final Streamable<? extends Iterable<? extends T>> streamable) {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        return new Streamable<T>() {
            @Override
            public Stream<T> stream() {
                return Streams.flattenIterables(streamable.stream());
            }
        };
    }

    public static <T> Streamable<T> flattenStreamables(final Streamable<? extends Streamable<T>> streamable) {
        Objects.requireNonNull(streamable, "The streamable cannot be null.");
        return new Streamable<T>() {
            @Override
            public Stream<T> stream() {
                return Streams.flatten(streamable.stream(), new AbstractStreamTransform<Streamable<T>, Stream<T>>() {
                    @Override
                    protected Stream<T> transform(final Streamable<T> item) {
                        return item.stream();
                    }
                });
            }
        };
    }

    @SafeVarargs
    public static <T> Streamable<T> fromArray(final T... values) {
        Objects.requireNonNull(values, "The array cannot be null.");
        return new Streamable<T>() {
            @Override
            public Stream<T> stream() {
                return Streams.fromArray(values);
            }
        };
    }

    public static <T> Streamable<T> fromIterable(final Iterable<T> iterable) {
        Objects.requireNonNull(iterable, "The iterable cannot be null.");
        return new Streamable<T>() {
            @Override
            public Stream<T> stream() {
                return Streams.fromIterator(iterable.iterator());
            }
        };
    }

    public static <T> Streamable<T> singleton(final T item) {
        return new Streamable<T>() {
            @Override
            public Stream<T> stream() {
                return Streams.singleton(item);
            }
        };
    }

    public static <T> T[] toArray(final Streamable<T> streamable) throws StreamException {
        return Streams.toArray(streamable.stream());
    }

    public static <T> List<? extends T> toList(final Streamable<T> streamable) throws StreamException {
        return Streams.toList(streamable.stream());
    }

    public static <T> Set<T> toSet(final Streamable<T> streamable) throws StreamException {
        return Streams.toSet(streamable.stream());
    }

    public static <T, R> Streamable<R> transform(final Streamable<T> streamable, final StreamTransform<? super T, ? extends R> transform) {
        return new Streamable<R>() {
            @Override
            public Stream<R> stream() {
                return Streams.transform(streamable.stream(), transform);
            }
        };
    }

    private Streamables() throws InstantiationException {
        throw new InstantiationException();
    }
}
