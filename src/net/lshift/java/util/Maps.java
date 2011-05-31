package net.lshift.java.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Maps
{
    public static <A,B> Map<B,A> invert(Map<A,B> map)
    {
        Map<B,A> result = new HashMap<B,A>();
        for(Map.Entry<A, B> entry: map.entrySet())
            result.put(entry.getValue(), entry.getKey());
        return result;
    }
    
    public static <K,V> Map<K,V> map(Iterable<Map.Entry<? extends K, ? extends V>> c)
    {
        Map<K,V> m = new HashMap<K,V>();
        for(Map.Entry<? extends K, ? extends V> e: c)
            m.put(e.getKey(), e.getValue());
        return m;
    }
    
    /**
     * Create a map entry
     * @see #map(java.util.Map.Entry...)
     * @param <K> key type of returned Map
     * @param <V> value type of returned type
     * @param k key for the entry
     * @param v value for the entry
     * @return an entry with key k, and value v
     */
    // public static <K,V> Map.Entry<K,V> entry(final K k, final V v)
    public static <K,V> Map.Entry<K,V> entry(final K k, final V v)
    {
        return new Map.Entry<K, V>() {
            public K getKey() {
                return k;
            }

            public V getValue() {
                return v;
            }

            public V setValue(V value) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Create a map.
     * This is a quick notation for maps:
     * map(entry("a",1), entry(
     * @param <K>
     * @param <V>
     * @param entries
     * @return
     */
    public static <K,V> Map<K,V> map(Map.Entry<? extends K, ? extends V> ... entries)
    {
        return map(Arrays.asList(entries));
    }
}
