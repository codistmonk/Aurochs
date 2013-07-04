/*
 *  The MIT License
 * 
 *  Copyright 2010 Codist Monk.
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

import static net.sourceforge.aprog.tools.Tools.*;
import static net.sourceforge.aurochs.AurochsTools.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.aprog.tools.Tools;
import net.sourceforge.aurochs.Grammar.Rule;
import net.sourceforge.aurochs.Grammar.SpecialSymbol;

/**
 * @author codistmonk (creation 2010-10-04)
 */
public final class LALR1ClosureTable implements Serializable {

	private final Grammar grammar;

	private final List<Kernel> kernels;

	private final Map<Kernel, Closure> closures;

	/**
	 *
	 * @param grammar
	 * <br>Not null
	 * <br>Shared
	 */
	public LALR1ClosureTable(final Grammar grammar) {
		final boolean debug = false;

		this.grammar = grammar;
		this.kernels = new ArrayList<Kernel>();
		this.closures = new LinkedHashMap<Kernel, Closure>();

		this.grammar.updatePriorities();

		final Item initialItem = this.new Item(0, 0, set(SpecialSymbol.END_TERMINAL));
		final Kernel initialKernel = this.new Kernel(0, set(initialItem));

		this.kernels.add(initialKernel);
		this.updateClosure(initialKernel);
		this.updateTransitions(this.getClosures().get(initialKernel));

		// <editor-fold defaultstate="collapsed" desc="DEBUG">
		if (debug) {
			for (final Kernel kernel : this.kernels) {
				Tools.debugPrint(kernel.getIndex(), this.getClosures().get(kernel));
			}
		}
		// </editor-fold>
	}

	/**
	 *
	 * @param kernel
	 * <br>Not null
	 * <br>Input-output
	 * <br>Shared
	 */
	private final void updateClosure(final Kernel kernel) {
		final Set<Item> todo = new LinkedHashSet<Item>(kernel.getItems());
		final Closure closure = this.getOrCreateClosure(kernel);
		boolean lookAheadPropagationNeeded = false;

		while (!todo.isEmpty()) {
			final Item fragment = take(todo);
			lookAheadPropagationNeeded |= AddOrMergeResult.MERGED.equals(closure.addOrMergeItem(fragment));

			if (fragment.hasNonterminalAfterCursor()) {
				for (final Item newFragment : fragment.newShiftedItems()) {
					switch (closure.addOrMergeItem(newFragment)) {
						case ADDED:
							todo.add(newFragment);
							break;
						case MERGED:
							lookAheadPropagationNeeded = true;
							break;
						default:
							break;
					}
				}
			}
		}

		if (lookAheadPropagationNeeded) {
			this.updateTransitions(closure);
		}
	}

	/**
	 *
	 * @param closure
	 * <br>Not null
	 * <br>Input-output
	 */
	private final void updateTransitions(final Closure closure) {
		final Map<Object, Set<Item>> prototransitions = closure.newPrototransitions();

		for (final Map.Entry<Object, Set<Item>> entry : prototransitions.entrySet()) {
			final Kernel newKernel = this.new Kernel(this.kernels.size(), entry.getValue());
			Closure nextClosure = this.getClosures().get(newKernel);

			if (nextClosure == null) {
				this.kernels.add(newKernel);

				nextClosure = this.getOrCreateClosure(newKernel);

				closure.getTransitions().put(entry.getKey(), newKernel);

				this.updateClosure(newKernel);
				this.updateTransitions(nextClosure);
			} else {
				closure.getTransitions().put(entry.getKey(), nextClosure.getKernel());

				this.updateClosure(newKernel);
			}
		}
	}

	/**
	 *
	 * @param kernel
	 * <br>Not null
	 * <br>Maybe shared
	 * @return
	 * <br>Not null
	 * <br>Maybe new
	 */
	private final Closure getOrCreateClosure(final Kernel kernel) {
		Closure result = this.getClosures().get(kernel);

		if (result == null) {
			result = this.new Closure(kernel);

			this.getClosures().put(kernel, result);
		}

		return result;
	}

	/**
	 *
	 * @return
	 * <br>Not null
	 * <br>Shared
	 */
	public final Grammar getGrammar() {
		return this.grammar;
	}

	/**
	 * 
	 * @return
	 * <br>Not null
	 * <br>Shared
	 */
	public final Map<Kernel, Closure> getClosures() {
		return this.closures;
	}

	/**
	 *
	 * @param index
	 * <br>Range: {@code [0 .. this.getClosures().size() - 1]}
	 * @return
	 * <br>Not null
	 * <br>Shared
	 */
	public final Kernel getKernel(final int index) {
		return this.kernels.get(index);
	}

	/**
	 * @author codistmonk (creatio 2010-10-05)
	 */
	public final class Kernel implements Serializable {

		private final int index;

