package net.sourceforge.aurochs2.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import net.sourceforge.aurochs2.core.Grammar.Rule;
import net.sourceforge.aurochs2.core.Lexer.Token;
import net.sourceforge.aurochs2.core.LexerBuilder.TokenGenerator;

/**
 * @author codistmonk (creation 2014-08-26)
 */
public final class ParserBuilder implements Serializable {
	
	private final Grammar grammar;
	
	private final Collection<Object> lexerTokens;
	
	public ParserBuilder(final Lexer lexer) {
		this.grammar = new Grammar();
		this.lexerTokens = new HashSet<>();
		
		if (lexer != null) {
			for (final Rule rule : lexer.getParser().getGrammar().getRules()) {
				if (rule.getListener() instanceof TokenGenerator) {
					this.lexerTokens.add(rule.getNonterminal());
				}
			}
		}
	}
	
	public final Grammar getGrammar() {
		return this.grammar;
	}
	
	public final Rule addRule(final Object nonterminal, final Object... development) {
		final int n = development.length;
		final Object[] actualDevelopment = new Object[n];
		this.getGrammar().getNonterminals();
		
		for (int i = 0; i < n; ++i) {
			final Object symbol = development[i];
			actualDevelopment[i] = this.lexerTokens.contains(symbol) ? token(symbol) : symbol;
		}
		
		return this.getGrammar().new Rule(nonterminal, actualDevelopment);
	}
	
	public final LRParser newParser() {
		return this.newParser(new LALR1ClosureTable(this.getGrammar()));
	}
	
	public final LRParser newParser(final ClosureTable table) {
		return new LRParser(new LRTable(table));
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -8371256689133167533L;
	
	public static final Token token(final Object symbol) {
		return new Token(symbol, symbol);
	}
	
}