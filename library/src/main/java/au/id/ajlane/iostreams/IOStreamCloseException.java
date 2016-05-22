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

/**
 * Indicates that the resources held by a {@link IOStream} could not be released.
 */
public final class IOStreamCloseException extends IOStreamException
{
    private static final long serialVersionUID = -8405057477078853214L;

    /**
     * Constructs a new {@code IOStreamCloseException} with the given cause.
     *
     * @param cause
     *     The underlying cause of the issue. Must be a non-{@code null}, checked exception. An {@link
     *     IOStreamException} will be replaced by its own cause.
     *
     * @throws RuntimeException
     *     If the cause is a runtime exception.
     * @throws NullPointerException
     *     If the cause is {@code null}.
     */
    public IOStreamCloseException(final Exception cause)
    {
        super("Could not close the stream.", cause);
    }
}
