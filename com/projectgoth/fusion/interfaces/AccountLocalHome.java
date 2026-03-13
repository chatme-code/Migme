package com.projectgoth.fusion.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface AccountLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/AccountLocal";
   String JNDI_NAME = "AccountLocal";

   AccountLocal create() throws CreateException;
}
