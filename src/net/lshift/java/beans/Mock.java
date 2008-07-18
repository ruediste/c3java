package net.lshift.java.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import net.lshift.java.lang.Types;

/**
 * Implement a 'bean' for a given interface by storing the
 * properties in a map, or Store.
 */
public class Mock
{
    // TODO: add support for add and remove listeners
    // TODO: add support for property change events
    // TODO: add support for indexed and mapped properties

   
    /**
     * Generic supporting for storing of bean properties.
     * You may limit the set of properties supported by throwing
     * UnsupportedOperationException, or in the case of set, simply
     * ignoring the value.
     */
    public interface Store
    {
        public Object getProperty(String name);
        public void setProperty(String name, Object value);
    }
    
    public interface Factory<T>
    extends Serializable
    {
        public T bean(final Store store);
        public T bean(Map<String,Object> map);
        public T bean();
    }
    
    public interface BeanInvocationHandler
    extends Serializable
    {
        public Object invoke(Store store, Method method, Object[] args)
            throws Throwable;
    }
    
    private static Map<Class<?>,Map<Method,BeanInvocationHandler>> cache = 
        new WeakHashMap<Class<?>, Map<Method,BeanInvocationHandler>>();
    
    private static final Method [] STORE_DELEGATE_METHODS = Object.class.getMethods();

    private static final BeanInvocationHandler STORE_DELEGATE =
        new BeanInvocationHandler() {

            private static final long serialVersionUID = 1L;

            public Object invoke(Store store, Method method, Object[] args)
                throws Throwable
            {
                try {                   
                    return method.invoke(store, args);
                }
                catch(InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        
    };
    
    private static void addMethods(Map <Method,BeanInvocationHandler> methods, Class<?> bean) 
        throws IntrospectionException
    {
        // TODO: this should really map from a method to an invocation handler,
        // so we can handle methods other than getters and setters
        
        if(!bean.isInterface())
            throw new IllegalArgumentException("Not an interface");
        
        BeanInfo info = Introspector.getBeanInfo(bean);
        for(PropertyDescriptor pinfo: info.getPropertyDescriptors()) {
            final String name = pinfo.getName();
            if(pinfo.getReadMethod() != null)
                if(pinfo.getPropertyType().isPrimitive()) {
                    final Object defaultValue = Types.DEFAULT_VALUES.get(pinfo.getPropertyType());
                    methods.put(pinfo.getReadMethod(), new BeanInvocationHandler() {

                        private static final long serialVersionUID = 1L;

                        public Object invoke(Store store, Method method, Object[] args)
                        throws Throwable
                        {
                            Object result = store.getProperty(name);
                            return result == null ? defaultValue : result;
                        }
                    });
                }
                else {
                    methods.put(pinfo.getReadMethod(), new BeanInvocationHandler() {

                        private static final long serialVersionUID = 1L;

                        public Object invoke(Store store, Method method, Object[] args)
                        throws Throwable
                        {
                            return store.getProperty(name);
                        }
                    });
                }
            
            if(pinfo.getWriteMethod() != null)
                methods.put(pinfo.getWriteMethod(), new BeanInvocationHandler() {

                    private static final long serialVersionUID = 1L;

                    public Object invoke(Store store, Method method, Object[] args)
                        throws Throwable
                    {
                        store.setProperty(name, args[0]);
                        return null;
                    }
                    
                });
        }

        for(Method method: bean.getDeclaredMethods())
            if(!methods.containsKey(method))
                throw new IllegalArgumentException(
                   "Method " + method.getName() + " on " + bean.getName()
                   + " is not a getter or setter");

        for(Class<?> superinterface: bean.getInterfaces())
            addMethods(methods, superinterface);
    }
    
    private static synchronized Map<Method,BeanInvocationHandler> methods(
        final Class<?> bean) 
        throws IntrospectionException
    {
        Map<Method,BeanInvocationHandler> methods = cache.get(bean);
        if(methods == null) {
            methods = new HashMap<Method,BeanInvocationHandler>();
            addMethods(methods, bean);
            for(Method method: STORE_DELEGATE_METHODS)
                methods.put(method, STORE_DELEGATE);
            cache.put(bean, methods);
        }
        
        return methods;
    }
    

    /**
     * Return a bean implementation using a property store
     * @param <T> the beans type.
     * @param iface the interface to implement. Introspection on interfaces
     *   works but does not look at super interfaces. We traverse the super
     *   interface tree, introspecting on all the interfaces.
     * @param store handles storing properties
     * @return
     * @throws IntrospectionException
     */
    public static <T> T bean(Class<T> iface, final Store store) 
        throws IntrospectionException
    {
        return factory(iface).bean(store);
    }

    public interface SerializableInvocationHandler
    extends InvocationHandler, Serializable { }
    
    public static class FactoryImpl<T>
    implements Factory<T>, Serializable
    {
        private static final long serialVersionUID = 1L;

        private transient Class<T> interfaceClass;
        private transient Map<Method,BeanInvocationHandler> methods;
        private String interfaceName;
        
        public FactoryImpl(Class<T> iface) 
            throws IntrospectionException
        {
            this.interfaceClass = iface;
            this.interfaceName = iface.getName();
            this.methods = methods(iface);
        }

        @SuppressWarnings("unchecked")
        public T bean(final Store store)
        {
            T instance = (T)Proxy.newProxyInstance(
                interfaceClass.getClassLoader(), 
                new Class [] { interfaceClass }, 
                new SerializableInvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable
                    {
                        final BeanInvocationHandler handler = methods.get(method);
                        return handler.invoke(store, method, args);
                    }
                                           
                });

            return instance;
        }

        public T bean(Map<String, Object> map)
        {
            return bean(store(map));
        }

        public T bean()
        {
            return bean(store(new HashMap<String, Object>()));
        }
        
        @SuppressWarnings("unchecked")
        private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException
        {
            in.defaultReadObject();
            interfaceClass = (Class<T>) Class.forName(interfaceName);
            try {
                methods = methods(interfaceClass);
            }
            catch (IntrospectionException e) {
                throw new IOException(e);
            }
        }
    }
    
    public static <T> Factory<T> factory(Class<T> iface)
    throws IntrospectionException
    {
        return new FactoryImpl<T>(iface);
    }
    
    public static class MapStore
    implements Store, Serializable
    {
        private static final long serialVersionUID = 1L;

        Map<String,Object> map;
        
        public MapStore(Map<String,Object> map)
        {
            this.map = map;
        }
        
        public Object getProperty(String name)
        {
            return map.get(name);
        }

        public void setProperty(String name, Object value)
        {
            map.put(name, value);
        }
        
        public String toString()
        {
            return map.toString();
        }
        
        public boolean equals(Object o)
        {
            if(o.getClass() == this.getClass()) {
                MapStore other = (MapStore)o;
                return this.map.equals(other.map);
            }
            else {
                return false;
            }
        }
        
        public int hashCode()
        {
            return map.hashCode();
        }

    }
    
    public static Store store(final Map<String,Object> map)
    {
        return new MapStore(map);
    }
    
    public static <T> T bean(Class<T> iface, Map<String,Object> map) 
    throws IntrospectionException
    {
        return bean(iface, store(map));
    }
    
    public static <T> T bean(Class<T> iface) 
    throws IntrospectionException
    {
        return bean(iface, store(new HashMap<String, Object>()));
    }
}
