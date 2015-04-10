package com.github.ruediste.c3java.properties;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.github.ruediste.c3java.invocationRecording.InvocationRecorder;
import com.google.common.reflect.TypeToken;

@SuppressWarnings("serial")
public class PropertyHandleTest {

	static interface TestClass<T> {
		T getT();

		String getString();

		void setString(String value);

		T funkyMethod(String dummy, int foo);
	}

	InvocationRecorder recorder;
	PropertyUtil util;

	TestClass<?> object;

	@Before
	public void setup() {
		recorder = new InvocationRecorder();
		util = new PropertyUtil();
		object = Mockito.mock(TestClass.class);
		Mockito.<Object> when(object.getT()).thenReturn(object);
		Mockito.<Object> when(object.funkyMethod(anyString(), anyInt()))
				.thenReturn(object);
	}

	@Test
	public void testSimple() {
		recorder.record(TypeToken.of(TestClass.class)).setString("null");
		PropertyHandle handle = util.toHandle(recorder.getInvocations());
		handle.setValue(object, "bar");
		verify(object).setString("bar");
	}

	@Test
	public void testNested() {
		recorder.record(new TypeToken<TestClass<TestClass<Double>>>() {
		}).getT().setString(null);
		PropertyHandle handle = util.toHandle(recorder.getInvocations());
		handle.getValue(object);
		InOrder order = inOrder(object);
		order.verify(object).getT();
		order.verify(object).getString();
	}

	@Test
	public void testNestedWithMethod() {
		recorder.record(new TypeToken<TestClass<TestClass<Double>>>() {
		}).funkyMethod(null, 3).getString();
		PropertyHandle handle = util.toHandle(recorder.getInvocations());
		handle.getValue(object);
		InOrder order = inOrder(object);
		order.verify(object).funkyMethod(null, 3);
		order.verify(object).getString();
	}
}
