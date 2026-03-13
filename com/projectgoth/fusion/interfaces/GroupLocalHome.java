package com.projectgoth.fusion.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface GroupLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/GroupLocal";
   String JNDI_NAME = "GroupLocal";

   GroupLocal create() throws CreateException;
}
