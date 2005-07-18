
package net.lshift.java.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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

    /**
     * LISP like map.
     * @return a collection which is the result of
     *   applying proc to each element in c. The collection wont neccessarily
     *   have the same implementation as c (for now, its always a Pair).
     */
    public static <T,U> Collection<U> map(final Transform<T,U> proc, Collection<T> c)
    {
	// because we know the size of the collection in advance
	// may as well construct an array list
	ArrayList<U> result = new ArrayList<U>(c.size());
	for(T item: c)
	    result.add(proc.apply(item));
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
	    }, true, c);
    }

    public static <T> Collection<Collection<T>> zip(Collection<T>... c)
    {
	Collection<Iterator<T>> iterators = 
	    map(Collections.Procedures.<T>iterator(), Arrays.asList(c));

	Collection<Collection<T>> result = new ArrayList<Collection<T>>();
	while (all(Collections.Procedures.<T>hasNext(), iterators))
	{
	    Collection<T> slice = map(Collections.Procedures.<T>next(), iterators);
	    result.add(slice);
	}

	return result;
    }
    
    public static <T> boolean equal(Collection<T> control,
				    Collection<T>... tests)
    {
	Predicate<Collection<T>> sameLength = new Predicate<Collection<T>>() {
	    public Boolean apply(Collection<T> x)
	    {
		return control.size() == x.size();
	    }
	};
	
	if (!all(sameLength, Arrays.asList(tests)))
	    return false;
	
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
	return !(controlIt.hasNext() || zippedIt.hasNext());
    }
}
