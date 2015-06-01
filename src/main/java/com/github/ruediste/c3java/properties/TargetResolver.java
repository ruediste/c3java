package com.github.ruediste.c3java.properties;

/**
 * Resolves the target object a {@link PropertyHandle} operates on based on a
 * root object.
 */
public interface TargetResolver {
    Object getValue(Object root);
}
