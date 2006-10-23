
package net.lshift.java.util;

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
 * Iterator, so where possible, I accept Collection as an argument type.
 * None of these functions mutate the original list. Thats because
 * java.util.Collections provides mutating versions of many of these
 * functions, or Mutating versions just don't make sense.
 */
public class Lists
{
    /*
      Note that although Pair is provided, the aim here is not to use it:
      wherever possible, we return ArrayLists, because thats what java
      programmers will be expecting in general: Pair has performance 
     */
    
    public static <E> void forEach(Procedure<E> proc, Collection<? extends E> list)
    {
	for(Iterator<? extends E> i = list.iterator(); i.hasNext();)
	    proc.apply(i.next());
    }

    public static class FoldState<E>
    {
	public FoldState(boolean proceed, E seed)
	{
	    this.seed = seed;
	    this.proceed = proceed;
	}

	public final E seed;
	public final boolean proceed;
    }

    public interface FoldProcedure<T,U>
    {
	public FoldState<U> apply(T item, U accumulator); 
    }

    /**
     * LISP like fold-right.
     */
    public static <E,S> S foldLeft
        (FoldProcedure<E,S> proc, 
         S seed, 
         Collection<? extends E> c)
    {
	for(Iterator<? extends E> i = c.iterator(); i.hasNext();) {
	    FoldState<S> state = proc.apply(i.next(), seed);
	    seed = state.seed;
	    if(!state.proceed)
		break;
	}

	return seed;
    }

    
    public static <E> List<E> list(E ... e)
    {
        return new ArrayList<E>(Arrays.asList(e));
    }
    
    // I can't figure out a way to get rid of this warning, so I've
    // put this in its own method, and supressed the warnings
    @SuppressWarnings("unchecked")
    private static <E> List<E> asList(Collection<? extends E> c)
    {
        return (c instanceof List) ? (List<E>)c : new ArrayList<E>(c);
    }

    /**
     * LISP like fold-left.
     */ 
    public static <E,S> S foldRight
        (FoldProcedure<E,S> proc, 
         S seed, 
         Collection<? extends E> c)
    {
	List<E> l = asList(c);
	for(ListIterator<E> i = l.listIterator(l.size()); i.hasPrevious();) {
	    FoldState<S> state = proc.apply(i.previous(), seed);
	    seed = state.seed;
	    if(!state.proceed)
		break;
	}

	return seed;
    }

    /**
     * LISP like map.
     * @return a collection which is the result of
     *   applying proc to each element in c. The collection wont neccessarily
     *   have the same implementation as c (for now, its always an ArrayList).
     */
    public static <E,S> List<S> map(final Transform<E,S> proc, Collection<E> c)
    {
	// because we know the size of the collection in advance
	// may as well construct an array list
	ArrayList<S> result = new ArrayList<S>(c.size());
	for(E item: c)
	    result.add(proc.apply(item));
	return result;
    }

    // Searching
    
    /**
     * Return the first element statisfying proc.
     * see find in SRFI-1 
     * @param <E>
     * @param proc the predicate to satisfy
     * @param c the collection to search in.
     * @return the first element statisfying proc, or null, if no element
     *   satisfies proc.
     */
    public static <E> E find(final Predicate<E> proc, Collection<? extends E> c)
    {
	return foldLeft
	    (new FoldProcedure<E, E>() {
		public FoldState<E> apply(E item, E accumulator) {
		    boolean result = proc.apply(item);
		    return new FoldState<E>(!result, result ? item : null);
		}
	    }, null, c);
    }

    // find-tail doesn't make any sense for java lists
    
    /**
     * Return the first non-null value returned by proc.
     * See any in SRFI-1
     */
    public static <R,E> R any(final Transform<E,R> proc, Collection<? extends E> c)
    {
        return foldLeft
            (new FoldProcedure<E, R>() {
                
                public FoldState<R> apply(E item, R accumulator)
                {
                    R result = proc.apply(item);
                    boolean proceed = (result == null);
                    return new FoldState<R>(proceed, proceed ? null : result);
                }
            }, null, c);
    }
    
