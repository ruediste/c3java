package com.github.ruediste.c3java.invocationRecording;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import com.google.common.base.Defaults;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

/**
 * Records invocations of methods to a proxy.
 */
public class MethodInvocationRecorder {

    private static Object placeHolder = new Object();
    private static volatile Map<Class<?>, Object> terminalTypes = new WeakHashMap<>();

    public static void addTerminalType(Class<?> clazz) {
        synchronized (placeHolder) {
            WeakHashMap<Class<?>, Object> tmp = new WeakHashMap<>(terminalTypes);
            tmp.put(clazz, placeHolder);
            terminalTypes = tmp;
        }
    }

    static {
        addTerminalType(String.class);
        addTerminalType(Date.class);
        addTerminalType(Optional.class);
    }

    private final ArrayList<MethodInvocation<Object>> invocations = new ArrayList<>();

    public <T> T getProxy(Class<T> type) {
        return getProxy(TypeToken.of(type));
    }

    private static final CallbackFilter FINALIZE_FILTER = new CallbackFilter() {
        @Override
        public int accept(Method method) {
            if (method.getName().equals("finalize") && method.getParameterTypes().length == 0
                    && method.getReturnType() == Void.TYPE) {
                return 0;
            }
            return 1;
        }
    };

    @SuppressWarnings("unchecked")
    public <T> T getProxy(TypeToken<T> type) {
        Class<? super T> cls = type.getRawType();

        if (isTerminal(type)) {
            return (T) Defaults.defaultValue(cls);
        }

        Enhancer e = new Enhancer();
        if (Enhancer.isEnhanced(cls)) {
            e.setSuperclass(cls.getSuperclass());
            e.setInterfaces(cls.getInterfaces());
        } else {
            e.setSuperclass(cls);
        }
        e.setCallbackFilter(FINALIZE_FILTER);
        e.setCallbacks(new Callback[] { NoOp.INSTANCE, new MethodInterceptor() {

            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                invocations.add(new MethodInvocation<Object>(type, method, Arrays.asList(args)));

                return getProxy(type.resolveType(method.getGenericReturnType()));
            }

        } });

        try {
            return (T) e.create();
        } catch (Exception ex) {
            throw new RuntimeException("Error while creating proxy of " + type, ex);
        }
    }

    protected boolean isTerminal(TypeToken<?> returnType) {
        Class<?> clazz = returnType.getRawType();
        return clazz.isPrimitive() || clazz.isEnum() || clazz.isAnnotationPresent(TerminalType.class)
                || Primitives.isWrapperType(clazz) || terminalTypes.containsKey(clazz) || clazz.isArray();
    }

    public List<MethodInvocation<Object>> getInvocations() {
        return Collections.unmodifiableList(invocations);
    }

    public MethodInvocation<Object> getLastInvocation() {
        return Iterables.getLast(invocations);
    }

    public static <T> List<MethodInvocation<Object>> getInvocations(Class<T> cls, Consumer<T> accessor) {
        return getInvocations(TypeToken.of(cls), accessor);
    }

    public static <T> List<MethodInvocation<Object>> getInvocations(TypeToken<T> cls, Consumer<T> accessor) {
        MethodInvocationRecorder recorder = new MethodInvocationRecorder();
        accessor.accept(recorder.getProxy(cls));
        return recorder.getInvocations();
    }

    public static <T> MethodInvocation<Object> getLastInvocation(Class<T> cls, Consumer<T> accessor) {
        return getLastInvocation(TypeToken.of(cls), accessor);
    }

    public static <T> MethodInvocation<Object> getLastInvocation(TypeToken<T> cls, Consumer<T> accessor) {
        MethodInvocationRecorder recorder = new MethodInvocationRecorder();
        accessor.accept(recorder.getProxy(cls));
        return recorder.getLastInvocation();
    }

}
