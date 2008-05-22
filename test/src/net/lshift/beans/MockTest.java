package net.lshift.beans;

import java.util.HashMap;
import java.util.Map;

import net.lshift.java.beans.Mock;

import junit.framework.TestCase;

public class MockTest
    extends TestCase
{
    interface ExampleBean
    {
        public int getInt1();
        public void setInt1(int i);
        public String getString1();
        public void setString1(String s);
        public boolean getBoolean1();
        public void setBoolean1(boolean b);
        public byte getByte1();
        public void setByte1(byte b);
        public short getShort1();
        public void setShort1(short s);
        public long getLong1();
        public void setLong1(long l);
        public char getChar1();
        public void setChar1(char c);
        public double getDouble1();
        public void setDouble1(double d);
        public float getFloat1();
        public void setFloat1(float f);
    }
    
    interface ExampleSubclassBean
    extends ExampleBean
    {
        
    }
    
    public void testMap()
    throws Exception
    {
        Map<String,Object> store = new HashMap<String,Object>();
        ExampleBean instance = Mock.bean(ExampleBean.class, store);
        assertEquals(0, instance.getInt1());
        instance.setInt1(55);
        assertEquals(55, instance.getInt1());
        assertEquals(55, store.get("int1"));
        instance.setString1("foo");
        assertEquals("foo", instance.getString1());
        assertEquals("foo", store.get("string1"));
        assertEquals(store.toString(), instance.toString());
    }
    
    public void testSubclassMap()
    throws Exception
    {
        Map<String,Object> store = new HashMap<String,Object>();
        ExampleSubclassBean instance = Mock.bean(ExampleSubclassBean.class, store);
        instance.setInt1(55);
        assertEquals(55, instance.getInt1());
        assertEquals(55, store.get("int1"));
        instance.setString1("foo");
        assertEquals("foo", instance.getString1());
        assertEquals("foo", store.get("string1"));
    }
    
    public void testDefaultValues()
    throws Exception
    {
        ExampleBean instance = Mock.bean(ExampleBean.class);
        assertEquals(0, instance.getByte1());
        assertEquals(0, instance.getChar1());
        assertEquals(0, instance.getInt1());
        assertEquals(0, instance.getLong1());
        assertEquals(new Float(0.0), instance.getFloat1());
        assertEquals(new Double(0.0), instance.getDouble1());
        assertFalse(instance.getBoolean1());
    }
}
