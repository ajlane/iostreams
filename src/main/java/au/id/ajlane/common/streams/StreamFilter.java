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

import java.io.Closeable;

/**
 * Tests items in a {@code Stream} in order to remove them.
 *
 * @param <T>
 *         The type of the items in the {@code Stream}.
 * @see Streams#filter(Stream, StreamFilter)
 * @see Streamables#filter(Streamable, StreamFilter)
 */
public interface StreamFilter<T> extends Closeable
{
    /**
     * Tests the current item in the {@code Stream}.
     *
     * @param item
     *         The current item.
     * @return A {@link FilterDecision} declaring whether to keep the current value and whether to continue testing.
     * @throws StreamFilterException
     *         If the filter cannot make a decision.
     */
    FilterDecision apply(T item) throws StreamFilterException;

    /**
     * Releases any resources held by the {@code StreamFilter}.
     * <p/>
     * Successive calls to {@code close()} should have no further effect.
     * <p/>
     * The behaviour of a {@code StreamFilter} after its {@code close} method has been called is undefined. Typically,
     * such a {@code StreamFilter} would decide to {@link FilterDecision#SKIP_AND_TERMINATE} for all items.
     *
     * @throws StreamCloseException
     *         If the {@code StreamFilter} could not be closed for some reason. The {@code StreamFilter} may not release
     *         all resources if this is the case.
     */
    @Override
    void close() throws StreamCloseException;
}
