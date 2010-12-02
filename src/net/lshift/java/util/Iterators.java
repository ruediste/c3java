package net.lshift.java.util;

import static net.lshift.java.util.Iterators.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Useful operations on iterators.
 * A lot of functions in List actually work on an Iterable, but
 * generally return a list.
 * @see Lists
 * @author david
 *
 */
public class Iterators
{
    /*
     * The following are included here despite having equivalents
     * in jakarta commons collections because they are trivial,
     * support generics, and our procedures.
     */
    
    /**
     * Transform an iterator.
     * Each call to next() will invokes transform.apply(iterator.next()).
     * The other methods simply delegate to iterator.
     * @param <E>
     * @param <F>
     * @param iterator
     * @param transform
     * @return
     */
    public static <E,F> Iterator<F> transform
        (final Iterator<E> iterator, 
         final Transform<E,F> transform)
    {
        /*
        * This avoids the question of lazyness - There is no natural
        * way to re-read a value, so I don't have to decide if I want to
        * keep the results of the transform.
        */
        return new Iterator<F>() {

            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            public F next()
            {
                return transform.apply(iterator.next());
            }

            public void remove()
            {
                iterator.remove();
            }
            
        };
    }

    public static <E,F> Iterable<F> transform
        (final Iterable<E> iterable, 
         final Transform<E,F> transform)
    {
        return new Iterable<F>() {
            public Iterator<F> iterator() {
                return transform(iterable.iterator(), transform);
            }
        };
    }
    
    /**
     * Return an iterator which will return at most count items
     * @param <E>
     * @param iterator
     * @param count 
     * @return
     */
    public static <E> Iterator<E> limit(final Iterator<E> iterator, final int count)
    {
        return new Iterator<E>() {
            
            int returned = 0;
            
            public boolean hasNext()
            {
                return (returned < count) && iterator.hasNext();
            }

            public E next()
            {
                if(returned >= count)
                    throw new NoSuchElementException();
                
                returned++;
                
                return iterator.next();
            }

            public void remove()
            {
                iterator.remove();
            }

        };
    }
    
    public static <E> Iterable<E> limit(final Iterable<E> iterable, final int count)
    {
        return new Iterable<E>() {
            public Iterator<E> iterator() {
                return limit(iterable.iterator(), count);
            }
        };
    }
    
    /**
     * Zip lists together.
     * This returns an single iterator, which in turn returns a list by 
     * adding one item from each iterator, until one of the input iterators is empty.
     * @param <E>
     * @param l
     * @return
     */
    public static <E> Iterator<Iterator<E>> zip(final List<Iterator<E>> iterators)
    {
        return new Iterator<Iterator<E>> () {
            public boolean hasNext() {
                return Lists.all(Collections.Procedures.<E>hasNext(), iterators);
            }

            public Iterator<E> next() {
                return transform(iterators.iterator(), Collections.Procedures.<E>next());
            }

            public void remove() {
                Lists.forEach(Collections.Procedures.remove(), iterators);
            }
            
        };
    }
    
    public static <E, F> Iterator<E> zip(
        List<Iterable<F>> l, 
        Transform<Iterator<F>, E> factory)
    {
        return transform(
            Iterators.<F>zip(
                Lists.map(Collections.Procedures.<F>iterator(), l)), 
            factory);
    }
}
