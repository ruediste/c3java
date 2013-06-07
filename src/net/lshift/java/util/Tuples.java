package net.lshift.java.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Access static methods for creating and manipulating tuples.
 * @author david
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class Tuples
    // This will extend the largest tuple class I implement, 4-tuple for now
    extends FiveTuple
{
    private static final long serialVersionUID = 1L;

    private Tuples(Object first, Object second, Object third, Object fourth, Object fifth) {
        super(first, second, third, fourth, fifth);
    }

    public static <T,U> TwoTuple<T,U> tuple(T a, U b)
    {
        return new TwoTuple<T,U>(a,b);
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
    public static <K,V> Map<K,V> map(TwoTuple<K,V> ... tuples) {
        return map(Arrays.asList(tuples));
    }

    private static <T,U> Transform<Iterator<Object>, TwoTuple<T,U>> twoTuple() {
        return new Transform<Iterator<Object>, TwoTuple<T,U>>() {
            public TwoTuple<T,U> apply(Iterator<Object> x) {
                return new TwoTuple<T,U>(x);
            }
        };
    }

    public static <T,U,V,W> Iterator<TwoTuple<T,U>> zip(
        TwoTuple<Iterable<T>, Iterable<U>> tl, 
        Transform<Iterator<Object>, FourTuple<T, U, V, W>> factory)
    {
        List<Iterable<Object>> l = (List<Iterable<Object>>)((List<?>)tl);
        return Iterators.zip(l, Tuples.<T,U>twoTuple());
    }

    public static <T,U,V> ThreeTuple<T,U,V> tuple(T a, U b, V c)
    {
        return new ThreeTuple<T,U,V>(a,b,c);
    }

    private static <T,U,V> Transform<Iterator<Object>, ThreeTuple<T,U,V>> threeTuple() {
        return new Transform<Iterator<Object>, ThreeTuple<T,U,V>>() {
            public ThreeTuple<T,U,V> apply(Iterator<Object> x) {
                return new ThreeTuple<T, U, V>(x);
            }
        };
    }

    public static <T,U,V,W> Iterator<ThreeTuple<T,U,V>> zip(
        ThreeTuple<Iterable<T>, Iterable<U>, Iterable<V>> tl, 
        Transform<Iterator<Object>, FourTuple<T, U, V, W>> factory) {
        List<Iterable<Object>> l = (List<Iterable<Object>>)((List<?>)tl);
        return Iterators.zip(l, Tuples.<T,U,V>threeTuple());
    }

    public static <T> OneTuple<T> tuple(T a) {
        return new OneTuple<T>(a);
    }

    public static <T,U,V,W> FourTuple<T,U,V,W> tuple(T a, U b, V c, W d) {
        return new FourTuple<T,U,V,W>(a,b,c, d);
    }

    private static <T,U,V,W> Transform<Iterator<Object>, FourTuple<T,U,V,W>> fourTuple() {
        return new Transform<Iterator<Object>, FourTuple<T,U,V,W>>() {
            public FourTuple<T, U, V, W> apply(Iterator<Object> x) {
                return new FourTuple<T, U, V, W>(x);
            }
        };
    }

    public static <T,U,V,W> Iterator<FourTuple<T,U,V,W>> zip(
        FourTuple<Iterable<T>, Iterable<U>, Iterable<V>, Iterable<W>> tl, 
        Transform<Iterator<Object>, FourTuple<T, U, V, W>> factory) {
        List<Iterable<Object>> l = (List<Iterable<Object>>)((List<?>)tl);
        return Iterators.zip(l, Tuples.<T, U, V, W>fourTuple());
    }

    public static <T> Transform<OneTuple<T>,T> first() {
        return new Transform<OneTuple<T>,T>() {
            public T apply(OneTuple<T> x) {
                return x.first;
            }
        };
    }

    public static <T> Transform<TwoTuple<?, T>,T> second() {
        return new Transform<TwoTuple<?, T>,T>() {
            public T apply(TwoTuple<?, T> x) {
                return x.second;
            }
        };
    }

    public static <T> Transform<ThreeTuple<?, ?, T>,T> third() {
        return new Transform<ThreeTuple<?, ?, T>,T>() {
            public T apply(ThreeTuple<?, ?, T> x) {
                return x.third;
            }
        };
    }

    public static <T> Transform<FourTuple<?, ?, ?, T>,T> fourth() {
        return new Transform<FourTuple<?, ?, ?, T>,T>() {
            public T apply(FourTuple<?, ?, ?, T> x) {
                return x.fourth;
            }
        };
    }


}
