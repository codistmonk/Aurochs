package net.sourceforge.aurochs2.core;

import static net.sourceforge.aprog.tools.Tools.set;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs2.core.Grammar.Rule;
import net.sourceforge.aurochs2.core.LRParser.ConflictResolver;

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
		
		{
			final int n = lrTable.getActions().size();
			
			for (int i = 0; i < n; ++i) {
				Tools.debugPrint(i, lrTable.getActions().get(i));
			}
		}
		
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
		
		ConflictResolver.setup(grammar);
		
		{
			final ConflictResolver resolver = new ConflictResolver();
			
			Tools.debugPrint(Arrays.deepToString((Object[]) parser.parseAll(tokens("1+1+1"), resolver)));
			Tools.debugPrint(resolver.getActionChoices());
			Tools.debugPrint(Arrays.deepToString((Object[]) parser.parseAll(tokens("1+1+1"), resolver)));
			Tools.debugPrint(resolver.getActionChoices());
		}
	}
	
	public final TokenSource tokens(final String string) {
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
