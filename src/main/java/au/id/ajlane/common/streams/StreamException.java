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

import java.io.IOException;

/**
 * The base class for exceptions thrown by {@link Stream}.
 *
 * @see StreamReadException
 * @see StreamCloseException
 */
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

    /**
     * Constructs a new {@link StreamException} with the given message and cause.
     * <p/>
     * Note that both a message and a cause <i>must</i> be provided.
     *
     * @param message
     *         A message describing the exception in terms of the {@code Stream}.
     * @param cause
     *         The underlying cause of the issue.
     * @throws IllegalArgumentException
     *         If the message is {@code null} or empty, or the cause is {@code null}.
     */
    public StreamException(final String message, final Exception cause) {
        super(checkMessage(message, cause), checkCause(cause));
    }
}
