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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

public class StreamablesTests {

    @Test
    public void testConcatArrayOfStreamables() throws StreamException {
        final Streamable<String> a = Streamables.fromArray("a1");
        final Streamable<String> b = Streamables.fromArray("b1");

        Assert.assertArrayEquals(new String[]{"a1", "b1"}, Streamables.toArray(Streamables.concat(a, b)));
        Assert.assertArrayEquals(new String[]{"a1", "b1"}, Streamables.toArray(Streamables.concat(a, b)));

        final Streamable<String> c = Streamables.fromArray("c1", "c2", "c3");
        final Streamable<String> d = Streamables.fromArray("d1", "d2");
        final Streamable<String> e = Streamables.fromArray("e1", "e2", "e3", "e4");

        Assert.assertArrayEquals(new String[]{"c1", "c2", "c3", "d1", "d2", "e1", "e2", "e3", "e4"}, Streamables.toArray(Streamables.concat(c, d, e)));

        try {
            Streamables.concat((Streamable<String>[]) null);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }

        final Streamable<String> g = Streamables.concat(null, null);
        try (final Stream<? extends String> stream = g.stream()) {
            try {
                stream.hasNext();
                Assert.fail();
            } catch (NullPointerException ex) {
                // Expected
            }
        }
    }

    @Test
    public void testConcatStreamableOfStreamables() throws StreamException {
        final Streamable<String> a = Streamables.fromArray("a1");
        final Streamable<String> b = Streamables.fromArray("b1");

        Assert.assertArrayEquals(new String[]{"a1", "b1"}, Streamables.toArray(Streamables.concat(Streamables.fromArray(a, b))));
        Assert.assertArrayEquals(new String[]{"a1", "b1"}, Streamables.toArray(Streamables.concat(Streamables.fromArray(a, b))));

        final Streamable<String> c = Streamables.fromArray("c1", "c2", "c3");
        final Streamable<String> d = Streamables.fromArray("d1", "d2");
        final Streamable<String> e = Streamables.fromArray("e1", "e2", "e3", "e4");

        Assert.assertArrayEquals(new String[]{"c1", "c2", "c3", "d1", "d2", "e1", "e2", "e3", "e4"}, Streamables.toArray(Streamables.concat(Streamables.fromArray(c, d, e))));

        try {
            Streamables.concat((Streamable<Streamable<String>>) null);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }

        final Streamable<String> g = Streamables.concat(Streamables.<Streamable<String>>fromArray(null, null));
        try (final Stream<? extends String> stream = g.stream()) {
            try {
                stream.hasNext();
                Assert.fail();
            } catch (NullPointerException ex) {
                // Expected
            }
        }
    }

