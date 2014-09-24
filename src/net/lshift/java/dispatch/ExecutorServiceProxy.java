package net.lshift.java.dispatch;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static java.util.Arrays.asList;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Iterables;

/**
 * Execute methods in a thread.
 * This looks like a pretty strange thing to want to do, but its born
 * of a specific need...
 *
 * I'm using sleepy cat BDB java edition, and I want to use its stored
 * collections. The collections rely on ambient authority to get a transaction:
 * the transactions are bound to threads. However there is no way to bind a
 * thread to a transaction, other than when the transaction is created.
 * If I want to write an RMI based server, this is obviously pretty useless.
 *
 * @author david
 *
 */
public class ExecutorServiceProxy
{
    private static final ThreadLocal<ExecutorServiceProxy> PROXY =
        new ThreadLocal<ExecutorServiceProxy>();

    private static final long DEFAULT_TIMEOUT = 30; // 30 seconds


    public interface ThreadProperty<T>
    {
        /**
         * Get the value of this property for the current thread
         * @return
         */
        public T get();

        /**
         * Set the value of this property for the current thread
         * @param value
         */
        public void set(T value);

        /**
         * Clear the value of this property for the current thread
         */
        public void remove();
    }

    public static <T> ThreadProperty<T> threadProperty(final ThreadLocal<T> tl)
    {
        return new ThreadProperty<T>() {

            public void remove()
            {
                tl.remove();
            }

            public T get()
            {
                return tl.get();
            }

            public void set(T value)
            {
                tl.set(value);
            }

        };
    }

    public final ExecutorService executor;

    /**
     * This is to allow you to transfer the values of thread context
     * to the execution service while it executes the requested method.
     * Its probably most useful for logging purposes.
     */
    public final Set<ThreadProperty<Object>> properties;

    public ExecutorServiceProxy(ExecutorService executor)
    {
        this(executor, new HashSet<ThreadProperty<Object>>());
    }

    public ExecutorServiceProxy
        (ExecutorService executor,
         Set<ThreadProperty<Object>> properties)
    {
        this.executor = executor;
        this.properties = properties;
    }

    private StackTraceElement [] truncateStack
        (Throwable cause, Class<?> delegate)
    {
        try {
            throw new Exception();
        }
        catch(Exception e) {
            return Iterables.toArray(
                Iterables.filter(
                    asList(cause.getStackTrace()),
                    not(in(asList(e.getStackTrace())))),
                StackTraceElement.class);
        }
    }

    private StackTraceElement [] mergeStack
        (Throwable cause,
         Throwable ref,
         Class<?> proxy)
    {
        StackTraceElement [] catchStack = ref.getStackTrace();
        StackTraceElement [] causeStack = cause.getStackTrace();
        List<StackTraceElement> merged =
            new ArrayList<StackTraceElement>(catchStack.length + causeStack.length);
        String proxyName = proxy.getName();
        for(StackTraceElement element: causeStack) {
            merged.add(element);
            if(element.getClassName().equals(proxyName))
                break;
        }

        merged.add(new StackTraceElement
                   (ExecutorServiceProxy.class.getName(),
                    "invoke", "Generated Source", -1));

        int index = 0;
        for(; (index != catchStack.length
               && !catchStack[index].getClassName().equals(proxyName)); ++index);
        for(; index != catchStack.length; ++index)
            if(!catchStack[index].getClassName().equals(proxyName))
                merged.add(catchStack[index]);


        return merged.toArray(new StackTraceElement[merged.size()]);
    }

    private class ProxyInvocationHandler
        implements InvocationHandler
    {
        final Object delegate;

        public ProxyInvocationHandler(Object delegate)
        {
            this.delegate = delegate;
        }

        public Object invoke(Object proxy, final Method method, final Object[] args)
            throws Throwable
        {
            final Map<ThreadProperty<Object>,Object> propertyValues =
                new HashMap<ThreadProperty<Object>,Object>();

            for(ThreadProperty<Object> property: properties) {
                propertyValues.put(property, property.get());
            }

            Future<Object> future = executor.submit(new Callable<Object>() {

                public Object call()
                throws Exception
                {
                    PROXY.set(ExecutorServiceProxy.this);
                    for(Map.Entry<ThreadProperty<Object>,Object> entry: propertyValues.entrySet())
                        entry.getKey().set(entry.getValue());

                    try {
                        return method.invoke(delegate, args);
                    }
                    catch(InvocationTargetException e) {
                        Throwable cause = e.getTargetException();
                        cause.setStackTrace
                        (truncateStack(cause, delegate.getClass()));
                        if(cause instanceof Exception)
                            throw (Exception)cause;
                        else
                            throw (Error)cause;
                    }
                    finally {
                        PROXY.remove();
                        for(ThreadProperty<Object> property: propertyValues.keySet())
                            property.remove();
                    }
                }

            });

            try {
                return future.get();
            }
            catch(ExecutionException e) {
                Throwable cause = e.getCause();
                try {
                    throw new Exception();
                }
                catch(Exception current) {
                    cause.setStackTrace
                    (mergeStack(cause, current, proxy.getClass()));
                }

                throw cause;
            }
            finally {
                // if the executor is shutting down, wait for it to finish
                // this gives the expected behaviour when stop is called
                // from within a proxy method
                if(executor.isShutdown())
                    executor.awaitTermination(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * Construct a proxy, for which each method is
     * invoked by the execution server.
     * @param delegate the object whose methods are to be run inside
     *   the worker thread.
     * @return a proxy implementing the interfaces
     */
    @SuppressWarnings("unchecked")
    public <T> T proxy(final Object delegate, Class<?> [] interfaces)
    {
        return (T)Proxy.newProxyInstance
            (delegate.getClass().getClassLoader(),
             interfaces,
             new ProxyInvocationHandler(delegate));
    }

    public void stop()
        throws InterruptedException
    {
        executor.shutdown();
        if(this != current())
            executor.awaitTermination(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    public static ExecutorServiceProxy current()
    {
        return PROXY.get();
    }
}
