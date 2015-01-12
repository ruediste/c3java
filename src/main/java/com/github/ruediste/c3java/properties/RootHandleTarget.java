package com.github.ruediste.c3java.properties;

public class RootHandleTarget implements HandleTarget {

	@Override
	public Object getValue(Object root) {
		return root;
	}

}
