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

import static java.util.Arrays.*;
import static net.sourceforge.aprog.tools.Tools.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.aurochs.AbstractLRParser.GeneratedToken;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aurochs.Grammar.Action;
import net.sourceforge.aurochs.Grammar.Regular;
import net.sourceforge.aurochs.Grammar.Rule;
import net.sourceforge.aurochs.LALR1LexerBuilder.LRLexer;

/**
 * @author codistmonk (creation 2011-09-09)
 */
public final class LRParserTools {

    /**
     * @throws IllegalInstantiationException To prevent instantiation
     */
    private LRParserTools() {
        throw new IllegalInstantiationException();
    }

    /**
     * @param parserClass
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    public static final LRParser newParser(final Class<?> parserClass) {
        try {
            final String LEXER_RULES_FIELD_NAME = "lexerRules";
            final String PARSER_RULES_FIELD_NAME = "parserRules";
            final Field lexerRulesField = getField(parserClass, LEXER_RULES_FIELD_NAME);
            final Field parserRulesField = getField(parserClass, PARSER_RULES_FIELD_NAME);
            final LRLexer lexer;
            final Object[] parser = new Object[1];

            if (lexerRulesField != null) {
                final LALR1LexerBuilder lexerBuilder = new LALR1LexerBuilder();

                lexerRulesField.setAccessible(true);

                for (final LexerRule lexerRule : (LexerRule[]) lexerRulesField.get(null)) {
                    lexerRule.addTo(lexerBuilder).getActions().addAll(getActions(parserClass, lexerRule.getName(), parser));
                }

                lexer = lexerBuilder.newLexer();
            } else {
                lexer = null;
            }

            final LALR1ParserBuilder parserBuilder = new LALR1ParserBuilder();

            parserRulesField.setAccessible(true);

            for (final ParserRule parserRule : (ParserRule[]) parserRulesField.get(null)) {
                if (parserRule instanceof AssociativityAndPriorityRule) {
                    ((AssociativityAndPriorityRule) parserRule).addTo(parserBuilder);
                } else {
                    ((NormalRule) parserRule).addTo(parserBuilder).getActions().addAll(getActions(parserClass, ((NormalRule) parserRule).getName(), parser));
                }
            }

			return parserBuilder.newParser(lexer);
        } catch (final Exception exception) {
            throw unchecked(exception);
        }
    }

    /**
     * @param parserClass
     * <br>Not null
     * @param ruleName
     * <br>Not null
     * @param parser
     * <br>Not null
     * @return
     * <br>Not null
     * <br>New
     */
    private static final List<Action> getActions(final Class<?> parserClass, final String ruleName, final Object[] parser) {
        final List<Action> result = new ArrayList<Action>();

        for (final Method m : parserClass.getDeclaredMethods()) {
            if (m.getName().equals(ruleName)) {
            	final Class<?>[] parameterTypes = m.getParameterTypes();
            	
                result.add(new Action() {

					@Override
                    public final void perform(final Rule rule, final GeneratedToken generatedToken, final List<Object> developmentTokens) {
						try {
							final Method method = parserClass.getDeclaredMethod(ruleName, parameterTypes);
							final List<Object> neededArguments = new ArrayList<Object>(3);
							final Map<Class<?>, Object> availableArguments = new HashMap<Class<?>, Object>();
							
							extractValues(developmentTokens);
							
							availableArguments.put(Rule.class, rule);
							availableArguments.put(GeneratedToken.class, generatedToken);
							availableArguments.put(List.class, developmentTokens);
							availableArguments.put(Object[].class, developmentTokens.toArray());
							
							for (final Class<?> neededArgumentType : method.getParameterTypes()) {
								neededArguments.add(availableArguments.get(neededArgumentType));
							}
							
							method.setAccessible(true);
							
							try {
								if (!Modifier.isStatic(method.getModifiers()) && parser[0] == null) {
									parser[0] = parserClass.newInstance();
								}
								
								generatedToken.setValue(method.invoke(parser[0], neededArguments.toArray()));
							} catch (final Exception exception) {
								Logger.getLogger(LRParserTools.class.getName()).log(Level.SEVERE, null, exception);
							}
						} catch (final Exception exception) {
							throw unchecked(exception);
						}
                    }

                    /**
					 * {@value}.
					 */
					private static final long serialVersionUID = -4899332283370685664L;

                });
            }
        }

        return result;
    }

