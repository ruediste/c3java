
package net.lshift.java.dispatch;

import java.util.*;

public class JavaC3
{
    public static class InconsistentPrecedenceGraphError 
	extends Error 
    { 
	private List reversedPartialResult;
	private List remainingInputs;

	protected InconsistentPrecedenceGraphError
	    (List reversedPartialResult, List remainingInputs)
	{
	    this.reversedPartialResult = reversedPartialResult;
	    this.remainingInputs = remainingInputs;
	}

	public String toString()
	{
	    return "inconsistent precendence: " + 
		reversedPartialResult + 
		" " + remainingInputs;
	}
    }

    protected static List directSuperclasses(Class c)
    {
	Class [] interfaces = c.getInterfaces();
	Class superclass = c.getSuperclass();

	List classes = new LinkedList();
	if(superclass != null)
	    classes.add(superclass);
	classes.addAll(Arrays.asList(interfaces));
	return classes;
    }

    private static class MergeLists
    {
	private LinkedList reversedPartialResult;
	private List remainingInputs;

	public MergeLists(Class c, List inputs)
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
		    throw new InconsistentPrecedenceGraphError
			(reversedPartialResult, remainingInputs);
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
    {
	List cDirectSuperclasses = directSuperclasses(c);
	List inputs = new ArrayList(cDirectSuperclasses.size()+1);
	Iterator i = cDirectSuperclasses.iterator();
	while(i.hasNext())
	    inputs.add(allSuperclasses((Class)i.next()));
	inputs.add(cDirectSuperclasses);

	MergeLists ml = new MergeLists(c, inputs);
	return ml.getLinearization();
    }

    public static List allSuperclasses(Class c)
    {
	if(c == Object.class)
	    return Collections.nCopies(0, null);
	else
	    return computeClassLinearization(c);
    }

}