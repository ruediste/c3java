package net.lshift.java.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

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
    
    public interface BeanInvocationHandler
    {
        public Object invoke(Store store, Object[] args)
            throws Throwable;
    }
    
    private static Map<Class<?>,Map<Method,BeanInvocationHandler>> cache = 
        new WeakHashMap<Class<?>, Map<Method,BeanInvocationHandler>>();
    
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
                methods.put(pinfo.getReadMethod(), new BeanInvocationHandler() {

                    public Object invoke(Store store, Object[] args)
                        throws Throwable
                    {
                        return store.getProperty(name);
                    }
                });
            
            if(pinfo.getWriteMethod() != null)
                methods.put(pinfo.getWriteMethod(), new BeanInvocationHandler() {

                    public Object invoke(Store store, Object[] args)
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
    
    private static synchronized Map<Method,BeanInvocationHandler> methods(Class<?> bean) 
        throws IntrospectionException
    {
        Map<Method,BeanInvocationHandler> methods = cache.get(bean);
        if(methods == null) {
            methods = new HashMap<Method,BeanInvocationHandler>();
            addMethods(methods, bean);
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
    @SuppressWarnings("unchecked")
    public static <T> T bean(Class<T> iface, final Store store) 
        throws IntrospectionException
    {
        final Map<Method,BeanInvocationHandler> methods = methods(iface);
        
        return (T)Proxy.newProxyInstance(
            iface.getClassLoader(), 
            new Class [] { iface }, 
            new InvocationHandler() {

                public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable
                {
                    BeanInvocationHandler handler = methods.get(method);
                    return handler.invoke(store, args);
                }
                            
            });
    }
    
    public static Store store(final Map<String,Object> map)
    {
        return new Store() {

            public Object getProperty(String name)
            {
                return map.get(name);
            }

            public void setProperty(String name, Object value)
            {
                map.put(name, value);
            }
            
        };
    }
    
    public static <T> T bean(Class<T> iface, Map<String,Object> map) 
    throws IntrospectionException
    {
        return bean(iface, store(map));
    }
}
