package net.lshift.java.util;

/**
 * Access static methods for creating and manipulating tuples.
 * @author david
 *
 */
@SuppressWarnings("unchecked")
public abstract class Tuples
    // This will extend the largest tuple class I implement, 3-tuple for now
    extends ThreeTuple
{

    @SuppressWarnings("unchecked")
    private Tuples(Object first, Object second, Object third) {
        super(first, second, third);
    }

}