    public static <E> E findLast(final Predicate<E> proc, Collection<? extends E> c)
    {
        return foldRight
            (new FoldProcedure<E, E>() {
                public FoldState<E> apply(E item, E accumulator) {
                    boolean result = proc.apply(item);
                    return new FoldState<E>(!result, result ? item : null);
                }
            }, null, c);
    }

    public static <R, E> R anyLast(final Transform<E,R> proc, Collection<? extends E> c)
    {
        return foldRight
            (new FoldProcedure<E, R>() {
                public FoldState<R> apply(E item, R accumulator) {
                    R result = proc.apply(item);
                    boolean proceed = (result == null);
                    return new FoldState<R>(proceed, proceed ? null : result);
                }
            }, null, c);
    }

    /**
     * Return true if proc is true for all elements of c.
     * @param <E>
     * @param proc
     * @param c
     * @return
     */
    public static <E> boolean all
        (final Predicate<E> proc, 
         Collection<? extends E> c)
    {
	return foldRight
	    (new FoldProcedure<E,Boolean>() {
		public FoldState<Boolean> apply(E item, Boolean accumulator) {
		    boolean result = proc.apply(item) && accumulator;
		    return new FoldState<Boolean>(result, result);
		}
	    }, true, c);
    }

    public static <E> E head(List<? extends E> list)
    {
        return list.get(0);
    }
    
    public static <E> List<E> tail(List<E> list)
    {
        return list.subList(1, list.size());
    }
    
    public static <E> Collection<Collection<E>> zip(Collection<E>... c)
    {
	Collection<Iterator<E>> iterators = 
	    map(Collections.Procedures.<E>iterator(), Arrays.asList(c));

	Collection<Collection<E>> result = new ArrayList<Collection<E>>();
	while (all(Collections.Procedures.<E>hasNext(), iterators))
	{
	    Collection<E> slice = map(Collections.Procedures.<E>next(), iterators);
	    result.add(slice);
	}

	return result;
    }
    
    public static <E> boolean equal(final Collection<E> control,
				    Collection<E>... tests)
    {
	Predicate<Collection<E>> sameLength = new Predicate<Collection<E>>() {
	    public Boolean apply(Collection<E> x)
	    {
		return control.size() == x.size();
	    }
	};
	
	if (!all(sameLength, Arrays.asList(tests)))
	    return false;
	
	Collection<Collection<E>> zipped = zip(tests);
	
	Iterator<E> controlIt = control.iterator();
	Iterator<Collection<E>> zippedIt = zipped.iterator();
	while (controlIt.hasNext() && zippedIt.hasNext())
	{
	    final E elem = controlIt.next();
	    Collection<E> slice = zippedIt.next();
	    
	    Predicate<E> equalToElem = new Predicate<E>() {

		public Boolean apply(E x)
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
    
    public static <E> List<E> concatenate(List<List<E>> lists)
    {
        int totalSize = foldLeft(new FoldProcedure<List<E>, Integer>() {
            public FoldState<Integer> apply(List<E> item, Integer accumulator) {
                return new FoldState<Integer>(true, accumulator + item.size());
            }
        }, 0, lists);
        
        List<E> concatenated = new ArrayList<E>(totalSize);
        for(List<E> item: lists)
            concatenated.addAll(item);
        return concatenated;
    }
    
    /**
     * Concatenate lists
     * @param <E>
     * @param lists the lists to concatenate
     * @return a list containing all the elements in the lists concatenated
     */
    public static <E> List<E> concatenate(List<E>... lists)
    {
        return concatenate(Arrays.asList(lists));
    }
    
    /**
     * Reverse a list
     * The list is not mutated. This is efficient for Pair, because
     * it iterates forwards.
     * @see Pair
     * @param <E>
     * @param c
     * @return a new list, containing the elements of c reversed
     */
    @SuppressWarnings("unchecked")
    public static <E> List<E> reverse(Collection<E> c)
    {
        // This is an efficient way to reverse a 
        int index = c.size();
        E[] l = (E[])(new Object[index]);
        for(E element: c)
            l[--index] = element;
        
        return Arrays.asList(l);
    }
}
