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
 * Tests {@link IOStreams}.
 */
@SuppressWarnings({"ProhibitedExceptionCaught", "StandardVariableNames"})
public class IOStreamsTests
{
    private static final String[] EMPTY = {};

    @Test
    public void testPartition() throws IOStreamException{
        final IOStream<String> a = IOStreams.fromArray("a-a1", "a-a2", "a-a3", "a-b1", "a-b2", "a-a4", "a-c1", "a-d1", "a-d2");
        final IOStream<IOStream<String>> aPartitions = a.partition((l,r)->l.charAt(2)==r.charAt(2));

        Assert.assertArrayEquals(new String [] { "a-a1", "a-a2", "a-a3" }, IOStreams.toArray(aPartitions.next()));
        Assert.assertArrayEquals(new String [] { "a-b1", "a-b2" }, IOStreams.toArray(aPartitions.next()));
        Assert.assertArrayEquals(new String [] { "a-a4" }, IOStreams.toArray(aPartitions.next()));
        Assert.assertArrayEquals(new String [] { "a-c1" }, IOStreams.toArray(aPartitions.next()));
        Assert.assertArrayEquals(new String[] { "a-d1", "a-d2" }, IOStreams.toArray(aPartitions.next()));
        Assert.assertFalse(aPartitions.hasNext());

        final IOStream<String> b = IOStreams.fromArray("b-a1", "b-a2", "b-a3", "b-b1", "b-b2");
        final IOStream<IOStream<String>> bPartitions = b.partition((l,r)->l.charAt(2)==r.charAt(2));

        try(final IOStream<String> bOrphan = bPartitions.next()) {
            Assert.assertEquals("b-a1", bOrphan.next());
        }
        Assert.assertArrayEquals(new String [] { "b-a2", "b-a3" }, IOStreams.toArray(bPartitions.next()));
        Assert.assertArrayEquals(new String [] { "b-b1", "b-b2" }, IOStreams.toArray(bPartitions.next()));
        Assert.assertFalse(bPartitions.hasNext());

        try{
            IOStreams.partition(null, (l,r)->false);
            Assert.fail();
        } catch (NullPointerException ex){
            // Expected
        }

        try{
            IOStreams.partition(a, null);
            Assert.fail();
        } catch (NullPointerException ex){
            // Expected
        }
    }

    @Test
    public void testGroup() throws IOStreamException{
        final IOStream<String> a = IOStreams.fromArray("a1", "a2", "a3", "a4", "a5");
        final IOStream<? extends IOStream<String>> aGroups = IOStreams.group(a, 2);
        Assert.assertArrayEquals(new String[]{ "a1", "a2" }, IOStreams.toArray(aGroups.next()));
        Assert.assertArrayEquals(new String[]{ "a3", "a4" }, IOStreams.toArray(aGroups.next()));
        Assert.assertArrayEquals(new String[]{ "a5" }, IOStreams.toArray(aGroups.next()));
        Assert.assertFalse(aGroups.hasNext());

        final IOStream<String> b = IOStreams.fromArray("b1", "b2", "b3");
        final IOStream<? extends IOStream<String>> bGroups = IOStreams.group(b, 5);
        Assert.assertArrayEquals(new String[]{"b1", "b2", "b3"}, IOStreams.toArray(bGroups.next()));
        Assert.assertFalse(bGroups.hasNext());

        try{
            IOStreams.group(null, 1);
            Assert.fail();
        } catch (NullPointerException ex){
            // Expected
        }

        try {
            IOStreams.group(b, 0);
            Assert.fail();
        } catch (IllegalArgumentException ex){
            // Expected
        }

        try {
            IOStreams.group(b, -10);
            Assert.fail();
        } catch (IllegalArgumentException ex){
            // Expected
        }
    }

