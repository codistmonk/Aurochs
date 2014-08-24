package net.sourceforge.aurochs2.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sourceforge.aurochs2.core.Grammar.ReductionListener;
import net.sourceforge.aurochs2.core.Grammar.Rule;

/**
 * @author codistmonk (creation 2014-08-24)
 */
public final class LRTable implements Serializable {
	
	private final Grammar grammar;
	
	private final List<Map<Object, Collection<LRTable.Action>>> actions;
	
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
			final Map<Object, Collection<LRTable.Action>> stateActions = this.actions.get(i);
			
			for (final Map.Entry<Object, Integer> transition : state.getTransitions().entrySet()) {
				stateActions.compute(transition.getKey(),
						(k, v) -> v == null ? new HashSet<>() : v).add(new Shift(transition.getValue()));
			}
			
			for (final Map.Entry<Object, Collection<Integer>> reductions : state.getReductions().entrySet()) {
				final Collection<LRTable.Action> actions = stateActions.compute(
						reductions.getKey(), (k, v) -> v == null ? new HashSet<>() : v);
				
				for (final Integer ruleIndex : reductions.getValue()) {
					actions.add(new Reduce(this.getGrammar().getRules().get(ruleIndex)));
				}
			}
		}
	}
	
	public final Grammar getGrammar() {
		return this.grammar;
	}
	
	public final List<Map<Object, Collection<LRTable.Action>>> getActions() {
		return this.actions;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -3901998885688156104L;
	
	/**
	 * @author codistmonk (creation 2014-08-24)
	 */
	public static abstract interface Action extends Serializable {
		
		public abstract void perform(List<StackItem> stack, TokenSource tokens);
		
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
		public final void perform(final List<StackItem> stack, final TokenSource tokens) {
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
		public final void perform(final List<StackItem> stack, final TokenSource tokens) {
			final int stackSize = stack.size();
			final int developmentSize = this.getRule().getDevelopment().length;
			final List<StackItem> tail = stack.subList(stackSize - developmentSize, stackSize);
			final ReductionListener listener = this.getRule().getListener();
			final Object newToken = this.getRule().getNonterminal();
			Object newDatum = null;
			
			if (listener != null) {
				final Object[] data = new Object[developmentSize];
				
				for (int i = 0; i < developmentSize; ++i) {
					data[i] = tail.get(i).getDatum();
				}
				
				newDatum = listener.reduction(this.getRule(), tail.toArray());
			}
			
			tail.clear();
			tokens.back();
			
			StackItem.last(stack).setToken(newToken).setDatum(newDatum);
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