
package net.lshift.java.dispatch;

import static net.lshift.java.util.Lists.filter;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lshift.java.dispatch.JavaC3.JavaC3Exception;
import net.lshift.java.lang.Types;
import net.lshift.java.util.Lists;
import net.lshift.java.util.Predicate;
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
    public static final MethodFilterMap ANONYMOUS_METHODS = 
        new MethodFilterMap() {
                public MethodFilter apply(final Method procedure) {
                    return new MethodFilter() {
                        public Boolean apply(ClosureMethod x) {
                            return validateAppliesTo(procedure, x.method);
                        }
                    };
                }
            };

    public static final MethodFilterMap NAMED_METHODS = 
        new MethodFilterMap() {
                public MethodFilter apply(final Method procedure) {
                    return new MethodFilter() {
                        public Boolean apply(ClosureMethod x) {
                            return defaultAppliesTo(procedure, x.method);
                        }
                    };
                }
            };

    public static final InvocationHandler DEFAULT_FALLBACK = new InvocationHandler() {

        @Override
        public Object invoke(Object target, Method procedure, Object[] args)
            throws Throwable
        {
            throw new UnsupportedOperationException
            ((new Signature
              (procedure, types(procedure, args))).toString());
        }
        
    };
            
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
	return proxy(constraint, 
	    NAMED_METHODS, 
	    ANY_VALUES, 
	    Lists.list(closure), 
	    DEFAULT_FALLBACK);
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
    public static <T> T proxy(Class<? extends T> constraint, Iterable<Object> closure)
    {
        return proxy(constraint, NAMED_METHODS, ANY_VALUES, 
            closure, 
            DEFAULT_FALLBACK);
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
	        Collections.singletonList((Class<Object>)closure.getClass()),
	        NAMED_METHODS, ANY_VALUES);

	Method template = constraint.getMethod(methodName, parameterTypes);
	ClosureMethod method = genclass.method(template, parameters).apply(parameters);
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
			       MethodFilterMap appliesTo,
			       MethodSelectorMap valueFilterFactory,
			       Iterable<Object> closuresList,
			       final InvocationHandler fallback)
    {
	final MultiClass genclass = 
	    dispatcher(constraint, Lists.map(new Transform<Object, Class<Object>>() {
	        public Class<Object> apply(Object x) {
	            return (Class<Object>) x.getClass();
	        }
	    }, closuresList), appliesTo, valueFilterFactory);

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
		     ClosureMethod method = genclass.method(procedure, args).apply(args);
		     if(method == null) {
		         return fallback.invoke(proxy, procedure, args);
		     }
		     else {
		         Object closure = closures.get(method.declaredBy.implementation);
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
        List<Class<Object>> list,
        MethodFilterMap appliesTo,
        MethodSelectorMap valueFilterFactory)
    {
        DispatcherType key = new DispatcherType(constraint, list, appliesTo, valueFilterFactory);
	MultiClass dispatcher = dispatchers.get(key);

	if(dispatcher == null) {
            dispatcher = new MultiClass(constraint, 
	        appliesTo, 
	        valueFilterFactory,
	        list );
	    dispatchers.put(key, dispatcher);
	}

	return dispatcher;
    }
    
    // ------------------------------------------------------------------------


    private static class DispatcherType
    {
	private final Class<?> constraint;
	private final List<Class<Object>> closures;
	private final MethodFilterMap appliesTo;
	private final MethodSelectorMap valueFilterFactory;
        

	public DispatcherType(
                          Class<?> constraint,
                          List<Class<Object>> closures,
                          MethodFilterMap appliesTo,
                          MethodSelectorMap valueFilterFactory)
        {
            super();
            this.constraint = constraint;
            this.closures = closures;
            this.appliesTo = appliesTo;
            this.valueFilterFactory = valueFilterFactory;
        }

        public int hashCode()
	{
	    return constraint.hashCode() ^ closures.hashCode();
	}

	public boolean equals(Object o)
	{
	    DispatcherType other = (DispatcherType)o;
	    return ((constraint == other.constraint) && 
		    (closures.equals(other.closures)) &&
		    (this.appliesTo.equals(other.appliesTo)) &&
		    (this.valueFilterFactory.equals(other.valueFilterFactory)));
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
        public Set<ClosureMethod> lookup(Signature signature);
    }
    
    private static class NoSuchProcedure
    implements Procedure
    {

        @Override
        public Set<ClosureMethod> lookup(Signature signature)
        {
            throw new NoSuchMethodError(signature.toString());
        }
        
    }
    
    private static class NoArgumentsProcedure
    implements Procedure
    {
        final Set<ClosureMethod> method;
        
        public NoArgumentsProcedure(ClosureMethod method)
        {
            this.method = Collections.singleton(method);
        }
        
        @Override
        public Set<ClosureMethod> lookup(Signature signature)
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
	    /*
	         FIXME: This is nigh unreadable
	         
	         The idea is that a method is only more specific than another
	         if for every parameter it is as specific, or more specific,
	         and that its more specific for one or more parameters.
	         
	         We are really getting the set of methods thats most specific for
	         each parameter, and then then intersecting these sets. An
	         unambiguous result is a set containing one method.
	         
	         Notice that if you use one java class, and only methods with
	         the same name, that an unabiguous result is the only one
	         possible.
	         
	         I've now added support for ignoring the method name, which
	         makes it possible to have methods with matching signatures,
	         and I've removed the override mechanism that applies to
	         lists of closures.
	         
	         Thats deliberately allows for multiple results, because I
	         want to allow for a separate resolver to filter the list.
	     */
	    
	    Class<?> parameterType = parameterTypes[position];
	    Map<Class<?>, Set<ClosureMethod>> index = indexes.get(position);
	    // allSuperclasses returns all the superclasses for the parameter
	    // in order of specificity. I'm going to iterate through
	    // that list, looking for matching methods
	    for(Class<?> c: JavaC3.allSuperclasses(parameterType)) {
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

	public Set<ClosureMethod> lookup(Signature signature)
	{
	    try {
		Set<ClosureMethod> methods = methods(signature.parameterTypes);
		if(methods.isEmpty()) {
		    for(int i = 0; i != indexes.size(); ++i)
			System.err.println(i + ": " + indexes.get(i));
		    // FIXME: this might be ambiguous, rather than no
		    // such method. We should work it out and report 
		    // appropriately.
		    throw new NoSuchMethodError(signature.toString());
		}

		return methods;
	    }
	    catch(JavaC3.JavaC3Exception e) {
		throw new AmbiguousMethodException(e.toString());
	    }
	}

        public Set<ClosureMethod> methods(Class<?> [] parameterTypes)
            throws JavaC3Exception
        {
            return methods(0, parameterTypes);
        }
	

    }

    // ------------------------------------------------------------------------

    public static boolean appliesTo(Method constraint, Class<?> [] params)
    {
	Class<?> [] cparams = constraint.getParameterTypes();
	boolean result = (params.length == cparams.length);
	for(int i = 0; result && i != params.length; ++i)
	    result = JavaC3.allSuperclasses(params[i]).contains(cparams[i]);
	return result;
    }

    /**
     * Default determine if method is applicable to a procedure.
     * This default implementation checks that the method names match
     * exactly.
     */
    public static boolean defaultAppliesTo(Method procedure, Method method)
    {
	return 
	    (procedure.getName().equals(method.getName()) &&
	     validateAppliesTo(procedure, method));
    }

    /**
     * All methods must satisfy this to be applicable.
     * For a method to apply to a procedure, the number and type of arguments
     * and the return types must be assignable, and it must be public.
     * @param procedure the procedure/multi-method
     * @param method the method
     * @return true if the method applies to the procedure
     */
    private static boolean validateAppliesTo(Method procedure, Method method)
    {
        return (appliesTo(procedure, method.getParameterTypes()) &&
	     procedure.getReturnType()
	     .isAssignableFrom(method.getReturnType()) &&
	     (method.getModifiers() & Modifier.PUBLIC) != 0);
    }



    /**
     * Reduce a list of methods to a list of methods with
     * unique signatures - the first item in the list wins.
     * This is the most obvious way to make it possible to
     * override a method from one class (closure) with a method
     * from another. Otherwise the method resolver would consider
     * the result ambiguous.
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
    
    public static class ClosureClass
    implements Comparable<ClosureClass>
    {
        public final int rank;
        public final Class<?> implementation;
        
        public ClosureClass(int rank, Class<?> implementation)
        {
            this.rank = rank;
            this.implementation = implementation;
        }

        public int compareTo(ClosureClass other)
        {
            return this.rank - other.rank;
        }
        
        public int hashCode()
        {
            return implementation.hashCode();
        }
        
        public String toString()
        {
            return "[" + implementation.getCanonicalName() + " rank " + rank + "]";
        }
    }
    
    /**
     * A method in a closure class.
     * We keep track of the closure, rather than calculate it later:
     * its faster, and I don't have to think about if it can always be
     * calculated :).
     */
    public static class ClosureMethod
    {
        public final ClosureClass declaredBy;
        public final Method method;
        
        public ClosureMethod(ClosureClass declaredBy, Method method)
        {
            if(declaredBy == null && method == null)
                throw new IllegalArgumentException();
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
                " in closure " + declaredBy + "]";
        }
        
        public int hashCode()
        {
            return toString().hashCode();
        }
    }

    // ------------------------------------------------------------------------

    protected static final MethodSelector INVALID_VALUE_FILTER = new MethodSelector() {
        public ClosureMethod apply(Object[] x) {
            throw new UnsupportedOperationException("No methods match this value");
        }
    };

    protected static final MethodSelector AMBIGUOUS_VALUE_FILTER = new MethodSelector() {
        public ClosureMethod apply(Object[] x) {
            throw new UnsupportedOperationException("Multiple methods match these parameters");
        }
    };

    public static MethodSelectorMap ANY_VALUES =
        new MethodSelectorMap() {
        public MethodSelector getMethodSelector(Method procedure, Set<ClosureMethod> methods) {
            if(methods.isEmpty()) {
                return INVALID_VALUE_FILTER;
            }

            if(methods.size() > 1) {
                // Sort the methods by closure rank
                List<ClosureMethod> sortedByClosure = new ArrayList<ClosureMethod>(methods);
                Collections.sort(sortedByClosure, new Comparator<ClosureMethod>() {
                    public int compare(ClosureMethod a, ClosureMethod b) {
                        return a.declaredBy.rank - b.declaredBy.rank;
                    }
                });
                
                // Get the first method by this ordering. We are happy if the
                // duplicates are overrides - that is, they are in different
                // closures.
                Iterator<ClosureMethod> methodsIterator = sortedByClosure.iterator();
                final ClosureMethod method = methodsIterator.next();
                ClosureMethod followedBy = methodsIterator.next();
                if(method.declaredBy.equals(followedBy.declaredBy)) {
                    return AMBIGUOUS_VALUE_FILTER;                        
                }

                return singleMethodValueFilter(method);
            }
            else {
                final ClosureMethod method = methods.iterator().next();
                return singleMethodValueFilter(method);
            }
        }

        private MethodSelector singleMethodValueFilter(
            final ClosureMethod method)
        {
            return new MethodSelector() {
                public ClosureMethod apply(Object[] parameters) {
                    return method;
                }
            };
        }
    };
    

    /**
     * A 'multiclass' - a class where each method is a multi - method. 
     * There is a procedure for each method in the contract.
     */
    public static class MultiClass
    {
	Map<Signature, MethodSelector> shortcuts = new HashMap<Signature, MethodSelector>();
	Map<Method, Procedure> procedures = new HashMap<Method, Procedure>();
	final MethodFilterMap signatureAppliesTo;
	final MethodSelectorMap methodSelectorMap;
	

        protected MultiClass(
            Class<?> constraint,
            MethodFilterMap appliesTo,
            MethodSelectorMap valueFilterFactory,
            List<Class<Object>> implementations)
        {
            this.signatureAppliesTo = appliesTo;
            this.methodSelectorMap = valueFilterFactory;
            Method [] procedures = constraint.getDeclaredMethods();
                for(int p = 0; p != procedures.length; ++p) {
                    List<ClosureMethod> methods = new ArrayList<ClosureMethod>();
                    int index = 0;
                    for(final Class<?> implementation: implementations) {
                        final ClosureClass closureClass = new ClosureClass(index++, implementation);
                        methods.addAll(Lists.map(new Transform<Method, ClosureMethod>(){
                            public ClosureMethod apply(Method method) {
                                return new ClosureMethod(closureClass, method);
                            }
                        }, Arrays.asList(implementation.getDeclaredMethods())));
                    this.procedures.put
                        (procedures[p], procedure(procedures[p], methods));
                }
            }
                
        }

	private Procedure procedure(Method method, List<ClosureMethod> methods)
        {
            Iterable<ClosureMethod> pmethods = filter(signatureAppliesTo.apply(method), methods);
            
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

	protected final MethodSelector method(Method procedureMethod, Object [] args)
	{
	    Signature signature = new Signature
		(procedureMethod, types(procedureMethod, args));
	    if(shortcuts.containsKey(signature)) {
		return shortcuts.get(signature);
	    }
	    else {
		Procedure procedure = procedures.get(procedureMethod);
		Set<ClosureMethod> methods = procedure.lookup(signature);
		for(ClosureMethod method: methods)
		    AccessibleObject.setAccessible(new AccessibleObject[] { method.method }, true);
		MethodSelector selector = 
		    methodSelectorMap.getMethodSelector(procedureMethod, methods);
		shortcuts.put(signature, selector);
		return selector;
	    }
	}
    }

}