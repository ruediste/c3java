package com.github.ruediste.c3java.properties;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.github.ruediste.c3java.invocationRecording.InvocationRecorder;
import com.github.ruediste.c3java.invocationRecording.MethodInvocation;
import com.github.ruediste.c3java.linearization.JavaC3;
import com.github.ruediste.c3java.properties.PropertyAccessor.AccessorType;
import com.github.ruediste.c3java.properties.PropertyPath.PropertyPathNode;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

public class PropertyUtil {

    /**
     * Helper class used to simplify repeated map.put calls
     */
    private static class Putter {

        private Map<String, PropertyDeclaration> map;
        private Class<?> type;

        private Putter(Map<String, PropertyDeclaration> map, Class<?> type) {
            this.map = map;
            this.type = type;
        }

        private void put(String key,
                Function<PropertyDeclaration, PropertyDeclaration> func) {
            PropertyDeclaration property = map.get(key);
            if (property == null) {
                property = new PropertyDeclaration(key, type);
            }
            map.put(key, func.apply(property));
        }
    }

    /**
     * Create an accessor from the given method, or null if the method is no
     * accessor
     */
    public PropertyAccessor getAccessor(Method method) {
        if (Modifier.isPrivate(method.getModifiers())
                || Modifier.isStatic(method.getModifiers())) {
            return null;
        }

        // check for getters
        if (method.getName().startsWith("get")) {
            String name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,
                    method.getName().substring("get".length()));

            // getFoo()
            if (method.getParameterCount() == 0) {
                return new PropertyAccessor(name, AccessorType.GETTER, method);
            }

        }

        // check for setters

