package com.github.ruediste.c3java.properties;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import org.junit.Test;

import com.github.ruediste.c3java.properties.PropertyPath.PropertyNode;

public class PropertyUtilTest {

    static class TestClassProperties {
        public int getR() {
            return 0;
        }

        public void setW(int arg) {
        }

        public int getRW() {
            return 0;
        }

        public void setRW(int arg) {
        }

        public static int getStatic() {
            return 0;
        }
    }

    static class ClassA {
        public int getA() {
            return 0;
        }
    }

    interface InterfaceB {
        public int getB();

        default public int getB1() {
            return 1;
        }
    }

    class ClassC extends ClassA implements InterfaceB {

        @Override
        public int getA() {
            return 0;
        }

        @Override
        public int getB() {
            return 0;
        }
    }

    void checkPropertyDeclaration(String name, Type declaringType, Type propertyType, Method getter, Method setter,
            Field backingField, PropertyDeclaration decl) {
        assertEquals(name, decl.getName());
        assertEquals(declaringType, decl.getDeclaringType());
        assertEquals(propertyType, decl.getPropertyType());
        assertEquals(getter, decl.getGetter());
        assertEquals(setter, decl.getSetter());
        assertEquals(backingField, decl.getBackingField());
    }

    @Test
    public void testPropertyIntroductionA() throws Exception {
        Map<String, PropertyDeclaration> props = PropertyUtil.getPropertyIntroductionMap(ClassA.class);
        assertEquals(1, props.size());
        checkPropertyDeclaration("a", ClassA.class, int.class, ClassA.class.getDeclaredMethod("getA"), null, null,
                props.get("a"));
    }

    @Test
    public void testPropertyIntroductionB() throws Exception {
        Map<String, PropertyDeclaration> props = PropertyUtil.getPropertyIntroductionMap(InterfaceB.class);
        assertEquals(2, props.size());
        checkPropertyDeclaration("b", InterfaceB.class, int.class, InterfaceB.class.getDeclaredMethod("getB"), null,
                null, props.get("b"));
        checkPropertyDeclaration("b1", InterfaceB.class, int.class, InterfaceB.class.getDeclaredMethod("getB1"), null,
                null, props.get("b1"));
    }

    @Test
    public void testPropertyIntroductionC() throws Exception {
        Map<String, PropertyDeclaration> props = PropertyUtil.getPropertyIntroductionMap(ClassC.class);
        assertEquals(3, props.size());
        checkPropertyDeclaration("a", ClassA.class, int.class, ClassA.class.getDeclaredMethod("getA"), null, null,
                props.get("a"));
        checkPropertyDeclaration("b", InterfaceB.class, int.class, InterfaceB.class.getDeclaredMethod("getB"), null,
                null, props.get("b"));
        checkPropertyDeclaration("b1", InterfaceB.class, int.class, InterfaceB.class.getDeclaredMethod("getB1"), null,
                null, props.get("b1"));
    }

    @Test
    public void testGetPropertyDeclarations() throws Exception {
        Map<String, PropertyDeclaration> props = PropertyUtil.getPropertyDeclarations(ClassC.class);
        assertEquals(2, props.size());
        checkPropertyDeclaration("a", ClassC.class, int.class, ClassC.class.getDeclaredMethod("getA"), null, null,
                props.get("a"));
        checkPropertyDeclaration("b", ClassC.class, int.class, ClassC.class.getDeclaredMethod("getB"), null, null,
                props.get("b"));

    }

    @Test
    public void testGetPropertyDeclarations1() throws Exception {
        Map<String, PropertyDeclaration> props = PropertyUtil.getPropertyDeclarations(TestClassProperties.class);
        assertEquals(3, props.size());
        checkPropertyDeclaration("r", TestClassProperties.class, int.class,
                TestClassProperties.class.getDeclaredMethod("getR"), null, null, props.get("r"));
        checkPropertyDeclaration("w", TestClassProperties.class, int.class, null,
                TestClassProperties.class.getDeclaredMethod("setW", int.class), null, props.get("w"));
        checkPropertyDeclaration("rW", TestClassProperties.class, int.class,
                TestClassProperties.class.getDeclaredMethod("getRW"),
                TestClassProperties.class.getDeclaredMethod("setRW", int.class), null, props.get("rW"));
    }

    void checkPropertyInfo(String name, Type propertyType, Method getter, Method setter, Field backingField,
            Type bearingType, PropertyInfo info) {
        assertEquals(name, info.getName());
        assertEquals(propertyType, info.getPropertyType());
        assertEquals(getter, info.getGetter());
        assertEquals(setter, info.getSetter());
        assertEquals(backingField, info.getBackingField());
        assertEquals(bearingType, info.getBearingType());
    }

    @Test
    public void testPropertyInfoA() throws Exception {
        Map<String, PropertyInfo> props = PropertyUtil.getPropertyInfoMap(ClassA.class);
        assertEquals(1, props.size());
        checkPropertyInfo("a", int.class, ClassA.class.getDeclaredMethod("getA"), null, null, ClassA.class,
                props.get("a"));
    }

    @Test
    public void testPropertyInfoB() throws Exception {
        Map<String, PropertyInfo> props = PropertyUtil.getPropertyInfoMap(InterfaceB.class);
        assertEquals(2, props.size());
        checkPropertyInfo("b", int.class, InterfaceB.class.getDeclaredMethod("getB"), null, null, InterfaceB.class,
                props.get("b"));
        checkPropertyInfo("b1", int.class, InterfaceB.class.getDeclaredMethod("getB1"), null, null, InterfaceB.class,
                props.get("b1"));
    }

    @Test
    public void testPropertyInfoC() throws Exception {
        Map<String, PropertyInfo> props = PropertyUtil.getPropertyInfoMap(ClassC.class);
        assertEquals(3, props.size());
        checkPropertyInfo("a", int.class, ClassC.class.getDeclaredMethod("getA"), null, null, ClassC.class,
                props.get("a"));
        checkPropertyInfo("b", int.class, ClassC.class.getDeclaredMethod("getB"), null, null, ClassC.class,
                props.get("b"));
        checkPropertyInfo("b1", int.class, InterfaceB.class.getDeclaredMethod("getB1"), null, null, ClassC.class,
                props.get("b1"));
    }

    @Test
    public void testGetPropertyPath() {
        PropertyPath path = PropertyUtil.getPropertyPath(ClassA.class, x -> x.getA());
        assertEquals("a", ((PropertyNode) path.nodes.get(0)).property.getName());
    }

    class ClassD {
        int foo;
    }

    @Test
    public void testFieldOnly() throws Exception {
        Map<String, PropertyInfo> props = PropertyUtil.getPropertyInfoMap(ClassD.class);
        assertEquals(1, props.size());
        PropertyInfo info = props.get("foo");
        checkPropertyInfo("foo", int.class, null, null, ClassD.class.getDeclaredField("foo"), ClassD.class, info);
        ClassD d = new ClassD();
        info.setValue(d, 3);
        assertEquals(3, info.getValue(d));

    }
}
