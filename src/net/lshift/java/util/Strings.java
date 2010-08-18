package net.lshift.java.util;

public class Strings
{
    public Transform<Object,String> replace(final String target, final String substitute)
    {
        return new Transform<Object,String>() {
            public String apply(Object x) {
                return x.toString().replace(target, substitute);
            }       
        };
    }
}
