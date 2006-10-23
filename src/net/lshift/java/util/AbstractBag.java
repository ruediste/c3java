
package net.lshift.java.util;

import java.util.*;

public abstract class AbstractBag<E>
    extends AbstractCollection<E>
    implements Bag<E>
{
    public boolean equals(Object o)
    {
        if(o instanceof Bag) {
            Bag other = (Bag)o;
            boolean result = (other.size() == size());
            if(result) {
                Collection<E> copy = new ArrayList<E>(this);
                for(Iterator i = other.iterator(); result && i.hasNext();)
                    result = copy.remove(i.next());
            }

            return result;
        }
        else {
            return false;
        }
    }

    private static class BagImpl<E>
        extends AbstractBag<E>
	implements java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        private Collection<E> store;

	public BagImpl(Collection<E> c)
	{
	    store = c;
	}

	public boolean add(E o)
	{
	    return store.add(o);
	}

	public boolean addAll(Collection<? extends E> c)
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

	public Iterator<E> iterator()
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

	public <U> U[] toArray(U [] a)
	{
	    return store.<U>toArray(a);
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

    public static <U> Bag<U> wrap(List<U> list)
    {
	return new BagImpl<U>(list);
    }

    public static <U> Bag<U> newBag()
    {
        return wrap(new ArrayList<U>());
    }
}
