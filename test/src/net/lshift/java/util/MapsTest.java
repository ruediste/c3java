package net.lshift.java.util;

import static net.lshift.java.util.Maps.entry;
import static net.lshift.java.util.Maps.map;

import java.util.Map;

import junit.framework.TestCase;

public class MapsTest
    extends TestCase
{
    public class A {}
    public class B extends A {}

    /**
     * This test is aimed at showing java will calculate the types are
     * compatible when the value types aren't homogeneous. It succeeds
     * just by compiling
     */
    public void testUnhomogenousEntries()
    {
        // This test is aimed at showing java will calculate the types are
        // compatible when the value types aren't homogeneous
        @SuppressWarnings({ "unused", "unchecked" })
        Map<String, A> map = map(entry("A", new A()), entry("B", new B()));
    }

    public void testEntry()
    {

    }
}
