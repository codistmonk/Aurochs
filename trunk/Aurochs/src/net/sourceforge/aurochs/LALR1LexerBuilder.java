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

import net.sourceforge.aurochs.Grammar.Regular;
import static net.sourceforge.aprog.tools.Tools.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.aurochs.LRParser.ReductionEvent;
import net.sourceforge.aurochs.LRParser.UnexpectedSymbolErrorEvent;

/**
 * @author codistmonk (creation 2011-09-06)
 */
public final class LALR1LexerBuilder {

    private final LALR1ParserBuilder lexerTableBuilder;

    private final Set<Object> tokens;

    private final Set<Object> nontokens;

    public LALR1LexerBuilder() {
        this.lexerTableBuilder = new LALR1ParserBuilder();
        this.tokens = new HashSet<Object>();
        this.nontokens = new HashSet<Object>();

        this.lexerTableBuilder.addProduction(Special.GENERATED_SYMBOLS, Special.GENERATED_SYMBOLS, Special.TOKEN);
        this.lexerTableBuilder.addProduction(Special.GENERATED_SYMBOLS, Special.GENERATED_SYMBOLS, Special.NONTOKEN);
        this.lexerTableBuilder.addProduction(Special.GENERATED_SYMBOLS);
    }

    /**
     * @return
     * <br>Not null
     * <br>New
     */
    public final LRTable newTable() {
        for (final Object terminal : this.lexerTableBuilder.getGrammar().getTerminals()) {
            this.lexerTableBuilder.getGrammar().getLeftAssociativeBinaryOperators().add(terminal);
            this.lexerTableBuilder.getGrammar().getPriorities().put(terminal, (short) 0);
        }

        return this.lexerTableBuilder.newTable();
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param development
     * <br>Not null
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    public final void addNontokenProduction(final Object nonterminal, final Object... development) {
        this.addNontoken(nonterminal);
        this.addHelperProduction(nonterminal, development);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param development
     * <br>Not null
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    public final void addTokenProduction(final Object nonterminal, final Object... development) {
        this.addToken(nonterminal);
        this.addHelperProduction(nonterminal, development);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param development
     * <br>Not null
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    public final void addHelperProduction(final Object nonterminal, final Object... development) {
        this.lexerTableBuilder.addProduction(nonterminal, development);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param regularDevelopment
     * <br>Not null
     */
    public final void addTokenProduction(final Object nonterminal, final Regular regularDevelopment) {
        this.addToken(nonterminal);
        this.addHelperProduction(nonterminal, regularDevelopment);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param regularDevelopment
     * <br>Not null
     */
    public final void addNontokenProduction(final Object nonterminal, final Regular regularDevelopment) {
        this.addNontoken(nonterminal);
        this.addHelperProduction(nonterminal, regularDevelopment);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param regularDevelopment
     * <br>Not null
     */
    public final void addHelperProduction(final Object nonterminal, final Regular regularDevelopment) {
        this.lexerTableBuilder.addProduction(nonterminal, regularDevelopment);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    private final void addToken(final Object nonterminal) {
        if (!this.tokens.contains(nonterminal)) {
            this.lexerTableBuilder.addProduction(Special.TOKEN, nonterminal);
            this.tokens.add(nonterminal);
        }
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    private final void addNontoken(final Object nonterminal) {
        if (!this.nontokens.contains(nonterminal)) {
            this.lexerTableBuilder.addProduction(Special.NONTOKEN, nonterminal);
            this.nontokens.add(nonterminal);
        }
    }

    /**
     * @return
     * <br>Not null
     * <br>New
     */
    public final LRParser newLexer() {
        return new LRParser(this.newTable());
    }

    /**
     * @param input
     * <br>Not null
     * <br>Will become reference
     * @return
     * <br>Not null
     * <br>New
     */
    public final Iterator<Object> tokenize(final Iterator<?> input) {
        return tokenize(this.newLexer(), input);
    }

    /**
     * @param lexer
     * <br>Not null
     * <br>Will become reference
     * @param input
     * <br>Not null
     * <br>Will become reference
     * @return
     * <br>Not null
     * <br>New
     */
    public static final Iterator<Object> tokenize(final LRParser lexer, final Iterator<?> input) {
        final Object[] token = new Object[1];

        lexer.addListener(new LRParser.Listener() {

            @Override
            public final void reductionOccured(final ReductionEvent event) {
                final boolean debug = false;

                // <editor-fold defaultstate="collapsed" desc="DEBUG">
                if (debug) {
                    debugPrint("generatedToken:", event.getGeneratedToken(), "tokens:", event.getTokens());
                }
                // </editor-fold>

                if (LALR1LexerBuilder.Special.TOKEN.equals(event.getGeneratedToken().getSymbol())) {
                    token[0] = event.getTokens().get(0);
                }
            }

            @Override
            public final void unexpectedSymbolErrorOccured(final UnexpectedSymbolErrorEvent event) {
                debugPrint(lexer.getStack(), lexer.getStateIndex(), lexer.getInputSymbol());
            }

        });

        return new net.sourceforge.aprog.tools.AbstractIterator<Object>(null) {

            private boolean lexerInitialized;

            @Override
            protected final boolean updateNextElement() {
                final boolean debug = false;

                if (!this.lexerInitialized) {
                    lexer.prepareToPerformFirstOperation(input);

                    this.lexerInitialized = true;
                }

                // <editor-fold defaultstate="collapsed" desc="DEBUG">
                if (debug) {
                    debugPrint("stack:", lexer.getStack(), "stateIndex:", lexer.getStateIndex(),
                            "inputSymbol:", lexer.getInputSymbol(), "token:", token[0]);
                }
                // </editor-fold>

                while (token[0] == null && !lexer.isDone()) {
                    lexer.performNextOperation();

                    // <editor-fold defaultstate="collapsed" desc="DEBUG">
                    if (debug) {
                        debugPrint("stack:", lexer.getStack(), "stateIndex:", lexer.getStateIndex(),
                                "inputSymbol:", lexer.getInputSymbol(), "token:", token[0]);
                    }
                    // </editor-fold>
                }

                // <editor-fold defaultstate="collapsed" desc="DEBUG">
                if (debug) {
                    debugPrint("token:", token[0], "lexer.isDone():", lexer.isDone());
                }
                // </editor-fold>

                this.setNextElement(token[0]);

                token[0] = null;

                return !lexer.isMatchFound();
            }

            @Override
            public final void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    static enum Special {

        TOKEN, NONTOKEN, GENERATED_SYMBOLS;

    }

}
