package net.lshift.java.lang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ResourceTree
{
    static class Client { }
    
    public interface ResourceConnectorFactory
    {
        public <RK, R> ResourceConnector<RK, R> connector(Class<RK> keyClass);
    }
    
    public interface ResourceConnector<RK, R>
    {
        public R connect(RK key);
        public void disconnect(R resource);
    }
    
    private ResourceConnectorFactory connectors;
    
    /**
     * Monitor a resource of type R, with children of type C.
     * @param <R>
     * @param <CK> the key that identifies the child resources
     * @param <CM> the monitor type for the child resources
     */
    private class Monitor<R, RK, CK, CM extends Monitor<?,CK,?,?>> 
    {
        final RK key;
        R resource;
        Set<Client> clients = new HashSet<Client>();
        Map<CK, CM> children = new HashMap<CK, CM>();
        
        public Monitor(RK key)
        {
            this.key = key;
        }
        
        public void connect(Client client)
        {
            if(clients.isEmpty()) {
                resource = connector().connect(key);
            }
            
            clients.add(client);
        }

        public void disconnect(Client client)
        {
            if(!clients.remove(client))
                throw new IllegalStateException(client + "is not currently connected to this resource");
            if(clients.isEmpty()) {
               connector().disconnect(resource);
               resource = null;
            }
        }

        public CM monitor(CK key)
        {
            if(children.containsKey(key)) {
                return children.get(key);
            }
            else {
                CM monitor = (CM) new Monitor(key);
                children.put(key, monitor);
                return monitor;
            }
        }
        
        @SuppressWarnings("unchecked")
        private  ResourceConnector<RK, R> connector()
        {
            return connectors.<RK,R>connector((Class<RK>)key.getClass());
        }
        
    }
}
