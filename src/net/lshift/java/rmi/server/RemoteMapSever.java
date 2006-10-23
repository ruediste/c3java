package net.lshift.java.rmi.server;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.util.Map;

import net.lshift.java.rmi.RemoteCollection;
import net.lshift.java.rmi.RemoteMap;

public class RemoteMapSever
    implements RemoteMap, Unreferenced
{
    private final Map map;

    public RemoteMapSever(Map map)
    {
        this.map = map;
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

    public RemoteCollection entrySet()
        throws RemoteException
    {
        return new RemoteCollectionServer(map.entrySet());
    }

    public boolean equals(Object o)
    {
        return map.equals(o);
    }

    public Object get(Object key)
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

    public RemoteCollection keySet() 
        throws RemoteException
    {
        return new RemoteCollectionServer(map.keySet());
    }

    public Object put(Object key, Object value)
    {
        return map.put(key, value);
    }

    public void putAll(Map t)
    {
        map.putAll(t);
    }

    public Object remove(Object key)
    {
        return map.remove(key);
    }

    public int size()
    {
        return map.size();
    }

    public RemoteCollection values() 
        throws RemoteException
    {
        return new RemoteCollectionServer(map.values());
    }

    public void unreferenced()
    {
        try {
            UnicastRemoteObject.unexportObject(this, true);
        }
        catch (NoSuchObjectException e) { }
        
    }
}
