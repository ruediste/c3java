package net.lshift.java.util;

import static net.lshift.java.util.Collections.Procedures.SIZE_INCREASING;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Sets
{
    public static <E> Set<E> set()
    {
        return new HashSet<E>();
    }
    
    public static <E> Set<E> set(E ... en)
    {
        return new HashSet<E>(Arrays.asList(en));
    }
    
    public static <E> Set<E> union(Collection<E> ... cn)
    {
        Set<E> result = new HashSet<E>();
        for(Collection<E> c: cn)
            result.addAll(c);
        return result;
    }
    
    public static <E> Set<E> union(Collection<E> c1)
    {
        Set<E> result = new HashSet<E>();
        result.addAll(c1);
        return result;
    }

    public static <E> Set<E> union(Collection<E> c1, Collection<E> c2)
    {
        Set<E> result = new HashSet<E>();
        result.addAll(c1);
        result.addAll(c2);
        return result;
    }

    public static <E> Set<E> union(
        Collection<E> c1, 
        Collection<E> c2,
        Collection<E> c3)
    {
        Set<E> result = new HashSet<E>();
        result.addAll(c1);
        result.addAll(c2);
        result.addAll(c3);
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


    public static <E> Set<E> intersection(Set<E> ... cn)
    {
        if(cn.length < 1)
            return java.util.Collections.emptySet();
        
        Set<E> result = new HashSet<E>();
        java.util.Collections.sort(Arrays.asList(cn), SIZE_INCREASING);
        result.addAll(cn[0]);
        for(int i = 1; i < cn.length; ++i)
            result.retainAll(cn[i]);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public static final <E> Set<E> symmetricDifference(Set<E> a, Set<E> b)
    {
        Set<E> result = union(a,b);
        result.removeAll(intersection(a, b));
        return result;
    }
    
    public static final <E> Set<E> xor(Set<E> a, Set<E> b)
    {
        return symmetricDifference(a, b);
    }
}
