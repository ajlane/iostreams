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

import java.util.Objects;

/**
 * Utilities for working with instances of {@link IOStreamPredicate}.
 */
public abstract class IOStreamPredicates
{
    /**
     * Inverts a predicate to match the opposite set of items.
     *
     * @param predicate
     *     The predicate to invert. Must not be null.
     * @param <T>
     *     The type of the items.
     *
     * @return An inverted predicate.
     */
    public static <T> IOStreamPredicate<T> invert(final IOStreamPredicate<T> predicate)
    {
        Objects.requireNonNull(predicate, "The predicate must not be null.");
        return new IOStreamPredicate<T>()
        {
            @Override
            public void close() throws Exception
            {
                predicate.close();
            }

            @Override
            public IOStreamPredicate<T> invert()
            {
                return predicate;
            }

            @Override
            public boolean test(T item) throws Exception
            {
                return !predicate.test(item);
            }
        };
    }

    /**
     * Inverts a predicate to match the opposite set of pairs.
     *
     * @param predicate
     *     The predicate to invert. Must not be null.
     * @param <A>
     *     The type of the first item in the pairs.
     * @param <B>
     *     The type of the second item in the pairs.
     *
     * @return An inverted predicate.
     */
    public static <A, B> IOStreamBiPredicate<A, B> invert(final IOStreamBiPredicate<A, B> predicate)
    {
        return new IOStreamBiPredicate<A, B>()
        {
            @Override
            public void close() throws Exception
            {
                predicate.close();
            }

            @Override
            public IOStreamBiPredicate<A, B> invert()
            {
                return predicate;
            }

            @Override
            public boolean test(A a, B b) throws Exception
            {
                return !predicate.test(a, b);
            }
        };
    }

    private IOStreamPredicates()
    {
    }
}
