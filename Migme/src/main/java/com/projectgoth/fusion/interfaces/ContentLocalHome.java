package com.projectgoth.fusion.interfaces;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBLocalHome;

public interface ContentLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/ContentLocal";
   String JNDI_NAME = "ContentLocal";

   ContentLocal create() throws CreateException;
}
