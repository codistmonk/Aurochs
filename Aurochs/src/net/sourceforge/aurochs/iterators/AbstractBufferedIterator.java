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

package net.sourceforge.aurochs.iterators;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author codistmonk (creation 2010-10-07)
 * @param <T> The element type
 */
public abstract class AbstractBufferedIterator<T> implements Iterator<T> {

    private final List<T> buffer;

    private int lastRead;

    private boolean initialized;

    protected AbstractBufferedIterator() {
        this.buffer = new LinkedList<T>();
    }

    /**
     * @param element
     * <br>Maybe null
     * <br>Will become reference
     */
    public final void prepend(final T element) {
        this.buffer.add(0, element);
    }

    /**
     * @param elements
     * <br>Not null
     */
    public final void prepend(final List<T> elements) {
        this.buffer.addAll(0, elements);
    }

    @Override
    public final boolean hasNext() {
        if (!this.buffer.isEmpty()) {
            return true;
        }

        if (!this.initialized) {
            this.lastRead = this.read();
            this.initialized = true;
        }

        return this.lastRead != -1;
    }

    @Override
    public final T next() {
        if (!this.buffer.isEmpty()) {
            return this.buffer.remove(0);
        }

        final T result = this.convert(this.lastRead);

        this.lastRead = this.read();

        return result;
    }

    @Override
    public final void remove() {
        // Do nothing
    }

    /**
     *
     * @param intValue
     * <br>Range: any integer
     * @return
     * <br>Maybe null
     */
    protected abstract T convert(final int intValue);

    /**
     *
     * @return {@code -1} to indicate end of input
     * <br>Range: any integer
     */
    protected abstract int read();

}
