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

    public PropertyAccessor(String name, AccessorType type, Method method) {
        super();
        this.name = name;
        this.type = type;
        this.method = method;
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

}
