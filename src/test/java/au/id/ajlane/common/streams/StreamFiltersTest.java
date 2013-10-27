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
 * Tests {@link StreamFilters}.
 */
public class StreamFiltersTest
{
    /**
     * Tests {@link StreamFilters#all()}.
     *
     * @throws StreamException
     *         If a {@code Stream} fails unexpectedly.
     */
    @Test
    public void testAllFilter() throws StreamException
    {
        final Stream<String> original = Streams.fromArray("a", "b", "c");
        try (final Stream<String> stream = Streams.filter(original, StreamFilters.all()))
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
     * Tests {@link StreamFilters#blacklist(Object[])}.
     *
     * @throws StreamException
     *         If a {@link Stream} fails unexpectedly.
     */
    @Test
    public void testArrayBlacklistFilter() throws StreamException
    {
        final Stream<String> original = Streams.fromArray("a", "b", "c");
        try (final Stream<String> stream = Streams.filter(original, StreamFilters.blacklist("a", "c")))
        {
            Assert.assertTrue(stream.hasNext());
            Assert.assertEquals("b", stream.next());
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
     * Tests {@link StreamFilters#whitelist(Object[])}.
     *
     * @throws StreamException
     *         If a {@link Stream} fails unexpectedly.
     */
    @Test
    public void testArrayWhitelistFilter() throws StreamException
    {
        final Stream<String> original = Streams.fromArray("a", "b", "c");
        try (final Stream<String> stream = Streams.filter(original, StreamFilters.whitelist("b", "c")))
        {
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
     * Tests {@link StreamFilters#blacklist(Collection)}.
     *
     * @throws StreamException
     *         If a {@code Stream} fails unexpectedly.
     */
    @Test
    public void testCollectionBlacklistFilter() throws StreamException
    {
        final Stream<String> original = Streams.fromArray("a", "b", "c");
        final Collection<String> collection = Arrays.asList("a", "c");
        try (final Stream<String> stream = Streams.filter(
                original,
                StreamFilters.blacklist(collection)
        )
        )
        {
            Assert.assertTrue(stream.hasNext());
            Assert.assertEquals("b", stream.next());
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
     * Tests {@link StreamFilters#whitelist(Object[])}.
     *
     * @throws StreamException
     *         If a {@link Stream} fails unexpectedly.
     */
    @Test
    public void testCollectionWhitelistFilter() throws StreamException
    {
        final Stream<String> original = Streams.fromArray("a", "b", "c");
        final Collection<String> collection = Arrays.asList("b", "c");
        try (final Stream<String> stream = Streams.filter(
                original,
                StreamFilters.whitelist(collection)
        ))
        {
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
     * Tests {@link StreamFilters#blacklist(Iterable)}.
     *
     * @throws StreamException
     *         If a {@code Stream} fails unexpectedly.
     */
    @Test
    public void testIterableBlacklistFilter() throws StreamException
    {
        final Stream<String> original = Streams.fromArray("a", "b", "c");
        final Iterable<String> iterable = Arrays.asList("a", "c");
        try (final Stream<String> stream = Streams.filter(
                original,
                StreamFilters.blacklist(iterable)
        ))
        {
            Assert.assertTrue(stream.hasNext());
            Assert.assertEquals("b", stream.next());
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
     * Tests {@link StreamFilters#whitelist(Object[])}.
     *
     * @throws StreamException
     *         If a {@link Stream} fails unexpectedly.
     */
    @Test
    public void testIterableWhitelistFilter() throws StreamException
    {
        final Stream<String> original = Streams.fromArray("a", "b", "c");
        final Iterable<String> iterable = Arrays.asList("b", "c");
        try (final Stream<String> stream = Streams.filter(
                original,
                StreamFilters.whitelist(iterable)
        ))
        {
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
     * Tests {@link StreamFilters#none()}.
     *
     * @throws StreamException
     *         If a {@link Stream} fails unexpectedly.
     */
    @Test
    public void testNoneFilter() throws StreamException
    {
        final Stream<String> original = Streams.fromArray("a", "b", "c");
        try (final Stream<String> stream = Streams.filter(original, StreamFilters.none()))
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
     * Tests {@link StreamFilters#blacklist(Collection)}.
     *
     * @throws StreamException
     *         If a {@code Stream} fails unexpectedly.
     */
    @Test
    public void testSetBlacklistFilter() throws StreamException
    {
        final Stream<String> original = Streams.fromArray("a", "b", "c");
        final Set<String> set = new HashSet<>(
                Arrays.asList("a", "c")
        );
        try (final Stream<String> stream = Streams.filter(
                original,
                StreamFilters.blacklist(set)
        ))
        {
            Assert.assertTrue(stream.hasNext());
            Assert.assertEquals("b", stream.next());
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
     * Tests {@link StreamFilters#whitelist(Set)}.
     *
     * @throws StreamException
     *         If a {@link Stream} fails unexpectedly.
     */
    @Test
    public void testSetWhitelistFilter() throws StreamException
    {
        final Stream<String> original = Streams.fromArray("a", "b", "c");
        final Set<String> set = new HashSet<>(
                Arrays.asList("b", "c")
        );
        try (final Stream<String> stream = Streams.filter(
                original,
                StreamFilters.whitelist(set)
        ))
        {
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
}
