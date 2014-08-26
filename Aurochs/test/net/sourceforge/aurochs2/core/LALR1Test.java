package net.sourceforge.aurochs2.core;

import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.join;
import static net.sourceforge.aprog.tools.Tools.list;
import static net.sourceforge.aprog.tools.Tools.set;
import static net.sourceforge.aurochs2.core.LexerBuilder.*;
import static net.sourceforge.aurochs2.core.TokenSource.characters;
import static net.sourceforge.aurochs2.core.TokenSource.tokens;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs2.core.Grammar.ReductionListener;
import net.sourceforge.aurochs2.core.LRParser.ConflictResolver;
import net.sourceforge.aurochs2.core.Lexer.Token;
import net.sourceforge.aurochs2.core.LexerBuilder.Union;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-08-24)
 */
public final class LALR1Test {
	
	@Test
	public final void testParser1() {
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
		
		assertTrue(parser.parse(tokens("1")));
		assertTrue(parser.parse(tokens("11")));
		assertTrue(parser.parse(tokens("1+1")));
		assertTrue(parser.parse(tokens("1-1")));
		assertTrue(parser.parse(tokens("(1)")));
		assertTrue(parser.parse(tokens("1(1)")));
		assertTrue(parser.parse(tokens("1(-1)")));
		assertTrue(parser.parse(tokens("-1")));
		assertFalse(parser.parse(tokens("1-")));
		
		final ConflictResolver conflictResolver = new ConflictResolver(parser);
		
		conflictResolver.resolve(characters("11(1)"), array(array('1', '1'), array('(', '1', ')')));
		conflictResolver.resolve(characters("111"), array(array('1', '1'), '1'));
		conflictResolver.resolve(characters("11+1"), array(array('1', '1'), '+', '1'));
		conflictResolver.resolve(characters("11-1"), array(array('1', '1'), '-', '1'));
		conflictResolver.resolve(characters("-11"), array(array('-', '1'), '1'));
		conflictResolver.resolve(characters("-1(1)"), array(array('-', '1'), array('(', '1', ')')));
		conflictResolver.resolve(characters("-1+1"), array(array('-', '1'), '+', '1'));
		conflictResolver.resolve(characters("-1-1"), array(array('-', '1'), '-', '1'));
		conflictResolver.resolve(characters("1+11"), array('1', '+', array('1', '1')));
		conflictResolver.resolve(characters("1+1(1)"), array('1', '+', array('1', array('(', '1', ')'))));
		conflictResolver.resolve(characters("1+1+1"), array(array('1', '+', '1'), '+', '1'));
		conflictResolver.resolve(characters("1+1-1"), array(array('1', '+', '1'), '-', '1'));
		conflictResolver.resolve(characters("1-11"), array('1', '-', array('1', '1')));
		conflictResolver.resolve(characters("1-1(1)"), array('1', '-', array('1', array('(', '1', ')'))));
		conflictResolver.resolve(characters("(1-1)"), array('(', array('1', '-', '1'), ')'));
		conflictResolver.resolve(characters("1-1+1"), array(array('1', '-', '1'), '+', '1'));
		conflictResolver.resolve(characters("1-1-1"), array(array('1', '-', '1'), '-', '1'));
		
		printAmbiguities(lrTable);
		
		print(lrTable);
	}
	
	@Test
	public final void testParser2() {
		final Grammar grammar = new Grammar();
		
		grammar.new Rule("()", "S");
		grammar.new Rule("S", '\'', "CS", '\'');
		grammar.new Rule("CS", "C", "CS");
		grammar.new Rule("CS");
		grammar.new Rule("C", 'a');
		grammar.new Rule("C", 'b');
		grammar.new Rule("C", '\\', '\'');
		
		final LALR1ClosureTable closureTable = new LALR1ClosureTable(grammar);
		final LRTable lrTable = new LRTable(closureTable);
		final LRParser parser = new LRParser(lrTable);
		
		Tools.debugPrint(closureTable.getStates().size());
		print(lrTable);
		printAmbiguities(lrTable);
		
		assertTrue(parser.parse(tokens("''")));
		assertTrue(parser.parse(tokens("'aba'")));
		assertTrue(parser.parse(tokens("'\\''")));
	}
	
