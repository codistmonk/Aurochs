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
import static net.sourceforge.aurochs.Grammar.SpecialSymbol.*;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.aprog.events.AbstractObservable;
import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs.LRTable.*;

/**
 * @author codistmonk (creation 2011-09-08)
 */
public abstract class AbstractLRParser extends AbstractObservable<AbstractLRParser.Listener> {

    private final LRTable table;

    private final Deque<Object> inputTokens;

    private final Stack stack;

    private Iterator<Object> input;

    /**
     *
     * @param table
     * <br>Not null
     * <br>Will become reference
     */
    protected AbstractLRParser(final LRTable table) {
        this.table = table;
        this.inputTokens = new LinkedList<Object>();
        this.stack = new Stack();
    }

    /**
     * @param input
     * <br>Not null
     * <br>Input-output
     * <br>Will become reference
     */
    @SuppressWarnings("unchecked")
    public final void prepareToPerformFirstOperation(final Iterator<?> input) {
        this.inputTokens.clear();
        this.stack.reset();
        this.input = (Iterator<Object>) input;
    }

    /**
     * @param input
     * <br>Not null
     * <br>Input-output
     * <br>Will become reference
     * @return
     * <br>Range: any boolean
     */
    public final boolean parse(final Iterator<?> input) {
        final boolean debug = false;

        this.prepareToPerformFirstOperation(input);

        do {
            // <editor-fold defaultstate="collapsed" desc="DEBUG">
            if (debug) {
                debugPrint("stack:", this.getStack(), "stateIndex:", this.getStateIndex(), "inputSymbol:", this.getInputSymbol());
            }
            // </editor-fold>
        } while (this.performNextOperation());// && !this.isDone());

        return this.isMatchFound();
    }

    /**
     * @return
     * <br>Range: any boolean
     */
    public final boolean isMatchFound() {
        return INITIAL_NONTERMINAL.equals(this.getInputSymbol()) && !this.input.hasNext();
    }

    /**
     * @return
     * <br>Range: any boolean
     */
    public final boolean isDone() {
        return this.isMatchFound() || this.getOperation(this.getInputSymbol()) == null;
    }

    /**
     *
     * @return
     * <br>Not null
     * <br>Shared
     */
    public final LRTable getTable() {
        return this.table;
    }

    /**
     *
     * @param token
     * <br>Maybe null
     * <br>Shared
     */
    public final void insertInputToken(final Object token) {
        this.inputTokens.push(token);
    }

    /**
     *
     * @return
     * <br>Maybe null
     * <br>Shared
     */
    public final Object getInputSymbol() {
        if (this.inputTokens.isEmpty()) {
            this.insertInputToken(this.input.hasNext() ? this.input.next() : END_TERMINAL);
        }

        return GeneratedToken.getSymbol(this.inputTokens.peek());
    }

    /**
     *
     * @return
     * <br>Not null
     * <br>Shared
     */
    public final GeneratedToken getInputNonterminalToken() {
        return (GeneratedToken) this.inputTokens.peek();
    }

    /**
     *
     * @return
     * <br>Maybe null
     * <br>Shared
     */
    public final Object takeInputToken() {
        this.getInputSymbol();

        return this.inputTokens.pop();
    }

    /**
     *
     * @return
     * <br>Not null
     * <br>Shared
     */
    public final Stack getStack() {
        return this.stack;
    }

    /**
     *
     * @return
     * <br>Range: {@code [0 .. this.table.getStates().size() - 1]}
     */
    public final int getStateIndex() {
        return this.getStack().peek();
    }

    /**
     *
     * @param symbol
     * <br>Maybe null
     * @return
     * <br>Maybe null
     * <br>Shared
     */
    public final Operation getOperation(final Object symbol) {
        return this.getTable().getStates().get(this.getStateIndex()).get(symbol);
    }

    /**
     * {@link #prepareToPerformFirstOperation(java.util.Iterator)} must be called before trying to perform the first operation.
     * @return
     * <br>Range: any boolean
     */
    public final boolean performNextOperation() {
        Operation operation = this.getOperation(this.getInputSymbol());

        if (operation == null) {
            this.new UnexpectedSymbolErrorEvent().fire();

            // In case error recovery was done by listeners
            operation = this.getOperation(this.getInputSymbol());
        }

        return this.dispatch("perform", operation);
    }

    /**
     *
     * @param methodName
     * <br>Not null
     * @param operation
     * <br>Maybe null
     * @return {@code false} if {@code operation} is {@code null}
     * <br>Range: any boolean
     */
    private final boolean dispatch(final String methodName, final Operation operation) {
        if (operation == null) {
            return false;
        }

        try {
            AbstractLRParser.class.getDeclaredMethod(methodName, operation.getClass()).invoke(this, operation);

            return true;
        } catch (final Exception exception) {
            throw unchecked(exception);
        }
    }

    /**
     *
     * @param shift
     * <br>Not null
     */
    private final void perform(final Shift shift) {
        this.getStack().push(this.takeInputToken(), shift.getNextStateIndex());
    }

    /**
     *
     * @param reduce
     * <br>Not null
     */
    private final void perform(final Reduction reduction) {
        this.insertInputToken(new GeneratedToken(reduction.getProduction().getNonterminal()));

        this.new ReductionEvent(reduction).fire();
    }

    /**
     * @author codistmonk (creation 2010-10-05)
     */
    public static interface Listener {

