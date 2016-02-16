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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;

/**
 * Tests {@link IOStreams}.
 */
@SuppressWarnings({"ProhibitedExceptionCaught", "StandardVariableNames"})
public class IOStreamsTests
{
    private static final String[] EMPTY = {};

    @Test
    public void testConcatArrayOfStreams() throws IOStreamException
    {
        final IOStream<String> a = IOStreams.fromArray("a1");
        final IOStream<String> b = IOStreams.fromArray("b1");

        Assert.assertArrayEquals(new String[]{"a1", "b1"}, IOStreams.concat(a, b).toArray(String[]::new));
        Assert.assertArrayEquals(IOStreamsTests.EMPTY, IOStreams.concat(a, b).toArray(String[]::new));

        final IOStream<String> c = IOStreams.fromArray("c1", "c2", "c3");
        final IOStream<String> d = IOStreams.fromArray("d1", "d2");
        final IOStream<String> e = IOStreams.fromArray("e1", "e2", "e3", "e4");

        Assert.assertArrayEquals(
            new String[]{"c1", "c2", "c3", "d1", "d2", "e1", "e2", "e3", "e4"},
            IOStreams.concat(c, d, e).toArray(String[]::new)
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

        Assert.assertArrayEquals(new String[]{"a1", "b1"},
                                 IOStreams.concat(IOStreams.fromArray(a, b)).toArray(String[]::new));
        Assert.assertArrayEquals(IOStreamsTests.EMPTY,
                                 IOStreams.concat(IOStreams.fromArray(a, b)).toArray(String[]::new));

        final IOStream<String> c = IOStreams.fromArray("c1", "c2", "c3");
        final IOStream<String> d = IOStreams.fromArray("d1", "d2");
        final IOStream<String> e = IOStreams.fromArray("e1", "e2", "e3", "e4");

        Assert.assertArrayEquals(
            new String[]{"c1", "c2", "c3", "d1", "d2", "e1", "e2", "e3", "e4"},
            IOStreams.concat(IOStreams.fromArray(c, d, e)).toArray(String[]::new)
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
            IOStreams.filter(IOStreams.fromArray("a1", "a2", "a3"), IOStreamFilters.<String>all())
                .toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            IOStreamsTests.EMPTY,
            IOStreams.filter(IOStreams.fromArray("b1", "b2", "b3"), IOStreamFilters.<String>none())
                .toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[]{"c2", "c3"},
            IOStreams.filter(IOStreams.fromArray("c1", "c2", "c3", "c4"), IOStreamFilters.whitelist("c2", "c3"))
                .toArray(String[]::new)
        );

        Assert.assertArrayEquals(
            IOStreamsTests.EMPTY,
            IOStreams.filter(IOStreams.<String>empty(), IOStreamFilters.whitelist("c2", "c3")).toArray(String[]::new)
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
                IOStreams.flattenArrays(
                    IOStreams.fromArray(
                        new String[]{"a1", "a2"},
                        new String[]{"b1", "b2", "b3"},
                        new String[]{"c1"}
                    )
                ).toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[]{"d1", "d2"},
                IOStreams.flattenArrays(
                    IOStreams.fromArray(
                        new String[]{"d1", "d2"},
                        IOStreamsTests.EMPTY
                    )
                ).toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[]{"e1", "e2"},
                IOStreams.flattenArrays(
                    IOStreams.fromArray(
                        IOStreamsTests.EMPTY,
                        new String[]{"e1", "e2"}
                                )
                ).toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            IOStreamsTests.EMPTY,
                IOStreams.flattenArrays(
                    IOStreams.fromArray(
                        IOStreamsTests.EMPTY,
                        IOStreamsTests.EMPTY
                                )
                ).toArray(String[]::new)
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
            f.toArray(String[]::new);
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
                IOStreams.flattenIterables(
                    IOStreams.fromArray(
                                        Arrays.asList("a1", "a2"),
                                        Arrays.asList("b1", "b2", "b3"),
                                        Collections.singletonList("c1")
                                )
                ).toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[]{"d1", "d2"},
                IOStreams.flattenIterables(
                    IOStreams.fromArray(
                                        Arrays.asList("d1", "d2"),
                                        Collections.emptyList()
                                )
                ).toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[]{"e1", "e2"},
                IOStreams.flattenIterables(
                    IOStreams.fromArray(
                                        Collections.emptyList(),
                                        Arrays.asList("e1", "e2")
                                )
                ).toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            IOStreamsTests.EMPTY,
                IOStreams.flattenIterables(
                    IOStreams.fromArray(
                                        Collections.emptyList(),
                                        Collections.emptyList()
                                )
                ).toArray(String[]::new)
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
            f.toArray(String[]::new);
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
                IOStreams.flattenIterators(
                    IOStreams.fromArray(
                                        Arrays.asList("a1", "a2")
                                              .iterator(),
                                        Arrays.asList("b1", "b2", "b3").iterator(),
                                        Collections.singletonList("c1").iterator()
                                )
                ).toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[]{"d1", "d2"},
                IOStreams.flattenIterators(
                    IOStreams.fromArray(
                                        Arrays.asList("d1", "d2")
                                              .iterator(), Collections.emptyList().iterator()
                                )
                ).toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[]{"e1", "e2"},
                IOStreams.flattenIterators(
                    IOStreams.fromArray(
                                        Collections.emptyList().iterator(),
                                        Arrays.asList("e1", "e2").iterator()
                                )
                ).toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            IOStreamsTests.EMPTY,
                IOStreams.flattenIterators(
                    IOStreams.fromArray(
                                        Collections.emptyList().iterator(),
                                        Collections.emptyList().iterator()
                                )
                ).toArray(String[]::new)
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
            f.toArray(String[]::new);
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
                IOStreams.flattenIOStreams(
                    IOStreams.fromArray(
                        IOStreams.fromArray("a1", "a2"),
                        IOStreams.fromArray("b1", "b2", "b3"),
                        IOStreams.fromArray("c1")
                                )
                ).toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[]{"d1", "d2"},
                IOStreams.flattenIOStreams(
                    IOStreams.fromArray(
                        IOStreams.fromArray("d1", "d2"),
                        IOStreams.fromArray()
                                )
                ).toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[]{"e1", "e2"},
                IOStreams.flattenIOStreams(
                    IOStreams.fromArray(
                        IOStreams.fromArray(),
                        IOStreams.fromArray("e1", "e2")
                                )
                ).toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            IOStreamsTests.EMPTY,
                IOStreams.flattenIOStreams(
                    IOStreams.fromArray(
                        IOStreams.fromArray(),
                        IOStreams.fromArray()
                                )
                ).toArray(String[]::new)
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
            f.toArray(String[]::new);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    @Test
    public void testFold() throws IOStreamException{
        final IOStream<Integer> a = IOStreams.fromArray(1,2,3,4,5,6,7,8,9,10);
        final Long aFolded = a.fold(0L, (sum, i) -> sum + i);
        Assert.assertEquals(55L, aFolded.longValue());
    }

    @Test
    public void testGroup() throws IOStreamException{
        final IOStream<String> a = IOStreams.fromArray("a1", "a2", "a3", "a4", "a5");
        final IOStream<? extends IOStream<String>> aGroups = IOStreams.group(a, 2);
        Assert.assertArrayEquals(new String[]{"a1", "a2"}, aGroups.next().toArray(String[]::new));
        Assert.assertArrayEquals(new String[]{"a3", "a4"}, aGroups.next().toArray(String[]::new));
        Assert.assertArrayEquals(new String[]{"a5"}, aGroups.next().toArray(String[]::new));
        Assert.assertFalse(aGroups.hasNext());

        final IOStream<String> b = IOStreams.fromArray("b1", "b2", "b3");
        final IOStream<? extends IOStream<String>> bGroups = IOStreams.group(b, 5);
        Assert.assertArrayEquals(new String[]{"b1", "b2", "b3"}, bGroups.next().toArray(String[]::new));
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
    public void testGroupAdjacent() throws IOStreamException{
        final IOStream<String> a = IOStreams.fromArray("a-a1", "a-a2", "a-a3", "a-b1", "a-b2", "a-a4", "a-c1", "a-d1", "a-d2");
        final IOStream<IOStream<String>> aGroups = a.group((l, r)-> l.charAt(2) == r.charAt(2));

        Assert.assertArrayEquals(new String[]{"a-a1", "a-a2", "a-a3"}, aGroups.next().toArray(String[]::new));
        Assert.assertArrayEquals(new String[]{"a-b1", "a-b2"}, aGroups.next().toArray(String[]::new));
        Assert.assertArrayEquals(new String[]{"a-a4"}, aGroups.next().toArray(String[]::new));
        Assert.assertArrayEquals(new String[]{"a-c1"}, aGroups.next().toArray(String[]::new));
        Assert.assertArrayEquals(new String[]{"a-d1", "a-d2"}, aGroups.next().toArray(String[]::new));
        Assert.assertFalse(aGroups.hasNext());

        final IOStream<String> b = IOStreams.fromArray("b-a1", "b-a2", "b-a3", "b-b1", "b-b2");
        final IOStream<IOStream<String>> bGroups = b.group((l, r)-> l.charAt(2) == r.charAt(2));

        try(final IOStream<String> bOrphan = bGroups.next()) {
            Assert.assertEquals("b-a1", bOrphan.next());
        }
        Assert.assertArrayEquals(new String[]{"b-a2", "b-a3"}, bGroups.next().toArray(String[]::new));
        Assert.assertArrayEquals(new String[]{"b-b1", "b-b2"}, bGroups.next().toArray(String[]::new));
        Assert.assertFalse(bGroups.hasNext());

        final IOStream<String> c = IOStreams.fromArray("c-a1");
        final IOStream<IOStream<String>> cGroups = c.group((l, r)->false);
        Assert.assertArrayEquals(new String[]{"c-a1"}, cGroups.next().toArray(String[]::new));
        Assert.assertFalse(cGroups.hasNext());

        final IOStream<String> d = IOStreams.empty();
        try(final IOStream<IOStream<String>> dGroups = d.split((l, r)->false)){
            Assert.assertFalse(dGroups.hasNext());
        }

        try{
            IOStreams.group(null, (l, r)->false);
            Assert.fail();
        } catch (NullPointerException ex){
            // Expected
        }

        try{
            IOStreams.group(a, null);
            Assert.fail();
        } catch (NullPointerException ex){
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
    public void testObserve() throws IOStreamException {
        final IOStream<String> a = IOStreams.fromArray("a1", "a2", "a3", "a4", "a5");
        final ArrayList<String> aObservedValues = new ArrayList<>(5);
        final String[] aValues = a.observe(aObservedValues::add).toArray(String[]::new);

        Assert.assertArrayEquals(new String[]{"a1", "a2", "a3", "a4", "a5"},
                                 aObservedValues.toArray(new String[aObservedValues.size()]));
        Assert.assertArrayEquals(new String[]{"a1", "a2", "a3", "a4", "a5"}, aValues);
        Assert.assertFalse(a.hasNext());

        try {
            IOStreams.observe(null, value -> {
            });
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }

        try {
            IOStreams.observe(a, null);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }
    }

    @Test
    public void testPeekable() throws IOStreamException{
        final IOStream<String> a = IOStreams.fromArray("a1", "a2", "a3", "a4", "a5");
        final PeekableIOStream<String> aPeekable = IOStreams.peekable(a);
        final String[] aPeeked = StreamSupport.stream(aPeekable.peek(3).spliterator(), false).toArray(String[]::new);

        Assert.assertArrayEquals(new String[] { "a1", "a2", "a3" }, aPeeked);
        Assert.assertArrayEquals(new String[]{"a1", "a2", "a3", "a4", "a5"}, aPeekable.toArray(String[]::new));
        Assert.assertFalse(aPeekable.hasNext());
        Assert.assertFalse(aPeekable.peek(1).iterator().hasNext());

        try{
            aPeekable.peek();
            Assert.fail();
        } catch (NoSuchElementException ex){
            // Expected
        }

        try{
            IOStreams.peekable(null);
            Assert.fail();
        } catch (NullPointerException ex){
            // Expected
        }
    }

    @Test
    public void testReduce() throws IOStreamException{
        final IOStream<String> a = IOStreams.fromArray("a1", "a2", "a3", "a4", "a5");
        final String aReduced = a.reduce(values -> {
            final StringBuilder builder = new StringBuilder();
            values.consume(builder::append);
            return builder.toString();
        });
        Assert.assertEquals("a1a2a3a4a5", aReduced);
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
        Assert.assertArrayEquals(IOStreamsTests.EMPTY, IOStreams.toArray(a, String[]::new));

        final IOStream<String> b = IOStreams.fromArray("b1", "b2", "b3");
        Assert.assertArrayEquals(new String[]{"b1", "b2", "b3"}, IOStreams.toArray(b, String[]::new));
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
