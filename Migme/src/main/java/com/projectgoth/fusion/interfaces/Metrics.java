package com.projectgoth.fusion.interfaces;

import java.rmi.RemoteException;
import java.util.Collection;
import jakarta.ejb.EJBObject;

public interface Metrics extends EJBObject {
   boolean logMetricsSampleSummaries(String var1, String var2, Collection var3) throws RemoteException;
}
