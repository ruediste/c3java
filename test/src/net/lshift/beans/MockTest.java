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
}
