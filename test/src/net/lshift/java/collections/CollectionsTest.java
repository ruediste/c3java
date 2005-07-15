
package net.lshift.java.collections;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class CollectionsTest
    extends TestCase
{
    public static TestSuite suite()
    {
	TestSuite suite = new TestSuite("all");
	suite.addTest(ListsTest.suite());
	suite.addTest(PairTest.suite());
	return suite;
    }
}
