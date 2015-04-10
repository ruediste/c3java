package com.github.ruediste.c3java.properties;

/**
 * A handle of a property on some target.
 * 
 * <p>
 * To access the property, first the target is resolved using the
 * {@link TargetResolver} based on the given root object. Then the property is
 * read or written via reflection.
 * </p>
 */
public class PropertyHandle {

	private TargetResolver target;
	private PropertyInfo info;

	/**
	 * Create a new property handle which uses the root as the target value.
	 */
	public PropertyHandle(PropertyInfo info) {
		this(new RootHandleTarget(), info);
	}

	/**
	 * Create a property handle for the given target and property.
	 */
	public PropertyHandle(TargetResolver target, PropertyInfo info) {
		this.target = target;
		this.info = info;
	}

	/**
	 * Get the value of the property.
	 */
	public Object getValue(Object root) {
		return info.getValue(target.getValue(root));
	}

	/**
	 * Set the value of the property.
	 */
	public void setValue(Object root, Object value) {
		info.setValue(target.getValue(root), value);
	}
}
