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

import static org.junit.Assert.*;

import java.util.Iterator;

import net.sourceforge.aprog.tools.AbstractIterator;
import net.sourceforge.aurochs.AnnotatedParserTools.*;

import org.junit.Test;

/**
 * @author codistmonk (creation 2011-09-09)
 */
public final class AnnotatedParserToolsTest {

    @Test
    public final void test1() {
        assertTrue(AnnotatedParserTools.newParser(Parser1.class).parse(input("bbb")));
    }

    @Test
    public final void test2() {
        assertTrue(AnnotatedParserTools.newParser(Parser2.class).parse(LALR1LexerBuilder.tokenize(AnnotatedParserTools.newLexer(Parser2.class), input("bb b"))));
    }

    /**
     * @param string
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    public static final Iterator<Object> input(final CharSequence string) {
        return new AbstractIterator<Object>(null) {

            private int index = 0;

            @Override
            protected final boolean updateNextElement() {
                this.setNextElement((int) string.charAt(this.index++));

                return this.index < string.length();
            }

            @Override
            public final void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    /**
     * @author codistmonk (creation 2011-09-09)
     */
    public static final class Parser1 {

        @ParserRule({ 'A', /* -> */ 'A', 'b' })
        Object action1(final Object[] tokens) {
            return tokens[0].toString() + tokens[1];
        }

        @ParserRule({ 'A', /* -> */ 'b' })
        Object action2(final Object[] tokens) {
            return tokens[0];
        }

    }

    /**
     * @author codistmonk (creation 2011-09-09)
     */
    public static final class Parser2 {

        @LexerVerbatimTokenRule({ 'B', 'b' })
        void lexerAction1() {
            // Deliberately left empty
        }

        @LexerNontokenRule({ '_', '_', ' ' })
        void lexerAction2() {
            // Deliberately left empty
        }

        @LexerNontokenRule({ '_', ' ' })
        void lexerAction3() {
            // Deliberately left empty
        }

        @ParserRule({ 'A', /* -> */ 'A', 'b' })
        Object action1(final Object[] tokens) {
            return tokens[0].toString() + tokens[1];
        }

        @ParserRule({ 'A', /* -> */ 'b' })
        Object action2(final Object[] tokens) {
            return tokens[0];
        }

    }

}
