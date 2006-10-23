
package net.lshift.java.dispatch;

import java.util.*;

/**
 * Implementation of direct superclasses with 'implements' before
 * 'extends'.  Although this appears to make more sense than the
 * default, it doesn't work on the concrete collections classes in
 * java.util.
 */
public class ImplementsFirstDirectSuperclasses
    extends DefaultDirectSuperclasses
{
    public static final JavaC3.DirectSuperclasses SUPERCLASSES = 
	new ImplementsFirstDirectSuperclasses();

    /**
     * Get the direct superclasses of a class.
     * Interfaces, followed by the superclasses. Interfaces with
     * no super interfaces extend Object.
     */
    public List<Class> directSuperclasses(Class c)
    {
	if(c.isPrimitive()) {
	    return primitiveSuperclasses(c);
	}
	else if(c.isArray()) {
	    return arrayDirectSuperclasses(0, c);
	}
	else {
	    Class [] interfaces = c.getInterfaces();
	    Class superclass = c.getSuperclass();

	    List<Class> classes = new LinkedList<Class>();
	    classes.addAll(Arrays.asList(interfaces));
	    if(superclass == null) {
		if(interfaces.length == 0 && 
		   c != Object.class)
		    classes.add(Object.class);
	    }
	    else {
		classes.add(superclass);
	    }

	    return classes;
	}
    }
}