    @Test
    public void testConcatArrayOfStreams() throws IOStreamException
    {
        final IOStream<String> a = IOStreams.fromArray("a1");
        final IOStream<String> b = IOStreams.fromArray("b1");

        Assert.assertArrayEquals(new String[]{"a1", "b1"}, IOStreams.toArray(IOStreams.concat(a, b)));
        Assert.assertArrayEquals(IOStreamsTests.EMPTY, IOStreams.toArray(IOStreams.concat(a, b)));

        final IOStream<String> c = IOStreams.fromArray("c1", "c2", "c3");
        final IOStream<String> d = IOStreams.fromArray("d1", "d2");
        final IOStream<String> e = IOStreams.fromArray("e1", "e2", "e3", "e4");

        Assert.assertArrayEquals(
            new String[]{"c1", "c2", "c3", "d1", "d2", "e1", "e2", "e3", "e4"},
            IOStreams.toArray(IOStreams.concat(c, d, e))
        );

        try
        {
            IOStreams.concat((IOStream<String>[]) null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
        try (final IOStream<String> g = IOStreams.concat(null, null))
        {
            g.hasNext();
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    @Test
    public void testConcatStreamOfStreams() throws IOStreamException
    {
        final IOStream<String> a = IOStreams.fromArray("a1");
        final IOStream<String> b = IOStreams.fromArray("b1");

        Assert.assertArrayEquals(new String[]{"a1", "b1"}, IOStreams.toArray(IOStreams.concat(IOStreams.fromArray(a, b))));
        Assert.assertArrayEquals(IOStreamsTests.EMPTY, IOStreams.toArray(IOStreams.concat(IOStreams.fromArray(a, b))));

        final IOStream<String> c = IOStreams.fromArray("c1", "c2", "c3");
        final IOStream<String> d = IOStreams.fromArray("d1", "d2");
        final IOStream<String> e = IOStreams.fromArray("e1", "e2", "e3", "e4");

        Assert.assertArrayEquals(
            new String[]{"c1", "c2", "c3", "d1", "d2", "e1", "e2", "e3", "e4"},
            IOStreams.toArray(IOStreams.concat(IOStreams.fromArray(c, d, e)))
        );

        try
        {
            IOStreams.concat((IOStream<IOStream<String>>) null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        try (final IOStream<String> g = IOStreams.concat(IOStreams.<IOStream<String>>fromArray(null, null)))
        {
            g.hasNext();
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    @Test
    public void testEmptyStream() throws IOStreamException
    {
        try (final IOStream<String> stream = IOStreams.empty())
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

    @Test
    public void testFilter() throws IOStreamException
    {
        Assert.assertArrayEquals(
            new String[]{"a1", "a2", "a3"},
            IOStreams.toArray(
                IOStreams.filter(
                    IOStreams.fromArray("a1", "a2", "a3"),
                    IOStreamFilters.<String>all()
                        )
                )
        );
        Assert.assertArrayEquals(
            IOStreamsTests.EMPTY,
            IOStreams.toArray(
                IOStreams.filter(
                    IOStreams.fromArray("b1", "b2", "b3"),
                    IOStreamFilters.<String>none()
                        )
                )
        );
        Assert.assertArrayEquals(
            new String[]{"c2", "c3"},
            IOStreams.toArray(
                IOStreams.filter(
                    IOStreams.fromArray("c1", "c2", "c3", "c4"),
                    IOStreamFilters.whitelist("c2", "c3")
                        )
                )
        );

        Assert.assertArrayEquals(
            IOStreamsTests.EMPTY,
            IOStreams.toArray(
                IOStreams.filter(
                    IOStreams.<String>empty(),
                    IOStreamFilters.whitelist("c2", "c3")
                        )
                )
        );

        try
        {
            IOStreams.filter(null, IOStreamFilters.all());
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
        try
        {
            IOStreams.filter(IOStreams.empty(), null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
        try
        {
            IOStreams.filter(null, null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    @Test
    public void testFlattenArrays() throws IOStreamException
    {
        Assert.assertArrayEquals(
            new String[]{"a1", "a2", "b1", "b2", "b3", "c1"},
            IOStreams.toArray(
                IOStreams.flattenArrays(
                    IOStreams.fromArray(
                                        new String[]{"a1", "a2"},
                                        new String[]{"b1", "b2", "b3"},
                                        new String[]{"c1"}
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            new String[]{"d1", "d2"},
            IOStreams.toArray(
                IOStreams.flattenArrays(
                    IOStreams.fromArray(
                        new String[]{"d1", "d2"},
                        IOStreamsTests.EMPTY
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            new String[]{"e1", "e2"},
            IOStreams.toArray(
                IOStreams.flattenArrays(
                    IOStreams.fromArray(
                        IOStreamsTests.EMPTY,
                        new String[]{"e1", "e2"}
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            IOStreamsTests.EMPTY,
            IOStreams.toArray(
                IOStreams.flattenArrays(
                    IOStreams.fromArray(
                        IOStreamsTests.EMPTY,
                        IOStreamsTests.EMPTY
                                )
                        )
                )
        );

        try
        {
            IOStreams.flattenArrays(null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        try (final IOStream<String> f = IOStreams.flattenArrays(IOStreams.<String[]>fromArray(null, null)))
        {
            IOStreams.toArray(f);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    @Test
    public void testFlattenIterables() throws IOStreamException
    {
        Assert.assertArrayEquals(
            new String[]{"a1", "a2", "b1", "b2", "b3", "c1"},
            IOStreams.toArray(
                IOStreams.flattenIterables(
                    IOStreams.fromArray(
                                        Arrays.asList("a1", "a2"),
                                        Arrays.asList("b1", "b2", "b3"),
                                        Collections.singletonList("c1")
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            new String[]{"d1", "d2"},
            IOStreams.toArray(
                IOStreams.flattenIterables(
                    IOStreams.fromArray(
                                        Arrays.asList("d1", "d2"),
                                        Collections.emptyList()
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            new String[]{"e1", "e2"},
            IOStreams.toArray(
                IOStreams.flattenIterables(
                    IOStreams.fromArray(
                                        Collections.emptyList(),
                                        Arrays.asList("e1", "e2")
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            IOStreamsTests.EMPTY,
            IOStreams.toArray(
                IOStreams.flattenIterables(
                    IOStreams.fromArray(
                                        Collections.emptyList(),
                                        Collections.emptyList()
                                )
                        )
                )
        );

        try
        {
            IOStreams.flattenIterables(null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        try (final IOStream<String> f = IOStreams.flattenIterables(IOStreams.<Iterable<String>>fromArray(null, null)))
        {
            IOStreams.toArray(f);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    @Test
    public void testFlattenIterators() throws IOStreamException
    {
        Assert.assertArrayEquals(
            new String[]{"a1", "a2", "b1", "b2", "b3", "c1"},
            IOStreams.toArray(
                IOStreams.flattenIterators(
                    IOStreams.fromArray(
                                        Arrays.asList("a1", "a2")
                                              .iterator(),
                                        Arrays.asList("b1", "b2", "b3").iterator(),
                                        Collections.singletonList("c1").iterator()
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            new String[]{"d1", "d2"},
            IOStreams.toArray(
                IOStreams.flattenIterators(
                    IOStreams.fromArray(
                                        Arrays.asList("d1", "d2")
                                              .iterator(), Collections.emptyList().iterator()
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            new String[]{"e1", "e2"},
            IOStreams.toArray(
                IOStreams.flattenIterators(
                    IOStreams.fromArray(
                                        Collections.emptyList().iterator(),
                                        Arrays.asList("e1", "e2").iterator()
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            IOStreamsTests.EMPTY,
            IOStreams.toArray(
                IOStreams.flattenIterators(
                    IOStreams.fromArray(
                                        Collections.emptyList().iterator(),
                                        Collections.emptyList().iterator()
                                )
                        )
                )
        );

        try
        {
            IOStreams.flattenIterators(null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        try (final IOStream<String> f = IOStreams.flattenIterators(IOStreams.<Iterator<String>>fromArray(null, null)))
        {
            IOStreams.toArray(f);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    @Test
    public void testFlattenStreams() throws IOStreamException
    {
        Assert.assertArrayEquals(
            new String[]{"a1", "a2", "b1", "b2", "b3", "c1"},
            IOStreams.toArray(
                IOStreams.flattenIOStreams(
                    IOStreams.fromArray(
                        IOStreams.fromArray("a1", "a2"),
                        IOStreams.fromArray("b1", "b2", "b3"),
                        IOStreams.fromArray("c1")
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            new String[]{"d1", "d2"},
            IOStreams.toArray(
                IOStreams.flattenIOStreams(
                    IOStreams.fromArray(
                        IOStreams.fromArray("d1", "d2"),
                        IOStreams.fromArray()
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            new String[]{"e1", "e2"},
            IOStreams.toArray(
                IOStreams.flattenIOStreams(
                    IOStreams.fromArray(
                        IOStreams.fromArray(),
                        IOStreams.fromArray("e1", "e2")
                                )
                        )
                )
        );
        Assert.assertArrayEquals(
            IOStreamsTests.EMPTY,
            IOStreams.toArray(
                IOStreams.flattenIOStreams(
                    IOStreams.fromArray(
                        IOStreams.fromArray(),
                        IOStreams.fromArray()
                                )
                        )
                )
        );

        try
        {
            IOStreams.flattenIOStreams(null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        try (final IOStream<String> f = IOStreams.flattenIOStreams(IOStreams.<IOStream<String>>fromArray(null, null)))
        {
            IOStreams.toArray(f);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    @Test
    public void testIterableStream() throws IOStreamException
    {
        final List<String> values = Arrays.asList("a", "b", "c");
        try (final IOStream<String> stream = IOStreams.fromIterable(values))
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

    @Test
    public void testIteratorStream() throws IOStreamException
    {
        final List<String> values = Arrays.asList("a", "b", "c");
        try (final IOStream<String> stream = IOStreams.fromIterator(values.iterator()))
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

    @Test
    public void testSingletonStream() throws IOStreamException
    {
        final String singleton = "a";
        try (final IOStream<Object> stream = IOStreams.<Object>singleton(singleton))
        {
            Assert.assertTrue(stream.hasNext());
            Assert.assertEquals(singleton, stream.next());
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

    @Test
    public void testToArray() throws IOStreamException
    {
        final IOStream<String> a = IOStreams.fromArray();
        Assert.assertArrayEquals(IOStreamsTests.EMPTY, IOStreams.toArray(a));

        final IOStream<String> b = IOStreams.fromArray("b1", "b2", "b3");
        Assert.assertArrayEquals(new String[]{"b1", "b2", "b3"}, IOStreams.toArray(b));
    }

    @Test
    public void testToList() throws IOStreamException
    {
        final IOStream<String> a = IOStreams.fromArray();
        Assert.assertEquals(new ArrayList<String>(0), IOStreams.toList(a));

        final IOStream<String> b = IOStreams.fromArray("b1", "b2", "b3");
        Assert.assertEquals(Arrays.asList("b1", "b2", "b3"), IOStreams.toList(b));
    }

    @Test
    public void testToSet() throws IOStreamException
    {
        final IOStream<String> a = IOStreams.fromArray();
        Assert.assertEquals(new HashSet<String>(0), IOStreams.toSet(a));

        final IOStream<String> b = IOStreams.fromArray("b1", "b2", "b3");
        Assert.assertEquals(new HashSet<>(Arrays.asList("b1", "b2", "b3")), IOStreams.toSet(b));
    }

}
