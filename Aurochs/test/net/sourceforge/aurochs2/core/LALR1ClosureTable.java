package net.sourceforge.aurochs2.core;

import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.set;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs2.core.Grammar.Rule;

/**
 * @author codistmonk (creation 2014-08-24)
 */
public final class LALR1ClosureTable implements ClosureTable {
	
	private final Grammar grammar;
	
	private final List<State> states;
	
	public LALR1ClosureTable(final Grammar grammar) {
		this.grammar = grammar;
		this.states = new ArrayList<State>();
		
		this.states.add(new State(grammar, set(new Item(null,
				grammar.getRules().get(0), 0, set(Grammar.Special.END_TERMINAL)))));
		
		for (int i = 0; i < this.states.size(); ++i) {
			final State state = this.states.get(i);
			final Map<Object, Collection<LALR1ClosureTable.Item>> nextKernels = state.computeNextKernels();
			
			for (final Map.Entry<Object, Collection<LALR1ClosureTable.Item>> entry : nextKernels.entrySet()) {
				final Collection<LALR1ClosureTable.Item> entryKernel = entry.getValue();
				boolean addNewState = true;
				
				for (int j = 0; j < this.states.size(); ++j) {
					final boolean existingKernelFound = matchAndConnectKernels(entryKernel,
							this.states.get(j).getKernel());
					
					if (existingKernelFound) {
						addNewState = false;
						
						if (state.getTransitions().put(entry.getKey(), j) != null) {
							throw new IllegalStateException();
						}
						
						break;
					}
				}
				
				if (addNewState) {
					state.getTransitions().put(entry.getKey(), this.states.size());
					this.states.add(new State(grammar, entryKernel));
				}
			}
		}
		
		this.propagateLookAheads();
		
//		for (int i = 0; i < this.states.size(); ++i) {
//			Tools.debugPrint(i, this.states.get(i).getClosure());
//		}
	}
	
	@Override
	public final Grammar getGrammar() {
		return this.grammar;
	}
	
	@Override
	public final List<State> getStates() {
		return this.states;
	}
	
	private final void propagateLookAheads() {
		boolean notDone;
		
		do {
			notDone = false;
			
			for (final State state : this.states) {
				notDone |= state.propagateLookAheads();
			}
		} while (notDone);
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -632237351102999005L;
	
	public static final boolean matchAndConnectKernels(final Collection<LALR1ClosureTable.Item> entryKernel,
			final Collection<LALR1ClosureTable.Item> stateJKernel) {
		boolean result = false;
		
		if (entryKernel.containsAll(stateJKernel)) {
			int matchCount = 0;
			
			for (final LALR1ClosureTable.Item entryItem : entryKernel) {
				for (final LALR1ClosureTable.Item stateJItem : stateJKernel) {
					if (entryItem.equals(stateJItem)) {
						entryItem.getLookAheadPropagationTargets().add(stateJItem);
						++matchCount;
					}
				}
			}
			
			result = matchCount == entryKernel.size();
		}
		
		return result;
	}
	
	/**
	 * @author codistmonk (creation 2014-08-24)
	 */
	public final class State implements ClosureTable.State {
		
		private final Collection<LALR1ClosureTable.Item> kernel;
		
		private final Set<LALR1ClosureTable.Item> closure;
		
		private final Map<Object, Integer> transitions;
		
		public State(final Grammar grammar, final Collection<LALR1ClosureTable.Item> kernel) {
			this.kernel = kernel;
			this.closure = new HashSet<>();
			this.transitions = new HashMap<>();
			
			final List<LALR1ClosureTable.Item> todo = new ArrayList<>(kernel);
			
			while (!todo.isEmpty()) {
				final LALR1ClosureTable.Item item = todo.remove(0);
				boolean addItemToClosure = true;
				
				for (final LALR1ClosureTable.Item existingItem : this.closure) {
					if (item.equals(existingItem)) {
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
								todo.add(new Item(item, rule, 0, new HashSet<>(nextLookAheads)));
							}
						}
					}
				}
			}
		}
		
		public final Map<Object, Collection<LALR1ClosureTable.Item>> computeNextKernels() {
			final Map<Object, Collection<LALR1ClosureTable.Item>> result = new HashMap<>();
			
			for (final LALR1ClosureTable.Item item : this.getClosure()) {
				if (item.hasNextSymbol()) {
					final LALR1ClosureTable.Item newItem = new Item(item, item.getRule(), item.getCursorIndex() + 1,
							new HashSet<>(item.getLookAheads()));
					result.compute(
							item.getNextSymbol(), (k, v) -> v == null ? new HashSet<>() : v).add(newItem);
				}
			}
			
			return result;
		}
		
		public final boolean propagateLookAheads() {
			boolean result = false;
			
			for (final LALR1ClosureTable.Item item : this.getKernel()) {
				result |= item.propagateLookAheads();
			}
			
			for (final LALR1ClosureTable.Item item : this.getClosure()) {
				result |= item.propagateLookAheads();
			}
			
			return result;
		}
		
		public final Collection<LALR1ClosureTable.Item> getKernel() {
			return this.kernel;
		}
		
		public final Collection<LALR1ClosureTable.Item> getClosure() {
			return this.closure;
		}
		
		@Override
		public final Map<Object, Integer> getTransitions() {
			return this.transitions;
		}
		
		@Override
		public final Map<Object, Collection<Integer>> getReductions() {
			final Map<Object, Collection<Integer>> result = new HashMap<>();
			
			for (final LALR1ClosureTable.Item item : this.getClosure()) {
				if (!item.hasNextSymbol()) {
					final Integer ruleIndex = item.getRule().getIndex();
					
					for (final Object lookAhead : item.getLookAheads()) {
						result.compute(lookAhead,
								(k, v) -> v == null ? new HashSet<>() : v).add(ruleIndex);
						
					}
				}
			}
			
			return result;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 4682355118829727025L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-24)
	 */
	public static final class Item implements Serializable {
		
		private final Grammar.Rule rule;
		
		private final int cursorIndex;
		
		private final Set<Object> lookAheads;
		
		private final Collection<LALR1ClosureTable.Item> lookAheadPropagationTargets;
		
		public Item(final Item parent, final Rule rule, final int cursorIndex, final Set<Object> lookAheads) {
			this.rule = rule;
			this.cursorIndex = cursorIndex;
			this.lookAheads = lookAheads;
			this.lookAheadPropagationTargets = new ArrayList<>();
			
			if (parent != null) {
				parent.getLookAheadPropagationTargets().add(this);
			}
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
		
		public final Collection<LALR1ClosureTable.Item> getLookAheadPropagationTargets() {
			return this.lookAheadPropagationTargets;
		}
		
		public final boolean propagateLookAheads() {
			boolean result = false;
			
			for (final LALR1ClosureTable.Item target : this.getLookAheadPropagationTargets()) {
				if (target.getLookAheads().addAll(this.getLookAheads())) {
					result = true;
					
					target.propagateLookAheads();
				}
			}
			
			return result;
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
			return this.getRule().getIndex() + this.getCursorIndex();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final LALR1ClosureTable.Item that = cast(this.getClass(), object);
			
			return that != null
					&& this.getRule().getIndex() == that.getRule().getIndex()
					&& this.getCursorIndex() == that.getCursorIndex();
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
			
			if (this.getCursorIndex() == n) {
				resultBuilder.append('.');
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