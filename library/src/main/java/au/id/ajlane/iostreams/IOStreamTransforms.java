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
 * Utility functions for working with instances of {@link IOStreamTransform}.
 */
@SuppressWarnings("StandardVariableNames")
public final class IOStreamTransforms
{
    @SuppressWarnings("rawtypes")
    private static final IOStreamTransform IDENTITY = new IOStreamTransform()
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
     * Provides the identity transform, which does not alter the items in the {@link IOStream}.
     *
     * @param <T>
     *     The type of the items in the original {@link IOStream}.
     * @param <R>
     *     The type of the items in the transformed {@link IOStream}.
     *
     * @return A {@link IOStreamTransform}.
     */
    @SuppressWarnings("unchecked")
    public static <T extends R, R> IOStreamTransform<T, R> identity()
    {
        return (IOStreamTransform<T, R>) IOStreamTransforms.IDENTITY;
    }

    /**
     * Joins two existing {@link IOStreamTransform} instances into a single {@code IOStreamTransform}.
     * <p>
     * Each transform is applied in sequence.
     *
     * @param a
     *     The first transform.
     * @param b
     *     The second transform.
     * @param <T>
     *     The type of the items in the original {@link IOStream}.
     * @param <I>
     *     The intermediate type of the transformed items.
     * @param <R>
     *     The type of the items in the transformed {@link IOStream}.
     *
     * @return A {@link IOStreamTransform}.
     */
    public static <T, I, R> IOStreamTransform<T, R> pipe(
        final IOStreamTransform<T, I> a,
        final IOStreamTransform<I, R> b
    )
    {
        return new AbstractIOStreamTransform<T, R>()
        {
            @Override
            public void close() throws Exception
            {
                try
                {
                    b.close();
                }
                finally
                {
                    a.close();
                }
            }

            @Override
            public R transform(final T item) throws Exception
            {
                return b.apply(a.apply(item));
            }
        };
    }

    /**
     * Joins three existing {@link IOStreamTransform} instances into a single {@code IOStreamTransform}.
     * <p>
     * Each transform is applied in sequence.
     *
     * @param a
     *     The first transform.
     * @param b
     *     The second transform.
     * @param c
     *     The third transform.
     * @param <T>
     *     The type of the items in the original {@link IOStream}.
     * @param <I1>
     *     The first intermediate type of the transformed items.
     * @param <I2>
     *     The second intermediate type of the transformed items.
     * @param <R>
     *     The type of the items in the transformed {@link IOStream}.
     *
     * @return A {@link IOStreamTransform}.
     */
    public static <T, I1, I2, R> IOStreamTransform<T, R> pipe(
        final IOStreamTransform<T, I1> a,
        final IOStreamTransform<I1, I2> b,
        final IOStreamTransform<I2, R> c
    )
    {
        return new AbstractIOStreamTransform<T, R>()
        {
            @Override
            public R transform(final T item) throws Exception
            {
                return c.apply(b.apply(a.apply(item)));
            }

            @Override
            public void close() throws Exception
            {
                try
                {
                    c.close();
                }
                finally
                {
                    try
                    {
                        b.close();
                    }
                    finally
                    {
                        c.close();
                    }
                }
            }
        };
    }

    /**
     * Joins four existing {@link IOStreamTransform} instances into a single {@code IOStreamTransform}.
     * <p>
     * Each transform is applied in sequence.
     *
     * @param a
     *     The first transform.
     * @param b
     *     The second transform.
     * @param c
     *     The third transform.
     * @param d
     *     The fourth transform.
     * @param <T>
     *     The type of the items in the original {@link IOStream}.
     * @param <I1>
     *     The first intermediate type of the transformed items.
     * @param <I2>
     *     The second intermediate type of the transformed items.
     * @param <I3>
     *     The third intermediate type of the transformed items.
     * @param <R>
     *     The type of the items in the transformed {@link IOStream}.
     *
     * @return A {@link IOStreamTransform}.
     */
    public static <T, I1, I2, I3, R> IOStreamTransform<T, R> pipe(
        final IOStreamTransform<T, I1> a,
        final IOStreamTransform<I1, I2> b,
        final IOStreamTransform<I2, I3> c,
        final IOStreamTransform<I3, R> d
    )
    {
        return new AbstractIOStreamTransform<T, R>()
        {
            @Override
            public R transform(final T item) throws Exception
            {
                return d.apply(c.apply(b.apply(a.apply(item))));
            }

            @Override
            public void close() throws Exception
            {
                try
                {
                    d.close();
                }
                finally
                {
                    try
                    {
                        c.close();
                    }
                    finally
                    {
                        try
                        {
                            b.close();
                        }
                        finally
                        {
                            a.close();
                        }
                    }
                }
            }
        };
    }

    /**
     * Joins five existing {@link IOStreamTransform} instances into a single {@code IOStreamTransform}.
     * <p>
     * Each transform is applied in sequence.
     * <p>
     * To join more than five transforms together, group the transforms in batches, then join the groups.
     *
     * @param a
     *     The first transform.
     * @param b
     *     The second transform.
     * @param c
     *     The third transform.
     * @param d
     *     The fourth transform.
     * @param e
     *     The fifth transform.
     * @param <T>
     *     The type of the items in the original {@link IOStream}.
     * @param <I1>
     *     The first intermediate type of the transformed items.
     * @param <I2>
     *     The second intermediate type of the transformed items.
     * @param <I3>
     *     The third intermediate type of the transformed items.
     * @param <I4>
     *     The fourth intermediate type of the transformed items.
     * @param <R>
     *     The type of the items in the transformed {@link IOStream}.
     *
     * @return A {@link IOStreamTransform}.
     */
    public static <T, I1, I2, I3, I4, R> IOStreamTransform<T, R> pipe(
        final IOStreamTransform<T, I1> a,
        final IOStreamTransform<I1, I2> b,
        final IOStreamTransform<I2, I3> c,
        final IOStreamTransform<I3, I4> d,
        final IOStreamTransform<I4, R> e
    )
    {
        return new AbstractIOStreamTransform<T, R>()
        {
            @Override
            public R transform(final T item) throws Exception
            {
                return e.apply(d.apply(c.apply(b.apply(a.apply(item)))));
            }

            @Override
            public void close() throws Exception
            {
                try
                {
                    e.close();
                }
                finally
                {
                    try
                    {
                        d.close();
                    }
                    finally
                    {
                        try
                        {
                            c.close();
                        }
                        finally
                        {
                            try
                            {
                                b.close();
                            }
                            finally
                            {
                                a.close();
                            }
                        }
                    }
                }
            }
        };
    }

    private IOStreamTransforms() throws InstantiationException
    {
        throw new InstantiationException("This class cannot be instantiated.");
    }
}
