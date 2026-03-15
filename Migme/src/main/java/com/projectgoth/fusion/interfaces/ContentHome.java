package com.projectgoth.fusion.interfaces;

import java.rmi.RemoteException;
import jakarta.ejb.CreateException;
import jakarta.ejb.EJBHome;

public interface ContentHome extends EJBHome {
   String COMP_NAME = "java:comp/env/ejb/Content";
   String JNDI_NAME = "ejb/Content";

   Content create() throws CreateException, RemoteException;
}
