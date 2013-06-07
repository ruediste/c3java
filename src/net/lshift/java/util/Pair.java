
package net.lshift.java.util;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Implement Collection using LISP style pairs.
 */
public class Pair<E>
    extends AbstractList<E>
    implements List<E>
{
    private E car;
    private Pair<E> cdr;

    private Pair(E car, Pair<E> cdr)
    {
	this.car = car;
	this.cdr = (Pair<E>)cdr;
    }

    public E head() {
	return car;
    }

    public Pair<E> tail() {
	return cdr;
    }

    public static <U> Pair<U> pair(U car, Pair<U> cdr)
    {
	return new Pair<U>(car, cdr);
    }

    public static <U> Pair<U> pair(U car, List<U> cdr)
    {
        if(cdr instanceof Pair)
            return new Pair<U>(car, (Pair<U>)cdr);
        else
            return new Pair<U>(car, Lists.foldRight(new Cons<U>(), null, cdr));
    }

    public static class Cons<U>
        implements Lists.FoldProcedure<U,Pair<U>>
    {
        public Lists.FoldState<Pair<U>> apply(U car, Pair<U> cdr)
        {
            return new Lists.FoldState<Pair<U>>(true, new Pair<U>(car, cdr));
        }
    }


    public static <U> Pair<U> list(U ... a)
    {
        return Lists.foldRight(new Cons<U>(), null, Arrays.asList(a));
    }

    public Iterator<E> iterator()
    {
	return new Iterator<E>() {

	    Pair<E> pair = Pair.this;

	    public boolean hasNext()
	    {
	        return pair != null;
	    }

	    public E next()
	    {
	        E car = pair.head();
	        pair = pair.tail();
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
        int size = 0;
	for(Pair<E> pair = this; pair != null; pair = pair.cdr)
            size++;
	return size;
    }

    @Override
    public E get(int index)
    {
        Pair<E> pair = this;
        while(index > 0 && pair != null) {
            pair = pair.cdr;
            index = index - 1;
        }

        if(index == 0 && pair != null)
            return pair.car;
        else
            throw new IndexOutOfBoundsException();
    }



}