package net.lshift.java.dispatch;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Dispatch
{
    public interface Filter<T>
    {
        /**
         * Executed before every invocation of a method on the instance.
         * Allows the arguments to be replaced.
         * @param instance the instance the method will be dispatched to
         * @param m the method being invoked
         * @param args the parameters the proxy was invoked with
         * @return arguments compatible with with the mathod being invoked
         */
        public Object [] before(T instance, Method m, Object [] args);
        
        /**
         * Executed after every invocation of a method on target.
         * Allows replacement of the return value
         * @param instance
         * @param m
         * @param args
         * @param returned
         * @return
         */
        public Object after(T instance, Method m, Object [] args, Object returned);
        public Throwable exception(T instance, Method m, Object [] args, Throwable exception);
    }
    
    /**
     * @param <T>
     * @param iface
     * @param filter
     * @param instance
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T filter(
        Class<T> iface, 
        final Filter<T> filter,
        final T instance)
    {
        return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class [] { iface }, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable
            {
                try {
                    return filter.after(
                        instance, 
                        method, 
                        args, 
                        method.invoke(
                            instance, 
                            filter.before(instance, method, args)));
                }
                catch(InvocationTargetException e) {
                    throw filter.exception(instance, method, args, e.getCause());
                }
            }
            
        });
    }
}
