package net.lshift.java.dispatch;

import java.lang.reflect.Method;


import net.lshift.java.util.Transform;

/**
 * 
 * Map from a multi-method to a method filter.
 * The method filter is a predicate that returns true for methods
 * in a closure that are applicable to the multi-method
 * @see DynamicDispatch#ANONYMOUS_METHODS
 * @see DynamicDispatch#NAMED_METHODS
 */
public interface MethodFilterMap
extends Transform<Method, MethodFilter>
{

}
