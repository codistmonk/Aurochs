package net.sourceforge.aurochs2.core;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.function.IntPredicate;

import net.sourceforge.aurochs2.core.IntParser.Reduce;
import net.sourceforge.aurochs2.core.IntParser.Shift;
import net.sourceforge.aurochs2.core.IntParser.State;

import org.junit.Test;

/**
 * @author codistmonk (creation 2014-08-05)
 */
public final class IntParserTest {
	
	@Test
	public final void testOneOrMore1() {
		final State state = oneOrMore(DIGIT);
		
		assertTrue(new IntParser(state, new ByteArrayInputStream("0".getBytes())).parse());
		assertTrue(new IntParser(state, new ByteArrayInputStream("42".getBytes())).parse());
		
		assertFalse(new IntParser(state, new ByteArrayInputStream("".getBytes())).parse());
		assertFalse(new IntParser(state, new ByteArrayInputStream("0a".getBytes())).parse());
		assertFalse(new IntParser(state, new ByteArrayInputStream("a".getBytes())).parse());
	}
	
	@Test
	public final void testOneOrMore2() {
		final State state = oneOrMore(LETTER);
		
		assertTrue(new IntParser(state, new ByteArrayInputStream("a".getBytes())).parse());
		assertTrue(new IntParser(state, new ByteArrayInputStream("AbC".getBytes())).parse());
		
		assertFalse(new IntParser(state, new ByteArrayInputStream("".getBytes())).parse());
		assertFalse(new IntParser(state, new ByteArrayInputStream("a0".getBytes())).parse());
		assertFalse(new IntParser(state, new ByteArrayInputStream("0".getBytes())).parse());
	}
	
	@Test
	public final void testZeroOrMore1() {
		final State state = zeroOrMore(DIGIT);
		
		assertTrue(new IntParser(state, new ByteArrayInputStream("0".getBytes())).parse());
		assertTrue(new IntParser(state, new ByteArrayInputStream("42".getBytes())).parse());
		assertTrue(new IntParser(state, new ByteArrayInputStream("".getBytes())).parse());
		
		assertFalse(new IntParser(state, new ByteArrayInputStream("0a".getBytes())).parse());
		assertFalse(new IntParser(state, new ByteArrayInputStream("a".getBytes())).parse());
	}
	
	@Test
	public final void testZeroOrMore2() {
		final State state = zeroOrMore(LETTER);
		
		assertTrue(new IntParser(state, new ByteArrayInputStream("a".getBytes())).parse());
		assertTrue(new IntParser(state, new ByteArrayInputStream("AbC".getBytes())).parse());
		assertTrue(new IntParser(state, new ByteArrayInputStream("".getBytes())).parse());
		
		assertFalse(new IntParser(state, new ByteArrayInputStream("a0".getBytes())).parse());
		assertFalse(new IntParser(state, new ByteArrayInputStream("0".getBytes())).parse());
	}
	
	@Test
	public final void testZeroOrOne1() {
		final State state = zeroOrOne(LETTER);
		
		assertTrue(new IntParser(state, new ByteArrayInputStream("a".getBytes())).parse());
		assertTrue(new IntParser(state, new ByteArrayInputStream("".getBytes())).parse());
		
		assertFalse(new IntParser(state, new ByteArrayInputStream("AbC".getBytes())).parse());
		assertFalse(new IntParser(state, new ByteArrayInputStream("a0".getBytes())).parse());
		assertFalse(new IntParser(state, new ByteArrayInputStream("0".getBytes())).parse());
	}
	
	@Test
	public final void testSequence1() {
		final State state = sequence("toto".getBytes());
		
		assertTrue(new IntParser(state, new ByteArrayInputStream("toto".getBytes())).parse());
		
		assertFalse(new IntParser(state, new ByteArrayInputStream("".getBytes())).parse());
		assertFalse(new IntParser(state, new ByteArrayInputStream("to".getBytes())).parse());
		assertFalse(new IntParser(state, new ByteArrayInputStream("toto ".getBytes())).parse());
	}
	
	@Test
	public final void testSequence2() {
		final State state = sequence("é".getBytes());
		
		assertTrue(new IntParser(state, new ByteArrayInputStream("é".getBytes())).parse());
	}
	
	public static final IntPredicate DIGIT = c -> '0' <= c && c <= '9';
	
	public static final IntPredicate UPPERCASE_LETTER = c -> 'A' <= c && c <= 'Z';
	
	public static final IntPredicate LOWERCASE_LETTER = c -> 'a' <= c && c <= 'z';
	
	public static final IntPredicate LETTER = UPPERCASE_LETTER.or(LOWERCASE_LETTER);
	
	public static final IntPredicate ANY = c -> true;
	
	public static final IntPredicate EOF = c -> c == -1;
	
	public static final State sequence(final byte[] bytes) {
		final int n = bytes.length;
		final IntPredicate[] predicates = new IntPredicate[n];
		
		for (int i = 0; i < n; ++i) {
			final int expected = bytes[i] & 0xFF;
			
			predicates[i] = c -> c == expected;
		}
		
		return sequence(predicates);
	}
	
	public static final State sequence(final IntPredicate... predicates) {
		int stateId = 0;
		final State result = new State("" + stateId);
		State state = result;
		
		for (final IntPredicate predicate : predicates) {
			final State next = new State("" + (++stateId));
			
			state.addTransition(predicate, new Shift(), next);
			
			state = next;
		}
		
		state.addTransition(EOF, new Reduce(0, predicates.length), State.END);
		
		return result;
	}
	
	public static final State zeroOrOne(final State result) {
		result.addTransition(EOF, new Reduce(0, 0), State.END);
		
		return result;
	}
	
	public static final State zeroOrOne(final IntPredicate predicate) {
		final State result = new State("0");
		final State state1 = new State("1");
		
		result.addTransition(predicate, new Shift(), state1);
		
		state1.addTransition(EOF, new Reduce(0, 1), State.END);
		
		return zeroOrOne(result);
	}
	
	public static final State zeroOrMore(final IntPredicate predicate) {
		return zeroOrOne(oneOrMore(predicate));
	}
	
	public static final State oneOrMore(final IntPredicate predicate) {
		final State result = new State("0");
		final State state1 = new State("1");
		final State state2 = new State("2");
		final State state3 = new State("3");
		
		result.addTransition(predicate, new Shift(), state1);
		
		state1.addTransition(ANY, new Reduce(0, 1), state2);
		
		state2.addTransition(predicate, new Shift(), state3);
		state2.addTransition(EOF, new Reduce(0, 1), State.END);
		
		state3.addTransition(ANY, new Reduce(0, 2), state2);
		
		return result;
	}
	
}
