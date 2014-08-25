package net.sourceforge.aurochs2.core;

import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.set;
import static net.sourceforge.aurochs2.core.TokenSource.tokens;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs2.core.LRParser.ConflictResolver;
import net.sourceforge.aurochs2.core.LRParser.ConflictResolver.Mode;
import net.sourceforge.aurochs2.core.LRTable.Action;

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
		
		resolveConflicts(parser, "11(1)", array(array('1', '1'), array('(', '1', ')')));
		resolveConflicts(parser, "111", array(array('1', '1'), '1'));
		resolveConflicts(parser, "11+1", array(array('1', '1'), '+', '1'));
		resolveConflicts(parser, "11-1", array(array('1', '1'), '-', '1'));
		resolveConflicts(parser, "-11", array(array('-', '1'), '1'));
		resolveConflicts(parser, "-1(1)", array(array('-', '1'), array('(', '1', ')')));
		resolveConflicts(parser, "-1+1", array(array('-', '1'), '+', '1'));
		resolveConflicts(parser, "-1-1", array(array('-', '1'), '-', '1'));
		resolveConflicts(parser, "1+11", array('1', '+', array('1', '1')));
		resolveConflicts(parser, "1+1(1)", array('1', '+', array('1', array('(', '1', ')'))));
		resolveConflicts(parser, "1+1+1", array(array('1', '+', '1'), '+', '1'));
		resolveConflicts(parser, "1+1-1", array(array('1', '+', '1'), '-', '1'));
		resolveConflicts(parser, "1-11", array('1', '-', array('1', '1')));
		resolveConflicts(parser, "1-1(1)", array('1', '-', array('1', array('(', '1', ')'))));
		resolveConflicts(parser, "(1-1)", array('(', array('1', '-', '1'), ')'));
		resolveConflicts(parser, "1-1+1", array(array('1', '-', '1'), '+', '1'));
		resolveConflicts(parser, "1-1-1", array(array('1', '-', '1'), '-', '1'));
		
		Tools.debugPrint("\n" + Tools.join("\n", collectAmbiguousExamples(lrTable).toArray()));
		
		print(lrTable);
	}
	
	public static final List<List<Object>> collectAmbiguousExamples(final LRTable lrTable) {
		final List<List<Object>> result = new ArrayList<>();
		final List<Map<Object, List<Action>>> actions = lrTable.getActions();
		final int n = actions.size();
		
		for (int stateIndex = 0; stateIndex < n; ++stateIndex) {
			final Map<Object, List<Action>> stateActions = actions.get(stateIndex);
			
			for (final Map.Entry<Object, List<Action>> entry : stateActions.entrySet()) {
				if (1 < entry.getValue().size()) {
					final List<Integer> path = new ArrayList<>();
					final List<Object> ambiguousExample = new ArrayList<>();
					
					path.add(0, stateIndex);
					ambiguousExample.add(0, entry.getKey());
					
					int target = stateIndex;
					
					while (!path.contains(0)) {
						Action targetAction = new LRTable.Shift(target);
						boolean antecedentFound = false;
						
						for (int i = 0; i < n && !antecedentFound; ++i) {
							if (path.contains(i)) {
								continue;
							}
							
							for (final Map.Entry<Object, List<Action>> entry2 : actions.get(i).entrySet()) {
								if (entry2.getValue().contains(targetAction)) {
									ambiguousExample.add(0, entry2.getKey());
									path.add(0, i);
									target = i;
									antecedentFound = true;
									break;
								}
							}
						}
						
						if (!antecedentFound) {
							Tools.debugError("Couldn't find path to ambiguity " + entry);
							break;
						}
					}
					
					result.add(ambiguousExample);
				}
			}
		}
		return result;
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
		
		while (!Arrays.deepEquals(expected, actual)) {
			actual = (Object[]) parser.parseAll(tokens(string), resolver);
			
			if (isZeroes(resolver.getActionChoices())) {
				break;
			}
		}
		
		resolver.setMode(Mode.ACCEPT_CURRENT);
		
		actual = (Object[]) parser.parseAll(tokens(string), resolver);
		
		if (!Arrays.deepEquals(expected, actual)) {
			throw new IllegalStateException();
		}
	}
	
	public static final boolean isZeroes(final List<Integer> list) {
		for (final Integer i : list) {
			if (i.intValue() != 0) {
				return false;
			}
		}
		
		return true;
	}
	
}
