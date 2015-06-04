package com.github.ruediste.c3java.properties;

import java.util.ArrayList;
import java.util.List;

import com.github.ruediste.c3java.invocationRecording.MethodInvocation;
import com.github.ruediste.c3java.invocationRecording.MethodInvocationUtil;

/**
 * Path from a root object to a property.
 * 
 * <p>
 * The difference to the {@link PropertyHandle} is that the path to the property
 * is transparent.
 */
public class PropertyPath {
    public final List<PropertyPathNode> nodes = new ArrayList<>();

    public interface PropertyPathNode {
        Object evaluate(Object target);
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

    public Object evaluate(Object target) {
        Object result = target;
        for (PropertyPathNode node : nodes) {
            result = node.evaluate(result);
        }
        return result;
    }
}
