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

import static net.sourceforge.aprog.tools.Tools.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.aurochs.AbstractLRParser.ReductionEvent;
import net.sourceforge.aurochs.AbstractLRParser.UnexpectedSymbolErrorEvent;
import net.sourceforge.aurochs.Grammar.Regular;

/**
 * @author codistmonk (creation 2011-09-06)
 */
public final class LALR1LexerBuilder {

    private final LALR1ParserBuilder lexerTableBuilder;

    private final Set<Object> tokens;

    private final Set<Object> nontokens;

    private final Set<Object> verbatimTokenNonterminals;

    public LALR1LexerBuilder() {
        this.lexerTableBuilder = new LALR1ParserBuilder();
        this.tokens = new HashSet<Object>();
        this.nontokens = new HashSet<Object>();
        this.verbatimTokenNonterminals = new HashSet<Object>();

        this.lexerTableBuilder.addRule(Special.ROOT);
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
    public final void addNontokenRule(final Object nonterminal, final Object... development) {
        this.addNontoken(nonterminal);
        this.addHelperRule(nonterminal, development);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param regularDevelopment
     * <br>Not null
     */
    public final void addNontokenRule(final Object nonterminal, final Regular regularDevelopment) {
        this.addNontoken(nonterminal);
        this.addHelperRule(nonterminal, regularDevelopment);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param development
     * <br>Not null
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    public final void addTokenRule(final Object nonterminal, final Object... development) {
        this.addToken(nonterminal);
        this.addHelperRule(nonterminal, development);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param regularDevelopment
     * <br>Not null
     */
    public final void addTokenRule(final Object nonterminal, final Regular regularDevelopment) {
        this.addToken(nonterminal);
        this.addHelperRule(nonterminal, regularDevelopment);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param development
     * <br>Not null
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    public final void addVerbatimTokenRule(final Object nonterminal, final Object... development) {
        this.addVerbatimToken(nonterminal);
        this.addHelperRule(nonterminal, development);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param regularDevelopment
     * <br>Not null
     */
    public final void addVerbatimTokenRule(final Object nonterminal, final Regular regularDevelopment) {
        this.addVerbatimToken(nonterminal);
        this.addHelperRule(nonterminal, regularDevelopment);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param development
     * <br>Not null
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    public final void addHelperRule(final Object nonterminal, final Object... development) {
        this.lexerTableBuilder.addRule(nonterminal, development);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param regularDevelopment
     * <br>Not null
     */
    public final void addHelperRule(final Object nonterminal, final Regular regularDevelopment) {
        this.lexerTableBuilder.addRule(nonterminal, regularDevelopment);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    private final void addToken(final Object nonterminal) {
        if (!this.tokens.contains(nonterminal)) {
            this.lexerTableBuilder.addRule(Special.ROOT, Special.ROOT, nonterminal);
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
            this.lexerTableBuilder.addRule(Special.ROOT, Special.ROOT, nonterminal);
            this.nontokens.add(nonterminal);
        }
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    private final void addVerbatimToken(final Object nonterminal) {
        if (!this.verbatimTokenNonterminals.contains(nonterminal)) {
            this.lexerTableBuilder.addRule(Special.ROOT, Special.ROOT, nonterminal);
            this.verbatimTokenNonterminals.add(nonterminal);
        }
    }

    /**
     * @return
     * <br>Not null
     * <br>New
     */
    public final LRLexer newLexer() {
        final LRLexer result = new LRLexer(this.newTable());

        result.getTokens().addAll(this.tokens);
        result.getVerbatimTokenNonterminals().addAll(this.verbatimTokenNonterminals);

        return result;
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
    public static final Iterator<Object> tokenize(final LRLexer lexer, final Iterator<?> input) {
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

                final Object symbol = event.getGeneratedToken().getSymbol();

                if (lexer.getTokens().contains(symbol)) {
                    token[0] = symbol;
                } else if (lexer.getVerbatimTokenNonterminals().contains(symbol)) {
                    switch (event.getTokens().size()) {
                        case 0:
                            break;
                        case 1:
                            token[0] = event.getTokens().get(0);

                            break;
                        default:
                            final StringBuilder tokenBuilder = new StringBuilder();

                            for (final Object developmentSymbol : event.getTokens()) {
                                tokenBuilder.append(developmentSymbol);
                            }

                            token[0] = tokenBuilder.toString();

                            break;
                    }
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

    /**
     * @author codistmonk (creation 2011-09-06)
     */
    static enum Special {

        ROOT;

    }

    /**
     * @author codistmonk (creation 2011-09-08)
     */
    public static final class LRLexer extends AbstractLRParser {

        private final Set<Object> tokens;

        private final Set<Object> verbatimTokenNonterminals;

        /**
         *
         * @param table
         * <br>Not null
         * <br>Will become reference
         */
        LRLexer(final LRTable table) {
            super(table);
            this.tokens = new HashSet<Object>();
            this.verbatimTokenNonterminals = new HashSet<Object>();
        }

        /**
         * @return
         * <br>Not null
         * <br>Reference
         */
        final Set<Object> getTokens() {
            return this.tokens;
        }

        /**
         * @return
         * <br>Not null
         * <br>Reference
         */
        final Set<Object> getVerbatimTokenNonterminals() {
            return this.verbatimTokenNonterminals;
        }

    }

}
