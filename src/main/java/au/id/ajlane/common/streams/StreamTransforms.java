package au.id.ajlane.common.streams;

public abstract class StreamTransforms {

    public static <T extends R, R> StreamTransform<T, R> identity() {
        return new StreamTransform<T, R>() {
            @Override
            public R apply(final T item) {
                return item;
            }

            @Override
            public void close() {
            }
        };
    }

    public static <T, I, R> StreamTransform<T, R> pipe(final StreamTransform<T, I> a, final StreamTransform<I, R> b) {
        return new AbstractStreamTransform<T, R>() {
            @Override
            public R transform(final T item) throws StreamTransformException {
                return b.apply(a.apply(item));
            }
        };
    }

    public static <T, I1, I2, R> StreamTransform<T, R> pipe(final StreamTransform<T, I1> a, final StreamTransform<I1, I2> b, final StreamTransform<I2, R> c) {
        return new AbstractStreamTransform<T, R>() {
            @Override
            public R transform(final T item) throws StreamTransformException {
                return c.apply(b.apply(a.apply(item)));
            }
        };
    }

    public static <T, I1, I2, I3, R> StreamTransform<T, R> pipe(final StreamTransform<T, I1> a, final StreamTransform<I1, I2> b, final StreamTransform<I2, I3> c, final StreamTransform<I3, R> d) {
        return new AbstractStreamTransform<T, R>() {
            @Override
            public R transform(final T item) throws StreamTransformException {
                return d.apply(c.apply(b.apply(a.apply(item))));
            }
        };
    }

    public static <T, I1, I2, I3, I4, R> StreamTransform<T, R> pipe(final StreamTransform<T, I1> a, final StreamTransform<I1, I2> b, final StreamTransform<I2, I3> c, final StreamTransform<I3, I4> d, final StreamTransform<I4, R> e) {
        return new AbstractStreamTransform<T, R>() {
            @Override
            public R transform(final T item) throws StreamTransformException {
                return e.apply(d.apply(c.apply(b.apply(a.apply(item)))));
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> StreamTransform<T, ?> pipe(final StreamTransform<T, ?> first, final StreamTransform... rest) {
        return new AbstractStreamTransform<T, Object>() {
            @Override
            public Object transform(final T item) throws StreamTransformException {
                Object result = first.apply(item);
                for (int i = 0; i < rest.length; i++) {
                    result = rest[i].apply(result);
                }
                return result;
            }
        };
    }

    private StreamTransforms() throws InstantiationException {
        throw new InstantiationException();
    }
}
