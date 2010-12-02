package net.lshift.java.lang;

import java.util.Iterator;

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
}
