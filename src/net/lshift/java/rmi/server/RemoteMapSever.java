package net.lshift.java.rmi.server;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.util.Map;

import net.lshift.java.rmi.RemoteCollection;
import net.lshift.java.rmi.RemoteMap;

public class RemoteMapSever<K,V>
    implements RemoteMap<K,V>, Unreferenced
{
    private final Map<K,V> map;
    private final StubFactory factory;

    public RemoteMapSever(Map<K,V> map)
    {
        this(RMIStubFactory.INSTANCE, map);
    }

    public RemoteMapSever(StubFactory factory, Map<K,V> map)
    {
        this.map = map;
        this.factory = factory;
    }

    public void clear()
    {
        map.clear();
    }

    public boolean containsKey(Object key)
    {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value)
    {
        return map.containsValue(value);
    }

    public RemoteCollection<Map.Entry<K, V>> entrySet()
        throws RemoteException
    {
        return factory.export(new RemoteCollectionServer<Map.Entry<K, V>>(factory, map.entrySet()));
    }

    public boolean equals(Object o)
    {
        return map.equals(o);
    }

    public V get(Object key)
    {
        return map.get(key);
    }

    public int hashCode()
    {
        return map.hashCode();
    }

    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    public RemoteCollection<K> keySet()
        throws RemoteException
    {
        return factory.export(new RemoteCollectionServer<K>(factory, map.keySet()));
    }

    public V put(K key, V value)
    {
        return map.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> t)
    {
        map.putAll(t);
    }

    public V remove(Object key)
    {
        return map.remove(key);
    }

    public int size()
    {
        return map.size();
    }

    public RemoteCollection<V> values()
        throws RemoteException
    {
        return factory.export(new RemoteCollectionServer<V>(factory, map.values()));
    }

    public void unreferenced()
    {
        try {
            UnicastRemoteObject.unexportObject(this, true);
        }
        catch (NoSuchObjectException e) { }

    }
}
