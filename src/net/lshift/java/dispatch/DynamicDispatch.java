/*
     This file is part of the LShift Java Library.

    The LShift Java Library is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    The LShift Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with The LShift Java Library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.lshift.java.dispatch;

import java.lang.reflect.*;
import java.util.*;

/**
 * Generate an implementation of an interface which uses
 * dynamic dispatch to call the closest matching method
 * in a java class.
 * The first dispatch of a given signature is O(N^2) while
 * subsequent dispatches are O(1). N = number of parameters or
 * the number of types in the linearization of each paramter type.
 * Note, you can pass primitive types as arguments, but they
 * are ignored for the purposes of dispatch - for a method
 * to be a member in a procedure, the primitive parameters
 * must match exactly. Thats because java will always have
 * chosen a procedure based on the types, and then promoted
 * the arguments as required for it.
 */
public class DynamicDispatch
{
    private static Map dispatchers = Collections.synchronizedMap(new HashMap());

    private static final Map PRIMITIVES;
    static {
	Map primitives = new HashMap();
	primitives.put(Void.class, Void.TYPE);
	primitives.put(Boolean.class, Boolean.TYPE);
	primitives.put(Double.class, Double.TYPE);
	primitives.put(Float.class, Float.TYPE);
	primitives.put(Long.class, Long.TYPE);
	primitives.put(Integer.class, Integer.TYPE);
	primitives.put(Short.class, Short.TYPE);
	primitives.put(Byte.class, Byte.TYPE);
	primitives.put(Character.class, Character.TYPE);
	PRIMITIVES = Collections.unmodifiableMap(primitives);
    }

    private static Class [] types(Method procedure, Object [] args)
    {
	Class [] parameterTypes = procedure.getParameterTypes();
	Class [] types = new Class[args.length];
	for(int i = 0; i != args.length; ++i) {
	    // this is how I handle primitives: I work out from the
	    // procedure if the argument should be a primitive, and
	    // then convert the type appropriately.
	    if(parameterTypes[i].isPrimitive())
		types[i] = (Class)PRIMITIVES.get(args[i].getClass());
	    else
		types[i] = args[i].getClass();
	}

	return types;
    }

    /**
     * Illegal access exception
     * this is really a runtime version java.lang.IllegalAccessException,
     * thrown in response to catching java.lang.IllegalAccessException
     * when we invoke a method. This can occur as a result of
     * invoking any method on a dynamic dispatch proxy.
     * Note: this will not be thrown as a result of a private method
     * in your dynamic dispatch class - that results in a
     * NoSuchMethodError - because private methods are ignored. This
     * really only happens as a result of a security policy
     * preventing access to a method.
     */
    public static class IllegalAccessException
	extends RuntimeException
    {
	public IllegalAccessException(String message)
	{
	    super(message);
	}
    }

    public static class AmbiguousMethodException
	extends RuntimeException
    {
	public AmbiguousMethodException(String message)
	{
	    super(message);
	}
    }

    public static Object proxy(Class constraint, Object closure)
    {
	return proxy(constraint, closure, null);
    }

    /**
     * Alternate invocation method.
     * This lets you dynamically select the method in the
     * constraint you wish to select. This can be handy when
     * dealing with primitive types dynamically.
     */
    public static Object invoke(Class constraint,
				Object closure,
				String methodName,
				Object [] parameters,
				Class [] parameterTypes)
	throws NoSuchMethodException, java.lang.IllegalAccessException,
	       InvocationTargetException
    {
	final DynamicDispatchClass genclass = 
	    dispatcher(constraint, closure.getClass());

	Method template = constraint.getMethod(methodName, parameterTypes);
	Method method = genclass.method(template, parameters);
	return method.invoke(closure, parameters);
    }

    /**
     * Generate an implementation of constraint, using methods
     * with matching signatures in delegate.
     * The implemtation of each method in constraint is to call
     * the closest matching method in closure given the types
     * of the arguments actually passed.
     */
    public static Object proxy(final Class constraint,
			       final Object closure,
			       final InvocationHandler fallback)
    {
	final DynamicDispatchClass genclass = 
	    dispatcher(constraint, closure.getClass());
	    
	return Proxy.newProxyInstance
	    (closure.getClass().getClassLoader(),
	     new Class [] { constraint },
	     new InvocationHandler() {
		 public Object invoke(Object proxy, Method procedure, Object [] args)
		     throws Throwable
		 {
		     Method method = genclass.method(procedure, args);
		     if(method == null) {
			 if(fallback != null)
			     return fallback.invoke(proxy, method, args);
			 else
			     throw new UnsupportedOperationException
				 ((new Signature
				   (procedure, types(procedure, args))).toString());
		     }
		     else {
			 try {
			     return method.invoke(closure, args);
			 }
			 catch(InvocationTargetException e) {
			     throw e.getTargetException();
			 }
			 catch(IllegalAccessException e) {
			     /* This can happen because of a security policy 
				(it doesn't seem to be possible to provoke it 
				otherwise - see test) */
			     throw new DynamicDispatch.IllegalAccessException
				 (e.getMessage());
			 }
		     }
		 }
	     });
    }

