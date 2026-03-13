package com.projectgoth.fusion.restapi.util;

import com.projectgoth.fusion.clientsession.SSOLogin;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.ejb.EJBIcePrxFinder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.slice.ConnectionPrx;
import org.apache.log4j.Logger;

public class ResourceUtil {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ResourceUtil.class));

   public static String getSID(String sessionId) throws FusionRestException {
      String sID = null;
      if (SSOLogin.isEncryptedSessionID(sessionId)) {
         sID = SSOLogin.getSessionIDFromEncryptedSessionID(sessionId);
      } else {
         sID = sessionId;
      }

      if (sID == null) {
         log.error(String.format("Failed to retreived session id.%s", sID));
         throw new FusionRestException(FusionRestException.RestException.MISSING_SESSION_ID, "Invalid session ID ");
      } else {
         return sID;
      }
   }

   public static ConnectionPrx getConnectionProxy(String sessionId) throws FusionRestException {
      String sID = getSID(sessionId);
      ConnectionPrx prx = EJBIcePrxFinder.getConnectionProxy(sID);
      if (prx == null) {
         log.error(String.format("Failed to retrieved connection from session id. %s", sID));
         throw new FusionRestException(FusionRestException.RestException.ERROR, "Invalid session ");
      } else {
         return prx;
      }
   }
}
