package net.lshift.java.lang;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LangTest
    extends TestCase
{
    public static TestSuite suite()
    {
	TestSuite suite = new TestSuite("lang");
	suite.addTest(new TestSuite(EqualsHelperTest.class));
	return suite;
    }
}
