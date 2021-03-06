package aurochs.core;

import static aurochs.core.StackItem.last;
import static aurochs.core.TokenSource.tokens;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import aurochs.core.Grammar.Rule;
import aurochs.core.Grammar.RuleAction;
import aurochs.core.Grammar.Special;
import aurochs.core.LRTable.Action;
import aurochs.core.LRTable.Shift;
import multij.tools.Tools;

/**
 * @author codistmonk (creation 2014-08-24)
 */
public final class LRParser implements Serializable {
	
	private final Grammar grammar;
	
	private final LRTable table;
	
	public LRParser(final Grammar grammar) {
		this(new LRTable(new LALR1ClosureTable(grammar)));
	}
	
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
		
		private final LRParser parser;
		
		private final RuleAction objectGenerator;
		
		private final List<Integer> actionChoices;
		
		private Mode mode;
		
		public ConflictResolver(final LRParser parser) {
			this(parser, TreeCollector.INSTANCE);
		}
		
		public ConflictResolver(final LRParser parser, final RuleAction objectGenerator) {
			this.parser = parser;
			this.objectGenerator = objectGenerator;
			this.actionChoices = new ArrayList<>();
			this.mode = Mode.TRY_NEXT;
		}
		
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
		
		public final ConflictResolver resolve(final List<?> tokens, final Object[] expected) {
			final Grammar grammar = this.parser.getGrammar();
			final List<Rule> rules = grammar.getRules();
			final RuleAction[] savedRuleActions = rules.stream()
					.map(rule -> rule.getAction()).toArray(RuleAction[]::new);
			
			try {
				if (this.objectGenerator != null) {
					ConflictResolver.setup(grammar, this.objectGenerator);
				}
				
				this.setMode(Mode.TRY_NEXT);
				
				final List<Object[]> actuals = new ArrayList<>();
				
				actuals.add((Object[]) this.parser.parse(tokens(tokens), this));
				
				while (!Arrays.deepEquals(expected, last(actuals))) {
					actuals.add((Object[]) this.parser.parse(tokens(tokens), this));
					
					if (isZeroes(this.getActionChoices())) {
						break;
					}
				}
				
				this.setMode(Mode.ACCEPT_CURRENT);
				
				final Object[] actual = (Object[]) this.parser.parse(tokens(tokens), this);
				
				this.getActionChoices().clear();
				this.setMode(Mode.TRY_NEXT);
				
				if (!Arrays.deepEquals(expected, actual)) {
					Tools.debugError("Expected:", Arrays.deepToString(expected));
					
					for (int i = 0; i < actuals.size(); ++i) {
						Tools.debugError("Actual[" + i + "]:", Arrays.deepToString(actuals.get(i)));
					}
					
					throw new IllegalStateException();
				}
				
				return this;
			} finally {
				final int n = savedRuleActions.length;
				
				for (int i = 0; i < n; ++i) {
					rules.get(i).setAction(savedRuleActions[i]);
				}
			}
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -2269472491106015129L;
		
		public static final Grammar setup(final Grammar grammar, final RuleAction listener) {
			for (final Rule rule : grammar.getRules()) {
				rule.setAction(listener);
			}
			
			return grammar;
		}
		
		public static final boolean isZeroes(final List<Integer> list) {
			for (final Integer i : list) {
				if (i.intValue() != 0) {
					return false;
				}
			}
			
			return true;
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
		public static final class TreeCollector implements RuleAction {
			
			@Override
			public final Object execute(final Rule rule, final Object[] data) {
				return data.length == 1 ? data[0] : data;
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = 5127951951772222324L;
			
			public static final TreeCollector INSTANCE = new TreeCollector();
			
		}
		
	}
	
	public final Object parse(final TokenSource<?> tokens, final ConflictResolver resolver) {
		final Object initialNonterminal = this.getGrammar().getRules().get(0).getNonterminal();
		final List<StackItem> stack = new ArrayList<>();
		int nextChoiceDelta = 1;
		int choiceIndex = 0;
		
		stack.add(new StackItem().setStateIndex(0).setToken(tokens.read().get()));
		
		while (1 <= stack.size() && last(stack).getToken() != initialNonterminal) {
			final List<Action> actions = this.getActions(last(stack));
			
			if (actions == null) {
				throw new IllegalStateException();
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
		
		if (tokens.get() != Special.END) {
			throw new IllegalStateException();
		}
		
		return last(stack).getDatum();
	}
	
	public final boolean parse(final TokenSource<?> tokens) {
		return this.parse(tokens, (Object[]) null);
	}
	
	public final boolean parse(final TokenSource<?> tokens, final Object[] datum) {
		final Parsing parsing = this.new Parsing(tokens);
		ParsingStatus status;
		
		do {
			status = parsing.step();
		} while (!status.isDone());
		
		if (datum != null) {
			datum[0] = parsing.getDatum();
		}
		
		return ParsingStatus.DONE == status;
	}
	
	public final boolean parsePrefix(final TokenSource<?> tokens) {
		final Parsing parsing = this.new Parsing(tokens);
		ParsingStatus status;
		
		do {
			status = parsing.step();
			
			if (ParsingStatus.ERROR == status) {
				status = parsing.setErrorMode().step();
			}
		} while (!status.isDone());
		
		return ParsingStatus.DONE == status;
	}
	
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
	
	/**
	 * @author codistmonk (creation 2014-08-24)
	 */
	public final class Parsing implements Serializable {
		
		private TokenSource<?> tokens;
		
		private final Object initialNonterminal;
		
		private final List<StackItem> stack;
		
		private Object datum;
		
		public Parsing(final TokenSource<?> tokens) {
			this.tokens = tokens;
			this.initialNonterminal = LRParser.this.getGrammar().getRules().get(0).getNonterminal();
			this.stack = new ArrayList<>();
			
			this.stack.add(new StackItem().setStateIndex(0).setToken(tokens.read().get()));
		}
		
		public final Object getDatum() {
			return this.datum;
		}
		
		public final ParsingStatus step() {
			if (!this.stack.isEmpty() && last(this.stack).getToken() != this.initialNonterminal) {
				final Action action = LRParser.this.getAction(last(this.stack));
				
				if (action == null) {
					return ParsingStatus.ERROR;
				}
				
				action.perform(this.stack, this.tokens);
				
				if (!this.stack.isEmpty()) {
					this.datum = last(this.stack).getDatum();
					
					while (this.getDatum() instanceof Lexer.Token) {
						this.datum = ((Lexer.Token) this.getDatum()).getDatum();
					}
				}
				
				return action instanceof Shift ? ParsingStatus.SHIFTED : ParsingStatus.REDUCED;
			}
			
			return this.tokens.get() == Special.END ? ParsingStatus.DONE : ParsingStatus.ERROR;
		}
		
		public final Parsing setErrorMode() {
			this.tokens.back();
			
			this.tokens = NO_TOKEN_SOURCE;
			
			last(this.stack).setToken(this.tokens.get());
			
			return this;
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -5182096759535319739L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-08-24)
	 */
	public static enum ParsingStatus {
		
		SHIFTED {
			
			@Override
			public final boolean isDone() {
				return false;
			}
			
		}, REDUCED {
			
			@Override
			public final boolean isDone() {
				return false;
			}
			
		}, DONE {
			
			@Override
			public final boolean isDone() {
				return true;
			}
			
		}, ERROR {
			
			@Override
			public final boolean isDone() {
				return true;
			}
			
		};
		
		public abstract boolean isDone();
		
	}
	
	public static final TokenSource<?> NO_TOKEN_SOURCE = new TokenSource<>(new ArrayList<>().iterator()).read();
	
}
