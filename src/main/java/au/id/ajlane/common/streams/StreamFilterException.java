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

/**
 * Indicates a problem when filtering a {@link Stream}.
 */
public class StreamFilterException extends StreamReadException
{
    private static final long serialVersionUID = 6468977132736522343L;

    /**
     * Constructs a new {@code StreamFilterException} with the given message and cause.
     *
     * @param message
     *         A message describing the exception in terms of the filtered {@link Stream}. Must not be empty or {@code
     *         null}.
     * @param cause
     *         The underlying cause of the issue. Must not be {@code null}.
     * @throws IllegalArgumentException
     *         If the message is empty.
     * @throws NullPointerException
     *         If either the message or the cause is {@code null}.
     */
    public StreamFilterException(final String message, final Exception cause)
    {
        super(message, cause);
    }
}
