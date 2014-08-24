package net.sourceforge.aurochs2.core;

import java.io.Serializable;
import java.util.List;

/**
 * @author codistmonk (creation 2014-08-24)
 */
public final class StackItem implements Serializable {
	
	private int stateIndex;
	
	private Object token;
	
	private Object datum;

	public final int getStateIndex() {
		return this.stateIndex;
	}
	
	public final StackItem setStateIndex(final int stateIndex) {
		this.stateIndex = stateIndex;
		
		return this;
	}
	
	public final Object getToken() {
		return this.token;
	}
	
	public final StackItem setToken(final Object token) {
		this.token = token;
		
		return this;
	}
	
	public final Object getDatum() {
		return this.datum;
	}
	
	public final StackItem setDatum(final Object datum) {
		this.datum = datum;
		
		return this;
	}
	
	@Override
	public final String toString() {
		return "(" + this.getStateIndex() + " " + this.getToken() + ")";
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -1379273182596052658L;
	
	public static final <T> T last(final List<T> list) {
		return list.get(list.size() - 1);
	}
	
}