package com.projectgoth.fusion.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface MISLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/MISLocal";
   String JNDI_NAME = "MISLocal";

   MISLocal create() throws CreateException;
}
