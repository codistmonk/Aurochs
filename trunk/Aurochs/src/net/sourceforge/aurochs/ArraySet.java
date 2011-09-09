package net.sourceforge.aurochs;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @param <E> The elements type
 * @author codistmonk (creation 2011-06-05)
 */
public final class ArraySet<E> extends AbstractSet<E> {

    private final Map<E, Integer> map;
    
    private final List<E> list;

    public ArraySet() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * @param collection
     * <br>Not null
     */
    public ArraySet(final Collection<? extends E> collection) {
        this(collection.size());

        this.addAll(collection);
    }

    /**
     * @param initialCapacity
     * <br>Range: <code>[0 .. Integer.MAX_VALUE]</code>
     * @throws IllegalArgumentException If the initial capacity is less than zero
     */
    public ArraySet(final int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * @param initialCapacity
     * <br>Range: <code>[0 .. Integer.MAX_VALUE]</code>
     * @param loadFactor
     * <br>Range: <code>[0 .. Float.POSITIVE_INFINITY[</code>
     * @throws IllegalArgumentException If the initial capacity is less than zero, or if the load factor is nonpositive
     */
    public ArraySet(final int initialCapacity, final float loadFactor) {
        this.map = new LinkedHashMap<E, Integer>(initialCapacity, loadFactor);
        this.list = new ArrayList<E>(initialCapacity);
    }

    @Override
    public final Iterator<E> iterator() {
        final Iterator<E> orderedIterator = this.list.iterator();
        final Map<E, Integer> unorderedCollection = this.map;

        return new Iterator<E>() {

            private E lastElementReturned;

            @Override
            public final boolean hasNext() {
                return orderedIterator.hasNext();
            }

            @Override
            public final E next() {
                this.lastElementReturned = orderedIterator.next();

                return this.lastElementReturned;
            }

            @Override
            public final void remove() {
                orderedIterator.remove();
                unorderedCollection.remove(this.lastElementReturned);
                ArraySet.this.updateIndices();
            }

        };
    }

    @Override
    public final int size() {
        return this.map.size();
    }

    @Override
    public final boolean add(final E element) {
        if (!this.map.containsKey(element)) {
            this.map.put(element, this.size());

            return this.list.add(element);
        }

        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final boolean contains(final Object key) {
        return ((Map<Object, ?>) this.map).containsKey(key);
    }

    @Override
    public final void clear() {
        this.map.clear();
        this.list.clear();
    }

    @Override
    public final boolean remove(final Object key) {
        @SuppressWarnings("unchecked")
        final Integer index = ((Map<Object, Integer>) this.map).remove(key);

        if (index != null) {
            this.list.remove(index.intValue());
            this.updateIndices();

            return true;
        }

        return false;
    }

    /**
     * @param index
     * <br>Range: <code>[0 .. this.size() - 1]</code>
     * @return
     * <br>Maybe null
     * <br>Strong reference
     */
    public final E get(final int index) {
        return this.list.get(index);
    }

    /**
     * @param index
     * <br>Range: <code>[0 .. this.size() - 1]</code>
     */
    public final void remove(final int index) {
        this.map.remove(this.list.remove(index));
        this.updateIndices();
    }

    /**
     * @param key
     * <br>Maybe null
     * @return
     * <br>Maybe null
     * <br>Strong reference
     */
    public final E find(final E key) {
        final Integer index = this.map.get(key);

        return index == null ? null : this.list.get(index.intValue());
    }

    /**
     * @param key
     * <br>Maybe null
     * @return
     * <br>Range: <code>[-1 .. this.size() - 1]</code>
     */
    public final int indexOf(final E key) {
        final Integer index = this.map.get(key);

        return index == null ? -1 : index;
    }

    final void updateIndices() {
        int i = 0;

        for (Map.Entry<E, Integer> entry : this.map.entrySet()) {
            entry.setValue(i++);
        }
    }

    /**
     * {@value}.
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * {@value}.
     */
    public static final float DEFAULT_LOAD_FACTOR = 0.75F;

}
