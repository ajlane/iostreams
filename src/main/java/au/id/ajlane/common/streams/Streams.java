package au.id.ajlane.common.streams;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class Streams {

    private static final Stream<?> EMPTY = new Stream<Object>() {
        @Override
        public void close() throws StreamCloseException {
        }

        @Override
        public boolean hasNext() throws StreamReadException {
            return false;
        }

        @Override
        public Object next() throws StreamReadException {
            throw new NoSuchElementException();
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> empty() {
        return (Stream<T>) EMPTY;
    }

    public static <T> Stream<T> singleton(final T item) {
        return new Stream<T>() {
            private boolean hasNext = true;

            @Override
            public void close() throws StreamCloseException {
            }

            @Override
            public boolean hasNext() throws StreamReadException {
                return hasNext;
            }

            @Override
            public T next() throws StreamReadException {
                if (hasNext) {
                    hasNext = false;
                    return item;
                }
                throw new NoSuchElementException();
            }
        };
    }

    public static <T, R> Stream<R> transform(final Stream<T> stream, final StreamTransform<T, R> transform) {
        return new Stream<R>() {
            @Override
            public void close() throws StreamCloseException {
                try {
                    transform.close();
                } finally {
                    stream.close();
                }
            }

            @Override
            public boolean hasNext() throws StreamReadException {
                return stream.hasNext();
            }

            @Override
            public R next() throws StreamReadException {
                return transform.apply(stream.next());
            }
        };
    }

    public static <T, R> Stream<R> flatten(final Stream<T> stream, final StreamTransform<T, Stream<R>> transform) {
        return concat(transform(stream, transform));
    }

    public static <T> Stream<T> concat(final Iterable<? extends Stream<T>> streams) {
        return concat(fromIterable(streams));
    }

    public static <T> Stream<T> concat(final Iterator<? extends Stream<T>> streams) {
        return concat(fromIterator(streams));
    }

    public static <T> Stream<T> concat(final Streamable<? extends Stream<T>> streams) {
        return concat(streams.stream());
    }

    public static <T> Stream<T> concat(final Stream<? extends Stream<T>> streams) {
        return new AbstractStream<T>() {
            private Stream<T> current = null;

            @Override
            protected void open() throws StreamReadException {
                if (streams.hasNext()) current = streams.next();
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
                        if (streams.hasNext()) {
                            current = streams.next();
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
                streams.close();
            }
        };
    }

    public static <T> Stream<T> concat(final Stream<T>... streams) {
        return new AbstractStream<T>() {
            private int i = 0;

            @Override
            protected T find() throws StreamReadException {
                while (i < streams.length) {
                    if (streams[i].hasNext()) {
                        return streams[i].next();
                    } else {
                        try {
                            streams[i].close();
                        } catch (StreamCloseException ex) {
                            throw new StreamReadException("Could not close one of the concatenated streams.", ex);
                        }
                        i++;
                    }
                }
                return terminate();
            }

            @Override
            protected void end() throws StreamCloseException {
                if (i < streams.length) {
                    streams[i].close();
                }
            }
        };
    }

    public static <T> Stream<T> fromIterable(final Iterable<T> iterable) {
        return fromIterator(iterable.iterator());
    }

    public static <T> Stream<T> fromIterator(final Iterator<T> iterator) {
        return new Stream<T>() {
            public boolean hasNext() {
                return iterator.hasNext();
            }

            public T next() {
                return iterator.next();
            }

            public void close() throws StreamCloseException {
                if (iterator instanceof Closeable) {
                    try {
                        ((Closeable) iterator).close();
                    } catch (IOException ex) {
                        throw new StreamCloseException("Could not close underlying iterator.", ex);
                    }
                }
            }
        };
    }

    public static <T> Stream<T> fromArray(final T... values) {
        return new Stream<T>() {
            private int i = 0;

            @Override
            public void close() throws StreamCloseException {
            }

            @Override
            public boolean hasNext() throws StreamReadException {
                return i < values.length;
            }

            @Override
            public T next() throws StreamReadException {
                if (i < values.length) return values[i++];
                throw new NoSuchElementException();
            }
        };
    }

    public static <T, TIterator extends Iterator<T> & AutoCloseable> Stream<T> fromCloseableIterator(final TIterator iterator) {
        return new Stream<T>() {
            public boolean hasNext() {
                return iterator.hasNext();
            }

            public T next() {
                return iterator.next();
            }

            public void close() throws StreamCloseException {
                try {
                    iterator.close();
                } catch (RuntimeException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new StreamCloseException("Could not close underlying iterator.", ex);
                }
            }
        };
    }

    private Streams() throws InstantiationException {
        throw new InstantiationException();
    }
}
