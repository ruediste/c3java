
package net.lshift.java.collections;

import java.util.*;

/**
 * Primitives which operate on collections.
 * Note: a java.util.List is rather more than the simplest thing you
 * could call a list, and a Collection has order via its
 * Iterator, so you can call it a list.
 */
public class Lists
{
    public interface ForEachProcedure<T>
    {
	public void apply(T item);
    }

    public static <T> void forEach(ForEachProcedure<T> proc, Collection<T> list)
    {
	for(Iterator<T> i = list.iterator(); i.hasNext();)
	    proc.apply(i.next());
    }

    public static class FoldState<T>
    {
	public FoldState(boolean proceed, T seed)
	{
	    this.seed = seed;
	    this.proceed = proceed;
	}

	public final T seed;
	public final boolean proceed;
    }

    public interface FoldProcedure<T,U>
    {
	public FoldState<U> apply(T item, U accumulator); 
    }

    /**
     * LISP like fold-right.
     */
    public static <T,U> U foldRight(FoldProcedure<T,U> proc, U seed, Collection<T> c)
    {
	for(Iterator<T> i = c.iterator(); i.hasNext();) {
	    FoldState<U> proceed = proc.apply(i.next(), seed);
	    seed = proceed.seed;
	    if(!proceed.proceed)
		break;
	}
	return seed;
    }

    /**
     * LISP like fold-left.
     */ 
    public static <T,U> U foldLeft(FoldProcedure<T,U> proc, U seed, Collection<T> c)
    {
	List<T> l = (c instanceof List) ? (List<T>)c : new ArrayList<T>(c);
	for(ListIterator<T> i = l.listIterator(l.size()); i.hasPrevious();) {
	    FoldState<U> proceed = proc.apply(i.previous(), seed);
	    seed = proceed.seed;
	    if(!proceed.proceed)
		break;
	}

	return seed;
    }

    /**
     * LISP like map.
     * @return a collection which is the result of
     *   applying proc to each element in c. The collection wont neccessarily
     *   have the same implementation as c (for now, its always a Pair).
     */
    public static <T,U> Collection<U> map(final Transform<T,U> proc, Collection<T> c)
    {
	return foldRight
	    (new FoldProcedure<T,Pair<U>>() {
		public FoldState<Pair<U>> apply(T item, Pair<U> accumulator) {
		    return new Lists.FoldState<Pair<U>>
			(true, Pair.cons(proc.apply(item), accumulator));
		}
	    }, null, c);
    }

    @SuppressWarnings("unchecked")
    public static <T,U> U[] map(final Transform<T,U> proc, T[] a)
    {
	U[] result = (U[])new Object[a.length];
	for(int i = 0; i != a.length; ++i)
	    result[i] = proc.apply(a[i]);
	return result;
    }

    public static <T> boolean any(final Predicate<T> proc, Collection<T> c)
    {
	return foldRight
	    (new FoldProcedure<T,Boolean>() {
		public FoldState<Boolean> apply(T item, Boolean accumulator) {
		    boolean result = proc.apply(item) || accumulator;
		    return new FoldState<Boolean>(!result, result);
		}
	    }, false, c);
    }

    public static <T> boolean all(final Predicate<T> proc, Collection<T> c)
    {
	return foldRight
	    (new FoldProcedure<T,Boolean>() {
		public FoldState<Boolean> apply(T item, Boolean accumulator) {
		    boolean result = proc.apply(item) && accumulator;
		    return new FoldState<Boolean>(result, result);
		}
	    }, false, c);
    }

    private static Transform<Collection,Iterator> ITERATOR_TRANSFORM
	= new Transform<Collection,Iterator>() {
	public Iterator apply(Collection c) {
	    return c.iterator();
	}
    };

    public static <T> Transform<Collection<T>,Iterator<T>> iteratorTransform()
    {
	return (Transform<Collection<T>,Iterator<T>>)ITERATOR_TRANSFORM;
    }

    public static <T> Collection<T[]> zip(Collection<T> ... c)
    {
	Transform<Collection<T>,Iterator<T>> it = List.<T>iteratorTransform();
	Iterator<T> [] iterators = map(it, c);
    }
}
