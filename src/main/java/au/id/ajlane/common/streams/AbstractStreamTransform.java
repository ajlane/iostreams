package au.id.ajlane.common.streams;

public abstract class AbstractStreamTransform<T, R> implements StreamTransform<T, R> {

    private boolean isOpen = false;

    @Override
    public final R apply(final T item) throws StreamTransformException {
        if (!isOpen) {
            open();
            isOpen = true;
        }
        return transform(item);
    }

    @Override
    public void close() throws StreamCloseException {
        // Do nothing by default
    }

    protected void open() throws StreamTransformException {
        // Do nothing by default
    }

    protected abstract R transform(T item) throws StreamTransformException;
}
