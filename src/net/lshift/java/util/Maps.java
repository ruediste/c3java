package net.lshift.java.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Maps
{
    public static class EntryImpl<K, V>
        implements Map.Entry<K, V>
    {
        private final K k;
        private final V v;

        protected EntryImpl(K k, V v)
        {
            this.k = k;
            this.v = v;
        }

        public K getKey() {
            return k;
        }

        public V getValue() {
            return v;
        }

        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
        
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            @SuppressWarnings("rawtypes")
            Map.Entry e = (Map.Entry)o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2)))
                    return true;
            }
            return false;
        }
        
        public int hashCode() {
            return (k==null   ? 0 : k.hashCode()) ^
                   (v==null ? 0 : v.hashCode());
        }

        public String toString() {
            return String.format("%s: %s", k, v);
        }
    }

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
    public static <K,V> Map.Entry<K,V> entry(final K k, final V v)
    {
        return new EntryImpl<K, V>(k, v);
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
    public static <K,V> Map<K,V> map(Map.Entry<? extends K, ? extends V> ... entries) {
        return map(Arrays.asList(entries));
    }

    /**
     * Create a map with no entries - avoid varargs-related generics warnings
     */
    public static <K,V> Map<K,V> map() {
        return java.util.Collections.emptyMap();
    }

    /**
     * Create a map with one entry - avoid varargs-related generics warnings
     */
    @SuppressWarnings("unchecked")
    public static <K,V> Map<K,V> map(
        Map.Entry<? extends K, ? extends V> e1) {
        return map(Lists.<Map.Entry<? extends K, ? extends V>>list(e1));
    }

    /**
     * Create a map with two entries - avoid varargs-related generics warnings
     */
    @SuppressWarnings("unchecked")
    public static <K,V> Map<K,V> map(
        Map.Entry<? extends K, ? extends V> e1,
        Map.Entry<? extends K, ? extends V> e2) {
        return map(Arrays.asList(e1, e2));
    }

    /**
     * Create a map with three entries - avoid varargs-related generics warnings
     */
    @SuppressWarnings("unchecked")
    public static <K,V> Map<K,V> map(
        Map.Entry<? extends K, ? extends V> e1,
        Map.Entry<? extends K, ? extends V> e2,
        Map.Entry<? extends K, ? extends V> e3) {
        return map(Arrays.asList(e1, e2, e3));
    }

    /**
     * Create a map with four entries - avoid varargs-related generics warnings
     */
    @SuppressWarnings("unchecked")
    public static <K,V> Map<K,V> map(
        Map.Entry<? extends K, ? extends V> e1,
        Map.Entry<? extends K, ? extends V> e2,
        Map.Entry<? extends K, ? extends V> e3,
        Map.Entry<? extends K, ? extends V> e4) {
        return map(Arrays.asList(e1, e2, e3, e4));
    }
    
    /**
     * Fill a map.
     * @param <K>
     * @param <V>
     * @param <KX>
     * @param keys The keys for which the map should contain values
     * @param values the factory for the values
     * @return
     */
    public static <K,V> Map<K,V> fill(Iterable<K> keys, final Factory<V> values) {
        return map(Iterators.transform(keys, new Transform<K, Map.Entry<? extends K, ? extends V>>() {
            public Map.Entry<? extends K, ? extends V> apply(K key) {
                return entry(key, values.create());
            }
        }));
    }
}
