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

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class InductiveEquality
    implements Equality
{
    private static final Map ASSUMPTIONS = new HashMap();
    private Equality delegate;

    public Equality getDelegate()
    {
        return delegate;
    }

    public void setDelegate(Equality delegate)
    {
        this.delegate = delegate;
    }

    private static class Equals
    {
	public Object a;
	public Object b;

	public Equals(Object a, Object b)
	{
	    this.a = a;
	    this.b = b;
	}

	public boolean equals(Object o)
	{
	    if(o instanceof Equals) {
		Equals other = (Equals)o;
		return ((this.a == other.a && this.b == other.b) ||
			(this.b == other.a && this.a == other.b));
	    }
	    else {
		return false;
	    }
	}

	public int hashCode()
	{
	    int ha = System.identityHashCode(a);
	    int hb = System.identityHashCode(b);

	    // these are probably pointers converted to
	    // integers, so assume that when hashing. Ie.
	    // large parts of the hash code would be the same
	    return (ha > hb) ? (ha>>>17)^hb : (hb>>>17)^ha;
	}
    }

    public boolean equals(Object a, Object b)
    {
	Thread thread = Thread.currentThread();
	if(ASSUMPTIONS.containsKey(thread)) {
	    return equals((Set)ASSUMPTIONS.get(thread), a, b);
	}
	else {
	    Set assumptions = new HashSet();
	    ASSUMPTIONS.put(thread, assumptions);

	    try {
		return equals(assumptions, a, b);
	    }
	    finally {
		ASSUMPTIONS.remove(thread);
	    }
	}
    }

    private boolean equals(Set assumptions, Object a, Object b)
    {
	if(a == null || b == null) {
	    return a == b;
	}
	else {
	    Equals test = new Equals(a, b);
	    if(assumptions.contains(test)) {
		return true;
	    }
	    else {
		assumptions.add(test);
		try {
		    return delegate.equals(a, b);
		}
		finally {
		    assumptions.remove(test);
		}
	    }
	}
    }

}