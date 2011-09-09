/*
 *  The MIT License
 *
 *  Copyright 2011 Codist Monk.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package net.sourceforge.aurochs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aurochs.LALR1LexerBuilder.LRLexer;

/**
 * @author codistmonk (creation 2011-09-09)
 */
public final class AnnotatedParserTools {

    /**
     * @throws IllegalInstantiationException To prevent instantiation
     */
    private AnnotatedParserTools() {
        throw new IllegalInstantiationException();
    }

    private static final Map<Integer, Object> tokenSymbols = new HashMap<Integer, Object>();

    private static final Map<Object, Integer> symbolTokens = new IdentityHashMap<Object, Integer>();

    private static int newToken = Character.MAX_VALUE + 1;

    /**
     * @return
     * <br>Range: <code>[Character.MAX_VALUE + 1 .. Integer.MAX_VALUE]</code>
     */
    public static final synchronized int newToken() {
        return newToken++;
    }

    /**
     * @param symbol
     * <br>Maybe null
     * <br>May become reference
     * @return
     * <br>Range: <code>[Character.MAX_VALUE + 1 .. Integer.MAX_VALUE]</code>
     */
    public static final int symbol(final Object symbol) {
        Integer result = symbolTokens.get(symbol);

        if (result == null) {
            result = newToken();

            tokenSymbols.put(result, symbol);
            symbolTokens.put(symbol, result);
        }

        return result;
    }

    /**
     * @param annotatedLexerClass
     * <br>Not null
     * @return
     * <br>Maybe null
     * <br>New
     */
    public static final LRLexer newLexer(final Class<?> annotatedLexerClass) {
        final LALR1LexerBuilder parserBuilder = new LALR1LexerBuilder();
        boolean lexerRulesExist = false;

        for (final Method method : annotatedLexerClass.getDeclaredMethods()) {
            // TODO add actions
            lexerRulesExist |= addTokenRule(parserBuilder, method);
            lexerRulesExist |= addVerbatimTokenRule(parserBuilder, method);
            lexerRulesExist |= addNontokenRule(parserBuilder, method);
            lexerRulesExist |= addHelperRule(parserBuilder, method);
        }

        return lexerRulesExist ? parserBuilder.newLexer() : null;
    }

    /**
     * @param annotatedParserClass
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    public static final LRParser newParser(final Class<?> annotatedParserClass) {
        final LALR1ParserBuilder parserBuilder = new LALR1ParserBuilder();

        for (final Method method : annotatedParserClass.getDeclaredMethods()) {
            final ParserRule ruleAnnotation = method.getAnnotation(ParserRule.class);

            if (ruleAnnotation != null) {
                final int[] symbols = ruleAnnotation.value();

                parserBuilder.addRule(symbols[0], getDevelopment(symbols));
                // TODO add action
            }
        }

        return parserBuilder.newParser(newLexer(annotatedParserClass));
    }

    /**
     * @param symbols
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    private static final Object[] getDevelopment(final int[] symbols) {
        final Object[] result = new Object[symbols.length - 1];

        for (int i = 0; i < result.length; ++i) {
            final int token = symbols[i + 1];

            result[i] = tokenSymbols.containsKey(token) ? tokenSymbols.get(token) : token;
        }

        return result;
    }

    /**
     * @param parserBuilder
     * <br>Not null
     * <br>Input-output
     * @param method
     * <br>Not null
     * @return <code>true</code> if an instance of {@link LexerTokenRule} is attached to <code>method</code>
     * <br>Range: any boolean
     */
    private static final boolean addTokenRule(final LALR1LexerBuilder parserBuilder, final Method method) {
        final LexerTokenRule ruleAnnotation = method.getAnnotation(LexerTokenRule.class);

        if (ruleAnnotation != null) {
            final int[] symbols = ruleAnnotation.value();

            parserBuilder.addTokenRule(symbols[0], getDevelopment(symbols));

            return true;
        }

        return false;
    }

    /**
     * @param parserBuilder
     * <br>Not null
     * <br>Input-output
     * @param method
     * <br>Not null
     * @return <code>true</code> if an instance of {@link LexerVerbatimTokenRule} is attached to <code>method</code>
     * <br>Range: any boolean
     */
    private static final boolean addVerbatimTokenRule(final LALR1LexerBuilder parserBuilder, final Method method) {
        final LexerVerbatimTokenRule ruleAnnotation = method.getAnnotation(LexerVerbatimTokenRule.class);

        if (ruleAnnotation != null) {
            final int[] symbols = ruleAnnotation.value();

            parserBuilder.addVerbatimTokenRule(symbols[0], getDevelopment(symbols));

            return true;
        }

        return false;
    }

    /**
     * @param parserBuilder
     * <br>Not null
     * <br>Input-output
     * @param method
     * <br>Not null
     * @return <code>true</code> if an instance of {@link LexerNontokenRule} is attached to <code>method</code>
     * <br>Range: any boolean
     */
    private static final boolean addNontokenRule(final LALR1LexerBuilder parserBuilder, final Method method) {
        final LexerNontokenRule ruleAnnotation = method.getAnnotation(LexerNontokenRule.class);

        if (ruleAnnotation != null) {
            final int[] symbols = ruleAnnotation.value();

            parserBuilder.addNontokenRule(symbols[0], getDevelopment(symbols));

            return true;
        }

        return false;
    }

    /**
     * @param parserBuilder
     * <br>Not null
     * <br>Input-output
     * @param method
     * <br>Not null
     * @return <code>true</code> if an instance of {@link LexerHelperRule} is attached to <code>method</code>
     * <br>Range: any boolean
     */
    private static final boolean addHelperRule(final LALR1LexerBuilder parserBuilder, final Method method) {
        final LexerHelperRule ruleAnnotation = method.getAnnotation(LexerHelperRule.class);

        if (ruleAnnotation != null) {
            final int[] symbols = ruleAnnotation.value();

            parserBuilder.addHelperRule(symbols[0], getDevelopment(symbols));

            return true;
        }

        return false;
    }

    /**
     * @author codistmonk (creation 2011-09-09)
     */
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface ParserRule {

        /**
         * @return
         * <br>Not null
         */
        int[] value();

    }

    /**
     * @author codistmonk (creation 2011-09-09)
     */
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface LexerTokenRule {

        /**
         * @return
         * <br>Not null
         */
        int[] value();

    }

    /**
     * @author codistmonk (creation 2011-09-09)
     */
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface LexerVerbatimTokenRule {

        /**
         * @return
         * <br>Not null
         */
        int[] value();

    }

    /**
     * @author codistmonk (creation 2011-09-09)
     */
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface LexerNontokenRule {

        /**
         * @return
         * <br>Not null
         */
        int[] value();

    }

    /**
     * @author codistmonk (creation 2011-09-09)
     */
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface LexerHelperRule {

        /**
         * @return
         * <br>Not null
         */
        int[] value();

    }

}