package com.projectgoth.fusion.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface MessageLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/MessageLocal";
   String JNDI_NAME = "MessageLocal";

   MessageLocal create() throws CreateException;
}
