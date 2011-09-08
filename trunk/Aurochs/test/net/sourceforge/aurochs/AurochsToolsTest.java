/*
 *  The MIT License
 * 
 *  Copyright 2010 Codist Monk.
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

import static java.util.Arrays.*;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * @author codistmonk (creation 2010-10-05)
 */
public final class AurochsToolsTest {

    @Test
    public final void testTake() {
        final List<Integer> list = new ArrayList<Integer>(Arrays.asList(42, 33));

        assertEquals((Object) 42, AurochsTools.take(list));
        assertEquals(Arrays.asList(33), list);
    }

    @Test
    public final void testGetOrCreate() {
        final Map<Object, List<Object>> map = new HashMap<Object, List<Object>>();

        AurochsTools.getOrCreate(map, 42, ArrayList.class).add(33);

        assertEquals(Arrays.asList(33), map.get(42));
    }

	@Test
	public final void testJoin() {
		assertEquals("42,33", AurochsTools.join(",", asList(42, 33)));
	}

}