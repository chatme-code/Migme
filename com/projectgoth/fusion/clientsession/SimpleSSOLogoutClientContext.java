package com.projectgoth.fusion.clientsession;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.ejb.EJBIcePrxFinder;
import com.projectgoth.fusion.slice.ConnectionPrx;
import javax.ejb.EJBException;
import org.apache.log4j.Logger;

public class SimpleSSOLogoutClientContext implements SSOLogoutClientContext {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SimpleSSOLogoutClientContext.class));

   public void postLogout(SSOLogoutSessionInfo sessionInfo) {
      try {
         ConnectionPrx connectionPrx = EJBIcePrxFinder.findConnectionPrx(sessionInfo.sessionID);
         if (connectionPrx != null) {
            connectionPrx.logout();
         }
      } catch (EJBException var3) {
      } catch (Exception var4) {
         log.error(String.format("Unable to logout fusion session %s", sessionInfo.sessionID), var4);
      }

   }
}
