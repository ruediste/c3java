package net.lshift.java.util;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Two tuple
 * @see OneTuple for more information
 * @author david
 *
 * @param <E>
 * @param <T>
 * @param <U>
 */
public class TwoTuple<T,U>
extends OneTuple<T>
implements Serializable
{
    private static final long serialVersionUID = 1L;

    public U second;
    
    public TwoTuple(T first, U second) {
        super(first);
        this.second = second;
    }

    @SuppressWarnings("unchecked")
    public TwoTuple(Iterator<Object> i)
    {
        super((T)i.next());
        this.second = (U)i.next();
    }

    @Override
    public Object get(int index)
    {
        switch(index) {
        case 0: return first;
        case 1: return second;
        default: throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int size()
    {
        return 2;
    }
    
    public U getSecond()
    {
        return second;
    }
}
