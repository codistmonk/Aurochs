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

import static org.junit.Assert.*;

import static net.sourceforge.aprog.tools.Tools.*;
import static net.sourceforge.aurochs.GrammarTest.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.aurochs.LALR1ClosureTable.Closure;
import net.sourceforge.aurochs.LALR1ClosureTable.Item;
import net.sourceforge.aurochs.LALR1ClosureTable.Kernel;

import org.junit.Before;
import org.junit.Test;

/**
 * @author codistmonk (creation 2010-10-05)
 */
public final class LALR1ClosureTableTest {

    private Grammar grammar;

    private LALR1ClosureTable closureTable;

    private List<Integer> kernelIndices;

    @Before
    public final void beforeEachTest() {
        this.grammar = null;
        this.closureTable = null;
        this.kernelIndices = new ArrayList<Integer>();
    }

    @Test
    public final void testClosures0() {
        // (0) _ -> A
        // (1) A -> []
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ rule(nonterminal("A"), development())
        );

        debugPrint("\n" + toString(this.closureTable.getClosures()));

        this.test(
                                   /*new state   0*/ kernel("< _ -> .A, $ >"), closure("< _ -> .A, $ >", "< A -> ., $ >"),
                transition(0, "A", /*new state*/ 1), kernel("< _ -> A., $ >"), closure("< _ -> A., $ >")
        );
    }

    @Test
    public final void testClosures1() {
        // (0) _ -> A
        // (1) A -> b
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ rule(nonterminal("A"), development("b"))
        );

        debugPrint("\n" + toString(this.closureTable.getClosures()));

        this.test(
                                   /*new state   0*/ kernel("< _ -> .A, $ >"), closure("< _ -> .A, $ >", "< A -> .b, $ >"),
                transition(0, "A", /*new state*/ 1), kernel("< _ -> A., $ >"), closure("< _ -> A., $ >"),
                transition(0, "b", /*new state*/ 2), kernel("< A -> b., $ >"), closure("< A -> b., $ >")
        );
    }

    @Test
    public final void testClosures2() {
        // (0) _ -> A
        // (1) A -> bc
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ rule(nonterminal("A"), development("b", "c"))
        );

        debugPrint("\n" + toString(this.closureTable.getClosures()));

        this.test(
                                   /*new state   0*/ kernel("< _ -> .A, $ >"),  closure("< _ -> .A, $ >", "< A -> .bc, $ >"),
                transition(0, "A", /*new state*/ 1), kernel("< _ -> A., $ >"),  closure("< _ -> A., $ >"),
                transition(0, "b", /*new state*/ 2), kernel("< A -> b.c, $ >"), closure("< A -> b.c, $ >"),
                transition(2, "c", /*new state*/ 3), kernel("< A -> bc., $ >"), closure("< A -> bc., $ >")
        );
    }

    @Test
    public final void testClosures3() {
        // (0) _ -> A
        // (1) A -> b
        // (2) A -> c
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ rule(nonterminal("A"), development("b")),
                /*(2)*/ rule(nonterminal("A"), development("c"))
        );

        debugPrint("\n" + toString(this.closureTable.getClosures()));

        this.test(
                                   /*new state   0*/ kernel("< _ -> .A, $ >"), closure("< _ -> .A, $ >", "< A -> .b, $ >", "< A -> .c, $ >"),
                transition(0, "A", /*new state*/ 1), kernel("< _ -> A., $ >"), closure("< _ -> A., $ >"),
                transition(0, "b", /*new state*/ 2), kernel("< A -> b., $ >"), closure("< A -> b., $ >"),
                transition(0, "c", /*new state*/ 3), kernel("< A -> c., $ >"), closure("< A -> c., $ >")
        );
    }

    @Test
    public final void testClosures4() {
        // (0) _ -> A
        // (1) A -> Ab
        // (2) A -> []
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ rule(nonterminal("A"), development("A", "b")),
                /*(2)*/ rule(nonterminal("A"), development())
        );

        debugPrint("\n" + toString(this.closureTable.getClosures()));

        this.test(
                                   /*new state   0*/ kernel("< _ -> .A, $ >"),                      closure("< _ -> .A, $ >", "< A -> .Ab, $/b >", "< A -> ., $/b >"),
                transition(0, "A", /*new state*/ 1), kernel("< _ -> A., $ >", "< A -> A.b, $/b >"), closure("< _ -> A., $ >", "< A -> A.b, $/b >"),
                transition(1, "b", /*new state*/ 2), kernel("< A -> Ab., $/b >"),                   closure("< A -> Ab., $/b >")
        );
    }

    @Test
    public final void testClosures5() {
        // (0) _ -> A
        // (1) A -> bA
        // (2) A -> []
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ rule(nonterminal("A"), development("b", "A")),
                /*(1)*/ rule(nonterminal("A"), development())
        );

        debugPrint("\n" + toString(this.closureTable.getClosures()));

        this.test(
                                   /*new state   0*/ kernel("< _ -> .A, $ >"),  closure("< _ -> .A, $ >", "< A -> .bA, $ >", "< A -> ., $ >"),
                transition(0, "A", /*new state*/ 1), kernel("< _ -> A., $ >"),  closure("< _ -> A., $ >"),
                transition(0, "b", /*new state*/ 2), kernel("< A -> b.A, $ >"), closure("< A -> b.A, $ >", "< A -> .bA, $ >", "< A -> ., $ >"),
                transition(2, "A", /*new state*/ 3), kernel("< A -> bA., $ >"), closure("< A -> bA., $ >"),
                transition(2, "b", /*existing state*/ 2)
        );
    }

    @Test
    public final void testClosures6() {
        // (0) _ -> A
        // (1) A -> Ab
        // (2) A -> b
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ rule(nonterminal("A"), development("A", "b")),
                /*(2)*/ rule(nonterminal("A"), development("b"))
        );

        debugPrint("\n" + toString(this.closureTable.getClosures()));

        this.test(
                                   /*new state   0*/ kernel("< _ -> .A, $ >"),                      closure("< _ -> .A, $ >", "< A -> .Ab, $/b >", "< A -> .b, $/b >"),
                transition(0, "A", /*new state*/ 1), kernel("< _ -> A., $ >", "< A -> A.b, $/b >"), closure("< _ -> A., $ >", "< A -> A.b, $/b >"),
                transition(0, "b", /*new state*/ 2), kernel("< A -> b., $/b >"),                    closure("< A -> b., $/b >"),
                transition(1, "b", /*new state*/ 3), kernel("< A -> Ab., $/b >"),                   closure("< A -> Ab., $/b >")
        );
    }

    @Test
    public final void testClosures7() {
        // (0) _ -> A
        // (1) A -> bA
        // (2) A -> b
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ rule(nonterminal("A"), development("b", "A")),
                /*(2)*/ rule(nonterminal("A"), development("b"))
        );

        debugPrint("\n" + toString(this.closureTable.getClosures()));

        this.test(
                                   /*new state   0*/ kernel("< _ -> .A, $ >"),                    closure("< _ -> .A, $ >", "< A -> .bA, $ >", "< A -> .b, $ >"),
                transition(0, "A", /*new state*/ 1), kernel("< _ -> A., $ >"),                    closure("< _ -> A., $ >"),
                transition(0, "b", /*new state*/ 2), kernel("< A -> b.A, $ >", "< A -> b., $ >"), closure("< A -> b.A, $ >", "< A -> b., $ >", "< A -> .bA, $ >", "< A -> .b, $ >"),
                transition(2, "A", /*new state*/ 3), kernel("< A -> bA., $ >"),                   closure("< A -> bA., $ >"),
                transition(2, "b", /*existing state*/ 2)
        );
    }

    @Test
    public final void testClosures8() {
        // (0) _ -> A
        // (1) A -> B
        // (2) B -> []
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ rule(nonterminal("A"), development("B")),
                /*(2)*/ rule(nonterminal("B"), development())
        );

        debugPrint("\n" + toString(this.closureTable.getClosures()));

        this.test(
                                   /*new state   0*/ kernel("< _ -> .A, $ >"), closure("< _ -> .A, $ >", "< A -> .B, $ >", "< B -> ., $ >"),
                transition(0, "A", /*new state*/ 1), kernel("< _ -> A., $ >"), closure("< _ -> A., $ >"),
                transition(0, "B", /*new state*/ 2), kernel("< A -> B., $ >"), closure("< A -> B., $ >")
        );
    }

    @Test
    public final void testClosures9() {
        // (0) _ -> A
        // (1) A -> Bc
        // (2) B -> []
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ rule(nonterminal("A"), development("B", "c")),
                /*(2)*/ rule(nonterminal("B"), development())
        );

        debugPrint("\n" + toString(this.closureTable.getClosures()));

        this.test(
                                   /*new state   0*/ kernel("< _ -> .A, $ >"),  closure("< _ -> .A, $ >", "< A -> .Bc, $ >", "< B -> ., c >"),
                transition(0, "A", /*new state*/ 1), kernel("< _ -> A., $ >"),  closure("< _ -> A., $ >"),
                transition(0, "B", /*new state*/ 2), kernel("< A -> B.c, $ >"), closure("< A -> B.c, $ >"),
                transition(2, "c", /*new state*/ 3), kernel("< A -> Bc., $ >"), closure("< A -> Bc., $ >")
        );
    }

    /**
     * 
     * @param assertions
     * <br>Not null
     */
    private final void test(final Object... assertions) {
        int stateCount = 0;

        for (final Object assertion : assertions) {
            if (assertion instanceof KernelMatcher) {
                this.testKernel((KernelMatcher) assertion);
                ++stateCount;
            }

            if (assertion instanceof ClosureMatcher) {
                this.testClosure((ClosureMatcher) assertion);
            }
        }

        assertEquals("State count", stateCount, this.closureTable.getClosures().size());

        final int[] transitionCounts = new int[stateCount];
        final Set<String> done = new HashSet<String>();

        for (final Object assertion : assertions) {
            if (assertion instanceof Object[]) {
                final Object[] array = (Object[]) assertion;
                final String transitionId = array[0] + " " + array[1];

                if (done.contains(transitionId)) {
                    throw new IllegalStateException("Duplicate transition: " + transitionId);
                }

                this.testTransition((Integer) array[0], array[1], (Integer) array[2]);
                ++transitionCounts[(Integer) array[0]];
                done.add(transitionId);
            }
        }

        this.testTransitionCounts(transitionCounts);
    }

    /**
     *
     * @param itemMatchers
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    private static final ClosureMatcher closure(final String... itemMatchers) {
        return new ClosureMatcher(itemMatchers);
    }

    /**
     *
     * @param stateIndex
     * <br>Range: {@code [0 .. Integer.MAX_VALUE]}
     * @param symbol
     * <br>Maybe null
     * <br>Shared
     * @param nextStateIndex
     * <br>Range: {@code [0 .. Integer.MAX_VALUE]}
     * @return
     * <br>Not null
     * <br>New
     */
    private static final Object[] transition(final int stateIndex, final Object symbol, final int nextStateIndex) {
        return array(stateIndex, symbol, nextStateIndex);
    }

    /**
     *
     * @param itemMatchers
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    private static final KernelMatcher kernel(final String... itemMatchers) {
        return new KernelMatcher(itemMatchers);
    }

    /**
     *
     * @param expectedTransitionCounts
     * <br>Not null
     */
    private final void testTransitionCounts(final int... expectedTransitionCounts) {
        for (int i = 0; i < expectedTransitionCounts.length; ++i) {
            assertEquals("Transition count for closure " + i, expectedTransitionCounts[i], this.getClosure(i).getTransitions().size());
        }

        assertEquals(expectedTransitionCounts.length, this.closureTable.getClosures().size());
    }

    /**
     * @param kernelMatcher
     * <br>Not null
     */
    private final void testKernel(final KernelMatcher kernelMatcher) {
        final Map.Entry<Kernel, Closure> entry = find(this.closureTable.getClosures(), kernelMatcher);

        assertNotNull("Kernel " + this.kernelIndices.size() + " not found: " + kernelMatcher.getItemMatchers(), entry);

        this.kernelIndices.add(entry.getKey().getIndex());
    }

    /**
     * @param closureMatcher
     * <br>Not null
     */
    private final void testClosure(final ClosureMatcher closureMatcher) {
        assertEquals("Closure " + (this.kernelIndices.size() - 1),
                closureMatcher,
                this.closureTable.getClosures().get(this.closureTable.getKernel(this.kernelIndices.get(this.kernelIndices.size() - 1))));
    }

    /**
     *
     * @param kernelIndex
     * <br>Range: {@code [0 .. this.closureTable.getClosures().size() - 1]}
     * @param symbol
     * <br>Maybe null
     * @param nextKernelIndex
     * <br>Range: {@code [0 .. this.closureTable.getClosures().size() - 1]}
     */
    private final void testTransition(final int kernelIndex, final Object symbol, final int nextKernelIndex) {
        final String transitionString = "Transition(" + kernelIndex + " " + symbol + ")";
        try {
            assertEquals(transitionString,
                    this.getKernelIndex(nextKernelIndex),
                    this.getClosure(this.getKernelIndex(kernelIndex)).getTransitions().get(symbol).getIndex());
        } catch (final NullPointerException exception) {
            ignore(exception);

            assertTrue(transitionString + " not found", false);
        }
    }

    /**
     * @param index
     * <br>Range: <code>[0 .. this.kernelIndices.size() - 1]</code>
     * @return
     * <br>Range: <code>[0 .. this.kernelIndices.size() - 1]</code>
     */
    private final int getKernelIndex(final int index) {
        return this.kernelIndices.get(index);
    }

    /**
     * @param kernelIndex
     * <br>Range: <code>[0 .. this.closureTable.getClosures().size() - 1]</code>
     * @return
     * <br>Not null
     * <br>Reference
     */
    private final Closure getClosure(final int kernelIndex) {
        return this.closureTable.getClosures().get(this.closureTable.getKernel(this.getKernelIndex(kernelIndex)));
    }

    /**
     *
     * @param rules
     * <br>Not null
     */
    private final void setGrammar(final Object[]... rules) {
        this.grammar = newGrammar(rules);
        this.closureTable = new LALR1ClosureTable(this.grammar);
    }

    /**
     * @param <K> The map key type
     * @param <V> The map value type
     * @param map
     * <br>Not null
     * @param keyMatcher
     * <br>Not null
     * @return
     * <br>Maybe null
     */
    private static final <K, V> Map.Entry<K, V> find(final Map<K, V> map, final Object keyMatcher) {
        for (final Map.Entry<K, V> entry : map.entrySet()) {
            if (keyMatcher.equals(entry.getKey())) {
                return entry;
            }
        }

        return null;
    }

    /**
     *
     * @param closures
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    private static final String toString(final Map<Kernel, Closure> closures) {
        final StringBuilder result = new StringBuilder();

        for (final Map.Entry<Kernel, Closure> entry : closures.entrySet()) {
            result.append(toString(entry.getKey()));
            result.append(" = ");
            result.append(toString(entry.getValue().getItems()));
            result.append("\n");
        }

        return result.toString();
    }

    /**
     *
     * @param kernel
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    private static final String toString(final Kernel kernel) {
        final StringBuilder result = new StringBuilder();

        result.append(kernel.getIndex());
        result.append(": c(");
        result.append(toString(kernel.getItems()));
        result.append(")");

        return result.toString();
    }

    /**
     *
     * @param items
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    private static final String toString(final Set<Item> items) {
        final StringBuilder result = new StringBuilder();

        result.append("{ ");
        result.append(AurochsTools.join(", ", items));
        result.append(" }");

        return result.toString();
    }

    /**
     * @author codistmonk (creation 2010-10-06)
     */
    private static final class KernelMatcher {

        private final Set<String> itemMatchers;

        /**
         *
         * @param itemMatchers
         * <br>Not null
         */
        KernelMatcher(final String... itemMatchers) {
            this.itemMatchers = set(itemMatchers);
        }

        /**
         *
         * @return
         * <br>Not null
         * <br>Shared
         */
        public final Set<String> getItemMatchers() {
            return this.itemMatchers;
        }

        @Override
        public final int hashCode() {
            return this.getItemMatchers().hashCode();
        }

        @Override
        public final boolean equals(final Object object) {
            final Kernel that = cast(Kernel.class, object);

            return that != null && this.getItemMatchers().equals(AurochsTools.iterate(that.getItems(), "toString"));
        }

    }

    /**
     * @author codistmonk (creation 2010-10-07)
     */
    private static final class ClosureMatcher {

        private final Set<String> itemMatchers;

        /**
         *
         * @param itemMatchers
         * <br>Not null
         */
        ClosureMatcher(final String... itemMatchers) {
            this.itemMatchers = set(itemMatchers);
        }

        /**
         *
         * @return
         * <br>Not null
         * <br>Shared
         */
        public final Set<String> getItemMatchers() {
            return this.itemMatchers;
        }

        @Override
        public final int hashCode() {
            return this.getItemMatchers().hashCode();
        }

        @Override
        public final boolean equals(final Object object) {
            final Closure that = cast(Closure.class, object);

            return that != null && this.getItemMatchers().equals(AurochsTools.iterate(that.getItems(), "toString"));
        }

        @Override
        public final String toString() {
            return this.getItemMatchers().toString();
        }

    }

}