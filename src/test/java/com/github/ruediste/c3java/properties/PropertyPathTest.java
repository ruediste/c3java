package com.github.ruediste.c3java.properties;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ruediste.c3java.invocationRecording.MethodInvocationRecorder;
import com.github.ruediste.c3java.properties.PropertyPath.PropertyNode;

@RunWith(MockitoJUnitRunner.class)
public class PropertyPathTest {

    interface A {
        int getInt();
    }

    static class B {
        private int i;

        private A a;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public A getA() {
            return a;
        }

        public void setA(A a) {
            this.a = a;
        }

        public A getComplex(int foo, String bar) {
            return a;
        }
    }

    MethodInvocationRecorder recorder;

    @Mock
    A a;

    B b;

    @Before
    public void before() {
        recorder = new MethodInvocationRecorder();
        b = new B();
        b.a = a;
        when(a.getInt()).thenReturn(5);
    }

    protected PropertyPath toPath() {
        return PropertyUtil.toPath(recorder.getInvocations());
    }

    @Test
    public void testEvaluateSimple() {
        recorder.getProxy(A.class).getInt();
        assertEquals(5, toPath().evaluate(a));
    }

    @Test
    public void testEvaluateNested() {
        recorder.getProxy(B.class).getA().getInt();
        assertEquals(5, toPath().evaluate(b));
    }

    @Test
    public void testPathNested() {
        recorder.getProxy(B.class).getA().getInt();
        assertEquals("a.int", toPath().getPath());
    }

    @Test
    public void testEvaluateComplex() {
        recorder.getProxy(B.class).getComplex(0, null).getInt();
        assertEquals(5, toPath().evaluate(b));
    }

    @Test
    public void testPathComplex() {
        recorder.getProxy(B.class).getComplex(0, null).getInt();
        assertEquals("getComplex(0,null).int", toPath().getPath());
    }

    @Test
    public void testGetAccessedProperty() throws Exception {
        recorder.getProxy(B.class).getA();
        assertEquals("a", toPath().getAccessedProperty().getName());
    }

    @Test(expected = RuntimeException.class)
    public void testGetAccessedPropertyFail() throws Exception {
        recorder.getProxy(B.class).getComplex(0, null);
        toPath().getAccessedProperty();
        fail();
    }

    @Test
    public void testGetLastNode() throws Exception {
        recorder.getProxy(B.class).getA();
        assertThat(toPath().getLastNode(), is(instanceOf(PropertyNode.class)));
    }

    @Test(expected = RuntimeException.class)
    public void testGetLastNodeEmpty() throws Exception {
        toPath().getLastNode();
        fail();
    }
}
