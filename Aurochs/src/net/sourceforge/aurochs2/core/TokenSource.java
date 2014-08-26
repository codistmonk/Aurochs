package net.sourceforge.aurochs2.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.sourceforge.aurochs2.core.Grammar.Special;

/**
 * @author codistmonk (creation 2014-08-24)
 */
public final class TokenSource implements Serializable, Iterable<Object> {
	
	private final Iterator<Object> tokens;
	
	private Object previous;
	
	private Object token;
	
	public TokenSource(final Iterator<Object> tokens) {
		this.tokens = tokens;
	}
	
	public final boolean hasNext() {
		return this.tokens.hasNext() || this.get() != Special.END || this.previous != null;
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
			this.token = this.tokens.hasNext() ? this.tokens.next() : Special.END;
		}
		
		return this;
	}
	
	public final Object get() {
		return this.token;
	}
	
	@Override
	public final Iterator<Object> iterator() {
		return new Iterator<Object>() {
			
			private Boolean hasNext;
			
			private Object next;
			
			@Override
			public final boolean hasNext() {
				if (this.hasNext == null) {
					this.next = TokenSource.this.read().get();
					this.hasNext = Special.END != this.next;
				}
				
				return this.hasNext;
			}
			
			@Override
			public final Object next() {
				if (!this.hasNext()) {
					throw new NoSuchElementException();
				}
				
				this.hasNext = null;
				
				return this.next;
			}
			
		};
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 5110131920139722902L;
	
	public static final List<Character> characters(final CharSequence sequence) {
		final int n = sequence.length();
		final List<Character> result = new ArrayList<>(n);
		
		for (int i = 0; i < n; ++i) {
			result.add(sequence.charAt(i));
		}
		
		return result;
	}
	
	public static final TokenSource tokens(final CharSequence sequence) {
		return tokens(characters(sequence));
	}
	
	public static final TokenSource tokens(final List<?> tokens) {
		return new TokenSource(new Iterator<Object>() {
			
			private final Iterator<?> i = tokens.iterator();
			
			@Override
			public final boolean hasNext() {
				return this.i.hasNext();
			}
			
			@Override
			public final Object next() {
				return this.i.next();
			}
			
		});
	}
	
}