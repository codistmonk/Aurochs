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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.aurochs.AbstractLRParser.GeneratedToken;
import net.sourceforge.aurochs.Grammar.Action;
import net.sourceforge.aurochs.Grammar.Regular;
import net.sourceforge.aurochs.Grammar.Rule;
import net.sourceforge.aurochs.Grammar.SpecialSymbol;
import net.sourceforge.aurochs.LALR1LexerBuilder.LRLexer;

/**
 * To define a parser, implement {@link InlineParserBuilder#parser()} with instructions of the form:<code>
 * <br>if (this.rules()) {
 * <br>&nbsp;&nbsp;this.addXxx(...);
 * <br>&nbsp;&nbsp;...
 * <br>&nbsp;&nbsp;this.addXxxRule(...);
 * <br>} else if (this.action()) {
 * <br>&nbsp;&nbsp;...
 * <br>}
 * </code>
 * <br>
 * <br>Example ("letter counter" program):<code>
 * <br>@Override
 * <br>public final void parser() {
 * <br>&nbsp;&nbsp;// &lt;lexer-rules&gt;
 * <br>
 * <br>&nbsp;&nbsp;if (this.rules()) {
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;this.addTokenRule("WORD", oneOrMore(range('a', 'z')));
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;this.addNontokenRule("_", zeroOrMore(' '));
 * <br>&nbsp;&nbsp;}
 * <br>
 * <br>&nbsp;&nbsp;// &lt;/lexer-rules&gt;
 * <br>
 * <br>&nbsp;&nbsp;// &lt;parser-rules&gt;
 * <br>
 * <br>&nbsp;&nbsp;int letterCount = 0;
 * <br>
 * <br>&nbsp;&nbsp;if (this.rules()) {
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;this.addRule("PROGRAM", "WORDS")
 * <br>&nbsp;&nbsp;} else if (this.action()) {
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(letterCount);
 * <br>&nbsp;&nbsp;}
 * <br>
 * <br>&nbsp;&nbsp;if (this.rules()) {
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;this.addRule("WORDS", "WORDS", "WORD")
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;this.addRule("WORDS", "WORD")
 * <br>&nbsp;&nbsp;} else if (this.action()) {
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;letterCount += ((String) this.value(0)).length();
 * <br>&nbsp;&nbsp;}
 * <br>
 * <br>&nbsp;&nbsp;// &lt;/parser-rules&gt;
 * <br>}
 * </code>
 * @author codistmonk (creation 2011-09-12)
 */
public abstract class InlineParserBuilder {

    private final List<Rule> actionRules;

    private int actionRuleIndex;

    private boolean isParsing;

    private Rule currentRule;

    private List<Object> currentValues;

    private GeneratedToken currentGeneratedToken;

    private LALR1LexerBuilder lexerBuilder;

    private LALR1ParserBuilder parserBuilder;

    private InlineAction inlineAction;

    protected InlineParserBuilder() {
        this.actionRules = new ArrayList<Rule>();
        this.inlineAction = this.new InlineAction();
    }

    final void resetActionRuleIndex() {
        this.actionRuleIndex = -1;
    }

    /**
     * @return
     * <br>Maybe null
     * <br>New
     */
    public final LRLexer newLexer() {
        this.updateParserBuilder();

        return this.lexerBuilder == null ? null : this.lexerBuilder.newLexer();
    }

    /**
     * @return
     * <br>Maybe null
     * <br>New
     */
    public final LRParser newParser() {
        this.updateParserBuilder();

        return this.parserBuilder == null ? null : this.parserBuilder.newParser(this.newLexer());
    }

    final void updateParserBuilder() {
        if (!this.isParsing) {
            this.resetActionRuleIndex();
            this.parser();

            this.isParsing = true;
        }
    }

    public abstract void parser();

    /**
     * @return
     * <br>Range: <code>[0 .. Integer.MAX_VALUE]</code>
     */
    protected final int getValueCount() {
        return this.currentValues.size();
    }

    /**
     * @param <T> The expected return type
     * @param index
     * <br>Range: <code>[0 .. this.getValueCount() - 1]</code>
     * @return
     * <br>Maybe null
     * <br>Reference
     */
    @SuppressWarnings("unchecked")
    protected final <T> T value(final int index) {
        return (T) this.currentValues.get(index);
    }

    /**
     * @param value
     * <br>Maybe null
     * <br>Will become reference
     */
    protected final void setValue(final Object value) {
        this.currentGeneratedToken.setValue(value);
    }

    /**
     * @return
     * <br>Range: any boolean
     */
    protected final boolean rules() {
        if (++this.actionRuleIndex == this.actionRules.size()) {
            this.actionRules.add(null);
        }

        return !this.isParsing;
    }

