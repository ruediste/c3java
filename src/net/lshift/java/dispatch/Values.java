package net.lshift.java.dispatch;

import static net.lshift.java.util.Lists.any;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lshift.java.dispatch.DynamicDispatch.ClosureMethod;
import net.lshift.java.util.Transform;

public class Values
implements MethodSelectorMap
{
    
    /**
     * Select this method if value.equals(parameter.toString())
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    public @interface String {
        java.lang.String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    public @interface Integer {
        int value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    public @interface Boolean {
        int value();
    }
    

    @SuppressWarnings("unchecked")
    private static final Transform<Annotation, Object> VALUE = 
        DynamicDispatch.proxy(Transform.class, new Object() {
            @SuppressWarnings("unused")
            public Object apply(String s) {
                return s.value();
            }
            
            @SuppressWarnings("unused")
            public Object apply(Integer i) {
                return i.value();
            }
            
            @SuppressWarnings("unused")
            public Object apply(Boolean b) {
                return b.value();
            }
            
            @SuppressWarnings("unused")
            public Object apply(Object o) {
                return null;
            }
        });
    
    private static final Object ANY = new Object();
    
    @Override
    public MethodSelector getMethodSelector(
        Method multiMethod, 
        Set<ClosureMethod> methods)
    {
        int parameterCount = multiMethod.getParameterAnnotations().length;
        final List<Map<Object,Set<ClosureMethod>>> values = 
            new ArrayList<Map<Object,Set<ClosureMethod>>>(parameterCount);
        
        for(int parameter = 0; parameter != parameterCount; ++parameter) {
            Map<Object,Set<ClosureMethod>> pvalues = new HashMap<Object,Set<ClosureMethod>>();
            for(ClosureMethod method: methods) {
            
                Annotation [] annotations = method.method.getParameterAnnotations()[parameter];
                Object value = any(VALUE, Arrays.asList(annotations));
                if(value != null) {
                    if(!pvalues.containsKey(value))
                        pvalues.put(value, new HashSet<ClosureMethod>());
                    pvalues.get(value).add(method);
                }
            }

            values.set(parameter, pvalues);
        }
        
        return new MethodSelector() {
            public ClosureMethod apply(Object[] x) {
                return null;
            }
            
        };
    }


}
