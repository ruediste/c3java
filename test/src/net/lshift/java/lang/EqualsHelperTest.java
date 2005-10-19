
package net.lshift.java.lang;

import java.util.*;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import net.lshift.java.util.*;

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