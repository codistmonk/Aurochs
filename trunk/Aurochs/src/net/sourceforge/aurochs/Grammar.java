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

import static java.util.Collections.*;

import static net.sourceforge.aprog.tools.Tools.*;
import static net.sourceforge.aurochs.AurochsTools.*;
import static net.sourceforge.aurochs.Grammar.SpecialSymbol.*;
import static net.sourceforge.aurochs.Grammar.UnmodifiableKey.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2010-10-04)
 */
public final class Grammar implements Serializable {

    private final Map<Object, Object> unmodifiables;

    private final Set<Object> nonterminals;

    private final Set<Object> terminals;

    private final List<Production> productions;

    private final Map<Object, List<Production>> productionMap;

    private final Set<Object> collapsables;

    private final Map<Object, Set<Object>> firstMap;

    private boolean collapsablesUpdated;

    private boolean firstsUpdated;

    private final Set<Object> leftAssociativeBinaryOperators;

    private final Set<Object> rightAssociativeBinaryOperators;

    private final Map<Object, Short> priorities;

    public Grammar() {
        this.unmodifiables = new HashMap<Object, Object>();
        this.nonterminals = new LinkedHashSet<Object>();
        this.terminals = new LinkedHashSet<Object>();
        this.productions = new ArrayList<Production>();
        this.productionMap = new LinkedHashMap<Object, List<Production>>();
        this.collapsables = new LinkedHashSet<Object>();
        this.firstMap = new LinkedHashMap<Object, Set<Object>>();
        this.leftAssociativeBinaryOperators = new LinkedHashSet<Object>();
        this.rightAssociativeBinaryOperators = new LinkedHashSet<Object>();
        this.priorities = new LinkedHashMap<Object, Short>();

        this.unmodifiables.put(NONTERMINALS, unmodifiableSet(this.nonterminals));
        this.unmodifiables.put(TERMINALS, unmodifiableSet(this.terminals));
        this.unmodifiables.put(PRODUCTIONS, unmodifiableList(this.productions));
    }

    /**
     * @return
     * <br>Not null
     * <br>Strong reference
     */
    public final Map<Object, Short> getPriorities() {
        return this.priorities;
    }

    /**
     * @return
     * <br>Not null
     * <br>Strong reference
     */
    public final Set<Object> getLeftAssociativeBinaryOperators() {
        return this.leftAssociativeBinaryOperators;
    }

    /**
     * @return
     * <br>Not null
     * <br>Strong reference
     */
    public final Set<Object> getRightAssociativeBinaryOperators() {
        return this.rightAssociativeBinaryOperators;
    }

    /**
     *
     * @return
     * <br>Not null
     * <br>Shared
     */
    @SuppressWarnings("unchecked")
    public final Set<Object> getNonterminals() {
        return (Set<Object>) this.unmodifiables.get(NONTERMINALS);
    }

    /**
     *
     * @return
     * <br>Not null
     * <br>Shared
     */
    @SuppressWarnings("unchecked")
    public final Set<Object> getTerminals() {
        return (Set<Object>) this.unmodifiables.get(TERMINALS);
    }

    /**
     *
     * @param symbol
     * <br>Maybe null
     * @return
     * <br>Maybe null
     * <br>Shared
     */
    public final List<Production> getProductions(final Object symbol) {
        return this.getProductionMap().get(symbol);
    }

    /**
     *
     * @param symbol
     * <br>Maybe null
     * @return
     * <br>Not null
     * <br>Shared
     */
    public final Set<Object> getFirsts(final Object symbol) {
        this.updateFirsts();

        return this.firstMap.get(symbol);
    }

    /**
     *
     * @param development
     * <br>Not null
     * @param result
     * <br>Not null
     * <br>Input-output
     * @return {@code result}
     * <br>Not null
     */
    public final Set<Object> getDevelopmentFirsts(final List<Object> development, final Set<Object> result) {
        this.updateFirsts();

        for (final Object symbol : development) {
            result.addAll(this.getFirsts(symbol));

            if (!this.canCollapse(symbol)) {
                break;
            }
        }

        return result;
    }

    /**
     *
     * @param symbol
     * <br>Maybe null
     * @return
     * <br>Range: any boolean
     */
    public final boolean canCollapse(final Object symbol) {
        this.updateCollapsables();

        return this.collapsables.contains(symbol);
    }

