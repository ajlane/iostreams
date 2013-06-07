package au.id.ajlane.common.streams;

public class StreamFilterException extends StreamReadException {
    public StreamFilterException(final String message, final Exception cause) {
        super(message, cause);
    }
}
