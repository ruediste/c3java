package net.lshift.java.dispatch;

import static net.lshift.java.lang.Types.asClass;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lshift.java.util.Transform;
import net.lshift.java.util.TwoTuple;


public class ConverterDispatch
{
    public interface ParameterConverterFactory
    {
        public <S extends Object, D extends Object> 
            Transform<S,D> converter(Class<S> source, Class<D> dest);
    }
    
    @SuppressWarnings("unchecked")
    public static <C> C proxy(
                    Class<C> contract,
                    Object target, 
                    ParameterConverterFactory converter)
    {
        return (C)Proxy.newProxyInstance(
                        contract.getClassLoader(), 
                        new Class [] { contract }, 
                        invocationHandler(contract, target, converter));
    }

    private static InvocationHandler invocationHandler(
                    final Method targetMethod,
                    final Method contractMethod,
                    final Object target,
                    final ParameterConverterFactory converter)
    {
        final int argc = targetMethod.getParameterTypes().length;
        final List<Transform<Object, Object>> converters = 
            new ArrayList<Transform<Object,Object>>(argc);
        // We use asClass here, because we don't care about translating
        // primitive types in a specific way.
        for(int i = 0; i != argc; ++i)
            converters.add((Transform<Object,Object>)converter.converter(
                           asClass(contractMethod.getParameterTypes()[i]),
                           asClass(targetMethod.getParameterTypes()[i])));
        
        return new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable
            {
                Object [] converted = new Object[argc];
                for(int i = 0; i != argc; ++i)
                    converted[i] = (Object)converters.get(i).apply(args[i]);
                return method.invoke(target, converted);
            }
            
        };
    }

    static class MethodKey extends TwoTuple<String,Integer> {
        public MethodKey(Method method) {
            super(method.getName(), method.getParameterTypes().length);
        }

        private static final long serialVersionUID = 1L; 
    }
    
    private static <C> InvocationHandler invocationHandler(
                    Class<C> contract,
                    Object target,
                    ParameterConverterFactory converter)
    {
        Map<MethodKey,Method> targets = new HashMap<MethodKey,Method>();
        for(Method method: target.getClass().getMethods()) {
            if(targets.containsKey(new MethodKey(method)))
                throw new IllegalArgumentException(
                    "Overloading is not supported. " + 
                    method.getName() + " in class " + 
                    target.getClass().getName() + " is overloaded.");
            targets.put(new MethodKey(method), method);
        }
        
        final Map<Method,InvocationHandler> handlers = new HashMap<Method,InvocationHandler>();
        for(Method contractMethod: contract.getMethods()) {
            Method targetMethod = targets.get(new MethodKey(contractMethod));
            handlers.put(contractMethod, 
                invocationHandler(targetMethod, 
                    contractMethod, 
                    target, 
                    converter));
        }
        
        return new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable
            {
                return handlers.get(method).invoke(proxy, method, args);
            }
      };
    }
}
