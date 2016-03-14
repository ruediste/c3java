package com.github.ruediste.c3java.properties;

import java.lang.reflect.Method;

/**
 * An accessor of a property. Accessors are combined to {@link PropertyInfo}s
 */
public class PropertyAccessor {

    public enum AccessorType {
        GETTER, SETTER
    }

    private final String name;
    private final AccessorType type;
    private final Method method;
    private final Class<?> propertyType;

    public PropertyAccessor(String name, AccessorType type, Method method, Class<?> propertyType) {
        super();
        this.name = name;
        this.type = type;
        this.method = method;
        this.propertyType = propertyType;
    }

    public String getName() {
        return name;
    }

    public AccessorType getType() {
        return type;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getPropertyType() {
        return propertyType;
    }

    @Override
    public String toString() {
        return "<" + type + ":" + method + ">";
    }
}
