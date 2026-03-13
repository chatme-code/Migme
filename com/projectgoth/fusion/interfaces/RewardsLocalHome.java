package com.projectgoth.fusion.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface RewardsLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/RewardsLocal";
   String JNDI_NAME = "RewardsLocal";

   RewardsLocal create() throws CreateException;
}
