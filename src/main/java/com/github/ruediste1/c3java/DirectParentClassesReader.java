package com.github.ruediste1.c3java;

import java.util.List;

/**
 * Reads direct parent classes of a type. Implementations of this interface can be
 * used together with {@link JavaC3} and determine the order in which the superclass
 * and the implemented interfaces of a type are used for the C3 linearization
 * algorithm. 
 * 
 * @author ruedi
 *
 */
public interface DirectParentClassesReader
{
    /**
     * Get the direct parent types
     * @param type the type to get the parent types of.
     */
    public List<Class<?>> directParentClasses
        (Class<?> type);
}