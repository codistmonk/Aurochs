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

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs.Grammar.Regular;
import net.sourceforge.aurochs.Grammar.RegularInfiniteRepetition;
import net.sourceforge.aurochs.Grammar.RegularSequence;
import net.sourceforge.aurochs.Grammar.RegularSymbol;
import net.sourceforge.aurochs.Grammar.RegularUnion;

/**
 * @author codistmonk (creation 2011-09-09)
 */
public final class RegularTools {

    /**
     * @throws IllegalInstantiationException To prevent instantiation
     */
    private RegularTools() {
        throw new IllegalInstantiationException();
    }

    /**
     * @param begin
     * <br>Range: any char
     * @param end
     * <br>Range: any char
     * @return
     * <br>Not null
     * <br>New
     */
    public static final RegularUnion range(final char begin, final char end) {
        final Regular[] regulars = new Regular[end - begin + 1];

        for (char c = begin; c <= end; ++c) {
            regulars[c - begin] = new RegularSymbol(c);
        }

        return new RegularUnion(regulars);
    }

    /**
     * @param symbolOrRegular
     * <br>Maybe null
     * <br>Will be strong reference
     * @return
     * <br>Not null
     * <br>New
     */
    public static final RegularInfiniteRepetition zeroOrMore(final Object symbolOrRegular) {
        return new RegularInfiniteRepetition(symbolOrRegular instanceof Regular ? (Regular) symbolOrRegular : new RegularSymbol(symbolOrRegular));
    }

    /**
     * @param symbolOrRegular
     * <br>Maybe null
     * <br>Will be strong reference
     * @return
     * <br>Not null
     * <br>New
     */
    public static final RegularSequence oneOrMore(final Object symbolOrRegular) {
        final Regular regular = symbolOrRegular instanceof Regular ? (Regular) symbolOrRegular : new RegularSymbol(symbolOrRegular);

        return new RegularSequence(regular, new RegularInfiniteRepetition(regular));
    }

    /**
     * @param symbolOrRegulars
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    public static final RegularUnion union(final Object... symbolOrRegulars) {
        final Regular[] regulars = new RegularSymbol[symbolOrRegulars.length];

        Tools.debugPrint(symbolOrRegulars);

        for (int i = 0; i < regulars.length; ++i) {
            regulars[i] = symbolOrRegulars[i] instanceof Regular ? (Regular) symbolOrRegulars[i] : new RegularSymbol(symbolOrRegulars[i]);
        }

        return new RegularUnion(regulars);
    }

}
