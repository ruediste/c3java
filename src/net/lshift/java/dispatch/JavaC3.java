
package net.lshift.java.dispatch;


import java.io.Serializable;
import java.util.*;

public class JavaC3
{
    public interface DirectSuperclasses
    {
	/**
	 * Get the super classes.
	 * @param type the type to get the superclasses of.
	 */
	public List directSuperclasses(Class type);
    }

    private static class LinearizationKey
    {
	public DirectSuperclasses directSuperclasses;
	public Class type;

	public  LinearizationKey(DirectSuperclasses directSuperclasses, Class type)
	{
	    this.directSuperclasses = directSuperclasses;
	    this.type = type;
	}

	public boolean equals(Object o)
	{
	    if(o == null) {
		// you wouldn't think this could happen, but there
		// is no way to stop garbage collection from hapenning
		// on linearizations during a get(), or put(), so we
		// must check this is not null.
		return false;
	    }
	    else {
		LinearizationKey other = (LinearizationKey)o;
		return type.equals(other.type) &&
		    directSuperclasses.equals(other.directSuperclasses);
	    }
	}

	public int hashCode()
	{
	    return type.hashCode();
	}
    }

    private static Map linearizations = 
	Collections.synchronizedMap(new WeakHashMap());

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


    protected static List computeClassLinearization(Class c, DirectSuperclasses dsc)
	throws JavaC3Exception
    {
	List cDirectSuperclasses = dsc.directSuperclasses(c);
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
	return allSuperclasses(c, DefaultDirectSuperclasses.SUPERCLASSES);
    }

    public static List allSuperclasses(Class c, DirectSuperclasses dsc)
	throws JavaC3Exception
    {
	LinearizationKey key = new LinearizationKey(dsc, c);
	List linearization = (List)linearizations.get(key);
	if(linearization == null) {
	    linearization = computeClassLinearization(c, dsc);
	    linearizations.put(key, linearization);
	}

	return linearization;
    }

}