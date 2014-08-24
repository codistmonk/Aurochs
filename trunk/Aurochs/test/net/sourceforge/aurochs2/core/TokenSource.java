package net.sourceforge.aurochs2.core;

import java.io.Serializable;
import java.util.Iterator;

import net.sourceforge.aurochs2.core.Grammar.Special;

/**
 * @author codistmonk (creation 2014-08-24)
 */
public final class TokenSource implements Serializable {
	
	private final Iterator<Object> tokens;
	
	private Object previous;
	
	private Object token;
	
	public TokenSource(final Iterator<Object> tokens) {
		this.tokens = tokens;
	}
	
	public final boolean hasNext() {
		return this.tokens.hasNext() || this.get() != Special.END_TERMINAL || this.previous != null;
	}
	
	public final TokenSource back() {
		this.previous = this.get();
		
		return this;
	}
	
	public final TokenSource read() {
		if (this.previous != null) {
			this.token = this.previous;
			this.previous = null;
		} else {
			this.token = this.tokens.hasNext() ? this.tokens.next() : Special.END_TERMINAL;
		}
		
		return this;
	}
	
	public final Object get() {
		return this.token;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 5110131920139722902L;
	
}