package net.sourceforge.aurochs2.core;

import java.io.Serializable;
import java.util.Iterator;

import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs2.core.LRParser.Parsing;
import net.sourceforge.aurochs2.core.LRParser.ParsingStatus;

/**
 * @author codistmonk (creation 2014-08-25)
 */
public final class Lexer implements Serializable {
	
	private final LRParser parser;
	
	private final Token[] tokenBox;
	
	public Lexer(final LRParser parser, final Token[] tokenBox) {
		this.parser = parser;
		this.tokenBox = tokenBox;
	}
	
	public final LRParser getParser() {
		return this.parser;
	}
	
	public final TokenSource translate(final TokenSource tokens) {
		final LRParser parser = this.getParser();
		final Object[] tokenBox = this.tokenBox;
//		final Parsing parsing = parser.new Parsing(tokens);
		
		return new TokenSource(new Iterator<Object>() {
			
//			private ParsingStatus parsingStatus = parsing.step();
			
			private boolean parsingStatus = parser.parsePrefix(tokens);
			
			@Override
			public final Object next() {
				return takeFrom(tokenBox);
			}
			
			@Override
			public final boolean hasNext() {
//				while (tokenBox[0] == null && !this.parsingStatus.isDone()) {
//					this.parsingStatus = parsing.step();
//				}
				
				while (tokenBox[0] == null && this.parsingStatus) {
					this.parsingStatus = parser.parsePrefix(tokens);
				}
				
				return tokenBox[0] != null || this.parsingStatus;
			}
			
		});
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 4768669397408042988L;
	
	public static final <T> T takeFrom(final T[] t) {
		final T result = t[0];
		
		if (result != null) {
			t[0] = null;
		}
		
		return result;
	}
	
	/**
	 * @author codistmonk (creation 2014-08-25)
	 */
	public static final class Token implements Serializable {
		
		private final Object nonterminal;
		
		private final Object datum;
		
		public Token(final Object nonterminal, final Object datum) {
			this.nonterminal = nonterminal;
			this.datum = datum;
		}
		
		public final Object getNonterminal() {
			return this.nonterminal;
		}
		
		public final Object getDatum() {
			return this.datum;
		}
		
		@Override
		public final int hashCode() {
			return this.getNonterminal().hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			final Token that = Tools.cast(this.getClass(), object);
			
			return that != null && this.getNonterminal().equals(that.getNonterminal());
		}
		
		@Override
		public final String toString() {
			return this.getNonterminal() + "(" + this.getDatum() + ")";
		}
		
		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -720258593117945847L;
		
	}
	
}