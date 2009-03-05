
package net.lshift.java.dispatch;

import java.lang.reflect.*;
import java.util.*;

import net.lshift.java.lang.Types;
import net.lshift.java.util.Lists;
import net.lshift.java.util.Transform;

/**
 * Generate an implementation of an interface which uses
 * dynamic dispatch to call the closest matching method
 * in a java class.
 * The first dispatch of a given signature is O(NM) while
 * subsequent dispatches are O(1). N = number of parameters and
 * M is the number of types in the linearization of each parameter type.
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
        if(args == null)
            return new Class<?>[0];
        
	Class<?> [] parameterTypes = procedure.getParameterTypes();
	Class<?> [] types = new Class[args.length];
	for(int i = 0; i != args.length; ++i) {
	    if(parameterTypes[i].isPrimitive()) {
	        // This is how I handle primitives: I work out from the
	        // procedure if the argument should be a primitive, and
	        // then convert the type appropriately.
		types[i] = Types.PRIMITIVE.get(args[i].getClass());
	    }
	    else if(args[i] == null) {
	        // We can work out the root of all the types by looking at the
	        // method signature. We consider null to be of that type, unless
	        // that type is Object, in which case we use Void, so you can
	        // dispatch on null.
	        // FIXME: have a magic Null<Foo> that we consider a sub-type of 
	        // Foo, where Foo is the root parameter type.
	        if(parameterTypes[i] == Object.class)
	            types[i] = Void.class;
	        else
	            types[i] = parameterTypes[i];
	    }
	    else {
		types[i] = args[i].getClass();
	    }
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
	return proxy(constraint, Lists.list(closure), null);
    }

    public static <T> T proxy(Class<? extends T> constraint, Iterable<Object> closure)
    {
        return proxy(constraint, closure, null);
    }

    /**
     * Generate a proxy for a Multiclass from several closures.
     * The order of the closures is important: if there are identical
     * methods in the closures, the method in the first closure in the
     * list will be chosen. This method effectively allows you to extend
     * proxy classes.
     * @see #MultiClass
     * @see ConditionalDelegate
     * @see Delegate
     * @param <T> the type of the proxy
     * @param constraint the interface the proxy will implement
     * @param closures the list if classes implementing the proxy.
     * @return
     */
    public static <T> T proxy(Class<? extends T> constraint, Object ... closures)
    {
        return proxy(constraint, Arrays.asList(closures), null);
    }

    /**
     * Alternate invocation method.
     * This lets you dynamically select the method in the
     * constraint you wish to select. This can be handy when
     * dealing with primitive types dynamically.
     */
    @SuppressWarnings("unchecked")
    public static <X extends Object>  Object invoke(
        Class<X> constraint,
        Object closure,
        String methodName,
        Object [] parameters,
        Class<?> [] parameterTypes)
    throws NoSuchMethodException, 
        java.lang.IllegalAccessException,
        InvocationTargetException
    {
	final MultiClass genclass = 
	    dispatcher(constraint, 
	        Collections.singletonList((Class<Object>)closure.getClass()));

	Method template = constraint.getMethod(methodName, parameterTypes);
	ClosureMethod method = genclass.method(template, parameters);
	return method.method.invoke(closure, parameters);
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
			       Iterable<Object> closuresList,
			       final InvocationHandler fallback)
    {
	final MultiClass genclass = 
	    dispatcher(constraint, Lists.map(new Transform<Object, Class<Object>>() {
	        public Class<Object> apply(Object x) {
	            return (Class<Object>) x.getClass();
	        }
	    }, closuresList));

	final Map<Class<?>,Object> closures = new HashMap<Class<?>,Object>();
	for(Object closure: Lists.reverseIterable(Lists.asList(closuresList)))
	    closures.put(closure.getClass(), closure);

	return (T)Proxy.newProxyInstance
	    (constraint.getClassLoader(),
	     new Class [] { constraint },
	     new InvocationHandler() {
		 public Object invoke(Object proxy, Method procedure, Object [] args)
		     throws Throwable
		 {
		     ClosureMethod method = genclass.method(procedure, args);
		     Object closure = closures.get(method.declaredBy);
		     if(method == null) {
			 if(fallback != null)
			     return fallback.invoke(proxy, procedure, args);
			 else
			     throw new UnsupportedOperationException
				 ((new Signature
				   (procedure, types(procedure, args))).toString());
		     }
		     else {
			 try {
			     return method.method.invoke(closure, args);
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

    private static MultiClass dispatcher(
        Class<?> constraint, 
        List<Class<Object>> list)
    {
	DispatcherType key = new DispatcherType(constraint, list);
	MultiClass dispatcher = dispatchers.get(key);

	if(dispatcher == null) {
	    dispatcher = new MultiClass(constraint, list);
	    dispatchers.put(key, dispatcher);
	}

	return dispatcher;
    }
    
    // ------------------------------------------------------------------------


    private static class DispatcherType
    {
	private Class<?> constraint;
	private List<Class<Object>> closures;

	public DispatcherType(Class<?> constraint, List<Class<Object>> closures)
	{
	    this.constraint = constraint;
	    this.closures = closures;
	}

	public int hashCode()
	{
	    return constraint.hashCode() ^ closures.hashCode();
	}

	public boolean equals(Object o)
	{
	    DispatcherType other = (DispatcherType)o;
	    return ((constraint == other.constraint) && 
		    (closures.equals(other.closures)));
	}
    }


    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static Class<? extends Null<?>> nullClass(Class<? extends Null> x)
    {
        return (Class<? extends Null<?>>)x;
    }
    
    private interface Procedure
    {
        public ClosureMethod lookup(Signature signature);
    }
    
    private static class NoSuchProcedure
    implements Procedure
    {

        @Override
        public ClosureMethod lookup(Signature signature)
        {
            throw new NoSuchMethodError(signature.toString());
        }
        
    }
    
    private static class NoArgumentsProcedure
    implements Procedure
    {
        final ClosureMethod method;
        
        public NoArgumentsProcedure(ClosureMethod method)
        {
            this.method = method;
        }
        
        @Override
        public ClosureMethod lookup(Signature signature)
        {
            return method;
        }
        
    }
    
    private static class MultiProcedure
    implements Procedure
    {
	// Each Map in this array is for a parameter. It Maps from a type
	// to a set of methods with that parameter type at that position.
	private List<Map<Class<?>,Set<ClosureMethod>>> indexes;

	// I keep these handy, because they can't be constructed, and of
	// course I only need them if they are the parameter type
	// of one of the methods.
	private List<Class<? extends Null<?>>> nullTypes;

	/**
	 * Constructor
	 * @param procedure the procedure method - a method in the
	 *   contract interface
	 * @param methods All the methods in the multi-class.
	 */
        public MultiProcedure(Method procedure, Iterable<ClosureMethod> pmethods)
	{
	    indexes = new ArrayList<Map<Class<?>, Set<ClosureMethod>>>();
	    nullTypes = new ArrayList<Class<? extends Null<?>>>(
	                    procedure.getParameterTypes().length);
	    
	    for(@SuppressWarnings("unused")
	        Class<?> type: procedure.getParameterTypes())
	        indexes.add(new HashMap<Class<?>,Set<ClosureMethod>>());

	    for(ClosureMethod method: pmethods) {
		Class<?> [] parameters = method.method.getParameterTypes();
		for(int i = 0; i != parameters.length; ++i) {
		    // It should be the case that that if the ith parameter of
		    // procedure is T then any Null will be Null<T>: how else
		    // could the method be applicable?
		    if(Null.class.isAssignableFrom(parameters[i]))
		        nullTypes.set(i, nullClass(parameters[i].asSubclass(Null.class)));
		    
		    Set<ClosureMethod> s = indexes.get(i).get(parameters[i]);
		    if(s == null) {
			s = new HashSet<ClosureMethod>();
			indexes.get(i).put(parameters[i], s);
		    }
		    
		    s.add(method);
		}

	    }
	}

	private Set<ClosureMethod> methods(int position, Class<?> [] parameterTypes)
	    throws JavaC3.JavaC3Exception
	{
	    Class<?> paramterType = parameterTypes[position];
	    Map<Class<?>, Set<ClosureMethod>> index = indexes.get(position);
	    for(Class<?> c: JavaC3.allSuperclasses(paramterType)) {
		Set<ClosureMethod> methods = index.get(c);
		if(methods != null) {
		    methods = new HashSet<ClosureMethod>(methods);
		    if(parameterTypes.length != position + 1)
			methods.retainAll(methods(position + 1, parameterTypes));
		    if(!methods.isEmpty())
			return methods;
		}
	    }

	    return Collections.emptySet();
	}

	public ClosureMethod lookup(Signature signature)
	{
	    try {
		Set<ClosureMethod> methods = methods(0, signature.parameterTypes);
		if(methods.isEmpty()) {
		    for(int i = 0; i != indexes.size(); ++i)
			System.err.println(i + ": " + indexes.get(i));
		    throw new NoSuchMethodError(signature.toString());
		}
		
		return methods.iterator().next();
	    }
	    catch(JavaC3.JavaC3Exception e) {
		throw new AmbiguousMethodException(e.toString());
	    }
	}
	
	public List<Class<? extends Null<?>>> getNullTypes()
	{
	    return nullTypes;
	}
    }

    // ------------------------------------------------------------------------

    protected static boolean appliesTo(Method constraint, Class<?> [] params)
    {
	Class<?> [] cparams = constraint.getParameterTypes();
	boolean result = (params.length == cparams.length);
	for(int i = 0; result && i != params.length; ++i)
	    result = JavaC3.allSuperclasses(params[i]).contains(cparams[i]);
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
     * Get the set of closure methods applicable to this procedure
     */
    protected static Iterable<ClosureMethod> procedureMethods(
        Method constraint, 
        List<ClosureMethod> methods)
    {
	List<ClosureMethod> cmethods = new ArrayList<ClosureMethod>();
	for(ClosureMethod method: methods) {
	    if(appliesTo(constraint, method.method))
		cmethods.add(method);
	}

	return unique(constraint, cmethods);
    }

    /**
     * Reduce a list of methods to a list of methods with
     * unique signatures - the first item in the list wins.
     * This is the most obvious way to make combining classes with
     * matching methods deterministic. It might not be necessary,
     * but for now, I just want to be sure.
     * @param procedure
     * @param pmethods
     * @return
     */
    public static Iterable<ClosureMethod> unique(
        Method procedure,
        List<ClosureMethod> pmethods)
    {
        Map<Signature, ClosureMethod> bySignature = new HashMap<Signature, ClosureMethod>();
        pmethods.listIterator(pmethods.size());
        for(ClosureMethod pmethod: Lists.reverseIterable(pmethods))
            bySignature.put(
                new Signature(procedure, pmethod.method.getParameterTypes()), 
                pmethod);
        return bySignature.values();
    }


    // ------------------------------------------------------------------------
    
    /**
     * A method in a closure class.
     * We keep track of the closure, rather than calculate it later:
     * its faster, and I don't have to think about if it can always be
     * calculated :).
     */
    public static class ClosureMethod
    {
        public final Class<?> declaredBy;
        public final Method method;
        
        public ClosureMethod(Class<?> declaredBy, Method method)
        {
            this.declaredBy = declaredBy;
            this.method = method;
        }
        
        public boolean equals(Object o)
        {
            if(o.getClass().equals(getClass())) {
                ClosureMethod other = (ClosureMethod)o;
                return ((this.declaredBy.equals(other.declaredBy)) 
                        && (this.method.equals(other.method)));
            }
            
            return false;
        }
        
        public String toString()
        {
            return "[" + 
                new Signature(method) + 
                " in closure " + declaredBy.getName() + "]";
        }
        
        public int hashCode()
        {
            return toString().hashCode();
        }
    }

    // ------------------------------------------------------------------------

    /**
     * A 'multiclass' - a class where each method is a multi - method. 
     * There is a procedure for each method in the contract.
     */
    public static class MultiClass
    {
	Map<Signature, ClosureMethod> shortcuts = new HashMap<Signature, ClosureMethod>();
	Map<Method, Procedure> procedures = new HashMap<Method, Procedure>();

        protected MultiClass(
            Class<?> constraint, 
            List<Class<Object>> implementations)
        {
            Method [] procedures = constraint.getDeclaredMethods();
                for(int p = 0; p != procedures.length; ++p) {
                    List<ClosureMethod> methods = new ArrayList<ClosureMethod>();
                    for(final Class<?> implementation: implementations) {
                        methods.addAll(Lists.map(new Transform<Method, ClosureMethod>(){
                            public ClosureMethod apply(Method x) {
                                return new ClosureMethod(implementation, x);
                            }
                        }, Arrays.asList(implementation.getDeclaredMethods())));
                    this.procedures.put
                        (procedures[p], procedure(procedures[p], methods));
                }
            }
        }

	private Procedure procedure(Method method, List<ClosureMethod> methods)
        {
            Iterable<ClosureMethod> pmethods = procedureMethods(method, methods);
            if(!pmethods.iterator().hasNext()) {
                return new NoSuchProcedure();
            }
            else {
                if(method.getParameterTypes().length == 0)
                    return new NoArgumentsProcedure(pmethods.iterator().next());
                else
                    return new MultiProcedure(method, pmethods);
            }
        }

	protected final ClosureMethod method(Method procedureMethod, Object [] args)
	{
	    Signature signature = new Signature
		(procedureMethod, types(procedureMethod, args));
	    if(shortcuts.containsKey(signature)) {
		return shortcuts.get(signature);
	    }
	    else {
		Procedure procedure = procedures.get(procedureMethod);
		ClosureMethod method = procedure.lookup(signature);
		AccessibleObject.setAccessible(new AccessibleObject[] { method.method }, true);
		shortcuts.put(signature, method);
		return method;
	    }
	}
    }

}