package net.lshift.java.rmi.server;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.util.Collection;

import net.lshift.java.rmi.RemoteCollection;
import net.lshift.java.rmi.RemoteIterator;

public class RemoteCollectionServer
    implements RemoteCollection, Unreferenced
{
    private static final long serialVersionUID = 1L;

    private final Collection collection;
    
    public RemoteCollectionServer(Collection collection)
        throws RemoteException
    {
        this.collection = collection;
    }

    public boolean add(Object o)
    {
        return collection.add(o);
    }

    public boolean addAll(Collection c)
    {
        return collection.addAll(c);
    }

    public void clear()
    {
        collection.clear();
    }

    public boolean contains(Object o)
    {
        return collection.contains(o);
    }

    public boolean containsAll(Collection c)
    {
        return collection.containsAll(c);
    }

    public boolean equals(Object o)
    {
        return collection.equals(o);
    }

    public int hashCode()
    {
        return collection.hashCode();
    }

    public boolean isEmpty()
    {
        return collection.isEmpty();
    }

    public RemoteIterator iterator()
        throws RemoteException
    {
        return new RemoteIteratorServer(collection.iterator());
    }

    public boolean remove(Object o)
    {
        return collection.remove(o);
    }

    public boolean removeAll(Collection c)
    {
        return collection.removeAll(c);
    }

    public boolean retainAll(Collection c)
    {
        return collection.retainAll(c);
    }

    public int size()
    {
        return collection.size();
    }

    public Object[] toArray()
    {
        return collection.toArray();
    }

    public Object[] toArray(Object[] a)
    {
        return collection.toArray(a);
    }

    public void unreferenced()
    {
        try {
            UnicastRemoteObject.unexportObject(this, true);
        }
        catch (NoSuchObjectException e) { }
    }
}
