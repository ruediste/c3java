
package com.github.ruediste1.c3java;

import java.util.*;

/**
 * Implementation of {@link DirectParentTypesReader} with 'implements' before
 * 'extends'.  Although this appears to make more sense than the
 * default, it doesn't work on the concrete collections classes in
 * java.util.
 */
public class ImplementsFirstDirectParentTypesReader implements DirectParentTypesReader
{
    public static final DirectParentTypesReader INSTANCE =
        new ImplementsFirstDirectParentTypesReader();

    /**
     * Get the direct superclasses of a class.
     * Interfaces, followed by the superclasses. Interfaces with
     * no super interfaces extend Object.
     */
    public List<Class<?>> directParentTypes(Class<?> c)
    {
        if(c.isPrimitive()) {
            return DefaultDirectParentTypesReader.primitiveSuperclasses(c);
        }
        else if(c.isArray()) {
            return DefaultDirectParentTypesReader.arrayDirectSuperclasses(0, c, this);
        }
        else {
            Class<?> [] interfaces = c.getInterfaces();
            Class<?> superclass = c.getSuperclass();

            List<Class<?>> classes = new LinkedList<Class<?>>();
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
