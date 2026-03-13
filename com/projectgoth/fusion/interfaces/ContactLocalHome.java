package com.projectgoth.fusion.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface ContactLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/ContactLocal";
   String JNDI_NAME = "ContactLocal";

   ContactLocal create() throws CreateException;
}
