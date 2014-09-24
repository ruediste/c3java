package net.lshift.java.util;

import junit.framework.TestCase;

public class LiteralTest
    extends TestCase
{
    public void testStringArray()
    {
        System.out.println(Literal.literal(new String [] { "a", "b", "c"}));
    }

    public void testIntArray()
    {
        System.out.println(Literal.literal(new int [] { 1, 2, 3 }));
        System.out.println(Literal.literal(new Integer [] { 1, 2, 3 }));
        System.out.println(Literal.literal(new short [] { 1, 2, 3 }));
    }

    public void test2D()
    {
        System.out.println(Literal.literal(new int [][] { new int [] {1, 2, 3 }}));
    }
}
