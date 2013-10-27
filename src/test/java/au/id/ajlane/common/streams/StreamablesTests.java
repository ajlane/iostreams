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

import java.util.*;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link Streamables}.
 */
@SuppressWarnings({"ProhibitedExceptionCaught", "StandardVariableNames"})
public class StreamablesTests
{
    private static final String[] EMPTY = {};

    /**
     * Tests {@link Streamables#concat(Streamable[])}.
     *
     * @throws StreamException
     *         If any {@link Stream} fails. Should not occur.
     */
    @Test
    public void testConcatArrayOfStreamables() throws StreamException
    {
        final Streamable<String> a = Streamables.fromArray("a1");
        final Streamable<String> b = Streamables.fromArray("b1");

        Assert.assertArrayEquals(
                new String[]{"a1", "b1"},
                Streamables.toArray(Streamables.concat(a, b))
        );
        Assert.assertArrayEquals(
                new String[]{"a1", "b1"},
                Streamables.toArray(Streamables.concat(a, b))
        );

        final Streamable<String> c = Streamables.fromArray("c1", "c2", "c3");
        final Streamable<String> d = Streamables.fromArray("d1", "d2");
        final Streamable<String> e = Streamables.fromArray("e1", "e2", "e3", "e4");

        Assert.assertArrayEquals(
                new String[]{"c1", "c2", "c3", "d1", "d2", "e1", "e2", "e3", "e4"},
                Streamables.toArray(Streamables.concat(c, d, e))
        );

        try
        {
            Streamables.concat((Streamable<String>[]) null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        final Streamable<String> g = Streamables.concat(null, null);
        try (final Stream<String> stream = g.stream())
        {
            stream.hasNext();
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    /**
     * Tests {@link Streamables#concat(Streamable)}.
     *
     * @throws StreamException
     *         If any {@link Stream} fails. Should not occur.
     */
    @Test
    public void testConcatStreamableOfStreamables() throws StreamException
    {
        final Streamable<String> a = Streamables.fromArray("a1");
        final Streamable<String> b = Streamables.fromArray("b1");

        Assert.assertArrayEquals(
                new String[]{"a1", "b1"},
                Streamables.toArray(Streamables.concat(Streamables.fromArray(a, b)))
        );
        Assert.assertArrayEquals(
                new String[]{"a1", "b1"},
                Streamables.toArray(Streamables.concat(Streamables.fromArray(a, b)))
        );

        final Streamable<String> c = Streamables.fromArray("c1", "c2", "c3");
        final Streamable<String> d = Streamables.fromArray("d1", "d2");
        final Streamable<String> e = Streamables.fromArray("e1", "e2", "e3", "e4");

        Assert.assertArrayEquals(
                new String[]{"c1", "c2", "c3", "d1", "d2", "e1", "e2", "e3", "e4"},
                Streamables.toArray(Streamables.concat(Streamables.fromArray(c, d, e)))
        );

        try
        {
            Streamables.concat((Streamable<Streamable<String>>) null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        final Streamable<String> g = Streamables.concat(Streamables.<Streamable<String>>fromArray(null, null));
        try (final Stream<String> stream = g.stream())
        {
            stream.hasNext();
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    /**
     * Tests {@link Streamables#empty()}.
     *
     * @throws StreamException
     *         If a {@link Stream} fails. Should not occur.
     */
    @Test
    public void testEmptyStreamable() throws StreamException
    {
        final Streamable<String> a = Streamables.empty();
        try (final Stream<String> stream = a.stream())
        {
            Assert.assertFalse(stream.hasNext());
            try
            {
                stream.next();
                Assert.fail();
            }
            catch (final NoSuchElementException ex)
            {
                // Expected
            }
        }
    }

    /**
     * Tests {@link Streamables#filter(Streamable, StreamFilter)}.
     *
     * @throws StreamException
     *         If a {@code Stream} fails. Should not occur.
     */
    @Test
    public void testFilter() throws StreamException
    {
        Assert.assertArrayEquals(
                new String[]{"a1", "a2", "a3"},
                Streamables.toArray(
                        Streamables.filter(
                                Streamables.fromArray("a1", "a2", "a3"),
                                StreamFilters.<String>all()
                        )
                )
        );
        Assert.assertArrayEquals(
                StreamablesTests.EMPTY,
                Streamables.toArray(
                        Streamables.filter(
                                Streamables.fromArray("b1", "b2", "b3"),
                                StreamFilters.<String>none()
                        )
                )
        );
        Assert.assertArrayEquals(
                new String[]{"c2", "c3"},
                Streamables.toArray(
                        Streamables.filter(
                                Streamables.fromArray("c1", "c2", "c3", "c4"),
                                StreamFilters.whitelist("c2", "c3")
                        )
                )
        );

        Assert.assertArrayEquals(
                StreamablesTests.EMPTY,
                Streamables.toArray(
                        Streamables.filter(
                                Streamables.<String>empty(),
                                StreamFilters.whitelist("c2", "c3")
                        )
                )
        );

        try
        {
            Streamables.filter(null, StreamFilters.all());
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
        try
        {
            Streamables.filter(Streamables.empty(), null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
        try
        {
            Streamables.filter(null, null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    /**
     * Tests {@link Streamables#flattenArrays(Streamable)}.
     *
     * @throws StreamException
     *         If any {@code Stream} fails. Should not occur.
     */
    @Test
    public void testFlattenArrays() throws StreamException
    {
        Assert.assertArrayEquals(
                new String[]{"a1", "a2", "b1", "b2", "b3", "c1"},
                Streamables.toArray(
                        Streamables.flattenArrays(
                                Streamables.fromArray(
                                        new String[]{
                                                "a1",
                                                "a2"
                                        }, new String[]{"b1", "b2", "b3"}, new String[]{"c1"}
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
                new String[]{"d1", "d2"},
                Streamables.toArray(
                        Streamables.flattenArrays(
                                Streamables.fromArray(
                                        new String[]{
                                                "d1",
                                                "d2"
                                        }, StreamablesTests.EMPTY
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
                new String[]{"e1", "e2"},
                Streamables.toArray(
                        Streamables.flattenArrays(
                                Streamables.fromArray(
                                        StreamablesTests.EMPTY,
                                        new String[]{"e1", "e2"}
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
                StreamablesTests.EMPTY,
                Streamables.toArray(
                        Streamables.flattenArrays(
                                Streamables.fromArray(
                                        StreamablesTests.EMPTY,
                                        StreamablesTests.EMPTY
                                )
                        )
                )
        );

        try
        {
            Streamables.flattenArrays(null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        final Streamable<String> f = Streamables.flattenArrays(Streamables.<String[]>fromArray(null, null));
        try
        {
            Streamables.toArray(f);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    /**
     * Tests {@link Streamables#flattenIterables(Streamable)}.
     *
     * @throws StreamException
     *         If a {@code Stream} fails. Should not occur.
     */
    @Test
    public void testFlattenIterables() throws StreamException
    {
        Assert.assertArrayEquals(
                new String[]{"a1", "a2", "b1", "b2", "b3", "c1"},
                Streamables.toArray(
                        Streamables.flattenIterables(
                                Streamables.fromArray(
                                        Arrays.asList(
                                                "a1",
                                                "a2"
                                        ), Arrays.asList("b1", "b2", "b3"), Collections.singletonList("c1")
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
                new String[]{"d1", "d2"},
                Streamables.toArray(
                        Streamables.flattenIterables(
                                Streamables.fromArray(
                                        Arrays.asList(
                                                "d1",
                                                "d2"
                                        ), Collections.emptyList()
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
                new String[]{"e1", "e2"},
                Streamables.toArray(
                        Streamables.flattenIterables(
                                Streamables.fromArray(
                                        Collections.emptyList(),
                                        Arrays.asList("e1", "e2")
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
                StreamablesTests.EMPTY,
                Streamables.toArray(
                        Streamables.flattenIterables(
                                Streamables.fromArray(
                                        Collections.emptyList(),
                                        Collections.emptyList()
                                )
                        )
                )
        );

        try
        {
            Streamables.flattenIterables(null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        final Streamable<String> f = Streamables.flattenIterables(Streamables.<Iterable<String>>fromArray(null, null));
        try
        {
            Streamables.toArray(f);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    /**
     * Tests {@link Streamables#flattenStreamables(Streamable)}.
     *
     * @throws StreamException
     *         If a {@link Stream} fails. Should not occur.
     */
    @Test
    public void testFlattenStreamables() throws StreamException
    {
        Assert.assertArrayEquals(
                new String[]{"a1", "a2", "b1", "b2", "b3", "c1"},
                Streamables.toArray(
                        Streamables.flattenStreamables(
                                Streamables.fromArray(
                                        Streamables.fromArray(
                                                "a1",
                                                "a2"
                                        ),
                                        Streamables.fromArray("b1", "b2", "b3"),
                                        Streamables.fromArray("c1")
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
                new String[]{"d1", "d2"},
                Streamables.toArray(
                        Streamables.flattenStreamables(
                                Streamables.fromArray(
                                        Streamables.fromArray(
                                                "d1",
                                                "d2"
                                        ), Streamables.<String>fromArray()
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
                new String[]{"e1", "e2"},
                Streamables.toArray(
                        Streamables.flattenStreamables(
                                Streamables.fromArray(
                                        Streamables.<String>fromArray(),
                                        Streamables.fromArray("e1", "e2")
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
                StreamablesTests.EMPTY,
                Streamables.toArray(
                        Streamables.flattenStreamables(
                                Streamables.fromArray(
                                        Streamables.fromArray(),
                                        Streamables.fromArray()
                                )
                        )
                )
        );

        try
        {
            Streamables.flattenStreamables(null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        final Streamable<String> f = Streamables.flattenStreamables(
                Streamables.<Streamable<String>>fromArray(
                        null,
                        null
                )
        );
        try
        {
            Streamables.toArray(f);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    /**
     * Tests {@link Streamables#fromIterable(Iterable)}.
     *
     * @throws StreamException
     *         If a {@link Stream} fails. Should not occur.
     */
    @Test
    public void testIterableStreamable() throws StreamException
    {
        final List<String> values = Arrays.asList("a", "b", "c");
        try (final Stream<String> stream = Streamables.fromIterable(values).stream())
        {
            Assert.assertTrue(stream.hasNext());
            Assert.assertEquals("a", stream.next());
            Assert.assertTrue(stream.hasNext());
            Assert.assertEquals("b", stream.next());
            Assert.assertTrue(stream.hasNext());
            Assert.assertEquals("c", stream.next());
            Assert.assertFalse(stream.hasNext());
            try
            {
                stream.next();
                Assert.fail();
            }
            catch (final NoSuchElementException ex)
            {
                // Expected
            }
        }
    }

    /**
     * Tests {@link Streamables#singleton(Object)}.
     *
     * @throws StreamException
     *         If a {@link Stream} fails. Should not occur.
     */
    @Test
    public void testSingletonStreamable() throws StreamException
    {
        final Streamable<Object> a = Streamables.<Object>singleton("a");
        try (final Stream<Object> stream = a.stream())
        {
            Assert.assertTrue(stream.hasNext());
            Assert.assertEquals("a", stream.next());
            Assert.assertFalse(stream.hasNext());
            try
            {
                stream.next();
                Assert.fail();
            }
            catch (final NoSuchElementException ex)
            {
                // Expected
            }
        }
    }

    /**
     * Tests {@link Streamables#toArray(Streamable)}.
     *
     * @throws StreamException
     *         If a {@link Stream} fails. Should not occur.
     */
    @Test
    public void testToArray() throws StreamException
    {
        final Streamable<String> a = Streamables.fromArray();
        Assert.assertArrayEquals(StreamablesTests.EMPTY, Streamables.toArray(a));

        final Streamable<String> b = Streamables.fromArray("b1", "b2", "b3");
        Assert.assertArrayEquals(new String[]{"b1", "b2", "b3"}, Streamables.toArray(b));
    }

    /**
     * Tests {@link Streamables#toList(Streamable)}.
     *
     * @throws StreamException
     *         If a {@link Stream} fails. Should not occur.
     */
    @Test
    public void testToList() throws StreamException
    {
        final Streamable<String> a = Streamables.fromArray();
        Assert.assertEquals(new ArrayList<String>(0), Streamables.toList(a));

        final Streamable<String> b = Streamables.fromArray("b1", "b2", "b3");
        Assert.assertEquals(Arrays.asList("b1", "b2", "b3"), Streamables.toList(b));
    }

    /**
     * Tests {@link Streamables#toSet(Streamable)}.
     *
     * @throws StreamException
     *         If a {@link Stream} fails. Should not occur.
     */
    @Test
    public void testToSet() throws StreamException
    {
        final Streamable<String> a = Streamables.fromArray();
        Assert.assertEquals(new HashSet<String>(0), Streamables.toSet(a));

        final Streamable<String> b = Streamables.fromArray("b1", "b2", "b3");
        Assert.assertEquals(new HashSet<>(Arrays.asList("b1", "b2", "b3")), Streamables.toSet(b));
    }

}
