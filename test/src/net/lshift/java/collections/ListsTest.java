package net.lshift.java.collections;

import java.io.Serializable;
import java.util.*;
import java.lang.reflect.*;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class ListsTest
    extends TestCase
{

    public static TestSuite suite()
    {
        return new TestSuite(ListsTest.class);
    }

    public void testFoldLeft()
    {
	assertEquals(Pair.<Character>list('a', 'b', 'c'),
		     Lists.foldLeft(new Pair.Cons<Character>(), null,
				    Pair.<Character>list('a', 'b', 'c')));
    }

}