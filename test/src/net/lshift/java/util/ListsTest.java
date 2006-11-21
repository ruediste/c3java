package net.lshift.java.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lshift.java.util.Lists.FoldProcedure;
import net.lshift.java.util.Lists.FoldState;

import junit.framework.TestCase;

public class ListsTest
    extends TestCase
{
    static FoldProcedure<Integer, List<Integer>> INSERT_FIRST = 
        new FoldProcedure<Integer, List<Integer>>() {

        public FoldState<List<Integer>> 
            apply(Integer item, List<Integer> accumulator)
        {
            accumulator.add(0, item);
            System.out.println(accumulator);
            return new FoldState<List<Integer>>(true, accumulator);
        }
    };
    
    FoldProcedure<Integer, Integer> SUM = new FoldProcedure<Integer, Integer>() {

        public FoldState<Integer> apply(Integer item, Integer accumulator)
        {
            return new FoldState<Integer>(true, item + accumulator);
        }
    };
    
    public void testFoldLeft()
    {
        List<Integer> l = Lists.list(1, 2, 3);
        assertEquals(new Integer(6), Lists.foldLeft(SUM, 0, l));
        assertEquals
            (Lists.reverse(l), 
             Lists.foldLeft(INSERT_FIRST , Lists.list(new Integer[0]), l));
    }
    
    public void testFoldRight()
    {
        List<Integer> l = Lists.list(1, 2, 3);
        assertEquals(new Integer(6), Lists.foldRight(SUM, 0, l));
        assertEquals
            (l, Lists.foldRight(INSERT_FIRST , Lists.list(new Integer[0]), l));
    }
    
    @SuppressWarnings("unchecked")
    public void testConcatenate()
    {
        assertEquals(Lists.list(1, 2, 3), 
                     Lists.concatenate(Lists.list(1),
                                       Lists.list(2),
                                       Lists.list(3)));
    }
    
    public void testFind()
    {
        Integer x = 2;
        assertTrue(x == Lists.find(Procedures.equal(2), Lists.list(1, x, 2, 3)));
        assertTrue(x == Lists.find(Procedures.equal(2), Lists.list(x, 1, 2, 3)));
        assertTrue(x == Lists.find(Procedures.equal(2), Lists.list(1, 3, x)));
        assertNull(Lists.find(Procedures.equal(4), Lists.list(1, 2, 3)));
    }
    
    public void testFindLast()
    {
        Integer x = 2;
        assertTrue(x == Lists.findLast(Procedures.equal(2), Lists.list(1, 2, x, 3)));
        assertTrue(x == Lists.findLast(Procedures.equal(2), Lists.list(1, 2, 3, x)));
        assertTrue(x == Lists.findLast(Procedures.equal(2), Lists.list(x, 1, 3)));
        assertNull(Lists.findLast(Procedures.equal(4), Lists.list(1, 2, 3)));
    }
    
    public void testAnyLast()
    {
        Map<Integer,Object> m = new HashMap<Integer,Object>();
        Integer x = 2;
        Object xvalue = new Object();
        m.put(x, xvalue);
        assertTrue(xvalue == Lists.anyLast(Procedures.get(m), Lists.list(1, 2, x, 3)));
        assertTrue(xvalue == Lists.anyLast(Procedures.get(m), Lists.list(1, 2, 3, x)));
        assertTrue(xvalue == Lists.anyLast(Procedures.get(m), Lists.list(x, 1, 3)));
    }
    
    public void testHead()
    {
        assertEquals(new Integer(1), Lists.head(Lists.list(1, 2, 3)));
    }
   
    public void testTail()
    {
        assertEquals(Lists.list(2, 3), Lists.tail(Lists.list(1, 2, 3)));
    }
}
