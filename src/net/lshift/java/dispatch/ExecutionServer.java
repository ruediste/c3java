package net.lshift.java.dispatch;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.BitSet;

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
public class ExecutionServer
    implements Runnable
{
    public static final int STATE_INITIALIZING = 0;
    public static final int STATE_IDLE = 1;
    public static final int STATE_EXECUTE = 2;
    public static final int STATE_EXECUTING = 3;
    public static final int STATE_RETURN = 4;
    public static final int STATE_EXCEPTION = 5;
    public static final int STATE_ERROR = 6;
    public static final int STATE_STOP = 7;
    public static final int STATE_STOPPED = 8;
    public static final int STATE_ABORTED = 9;

    public static final BitSet RUNNING_STATES = new BitSet();
    static {
        RUNNING_STATES.set(STATE_IDLE);
        RUNNING_STATES.set(STATE_EXECUTING);
        RUNNING_STATES.set(STATE_RETURN);
        RUNNING_STATES.set(STATE_EXCEPTION);
        RUNNING_STATES.set(STATE_ERROR);
    }
    
    public static final String [] STATE_NAMES = {
        "initializing",
        "idle",
        "execute",
        "executing",
        "return",
        "exception",
        "error",
        "stop",
        "stopped",
        "aborted"
    };
    
    public interface Executable
    {
        public Object execute()
            throws Exception;
    }
    
    /**
     * The execution server is in an invalid state
     * for the requested operation.
     * @author david
     *
     */
    public class ExecutionServerIllegalStateException
        extends java.lang.IllegalStateException 
    {
        private static final long serialVersionUID = 1L;
        
        public ExecutionServerIllegalStateException(int state)
        {
            super(STATE_NAMES[state]);
        }
    }
    
    public int state = STATE_STOPPED;
    public Executable executable = null;
    public Object result = null;

    /**
     * Start the execution server.
     * This starts a thread that will remain running
     * until you call stop()
     * @throws InterruptedException 
     * @see stop()
     */
    public synchronized void start() 
        throws InterruptedException
    {
        if(this.state != STATE_STOPPED)
            throw new IllegalStateException();
        this.state = STATE_INITIALIZING;
        Thread thread = new Thread(this);
        thread.start();
        
        while(state != STATE_IDLE)
            wait();
    }
    
    public synchronized void run()
    {
        if(this.state != STATE_INITIALIZING)
            throw new ExecutionServerIllegalStateException(this.state);
        
        try {
            this.state = STATE_IDLE;
            notify();
            
            while(true) {
                switch(this.state) {
                case STATE_IDLE:
                case STATE_RETURN:
                case STATE_EXCEPTION:
                case STATE_ERROR:
                    wait();
                    continue;
                case STATE_EXECUTE:
                    this.state = STATE_EXECUTING;
                    try {
                        this.result = executable.execute();
                        this.state = STATE_RETURN;
                    } 
                    catch (Exception e) {
                        this.result = e;
                        this.state = STATE_EXCEPTION;
                    }
                    catch(Error e) {
                        this.result = e;
                        this.state = STATE_ERROR;
                    }

                    notify();
                    break;
                case STATE_STOP:
                    this.state = STATE_STOPPED;
                    notify();
                    return;

                default:
                    throw new ExecutionServerIllegalStateException(this.state);
                }
            }
        }
        catch(Throwable t) {
            t.printStackTrace();
            this.state = STATE_ABORTED;
            this.result = t;
            notify();
        }
    }
    
    private void idle() 
        throws InterruptedException
    {
        if(!RUNNING_STATES.get(this.state))
            throw new ExecutionServerIllegalStateException(this.state);
        
        while(this.state != STATE_IDLE)
            wait();
    }
    
    /**
     * Ask the execution server to execute something
     * @param executable
     * @return whatever the executable returns
     * @throws Exception any exeption the executable throws
     * @throws ExecutionServerIllegalStateException if the server
     *   isn't running at the moment (you must call start() before
     *   calling this, and you can't call this after calling stop()
     */
    public synchronized Object execute(Executable executable)
        throws Exception
    {
        idle();
        
        this.executable = executable;
        this.state = STATE_EXECUTE;
        notify();
        
        while(true) {
            switch(this.state) {
            case STATE_EXECUTE:
            case STATE_EXECUTING:
                wait();
                continue;
            case STATE_RETURN:
                this.state = STATE_IDLE;
                return result;
            case STATE_EXCEPTION:
                this.state = STATE_IDLE;
                throw (Exception)result;
            case STATE_ERROR:
                this.state = STATE_IDLE;
                throw (Error)result;
            default:
                throw new ExecutionServerIllegalStateException(this.state);
            }
        }
    }
    
    public synchronized void stop()
        throws InterruptedException
    {
        idle();

        this.state = STATE_STOP;
        notify();
        
        while(true) {
            switch(this.state) {
            case STATE_STOP:
                wait();
                continue;
            case STATE_STOPPED:
                return;
            default:
                throw new ExecutionServerIllegalStateException(this.state);
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
    public Object proxy(final Object delegate, Class [] interfaces)
    {
        return Proxy.newProxyInstance
            (delegate.getClass().getClassLoader(),
             interfaces,
             new InvocationHandler() {

            public Object invoke(Object proxy, final Method method, final Object[] args)
                throws Throwable
            {
                return execute(new Executable() {

                    public Object execute()
                        throws Exception
                    {
                        try {
                            return method.invoke(delegate, args);
                        }
                        catch(InvocationTargetException e) {
                            Throwable cause = e.getTargetException();
                            if(cause instanceof Exception)
                                throw (Exception)cause;
                            else
                                throw (Error)cause;
                        }
                    }

                });
            }

        });
    }
}
