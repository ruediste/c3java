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