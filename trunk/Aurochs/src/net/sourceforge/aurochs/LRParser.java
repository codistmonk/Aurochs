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

import java.util.Iterator;
import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs.LALR1LexerBuilder.LRLexer;

/**
 * @author codistmonk (creation 2010-10-05)
 */
public final class LRParser extends AbstractLRParser {

    private final LRLexer lexer;

    /**
     * @param table
     * <br>Not null
     * <br>Will become reference
     */
    public LRParser(final LRTable table) {
        this(table, null);
    }

    /**
     * @param table
     * <br>Not null
     * <br>Will become reference
     * @param lexer
     * <br>Maybe null
     * <br>Will become reference
     */
    public LRParser(final LRTable table, final LRLexer lexer) {
        super(table);
        this.lexer = lexer;
    }

    /**
     * @return
     * <br>Maybe null
     * <br>Reference
     */
    public final LRLexer getLexer() {
        return this.lexer;
    }

    @Override
    protected final void doParse(final Iterator<?> input) {
        if (this.getLexer() != null) {
            super.doParse(LALR1LexerBuilder.tokenize(this.getLexer(), input));
        } else {
            super.doParse(input);
        }
    }

}
