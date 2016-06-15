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
 * A predicate function that requires a pair of parameters.
 *
 * @param <A> The type of the first parameter.
 * @param <B> The type of the second parameter.
 */
@FunctionalInterface
public interface IOStreamBiPredicate<A, B> extends AutoCloseable
{

    @Override
    default void close() throws Exception
    {
    }

    /**
     * Inverts the predicate.
     *
     * @return A view of the predicate with the result inverted.
     */
    default IOStreamBiPredicate<A, B> invert()
    {
        return IOStreamPredicates.invert(this);
    }

    /**
     * Tests whether a pair of items match the predicate.
     *
     * @param a
     *     The first item to test.
     * @param b
     *     The second item to test.
     *
     * @return {@code true} if the pair matches the predicate.
     *
     * @throws Exception
     *     If there was a problem with testing the pair.
     */
    boolean test(final A a, final B b) throws Exception;
}
