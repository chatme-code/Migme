package com.projectgoth.fusion.interfaces;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface WebHome extends EJBHome {
   String COMP_NAME = "java:comp/env/ejb/Web";
   String JNDI_NAME = "ejb/Web";

   Web create() throws CreateException, RemoteException;
}
