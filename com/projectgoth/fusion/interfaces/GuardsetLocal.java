package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.ejb.FusionEJBException;
import javax.ejb.EJBLocalObject;

public interface GuardsetLocal extends EJBLocalObject {
   Short getMinimumClientVersionForAccess(int var1, int var2) throws FusionEJBException;
}
