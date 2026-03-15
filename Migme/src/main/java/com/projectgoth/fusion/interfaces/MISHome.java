package com.projectgoth.fusion.interfaces;

import java.rmi.RemoteException;
import jakarta.ejb.CreateException;
import jakarta.ejb.EJBHome;

public interface MISHome extends EJBHome {
   String COMP_NAME = "java:comp/env/ejb/MIS";
   String JNDI_NAME = "ejb/MIS";

   MIS create() throws CreateException, RemoteException;
}
