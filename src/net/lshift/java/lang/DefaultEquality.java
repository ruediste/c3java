
package net.lshift.java.lang;

import java.util.*;
import net.lshift.java.util.Bag;
import net.lshift.java.dispatch.DynamicDispatch;


public class DefaultEquality
{
    private Equality delegate;

    public DefaultEquality()
    {
        this.delegate = (Equality)DynamicDispatch.proxy(Equality.class, this);
    }

    public DefaultEquality(Equality delegate)
    {
        this.delegate = delegate;
    }

    public static Equality equality(Equality delegate)
    {
        return (Equality)DynamicDispatch.proxy
            (Equality.class, new DefaultEquality(delegate));
    }

    public Equality getDeligate()
    {
        return this.delegate;
    }

    public void setDelegate(Equality equality)
    {
        this.delegate = equality;
    }

    public boolean equals(Object a, Object b)
    {
        return EqualsHelper.equals(a, b, delegate);
    }

    public boolean equals(String a, String b)
    {
        return a.equals(b);
    }

    public boolean equals(Bag<Object> a, Bag<Object> b)
    {
        return EqualsHelper.equals(a, b, delegate);
    }

    public boolean equals(Set<Object> a, Set<Object> b)
    {
        return EqualsHelper.equals(a, b, delegate);
    }

    public boolean equals(List a, List b)
    {
        return EqualsHelper.equals(a, b, delegate);
    }

    public boolean equals(Map a, Map b)
    {
        return EqualsHelper.equals(a, b, delegate);
    }

}
