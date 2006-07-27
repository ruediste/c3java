package net.lshift.java.rmi;

import java.rmi.RemoteException;
import java.util.Iterator;

public class RemoteIteratorAdaptor
    implements Iterator
{
    private final RemoteIterator iterator;
    
    public RemoteIteratorAdaptor(RemoteIterator iterator)
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

    public Object next()
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
