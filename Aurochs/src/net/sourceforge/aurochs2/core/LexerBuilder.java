package net.sourceforge.aurochs2.core;

import static net.sourceforge.aprog.tools.Tools.join;

import java.io.Serializable;

import net.sourceforge.aurochs2.core.Grammar.RuleAction;
import net.sourceforge.aurochs2.core.Grammar.Rule;
import net.sourceforge.aurochs2.core.Lexer.Token;

/**
 * @author codistmonk (creation 2014-08-25)
 */
public final class LexerBuilder implements Serializable {
	
	private final Grammar grammar;
	
	private final Token[] tokenBox;
	
	private final LexerBuilder.TokenGenerator defaultTokenGenerator;
	
	private final RuleAction defaultRuleAction;
	
	private final Object initialNonterminal;
	
	private final Object commonNonterminal;
	
	private int newToken;
	
	public LexerBuilder() {
		this(new StringTokenGenerator(new Token[1]), StringCollector.INSTANCE);
	}
	
	public LexerBuilder(final LexerBuilder.TokenGenerator defaultTokenGenerator, final RuleAction defaultRuleAction) {
		this.grammar = new Grammar();
		this.tokenBox = defaultTokenGenerator.getTokenBox();
		this.defaultTokenGenerator = defaultTokenGenerator;
		this.defaultRuleAction = defaultRuleAction;
		this.initialNonterminal = this.newToken();
		this.commonNonterminal = this.newToken();
		
		this.grammar.new Rule(this.initialNonterminal, this.commonNonterminal);
	}
	
	public final TokenGenerator getDefaultTokenGenerator() {
		return this.defaultTokenGenerator;
	}
	
	public final RuleAction getDefaultRuleAction() {
		return this.defaultRuleAction;
	}
	
	public final Token[] getTokenBox() {
		return this.tokenBox;
	}
	
	public final Lexer newLexer(final ClosureTable closureTable) {
		final Lexer result = new Lexer(new LRParser(new LRTable(closureTable)), this.getTokenBox());
		
		result.getParser().getTable().printAmbiguities();
		
		return result;
	}
	
	public final Lexer newLexer() {
		return this.newLexer(new LALR1ClosureTable(this.getGrammar()));
	}
	
	public final Grammar getGrammar() {
		return this.grammar;
	}
	
	public final Rule generate(final Object token, final Object... development) {
		this.getGrammar().new Rule(this.commonNonterminal, token);
		
		return this.getGrammar().new Rule(token,
				this.computeActualDevelopment(development)).setAction(this.getDefaultTokenGenerator());
	}
	
	public final Rule skip(final Object... development) {
		final Object token = this.newToken();
		
		this.getGrammar().new Rule(this.commonNonterminal, token);
		
		return this.define(token, development);
	}
	
	public final Rule define(final Object nonterminal, final Object... development) {
		return this.getGrammar().new Rule(nonterminal,
				this.computeActualDevelopment(development)).setAction(this.getDefaultRuleAction());
	}
	
	public final Object newToken() {
		return ++this.newToken;
	}
	
	final Object symbol(final Object symbol) {
		return symbol instanceof LexerBuilder.Regular ?
				((LexerBuilder.Regular) symbol).updateRules(this) : symbol;
	}
	
