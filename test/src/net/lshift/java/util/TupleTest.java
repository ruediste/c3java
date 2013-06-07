package net.lshift.java.util;

import static net.lshift.java.util.Lists.list;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class TupleTest
    extends TestCase
{
    @SuppressWarnings("unchecked")
    public void testStaticInitializers()
    {
        // This basically shows how when I import ThreeTuple.tuple I
        // also import all the overloaded 'tuple' methods in the super
        // classes.
        @SuppressWarnings("unused")
        List<ThreeTuple<Character,Integer,Integer>> l1 = list(
                        Tuples.tuple('a', 1, 0),
                        Tuples.tuple('b', 2, 0));
    }
    
    @SuppressWarnings("unchecked")
    public void testMapStaticInitializers()
    {
        @SuppressWarnings("unused")
        Map<Character,Integer> m1 = Tuples.map(
            list(Tuples.tuple('a', 1),
                 Tuples.tuple('b', 2)));
        @SuppressWarnings("unused")
        Map<Character,Integer> m2 = Tuples.map(
            Tuples.tuple('a', 1),
            Tuples.tuple('b', 2)); 
    }
    
    @SuppressWarnings("unchecked")
    public void testEquals()
    {
        assertEquals(Tuples.tuple('a'), list('a'));
        assertEquals(Tuples.tuple('a', 1), list('a', 1));
        assertEquals(Tuples.tuple('a', 1, 0), list('a', 1, 0));
    }
}
