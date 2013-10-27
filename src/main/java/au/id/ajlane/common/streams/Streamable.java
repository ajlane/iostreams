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
 * Provides reusable access to a stream of items.
 * <p/>
 * For once-off streams, prefer to use {@link Stream} directly.
 * <p/>
 * Utility methods on {@link Streamables} can make working with {@code Streamable}s easier: Use {@link
 * Streamables#transform} to modify the items in the provided {@code Stream}s, or {@link Streamables#filter} to remove
 * particular items. Join the {@code Streams}s from multiple {@code Streamable}s together with {@link
 * Streamables#concat}.
 *
 * @param <T>
 *         The type of the items in the {@code Streamable}.
 */
public interface Streamable<T>
{
    /**
     * Provides a ready {@link Stream} to iterate over the items in this {@code Streamable}.
     *
     * @return An instance of {@link Stream}.
     */
    Stream<T> stream();
}
