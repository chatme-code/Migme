package com.projectgoth.fusion.interfaces;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBLocalHome;

public interface GroupLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/GroupLocal";
   String JNDI_NAME = "GroupLocal";

   GroupLocal create() throws CreateException;
}
