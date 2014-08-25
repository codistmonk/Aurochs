package net.sourceforge.aurochs2.core;

import java.io.Serializable;
import java.util.Iterator;

import net.sourceforge.aurochs2.core.LRParser.Parsing;
import net.sourceforge.aurochs2.core.LRParser.ParsingStatus;

/**
 * @author codistmonk (creation 2014-08-25)
 */
public final class Lexer implements Serializable {
	
	private final LRParser parser;
	
	private final Object[] tokenBox;
	
	public Lexer(final LRParser parser, final Object[] tokenBox) {
		this.parser = parser;
		this.tokenBox = tokenBox;
	}
	
	public final LRParser getParser() {
		return this.parser;
	}
	
	public final TokenSource translate(final TokenSource tokens) {
		final LRParser parser = this.getParser();
		final Object[] tokenBox = this.tokenBox;
		final Parsing parsing = parser.new Parsing(tokens);
		
		return new TokenSource(new Iterator<Object>() {
			
			private ParsingStatus parsingStatus = parsing.step();
			
			@Override
			public final Object next() {
				return takeFrom(tokenBox);
			}
			
			@Override
			public final boolean hasNext() {
				while (tokenBox[0] == null && !this.parsingStatus.isDone()) {
					this.parsingStatus = parsing.step();
				}
				
				return tokenBox[0] != null || !this.parsingStatus.isDone();
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
	
}