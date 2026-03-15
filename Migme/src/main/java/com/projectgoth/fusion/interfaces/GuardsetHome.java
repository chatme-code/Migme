package com.projectgoth.fusion.interfaces;

import java.rmi.RemoteException;
import jakarta.ejb.CreateException;
import jakarta.ejb.EJBHome;

public interface GuardsetHome extends EJBHome {
   String COMP_NAME = "java:comp/env/ejb/Guardset";
   String JNDI_NAME = "ejb/Guardset";

   Guardset create() throws CreateException, RemoteException;
}
