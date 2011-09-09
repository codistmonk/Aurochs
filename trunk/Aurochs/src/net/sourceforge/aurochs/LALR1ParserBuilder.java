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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs.Grammar.Rule;
import net.sourceforge.aurochs.Grammar.Regular;
import net.sourceforge.aurochs.LALR1ClosureTable.Closure;
import net.sourceforge.aurochs.LALR1ClosureTable.Item;
import net.sourceforge.aurochs.LALR1ClosureTable.Kernel;
import net.sourceforge.aurochs.LALR1LexerBuilder.LRLexer;
import net.sourceforge.aurochs.LRTable.BeforeOperationAddedEvent;

/**
 * @author codistmonk (creation 2010-10-06)
 */
public final class LALR1ParserBuilder {

    private final Grammar grammar;

    private boolean epsilonProdutionsAllowed;

    private final List<LRTable.Listener> listeners;

    public LALR1ParserBuilder() {
        this.grammar = new Grammar();
        this.listeners = new ArrayList<LRTable.Listener>();

        this.addListener(new LRTable.Listener() {

            @Override
            public final void beforeOperationAdded(final BeforeOperationAddedEvent event) {
                if (event.getResolution() == null) {
                    final String message = "Conflict " + event.getOldOperation() + "/" + event.getNewOperation() +
                            " in state " + event.getStateIndex() + " on symbol " + event.getSymbol() +
                            " for paths " + event.getPathsToState();

                    event.setResolution(event.getOldOperation());

                    getLoggerForThisMethod().log(Level.WARNING, "{0} resolved in favor of {1}", array(message, event.getResolution()));
                }
            }

        });
    }

    /**
     * @param listener
     * <br>Not null
     * <br>Will be strong reference
     */
    public final void addListener(final LRTable.Listener listener) {
        this.listeners.add(listener);
    }

    /**
     * @param listener
     * <br>Maybe null
     */
    public final void removeListener(final LRTable.Listener listener) {
        this.listeners.remove(listener);
    }

    /**
     * @return
     * <br>Not null
     * <br>New
     */
    public final LRTable.Listener[] getListeners() {
        return this.listeners.toArray(new LRTable.Listener[this.listeners.size()]);
    }

    /**
     * @param symbol
     * <br>Maybe null
     * <br>May become strong reference
     * @param priority
     * <br>Range: any short
     * @throws IllegalStateException If <code>symbol</code>'s priority has already been defined
     */
    public final void setPriority(final Object symbol, final short priority) {
        if (this.getGrammar().getPriorities().put(symbol, priority) != null) {
            throw new IllegalStateException("Priority already defined for symbol " + symbol);
        }
    }

    /**
     * @return
     * <br>Range: any boolean
     */
    public final boolean isEpsilonProdutionsAllowed() {
        return this.epsilonProdutionsAllowed;
    }

    /**
     * @param epsilonProdutionsAllowed
     * <br>Range: any boolean
     */
    public void setEpsilonProdutionsAllowed(final boolean epsilonProdutionsAllowed) {
        this.epsilonProdutionsAllowed = epsilonProdutionsAllowed;
    }

