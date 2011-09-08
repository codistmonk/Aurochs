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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.aprog.events.AbstractObservable;

import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs.Grammar.Production;

/**
 * @author codistmonk (creation 2010-10-05)
 */
public final class LRTable extends AbstractObservable<LRTable.Listener> implements Serializable {

    private final Grammar grammar;

    private final List<Map<Object, Operation>> states;

    /**
     * Each element of this list is a collection of paths leading to the corresponding state.
     * <br>A path is a sequence of input symbols.
     * <br>This information is used to generate detailed error messages to help resolve conflicts.
     */
    private final List<ArraySet<List<Object>>> paths;

    /**
     * @param grammar
     * <br>Not null
     * <br>Will be strong reference
     */
    public LRTable(final Grammar grammar) {
        this.grammar = grammar;
        this.states = new ArrayList<Map<Object, Operation>>();
        this.paths = new ArrayList<ArraySet<List<Object>>>();

        this.getOrCreatePaths(0).add(new ArrayList<Object>());
    }

    /**
     * @return
     * <br>Range: <code>[0 .. Integer.MAX_VALUE]</code>
     */
    public final int getStateCount() {
        return this.states.size();
    }

    /**
     * @param stateIndex
     * <br>Range: <code>[0 .. this.getStateCount() - 1]</code>
     * @return
     * <br>Not null
     * <br>Strong reference
     */
    public final Set<List<Object>> getPathsToState(final int stateIndex) {
        return this.getOrCreatePaths(stateIndex);
    }

    /**
     * @param originStateIndex
     * <br>Range: {@code [0 .. Integer.MAX_VALUE]}
     * @param symbol
     * <br>Maybe null
     * <br>Will be strong reference
     * @param destinationClosure
     * <br>Not null
     */
    public final void addShift(final int originStateIndex, final Object symbol, final LALR1ClosureTable.Closure destinationClosure) {
        final int destinationStateIndex = destinationClosure.getKernel().getIndex();

        this.addOperation(originStateIndex, symbol, this.new Shift(destinationStateIndex, destinationClosure.getPriority()));
        this.addPaths(originStateIndex, symbol, destinationStateIndex);
    }

    /**
     *
     * @param originStateIndex
     * <br>Range: {@code [0 .. Integer.MAX_VALUE]}
     * @param symbol
     * <br>Maybe null
     * <br>Shared
     * @param productionIndex
     * <br>Range: {@code [0 .. this.getGrammar().getProductions().size() - 1]}
     */
    public final void addReduction(final int originStateIndex, final Object symbol, final int productionIndex) {
        this.addOperation(originStateIndex, symbol, this.new Reduction(productionIndex));
    }

    /**
     * @return
     * <br>Not null
     * <br>Strong reference
     */
    public final Grammar getGrammar() {
        return this.grammar;
    }

    /**
     *
     * @return
     * <br>Not null
     * <br>Shared
     */
    public final List<Map<Object, Operation>> getStates() {
        return this.states;
    }

    /**
     * @param stateIndex
     * <br>Range: <code>[0 .. Integre.MAX_VALUE]</code>
     * @return
     * <br>Not null
     * <br>Maybe new
     * <br>Strong reference
     */
    private final ArraySet<List<Object>> getOrCreatePaths(final int stateIndex) {
        while (this.paths.size() <= stateIndex) {
            final ArraySet<List<Object>> pathsForState = new ArraySet<List<Object>>();

            pathsForState.add(new ArrayList<Object>());

            this.paths.add(pathsForState);
        }

        return this.paths.get(stateIndex);
    }

    /**
     * @param originStateIndex
     * <br>Range: {@code [0 .. Integer.MAX_VALUE]}
     * @param symbol
     * <br>Maybe null
     * <br>Shared
     * @param destinationStateIndex
     * <br>Range: {@code [0 .. Integer.MAX_VALUE]}
     */
    private final void addPaths(final int originStateIndex, final Object symbol, final int destinationStateIndex) {
        final boolean debug = false; // TODO Decide whether or not all paths must be computed (slower)
        final Set<List<Object>> destinationPaths = this.getOrCreatePaths(destinationStateIndex);

        if (debug) {
            final ArraySet<List<Object>> originPaths = this.getOrCreatePaths(originStateIndex);

            for (int i = originPaths.size() - 1; i >= 0; --i) {
                addNewPath(destinationPaths, originPaths.get(i), symbol);
            }
        } else if (destinationPaths.isEmpty()) {
            Tools.debugPrint(originStateIndex, symbol, destinationStateIndex);
            addNewPath(destinationPaths, this.getOrCreatePaths(originStateIndex).iterator().next(), symbol);
        }
    }

