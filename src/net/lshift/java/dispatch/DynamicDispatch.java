
package net.lshift.java.dispatch;

import java.lang.reflect.*;
import java.util.*;

/**
 * Generate an implementation of an interface which uses
 * dynamic dispatch to call the closest matching method
 * in a java class.
 * The first dispatch of a given signature is O(N) while
 * subsequent dispatches are O(1). N = number of methods in closure.
 */
public class DynamicDispatch
{
    private static Map dispatchers = new HashMap();

    /**
     * Generate an implementation of constraint, using methods
     * with matching signatures in delegate.
     * The implemtation of each method in constraint is to call
     * the closest matching method in closure given the types
     * of the arguments actually passed.
     */
    public static Object proxy(Class constraint, final Object closure)
    {
	final DynamicDispatchClass genclass = 
	    dispatcher(constraint, closure.getClass());
	    
	return Proxy.newProxyInstance
	    (closure.getClass().getClassLoader(),
	     new Class [] { constraint },
	     new InvocationHandler() {
		 public Object invoke(Object proxy, Method method, Object [] args)
		     throws Throwable
		 {
		     return genclass.invoke(method, closure, args);
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

    private static class Signature
    {
	public Method procedure;
	public Class [] args;

	public Signature(Method procedure, Object [] args)
	{
	    this.procedure = procedure;
	    this.args = new Class[args.length];
	    for(int i = 0; i != args.length; ++i)
		this.args[i] = args[i].getClass();
	}

	public int hashCode()
	{
	    int hash = procedure.hashCode();
	    for(int i = 0; i < args.length; ++i)
		hash = hash ^ args[i].hashCode();
	    return hash;
	}

	public boolean equals(Object o)
	{
	    Signature other = (Signature)o;
	    boolean result = (this.procedure == other.procedure);
	    for(int i = 0; result && i != this.args.length; ++i)
		result = (this.args[i] == other.args[i]);
	    return result;
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
	// the ArrayList is important since I want to sort them later
	Set cmethods = new HashSet();
	for(int i = 0; i != methods.length; ++i) {
	    if(appliesTo(constraint, methods[i]))
		cmethods.add(methods[i]);
	}

	return cmethods;
    }


    /**
     * Order a set of methods by specificity
     */
    protected static List linearize(Set methods)
    {
	// TODO: Can I prove this always completes?
	// I think this is a way of writing a bubble sort.
	List linearized = new ArrayList(methods.size());
	// each iteration of this loop finds all the methods in the
	// set with no more specific methods the set, places them in
	// any order in the linearization, and removes them from methods,
	// until methods is empty.
	while(!methods.isEmpty()) {
	    int start = linearized.size();
	    Iterator i = methods.iterator();
	    loop: while(i.hasNext()) {
		Method method = (Method)i.next();
		Iterator j = methods.iterator();
		while(j.hasNext()) {
		    Method testmethod = (Method)j.next();
		    if(testmethod != method && appliesTo(method, testmethod))
			continue loop;
		}

		linearized.add(method);
	    }

	    methods.removeAll(linearized.subList(start, linearized.size()));
	}

	return linearized;
    }

    private static Method lookup(List linearized, Signature signature)
    {
	Iterator i = linearized.iterator();
	while(i.hasNext()) {
	    Method method = (Method)i.next();
	    if(appliesTo(method, signature.args))
		return method;
	}

	throw new NoSuchMethodError();
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
		    (procedures[p], linearize
		     (procedureMethods(procedures[p], methods)));
	    }
	}

	private final Method method(Method procedure, Object [] args)
	{
	    Signature signature = new Signature(procedure, args);
	    Method method = (Method)shortcuts.get(signature);
	    if(method == null) {
		List linearized = (List)procedures.get(procedure);
		method = lookup(linearized, signature);
		shortcuts.put(signature, method);
	    }

	    return method;
	}

	protected Object invoke(Method procedure, Object closure, Object [] args)
	    throws Throwable
	{
	    Method method = method(procedure, args);
	    return method.invoke(closure, args);
	}
    }

}