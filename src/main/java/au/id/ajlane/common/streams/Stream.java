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
 * An iterator over a stream of items.
 * <p/>
 * Unlike {@link java.util.Iterator}, this class accommodates streams which are backed by heavy-weight resources
 * (sockets, databases, etc.), require blocking calculations, or require clean-up when the consumer has finishes
 * iterating.
 * <p/>
 * Consider the easier-to-use {@link AbstractStream} when implementing a new {@code Stream}.
 * <p/>
 * Utility methods on {@link Streams} can make working with {@code Stream}s easier: Use {@link Streams#transform} to
 * modify the items in a {@code Stream}, or {@link Streams#filter} to remove particular items. Join multiple {@code
 * Stream}s together with {@link Streams#concat}.
 * <p/>
 * When defining public interfaces, consider carefully whether you require a {@code Stream} or an {@code Streamable}.
 *
 * @param <T>
 *         The type of the items in the {@code Stream}
 */
public interface Stream<T> extends Closeable {

    /**
     * Releases any resources held by the {@code Stream}.
     * <p/>
     * Successive calls to {@code close()} should have no further effect.
     * <p/>
     * The behaviour of a {@code Stream} after its {@code close} method has been called is undefined. Typically, such a
     * {@code Stream} would behave as if it contained no more items (by returning {@code false} from {@link #hasNext}).
     *
     * @throws StreamCloseException
     *         If the {@code Stream} could not be closed for some reason. The {@code Stream} may not release all
     *         resources if this is the case.
     */
    @Override
    public void close() throws StreamCloseException;

    /**
     * Checks if there are any more items in the {@code Stream}.
     * <p/>
     * It is not uncommon for significant work to be necessary in order to calculate {@code hasNext}. Typically,
     * implementations not be able to determine if there is a next item without fetching and buffering it.
     * <p/>
     * If the thread is interrupted before this method returns, implementations may choose to throw a {@link
     * StreamReadException} with a {@link InterruptedException} as the cause.
     *
     * @return {@code true} if a subsequent call to {@link #next} will succeed. {@code false} otherwise.
     * @throws StreamReadException
     *         If there was any problem in reading from the underlying resource.
     */
    public boolean hasNext() throws StreamReadException;

    /**
     * Returns the next item in the {@code Stream}.
     * <p/>
     * If the thread is interrupted before this method returns, implementations may choose to throw a {@link
     * StreamReadException} with a {@link InterruptedException} as the cause.
     *
     * @return The next item in the {@code Stream}. {@code null} is a valid item, although discouraged.
     * @throws java.util.NoSuchElementException
     *         If there is no next item (calling {@link #hasNext} before this method would have returned {@code
     *         false}).
     * @throws StreamReadException
     *         If there was any problem in reading from the underlying resource.
     */
    public T next() throws StreamReadException;
}
