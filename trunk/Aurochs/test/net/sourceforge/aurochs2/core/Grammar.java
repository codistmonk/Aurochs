package net.sourceforge.aurochs2.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author codistmonk (creation 2014-08-24)
 */
public final class Grammar implements Serializable {
	
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