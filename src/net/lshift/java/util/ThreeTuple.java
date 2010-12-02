package net.lshift.java.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

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
    private ThreeTuple(Iterator<Object> i)
    {
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

    public static <T,U,V> ThreeTuple<T,U,V> tuple(T a, U b, V c)
    {
        return new ThreeTuple<T,U,V>(a,b,c);
    }
    
    private static <T,U,V> Transform<Iterator<Object>, ThreeTuple<T,U,V>> threeTuple()
    {
        return new Transform<Iterator<Object>, ThreeTuple<T,U,V>>() {
            public ThreeTuple<T,U,V> apply(Iterator<Object> x) {
                return new ThreeTuple<T, U, V>(x);
            }
        };
    }

    public static <T,U,V,W> Iterator<ThreeTuple<T,U,V>> zip(
        ThreeTuple<Iterable<T>, Iterable<U>, Iterable<V>> tl, 
        Transform<Iterator<Object>, FourTuple<T, U, V, W>> factory)
    {
        @SuppressWarnings("unchecked")
        List<Iterable<Object>> l = (List<Iterable<Object>>)((List<?>)tl);
        return Iterators.zip(l, ThreeTuple.<T,U,V>threeTuple());
    }
}
