package net.lshift.java.dispatch;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class DispatchTest
    extends TestCase
{
    public static TestSuite suite()
    {
	TestSuite suite = new TestSuite("dispatch");
	suite.addTest(JavaC3Test.suite());
	// suite.addTest(ImplementsFirstDirectSuperclassesTest.suite());
	suite.addTest(DynamicDispatchTest.suite());
	return suite;
    }
}
