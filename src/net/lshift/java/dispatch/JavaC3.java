
package net.lshift.java.dispatch;

import java.lang.reflect.Array;
import java.io.Serializable;
import java.util.*;

public class JavaC3
{
    private static Map linearizations = new WeakHashMap();
    private static final Map PRIMITIVE_SUPERCLASSES;
    static {
	Map superclasses = new HashMap();
	final List none =  Collections.EMPTY_LIST;
	// does this make any sense? It does from a 'widening conversion'
	// point of vview in java. 
	superclasses.put(Void.TYPE, none);
	superclasses.put(Boolean.TYPE, none);
	superclasses.put(Double.TYPE, Collections.singletonList(Float.TYPE));
	superclasses.put(Float.TYPE, Collections.singletonList(Long.TYPE));
	superclasses.put(Long.TYPE, Collections.singletonList(Integer.TYPE));
	superclasses.put
	    (Integer.TYPE, Arrays.asList(new Class [] { Short.TYPE, Character.TYPE }));
	superclasses.put(Short.TYPE, Collections.singletonList(Byte.TYPE));
	superclasses.put(Byte.TYPE, none);
	superclasses.put(Character.TYPE, none);
	PRIMITIVE_SUPERCLASSES = Collections.unmodifiableMap(superclasses);
    }

    /**
     * Thrown when its not possible to linearize
     * all superclasses.
     */
    public static class JavaC3Exception
	extends Exception
    { 
	private List reversedPartialResult;
	private List remainingInputs;

	protected JavaC3Exception
	    (List reversedPartialResult, List remainingInputs)
	{
	    super("inconsistent precedence");
	    this.reversedPartialResult = reversedPartialResult;
	    this.remainingInputs = remainingInputs;
	}


	/**
	 * Gets the value of reversedPartialResult
	 * This is really for expert use only. Its
	 * the value of reversedPartialResult at the
	 * point the linearization failed.
	 * @return the value of reversedPartialResult
	 */
	public List getReversedPartialResult()  {
	    return this.reversedPartialResult;
	}

	/**
	 * Gets the value of remainingInputs
	 * This is really for expert use only. Its
	 * the value of remaining inputs at the point
	 * the linearization failed.
	 * @return the value of remainingInputs
	 */
	public List getRemainingInputs()  {
	    return this.remainingInputs;
	}

	public String toString()
	{
	    return "inconsistent precendence: " + 
		reversedPartialResult + 
		" " + remainingInputs;
	}
    }

    /**
     * Get the direct superclasses of a class.
     * This is complicated, and possibly evil: in dylan, any class
     * which does not have another direct superclass extends Object.
     * In java, interfaces do not extend Object, or any equivalent.
     * This implementation makes an interface with no super interfaces
     * extend Object. Further, in classes which extend object, and
     * implement 1 or more interfaces, Object is last in the list
     * of direct superclasses, while any other super class comes first.
     * This seems a bit arbitrary, but works, and gives sensible
     * results in most cases.
     */
    protected static List directSuperclasses(Class c)
    {
	if(c.isPrimitive()) {
	    // I am not really sure that superclasses of primitive
	    // types are actually useful for anything
	    return new LinkedList((List)PRIMITIVE_SUPERCLASSES.get(c));
	}
	else if(c.isArray()) {
	    return arrayDirectSuperclasses(0, c);
	}
	else {
	    Class [] interfaces = c.getInterfaces();
	    Class superclass = c.getSuperclass();

	    List classes = new LinkedList();
	    if(superclass == Object.class) {
		classes.addAll(Arrays.asList(interfaces));
		classes.add(Object.class);
	    }
	    else if(superclass == null) {
		classes.addAll(Arrays.asList(interfaces));
		if(classes.isEmpty() && c != Object.class)
		    classes.add(Object.class);
	    }
	    else {
		classes.add(superclass);
		classes.addAll(Arrays.asList(interfaces));
	    }

	    return classes;
	}
    }

    /* the following is translated from sisc 1.8.5 s2j/reflection.scm
       java-array-superclasses. */

    protected static List ARRAY_SUPERCLASSES = Arrays.asList
	(new Class [] { Serializable.class, Cloneable.class, Object.class });

