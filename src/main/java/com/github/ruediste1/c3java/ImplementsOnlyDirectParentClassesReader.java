package com.github.ruediste1.c3java;

import java.util.*;

/**
 * Implementation of {@link DirectParentClassesReader} which only includes
 * interfaces and java.lang.Object. Note: it includes all the
 * interfaces implemented by all its super classes as directly
 * implemented.
 */
public class ImplementsOnlyDirectParentClassesReader
    implements DirectParentClassesReader
{
    public static final DirectParentClassesReader INSTANCE =
        new ImplementsOnlyDirectParentClassesReader();

    /**
     * Get the direct superclasses of a class.
     * Interfaces, followed by the superclasses. Interfaces with
     * no super interfaces extend Object.
     */
    public List<Class<?>> directParentClasses(Class<?> c)
    {
        if(c.isPrimitive()) {
            return DefaultDirectParentClassesReader.primitiveSuperclasses(c);
        }
        else if(c.isArray()) {
            return DefaultDirectParentClassesReader.arrayDirectSuperclasses(0, c, this);
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
