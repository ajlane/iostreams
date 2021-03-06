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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

/**
 * Tests {@link IOStreams}.
 */
@SuppressWarnings({"ProhibitedExceptionCaught", "StandardVariableNames"})
public class IOStreamsTest
{
    @Test
    public void testCast() throws IOStreamException, InterruptedException
    {
        final IOStream<Object> a = TestStream.of("a1", "a2", "a3");
        try (final IOStream<String> aAsStrings = IOStreams.cast(a))
        {
            Assert.assertArrayEquals(new String[]{"a1", "a2", "a3"}, aAsStrings.toArray(String[]::new));
        }

        final IOStream<Object> b = TestStream.of("b1", 2);
        try (final IOStream<String> bAsStrings = IOStreams.cast(b))
        {
            Assert.assertTrue(bAsStrings.hasNext());
            Assert.assertEquals("b1", bAsStrings.next());
            Assert.assertTrue(bAsStrings.hasNext());
            try
            {
                // Interestingly, if we don't try to read/assign the value, it doesn't throw.
                final String b2 = bAsStrings.next();
                Assert.fail();
            }
            catch (final ClassCastException ex)
            {
                // Expected
            }
        }
    }

    @Test
    public void testConcatArrayOfStreams() throws IOStreamException, InterruptedException
    {
        final TestStream<String> a = TestStream.of("a1");
        final TestStream<String> b = TestStream.of("b1");
        Assert.assertArrayEquals(
            new String[]{"a1", "b1"},
            IOStreams.concat(a, b)
                .toArray(String[]::new)
        );

        Assert.assertArrayEquals(
            new String[0],
            IOStreams.concat(a, b)
                .toArray(String[]::new)
        );

        final TestStream<String> c = TestStream.of("c1", "c2", "c3");
        final TestStream<String> d = TestStream.of("d1", "d2");
        final TestStream<String> e = TestStream.of("e1", "e2", "e3", "e4");

        Assert.assertArrayEquals(
            new String[]{"c1", "c2", "c3", "d1", "d2", "e1", "e2", "e3", "e4"},
            IOStreams.concat(c, d, e)
                .toArray(String[]::new)
        );

        Assert.assertTrue(a.isClosed());
        Assert.assertTrue(b.isClosed());
        Assert.assertTrue(c.isClosed());
        Assert.assertTrue(d.isClosed());
        Assert.assertTrue(e.isClosed());

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

        final ErrorStream<String> f = new ErrorStream<>(new RuntimeException("f"), null);
        final ErrorStream<String> g = new ErrorStream<>(new IOException("g"), null);
        final ErrorStream<String> h = new ErrorStream<>(new RuntimeException("h"), null);
        final IOStream<String> fgh = IOStreams.concat(f, g, h);

        try
        {
            fgh.hasNext();
            Assert.fail();
        }
        catch (final RuntimeException ex)
        {
            if (!"f".equals(ex.getMessage()))
            {
                throw ex;
            }
        }

        try
        {
            fgh.next();
            Assert.fail();
        }
        catch (final RuntimeException ex)
        {
            if (!"f".equals(ex.getMessage()))
            {
                throw ex;
            }
        }

        final ErrorStream<String> i = new ErrorStream<>(null, new IOException("i"));
        final ErrorStream<String> j = new ErrorStream<>(null, new IOException("j"));
        final ErrorStream<String> k = new ErrorStream<>(null, new IOException("k"));
        final IOStream<String> ijk = IOStreams.concat(i, j, k);

        boolean closeFailed = false;
        try (final IOStream<String> autoCloseIjk = ijk)
        {
            Assert.assertTrue(ijk.hasNext());
            Assert.assertEquals(null, ijk.next());
        }
        catch (final IOStreamCloseException ex)
        {
            closeFailed = true;
            Assert.assertEquals(
                "k",
                ex.getCause()
                    .getMessage()
            );
            Assert.assertEquals(
                "j",
                ex.getCause()
                    .getSuppressed()[0].getMessage()
            );
            Assert.assertEquals(
                "i",
                ex.getCause()
                    .getSuppressed()[0].getSuppressed()[0].getMessage()
            );
        }
        Assert.assertTrue(closeFailed);

        final ErrorStream<String> l = new ErrorStream<>(null, new IOException("l"));
        final ErrorStream<String> m = new ErrorStream<>(null, new RuntimeException("m"));
        final ErrorStream<String> n = new ErrorStream<>(null, new IOException("n"));
        final IOStream<String> lmn = IOStreams.concat(l, m, n);

        try (final IOStream<String> autoCloseLmn = lmn)
        {
            lmn.limit(6)
                .consume();
        }
        catch (final RuntimeException ex)
        {
            Assert.assertEquals(
                "Suppressed a runtime exception with a checked exception.",
                ex.getMessage()
            );
            Assert.assertEquals(
                "n",
                ex.getCause()
                    .getMessage()
            );
            Assert.assertEquals(
                "m",
                ex.getCause()
                    .getSuppressed()[0].getMessage()
            );
            Assert.assertEquals(
                "l",
                ex.getCause()
                    .getSuppressed()[0].getSuppressed()[0].getMessage()
            );
        }
    }

