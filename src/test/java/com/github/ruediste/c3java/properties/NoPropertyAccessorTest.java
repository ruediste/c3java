package com.github.ruediste.c3java.properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class NoPropertyAccessorTest {

    private interface A {
        public void setI(int i);

        @NoPropertyAccessor
        public void setJ(int i);
    }

    @Test
    public void testAccessorIgnored() throws Exception {
        assertNotNull(PropertyUtil.getAccessor(A.class.getMethod("setI", int.class)));
        assertNull(PropertyUtil.getAccessor(A.class.getMethod("setJ", int.class)));
    }
}
