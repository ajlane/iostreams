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
 * A predicate for testing whether items match some criteria.
 *
 * @param <T> The type of the items.
 */
@FunctionalInterface
public interface IOStreamPredicate<T> extends AutoCloseable
{
    @Override
    default void close() throws Exception
    {
    }

    /**
     * Inverts the predicate to match the opposite set of values.
     * @return An inverted predicate.
     */
    default IOStreamPredicate<T> invert()
    {
        return IOStreamPredicates.invert(this);
    }

    /**
     * Tests whether an item matches the predicate.
     *
     * @param item The item to test.
     * @return {@code true} if the item matches, {@code false} otherwise.
     * @throws Exception If there was a problem with testing the item.
     */
    boolean test(final T item) throws Exception;
}
