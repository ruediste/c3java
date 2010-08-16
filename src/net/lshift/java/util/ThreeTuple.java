package net.lshift.java.util;

import java.io.Serializable;

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

    public static <T,U,V> ThreeTuple<T,U,V> tuple(T a, U b, V c)
    {
        return new ThreeTuple<T,U,V>(a,b,c);
    }
}
