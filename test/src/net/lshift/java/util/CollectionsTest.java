package net.lshift.java.util;

import junit.framework.TestCase;

public class CollectionsTest
    extends TestCase
{
    public void testHasNext()
    {
        assertFalse(Collections.Procedures.hasNext().apply(Lists.list().iterator()));
        assertTrue(Collections.Procedures.hasNext().apply(Lists.list(new Object()).iterator()));
    }
    
    public void testIsEmpty()
    {
        assertTrue(Collections.Procedures.isEmpty().apply(Lists.list()));
    }
}
