package net.lshift.java.lang;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import net.lshift.java.dispatch.DefaultDirectSuperclasses;
import net.lshift.java.dispatch.JavaC3;
import net.lshift.java.util.Lists;
import net.lshift.java.util.Transform;

public class AnnotationHelper
{
    /**
     * Search for an annotation.
     * Searches the C3 linearization of the methods class for
     * the first matching annotation. This should give the
     * most specific version of the annotation.
     * @param <T>
     * @param annotationClass
     * @param method
     * @param superclasses
     * @return
     */
    public static <T extends Annotation> T getAnnotation
        (final Class<T> annotationClass, 
         Method method,
         JavaC3.DirectSuperclasses superclasses)
    {
        // FIXME: cache the result
        final String name = method.getName();
        final Class<?> [] signature = method.getParameterTypes();
        return Lists.any(new Transform<Class<?>,T>() {

            public T apply(Class<?> c)
            {
                try {
                    Method smethod = c.getDeclaredMethod(name, signature);
                    return smethod.getAnnotation(annotationClass);
                }
                catch(NoSuchMethodException e) {
                    return null;
                }
            }
            
        }, JavaC3.allSuperclasses(method.getDeclaringClass(), superclasses));
    }
 
    public static <T extends Annotation> T getAnnotation
        (final Class<T> annotationClass, 
         final Method method)
    {
        return getAnnotation
            (annotationClass, 
             method, 
             DefaultDirectSuperclasses.SUPERCLASSES);
    }
    
    public static <T extends Annotation> T getAnnotation(
        final Class<T> annotationClass, 
        final Class<?> type,
        JavaC3.DirectSuperclasses superclasses)
    {
        return Lists.any(new Transform<Class<?>,T>() {

            public T apply(Class<?> c)
            {
                return type.getAnnotation(annotationClass);
            }
            
        }, JavaC3.allSuperclasses(type, superclasses));
    }
    
    public static <T extends Annotation> T getAnnotation(
                    final Class<T> annotationClass, 
                    final Class<?> type)
    {
        return getAnnotation(
            annotationClass, 
            type, 
            DefaultDirectSuperclasses.SUPERCLASSES);
    }
}
