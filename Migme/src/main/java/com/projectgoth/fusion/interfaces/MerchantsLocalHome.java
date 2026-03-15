package com.projectgoth.fusion.interfaces;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBLocalHome;

public interface MerchantsLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/MerchantsLocal";
   String JNDI_NAME = "MerchantsLocal";

   MerchantsLocal create() throws CreateException;
}
