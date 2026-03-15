package com.projectgoth.fusion.interfaces;

import java.rmi.RemoteException;
import jakarta.ejb.CreateException;
import jakarta.ejb.EJBHome;

public interface VoucherHome extends EJBHome {
   String COMP_NAME = "java:comp/env/ejb/Voucher";
   String JNDI_NAME = "ejb/Voucher";

   Voucher create() throws CreateException, RemoteException;
}
