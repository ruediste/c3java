package net.lshift.java.rmi;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class RemoteMapAdaptor<K,V>
    implements Map<K,V>
{
    private final RemoteMap<K,V> map;

    // These _could_ be weak references, but it doesn't seem very important
    private Set<K> keySet = null;
    private Set<Entry<K,V>> entrySet = null;
    private Collection<V> values = null;

    public RemoteMapAdaptor(RemoteMap<K,V> map)
    {
        this.map = map;
    }

    public void clear()
    {
        try {
            map.clear();
        }
        catch(RemoteException e) {
            throw  new RemoteExceptionWrapper(e);
        }
    }

    public boolean containsKey(Object key)
    {
        try {
            return map.containsKey(key);
        }
        catch(RemoteException e) {
            throw  new RemoteExceptionWrapper(e);
        }
    }

    public boolean containsValue(Object value)
    {
        try {
            return map.containsValue(value);
        }
        catch(RemoteException e) {
            throw  new RemoteExceptionWrapper(e);
        }
    }

    public Set<Entry<K,V>> entrySet()
    {
        if(entrySet == null) {
            try {
                entrySet = new RemoteSetAdaptor<Entry<K,V>>(map.entrySet());
            }
            catch(RemoteException e) {
                throw  new RemoteExceptionWrapper(e);
            }
        }

        return entrySet;
    }

    public V get(Object key)
    {
        try {
            return map.get(key);
        }
        catch(RemoteException e) {
            throw  new RemoteExceptionWrapper(e);
        }
    }

    public boolean isEmpty()
    {
        try {
            return map.isEmpty();
        }
        catch(RemoteException e) {
            throw  new RemoteExceptionWrapper(e);
        }
    }

    public Set<K> keySet()
    {
        if(keySet == null) {
            try {
                keySet = new RemoteSetAdaptor<K>(map.keySet());
            }
            catch(RemoteException e) {
                throw  new RemoteExceptionWrapper(e);
            }
        }

        return keySet;
    }

    public V put(K key, V value)
    {
        try {
            return map.put(key, value);
        }
        catch(RemoteException e) {
            throw  new RemoteExceptionWrapper(e);
        }
    }

    public void putAll(Map<? extends K,? extends V> t)
    {
        try {
            map.putAll(t);
        }
        catch(RemoteException e) {
            throw  new RemoteExceptionWrapper(e);
        }
    }

    public V remove(Object key)
    {
        try {
            return map.remove(key);
        }
        catch(RemoteException e) {
            throw  new RemoteExceptionWrapper(e);
        }
    }

    public int size()
    {
        try {
            return map.size();
        }
        catch(RemoteException e) {
            throw  new RemoteExceptionWrapper(e);
        }

    }

    public Collection<V> values()
    {
        if(values == null) {
            try {
                values =  new RemoteCollectionAdaptor<V>(map.values());
            }
            catch(RemoteException e) {
                throw  new RemoteExceptionWrapper(e);
            }
        }

        return values;
    }

}
