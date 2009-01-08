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
import net.lshift.java.util.Transform;

/**
 * Implement a 'bean' for a given interface by storing the
 * properties in a map, or Store. Unlike normal bean semantics,
 * the super-interfaces are traversed, and fields added from
 * them, so methods like describe, copy, and assign work
 * differently to this you find in commons BeanUtils, for example.
 */
public class Mock
{
    // TODO: add support for add and remove listeners
    // TODO: add support for property change events
    // TODO: add support for indexed and mapped properties

    public static class MockIntrospectionException
    extends IllegalArgumentException
    {
        private static final long serialVersionUID = 1L;

        public MockIntrospectionException(IntrospectionException e)
        {
            super(e);
        }
    }

    /**
     * Generic support for storing of bean properties.
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
        
        /**
         * Transform which applies copy.
         * @see #copy(Object)
         * @param <U>
         * @return
         */
        public <U extends T> Transform<U, T> copy();
        
        /**
         * Create a bean by copying a bean that implements the
         * interface this factory supports. Equivalent to bean(describe(other)).
         * @see #bean(Map)
         * @see #describe(Object)
         * @param <U> The type of the bean we reflect on. Essentially
         *   anonymous, but can't be because of the definition of Transform
         * @param other
         * @return
         */
        public <U extends T> T copy(U other);
        
        /**
         * Describe a bean. Only fields with getters in T
         * will be included in the description. The bean has already
         * been validated during the creation of the factory, so
         * this method throws no exceptions
         * @param <U> The type of the bean we reflect on. Essentially
         *   anonymous.
         * @param bean
         * @return
         */
        public <U extends T> Map<String,Object> describe(U bean);
        
