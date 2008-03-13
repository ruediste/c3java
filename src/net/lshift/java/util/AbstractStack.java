package net.lshift.java.util;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractStack<E>
    extends AbstractList<E>
    implements Stack<E>
{
    /** 
     * append an item to the list
     */
    public void push(E item)
    {
        add(item);
    }
    
    /**
     * Remove the last item from the list, returning it
     * @return get(size() - 1)
     */
    public E pop()
    {
        return remove(size() - 1);
    }
    
    /**
     * Look at the item at the top of the stack.
     * Thats the last item in the list...
     */
    public E peek()
    {
        return get(size() - 1);
    }
    
    public static <E> Stack<E> stack(final List<E> list)
    {
        return new AbstractStack<E>() {

            @Override
            public E get(int index)
            {
                return list.get(index);
            }

            @Override
            public int size()
            {
                return list.size();
            }
            
            public Iterator<E> iterator()
            {
                return list.iterator();
            }
            
        };
    }

}
