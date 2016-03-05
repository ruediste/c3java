package com.github.ruediste.c3java.properties;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.github.ruediste.c3java.invocationRecording.MethodInvocation;
import com.github.ruediste.c3java.invocationRecording.MethodInvocationRecorder;
import com.github.ruediste.c3java.linearization.JavaC3;
import com.github.ruediste.c3java.properties.PropertyAccessor.AccessorType;
import com.github.ruediste.c3java.properties.PropertyPath.PropertyPathNode;
import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

public class PropertyUtil {

    private PropertyUtil() {
    }

    /**
     * Create an accessor from the given method, or null if the method is no
     * accessor
     */
    static public PropertyAccessor getAccessor(Method method) {
        if (Modifier.isPrivate(method.getModifiers()) || Modifier.isStatic(method.getModifiers())
                || method.isAnnotationPresent(NoPropertyAccessor.class)) {
            return null;
        }

        // check for getters
        if (method.getName().startsWith("get") && method.getReturnType() != null) {
            String name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, method.getName().substring("get".length()));

            // getFoo()
            if (method.getParameterCount() == 0) {
                return new PropertyAccessor(name, AccessorType.GETTER, method, method.getReturnType());
            }

        }

        // check for setters

        if (method.getName().startsWith("set")) {
            String name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, method.getName().substring("set".length()));
            // setFoo(value)
            if (method.getParameterCount() == 1) {
                return new PropertyAccessor(name, AccessorType.SETTER, method, method.getParameterTypes()[0]);
            }

        }
        return null;
    }

    /**
     * Return all properties which are directly declared on the provided type
     */
    static public Map<String, PropertyDeclaration> getPropertyDeclarations(Class<?> type) {
        Map<String, PropertyDeclaration> result = new HashMap<>();
        HashSet<String> failingProperties = new HashSet<>();

        // scan methods
        for (Method method : type.getDeclaredMethods()) {
            PropertyAccessor accessor = getAccessor(method);
            if (accessor == null)
                continue;
            PropertyDeclaration property = result.get(accessor.getName());
            if (property == null) {
                property = new PropertyDeclaration(accessor.getName(), type);
            }
            if (property.matchesPropertyType(accessor))
                result.put(accessor.getName(), property.withAccessor(accessor));
            else {
                failingProperties.add(accessor.getName());
            }
        }

        // fill backing fields
        for (Field f : type.getDeclaredFields()) {
            String name = f.getName();
            PropertyDeclaration property = result.get(name);
            if (property != null) {
                if (property.matchesPropertyType(f.getGenericType()))
                    result.put(name, property.withBackingField(f));
            }
        }
        failingProperties.forEach(x -> result.remove(x));
        return result;
    }

    static public PropertyInfo getPropertyInfo(Class<?> type, String name) {
        return tryGetPropertyInfo(type, name)
                .orElseThrow(() -> new RuntimeException("no property named " + name + " found on class " + type));
    }

    public static Optional<PropertyInfo> tryGetPropertyInfo(Class<?> type, String name) {
        return Optional.ofNullable(getPropertyInfoMap(type).get(name));
    }

    private static Map<Class<?>, Map<String, PropertyInfo>> propertyInfoMapCache = new ConcurrentHashMap<>();

    /**
     * Return a map containing all property infos for a type. Inheritance is
     * taken into account.
     */
    static public Map<String, PropertyInfo> getPropertyInfoMap(Class<?> type) {
        if (type == null || type == Object.class)
            return Collections.emptyMap();

        // no synchronization required. In the worst case, a map will
        // be calculated multiple times
        Map<String, PropertyInfo> result = propertyInfoMapCache.get(type);
        if (result == null) {
            result = calculatePropertyInfoMap(type);
            propertyInfoMapCache.put(type, result);
        }
        return result;
    }

    private static Map<String, PropertyInfo> calculatePropertyInfoMap(Class<?> type) {
        Map<String, PropertyInfo> result = new HashMap<>();

        for (Class<?> cls : JavaC3.allSuperclassesReverse(type)) {
            if (Object.class.equals(cls))
                continue;
            for (PropertyDeclaration decl : getPropertyDeclarations(cls).values()) {
                PropertyInfo existingInfo = result.get(decl.getName());
                if (existingInfo == null) {
                    result.put(decl.getName(), decl.toInfo(type));
                } else {
                    result.put(decl.getName(), existingInfo.extendedWith(decl));
                }
            }
        }

        return result;
    }

    static public PropertyDeclaration getPropertyIntroduction(Class<?> type, String name) {
        return getPropertyIntroductionMap(type).get(name);
    }

    private static Map<Class<?>, Map<String, PropertyDeclaration>> propertyIntroductionMapCache = new ConcurrentHashMap<>();

    /**
     * Return the {@link PropertyDeclaration}s of the given type. For each
     * property, the property declaration which introduced the property is
     * returned.
     */
    static public Map<String, PropertyDeclaration> getPropertyIntroductionMap(Class<?> type) {
        Preconditions.checkNotNull(type, "type is null");
        return propertyIntroductionMapCache.computeIfAbsent(type, PropertyUtil::calculatePropertyIntroductionMap);
    }

    static private Map<String, PropertyDeclaration> calculatePropertyIntroductionMap(Class<?> type) {
        Map<String, PropertyDeclaration> result = new HashMap<>();

        for (Class<?> cls : Lists.reverse(Lists.newArrayList(JavaC3.allSuperclasses(type)))) {
            if (Object.class.equals(cls)) {
                continue;
            }
            for (PropertyDeclaration prop : getPropertyDeclarations(cls).values()) {
                result.putIfAbsent(prop.getName(), prop);
            }
        }

        return result;
    }

    static public PropertyPath toPath(MethodInvocationRecorder recorder) {
        return toPath(recorder.getInvocations());
    }

    static public PropertyPath toPath(List<MethodInvocation<Object>> invocations) {
        PropertyPath result = new PropertyPath();
        for (MethodInvocation<Object> invocation : invocations) {
            result.nodes.add(getPathNode(invocation));
        }
        return result;
    }

    static public <T> PropertyPath getPropertyPath(Class<T> type, Consumer<T> pathAccessor) {
        return getPropertyPath(TypeToken.of(type), pathAccessor);
    }

    static public <T> PropertyPath getPropertyPath(TypeToken<T> type, Consumer<T> pathAccessor) {
        MethodInvocationRecorder recorder = new MethodInvocationRecorder();
        pathAccessor.accept(recorder.getProxy(type));
        return toPath(recorder.getInvocations());
    }

    static public PropertyPathNode getPathNode(MethodInvocation<Object> invocation) {
        return tryGetAccessedProperty(invocation).<PropertyPathNode> map(p -> new PropertyPath.PropertyNode(p))
                .orElseGet(() -> new PropertyPath.MethodNode(invocation));

    }

    static public PropertyInfo getAccessedProperty(List<MethodInvocation<Object>> invocations) {
        MethodInvocation<Object> last = invocations.get(invocations.size() - 1);

        return getAccessedProperty(last);
    }

    static public Optional<PropertyInfo> tryGetAccessedProperty(MethodInvocation<Object> accessorInvocation) {
        PropertyAccessor accessor = getAccessor(accessorInvocation.getMethod());

        if (accessor == null)
            return Optional.empty();

        return tryGetPropertyInfo(accessorInvocation.getInstanceType().getRawType(), accessor.getName());

    }

    static public PropertyInfo getAccessedProperty(MethodInvocation<Object> accessorInvocation) {
        PropertyAccessor accessor = getAccessor(accessorInvocation.getMethod());

        if (accessor == null)
            throw new RuntimeException("method " + accessorInvocation.getMethod() + " is no property accessor");

        PropertyInfo info = getPropertyInfo(accessorInvocation.getInstanceType().getRawType(), accessor.getName());

        return info;
    }

    static public void clearCache() {
        propertyInfoMapCache.clear();
        propertyIntroductionMapCache.clear();
    }

}
