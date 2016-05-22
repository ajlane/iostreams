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
 * A wrapper for streams that provides additional metrics for testing.
 *
 * @param <T>
 *     The type of the items in the stream.
 */
public class TestStream<T> implements IOStream<T>
{
    /**
     * Wraps another stream to provide test metrics.
     *
     * @param stream
     *     The stream to wrap.
     * @param <T>
     *     The type of the items in the stream.
     *
     * @return A test stream that delegates to the given stream.
     */
    public static <T> TestStream<T> wrap(final IOStream<T> stream)
    {
        return new TestStream<>(stream);
    }

    /**
     * Creates a test stream containing the given items.
     *
     * @param items
     *     The items that the stream should provide.
     * @param <T>
     *     The type of the items.
     *
     * @return A test stream.
     */
    @SafeVarargs
    public static <T> TestStream<T> of(final T... items)
    {
        return wrap(IOStreams.fromArray(items));
    }
    private final IOStream<T> stream;
    private boolean closed = false;

    private TestStream(final IOStream<T> stream)
    {
        this.stream = stream;
    }

    @Override
    public void close() throws IOStreamCloseException
    {
        stream.close();
        closed = true;
    }

    @Override
    public boolean hasNext() throws IOStreamReadException
    {
        return stream.hasNext();
    }

    /**
     * Checks that the close method has actually been called.
     *
     * @return {@code true} if {@link #close()} has been called at least once.
     */
    public boolean isClosed()
    {
        return closed;
    }

    @Override
    public T next() throws IOStreamReadException
    {
        return stream.next();
    }
}
