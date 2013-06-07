package au.id.ajlane.common.streams;

public class StreamCloseException extends StreamException {
    public StreamCloseException(final String message, final Exception cause) {
        super(message, cause);
    }
}
