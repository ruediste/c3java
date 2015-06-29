package com.github.ruediste.c3java.method;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.junit.Test;

public class MethodUtilTest {

    private Method Bab;
    private Method Bb;
    private Method Aa;
    private Method Aab;
    private Method Ap;
    private Method Bp;
    private Method Ac;
    private Method Bc;

    private static class A {
        void a() {
        };

        void ab() {
        };

        private void p() {
        }

        private void c() {
        }

    }

    private static class B extends A {
        @Override
        void ab() {
        };

        void b() {
        };

        private void p() {
        }

        public void c() {
        };

    }

    public MethodUtilTest() throws Throwable {
        Aa = A.class.getDeclaredMethod("a");
        Aab = A.class.getDeclaredMethod("ab");
        Ap = A.class.getDeclaredMethod("p");
        Ac = A.class.getDeclaredMethod("c");

        Bab = B.class.getDeclaredMethod("ab");
        Bb = B.class.getDeclaredMethod("b");
        Bp = B.class.getDeclaredMethod("p");
        Bc = B.class.getDeclaredMethod("c");

    }

    @Test
    public void testGetDeclarations() throws Exception {
        assertThat(MethodUtil.getDeclarations(Aa), contains(Aa));
        assertThat(MethodUtil.getDeclarations(Bb), contains(Bb));
        assertThat(MethodUtil.getDeclarations(Bab), contains(Bab, Aab));
    }

    @Test
    public void testGetDeclarationsPrivate() throws Exception {
        assertThat(MethodUtil.getDeclarations(Ap), contains(Ap));
        assertThat(MethodUtil.getDeclarations(Bp), contains(Bp));
        assertThat(MethodUtil.getDeclarations(Ac), contains(Ac));
        assertThat(MethodUtil.getDeclarations(Bc), contains(Bc));
    }

}
