package net.lshift.java.lang;

import net.lshift.java.util.Transform;

public class Objects
{
    public static final Transform<Object,Class<?>> CLASS = 
        new Transform<Object,Class<?>>() {
    
        public Class<?> apply(Object x) {
            return x.getClass();
        }
    };
    public static final Transform<Object,Integer> HASH_CODE = 
        new Transform<Object,Integer>() {
    
        public Integer apply(Object x) {
            return x.hashCode();
        }
    };
    public static final Transform<? extends Object,String> TO_STRING = 
        new Transform<Object,String>() {
    
        public String apply(Object x) {
            return x.toString();
        }
    };

}
