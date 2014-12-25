/*
     This file is part of the LShift Java Library.

    The LShift Java Library is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    The LShift Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with The LShift Java Library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.github.ruediste1.c3java;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.github.ruediste1.c3java.JavaC3.JavaC3Exception;
import com.google.common.collect.ImmutableList;

public class ImplementsFirstDirectParentClassesReaderTest {

	@Test
	public void testDirectSuperClasses() {
		assertEquals(ImmutableList.of(Set.class, Cloneable.class,
				Serializable.class, AbstractSet.class),
				ImplementsFirstDirectParentClassesReader.INSTANCE
						.directParentClasses(HashSet.class));
	}

	@Test
	public void testCollection() throws Exception {
		Iterable<Class<?>> linearization = JavaC3.allSuperclasses(
				Collection.class,
				ImplementsFirstDirectParentClassesReader.INSTANCE);
		assertEquals(ImmutableList.of(Collection.class, Iterable.class,
				Object.class), linearization);
	}

	@Test
	public void testSet() throws Exception {
		Iterable<Class<?>> linearization = JavaC3.allSuperclasses(Set.class,
				ImplementsFirstDirectParentClassesReader.INSTANCE);
		assertEquals(ImmutableList.of(Set.class, Collection.class,
				Iterable.class, Object.class), linearization);
	}

	@Test
	public void testAbstractSet() throws Exception {
		Iterable<Class<?>> linearization = JavaC3.allSuperclasses(
				AbstractSet.class,
				ImplementsFirstDirectParentClassesReader.INSTANCE);
		assertEquals(ImmutableList.of(AbstractSet.class, Set.class,
				AbstractCollection.class, Collection.class, Iterable.class,
				Object.class), linearization);
	}

	@Test(expected = JavaC3Exception.class)
	public void testHashSet() throws Exception {
		JavaC3.allSuperclasses(
				HashSet.class, ImplementsFirstDirectParentClassesReader.INSTANCE);
	}
}