    /**
     * @param destinationPaths
     * <br>Not null
     * <br>Input-output
     * @param originPath
     * <br>Not null
     * @param symbol
     * <br>Maybe null
     * <br>Will become a strong reference
     */
    private static final void addNewPath(final Set<List<Object>> destinationPaths, final List<Object> originPath, final Object symbol) {
        final List<Object> newPath = new ArrayList<Object>(originPath.size() + 1);

        newPath.addAll(originPath);
        newPath.add(symbol);

        destinationPaths.add(newPath);
    }

    /**
     * @param originStateIndex
     * <br>Range: {@code [0 .. Integer.MAX_VALUE]}
     * @param symbol
     * <br>Maybe null
     * <br>Will become strong reference
     * @param operation
     * <br>Not null
     * <br>May become strong reference
     * @throws IllegalStateException If there is a conflict
     */
    private final void addOperation(final int originStateIndex, final Object symbol, final Operation operation) {
        final Map<Object, Operation> transitions = this.getOrCreateState(originStateIndex);
        final BeforeOperationAddedEvent event = this.new BeforeOperationAddedEvent(
                originStateIndex, symbol, operation, transitions.get(symbol), operation);

        event.maybeResolveConflict();
        event.fire();
        transitions.put(symbol, event.getResolution());
    }

    /**
     *
     * @param originStateIndex
     * <br>Range: <code>[0 .. Integer.MAX_VALUE]</code>
     * @return
     * <br>Not null
     * <br>Maybe new
     * <br>Strong reference
     */
    private final Map<Object, Operation> getOrCreateState(final int originStateIndex) {
        while (this.getStates().size() <= originStateIndex) {
            this.getStates().add(new LinkedHashMap<Object, Operation>());
        }

        return this.getStates().get(originStateIndex);
    }

    /**
     * @author codistmonk (creation 2010-10-05)
     */
    public interface Operation extends Serializable {
        
        /**
         * @return
         * <br>Range: any short
         */
        public abstract short getPriority();

    }

    /**
     * @author codistmonk (creation 2010-10-05)
     */
    public final class Shift implements Operation {

        private final int nextStateIndex;

        private final short priority;

        /**
         *
         * @param nextStateIndex
         * <br>Range: {@code [0 .. LRTable.this.getStates().size() - 1]}
         * @param priority
         * <br>Range: any short
         */
        Shift(final int nextStateIndex, final short priority) {
            this.nextStateIndex = nextStateIndex;
            this.priority = priority;
        }

        @Override
        public final short getPriority() {
            return this.priority;
        }

        /**
         *
         * @return
         * <br>Range: {@code [0 .. LRTable.this.getStates().size() - 1]}
         */
        public int getNextStateIndex() {
            return this.nextStateIndex;
        }

        @Override
        public final String toString() {
            return "s" + this.getNextStateIndex() + "(" + this.getPriority() + ")";
        }

        /**
         * {@value}.
         */
        private static final long serialVersionUID = 8871194483278294292L;

    }

    /**
     * @author codistmonk (creation 2010-10-05)
     */
    public final class Reduction implements Operation {

        private final int productionIndex;

        /**
         *
         * @param productionIndex
         * <br>Range: {@code [0 .. LRTable.this.getGrammar().getProductionList().size() - 1]}
         */
        Reduction(final int productionIndex) {
            this.productionIndex = productionIndex;
        }

        @Override
        public final short getPriority() {
            return this.getProduction().getPriority();
        }

        /**
         *
         * @return
         * <br>Range: {@code [0 .. LRTable.this.getGrammar().getProductionList().size() - 1]}
         */
        public final int getProductionIndex() {
            return this.productionIndex;
        }

        /**
         *
         * @return
         * <br>Not null
         * <br>Shared
         */
        public final Production getProduction() {
            return LRTable.this.getGrammar().getProduction(this.getProductionIndex());
        }

        @Override
        public final String toString() {
            return "r" + this.getProductionIndex() + "(" + this.getPriority() + ")";
        }

        /**
         * {@value}.
         */
        private static final long serialVersionUID = -3217753113718838167L;

    }

    /**
     * An instance of this class is created before each cell modification.
     * <br><code>newOperation</code> is the operation that the algorithm wants to put in the cell.
     * <br>If <code>oldOperation</code> is not null, then there is a conflict.
     * <br>The final value of <code>resolution</code> will always be used to fill the cell.
     * <br>If <code>resolution</code> is null, then there is a problem (eg unresolved conflict).
     *
     * @author codistmonk (creation 2011-06-08)
     */
    public final class BeforeOperationAddedEvent extends AbstractEvent<LRTable, Listener> {

