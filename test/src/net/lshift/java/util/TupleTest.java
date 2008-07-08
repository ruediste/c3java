package net.lshift.java.util;

import static net.lshift.java.util.Lists.list;
import static net.lshift.java.util.Tuples.tuple;
import static net.lshift.java.util.Tuples.map;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;

public class TupleTest
    extends TestCase
{
    @SuppressWarnings("unchecked")
    public void staticInitializersTest()
    {
        // This basically shows how when I import ThreeTuple.tuple I
        // also import all the overloaded 'tuple' methods in the super
        // classes.
        Map<Character,Integer> m = map(
                        list(tuple('a', 1),
                             tuple('b', 2)));
        List<ThreeTuple<Character,Integer,Integer>> l1 = list(
                        tuple('a', 1, 0),
                        tuple('b', 2, 0));
        
    }
}