    @Test
    public void testConcatStreamOfStreams() throws IOStreamException, InterruptedException
    {
        final TestStream<String> a = TestStream.of("a1");
        final TestStream<String> b = TestStream.of("b1");

        Assert.assertArrayEquals(
            new String[]{"a1", "b1"},
            IOStreams.concat(IOStreams.fromArray(a, b))
                .toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[0],
            IOStreams.concat(IOStreams.fromArray(a, b))
                .toArray(String[]::new)
        );

        final TestStream<String> c = TestStream.of("c1", "c2", "c3");
        final TestStream<String> d = TestStream.of("d1", "d2");
        final TestStream<String> e = TestStream.of("e1", "e2", "e3", "e4");

        Assert.assertArrayEquals(
            new String[]{"c1", "c2", "c3", "d1", "d2", "e1", "e2", "e3", "e4"},
            IOStreams.concat(IOStreams.fromArray(c, d, e))
                .toArray(String[]::new)
        );

        Assert.assertTrue(a.isClosed());
        Assert.assertTrue(b.isClosed());
        Assert.assertTrue(c.isClosed());
        Assert.assertTrue(d.isClosed());
        Assert.assertTrue(e.isClosed());

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
    public void testCount() throws IOStreamException, InterruptedException
    {
        try (final IOStream<String> a = TestStream.of("a1", "a2", "a3"))
        {
            Assert.assertEquals(3, IOStreams.count(a));
        }

        final ErrorStream<?> b = new ErrorStream<>(new IOException("b-read"), new IOException("b-close"));
        boolean caughtReadException = false;
        try
        {
            IOStreams.count(b);
        }
        catch (final IOStreamReadException ex)
        {
            Assert.assertEquals(
                "b-read",
                ex.getCause()
                    .getMessage()
            );
            caughtReadException = true;

            Assert.assertEquals(
                "b-close",
                ex.getSuppressed()[0].getCause()
                    .getMessage()
            );
        }
        Assert.assertTrue(caughtReadException);
    }

    @Test
    public void testEmptyStream() throws IOStreamException, InterruptedException
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
    public void testExceptionSuppression() throws InterruptedException
    {
        final IOException tryA = new IOException("tryA");
        final IOException finallyA = new IOException("finallyA");
        finallyA.addSuppressed(tryA);

        final IOStreamException tryB = new IOStreamReadException(finallyA);
        final IOStreamException finallyB = new IOStreamCloseException(tryB);
        finallyB.addSuppressed(tryB);

        final IOStreamException tryC = new IOStreamReadException(finallyB);
        final IOStreamException finallyC = new IOStreamCloseException(tryC);
        finallyC.addSuppressed(tryC);

        Assert.assertEquals("finallyA", finallyC.getCause().getMessage());
        Assert.assertEquals("tryA", finallyC.getCause().getSuppressed()[0].getMessage());
        Assert.assertEquals(1, finallyC.getCause().getSuppressed().length);
    }

    @Test
    public void testFilter() throws IOStreamException, InterruptedException
    {
        final TestStream<String> a = TestStream.of("a1", "a2", "a3");
        final TestStreamFilter<String> allA = TestStreamFilter.wrap(IOStreamFilters.all());
        Assert.assertArrayEquals(
            new String[]{"a1", "a2", "a3"},
            IOStreams.filter(a, allA)
                .toArray(String[]::new)
        );
        Assert.assertTrue(a.isClosed());
        Assert.assertTrue(allA.isClosed());

        final TestStream<String> b = TestStream.of("b1", "b2", "b3");
        final TestStreamFilter<String> noneB = TestStreamFilter.wrap(IOStreamFilters.none());
        Assert.assertArrayEquals(
            new String[0],
            IOStreams.filter(b, noneB)
                .toArray(String[]::new)
        );
        Assert.assertTrue(b.isClosed());
        Assert.assertTrue(noneB.isClosed());

        final TestStream<String> c = TestStream.of("c1", "c2", "c3", "c4");
        final TestStreamFilter<String> whitelistC = TestStreamFilter.wrap(IOStreamFilters.whitelist("c3", "c2"));
        Assert.assertArrayEquals(
            new String[]{"c2", "c3"},
            IOStreams.filter(c, whitelistC)
                .toArray(String[]::new)
        );
        Assert.assertTrue(c.isClosed());
        Assert.assertTrue(whitelistC.isClosed());

        final TestStream<String> d = TestStream.wrap(IOStreams.empty());
        final TestStreamFilter<String> whitelistD = TestStreamFilter.wrap(IOStreamFilters.whitelist("d3", "d2"));
        Assert.assertArrayEquals(
            new String[0],
            IOStreams.filter(d, whitelistD)
                .toArray
                    (String[]::new)
        );
        Assert.assertTrue(d.isClosed());
        Assert.assertTrue(whitelistD.isClosed());

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
    public void testFlattenArrays() throws IOStreamException, InterruptedException
    {
        Assert.assertArrayEquals(
            new String[]{"a1", "a2", "b1", "b2", "b3", "c1"},
            IOStreams.flattenArrays(
                IOStreams.fromArray(
                    new String[]{"a1", "a2"},
                    new String[]{"b1", "b2", "b3"},
                    new String[]{"c1"}
                )
            )
                .toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[]{"d1", "d2"},
            IOStreams.flattenArrays(
                IOStreams.fromArray(
                    new String[]{"d1", "d2"},
                    new String[0]
                )
            )
                .toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[]{"e1", "e2"},
            IOStreams.flattenArrays(
                IOStreams.fromArray(
                    new String[0],
                    new String[]{"e1", "e2"}
                )
            )
                .toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[0],
            IOStreams.flattenArrays(
                IOStreams.fromArray(
                    new String[0],
                    new String[0]
                )
            )
                .toArray(String[]::new)
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

        try (final IOStream<String> f = IOStreams.flattenArrays(IOStreams.fromArray(null, null)))
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
    public void testFlattenIterables() throws IOStreamException, InterruptedException
    {
        Assert.assertArrayEquals(
            new String[]{"a1", "a2", "b1", "b2", "b3", "c1"},
            IOStreams.flattenIterables(
                IOStreams.fromArray(
                    Arrays.asList("a1", "a2"),
                    Arrays.asList("b1", "b2", "b3"),
                    Collections.singletonList("c1")
                )
            )
                .toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[]{"d1", "d2"},
            IOStreams.flattenIterables(
                IOStreams.fromArray(
                    Arrays.asList("d1", "d2"),
                    Collections.emptyList()
                )
            )
                .toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[]{"e1", "e2"},
            IOStreams.flattenIterables(
                IOStreams.fromArray(
                    Collections.emptyList(),
                    Arrays.asList("e1", "e2")
                )
            )
                .toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[0],
            IOStreams.flattenIterables(
                IOStreams.fromArray(
                    Collections.emptyList(),
                    Collections.emptyList()
                )
            )
                .toArray(String[]::new)
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
    public void testFlattenIterators() throws IOStreamException, InterruptedException
    {
        Assert.assertArrayEquals(
            new String[]{"a1", "a2", "b1", "b2", "b3", "c1"},
            IOStreams.flattenIterators(
                IOStreams.fromArray(
                    Arrays.asList("a1", "a2")
                        .iterator(),
                    Arrays.asList("b1", "b2", "b3")
                        .iterator(),
                    Collections.singletonList("c1")
                        .iterator()
                )
            )
                .toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[]{"d1", "d2"},
            IOStreams.flattenIterators(
                IOStreams.fromArray(
                    Arrays.asList("d1", "d2")
                        .iterator(),
                    Collections.emptyList()
                        .iterator()
                )
            )
                .toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[]{"e1", "e2"},
            IOStreams.flattenIterators(
                IOStreams.fromArray(
                    Collections.emptyList()
                        .iterator(),
                    Arrays.asList("e1", "e2")
                        .iterator()
                )
            )
                .toArray(String[]::new)
        );
        Assert.assertArrayEquals(
            new String[0],
            IOStreams.flattenIterators(
                IOStreams.fromArray(
                    Collections.emptyList()
                        .iterator(),
                    Collections.emptyList()
                        .iterator()
                )
            )
                .toArray(String[]::new)
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
    public void testFold() throws IOStreamException, InterruptedException
    {
        final TestStream<Integer> a = TestStream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        final Long aFolded = a.fold(0L, (sum, i) -> sum + i);
        Assert.assertEquals(55L, aFolded.longValue());
        Assert.assertTrue(a.isClosed());
    }

    @Test
    public void testGroup() throws IOStreamException, InterruptedException
    {
        final TestStream<String> a = TestStream.of("a1", "a2", "a3", "a4", "a5");
        try (final IOStream<? extends IOStream<String>> aGroups = IOStreams.group(a, 2))
        {
            Assert.assertArrayEquals(
                new String[]{"a1", "a2"},
                aGroups.next()
                    .toArray(String[]::new)
            );
            Assert.assertArrayEquals(
                new String[]{"a3", "a4"},
                aGroups.next()
                    .toArray(String[]::new)
            );
            Assert.assertArrayEquals(
                new String[]{"a5"},
                aGroups.next()
                    .toArray(String[]::new)
            );
            Assert.assertFalse(aGroups.hasNext());
        }

        final TestStream<String> b = TestStream.of("b1", "b2", "b3");
        try (final IOStream<? extends IOStream<String>> bGroups = IOStreams.group(b, 5))
        {
            Assert.assertArrayEquals(
                new String[]{"b1", "b2", "b3"},
                bGroups.next()
                    .toArray(String[]::new)
            );
            Assert.assertFalse(bGroups.hasNext());
        }

        final TestStream<String> c = TestStream.of();
        try (final IOStream<? extends IOStream<String>> cGroups = IOStreams.group(c, 5))
        {
            Assert.assertFalse(cGroups.hasNext());
        }

        Assert.assertTrue(a.isClosed());
        Assert.assertTrue(b.isClosed());
        Assert.assertTrue(c.isClosed());

        Assert.assertTrue(a.isClosed());
        Assert.assertTrue(b.isClosed());

        try
        {
            IOStreams.group(null, 1);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        try
        {
            IOStreams.group(b, 0);
            Assert.fail();
        }
        catch (final IllegalArgumentException ex)
        {
            // Expected
        }

        try
        {
            IOStreams.group(b, -10);
            Assert.fail();
        }
        catch (final IllegalArgumentException ex)
        {
            // Expected
        }
    }

    @Test
    public void testGroupAdjacent() throws IOStreamException, InterruptedException
    {
        final TestStream<String> a = TestStream.of(
            "a-a1", "a-a2", "a-a3", "a-b1", "a-b2", "a-a4", "a-c1", "a-d1", "a-d2"
        );
        try (final IOStream<? extends IOStream<String>> aGroups = a.group((l, r) -> l.charAt(2) == r.charAt(2)))
        {
            Assert.assertArrayEquals(
                new String[]{"a-a1", "a-a2", "a-a3"},
                aGroups.next()
                    .toArray(String[]::new)
            );
            Assert.assertFalse(a.isClosed());
            Assert.assertArrayEquals(
                new String[]{"a-b1", "a-b2"},
                aGroups.next()
                    .toArray(String[]::new)
            );
            Assert.assertFalse(a.isClosed());
            Assert.assertArrayEquals(
                new String[]{"a-a4"},
                aGroups.next()
                    .toArray(String[]::new)
            );
            Assert.assertFalse(a.isClosed());
            Assert.assertArrayEquals(
                new String[]{"a-c1"},
                aGroups.next()
                    .toArray(String[]::new)
            );
            Assert.assertFalse(a.isClosed());
            Assert.assertArrayEquals(
                new String[]{"a-d1", "a-d2"},
                aGroups.next()
                    .toArray(String[]::new)
            );
            Assert.assertFalse(a.isClosed());
            Assert.assertFalse(aGroups.hasNext());
        }
        Assert.assertTrue(a.isClosed());

        final TestStream<String> b = TestStream.of("b-a1", "b-a2", "b-a3", "b-b1", "b-b2");
        try (final IOStream<? extends IOStream<String>> bGroups = b.group((l, r) -> l.charAt(2) == r.charAt(2)))
        {
            try (final IOStream<String> bOrphan = bGroups.next())
            {
                Assert.assertEquals("b-a1", bOrphan.next());
            }
            Assert.assertArrayEquals(
                new String[]{"b-a2", "b-a3"},
                bGroups.next()
                    .toArray(String[]::new)
            );
            Assert.assertArrayEquals(
                new String[]{"b-b1", "b-b2"},
                bGroups.next()
                    .toArray(String[]::new)
            );
            Assert.assertFalse(bGroups.hasNext());
        }
        Assert.assertTrue(b.isClosed());

        final TestStream<String> c = TestStream.of("c-a1");
        try (final IOStream<? extends IOStream<String>> cGroups = c.group((l, r) -> false))
        {
            Assert.assertArrayEquals(
                new String[]{"c-a1"},
                cGroups.next()
                    .toArray(String[]::new)
            );
            Assert.assertFalse(cGroups.hasNext());
        }
        Assert.assertTrue(c.isClosed());

        final TestStream<String> d = TestStream.wrap(IOStreams.empty());
        try (final IOStream<? extends IOStream<String>> dGroups = d.split((l, r) -> false))
        {
            Assert.assertFalse(dGroups.hasNext());
        }
        Assert.assertTrue(d.isClosed());

        try
        {
            IOStreams.group(null, (l, r) -> false);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        try
        {
            IOStreams.group(a, null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    @Test
    public void testIterableStream() throws IOStreamException, InterruptedException
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
    public void testIteratorStream() throws IOStreamException, InterruptedException
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
    public void testKeep() throws IOStreamException, InterruptedException
    {
        try (final IOStream<String> a = IOStreams.keep(TestStream.of("a1", "a2", "a3"), "a2"::equals))
        {
            Assert.assertArrayEquals(new String[]{"a2"}, a.toArray(String[]::new));
        }
    }

    @Test
    public void testLimit() throws IOStreamException, InterruptedException
    {
        try (final IOStream<String> a = IOStreams.limit(TestStream.of("a1", "a2", "a3"), 2))
        {
            Assert.assertArrayEquals(new String[]{"a1", "a2"}, a.toArray(String[]::new));
        }
    }

    @Test
    public void testMap() throws IOStreamException, InterruptedException
    {
        try (final IOStream<String> a = IOStreams.map(TestStream.of("a1", "a2", "a3"), s -> s.replace('a', 'z')))
        {
            Assert.assertArrayEquals(new String[]{"z1", "z2", "z3"}, a.toArray(String[]::new));
        }

        try (final IOStream<String> b = IOStreams.map(
            TestStream.of("b1", "b2", "b3"),
            new IOStreamTransform<String, String>()
            {
                @Override
                public String apply(final String item)
                {
                    return item.replace('b', 'z');
                }

                @Override
                public void close() throws IOException
                {
                    throw new IOException("b");
                }
            }
        ))
        {
            Assert.assertArrayEquals(new String[]{"z1", "z2", "z3"}, b.toArray(String[]::new));
        }
        catch (final IOStreamCloseException ex)
        {
            Assert.assertEquals(
                "b",
                ex.getCause()
                    .getMessage()
            );
        }

        boolean caughtCloseException = false;
        try (final IOStream<String> c = IOStreams.map(
            new ErrorStream<>(new IOException("c-read"), new RuntimeException("c-close")),
            new IOStreamTransform<String, String>()
            {
                @Override
                public String apply(final String item)
                {
                    throw new RuntimeException("c-map");
                }

                @Override
                public void close() throws IOException
                {
                    throw new IOException("c-map-close");
                }
            }
        ))
        {
            try
            {
                c.hasNext();
                Assert.fail();
            }
            catch (final IOStreamReadException ex)
            {
                Assert.assertEquals(
                    "c-read",
                    ex.getCause()
                        .getMessage()
                );
            }
        }
        catch (final IOStreamCloseException ex)
        {
            caughtCloseException = true;
            Assert.assertEquals(
                "c-map-close",
                ex.getCause()
                    .getMessage()
            );
            Assert.assertEquals(
                "c-close",
                ex.getCause()
                    .getSuppressed()[0]
                    .getMessage()
            );
        }
        Assert.assertTrue(caughtCloseException);
    }

    @Test
    public void testMapWithExceptionHandler() throws IOStreamException, InterruptedException
    {
        final IOStreamTransformExceptionHandler<String> skipHandler = (item, ex) -> FilterDecision.SKIP_AND_CONTINUE;

        try (final IOStream<String> a = IOStreams.map(
            TestStream.of("a1", "a2", "a3"),
            s -> s.replace('a', 'z'),
            skipHandler
        ))
        {
            Assert.assertArrayEquals(new String[]{"z1", "z2", "z3"}, a.toArray(String[]::new));
        }

        try (final IOStream<String> b = IOStreams.map(
            TestStream.of("b1", "b2", "b3"),
            new IOStreamTransform<String, String>()
            {
                @Override
                public String apply(final String item) throws Exception
                {
                    if ("b2".equals(item))
                    {
                        throw new IOException("b2");
                    }
                    return item;
                }

                @Override
                public void close() throws IOException
                {
                    throw new IOException("b");
                }
            }, skipHandler
        ))
        {
            Assert.assertArrayEquals(new String[]{"z1", "z3"}, b.toArray(String[]::new));
        }
        catch (final IOStreamCloseException ex)
        {
            Assert.assertEquals(
                "b",
                ex.getCause()
                    .getMessage()
            );
        }
    }

    @Test
    public void testMax() throws IOStreamException, InterruptedException
    {
        try (TestStream<Integer> a = TestStream.of(1, 4, 6, 10, 2, 9, -2))
        {
            Assert.assertEquals(
                10,
                (int) IOStreams.max(a, Comparator.naturalOrder())
                    .orElse(-1)
            );

            Assert.assertTrue(a.isClosed());
        }

        try (TestStream<Integer> b = TestStream.of())
        {
            Assert.assertEquals(
                null,
                IOStreams.max(b, Comparator.naturalOrder())
                    .orElse(null)
            );

            Assert.assertTrue(b.isClosed());
        }
    }

    @Test
    public void testMin() throws IOStreamException, InterruptedException
    {
        try (TestStream<Integer> a = TestStream.of(5, 7, 2, 10, -3, 1, 0))
        {
            Assert.assertEquals(
                -3,
                (int) IOStreams.min(a, Comparator.naturalOrder())
                    .orElse(100)
            );

            Assert.assertTrue(a.isClosed());
        }

        try (TestStream<Integer> b = TestStream.of())
        {
            Assert.assertEquals(
                null,
                IOStreams.min(b, Comparator.naturalOrder())
                    .orElse(null)
            );

            Assert.assertTrue(b.isClosed());
        }
    }

    @Test
    public void testObserve() throws IOStreamException, InterruptedException
    {
        final IOStream<String> a = IOStreams.fromArray("a1", "a2", "a3", "a4", "a5");
        final ArrayList<String> aObservedValues = new ArrayList<>(5);
        final String[] aValues = a.observe(aObservedValues::add)
            .toArray(String[]::new);

        Assert.assertArrayEquals(
            new String[]{"a1", "a2", "a3", "a4", "a5"},
            aObservedValues.toArray(new String[aObservedValues.size()])
        );
        Assert.assertArrayEquals(new String[]{"a1", "a2", "a3", "a4", "a5"}, aValues);
        Assert.assertFalse(a.hasNext());

        try
        {
            IOStreams.observe(null, value ->
            {
            });
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        try
        {
            IOStreams.observe(a, null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    @Test
    public void testPeekable() throws IOStreamException, InterruptedException
    {
        final IOStream<String> a = IOStreams.fromArray("a1", "a2", "a3", "a4", "a5");
        final PeekableIOStream<String> aPeekable = IOStreams.peekable(a);
        final String[] aPeeked = StreamSupport.stream(aPeekable.peek(3)
            .spliterator(), false)
            .toArray(String[]::new);

        Assert.assertArrayEquals(new String[]{"a1", "a2", "a3"}, aPeeked);
        Assert.assertArrayEquals(new String[]{"a1", "a2", "a3", "a4", "a5"}, aPeekable.toArray(String[]::new));
        Assert.assertFalse(aPeekable.hasNext());
        Assert.assertFalse(aPeekable.peek(1)
            .iterator()
            .hasNext());

        final IOStream<String> b = IOStreams.fromArray("b1", "b2", "b3", "b4");
        final PeekableIOStream<String> bPeekable = IOStreams.peekable(b);
        final String[] bPeekedFirst = StreamSupport.stream(bPeekable.peek(3)
            .spliterator(), false)
            .toArray(String[]::new);
        Assert.assertArrayEquals(new String[]{"b1", "b2", "b3"}, bPeekedFirst);
        Assert.assertEquals("b1", bPeekable.next());
        final String[] bPeekedSecond = StreamSupport.stream(bPeekable.peek(3)
            .spliterator(), false)
            .toArray(String[]::new);
        Assert.assertArrayEquals(new String[]{"b2", "b3", "b4"}, bPeekedSecond);

        try
        {
            aPeekable.peek();
            Assert.fail();
        }
        catch (final NoSuchElementException ex)
        {
            // Expected
        }

        try
        {
            IOStreams.peekable(null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    @Test
    public void testReduce() throws IOStreamException, InterruptedException
    {
        try (final IOStream<String> a = IOStreams.fromArray("a1", "a2", "a3", "a4", "a5"))
        {
            final String aReduced = a.reduce(values ->
            {
                final StringBuilder builder = new StringBuilder();
                values.consume(builder::append);
                return builder.toString();
            });
            Assert.assertEquals("a1a2a3a4a5", aReduced);
        }

        try (final IOStream<Integer> b = IOStreams.fromArray(1, 2, 3, 4, 5))
        {
            b.reduce(new IOStreamTransform<IOStream<Integer>, Integer>()
            {
                @Override
                public Integer apply(final IOStream<Integer> values) throws Exception
                {
                    return 0;
                }

                @Override
                public void close() throws Exception
                {
                    throw new IOException("b");
                }
            });
            Assert.fail();
        }
        catch (final IOStreamCloseException ex)
        {
            Assert.assertEquals(
                "b",
                ex.getCause()
                    .getMessage()
            );
        }
    }

    @Test
    public void testRepeat() throws IOStreamException, InterruptedException
    {
        final AtomicInteger counterA = new AtomicInteger(1);
        final TestStream<Integer> a = TestStream.wrap(IOStreams.repeat(counterA::getAndIncrement));
        Assert.assertArrayEquals(
            new Integer[]{1, 2, 3, 4, 5},
            a.until(i -> i > 5)
                .toArray(Integer[]::new)
        );
        Assert.assertTrue(a.isClosed());
        Assert.assertFalse(a.hasNext());
        try
        {
            a.next();
            Assert.fail();
        }
        catch (final NoSuchElementException ex)
        {
            // Expected
        }
        try
        {
            IOStreams.repeat(null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        final AtomicInteger counterB = new AtomicInteger(1);
        final TestStream<String> b = TestStream.wrap(IOStreams.repeat(() ->
        {
            throw new IOException("b" + counterB.getAndIncrement());
        }));
        try
        {
            b.hasNext();
            Assert.fail();
        }
        catch (final IOStreamException ex)
        {
            Assert.assertEquals(
                "b1",
                ex.getCause()
                    .getMessage()
            );
        }
        try
        {
            b.next();
            Assert.fail();
        }
        catch (final IOStreamException ex)
        {
            Assert.assertEquals(
                "b2",
                ex.getCause()
                    .getMessage()
            );
        }
        Assert.assertFalse(b.isClosed());
        try
        {
            b.consume();
            Assert.fail();
        }
        catch (final IOStreamException ex)
        {
            Assert.assertEquals(
                "b3",
                ex.getCause()
                    .getMessage()
            );
        }
        Assert.assertTrue(b.isClosed());
        b.consume();
    }

    @Test
    public void testSingletonStream() throws IOStreamException, InterruptedException
    {
        final String singleton = "a";
        try (final IOStream<Object> stream = IOStreams.singleton(singleton))
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
    public void testSkipByCount() throws IOStreamException, InterruptedException
    {
        try (final IOStream<String> a = IOStreams.skip(TestStream.of("a1", "a2", "a3", "a4", "a5", "a6", "a7"), 3))
        {
            Assert.assertArrayEquals(new String[]{"a4", "a5", "a6", "a7"}, a.toArray(String[]::new));
        }

        try (final IOStream<String> a = IOStreams.skip(TestStream.of("a1", "a2", "a3"), 0))
        {
            Assert.assertArrayEquals(new String[]{"a1", "a2", "a3"}, a.toArray(String[]::new));
        }
    }

    @Test
    public void testSkipByPredicate() throws IOStreamException, InterruptedException
    {
        try (final IOStream<String> a = IOStreams.skip(TestStream.of("a1", "a2", "a3"), "a2"::equals))
        {
            Assert.assertArrayEquals(new String[]{"a1", "a3"}, a.toArray(String[]::new));
        }
    }

    @Test
    public void testToArray() throws IOStreamException, InterruptedException
    {
        final IOStream<String> a = IOStreams.fromArray();
        Assert.assertArrayEquals(new String[0], IOStreams.toArray(a, String[]::new));

        final IOStream<String> b = IOStreams.fromArray("b1", "b2", "b3");
        Assert.assertArrayEquals(new String[]{"b1", "b2", "b3"}, IOStreams.toArray(b, String[]::new));
    }

    @Test
    public void testToList() throws IOStreamException, InterruptedException
    {
        final IOStream<String> a = IOStreams.fromArray();
        Assert.assertEquals(new ArrayList<String>(0), IOStreams.toList(a));

        final IOStream<String> b = IOStreams.fromArray("b1", "b2", "b3");
        Assert.assertEquals(Arrays.asList("b1", "b2", "b3"), IOStreams.toList(b));
    }

    @Test
    public void testToSet() throws IOStreamException, InterruptedException
    {
        final IOStream<String> a = IOStreams.fromArray();
        Assert.assertEquals(new HashSet<String>(0), IOStreams.toSet(a));

        final IOStream<String> b = IOStreams.fromArray("b1", "b2", "b3");
        Assert.assertEquals(new HashSet<>(Arrays.asList("b1", "b2", "b3")), IOStreams.toSet(b));
    }

    @Test
    public void testTruncate() throws IOStreamException, InterruptedException
    {
        TestStream<String> a = TestStream.of("a1", "a2", "a3", "a4");
        IOStream<String> truncatedA = IOStreams.empty(a);
        Assert.assertFalse(truncatedA.hasNext());
        try
        {
            truncatedA.next();
            Assert.fail();
        }
        catch (final NoSuchElementException ex)
        {
            // Expected
        }
        Assert.assertFalse(a.isClosed());
        truncatedA.close();
        Assert.assertTrue(a.isClosed());

        boolean caughtCloseException = false;
        try (final IOStream<String> b = IOStreams.empty(new ErrorStream<>(
            new IOException("b-read"),
            new IOException("b-close")
        )))
        {
            Assert.assertFalse(b.hasNext());
        }
        catch (final IOStreamCloseException ex)
        {
            Assert.assertEquals(
                "b-close",
                ex.getCause()
                    .getMessage()
            );
            caughtCloseException = true;
        }
        Assert.assertTrue(caughtCloseException);
    }

    @Test
    public void testUntil() throws IOStreamException, InterruptedException
    {
        final TestStream<String> a = TestStream.of("a1", "a2", "a3", "a4");
        Assert.assertArrayEquals(
            new String[]{"a1", "a2"},
            IOStreams.until(a, "a3"::equals)
                .toArray(String[]::new)
        );
        Assert.assertTrue(a.isClosed());
        Assert.assertArrayEquals(
            new String[0],
            IOStreams.until(a, "a3"::equals)
                .toArray(String[]::new)
        );
        Assert.assertTrue(a.isClosed());

        try
        {
            IOStreams.until(a, null);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }

        try
        {
            IOStreams.until(null, "a3"::equals);
            Assert.fail();
        }
        catch (final NullPointerException ex)
        {
            // Expected
        }
    }

    @Test
    public void testZip() throws IOStreamException, InterruptedException
    {
        TestStream<String> a1 = TestStream.of("a1-1", "a1-2", "a1-3");
        TestStream<String> a2 = TestStream.of("a2-1", "a2-2", "a2-3");
        try (final IOStream<String> aZipped = IOStreams.zip(a1, a2, (l, r) -> l + "+" + r))
        {
            Assert.assertArrayEquals(
                new String[]{
                    "a1-1+a2-1",
                    "a1-2+a2-2",
                    "a1-3+a2-3"
                },
                aZipped.toArray(String[]::new)
            );
        }
        Assert.assertTrue(a1.isClosed());
        Assert.assertTrue(a2.isClosed());

        TestStream<String> b1 = TestStream.of("b1-1", "b1-2");
        TestStream<String> b2 = TestStream.of("b2-1", "b2-2", "b2-3", "b2-4");
        try (final IOStream<String> bZipped = IOStreams.zip(b1, b2, (l, r) -> l + "+" + r))
        {
            Assert.assertArrayEquals(
                new String[]{
                    "b1-1+b2-1",
                    "b1-2+b2-2"
                },
                new String[]{
                    bZipped.next(),
                    bZipped.next()
                }
            );

            Assert.assertFalse(bZipped.hasNext());

            Assert.assertFalse(b1.hasNext());
            Assert.assertTrue(b2.hasNext());

            Assert.assertFalse(b1.isClosed());
            Assert.assertFalse(b2.isClosed());

            Assert.assertArrayEquals(
                new String[]{
                    "b2-3",
                    "b2-4"
                },
                b2.toArray(String[]::new)
            );
        }
        Assert.assertTrue(b1.isClosed());
        Assert.assertTrue(b2.isClosed());
    }

    @Test
    public void testZipAll() throws IOStreamException, InterruptedException
    {
        TestStream<String> a1 = TestStream.of("a1-1", "a1-2", "a1-3");
        TestStream<String> a2 = TestStream.of("a2-1", "a2-2", "a2-3");
        try (final IOStream<String> aZipped = IOStreams.zipAll(
            a1,
            a2,
            (l, r) -> l + "+" + r,
            () -> "a1-x",
            () -> "a2-x"
        ))
        {
            Assert.assertArrayEquals(
                new String[]{
                    "a1-1+a2-1",
                    "a1-2+a2-2",
                    "a1-3+a2-3"
                },
                aZipped.toArray(String[]::new)
            );
        }
        Assert.assertTrue(a1.isClosed());
        Assert.assertTrue(a2.isClosed());

        TestStream<String> b1 = TestStream.of("b1-1", "b1-2");
        TestStream<String> b2 = TestStream.of("b2-1", "b2-2", "b2-3", "b2-4");
        try (final IOStream<String> bZipped = IOStreams.zipAll(
            b1,
            b2,
            (l, r) -> l + "+" + r,
            () -> "b1-x",
            () -> "b2-x"
        ))
        {
            Assert.assertArrayEquals(
                new String[]{
                    "b1-1+b2-1",
                    "b1-2+b2-2",
                    "b1-x+b2-3",
                    "b1-x+b2-4"
                },
                new String[]{
                    bZipped.next(),
                    bZipped.next(),
                    bZipped.next(),
                    bZipped.next()
                }
            );

            Assert.assertFalse(b1.hasNext());
            Assert.assertFalse(b2.hasNext());

            Assert.assertFalse(b1.isClosed());
            Assert.assertFalse(b2.isClosed());
        }
        Assert.assertTrue(b1.isClosed());
        Assert.assertTrue(b2.isClosed());
    }
}
