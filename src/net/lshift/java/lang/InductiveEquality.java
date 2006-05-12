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