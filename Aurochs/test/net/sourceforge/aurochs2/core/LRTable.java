package net.sourceforge.aurochs2.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
					actions.add(new Reduce(ruleIndex));
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
		// NOP
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
		
		private final int ruleIndex;
		
		public Reduce(final int ruleIndex) {
			this.ruleIndex = ruleIndex;
		}
		
		public final int getRuleIndex() {
			return this.ruleIndex;
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