package net.lshift.java.util;

import static net.lshift.java.util.Sets.set;
import junit.framework.TestCase;

public class SetsTest
extends TestCase
{
    public void testSymmetricDifference()
    {
        assertEquals(set("d", "a"), 
            Sets.symmetricDifference(
                set("a", "b", "c"), 
                set("b", "c", "d")));
        assertEquals(set("d", "a"), 
            Sets.symmetricDifference(
                set("a", "b", "c"), 
                set("d", "c", "b")));
        assertEquals(set(), Sets.symmetricDifference(
            set("a", "b", "c"), 
            set("a", "b", "c")));
        assertEquals(set(), Sets.symmetricDifference(set(), set()));
        assertEquals(set("a", "b", "c"), 
            Sets.symmetricDifference(Sets.<String>set(), set("a", "b", "c")));
    }
}
