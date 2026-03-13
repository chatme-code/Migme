package com.projectgoth.fusion.interfaces;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface AccountHome extends EJBHome {
   String COMP_NAME = "java:comp/env/ejb/Account";
   String JNDI_NAME = "ejb/Account";

   Account create() throws CreateException, RemoteException;
}
