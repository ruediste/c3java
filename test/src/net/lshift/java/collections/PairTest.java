package net.lshift.java.collections;

import java.io.Serializable;
import java.util.*;
import java.lang.reflect.*;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class PairTest
    extends TestCase
{

    public static TestSuite suite()
    {
        return new TestSuite(PairTest.class);
    }

    public void testList()
    {
	Pair<Character> list = Pair.list('a', 'b', 'c');
	assertEquals(3, list.size());
	Iterator<Character> i = list.iterator();
	assertEquals('a', i.next().charValue());
	assertEquals('b', i.next().charValue());
	assertEquals('c', i.next().charValue());
	assertFalse(i.hasNext());
    }
}