package net.lshift.java.lang;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Iterator;

import junit.framework.TestCase;
import net.lshift.java.util.Procedures;

public class AnnotationHelperTest
    extends TestCase
{
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface TestMethodAnnotation {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface TestTypeAnnotation {
        String value();
    }

    @TestTypeAnnotation(value="1")
    public interface TestA
    {
        @TestMethodAnnotation(value="1")
        public void a();
    }

    public class TestAImpl1
        implements TestA
    {
        public void a() { }
    }

    @TestTypeAnnotation(value="2")
    public class TestAImpl2
        implements TestA
    {
        @TestMethodAnnotation(value="2")
        public void a() { }
    }

    public class TestAImpl2x
        extends TestAImpl2
        {

        }

    public void testMethodAnnotation()
        throws Exception
    {
            assertEquals(AnnotationHelper.getAnnotation
                            (TestMethodAnnotation.class,
                             TestAImpl1.class.getMethod
                                 ("a", new Class [] {})).value(),
                            "1");
            assertEquals(AnnotationHelper.getAnnotation
                            (TestMethodAnnotation.class,
                             TestAImpl2.class.getMethod
                                 ("a", new Class [] {})).value(),
                            "2");
    }

    public void testTypeAnnotation()
        throws Exception
    {
            assertEquals("1",
                AnnotationHelper.getAnnotation(
                    TestTypeAnnotation.class,
                    TestAImpl1.class).value());
            assertEquals("2",
                AnnotationHelper.getAnnotation(
                    TestTypeAnnotation.class,
                    TestAImpl2.class).value());
            assertEquals("2",
                AnnotationHelper.getAnnotation(
                    TestTypeAnnotation.class,
                    TestAImpl2x.class).value());
    }

    private void assertTestAnnotationsPresent(
        Class<?> type, String... expectedAnnotations)
    {
        Iterator<TestTypeAnnotation> annotations =
            AnnotationHelper.getAnnotations(
                TestTypeAnnotation.class,
                type).iterator();

        for (String expected : expectedAnnotations)
        {
            assertTrue(annotations.hasNext());
            assertEquals(expected, annotations.next().value());
        }
        assertFalse(annotations.hasNext());
    }

    public void testTypeAnnotations()
        throws Exception
    {
        assertTestAnnotationsPresent(TestAImpl1.class, "1");
        assertTestAnnotationsPresent(TestAImpl2.class, "2", "1");
        assertTestAnnotationsPresent(TestAImpl2x.class, "2", "1");
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface TestInjectorA {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface TestInjectorB {

    }

    public static class InjectorSrc
    {
        @TestInjectorA
        public String a = "a";

        @TestInjectorB
        public String b = "b";
    }

    public static class InjectorDst
    {
        @TestInjectorA
        public String x;

        @TestInjectorB
        public CharSequence y;

        @TestInjectorA
        public String z;
    }

    public void testInjector()
    {
        AnnotationHelper.Injector<InjectorSrc, InjectorDst> injecter =
            AnnotationHelper.injector(
                InjectorSrc.class,
                InjectorDst.class,
                Procedures.<Annotation>any());
        System.out.println(injecter);
        InjectorDst dst = new InjectorDst();
        InjectorSrc src = new InjectorSrc();
        injecter.inject(src, dst);
        assertEquals(src.a, dst.x);
        assertEquals(src.a, dst.z);
        assertEquals(src.b, dst.y);
    }
}
