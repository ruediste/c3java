package net.lshift.java.lang;

import static net.lshift.java.util.Lists.filter;
import static net.lshift.java.util.Procedures.and;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lshift.java.dispatch.DefaultDirectSuperclasses;
import net.lshift.java.dispatch.JavaC3;
import net.lshift.java.util.Lists;
import net.lshift.java.util.Predicate;
import net.lshift.java.util.Procedures;
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

    /**
     * An Injector copies fields from one class to another.
     * The set of properties considered can be restricted. The copies are
     * selected by type compatibility. That is, each field in the source
     * is copied to each field in the destination which is assignable from
     * the source's static type.
     * @param <S> the source bean's class
     * @param <D> the destination bean's class
     */
    public interface Injector<S,D> {
        public void inject(S src, D dst);
    }

    /**
     * Construct an injector given two classes, and a predicate which filters
     * fields.
     * @param <S>
     * @param <D>
     * @param a
     * @param b
     * @param include
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <S, D> Injector<S,D> injector(
        final Class<? extends S> a,
        final Class<? extends D> b,
        Predicate<Annotation> include)
    {
        final Map<Field, List<Field>> assignments = new HashMap<Field, List<Field>>();

        Field[] dstFields = b.getFields();
        for(Field srcField: a.getFields()) {
            for(Annotation srcAnnotation: filter(include, srcField.getAnnotations())) {
                assignments.put(srcField,
                    filter(
                        and(
                            hasAnnotation(srcAnnotation.annotationType()),
                            isAssignableFrom(srcField.getType())),
                        dstFields));
            }
        }

        return new Injector<S,D>() {

            @Override
            public void inject(S src, D dst)
            {
                for(Map.Entry<Field, List<Field>> assignmentSrc: assignments.entrySet()) {
                    for(Field assignmentDst: assignmentSrc.getValue()) {
                        Object value;
                        try {
                            value = assignmentSrc.getKey().get(src);
                            assignmentDst.set(dst, value);
                        } catch (IllegalArgumentException e) {
                            throw new RuntimeException(
                                "You've somehow managed to call this with incorrect typed arguments. " +
                                "I.e. arguments that don't match the classes you constructed the " +
                                "injector for.", e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Problem reading or writing fields in injector", e);
                        }
                    }
                }
            }

            public String toString()
            {
                return assignments.toString();
            }

        };
    }


    private static Predicate<Field> isAssignableFrom(final Class<?> type)
    {
        return new Predicate<Field>() {
            public Boolean apply(Field x) {
                return x.getType().isAssignableFrom(type);
            }
        };
    }

    /**
     * Use an annotation to restrict the fields used in injection.
     * The annotation will typically reflect a single cross cutting concern
     * @param annotation
     * @return a predicate which filters a field based on it having
     * the annotation specified.
     */
    public static Predicate<Field> hasAnnotation(
        final Class<? extends Annotation> annotation)
    {
        return new Predicate<Field>() {
            public Boolean apply(Field x) {
                return x.getAnnotation(annotation) != null;
            }

        };
    }

    /**
     * Use an annotation to restrict the fields used in injection.
     * The annotation will typically reflect a single cross cutting concern
     * @param <A>
     * @param <B>
     * @param a
     * @param b
     * @return
     */
    public static <A, B> Injector<A,B> injector(
        final Class<A> a,
        final Class<B> b)
    {
        return injector(a, b, Procedures.<Annotation>any());
    }
}
