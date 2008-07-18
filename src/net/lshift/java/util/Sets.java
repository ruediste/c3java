package net.lshift.java.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Sets
{
    public static <E> Set<E> union(Collection<E> ... cn)
    {
        Set<E> result = new HashSet<E>();
        for(Collection<E> c: cn)
            result.addAll(c);
        return result;
    }
    
    public static <E> Set<E> union(E [] ... cn)
    {
        Set<E> result = new HashSet<E>();
        for(E[] c: cn)
            result.addAll(Arrays.asList(c));
        return result;
    }
    
    public static <E> Set<E> union(Iterable<E> ... cn)
    {
        Set<E> result = new HashSet<E>();
        for(Iterable<E> c: cn)
            for(E e: c)
                result.add(e);
        return result;
    }
    
    
}
