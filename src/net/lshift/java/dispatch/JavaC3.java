
package net.lshift.java.dispatch;

import java.util.*;

public class JavaC3
{
    private static Map linearizations = new WeakHashMap();

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
     * java reflection API, its in the order of declaration.
     */
    protected static List directSuperclasses(Class c)
    {
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
	    if(c == Object.class)
		linearization = Collections.nCopies(0, null);
	    else
		linearization = computeClassLinearization(c);
	    linearizations.put(c, linearization);
	}

	System.out.println(c.getName() + ": " + linearization);

	return linearization;
    }

}