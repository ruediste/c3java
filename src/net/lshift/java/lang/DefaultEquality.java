/*

Copyright (c) 2006 LShift Ltd

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

 */

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

    public boolean equals(Bag a, Bag b)
    {
        return EqualsHelper.equals(a, b, delegate);
    }

    public boolean equals(Set a, Set b)
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
