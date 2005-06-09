
package net.lshift.java.data;

import java.lang.reflect.*;
import java.beans.*;
import java.util.*;

/**
 * Given an interface which defines getters and setters, generate a
 * class which implements those getters and setters using a map.
 *
 * This is really to encourage the definition of interfaces -
 * you won't have the excuse that you have to write two classes. I
 * suggest you construct immutable versions of your bean
 * by using Collections.unmodifiableMap when required. Alternatively
 * you can use the listener to veto changes of specific properties
 */
public class StoreBean
{
    protected static Map MAP_BEAN_INFO = new WeakHashMap();

    protected static class StoreBeanInfo
    {
	public Class type;
	// Map from the method to the property name
	public Map readers = new HashMap();
	public Map writers = new HashMap();
	// Set of properties which are constriained
	public Set constrained = new HashSet();

	public StoreBeanInfo(Class type)
	    throws Exception
	{
	    this.type = type;

	    BeanInfo info = Introspector.getBeanInfo(type);
	    PropertyDescriptor [] descriptors = info.getPropertyDescriptors();
	    for(int i = 0; i != descriptors.length; ++i) {
		if(descriptors[i].getReadMethod() != null) {
		    readers.put(descriptors[i].getReadMethod(),
				descriptors[i].getName());
		}

		if(descriptors[i].getWriteMethod() != null) {
		    writers.put(descriptors[i].getWriteMethod(),
				descriptors[i].getName());
		    if(descriptors[i].isConstrained())
			constrained.add(descriptors[i].getName());
		}

	    }
	}
    }

    protected static synchronized StoreBeanInfo info(Class type)
	throws Exception
    {
	StoreBeanInfo info = (StoreBeanInfo)MAP_BEAN_INFO.get(type);
	if(info == null) {
	    info = new StoreBeanInfo(type);
	    MAP_BEAN_INFO.put(type, info);
	}

	return info;
    }

    // ----------------------------------------------------------------------------------

    final Set propertyChangeListeners = new HashSet();
    final Set vetoableChangeListeners = new HashSet();
    final Store store;

    public StoreBean(final Store store)
    {
	this.store = store;
    }

    public StoreBean(final Map store)
    {
	this.store = new Store() {
		public Object get(String fieldName) {
		    return store.get(fieldName);
		}

		public void set(String fieldName, Object fieldValue) {
		    store.put(fieldName, fieldValue);
		}
	    };
    }

    /**
     * Create an instance.
     * @param bean - the bean interface to implement. It must
     * consist strictly of getters and setters.
     * @param map the map to store the properties in. The property
     * names are used as keys.
     * @param listener to be notified of property change events and
     * veto changes.
     */
    public Object proxy(final Class bean)
	throws Exception
    {
	final StoreBeanInfo info = info(bean);

	return Proxy.newProxyInstance
	    (bean.getClassLoader(),
	     new Class [] { bean },
	     new InvocationHandler() {
		 public Object invoke(Object proxy, Method method, Object [] args)
		     throws Throwable
		 {
		     if(info.readers.containsKey(method)) {
			 return store.get((String)info.readers.get(method)); 
		     }
		     else if(info.writers.containsKey(method)) {
			 String name = (String)info.writers.get(method);
			 Object newValue = args[0];
			 notify(proxy, name, newValue);
			 store.set(name, newValue);
			 return null;
		     }
		     else {
			 throw new UnsupportedOperationException();
		     }
		 }

		 private final void notify(Object source, String name, Object newValue)
		     throws PropertyVetoException
		 {
		     boolean constrained = info.constrained.contains(name);

		     if((!vetoableChangeListeners.isEmpty() && constrained) ||
			!propertyChangeListeners.isEmpty()) {

			 PropertyChangeEvent event =
			     new PropertyChangeEvent
			     (source, name, store.get(name), newValue);

			 if(constrained) {
			     Iterator i = vetoableChangeListeners.iterator();

			     while(i.hasNext()) {
				 VetoableChangeListener listener = 
				     (VetoableChangeListener)i.next();
				 listener.vetoableChange(event);
			     }
			 }

			 Iterator i = propertyChangeListeners.iterator();

			 while(i.hasNext()) {
			     PropertyChangeListener listener = 
				 (PropertyChangeListener)i.next();
			     listener.propertyChange(event);
			 }
		     }
		 }
	     });
    }

}