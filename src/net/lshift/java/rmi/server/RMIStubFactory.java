package net.lshift.java.rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIStubFactory
    implements StubFactory
{
    public static RMIStubFactory INSTANCE = new RMIStubFactory();

    @SuppressWarnings("unchecked")
    public <T extends Remote> T export(T delegate)
        throws RemoteException
    {
        return (T)UnicastRemoteObject.exportObject(delegate);
    }

}