    @Test
    public void testEmptyStreamable() throws StreamException {
        final Streamable<String> a = Streamables.empty();
        try (final Stream<? extends String> stream = a.stream()) {
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
        Assert.assertArrayEquals(new String[]{"a1", "a2", "a3"}, Streamables.toArray(Streamables.filter(Streamables.fromArray("a1", "a2", "a3"), StreamFilters.<String>all())));
        Assert.assertArrayEquals(new String[]{}, Streamables.toArray(Streamables.filter(Streamables.fromArray("b1", "b2", "b3"), StreamFilters.<String>none())));
        Assert.assertArrayEquals(new String[]{"c2", "c3"}, Streamables.toArray(Streamables.filter(Streamables.fromArray("c1", "c2", "c3", "c4"), StreamFilters.whitelist("c2", "c3"))));

        Assert.assertArrayEquals(new String[]{}, Streamables.toArray(Streamables.filter(Streamables.<String>empty(), StreamFilters.whitelist("c2", "c3"))));

        try {
            Streamables.filter(null, StreamFilters.all());
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }
        try {
            Streamables.filter(Streamables.empty(), null);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }
        try {
            Streamables.filter(null, null);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }
    }

    @Test
    public void testFlattenArrays() throws StreamException {
        Assert.assertArrayEquals(new String[]{"a1", "a2", "b1", "b2", "b3", "c1"}, Streamables.toArray(Streamables.flattenArrays(Streamables.fromArray(new String[]{"a1", "a2"}, new String[]{"b1", "b2", "b3"}, new String[]{"c1"}))));
        Assert.assertArrayEquals(new String[]{"d1", "d2"}, Streamables.toArray(Streamables.flattenArrays(Streamables.fromArray(new String[]{"d1", "d2"}, new String[]{}))));
        Assert.assertArrayEquals(new String[]{"e1", "e2"}, Streamables.toArray(Streamables.flattenArrays(Streamables.fromArray(new String[]{}, new String[]{"e1", "e2"}))));
        Assert.assertArrayEquals(new String[]{}, Streamables.toArray(Streamables.flattenArrays(Streamables.fromArray(new String[]{}, new String[]{}))));

        try {
            Streamables.flattenArrays(null);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }

        final Streamable<String> f = Streamables.flattenArrays(Streamables.<String[]>fromArray(null, null));
        try {
            Streamables.toArray(f);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }
    }

    @Test
    public void testFlattenIterables() throws StreamException {
        Assert.assertArrayEquals(new String[]{"a1", "a2", "b1", "b2", "b3", "c1"}, Streamables.toArray(Streamables.flattenIterables(Streamables.fromArray(Arrays.asList("a1", "a2"), Arrays.asList("b1", "b2", "b3"), Arrays.asList("c1")))));
        Assert.assertArrayEquals(new String[]{"d1", "d2"}, Streamables.toArray(Streamables.flattenIterables(Streamables.fromArray(Arrays.asList("d1", "d2"), Arrays.asList()))));
        Assert.assertArrayEquals(new String[]{"e1", "e2"}, Streamables.toArray(Streamables.flattenIterables(Streamables.fromArray(Arrays.asList(), Arrays.asList("e1", "e2")))));
        Assert.assertArrayEquals(new String[]{}, Streamables.toArray(Streamables.flattenIterables(Streamables.fromArray(Arrays.asList(), Arrays.asList()))));

        try {
            Streamables.flattenIterables(null);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }

        final Streamable<String> f = Streamables.flattenIterables(Streamables.<Iterable<String>>fromArray(null, null));
        try {
            Streamables.toArray(f);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }
    }

    @Test
    public void testFlattenStreamables() throws StreamException {
        Assert.assertArrayEquals(new String[]{"a1", "a2", "b1", "b2", "b3", "c1"}, Streamables.toArray(Streamables.flattenStreamables(Streamables.fromArray(Streamables.fromArray("a1", "a2"), Streamables.fromArray("b1", "b2", "b3"), Streamables.fromArray("c1")))));
        Assert.assertArrayEquals(new String[]{"d1", "d2"}, Streamables.toArray(Streamables.flattenStreamables(Streamables.fromArray(Streamables.fromArray("d1", "d2"), Streamables.<String>fromArray()))));
        Assert.assertArrayEquals(new String[]{"e1", "e2"}, Streamables.toArray(Streamables.flattenStreamables(Streamables.fromArray(Streamables.<String>fromArray(), Streamables.fromArray("e1", "e2")))));
        Assert.assertArrayEquals(new String[]{}, Streamables.toArray(Streamables.flattenStreamables(Streamables.fromArray(Streamables.fromArray(), Streamables.fromArray()))));

        try {
            Streamables.flattenStreamables(null);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }

        final Streamable<String> f = Streamables.flattenStreamables(Streamables.<Streamable<String>>fromArray(null, null));
        try {
            Streamables.toArray(f);
            Assert.fail();
        } catch (NullPointerException ex) {
            // Expected
        }
    }

    @Test
    public void testSingltonStreamable() throws StreamException {
        final Streamable<Object> a = Streamables.<Object>singleton("a");
        try (final Stream<Object> stream = a.stream()) {
            Assert.assertTrue(stream.hasNext());
            Assert.assertEquals("a", stream.next());
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
        final Streamable<String> a = Streamables.fromArray();
        Assert.assertArrayEquals(new String[]{}, Streamables.toArray(a));

        final Streamable<String> b = Streamables.fromArray("b1", "b2", "b3");
        Assert.assertArrayEquals(new String[]{"b1", "b2", "b3"}, Streamables.toArray(b));
    }

    @Test
    public void testToList() throws StreamException {
        final Streamable<String> a = Streamables.fromArray();
        Assert.assertEquals(new ArrayList<String>(0), Streamables.toList(a));

        final Streamable<String> b = Streamables.fromArray("b1", "b2", "b3");
        Assert.assertEquals(Arrays.asList("b1", "b2", "b3"), Streamables.toList(b));
    }

    @Test
    public void testToSet() throws StreamException {
        final Streamable<String> a = Streamables.fromArray();
        Assert.assertEquals(new HashSet<String>(0), Streamables.toSet(a));

        final Streamable<String> b = Streamables.fromArray("b1", "b2", "b3");
        Assert.assertEquals(new HashSet<>(Arrays.asList("b1", "b2", "b3")), Streamables.toSet(b));
    }

}
