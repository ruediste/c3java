package net.lshift.java.dispatch;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTest
    extends TestCase
{
    public static TestSuite suite()
    {
	TestSuite suite = new TestSuite("all");
	suite.addTest(JavaC3Test.suite());
	suite.addTest(DynamicDispatchTest.suite());
	return suite;
    }
}
