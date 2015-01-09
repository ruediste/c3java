package com.github.ruediste.c3java.linearization;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.github.ruediste.c3java.linearization.DefaultDirectSuperclassesInspector;
import com.github.ruediste.c3java.linearization.JavaC3;
import com.google.common.collect.ImmutableList;

public class JavaC3Test {

	@Test
	public void testWidening() {
		// this is here because it won't compile if my
		// assumptions about widening are wrong...
		byte b = 1;
		int i = b;
		char c = '1';
		i = c;
		long l = i;
		float f = l;
		@SuppressWarnings("unused")
		double d = f;
	}

	@Test
	public void testDirectSuperClasses() {
		assertEquals(ImmutableList.of(AbstractSet.class, Set.class,
				Cloneable.class, Serializable.class),
				DefaultDirectSuperclassesInspector.INSTANCE
						.directParentClasses(HashSet.class));
	}

	@Test
	public void testSet() throws Exception {
		Iterable<Class<?>> linearization = JavaC3.allSuperclasses(Set.class);
		assertEquals(ImmutableList.of(Set.class, Collection.class,
				Iterable.class, Object.class), linearization);
	}

	@Test
	public void testAbstractSet() throws Exception {
		Iterable<Class<?>> linearization = JavaC3
				.allSuperclasses(AbstractSet.class);
		assertEquals(ImmutableList.of(AbstractSet.class,
				AbstractCollection.class, Set.class, Collection.class,
				Iterable.class, Object.class), linearization);
	}

	@Test
	public void testHashSet() throws Exception {
		Iterable<Class<?>> linearization = JavaC3
				.allSuperclasses(HashSet.class);
		assertEquals(ImmutableList.of(HashSet.class, AbstractSet.class,
				AbstractCollection.class, Set.class, Collection.class,
				Iterable.class, Cloneable.class, Serializable.class,
				Object.class), linearization);
	}

	@Test
	public void testPrimitives() throws Exception {
		Iterable<Class<?>> linearization = JavaC3.allSuperclasses(Short.TYPE);
		assertEquals(ImmutableList.of(short.class, byte.class), linearization);
		linearization = JavaC3.allSuperclasses(Double.TYPE);
		assertEquals(ImmutableList.of(double.class, float.class, long.class,
				int.class, short.class, byte.class, char.class), linearization);
		linearization = JavaC3.allSuperclasses(Boolean.TYPE);
		assertEquals(ImmutableList.of(boolean.class), linearization);
	}

	@Test
	public void testArrays() throws Exception {
		Set<?>[] array1 = new HashSet[0];
		Iterable<Class<?>> linearization = JavaC3.allSuperclasses(array1
				.getClass());
		System.out.println("HashSet[]: " + linearization);
		Set<?>[][] array2 = new HashSet[0][0];
		linearization = JavaC3.allSuperclasses(array2.getClass());
		System.out.println("HashSet[][]: " + linearization);
	}
}
