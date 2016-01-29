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

package au.id.ajlane.iostreams;

/**
 * Indicates that the resources held by a {@link IOStream} could not be released.
 */
public class IOStreamCloseException extends IOStreamException
{
    private static final long serialVersionUID = -4458433200743954325L;

    /**
     * Constructs a new {@code IOStreamCloseException} with the given message and cause.
     *
     * @param message
     *         A message describing the issue in terms of the {@link IOStream}. Must not be empty of {@code null}.
     * @param cause
     *         The underlying cause of the issue. Must not be {@code null}.
     * @throws IllegalArgumentException
     *         If the message is empty.
     * @throws NullPointerException
     *         If the either the message or the cause is {@code null}.
     */
    public IOStreamCloseException(final String message, final Exception cause)
    {
        super(message, cause);
    }
}