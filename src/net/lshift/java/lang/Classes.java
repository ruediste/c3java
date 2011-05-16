package net.lshift.java.lang;

import net.lshift.java.util.Predicate;
import net.lshift.java.util.Transform;

public class Classes
{
    public static final <E> Transform<Class<E>,String> getName() {
        return new Transform<Class<E>,String>() {
    
            public String apply(Class<E> x) {
                return x.getName();
            }
        };
    }



    public static final Transform<Class<?>,String> CANONICAL_NAME = 
        new Transform<Class<?>,String>() {
    
        public String apply(Class<?> x) {
            return x.getCanonicalName();
        }
    };

    public static final Transform<Class<?>,String> SIMPLE_NAME = 
        new Transform<Class<?>,String>() {
    
        public String apply(Class<?> x) {
            return x.getSimpleName();
        }
    };

    public static final Predicate<Class<?>> isAssignableFrom(final Class<?> dst)
    {
        return new Predicate<Class<?>>() {
            public Boolean apply(Class<?> x) {
                return dst.isAssignableFrom(x);
            }  
        };
    }
}