    protected static List arrayDirectSuperclasses(int level, Class c)
    {
	List classes;

	if(c.isArray()) {
	    classes = arrayDirectSuperclasses(level + 1, c.getComponentType());
	}
	else {
	    List componentSuperclasses = directSuperclasses(c);
	    if(componentSuperclasses.isEmpty() && !c.isInterface()) {
		classes = (level == 1) ? new LinkedList(ARRAY_SUPERCLASSES) :
		    makeArrayClasses(ARRAY_SUPERCLASSES, level - 1);
	    }
	    else {
		classes = makeArrayClasses(componentSuperclasses, level);
	    }
	}

	return classes;
    }

    // this compensates for the lack of map
    public static List makeArrayClasses(List classes, int dims)
    {
	Iterator i = classes.iterator();
	LinkedList arrayClasses = new LinkedList();
	while(i.hasNext())
	    arrayClasses.add(makeArrayClass((Class)i.next(), dims));
	return arrayClasses;
    }

    /* copied from sisc 1.8.5 s2j/Utils.java */
    public static Class makeArrayClass(Class c, int dims) 
    {
        return Array.newInstance(c, new int[dims]).getClass();
    }

    private static class MergeLists
    {
	private LinkedList reversedPartialResult;
	private List remainingInputs;

	public MergeLists(Class c, List inputs)
	    throws JavaC3Exception
	{
	    reversedPartialResult = new LinkedList();
	    this.reversedPartialResult.add(c);
	    remainingInputs = inputs;
	    mergeLists();
	}

	private Class candidate(Class c)
	{
	    Iterator inputs = remainingInputs.iterator();
	    boolean anyTail = false;
	    while(!anyTail && inputs.hasNext())
		anyTail = ((List)inputs.next()).lastIndexOf(c) > 0;
	    return anyTail ? null : c;
	}

	private Class candidateAtHead(List l)
	{
	    return l.isEmpty() ? null : candidate((Class)l.get(0));
	}

	private Class anyCandidateAtHead()
	{
	    Iterator i = remainingInputs.iterator();
	    Class any = null;
	    while(any == null && i.hasNext())
		any = candidateAtHead((List)i.next());
	    return any;
	}

	private boolean remainingInputs()
	{
	    Iterator inputs = remainingInputs.iterator();
	    boolean remaining = false;
	    while(!remaining && inputs.hasNext())
		remaining = !((List)inputs.next()).isEmpty();
	    return remaining;
	}

	private void mergeLists()
	    throws JavaC3Exception
	{
	    while(remainingInputs()) {

		Class next = anyCandidateAtHead();

		if(next != null) {
		    Iterator i = remainingInputs.iterator();
		    while(i.hasNext()) {
			List list = (List)i.next();
			if(list.indexOf(next) == 0) list.remove(0);
		    }

		    reversedPartialResult.addFirst(next);
		}
		else {
		    throw new JavaC3Exception(reversedPartialResult, remainingInputs);
		}
	    }

	    Collections.reverse(reversedPartialResult);
	}

	public List getLinearization()
	{
	    return reversedPartialResult;
	}
    }

    protected static List computeClassLinearization(Class c)
	throws JavaC3Exception
    {
	List cDirectSuperclasses = directSuperclasses(c);
	List inputs = new ArrayList(cDirectSuperclasses.size()+1);
	Iterator i = cDirectSuperclasses.iterator();
	// the lists in input are consumed, so they must be cloned
	while(i.hasNext()) {
	    List allSuperclasses = new LinkedList();
	    allSuperclasses.addAll(allSuperclasses((Class)i.next()));
	    inputs.add(allSuperclasses);
	}

	inputs.add(cDirectSuperclasses);

	MergeLists ml = new MergeLists(c, inputs);
	return Collections.unmodifiableList(ml.getLinearization());
    }

    public static List allSuperclasses(Class c)
	throws JavaC3Exception
    {
	List linearization = (List)linearizations.get(c);
	if(linearization == null) {
	    linearization = computeClassLinearization(c);
	    linearizations.put(c, linearization);
	}

	return linearization;
    }

}