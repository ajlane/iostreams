package au.id.ajlane.common.streams;

public class StreamTransformException extends StreamReadException {
    public StreamTransformException(final String message, final Exception cause) {
        super(message, cause);
    }
}
