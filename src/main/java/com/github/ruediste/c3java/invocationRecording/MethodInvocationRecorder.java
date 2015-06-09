package com.github.ruediste.c3java.invocationRecording;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.google.common.base.Defaults;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;

/**
 * Records invocations of methods to a proxy.
 */
public class MethodInvocationRecorder {

    private final ArrayList<MethodInvocation<Object>> invocations = new ArrayList<>();

    public <T> T getProxy(Class<T> type) {
        return getProxy(TypeToken.of(type));
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(TypeToken<T> type) {
        if (isTerminal(type)) {
            return (T) Defaults.defaultValue(type.getRawType());
        }

        Enhancer e = new Enhancer();
        e.setSuperclass(type.getRawType());
        e.setCallback(new MethodInterceptor() {

            @Override
            public Object intercept(Object obj, Method method, Object[] args,
                    MethodProxy proxy) throws Throwable {
                invocations.add(new MethodInvocation<Object>(type, method,
                        Arrays.asList(args)));

                return getProxy(type.resolveType(method.getGenericReturnType()));
            }

        });

        return (T) e.create();
    }

    private boolean isTerminal(TypeToken<?> returnType) {
        Class<?> clazz = returnType.getRawType();
        return clazz.isPrimitive() || String.class.equals(clazz)
                || Date.class.equals(clazz);
    }

    public List<MethodInvocation<Object>> getInvocations() {
        return Collections.unmodifiableList(invocations);
    }

    public MethodInvocation<Object> getLastInvocation() {
        return Iterables.getLast(invocations);
    }

}
