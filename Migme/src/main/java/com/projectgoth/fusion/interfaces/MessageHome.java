package com.projectgoth.fusion.interfaces;

import java.rmi.RemoteException;
import jakarta.ejb.CreateException;
import jakarta.ejb.EJBHome;

public interface MessageHome extends EJBHome {
   String COMP_NAME = "java:comp/env/ejb/Message";
   String JNDI_NAME = "ejb/Message";

   Message create() throws CreateException, RemoteException;
}
