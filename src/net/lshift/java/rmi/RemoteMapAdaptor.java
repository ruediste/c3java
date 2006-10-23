package net.lshift.java.rmi;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class RemoteMapAdaptor
    implements Map
{
    private final RemoteMap map;
    
    // These _could_ be weak references, but it doesn't seem very important
    private Set keySet = null;
    private Set entrySet = null;
    private Collection values = null;

    public RemoteMapAdaptor(RemoteMap map)
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

    public Set entrySet()
    {
        if(entrySet == null) {
            try {
                entrySet = new RemoteSetAdaptor(map.entrySet());
            }
            catch(RemoteException e) {
                throw  new RemoteExceptionWrapper(e);
            }
        }
        
        return entrySet;
    }

    public Object get(Object key)
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

    public Set keySet()
    {
        if(keySet == null) {
            try {
                keySet = new RemoteSetAdaptor(map.keySet());
            }
            catch(RemoteException e) {
                throw  new RemoteExceptionWrapper(e);
            }
        }
        
        return keySet;
    }

    public Object put(Object key, Object value)
    {
        try {
            return map.put(key, value);
        }
        catch(RemoteException e) {
            throw  new RemoteExceptionWrapper(e);
        }
    }

    public void putAll(Map t)
    {
        try {
            map.putAll(t);
        }
        catch(RemoteException e) {
            throw  new RemoteExceptionWrapper(e);
        }
    }

    public Object remove(Object key)
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

    public Collection values()
    {
        if(values == null) {
            try {
                values =  new RemoteCollectionAdaptor(map.values());
            }
            catch(RemoteException e) {
                throw  new RemoteExceptionWrapper(e);
            }
        }
        
        return values;
    }

}
