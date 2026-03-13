package com.projectgoth.fusion.interfaces;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface MetricsLocalHome extends EJBLocalHome {
   String COMP_NAME = "java:comp/env/ejb/MetricsLocal";
   String JNDI_NAME = "MetricsLocal";

   MetricsLocal create() throws CreateException;
}
