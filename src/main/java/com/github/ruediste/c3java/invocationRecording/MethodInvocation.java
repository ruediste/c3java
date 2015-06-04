package com.github.ruediste.c3java.invocationRecording;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import com.google.common.base.MoreObjects;
import com.google.common.reflect.TypeToken;

/**
 * The record of a method invocation
 */
public class MethodInvocation<T> {

    private final List<T> arguments;
    final private TypeToken<?> instanceType;
    final private Method method;

    public MethodInvocation(TypeToken<?> instanceType, Method method,
            List<T> arguments) {
        this.instanceType = instanceType;
        this.method = method;
        this.arguments = new ArrayList<>(arguments);
    }

    public <X> MethodInvocation<X> withArguments(List<X> arguments) {
        return new MethodInvocation<>(instanceType, method, arguments);
    }

    /**
     * The list of argument objects used during the invocation
     */
    public List<T> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public <O> boolean isCallToSameMethod(MethodInvocation<O> other,
            BiPredicate<? super T, ? super O> comparator) {
        if (method != other.method) {
            return false;
        }
        if (arguments.size() != other.getArguments().size()) {
            return false;
        }

        Iterator<T> it = arguments.iterator();
        Iterator<O> oit = other.getArguments().iterator();
        while (it.hasNext() && oit.hasNext()) {
            if (!comparator.test(it.next(), oit.next())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("method", method)
                .add("arguments", arguments).toString();
    }

    /**
     * The method which was invoked
     */
    public Method getMethod() {
        return method;
    }

    /**
     * The class of the instance the method was called on. Can be different from
     * {@link Method#getDeclaringClass()}
     */
    public TypeToken<?> getInstanceType() {
        return instanceType;
    }

    public <R> MethodInvocation<R> map(
            BiFunction<AnnotatedType, ? super T, R> func) {
        ArrayList<R> args = new ArrayList<>();
        Iterator<AnnotatedType> pit = Arrays.asList(
                getMethod().getAnnotatedParameterTypes()).iterator();
        Iterator<T> ait = getArguments().iterator();
        while (pit.hasNext() && ait.hasNext()) {
            args.add(func.apply(pit.next(), ait.next()));
        }
        return withArguments(args);
    }
}
