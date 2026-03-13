package com.projectgoth.fusion.interfaces;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface UserHome extends EJBHome {
   String COMP_NAME = "java:comp/env/ejb/User";
   String JNDI_NAME = "ejb/User";

   User create() throws CreateException, RemoteException;
}
