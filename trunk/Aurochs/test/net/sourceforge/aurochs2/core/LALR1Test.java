package net.sourceforge.aurochs2.core;

import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.set;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs2.core.Grammar.Rule;
import net.sourceforge.aurochs2.core.LRParser.ConflictResolver;
import net.sourceforge.aurochs2.core.LRParser.ConflictResolver.Mode;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-08-24)
 */
public final class LALR1Test {
	
	@Test
	public final void test() {
		final Grammar grammar = new Grammar();
		
		grammar.new Rule("()", "E");
		grammar.new Rule("E", "E", '+', "E");
		grammar.new Rule("E", "E", '-', "E");
		grammar.new Rule("E", '-', "E");
		grammar.new Rule("E", "E", "E");
		grammar.new Rule("E", '(', "E", ')');
		grammar.new Rule("E", '1');
		
		{
			final Map<Object, Collection<Object>> firsts = grammar.getFirsts();
			
			assertEquals(set("()", "E"), firsts.keySet());
			assertEquals(set('-', '(', '1'), new HashSet<>(firsts.get("()")));
			assertEquals(set('-', '(', '1'), new HashSet<>(firsts.get("E")));
		}
		
		final LALR1ClosureTable closureTable = new LALR1ClosureTable(grammar);
		
		Tools.debugPrint(closureTable.getStates().size());
		
		final LRTable lrTable = new LRTable(closureTable);
		
		print(lrTable);
		
		final LRParser parser = new LRParser(lrTable);
		
		assertTrue(parser.parseAll(tokens("1")));
		assertTrue(parser.parseAll(tokens("11")));
		assertTrue(parser.parseAll(tokens("1+1")));
		assertTrue(parser.parseAll(tokens("1-1")));
		assertTrue(parser.parseAll(tokens("(1)")));
		assertTrue(parser.parseAll(tokens("1(1)")));
		assertTrue(parser.parseAll(tokens("1(-1)")));
		assertTrue(parser.parseAll(tokens("-1")));
		assertFalse(parser.parseAll(tokens("1-")));
		
		resolveConflicts(parser, "1+1+1", array(array('1', '+', '1'), '+', '1'));
		resolveConflicts(parser, "1+1-1", array(array('1', '+', '1'), '-', '1'));
		resolveConflicts(parser, "1-1+1", array(array('1', '-', '1'), '+', '1'));
		resolveConflicts(parser, "1-1-1", array(array('1', '-', '1'), '-', '1'));
		resolveConflicts(parser, "111", array(array('1', '1'), '1'));
		resolveConflicts(parser, "11+1", array(array('1', '1'), '+', '1'));
		resolveConflicts(parser, "1+11", array('1', '+', array('1', '1')));
		resolveConflicts(parser, "(1-1)", array('(', array('1', '-', '1'), ')'));
		resolveConflicts(parser, "-1-1", array(array('-', '1'), '-', '1'));
		
		print(lrTable);
	}
	
	public static final void print(final LRTable lrTable) {
		final int n = lrTable.getActions().size();
		
		for (int i = 0; i < n; ++i) {
			Tools.debugPrint(i, lrTable.getActions().get(i));
		}
	}
	
	public static final void resolveConflicts(final LRParser parser, final String string, final Object[] expected) {
		ConflictResolver.setup(parser.getGrammar());
		
		final ConflictResolver resolver = new ConflictResolver();
		Object[] actual = (Object[]) parser.parseAll(tokens(string), resolver);
		final List<Integer> stop = new ArrayList<>(resolver.getActionChoices());
		
		while (!Arrays.deepEquals(expected, actual)) {
			Tools.debugPrint(Arrays.deepToString(actual));
			actual = (Object[]) parser.parseAll(tokens(string), resolver);
			
			if (stop.equals(resolver.getActionChoices())) {
				break;
			}
		}
		
		Tools.debugPrint(Arrays.deepToString(actual));
		
		resolver.setMode(Mode.ACCEPT_CURRENT);
		
		actual = (Object[]) parser.parseAll(tokens(string), resolver);
		
		if (!Arrays.deepEquals(expected, actual)) {
			throw new IllegalStateException();
		}
	}
	
	public static final TokenSource tokens(final String string) {
		return new TokenSource(new Iterator<Object>() {
			
			private final int n = string.length();
			
			private int i = 0;
			
			@Override
			public final boolean hasNext() {
				return this.i < this.n;
			}
			
			@Override
			public final Object next() {
				return string.charAt(this.i++);
			}
			
		});
	}
	
}
