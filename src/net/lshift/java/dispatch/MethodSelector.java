/**
 * 
 */
package net.lshift.java.dispatch;

import net.lshift.java.dispatch.DynamicDispatch.ClosureMethod;
import net.lshift.java.util.Transform;

/**
 * Extend dynamic dispatch by selecting a method by parameter value.
 * @see MethodSelectorMap
 */
public interface MethodSelector
extends Transform<Object [], ClosureMethod> { }