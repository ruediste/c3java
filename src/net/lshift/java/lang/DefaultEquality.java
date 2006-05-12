/*
     This file is part of the LShift Java Library.

    The LShift Java Library is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    The LShift Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with The LShift Java Library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
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
