package net.sourceforge.aurochs2.core;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static net.sourceforge.aprog.tools.Tools.join;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.Pair;
import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs2.core.Grammar.Rule;
import net.sourceforge.aurochs2.core.Lexer.Token;
import net.sourceforge.aurochs2.core.LexerBuilder.TokenGenerator;

/**
 * @author codistmonk (creation 2014-08-26)
 */
public final class ParserBuilder implements Serializable {
	
	private final Grammar grammar;
	
	private final Collection<Object> lexerTokens;
	
	private final List<Priority> priorities;
	
	public ParserBuilder(final Lexer lexer) {
		this.grammar = new Grammar();
		this.lexerTokens = new HashSet<>();
		this.priorities = new ArrayList<>();
		
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
	
	public final void setPriority(final int priority, final Object... symbols) {
		this.priorities.add(new Priority(priority, Arrays.asList(symbols)));
	}
	
	public final Rule define(final Object nonterminal, final Object... development) {
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
		final LRParser result = new LRParser(new LRTable(table));
		final List<List<Object>> ambiguities = result.getTable().collectAmbiguousExamples();
		
		for (final List<Object> ambiguity : ambiguities) {
			final int ambiguitySize = ambiguity.size();
			final List<Pair<Integer, List<Object>>> prefixCovers = new ArrayList<>();
			final List<Pair<Integer, List<Object>>> suffixCovers = new ArrayList<>();
			
			for (final Priority priority : this.priorities) {
				final List<Object> symbols = priority.getSymbols();
				final int symbolsSize = symbols.size();
				
				{
					int goodOffset = Integer.MIN_VALUE;
					
					for (int symbolsOffset = -symbolsSize; symbolsOffset <= 0; ++symbolsOffset) {
						boolean offsetIsGood = true;
						
						for (int i = 0; i < min(ambiguitySize, symbolsOffset + symbolsSize); ++i) {
							if (!ambiguity.get(i).equals(symbols.get(i - symbolsOffset))) {
								offsetIsGood = false;
							}
						}
						
						if (offsetIsGood) {
							goodOffset = symbolsOffset;
						}
					}
					
					if (Integer.MIN_VALUE != goodOffset) {
						prefixCovers.add(new Pair<>(goodOffset, symbols));
					}
				}
				
				{
					int goodOffset = Integer.MAX_VALUE;
					
					for (int symbolsOffset = ambiguitySize - 1; ambiguitySize - symbolsSize <= symbolsOffset; --symbolsOffset) {
						boolean offsetIsGood = true;
						
						for (int i = max(0, symbolsOffset); i < ambiguitySize; ++i) {
							if (!ambiguity.get(i).equals(symbols.get(i - symbolsOffset))) {
								offsetIsGood = false;
							}
						}
						
						if (offsetIsGood) {
							goodOffset = symbolsOffset;
						}
					}
					
					if (Integer.MAX_VALUE != goodOffset) {
						suffixCovers.add(new Pair<>(goodOffset, symbols));
					}
				}
			}
			
			if (!prefixCovers.isEmpty() && !suffixCovers.isEmpty()) {
				Tools.debugPrint(ambiguity);
				Tools.debugPrint(prefixCovers);
				Tools.debugPrint(suffixCovers);
			}
		}
		
		if (!ambiguities.isEmpty()) {
			Tools.getLoggerForThisMethod().warning("Ambiguities:\n" + join("\n", ambiguities.toArray()));
		}
		
		return result;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -8371256689133167533L;
	
	public static final Token token(final Object symbol) {
		return new Token(symbol, symbol);
	}
	
	/**
	 * @author codistmonk (creation 2014-08-26)
	 */
	public static final class Priority implements Serializable {
		
		private final int priority;
		
		private final List<Object> symbols;
		
		public Priority(final int priority, final List<Object> symbols) {
			this.priority = priority;
			this.symbols = symbols;
		}
		
		public final int getPriority() {
			return this.priority;
		}
		
		public final List<Object> getSymbols() {
			return this.symbols;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 4902835510482603305L;
		
	}
	
}