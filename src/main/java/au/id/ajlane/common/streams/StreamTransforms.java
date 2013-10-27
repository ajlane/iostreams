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
 * Utility functions for working with instances of {@link StreamTransform}.
 */
@SuppressWarnings("StandardVariableNames")
public final class StreamTransforms
{
    @SuppressWarnings("rawtypes")
    private static final StreamTransform IDENTITY = new StreamTransform()
    {
        @Override
        public Object apply(final Object item)
        {
            return item;
        }

        @Override
        public void close()
        {
        }
    };

    /**
     * Provides the identity transform, which does not alter the items in the {@link Stream}.
     *
     * @param <T>
     *         The type of the items in the original {@link Stream}.
     * @param <R>
     *         The type of the items in the transformed {@link Stream}.
     * @return A {@link StreamTransform}.
     */
    @SuppressWarnings("unchecked")
    public static <T extends R, R> StreamTransform<T, R> identity()
    {
        return (StreamTransform<T, R>) StreamTransforms.IDENTITY;
    }

    /**
     * Joins two existing {@link StreamTransform} instances into a single {@code StreamTransform}.
     * <p/>
     * Each transform is applied in sequence.
     *
     * @param a
     *         The first transform.
     * @param b
     *         The second transform.
     * @param <T>
     *         The type of the items in the original {@link Stream}.
     * @param <I>
     *         The intermediate type of the transformed items.
     * @param <R>
     *         The type of the items in the transformed {@link Stream}.
     * @return A {@link StreamTransform}.
     */
    public static <T, I, R> StreamTransform<T, R> pipe(final StreamTransform<T, I> a, final StreamTransform<I, R> b)
    {
        return new AbstractStreamTransform<T, R>()
        {
            @Override
            public R transform(final T item) throws StreamTransformException
            {
                return b.apply(a.apply(item));
            }
        };
    }

    /**
     * Joins three existing {@link StreamTransform} instances into a single {@code StreamTransform}.
     * <p/>
     * Each transform is applied in sequence.
     *
     * @param a
     *         The first transform.
     * @param b
     *         The second transform.
     * @param c
     *         The third transform.
     * @param <T>
     *         The type of the items in the original {@link Stream}.
     * @param <I1>
     *         The first intermediate type of the transformed items.
     * @param <I2>
     *         The second intermediate type of the transformed items.
     * @param <R>
     *         The type of the items in the transformed {@link Stream}.
     * @return A {@link StreamTransform}.
     */
    public static <T, I1, I2, R> StreamTransform<T, R> pipe(final StreamTransform<T, I1> a, final StreamTransform<I1, I2> b, final StreamTransform<I2, R> c)
    {
        return new AbstractStreamTransform<T, R>()
        {
            @Override
            public R transform(final T item) throws StreamTransformException
            {
                return c.apply(b.apply(a.apply(item)));
            }
        };
    }

    /**
     * Joins four existing {@link StreamTransform} instances into a single {@code StreamTransform}.
     * <p/>
     * Each transform is applied in sequence.
     *
     * @param a
     *         The first transform.
     * @param b
     *         The second transform.
     * @param c
     *         The third transform.
     * @param d
     *         The fourth transform.
     * @param <T>
     *         The type of the items in the original {@link Stream}.
     * @param <I1>
     *         The first intermediate type of the transformed items.
     * @param <I2>
     *         The second intermediate type of the transformed items.
     * @param <I3>
     *         The third intermediate type of the transformed items.
     * @param <R>
     *         The type of the items in the transformed {@link Stream}.
     * @return A {@link StreamTransform}.
     */
    public static <T, I1, I2, I3, R> StreamTransform<T, R> pipe(final StreamTransform<T, I1> a, final StreamTransform<I1, I2> b, final StreamTransform<I2, I3> c, final StreamTransform<I3, R> d)
    {
        return new AbstractStreamTransform<T, R>()
        {
            @Override
            public R transform(final T item) throws StreamTransformException
            {
                return d.apply(c.apply(b.apply(a.apply(item))));
            }
        };
    }

    /**
     * Joins five existing {@link StreamTransform} instances into a single {@code StreamTransform}.
     * <p/>
     * Each transform is applied in sequence.
     * <p/>
     * To join more than five transforms together, group the transforms in batches, then join the groups.
     *
     * @param a
     *         The first transform.
     * @param b
     *         The second transform.
     * @param c
     *         The third transform.
     * @param d
     *         The fourth transform.
     * @param e
     *         The fifth transform.
     * @param <T>
     *         The type of the items in the original {@link Stream}.
     * @param <I1>
     *         The first intermediate type of the transformed items.
     * @param <I2>
     *         The second intermediate type of the transformed items.
     * @param <I3>
     *         The third intermediate type of the transformed items.
     * @param <I4>
     *         The fourth intermediate type of the transformed items.
     * @param <R>
     *         The type of the items in the transformed {@link Stream}.
     * @return A {@link StreamTransform}.
     */
    public static <T, I1, I2, I3, I4, R> StreamTransform<T, R> pipe(final StreamTransform<T, I1> a, final StreamTransform<I1, I2> b, final StreamTransform<I2, I3> c, final StreamTransform<I3, I4> d, final StreamTransform<I4, R> e)
    {
        return new AbstractStreamTransform<T, R>()
        {
            @Override
            public R transform(final T item) throws StreamTransformException
            {
                return e.apply(d.apply(c.apply(b.apply(a.apply(item)))));
            }
        };
    }

    private StreamTransforms() throws InstantiationException
    {
        throw new InstantiationException("This class cannot be instantiated.");
    }
}
