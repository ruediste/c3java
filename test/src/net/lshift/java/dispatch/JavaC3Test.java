/*

Copyright (c) 2006 LShift Ltd

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

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