package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.ejb.FusionEJBException;
import java.rmi.RemoteException;
import javax.ejb.EJBObject;

public interface Guardset extends EJBObject {
   Short getMinimumClientVersionForAccess(int var1, int var2) throws FusionEJBException, RemoteException;
}