    /**
     * @param tokens
     * <br>Not null
     * <br>Input-output
     */
    static final void extractValues(final List<Object> tokens) {
        for (int i = 0; i < tokens.size(); ++i) {
            if (tokens.get(i) instanceof GeneratedToken) {
                tokens.set(i, ((GeneratedToken) tokens.get(i)).getValue());
            }
        }
    }


    /**
     * @param cls
     * <br>Not null
     * @param name
     * <br>Not null
     * @return
     * <br>Maybe null
     */
    public static final Field getField(final Class<?> cls, final String name) {
        try {
            return cls.getDeclaredField(name);
        } catch (final NoSuchFieldException e1) {
            ignore(e1);

            try {
                return cls.getField(name);
            } catch (final NoSuchFieldException e2) {
                ignore(e2);
            }
        }

        return null;
    }

    /**
     * @param nonterminalAndDevelopment
     * <br>Not null
     * <br>Will become reference
     * @return
     * <br>Not null
     * <br>New
     */
    public static final LexerTokenRule tokenRule(final Object... nonterminalAndDevelopment) {
        return namedTokenRule(null, nonterminalAndDevelopment);
    }

    /**
     * @param name
     * <br>Maybe null
     * <br>Will become reference
     * @param nonterminalAndDevelopment
     * <br>Not null
     * <br>Will become reference
     * @return
     * <br>Not null
     * <br>New
     */
    public static final LexerTokenRule namedTokenRule(final String name, final Object... nonterminalAndDevelopment) {
        return new LexerTokenRule(name, nonterminalAndDevelopment);
    }

    /**
     * @param nonterminalAndDevelopment
     * <br>Not null
     * <br>Will become reference
     * @return
     * <br>Not null
     * <br>New
     */
    public static final LexerVerbatimTokenRule verbatimTokenRule(final Object... nonterminalAndDevelopment) {
        return namedVerbatimTokenRule(null, nonterminalAndDevelopment);
    }

    /**
     * @param name
     * <br>Maybe null
     * <br>Will become reference
     * @param nonterminalAndDevelopment
     * <br>Not null
     * <br>Will become reference
     * @return
     * <br>Not null
     * <br>New
     */
    public static final LexerVerbatimTokenRule namedVerbatimTokenRule(final String name, final Object... nonterminalAndDevelopment) {
        return new LexerVerbatimTokenRule(name, nonterminalAndDevelopment);
    }

    /**
     * @param nonterminalAndDevelopment
     * <br>Not null
     * <br>Will become reference
     * @return
     * <br>Not null
     * <br>New
     */
    public static final LexerNontokenRule nontokenRule(final Object... nonterminalAndDevelopment) {
        return namedNontokenRule(null, nonterminalAndDevelopment);
    }

    /**
     * @param name
     * <br>Maybe null
     * <br>Will become reference
     * @param nonterminalAndDevelopment
     * <br>Not null
     * <br>Will become reference
     * @return
     * <br>Not null
     * <br>New
     */
    public static final LexerNontokenRule namedNontokenRule(final String name, final Object... nonterminalAndDevelopment) {
        return new LexerNontokenRule(name, nonterminalAndDevelopment);
    }

    /**
     * @param nonterminalAndDevelopment
     * <br>Not null
     * <br>Will become reference
     * @return
     * <br>Not null
     * <br>New
     */
    public static final LexerHelperRule helperRule(final Object... nonterminalAndDevelopment) {
        return namedHelperRule(null, nonterminalAndDevelopment);
    }

