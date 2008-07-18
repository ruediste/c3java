package net.lshift.java.lang;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.lshift.java.util.Maps;

public class Types
{
    public static final Map<Class<?>,Class<?>> PRIMITIVE_WRAPPER;
    static {
        Map<Class<?>,Class<?>> reps = new HashMap<Class<?>,Class<?>>();
        addPrimitive(reps, Boolean.class);
        addPrimitive(reps, Byte.class);
        addPrimitive(reps, Character.class);
        addPrimitive(reps, Double.class);
        addPrimitive(reps, Float.class);
        addPrimitive(reps, Integer.class);
        addPrimitive(reps, Long.class);
        addPrimitive(reps, Short.class);
        addPrimitive(reps, Void.class);
        PRIMITIVE_WRAPPER = Collections.unmodifiableMap(reps);
    }
    
    static void addPrimitive(Map<Class<?>,Class<?>> reps, Class<?> rep)
    {
        try {
            reps.put((Class<?>) rep.getField("TYPE").get(null), rep);
            
        }
        catch (Exception e) {
            throw new ExceptionInInitializerError(
                rep.getName() + " does not contain a primtive type");
        }
    }
    
    public static Class<? extends Object> getWrapperClass(Class <?> primitive)
    {
        if(!primitive.isPrimitive())
            throw new IllegalArgumentException(
                primitive.getName() + " is not primitive");
        if(!PRIMITIVE_WRAPPER.containsKey(primitive))
            throw new IllegalArgumentException(
                primitive.getName() + " is not a supported primitive");
        return PRIMITIVE_WRAPPER.get(primitive);
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Object> asClass(Class<?> type)
    {
        return PRIMITIVE_WRAPPER.containsKey(type) 
            ? PRIMITIVE_WRAPPER.get(type)
            : (Class<Object>)type;
    }
    
    public static final Map<Class<?>,Class<?>> PRIMITIVE;
    static {
        PRIMITIVE = Collections.unmodifiableMap(Maps.invert(PRIMITIVE_WRAPPER));
    }
 
    public static final Map<Class<?>,Object> DEFAULT_VALUES;
    static {
        Map<Class<?>,Object> primitives = new HashMap<Class<?>,Object>();
        primitives.put(Boolean.TYPE, false);
        primitives.put(Double.TYPE, new Double(0.0));
        primitives.put(Float.TYPE, new Float(0.0));
        primitives.put(Long.TYPE, new Long(0));
        primitives.put(Integer.TYPE, new Integer(0));
        primitives.put(Short.TYPE, new Short((short)0));
        primitives.put(Byte.TYPE, new Byte((byte)0));
        primitives.put(Character.TYPE, new Character((char)0));
        DEFAULT_VALUES = Collections.unmodifiableMap(primitives);
    }
 
}
