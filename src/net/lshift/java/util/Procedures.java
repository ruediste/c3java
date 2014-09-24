package net.lshift.java.util;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import net.lshift.java.lang.Variable;

public class Procedures
{
    public static <V> Predicate<V> eq(final V value) {
        return new Predicate<V>() {
            public boolean apply(V x)
            {
                return value == x;
            }
        };
    }

    public static <E> Function<List<E>, E> get(final int index) {
        return new Function<List<E>, E>() {
            @Override public E apply(List<E> input) {
                return input.get(index);
            }
        };
    }

    public static <E> Function<List<E>, E> head() {
        return get(0);
    }

    public static <E> Function<List<E>, List<E>> tail() {
        return new Function<List<E>, List<E>>() {
            @Override public List<E> apply(List<E> input) {
                return input.subList(1, input.size());
            }
        };
    }

    public static <E> Predicate<List<E>> contains(final E e) {
        return new Predicate<List<E>>() {
            @Override public boolean apply(List<E> input) {
                return input.contains(input);
            }
        };
    }

    public static <T, U> Variable<U> variable(
        final Transform<T,U> transform,
        final T value)
    {
        return new Variable<U>() {
            public U get() {
                return transform.apply(value);
            }
        };
    }
}
