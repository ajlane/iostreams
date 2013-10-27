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
 * A convenient base class for implementing {@link StreamTransform}.
 *
 * @param <T>
 *         The type of the items in the original {@code Stream}.
 * @param <R>
 *         The type of the items in the new {@code Stream}.
 */
public abstract class AbstractStreamTransform<T, R> implements StreamTransform<T, R>
{
    @SuppressWarnings("BooleanVariableAlwaysNegated")
    private boolean open = false;

    @Override
    public final R apply(final T item) throws StreamTransformException
    {
        if (!this.open)
        {
            this.open();
            this.open = true;
        }
        return this.transform(item);
    }

    @Override
    public void close() throws StreamCloseException
    {
        // Do nothing by default
    }

    /**
     * Prepares the transform to work.
     * <p/>
     * This method is called once by the base class before the first item is transformed.
     *
     * @throws StreamTransformException
     *         If there was any problem in preparing the transform.
     */
    protected void open() throws StreamTransformException
    {
        // Do nothing by default
    }

    /**
     * Transforms a single item in the {@link Stream}.
     * <p/>
     * This method is called by the base class when {@link #apply} is called.
     *
     * @param item
     *         The item to transform.
     * @return The transformed item.
     * @throws StreamTransformException
     *         If the item cannot be transformed.
     */
    protected abstract R transform(T item) throws StreamTransformException;
}
