package com.github.ruediste.c3java.properties;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.github.ruediste.c3java.invocationRecording.MethodInvocation;
import com.github.ruediste.c3java.invocationRecording.MethodInvocationUtil;

/**
 * Path from a root object, traversing method calls or properties.
 * 
 * <p>
 * If the last node in the path is a property, it can be retrieved using
 * {@link #getAccessedProperty()}
 * 
 */
public class PropertyPath {
    public final List<PropertyPathNode> nodes = new ArrayList<>();

    public interface PropertyPathNode {
        Object evaluate(Object target);

        /**
         * Return the segment to be used for {@link PropertyPath#getPath()}
         */
        String pathSegment();
    }

    public static class PropertyNode implements PropertyPathNode {
        public final PropertyInfo property;

        public PropertyNode(PropertyInfo property) {
            this.property = property;
        }

        @Override
        public Object evaluate(Object target) {
            return property.getValue(target);
        }

        @Override
        public String toString() {
            return "PropertyNode[" + property.getName() + "]";
        }

        @Override
        public String pathSegment() {
            return property.getName();
        }
    }

    public static class MethodNode implements PropertyPathNode {
        public MethodInvocation<Object> invocation;

        MethodNode(MethodInvocation<Object> invocation) {
            this.invocation = invocation;
        }

        @Override
        public Object evaluate(Object target) {
            return MethodInvocationUtil.invoke(target, invocation);
        }

        @Override
        public String toString() {
            return "MethodNode[" + invocation + "]";
        }

        @Override
        public String pathSegment() {
            return invocation.getMethod().getName() + "("
                    + invocation.getArguments().stream().map(Objects::toString)
                            .collect(Collectors.joining(","))
                    + ")";
        }
    }

    public PropertyPathNode getLastNode() {
        if (nodes.size() == 0)
            throw new RuntimeException("Path is empty");
        return nodes.get(nodes.size() - 1);
    }

    public PropertyInfo getAccessedProperty() {
        PropertyPathNode lastNode = getLastNode();
        if (!(lastNode instanceof PropertyNode))
            throw new RuntimeException(
                    "Path does not end with a property but with a " + lastNode);
        return ((PropertyNode) lastNode).property;
    }

    /**
     * Invoke all nodes of this path in order
     */
    public Object evaluate(Object target) {
        Object result = target;
        for (PropertyPathNode node : nodes) {
            result = node.evaluate(result);
        }
        return result;
    }

    /**
     * Evaluate all nodes of this path except for the last. The last node needs
     * to be a {@link PropertyNode}. The represented property will be set to the
     * specified value
     */
    public void set(Object target, Object value) {
        Object current = target;
        for (int i = 0; i < nodes.size() - 1; i++) {
            current = nodes.get(i).evaluate(current);
        }
        getAccessedProperty().setValue(current, value);
    }

    /**
     * Get a string representation of this path in the form of
     * "propertyName"."propertyName". ...
     */
    public String getPath() {
        return nodes.stream().map(n -> n.pathSegment()).collect(joining("."));
    }
}
