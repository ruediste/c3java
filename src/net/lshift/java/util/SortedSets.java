package net.lshift.java.util;

import static net.lshift.java.util.Collections.Procedures.SIZE_INCREASING;


import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Operations on sorted sets, or which return sorted sets.
 * @author david
 *
 */
public class SortedSets
{
    public static class EmptySortedSet<T>
    extends AbstractSet<T>
    implements SortedSet<T>
    {

        private final Comparator<T> comparator;

        public EmptySortedSet(Comparator<T> comparator)
        {
            this.comparator = comparator;
        }

        @Override
        public Iterator<T> iterator()
        {
            return new Iterator<T>() {

                @Override
                public boolean hasNext()
                {
                    return false;
                }

                @Override
                public T next()
                {
                    throw new NoSuchElementException();
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }

            };
        }

        @Override
        public int size()
        {
            return 0;
        }

        @Override
        public Comparator<? super T> comparator()
        {
            return comparator;
        }

        @Override
        public T first()
        {
            throw new NoSuchElementException();
        }

        @Override
        public SortedSet<T> headSet(T toElement)
        {
            return this;
        }

        @Override
        public T last()
        {
            throw new NoSuchElementException();
        }

        @Override
        public SortedSet<T> subSet(T fromElement, T toElement)
        {
            return this;
        }

        @Override
        public SortedSet<T> tailSet(T fromElement)
        {
            return this;
        }

    }

    // ------------------------------------------------------------------------

    public static <E> SortedSet<E> union(Collection<E> ... cn)
    {
        SortedSet<E> result = new TreeSet<E>();
        for(Collection<E> c: cn)
            result.addAll(c);
        return result;
    }

    public static <E> SortedSet<E> union(
        Comparator<E> comparator,
        Collection<E> ... cn)
    {
        SortedSet<E> result = new TreeSet<E>(comparator);
        for(Collection<E> c: cn)
            result.addAll(c);
        return result;
    }

    public static <E> SortedSet<E> union(
        Comparator<E> comparator,
        Collection<E> a)
    {
        SortedSet<E> result = new TreeSet<E>(comparator);
        result.addAll(a);
        return result;
    }

    public static <E> SortedSet<E> union(
        Comparator<E> comparator,
        Collection<E> a,
        Collection<E> b)
    {
        SortedSet<E> result = new TreeSet<E>(comparator);
        result.addAll(a);
        result.addAll(b);
        return result;
    }

    public static <E> SortedSet<E> union(
        Comparator<E> comparator,
        Collection<E> a,
        Collection<E> b,
        Collection<E> c)
    {
        SortedSet<E> result = new TreeSet<E>(comparator);
        result.addAll(a);
        result.addAll(b);
        result.addAll(c);
        return result;
    }

    public static <E> SortedSet<E> union(E [] ... cn)
    {
        SortedSet<E> result = new TreeSet<E>();
        for(E[] c: cn)
            result.addAll(Arrays.asList(c));
        return result;
    }

    public static <E> SortedSet<E> union(Comparator<E> comparator, E [] ... cn)
    {
        SortedSet<E> result = new TreeSet<E>(comparator);
        for(E[] c: cn)
            result.addAll(Arrays.asList(c));
        return result;
    }

    public static <E> SortedSet<E> union(Comparator<E> comparator, E [] a)
    {
        SortedSet<E> result = new TreeSet<E>(comparator);
        result.addAll(Arrays.asList(a));
        return result;
    }

    public static <E> SortedSet<E> union(Comparator<E> comparator, E [] a, E [] b)
    {
        SortedSet<E> result = new TreeSet<E>(comparator);
        result.addAll(Arrays.asList(a));
        result.addAll(Arrays.asList(b));
        return result;
    }

    public static <E> SortedSet<E> union(Comparator<E> comparator, E [] a, E [] b, E [] c)
    {
        SortedSet<E> result = new TreeSet<E>(comparator);
        result.addAll(Arrays.asList(a));
        result.addAll(Arrays.asList(b));
        result.addAll(Arrays.asList(c));
        return result;
    }

    public static <E> Set<E> union(Iterable<E> ... cn)
    {
        SortedSet<E> result = new TreeSet<E>();
        for(Iterable<E> c: cn)
            for(E e: c)
                result.add(e);
        return result;
    }

    public static <E> SortedSet<E> union(Comparator<E> comparator, Iterable<E> ... cn)
    {
        SortedSet<E> result = new TreeSet<E>(comparator);
        for(Iterable<E> c: cn)
            for(E e: c)
                result.add(e);
        return result;
    }

    public static <E> SortedSet<E> union(SortedSet<E> ... cn)
    {
        Comparator<? super E> comparator = cn[0].comparator();
        SortedSet<E> result = new TreeSet<E>(comparator);
        for(SortedSet<E> c: cn) {
            if(!(comparator == null
                 ? result.comparator() == null
                 : comparator.equals(result.comparator())))
                throw new IllegalArgumentException(
                    "The comparators of all the sets must match");
            result.addAll(c);
        }

        return result;
    }



    public static <E> SortedSet<E> intersection(Set<E> ... cn)
    {
        if(cn.length < 1)
            return emptySortedSet();

        SortedSet<E> result = new TreeSet<E>();
        java.util.Collections.sort(Arrays.asList(cn), SIZE_INCREASING);
        result.addAll(cn[0]);
        for(int i = 1; i < cn.length; ++i)
            result.retainAll(cn[i]);
        return result;
    }

    public static <E> SortedSet<E> emptySortedSet()
    {
        return new EmptySortedSet<E>(null);
    }

    public static <E> SortedSet<E> emptySortedSet(Comparator<E> comparator)
    {
        return new EmptySortedSet<E>(comparator);
    }

    @SuppressWarnings("unchecked")
    public static final <E> SortedSet<E> symmetricDifference(Set<E> a, Set<E> b)
    {
        SortedSet<E> result = union(a,b);
        result.removeAll(intersection(a, b));
        return result;
    }

    /**
     * The symmetricDifference of two sorted sets.
     * The two sorted sets must use the same comparator.
     * @param <E>
     * @param a
     * @param b
     * @return a sorted set, with the same sort order as the parameters
     */
    @SuppressWarnings("unchecked")
    public static final <E> SortedSet<E> symmetricDifference(
        SortedSet<E> a,
        SortedSet<E> b)
    {
        SortedSet<E> result = union(a,b);
        result.removeAll(intersection(a, b));
        return result;
    }

    public static final <E> SortedSet<E> xor(Set<E> a, Set<E> b)
    {
        return symmetricDifference(a, b);
    }
}
