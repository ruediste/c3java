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

    public void testWidening()
    {
	// this is here because it won't compile if my
	// assumptions about widening are wrong...
	byte b = 1;
	int i = b;
	char c = '1';
	i = c;
	long l =  i;
	float f = l;
	double d = f;
    }

    public void testDirectSuperClasses()
    {
	assertEquals
	    (Arrays.asList
	     (new Class [] { AbstractSet.class,
			     Set.class,
			     Cloneable.class,
			     Serializable.class }),
	     DefaultDirectSuperclasses.SUPERCLASSES.directSuperclasses(HashSet.class));
    }

    public void testSet()
	throws Exception
    {
	List linearization = JavaC3.allSuperclasses(Set.class);
	System.out.println("Set: " + linearization);
    }

    public void testAbstractSet()
	throws Exception
    {
	List linearization = JavaC3.allSuperclasses(AbstractSet.class);
	System.out.println("AbstractSet: " + linearization);
	
    }

    public void testHashSet()
	throws Exception
    {
	List linearization = JavaC3.allSuperclasses(HashSet.class);
	System.out.println("HashSet: " + linearization);
    }

    public void testPrimitives()
	throws Exception
    {
	List linearization = JavaC3.allSuperclasses(Double.TYPE);
	System.out.println("double: " + linearization);
	linearization = JavaC3.allSuperclasses(Boolean.TYPE);
	System.out.println("boolean: " + linearization);
    }

    public void testArrays()
	throws Exception
    {
	HashSet [] array1 = new HashSet[0];
	List linearization = JavaC3.allSuperclasses(array1.getClass());
	System.out.println("HashSet[]: " + linearization);
	HashSet [][] array2 = new HashSet[0][0];
	linearization = JavaC3.allSuperclasses(array2.getClass());
	System.out.println("HashSet[][]: " + linearization);
    }
}