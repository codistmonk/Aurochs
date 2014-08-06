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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sourceforge.aprog.tools.AbstractIterator;
import net.sourceforge.aurochs.AbstractLRParser.GeneratedToken;
import net.sourceforge.aurochs.AbstractLRParser.ReductionEvent;
import net.sourceforge.aurochs.AbstractLRParser.UnexpectedSymbolErrorEvent;
import net.sourceforge.aurochs.Grammar.Regular;
import net.sourceforge.aurochs.Grammar.Rule;
import net.sourceforge.aurochs.Grammar.SpecialSymbol;

/**
 * @author codistmonk (creation 2011-09-06)
 */
public final class LALR1LexerBuilder implements Serializable {
	
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
     * @return
     * <br>Not null
     * <br>Maybe New
     * <br>Reference
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    public final Rule addNontokenRule(final Object nonterminal, final Object... development) {
        this.addNontoken(nonterminal);

        return this.addHelperRule(nonterminal, development);
    }
    
    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param regularDevelopment
     * <br>Not null
     * @return
     * <br>Not null
     * <br>Maybe New
     * <br>Reference
     */
    public final Rule addNontokenRule(final Object nonterminal, final Regular regularDevelopment) {
        this.addNontoken(nonterminal);

        return this.addHelperRule(nonterminal, regularDevelopment);
    }
    
    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param development
     * <br>Not null
     * @return
     * <br>Not null
     * <br>Maybe New
     * <br>Reference
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    public final Rule addTokenRule(final Object nonterminal, final Object... development) {
        this.addToken(nonterminal);

        return this.addHelperRule(nonterminal, development);
    }
    
    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param regularDevelopment
     * <br>Not null
     * @return
     * <br>Not null
     * <br>Maybe New
     * <br>Reference
     */
    public final Rule addTokenRule(final Object nonterminal, final Regular regularDevelopment) {
        this.addToken(nonterminal);

        return this.addHelperRule(nonterminal, regularDevelopment);
    }
    
    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param development
     * <br>Not null
     * @return
     * <br>Not null
     * <br>Maybe New
     * <br>Reference
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    public final Rule addVerbatimTokenRule(final Object nonterminal, final Object... development) {
        this.addVerbatimToken(nonterminal);

        return this.addHelperRule(nonterminal, development);
    }
    
    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param regularDevelopment
     * <br>Not null
     * @return
     * <br>Not null
     * <br>Maybe New
     * <br>Reference
     */
    public final Rule addVerbatimTokenRule(final Object nonterminal, final Regular regularDevelopment) {
        this.addVerbatimToken(nonterminal);

        return this.addHelperRule(nonterminal, regularDevelopment);
    }
    
    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param development
     * <br>Not null
     * @return
     * <br>Not null
     * <br>Maybe New
     * <br>Reference
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    public final Rule addHelperRule(final Object nonterminal, final Object... development) {
        return this.lexerTableBuilder.addRule(nonterminal, development);
    }
    
    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param regularDevelopment
     * <br>Not null
     * @return
     * <br>Not null
     * <br>Maybe New
     * <br>Reference
     */
    public final Rule addHelperRule(final Object nonterminal, final Regular regularDevelopment) {
        return this.lexerTableBuilder.addRule(nonterminal, regularDevelopment);
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
	 * {@value}.
	 */
	private static final long serialVersionUID = -8641712690017409809L;
    
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

        lexer.addListener(new ReductionListener(lexer, token));

        return new TokenSource(token, lexer, input);
    }
    
    /**
     * @param token
     * <br>Maybe null
     * @return
     * <br>Maybe null
     * <br>Maybe new
     */
    static final Object condense(final Object token) {
        if (token instanceof Character || token instanceof CharSequence) {
            return token;
        }

        final List<?> list = cast(List.class, token);

        if (list != null && !list.isEmpty()) {
            final StringBuilder result = new StringBuilder();

            for (final Object subToken : list) {
                result.append(condense(subToken));
            }

            return result.toString();
        }

        return token;
    }
    
    /**
     * @param token
     * <br>Maybe null
     * @return
     * <br>Range: any boolean
     */
    static final boolean isCharacterOrCharSequence(final Object token) {
        if (token instanceof Character || token instanceof CharSequence) {
            return true;
        }

        final List<?> list = cast(List.class, token);

        if (list != null && !list.isEmpty()) {
            return isCharacterOrCharSequence(list.get(0));
        }

        return false;
    }
    
