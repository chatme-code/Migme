package com.projectgoth.fusion.interfaces;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBLocalHome;

public interface WebLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/WebLocal";
   String JNDI_NAME = "WebLocal";

   WebLocal create() throws CreateException;
}
