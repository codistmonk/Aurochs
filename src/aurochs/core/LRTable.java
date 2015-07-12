package aurochs.core;

import static aurochs.core.StackItem.last;
import static multij.tools.Tools.cast;
import static multij.tools.Tools.join;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import aurochs.core.Grammar.Rule;
import aurochs.core.Grammar.RuleAction;
import multij.tools.Tools;

/**
 * @author codistmonk (creation 2014-08-24)
 */
public final class LRTable implements Serializable {
	
	private final Grammar grammar;
	
	private final List<Map<Object, List<LRTable.Action>>> actions;
	
	public LRTable(final ClosureTable closureTable) {
		this.grammar = closureTable.getGrammar();
		this.actions = new ArrayList<>();
		
		final List<? extends ClosureTable.State> states = closureTable.getStates();
		final int n = states.size();
		
		for (int i = 0; i < n; ++i) {
			this.actions.add(new HashMap<>());
		}
		
		
		for (int i = 0; i < n; ++i) {
			final ClosureTable.State state = states.get(i);
			final Map<Object, List<LRTable.Action>> stateActions = this.actions.get(i);
			
			for (final Map.Entry<Object, Integer> transition : state.getTransitions().entrySet()) {
				stateActions.compute(transition.getKey(), GET_OR_CREATE_ARRAY_LIST).add(
						new Shift(transition.getValue()));
			}
			
			for (final Map.Entry<Object, Collection<Integer>> reductions : state.getReductions().entrySet()) {
				final Collection<LRTable.Action> actions = stateActions.compute(
						reductions.getKey(), GET_OR_CREATE_ARRAY_LIST);
				
				for (final Integer ruleIndex : reductions.getValue()) {
					actions.add(new Reduce(this.getGrammar().getRules().get(ruleIndex)));
				}
			}
		}
	}
	
	public final Grammar getGrammar() {
		return this.grammar;
	}
	
	public final List<Map<Object, List<LRTable.Action>>> getActions() {
		return this.actions;
	}
	
	public final List<List<Object>> collectAmbiguousExamples() {
		final List<List<Object>> result = new ArrayList<>();
		final List<Map<Object, List<Action>>> actions = this.getActions();
		final int n = actions.size();
		
		for (int stateIndex = 0; stateIndex < n; ++stateIndex) {
			final Map<Object, List<Action>> stateActions = actions.get(stateIndex);
			
			for (final Map.Entry<Object, List<Action>> entry : stateActions.entrySet()) {
				if (1 < entry.getValue().size()) {
					final List<Integer> path = new ArrayList<>();
					final List<Object> ambiguousExample = new ArrayList<>();
					
					path.add(0, stateIndex);
					ambiguousExample.add(0, entry.getKey());
					
					int target = stateIndex;
					
					while (!path.contains(0)) {
						Action targetAction = new LRTable.Shift(target);
						boolean antecedentFound = false;
						
						for (int i = 0; i < n && !antecedentFound; ++i) {
							if (path.contains(i)) {
								continue;
							}
							
							for (final Map.Entry<Object, List<Action>> entry2 : actions.get(i).entrySet()) {
								if (entry2.getValue().contains(targetAction)) {
									ambiguousExample.add(0, entry2.getKey());
									path.add(0, i);
									target = i;
									antecedentFound = true;
									break;
								}
							}
						}
						
						if (!antecedentFound) {
							Tools.debugError("Couldn't find path to ambiguity " + entry);
							break;
						}
					}
					
					result.add(ambiguousExample);
				}
			}
		}
		
		return result;
	}
	
	public final void printAmbiguities() {
		final List<List<Object>> ambiguities = this.collectAmbiguousExamples();
		
		if (!ambiguities.isEmpty()) {
			Tools.getLoggerForThisMethod().warning(
					"Ambiguities detected (" + ambiguities.size() +"):\n" + join("\n", ambiguities.toArray()));
		}
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -3901998885688156104L;
	
	public static final BiFunction<? super Object, ? super List<Action>,
			? extends List<Action>> GET_OR_CREATE_ARRAY_LIST = (k, v) -> v == null ? new ArrayList<>() : v;
	
	/**
	 * @author codistmonk (creation 2014-08-24)
	 */
	public static abstract interface Action extends Serializable {
		
		public abstract void perform(List<StackItem> stack, TokenSource<?> tokens);
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-24)
	 */
	public static final class Shift implements LRTable.Action {
		
		private final int nextStateIndex;
		
		public Shift(final int nextStateIndex) {
			this.nextStateIndex = nextStateIndex;
		}
		
		public final int getNextStateIndex() {
			return this.nextStateIndex;
		}
		
		@Override
		public final void perform(final List<StackItem> stack, final TokenSource<?> tokens) {
			stack.add(new StackItem().setStateIndex(this.getNextStateIndex()).setToken(tokens.read().get()));
		}
		
		@Override
		public final int hashCode() {
			return this.getNextStateIndex();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final LRTable.Shift that = cast(this.getClass(), object);
			
			return that != null && this.getNextStateIndex() == that.getNextStateIndex();
		}
		
		@Override
		public final String toString() {
			return "s" + this.getNextStateIndex();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -882400449940537919L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-24)
	 */
	public static final class Reduce implements LRTable.Action {
		
		private final Rule rule;
		
		public Reduce(final Rule rule) {
			this.rule = rule;
		}
		
		public final Rule getRule() {
			return this.rule;
		}
		
		public final int getRuleIndex() {
			return this.getRule().getIndex();
		}
		
		@Override
		public final void perform(final List<StackItem> stack, final TokenSource<?> tokens) {
			final int stackSize = stack.size();
			final int developmentSize = this.getRule().getDevelopment().length;
			final List<StackItem> tail = stack.subList(stackSize - 1 - developmentSize, stackSize - 1);
			final RuleAction listener = this.getRule().getAction();
			final Object newToken = this.getRule().getNonterminal();
			Object newDatum = null;
			
			if (listener != null) {
				final Object[] data = new Object[developmentSize];
				
				for (int i = 0; i < developmentSize; ++i) {
					data[i] = tail.get(i).getDatum();
					
					while (data[i] instanceof Lexer.Token) {
						data[i] = ((Lexer.Token) data[i]).getDatum();
					}
				}
				
				newDatum = listener.execute(this.getRule(), data);
			}
			
			int nextStateIndex = last(stack).getStateIndex();
			
			if (!tail.isEmpty()) {
				nextStateIndex = tail.get(0).getStateIndex();
				tail.clear();
			}
			
			tokens.back();
			
			last(stack).setStateIndex(nextStateIndex).setToken(newToken).setDatum(newDatum);
		}
		
		@Override
		public final int hashCode() {
			return this.getRuleIndex();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final LRTable.Reduce that = cast(this.getClass(), object);
			
			return that != null && this.getRuleIndex() == that.getRuleIndex();
		}
		
		@Override
		public final String toString() {
			return "r" + this.getRuleIndex();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -1092278669508228566L;
		
	}
	
}