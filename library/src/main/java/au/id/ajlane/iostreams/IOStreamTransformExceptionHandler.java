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
 * Handles exceptions thrown by a {@link IOStreamTransform}.
 * <p>
 * When used with {@link IOStream#map(IOStreamTransform, IOStreamTransformExceptionHandler)}, this allows problematic
 * items to be retroactively filtered, without modifying the transform.
 *
 * @param <T>
 *     The type of the items being transformed.
 */
@FunctionalInterface
public interface IOStreamTransformExceptionHandler<T> extends AutoCloseable
{
    @Override
    default void close() throws Exception
    {
    }

    /**
     * Handles a failure from the transform.
     * <p>
     * If this method returns {@link FilterDecision#KEEP_AND_CONTINUE} or {@link FilterDecision#KEEP_AND_TERMINATE}, the
     * stream should terminate with an exception as usual. To terminate without exception, this method should return
     * {@link FilterDecision#SKIP_AND_TERMINATE}. To skip just this item, it should return {@link
     * FilterDecision#SKIP_AND_CONTINUE}.
     *
     * @param item
     *     The item which could not be transformed.
     * @param ex
     *     The checked exception thrown by the transform.
     *
     * @return A filter decision.
     *
     * @throws Exception
     *     If the item or exception could not be handled.
     */
    FilterDecision handle(T item, Exception ex) throws Exception;
}
