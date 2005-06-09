
package net.lshift.java.data;

public interface Store
{
    public Object get(String propertyName);

    public void set(String propertyName, Object propertyValue);
}