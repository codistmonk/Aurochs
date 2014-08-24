package net.sourceforge.aurochs2.core;

import static net.sourceforge.aprog.tools.Tools.array;
import static net.sourceforge.aurochs2.core.StackItem.last;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sourceforge.aprog.tools.Pair;
import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs2.core.Grammar.ReductionListener;
import net.sourceforge.aurochs2.core.Grammar.Rule;
import net.sourceforge.aurochs2.core.Grammar.Special;
import net.sourceforge.aurochs2.core.LRTable.Action;

/**
	 * @author codistmonk (creation 2014-08-24)
	 */
	public final class LRParser implements Serializable {
		
		private final Grammar grammar;
		
		private final LRTable table;
		
		public LRParser(final LRTable table) {
			this.grammar = table.getGrammar();
			this.table = table;
		}
		
		public final Grammar getGrammar() {
			return this.grammar;
		}
		
		public final LRTable getTable() {
			return this.table;
		}
		
		/**
		 * @author codistmonk (creation 2014-08-24)
		 */
		public static final class ConflictResolver implements Serializable {
			
			private final List<Integer> actionChoices = new ArrayList<>();
			
			private Mode mode = Mode.TRY_NEXT;
			
			public final List<Integer> getActionChoices() {
				return this.actionChoices;
			}
			
			public final Mode getMode() {
				return this.mode;
			}
			
			public final ConflictResolver setMode(final Mode mode) {
				this.mode = mode;
				
				return this;
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = -2269472491106015129L;
			
			public static final Grammar setup(final Grammar grammar) {
				for (final Rule rule : grammar.getRules()) {
					rule.setListener(TreeCollector.INSTANCE);
				}
				
				return grammar;
			}
			
			/**
			 * @author codistmonk (creation 2014-08-24)
			 */
			public static enum Mode {
				
				TRY_NEXT, ACCEPT_CURRENT;
				
			}
			
			/**
			 * @author codistmonk (creation 2014-08-24)
			 */
			public static final class TreeCollector implements ReductionListener {
				
				@Override
				public final Object reduction(final Rule rule, final Object[] data) {
					return data;
				}
				
				/**
				 * {@value}.
				 */
				private static final long serialVersionUID = 5127951951772222324L;
				
				public static final TreeCollector INSTANCE = new TreeCollector();
				
			}
			
		}
		
		public final Object parseAll(final TokenSource tokens, final ConflictResolver resolver) {
			final Object initialNonterminal = this.getGrammar().getRules().get(0).getNonterminal();
			final List<StackItem> stack = new ArrayList<>();
			int nextChoiceDelta = 1;
			int choiceIndex = 0;
			
			stack.add(new StackItem().setStateIndex(0).setToken(tokens.read().get()));
			
			while (1 <= stack.size() && last(stack).getToken() != initialNonterminal) {
				final List<Action> actions = this.getActions(last(stack));
				
				if (actions == null) {
					return false;
				}
				
				Action action = actions.get(0);
				
				if (1 < actions.size()) {
					switch (resolver.getMode()) {
					case ACCEPT_CURRENT:
						if (choiceIndex < resolver.getActionChoices().size()) {
							action = actions.get(resolver.getActionChoices().get(choiceIndex));
						}
						
						actions.clear();
						actions.add(action);
						
						break;
					case TRY_NEXT:
						Tools.debugPrint(choiceIndex, resolver.getActionChoices().size());
						
						if (choiceIndex < resolver.getActionChoices().size()) {
							int choice = resolver.getActionChoices().get(choiceIndex) + nextChoiceDelta;
							
							if (choice == actions.size()) {
								choice = 0;
								nextChoiceDelta = 1;
							} else {
								nextChoiceDelta = 0;
							}
							
							action = actions.get(choice);
							resolver.getActionChoices().set(choiceIndex, choice);
						} else {
							resolver.getActionChoices().add(0);
						}
						
						break;
					}
					
					++choiceIndex;
				}
				
				action.perform(stack, tokens);
			}
			
			if (tokens.get() != Special.END_TERMINAL) {
				throw new IllegalStateException();
			}
			
			return last(stack).getDatum();
		}
		
		public final boolean parseAll(final TokenSource tokens) {
			final Object initialNonterminal = this.getGrammar().getRules().get(0).getNonterminal();
			final List<StackItem> stack = new ArrayList<>();
			
			stack.add(new StackItem().setStateIndex(0).setToken(tokens.read().get()));
			
			while (1 <= stack.size() && last(stack).getToken() != initialNonterminal) {
				final Action action = this.getAction(last(stack));
				
				if (action == null) {
					return false;
				}
				
				action.perform(stack, tokens);
			}
			
			
			return tokens.get() == Special.END_TERMINAL;
		}
		
//		public final boolean parsePrefix(final TokenSource tokens) {
//			final LRTable.Reduce r0 = new LRTable.Reduce(this.getGrammar().getRules().get(0));
//			final List<Object> stack = new ArrayList<>();
//			int stateIndex = 0;
//			Action action;
//			Object token;
//			
//			do {
//				token = tokens.read().get();
//				action = this.getAction(last(stack));
//				
//				if (action != null) {
//					stateIndex = action.perform(stateIndex, tokens, stack);
//				}
//			} while (action != null && !r0.equals(action));
//			
//			return action != null;
//		}
		
		public final List<Action> getActions(final StackItem stackItem) {
			final List<Action> actions = this.getTable().getActions()
					.get(stackItem.getStateIndex()).get(stackItem.getToken());
			
			if (actions == null || actions.isEmpty()) {
				return null;
			}
			
			return actions;
		}
		
		public final Action getAction(final StackItem stackItem) {
			final List<Action> actions = this.getActions(stackItem);
			
			if (actions == null) {
				return null;
			}
			
			return actions.get(0);
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -7182999842019515805L;
		
	}