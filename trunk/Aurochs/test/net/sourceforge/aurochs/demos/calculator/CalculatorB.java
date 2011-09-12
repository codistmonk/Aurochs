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
import static net.sourceforge.aurochs.RegularTools.*;

import java.util.Scanner;

import net.sourceforge.aurochs.InlineParserBuilder;
import net.sourceforge.aurochs.LRParser;

/**
 * @author codistmonk (creation 2011-09-12)
 */
public final class CalculatorB extends InlineParserBuilder {

    public CalculatorB() {
        super(true);
    }

    @Override
    public final void parser() {
        // <lexer-rules>

        if (this.rules()) {
            this.addVerbatimTokenRule("QUIT",    /* -> */ 'q', 'u', 'i', 't');
            this.addVerbatimTokenRule("+",       /* -> */ '+');
            this.addVerbatimTokenRule("*",       /* -> */ '*');
            this.addVerbatimTokenRule("(",       /* -> */ '(');
            this.addVerbatimTokenRule(")",       /* -> */ ')');
            this.addTokenRule(        "INTEGER", /* -> */ oneOrMore(range('0', '9')));
            this.addNontokenRule(     "_",       /* -> */ zeroOrMore(' '));
        }

        // </lexer-rules>

        // <parser-rules>

        if (this.rules()) {
            this.addLeftAssociativeBinaryOperator('+', 100);
            this.addLeftAssociativeBinaryOperator('*', 200);

            this.addRule("COMMAND",    /* -> */  "quit");
        } else if (this.action()) {
            System.exit(0);
        }

        if (this.rules()) {
            this.addRule("COMMAND",    /* -> */  "EXPRESSION");
        } else if (this.action()) {
            System.out.println(this.value(0));

            this.setValue(this.value(0));
        }

        if (this.rules()) {
            this.addRule("EXPRESSION", /* -> */  "EXPRESSION", '+', "EXPRESSION");
        } else if (this.action()) {
            this.setValue((Integer) this.value(0) + (Integer) this.value(2));
        }

        if (this.rules()) {
            this.addRule("EXPRESSION", /* -> */  "EXPRESSION", '*', "EXPRESSION");
        } else if (this.action()) {
            this.setValue((Integer) this.value(0) * (Integer) this.value(2));
        }

        if (this.rules()) {
            this.addRule("EXPRESSION", /* -> */  '(', "EXPRESSION", ')');
        } else if (this.action()) {
            this.setValue(this.value(1));
        }

        if (this.rules()) {
            this.addRule("EXPRESSION", /* -> */  "INTEGER");
        } else if (this.action()) {
            this.setValue(Integer.parseInt(this.value(0).toString()));
        }

        // </parser-rules>
    }

    /**
     * @param commandLineArguments
     * <br>Unused
     */
    public static final void main(final String[] commandLineArguments) {
        final LRParser parser = new CalculatorB().newParser();
        final Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()) {
            if (!parser.parse(input(scanner.next()))) {
                System.err.println("Syntax error");
            }
        }
    }

}