        if (method.getName().startsWith("set")) {
            String name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,
                    method.getName().substring("set".length()));
            // setFoo()
            if (method.getParameterCount() == 1) {
                return new PropertyAccessor(name, AccessorType.SETTER, method);
            }

        }
        return null;
    }

    /**
     * Return all properties which are directly declared on the provided type
     */
    public Map<String, PropertyDeclaration> getDeclaredProperties(Class<?> type) {
        Map<String, PropertyDeclaration> result = new HashMap<>();

        // scan methods
        for (Method method : type.getDeclaredMethods()) {
            PropertyAccessor accessor = getAccessor(method);
            if (accessor == null)
                continue;

            Putter putter = new Putter(result, type);

            switch (accessor.getType()) {
            case GETTER:
                putter.put(accessor.getName(), p -> p.withGetter(method));
                break;
            case SETTER:
                putter.put(accessor.getName(), p -> p.withSetter(method));
                break;
            default:
                throw new RuntimeException("should not happen");
            }
        }
        // fill backing fields
        for (Field f : type.getDeclaredFields()) {
            String name = f.getName();
            PropertyDeclaration property = result.get(name);
            if (property != null) {
                result.put(name, property.withBackingField(f));
            }
        }
        return result;
    }

    /**
     * Return all types the given type is assignable to. The returned set is
     * ordered.
     * 
     */
    public Set<Class<?>> getTypes(Class<?> type) {
        LinkedHashSet<Class<?>> result = new LinkedHashSet<>();
        fillTypes(type, result);
        return result;
    }

    private void fillTypes(Class<?> type, Set<Class<?>> result) {
        if (type == null)
            return;

        // reinsert
        result.remove(type);
        result.add(type);

        fillTypes(type.getSuperclass(), result);
        for (Class<?> t : type.getInterfaces()) {
            fillTypes(t, result);
        }
    }

    public PropertyInfo getPropertyInfo(Class<?> type, String name) {
        return getPropertyInfoMap(type).get(name);
    }

    public Map<String, PropertyInfo> getPropertyInfoMap(Class<?> type) {
        Map<String, PropertyInfo> result = new HashMap<>();
        if (type == null || type == Object.class)
            return result;

        // copy map from superclass
        result.putAll(getPropertyInfoMap(type.getSuperclass()));

        // merge with all interfaces
        for (Class<?> t : type.getInterfaces()) {
            Map<String, PropertyInfo> map = getPropertyInfoMap(t);
            for (PropertyInfo info : map.values()) {
                PropertyInfo existingInfo = result.get(info.getName());
                if (existingInfo == null)
                    result.put(info.getName(), info);
                else {
                    result.put(info.getName(), existingInfo.mergedWith(info));
                }
            }
        }

        // merge with declarations
        for (PropertyDeclaration decl : getDeclaredProperties(type).values()) {
            PropertyInfo existingInfo = result.get(decl.getName());
            if (existingInfo == null) {
                result.put(decl.getName(), decl.toInfo());
            }
        }
        return result;
    }

    public PropertyDeclaration getPropertyIntroduction(Class<?> type,
            String name) {
        return getPropertyIntroductionMap(type).get(name);
    }

    /**
     * Return the {@link PropertyDeclaration}s of the given type. For each
     * property, the property declaration which introduced the property is
     * returned.
     */
    public Map<String, PropertyDeclaration> getPropertyIntroductionMap(
            Class<?> type) {
        Map<String, PropertyDeclaration> result = new HashMap<>();

        for (Class<?> cls : Lists.reverse(Lists.newArrayList(JavaC3
                .allSuperclasses(type)))) {
            if (Object.class.equals(cls)) {
                continue;
            }
            for (PropertyDeclaration prop : getDeclaredProperties(cls).values()) {
                result.putIfAbsent(prop.getName(), prop);
            }
        }

        return result;
    }

    public PropertyPath toPath(List<MethodInvocation<Object>> invocations) {
        PropertyPath result = new PropertyPath();
        for (MethodInvocation<Object> invocation : invocations) {
            result.nodes.add(getPathNode(invocation));
        }
        return result;
    }

    public <T> PropertyPath getPropertyPath(Class<T> type,
            Consumer<T> pathAccessor) {
        return getPropertyPath(TypeToken.of(type), pathAccessor);
    }

    public <T> PropertyPath getPropertyPath(TypeToken<T> type,
            Consumer<T> pathAccessor) {
        InvocationRecorder recorder = new InvocationRecorder();
        pathAccessor.accept(recorder.getProxy(type));
        return toPath(recorder.getInvocations());
    }

    public PropertyPathNode getPathNode(MethodInvocation<Object> invocation) {
        return tryGetAccessedProperty(invocation).<PropertyPathNode> map(
                p -> new PropertyPath.PropertyNode(p)).orElseGet(
                () -> new PropertyPath.MethodNode(invocation));

    }

    public PropertyInfo getAccessedProperty(
            List<MethodInvocation<Object>> invocations) {
        MethodInvocation<Object> last = invocations.get(invocations.size() - 1);

        return getAccessedProperty(last);
    }

    public Optional<PropertyInfo> tryGetAccessedProperty(
            MethodInvocation<Object> accessorInvocation) {
        PropertyAccessor accessor = getAccessor(accessorInvocation.getMethod());

        if (accessor == null)
            return Optional.empty();

        PropertyInfo info = getPropertyInfo(accessorInvocation
                .getInstanceType().getRawType(), accessor.getName());

        if (info == null)
            return Optional.empty();
        return Optional.of(info);
    }

    public PropertyInfo getAccessedProperty(
            MethodInvocation<Object> accessorInvocation) {
        PropertyAccessor accessor = getAccessor(accessorInvocation.getMethod());

        if (accessor == null)
            throw new RuntimeException("method "
                    + accessorInvocation.getMethod()
                    + " is no property accessor");

        PropertyInfo info = getPropertyInfo(accessorInvocation
                .getInstanceType().getRawType(), accessor.getName());

        if (info == null)
            throw new RuntimeException("no property named "
                    + accessor.getName() + " found on "
                    + accessorInvocation.getInstanceType());
        return info;
    }
}
