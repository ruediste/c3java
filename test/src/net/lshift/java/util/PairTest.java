package net.lshift.java.util;

import junit.framework.TestCase;

public class PairTest
    extends TestCase
{
    public void testPair()
    {
        Pair<Integer> triple = Pair.pair(1, Pair.pair(2, Pair.pair(3, null)));
        assertEquals(3, triple.size());
        assertEquals(1, triple.get(0).intValue());
        assertEquals(2, triple.get(1).intValue());
        assertEquals(3, triple.get(2).intValue());
    }
}
