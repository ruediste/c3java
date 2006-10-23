package net.lshift.java.util;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UtilTest
    extends TestCase
{
    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite("lang");
        suite.addTest(new TestSuite(PairTest.class));
        suite.addTest(new TestSuite(ListsTest.class));
        suite.addTest(new TestSuite(CollectionsTest.class));
        return suite;
    }
}