    /**
     *
     * @param development
     * <br>Not null
     * @return
     * <br>Range: any boolean
     */
    public final boolean canDevelopmentCollapse(final List<Object> development) {
        for (final Object symbol : development) {
            if (!this.canCollapse(symbol)) {
                return false;
            }
        }

        return true;
    }

    /**
     *
     * @param object
     * <br>Maybe null
     * @return
     * <br>Range: any boolean
     */
    public final boolean hasSymbol(final Object object) {
        return this.hasNonterminal(object) || this.hasTerminal(object);
    }

    /**
     *
     * @param symbol
     * <br>Maybe null
     * @return
     * <br>Range: any boolean
     */
    public final boolean hasNonterminal(final Object symbol) {
        return this.getNonterminals().contains(symbol);
    }

    /**
     *
     * @param symbol
     * <br>Maybe null
     * @return
     * <br>Range: any boolean
     */
    public final boolean hasTerminal(final Object symbol) {
        return this.getTerminals().contains(symbol);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become strong reference
     * @param development
     * <br>Not null
     * @return
     * <br>Not null
     * <br>Maybe New
     * <br>Strong reference
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    public final Production addProduction(final Object nonterminal, final Object... development) {
        return this.addProductionWithEpsilons(nonterminal, development);
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become strong reference
     * @param development
     * <br>Not null
     * @return
     * <br>Not null
     * <br>Maybe New
     * <br>Strong reference
     * @throws IllegalArgumentException if {@code nonterminal} is {@link SpecialSymbol#END_TERMINAL}
     */
    private final Production addProductionWithEpsilons(final Object nonterminal, final Object[] development) {
        if (END_TERMINAL.equals(nonterminal)) {
            throw new IllegalArgumentException(END_TERMINAL + " cannot be used as nonterminal");
        }

        this.maybeInitializeProductionMap(nonterminal);

        final List<Production> productionsWithSameNonterminal = getOrCreate(this.getProductionMap(), nonterminal, ArrayList.class);
        final Production newProduction = this.new Production(this.getProductions().size(), nonterminal, development);

        for (final Production existingProduction : productionsWithSameNonterminal) {
            if (newProduction.getDevelopment().equals(existingProduction.getDevelopment())) {
                if (!mergeEpsilons(development, existingProduction.getOriginalDevelopment())) {
                    getLoggerForThisMethod().log(Level.WARNING, "Grammar is ambiguous because of productions {0} and {1}", array(existingProduction, newProduction));
                }

                return existingProduction;
            }
        }

        this.productions.add(newProduction);
        this.nonterminals.add(nonterminal);
        this.terminals.addAll(newProduction.getDevelopment());
        this.terminals.removeAll(this.getNonterminals());
        productionsWithSameNonterminal.add(newProduction);

        return newProduction;
    }

    /**
     * @param source
     * <br>Not null
     * @param target
     * <br>Not null
     * <br>Input-output
     * @return
     * <br>Range: any boolean
     */
    private static final boolean mergeEpsilons(final Object[] source, final Object[] target) {
        if (source.length != target.length) {
            return false;
        }
        
        boolean sourceHasEpsilonNestings = false;
        boolean targetHasEpsilonNestings = false;

        for (int i = 0; i < source.length; ++i) {
            if (source[i] instanceof Epsilon) {
                sourceHasEpsilonNestings = true;

                if (target[i] instanceof Epsilon) {
                    if (!source[i].equals(target[i])) {
                        return false;
                    }
                } else {
                    target[i] = source[i];
                }
            }

            if (target[i] instanceof Epsilon) {
                targetHasEpsilonNestings = true;
            }
        }

        return sourceHasEpsilonNestings && targetHasEpsilonNestings;
    }

    /**
     * @author codistmonk (creation 2011-06-08)
     */
    static final class Epsilon {

        private final Object nonterminal;

        /**
         * @param nonterminal
         * <br>Maybe null
         * <br>Will be strong reference
         */
        Epsilon(final Object nonterminal) {
            this.nonterminal = nonterminal;
        }

        /**
         * @return
         * <br>Maybe null
         * <br>Strong reference
         */
        public final Object getNonterminal() {
            return this.nonterminal;
        }

        @Override
        public final boolean equals(final Object object) {
            final Epsilon that = cast(this.getClass(), object);

            return that != null && Tools.equals(this.getNonterminal(), that.getNonterminal());
        }

        @Override
        public final int hashCode() {
            return Tools.hashCode(this.getNonterminal());
        }

        @Override
        public final String toString() {
            return "(" + this.getNonterminal() + ")";
        }

        /**
         * {@value}.
         */
        private static final long serialVersionUID = -5270159839679055118L;

        /**
         * @param symbolOrEpsilon
         * <br>Maybe null
         * @return
         * <br>Not null
         * <br>Maybe new
         */
        static final Epsilon castOrCreate(final Object symbolOrEpsilon) {
            if (symbolOrEpsilon instanceof Epsilon) {
                return (Epsilon) symbolOrEpsilon;
            }

            return new Epsilon(symbolOrEpsilon);
        }

    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will be strong reference
     * @param regularDevelopment
     * <br>Not null
     */
    public final void addProduction(final Object nonterminal, final Regular regularDevelopment) {
        this.addProduction(nonterminal, regularDevelopment.getOrCreateSymbol(this));
    }

    /**
     *
     * @return
     * <br>Not null
     * <br>Shared
     */
    @SuppressWarnings("unchecked")
    public final List<Production> getProductions() {
        return (List<Production>) this.unmodifiables.get(PRODUCTIONS);
    }

    /**
     *
     * @param index
     * <br>Range: {@code [0 .. this.getProductions().size() - 1]}
     * @return
     * <br>Not null
     * <br>Shared
     */
    public final Production getProduction(final int index) {
        return this.getProductions().get(index);
    }

    final void updatePriorities() {
        for (final Map.Entry<Object, Short> entry : new LinkedHashSet<Map.Entry<Object, Short>>(this.getPriorities().entrySet())) {
            for (final Object terminal : this.getFirsts(entry.getKey())) {
                final Short oldPriority = this.getPriorities().put(terminal, entry.getValue());

                if (oldPriority != null && oldPriority > entry.getValue()) {
                    this.getPriorities().put(terminal, oldPriority);
                }
            }
        }
    }

    /**
     * @param production
     * <br>Not null
     */
    final void removeProduction(final Production production) {
        this.getProductions(production.getNonterminal()).remove(production);
        this.productions.remove(production);

        for (int i = production.getIndex(); i < this.productions.size(); ++i) {
            this.productions.get(i).setIndex(i);
        }
    }

    private final void updateCollapsables() {
        if (this.collapsablesUpdated) {
            return;
        }

        this.collapsablesUpdated = true;

        final List<Production> todo = new LinkedList<Production>(this.getProductions());

        while (this.findCollapsables(todo)) {
            // Deliberately left empty
        }
    }

    /**
     * @param todo
     * <br>Not null
     * <br>Input-output
     * @return
     * <br>Range: any boolean
     */
    private final boolean findCollapsables(final List<Production> todo) {
        boolean result = false;
        final Iterator<Production> iterator = todo.iterator();

        while (iterator.hasNext()) {
            final Production production = iterator.next();

            if (this.canProductionCollapse(production)) {
                this.collapsables.add(production.getNonterminal());

                iterator.remove();

                result = true;
            }
        }

        return result;
    }

    /**
     * @param production
     * <br>Not null
     * @return
     * <br>Range: any boolean
     */
    private final boolean canProductionCollapse(final Production production) {
        return this.canCollapse(production.getNonterminal()) ||
                this.countCollapsableSymbols(production.getDevelopment()) == production.getDevelopmentSymbolCount();
    }

    /**
     * @param development
     * <br>Not null
     * @return
     * <br>Range: <code>[0 .. development.size()]</code>
     */
    private final int countCollapsableSymbols(final List<Object> development) {
        int result = 0;

        for (final Object symbol : development) {
            if (this.hasTerminal(symbol)) {
                break;
            } else if (this.canCollapse(symbol)) {
                ++result;
            }
        }

        return result;
    }

    private final void updateFirsts() {
        if (this.firstsUpdated) {
            return;
        }

        this.firstsUpdated = true;

        this.initializeTerminalsFirsts();
        this.initializeNonterminalsFirsts();
        this.collectTerminalsAndNonterminalsAsFirsts();
        this.replaceNonterminalFirsts();
    }

    private final void initializeTerminalsFirsts() {
        for (final Object terminal : this.getTerminals()) {
            this.getOrCreateFirsts(terminal).add(terminal);
        }
    }

    private final void initializeNonterminalsFirsts() {
        for (final Object terminal : this.getNonterminals()) {
            this.getOrCreateFirsts(terminal);
        }
    }

    private final void collectTerminalsAndNonterminalsAsFirsts() {
        for (final Production production : this.getProductions()) {
            for (final Object symbol : production.getDevelopment()) {
                this.getOrCreateFirsts(production.getNonterminal()).add(symbol);

                if (!this.canCollapse(symbol)) {
                    break;
                }
            }
        }
    }

    private final void replaceNonterminalFirsts() {
        for (final Set<Object> firsts : this.firstMap.values()) {
            final Set<Object> done = new LinkedHashSet<Object>();
            final List<Object> todo = new LinkedList<Object>(firsts);

            while (!todo.isEmpty()) {
                final Object symbol = todo.remove(0);

                if (this.hasTerminal(symbol)) {
                    firsts.add(symbol);
                } else if (!done.contains(symbol)) {
                    done.add(symbol);
                    todo.addAll(this.getFirsts(symbol));
                }
            }

            firsts.removeAll(done);
        }
    }

    /**
     * @param symbol
     * <br>Maybe null
     * <br>May become strong reference
     * @return
     * <br>Not null
     * <br>Maybe new
     * <br>Strong reference
     */
    private final Set<Object> getOrCreateFirsts(final Object symbol) {
        Set<Object> result = this.firstMap.get(symbol);

        if (result == null) {
            result = new LinkedHashSet<Object>();

            this.firstMap.put(symbol, result);
        }

        return result;
    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>May become strong reference
     */
    private final void maybeInitializeProductionMap(final Object nonterminal) {
        if (this.getProductionMap().isEmpty()) {
            this.getProductionMap().put(INITIAL_NONTERMINAL, new ArrayList<Production>());
            this.addProduction(INITIAL_NONTERMINAL, nonterminal);
        }
    }

//    /**
//     * @param nonterminal
//     * <br>Maybe null
//     * <br>Will become strong reference
//     * @param development
//     * <br>Not null
//     * <br>Will become strong reference
//     * @return
//     * <br>Not null
//     * <br>New
//     * <br>Strong reference
//     */
//    private final Production newProduction(final Object nonterminal, final List<Object> development) {
//        final Production result = this.new Production(this.getProductions().size(), nonterminal, development);
//
//        this.productions.add(result);
//        this.nonterminals.add(nonterminal);
//        this.terminals.addAll(result.getDevelopment());
//        this.terminals.removeAll(this.getNonterminals());
//
//        return result;
//    }

    /**
     * @param nonterminal
     * <br>Maybe null
     * <br>Will become strong reference
     * @param development
     * <br>Not null
     * <br>Will become strong reference
     * @return
     * <br>Not null
     * <br>New
     * <br>Strong reference
     */
    private final Production newProduction(final Object nonterminal, final Object[] development) {
        final Production result = this.new Production(this.getProductions().size(), nonterminal, development);

        this.productions.add(result);
        this.nonterminals.add(nonterminal);
        this.terminals.addAll(result.getDevelopment());
        this.terminals.removeAll(this.getNonterminals());

        return result;
    }

    /**
     *
     * @return
     * <br>Not null
     * <br>Shared
     */
    private final Map<Object, List<Production>> getProductionMap() {
        return this.productionMap;
    }

    /**
     * @author codistmonk (creation 2010-10-04)
     */
    public final class Production implements Serializable {

        private int index;

        private final Object nonterminal;

        private final Object[] originalDevelopment;

        private final List<Object> development;

        /**
         *
         * @param index
         * <br>Range: {@code [0 .. Integer.MAX_VALUE]}
         * @param nonterminal
         * <br>Maybe null
         * <br>Will be strong reference
         * @param originalDevelopment
         * <br>Not null
         */
        public Production(final int index, final Object nonterminal, final Object[] originalDevelopment) {
            this.index = index;
            this.nonterminal = nonterminal;
            this.originalDevelopment = originalDevelopment.clone();
            this.development = new ArrayList<Object>(this.getOriginalDevelopment().length);

            this.resetDevelopment();
        }

        /**
         * @return
         * <br>Not null
         * <br>Strong reference
         */
        final Object[] getOriginalDevelopment() {
            return this.originalDevelopment;
        }

        private final void resetDevelopment() {
            this.getDevelopment().clear();

            for (final Object symbol : this.getOriginalDevelopment()) {
                if (!(symbol instanceof Epsilon)) {
                    this.getDevelopment().add(symbol);
                }
            }
        }

        /**
         * @return
         * <br>Range: any short
         */
        public final short getPriority() {
            short result = Short.MIN_VALUE;

            for (final Object symbol : this.getDevelopment()) {
                if (Grammar.this.hasTerminal(symbol)) {
                    final Short priority = Grammar.this.getPriorities().get(symbol);

                    if (priority != null) {
                        result = (short) Math.max(result, priority);
                    }
                }
            }

            return result;
        }

        /**
         *
         * @return
         * <br>Not null
         * <br>Shared
         */
        public final List<Object> getDevelopment() {
            return this.development;
        }

        /**
         *
         * @return
         * <br>Range: {@code [0 .. Integer.MAX_VALUE]}
         */
        public final int getIndex() {
            return this.index;
        }

        /**
         *
         * @return
         * <br>Maybe null
         * <br>Shared
         */
        public final Object getNonterminal() {
            return this.nonterminal;
        }

        /**
         *
         * @return
         * <br>Range: { this.getDevelopment().size() }
         */
        public final int getDevelopmentSymbolCount() {
            return this.getDevelopment().size();
        }

        @Override
        public final String toString() {
            return this.getNonterminal() + " -> " + Arrays.toString(this.getOriginalDevelopment());
        }

        /**
         * @param index
         * <br>Range: {@code [0 .. Integer.MAX_VALUE]}
         */
        final void setIndex(final int index) {
            this.index = index;
        }

        /**
         * @param index
         * <br>Range: <code>[0 .. this.getSymbolCount() - 1
         * @return
         * <br>Not null
         * <br>New
         */
        final Object[] newOriginalDevelopmentWithEpsilon(final int index) {
            final Object[] result = this.getOriginalDevelopment().clone();

            result[index] = Epsilon.castOrCreate(result[index]);

            return result;
        }

        /**
         * @param nonterminal
         * <br>Maybe null
         * @return
         * <br>Not null
         * <br>New
         */
        final Object[] newOriginalDevelopmentWithEpsilon(final Object nonterminal) {
            final Object[] result = this.getOriginalDevelopment().clone();

            for (int i = 0; i < result.length; ++i) {
                if (Tools.equals(nonterminal, result[i])) {
                    result[i] = Epsilon.castOrCreate(result[i]);
                }
            }

            return result;
        }

        /**
         * {@value}.
         */
        private static final long serialVersionUID = 1002200991414988497L;

    }

    /**
     * {@value}.
     */
    private static final long serialVersionUID = 8965715477317349325L;

    /**
     * @author codistmonk (creation 2010-10-04)
     */
    public static enum SpecialSymbol {

        INITIAL_NONTERMINAL, END_TERMINAL;

    }

    /**
     * @author codistmonk (creation 2011-05-25)
     */
    public static interface Regular {

        /**
         * @param grammar
         * <br>Not null
         * <br>Input-output
         * @return
         * <br>Maybe null
         * <br>Maybe new
         * <br>Strong reference
         */
        public abstract Object getOrCreateSymbol(Grammar grammar);

    }

    /**
     * @author codistmonk (creation 2011-05-25)
     */
    public static final class RegularSymbol implements Regular {

        private final Object symbol;

        /**
         * @param symbol
         * <br>Maybe null
         * <br>Will be strong reference
         */
        public RegularSymbol(final Object symbol) {
            this.symbol = symbol;
        }

        /**
         * @return
         * <br>Maybe null
         * <br>Strong reference
         */
        public final Object getSymbol() {
            return this.symbol;
        }

        @Override
        public final Object getOrCreateSymbol(final Grammar grammar) {
            ignore(grammar);

            return this.getSymbol();
        }

    }

    /**
     * @author codistmonk (creation 2011-05-25)
     */
    public static abstract class AbstractRegularGroup implements Regular {

        private final Regular[] regulars;

        /**
         * @param regulars
         * <br>Not null
         * <br>Will be strong reference
         */
        public AbstractRegularGroup(final Regular... regulars) {
            this.regulars = regulars;
        }

        /**
         * @return
         * <br>Not null
         * <br>Strong reference
         */
        public final Regular[] getRegulars() {
            return this.regulars;
        }
    }

    /**
     * @author codistmonk (creation 2011-05-25)
     */
    public static final class RegularSequence extends AbstractRegularGroup {

        /**
         * @param regulars
         * <br>Not null
         * <br>Will be strong reference
         */
        public RegularSequence(final Regular... regulars) {
            super(regulars);
        }

        @Override
        public final Object getOrCreateSymbol(final Grammar grammar) {
            final Object result = new Object();
            final Object[] development = new Object[this.getRegulars().length];

            for (int i = 0; i < development.length; ++i) {
                development[i] = this.getRegulars()[i].getOrCreateSymbol(grammar);
            }

            grammar.addProduction(result, development);

            return result;
        }

    }

    /**
     * @author codistmonk (creation 2011-05-25)
     */
    public static final class RegularUnion extends AbstractRegularGroup {

        /**
         * @param regulars
         * <br>Not null
         * <br>Will be strong reference
         */
        public RegularUnion(final Regular... regulars) {
            super(regulars);
        }

        @Override
        public final Object getOrCreateSymbol(final Grammar grammar) {
            final Object result = new Object();

            for (final Regular regular : this.getRegulars()) {
                grammar.addProduction(result, regular.getOrCreateSymbol(grammar));
            }

            return result;
        }

    }

    /**
     * @author codistmonk (creation 2011-05-25)
     */
    public static abstract class AbstractRegularRepetition implements Regular {

        private final Regular repeatedRegular;

        /**
         * @param repeatedRegular
         * <br>Not null
         * <br>Will be strong reference
         */
        public AbstractRegularRepetition(final Regular repeatedRegular) {
            this.repeatedRegular = repeatedRegular;
        }

        /**
         * @return
         * <br>Not null
         * <br>Strong reference
         */
        public final Regular getRepeatedRegular() {
            return this.repeatedRegular;
        }

    }

    /**
     * @author codistmonk (creation 2011-05-25)
     */
    public static final class RegularFiniteRepetition extends AbstractRegularRepetition {

        private final int count;

        /**
         * @param count
         * <br>Range: <code>[0 .. Integer.MAX_VALUE]</code>
         * @param repeatedRegular
         * <br>Not null
         * <br>Will be strong reference
         */
        public RegularFiniteRepetition(final int count, final Regular repeatedRegular) {
            super(repeatedRegular);
            this.count = count;
        }

        /**
         * @return
         * <br>Range: <code>[0 .. Integer.MAX_VALUE]</code>
         */
        public final int getCount() {
            return this.count;
        }

        @Override
        public final Object getOrCreateSymbol(final Grammar grammar) {
            final Object result = new Object();
            final Object[] development = new Object[this.getCount()];

            Arrays.fill(development, this.getRepeatedRegular().getOrCreateSymbol(grammar));

            grammar.addProduction(result, development);

            return result;
        }

    }

    /**
     * @author codistmonk (creation 2011-05-25)
     */
    public static final class RegularInfiniteRepetition extends AbstractRegularRepetition {

        /**
         * @param repeatedRegular
         * <br>Not null
         * <br>Will be strong reference
         */
        public RegularInfiniteRepetition(final Regular repeatedRegular) {
            super(repeatedRegular);
        }

        @Override
        public final Object getOrCreateSymbol(final Grammar grammar) {
            final Object result = new Object();

            grammar.addProduction(result, result, this.getRepeatedRegular().getOrCreateSymbol(grammar));
            grammar.addProduction(result);

            return result;
        }

    }

    /**
     * @author codistmonk (creation 2001-07-08)
     */
    public static final class AmbiguousGrammarException extends RuntimeException {

        /**
         * @param message
         * <br>Not null
         * <br>Will become strong reference
         */
        public AmbiguousGrammarException(final String message) {
            super(message);
        }

        /**
         * {@value}.
         */
        private static final long serialVersionUID = 172176626186227517L;

    }

    /**
     * @author codistmonk (creation 2010-10-07)
     */
    static enum UnmodifiableKey {

        NONTERMINALS, TERMINALS, PRODUCTIONS;

    }

}
