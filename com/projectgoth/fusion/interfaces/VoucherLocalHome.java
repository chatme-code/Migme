package com.projectgoth.fusion.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface VoucherLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/VoucherLocal";
   String JNDI_NAME = "VoucherLocal";

   VoucherLocal create() throws CreateException;
}
