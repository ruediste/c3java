
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
    public static <T,U> U foldLeft(FoldProcedure<T,U> proc, U seed, Collection<T> c)
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
    public static <T,U> U foldRight(FoldProcedure<T,U> proc, U seed, Collection<T> c)
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

    public static <T, U> T foldRight1(FoldProcedure<T,T> proc, Collection<T> c) {
	if (c.isEmpty())
	    return null;
	List<T> pruned = new ArrayList<T>(c);
	T seed = pruned.remove(pruned.size() - 1);
	return foldRight(proc, seed, pruned);
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

// This is really non-functional for some reason. Try using it...
//    public static <T,U> U[] map(final Transform<T,U> proc, T[] a)
//    {
//	U[] result = (U[]) new Object[a.length];
//	for(int i = 0; i != a.length; ++i)
//	    result[i] = proc.apply(a[i]);
//	return result;
//    }

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
	    }, true, c);
    }

    public static <T> Collection<Collection<T>> zip(Collection<T>... c)
    {
	Transform<Collection<T>, Iterator<T>> it = new Transform<Collection<T>, Iterator<T>>() {
	    public Iterator<T> apply(Collection<T> x)
	    {
		return x.iterator();
	    }
	};
	Collection<Collection<T>> collections = Arrays.asList(c);
	Collection<Iterator<T>> iteratorsCollection = map(it, collections);
	Predicate<Iterator<T>> allHasNext = new Predicate<Iterator<T>>() {
	    public Boolean apply(Iterator<T> x)
	    {
		return x.hasNext();
	    }
	};
	Collection<Collection<T>> result = new ArrayList<Collection<T>>();
	Transform<Iterator<T>, T> nextTransform = new Transform<Iterator<T>, T>() {

	    public T apply(Iterator<T> x)
	    {
		return x.next();
	    }
	};
	while (all(allHasNext, iteratorsCollection))
	{
	    Collection<T> slice = map(nextTransform, iteratorsCollection);
	    result.add(slice);
	}
	return result;
    }
    
    public static <T> boolean equal(Collection<T> control,
	    Collection<T>... tests)
    {
	Collection<Collection<T>> zipped = zip(tests);
	
	Iterator<T> controlIt = control.iterator();
	Iterator<Collection<T>> zippedIt = zipped.iterator();
	while (controlIt.hasNext() && zippedIt.hasNext())
	{
	    final T elem = controlIt.next();
	    Collection<T> slice = zippedIt.next();
	    
	    Predicate<T> equalToElem = new Predicate<T>() {

		public Boolean apply(T x)
		{
		    if (null == x && null == elem)
			return true;
		    else if (null == x || null == elem)
			return false;
		    else
			return elem.equals(x);
		}
	    };
	    if (!all(equalToElem, slice))
		return false;
	}
	return true;
    }
}
