package net.lshift.java.util;

import java.util.List;

/**
 * Stack semantics added to a list.
 * java.util does contain a stack, but its a concrete class, based
 * on Vector - so we define our own interface here.
 * @see java.util.Stack
 * @author david
 *
 * @param <E>
 */
public interface Stack<E>
    extends List<E>
{
    /**
     * append an item to the list
     */
    public void push(E item);

    /**
     * Remove the last item from the list, returning it
     * @return get(size() - 1)
     */
    public E pop();

    /**
     * Look at the item at the top of the stack.
     * Thats the last item in the list...
     */
    public E peek();

}
