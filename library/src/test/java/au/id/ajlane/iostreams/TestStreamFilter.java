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
 *     The type of the items being filtered.
 */
public class TestStreamFilter<T> implements IOStreamFilter<T>
{
    private final IOStreamFilter<T> filter;

    /**
     * Wraps another filter to provide test metrics.
     *
     * @param filter
     *     The filter to wrap.
     * @param <T>
     *     The type of the items being filtered.
     *
     * @return A test filter that delegates to the given filter.
     */
    public static <T> TestStreamFilter<T> wrap(final IOStreamFilter<T> filter)
    {
        return new TestStreamFilter<>(filter);
    }

    private TestStreamFilter(final IOStreamFilter<T> filter)
    {
        this.filter = filter;
    }

    private boolean closed = false;

    @Override
    public void close() throws Exception
    {
        filter.close();
        closed = true;
    }

    @Override
    public FilterDecision apply(final T item) throws Exception
    {
        return filter.apply(item);
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
}
