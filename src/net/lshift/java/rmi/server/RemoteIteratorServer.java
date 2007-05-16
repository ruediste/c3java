package net.lshift.java.rmi.server;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.util.Iterator;

import net.lshift.java.rmi.RemoteIterator;

public class RemoteIteratorServer
    implements RemoteIterator, Unreferenced
{
    private static final long serialVersionUID = 1L;

    private final Iterator iterator;
    
    public RemoteIteratorServer(Iterator iterator)
        throws RemoteException
    {
        this.iterator = iterator;
    }

    public boolean hasNext()
    {
        return iterator.hasNext();
    }

    public Object next()
    {
        return iterator.next();
    }

    public void remove()
    {
        iterator.remove();
    }

    public void unreferenced()
    {
        try {
            UnicastRemoteObject.unexportObject(this, true);
        }
        catch (NoSuchObjectException e) { }
    }
    
}