	@Test
	public final void testLexer1() {
		final Grammar grammar = new Grammar();
		
		grammar.new Rule("()", "S");
		grammar.new Rule("S", '\'', ".+", '\'');
		grammar.new Rule("S", '\'', '\'');
		grammar.new Rule("S", " +");
		grammar.new Rule(".+", ".", ".+");
		grammar.new Rule(".+", ".");
		grammar.new Rule(".", 'a');
		grammar.new Rule(".", 'b');
		grammar.new Rule(".", '\\', '\'');
		grammar.new Rule(" +", ' ', " +");
		grammar.new Rule(" +", ' ');
		
		final LALR1ClosureTable closureTable = new LALR1ClosureTable(grammar);
		final LRTable lrTable = new LRTable(closureTable);
		final LRParser parser = new LRParser(lrTable);
		final ConflictResolver conflictResolver = new ConflictResolver(parser);
		
		conflictResolver.resolve(characters("  "), array(' ', ' '));
		
		printAmbiguities(lrTable);
		
		assertTrue(parser.parse(tokens("''")));
		assertTrue(parser.parse(tokens("'aba'")));
		assertTrue(parser.parse(tokens("'\\''")));
		assertTrue(parser.parse(tokens("   ")));
		assertFalse(parser.parse(tokens("")));
		
		{
			final Token[] tokenBox = { null };
			final List<Object> output = new ArrayList<Object>();
			
			ConflictResolver.setup(grammar, StringCollector.INSTANCE);
			
			final ReductionListener listener = new StringTokenGenerator(tokenBox);
			grammar.getRules().get(1).setListener(listener);
			grammar.getRules().get(2).setListener(listener);
			
			final Lexer lexer = new Lexer(parser, tokenBox);
			
			for (final Object token : lexer.translate(tokens("''  'bb' "))) {
				output.add(token);
			}
			
			assertNull(tokenBox[0]);
			assertEquals(2L, output.size());
		}
	}
	
	@Test
	public final void testLexer2() {
		final LexerBuilder lexerBuilder = new LexerBuilder();
		
		final Union digit = union(range('0', '9'));
		final Union letter = union(union(range('a', 'z')), union(range('A', 'Z')));
		lexerBuilder.generate("natural", oneOrMore(digit));
		lexerBuilder.generate("variable", letter);
		lexerBuilder.generate("string", '\'', "characters", '\'');
		lexerBuilder.generate("string", '\'', '\'');
		lexerBuilder.define("characters", oneOrMore(union(digit, letter, ' ', sequence('\\', '\''))));
		lexerBuilder.skip(oneOrMore(' '));
		
		final Lexer lexer = lexerBuilder.newLexer();
		
		new ConflictResolver(lexer.getParser()).resolve(characters("''"), array(array('\'', '\'')));
		
		printAmbiguities(lexer.getParser().getTable());
		
		final List<Token> output = list(lexer.translate(tokens(characters("' '   ''"))));
		
		assertEquals(2L, output.size());
	}
	
	public static final void print(final LRTable lrTable) {
		final int n = lrTable.getActions().size();
		
		for (int i = 0; i < n; ++i) {
			Tools.debugPrint(i, lrTable.getActions().get(i));
		}
	}
	
	public static final void printAmbiguities(final LRTable lrTable) {
		final List<List<Object>> ambiguities = lrTable.collectAmbiguousExamples();
		
		if (!ambiguities.isEmpty()) {
			Tools.debugPrint("\n" + join("\n", ambiguities.toArray()));
		}
	}
	
}