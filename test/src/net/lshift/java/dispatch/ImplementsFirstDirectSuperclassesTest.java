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
import java.util.Set;

import junit.framework.TestCase;
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
        Iterable<Class<?>> linearization = JavaC3.allSuperclasses
            (Set.class,ImplementsFirstDirectSuperclasses.SUPERCLASSES);
        System.out.println("Set: " + linearization);
    }

    public void testAbstractSet()
        throws Exception
    {
        Iterable<Class<?>> linearization = JavaC3.allSuperclasses
            (AbstractSet.class, ImplementsFirstDirectSuperclasses.SUPERCLASSES);
        System.out.println("AbstractSet: " + linearization);

    }

//    public void testHashSet()
//      throws Exception
//    {
//      List linearization = JavaC3.allSuperclasses
//          (HashSet.class, ImplementsFirstDirectSuperclasses.SUPERCLASSES);
//      System.out.println("HashSet: " + linearization);
//    }
}
