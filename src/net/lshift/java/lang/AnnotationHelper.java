package net.lshift.java.lang;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import net.lshift.java.dispatch.DefaultDirectSuperclasses;
import net.lshift.java.dispatch.JavaC3;
import net.lshift.java.util.Lists;
import net.lshift.java.util.Predicate;
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
                return c.getAnnotation(annotationClass);
            }
            
        }, JavaC3.allSuperclasses(type, superclasses));
    }

    /**
     * Retrieves all annotations of a type and its superclasses
     * @param annotationClass
     * @param type
     * @param <T>
     * @return
     */
    public static <T extends Annotation> Iterable<T> getAnnotations(
            final Class<T> annotationClass,
            final Class<?> type)
    {
        return getAnnotations
            (annotationClass,
             type,
             DefaultDirectSuperclasses.SUPERCLASSES);
    }

    public static <T extends Annotation> Iterable<T> getAnnotations(
        final Class<T> annotationClass,
        final Class<?> type,
        JavaC3.DirectSuperclasses superclasses)
    {
        List<T> annotations = Lists.map(new Transform<Class<?>,T>() {

            public T apply(Class<?> c) {
                return c.getAnnotation(annotationClass);
            }
        }, JavaC3.allSuperclasses(type, superclasses));

        return Lists.filter(new Predicate<T>() {

            public Boolean apply(T t) {
                return t != null;
            }
        }, annotations);
    }

    /**
     * Tell me which of my superclasses has the named annotation.
     * @param annotationClass
     * @param type
     * @param superclasses
     * @return
     */
    public static Class<?> getAnnotatedClass(
        final Class<? extends Annotation> annotationClass, 
        final Class<?> type,
        JavaC3.DirectSuperclasses superclasses)
    {
        return Lists.any(new Transform<Class<?>, Class<?>>() {

            public Class<?> apply(Class<?> c)
            {
                return c.getAnnotation(annotationClass) == null ? null : c;
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
    
    /**
     * Tell me which of my superclasses has the named annotation.
     * direct superclasses is the default.
     * @see DefaultDirectSuperclasses#SUPERCLASSES
     * @see #getAnnotatedClass(Class, Class, net.lshift.java.dispatch.JavaC3.DirectSuperclasses)
     */
    public static Class<?> getAnnotatedClass(
        final Class<? extends Annotation> annotationClass, 
        final Class<?> type)
    {
        return getAnnotatedClass(
            annotationClass, 
            type, 
            DefaultDirectSuperclasses.SUPERCLASSES);
    }
}