    /**
     * @param tokens
     * <br>Not null
     * <br>Input-output
     */
    static final void unwrapRegulars(final List<Object> tokens) {
        for (int i = 0; i < tokens.size(); ++i) {
            final GeneratedToken generatedToken = cast(GeneratedToken.class, tokens.get(i));

            if (generatedToken != null && generatedToken.getSymbol() instanceof Regular.GeneratedSymbol) {
                tokens.set(i, generatedToken.getValue());
            }
        }
    }
    
    /**
     * @author codistmonk (creation 2014-08-06)
     */
    static final class TokenSource extends AbstractIterator<Object> implements Serializable {
    	
		private final Object[] token;
		
		private final LRLexer lexer;
		
		private final Iterator<?> input;
		
		private boolean lexerInitialized;
		
		TokenSource(final Object[] token, final LRLexer lexer, final Iterator<?> input) {
			super(null);
			this.token = token;
			this.lexer = lexer;
			this.input = input;
		}
		
		@Override
		protected final boolean updateNextElement() {
		    final boolean debug = false;

		    if (!this.lexerInitialized) {
		        this.lexer.prepareToPerformFirstOperation(this.input);

		        this.lexerInitialized = true;
		    }

		    // <editor-fold defaultstate="collapsed" desc="DEBUG">
		    if (debug) {
		        debugPrint(this.lexer.getTable().getGrammar().getRules());
		        debugPrint(this.lexer.getTable().getStates());
		        debugPrint(this.lexer.getTable().getStates().get(this.lexer.getStateIndex()));
		        debugPrint("stack:", this.lexer.getStack(), "stateIndex:", this.lexer.getStateIndex(),
		                "inputSymbol:", this.lexer.getInputSymbol(), "token:", this.token[0]);
		    }
		    // </editor-fold>

		    while (this.token[0] == null && !this.lexer.isDone()) {
		        this.lexer.performNextOperation();

		        // <editor-fold defaultstate="collapsed" desc="DEBUG">
		        if (debug) {
		            debugPrint("stack:", this.lexer.getStack(), "stateIndex:", this.lexer.getStateIndex(),
		                    "inputSymbol:", this.lexer.getInputSymbol(), "token:", this.token[0]);
		        }
		        // </editor-fold>
		    }

		    // <editor-fold defaultstate="collapsed" desc="DEBUG">
		    if (debug) {
		        debugPrint("token:", this.token[0], "lexer.isDone():", this.lexer.isDone());
		    }
		    // </editor-fold>

		    this.setNextElement(this.token[0]);

		    this.token[0] = null;

		    return !this.lexer.isMatchFound();
		}

		@Override
		public final void remove() {
		    throw new UnsupportedOperationException();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 4861261710501138855L;
		
	}

    /**
     * @author codistmonk (creation 2014-08-06)
     */
	static final class ReductionListener implements LRParser.Listener {
		
		private final LRLexer lexer;
		
		private final Object[] token;

		ReductionListener(final LRLexer lexer, final Object[] token) {
			this.lexer = lexer;
			this.token = token;
		}

		@Override
		public final void reductionOccured(final ReductionEvent event) {
		    final boolean debug = false;

		    // <editor-fold defaultstate="collapsed" desc="DEBUG">
		    if (debug) {
		        debugPrint("generatedToken:", event.getGeneratedToken(), "tokens:", event.getTokens());
		    }
		    // </editor-fold>

		    final Object symbol = event.getGeneratedToken().getSymbol();

		    unwrapRegulars(event.getTokens());

		    if (symbol instanceof Regular.GeneratedSymbol) {
		        event.getGeneratedToken().setValue(condense(event.getTokens()));
		    }

		    if (this.lexer.getTokens().contains(symbol)) {
		        this.token[0] = new GeneratedToken(symbol);

		        ((GeneratedToken) this.token[0]).setValue(condense(event.getTokens()));
		    } else if (this.lexer.getVerbatimTokenNonterminals().contains(symbol)) {
		        switch (event.getTokens().size()) {
		            case 0:
		                break;
		            case 1:
		                this.token[0] = event.getTokens().get(0);

		                break;
		            default:
		                this.token[0] = condense(event.getTokens());

		                break;
		        }
		    }
		}

		@Override
		public final void unexpectedSymbolErrorOccured(final UnexpectedSymbolErrorEvent event) {
		    debugPrint(this.lexer.getStack(), this.lexer.getStateIndex(), this.lexer.getInputSymbol());
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 5249788461251321361L;
		
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
    public static final class LRLexer extends AbstractLRParser implements Serializable {
    	
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
    	
        /**
		 * {@value}.
		 */
		private static final long serialVersionUID = -5286285558542183142L;
        
    }
    
}
