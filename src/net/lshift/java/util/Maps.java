package net.lshift.java.util;

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
    
    public static <K,V> Map<K,V> map(Iterable<Map.Entry<K,V>> c)
    {
        Map<K,V> m = new HashMap<K,V>();
        for(Map.Entry<K, V> e: c)
            m.put(e.getKey(), e.getValue());
        return m;
    }
}
