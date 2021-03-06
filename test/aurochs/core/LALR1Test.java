package aurochs.core;

import static aurochs.core.LexerBuilder.*;
import static aurochs.core.TokenSource.characters;
import static aurochs.core.TokenSource.tokens;
import static multij.tools.Tools.array;
import static multij.tools.Tools.list;
import static multij.tools.Tools.set;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import multij.tools.Tools;

import org.junit.Test;

import aurochs.core.Grammar;
import aurochs.core.LALR1ClosureTable;
import aurochs.core.LRParser;
import aurochs.core.LRTable;
import aurochs.core.Lexer;
import aurochs.core.LexerBuilder;
import aurochs.core.ParserBuilder;
import aurochs.core.Grammar.RuleAction;
import aurochs.core.LRParser.ConflictResolver;
import aurochs.core.Lexer.Token;
import aurochs.core.LexerBuilder.Union;
import aurochs.core.ParserBuilder.Priority.Associativity;

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
		
		lrTable.printAmbiguities();
		
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
		lrTable.printAmbiguities();
		
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
		
		lrTable.printAmbiguities();
		
		assertTrue(parser.parse(tokens("''")));
		assertTrue(parser.parse(tokens("'aba'")));
		assertTrue(parser.parse(tokens("'\\''")));
		assertTrue(parser.parse(tokens("   ")));
		assertFalse(parser.parse(tokens("")));
		
		{
			final Token[] tokenBox = { null };
			final List<Object> output = new ArrayList<>();
			
			ConflictResolver.setup(grammar, StringCollector.INSTANCE);
			
			final RuleAction listener = new StringTokenGenerator(tokenBox);
			grammar.getRules().get(1).setAction(listener);
			grammar.getRules().get(2).setAction(listener);
			
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
		
		lexer.getParser().getTable().printAmbiguities();
		
		final List<Token> output = list(lexer.translate(tokens(characters("' '   ''"))));
		
		assertEquals(2L, output.size());
	}
	
	@Test
	public final void testFullParser1() {
		final LexerBuilder lexerBuilder = new LexerBuilder();
		final Union digit = union(range('0', '9'));
		final Union letter = union(union(range('a', 'z')), union(range('A', 'Z')));
		
		lexerBuilder.generate("natural", oneOrMore(digit));
		lexerBuilder.generate("variable", letter);
		lexerBuilder.generate("+", '+');
		lexerBuilder.generate("-", '-');
		lexerBuilder.generate("(", '(');
		lexerBuilder.generate(")", ')');
		lexerBuilder.generate("string", '\'', "characters", '\'');
		lexerBuilder.generate("string", '\'', '\'');
		lexerBuilder.define("characters", oneOrMore(union(digit, letter, ' ', '+', '-', '(', ')', sequence('\\', '\''))));
		lexerBuilder.skip(oneOrMore(' '));
		
		final Lexer lexer = lexerBuilder.newLexer();
		final ParserBuilder parserBuilder = new ParserBuilder(lexer);
		
		parserBuilder.define("()", "Expression");
		parserBuilder.define("Expression", "Expression", "Expression");
		parserBuilder.define("Expression", "Expression", ("+"), "Expression");
		parserBuilder.define("Expression", "Expression", ("-"), "Expression");
		parserBuilder.define("Expression", ("-"), "Expression");
		parserBuilder.define("Expression", ("("), "Expression", (")"));
		parserBuilder.define("Expression", ("string"));
		parserBuilder.define("Expression", ("variable"));
		parserBuilder.define("Expression", ("natural"));
		
		parserBuilder.resolveConflictWith(array("Expression", "Expression"), array(("("), "Expression", (")")));
		parserBuilder.resolveConflictWith(array(("-"), "Expression"), array(("("), "Expression", (")")));
		parserBuilder.resolveConflictWith("Expression", ("+"), array("Expression", array(("("), "Expression", (")"))));
		parserBuilder.resolveConflictWith("Expression", ("-"), array("Expression", array(("("), "Expression", (")"))));
		parserBuilder.resolveConflictWith(("("), array("Expression", ("-"), "Expression"), (")"));
		
		parserBuilder.setPriority(300, Associativity.NONE, ("-"), "Expression");
		parserBuilder.setPriority(300, Associativity.LEFT, "Expression", ("string"));
		parserBuilder.setPriority(300, Associativity.LEFT, "Expression", ("natural"));
		parserBuilder.setPriority(300, Associativity.LEFT, "Expression", ("variable"));
		parserBuilder.setPriority(300, Associativity.LEFT, "Expression", "Expression");
		parserBuilder.setPriority(100, Associativity.LEFT, "Expression", ("+"), "Expression");
		parserBuilder.setPriority(100, Associativity.LEFT, "Expression", ("-"), "Expression");
		
		final LRParser parser = parserBuilder.newParser();
		
		assertEquals(0L, parser.getTable().collectAmbiguousExamples().size());
		
		// This bloc is not needed anymore for conflict resolution, but can still be used for testing because
		// it will fail if there is a problem with the resolution done in ParserBuilder 
		{
			final ConflictResolver resolver = new ConflictResolver(parser);
			
			resolver.resolve(list(lexer.translate(tokens("aa''"))), array(array("a", "a"), "''"));
			resolver.resolve(list(lexer.translate(tokens("aa11"))), array(array("a", "a"), "11"));
			resolver.resolve(list(lexer.translate(tokens("aa(a)"))), array(array("a", "a"), array("(", "a", ")")));
			resolver.resolve(list(lexer.translate(tokens("aaa"))), array(array("a", "a"), "a"));
			resolver.resolve(list(lexer.translate(tokens("aa+a"))), array(array("a", "a"), "+", "a"));
			resolver.resolve(list(lexer.translate(tokens("aa-a"))), array(array("a", "a"), "-", "a"));
			resolver.resolve(list(lexer.translate(tokens("-a''"))), array(array("-", "a"), "''"));
			resolver.resolve(list(lexer.translate(tokens("-a1"))), array(array("-", "a"), "1"));
			resolver.resolve(list(lexer.translate(tokens("-a(a)"))), array(array("-", "a"), array("(", "a", ")")));
			resolver.resolve(list(lexer.translate(tokens("-aa"))), array(array("-", "a"), "a"));
			resolver.resolve(list(lexer.translate(tokens("-a+a"))), array(array("-", "a"), "+", "a"));
			resolver.resolve(list(lexer.translate(tokens("-a-a"))), array(array("-", "a"), "-", "a"));
			resolver.resolve(list(lexer.translate(tokens("a+a''"))), array("a", "+", array("a", "''")));
			resolver.resolve(list(lexer.translate(tokens("a+a1"))), array("a", "+", array("a", "1")));
			resolver.resolve(list(lexer.translate(tokens("a+a(a)"))), array("a", "+", array("a", array("(", "a", ")"))));
			resolver.resolve(list(lexer.translate(tokens("a+aa"))), array("a", "+", array("a", "a")));
			resolver.resolve(list(lexer.translate(tokens("a+a+a"))), array(array("a", "+", "a"), "+", "a"));
			resolver.resolve(list(lexer.translate(tokens("a+a-a"))), array(array("a", "+", "a"), "-", "a"));
			resolver.resolve(list(lexer.translate(tokens("a-a''"))), array("a", "-", array("a", "''")));
			resolver.resolve(list(lexer.translate(tokens("a-a1"))), array("a", "-", array("a", "1")));
			resolver.resolve(list(lexer.translate(tokens("a-a(a)"))), array("a", "-", array("a", array("(", "a", ")"))));
			resolver.resolve(list(lexer.translate(tokens("a-aa"))), array("a", "-", array("a", "a")));
			resolver.resolve(list(lexer.translate(tokens("(a-a)"))), array("(", array("a", "-", "a"), ")"));
			resolver.resolve(list(lexer.translate(tokens("a-a+a"))), array(array("a", "-", "a"), "+", "a"));
			resolver.resolve(list(lexer.translate(tokens("a-a-a"))), array(array("a", "-", "a"), "-", "a"));
		}
		
		assertTrue(parser.parse(lexer.translate(tokens("12(-42)   'toto'"))));
	}
	
	public static final void print(final LRTable lrTable) {
		final int n = lrTable.getActions().size();
		
		for (int i = 0; i < n; ++i) {
			Tools.debugPrint(i, lrTable.getActions().get(i));
		}
	}
	
}
