package com.github.ruediste.c3java.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Objects;
import com.google.common.reflect.TypeToken;

/**
 * The property declaration of a single type regarding a single property.
 * 
 * <p>
 * This is the raw input data. To find out about how a property looks on a given
 * class, use a {@link PropertyInfo}. However, {@link PropertyDeclaration}s are
 * very useful for reflection, when you exactly want to know what's declared
 * where.
 * </p>
 */
public class PropertyDeclaration implements AnnotatedElement {
    private final String name;
    private final Class<?> declaringType;
    private final Method getter;
    private final Method setter;
    private final Field backingField;
    private final TypeToken<?> propertyType;

    public PropertyDeclaration(String name, Class<?> declaringType) {
        this(name, declaringType, null, null, null, null);
    }

    public PropertyDeclaration(String name, Class<?> declaringType, TypeToken<?> propertyType, Method getter,
            Method setter, Field backingField) {
        super();
        this.name = name;
        this.declaringType = declaringType;
        this.propertyType = propertyType;
        this.getter = getter;
        if (getter != null)
            getter.setAccessible(true);
        this.setter = setter;
        if (setter != null)
            setter.setAccessible(true);
        this.backingField = backingField;
        if (backingField != null)
            backingField.setAccessible(true);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, declaringType, getter, setter, backingField, propertyType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PropertyDeclaration other = (PropertyDeclaration) obj;
        return Objects.equal(name, other.name) && Objects.equal(declaringType, other.declaringType)
                && Objects.equal(getter, other.getter) && Objects.equal(setter, other.setter)
                && Objects.equal(backingField, other.backingField) && Objects.equal(propertyType, other.propertyType);
    }

    @Override
    public String toString() {
        return java.util.Objects.toString(declaringType) + "::" + name;
    }

    public String getName() {
        return name;
    }

    /**
     * The type containing the property declaration
     */
    public Class<?> getDeclaringType() {
        return declaringType;
    }

    public Method getGetter() {
        return getter;
    }

    public Method getSetter() {
        return setter;
    }

    public Field getBackingField() {
        return backingField;
    }

    public PropertyDeclaration withAccessor(PropertyAccessor accessor) {
        if (!matchesPropertyType(accessor))
            throw new RuntimeException(
                    "property type of " + accessor + " does not match property type " + propertyType);
        switch (accessor.getType()) {
        case GETTER:
            return new PropertyDeclaration(name, declaringType, accessor.getPropertyType(), accessor.getMethod(),
                    setter, backingField);
        case SETTER:
            return new PropertyDeclaration(name, declaringType, accessor.getPropertyType(), getter,
                    accessor.getMethod(), backingField);
        default:
            throw new RuntimeException("should not happen");
        }
    }

    public boolean matchesPropertyType(PropertyAccessor accessor) {
        return matchesPropertyType(accessor.getPropertyType());
    }

    public boolean matchesPropertyType(TypeToken<?> type) {
        return propertyType == null || propertyType.equals(type);
    }

    public PropertyDeclaration withBackingField(Field backingField) {
        TypeToken<?> fieldType = TypeToken.of(backingField.getGenericType());
        if (propertyType != null && !propertyType.equals(fieldType))
            throw new RuntimeException(
                    "field type of " + backingField + " does not match property type " + propertyType);
        return new PropertyDeclaration(name, declaringType, fieldType, getter, setter, backingField);
    }

    public PropertyInfo toInfo(Class<?> bearingType) {
        return new PropertyInfo(name, propertyType, getter, setter, backingField, this, null, bearingType);
    }

    public TypeToken<?> getPropertyType() {
        return propertyType;
    }

    public Object getValue(Object target) {
        try {
            if (getter != null)
                return getter.invoke(target);
            if (backingField != null) {
                return backingField.get(target);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Property declaration " + getDeclaringType() + "." + getName() + " is not readable");
    }

    public void setValue(Object target, Object value) {
        try {
            if (setter != null) {
                setter.invoke(target, value);
                return;
            }
            if (backingField != null) {
                backingField.set(target, value);
                return;
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException(
                "Property declaration " + getDeclaringType() + "." + getName() + " is not writeable");
    }

    private Stream<AnnotatedElement> elements() {
        return Stream.of(Optional.ofNullable(backingField), Optional.ofNullable(getter), Optional.ofNullable(setter))
                .filter(x -> x.isPresent()).map(x -> x.get());
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return elements().map(x -> Optional.ofNullable(x.getAnnotation(annotationClass))).filter(x -> x.isPresent())
                .map(x -> x.get()).findFirst().orElse(null);
    }

    @Override
    public Annotation[] getAnnotations() {
        return getAnnotationsImpl(x -> x.getAnnotations());
    }

    public Annotation[] getAnnotationsImpl(Function<AnnotatedElement, Annotation[]> f) {
        return elements().flatMap(x -> Arrays.stream(f.apply(x))).collect(Collectors.toList())
                .toArray(new Annotation[] {});
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return getAnnotationsImpl(x -> x.getDeclaredAnnotations());
    }
}