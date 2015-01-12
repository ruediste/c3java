package com.github.ruediste.c3java.properties;


public class PropertyHandle implements HandleTarget {

	private HandleTarget target;
	private PropertyInfo info;

	public PropertyHandle(PropertyInfo info) {
		this(new RootHandleTarget(), info);
	}

	public PropertyHandle(HandleTarget target, PropertyInfo info) {
		this.target = target;
		this.info = info;
	}

	@Override
	public Object getValue(Object root) {
		return info.getValue(target.getValue(root));
	}

	public void setValue(Object root, Object value) {
		info.setValue(target.getValue(root), value);
	}
}
