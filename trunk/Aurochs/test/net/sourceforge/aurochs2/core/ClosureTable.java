package net.sourceforge.aurochs2.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author codistmonk (creation 2014-08-24)
 */
public abstract interface ClosureTable extends Serializable {
	
	public abstract Grammar getGrammar();
	
	public abstract List<? extends ClosureTable.State> getStates();
	
	/**
	 * @author codistmonk (creation 2014-08-24)
	 */
	public static abstract interface State extends Serializable {
		
		public abstract Map<Object, Integer> getTransitions();
		
		public abstract Map<Object, Collection<Integer>> getReductions();
		
	}
	
}
