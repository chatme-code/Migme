package com.projectgoth.fusion.interfaces;

import java.rmi.RemoteException;
import jakarta.ejb.CreateException;
import jakarta.ejb.EJBHome;

public interface GroupHome extends EJBHome {
   String COMP_NAME = "java:comp/env/ejb/Group";
   String JNDI_NAME = "ejb/Group";

   Group create() throws CreateException, RemoteException;
}
