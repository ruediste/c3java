package net.lshift.java.lang;

import java.util.Iterator;

import net.lshift.java.util.Transform;

public class Strings
{
    public static String join(Iterable<String> strings, String separator)
    {
        StringBuffer buffer = new StringBuffer();
        Iterator<String> i  = strings.iterator();
        if(i.hasNext())
            buffer.append(i.next());
        while(i.hasNext()) {
            buffer.append(separator);
            buffer.append(i.next());
        }
        
        return buffer.toString();
    }
    
    public static <T> Transform<T,String> asString() {
        return new Transform<T,String>() {
            public String apply(Object x) {
                return x.toString();
            }
        };
    }
}
