
package net.lshift.java.collections;

import java.util.Collection;
import java.util.Iterator;

public class Collections
{
    public static class Procedures
    {
	public interface HasNextPredicate<T>
	    extends Predicate<Iterator<T>> { }

	public static final HasNextPredicate<Object> HAS_NEXT =
	    new HasNextPredicate<Object>() {
		public Boolean apply(Iterator i) {
		    return i.hasNext();
		}
	    };

	public static <T> HasNextPredicate<T> hasNext()
	{
	    return (HasNextPredicate<T>)HAS_NEXT;
	}

	public interface NextTransform<T>
	    extends Transform<Iterator<T>,T> { }

	public static final NextTransform<Object> NEXT =
	    new NextTransform<Object>() {
	    public Object apply(Iterator i) {
		return i.next();
	    }
	};

	public static <T> NextTransform<T> next()
	{
	    return (NextTransform<T>)NEXT;
	}

	public static interface IteratorTransform<T>
	    extends Transform<Collection<T>,Iterator<T>> { }

	
	public static final IteratorTransform<Object> ITERATOR =
	    new IteratorTransform<Object>() {
	    public Iterator apply(Collection c) {
		return c.iterator();
	    }
	};

	public static <T> IteratorTransform<T> iterator()
	{
	    return (IteratorTransform<T>)ITERATOR;
	}

	public static interface SizeTransform<T>
	    extends Transform<Collection<T>,Integer> { }

	
	public static final SizeTransform<Object> SIZE =
	    new SizeTransform<Object>() {
	    public Integer apply(Collection c) {
		return c == null ? 0 : c.size();
	    }
	};

	public static <T> SizeTransform<T> size()
	{
	    return (SizeTransform<T>)SIZE;
	}

	public static interface IsEmptyPredicate<T>
	    extends Predicate<Collection<T>> { }

	
	public static final IsEmptyPredicate<Object> IS_EMPTY =
	    new IsEmptyPredicate<Object>() {
	    public Boolean apply(Collection c) {
		return c == null || c.isEmpty();
	    }
	};

	public static <T> IsEmptyPredicate<T> isEmpty()
	{
	    return (IsEmptyPredicate<T>)IS_EMPTY;
	}
    }
}