    /**
     * @param name
     * <br>Maybe null
     * <br>Will become reference
     * @param nonterminalAndDevelopment
     * <br>Not null
     * <br>Will become reference
     * @return
     * <br>Not null
     * <br>New
     */
    public static final LexerHelperRule namedHelperRule(final String name, final Object... nonterminalAndDevelopment) {
        return new LexerHelperRule(name, nonterminalAndDevelopment);
    }

    /**
     * @param nonterminalAndDevelopment
     * <br>Not null
     * <br>Will become reference
     * @return
     * <br>Not null
     * <br>New
     */
    public static final NormalRule rule(final Object... nonterminalAndDevelopment) {
        return namedRule(null, nonterminalAndDevelopment);
    }

    /**
     * @param name
     * <br>Maybe null
     * <br>Will become reference
     * @param nonterminalAndDevelopment
     * <br>Not null
     * <br>Will become reference
     * @return
     * <br>Not null
     * <br>New
     */
    public static final NormalRule namedRule(final String name, final Object... nonterminalAndDevelopment) {
        return new NormalRule(name, nonterminalAndDevelopment);
    }

    /**
     * @param symbol
     * <br>Maybe null
     * <br>Will become reference
     * @param priority
     * <br>Range: <code>[Short.MIN_VALUE .. Short.MAX_VALUE]</code>
     * @return
     * <br>Not null
     * <br>New
     */
    public static final AssociativityAndPriorityRule leftAssociative(final Object symbol, final int priority) {
        return new AssociativityAndPriorityRule(true, (short) priority, symbol);
    }

    /**
     * @param symbol
     * <br>Maybe null
     * <br>Will become reference
     * @param priority
     * <br>Range: <code>[Short.MIN_VALUE .. Short.MAX_VALUE]</code>
     * @return
     * <br>Not null
     * <br>New
     */
    public static final AssociativityAndPriorityRule rightAssociative(final Object symbol, final int priority) {
        return new AssociativityAndPriorityRule(false, (short) priority, symbol);
    }

    /**
     * @author codistmonk (creation 2011-09-11)
     */
    public static interface ParserRule {

        // Deliberately left empty

    }

    /**
     * @author codistmonk (creation 2011-09-09)
     */
    public static abstract class ProductionRule implements ParserRule {

        private final String name;

        private final Object nonterminal;

        private final Object[] development;

        private final Regular regular;

        /**
         * @param name
         * <br>Maybe null
         * <br>Will become reference
         * @param nonterminalAndDevelopment
         * <br>Not null
         * <br>Will become reference
         */
        protected ProductionRule(final String name, final Object[] nonterminalAndDevelopment) {
            final boolean debug = false;
            this.name = name;
            this.nonterminal = nonterminalAndDevelopment[0];

            if (nonterminalAndDevelopment.length == 2 && nonterminalAndDevelopment[1] instanceof Regular) {
                this.development = null;
                this.regular = (Regular) nonterminalAndDevelopment[1];
            } else {
                this.development = copyOfRange(nonterminalAndDevelopment, 1, nonterminalAndDevelopment.length);
                this.regular = null;
            }

            // <editor-fold defaultstate="collapsed" desc="DEBUG">
            if (debug) {
                debugPrint(this, "name:", this.getName(), "nonterminal:", this.getNonterminal(),
                        "regular:", this.getRegular(), "development:", Arrays.toString(this.getDevelopment()));
            }
            // </editor-fold>
        }

        /**
         * @return
         * <br>Maybe null
         * <br>Reference
         */
        public final Object[] getDevelopment() {
            return this.development;
        }

        /**
         * @return
         * <br>Maybe null
         * <br>Reference
         */
        public final String getName() {
            return this.name;
        }

