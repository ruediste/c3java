package net.lshift.java.util;

import net.lshift.java.util.RandomExponentialBackoff.Session;
import junit.framework.TestCase;

public class RandomExponentialBackoffTest
    extends TestCase
{
    RandomExponentialBackoff backoff = new RandomExponentialBackoff();

    public void testNewSession()
    {
        backoff.newSession();
    }

    public void testNoBackoff()
    {
        double before = backoff.getAverageSuccessTime();
        Session session = backoff.newSession();
        session.complete();
        assertEquals(before, backoff.getAverageSuccessTime());
    }

    public void testBackoff(int n)
        throws InterruptedException
    {
        Session session = backoff.newSession();
        for(int i = 0; i != n; ++i)
            session.backoff();
        session.complete();
        System.out.println("average success time = " + backoff.getAverageSuccessTime());
    }

    public void testBackoff()
        throws InterruptedException
    {
        // careful! if you consecutively increase n the execution time
        // is O(exp(sum(n))
        testBackoff(1);
        testBackoff(2);
        // testBackoff(3);
        testBackoff(1);
        testBackoff(1);
        testBackoff(1);
        // testBackoff(1);
        // testBackoff(1);
        // testBackoff(1);
    }
}
