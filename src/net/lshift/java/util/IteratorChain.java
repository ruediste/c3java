
package net.lshift.java.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Concatenate the members of several iterators into one.
 *
 * Commons Collections would seem to have this covered by
 * its IteratorChain, or where you require laziness ObjectGraphIterator.
 * ObjectGraphIterator seems to get remove() wrong and IteratorChain
 * isn't lazy.
 *
 * This class covers the useful functionality of both classes (because this
 * class can be composed to do the same thing as ObjectGraphIterator
 * anyway). Its implemeted in about 40 lines, while these other two
 * classes are 450 lines combined.
 *
 * @author david
 *
 */
public class IteratorChain<E>
    implements Iterator<E>
{
    private final Iterator<Iterator<E>> iterators;
    private Iterator<E> used = null;
    private Iterator<E> current = null;

    public IteratorChain(Iterator<Iterator<E>> iterators)
    {
        this.iterators = iterators;
    }

    public IteratorChain(Iterator<E> [] iterators)
    {
        this(Arrays.asList(iterators).iterator());
    }

    public boolean hasNext()
    {
        while((current == null || !current.hasNext()) && iterators.hasNext())
            current = iterators.next();
        return (current == null) ? false : current.hasNext();
    }

    public E next()
    {
        if(!hasNext())
            throw new NoSuchElementException();
        used = current;
        return current.next();
    }

    public void remove()
    {
        if(used == null)
            throw new IllegalStateException();
        used.remove();
    }
}