        /**
         * @return
         * <br>Maybe null
         * <br>Reference
         */
        public final Object getNonterminal() {
            return this.nonterminal;
        }

        /**
         * @return
         * <br>Maybe null
         * <br>Reference
         */
        public final Regular getRegular() {
            return this.regular;
        }

    }

    /**
     * @author codistmonk (creation 2011-09-09)
     */
    public static abstract class LexerRule extends ProductionRule {

        /**
         * @param name
         * <br>Maybe null
         * <br>Will become reference
         * @param nonterminalAndDevelopment
         * <br>Not null
         * <br>Will become reference
         */
        public LexerRule(final String name, final Object[] nonterminalAndDevelopment) {
            super(name, nonterminalAndDevelopment);
        }

        /**
         * @param lexerBuilder
         * <br>Not null
         * <br>Input-output
         * @return
         * <br>Not null
         * <br>Maybe New
         * <br>Reference
         */
        public final Rule addTo(final LALR1LexerBuilder lexerBuilder) {
            if (this.getDevelopment() != null) {
                return this.addNonregularTo(lexerBuilder);
            }

            return this.addRegularTo(lexerBuilder);
        }

        /**
         * @param lexerBuilder
         * <br>Not null
         * <br>Input-output
         * @return
         * <br>Not null
         * <br>Maybe New
         * <br>Reference
         */
        protected abstract Rule addNonregularTo(final LALR1LexerBuilder lexerBuilder);

        /**
         * @param lexerBuilder
         * <br>Not null
         * <br>Input-output
         * @return
         * <br>Not null
         * <br>Maybe New
         * <br>Reference
         */
        protected abstract Rule addRegularTo(final LALR1LexerBuilder lexerBuilder);

    }

    /**
     * @author codistmonk (creation 2011-09-09)
     */
    public static final class LexerTokenRule extends LexerRule {

        /**
         * @param name
         * <br>Maybe null
         * <br>Will become reference
         * @param nonterminalAndDevelopment
         * <br>Not null
         * <br>Will become reference
         */
        public LexerTokenRule(final String name, final Object[] nonterminalAndDevelopment) {
            super(name, nonterminalAndDevelopment);
        }

        @Override
        protected final Rule addNonregularTo(final LALR1LexerBuilder lexerBuilder) {
            return lexerBuilder.addTokenRule(this.getNonterminal(), this.getDevelopment());
        }

        @Override
        protected final Rule addRegularTo(final LALR1LexerBuilder lexerBuilder) {
            return lexerBuilder.addTokenRule(this.getNonterminal(), this.getRegular());
        }

    }

    /**
     * @author codistmonk (creation 2011-09-09)
     */
    public static final class LexerVerbatimTokenRule extends LexerRule {

        /**
         * @param name
         * <br>Maybe null
         * <br>Will become reference
         * @param nonterminalAndDevelopment
         * <br>Not null
         * <br>Will become reference
         */
        public LexerVerbatimTokenRule(final String name, final Object[] nonterminalAndDevelopment) {
            super(name, nonterminalAndDevelopment);
        }

        @Override
        protected final Rule addNonregularTo(final LALR1LexerBuilder lexerBuilder) {
            return lexerBuilder.addVerbatimTokenRule(this.getNonterminal(), this.getDevelopment());
        }

        @Override
        protected final Rule addRegularTo(final LALR1LexerBuilder lexerBuilder) {
            return lexerBuilder.addVerbatimTokenRule(this.getNonterminal(), this.getRegular());
        }

    }

    /**
     * @author codistmonk (creation 2011-09-09)
     */
    public static final class LexerNontokenRule extends LexerRule {

        // TODO rename nontoken into custom? (so that the user can return their own token in an action)

        /**
         * @param name
         * <br>Maybe null
         * <br>Will become reference
         * @param nonterminalAndDevelopment
         * <br>Not null
         * <br>Will become reference
         */
        public LexerNontokenRule(final String name, final Object[] nonterminalAndDevelopment) {
            super(name, nonterminalAndDevelopment);
        }

