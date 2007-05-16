package net.lshift.java.rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface StubFactory
{
    public <T extends Remote> T export(T delegate)
        throws RemoteException;
}
