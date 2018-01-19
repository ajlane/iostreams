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
 * An extended interface for streams which support peeking at upcoming items.
 *
 * @param <T>
 *     The type of the items in the stream.
 */
public interface PeekableIOStream<T> extends IOStream<T>
{
    /**
     * Peeks at the next item in the stream.
     * <p>
     * Useful if the stream is guaranteed to have a next item. Otherwise, use {@link #peek(int)} with a size of {@code
     * 1}.
     *
     * @return The next item in the stream.
     *
     * @throws IOStreamReadException
     *     If there was a problem with reading the stream.
     * @throws java.util.NoSuchElementException
     *     If there is no next item in the stream.
     * @throws InterruptedException
     *     If the thread was interrupted.
     */
    default T peek() throws IOStreamReadException, InterruptedException
    {
        return this.peek(1)
            .iterator()
            .next();
    }

    /**
     * Peeks at the next n items in the stream.
     *
     * @param n
     *     The number of items to peek at.
     *
     * @return An iterable set of items. May be smaller than the given size if the stream cannot provide enough values.
     *
     * @throws IOStreamReadException
     *     If there was a problem with reading the stream.
     * @throws InterruptedException
     *     If the thread was interrupted.
     */
    Iterable<T> peek(int n) throws IOStreamReadException, InterruptedException;
}