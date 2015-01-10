package com.github.ruediste.c3java.properties;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.github.ruediste.c3java.linearization.JavaC3;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;

/**
 * Contains the
 *
 * @author ruedi
 *
 */
public class PropertyReflectionUtil {

	/**
	 * Helper class used to simplify repeated map.put calls
	 */
	private static class Putter {

		private Map<String, PropertyDeclaration> map;
		private Class<?> type;

		private Putter(Map<String, PropertyDeclaration> map, Class<?> type) {
			this.map = map;
			this.type = type;
		}

		private void put(String key, Function<PropertyDeclaration, PropertyDeclaration> func) {
			PropertyDeclaration property = map.get(key);
			if (property == null) {
				property = new PropertyDeclaration(key, type, null, null, null);
			}
			map.put(key, func.apply(property));
		}
	}

	/**
	 * Return all properties which are directly declared on the provided type
	 */
	public Map<String, PropertyDeclaration> getDeclaredProperties(Class<?> type) {
		Map<String, PropertyDeclaration> result = new HashMap<>();

		// scan methods
		for (Method method : type.getDeclaredMethods()) {
			if (Modifier.isPrivate(method.getModifiers())
					|| Modifier.isStatic(method.getModifiers())) {
				continue;
			}

			Putter putter = new Putter(result, type);

			// check for getters
			if (method.getName().startsWith("get")) {
				String name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,
						method.getName().substring("get".length()));

				// getFoo()
				if (method.getParameterCount() == 0) {
					putter.put(name, p -> p.withGetter(method));
					continue;
				}

				// getFoo(1)
				if (method.getParameterCount() == 1
						&& Integer.TYPE.equals(method.getParameterTypes()[0])) {
					putter.put(name, p -> p.withGetter(method));
					continue;
				}

			}

			// check for setters

			if (method.getName().startsWith("set")) {
				String name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,
						method.getName().substring("set".length()));
				// setFoo()
				if (method.getParameterCount() == 1) {
					putter.put(name, p -> p.withSetter(method));
					continue;
				}

				// setFoo(1,x)
				if (method.getParameterCount() == 2
						&& Integer.TYPE.equals(method.getParameterTypes()[0])) {
					putter.put(name, p -> p.withSetter(method));
					continue;
				}
			}
		}
		// fill backing fields
		for (Field f : type.getDeclaredFields()) {
			String name = f.getName();
			PropertyDeclaration property = result.get(name);
			if (property != null) {
				result.put(name, property.withBackingField(f));
			}
		}
		return result;
	}

	public PropertyDeclaration getPropertyIntroduction(Class<?> type, String name) {
		return getPropertyIntroductionMap(type).get(name);
	}

	/**
	 * Return the {@link PropertyDeclaration}s of the given type. For each property, the
	 * property declaration which introduced the property is returned.
	 */
	public Map<String, PropertyDeclaration> getPropertyIntroductionMap(Class<?> type) {
		Map<String, PropertyDeclaration> result = new HashMap<>();

		for (Class<?> cls : Lists.reverse(Lists.newArrayList(JavaC3
				.allSuperclasses(type)))) {
			if (Object.class.equals(cls)) {
				continue;
			}
			for (PropertyDeclaration prop : getDeclaredProperties(cls).values()) {
				result.putIfAbsent(prop.getName(), prop);
			}
		}

		return result;
	}

}
