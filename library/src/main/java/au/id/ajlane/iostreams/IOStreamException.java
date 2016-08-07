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
 * Represents the failure of an {@link IOStream} to produce or process items.
 */
public class IOStreamException extends IOException
{
    private static Exception fixCause(final Exception cause)
    {
        if (cause == null)
        {
            throw new NullPointerException("The cause cannot be null.");
        }
        else if (cause instanceof RuntimeException)
        {
            throw (RuntimeException) cause;
        }
        else if (cause instanceof IOStreamException)
        {
            final Exception inner = ((IOStreamException) cause).getCause();
            for (final Throwable suppressed : cause.getSuppressed())
            {
                inner.addSuppressed(suppressed);
            }
            return fixCause(inner);
        }
        else
        {
            return cause;
        }
    }

    /**
     * Constructs a new {@code IOStreamException} with the given message and cause.
     *
     * @param cause
     *     The underlying cause of the issue. Must not be {@code null}. An {@code IOStreamException} will be replaced
     *     with it's own cause.
     *
     * @throws RuntimeException
     *     if the given cause is a runtime exception.
     * @throws NullPointerException
     *     if the given cause is {@code null}.
     */
    public IOStreamException(final Exception cause)
    {
        super(fixCause(cause));
    }

    /**
     * The cause of the issue.
     * <p>
     * The cause of an {@code IOStreamException} is always a checked exception, and is never another {@link
     * IOStreamException}.
     *
     * @return A non-null checked exception.
     */
    @Override
    public Exception getCause()
    {
        return (Exception) super.getCause();
    }
}
