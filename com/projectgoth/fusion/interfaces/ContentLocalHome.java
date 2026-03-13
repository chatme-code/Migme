package com.projectgoth.fusion.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface ContentLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/ContentLocal";
   String JNDI_NAME = "ContentLocal";

   ContentLocal create() throws CreateException;
}
