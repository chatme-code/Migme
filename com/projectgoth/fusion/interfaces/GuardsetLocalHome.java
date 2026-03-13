package com.projectgoth.fusion.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface GuardsetLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/GuardsetLocal";
   String JNDI_NAME = "GuardsetLocal";

   GuardsetLocal create() throws CreateException;
}
