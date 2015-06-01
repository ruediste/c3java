package com.github.ruediste.c3java.properties;

public class RootHandleTarget implements TargetResolver {

    @Override
    public Object getValue(Object root) {
        return root;
    }

}
