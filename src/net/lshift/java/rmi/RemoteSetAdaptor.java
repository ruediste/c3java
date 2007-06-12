package net.lshift.java.rmi;

import java.rmi.RemoteException;
import java.util.Set;

public class RemoteSetAdaptor<E>
    extends RemoteCollectionAdaptor<E>
    implements Set<E>
{
    /**
     * Get Set adaptor for a remote collection.
     * Sets interface doesn't actually differ from that of
     * collection, so its not neccessary to define an additional
     * remote class.
     * @param collection
     * @throws RemoteException
     */
    public RemoteSetAdaptor(RemoteCollection<E> collection)
        throws RemoteException
    {
        super(collection);
    }
}
