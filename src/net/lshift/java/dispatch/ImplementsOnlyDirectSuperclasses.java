/*

Copyright (c) 2006 LShift Ltd

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

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