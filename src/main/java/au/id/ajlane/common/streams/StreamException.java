package au.id.ajlane.common.streams;

import java.io.IOException;

public abstract class StreamException extends IOException {

    private static Exception checkCause(final Exception cause) {
        if (cause == null) {
            final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("The cause of a " + StreamException.class.getSimpleName() + " cannot be null.");
            illegalArgumentException.addSuppressed(cause);
            throw illegalArgumentException;
        }
        return cause;
    }

    private static String checkMessage(final String message, final Exception cause) {
        if (message == null || message.length() == 0) {
            final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("The message of a " + StreamException.class.getSimpleName() + " cannot be null or empty. Try to describe the cause of the exception in terms of what the stream is doing.");
            illegalArgumentException.addSuppressed(cause);
            throw illegalArgumentException;
        }
        return message;
    }

    public StreamException(final String message, final Exception cause) {
        super(checkMessage(message, cause), checkCause(cause));
    }
}
