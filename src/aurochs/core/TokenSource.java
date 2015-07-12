package aurochs.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import aurochs.core.Grammar.Special;

/**
 * @author codistmonk (creation 2014-08-24)
 */
public final class TokenSource<T> implements Serializable, Iterable<T> {
	
	private final Iterator<T> tokens;
	
	private Object previous;
	
	private Object token;
	
	public TokenSource(final Iterator<T> tokens) {
		this.tokens = tokens;
	}
	
	public final boolean hasNext() {
		return this.tokens.hasNext() || this.get() != Special.END || this.previous != null;
	}
	
	public final TokenSource<T> back() {
		this.previous = this.get();
		
		return this;
	}
	
	public final TokenSource<T> read() {
		if (this.previous != null) {
			this.token = this.previous;
			this.previous = null;
		} else {
			this.token = this.tokens.hasNext() ? this.tokens.next() : Special.END;
		}
		
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public final T get() {
		return (T) this.token;
	}
	
	@Override
	public final Iterator<T> iterator() {
		return new Iterator<T>() {
			
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
			
			@SuppressWarnings("unchecked")
			@Override
			public final T next() {
				if (!this.hasNext()) {
					throw new NoSuchElementException();
				}
				
				this.hasNext = null;
				
				return (T) this.next;
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
	
	public static final TokenSource<Character> tokens(final CharSequence sequence) {
		return tokens(characters(sequence));
	}
	
	public static final <T> TokenSource<T> tokens(final List<T> tokens) {
		return new TokenSource<>(new Iterator<T>() {
			
			private final Iterator<T> i = tokens.iterator();
			
			@Override
			public final boolean hasNext() {
				return this.i.hasNext();
			}
			
			@Override
			public final T next() {
				return this.i.next();
			}
			
		});
	}
	
}