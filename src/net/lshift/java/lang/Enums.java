package net.lshift.java.lang;

import net.lshift.java.util.Transform;

public class Enums {
    public static <T extends Enum<T>> Transform<T,String> name() {
        return new Transform<T,String>() {
            public String apply(T x) {
                return x.name();
            }
        };
    }
    
    public static <T extends Enum<T>> Transform<String,T> valueOf(final Class<T> enumType) {
        return new Transform<String,T>() {
            public T apply(String x) {
                return Enum.valueOf(enumType, x);
            }
        };
    }
}