        private final int stateIndex;

        private final Object symbol;

        private final Operation newOperation;

        private final Operation oldOperation;

        private Operation resolution;

        /**
         * @param stateIndex
         * <br>Range: <code>[0 .. this.getTable().getStateCount() - 1]</code>
         * @param symbol
         * <br>Maybe new
         * <br>Strong reference
         * @param newOperation
         * <br>Not null
         * <br>Strong reference
         * @param oldOperation
         * <br>Maybe null
         * <br>Strong reference
         * @param resolution
         * <br>Maybe null
         * <br>Strong reference
         */
        public BeforeOperationAddedEvent(final int stateIndex, final Object symbol, final Operation newOperation,
                final Operation oldOperation, final Operation resolution) {
            this.stateIndex = stateIndex;
            this.symbol = symbol;
            this.newOperation = newOperation;
            this.oldOperation = oldOperation;
            this.resolution = resolution;
        }

        @Override
        protected final void notifyListener(final Listener listener) {
            listener.beforeOperationAdded(this);
        }

        /**
         * @return
         * <br>Not null
         */
        public final LRTable getTable() {
            // XXX Giving public access to the table allows to add operations
            // while processing events, which may lead to other events being
            // generated; is it ok?
            return LRTable.this;
        }

        /**
         * @return
         * <br>Not null
         * <br>Strong reference
         */
        public final Set<List<Object>> getPathsToState() {
            return LRTable.this.getPathsToState(this.getStateIndex());
        }

        /**
         * @return
         * <br>Not null
         * <br>Strong reference
         */
        public final Operation getNewOperation() {
            return this.newOperation;
        }

        /**
         * @return
         * <br>Maybe null
         * <br>Strong reference
         */
        public final Operation getOldOperation() {
            return this.oldOperation;
        }

        /**
         * @return
         * <br>Range: <code>[0 .. this.getTable().getStateCount() - 1]</code>
         */
        public final int getStateIndex() {
            return this.stateIndex;
        }

        /**
         * @return
         * <br>Maybe null
         * <br>Strong reference
         */
        public final Object getSymbol() {
            return this.symbol;
        }

        /**
         * @return
         * <br>Maybe null
         * <br>Strong reference
         */
        public final Operation getResolution() {
            return this.resolution;
        }

        /**
         * @param resolution
         * <br>Maybe null
         * <br>Will be a strong reference
         */
        public final void setResolution(final Operation resolution) {
            this.resolution = resolution;
        }

        public final void maybeResolveConflict() {
            final boolean debug = false;

            // <editor-fold defaultstate="collapsed" desc="DEBUG">
            if (debug) {
                Tools.debugPrint(this.getStateIndex() + "[" + this.getSymbol() + "]",
                        this.getOldOperation(), this.getNewOperation(), this.getPathsToState());
            }
            // </editor-fold>

            if (this.getOldOperation() != null) {
                if (this.getOldOperation().getPriority() == this.getNewOperation().getPriority()) {
                    if (this.isLeftAssociativeBinaryOperator(this.getSymbol())) {
                        this.setResolution(this.getOldOperation());
                    } else if (this.isRightAssociativeBinaryOperator(this.getSymbol())) {
                        // Do nothing because the new transition is correct
                    } else {
                        this.setResolution(null);
                    }
                } else if (this.getOldOperation().getPriority() > this.getNewOperation().getPriority()) {
                    this.setResolution(this.getOldOperation());
                } else {
                    // Do nothing because the new transition is correct
                }
            }
        }

        /**
         * @param symbol
         * <br>maybe null
         * @return
         * <br>Range: any boolean
         */
        private final boolean isLeftAssociativeBinaryOperator(final Object symbol) {
            return this.getTable().getGrammar().getLeftAssociativeBinaryOperators().contains(symbol);
        }

        /**
         * @param symbol
         * <br>maybe null
         * @return
         * <br>Range: any boolean
         */
        private final boolean isRightAssociativeBinaryOperator(final Object symbol) {
            return this.getTable().getGrammar().getRightAssociativeBinaryOperators().contains(symbol);
        }

    }

    /**
     * {@value}.
     */
    private static final long serialVersionUID = -5304295580997974647L;

    /**
     * @author codistmonk (creation 2011-06-08)
     */
    public static interface Listener {

        /**
         * @param event
         * <br>Not null
         */
        public abstract void beforeOperationAdded(final BeforeOperationAddedEvent event);

    }

}
