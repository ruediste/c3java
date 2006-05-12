/*
     This file is part of the LShift Java Library.

    The LShift Java Library is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    The LShift Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with The LShift Java Library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.lshift.java.dispatch;

import java.util.*;

/**
 * Implementation of direct superclasses which only includes
 * interfaces and java.lang.Object. Note: it includes all the
 * interfaces implemented by all its super classes as directly
 * implemented.
 */
public class ImplementsOnlyDirectSuperclasses
    extends DefaultDirectSuperclasses
{
    public static final JavaC3.DirectSuperclasses SUPERCLASSES = 
	new ImplementsOnlyDirectSuperclasses();

    /**
     * Get the direct superclasses of a class.
     * Interfaces, followed by the superclasses. Interfaces with
     * no super interfaces extend Object.
     */
    public List directSuperclasses(Class c)
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

	    List classes = new LinkedList();
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