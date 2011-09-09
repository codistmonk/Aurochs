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

import static net.sourceforge.aprog.tools.Tools.*;
import static net.sourceforge.aurochs.LRParserTools.*;
import static net.sourceforge.aurochs.RegularTools.*;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author codistmonk (creation 2011-09-09)
 */
public final class LRParserToolsTest {

    @Test
    public final void test1() {
        assertTrue(newParser(Parser1.class).parse(LRParserTest.input("bb b")));
    }

    /**
     * @author codistmonk (creation 2011-09-09)
     */
    public static final class Parser1 {

        static final AbstractLexerRule[] lexerRules = {

            verbatimTokenRule('B', /* -> */ 'b'),

            nontokenRule(     '_', /* -> */ zeroOrMore(' ')),

        };

        static final ParserRule[] parserRules = {

            namedRule("a1",   'A', /* -> */  'A', 'b'),

            namedRule("a2",   'A', /* -> */  'b')

        };

        // <lexer-actions/>

        // <parser-actions>

        /**
         * @param values
         * <br>Not null
         * @return
         * <br>Not null
         */
        final Object a1Action(final Object[] values) {
            return values[0].toString() + values[1];
        }

        /**
         * @param values
         * <br>Not null
         * @return
         * <br>Not null
         */
        final Object a2Action(final Object[] values) {
            return values[0];
        }

        // </parser-actions>

    }

}
