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

import java.util.logging.LogRecord;
import static net.sourceforge.aprog.tools.Tools.*;
import net.sourceforge.aurochs.AbstractLRParser.GeneratedToken;
import net.sourceforge.aurochs.Grammar.Rule;
import static net.sourceforge.aurochs.LRParserTest.Nonterminal.*;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;

import net.sourceforge.aurochs.Grammar.Regular;
import net.sourceforge.aurochs.Grammar.RegularInfiniteRepetition;
import net.sourceforge.aurochs.Grammar.RegularSequence;
import net.sourceforge.aurochs.Grammar.RegularSymbol;
import net.sourceforge.aurochs.Grammar.RegularUnion;
import net.sourceforge.aurochs.AbstractLRParser.ReductionEvent;
import net.sourceforge.aurochs.AbstractLRParser.UnexpectedSymbolErrorEvent;
import net.sourceforge.aurochs.LALR1LexerBuilder.LRLexer;
import net.sourceforge.aurochs.LRTable.BeforeOperationAddedEvent;
import net.sourceforge.aurochs.LRTable.Operation;
import org.junit.After;
import org.junit.Before;

import org.junit.Test;

/**
 * @author codistmonk (creation 2010-10-06)
 */
public final class LRParserTest {

    private LALR1LexerBuilder lexerBuilder;

    private LALR1ParserBuilder parserBuilder;

    private LRLexer lexer;

    private AbstractLRParser parser;

    @Before
    public final void beforeEachTest() {
        this.lexerBuilder = new LALR1LexerBuilder();
        this.parserBuilder = new LALR1ParserBuilder();
        this.lexer = null;
        this.parser = null;
    }

    @After
    public final void afterEachTest() {
        this.lexerBuilder = null;
        this.parserBuilder = null;
        this.lexer = null;
        this.parser = null;
    }

    @Test
    public final void testParse0() {
        final boolean debug = false;

        this.parserBuilder.addRule(INTEGER, DIGIT, INTEGER);
        this.parserBuilder.addRule(INTEGER);
        this.parserBuilder.addRule(DIGIT, '0');

        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            this.addListenerTo(this.getParser());

            int stateIndex = 0;

            for (final Map<Object, Operation> state : this.getParser().getTable().getStates()) {
                debugPrint(stateIndex++, state);
            }
        }
        // </editor-fold>

