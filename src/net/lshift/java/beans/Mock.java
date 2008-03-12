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
        public Object get(String name);
        public void set(String name, Object value);
    }
    
    private static Map<Class<?>,Map<Method,String>> cache = new WeakHashMap<Class<?>, Map<Method,String>>();
    
    private static void addMethods(Map <Method,String> methods, Class<?> bean) 
        throws IntrospectionException
    {
        // TODO: this should really map from a method to an invocation handler,
        // so we can handle methods other than getters and setters
        
        if(!bean.isInterface())
            throw new IllegalArgumentException("Not an interface");
        
        BeanInfo info = Introspector.getBeanInfo(bean);
        for(PropertyDescriptor pinfo: info.getPropertyDescriptors()) {
            if(pinfo.getReadMethod() != null)
                methods.put(pinfo.getReadMethod(), pinfo.getName());
            if(pinfo.getWriteMethod() != null)
                methods.put(pinfo.getWriteMethod(), pinfo.getName());
        }

        for(Method method: bean.getMethods())
            if(!methods.containsKey(method))
                throw new IllegalArgumentException(
                                "Method " + method.getName() + " on " + bean.getName()
                                + " is not a getter or setter");

        for(Class<?> superinterface: bean.getInterfaces())
            addMethods(methods, superinterface);
    }
    
    private static synchronized Map<Method,String> methods(Class<?> bean) 
        throws IntrospectionException
    {
        Map<Method,String> methods = cache.get(bean);
        if(methods == null) {
            methods = new HashMap<Method,String>();
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
        final Map<Method,String> methods = methods(iface);
        
        return (T)Proxy.newProxyInstance(
            iface.getClassLoader(), 
            new Class [] { iface }, 
            new InvocationHandler() {

                public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable
                {
                    String name = methods.get(method);
                    switch(args == null ? 0 : args.length) {
                    case 0:
                        return store.get(name);
                    case 1:
                        store.set(name, args[0]);
                        return null;
                    default:
                        throw new UnsupportedOperationException(
                            "Unexpected number of arguments for " 
                            + method.getName() + ": " + args.length);
                    }
                }
                            
            });
    }
    
    public static Store store(final Map<String,Object> map)
    {
        return new Store() {

            public Object get(String name)
            {
                return map.get(name);
            }

            public void set(String name, Object value)
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
