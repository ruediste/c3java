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
    // The fact that this interface isn't public is part of the test
    interface ExampleBean
    {
        public int getInt1();
        public void setInt1(int i);
        public String getString1();
        public void setString1(String s);
        public boolean isBoolean1();
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

    public class ConcreteExampleBean
    implements ExampleBean
    {
        int int1;
        String string1;
        boolean boolean1;
        byte byte1;
        short short1;
        long long1;
        char char1;
        double double1;
        float float1;

        public int getInt1()
        {
            return int1;
        }

        public void setInt1(int int1)
        {
            this.int1 = int1;
        }

        public String getString1()
        {
            return string1;
        }

        public void setString1(String string1)
        {
            this.string1 = string1;
        }

        public boolean isBoolean1()
        {
            return boolean1;
        }

        public void setBoolean1(boolean boolean1)
        {
            this.boolean1 = boolean1;
        }

        public byte getByte1()
        {
            return byte1;
        }

        public void setByte1(byte byte1)
        {
            this.byte1 = byte1;
        }

        public short getShort1()
        {
            return short1;
        }

        public void setShort1(short short1)
        {
            this.short1 = short1;
        }

        public long getLong1()
        {
            return long1;
        }

        public void setLong1(long long1)
        {
            this.long1 = long1;
        }

        public char getChar1()
        {
            return char1;
        }

        public void setChar1(char char1)
        {
            this.char1 = char1;
        }

        public double getDouble1()
        {
            return double1;
        }

        public void setDouble1(double double1)
        {
            this.double1 = double1;
        }

        public float getFloat1()
        {
            return float1;
        }

        public void setFloat1(float float1)
        {
            this.float1 = float1;
        }

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
        assertFalse(instance.isBoolean1());
    }

    private ExampleBean concreteExample()
    {
        ExampleBean a = new ConcreteExampleBean();
        init(a);
        return a;
    }

    private ExampleBean example()
    throws Exception
    {
        ExampleBean a = exampleFactory.bean();
        init(a);
        return a;
    }

    private void init(ExampleBean a)
    {
        a.setBoolean1(true);
        a.setByte1((byte)20);
        a.setChar1('z');
        a.setDouble1(10.1);
        a.setFloat1((float)20.5);
        a.setInt1(56);
        a.setLong1(597);
        a.setShort1((short)99);
        a.setString1("foo");
    }

    public void testEquals()
    throws Exception
    {
        ExampleBean a = example();
        ExampleBean b = example();
        assertEquals(a,b);
        b.setString1("bar");
        assertFalse(a.equals(b));
    }

    public void testSerializable()
    throws Exception
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        System.out.println(Lists.list(example().getClass().getInterfaces()));
        out.writeObject(example());
    }

    public void testCopy()
    throws Exception
    {
        exampleFactory.copy(concreteExample());
        ExampleBean a = example();
        assertEquals(a, exampleFactory.copy(a));
    }

    public void testAssign()
    throws Exception
    {
        ExampleBean a = example();
        ExampleBean b = exampleFactory.bean();
        exampleFactory.assign(b, a);
        assertEquals(a, b);
        ExampleBean c = new ConcreteExampleBean();
        exampleFactory.assign(c, a);
        assertEquals(a, exampleFactory.copy(c));
    }
}
