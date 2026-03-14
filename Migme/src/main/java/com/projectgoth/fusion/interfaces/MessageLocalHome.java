package com.projectgoth.fusion.interfaces;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBLocalHome;

public interface MessageLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/MessageLocal";
   String JNDI_NAME = "MessageLocal";

   MessageLocal create() throws CreateException;
}
