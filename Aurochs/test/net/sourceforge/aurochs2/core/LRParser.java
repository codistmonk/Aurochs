package net.sourceforge.aurochs2.core;

import static net.sourceforge.aurochs2.core.StackItem.last;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.aprog.tools.Tools;
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
		
		public final boolean parseAll(final TokenSource tokens) {
			final Object initialNonterminal = this.getGrammar().getRules().get(0).getNonterminal();
			final List<StackItem> stack = new ArrayList<>();
			
			stack.add(new StackItem().setStateIndex(0).setToken(tokens.read().get()));
			
			while (1 <= stack.size() && last(stack).getToken() != initialNonterminal) {
				final Action action = this.getAction(last(stack));
				
				if (action == null) {
					Tools.debugPrint();
					return false;
				}
				
				action.perform(stack, tokens);
				Tools.debugPrint(action, stack);
			}
			
			Tools.debugPrint(tokens.get());
			
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
		
		public final Action getAction(final StackItem stackItem) {
			final Collection<Action> actions = this.getTable().getActions()
					.get(stackItem.getStateIndex()).get(stackItem.getToken());
			
			if (actions == null || actions.isEmpty()) {
				return null;
			}
			
			return actions.iterator().next();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -7182999842019515805L;
		
	}