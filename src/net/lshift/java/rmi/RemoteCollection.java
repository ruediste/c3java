package net.lshift.java.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

public interface RemoteCollection<E>
    extends Remote
{
    public int size()
        throws RemoteException;
    
    public boolean isEmpty()
        throws RemoteException;
    
    public boolean contains(Object o)
        throws RemoteException;

    public RemoteIterator<E> iterator()
        throws RemoteException;

    public Object[] toArray()
        throws RemoteException;

    public boolean add(E o)
        throws RemoteException;
    
    public boolean remove(Object o)
        throws RemoteException;

    public boolean containsAll(Collection<?> c)
        throws RemoteException;

    public boolean addAll(Collection<? extends E> c)
        throws RemoteException;

    public boolean removeAll(Collection<?> c)
        throws RemoteException;

    public boolean retainAll(Collection<?> c)
        throws RemoteException;

    public void clear()
        throws RemoteException;


}
