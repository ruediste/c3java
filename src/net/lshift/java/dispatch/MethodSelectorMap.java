/**
 * 
 */
package net.lshift.java.dispatch;

import java.lang.reflect.Method;
import java.util.Set;

import net.lshift.java.dispatch.DynamicDispatch.ClosureMethod;

/**
 * Extend dynamic dispatch by selecting methods using parameter values.
 * This function takes a list of candidate ClosureMethods, and returns
 * a MethodSelector. This is so we can cache any work done inspecting
 * the candidate methods. The resulting ParameterValueFilter will return
 * a single method from the list of candidates, based on the parameter values.
 */
public interface MethodSelectorMap
{
    public MethodSelector getMethodSelector(
        Method procedure, 
        Set<ClosureMethod> methods);

}