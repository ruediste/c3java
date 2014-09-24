package net.lshift.java.dispatch;


import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;

/**
 * Dispatch to a delegate, converting parameters and return values.
 * The delegate must implement the proxy interface. The idea is that the
 * parameters are processed consistently according to their type, rather than by
 * the method or position. This is useful for simulating RPC - simply by using
 * serialization to clone the arguments and return values. It can also be used
 * to normalise values.
 */
public class ConverterDispatch
{
    public interface ParameterConverterFactory
    {
        public <S extends Object, D extends Object>
            Function<S,D> converter(Class<S> source, Class<D> dest);
    }

    /**
     * Create a proxy, given a parameter converter.
     * @param <C> the contract class
     * @param contract the contract class
     * @param target the object to delegate to
     * @param converter the converter, which nows how to convert
     *   parameters and return values.
     * @return
     */
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

    @SuppressWarnings("unchecked")
    private static InvocationHandler invocationHandler(
                    final Method targetMethod,
                    final Method contractMethod,
                    final Object target,
                    final ParameterConverterFactory converter)
    {
        final int argc = targetMethod.getParameterTypes().length;
        final List<Function<Object, Object>> converters = Lists.newArrayListWithCapacity(argc);
        // We use wrap here, because we distinguish between primitives and there wrapper classes.
        // That's because that's consistent with the compiler auto-boxing
        for(int i = 0; i != argc; ++i)
            converters.add((Function<Object,Object>)converter.converter(
                           Primitives.wrap(contractMethod.getParameterTypes()[i]),
                           Primitives.wrap(targetMethod.getParameterTypes()[i])));

        return new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable
            {
                Object [] converted = new Object[argc];
                for(int i = 0; i != argc; ++i)
                    converted[i] = (Object)converters.get(i).apply(args[i]);
                try {
                    return method.invoke(target, converted);
                }
                catch(InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        };
    }

    static class MethodKey {
        public final String name;
        public final int parameters;

        public MethodKey(Method method) {
            this.name = method.getName();
            this.parameters = method.getParameterTypes().length;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name, parameters);
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) {
                return true;
            } else if(this.getClass() == obj.getClass()) {
                MethodKey other = (MethodKey)obj;
                return this.name.equals(other.name) && this.parameters == other.parameters;
            } else {
                return false;
            }
        }
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
