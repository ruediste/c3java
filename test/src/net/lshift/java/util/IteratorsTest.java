package net.lshift.java.util;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class IteratorsTest
    extends TestCase
{
    public void testLimit()
    {
        List<Integer> result = Lists.asList
            (Iterators.limit
             (Arrays.asList(new Integer [] { 1, 2, 3, 4, 5 }), 3));
        assertEquals(3, result.size());
        for(int i = 0; i != 3; ++i)
            assertEquals(new Integer(i + 1), result.get(i));
    }
}
