package com.github.ruediste.c3java.invocationRecording;

import static org.junit.Assert.assertEquals;

import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

public class MethodInvocationTest {

    private MethodInvocationRecorder recorder;

    interface A {
        void a();

        void b(int i);
    }

    @Before
    public void before() {
        recorder = new MethodInvocationRecorder();
    }

    @Test
    public void testIsCallToSameMethod() throws Exception {
        A proxy = recorder.getProxy(A.class);
        proxy.a(); // 0
        proxy.a(); // 1
        proxy.b(1);// 2
        proxy.b(1);// 3
        proxy.b(2);// 4
        assertEquals(true, isCallToSame(0, 0));
        assertEquals(true, isCallToSame(0, 1));
        assertEquals(false, isCallToSame(0, 2));
        assertEquals(true, isCallToSame(2, 2));
        assertEquals(true, isCallToSame(2, 3));
        assertEquals(false, isCallToSame(2, 4));
    }

    private boolean isCallToSame(int idx1, int idx2) {
        return get(idx1).isCallToSameMethod(get(idx2),
                (a, b) -> Objects.equals(a, b));
    }

    private MethodInvocation<Object> get(int idx) {
        return recorder.getInvocations().get(idx);
    }
}
