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

package net.lshift.java.util;

import java.util.*;

public abstract class AbstractBag
    extends AbstractCollection
    implements Bag
{
    public boolean equals(Object o)
    {
        if(o instanceof Bag) {
            Bag other = (Bag)o;
            boolean result = (other.size() == size());
            if(result) {
                Collection copy = new ArrayList(this);
                for(Iterator i = other.iterator(); result && i.hasNext();)
                    result = copy.remove(i.next());
            }

            return result;
        }
        else {
            return false;
        }
    }

    private static class BagImpl
        extends AbstractBag
	implements java.io.Serializable
    {
	private Collection store;

	public BagImpl(Collection c)
	{
	    store = c;
	}

	public boolean add(Object o)
	{
	    return store.add(o);
	}

	public boolean addAll(Collection c)
	{
	    return store.addAll(c);
	}

	public void clear()
	{
	    store.clear();
	}

	public boolean contains(Object o)
	{
	    return store.contains(o);
	}

	public boolean containsAll(Collection c)
	{
	    return store.containsAll(c);
	}

	public boolean isEmpty()
	{
	    return store.isEmpty();
	}

	public Iterator iterator()
	{
	    return store.iterator();
	}

	public boolean remove(Object o)
	{
	    return store.remove(o);
	}

	public boolean removeAll(Collection c)
	{
	    return store.removeAll(c);
	}

	public boolean retainAll(Collection c)
	{
	    return store.retainAll(c);
	}

	public int size()
	{
	    return store.size();
	}

	public Object [] toArray()
	{
	    return store.toArray();
	}

	public Object [] toArray(Object [] a)
	{
	    return store.toArray(a);
	}

	public int hashCode()
	{
	    int sum = 0;
	    for(Iterator i = iterator(); i.hasNext();)
		sum += i.next().hashCode();
	    return sum;
	}

	public String toString()
	{
	    return "Bag(" + store.toString() + ")";
	}
    }

    public static Bag wrap(List list)
    {
	return new BagImpl(list);
    }

    public static Bag newBag()
    {
        return wrap(new ArrayList());
    }
}
