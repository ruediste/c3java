package net.lshift.java.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

public interface RemoteCollection
    extends Remote
{
    public int size()
        throws RemoteException;
    
    public boolean isEmpty()
        throws RemoteException;
    
    public boolean contains(Object o)
        throws RemoteException;

    public RemoteIterator iterator()
        throws RemoteException;

    public Object[] toArray()
        throws RemoteException;

    public boolean add(Object o)
        throws RemoteException;
    
    public boolean remove(Object o)
        throws RemoteException;

    public boolean containsAll(Collection c)
        throws RemoteException;

    public boolean addAll(Collection c)
        throws RemoteException;

    public boolean removeAll(Collection c)
        throws RemoteException;

    public boolean retainAll(Collection c)
        throws RemoteException;

    public void clear()
        throws RemoteException;


}
