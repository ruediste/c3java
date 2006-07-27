package net.lshift.java.rmi;

import java.rmi.RemoteException;
import java.util.Set;

public class RemoteSetAdaptor
    extends RemoteCollectionAdaptor
    implements Set
{
    /**
     * Get Set adaptor for a remote collection.
     * Sets interface doesn't actually differ from that of
     * collection, so its not neccessary to define an additional
     * remote class.
     * @param collection
     * @throws RemoteException
     */
    public RemoteSetAdaptor(RemoteCollection collection)
        throws RemoteException
    {
        super(collection);
    }
}