        /**
         * Selectively assign one bean from another. The fields assigned
         * are those specified in the interface of this factory.
         * @param <U>
         * @param <V>
         * @param assignTo
         * @param assignFrom
         */
        public <U extends T, V extends T> void assign(U assignTo, V assignFrom); 
    }
    
    private enum MethodType {
        READER, WRITER, OTHER
    }
    
    public interface BeanInvocationHandler
    extends Serializable
    {
        public Object invoke(Store store, Method method, Object[] args)
            throws Throwable;
        
        /**
         * Get the name of the relevant property, if applicable.
         * @return the property name, or null if the method type is
         *   not GETTER or SETTER.
         */
        public String getPropertyName();

        /**
         * The type of method. We use this to find getters and setters.
         */
        public MethodType getMethodType();
        
        /**
         * If this method is a reader, get the writer method
         */
        public Method getWriter();
    }
    
    private static final class EqualsHandler
    implements BeanInvocationHandler
    {
        private final Class<?> bean;
        private static final long serialVersionUID = 1L;

        private EqualsHandler(Class<?> bean)
        {
            this.bean = bean;
        }

        @Override
        public Object invoke(Store store, Method method, Object[] args)
        throws Throwable
        {
            MockInvocationHandler mockInvocationHandler = mockOf(args[0]);
            if(mockInvocationHandler != null && 
               mockInvocationHandler.getInterface() == bean) {
                return mockInvocationHandler.getStore().equals(store);
            }
            else {
                return false;
            }
        }

        @Override
        public MethodType getMethodType()
        {
            return MethodType.OTHER;
        }

        @Override
        public String getPropertyName()
        {
            return null;
        }

        @Override
        public Method getWriter()
        {
            return null;
        }
    }

    private static abstract class PropertyHandler
    implements BeanInvocationHandler
    {
        private static final long serialVersionUID = 1L;
        protected final String name;
        
        private PropertyHandler(String name)
        {
            this.name = name;
        }
        
        @Override
        public String getPropertyName()
        {
            return name;
        }
    }
    
    private static class Reader
    extends PropertyHandler
    implements BeanInvocationHandler
    {
        private static final long serialVersionUID = 1L;
        private final Method writer;

        private Reader(String name, Method writer)
        {
            super(name);
            this.writer = writer;
        }

        public Object invoke(Store store, Method method, Object[] args)
        throws Throwable
        {
            return store.getProperty(name);
        }

        @Override
        public MethodType getMethodType()
        {
            return MethodType.READER;
        }

        @Override
        public Method getWriter()
        {
            return writer;
        }
    }
    
    private static final class PrimitiveReader
    extends Reader
    implements BeanInvocationHandler
    {
        private static final long serialVersionUID = 1L;

        final Object defaultValue;
    
        private PrimitiveReader(String name, Method writer, Object defaultValue)
        {
            super(name, writer);
            this.defaultValue = defaultValue;
        }

        public Object invoke(Store store, Method method, Object[] args)
        throws Throwable
        {
            Object result = store.getProperty(name);
            return result == null ? defaultValue : result;
        }
    }

    private static class Writer
    extends PropertyHandler
    implements BeanInvocationHandler
    {
        private static final long serialVersionUID = 1L;

        private Writer(String name)
        {
            super(name);
        }

        public Object invoke(Store store, Method method, Object[] args)
        throws Throwable
        {
            store.setProperty(name, args[0]);
            return null;
        }

        @Override
        public MethodType getMethodType()
        {
            return MethodType.WRITER;
        }

        @Override
        public Method getWriter()
        {
            return null;
        }
    }


    private static Map<Class<?>,Map<Method,BeanInvocationHandler>> cache = 
        new WeakHashMap<Class<?>, Map<Method,BeanInvocationHandler>>();
    
    private static final Method [] STORE_DELEGATE_METHODS = Object.class.getMethods();
    private static final Method EQUALS_METHOD;
    static {
        try {
            EQUALS_METHOD = Object.class.getMethod("equals", new Class<?> [] { Object.class });
        } catch (Exception e) {
            throw new InstantiationError("Couldn't resolve Object.class.equals(Object)");
        }
    }

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

            @Override
            public MethodType getMethodType()
            {
                return MethodType.OTHER;
            }

            @Override
            public String getPropertyName()
            {
                return null;
            }

            @Override
            public Method getWriter()
            {
                return null;
            }  
    };
    
    private static boolean addPropertyMethods(
        Map <Method,BeanInvocationHandler> methods, 
        final Class<?> bean) 
        throws IntrospectionException
    {
        boolean copyable = true;

        if(!bean.isInterface())
            throw new IllegalArgumentException("Not an interface");
        
        BeanInfo info = Introspector.getBeanInfo(bean);
        for(PropertyDescriptor pinfo: info.getPropertyDescriptors()) {
            final String name = pinfo.getName();
            final Method writeMethod = pinfo.getWriteMethod();
            if(pinfo.getReadMethod() != null) {
                final Method method = pinfo.getReadMethod();
                if(!method.isAccessible())
                    method.setAccessible(true);
                if(pinfo.getPropertyType().isPrimitive()) {
                    final Object defaultValue = Types.DEFAULT_VALUES.get(pinfo.getPropertyType());
                    methods.put(method, new PrimitiveReader(name, writeMethod, defaultValue));
                }
                else {
                    methods.put(method, new Reader(name, writeMethod));
                }
            }
            else {
                copyable = false;
            }
            
            if(writeMethod != null) {
                if(!writeMethod.isAccessible())
                    writeMethod.setAccessible(true);
                methods.put(writeMethod, new Writer(name));
            }
        }

        for(Method method: bean.getDeclaredMethods())
            if(!methods.containsKey(method))
                throw new IllegalArgumentException(
                   "Method " + method.getName() + " on " + bean.getName()
                   + " is not a getter or setter");

        for(Class<?> superinterface: bean.getInterfaces())
            copyable &= addPropertyMethods(methods, superinterface);
        
        return copyable;
    }
    
    private static MockInvocationHandler mockOf(Object instance)
    {
        if(Proxy.isProxyClass(instance.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(instance);
            if(handler instanceof MockInvocationHandler) {
                return (MockInvocationHandler)handler;
            }
        }
        
        return null;
    }
    
    private static synchronized Map<Method,BeanInvocationHandler> methods(
        final Class<?> bean) 
        throws IntrospectionException
    {
        Map<Method,BeanInvocationHandler> methods = cache.get(bean);
        if(methods == null) {
            methods = new HashMap<Method,BeanInvocationHandler>();
            addPropertyMethods(methods, bean);
            for(Method method: STORE_DELEGATE_METHODS)
                methods.put(method, STORE_DELEGATE);
            cache.put(bean, methods);
            methods.put(EQUALS_METHOD, new EqualsHandler(bean));
        }
        
        return methods;
    }
    

    /**
     * Return a bean implementation using a property store
     * @param <T> the beans type.
     * @param iface the interface to implement. Introspection on interfaces
     *   works but does not look at super interfaces. We traverse the super
     *   interface tree, and introspect on all the interfaces.
     * @param store handles storing properties
     * @return
     * @throws IntrospectionException
     */
    public static <T> T bean(Class<T> iface, final Store store) 
    {
        return factory(iface).bean(store);
    }

    public interface MockInvocationHandler
    extends InvocationHandler, Serializable 
    { 
        public Class<?> getInterface();

        public Object getStore();
    }
    
    public static class FactoryImpl<T>
    implements Factory<T>, Serializable
    {
        private static final long serialVersionUID = 1L;

        private static final Object [] NO_ARGUMENTS = new Object[0];

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
                new MockInvocationHandler() {

                    private static final long serialVersionUID = 1L;

                    public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable
                    {
                        final BeanInvocationHandler handler = methods.get(method);
                        return handler.invoke(store, method, args);
                    }

                    @Override
                    public Class<?> getInterface()
                    {
                        return interfaceClass;
                    }

                    @Override
                    public Object getStore()
                    {
                        return store;
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

        @Override
        public <U extends T> Transform<U, T> copy()
        {
            return new Transform<U, T>() {
                public T apply(U x) {
                    return copy(x);
                }
            };
        }

        private RuntimeException invalidBeanException(
            String field, 
            Object bean,
            Exception cause)
        {
            if(interfaceClass.isInstance(bean)) {
                return new IllegalArgumentException(
                    "You seem to have found a bug. The field " + field + " in " +
                    interfaceClass.getCanonicalName() + " has an invalid getter or setter" +
                    " and the factory constructor should have detected that", cause);
            }
            else {
                throw new IllegalArgumentException(bean + " does not implement " +
                    interfaceClass.getCanonicalName() + 
                    " and that should be impossible if you have honoured the " +
                    " type parameters for this factory. If you recompile, you " +
                    " will probably find a compilation error", cause);
            }
        }
        
        public <U extends T> Map<String,Object> describe(U bean)
        {
            // FIXME: this could be more efficient, if I kept a list
            // of readers, rather than a list of all the methods handled.
            
            // NOTE: this method does not use assign, below, hence it works
            // for interfaces that don't have a full complement of setters
 
            Map<String,Object> description = new HashMap<String,Object>();
            for(Map.Entry<Method, BeanInvocationHandler> method: methods.entrySet()) {
                if(MethodType.READER.equals(method.getValue().getMethodType())) {
                    String name = method.getValue().getPropertyName();
                    Method readMethod = method.getKey();
                    description.put(name, invokePropertyMethod(bean, name, readMethod));
                }
            }
            
            return description;
        }

        private <U> Object invokePropertyMethod(
            U bean,
            String name,
            Method readMethod,
            Object ... arguments)
        {
            try {
                return readMethod.invoke(bean, arguments);
            } catch (IllegalArgumentException e) {
                throw invalidBeanException(name, bean, e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(
                    "The method " + readMethod.getName() + " in " +
                    interfaceClass + " isn't accessible. Either " +
                    interfaceClass + " must be public, or this function " +
                    "must be authorised to override reflection");
            } catch (InvocationTargetException e) {
                if(e.getCause() instanceof RuntimeException)
                    throw (RuntimeException)e.getCause();
                else
                    throw invalidBeanException(name, bean,e);
            }
        }
        
        @Override
        public <U extends T> T copy(U other)
        {
            return bean(describe(other));
        }

        @Override
        public <U extends T, V extends T> void assign(U assignTo, V assignFrom)
        {
            for(Map.Entry<Method, BeanInvocationHandler> method: methods.entrySet()) {
                if(MethodType.READER.equals(method.getValue().getMethodType())) {
                    Method readMethod = method.getKey();
                    Method writeMethod = method.getValue().getWriter();
                    String name = method.getValue().getPropertyName();
                    if(writeMethod == null)
                        throw new IllegalArgumentException(
                            "No setter for " + name +
                            "in " + interfaceClass.getCanonicalName() + 
                            ". To use assign, there must be a set method " +
                            "corresponding to every get method");

                    this.invokePropertyMethod(assignTo, name, writeMethod, 
                        invokePropertyMethod(assignFrom, name, readMethod));
                }
            }
            
        }
    }
    
    public static <T> Factory<T> factory(Class<T> iface)
    {
        try {
            return new FactoryImpl<T>(iface);
        } catch (IntrospectionException e) {
            throw new MockIntrospectionException(e);
        }
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
    {
        return bean(iface, store(map));
    }
    
    public static <T> T bean(Class<T> iface) 
    {
        return bean(iface, store(new HashMap<String, Object>()));
    }
    
    public static <T> T copy(Class<T> iface, T source)
    {
        return factory(iface).copy(source);
    }
}
