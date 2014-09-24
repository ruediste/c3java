package net.lshift.java.util;

import java.util.Iterator;

import net.lshift.java.util.Transform;

/**
 * Provides an iterator that transforms objects from one type to another only as next() is called
 * @author james
 *
 * @param <I> The parameter for the original iterator
 * @param <O> The parameter for the output iterator
 */
public class LazyTransformIterator<I, O> implements Iterator<O> {

    Iterator<I> it;
    Transform<I, O> trans;

    public LazyTransformIterator(Iterator<I> it, Transform<I, O> trans)
    {
        this.it = it;
        this.trans = trans;
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public O next() {
        return trans.apply(it.next());
    }

    @Override
    public void remove() {
        it.remove();
    }


}
