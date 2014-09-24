package net.lshift.java.util;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

/**
 * One tuple.
 * Tuples in general exist as a quick way to make a composite key
 * for a map. You may well find other uses for them.
 * Tuples extends AbstractList to get sensible hashCode and equals. They
 * are read only lists - supporting set would break the type safety which is
 * much of the point. Note that equality only requires the 'other' is a list,
 * containing one element equal to this member, not that its a OneTuple.
 * this is a degenerative case - its unlikely you will
 * ever use this, except perhaps as a list of length one.
 *
 * @param <E> This is the element type of the list, It must be a superclass
 * of T. For other tuples, it must be the superclass of all the fields.
 * @param <T> The type of the first member
 */
public class OneTuple<T>
extends AbstractList<Object>
implements List<Object>, Serializable
{
    private static final long serialVersionUID = 1L;

    public final T first;

    public OneTuple(T first)
    {
        this.first = first;
    }

    @Override
    public Object get(int index)
    {
        if(index != 0)
            throw new IndexOutOfBoundsException();
        return first;
    }

    @Override
    public int size()
    {
        return 1;
    }

    public T getFirst()
    {
        return first;
    }

}
