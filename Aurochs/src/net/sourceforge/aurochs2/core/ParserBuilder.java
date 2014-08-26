package net.sourceforge.aurochs2.core;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static net.sourceforge.aprog.tools.Tools.append;
import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.join;
import static net.sourceforge.aurochs2.core.ParserBuilder.Priority.Associativity.LEFT;
import static net.sourceforge.aurochs2.core.ParserBuilder.Priority.Associativity.RIGHT;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import net.sourceforge.aprog.tools.Pair;
import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs2.core.Grammar.Rule;
import net.sourceforge.aurochs2.core.LRParser.ConflictResolver;
import net.sourceforge.aurochs2.core.Lexer.Token;
import net.sourceforge.aurochs2.core.LexerBuilder.TokenGenerator;
import net.sourceforge.aurochs2.core.ParserBuilder.Priority.Associativity;

/**
 * @author codistmonk (creation 2014-08-26)
 */
public final class ParserBuilder implements Serializable {
	
	private final Grammar grammar;
	
	private final Collection<Object> lexerTokens;
	
	private final List<Object[]> exampleTrees;
	
	private final List<Priority> priorities;
	
	public ParserBuilder(final Lexer lexer) {
		this.grammar = new Grammar();
		this.lexerTokens = new HashSet<>();
		this.exampleTrees = new ArrayList<>();
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
	
	public final void resolveConflictWith(final Object... exampleTree) {
		this.exampleTrees.add(exampleTree);
	}
	
	public final void setPriority(final int priority, final Associativity preferredAssociativity, final Object... symbols) {
		this.priorities.add(new Priority(priority, preferredAssociativity, this.tokenify(Arrays.asList(symbols))));
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
	
	public static final List<Object> flatten(final Object... tree) {
		final List<Object> result = new ArrayList<>();
		
		for (final Object object : tree) {
			if (object instanceof Object[]) {
				result.addAll(flatten((Object[]) object));
			} else {
				result.add(object);
			}
		}
		
		return result;
	}
	
	public final LRParser newParser(final ClosureTable table) {
		final LRParser result = new LRParser(new LRTable(table));
		
		this.resolveConflicts(result);
		
		printAmbiguities(result.getTable());
		
		return result;
	}
	
	private final void resolveConflicts(final LRParser result) {
		final ConflictResolver resolver = new ConflictResolver(result);
		
		for (final Object[] exampleTree : this.exampleTrees) {
			resolver.resolve(this.tokenify(flatten(exampleTree)), exampleTree);
		}
		
		final List<List<Object>> ambiguities = result.getTable().collectAmbiguousExamples();
		
		for (final List<Object> ambiguity : ambiguities) {
			final int ambiguitySize = ambiguity.size();
			final List<Pair<Integer, Priority>> prefixCovers = new ArrayList<>();
			final List<Pair<Integer, Priority>> suffixCovers = new ArrayList<>();
			
			for (final Priority priority : this.priorities) {
				final List<Object> symbols = priority.getSymbols();
				final int symbolsSize = symbols.size();
				
				{
					int goodOffset = Integer.MIN_VALUE;
					
					for (int symbolsOffset = -symbolsSize + 1; symbolsOffset <= 0; ++symbolsOffset) {
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
						prefixCovers.add(new Pair<>(goodOffset, priority));
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
						suffixCovers.add(new Pair<>(goodOffset, priority));
					}
				}
			}
			
			for (final Pair<Integer, Priority> prefixCover : prefixCovers) {
				final int prefixCoverOffset = prefixCover.getFirst();
				final int prefixCoverPriority = prefixCover.getSecond().getPriority();
				final Associativity prefixCoverAssociativity = prefixCover.getSecond().getPreferredAssociativity();
				final List<Object> prefixCoverSymbols = prefixCover.getSecond().getSymbols();
				final int prefixCoverSize = prefixCoverSymbols.size();
				
				for (final Pair<Integer, Priority> suffixCover : suffixCovers) {
					final int suffixCoverOffset = suffixCover.getFirst();
					final int suffixCoverPriority = suffixCover.getSecond().getPriority();
					final Associativity suffixCoverAssociativity = suffixCover.getSecond().getPreferredAssociativity();
					final List<Object> suffixCoverSymbols = suffixCover.getSecond().getSymbols();
					final int overlap = (prefixCoverSize + prefixCoverOffset) - suffixCoverOffset;
					final int suffixCoverSize = suffixCoverSymbols.size();
					
					if (0 < overlap) {
						final List<Object> tokens = new ArrayList<>(prefixCoverSize + suffixCoverSize - overlap);
						final Object[] expected;
						
						if (prefixCoverPriority < suffixCoverPriority
								|| (prefixCoverPriority == suffixCoverPriority
									&& (prefixCoverAssociativity == RIGHT || suffixCoverAssociativity == RIGHT)
									&& !(prefixCoverAssociativity == LEFT || suffixCoverAssociativity == LEFT))) {
							final List<Object> left = prefixCoverSymbols.subList(0, prefixCoverSize - overlap);
							final List<Object> right = suffixCoverSymbols;
							
							tokens.addAll(left);
							tokens.addAll(right);
							expected = append(stringify(left), bloc(stringify(right)));
						} else if (suffixCoverPriority < prefixCoverPriority
								|| (prefixCoverPriority == suffixCoverPriority
									&& (prefixCoverAssociativity == LEFT || suffixCoverAssociativity == LEFT)
									&& !(prefixCoverAssociativity == RIGHT || suffixCoverAssociativity == RIGHT))) {
							final List<Object> left = prefixCoverSymbols;
							final List<Object> right = suffixCoverSymbols.subList(overlap, suffixCoverSize);
							
							tokens.addAll(left);
							tokens.addAll(right);
							expected = append(bloc(stringify(left)), stringify(right));
						} else {
							expected = null;
						}
						
						if (expected != null) {
							resolver.resolve(tokens, expected);
						}
					}
				}
				
			}
		}
	}
	
	private final List<Object> tokenify(final List<Object> tokens) {
		final int n = tokens.size();
		
		for (int i = 0; i < n; ++i) {
			final Object token = tokens.get(i);
			
			if (this.lexerTokens.contains(token)) {
				tokens.set(i, token(token));
			}
		}
		
		return tokens;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -8371256689133167533L;
	
	public static final Token token(final Object symbol) {
		return new Token(symbol, symbol);
	}
	
	private static final Object[] stringify(final List<Object> symbols) {
		return symbols.stream().map(Object::toString).toArray();
	}
	
	private static final Object[] bloc(final Object object) {
		final Object[] asArray = cast(Object[].class, object);
		
		return asArray != null && asArray.length == 1 ? asArray : array(object);
	}
	
	public static final void printAmbiguities(final LRTable lrTable) {
		final List<List<Object>> ambiguities = lrTable.collectAmbiguousExamples();
		
		if (!ambiguities.isEmpty()) {
			Tools.getLoggerForThisMethod().warning("Ambiguities:\n" + join("\n", ambiguities.toArray()));
		}
	}
	
	/**
	 * @author codistmonk (creation 2014-08-26)
	 */
	public static final class Priority implements Serializable {
		
		private final int priority;
		
		private final Associativity preferredAssociativity;
		
		private final List<Object> symbols;
		
		public Priority(final int priority, final Associativity preferredAssociativity, final List<Object> symbols) {
			this.priority = priority;
			this.preferredAssociativity = preferredAssociativity;
			this.symbols = symbols;
		}
		
		public final int getPriority() {
			return this.priority;
		}
		
		public final Associativity getPreferredAssociativity() {
			return this.preferredAssociativity;
		}
		
		public final List<Object> getSymbols() {
			return this.symbols;
		}
		
		@Override
		public final String toString() {
			return this.getPriority() + ":" + this.getSymbols();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 4902835510482603305L;
		
		/**
		 * @author codistmonk (creation 2014-08-26)
		 */
		public static enum Associativity {
			
			LEFT, RIGHT, NONE;
			
		}
		
	}
	
}