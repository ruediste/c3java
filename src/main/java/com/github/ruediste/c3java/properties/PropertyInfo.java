package com.github.ruediste.c3java.properties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;

public class PropertyInfo {

	private final String name;
	private final Type propertyType;
	private final Method getter;
	private final Method setter;

	public PropertyInfo(String name, Type propertyType, Method getter,
			Method setter) {
		this.name = name;
		this.propertyType = propertyType;
		this.getter = getter;
		this.setter = setter;
	}

	public Object getValue(Object target) {
		try {
			return getter.invoke(target);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public void setValue(Object target, Object value) {
		try {
			setter.invoke(target, value);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean canRead() {
		return getter != null;
	}

	public boolean canWrite() {
		return setter != null;
	}

	public String getName() {
		return name;
	}

	public Type getPropertyType() {
		return propertyType;
	}

	public PropertyInfo withGetter(Method getter) {
		return new PropertyInfo(name, propertyType, getter, setter);
	}

	public PropertyInfo withSetter(Method setter) {
		return new PropertyInfo(name, propertyType, getter, setter);
	}

	public PropertyInfo mergedWith(PropertyInfo info) {
		if (!Objects.equals(name, info.getName()))
			throw new RuntimeException(
					"names of to be merged properties does not match");
		if (!Objects.equals(propertyType, info.getPropertyType()))
			throw new RuntimeException(
					"types of to be merged properties does not match");
		PropertyInfo result = this;
		if (getter == null && info.getGetter() != null)
			result = result.withGetter(info.getGetter());
		if (setter == null && info.getSetter() != null)
			result = result.withSetter(info.getSetter());

		return result;
	}

	public Method getGetter() {
		return getter;
	}

	public Method getSetter() {
		return setter;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, propertyType, getter, setter);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyInfo other = (PropertyInfo) obj;
		return Objects.equals(name, other.name)
				&& Objects.equals(propertyType, other.propertyType)
				&& Objects.equals(getter, other.getter)
				&& Objects.equals(setter, other.setter);

	}

	@Override
	public String toString() {
		return "PropertyInfo [name=" + name + ", propertyType=" + propertyType
				+ ", getter=" + getter + ", setter=" + setter + "]";
	}

}