    private static DynamicDispatchClass dispatcher(Class constraint, Class closure)
    {
	DispatcherType key = new DispatcherType(constraint, closure);
	DynamicDispatchClass dispatcher = 
	    (DynamicDispatchClass)dispatchers.get(key);

	if(dispatcher == null) {
	    dispatcher = new DynamicDispatchClass(constraint, closure);
	    dispatchers.put(key, dispatcher);
	}

	return dispatcher;
    }

    // ------------------------------------------------------------------------


    private static class DispatcherType
    {
	private Class constraint;
	private Class closure;

	public DispatcherType(Class constraint, Class closure)
	{
	    this.constraint = constraint;
	    this.closure = closure;
	}

	public int hashCode()
	{
	    return constraint.hashCode() ^ closure.hashCode();
	}

	public boolean equals(Object o)
	{
	    DispatcherType other = (DispatcherType)o;
	    return ((constraint == other.constraint) && 
		    (closure == other.closure));
	}
    }


    // ------------------------------------------------------------------------

    private static class Procedure
    {
	// Each Map in this array is for a parameter. It Maps from a type
	// to a set of methods with that parameter type at that position.
	private Map [] indexes;

	public Procedure(Method procedure, Method [] methods)
	{
	    indexes = new Map[procedure.getParameterTypes().length];
	    for(int i = 0; i != indexes.length; ++i)
		indexes[i] = new HashMap();

	    Set pmethods = procedureMethods(procedure, methods);
	    Iterator pmi = pmethods.iterator();

	    while(pmi.hasNext()) {
		Method method = (Method)pmi.next();
		Class [] parameters = method.getParameterTypes();
		for(int i = 0; i != parameters.length; ++i) {
		    Set s = (Set)indexes[i].get(parameters[i]);
		    if(s == null) {
			s = new HashSet();
			indexes[i].put(parameters[i], s);
		    }
		    s.add(method);
		}

	    }

	}

	private Set methods(int position, Class [] parameterTypes)
	    throws JavaC3.JavaC3Exception
	{
	    Class paramterType = parameterTypes[position];
	    List linearization = JavaC3.allSuperclasses(paramterType);
	    Map index = indexes[position];
	    Iterator i = linearization.iterator();
	    while(i.hasNext()) {
		Set methods = (Set)index.get(i.next());
		if(methods != null) {
		    methods = new HashSet(methods);
		    if(parameterTypes.length != position + 1)
			methods.retainAll(methods(position + 1, parameterTypes));
		    if(!methods.isEmpty())
			return methods;
		}
	    }

	    return Collections.EMPTY_SET;
	}

	private Method lookup(Signature signature)
	{
	    try {
		Set methods = methods(0, signature.parameterTypes);
		if(methods.isEmpty()) {
		    for(int i = 0; i != indexes.length; ++i)
			System.err.println(i + ": " + indexes[i]);
		    throw new NoSuchMethodError(signature.toString());
		}
		return (Method)methods.iterator().next();
	    }
	    catch(JavaC3.JavaC3Exception e) {
		throw new AmbiguousMethodException(e.toString());
	    }
	}

    }

    // ------------------------------------------------------------------------

    protected static boolean appliesTo(Method constraint, Class [] params)
    {
	Class[]  cparams = constraint.getParameterTypes();
	boolean result = (params.length == cparams.length);
	for(int i = 0; result && i != params.length; ++i)
	    result = (cparams[i].isAssignableFrom(params[i]));
	return result;
    }

    /**
     * Determine if method is applicable to a procedure.
     */
    protected static boolean appliesTo(Method procedure, Method method)
    {
	return 
	    (procedure.getName().equals(method.getName()) &&
	     appliesTo(procedure, method.getParameterTypes()) &&
	     procedure.getReturnType()
	     .isAssignableFrom(method.getReturnType()) &&
	     (method.getModifiers() & Modifier.PUBLIC) != 0);
    }

    /**
     * Get the set of methods applicable to this procedure
     */
    protected static Set procedureMethods(Method constraint, Method [] methods)
    {
	Set cmethods = new HashSet();
	for(int i = 0; i != methods.length; ++i) {
	    if(appliesTo(constraint, methods[i]))
		cmethods.add(methods[i]);
	}

	return cmethods;
    }

    // ------------------------------------------------------------------------

    private static class DynamicDispatchClass
    {
	Map shortcuts = new HashMap();
	Map procedures = new HashMap();

	protected DynamicDispatchClass(Class constraint, Class closure)
	{
	    Method [] procedures = constraint.getDeclaredMethods();
	    Method [] methods = closure.getDeclaredMethods();
	    for(int p = 0; p != procedures.length; ++p) {
		this.procedures.put
		    (procedures[p], new Procedure(procedures[p], methods));
	    }
	}

	protected final Method method(Method procedureMethod, Object [] args)
	{
	    Signature signature = new Signature
		(procedureMethod, types(procedureMethod, args));
	    if(shortcuts.containsKey(signature)) {
		return (Method)shortcuts.get(signature);
	    }
	    else {
		Procedure procedure = (Procedure)procedures.get(procedureMethod);
		Method method = procedure.lookup(signature);
		AccessibleObject.setAccessible(new AccessibleObject[] { method }, true);
		shortcuts.put(signature, method);
		return method;
	    }
	}
    }

}