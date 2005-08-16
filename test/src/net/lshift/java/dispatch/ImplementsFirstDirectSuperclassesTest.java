package net.lshift.java.dispatch;

import java.io.Serializable;
import java.util.*;
import java.lang.reflect.*;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class ImplementsFirstDirectSuperclassesTest
    extends TestCase
{

    public static TestSuite suite()
    {
        return new TestSuite(ImplementsFirstDirectSuperclassesTest.class);
    }

    public void testDirectSuperClasses()
    {
	assertEquals
	    (Arrays.asList
	     (new Class [] { Set.class,
			     Cloneable.class,
			     Serializable.class, 
			     AbstractSet.class }),
	     ImplementsFirstDirectSuperclasses.
	     SUPERCLASSES.directSuperclasses(HashSet.class));
    }

    public void testSet()
	throws Exception
    {
	List linearization = JavaC3.allSuperclasses
	    (Set.class,ImplementsFirstDirectSuperclasses.SUPERCLASSES);
	System.out.println("Set: " + linearization);
    }

    public void testAbstractSet()
	throws Exception
    {
	List linearization = JavaC3.allSuperclasses
	    (AbstractSet.class, ImplementsFirstDirectSuperclasses.SUPERCLASSES);
	System.out.println("AbstractSet: " + linearization);
	
    }

    public void testHashSet()
	throws Exception
    {
	List linearization = JavaC3.allSuperclasses
	    (HashSet.class, ImplementsFirstDirectSuperclasses.SUPERCLASSES);
	System.out.println("HashSet: " + linearization);
    }
}