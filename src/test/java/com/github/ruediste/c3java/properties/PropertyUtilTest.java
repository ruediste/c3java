package com.github.ruediste.c3java.properties;

import static org.junit.Assert.assertEquals;

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

    @Test
    public void testPropertyIntroductionA() throws Exception {
        Map<String, PropertyDeclaration> props = PropertyUtil
                .getPropertyIntroductionMap(ClassA.class);
        assertEquals(1, props.size());
        assertEquals(new PropertyDeclaration("a", ClassA.class, int.class,
                ClassA.class.getDeclaredMethod("getA"), null, null),
                props.get("a"));
    }

    @Test
    public void testPropertyIntroductionB() throws Exception {
        Map<String, PropertyDeclaration> props = PropertyUtil
                .getPropertyIntroductionMap(InterfaceB.class);
        assertEquals(2, props.size());
        assertEquals(new PropertyDeclaration("b", InterfaceB.class, int.class,
                InterfaceB.class.getDeclaredMethod("getB"), null, null),
                props.get("b"));
        assertEquals(new PropertyDeclaration("b1", InterfaceB.class, int.class,
                InterfaceB.class.getDeclaredMethod("getB1"), null, null),
                props.get("b1"));
    }

    @Test
    public void testPropertyIntroductionC() throws Exception {
        Map<String, PropertyDeclaration> props = PropertyUtil
                .getPropertyIntroductionMap(ClassC.class);
        assertEquals(3, props.size());
        assertEquals(new PropertyDeclaration("a", ClassA.class, int.class,
                ClassA.class.getDeclaredMethod("getA"), null, null),
                props.get("a"));
        assertEquals(new PropertyDeclaration("b", InterfaceB.class, int.class,
                InterfaceB.class.getDeclaredMethod("getB"), null, null),
                props.get("b"));
        assertEquals(new PropertyDeclaration("b1", InterfaceB.class, int.class,
                InterfaceB.class.getDeclaredMethod("getB1"), null, null),
                props.get("b1"));
    }

    @Test
    public void testGetDeclaredProperties() throws Exception {
        Map<String, PropertyDeclaration> props = PropertyUtil
                .getDeclaredProperties(ClassC.class);
        assertEquals(2, props.size());
        assertEquals(new PropertyDeclaration("a", ClassC.class, int.class,
                ClassC.class.getDeclaredMethod("getA"), null, null),
                props.get("a"));
        assertEquals(new PropertyDeclaration("b", ClassC.class, int.class,
                ClassC.class.getDeclaredMethod("getB"), null, null),
                props.get("b"));

    }

    @Test
    public void testGetDeclaredProperties1() throws Exception {
        Map<String, PropertyDeclaration> props = PropertyUtil
                .getDeclaredProperties(TestClassProperties.class);
        assertEquals(3, props.size());
        assertEquals(new PropertyDeclaration("r", TestClassProperties.class,
                int.class, TestClassProperties.class.getDeclaredMethod("getR"),
                null, null), props.get("r"));
        assertEquals(
                new PropertyDeclaration("w", TestClassProperties.class,
                        int.class, null,
                        TestClassProperties.class.getDeclaredMethod("setW",
                                int.class), null), props.get("w"));
        assertEquals(
                new PropertyDeclaration("rW", TestClassProperties.class,
                        int.class,
                        TestClassProperties.class.getDeclaredMethod("getRW"),
                        TestClassProperties.class.getDeclaredMethod("setRW",
                                int.class), null), props.get("rW"));
    }

    @Test
    public void testPropertyInfoA() throws Exception {
        Map<String, PropertyInfo> props = PropertyUtil
                .getPropertyInfoMap(ClassA.class);
        assertEquals(1, props.size());
        assertEquals(
                new PropertyInfo("a", int.class,
                        ClassA.class.getDeclaredMethod("getA"), null,
                        ClassA.class), props.get("a"));
    }

    @Test
    public void testPropertyInfoB() throws Exception {
        Map<String, PropertyInfo> props = PropertyUtil
                .getPropertyInfoMap(InterfaceB.class);
        assertEquals(2, props.size());
        assertEquals(
                new PropertyInfo("b", int.class,
                        InterfaceB.class.getDeclaredMethod("getB"), null,
                        InterfaceB.class), props.get("b"));
        assertEquals(
                new PropertyInfo("b1", int.class,
                        InterfaceB.class.getDeclaredMethod("getB1"), null,
                        InterfaceB.class), props.get("b1"));
    }

    @Test
    public void testPropertyInfoC() throws Exception {
        Map<String, PropertyInfo> props = PropertyUtil
                .getPropertyInfoMap(ClassC.class);
        assertEquals(3, props.size());
        assertEquals(
                new PropertyInfo("a", int.class,
                        ClassA.class.getDeclaredMethod("getA"), null,
                        ClassA.class), props.get("a"));
        assertEquals(
                new PropertyInfo("b", int.class,
                        InterfaceB.class.getDeclaredMethod("getB"), null,
                        InterfaceB.class), props.get("b"));
        assertEquals(
                new PropertyInfo("b1", int.class,
                        InterfaceB.class.getDeclaredMethod("getB1"), null,
                        InterfaceB.class), props.get("b1"));
    }

    @Test
    public void testGetPropertyPath() {
        PropertyPath path = PropertyUtil.getPropertyPath(ClassA.class,
                x -> x.getA());
        assertEquals("a", ((PropertyNode) path.nodes.get(0)).property.getName());
    }
}
