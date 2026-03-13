package com.projectgoth.fusion.ejb;

import java.rmi.RemoteException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

public class ContactSession extends ContactBean implements SessionBean {
   public void ejbActivate() throws EJBException, RemoteException {
      super.ejbActivate();
   }

   public void ejbPassivate() throws EJBException, RemoteException {
      super.ejbPassivate();
   }

   public void setSessionContext(SessionContext ctx) throws EJBException {
      super.setSessionContext(ctx);
   }

   public void unsetSessionContext() {
   }

   public void ejbRemove() throws EJBException, RemoteException {
      super.ejbRemove();
   }
}
