package net.lshift.java;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTest
    extends TestCase
{
    public static TestSuite suite()
    {
	TestSuite suite = new TestSuite("all");
	suite.addTest(net.lshift.java.dispatch.DispatchTest.suite());
	suite.addTest(net.lshift.java.lang.LangTest.suite());
	return suite;
    }
}
