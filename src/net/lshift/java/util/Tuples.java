package net.lshift.java.util;

/**
 * Access static methods for creating and manipulating tuples.
 * @author david
 *
 */
@SuppressWarnings("unchecked")
public abstract class Tuples
    // This will extend the largest tuple class I implement, 4-tuple for now
    extends FourTuple
{
    private static final long serialVersionUID = 1L;

    private Tuples(Object first, Object second, Object third, Object fourth) {
        super(first, second, third, fourth);
    }

}
