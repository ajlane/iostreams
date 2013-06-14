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

public class StreamsTests {

    @Test
    public void testConcatArrayOfStreams() throws StreamException {
        final Stream<String> a = Streams.fromArray("a1");
        final Stream<String> b = Streams.fromArray("b1");

        Assert.assertArrayEquals(new String[]{"a1", "b1"}, Streams.toArray(Streams.concat(a, b)));
        Assert.assertArrayEquals(new String[]{}, Streams.toArray(Streams.concat(a, b)));

        final Stream<String> c = Streams.fromArray("c1", "c2", "c3");
        final Stream<String> d = Streams.fromArray("d1", "d2");
        final Stream<String> e = Streams.fromArray("e1", "e2", "e3", "e4");

        Assert.assertArrayEquals(new String[]{"c1", "c2", "c3", "d1", "d2", "e1", "e2", "e3", "e4"}, Streams.toArray(Streams.concat(c, d, e)));

        try {
            Streams.concat((Stream<String>[]) null);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }
        try (final Stream<String> g = Streams.concat(null, null)) {
            try {
                g.hasNext();
                Assert.fail();
            } catch (NullPointerException ex) {
                // Expected
            }
        }
    }

    @Test
    public void testConcatStreamOfStreams() throws StreamException {
        final Stream<String> a = Streams.fromArray("a1");
        final Stream<String> b = Streams.fromArray("b1");

        Assert.assertArrayEquals(new String[]{"a1", "b1"}, Streams.toArray(Streams.concat(Streams.fromArray(a, b))));
        Assert.assertArrayEquals(new String[]{}, Streams.toArray(Streams.concat(Streams.fromArray(a, b))));

        final Stream<String> c = Streams.fromArray("c1", "c2", "c3");
        final Stream<String> d = Streams.fromArray("d1", "d2");
        final Stream<String> e = Streams.fromArray("e1", "e2", "e3", "e4");

        Assert.assertArrayEquals(new String[]{"c1", "c2", "c3", "d1", "d2", "e1", "e2", "e3", "e4"}, Streams.toArray(Streams.concat(Streams.fromArray(c, d, e))));

        try {
            Streams.concat((Stream<Stream<String>>) null);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }

        try (final Stream<String> g = Streams.concat(Streams.<Stream<String>>fromArray(null, null))) {
            try {
                g.hasNext();
                Assert.fail();
            } catch (NullPointerException ex) {
                // Expected
            }
        }
    }

    @Test
    public void testEmptyStream() throws StreamException {
        try (final Stream<String> stream = Streams.empty()) {
            Assert.assertFalse(stream.hasNext());
            try {
                stream.next();
                Assert.fail();
            } catch (NoSuchElementException ex) {
                // Expected
            }
        }
    }

    @Test
    public void testFilter() throws StreamException {
        Assert.assertArrayEquals(new String[]{"a1", "a2", "a3"}, Streams.toArray(Streams.filter(Streams.fromArray("a1", "a2", "a3"), StreamFilters.<String>all())));
        Assert.assertArrayEquals(new String[]{}, Streams.toArray(Streams.filter(Streams.fromArray("b1", "b2", "b3"), StreamFilters.<String>none())));
        Assert.assertArrayEquals(new String[]{"c2", "c3"}, Streams.toArray(Streams.filter(Streams.fromArray("c1", "c2", "c3", "c4"), StreamFilters.whitelist("c2", "c3"))));

        Assert.assertArrayEquals(new String[]{}, Streams.toArray(Streams.filter(Streams.<String>empty(), StreamFilters.whitelist("c2", "c3"))));

        try {
            Streams.filter(null, StreamFilters.all());
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }
        try {
            Streams.filter(Streams.empty(), null);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }
        try {
            Streams.filter(null, null);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }
    }

    @Test
    public void testFlattenArrays() throws StreamException {
        Assert.assertArrayEquals(new String[]{"a1", "a2", "b1", "b2", "b3", "c1"}, Streams.toArray(Streams.flattenArrays(Streams.fromArray(new String[]{"a1", "a2"}, new String[]{"b1", "b2", "b3"}, new String[]{"c1"}))));
        Assert.assertArrayEquals(new String[]{"d1", "d2"}, Streams.toArray(Streams.flattenArrays(Streams.fromArray(new String[]{"d1", "d2"}, new String[]{}))));
        Assert.assertArrayEquals(new String[]{"e1", "e2"}, Streams.toArray(Streams.flattenArrays(Streams.fromArray(new String[]{}, new String[]{"e1", "e2"}))));
        Assert.assertArrayEquals(new String[]{}, Streams.toArray(Streams.flattenArrays(Streams.fromArray(new String[]{}, new String[]{}))));

        try {
            Streams.flattenArrays(null);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }

        try (final Stream<String> f = Streams.flattenArrays(Streams.<String[]>fromArray(null, null))) {
            try {
                Streams.toArray(f);
                Assert.fail();
            } catch (NullPointerException ex) {
                // Expected
            }
        }
    }