	private final Object[] computeActualDevelopment(final Object... development) {
		final int n = development.length;
		final Object[] result = new Object[n];
		
		for (int i = 0; i < n; ++i) {
			result[i] = this.symbol(development[i]);
		}
		
		return result;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 6798178708273123305L;
	
	public static final LexerBuilder.ZeroOrOne zeroOrOne(final Object symbol) {
		return new ZeroOrOne(symbol);
	}
	
	public static final LexerBuilder.ZeroOrMore zeroOrMore(final Object symbol) {
		return new ZeroOrMore(symbol);
	}
	
	public static final LexerBuilder.OneOrMore oneOrMore(final Object symbol) {
		return new OneOrMore(symbol);
	}
	
	public static final LexerBuilder.Union union(final Object... symbols) {
		return new Union(symbols);
	}
	
	public static final LexerBuilder.Sequence sequence(final Object... symbols) {
		return new Sequence(symbols);
	}
	
	public static final Object[] range(final char first, final char last) {
		final Object[] result = new Object[1 + last - first];
		
		for (char c = first; c <= last; ++c) {
			result[c - first] = c;
		}
		
		return result;
	}
	
	/**
	 * @author codistmonk (creation 2014-08-25)
	 */
	public static abstract interface Regular extends Serializable {
		
		public abstract Object updateRules(LexerBuilder lexerBuilder);
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-25)
	 */
	public static final class ZeroOrOne implements LexerBuilder.Regular {
		
		private final Object symbol;
		
		public ZeroOrOne(final Object symbol) {
			this.symbol = symbol;
		}
		
		public final Object getSymbol() {
			return this.symbol;
		}
		
		@Override
		public final Object updateRules(final LexerBuilder lexerBuilder) {
			final Object result = lexerBuilder.newToken();
			final Object symbol = lexerBuilder.symbol(this.getSymbol());
			
			lexerBuilder.define(result, symbol);
			lexerBuilder.define(result);
			
			return result;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 7370102693026567533L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-25)
	 */
	public static final class ZeroOrMore implements LexerBuilder.Regular {
		
		private final Object symbol;
		
		public ZeroOrMore(final Object symbol) {
			this.symbol = symbol;
		}
		
		public final Object getSymbol() {
			return this.symbol;
		}
		
		@Override
		public final Object updateRules(final LexerBuilder lexerBuilder) {
			final Object result = lexerBuilder.newToken();
			final Object symbol = lexerBuilder.symbol(this.getSymbol());
			
			lexerBuilder.define(result, symbol, result);
			lexerBuilder.define(result);
			
			return result;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 1464714668647993745L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-25)
	 */
	public static final class OneOrMore implements LexerBuilder.Regular {
		
		private final Object symbol;
		
		public OneOrMore(final Object symbol) {
			this.symbol = symbol;
		}
		
		public final Object getSymbol() {
			return this.symbol;
		}
		
		@Override
		public final Object updateRules(final LexerBuilder lexerBuilder) {
			final Object result = lexerBuilder.newToken();
			final Object symbol = lexerBuilder.symbol(this.getSymbol());
			
			lexerBuilder.define(result, symbol, result);
			lexerBuilder.define(result, symbol);
			
			return result;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -1443270719375734812L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-25)
	 */
	public static final class Union implements LexerBuilder.Regular {
		
		private final Object[] symbols;
		
		public Union(final Object[] symbols) {
			this.symbols = symbols;
		}
		
		public final Object[] getSymbols() {
			return this.symbols;
		}
		
		@Override
		public final Object updateRules(final LexerBuilder lexerBuilder) {
			final Object result = lexerBuilder.newToken();
			
			for (final Object symbol : this.getSymbols()) {
				lexerBuilder.define(result, lexerBuilder.symbol(symbol));
			}
			
			return result;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 7575577428161932486L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-25)
	 */
	public static final class Sequence implements LexerBuilder.Regular {
		
		private final Object[] symbols;
		
		public Sequence(final Object[] symbols) {
			this.symbols = symbols;
		}
		
		public final Object[] getSymbols() {
			return this.symbols;
		}
		
		@Override
		public final Object updateRules(final LexerBuilder lexerBuilder) {
			final Object result = lexerBuilder.newToken();
			
			lexerBuilder.define(result, this.getSymbols());
			
			return result;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 8289572608528690480L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-25)
	 */
	public static abstract class TokenGenerator implements RuleAction {
		
		private final Token[] tokenBox;
		
		protected TokenGenerator(final Token[] tokenBox) {
			this.tokenBox = tokenBox;
		}
		
		public final Token[] getTokenBox() {
			return this.tokenBox;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -4228845664798392390L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-25)
	 */
	public static final class StringTokenGenerator extends LexerBuilder.TokenGenerator {
		
		public StringTokenGenerator(final Token[] tokenBox) {
			super(tokenBox);
		}
		
		@Override
		public final Object execute(final Rule rule, final Object[] data) {
			this.getTokenBox()[0] = new Token(rule.getNonterminal(), join("", data));
			
			return null;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 6122957581557730089L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-25)
	 */
	public static final class StringCollector implements RuleAction {
		
		@Override
		public final Object execute(final Rule rule, final Object[] data) {
			return join("", data);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -8547668424510589377L;
		
		public static final LexerBuilder.StringCollector INSTANCE = new StringCollector();
		
	}
	
}