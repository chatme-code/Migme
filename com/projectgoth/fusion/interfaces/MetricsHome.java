package com.projectgoth.fusion.interfaces;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface MetricsHome extends EJBHome {
   String COMP_NAME = "java:comp/env/ejb/Metrics";
   String JNDI_NAME = "ejb/Metrics";

   Metrics create() throws CreateException, RemoteException;
}
