package net.lshift.java.dispatch;

import java.io.Serializable;
import java.util.*;
import java.lang.reflect.*;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class JavaC3Test
    extends TestCase
{
    public static TestSuite suite()
    {
        return new TestSuite(JavaC3Test.class);
    }

    public void testDirectSuperClasses()
    {
	assertEquals
	    (Arrays.asList
	     (new Class [] { AbstractSet.class,
			     Set.class,
			     Cloneable.class,
			     Serializable.class }),
	     JavaC3.directSuperclasses(HashSet.class));
    }

    public void testSet()
    {
	List linearization = JavaC3.allSuperclasses(Set.class);
	System.out.println("Set: " + linearization);
    }

    public void testAbstractSet()
    {
	List linearization = JavaC3.allSuperclasses(AbstractSet.class);
	System.out.println("AbstractSet: " + linearization);
    }

    public void testHashSet()
    {
	List linearization = JavaC3.allSuperclasses(HashSet.class);
	System.out.println("HashSet: " + linearization);
    }
}