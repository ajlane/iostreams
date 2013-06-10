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
 * Indicates a problem when reading from a {@link Stream}.
 */
public class StreamReadException extends StreamException {

    /**
     * Constructs a new {@link StreamReadException} with the given message and cause.
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
    public StreamReadException(final String message, final Exception cause) {
        super(message, cause);
    }
}
