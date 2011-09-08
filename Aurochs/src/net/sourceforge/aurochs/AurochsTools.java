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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Tools;

/**
 *
 * @author codistmonk (creation 2010-10-05)
 */
public final class AurochsTools {

    /**
     * @throws IllegalInstantiationException To prevent instantiation
     */
    private AurochsTools() {
        throw new IllegalInstantiationException();
    }

	/**
	 *
	 * @param elements
	 * <br>Not null
	 * @param methodName
	 * <br>Not null
	 * @return
	 * <br>Not null
	 * <br>New
	 */
	public static final HashSet<?> iterate(final Set<?> elements, final String methodName) {
        try {
            final HashSet<Object> result = new HashSet<Object>();

            for (final Object element : elements) {
                final Method method = element.getClass().getMethod(methodName);

                result.add(method.invoke(element));
            }

            return result;
        } catch (final Exception exception) {
            throw unchecked(exception);
        }
	}

    /**
     *
     * @param <T> The type of the elements
     * @param iterable
     * <br>Not null
     * <br>Input-output
     * @return
     * <br>Maybe null
     */
    public static final <T> T take(final Iterable<T> iterable) {
        final Iterator<T> iterator = iterable.iterator();
        final T result = iterator.next();

        iterator.remove();

        return result;
    }

    /**
     *
     * @param <K>
     * @param <V>
     * @param map
     * <br>Not null
     * <br>Input-output
     * @param key
     * <br>Maybe null
     * <br>Shared
     * @param valueClass
     * <br>Not null
     * @throws RuntimeException if {@code valueClass} cannot be instantiated with a default constructor
     */
    @SuppressWarnings("unchecked")
    public static final <K, V> V getOrCreate(final Map<K, V> map, final K key, final Class<?> valueClass) {
        V result = map.get(key);

        if (result == null) {
            try {
                result = (V) valueClass.newInstance();
            } catch (final Exception exception) {
                throw unchecked(exception);
            }

            map.put(key, result);
        }

        return result;
    }

	/**
	 *
	 * @param separator
	 * <br>Maybe null
	 * @param elements
	 * <br>Maybe null
	 * @return
	 * <br>Not null
	 * <br>New
	 */
	public static final String join(final String separator, final Iterable<?> elements) {
		final StringBuilder result = new StringBuilder();
		final Iterator<?> iterator = elements.iterator();

		while (iterator.hasNext()) {
			result.append(iterator.next());

			if (iterator.hasNext()) {
				result.append(separator);
			}
		}

		return result.toString();
	}

}
