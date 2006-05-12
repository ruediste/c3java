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

package net.lshift.java.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import junit.framework.TestCase;
import net.lshift.java.util.AbstractBag;

public class EqualsHelperTest
    extends TestCase
{
    public class A
    {
        public A(String a1, String a2, String a3)
        {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
        }

        public String a1;
        public String a2;
        public String a3;
    }

    public class B
    {
        public C c1;
    }

    public class C
    {
        public C(A a1, B b1)
        {
            this.a1 = a1;
            this.b1 = b1;
        }

        public A a1;
        public B b1;
    }

    public C exampleC1(String x)
    {
        B b = new B();
        C c = new C(new A(x, x, null), b);
        b.c1 = c;
        return c;
    }

    public List list(Object a, Object b, Object c)
    {
        return Arrays.asList(new Object[] { a, b, c });
    }

    public void testObjects()
    {
        assertTrue(EqualsHelper.equals(exampleC1("a"), exampleC1("a")));
        assertFalse(EqualsHelper.equals(exampleC1("a"), exampleC1("b")));
    }

    public void testSets()
    {
        assertTrue
            (EqualsHelper.equals
             (new TreeSet(list("1", "2", "3")),
              new HashSet(list("1", "2", "3"))));
        assertFalse
            (EqualsHelper.equals
             (new TreeSet(list("1", "2", "3")),
              new HashSet(list("1", "2", "4"))));
    }

    public void testLists()
    {
        assertTrue
            (EqualsHelper.equals
             (new ArrayList(list("1", "2", "3")),
              new LinkedList(list("1", "2", "3"))));
        assertFalse
            (EqualsHelper.equals
             (new ArrayList(list("1", "2", "3")),
              new ArrayList(list("1", "2", "4"))));
        assertTrue
            (EqualsHelper.equals
             (new ArrayList(list(exampleC1("1"),
                                 exampleC1("2"), 
                                 exampleC1("3"))),
              new LinkedList(list(exampleC1("1"), 
                                  exampleC1("2"), 
                                  exampleC1("3")))));
        assertFalse
            (EqualsHelper.equals
             (new ArrayList(list(exampleC1("1"),
                                 exampleC1("2"), 
                                 exampleC1("3"))),
              new LinkedList(list(exampleC1("1"), 
                                  exampleC1("2"), 
                                  exampleC1("4")))));
    }

    public void testBags()
    {
        assertTrue(EqualsHelper.equals
                   (AbstractBag.wrap(list("1", "2", "3")),
                    AbstractBag.wrap(list("3", "2", "1"))));
        assertTrue(EqualsHelper.equals
                   (AbstractBag.wrap(list("1", "2", "3")),
                    AbstractBag.wrap(list("1", "2", "3"))));
        assertTrue(EqualsHelper.equals
                   (AbstractBag.wrap(list("1", "1", "3")),
                    AbstractBag.wrap(list("3", "1", "1"))));
        assertFalse(EqualsHelper.equals
                    (AbstractBag.wrap(list("1", "2", "3")),
                     AbstractBag.wrap(list("1", "2", "4"))));
        assertFalse(EqualsHelper.equals
                    (AbstractBag.wrap(list("1", "1", "3")),
                     AbstractBag.wrap(list("3", "2", "1"))));

        assertTrue
            (EqualsHelper.equals
             (AbstractBag.wrap(list(exampleC1("1"),
                                    exampleC1("2"), 
                                    exampleC1("3"))),
              AbstractBag.wrap(list(exampleC1("3"), 
                                    exampleC1("2"), 
                                    exampleC1("1")))));
    }
}