package au.id.ajlane.common.streams;

import java.io.Closeable;

public interface StreamFilter<T> extends Closeable {
    public FilterDecision apply(T item) throws StreamFilterException;

    public void close() throws StreamCloseException;
}
