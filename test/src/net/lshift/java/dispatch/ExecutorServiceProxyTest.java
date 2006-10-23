package net.lshift.java.dispatch;

import java.util.concurrent.Executors;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ExecutorServiceProxyTest
    extends TestCase
{
    public static TestSuite suite()
    {
        return new TestSuite(ExecutorServiceProxyTest.class);
    }

    public static class TargetException
        extends Exception
    {
        private static final long serialVersionUID = 1L;
    }
    
    public static class TargetError
        extends Error
    {
        private static final long serialVersionUID = 1L;
    }
    
    
    
    public interface Target
    {
        public void set();
        
        public boolean test();
        
        public void exception()
            throws TargetException;
        
        public void error();

        public void stop()
            throws InterruptedException;
        
        public void remove();
    }
    
    public static class TargetImpl
        implements Target
    {
        private final Object magic = new Object();
        public final ThreadLocal<Object> local = new ThreadLocal<Object>();
        
        public void set()
        {
            local.set(magic);
        }
        
        public boolean test()
        {
            return local.get() == magic;
        }
        
        public void exception()
            throws TargetException
        {
            throw new TargetException();
        }
        
        public void error()
        {
            throw new TargetError();
        }
        
        public void stop()
            throws InterruptedException
        {
            ExecutorServiceProxy.current().stop();
        }

        public void remove()
        {
            local.remove();
        }
    }
    
    ExecutorServiceProxy server;
    TargetImpl impl;
    Target target;
    
    public void setUp() 
        throws InterruptedException
    {
        server = new ExecutorServiceProxy(Executors.newSingleThreadExecutor());
        impl = new TargetImpl();
        target = (Target)server.proxy(impl, new Class [] { Target.class });
    }
    
    public void tearDown() 
        throws InterruptedException
    {
        server.stop();
    }
    
    public void testMagic()
    {
        target.set();
        assertTrue(target.test());
    }
    
    public void testException()
    {
        boolean exception = false;
        try {
            target.exception();
        }
        catch(TargetException e) {
            e.printStackTrace();
            exception = true;
        }
        
        assertTrue(exception);
    }
    
    public void testError()
    {
        boolean error = false;
        try {
            target.error();
        }
        catch(TargetError e) {
            error = true;
        }
        
        assertTrue(error);
    }
    
    public void testCurrentStop()
        throws Exception
    {
        ExecutorServiceProxy server = new ExecutorServiceProxy(Executors.newSingleThreadExecutor());
        Target target = (Target)server.proxy(new TargetImpl(), new Class [] { Target.class });
        target.stop();
        assertTrue(server.executor.isTerminated());
    }
    
    public void testProperties()
    {
        server.properties.add(ExecutorServiceProxy.threadProperty(impl.local));

        try {
            impl.set();
            assertTrue(target.test());
        }
        finally {
            impl.remove();
        }
        
        assertFalse(target.test());
    }
}
