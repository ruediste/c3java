
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

	private Set methods(int position, Iterator parameterTypes)
	    throws JavaC3.JavaC3Exception
	{
	    Class paramterType = (Class)parameterTypes.next();
	    List linearization = JavaC3.allSuperclasses(paramterType);
	    Map index = indexes[position];
	    Iterator i = linearization.iterator();
	    while(i.hasNext()) {
		Set methods = (Set)index.get(i.next());
		if(methods != null) {
		    if(parameterTypes.hasNext())
			methods.removeAll(methods(position + 1, parameterTypes));
		    if(!methods.isEmpty())
			return methods;
		}
	    }

	    return Collections.EMPTY_SET;
	}

	private Method lookup(Signature signature)
	{
	    try {
		Set methods = methods(0, Arrays.asList(signature.args).iterator());
		if(methods.isEmpty())
		    throw new NoSuchMethodError();
		return (Method)methods.iterator().next();
	    }
	    catch(JavaC3.JavaC3Exception e) {
		throw new NoSuchMethodError(e.toString());
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

	private final Method method(Method procedureMethod, Object [] args)
	{
	    Signature signature = new Signature(procedureMethod, args);
	    Method method = (Method)shortcuts.get(signature);
	    if(method == null) {
		Procedure procedure = (Procedure)procedures.get(procedureMethod);
		method = procedure.lookup(signature);
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