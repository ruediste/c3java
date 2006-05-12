/*
     This file is part of the LShift Java Library.

    The LShift Java Library is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    The LShift Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with The LShift Java Library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.lshift.java.dispatch;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
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