    /**
     *
     * @param nonterminal
     * <br>Maybe null
     * <br>Will be strong reference
     * @param development
     * <br>Not null
     * @return
     * <br>Not null
     * <br>Maybe New
     * <br>Reference
     */
    public final Rule addRule(final Object nonterminal, final Object... development) {
        return this.getGrammar().addRule(nonterminal, development);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will be strong reference
     * @param regularDevelopment
     * <br>Not null
     * @return
     * <br>Not null
     * <br>Maybe New
     * <br>Reference
     */
    public final Rule addRule(final Object nonterminal, final Regular regularDevelopment) {
        return this.getGrammar().addRule(nonterminal, regularDevelopment);
    }

    /**
     * @param symbol
     * <br>Maybe null
     */
    public final void addLeftAssociativeBinaryOperator(final Object symbol) {
        if (this.getGrammar().getRightAssociativeBinaryOperators().contains(symbol)) {
            throw new IllegalStateException(symbol + " is right-associative");
        }

        this.getGrammar().getLeftAssociativeBinaryOperators().add(symbol);
    }

    /**
     * @param symbol
     * <br>Maybe null
     */
    public final void addRightAssociativeBinaryOperator(final Object symbol) {
        if (this.getGrammar().getLeftAssociativeBinaryOperators().contains(symbol)) {
            throw new IllegalStateException(symbol + " is left-associative");
        }

        this.getGrammar().getRightAssociativeBinaryOperators().add(symbol);
    }

    /**
     *
     * @return
     * <br>Not null
     * <br>New
     */
    public final LRTable newTable() {
        this.maybeRemoveEpsilonRules();

        return newTable(new LALR1ClosureTable(this.getGrammar()), this.getListeners());
    }

    /**
     * @return
     * <br>Not null
     * <br>New
     */
    public final LRParser newParser() {
        return new LRParser(this.newTable());
    }

    /**
     * @param lexer
     * <br>Maybe null
     * <br>Will become reference
     * @return
     * <br>Not null
     * <br>New
     */
    public final LRParser newParser(final LRLexer lexer) {
        return new LRParser(this.newTable(), lexer);
    }

    /**
     * @return
     * <br>Not null
     * <br>Strong reference
     */
    final Grammar getGrammar() {
        return this.grammar;
    }

    private final void maybeRemoveEpsilonRules() {
        final boolean debug = false;

        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            for (final Rule rule : this.getGrammar().getRules()) {
                debugPrint("(", rule.getIndex(), ")", rule.getNonterminal(), rule.getDevelopment());
            }
        }
        // </editor-fold>

        if (!this.isEpsilonProdutionsAllowed()) {
            this.normalizeEpsilonNonterminalUses();
            this.normalizeInitialRule();
        }

        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            for (final Rule rule : this.getGrammar().getRules()) {
                debugPrint("(", rule.getIndex(), ")", rule.getNonterminal(), rule.getDevelopment());
            }
        }
        // </editor-fold>
    }

    private final void normalizeEpsilonNonterminalUses() {
        for (final Object nonterminal : this.getGrammar().getNonterminals()) {
            if (this.getGrammar().canCollapse(nonterminal)) {
                this.addAlternativeUses(nonterminal);
            }
        }

        this.removeAllEpsilonRules();
    }

    /**
     * Adds alternative rules corresponding to the cases where <code>nonterminal</code> is collapsed.
     * <br>For instance, if the nontermial <code>C</code> can collapse (ie expand to epsilon),
     * then from the rule <code>A -&gt; bCdCe</code> we can add:<ul>
     *  <li><code>A -&gt; bdCe</code></li>
     *  <li><code>A -&gt; bCde</code></li>
     *  <li><code>A -&gt; bde</code></li>
     * </ul>
     * @param nonterminal
     * <br>Maybe null
     */
    private final void addAlternativeUses(final Object nonterminal) {
        // Duplicate each rule using nonterminal and remove nonterminals from the duplicate
        // Repeat until nonterminal isn't used anymore
        final List<Rule> todo = new LinkedList<Rule>(this.getGrammar().getRules());

        while (!todo.isEmpty()) {
            final Rule rule = AurochsTools.take(todo);

            if (this.getGrammar().getFirsts(nonterminal).isEmpty()) {
                this.getGrammar().removeRule(rule);
                this.getGrammar().addRule(rule.getNonterminal(), rule.newOriginalDevelopmentWithEpsilon(nonterminal))
                        .getActions().addAll(rule.getActions());
            } else {
                for (int i = 0; i < rule.getOriginalDevelopment().length; ++i) {
                    if (Tools.equals(nonterminal, rule.getOriginalDevelopment()[i])) {
                        todo.add(this.getGrammar().addRule(
                                rule.getNonterminal(), rule.newOriginalDevelopmentWithEpsilon(i)));
                    }
                }
            }
        }
    }

    private final void removeAllEpsilonRules() {
        final boolean debug = false;

        for (final Rule rule : new ArrayList<Rule>(this.getGrammar().getRules())) {
            if (rule.getDevelopment().isEmpty() && rule.getNonterminal() != Grammar.SpecialSymbol.INITIAL_NONTERMINAL) {
                // <editor-fold defaultstate="collapsed" desc="DEBUG">
                if (debug) {
                    debugPrint(rule.getNonterminal(), rule.getDevelopment());
                }
                // </editor-fold>

                this.getGrammar().removeRule(rule);

                if (!rule.getActions().isEmpty()) {
                    getLoggerForThisMethod().log(Level.WARNING, "Removed rule with action: {0}", rule);
                }
            }
        }
    }

    /**
     * Ensures there is only one initial rule.
     */
    private final void normalizeInitialRule() {
        final List<Rule> initialRules = this.getGrammar().getRules(Grammar.SpecialSymbol.INITIAL_NONTERMINAL);

        if (initialRules.size() > 1) {
            final Object nonterminal = new Object();

            for (final Rule rule : initialRules) {
                this.getGrammar().addRule(nonterminal, rule.getDevelopment().toArray());
            }

            while (initialRules.size() > 1) {
                final Rule rule = initialRules.get(1);

                this.getGrammar().removeRule(rule);

                if (!rule.getActions().isEmpty()) {
                    getLoggerForThisMethod().log(Level.WARNING, "Removed rule with action: {0}", rule);
                }
           }

            initialRules.get(0).getDevelopment().set(0, nonterminal);
        }
    }

    /**
     * @param list
     * <br>Not null
     * @param index
     * <br>Range: <code>[0 .. list.size() - 1]</code>
     * @return
     * <br>Not null
     * <br>New
     */
    public static final Object[] remove(final List<Object> list, final int index) {
        final Object[] result = new Object[list.size() - 1];

        for (int i = 0; i < result.length; ++i) {
            result[i] = list.get(i < index ? i : i + 1);
        }

        return result;
    }

    /**
     * @param closureTable
     * <br>Not null
     * @param listeners
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    public static final LRTable newTable(final LALR1ClosureTable closureTable, final LRTable.Listener... listeners) {
        final boolean debug = false;
        final LRTable result = new LRTable(closureTable.getGrammar());

        for (final LRTable.Listener listener : listeners) {
            result.addListener(listener);
        }

        for (final Closure closure : closureTable.getClosures().values()) {
            final int closureIndex = closure.getKernel().getIndex();

            for (final Map.Entry<Object, Kernel> entry : closure.getTransitions().entrySet()) {
                // <editor-fold defaultstate="collapsed" desc="DEBUG">
                if (debug) {
                    debugPrint(closureIndex + "[" + entry.getKey() + "] = s" + closureTable.getClosures().get(entry.getValue()).getKernel().getIndex());
                }
                // </editor-fold>

                result.addShift(closureIndex, entry.getKey(), closureTable.getClosures().get(entry.getValue()));
            }

            for (final Item item : closure.getItems()) {
                if (item.isFinal()) {
                    for (final Object symbol : item.getLookAheads()) {
                        // <editor-fold defaultstate="collapsed" desc="DEBUG">
                        if (debug) {
                            debugPrint(closureIndex + "[" + symbol + "] = r" + item.getRuleIndex());
                        }
                        // </editor-fold>

                        result.addReduction(closureIndex, symbol, item.getRuleIndex());
                    }
                }
            }
        }

        return result;
    }

}
