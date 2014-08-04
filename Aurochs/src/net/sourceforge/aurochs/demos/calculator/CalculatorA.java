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

package net.sourceforge.aurochs.demos.calculator;

import static net.sourceforge.aurochs.AurochsTools.*;
import static net.sourceforge.aurochs.LRParserTools.*;
import static net.sourceforge.aurochs.RegularTools.*;

import java.util.Scanner;

import net.sourceforge.aurochs.LRParser;
import net.sourceforge.aurochs.LRParserTools;

/**
 * @author codistmonk (creation 2011-09-11)
 */
public final class CalculatorA {

    static final LexerRule[] lexerRules = {

        verbatimTokenRule("QUIT",    /* -> */ 'q', 'u', 'i', 't'),

        verbatimTokenRule("+",       /* -> */ '+'),

        verbatimTokenRule("*",       /* -> */ '*'),

        verbatimTokenRule("(",       /* -> */ '('),

        verbatimTokenRule(")",       /* -> */ ')'),

        tokenRule(        "INTEGER", /* -> */ oneOrMore(range('0', '9'))),

        nontokenRule(     '_',       /* -> */ zeroOrMore(' ')),

    };

    static final ParserRule[] parserRules = {

        leftAssociative('+', 100),

        leftAssociative('*', 200),

        namedRule("quitCommand",              "COMMAND",    /* -> */  "quit"),

        namedRule("expressionCommand",        "COMMAND",    /* -> */  "EXPRESSION"),

        namedRule("additionExpression",       "EXPRESSION", /* -> */  "EXPRESSION", '+', "EXPRESSION"),

        namedRule("multiplicationExpression", "EXPRESSION", /* -> */  "EXPRESSION", '*', "EXPRESSION"),

        namedRule("parenthesizedExpression",  "EXPRESSION", /* -> */  '(', "EXPRESSION", ')'),

        namedRule("integerExpression",        "EXPRESSION", /* -> */  "INTEGER"),

    };

    // <lexer-actions/>

    // <parser-actions>

    /**
     * @param values
     * <br>Not null
     * @return
     * <br>Not null
     */
    final void quitCommand() {
        System.exit(0);
    }

    /**
     * @param values
     * <br>Not null
     * @return
     * <br>Not null
     */
    final Object expressionCommand(final Object[] values) {
        System.out.println(values[0]);

        return values[0];
    }

    /**
     * @param values
     * <br>Not null
     * @return
     * <br>Not null
     */
    final Object additionExpression(final Object[] values) {
        return (Integer) values[0] + (Integer) values[2];
    }

    /**
     * @param values
     * <br>Not null
     * @return
     * <br>Not null
     */
    final Object multiplicationExpression(final Object[] values) {
        return (Integer) values[0] * (Integer) values[2];
    }

    /**
     * @param values
     * <br>Not null
     * @return
     * <br>Not null
     */
    final Object parenthesizedExpression(final Object[] values) {
        return values[1];
    }

    /**
     * @param values
     * <br>Not null
     * @return
     * <br>Not null
     */
    final Object integerExpression(final Object[] values) {
        return Integer.parseInt(values[0].toString());
    }

    // </parser-actions>

    /**
     * @param commandLineArguments
     * <br>Unused
     */
    public static final void main(final String[] commandLineArguments) {
        final LRParser parser = LRParserTools.newParser(CalculatorA.class);

        try (final Scanner scanner = new Scanner(System.in)) {
        	while (scanner.hasNext()) {
        		if (!parser.parse(input(scanner.next()))) {
        			System.err.println("Syntax error");
        		}
        	}
        }
    }

}
