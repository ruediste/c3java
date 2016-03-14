package com.github.ruediste.c3java.invocationRecording;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility function for {@link MethodInvocation}
 */
public class MethodInvocationUtil {

    public static Object invoke(Object target, MethodInvocation<Object> invocation) {
        try {
            Method method = invocation.getMethod();
            method.setAccessible(true);
            return method.invoke(target, invocation.getArguments().toArray());
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException("Error while invoking " + invocation.getMethod() + " on target " + target, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error while executing " + invocation.getMethod(), e);
        }
    }
}
