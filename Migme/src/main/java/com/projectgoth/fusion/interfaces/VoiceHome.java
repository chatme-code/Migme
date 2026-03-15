package com.projectgoth.fusion.interfaces;

import java.rmi.RemoteException;
import jakarta.ejb.CreateException;
import jakarta.ejb.EJBHome;

public interface VoiceHome extends EJBHome {
   String COMP_NAME = "java:comp/env/ejb/Voice";
   String JNDI_NAME = "ejb/Voice";

   Voice create() throws CreateException, RemoteException;
}
