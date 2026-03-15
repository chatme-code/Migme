package com.projectgoth.fusion.interfaces;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBLocalHome;

public interface ContactLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/ContactLocal";
   String JNDI_NAME = "ContactLocal";

   ContactLocal create() throws CreateException;
}
