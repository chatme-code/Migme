package com.projectgoth.fusion.interfaces;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface ContactHome extends EJBHome {
   String COMP_NAME = "java:comp/env/ejb/Contact";
   String JNDI_NAME = "ejb/Contact";

   Contact create() throws CreateException, RemoteException;
}