		private final Set<Item> items;

		/**
		 *
		 * @param index
		 * <br>Range: any integer
		 * @param items
		 * <br>Not null
		 */
		public Kernel(final int index, final Set<Item> items) {
			this.index = index;
			this.items = new LinkedHashSet<Item>();

			this.getItems().addAll(items);
		}

		/**
		 *
		 * @return
		 * <br>Not null
		 * <br>Shared
		 */
		public final Set<Item> getItems() {
			return this.items;
		}

		/**
		 *
		 * @return
		 * <br>Range: any integer
		 */
		public final int getIndex() {
			return this.index;
		}

		@Override
		public final boolean equals(final Object object) {
			final Kernel that = cast(this.getClass(), object);

			return that != null && this.getItems().equals(that.getItems());
		}

		@Override
		public final int hashCode() {
			return this.getItems().hashCode();
		}

		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -7061311367317395650L;

	}

	/**
	 * @author codistmonk (creation 2010-10-04)
	 */
	public final class Item implements Serializable {

		private final int ruleIndex;

		private final int cursorIndex;

		private final Set<?> lookAheads;

		/**
		 *
		 * @param ruleIndex
		 * <br>Range: {@code [0 .. LALR1Table.this.getGrammar().getRules().size() - 1]}
		 * @param cursorIndex
		 * <br>Range: {@code [0 .. LALR1Table.this.getGrammar().getRules().get(ruleIndex).getDevelopment().size()]}
		 * @param lookAheads
		 * <br>Not null
		 * <br>Shared
		 */
		Item(final int ruleIndex, final int cursorIndex, final Set<?> lookAheads) {
			this.ruleIndex = ruleIndex;
			this.cursorIndex = cursorIndex;
			this.lookAheads = lookAheads;
		}

		/**
		 *
		 * @return
		 * <br>Not null
		 * <br>New
		 */
		public final Set<Object> newAfterShiftLookAheads() {
			final List<Object> sequenceAfterSymbolAfterCursor = this.getSequenceAfterSymbolAfterCursor();
			final Set<Object> result = this.getGrammar().getDevelopmentFirsts(
					sequenceAfterSymbolAfterCursor, new LinkedHashSet<Object>());

			if (this.getGrammar().canDevelopmentCollapse(sequenceAfterSymbolAfterCursor)) {
				result.addAll(this.getLookAheads());
			}

			return result;
		}

		/**
		 *
		 * @return
		 * <br>Not null
		 * <br>New
		 */
		public final Collection<Item> newShiftedItems() {
			final List<Rule> rulesForSymbolAfterCursor = this.getGrammar().getRules(this.getSymbolAfterCursor());
			final Collection<Item> result = new ArrayList<Item>(rulesForSymbolAfterCursor.size());

			for (final Rule rule : rulesForSymbolAfterCursor) {
				result.add(LALR1ClosureTable.this.new Item(rule.getIndex(), 0, this.newAfterShiftLookAheads()));
			}

			return result;
		}

		/**
		 * @return
		 * <br>Range: any boolean
		 */
		public final boolean isFinal() {
			return this.getCursorIndex() == this.getDevelopmentSymbolCount();
		}

		/**
		 *
		 * @return
		 * <br>Range: {@code [0 .. LALR1Table.this.getRule().getDevelopment().size()]}
		 */
		public final int getCursorIndex() {
			return this.cursorIndex;
		}

		/**
		 *
		 * @return
		 * <br>Range: {@code [0 .. LALR1Table.this.getGrammar().getRules().size() - 1]}
		 */
		public final int getRuleIndex() {
			return this.ruleIndex;
		}

		/**
		 *
		 * @return
		 * <br>Not null
		 * <br>Shared
		 */
		@SuppressWarnings("unchecked")
		public final Set<Object> getLookAheads() {
			return (Set<Object>) this.lookAheads;
		}

		/**
		 *
		 * @return
		 * <br>Not null
		 * <br>Shared
		 */
		public final Grammar getGrammar() {
			return LALR1ClosureTable.this.getGrammar();
		}

		/**
		 *
		 * @return
		 * <br>Not null
		 * <br>Shared
		 */
		public final Rule getRule() {
			return this.getGrammar().getRule(this.getRuleIndex());
		}

		/**
		 *
		 * @return
		 * <br>Maybe null
		 * <br>Shared
		 */
		public final Object getNonterminal() {
			return this.getRule().getNonterminal();
		}

		/**
		 *
		 * @return
		 * <br>Not null
		 * <br>Shared
		 */
		public final List<Object> getDevelopment() {
			return this.getRule().getDevelopment();
		}

		/**
		 *
		 * @return
		 * <br>Range: { this.getDevelopment().size() }
		 */
		public final int getDevelopmentSymbolCount() {
			return this.getRule().getDevelopmentSymbolCount();
		}

