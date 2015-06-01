package com.github.ruediste.c3java.linearization;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.github.ruediste.c3java.linearization.ImplementsFirstDirectSuperclassesInspector;
import com.github.ruediste.c3java.linearization.JavaC3;
import com.github.ruediste.c3java.linearization.JavaC3.JavaC3Exception;
import com.google.common.collect.ImmutableList;

public class ImplementsFirstDirectSuperclassesInspectorTest {

    @Test
    public void testDirectSuperClasses() {
        assertEquals(ImmutableList.of(Set.class, Cloneable.class,
                Serializable.class, AbstractSet.class),
                ImplementsFirstDirectSuperclassesInspector.INSTANCE
                        .directParentClasses(HashSet.class));
    }

    @Test
    public void testCollection() throws Exception {
        Iterable<Class<?>> linearization = JavaC3.allSuperclasses(
                Collection.class,
                ImplementsFirstDirectSuperclassesInspector.INSTANCE);
        assertEquals(ImmutableList.of(Collection.class, Iterable.class,
                Object.class), linearization);
    }

    @Test
    public void testSet() throws Exception {
        Iterable<Class<?>> linearization = JavaC3.allSuperclasses(Set.class,
                ImplementsFirstDirectSuperclassesInspector.INSTANCE);
        assertEquals(ImmutableList.of(Set.class, Collection.class,
                Iterable.class, Object.class), linearization);
    }

    @Test
    public void testAbstractSet() throws Exception {
        Iterable<Class<?>> linearization = JavaC3.allSuperclasses(
                AbstractSet.class,
                ImplementsFirstDirectSuperclassesInspector.INSTANCE);
        assertEquals(ImmutableList.of(AbstractSet.class, Set.class,
                AbstractCollection.class, Collection.class, Iterable.class,
                Object.class), linearization);
    }

    @Test(expected = JavaC3Exception.class)
    public void testHashSet() throws Exception {
        JavaC3.allSuperclasses(HashSet.class,
                ImplementsFirstDirectSuperclassesInspector.INSTANCE);
    }
}
