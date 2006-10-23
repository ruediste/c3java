package net.lshift.java.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface RemoteMap
    extends Remote
{
    public void clear()
        throws RemoteException;

    public boolean containsKey(Object key)
        throws RemoteException;

    public boolean containsValue(Object value)
        throws RemoteException;

    public RemoteCollection entrySet()
        throws RemoteException;

    public Object get(Object key)
        throws RemoteException;

    public boolean isEmpty()
        throws RemoteException;

    public RemoteCollection keySet()
        throws RemoteException;

    public Object put(Object key, Object value)
        throws RemoteException;

    public void putAll(Map t)
        throws RemoteException;

    public Object remove(Object key)
        throws RemoteException;

    public int size()
        throws RemoteException;

    public RemoteCollection values()
        throws RemoteException;
}
