package net.lshift.java.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import junit.framework.TestCase;

public class AnnotationHelperTest
    extends TestCase
{
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface TestMethodAnnotation {
        String value();
    }

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
    
    public class TestAImpl2
        implements TestA
    {
        @TestMethodAnnotation(value="2")
        public void a() { }
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
}
