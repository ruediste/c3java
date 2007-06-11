
package net.lshift.java.dispatch;




import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.lshift.java.util.Collections;
import net.lshift.java.util.Lists;
import net.lshift.java.util.Predicate;
import net.lshift.java.util.Transform;

/**
 * Implement C3 Linearization
 * http://www.webcom.com/haahr/dylan/linearization-oopsla96.html
 * @author david
 */
public class JavaC3
{

    /*
     * This is a translation of the dylan example at the end of the above paper.
     * Its the driver behind the generic list processing library in
     * net.lshift.util.
     */
    
    public interface DirectSuperclasses
    {
	/**
	 * Get the super classes.
	 * @param type the type to get the superclasses of.
	 */
	public List<Class> directSuperclasses(Class type);
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

    private static Map<LinearizationKey, List<Class>> linearizations = 
	java.util.Collections.synchronizedMap
            (new WeakHashMap<LinearizationKey, List<Class>>());

    /**
     * Thrown when its not possible to linearize
     * all superclasses.
     */
    public static class JavaC3Exception
	extends Error
    { 
        private static final long serialVersionUID = 1L;
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



    protected static List<Class> mergeLists
        (List<Class> partialResult,
         final List<List<Class>> remainingInputs,
         final DirectSuperclasses dsc) 
        throws JavaC3Exception
    {
        /*
             The main difference between this and the Dylan version is
             that the partial order is not reversed. Thats because its cheaper
             to append to the list in java than to prepend.
         */
        
        Predicate<List<Class>> empty = Collections.Procedures.isEmpty();
        if(Lists.all(empty, remainingInputs)) {
            return partialResult;
        }
        else {
            // start of selection rule
            final Predicate<Class> isCandidate = new Predicate<Class>() {

                public Boolean apply(final Class c) {

                    
                    Predicate<List<Class>> isHead = new Predicate<List<Class>>() {
                        public Boolean apply(List<Class> l) {
                            return l.isEmpty() ? false : c.equals(Lists.head(l));
                        }
                    };

                    Predicate<List<Class>> isTail = new Predicate<List<Class>>() {
                        public Boolean apply(List<Class> l) {
                            return l.indexOf(c) > 0;
                        }
                    };
                    
                    boolean result = (Lists.find(isHead, remainingInputs) != null)
                        && (Lists.find(isTail, remainingInputs) == null);
                    return result;
                }
            };
            
            Transform<Class,Class> candidateDirectSuperclass = new Transform<Class,Class>() {
                public Class apply(final Class c) {
                    return Lists.find(isCandidate, dsc.directSuperclasses(c));
                }
            };
            
            final Class next = Lists.anyLast(candidateDirectSuperclass, partialResult);
            
            // end of selection rule
            
            if(next != null) {
                
                Transform<List<Class>, List<Class>> removeNext =
                    new Transform<List<Class>, List<Class>>() {
                        public List<Class> apply(List<Class> l) {
                            return (!l.isEmpty() && Lists.head(l).equals(next)) 
                                ? Lists.tail(l) : l;
                        }
                };

                // we certainly don't need partialResult again, so it should
                // be fine to append to it here
                partialResult.add(next);
                return mergeLists(partialResult, Lists.map(removeNext, remainingInputs), dsc);
            }
            else {
                throw new JavaC3Exception(partialResult, remainingInputs);
            }
        }
    }
    
    protected static List<Class> computeClassLinearization
        (Class c, 
         final DirectSuperclasses dsc)
	throws JavaC3Exception
    {
	List<Class> cDirectSuperclasses = dsc.directSuperclasses(c);
        
        Transform<Class,List<Class>> cplList = new Transform<Class,List<Class>>() {
            public List<Class> apply(Class c) {
                return allSuperclasses(c, dsc);
            }
        };
        
        return mergeLists
            (Lists.list(c), 
             Lists.concatenate
             (Lists.map(cplList, cDirectSuperclasses), 
              Lists.list(cDirectSuperclasses)),
             dsc);
        
    }

    public static List<Class> allSuperclasses(Class c)
	throws JavaC3Exception
    {
	return allSuperclasses(c, DefaultDirectSuperclasses.SUPERCLASSES);
    }

    public static List<Class> allSuperclasses(Class c, DirectSuperclasses dsc)
	throws JavaC3Exception
    {
	LinearizationKey key = new LinearizationKey(dsc, c);
	List<Class> linearization = linearizations.get(key);
	if(linearization == null) {
	    linearization = computeClassLinearization(c, dsc);
	    linearizations.put(key, linearization);
	}

	return linearization;
    }

}