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

import static org.junit.Assert.*;

import net.sourceforge.aurochs.Grammar.Production;
import net.sourceforge.aurochs.Grammar.RegularSymbol;
import net.sourceforge.aurochs.Grammar.RegularUnion;
import net.sourceforge.aurochs.Grammar.RegularSequence;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author codistmonk (creation 2010-10-04)
 */
public final class GrammarTest {

    private Grammar grammar;

    @Before
    public final void beforeEachTest() {
        this.grammar = null;
    }

    @Test
    public final void testCanCollapseAndGetFirsts0() {
        // (0) _ -> A
        // (1) A -> []
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development())
        );

        this.testCanCollapseAndGetFirsts("A", CAN_COLLAPSE, firsts());
    }

    @Test
    public final void testCanCollapseAndGetFirsts1() {
        // (0) _ -> A
        // (1) A -> b
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development("b"))
        );

        this.testCanCollapseAndGetFirsts("A", CANNOT_COLLAPSE, firsts("b"));
    }

    @Test
    public final void testCanCollapseAndGetFirsts2() {
        // (0) _ -> A
        // (1) A -> bc
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development("b", "c"))
        );

        this.testCanCollapseAndGetFirsts("A", CANNOT_COLLAPSE, firsts("b"));
    }

    @Test
    public final void testCanCollapseAndGetFirsts3() {
        // (0) _ -> A
        // (1) A -> b
        // (2) A -> c
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development("b")),
                /*(2)*/ production(nonterminal("A"), development("c"))
        );

        this.testCanCollapseAndGetFirsts("A", CANNOT_COLLAPSE, firsts("b", "c"));
    }

    @Test
    public final void testCanCollapseAndGetFirsts4() {
        // (0) _ -> A
        // (1) A -> Ab
        // (2) A -> []
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development("A", "b")),
                /*(2)*/ production(nonterminal("A"), development())
        );

        this.testCanCollapseAndGetFirsts("A", CAN_COLLAPSE, firsts("b"));
    }

    @Test
    public final void testCanCollapseAndGetFirsts5() {
        // (0) _ -> A
        // (1) A -> bA
        // (2) A -> []
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development("b", "A")),
                /*(1)*/ production(nonterminal("A"), development())
        );

        this.testCanCollapseAndGetFirsts("A", CAN_COLLAPSE, firsts("b"));
    }

    @Test
    public final void testCanCollapseAndGetFirsts6() {
        // (0) _ -> A
        // (1) A -> Ab
        // (2) A -> b
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development("A", "b")),
                /*(2)*/ production(nonterminal("A"), development("b"))
        );

        this.testCanCollapseAndGetFirsts("A", CANNOT_COLLAPSE, firsts("b"));
    }

    @Test
    public final void testCanCollapseAndGetFirsts7() {
        // (0) _ -> A
        // (1) A -> bA
        // (2) A -> b
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development("b", "A")),
                /*(2)*/ production(nonterminal("A"), development("b"))
        );

        this.testCanCollapseAndGetFirsts("A", CANNOT_COLLAPSE, firsts("b"));
    }

    @Test
    public final void testCanCollapseAndGetFirsts8() {
        // (0) _ -> A
        // (1) A -> B
        // (2) B -> []
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development("B")),
                /*(2)*/ production(nonterminal("B"), development())
        );

        this.testCanCollapseAndGetFirsts("A", CAN_COLLAPSE, firsts());
        this.testCanCollapseAndGetFirsts("B", CAN_COLLAPSE, firsts());
    }

    @Test
    public final void testCanCollapseAndGetFirsts9() {
        // (0) _ -> A
        // (1) A -> Bc
        // (2) B -> []
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development("B", "c")),
                /*(2)*/ production(nonterminal("B"), development())
        );

        this.testCanCollapseAndGetFirsts("A", CANNOT_COLLAPSE, firsts("c"));
        this.testCanCollapseAndGetFirsts("B", CAN_COLLAPSE, firsts());
    }

    @Test
    public final void testCanCollapseAndGetFirsts10() {
        // (0) _ -> A
        // (1) A -> Bc
        // (2) B -> A
        // (3) B -> []
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development("B", "c")),
                /*(2)*/ production(nonterminal("B"), development("A")),
                /*(3)*/ production(nonterminal("B"), development())
        );

        this.testCanCollapseAndGetFirsts("A", CANNOT_COLLAPSE, firsts("c"));
        this.testCanCollapseAndGetFirsts("B", CAN_COLLAPSE, firsts("c"));
    }

    @Test
    public final void testCanCollapseAndGetFirsts11() {
        // (0) _ -> A
        // (1) A -> BC
        // (2) B -> []
        // (3) C -> []
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development("B", "C")),
                /*(2)*/ production(nonterminal("B"), development()),
                /*(3)*/ production(nonterminal("C"), development())
        );

        this.testCanCollapseAndGetFirsts("A", CAN_COLLAPSE, firsts());
        this.testCanCollapseAndGetFirsts("B", CAN_COLLAPSE, firsts());
        this.testCanCollapseAndGetFirsts("C", CAN_COLLAPSE, firsts());
    }

    @Test
    public final void testCanCollapseAndGetFirsts12() {
        // (0) _ -> A
        // (1) A -> BC
        // (2) B -> []
        // (3) C -> d
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development("B", "C")),
                /*(2)*/ production(nonterminal("B"), development()),
                /*(3)*/ production(nonterminal("C"), development("d"))
        );

        this.testCanCollapseAndGetFirsts("A", CANNOT_COLLAPSE, firsts("d"));
        this.testCanCollapseAndGetFirsts("B", CAN_COLLAPSE, firsts());
        this.testCanCollapseAndGetFirsts("C", CANNOT_COLLAPSE, firsts("d"));
    }

    @Test
    public final void testCanCollapseAndGetFirsts13() {
        // (0) _ -> A
        // (1) A -> []
        // (2) A -> ABC
        // (3) B -> Bd
        // (4) B -> A
        // (5) C -> e
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development("A", "B", "C")),
                /*(2)*/ production(nonterminal("A"), development()),
                /*(3)*/ production(nonterminal("B"), development("B", "d")),
                /*(4)*/ production(nonterminal("B"), development("A")),
                /*(5)*/ production(nonterminal("C"), development("e"))
        );

        this.testCanCollapseAndGetFirsts("A", CAN_COLLAPSE, firsts("d", "e"));
        this.testCanCollapseAndGetFirsts("B", CAN_COLLAPSE, firsts("d", "e"));
        this.testCanCollapseAndGetFirsts("C", CANNOT_COLLAPSE, firsts("e"));
    }

    @Test
    public final void testCanCollapseAndGetFirsts14() {
        // (0) _ -> A
        // (1) A -> B
        // (2) B -> Cd
        // (3) C -> A
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development("B")),
                /*(2)*/ production(nonterminal("A"), development()),
                /*(3)*/ production(nonterminal("B"), development("C", "d")),
                /*(4)*/ production(nonterminal("C"), development("A"))
        );

        this.testCanCollapseAndGetFirsts("A", CAN_COLLAPSE, firsts("d"));
        this.testCanCollapseAndGetFirsts("B", CANNOT_COLLAPSE, firsts("d"));
        this.testCanCollapseAndGetFirsts("C", CAN_COLLAPSE, firsts("d"));
    }

    @Test
    public final void testCanCollapseAndGetFirsts15() {
        // (0) _ -> A
        // (1) A -> BA
        // (2) B -> []
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development("B", "A")),
                /*(2)*/ production(nonterminal("B"), development())
        );

        this.testCanCollapseAndGetFirsts("A", CAN_COLLAPSE, firsts());
        this.testCanCollapseAndGetFirsts("B", CAN_COLLAPSE, firsts());
    }

    @Test
    public final void testCanCollapseAndGetFirsts16() {
        // (0) _ -> A
        // (1) A -> BA
        // (2) B -> []
        this.setGrammar(
                /*(0)   implicit*/
                /*(1)*/ production(nonterminal("A"), development("A", "B")),
                /*(2)*/ production(nonterminal("B"), development())
        );

        this.testCanCollapseAndGetFirsts("A", CAN_COLLAPSE, firsts());
        this.testCanCollapseAndGetFirsts("B", CAN_COLLAPSE, firsts());
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testAddProductionFailure() {
        this.setGrammar(production(Grammar.SpecialSymbol.END_TERMINAL, development()));
    }

    @Test
    public final void testRegular0() {
        final boolean debug = false;
        this.grammar = new Grammar();

        this.grammar.addProduction('A', new RegularSequence(new RegularSymbol('b'), new RegularSymbol('A')));
        this.grammar.addProduction('A', new RegularUnion(new RegularSymbol('c'), new RegularSequence()));

        // <editor-fold defaultstate="collapsed" desc="DEBUG">
        if (debug) {
            for (final Production production : this.grammar.getProductions()) {
                debugPrint(production.getIndex(), production.getNonterminal(), production.getDevelopment());
            }
        }
        // </editor-fold>

        this.testCanCollapseAndGetFirsts('A', CAN_COLLAPSE, firsts('b', 'c'));
    }

    /**
     *
     * @param nonterminal
     * <br>Maybe null
     * @param expectedCanBeDerivedIntoAnEmptySequence
     * <br>Range: any boolean
     * @param expectedFirsts
     * <br>Not null
     */
    private final void testCanCollapseAndGetFirsts(final Object nonterminal,
            final boolean expectedCanCollapse, final Object[] expectedFirsts) {
        assertEquals(expectedCanCollapse, this.grammar.canCollapse(nonterminal));
        assertEquals(set(expectedFirsts), this.grammar.getFirsts(nonterminal));
    }

    /**
     *
     * @param productions
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    private final void setGrammar(final Object[]... productions) {
        this.grammar = newGrammar(productions);
    }

    /**
     * {@value}.
     */
    private static final boolean CAN_COLLAPSE = true;

    /**
     * {@value}.
     */
    private static final boolean CANNOT_COLLAPSE = !CAN_COLLAPSE;

    /**
     *
     * @param productions
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    static final Grammar newGrammar(final Object[]... productions) {
        final Grammar result = new Grammar();

        for (final Object[] production : productions) {
            result.addProduction(production[0], (Object[]) production[1]);
        }

        return result;
    }

    /**
     *
     * @param nonterminal
     * <br>Maybe null
     * <br>Shared
     * @param development
     * <br>Not null
     * <br>Shared
     * @return
     * <br>Not null
     * <br>New
     */
    static final Object[] production(final Object nonterminal, final Object[] development) {
        return array(nonterminal, development);
    }

    /**
     *
     * @param nonterminal
     * <br>Maybe null
     * @return {@code nonterminal}
     * <br>Maybe null
     */
    static final Object nonterminal(final Object nonterminal) {
        return nonterminal;
    }

    /**
     *
     * @param development
     * <br>Not null
     * @return {@code development}
     * <br>Not null
     */
    static final Object[] development(final Object... development) {
        return development;
    }

    /**
     *
     * @param firsts
     * <br>Not null
     * @return {@code firsts}
     * <br>Not null
     */
    static final Object[] firsts(final Object... firsts) {
        return firsts;
    }

}