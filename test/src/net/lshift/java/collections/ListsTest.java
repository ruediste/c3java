package net.lshift.java.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lshift.java.collections.Lists.FoldState;

public class ListsTest
    extends TestCase
{

    public static TestSuite suite()
    {
        return new TestSuite(ListsTest.class);
    }

    public void testFoldLeft()
    {
	assertTrue(Lists.equal(Pair.<Character>list('a', 'b', 'c'),
		     Lists.foldLeft(new Pair.Cons<Character>(), null,
				    Pair.<Character>list('c', 'b', 'a'))));
    }

    public void testFoldRight()
    {
	// -2 == 1 - (5 - 2)
	Integer minusTwo = Lists.foldRight(
		new Lists.FoldProcedure<Integer, Integer>() {

		    public FoldState<Integer> apply(Integer item,
			    Integer accumulator)
		    {
			return new FoldState<Integer>(true, item.intValue()
				- accumulator.intValue());
		    }
		}, 2, Pair.list(1, 5));
	assertEquals(minusTwo, new Integer(-2));

	Collection<Character> hello = Lists.foldRight(
		new Pair.Cons<Character>(), Pair.cons('o', null), Arrays
			.asList('h', 'e', 'l', 'l'));
	assertTrue(Lists.equal(Arrays.asList('h', 'e', 'l', 'l', 'o'), hello));
    }
    
    public void testMap() {
	Transform<Integer, Integer> plusOne = new Transform<Integer, Integer>() {

	    public Integer apply(Integer x) {
		return 8 - x.intValue();
	    }
	};
	
	List<Integer> input = Arrays.asList(2,3,4,5,6);
	Collection<Integer> output = Lists.map(plusOne, input);
	Collections.reverse(input);
	assertTrue(Lists.equal(input, output));
    }
    
    public void testZip()
    {
	List<Integer> row0 = Arrays.asList(3, 2, 1);
	List<Integer> row1 = Arrays.asList(10, 8, 6, 4, 2);
	List<Integer> row2 = Arrays.asList(21, 18, 15, 12, 9, 6, 3);
	List<Collection<Integer>> zipped = new ArrayList<Collection<Integer>>(
		Lists.zip(row0, row1, row2));
	assertEquals(3, zipped.size());
	Collection<Integer> col0 = zipped.get(0);
	assertTrue(Arrays.deepEquals(new Integer[] { 3, 10, 21 }, col0
		.toArray(new Integer[col0.size()])));
	Collection<Integer> col1 = zipped.get(1);
	assertTrue(Arrays.deepEquals(new Integer[] { 2, 8, 18 }, col1
		.toArray(new Integer[col1.size()])));
	Collection<Integer> col2 = zipped.get(2);
	assertTrue(Arrays.deepEquals(new Integer[] { 1, 6, 15 }, col2
		.toArray(new Integer[col2.size()])));
    }
    
    public void testEquals()
    {
	List<Integer> row0 = Arrays.asList(1,2,3);
	List<Integer> row1 = Arrays.asList(1,2,3,4,5);
	List<Integer> row2 = Arrays.asList(1,2,3,4,5);
	assertFalse(Lists.equal(row0, row1));
	assertTrue(Lists.equal(row0, row0));
	assertTrue(Lists.equal(row1, row2));
	assertTrue(Lists.equal(row2, row1));
    }
}