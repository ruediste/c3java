package net.lshift.java.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteIterator<T>
    extends Remote
{
    public boolean hasNext()
        throws RemoteException;

    public T next()
        throws RemoteException;

    public void remove()
        throws RemoteException;

}
