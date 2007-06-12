package net.lshift.java.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface RemoteMap<K,V>
    extends Remote
{
    public void clear()
        throws RemoteException;

    public boolean containsKey(Object key)
        throws RemoteException;

    public boolean containsValue(Object value)
        throws RemoteException;

    public RemoteCollection<Map.Entry<K, V>> entrySet()
        throws RemoteException;

    public V get(Object key)
        throws RemoteException;

    public boolean isEmpty()
        throws RemoteException;

    public RemoteCollection<K> keySet()
        throws RemoteException;

    public V put(K key, V value)
        throws RemoteException;

    public void putAll(Map<? extends K, ? extends V> t)
        throws RemoteException;

    public V remove(Object key)
        throws RemoteException;

    public int size()
        throws RemoteException;

    public RemoteCollection<V> values()
        throws RemoteException;
}
