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

import java.util.*;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link IOStreamables}.
 */
@SuppressWarnings({"ProhibitedExceptionCaught", "StandardVariableNames"})
public class IOStreamablesTests
{
    private static final String[] EMPTY = {};

    /**
     * Tests {@link IOStreamables#concat(IOStreamable[])}.
     *
     * @throws IOStreamException
     *         If any {@link IOStream} fails. Should not occur.
     */
    @Test
    public void testConcatArrayOfStreamables() throws IOStreamException
    {
        final IOStreamable<String> a = IOStreamables.fromArray("a1");
        final IOStreamable<String> b = IOStreamables.fromArray("b1");

        Assert.assertArrayEquals(
            new String[]{"a1", "b1"},
            IOStreamables.toArray(IOStreamables.concat(a, b))
        );
        Assert.assertArrayEquals(
            new String[]{"a1", "b1"},
            IOStreamables.toArray(IOStreamables.concat(a, b))
        );

        final IOStreamable<String> c = IOStreamables.fromArray("c1", "c2", "c3");
        final IOStreamable<String> d = IOStreamables.fromArray("d1", "d2");
        final IOStreamable<String> e = IOStreamables.fromArray("e1", "e2", "e3", "e4");

        Assert.assertArrayEquals(
            new String[]{"c1", "c2", "c3", "d1", "d2", "e1", "e2", "e3", "e4"},
            IOStreamables.toArray(IOStreamables.concat(c, d, e))
        );

        try
        {
            IOStreamables.concat((IOStreamable<String>[]) null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        final IOStreamable<String> g = IOStreamables.concat(null, null);
        try (final IOStream<String> stream = g.stream())
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
     * Tests {@link IOStreamables#concat(IOStreamable)}.
     *
     * @throws IOStreamException
     *         If any {@link IOStream} fails. Should not occur.
     */
    @Test
    public void testConcatStreamableOfStreamables() throws IOStreamException
    {
        final IOStreamable<String> a = IOStreamables.fromArray("a1");
        final IOStreamable<String> b = IOStreamables.fromArray("b1");

        Assert.assertArrayEquals(
            new String[]{"a1", "b1"},
            IOStreamables.toArray(IOStreamables.concat(IOStreamables.fromArray(a, b)))
        );
        Assert.assertArrayEquals(
            new String[]{"a1", "b1"},
            IOStreamables.toArray(IOStreamables.concat(IOStreamables.fromArray(a, b)))
        );

        final IOStreamable<String> c = IOStreamables.fromArray("c1", "c2", "c3");
        final IOStreamable<String> d = IOStreamables.fromArray("d1", "d2");
        final IOStreamable<String> e = IOStreamables.fromArray("e1", "e2", "e3", "e4");

        Assert.assertArrayEquals(
            new String[]{"c1", "c2", "c3", "d1", "d2", "e1", "e2", "e3", "e4"},
            IOStreamables.toArray(IOStreamables.concat(IOStreamables.fromArray(c, d, e)))
        );

        try
        {
            IOStreamables.concat((IOStreamable<IOStreamable<String>>) null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        final IOStreamable<String> g = IOStreamables.concat(IOStreamables.<IOStreamable<String>>fromArray(null, null));
        try (final IOStream<String> stream = g.stream())
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
     * Tests {@link IOStreamables#empty()}.
     *
     * @throws IOStreamException
     *         If a {@link IOStream} fails. Should not occur.
     */
    @Test
    public void testEmptyStreamable() throws IOStreamException
    {
        final IOStreamable<String> a = IOStreamables.empty();
        try (final IOStream<String> stream = a.stream())
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
     * Tests {@link IOStreamables#filter(IOStreamable, IOStreamFilter)}.
     *
     * @throws IOStreamException
     *         If a {@code IOStream} fails. Should not occur.
     */
    @Test
    public void testFilter() throws IOStreamException
    {
        Assert.assertArrayEquals(
            new String[]{"a1", "a2", "a3"},
            IOStreamables.toArray(
                IOStreamables.filter(
                    IOStreamables.fromArray("a1", "a2", "a3"),
                    IOStreamFilters.<String>all()
                        )
                )
        );
        Assert.assertArrayEquals(
            IOStreamablesTests.EMPTY,
            IOStreamables.toArray(
                IOStreamables.filter(
                    IOStreamables.fromArray("b1", "b2", "b3"),
                    IOStreamFilters.<String>none()
                        )
                )
        );
        Assert.assertArrayEquals(
            new String[]{"c2", "c3"},
            IOStreamables.toArray(
                IOStreamables.filter(
                    IOStreamables.fromArray("c1", "c2", "c3", "c4"),
                    IOStreamFilters.whitelist("c2", "c3")
                        )
                )
        );

        Assert.assertArrayEquals(
            IOStreamablesTests.EMPTY,
            IOStreamables.toArray(
                IOStreamables.filter(
                    IOStreamables.<String>empty(),
                    IOStreamFilters.whitelist("c2", "c3")
                        )
                )
        );

        try
        {
            IOStreamables.filter(null, IOStreamFilters.all());
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
        try
        {
            IOStreamables.filter(IOStreamables.empty(), null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
        try
        {
            IOStreamables.filter(null, null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    /**
     * Tests {@link IOStreamables#flattenArrays(IOStreamable)}.
     *
     * @throws IOStreamException
     *         If any {@code IOStream} fails. Should not occur.
     */
    @Test
    public void testFlattenArrays() throws IOStreamException
    {
        Assert.assertArrayEquals(
            new String[]{"a1", "a2", "b1", "b2", "b3", "c1"},
            IOStreamables.toArray(
                IOStreamables.flattenArrays(
                    IOStreamables.fromArray(
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
            IOStreamables.toArray(
                IOStreamables.flattenArrays(
                    IOStreamables.fromArray(
                        new String[]{
                                                "d1",
                                                "d2"
                                        }, IOStreamablesTests.EMPTY
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            new String[]{"e1", "e2"},
            IOStreamables.toArray(
                IOStreamables.flattenArrays(
                    IOStreamables.fromArray(
                        IOStreamablesTests.EMPTY,
                        new String[]{"e1", "e2"}
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            IOStreamablesTests.EMPTY,
            IOStreamables.toArray(
                IOStreamables.flattenArrays(
                    IOStreamables.fromArray(
                        IOStreamablesTests.EMPTY,
                        IOStreamablesTests.EMPTY
                                )
                        )
                )
        );

        try
        {
            IOStreamables.flattenArrays(null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        final IOStreamable<String> f = IOStreamables.flattenArrays(IOStreamables.<String[]>fromArray(null, null));
        try
        {
            IOStreamables.toArray(f);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    /**
     * Tests {@link IOStreamables#flattenIterables(IOStreamable)}.
     *
     * @throws IOStreamException
     *         If a {@code IOStream} fails. Should not occur.
     */
    @Test
    public void testFlattenIterables() throws IOStreamException
    {
        Assert.assertArrayEquals(
            new String[]{"a1", "a2", "b1", "b2", "b3", "c1"},
            IOStreamables.toArray(
                IOStreamables.flattenIterables(
                    IOStreamables.fromArray(
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
            IOStreamables.toArray(
                IOStreamables.flattenIterables(
                    IOStreamables.fromArray(
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
            IOStreamables.toArray(
                IOStreamables.flattenIterables(
                    IOStreamables.fromArray(
                                        Collections.emptyList(),
                                        Arrays.asList("e1", "e2")
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            IOStreamablesTests.EMPTY,
            IOStreamables.toArray(
                IOStreamables.flattenIterables(
                    IOStreamables.fromArray(
                                        Collections.emptyList(),
                                        Collections.emptyList()
                                )
                        )
                )
        );

        try
        {
            IOStreamables.flattenIterables(null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        final IOStreamable<String> f = IOStreamables
            .flattenIterables(IOStreamables.<Iterable<String>>fromArray(null, null));
        try
        {
            IOStreamables.toArray(f);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    /**
     * Tests {@link IOStreamables#flattenStreamables(IOStreamable)}.
     *
     * @throws IOStreamException
     *         If a {@link IOStream} fails. Should not occur.
     */
    @Test
    public void testFlattenStreamables() throws IOStreamException
    {
        Assert.assertArrayEquals(
            new String[]{"a1", "a2", "b1", "b2", "b3", "c1"},
            IOStreamables.toArray(
                IOStreamables.flattenStreamables(
                    IOStreamables.fromArray(
                        IOStreamables.fromArray(
                                                "a1",
                                                "a2"
                                        ),
                        IOStreamables.fromArray("b1", "b2", "b3"),
                        IOStreamables.fromArray("c1")
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            new String[]{"d1", "d2"},
            IOStreamables.toArray(
                IOStreamables.flattenStreamables(
                    IOStreamables.fromArray(
                        IOStreamables.fromArray(
                                                "d1",
                                                "d2"
                                        ), IOStreamables.<String>fromArray()
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            new String[]{"e1", "e2"},
            IOStreamables.toArray(
                IOStreamables.flattenStreamables(
                    IOStreamables.fromArray(
                        IOStreamables.<String>fromArray(),
                        IOStreamables.fromArray("e1", "e2")
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            IOStreamablesTests.EMPTY,
            IOStreamables.toArray(
                IOStreamables.flattenStreamables(
                    IOStreamables.fromArray(
                        IOStreamables.fromArray(),
                        IOStreamables.fromArray()
                                )
                        )
                )
        );

        try
        {
            IOStreamables.flattenStreamables(null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        final IOStreamable<String> f = IOStreamables.flattenStreamables(
            IOStreamables.<IOStreamable<String>>fromArray(
                        null,
                        null
                )
        );
        try
        {
            IOStreamables.toArray(f);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    /**
     * Tests {@link IOStreamables#fromIterable(Iterable)}.
     *
     * @throws IOStreamException
     *         If a {@link IOStream} fails. Should not occur.
     */
    @Test
    public void testIterableStreamable() throws IOStreamException
    {
        final List<String> values = Arrays.asList("a", "b", "c");
        try (final IOStream<String> stream = IOStreamables.fromIterable(values).stream())
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
     * Tests {@link IOStreamables#singleton(Object)}.
     *
     * @throws IOStreamException
     *         If a {@link IOStream} fails. Should not occur.
     */
    @Test
    public void testSingletonStreamable() throws IOStreamException
    {
        final IOStreamable<Object> a = IOStreamables.<Object>singleton("a");
        try (final IOStream<Object> stream = a.stream())
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
     * Tests {@link IOStreamables#toArray(IOStreamable)}.
     *
     * @throws IOStreamException
     *         If a {@link IOStream} fails. Should not occur.
     */
    @Test
    public void testToArray() throws IOStreamException
    {
        final IOStreamable<String> a = IOStreamables.fromArray();
        Assert.assertArrayEquals(IOStreamablesTests.EMPTY, IOStreamables.toArray(a));

        final IOStreamable<String> b = IOStreamables.fromArray("b1", "b2", "b3");
        Assert.assertArrayEquals(new String[]{"b1", "b2", "b3"}, IOStreamables.toArray(b));
    }

    /**
     * Tests {@link IOStreamables#toList(IOStreamable)}.
     *
     * @throws IOStreamException
     *         If a {@link IOStream} fails. Should not occur.
     */
    @Test
    public void testToList() throws IOStreamException
    {
        final IOStreamable<String> a = IOStreamables.fromArray();
        Assert.assertEquals(new ArrayList<String>(0), IOStreamables.toList(a));

        final IOStreamable<String> b = IOStreamables.fromArray("b1", "b2", "b3");
        Assert.assertEquals(Arrays.asList("b1", "b2", "b3"), IOStreamables.toList(b));
    }

    /**
     * Tests {@link IOStreamables#toSet(IOStreamable)}.
     *
     * @throws IOStreamException
     *         If a {@link IOStream} fails. Should not occur.
     */
    @Test
    public void testToSet() throws IOStreamException
    {
        final IOStreamable<String> a = IOStreamables.fromArray();
        Assert.assertEquals(new HashSet<String>(0), IOStreamables.toSet(a));

        final IOStreamable<String> b = IOStreamables.fromArray("b1", "b2", "b3");
        Assert.assertEquals(new HashSet<>(Arrays.asList("b1", "b2", "b3")), IOStreamables.toSet(b));
    }

}
