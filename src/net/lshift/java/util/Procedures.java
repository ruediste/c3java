package net.lshift.java.util;

import java.util.Map;

public class Procedures
{
    public static <V> Predicate<V> equal(final V value)
    {
        return new Predicate<V>() {
            public Boolean apply(V x)
            {
                return value.equals(x);
            }
        };
    }

    public static <V> Predicate<V> eq(final V value)
    {
        return new Predicate<V>() {
            public Boolean apply(V x)
            {
                return value == x;
            }
        };
    }
    
    public static <K,V> Transform<K,V> get(final Map<K,V> map)
    {
        return new Transform<K,V>() {

            public V apply(K x)
            {
                return map.get(x);
            }
            
        };
    }
}
