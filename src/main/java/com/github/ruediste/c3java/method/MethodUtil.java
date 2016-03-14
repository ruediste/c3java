package com.github.ruediste.c3java.method;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import com.github.ruediste.c3java.linearization.JavaC3;
import com.google.common.collect.Iterables;

public class MethodUtil {

    /**
     * Get all method declarations which are overridded by a given method
     * declaration. The result starts with the given method.
     */
    public static Iterable<Method> getDeclarations(Method method) {
        ArrayList<Method> result = new ArrayList<>();
        result.add(method);
        for (Class<?> cls : Iterables.skip(JavaC3.allSuperclasses(method.getDeclaringClass()), 1)) {
            for (Method m : cls.getDeclaredMethods()) {

                if (m.getName().equals(method.getName())
                        && Arrays.equals(m.getParameterTypes(), method.getParameterTypes())) {
                    if (doesOverride(method, m))
                        result.add(m);
                }
            }
        }
        return result;
    }

    /**
     * Directly copied form Salta's MethodOverrideIndex
     */
    private static boolean doesOverride(Method m, Method ancestorMethod) {
        if (Modifier.isFinal(ancestorMethod.getModifiers()))
            return false;
        if (Modifier.isPublic(ancestorMethod.getModifiers()))
            return true;
        if (Modifier.isProtected(ancestorMethod.getModifiers()))
            return true;
        if (Modifier.isPrivate(ancestorMethod.getModifiers()))
            return false;

        // ancestorMethod must be package visible
        String ancestorPackage = ancestorMethod.getDeclaringClass().getPackage().getName();
        String mPackage = m.getDeclaringClass().getPackage().getName();
        if (Objects.equals(ancestorPackage, mPackage))
            return true;
        return false;
    }

}
