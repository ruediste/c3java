
package net.lshift.java.util;

import java.util.Collection;
import java.util.Iterator;

public class Collections
{
    public static class Procedures
    {

	public static final Predicate<? extends Iterator> HAS_NEXT =
	    new Predicate<Iterator>() {
		public Boolean apply(Iterator i) {
		    return i.hasNext();
		}
	    };

	// I _could_ return something not a singleton here
	// to avoid a warning, and implement equals in a
	// sensible way, but I'd rather suppress warnings
	@SuppressWarnings("unchecked")
	public static <E> Predicate<Iterator<E>> hasNext()
	{
	    return (Predicate<Iterator<E>>)HAS_NEXT;
	}

	public interface NextTransform<T>
	    extends Transform<Iterator<T>,T> { }

	public static final NextTransform<Object> NEXT =
	    new NextTransform<Object>() {
	    public Object apply(Iterator i) {
		return i.next();
	    }
	};

        // I _could_ return something not a singleton here
        // to avoid a warning, and implement equals in a
        // sensible way, but I'd rather suppress warnings
        @SuppressWarnings("unchecked")
	public static <T> NextTransform<T> next()
	{
	    return (NextTransform<T>)NEXT;
	}

	public static interface IteratorTransform<T>
	    extends Transform<Collection<T>,Iterator<T>> { }

	
	public static final IteratorTransform<Object> ITERATOR =
	    new IteratorTransform<Object>() {
	    public Iterator<Object> apply(Collection<Object> c) {
		return c.iterator();
	    }
	};

        // I _could_ return something not a singleton here
        // to avoid a warning, and implement equals in a
        // sensible way, but I'd rather suppress warnings
        @SuppressWarnings("unchecked")
	public static <T> IteratorTransform<T> iterator()
	{
	    return (IteratorTransform<T>)ITERATOR;
	}

	public static final Transform<Collection, Integer> SIZE =
	    new Transform<Collection,Integer>() {
	    public Integer apply(Collection c) {
		return c == null ? 0 : c.size();
	    }
	};

        // I _could_ return something not a singleton here
        // to avoid a warning, and implement equals in a
        // sensible way, but I'd rather suppress warnings
        @SuppressWarnings("unchecked")
	public static <C extends Collection> Transform<C,Integer> size()
	{
	    return (Transform<C, Integer>)SIZE;
	}

	public static final Predicate<Collection> IS_EMPTY =
	    new Predicate<Collection>() {
	    public Boolean apply(Collection x)
            {
	        return x.isEmpty();
            }
	};

        // I _could_ return something not a singleton here
        // to avoid a warning, and implement equals in a
        // sensible way, but I'd rather suppress warnings
        @SuppressWarnings("unchecked")
        public static <C extends Collection> Predicate<C> isEmpty()
	{
	    return (Predicate<C>)IS_EMPTY;
	}
    }
}