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

/**
 * Transforms the items in a {@link Stream} from one type to another.
 *
 * @param <T>
 *         The type of the items in the original {@code Stream}.
 * @param <R>
 *         The type of the items in the transformed {@code Stream}.
 * @see Streams#transform(Stream, StreamTransform)
 * @see Streamables#transform(Streamable, StreamTransform)
 */
public interface StreamTransform<T, R>
{
    /**
     * Transforms a single item in the {@link Stream}.
     *
     * @param item
     *         The item to transform.
     * @return The transformed item.
     * @throws StreamTransformException
     *         If the item cannot be transformed.
     */
    R apply(T item) throws StreamTransformException;

    /**
     * Releases any resources held by the {@code StreamTransform}.
     * <p/>
     * Successive calls to {@code close()} should have no further effect.
     * <p/>
     * The behaviour of a {@code StreamTransform} after its {@code close} method has been called is undefined.
     *
     * @throws StreamCloseException
     *         If the {@code StreamFilter} could not be closed for some reason. The {@code StreamFilter} may not release
     *         all resources if this is the case.
     */
    void close() throws StreamCloseException;
}
