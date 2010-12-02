package net.lshift.java.util;

import java.util.Iterator;
import java.util.List;

public class FourTuple<T, U, V, W>
    extends ThreeTuple<T, U, V>
{
    private static final long serialVersionUID = 1L;
    public final W fourth;
    
    public FourTuple(T first, U second, V third, W fourth) 
    {
        super(first, second, third);
        this.fourth = fourth;
    }
    
    @SuppressWarnings("unchecked")
    private FourTuple(Iterator<Object> i)
    {
        super((T)i.next(), (U)i.next(), (V)i.next());
        this.fourth = (W)i.next();
    }

    @Override
    public Object get(int index)
    {
        switch(index) {
        case 0: return first;
        case 1: return second;
        case 2: return third;
        case 3: return fourth;
        default: throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int size()
    {
        return 4;
    }
    
    public static <T,U,V,W> FourTuple<T,U,V,W> tuple(T a, U b, V c, W d)
    {
        return new FourTuple<T,U,V,W>(a,b,c, d);
    }
    
    private static <T,U,V,W> Transform<Iterator<Object>, FourTuple<T,U,V,W>> fourTuple()
    {
        return new Transform<Iterator<Object>, FourTuple<T,U,V,W>>() {
            public FourTuple<T, U, V, W> apply(Iterator<Object> x) {
                return new FourTuple<T, U, V, W>(x);
            }
        };
    }

    public static <T,U,V,W> Iterator<FourTuple<T,U,V,W>> zip(
        FourTuple<Iterable<T>, Iterable<U>, Iterable<V>, Iterable<W>> tl, 
        Transform<Iterator<Object>, FourTuple<T, U, V, W>> factory)
    {
        @SuppressWarnings("unchecked")
        List<Iterable<Object>> l = (List<Iterable<Object>>)((List<?>)tl);
        return Iterators.zip(l, FourTuple.<T, U, V, W>fourTuple());
    }
}