		/**
		 *
		 * @return
		 * <br>Range: any boolean
		 */
		public final boolean hasNonterminalAfterCursor() {
			return !this.isFinal() && this.getGrammar().hasNonterminal(this.getSymbolAfterCursor());
		}

		/**
		 *
		 * @return
		 * <br>Maybe null
		 * @throws IndexOutOfBoundsException If the cursor is at the end of the development
		 */
		public final Object getSymbolAfterCursor() {
			return this.getDevelopment().get(this.getCursorIndex());
		}

		/**
		 *
		 * @return
		 * <br>Not null
		 * <br>New
		 */
		public final List<Object> getSequenceAfterSymbolAfterCursor() {
			return this.getDevelopment().subList(
					this.getCursorIndex() + 1,
					this.getDevelopment().size());
		}

		@Override
		public final boolean equals(final Object object) {
			final Item that = cast(this.getClass(), object);

			return that != null && this.getRuleIndex() == that.getRuleIndex() &&
					this.getCursorIndex() == that.getCursorIndex();
		}

		@Override
		public final int hashCode() {
			return this.getRuleIndex() + this.getCursorIndex();
		}

		@Override
		public final String toString() {
			final StringBuilder result = new StringBuilder();

			result.append("< ");
			result.append(SpecialSymbol.INITIAL_NONTERMINAL.equals(this.getNonterminal()) ? "_" : this.getNonterminal());
			result.append(" -> ");

			int i = 0;

			for (; i < this.getCursorIndex(); ++i) {
				result.append(this.getDevelopment().get(i));
			}

			result.append(".");

			for (; i < this.getDevelopmentSymbolCount(); ++i) {
				result.append(this.getDevelopment().get(i));
			}

			result.append(", ");
			result.append(AurochsTools.join("/", this.getLookAheads()).replace("END_TERMINAL", "$"));
			result.append(" >");

			return result.toString();
		}

		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = 8286026578080981912L;

	}

	/**
	 * @author codistmonk (creation 2010-10-05)
	 */
	public final class Closure implements Serializable {

		private final Kernel kernel;

		private final ArraySet<Item> items;

		private final Map<Object, Kernel> transitions;

		/**
		 * @param kernel
		 * <br>Not null
		 * <br>Will become strong reference
		 */
		public Closure(final Kernel kernel) {
			this.kernel = kernel;
			this.items = new ArraySet<Item>();
			this.transitions = new LinkedHashMap<Object, Kernel>();
		}

		/**
		 * @return
		 * <br>Range: any short
		 */
		public final short getPriority() {
			short result = Short.MIN_VALUE;

			for (final Item item : this.getKernel().getItems()) {
				result = (short) Math.max(result, item.getRule().getPriority());
			}

			return result;
		}

		/**
		 *
		 * @return
		 * <br>Not null
		 * <br>Shared
		 */
		public final Kernel getKernel() {
			return this.kernel;
		}

		/**
		 *
		 * @return
		 * <br>Not null
		 * <br>Shared
		 */
		public final ArraySet<Item> getItems() {
			return this.items;
		}

		/**
		 *
		 * @return
		 * <br>Not null
		 * <br>Shared
		 */
		public final Map<Object, Kernel> getTransitions() {
			return this.transitions;
		}

		/**
		 *
		 * @return
		 * <br>Not null
		 * <br>New
		 */
		public final Map<Object, Set<Item>> newPrototransitions() {
			final Map<Object, Set<Item>> result = new LinkedHashMap<Object, Set<Item>>(this.getItems().size());

			for (final Item item : this.getItems()) {
				if (!item.isFinal()) {
					getOrCreate(result, item.getSymbolAfterCursor(), LinkedHashSet.class).add(
							LALR1ClosureTable.this.new Item(item.getRuleIndex(), item.getCursorIndex() + 1,
							new LinkedHashSet<Object>(item.getLookAheads())));
				}
			}

			return result;
		}

		/**
		 *
		 * @param item
		 * <br>Not null
		 * <br>Maybe shared
		 * @return
		 * <br>Not null
		 */
		public final AddOrMergeResult addOrMergeItem(final Item item) {
			final Item existingItem = this.getItems().find(item);

			if (existingItem == null) {
				this.getItems().add(item);

				return AddOrMergeResult.ADDED;
			}
			
			return existingItem.getLookAheads().addAll(item.getLookAheads()) ?
				AddOrMergeResult.MERGED : AddOrMergeResult.NOTHING;
		}

		@Override
		public final String toString() {
			return this.getItems().toString();
		}

		/**
		 * {@value}.
		 */
		private static final long serialVersionUID = -1791634550665052787L;

	}

	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 8234202554491100689L;

	/**
	 * @author codistmonk (creation 2010-10-05)
	 */
	public static enum AddOrMergeResult {

		ADDED, MERGED, NOTHING;

	}

}
