
package net.lshift.java.dispatch;

import java.util.*;
import java.lang.reflect.*;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class DynamicDispatchTest
    extends TestCase
{
    public static TestSuite suite()
    {
        return new TestSuite(DynamicDispatchTest.class);
    }

    public interface X { }

    public static class A { }
    public static class B extends A { }
    public static class C extends A { }
    public static class D extends A { }
    public static class E extends A implements X { }

    public static class ToStringABC
    {
	public String toString(A a)
	{
	    return "A";
	}

	public String toString(B b)
	{
	    return "B";
	}

	public String toString(C c)
	{
	    return "C";
	}

	public String toString(X c)
	{
	    return "X";
	}

	public String toString(B b, C c)
	{
	    return "BC";
	}

	public String toString(C c, B b)
	{
	    return "CB";
	}
    }

    public interface ToString
    {
	public String toString(Object o);
	public String toString(Object o1, Object o2);
    }

    private Method toStringProcedure()
	throws Exception
    {
	return ToString.class.getDeclaredMethod
	    ("toString", new Class [] { Object.class });
    }

    private Method toString2Procedure()
	throws Exception
    {
	return ToString.class.getDeclaredMethod
	    ("toString", new Class [] { Object.class, Object.class });
    }

    private Method toStringMethod(Class c)
	throws Exception
    {
	return ToStringABC.class.getDeclaredMethod
	    ("toString", new Class [] { c });
    }

    private Method toStringMethod(Class c1, Class c2)
	throws Exception
    {
	return ToStringABC.class.getDeclaredMethod
	    ("toString", new Class [] { c1, c2 });
    }

    public void testAppliesTo()
	throws Exception
    {
	assertTrue("procedure applies to argument types",
		   DynamicDispatch.appliesTo
		   (toStringProcedure(), new Class [] { A.class }));
	assertTrue("procedure applies to method",
		   DynamicDispatch.appliesTo
		   (toStringProcedure(), toStringMethod(A.class)));
    }

    public void testProcedureMethods()
	throws Exception
    {
	Set methods = DynamicDispatch.procedureMethods
	    (toStringProcedure(), ToStringABC.class.getDeclaredMethods());
	assertEquals(4, methods.size());
	assertTrue("contains A", methods.contains(toStringMethod(A.class)));
	assertTrue("contains B", methods.contains(toStringMethod(B.class)));
	assertTrue("contains C", methods.contains(toStringMethod(C.class)));
	assertTrue("contains X", methods.contains(toStringMethod(X.class)));

	methods = DynamicDispatch.procedureMethods
	    (toString2Procedure(), ToStringABC.class.getDeclaredMethods());
	assertEquals(2, methods.size());
	assertTrue("contains BC", methods.contains(toStringMethod(B.class, C.class)));
	assertTrue("contains CB", methods.contains(toStringMethod(C.class, B.class)));

    }

    public void testToStringABC()
    {
	ToString x = (ToString)DynamicDispatch.proxy(ToString.class, new ToStringABC());
	assertEquals("A", x.toString(new A()));
	assertEquals("B", x.toString(new B()));
	assertEquals("C", x.toString(new C()));
	assertEquals("A", x.toString(new D()));
	assertEquals("BC", x.toString(new B(), new C()));
	assertEquals("CB", x.toString(new C(), new B()));
    }
}