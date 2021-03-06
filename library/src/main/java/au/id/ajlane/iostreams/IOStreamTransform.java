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
 * Transforms the items in a {@link IOStream} from one type to another.
 *
 * @param <T>
 *     The type of the items in the original {@code IOStream}.
 * @param <R>
 *     The type of the items in the transformed {@code IOStream}.
 *
 * @see IOStreams#map(IOStream, IOStreamTransform)
 * @see IOStreamables#map(IOStreamable, java.util.function.Supplier)
 */
@FunctionalInterface
public interface IOStreamTransform<T, R> extends AutoCloseable
{
    /**
     * Transforms a single item in the {@link IOStream}.
     *
     * @param item
     *     The item to transform.
     *
     * @return The transformed item.
     *
     * @throws Exception
     *     If the item cannot be transformed.
     */
    R apply(T item) throws Exception;

    /**
     * Releases any resources held by the {@code IOStreamTransform}.
     * <p>
     * Successive calls to {@code close()} should have no further effect.
     * <p>
     * The behaviour of a {@code IOStreamTransform} after its {@code close} method has been called is undefined.
     *
     * @throws Exception
     *     If the {@code IOStreamTransform} could not be closed for some reason. The {@code IOStreamTransform} may not
     *     release all resources if this is the case.
     */
    default void close() throws Exception
    {
    }
}