        assertTrue(this.parse(input("")));
        assertTrue(this.parse(input("0")));
        assertTrue(this.parse(input("00")));
        assertTrue(this.parse(input("000")));
        assertTrue(this.parse(input("0000")));
    }

    @Test
    public final void testParse0p5() {
        final boolean debug = false;

        this.lexerBuilder.addTokenRule(DIGIT, '0');
        this.lexerBuilder.addNontokenRule(WHITE_SPACE, ' ');
        this.lexerBuilder.addNontokenRule(WHITE_SPACE);

        this.parserBuilder.addRule(PROGRAM, DIGIT);

        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            this.addListenerTo(this.getParser());

            int stateIndex = 0;

            for (final Map<Object, Operation> state : this.getParser().getTable().getStates()) {
                debugPrint(stateIndex++, state);
            }
        }
        // </editor-fold>

        assertFalse(this.tokenizeAndParse(input("")));
        assertTrue(this.tokenizeAndParse(input("0")));
        assertTrue(this.tokenizeAndParse(input("0 ")));
        assertTrue(this.tokenizeAndParse(input(" 0")));
        assertTrue(this.tokenizeAndParse(input(" 0 ")));
    }

    @Test
    public final void testParse1() {
        final boolean debug = false;

        this.parserBuilder.addRule(INTEGER, INTEGER, DIGIT);
        this.parserBuilder.addRule(INTEGER, DIGIT);
        this.parserBuilder.addRule(DIGIT, '0');
        this.parserBuilder.addRule(DIGIT, '1');
        this.parserBuilder.addRule(DIGIT, '2');
        this.parserBuilder.addRule(DIGIT, '3');
        this.parserBuilder.addRule(DIGIT, '4');
        this.parserBuilder.addRule(DIGIT, '5');
        this.parserBuilder.addRule(DIGIT, '6');
        this.parserBuilder.addRule(DIGIT, '7');
        this.parserBuilder.addRule(DIGIT, '8');
        this.parserBuilder.addRule(DIGIT, '9');

        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            this.addListenerTo(this.getParser());

            int stateIndex = 0;

            for (final Map<Object, Operation> state : this.getParser().getTable().getStates()) {
                debugPrint(stateIndex++, state);
            }
        }
        // </editor-fold>

        assertFalse(this.parse(input("")));
        assertTrue(this.parse(input("0")));
        assertTrue(this.parse(input("01")));
        assertTrue(this.parse(input("001122")));
    }

    @Test
    public final void testParse2() {
        final boolean debug = false;

        this.lexerBuilder.addVerbatimTokenRule(A, 'a');
        this.lexerBuilder.addNontokenRule(WHITE_SPACE, WHITE_SPACE, ' ');
        this.lexerBuilder.addNontokenRule(WHITE_SPACE);

        this.parserBuilder.addRule(LIST, 'a', LIST);
        this.parserBuilder.addRule(LIST, 'a');

        this.testErrors(0);

        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            this.addListenerTo(this.getParser());

            int index = 0;

            for (final Grammar.Rule rule : this.getParser().getTable().getGrammar().getRules()) {
                debugPrint(index++, rule);
            }

            index = 0;

            for (final Map<Object, Operation> state : this.getParser().getTable().getStates()) {
                debugPrint(index++, state);
            }
        }
        // </editor-fold>

        assertFalse(this.tokenizeAndParse(input(" ")));
        assertTrue(this.tokenizeAndParse(input("a")));
        assertTrue(this.tokenizeAndParse(input(" a")));
        assertTrue(this.tokenizeAndParse(input("a ")));
        assertTrue(this.tokenizeAndParse(input("a a")));
        assertTrue(this.tokenizeAndParse(input("a a  aa    a")));
    }

    @Test
    public final void testParse2p5() {
        final boolean debug = false;

        this.lexerBuilder.addNontokenRule(WHITE_SPACE, WHITE_SPACE, ' ');
        this.lexerBuilder.addNontokenRule(WHITE_SPACE);
        this.lexerBuilder.addTokenRule(INTEGER, oneOrMore(DIGIT));
        this.lexerBuilder.addHelperRule(DIGIT, range('0', '9'));

        this.parserBuilder.addRule(LIST, INTEGER, LIST);
        this.parserBuilder.addRule(LIST, INTEGER);

        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            this.addListenerTo(this.getParser());

            int index = 0;

            for (final Grammar.Rule rule : this.getParser().getTable().getGrammar().getRules()) {
                debugPrint(index++, rule);
            }

            index = 0;

            for (final Map<Object, Operation> state : this.getParser().getTable().getStates()) {
                debugPrint(index++, state);
            }
        }
        // </editor-fold>

        assertFalse(this.tokenizeAndParse(input(" ")));
        assertTrue(this.tokenizeAndParse(input("0")));
        assertTrue(this.tokenizeAndParse(input(" 0")));
        assertTrue(this.tokenizeAndParse(input("0 ")));
        assertTrue(this.tokenizeAndParse(input("0 1")));
        assertTrue(this.tokenizeAndParse(input("0 1  42    123456")));
    }

    @Test
    public final void testParse3() {
        final boolean debug = false;

        this.parserBuilder.addLeftAssociativeBinaryOperator('+');

        this.parserBuilder.addRule(EXPRESSION, EXPRESSION, '+', EXPRESSION);
        this.parserBuilder.addRule(EXPRESSION, 'a');

        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            this.addListenerTo(this.getParser());

            int stateIndex = 0;

            for (final Map<Object, Operation> state : this.getParser().getTable().getStates()) {
                debugPrint(stateIndex++, state);
            }
        }
        // </editor-fold>

        assertFalse(this.parse(input("")));
        assertTrue(this.parse(input("a")));
        assertTrue(this.parse(input("a+a")));
        assertTrue(this.parse(input("a+a+a")));
    }

    @Test
    public final void testParse4() {
        final boolean debug = false;

        this.lexerBuilder.addVerbatimTokenRule(A, 'a');
        this.lexerBuilder.addVerbatimTokenRule(PLUS, '+');
        this.lexerBuilder.addNontokenRule(WHITE_SPACE, ' ');
        this.lexerBuilder.addNontokenRule(WHITE_SPACE);

        this.parserBuilder.addLeftAssociativeBinaryOperator('+');

        this.parserBuilder.addRule(EXPRESSION, EXPRESSION, '+', EXPRESSION);
        this.parserBuilder.addRule(EXPRESSION, 'a');

        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            this.addListenerTo(this.getParser());

            int stateIndex = 0;

            for (final Map<Object, Operation> state : this.getParser().getTable().getStates()) {
                debugPrint(stateIndex++, state);
            }
        }
        // </editor-fold>

        assertFalse(this.tokenizeAndParse(input("")));
        assertTrue(this.tokenizeAndParse(input("a")));
        assertTrue(this.tokenizeAndParse(input("a+a")));
        assertTrue(this.tokenizeAndParse(input("a+a+a")));
        assertTrue(this.tokenizeAndParse(input("a +a+ a")));
    }

    @Test
    public final void testParse6() {
        final boolean debug = false;

        this.parserBuilder.addRule(A, 'b', C, 'd');
        this.parserBuilder.addRule(A, 'b', 'd', 'e');
        this.parserBuilder.addRule(C);

        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            this.addListenerTo(this.getParser());

            int stateIndex = 0;

            for (final Map<Object, Operation> state : this.getParser().getTable().getStates()) {
                debugPrint(stateIndex++, state);
            }
        }
        // </editor-fold>

        assertTrue(this.parse(input("bd")));
        assertTrue(this.parse(input("bde")));
    }

    @Test
    public final void testParse7() {
        final boolean debug = false;

        this.lexerBuilder.addTokenRule(C, 'c');
        this.lexerBuilder.addTokenRule(E, 'e');
        this.lexerBuilder.addTokenRule(F, 'f');
        this.lexerBuilder.addNontokenRule(D, D, 'g');
        this.lexerBuilder.addNontokenRule(D);

        this.parserBuilder.addRule(A, B);
        this.parserBuilder.addRule(A, C, E);

        this.parserBuilder.addRule(B, B, F);
        this.parserBuilder.addRule(B, C);


        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            this.addListenerTo(this.getParser());

            int stateIndex = 0;

            for (final Map<Object, Operation> state : this.getParser().getTable().getStates()) {
                debugPrint(stateIndex++, state);
            }
        }
        // </editor-fold>

        assertTrue(this.tokenizeAndParse(input("ce")));
        assertTrue(this.tokenizeAndParse(input("cge")));
    }

    @Test
    public final void testParse8() {
        final boolean debug = false;

        this.lexerBuilder.addTokenRule(PLUS, '+');
        this.lexerBuilder.addTokenRule(A, 'a');
        this.lexerBuilder.addNontokenRule(S, ' ', S);
        this.lexerBuilder.addNontokenRule(S);

        this.parserBuilder.addLeftAssociativeBinaryOperator(PLUS);

        this.parserBuilder.addRule(E, E, PLUS, E);
        this.parserBuilder.addRule(E, A);

        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            this.addListenerTo(this.getParser());

            int stateIndex = 0;

            for (final Map<Object, Operation> state : this.getParser().getTable().getStates()) {
                debugPrint(stateIndex++, state);
            }
        }
        // </editor-fold>

        assertTrue(this.tokenizeAndParse(input("a + a")));
        assertTrue(this.tokenizeAndParse(input("a + a + a")));
    }

    @Test
    public final void testParse9() {
        final boolean debug = false;

        this.lexerBuilder.addVerbatimTokenRule(A, 'a');
        this.lexerBuilder.addVerbatimTokenRule(PLUS, '+');
        this.lexerBuilder.addNontokenRule(_, S);
        this.lexerBuilder.addNontokenRule(S, ' ', S);
        this.lexerBuilder.addNontokenRule(S);

        this.parserBuilder.addLeftAssociativeBinaryOperator('+');

        this.parserBuilder.addRule(E, E, '+', E);
        this.parserBuilder.addRule(E, 'a');

        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            this.addListenerTo(this.getParser());

            int stateIndex = 0;

            for (final Map<Object, Operation> state : this.getParser().getTable().getStates()) {
                debugPrint(stateIndex++, state);
            }
        }
        // </editor-fold>

        assertTrue(this.tokenizeAndParse(input("a + a")));
    }

    @Test
    public final void testParse10() {
        final boolean debug = false;

        this.lexerBuilder.addTokenRule(A, 'a');
        this.lexerBuilder.addTokenRule(ZERO, '0');
        this.lexerBuilder.addTokenRule(EQUAL, '=');
        this.lexerBuilder.addTokenRule(PLUS, '+');
        this.lexerBuilder.addNontokenRule(_, S);
        this.lexerBuilder.addNontokenRule(S, ' ', S);
        this.lexerBuilder.addNontokenRule(S);

        this.parserBuilder.addLeftAssociativeBinaryOperator(PLUS);

        this.parserBuilder.addRule(I, E);
        this.parserBuilder.addRule(I, A, EQUAL, E);
        this.parserBuilder.addRule(E, E, PLUS, E);
        this.parserBuilder.addRule(E, A);
        this.parserBuilder.addRule(E, ZERO);

        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            this.addListenerTo(this.getParser());

            int stateIndex = 0;

            for (final Map<Object, Operation> state : this.getParser().getTable().getStates()) {
                debugPrint(stateIndex++, state);
            }
        }
        // </editor-fold>

        assertTrue(this.tokenizeAndParse(input("a = 0")));
        assertTrue(this.tokenizeAndParse(input("a = 0+a")));
    }

    @Test
    public final void testParse11() {
        final boolean debug = false;

        this.parserBuilder.addLeftAssociativeBinaryOperator('+');
        this.parserBuilder.addLeftAssociativeBinaryOperator('*');
        this.parserBuilder.setPriority((Character) '+', (short) 100);
        this.parserBuilder.setPriority((Character) '*', (short) 200);

        this.parserBuilder.addRule(EXPRESSION, EXPRESSION, '+', EXPRESSION);
        this.parserBuilder.addRule(EXPRESSION, EXPRESSION, '*', EXPRESSION);
        this.parserBuilder.addRule(EXPRESSION, '1');
        this.parserBuilder.addRule(EXPRESSION, '2');

        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            this.addListenerTo(this.getParser());

            int stateIndex = 0;

            for (final Map<Object, Operation> state : this.getParser().getTable().getStates()) {
                debugPrint(stateIndex++, state);
            }
        }
        // </editor-fold>

        assertTrue(this.parse(input("1")));
        assertTrue(this.parse(input("2+1")));
        assertTrue(this.parse(input("2*1")));
        assertTrue(this.parse(input("2+1+2")));
        assertTrue(this.parse(input("1*2*1")));
        assertTrue(this.parse(input("2+1*2")));
        assertTrue(this.parse(input("1*2+1")));

        final SimpleCalculator calculator = new SimpleCalculator();

        this.getParser().addListener(calculator);

        this.parse(input("2+2*1"));

        assertEquals((Integer) 4, calculator.getValueStack().remove(0));

        this.parse(input("2*2+1"));

        assertEquals((Integer) 5, calculator.getValueStack().remove(0));
    }

    @Test(timeout=20000)
    public final void testParse12() {
        final boolean debug = false;

        this.lexerBuilder.addTokenRule(INTEGER, oneOrMore(DIGIT));
        this.lexerBuilder.addTokenRule(IDENTIFIER, oneOrMore(LETTER));
        this.lexerBuilder.addTokenRule(EQUAL, '=');
        this.lexerBuilder.addTokenRule(PLUS, '+');
        this.lexerBuilder.addTokenRule(MINUS, '-');
        this.lexerBuilder.addTokenRule(TIMES, '*');
        this.lexerBuilder.addTokenRule(DIVIDED, '/');
        this.lexerBuilder.addTokenRule(MODULO, '/');
        this.lexerBuilder.addTokenRule(POWER, '^');
        this.lexerBuilder.addTokenRule(LEFT_PARENTHESIS, '(');
        this.lexerBuilder.addTokenRule(RIGHT_PARENTHESIS, ')');
        this.lexerBuilder.addNontokenRule(WHITE_SPACE, zeroOrMore(' '));
        this.lexerBuilder.addHelperRule(DIGIT, range('0', '9'));
        this.lexerBuilder.addHelperRule(LETTER, range('a', 'z'));

        this.parserBuilder.addLeftAssociativeBinaryOperator(PLUS);
        this.parserBuilder.addLeftAssociativeBinaryOperator(MINUS);
        this.parserBuilder.addLeftAssociativeBinaryOperator(TIMES);
        this.parserBuilder.addLeftAssociativeBinaryOperator(DIVIDED);
        this.parserBuilder.addLeftAssociativeBinaryOperator(MODULO);
        this.parserBuilder.addLeftAssociativeBinaryOperator(POWER);
        this.parserBuilder.setPriority(PLUS, (short) 100);
        this.parserBuilder.setPriority(MINUS, (short) 100);
        this.parserBuilder.setPriority(TIMES, (short) 200);
        this.parserBuilder.setPriority(DIVIDED, (short) 200);
        this.parserBuilder.setPriority(MODULO, (short) 200);
        this.parserBuilder.setPriority(POWER, (short) 300);

        this.parserBuilder.addRule(INSTRUCTION, EXPRESSION);
        this.parserBuilder.addRule(INSTRUCTION, IDENTIFIER, EQUAL, EXPRESSION);
        this.parserBuilder.addRule(EXPRESSION, LEFT_PARENTHESIS, EXPRESSION, RIGHT_PARENTHESIS);
        this.parserBuilder.addRule(EXPRESSION, EXPRESSION, PLUS, EXPRESSION);
        this.parserBuilder.addRule(EXPRESSION, EXPRESSION, MINUS, EXPRESSION);
        this.parserBuilder.addRule(EXPRESSION, EXPRESSION, TIMES, EXPRESSION);
        this.parserBuilder.addRule(EXPRESSION, EXPRESSION, DIVIDED, EXPRESSION);
        this.parserBuilder.addRule(EXPRESSION, EXPRESSION, MODULO, EXPRESSION);
        this.parserBuilder.addRule(EXPRESSION, EXPRESSION, POWER, EXPRESSION);
        this.parserBuilder.addRule(EXPRESSION, IDENTIFIER);
        this.parserBuilder.addRule(EXPRESSION, INTEGER);

        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            this.addListenerTo(this.getParser());

            int stateIndex = 0;

            for (final Map<Object, Operation> state : this.getParser().getTable().getStates()) {
                debugPrint(stateIndex++, state);
            }
        }
        // </editor-fold>

        assertFalse(this.tokenizeAndParse(input(" ")));
        assertTrue(this.tokenizeAndParse(input("0")));
        assertTrue(this.tokenizeAndParse(input(" 0")));
        assertTrue(this.tokenizeAndParse(input("0 ")));
        assertTrue(this.tokenizeAndParse(input(" 0 ")));
        assertFalse(this.tokenizeAndParse(input("0 1")));
        assertTrue(this.tokenizeAndParse(input("0+1")));
        assertTrue(this.tokenizeAndParse(input("a = 42")));
        assertTrue(this.tokenizeAndParse(input("2 * (3 - 4)")));
        assertFalse(this.tokenizeAndParse(input("2 * (3 - 4")));
    }

    @Test
    public final void testActions0() {
        final int[] reductionCount = new int[1];

        this.parserBuilder.addRule('A', 'b').getActions().add(new Grammar.Action() {

            @Override
            public final void perform(final Rule rule, final GeneratedToken generatedToken, final List<Object> developmentTokens) {
                assertEquals(Arrays.asList('b'), developmentTokens);

                ++reductionCount[0];
            }

        });

        assertTrue(this.parse(input("b")));
        assertEquals(1, reductionCount[0]);
    }

    @Test
    public final void testActions1() {
        final int[] reductionCount = new int[1];

        this.parserBuilder.addRule('A', 'b', 'C').getActions().add(new Grammar.Action() {

            @Override
            public final void perform(final Rule rule, final GeneratedToken generatedToken, final List<Object> developmentTokens) {
                final boolean debug = false;

                // <editor-fold defaultstate="collapsed" desc="DEBUG">
                if (debug) {
                    debugPrint("Derivation found:", generatedToken, "->", developmentTokens);
                }
                // </editor-fold>

                assertEquals(Arrays.asList('b', null), developmentTokens);

                ++reductionCount[0];
            }

        });
        this.parserBuilder.addRule('C');

        assertTrue(this.parse(input("b")));
        assertEquals(1, reductionCount[0]);
    }

    @Test
    public final void testActions2() {
        final boolean debug = false;
        final int[] integerDerivationCount = new int[1];
        final Object[] result = new Integer[1];

        this.lexerBuilder.addTokenRule(INTEGER, range('0', '9')).getActions().add(new Grammar.Action() {

            @Override
            public final void perform(final Rule rule, final GeneratedToken generatedToken, final List<Object> developmentTokens) {
                // <editor-fold defaultstate="collapsed" desc="DEBUG">
                if (debug) {
                    debugPrint("Derivation found:", generatedToken, "->", developmentTokens);
                }
                // </editor-fold>

                ++integerDerivationCount[0];
            }

        });
        this.lexerBuilder.addVerbatimTokenRule(PLUS, '+');
        this.lexerBuilder.addVerbatimTokenRule(TIMES, '*');
        this.lexerBuilder.addVerbatimTokenRule(LEFT_PARENTHESIS, '(');
        this.lexerBuilder.addVerbatimTokenRule(RIGHT_PARENTHESIS, ')');
        this.lexerBuilder.addNontokenRule(_, zeroOrMore(' '));

        this.parserBuilder.addRule(PROGRAM, 'E').getActions().add(new Grammar.Action() {

            @Override
            public final void perform(final Rule rule, final GeneratedToken generatedToken, final List<Object> developmentTokens) {
                result[0] = ((GeneratedToken) developmentTokens.get(0)).getUserObject();
            }

        });
        this.parserBuilder.addRule('E', 'E', '+', 'E').getActions().add(new Grammar.Action() {

            @Override
            public final void perform(final Rule rule, final GeneratedToken generatedToken, final List<Object> developmentTokens) {
                // <editor-fold defaultstate="collapsed" desc="DEBUG">
                if (debug) {
                    debugPrint("Derivation found:", generatedToken, "->", developmentTokens);
                }
                // </editor-fold>

                generatedToken.setUserObject((Integer) ((GeneratedToken) developmentTokens.get(0)).getUserObject() + (Integer) ((GeneratedToken) developmentTokens.get(2)).getUserObject());
            }

        });
        this.parserBuilder.addRule('E', 'E', '*', 'E').getActions().add(new Grammar.Action() {

            @Override
            public final void perform(final Rule rule, final GeneratedToken generatedToken, final List<Object> developmentTokens) {
                // <editor-fold defaultstate="collapsed" desc="DEBUG">
                if (debug) {
                    debugPrint("Derivation found:", generatedToken, "->", developmentTokens);
                }
                // </editor-fold>

                generatedToken.setUserObject((Integer) ((GeneratedToken) developmentTokens.get(0)).getUserObject() * (Integer) ((GeneratedToken) developmentTokens.get(2)).getUserObject());
            }

        });
        this.parserBuilder.addRule('E', '(', 'E', ')').getActions().add(new Grammar.Action() {

            @Override
            public final void perform(final Rule rule, final GeneratedToken generatedToken, final List<Object> developmentTokens) {
                // <editor-fold defaultstate="collapsed" desc="DEBUG">
                if (debug) {
                    debugPrint("Derivation found:", generatedToken, "->", developmentTokens);
                }
                // </editor-fold>

                generatedToken.setUserObject(((GeneratedToken) developmentTokens.get(1)).getUserObject());
            }

        });
        this.parserBuilder.addRule('E', INTEGER).getActions().add(new Grammar.Action() {

            @Override
            public final void perform(final Rule rule, final GeneratedToken generatedToken, final List<Object> developmentTokens) {
                // <editor-fold defaultstate="collapsed" desc="DEBUG">
                if (debug) {
                    debugPrint("Derivation found:", generatedToken, "->", developmentTokens);
                }
                // </editor-fold>

                generatedToken.setUserObject(Integer.parseInt(((GeneratedToken) developmentTokens.get(0)).getUserObject().toString()));
            }

        });

        this.parserBuilder.addLeftAssociativeBinaryOperator('+');
        this.parserBuilder.addLeftAssociativeBinaryOperator('*');
        this.parserBuilder.setPriority('+', (short) 100);
        this.parserBuilder.setPriority('*', (short) 200);

        assertTrue(this.tokenizeAndParse(input("1 + (2 + 3) * 4")));
        assertEquals(21, result[0]);
        assertEquals(4, integerDerivationCount[0]);
    }

    @Test(expected=Grammar.AmbiguousGrammarException.class)
    public final void testError0() {
        this.parserBuilder.addRule('A');
        this.parserBuilder.addRule('A', 'B');
        this.parserBuilder.addRule('B');

        this.testAmbiguousParserGrammar();
    }

    @Test(expected=Grammar.AmbiguousGrammarException.class)
    public final void testError1() {
        this.parserBuilder.addRule('A', 'b');
        this.parserBuilder.addRule('A', 'C');
        this.parserBuilder.addRule('C', 'b');

        this.testAmbiguousParserGrammar();
    }

    /**
     * @throws Grammar.AmbiguousGrammarException If the grammar handled through this.parserBuilder is ambiguous
     */
    private final void testAmbiguousParserGrammar() {
        try {
            this.addAmbiguousGrammarExceptionThrowerToGrammarLogger();

            this.parserBuilder.newTable();
        } finally {
            this.removeAmbiguousGrammarExceptionThrowerFromGrammarLogger();
        }
    }

    /**
     * @return
     * <br>Not null
     * <br>Reference
     * <br>Maybe new
     */
    private final LRLexer getLexer() {
        if (this.lexer == null) {
            this.lexer = this.lexerBuilder.newLexer();
        }

        return this.lexer;
    }

    /**
     * @return
     * <br>Not null
     * <br>Reference
     * <br>Maybe new
     */
    private final AbstractLRParser getParser() {
        if (this.parser == null) {
            this.parser = this.parserBuilder.newParser();
        }

        return this.parser;
    }

    /**
     * @param input
     * <br>Not null
     * <br>Input-output
     * <br>Will become reference
     * @return
     * <br>Range: any boolean
     */
    private final boolean parse(final Iterator<?> input) {
        return this.getParser().parse(input);
    }

    /**
     * @param input
     * <br>Not null
     * <br>Will become reference
     * @return
     * <br>Not null
     * <br>New
     */
    private final Iterator<Object> tokenize(final Iterator<?> input) {
        return LALR1LexerBuilder.tokenize(this.getLexer(), input);
    }

    /**
     * @param input
     * <br>Not null
     * <br>Input-output
     * <br>Will become reference
     * @return
     * <br>Range: any boolean
     */
    private final boolean tokenizeAndParse(final Iterator<?> input) {
        return this.getParser().parse(this.tokenize(input));
    }

    // TODO testActionsXXX()

    /**
     * @param this.parserBuilder
     * <br>Not null
     * <br>Input-output
     * @param expectedErrorCount
     * <br>Range: <code>[0 .. Integer.MAX_VALUE]</code>
     */
    private final void testErrors(final int expectedErrorCount) {
        final int[] errorCount = { 0 };

        this.parserBuilder.addListener(new LRTable.Listener() {

            @Override
            public final void beforeOperationAdded(final BeforeOperationAddedEvent event) {
                if (event.getResolution() == null) {
                    debugPrint("Unresolved conflict " + event.getOldOperation() + "/" + event.getNewOperation() + " in state " + event.getStateIndex() + " for symbol " + event.getSymbol(),
                            "\n Examples of paths leading to conflict: " + event.getPathsToState());

                    ++errorCount[0];
                } else if (event.getOldOperation() != null) {
                    debugPrint("Resolved conflict " + event.getOldOperation() + "/" + event.getNewOperation() + " in state " + event.getStateIndex() + " for symbol " + event.getSymbol(),
                            "\n Examples of paths leading to conflict: " + event.getPathsToState());
                }
            }

        });

        this.parserBuilder.newTable();

        assertEquals(expectedErrorCount, errorCount[0]);
    }

    /**
     *
     * @param parser
     * <br>Not null
     * <br>Input-output
     */
    private final void addListenerTo(final AbstractLRParser parser) {
        parser.addListener(new LRParser.Listener() {

            @Override
            public final void reductionOccured(final ReductionEvent event) {
                event.getGeneratedToken().setUserObject(event.getTokens());

                debugPrint(event.getReduction(), event.getGeneratedToken());
            }

            @Override
            public final void unexpectedSymbolErrorOccured(final UnexpectedSymbolErrorEvent event) {
                debugPrint("Unexpected", event.getSource().getInputSymbol());
            }

        });
    }

    private final void addAmbiguousGrammarExceptionThrowerToGrammarLogger() {
        Logger.getLogger(Grammar.class.getName()).addHandler(AMBIGUOUS_GRAMMAR_EXCEPTION_THROWER);
    }

    private final void removeAmbiguousGrammarExceptionThrowerFromGrammarLogger() {
        Logger.getLogger(Grammar.class.getName()).removeHandler(AMBIGUOUS_GRAMMAR_EXCEPTION_THROWER);
    }

    private static final Handler AMBIGUOUS_GRAMMAR_EXCEPTION_THROWER = new Handler() {

        @Override
        public final void publish(final LogRecord record) {
            if (record.getMessage().contains("Grammar is ambiguous")) {
                throw new Grammar.AmbiguousGrammarException(record.getMessage());
            }
        }

        @Override
        public final void flush() {
            // Deliberately left empty
        }

        @Override
        public final void close() throws SecurityException {
            // Deliberately left empty
        }

    };

    /**
     *
     * @param string
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    public static final Iterator<Character> input(final String string) {
        return toList(string).iterator();
    }

    /**
     *
     * @param string
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    public static final List<Character> toList(final String string) {
        final List<Character> result = new ArrayList<Character>();

        for (int i = 0; i < string.length(); ++i) {
            result.add(string.charAt(i));
        }

        return result;
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
     * @param symbol
     * <br>Maybe null
     * <br>Will be strong reference
     * @return
     * <br>Not null
     * <br>New
     */
    public static final RegularInfiniteRepetition zeroOrMore(final Object symbol) {
        return new RegularInfiniteRepetition(new RegularSymbol(symbol));
    }

    /**
     * @param symbol
     * <br>Maybe null
     * <br>Will be strong reference
     * @return
     * <br>Not null
     * <br>New
     */
    public static final RegularSequence oneOrMore(final Object symbol) {
        final RegularSymbol regular = new RegularSymbol(symbol);

        return new RegularSequence(regular, new RegularInfiniteRepetition(regular));
    }

    /**
     * @param symbols
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    public static final RegularUnion union(final Object... symbols) {
        final RegularSymbol[] regulars = new RegularSymbol[symbols.length];

        for (int i = 0; i < regulars.length; ++i) {
            regulars[i] = new RegularSymbol(symbols[i]);
        }

        return new RegularUnion(regulars);
    }

    /**
     * @author codistmonk (creation 2010-10-06)
     */
    public static enum Nonterminal {

        WHITE_SPACE, INTEGER, DIGIT, LIST, INSTRUCTION, IDENTIFIER, OPERATOR, EXPRESSION, LETTER, PROGRAM,
        A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, _,
        ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,
        PLUS, MINUS, TIMES, DIVIDED, MODULO, POWER,
        LEFT_PARENTHESIS, RIGHT_PARENTHESIS,
        EQUAL, LESS, LESS_OR_EQUAL, GREATER, GREATER_OR_EQUAL;

    }

    /**
     * A simple listener to do basic calculus with one-digit integers.
     *
     * @author codistmonk (creation 2011-05-31)
     */
    private static final class SimpleCalculator implements LRParser.Listener {

        private final List<Integer> valueStack;

        private final List<Character> operatorStack;

        SimpleCalculator() {
            this.valueStack = new LinkedList<Integer>();
            this.operatorStack = new LinkedList<Character>();
        }

        /**
         * @return
         * <br>Not null
         * <br>Strong reference
         */
        public final List<Character> getOperatorStack() {
            return this.operatorStack;
        }

        public final List<Integer> getValueStack() {
            return this.valueStack;
        }

        @Override
        public final void reductionOccured(final ReductionEvent event) {
            switch (event.getReduction().getRuleIndex()) {
                case 1: // E + E
                    this.getValueStack().add(0, this.getValueStack().remove(0) + this.getValueStack().remove(0));
                    break;
                case 2: // E * E
                    this.getValueStack().add(0, this.getValueStack().remove(0) * this.getValueStack().remove(0));
                    break;
                default:
                    if (event.getTokens().size() == 1) {
                        this.maybeProcessToken(event.getTokens().get(0));
                    }
                    break;
            }
        }

        @Override
        public final void unexpectedSymbolErrorOccured(final UnexpectedSymbolErrorEvent event) {
            ignore(event);
        }

        /**
         * @param token
         * <br>Maybe null
         * <br>May become strong reference
         */
        private final void maybeProcessToken(final Object token) {
            if (token instanceof Character) {
                switch ((Character) token) {
                    case '+':
                    case '*':
                        this.getOperatorStack().add(0, (Character) token);
                        break;
                    case '1':
                    case '2':
                        this.getValueStack().add(0, (Character) token - '0');
                        break;
                }
            }
        }

    }

}