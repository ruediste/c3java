package net.lshift.java.io;

import static net.lshift.java.util.Collections.Procedures.IDENTITY;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.lshift.java.lang.EqualsHelper;
import net.lshift.java.util.Transform;

public class SerializationTest
    extends TestCase
{
    public static class A
    implements Serializable
    {
        private static final long serialVersionUID = 1L;

        public A(String foo, String bar)
        {
            this.foo = foo;
            this.bar = bar;
        }
        
        String foo;
        String bar;
    }
    
    public static class B
    implements Serializable
    {
        private static final long serialVersionUID = 1L;

        A x = new A("x", "1");
        A y = new A("y", "2");
        
    }
    
    public static Transform<Object,Object> INTEGER_TO_STRING = 
        new Transform<Object, Object>() {
        public Object apply(Object x) {
            if(x instanceof String)
                return new Integer(x.toString());
            else
                return x;
        }
    };
    
    public static Transform<Object,Object> STRING_TO_INTEGER =
        new Transform<Object, Object>() {
        public Object apply(Object x) {
            if(x instanceof Integer)
                return ((Integer)x).toString();
            else
                return x;
        }
    };
    
    @SuppressWarnings("unchecked")
    public void testMapOnCollections()
    {
        Map<String,String> source = new HashMap<String,String>();
        for(int i = 0; i != 11; ++i)
            source.put(Integer.toString(i), Integer.toString(i));
        Map<String, Integer> converted =
            (Map<String, Integer>) Serialization.map(
                INTEGER_TO_STRING, 
                IDENTITY, source);

        for(Integer value: converted.values())
            assert(value instanceof Integer);
        
        Map<String, String> reconverted =
            (Map<String, String>) Serialization.map(
                STRING_TO_INTEGER, IDENTITY, converted);
        
        assertEquals(source, reconverted);
        
        assertEquals(source, Serialization.map(INTEGER_TO_STRING, STRING_TO_INTEGER, source));
    }
    
    public static Transform<Object,Object> DECIMAL_FORMAT = 
        new Transform<Object, Object>() {
        public Object apply(Object x) {
            if(x instanceof String) {
                try {
                    Float.toString(Integer.parseInt(x.toString()));
                }
                catch(NumberFormatException e) { }
            }

            return x;
        }
    };
    
    public static Transform<Object,Object> DECIMAL_UNFORMAT = 
        new Transform<Object, Object>() {
        public Object apply(Object x) {
            if(x instanceof String) {
                return x.toString().split("\\.")[0];
            }
            else {
                return x;
            }
        }
    };
    
    public void testMapOnObjects()
    {
        B source = new B();
        B converted = 
            (B)Serialization.map(DECIMAL_FORMAT, IDENTITY, source);

        Float.parseFloat(source.x.bar);
        Float.parseFloat(source.y.bar);
        
        B reconverted =
            (B)Serialization.map(DECIMAL_FORMAT, IDENTITY, converted);
        
        assertTrue(EqualsHelper.equals(source, reconverted));
        
        assertTrue(EqualsHelper.equals(
            source, 
            Serialization.map(DECIMAL_FORMAT,DECIMAL_UNFORMAT, source)));
    }

}
