package com.projectgoth.fusion.interfaces;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface MerchantsHome extends EJBHome {
   String COMP_NAME = "java:comp/env/ejb/Merchants";
   String JNDI_NAME = "ejb/Merchants";

   Merchants create() throws CreateException, RemoteException;
}
