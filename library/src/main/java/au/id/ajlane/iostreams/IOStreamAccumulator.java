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
 * A function which accumulates items into a result.
 *
 * @param <R>
 *     The type of the result.
 * @param <T>
 *     The type of the items.
 */
@FunctionalInterface
public interface IOStreamAccumulator<R, T> extends AutoCloseable
{
    /**
     * Adds an item to the previous result, returning the new result.
     *
     * @param result
     *     The intermediary result.
     * @param item
     *     The new item to accumulate.
     *
     * @return The new result.
     *
     * @throws Exception
     *     If there was a problem while accumulating the item.
     */
    R add(R result, T item) throws Exception;

    @Override
    default void close() throws Exception
    {
    }
}
