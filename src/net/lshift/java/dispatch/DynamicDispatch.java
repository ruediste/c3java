
package net.lshift.java.dispatch;

import java.lang.reflect.*;
import java.util.*;

import net.lshift.java.lang.Types;

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
    private static Map<DispatcherType,MultiClass> dispatchers = 
        Collections.synchronizedMap(new HashMap<DispatcherType,MultiClass>());

    private static Class<?> [] types(Method procedure, Object [] args)
    {
	Class<?> [] parameterTypes = procedure.getParameterTypes();
	Class<?> [] types = new Class[args.length];
	for(int i = 0; i != args.length; ++i) {
	    // this is how I handle primitives: I work out from the
	    // procedure if the argument should be a primitive, and
	    // then convert the type appropriately.
	    if(parameterTypes[i].isPrimitive())
		types[i] = Types.PRIMITIVE.get(args[i].getClass());
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
        private static final long serialVersionUID = 1L;

        public IllegalAccessException(String message)
	{
	    super(message);
	}
    }

    public static class AmbiguousMethodException
	extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        public AmbiguousMethodException(String message)
	{
	    super(message);
	}
    }

    public static <T> T proxy(Class<? extends T> constraint, Object closure)
    {
	return proxy(constraint, closure, null);
    }

    /**
     * Alternate invocation method.
     * This lets you dynamically select the method in the
     * constraint you wish to select. This can be handy when
     * dealing with primitive types dynamically.
     */
    public static Object invoke(Class<?> constraint,
				Object closure,
				String methodName,
				Object [] parameters,
				Class<?> [] parameterTypes)
	throws NoSuchMethodException, java.lang.IllegalAccessException,
	       InvocationTargetException
    {
	final MultiClass genclass = 
	    dispatcher(constraint, closure.getClass());

	Method template = constraint.getMethod(methodName, parameterTypes);
	Method method = genclass.method(template, parameters);
	return method.invoke(closure, parameters);
    }

    /**
     * Generate an implementation of constraint, using methods
     * with matching signatures in delegate.
     * The implementation of each method in constraint is to call
     * the closest matching method in closure given the types
     * of the arguments actually passed.
     */
    @SuppressWarnings("unchecked")
    public static <T> T proxy(final Class<? extends T> constraint,
			       final Object closure,
			       final InvocationHandler fallback)
    {
	final MultiClass genclass = 
	    dispatcher(constraint, closure.getClass());

	return (T)Proxy.newProxyInstance
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

    private static MultiClass dispatcher(Class<?> constraint, Class<? extends Object> closure)
    {
	DispatcherType key = new DispatcherType(constraint, closure);
	MultiClass dispatcher = 
	    dispatchers.get(key);

	if(dispatcher == null) {
	    dispatcher = new MultiClass(constraint, closure);
	    dispatchers.put(key, dispatcher);
	}

	return dispatcher;
    }

    // ------------------------------------------------------------------------


    private static class DispatcherType
    {
	private Class<?> constraint;
	private Class<? extends Object> closure;

	public DispatcherType(Class<?> constraint, Class<? extends Object> closure)
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
	private List<Map<Class<?>,Set<Method>>> indexes;

	public Procedure(Method procedure, Method [] methods)
	{
	    indexes = new ArrayList<Map<Class<?>, Set<Method>>>();
	    for(@SuppressWarnings("unused")
	        Class<?> type: procedure.getParameterTypes())
	        indexes.add(new HashMap<Class<?>,Set<Method>>());

	    Set<Method> pmethods = procedureMethods(procedure, methods);
	    Iterator<Method> pmi = pmethods.iterator();

	    while(pmi.hasNext()) {
		Method method = (Method)pmi.next();
		Class<?> [] parameters = method.getParameterTypes();
		for(int i = 0; i != parameters.length; ++i) {
		    Set<Method> s = indexes.get(i).get(parameters[i]);
		    if(s == null) {
			s = new HashSet<Method>();
			indexes.get(i).put(parameters[i], s);
		    }
		    s.add(method);
		}

	    }

	}

	private Set<Method> methods(int position, Class<?> [] parameterTypes)
	    throws JavaC3.JavaC3Exception
	{
	    Class<?> paramterType = parameterTypes[position];
	    Map<Class<?>, Set<Method>> index = indexes.get(position);
	    for(Class<?> c: JavaC3.allSuperclasses(paramterType)) {
		Set<Method> methods = index.get(c);
		if(methods != null) {
		    methods = new HashSet<Method>(methods);
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
		Set<Method> methods = methods(0, signature.parameterTypes);
		if(methods.isEmpty()) {
		    for(int i = 0; i != indexes.size(); ++i)
			System.err.println(i + ": " + indexes.get(i));
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

    protected static boolean appliesTo(Method constraint, Class<?> [] params)
    {
	Class<?> [] cparams = constraint.getParameterTypes();
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
    protected static Set<Method> procedureMethods(Method constraint, Method [] methods)
    {
	Set<Method> cmethods = new HashSet<Method>();
	for(int i = 0; i != methods.length; ++i) {
	    if(appliesTo(constraint, methods[i]))
		cmethods.add(methods[i]);
	}

	return cmethods;
    }

    // ------------------------------------------------------------------------

    public static class MultiClass
    {
	Map<Signature, Method> shortcuts = new HashMap<Signature, Method>();
	Map<Method, Procedure> procedures = new HashMap<Method, Procedure>();

        protected MultiClass(Class<?> constraint, Class<? extends Object> implementation)
        {
            Method [] procedures = constraint.getDeclaredMethods();
            Method [] methods = implementation.getDeclaredMethods();
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
		return shortcuts.get(signature);
	    }
	    else {
		Procedure procedure = procedures.get(procedureMethod);
		Method method = procedure.lookup(signature);
		AccessibleObject.setAccessible(new AccessibleObject[] { method }, true);
		shortcuts.put(signature, method);
		return method;
	    }
	}

 
    }

}