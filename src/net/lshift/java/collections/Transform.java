
package net.lshift.java.collections;

public interface Transform<T,U>
{
    public U apply(T x);
}
