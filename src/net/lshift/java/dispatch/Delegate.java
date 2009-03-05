package net.lshift.java.dispatch;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.lshift.java.lang.Variable;
import net.lshift.java.lang.Variables;

public class Delegate
{
    public static final InvocationHandler NOOP = new InvocationHandler() {
        public Object invoke(Object proxy, Method method, Object[] args) {
            return null;
        }
    };
    
    public static final InvocationHandler UNSUPORTED = new InvocationHandler() {
        public Object invoke(Object proxy, Method method, Object[] args) {
            throw new UnsupportedOperationException();
        }
    };

    /**
     * Create a proxy which only delegates if a condition is true.
     * @param <C> the contract class
     * @param contract the contract class
     * @param target the object to delegate to
     * @param condition - if Reference.get() returns true at the
     *   time of the call, then the method will be executed, otherwise not.
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <C> C delegateIf(
                    Class<C> contract,
                    Variable<C> target, 
                    Variable<Boolean> condition)
    {
        checkVoid(contract);
        return (C)Proxy.newProxyInstance(
                        contract.getClassLoader(), 
                        new Class [] { contract }, 
                        invocationHandler(contract, target, condition));
    }

    private static <C> void checkVoid(Class<C> contract)
    {
        for(Method method: contract.getMethods())
            if(!Void.TYPE.equals(method.getReturnType()))
                throw new IllegalArgumentException(
                    "Only methods with void as their return type are allowed: " +
                    method.getName() + " in interface " + contract.getName() + 
                    " has return type " + method.getReturnType());
    }
    
    public static <C> C delegate(
        Class<C> contract,
        Variable<C> target)
    {
        return delegateIf(contract, target, Variables.TRUE);
    }
    
    @SuppressWarnings("unchecked")
    public static <C> C noop(Class<C> contract)
    {
        checkVoid(contract);
        return (C)Proxy.newProxyInstance(
                        contract.getClassLoader(), 
                        new Class [] { contract }, 
                        NOOP);
    }
    
    @SuppressWarnings("unchecked")
    public static <C> C unsupported(Class<C> contract)
    {
        checkVoid(contract);
        return (C)Proxy.newProxyInstance(
                        contract.getClassLoader(), 
                        new Class [] { contract }, 
                        UNSUPORTED);
    }
    
    private static <C> InvocationHandler invocationHandler(
        final Class<C> contract,
        final Variable<C> target,
        final Variable<Boolean> condition)
    {
        return new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
            {
                try {
                    if(condition.get())
                        method.invoke(target.get(), args);
                    return null;
                }
                catch(InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        };
    }
   
}