    /**
     * @return
     * <br>Range: any boolean
     */
    protected final boolean action() {
        return this.isParsing && this.currentRule == this.actionRules.get(this.actionRuleIndex);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param development
     * <br>Not null
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    protected final void addTokenRule(final Object nonterminal, final Object... development) {
        this.setActionRule(this.getLexerBuilder().addTokenRule(nonterminal, development));
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param regularDevelopment 
     * <br>Not null
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    protected final void addTokenRule(final Object nonterminal, final Regular regularDevelopment) {
        this.setActionRule(this.getLexerBuilder().addTokenRule(nonterminal, regularDevelopment));
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param development
     * <br>Not null
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    protected final void addVerbatimTokenRule(final Object nonterminal, final Object... development) {
        this.setActionRule(this.getLexerBuilder().addVerbatimTokenRule(nonterminal, development));
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param regularDevelopment
     * <br>Not null
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    protected final void addVerbatimTokenRule(final Object nonterminal, final Regular regularDevelopment) {
        this.setActionRule(this.getLexerBuilder().addVerbatimTokenRule(nonterminal, regularDevelopment));
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param development
     * <br>Not null
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    protected final void addNontokenRule(final Object nonterminal, final Object... development) {
        this.setActionRule(this.getLexerBuilder().addNontokenRule(nonterminal, development));
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param regularDevelopment
     * <br>Not null
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    protected final void addNontokenRule(final Object nonterminal, final Regular regularDevelopment) {
        this.setActionRule(this.getLexerBuilder().addNontokenRule(nonterminal, regularDevelopment));
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param development
     * <br>Not null
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    protected final void addHelperRule(final Object nonterminal, final Object... development) {
        this.setActionRule(this.getLexerBuilder().addHelperRule(nonterminal, development));
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param regularDevelopment
     * <br>Not null
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    protected final void addHelperRule(final Object nonterminal, final Regular regularDevelopment) {
        this.setActionRule(this.getLexerBuilder().addHelperRule(nonterminal, regularDevelopment));
    }

    /**
     * @param symbol
     * <br>Maybe null
     * <br>May become reference
     * @param priority
     * <br>Range: <code>[Short.MIN_VALUE .. Short.MAX_VALUE]</code>
     * @throws IllegalStateException If <code>symbol</code> is already registered as right-associative
     */
    protected final void addLeftAssociativeBinaryOperator(final Object symbol, final int priority) {
        this.getParserBuilder().addLeftAssociativeBinaryOperator(symbol);
        this.getParserBuilder().setPriority(symbol, (short) priority);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param development
     * <br>Not null
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    protected final void addRule(final Object nonterminal, final Object... development) {
        this.setActionRule(this.getParserBuilder().addRule(nonterminal, development));
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become reference
     * @param regularDevelopment
     * <br>Not null
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    protected final void addRule(final Object nonterminal, final Regular regularDevelopment) {
        this.setActionRule(this.getParserBuilder().addRule(nonterminal, regularDevelopment));
    }

    /**
     * @param symbol
     * <br>Maybe null
     * <br>May become reference
     * @param priority
     * <br>Range: <code>[Short.MIN_VALUE .. Short.MAX_VALUE]</code>
     * @return
     * <br>Range: any boolean
     * @throws IllegalStateException If <code>symbol</code> is already registered as left-associative
     */
    protected final boolean addRightAssociativeBinaryOperator(final Object symbol, final int priority) {
        this.getParserBuilder().addRightAssociativeBinaryOperator(symbol);
        this.getParserBuilder().setPriority(symbol, (short) priority);

        return false;
    }

    /**
     * @param currentGeneratedToken
     * <br>Maybe null
     * <br>Will become reference
     */
    final void setCurrentGeneratedToken(final GeneratedToken currentGeneratedToken) {
        this.currentGeneratedToken = currentGeneratedToken;
    }

    /**
     * @param currentValues
     * <br>Maybe null
     * <br>Will become reference
     */
    final void setCurrentValues(final List<Object> currentValues) {
        this.currentValues = currentValues;
    }

    /**
     * @param rule
     * <br>Maybe null
     * <br>Will become reference
     */
    final void setCurrentRule(final Rule rule) {
        this.currentRule = rule;
    }

    /**
     * @param rule
     * <br>Not null
     * <br>Will become reference
     */
    private final void setActionRule(final Rule rule) {
        rule.getActions().add(this.inlineAction);

        this.actionRules.set(this.actionRuleIndex, rule);
    }

    /**
     * @return
     * <br>Not null
     * <br>Maybe new
     * <br>Reference
     */
    private final LALR1LexerBuilder getLexerBuilder() {
        if (this.lexerBuilder == null) {
            this.lexerBuilder = new LALR1LexerBuilder();
        }

        return this.lexerBuilder;
    }

    /**
     * @return
     * <br>Not null
     * <br>Maybe new
     * <br>Reference
     */
    private final LALR1ParserBuilder getParserBuilder() {
        if (this.parserBuilder == null) {
            this.parserBuilder = new LALR1ParserBuilder();
        }

        return this.parserBuilder;
    }

    /**
     * @author codistmonk (creation 2011-09-12)
     */
    private final class InlineAction implements Action {

        /**
         * Package-private default constructor to suppress visibility warnings.
         */
        InlineAction() {
            // Deliberately left empty
        }

        @Override
        public final void perform(final Rule rule, final GeneratedToken generatedToken, final List<Object> developmentTokens) {
            LRParserTools.extractValues(developmentTokens);

            InlineParserBuilder.this.setCurrentRule(rule);
            InlineParserBuilder.this.setCurrentGeneratedToken(generatedToken);
            InlineParserBuilder.this.setCurrentValues(developmentTokens);

            InlineParserBuilder.this.resetActionRuleIndex();
            InlineParserBuilder.this.parser();
        }

    }

}
