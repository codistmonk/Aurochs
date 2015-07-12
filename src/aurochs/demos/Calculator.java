package aurochs.demos;

import static aurochs.core.LexerBuilder.*;
import static aurochs.core.ParserBuilder.block;
import static aurochs.core.ParserBuilder.Priority.Associativity.LEFT;
import static aurochs.core.ParserBuilder.Priority.Associativity.NONE;
import static aurochs.core.TokenSource.tokens;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import aurochs.core.LRParser;
import aurochs.core.Lexer;
import aurochs.core.LexerBuilder;
import aurochs.core.ParserBuilder;
import aurochs.core.LexerBuilder.Union;
import net.sourceforge.aprog.tools.IllegalInstantiationException;

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
		
		lexerBuilder.generate("quit", sequence(':', 'q', 'u', 'i', 't'));
		lexerBuilder.generate("help", sequence(':', 'h', 'e', 'l', 'p'));
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
		final Map<String, BigInteger> context = new HashMap<>();
		
		parserBuilder.define("()", "Instruction");
		parserBuilder.define("Instruction", "quit").setAction((rule, data) -> {
			System.exit(0);
			
			return null;
		});
		parserBuilder.define("Instruction", "help").setAction((rule, data) -> {
			help();
			
			return null;
		});
		parserBuilder.define("Instruction", "variable", "=", "Expression").setAction((rule, data) -> {
			final BigInteger value = (BigInteger) data[2];
			
			if (value != null) {
				context.put(data[0].toString(), value);
				
				System.out.println(value);
			}
			
			return null;
		});
		parserBuilder.define("Instruction", "Expression").setAction((rule, data) -> {
			if (data[0] != null) {
				System.out.println(data[0]);
			}
			
			return null;
		});
		parserBuilder.define("Expression", "Expression", "Expression").setAction((rule, data) -> {
			final BigInteger left = (BigInteger) data[0];
			final BigInteger right = (BigInteger) data[1];
			
			return left != null && right != null ? left.multiply(right) : null;
		});
		parserBuilder.define("Expression", "Expression", "+", "Expression").setAction((rule, data) -> {
			final BigInteger left = (BigInteger) data[0];
			final BigInteger right = (BigInteger) data[2];
			
			return left != null && right != null ? left.add(right) : null;
		});
		parserBuilder.define("Expression", "Expression", "-", "Expression").setAction((rule, data) -> {
			final BigInteger left = (BigInteger) data[0];
			final BigInteger right = (BigInteger) data[2];
			
			return left != null && right != null ? left.subtract(right) : null;
		});
		parserBuilder.define("Expression", "-", "Expression").setAction((rule, data) -> ((BigInteger) data[1]).negate());
		parserBuilder.define("Expression", "(", "Expression", ")").setAction((rule, data) -> data[1]);
		parserBuilder.define("Expression", "natural").setAction((rule, data) -> new BigInteger(data[0].toString()));
		parserBuilder.define("Expression", "variable").setAction((rule, data) -> {
			final BigInteger result = context.get(data[0].toString());
			
			if (result == null) {
				System.err.println("Undefined: " + data[0]);
			}
			
			return result;
		});
		
		parserBuilder.resolveConflictWith(block("Expression", "Expression"), block("(", "Expression", ")"));
		parserBuilder.resolveConflictWith(block("-", "Expression"), block("(", "Expression", ")"));
		parserBuilder.resolveConflictWith("Expression", "+", block("Expression", block("(", "Expression", ")")));
		parserBuilder.resolveConflictWith("Expression", "-", block("Expression", block("(", "Expression", ")")));
		parserBuilder.resolveConflictWith("(", block("Expression", "-", "Expression"), ")");
		
		parserBuilder.setPriority(300, NONE, "-", "Expression");
		parserBuilder.setPriority(300, LEFT, "Expression", "natural");
		parserBuilder.setPriority(300, LEFT, "Expression", "variable");
		parserBuilder.setPriority(300, LEFT, "Expression", "Expression");
		parserBuilder.setPriority(100, LEFT, "Expression", "+", "Expression");
		parserBuilder.setPriority(100, LEFT, "Expression", "-", "Expression");
		
		final LRParser parser = parserBuilder.newParser();
		
		help();
		
		try (final Scanner scanner = new Scanner(System.in)) {
			while (scanner.hasNext()) {
				if (!parser.parse(lexer.translate(tokens(scanner.nextLine())))) {
					System.err.println("Syntax error");
				}
			}
		}
	}
	
	public static final void help() {
		System.out.println("Instructions:");
		System.out.println("	:quit");
		System.out.println("	:help");
		System.out.println("	a=123");
		System.out.println("Expressions:");
		System.out.println("	12 + ab-(-3)");
		System.out.println();
	}
	
}
