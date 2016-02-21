/*
 * Copyright 2016 Aaron Lane
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

package au.id.ajlane.iostreams;

import java.io.IOException;

/**
 * The base class for exceptions thrown by {@link IOStream}.
 *
 * @see IOStreamReadException
 * @see IOStreamCloseException
 */
public abstract class IOStreamException extends IOException
{
    private static final long serialVersionUID = 2772263250587555205L;

    private static Exception checkCause(final Exception cause)
    {
        if (cause == null)
        {
            throw new NullPointerException(
                "The cause of a " + IOStreamException.class.getSimpleName() + " cannot be null."
            );
        }
        return cause;
    }

    private static String checkMessage(final String message, final Exception cause)
    {
        if (message == null)
        {
            final NullPointerException error = new NullPointerException(
                "The message cannot be null."
                    + " Try to describe the cause of the exception in terms of what the stream is doing."
            );
            error.addSuppressed(cause);
            throw error;
        }
        if (message.isEmpty())
        {
            final IllegalArgumentException error = new IllegalArgumentException(
                "The message cannot be empty."
                    + " Try to describe the cause of the exception in terms of what the stream is doing."
            );
            error.addSuppressed(cause);
            throw error;
        }
        return message;
    }

    /**
     * Constructs a new {@code IOStreamException} with the given message and cause.
     *
     * @param message
     *     A message describing the exception in terms of the {@link IOStream}. Must not be empty or {@code null}.
     * @param cause
     *     The underlying cause of the issue. Must not be {@code null}.
     *
     * @throws IllegalArgumentException
     *     If the message is empty.
     * @throws NullPointerException
     *     If either the message or the cause is {@code null}.
     */
    protected IOStreamException(final String message, final Exception cause)
    {
        super(IOStreamException.checkMessage(message, cause), IOStreamException.checkCause(cause));
    }
}
