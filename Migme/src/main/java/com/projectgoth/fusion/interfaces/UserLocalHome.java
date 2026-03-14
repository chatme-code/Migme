package com.projectgoth.fusion.interfaces;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBLocalHome;

public interface UserLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/UserLocal";
   String JNDI_NAME = "UserLocal";

   UserLocal create() throws CreateException;
}
