package net.lshift.java.rmi;

import java.rmi.RemoteException;
import java.util.Iterator;

public class RemoteIteratorAdaptor<E>
    implements Iterator<E>
{
    private final RemoteIterator<E> iterator;

    public RemoteIteratorAdaptor(RemoteIterator<E> iterator)
    {
        this.iterator = iterator;
    }

    public boolean hasNext()
    {
        try {
            return iterator.hasNext();
        }
        catch (RemoteException e) {
            throw new RemoteExceptionWrapper(e);
        }
    }

    public E next()
    {
        try {
            return iterator.next();
        }
        catch (RemoteException e) {
            throw new RemoteExceptionWrapper(e);
        }
    }

    public void remove()
    {
        try {
            iterator.remove();
        }
        catch (RemoteException e) {
            throw new RemoteExceptionWrapper(e);
        }
    }


}
