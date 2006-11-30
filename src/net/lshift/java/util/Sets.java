package net.lshift.java.util;

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
}
