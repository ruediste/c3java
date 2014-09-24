package net.lshift.java.util;

import java.io.Serializable;
import java.util.Iterator;

public class ThreeTuple<T, U, V>
    extends TwoTuple<T, U>
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    public V third;

    public ThreeTuple(T first, U second, V third)
    {
        super(first, second);
        this.third = third;
    }

    @SuppressWarnings("unchecked")
    ThreeTuple(Iterator<Object> i) {
        super((T)i.next(), (U)i.next());
        this.third = (V)i.next();
    }

    @Override
    public Object get(int index)
    {
        switch(index) {
        case 0: return first;
        case 1: return second;
        case 2: return third;
        default: throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int size()
    {
        return 3;
    }

    public V getThird()
    {
        return third;
    }
}
