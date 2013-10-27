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
public abstract class StreamException extends IOException
{
    private static final long serialVersionUID = 2772263250587555205L;

    private static Exception checkCause(final Exception cause)
    {
        if (cause == null)
        {
            final NullPointerException error = new NullPointerException(
                    "The cause of a " + StreamException.class.getSimpleName() + " cannot be null."
            );
            error.addSuppressed(cause);
            throw error;
        }
        return cause;
    }

    private static String checkMessage(final String message, final Exception cause)
    {
        if (message == null)
        {
            final NullPointerException error = new NullPointerException(
                    "The message of a " + StreamException.class.getSimpleName() +
                    " cannot be null. Try to describe the cause of the exception in terms of what the stream is doing."
            );
            error.addSuppressed(cause);
            throw error;
        }
        if (message.isEmpty())
        {
            final IllegalArgumentException error = new IllegalArgumentException(
                    "The message of a " + StreamException.class.getSimpleName() +
                    " cannot empty. Try to describe the cause of the exception in terms of what the stream is doing."
            );
            error.addSuppressed(cause);
            throw error;
        }
        return message;
    }

    /**
     * Constructs a new {@code StreamException} with the given message and cause.
     *
     * @param message
     *         A message describing the exception in terms of the {@link Stream}. Must not be empty or {@code null}.
     * @param cause
     *         The underlying cause of the issue. Must not be {@code null}.
     * @throws IllegalArgumentException
     *         If the message is empty.
     * @throws NullPointerException
     *         If either the message or the cause is {@code null}.
     */
    protected StreamException(final String message, final Exception cause)
    {
        super(StreamException.checkMessage(message, cause), StreamException.checkCause(cause));
    }
}
