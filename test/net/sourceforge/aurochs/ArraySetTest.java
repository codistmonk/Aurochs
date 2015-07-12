/*
 *  The MIT License
 * 
 *  Copyright 2011 Codist Monk.
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package net.sourceforge.aurochs;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author codistmonk (creation 2011-06-07)
 */
public final class ArraySetTest {

    @Test
    public final void test0() {
        final ArraySet<Object> set = new ArraySet<Object>();
        final String string0 = "hello";
        final String string1 = "world";
        final String string2 = "!";

        assertEquals(0, set.size());

        set.add(string0);

        assertEquals(1, set.size());
        assertTrue(set.contains(string0));

        set.add(string0);

        assertEquals(1, set.size());
        assertTrue(set.contains(string0));

        set.add(new String(string0.toCharArray()));

        assertEquals(1, set.size());
        assertTrue(set.contains(string0));

        set.add(string1);
        set.add(string2);

        assertEquals(3, set.size());
        assertTrue(set.contains(string0));
        assertTrue(set.contains(string1));
        assertTrue(set.contains(string2));
        assertSame(string0, set.find(string0));
        assertSame(string1, set.find(string1));
        assertSame(string2, set.find(string2));
        assertEquals(0, set.indexOf(string0));
        assertEquals(1, set.indexOf(string1));
        assertEquals(2, set.indexOf(string2));
        assertSame(string0, set.get(0));
        assertSame(string1, set.get(1));
        assertSame(string2, set.get(2));
        assertTrue(set.remove(string0));
        assertFalse(set.contains(string0));
        assertTrue(set.contains(string1));
        assertTrue(set.contains(string2));
        set.remove(1);
        assertTrue(set.contains(string1));
        assertFalse(set.contains(string2));
        assertEquals(1, set.size());

        set.clear();

        assertFalse(set.contains(string1));
        assertEquals(0, set.size());
    }

}