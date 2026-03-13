package com.projectgoth.fusion.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface VoiceLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/VoiceLocal";
   String JNDI_NAME = "VoiceLocal";

   VoiceLocal create() throws CreateException;
}
