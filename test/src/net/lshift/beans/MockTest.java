package net.lshift.beans;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import net.lshift.java.beans.Mock;
import net.lshift.java.util.Lists;

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
    
    Mock.Factory<ExampleBean> exampleFactory;
    
    public void setUp()
    throws Exception
    {
        exampleFactory = Mock.factory(ExampleBean.class);
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
    
    private ExampleBean example()
    throws Exception
    {
        ExampleBean a = exampleFactory.bean();
        a.setBoolean1(true);
        a.setByte1((byte)20);
        a.setChar1('z');
        a.setDouble1(10.1);
        a.setFloat1((float)20.5);
        a.setInt1(56);
        a.setLong1(597);
        a.setShort1((short)99);
        a.setString1("gargle");
        return a;
    }
    
//    public void testEquals()
//    throws Exception
//    {
//        ExampleBean a = example();
//        ExampleBean b = example();
//        assertEquals(a,b);
//        b.setString1("swallow");
//        assertFalse(a.equals(b));
//    }
    
    public void testSerializable()
    throws Exception
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        System.out.println(Lists.list(example().getClass().getInterfaces()));
        out.writeObject(example());
    }
}
