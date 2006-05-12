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