        @Override
        protected final Rule addNonregularTo(final LALR1LexerBuilder lexerBuilder) {
            return lexerBuilder.addNontokenRule(this.getNonterminal(), this.getDevelopment());
        }

        @Override
        protected final Rule addRegularTo(final LALR1LexerBuilder lexerBuilder) {
            return lexerBuilder.addNontokenRule(this.getNonterminal(), this.getRegular());
        }

    }

    /**
     * @author codistmonk (creation 2011-09-09)
     */
    public static final class LexerHelperRule extends LexerRule {

        // TODO remove helper rules and replace them with normal rules

        /**
         * @param name
         * <br>Maybe null
         * <br>Will become reference
         * @param nonterminalAndDevelopment
         * <br>Not null
         * <br>Will become reference
         */
        public LexerHelperRule(final String name, final Object[] nonterminalAndDevelopment) {
            super(name, nonterminalAndDevelopment);
        }

        @Override
        protected final Rule addNonregularTo(final LALR1LexerBuilder lexerBuilder) {
            return lexerBuilder.addHelperRule(this.getNonterminal(), this.getDevelopment());
        }

        @Override
        protected final Rule addRegularTo(final LALR1LexerBuilder lexerBuilder) {
            return lexerBuilder.addHelperRule(this.getNonterminal(), this.getRegular());
        }

    }

    /**
     * @author codistmonk (creation 2011-09-11)
     */
    public static final class AssociativityAndPriorityRule implements ParserRule {

        private final boolean leftAssociative;

        private final short priority;

        private final Object symbol;

        /**
         * @param leftAssociative
         * <br>Range: any boolean
         * @param priority
         * <br>Range: any short
         * @param symbol
         * <br>Maybe null
         * <br>Will become reference
         */
        public AssociativityAndPriorityRule(final boolean leftAssociative, final short priority, final Object symbol) {
            this.leftAssociative = leftAssociative;
            this.priority = priority;
            this.symbol = symbol;
        }

        /**
         * @return
         * <br>Range: any boolean
         */
        public final boolean isLeftAssociative() {
            return this.leftAssociative;
        }

        /**
         * @return
         * <br>Range: any short
         */
        public final short getPriority() {
            return this.priority;
        }

        /**
         * @return
         * <br>Maybe null
         * <br>Reference
         */
        public final Object getSymbol() {
            return this.symbol;
        }

        /**
         * @param parserBuilder
         * <br>Not null
         * <br>Input-output
         */
        public final void addTo(final LALR1ParserBuilder parserBuilder) {
            if (this.isLeftAssociative()) {
                parserBuilder.addLeftAssociativeBinaryOperator(this.getSymbol());
            } else {
                parserBuilder.addRightAssociativeBinaryOperator(this.getSymbol());
            }

            parserBuilder.setPriority(this.getSymbol(), this.getPriority());
        }

    }

    /**
     * @author codistmonk (creation 2011-09-09)
     */
    public static final class NormalRule extends ProductionRule {

        /**
         * @param name
         * <br>Maybe null
         * <br>Will become reference
         * @param nonterminalAndDevelopment
         * <br>Not null
         * <br>Will become reference
         */
        public NormalRule(final String name, final Object[] nonterminalAndDevelopment) {
            super(name, nonterminalAndDevelopment);
        }

        /**
         * @param parserBuilder
         * <br>Not null
         * <br>Input-output
         * @return
         * <br>Not null
         * <br>Maybe New
         * <br>Reference
         */
        public final Rule addTo(final LALR1ParserBuilder parserBuilder) {
            if (this.getDevelopment() != null) {
                return parserBuilder.addRule(this.getNonterminal(), this.getDevelopment());
            }

            return parserBuilder.addRule(this.getNonterminal(), this.getRegular());
        }

    }

}
