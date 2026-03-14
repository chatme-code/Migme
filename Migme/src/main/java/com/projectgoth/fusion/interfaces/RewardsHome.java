package com.projectgoth.fusion.interfaces;

import java.rmi.RemoteException;
import jakarta.ejb.CreateException;
import jakarta.ejb.EJBHome;

public interface RewardsHome extends EJBHome {
   String COMP_NAME = "java:comp/env/ejb/Rewards";
   String JNDI_NAME = "ejb/Rewards";

   Rewards create() throws CreateException, RemoteException;
}
