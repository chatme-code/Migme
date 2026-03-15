package com.projectgoth.fusion.ejb;

import java.rmi.RemoteException;
import jakarta.ejb.EJBException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;

public class MessageSession extends MessageBean implements SessionBean {
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
