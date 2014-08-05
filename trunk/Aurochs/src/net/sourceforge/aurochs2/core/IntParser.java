package net.sourceforge.aurochs2.core;

import static net.sourceforge.aprog.tools.Tools.unchecked;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;

/**
 * @author codistmonk (creation 2014-08-05)
 */
public final class IntParser implements Serializable {
	
	private final Context context;
	
	private State state;
	
	public IntParser(final State initialState, final InputStream input) {
		this.context = new Context(input);
		this.state = initialState;
	}
	
	public final State getState() {
		return this.state;
	}
	
	public final Context getContext() {
		return this.context;
	}
	
	public final Status step() {
		this.state = this.getState().processNextToken(this.getContext());
		
		if (this.getState() == null) {
			return Status.ERROR;
		}
		
		final boolean contextIsDone = this.getContext().isDone();
		final boolean stateIsTerminal = this.getState().getTransitions().isEmpty();
		
		if (contextIsDone && stateIsTerminal) {
			return Status.DONE;
		}
		
		if (!contextIsDone && stateIsTerminal) {
			return Status.ERROR;
		}
		
		return Status.CONTINUE;
	}
	
	public final boolean parse() {
		Status status = this.step();
		
		while (Status.CONTINUE == status) {
			status = this.step();
		}
		
		return Status.DONE == status;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -6584346545524768354L;
	
	/**
	 * @author codistmonk (creation 2014-08-05)
	 */
	public static enum Status {
		
		CONTINUE, DONE, ERROR;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-05)
	 */
	public static final class Context implements Serializable {
		
		private final List<Integer> stack;
		
		private final List<Integer> bufferedTokens;
		
		private final InputStream input;
		
		private int currentToken;
		
		public Context(final InputStream input) {
			this.stack = new LinkedList<>();
			this.bufferedTokens = new LinkedList<>();
			this.input = input;
		}
		
		public final List<Integer> getStack() {
			return this.stack;
		}
		
		public final boolean isDone() {
			try {
				return this.getStack().size() == 1
						&& this.getCurrentToken() == -1
						&& this.input.available() == 0;
			} catch (final IOException exception) {
				throw unchecked(exception);
			}
		}
		
		public final List<Integer> getBufferedTokens() {
			return this.bufferedTokens;
		}
		
		public final InputStream getInput() {
			return this.input;
		}
		
		public final int getCurrentToken() {
			return this.currentToken;
		}
		
		public final int next() {
			if (this.getBufferedTokens().isEmpty()) {
				try {
					this.currentToken = this.getInput().read();
				} catch (final IOException exception) {
					throw unchecked(exception);
				}
			} else {
				this.currentToken = this.getBufferedTokens().remove(0);
			}
			
			return this.getCurrentToken();
		}
		
		public final void push(final int token) {
			this.getBufferedTokens().add(0, token);
		}
		
		public final void shift() {
			this.getStack().add(this.getCurrentToken());
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -1493638042969203347L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-05)
	 */
	public static abstract interface ContextAction extends Serializable {
		
		public abstract void update(Context context);
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-05)
	 */
	public static final class Shift implements ContextAction {
		
		@Override
		public final void update(final Context context) {
			context.shift();
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 7115076706133446847L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-05)
	 */
	public static final class Reduce implements ContextAction {
		
		private final int generatedToken;
		
		private final int stackChunkSize;
		
		public Reduce(final int generatedToken, final int stackChunkSize) {
			this.generatedToken = generatedToken;
			this.stackChunkSize = stackChunkSize;
		}
		
		public final int getGeneratedToken() {
			return this.generatedToken;
		}
		
		public final int getStackChunkSize() {
			return this.stackChunkSize;
		}
		
		@Override
		public final void update(final Context context) {
			context.getStack().subList(0, this.getStackChunkSize()).clear();
			context.getStack().add(0, this.getGeneratedToken());
			context.push(context.getCurrentToken());
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -5841545600239311923L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-05)
	 */
	public static final class State implements Serializable {
		
		private final String name;
		
		private final List<Transition> transitions;
		
		public State(final String name) {
			this.name = name;
			this.transitions = new ArrayList<>();
		}
		
		@Override
		public final State clone() {
			return this.clone(new HashMap<>());
		}
		
		public final State addTransition(final IntPredicate predicate,
				final ContextAction contextAction, final State nextState) {
			this.transitions.add(new Transition(predicate, contextAction, nextState));
			
			return this;
		}
		
		public final List<Transition> getTransitions() {
			return this.transitions;
		}
		
		public final State processNextToken(final Context context) {
			final int token = context.next();
			
			for (final Transition transition : this.getTransitions()) {
				if (transition.getPredicate().test(token)) {
					transition.getContextAction().update(context);
					
					return transition.getNextState();
				}
			}
			
			return null;
		}
		
		@Override
		public final String toString() {
			return this.name;
		}
		
		private final State clone(final Map<State, State> clones) {
			State result = clones.get(this);
			
			if (result == null) {
				result = new State(this.name);
				
				for (final Transition transition : this.getTransitions()) {
					result.addTransition(transition.getPredicate(),
							transition.getContextAction(), transition.getNextState().clone(clones));
				}
			}
			
			return result;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 1440807894436761508L;
		
		public static final State END = new State("END");
		
		/**
		 * @author codistmonk (creation 2014-08-05)
		 */
		public static final class Transition implements Serializable {
			
			private final IntPredicate predicate;
			
			private final ContextAction contextAction;
			
			private final State nextState;
			
			public Transition(final IntPredicate predicate, final ContextAction stackAction, final State nextState) {
				this.predicate = predicate;
				this.contextAction = stackAction;
				this.nextState = nextState;
			}
			
			public final IntPredicate getPredicate() {
				return this.predicate;
			}
			
			public final ContextAction getContextAction() {
				return this.contextAction;
			}
			
			public final State getNextState() {
				return this.nextState;
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = 6950671624886636848L;
			
		}
		
	}
	
}
