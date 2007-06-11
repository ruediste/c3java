package net.lshift.java.util;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import net.lshift.java.dispatch.DynamicDispatch;

/**
 * Generate canonical literal strings for java values
 * @author david
 *
 */
public class Literal
{
    private interface ToLiteral
    {
        public String literal(Object o);
    }
    
    
    static Map<Character,String> ESCAPE;
    static {
        Map<Character, String> escape = new HashMap<Character,String>();
        escape.put('\b', "\\b");
        escape.put('\t', "\\t");
        escape.put('\n', "\\n");
        escape.put('\f', "\\f");
        escape.put('\r', "\\r");
        escape.put('\"', "\\\"");
        escape.put('\'', "\\\'");
        escape.put('\\', "\\\\");
        escape.put('\0', "\\0");
        ESCAPE = java.util.Collections.unmodifiableMap(escape);
    }

    static ToLiteral DISPATCHER = DynamicDispatch.<ToLiteral>proxy
        (ToLiteral.class, new Object() {

            @SuppressWarnings("unused")
            public String literal(Object object)
            {
                if(object.getClass().isArray()) {
                    StringBuffer buf = new StringBuffer();
                    buf.append("new ");
                    buf.append(object.getClass().getSimpleName());
                    buf.append(" { ");
                    for(int i = 0; i != Array.getLength(object); ++i) {
                        if(i != 0) buf.append(", ");
                        buf.append(Literal.literal(Array.get(object, i)));
                    }
                    buf.append(" }");
                    return buf.toString();
                }
                else {
                    return object.toString();
                }
            }

            public String literal(Character c)
            {
                if(ESCAPE.containsKey(c)) {
                    return ESCAPE.get(c);
                }
                else if(c > 0xff || c <= 0x1f) {
                    return "\\u" + Integer.toHexString(c);
                }
                else {
                    return Character.toString(c);
                }
            }
            
            @SuppressWarnings("unused")
            public String literal(String str)
            {
                StringBuffer buf = new StringBuffer();
                buf.append('"');
                for(char c: str.toCharArray())
                    buf.append(literal(c));
                buf.append('"');
                
                return buf.toString();
            }
                        
           
            @SuppressWarnings("unused")
            public String literal(Long i)
            {
                return Long.toString(i) + "L";
            }
            
        });

    public static String literal(Object o)
    {
        return DISPATCHER.literal(o);
    }
    
}
