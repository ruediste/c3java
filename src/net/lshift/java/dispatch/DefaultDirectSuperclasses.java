
package net.lshift.java.dispatch;

import java.lang.reflect.Array;
import java.util.*;
import java.io.Serializable;

import com.google.common.collect.ImmutableList;

import net.lshift.java.util.Lists;

/**
 * Default implementation of default superclasses.
 * C3 needs an ordering of the direct superclasses.
 * We can get one using reflection, but there are at least 2 sensible
 * ways to do this. Also, you may not want to rely on the ordering that
 * reflection gives to the set of implemented interfaces. Testing
 * suggests the order is sane - ie. its the order in which they are
 * declared, but I can't find that documented anywhere, and
 * in the case of other peoples classes, that may not be acceptable
 * anyway. There are a few intersecting concerns involved - like basic
 * type ordering, array handling, and in future, generics, so you
 * are going to want to subclass this in most cases.
 */
public class DefaultDirectSuperclasses
    implements JavaC3.DirectSuperclasses
{
    private static final Map<Class<?>,List<Class<?>>> PRIMITIVE_SUPERCLASSES;

    static {
        Map<Class<?>,List<Class<?>>> superclasses = new HashMap<Class<?>,List<Class<?>>>();
        final List<Class<?>> none =  Collections.emptyList();
        // does this make any sense? It does from a 'widening conversion'
        // point of view in java.
        superclasses.put(Void.TYPE, none);
        superclasses.put(Boolean.TYPE, none);
        superclasses.put(Double.TYPE, classList(Float.TYPE));
        superclasses.put(Float.TYPE, classList(Long.TYPE));
        superclasses.put(Long.TYPE, classList(Integer.TYPE));
        superclasses.put(Integer.TYPE, classList(Short.TYPE, Character.TYPE));
        superclasses.put(Short.TYPE, classList(Byte.TYPE));
        superclasses.put(Byte.TYPE, none);
        superclasses.put(Character.TYPE, none);
        PRIMITIVE_SUPERCLASSES = Collections.unmodifiableMap(superclasses);
    }

    private static List<Class<?>> classList(Class<?> ... class1) {
        return Collections.unmodifiableList(Arrays.asList(class1));
    }

    public static final JavaC3.DirectSuperclasses SUPERCLASSES =
        new DefaultDirectSuperclasses();

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
    public List<Class<?>> directSuperclasses(Class<?> c)
    {
        if(c.isPrimitive()) {
            return primitiveSuperclasses(c);
        }
        else if(c.isArray()) {
            return arrayDirectSuperclasses(0, c);
        }
        else {
            Class<?> [] interfaces = c.getInterfaces();
            Class<?> superclass = c.getSuperclass();

            if(c == Object.class) {
                return ImmutableList.of();
            } else if(superclass == null && interfaces.length == 0) {
                return classList(Object.class);
            } else if(superclass == Object.class) {
                return Lists.concatenate(Arrays.asList(interfaces), classList(Object.class));
            } else if(superclass == null) {
                return Arrays.asList(interfaces);
            } else {
                return Lists.concatenate(classList(superclass), Arrays.asList(interfaces));
            }
        }
    }

    public List<Class<?>> primitiveSuperclasses(Class<?> c)
    {
            return PRIMITIVE_SUPERCLASSES.get(c);
    }

    /* the following is translated from sisc 1.8.5 s2j/reflection.scm
       java-array-superclasses. */

    protected static List<Class<?>> ARRAY_SUPERCLASSES =
        classList(Serializable.class, Cloneable.class, Object.class);

    public List<Class<?>> arrayDirectSuperclasses(int level, Class<?> c)
    {
        List<Class<?>> classes;

        if(c.isArray()) {
            classes = arrayDirectSuperclasses(level + 1, c.getComponentType());
        }
        else {
            List<Class<?>> componentSuperclasses = directSuperclasses(c);
            if(componentSuperclasses.isEmpty() && !c.isInterface()) {
                classes = (level == 1) ? new LinkedList<Class<?>>(ARRAY_SUPERCLASSES) :
                    makeArrayClasses(ARRAY_SUPERCLASSES, level - 1);
            }
            else {
                classes = makeArrayClasses(componentSuperclasses, level);
            }
        }

        return classes;
    }

    // this compensates for the lack of map
    public static List<Class<?>> makeArrayClasses(List<Class<?>> classes, int dims)
    {
        Iterator<Class<?>> i = classes.iterator();
        LinkedList<Class<?>> arrayClasses = new LinkedList<Class<?>>();
        while(i.hasNext())
            arrayClasses.add(makeArrayClass(i.next(), dims));
        return arrayClasses;
    }

    /* copied from sisc 1.8.5 s2j/Utils.java */
    public static Class<?> makeArrayClass(Class<?> c, int dims)
    {
        return Array.newInstance(c, new int[dims]).getClass();
    }

}
