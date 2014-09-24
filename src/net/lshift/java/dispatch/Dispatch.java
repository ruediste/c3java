package net.lshift.java.dispatch;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Dispatch
{

    public static class Before<T,C> {
        public final T instance;
        public final C context;
        public final Object [] arguments;

        public Before(T instance, C context, Object[] arguments) {
            this.instance = instance;
            this.context = context;
            this.arguments = arguments;
        }
    }

    public interface Filter<T, C>
    {
        /**
         * Executed before every invocation of a method on the instance.
         * Allows the arguments to be replaced.
         * @param instance the instance the method will be dispatched to
         * @param m the method being invoked
         * @param args the parameters the proxy was invoked with
         * @return a tuple of: arguments compatible with with the method being invoked
         */
        public Before<T, C> before(T instance, Method m, Object [] args);

        /**
         * Executed when the invocation of a method on target returns.
         * This method is not called when the target throws an exception
         * Allows replacement of the return value
         * @param instance
         * @param m
         * @param args
         * @param returned
         * @return
         */
        public Object returned(C context, T instance, Method m, Object [] args, Object returned);

        /**
         * Called when the target invocation resulted in an exception
         * @param instance
         * @param m
         * @param args
         * @param exception
         * @return
         */
        public Throwable exception(C context, T instance, Method m, Object [] args, Throwable exception);

        /**
         * Executed after every invocation of a method on the instance.
         * This is effectively a dynamic version of finally
         * @param instance the instance the method will be dispatched to
         * @param m the method being invoked
         * @param args the parameters the proxy was invoked with
         * @return arguments compatible with with the mathod being invoked
         */
        public void after(C context, T instance, Method m, Object [] args);
    }

    /**
     * @param <T>
     * @param iface
     * @param instance
     * @param filter
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T,U extends T,C> T filter(
        Class<T> iface,
        final U instance,
        final Filter<U,C> filter)
    {
        return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class [] { iface }, new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable
            {
                Before<U,C> before = filter.before(instance, method, args);
                try {

                    return filter.returned(
                        before.context,
                        before.instance,
                        method,
                        args,
                        method.invoke(
                            before.instance,
                            before.arguments));
                }
                catch(InvocationTargetException e) {
                    throw filter.exception(before.context, before.instance, method, args, e.getCause());
                }
                finally {
                    filter.after(before.context, before.instance, method, args);
                }
            }
        });
    }
}
