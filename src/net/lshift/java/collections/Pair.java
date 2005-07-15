
package net.lshift.java.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.AbstractCollection;
import java.util.NoSuchElementException;

/**
 * Implement Collection using LISP style pairs.
 */
public class Pair<T>
    extends AbstractCollection
    implements Collection
{
    private T car;
    private Pair<T> cdr;

    private Pair(T car, Pair<T> cdr)
    {
	this.car = car;
	this.cdr = cdr;
    }

    public T car() {
	return car;
    }

    public Pair<T> cdr() {
	return cdr;
    }

    public static <U> Pair<U> cons(U car, Pair<U> cdr)
    {
	return new Pair(car, cdr);
    }

    public static class Cons<U>
	implements Lists.FoldProcedure<U,Pair<U>>
    {
	public Lists.FoldState<Pair<U>> apply(U car, Pair<U> cdr)
	{
	    return new Lists.FoldState(true, new Pair(car, cdr));
	}
    }

    public static <U> Pair<U> list(Collection<U> c)
    {
	return Lists.foldLeft(new Cons<U>(), null, c);
    }

    public static <U> Pair<U> list(U ... a)
    {
	return list(Arrays.asList(a));
    }

    public Iterator<T> iterator()
    {
	return new Iterator() {

		Pair<T> pair = Pair.this;

		public boolean hasNext()
		{
		    return pair != null;
		}

		public T next()
		{
		    T car = pair.car();
		    pair = pair.cdr();
		    return car;
		}

		public void remove()
		{
		    throw new UnsupportedOperationException();
		}
	    };
    }

    public int size()
    {
	Pair pair = this;
	for(int i = 0;; pair = pair.cdr(), ++i)
	    if(pair == null) return i;
    }


}