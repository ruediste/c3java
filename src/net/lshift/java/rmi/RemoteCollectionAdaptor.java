package net.lshift.java.rmi;

import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

public class RemoteCollectionAdaptor<E>
    implements Collection<E>
{
    private final RemoteCollection<E> collection;

    public RemoteCollectionAdaptor(RemoteCollection<E> collection)
    {
        this.collection = collection;
    }

    public boolean add(E o)
    {
        try {
            return collection.add(o);
        }
        catch (RemoteException e) {
            throw new RemoteExceptionWrapper(e);
        }
    }

    public boolean addAll(Collection<? extends E> c)
    {
        try {
            return collection.addAll(c);
        }
        catch (RemoteException e) {
            throw new RemoteExceptionWrapper(e);
        }
    }

    public void clear()
    {
        try {
            collection.clear();
        }
        catch (RemoteException e) {
            throw new RemoteExceptionWrapper(e);
        }
    }

    public boolean contains(Object o)
    {
        try {
            return collection.contains(o);
        }
        catch (RemoteException e) {
            throw new RemoteExceptionWrapper(e);
        }
    }

    public boolean containsAll(Collection c)
    {
        try {
            return collection.containsAll(c);
        }
        catch (RemoteException e) {
            throw new RemoteExceptionWrapper(e);
        }
    }

    public boolean isEmpty()
    {
        try {
            return collection.isEmpty();
        }
        catch (RemoteException e) {
            throw new RemoteExceptionWrapper(e);
        }
    }

    public Iterator<E> iterator()
    {
        try {
            return new RemoteIteratorAdaptor<E>(collection.iterator());
        }
        catch (RemoteException e) {
            throw new RemoteExceptionWrapper(e);
        }
    }

    public boolean remove(Object o)
    {
        try {
            return collection.remove(o);
        }
        catch (RemoteException e) {
            throw new RemoteExceptionWrapper(e);
        }
    }

    public boolean removeAll(Collection c)
    {
        try {
            return collection.removeAll(c);
        }
        catch (RemoteException e) {
            throw new RemoteExceptionWrapper(e);
        }
    }

    public boolean retainAll(Collection c)
    {
        try {
            return collection.retainAll(c);
        }
        catch (RemoteException e) {
            throw new RemoteExceptionWrapper(e);
        }
    }

    public int size()
    {
        try {
            return collection.size();
        }
        catch (RemoteException e) {
            throw new RemoteExceptionWrapper(e);
        }
    }

    public Object[] toArray()
    {
        try {
            return collection.toArray();
        }
        catch (RemoteException e) {
            throw new RemoteExceptionWrapper(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T [] array)
    {
        Object [] untyped = toArray();
        if(untyped.length > array.length)
            array = (T [])Array.newInstance
                (array.getClass().getComponentType(), untyped.length);
        System.arraycopy(untyped, 0, array, 0, untyped.length);
        return array;
    }
}
