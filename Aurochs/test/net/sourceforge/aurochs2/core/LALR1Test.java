package net.sourceforge.aurochs2.core;

import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.set;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs2.core.LALR1Test.Grammar.Rule;

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
	}
	
	/**
	 * @author codistmonk (creation 2014-08-24)
	 */
	public static final class LALR1ClosureTable implements Serializable {
		
		private final List<State> states;
		
		public LALR1ClosureTable(final Grammar grammar) {
			this.states = new ArrayList<State>();
			
			this.states.add(new State(grammar, set(new Item(grammar.getRules().get(0), 0, set(Special.END_TERMINAL)))));
			
			Tools.debugPrint(this.states.get(0).getKernel(), this.states.get(0).getClosure());
		}
		
		public final List<State> getStates() {
			return this.states;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -632237351102999005L;
		
		/**
		 * @author codistmonk (creation 2014-08-24)
		 */
		public final class State implements Serializable {
			
			private final Collection<Item> kernel;
			
			private final Set<Item> closure;
			
			public State(final Grammar grammar, final Collection<Item> kernel) {
				this.kernel = kernel;
				this.closure = new HashSet<>();
				
				final List<Item> todo = new ArrayList<>(kernel);
				
				while (!todo.isEmpty()) {
					final Item item = todo.remove(0);
					boolean addItemToClosure = true;
					
					for (final Item existingItem : this.closure) {
						if (item.getRule().getIndex() == existingItem.getRule().getIndex()
								&& item.getCursorIndex() == existingItem.getCursorIndex()) {
							existingItem.getLookAheads().addAll(item.getLookAheads());
							addItemToClosure = false;
							break;
						}
					}
					
					if (addItemToClosure) {
						this.closure.add(item);
						
						if (item.hasNextSymbol()) {
							final Object nextSymbol = item.getNextSymbol();
							final Set<Object> nextLookAheads = item.getNextLookAheads();
							
							for (final Rule rule : grammar.getRules()) {
								if (nextSymbol.equals(rule.getNonterminal())) {
									todo.add(new Item(rule, 0, nextLookAheads));
								}
							}
						}
					}
				}
			}
			
			public final Collection<Item> getKernel() {
				return this.kernel;
			}
			
			public final Collection<Item> getClosure() {
				return this.closure;
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = 4682355118829727025L;
			
		}
		
		/**
		 * @author codistmonk (creation 2014-08-24)
		 */
		public static enum Special {
			
			END_TERMINAL;
			
		}
		
		/**
		 * @author codistmonk (creation 2014-08-24)
		 */
		public static final class Item implements Serializable {
			
			private final Grammar.Rule rule;
			
			private final int cursorIndex;
			
			private final Set<Object> lookAheads;
			
			public Item(final Rule rule, final int cursorIndex, final Set<Object> lookAheads) {
				this.rule = rule;
				this.cursorIndex = cursorIndex;
				this.lookAheads = lookAheads;
			}
			
			public final Grammar.Rule getRule() {
				return this.rule;
			}
			
			public final int getCursorIndex() {
				return this.cursorIndex;
			}
			
			public final Set<Object> getLookAheads() {
				return this.lookAheads;
			}
			
			public final boolean hasNextSymbol() {
				return this.getCursorIndex() < this.getRule().getDevelopment().length;
			}
			
			public final Object getNextSymbol() {
				return this.getRule().getDevelopment()[this.getCursorIndex()];
			}
			
			public final Set<Object> getNextLookAheads() {
				final Grammar grammar = this.getRule().getGrammar();
				final Set<Object> nonterminals = grammar.getNonterminals();
				final Set<Object> collapsables = grammar.getCollapsables();
				final Map<Object, Collection<Object>> firsts = grammar.getFirsts();
				final Set<Object> result = new HashSet<>();
				final Object[] development = this.getRule().getDevelopment();
				final int n = development.length;
				int i;
				
				for (i = this.getCursorIndex() + 1; i < n; ++i) {
					final Object symbol = development[i];
					final boolean symbolIsTerminal = !nonterminals.contains(symbol);
					
					if (symbolIsTerminal) {
						result.add(symbol);
						break;
					}
					
					result.addAll(firsts.get(symbol));
					
					if (!collapsables.contains(symbol)) {
						break;
					}
				}
				
				if (i == n) {
					result.addAll(this.getLookAheads());
				}
				
				return result;
			}
			
			@Override
			public final int hashCode() {
				return this.getRule().getIndex() + this.getCursorIndex() + this.getLookAheads().hashCode();
			}
			
			@Override
			public final boolean equals(final Object object) {
				final Item that = cast(this.getClass(), object);
				
				return that != null
						&& this.getRule().getIndex() == that.getRule().getIndex()
						&& this.getCursorIndex() == that.getCursorIndex() &&
						this.getLookAheads().equals(that.getLookAheads());
			}
			
			@Override
			public final String toString() {
				final StringBuilder resultBuilder = new StringBuilder();
				final Object[] development = this.getRule().getDevelopment();
				final int n = development.length;
				
				resultBuilder.append('[').append(this.getRule().getNonterminal()).append(" -> ");
				
				for (int i = 0; i < n; ++i) {
					resultBuilder.append(i == this.getCursorIndex() ? '.' : ' ').append(development[i]);
				}
				
				resultBuilder.append(", ").append(Tools.join("/", this.getLookAheads().toArray())).append(']');
				
				return resultBuilder.toString();
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = 6541348303501689133L;
			
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-24)
	 */
	public static final class Grammar implements Serializable {
		
		private final List<Rule> rules = new ArrayList<>();
		
		private final Set<Object> nonterminals = new HashSet<>();
		
		private final Set<Object> collapsables = new HashSet<>();
		
		private Map<Object, Collection<Object>> firsts;
		
		public final List<Rule> getRules() {
			return this.rules;
		}
		
		public final Set<Object> getNonterminals() {
			return this.nonterminals;
		}
		
		public final Set<Object> getCollapsables() {
			return this.collapsables;
		}
		
		public final Map<Object, Collection<Object>> getFirsts() {
			if (this.firsts == null) {
				final Set<Object> nonterminals = this.getNonterminals();
				this.firsts = new HashMap<>();
				
				for (final Object nonterminal: nonterminals) {
					this.firsts.put(nonterminal, new HashSet<>());
				}
				
				boolean notDone;
				
				do {
					notDone = false;
					
					for (final Rule rule : this.getRules()) {
						final Object nonterminal = rule.getNonterminal();
						final Object[] development = rule.getDevelopment();
						final int n = development.length;
						final Collection<Object> nonterminalFirsts = this.firsts.get(nonterminal);
						int i;
						
						for (i = 0; i < n; ++i) {
							final Object symbol = development[i];
							final boolean symbolIsTerminal = !nonterminals.contains(symbol);
							
							if (symbolIsTerminal) {
								notDone |= nonterminalFirsts.add(symbol);
								break;
							}
							
							notDone |= nonterminalFirsts.addAll(this.firsts.get(symbol));
							
							if (!this.collapsables.contains(symbol)) {
								break;
							}
						}
						
						if (i == n) {
							notDone |= this.collapsables.add(nonterminal);
						}
					}
				} while (notDone);
			}
			
			return this.firsts;
		}
		
		final void checkEditable() {
			if (this.firsts != null) {
				throw new IllegalStateException();
			}
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 6864077963839662803L;
		
		/**
		 * @author codistmonk (creation 2014-08-24)
		 */
		public final class Rule implements Serializable {
			
			private final int index;
			
			private final Object nonterminal;
			
			private final Object[] development;
			
			public Rule(final Object nonterminal, final Object... development) {
				Grammar.this.checkEditable();
				
				final List<Rule> rules = Grammar.this.getRules();
				
				this.index = rules.size();
				this.nonterminal = nonterminal;
				this.development = development;
				
				rules.add(this);
				
				Grammar.this.getNonterminals().add(nonterminal);
			}
			
			public final Grammar getGrammar() {
				return Grammar.this;
			}
			
			public final int getIndex() {
				return this.index;
			}
			
			public final Object getNonterminal() {
				return this.nonterminal;
			}
			
			public final Object[] getDevelopment() {
				return this.development;
			}
			
			@Override
			public final String toString() {
				return this.getNonterminal() + " -> " + Arrays.toString(this.getDevelopment());
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = 5775468391445234490L;
			
		}
		
		/**
		 * @author codistmonk (creation 2014-08-24)
		 */
		public static abstract interface ReductionListener extends Serializable {
			
		}
		
	}
	
}