        /**
         *
         * @param event
         * <br>Not null
         */
        public abstract void reductionOccured(final ReductionEvent event);

        /**
         *
         * @param event
         * <br>Not null
         */
        public abstract void unexpectedSymbolErrorOccured(final UnexpectedSymbolErrorEvent event);

    }

    /**
     * @author codistmonk (creation 2010-10-05)
     */
    public abstract class AbstractEvent extends AbstractObservable<Listener>.AbstractEvent<AbstractLRParser, Listener> {
        // Deliberately left empty
    }

    /**
     * @author codistmonk (creation 2010-10-05)
     */
    public final class ReductionEvent extends AbstractEvent {

        private final Reduction reduction;

        private final GeneratedToken generatedToken;

        private final List<Object> tokens;

        /**
         *
         * @param reduction
         * <br>Not null
         * <br>Shared
         */
        public ReductionEvent(final Reduction reduction) {
            this.reduction = reduction;
            this.generatedToken = AbstractLRParser.this.getInputNonterminalToken();
            this.tokens = AbstractLRParser.this.getStack().pop(reduction.getProduction().getDevelopmentSymbolCount());
        }

        /**
         *
         * @return
         * <br>Not null
         * <br>Shared
         */
        public final GeneratedToken getGeneratedToken() {
            return this.generatedToken;
        }

        /**
         *
         * @return
         * <br>Not null
         * <br>Shared
         */
        public Reduction getReduction() {
            return this.reduction;
        }

        /**
         *
         * @return
         * <br>Not null
         * <br>Shared
         */
        public final List<Object> getTokens() {
            return this.tokens;
        }

        @Override
        protected final void notifyListener(final Listener listener) {
            listener.reductionOccured(this);
        }

    }

    /**
     * @author codistmonk (creation 2010-10-05)
     */
    public final class UnexpectedSymbolErrorEvent extends AbstractEvent {

        @Override
        protected final void notifyListener(final Listener listener) {
            listener.unexpectedSymbolErrorOccured(this);
        }

    }

    /**
     * @author codistmonk (creation 2010-10-07)
     */
    public static final class Stack {

        private final List<Object> tokens;

        private final List<Integer> stateIndices;

        public Stack() {
            this.tokens = new LinkedList<Object>();
            this.stateIndices = new LinkedList<Integer>();

            this.reset();
        }

        public final void reset() {
            this.tokens.clear();
            this.stateIndices.clear();

            this.push(null, 0);
        }

        /**
         *
         * @param token
         * <br>Maybe null
         * <br>Shared
         * @param stateIndex
         * <br>Range: any integer
         */
        public final void push(final Object token, final int stateIndex) {
            this.tokens.add(token);
            this.stateIndices.add(stateIndex);
        }

        /**
         *
         * @param elementCount
         * <br>Range: any integer
         * @return The top {@code elementCount} tokens in the same order they were added
         * <br>Not null
         * <br>New
         */
        public final List<Object> pop(final int elementCount) {
            final int end = this.getElementCount();
            final int start = end - elementCount;
            final List<Object> tail = this.tokens.subList(start, end);
            final List<Object> result = new ArrayList<Object>(tail);

            tail.clear();
            this.stateIndices.subList(start, end).clear();

            return result;
        }

        /**
         *
         * @return The top state index
         * <br>Range: any integer
         */
        public final int peek() {
            return this.stateIndices.get(this.getElementCount() - 1);
        }

        /**
         *
         * @return
         * <br>Range: {@code [0 .. Integer.MAX_VALUE]}
         */
        public final int getElementCount() {
            return this.tokens.size();
        }

        @Override
        public final String toString() {
            return this.tokens.toString();
        }

    }

    /**
     * @author codistmonk (2010-10-07)
     */
    public static final class GeneratedToken {

        private final Object symbol;

        private Object userObject;

        /**
         *
         * @param symbol
         * <br>Maybe null
         * <br>Will become reference
         */
        public GeneratedToken(final Object symbol) {
            this.symbol = symbol;
        }

        @Override
        public final boolean equals(final Object object) {
            final GeneratedToken that = cast(this.getClass(), object);

            return that != null && Tools.equals(this.getSymbol(), that.getSymbol()) || Tools.equals(this.getSymbol(), object);
        }

        @Override
        public final int hashCode() {
            return Tools.hashCode(this.getSymbol());
        }

        /**
         *
         * @return
         * <br>Maybe null
         * <br>Reference
         */
        public final Object getSymbol() {
            return this.symbol;
        }

        /**
         *
         * @param <T> The expected user object type
         * @return
         * <br>Maybe null
         * <br>Reference
         */
        @SuppressWarnings("unchecked")
        public final <T> T getUserObject() {
            return (T) this.userObject;
        }

        /**
         *
         * @param userObject
         * <br>Maybe null
         * <br>Will become reference
         */
        public final void setUserObject(final Object userObject) {
            this.userObject = userObject;
        }

        @Override
        public String toString() {
            return this.getSymbol() + "(" + this.getUserObject() + ")";
        }

        /**
         *
         * @param token
         * <br>Maybe null
         * @return
         * <br>Maybe null
         */
        public static final Object getSymbol(final Object token) {
            final GeneratedToken generatedToken = cast(GeneratedToken.class, token);

            return generatedToken != null ? generatedToken.getSymbol() : token;
        }

    }

}
