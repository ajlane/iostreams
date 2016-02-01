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

package au.id.ajlane.iostreams;

/**
 * A convenient base class for implementing {@link IOStreamConsumer}.
 *
 * @param <T>
 *         The type of the items in the {@code IOStream}.
 */
public abstract class AbstractIOStreamConsumer<T> implements IOStreamConsumer<T>
{
    private boolean open = false;

    @Override
    public final void accept(final T item) throws Exception
    {
        if (!this.open)
        {
            this.open();
            this.open = true;
        }
        this.consume(item);
    }

    @Override
    public void close() throws Exception
    {
        // Do nothing by default
    }

    /**
     * Prepares the consumer to work.
     * <p>
     * This method is called once by the base class before the first item is consumed.
     *
     * @throws Exception
     *         If there was any problem in preparing the consumer.
     */
    protected void open() throws Exception
    {
        // Do nothing by default
    }

    /**
     * Consumes a single item in the {@link IOStream}.
     * <p>
     * This method is called by the base class when {@link #accept} is called.
     *
     * @param item
     *         The item to consume.
     * @throws Exception
     *         If the item cannot be consumed.
     */
    protected abstract void consume(T item) throws Exception;
}
