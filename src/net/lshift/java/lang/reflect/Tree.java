package net.lshift.java.lang.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.lshift.java.util.Transform;

/**
 * Java data structures as trees using reflection
 */
public class Tree {
    @SuppressWarnings("unchecked")
    public static <T> T transform(T root, Transform<Object,Object> transform)
    throws IllegalArgumentException, IllegalAccessException {
        Class<?> c = root.getClass();
        replaceFields(c, root, transform);
        return (T)transform.apply(root);
    }
    
    public static void replaceFields(
        Class<?> c, 
        Object root, 
        Transform<Object,Object> transform)
    throws IllegalArgumentException, IllegalAccessException {
        for(Field field: c.getDeclaredFields()) {
            if(!Modifier.isStatic(field.getModifiers())) {
                if(!field.isAccessible()) {
                    field.setAccessible(true);
                    field.set(root, isPrimitive(field.getType()) ? transform.apply(field.get(root)) : transform(field.get(root), transform));
                    
                }
            }
        }
        
        if(c.getSuperclass() != null) {
            replaceFields(c.getSuperclass(), root, transform);
        }
    }

    private static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive();
    }
}