    @Test
    public void testFlattenIterables() throws StreamException {
        Assert.assertArrayEquals(new String[]{"a1", "a2", "b1", "b2", "b3", "c1"}, Streams.toArray(Streams.flattenIterables(Streams.fromArray(Arrays.asList("a1", "a2"), Arrays.asList("b1", "b2", "b3"), Arrays.asList("c1")))));
        Assert.assertArrayEquals(new String[]{"d1", "d2"}, Streams.toArray(Streams.flattenIterables(Streams.fromArray(Arrays.asList("d1", "d2"), Arrays.asList()))));
        Assert.assertArrayEquals(new String[]{"e1", "e2"}, Streams.toArray(Streams.flattenIterables(Streams.fromArray(Arrays.asList(), Arrays.asList("e1", "e2")))));
        Assert.assertArrayEquals(new String[]{}, Streams.toArray(Streams.flattenIterables(Streams.fromArray(Arrays.asList(), Arrays.asList()))));

        try {
            Streams.flattenIterables(null);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }

        try (final Stream<String> f = Streams.flattenIterables(Streams.<Iterable<String>>fromArray(null, null))) {
            try {
                Streams.toArray(f);
                Assert.fail();
            } catch (NullPointerException ex) {
                // Expected
            }
        }
    }

    @Test
    public void testFlattenIterators() throws StreamException {
        Assert.assertArrayEquals(new String[]{"a1", "a2", "b1", "b2", "b3", "c1"}, Streams.toArray(Streams.flattenIterators(Streams.fromArray(Arrays.asList("a1", "a2").iterator(), Arrays.asList("b1", "b2", "b3").iterator(), Arrays.asList("c1").iterator()))));
        Assert.assertArrayEquals(new String[]{"d1", "d2"}, Streams.toArray(Streams.flattenIterators(Streams.fromArray(Arrays.asList("d1", "d2").iterator(), Arrays.asList().iterator()))));
        Assert.assertArrayEquals(new String[]{"e1", "e2"}, Streams.toArray(Streams.flattenIterators(Streams.fromArray(Arrays.asList().iterator(), Arrays.asList("e1", "e2").iterator()))));
        Assert.assertArrayEquals(new String[]{}, Streams.toArray(Streams.flattenIterators(Streams.fromArray(Arrays.asList().iterator(), Arrays.asList().iterator()))));

        try {
            Streams.flattenIterators(null);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }

        try (final Stream<String> f = Streams.flattenIterators(Streams.<Iterator<String>>fromArray(null, null))) {
            try {
                Streams.toArray(f);
                Assert.fail();
            } catch (NullPointerException ex) {
                // Expected
            }
        }
    }

    @Test
    public void testFlattenStreams() throws StreamException {
        Assert.assertArrayEquals(new String[]{"a1", "a2", "b1", "b2", "b3", "c1"}, Streams.toArray(Streams.flattenStreams(Streams.fromArray(Streams.fromArray("a1", "a2"), Streams.fromArray("b1", "b2", "b3"), Streams.fromArray("c1")))));
        Assert.assertArrayEquals(new String[]{"d1", "d2"}, Streams.toArray(Streams.flattenStreams(Streams.fromArray(Streams.fromArray("d1", "d2"), Streams.fromArray()))));
        Assert.assertArrayEquals(new String[]{"e1", "e2"}, Streams.toArray(Streams.flattenStreams(Streams.fromArray(Streams.fromArray(), Streams.fromArray("e1", "e2")))));
        Assert.assertArrayEquals(new String[]{}, Streams.toArray(Streams.flattenStreams(Streams.fromArray(Streams.fromArray(), Streams.fromArray()))));

        try {
            Streams.flattenStreams(null);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }

        try (final Stream<String> f = Streams.flattenStreams(Streams.<Stream<String>>fromArray(null, null))) {
            try {
                Streams.toArray(f);
                Assert.fail();
            } catch (NullPointerException ex) {
                // Expected
            }
        }
    }

    @Test
    public void testSingltonStream() throws StreamException {
        final String singleton = "a";
        try (final Stream<Object> stream = Streams.<Object>singleton(singleton)) {
            Assert.assertTrue(stream.hasNext());
            Assert.assertEquals(singleton, stream.next());
            Assert.assertFalse(stream.hasNext());
            try {
                stream.next();
                Assert.fail();
            } catch (NoSuchElementException ex) {
            }
        }
    }

    @Test
    public void testToArray() throws StreamException {
        final Stream<String> a = Streams.fromArray();
        Assert.assertArrayEquals(new String[]{}, Streams.toArray(a));

        final Stream<String> b = Streams.fromArray("b1", "b2", "b3");
        Assert.assertArrayEquals(new String[]{"b1", "b2", "b3"}, Streams.toArray(b));
    }

    @Test
    public void testToList() throws StreamException {
        final Stream<String> a = Streams.fromArray();
        Assert.assertEquals(new ArrayList<String>(0), Streams.toList(a));

        final Stream<String> b = Streams.fromArray("b1", "b2", "b3");
        Assert.assertEquals(Arrays.asList("b1", "b2", "b3"), Streams.toList(b));
    }

    @Test
    public void testToSet() throws StreamException {
        final Stream<String> a = Streams.fromArray();
        Assert.assertEquals(new HashSet<String>(0), Streams.toSet(a));

        final Stream<String> b = Streams.fromArray("b1", "b2", "b3");
        Assert.assertEquals(new HashSet<>(Arrays.asList("b1", "b2", "b3")), Streams.toSet(b));
    }

}
