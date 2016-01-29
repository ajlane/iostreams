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
 * Indicates a problem when transforming in the items in a {@link IOStream}.
 */
public class IOStreamTransformException extends IOStreamReadException
{
    private static final long serialVersionUID = 2808271490532906032L;

    /**
     * Constructs a {@code IOStreamTransformException} with the given message and cause.
     *
     * @param message
     *         A message describing the exception in terms of the transformed {@link IOStream}. Must not be empty or
     *         {@code null}.
     * @param cause
     *         The underlying cause of the issue. Must not be {@code null}.
     * @throws IllegalArgumentException
     *         If the message is empty.
     * @throws NullPointerException
     *         If either the message or the cause is {@code null}.
     */
    public IOStreamTransformException(final String message, final Exception cause)
    {
        super(message, cause);
    }
}
