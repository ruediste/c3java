package net.lshift.java.dispatch;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ExecutionServerTest
    extends TestCase
{
    public static TestSuite suite()
    {
        return new TestSuite(ExecutionServerTest.class);
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
    }
    
    public static class TargetImpl
        implements Target
    {
        private final Object magic = new Object();
        private final ThreadLocal local = new ThreadLocal();
        
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
    }
    
    ExecutionServer server;
    Target target;
    
    public void setUp() 
        throws InterruptedException
    {
        server = new ExecutionServer();
        target = (Target)server.proxy(new TargetImpl(), new Class [] { Target.class });
        server.start();
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
}
