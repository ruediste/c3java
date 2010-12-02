package net.lshift.java.util;

/**
 * Access static methods for creating and manipulating tuples.
 * @author david
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class Tuples
    // This will extend the largest tuple class I implement, 4-tuple for now
    extends FiveTuple
{
    private static final long serialVersionUID = 1L;

    private Tuples(Object first, Object second, Object third, Object fourth, Object fifth) {
        super(first, second, third, fourth, fifth);
    }

}
