package net.lshift.java.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    public static <T,U> TwoTuple<T,U> tuple(T a, U b)
    {
        return new TwoTuple<T,U>(a,b);
    }
    
    public static <T> Transform<TwoTuple<?, T>,T> second()
    {
        return new Transform<TwoTuple<?, T>,T>() {
            public T apply(TwoTuple<?, T> x) {
                return x.second;
            }
        };
    }

    /**
     * Create a map from an iterable of two tuples.
     * There's more than one way to do this. Take your pick.
     * @see Maps#map(Iterable)
     * @param <K>
     * @param <V>
     * @param c
     * @return
     */
    public static <K,V> Map<K,V> map(Iterable<TwoTuple<K, V>> c)
    {
        Map<K,V> m = new HashMap<K,V>();
        for(TwoTuple<K, V> e: c)
            m.put(e.first, e.second);
        return m;
    }
    
    /**
     * Create a map from a list of tuples.
     * There's more than one way to do this, take your pick.
     * @see Maps#map(java.util.Map.Entry...)
     * @param <K>
     * @param <V>
     * @param tuples
     * @return
     */
    public static <K,V> Map<K,V> map(TwoTuple<K,V> ... tuples)
    {
        return map(Arrays.asList(tuples));
    }
}
