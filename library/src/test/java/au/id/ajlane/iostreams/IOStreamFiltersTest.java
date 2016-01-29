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
 * Tests {@link IOStreamFilters}.
 */
public class IOStreamFiltersTest
{
    /**
     * Tests {@link IOStreamFilters#all()}.
     *
     * @throws IOStreamException
     *         If a {@code IOStream} fails unexpectedly.
     */
    @Test
    public void testAllFilter() throws IOStreamException
    {
        final IOStream<String> original = IOStreams.fromArray("a", "b", "c");
        try (final IOStream<String> stream = IOStreams.filter(original, IOStreamFilters.all()))
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
     * Tests {@link IOStreamFilters#blacklist(Object[])}.
     *
     * @throws IOStreamException
     *         If a {@link IOStream} fails unexpectedly.
     */
    @Test
    public void testArrayBlacklistFilter() throws IOStreamException
    {
        final IOStream<String> original = IOStreams.fromArray("a", "b", "c");
        try (final IOStream<String> stream = IOStreams.filter(original, IOStreamFilters.blacklist("a", "c")))
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
     * Tests {@link IOStreamFilters#whitelist(Object[])}.
     *
     * @throws IOStreamException
     *         If a {@link IOStream} fails unexpectedly.
     */
    @Test
    public void testArrayWhitelistFilter() throws IOStreamException
    {
        final IOStream<String> original = IOStreams.fromArray("a", "b", "c");
        try (final IOStream<String> stream = IOStreams.filter(original, IOStreamFilters.whitelist("b", "c")))
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
     * Tests {@link IOStreamFilters#blacklist(Collection)}.
     *
     * @throws IOStreamException
     *         If a {@code IOStream} fails unexpectedly.
     */
    @Test
    public void testCollectionBlacklistFilter() throws IOStreamException
    {
        final IOStream<String> original = IOStreams.fromArray("a", "b", "c");
        final Collection<String> collection = Arrays.asList("a", "c");
        try (final IOStream<String> stream = IOStreams.filter(
            original,
            IOStreamFilters.blacklist(collection)
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
     * Tests {@link IOStreamFilters#whitelist(Object[])}.
     *
     * @throws IOStreamException
     *         If a {@link IOStream} fails unexpectedly.
     */
    @Test
    public void testCollectionWhitelistFilter() throws IOStreamException
    {
        final IOStream<String> original = IOStreams.fromArray("a", "b", "c");
        final Collection<String> collection = Arrays.asList("b", "c");
        try (final IOStream<String> stream = IOStreams.filter(
            original,
            IOStreamFilters.whitelist(collection)
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
     * Tests {@link IOStreamFilters#blacklist(Iterable)}.
     *
     * @throws IOStreamException
     *         If a {@code IOStream} fails unexpectedly.
     */
    @Test
    public void testIterableBlacklistFilter() throws IOStreamException
    {
        final IOStream<String> original = IOStreams.fromArray("a", "b", "c");
        final Iterable<String> iterable = Arrays.asList("a", "c");
        try (final IOStream<String> stream = IOStreams.filter(
            original,
            IOStreamFilters.blacklist(iterable)
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
     * Tests {@link IOStreamFilters#whitelist(Object[])}.
     *
     * @throws IOStreamException
     *         If a {@link IOStream} fails unexpectedly.
     */
    @Test
    public void testIterableWhitelistFilter() throws IOStreamException
    {
        final IOStream<String> original = IOStreams.fromArray("a", "b", "c");
        final Iterable<String> iterable = Arrays.asList("b", "c");
        try (final IOStream<String> stream = IOStreams.filter(
            original,
            IOStreamFilters.whitelist(iterable)
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
     * Tests {@link IOStreamFilters#none()}.
     *
     * @throws IOStreamException
     *         If a {@link IOStream} fails unexpectedly.
     */
    @Test
    public void testNoneFilter() throws IOStreamException
    {
        final IOStream<String> original = IOStreams.fromArray("a", "b", "c");
        try (final IOStream<String> stream = IOStreams.filter(original, IOStreamFilters.none()))
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
     * Tests {@link IOStreamFilters#blacklist(Collection)}.
     *
     * @throws IOStreamException
     *         If a {@code IOStream} fails unexpectedly.
     */
    @Test
    public void testSetBlacklistFilter() throws IOStreamException
    {
        final IOStream<String> original = IOStreams.fromArray("a", "b", "c");
        final Set<String> set = new HashSet<>(
                Arrays.asList("a", "c")
        );
        try (final IOStream<String> stream = IOStreams.filter(
            original,
            IOStreamFilters.blacklist(set)
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
     * Tests {@link IOStreamFilters#whitelist(Set)}.
     *
     * @throws IOStreamException
     *         If a {@link IOStream} fails unexpectedly.
     */
    @Test
    public void testSetWhitelistFilter() throws IOStreamException
    {
        final IOStream<String> original = IOStreams.fromArray("a", "b", "c");
        final Set<String> set = new HashSet<>(
                Arrays.asList("b", "c")
        );
        try (final IOStream<String> stream = IOStreams.filter(
            original,
            IOStreamFilters.whitelist(set)
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
