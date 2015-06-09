package com.github.ruediste.c3java.invocationRecording;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.google.common.reflect.TypeToken;

@SuppressWarnings("serial")
public class MethodInvocationRecorderTest {

    static interface TestClass<T> {
        T getT();

        String getString();
    }

    MethodInvocationRecorder recorder;

    @Before
    public void setup() {
        recorder = new MethodInvocationRecorder();
    }

    @Test
    public void testSingle() {
        recorder.getProxy(new TypeToken<TestClass<?>>() {
        }).getString();

        assertEquals(1, recorder.getInvocations().size());
        assertEquals(new TypeToken<TestClass<?>>() {
        }, recorder.getInvocations().get(0).getInstanceType());
        assertEquals("getString", recorder.getInvocations().get(0).getMethod()
                .getName());
    }

    @Test
    public void testGeneric() {
        recorder.getProxy(new TypeToken<TestClass<?>>() {
        }).getT().hashCode();

        assertEquals(2, recorder.getInvocations().size());
        assertEquals(new TypeToken<TestClass<?>>() {
        }, recorder.getInvocations().get(0).getInstanceType());
        assertEquals("getT", recorder.getInvocations().get(0).getMethod()
                .getName());
        assertEquals("capture#2-of ? extends class java.lang.Object", recorder
                .getInvocations().get(1).getInstanceType().toString());
        assertEquals("hashCode", recorder.getInvocations().get(1).getMethod()
                .getName());
    }

    @Test
    public void testTerminal() {
        recorder.getProxy(String.class);
        assertEquals(0, recorder.getInvocations().size());
    }

    @Test
    public void testGeneric2() {
        recorder.getProxy(new TypeToken<TestClass<TestClass<?>>>() {
        }).getT().getT().hashCode();

        assertEquals(3, recorder.getInvocations().size());
        assertEquals(new TypeToken<TestClass<TestClass<?>>>() {
        }, recorder.getInvocations().get(0).getInstanceType());
        assertEquals("getT", recorder.getInvocations().get(0).getMethod()
                .getName());
        assertEquals("getT", recorder.getInvocations().get(1).getMethod()
                .getName());
        assertEquals("hashCode", recorder.getInvocations().get(2).getMethod()
                .getName());
    }

}
