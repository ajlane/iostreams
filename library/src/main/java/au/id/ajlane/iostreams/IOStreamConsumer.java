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
 * A function which recieves/consumes items.
 *
 * @param <T>
 *     The type of the items.
 */
@FunctionalInterface
public interface IOStreamConsumer<T> extends AutoCloseable
{
    /**
     * Represents the state of the consumer.
     */
    enum IOStreamConsumerState
    {
        /**
         * The consumer is expecting to consume more items.
         */
        CONTINUE,
        /**
         * The consumer is not expecting to consumer any more items.
         */
        BREAK
    }

    /**
     * Provides an item for the consumer to consume.
     *
     * @param item
     *     The item to consume.
     *
     * @return The state of the consumer immediately after consuming the item. Consumers are free to change their state
     * at any time, so this value may not represent the current state of the consumer.
     *
     * @throws Exception
     *     If there was a problem with consuming the item.
     */
    IOStreamConsumerState accept(T item) throws Exception;

    @Override
    default void close() throws Exception
    {
    }

    static <T> IOStreamConsumer<? super T> none()
    {
        return (IOStreamConsumer<Object>) item -> IOStreamConsumerState.BREAK;
    }

    static <T> IOStreamConsumer<? super T> ignoreAll()
    {
        return (IOStreamConsumer<Object>) item -> IOStreamConsumerState.CONTINUE;
    }
}
