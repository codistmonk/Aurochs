package net.sourceforge.aurochs2.demos;

import static net.sourceforge.aurochs2.core.LexerBuilder.*;
import static net.sourceforge.aurochs2.core.ParserBuilder.bloc;
import static net.sourceforge.aurochs2.core.ParserBuilder.Priority.Associativity.LEFT;
import static net.sourceforge.aurochs2.core.ParserBuilder.Priority.Associativity.NONE;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aurochs2.core.LRParser;
import net.sourceforge.aurochs2.core.Lexer;
import net.sourceforge.aurochs2.core.LexerBuilder;
import net.sourceforge.aurochs2.core.ParserBuilder;
import net.sourceforge.aurochs2.core.LexerBuilder.Union;

/**
 * @author codistmonk (creation 2014-08-26)
 */
public final class Calculator {
	
	private Calculator() {
		throw new IllegalInstantiationException();
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static final void main(final String[] commandLineArguments) {
		final LexerBuilder lexerBuilder = new LexerBuilder();
		final Union digit = union(range('0', '9'));
		final Union letter = union(union(range('a', 'z')), union(range('A', 'Z')));
		
		lexerBuilder.generate("natural", oneOrMore(digit));
		lexerBuilder.generate("variable", letter);
		lexerBuilder.generate("=", '=');
		lexerBuilder.generate("(", '(');
		lexerBuilder.generate(")", ')');
		lexerBuilder.generate("+", '+');
		lexerBuilder.generate("-", '-');
		lexerBuilder.skip(oneOrMore(' '));
		
		final Lexer lexer = lexerBuilder.newLexer();
		final ParserBuilder parserBuilder = new ParserBuilder(lexer);
		
		parserBuilder.define("()", "Instruction");
		parserBuilder.define("Instruction", "variable", "=", "Expression");
		parserBuilder.define("Instruction", "Expression");
		parserBuilder.define("Expression", "Expression", "Expression");
		parserBuilder.define("Expression", "Expression", "+", "Expression");
		parserBuilder.define("Expression", "Expression", "-", "Expression");
		parserBuilder.define("Expression", "-", "Expression");
		parserBuilder.define("Expression", "(", "Expression", ")");
		parserBuilder.define("Expression", "natural");
		parserBuilder.define("Expression", "variable");
		
		parserBuilder.resolveConflictWith(bloc("Expression", "Expression"), bloc("(", "Expression", ")"));
		parserBuilder.resolveConflictWith(bloc("-", "Expression"), bloc("(", "Expression", ")"));
		parserBuilder.resolveConflictWith("Expression", "+", bloc("Expression", bloc("(", "Expression", ")")));
		parserBuilder.resolveConflictWith("Expression", "-", bloc("Expression", bloc("(", "Expression", ")")));
		parserBuilder.resolveConflictWith("(", bloc("Expression", "-", "Expression"), ")");
		
		parserBuilder.setPriority(300, NONE, "-", "Expression");
		parserBuilder.setPriority(300, LEFT, "Expression", "natural");
		parserBuilder.setPriority(300, LEFT, "Expression", "variable");
		parserBuilder.setPriority(300, LEFT, "Expression", "Expression");
		parserBuilder.setPriority(100, LEFT, "Expression", "+", "Expression");
		parserBuilder.setPriority(100, LEFT, "Expression", "-", "Expression");
		
		final LRParser parser = parserBuilder.newParser();
	}
	
}
