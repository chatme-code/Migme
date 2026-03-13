package com.projectgoth.fusion.ejb;

import Ice.InitializationData;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.slice.AuthenticationServicePrx;
import com.projectgoth.fusion.slice.BlueLabelServicePrx;
import com.projectgoth.fusion.slice.CallMakerPrx;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.EventSystemPrx;
import com.projectgoth.fusion.slice.MessageLoggerPrx;
import com.projectgoth.fusion.slice.RecommendationDataCollectionServicePrx;
import com.projectgoth.fusion.slice.RegistryAdminPrx;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.ReputationServicePrx;
import com.projectgoth.fusion.slice.SMSSenderPrx;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import com.projectgoth.fusion.slice.UserPrx;
import javax.ejb.EJBException;
import org.apache.log4j.Logger;

public class EJBIcePrxFinder {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(EJBIcePrxFinder.class));
   private static IcePrxFinder icePrxFinder;
   private static boolean logMessagesToFile = false;

   public static RegistryPrx getRegistry() throws EJBException {
      try {
         return icePrxFinder.getRegistry(false);
      } catch (Exception var1) {
         return null;
      }
   }

   public static RegistryAdminPrx getRegistryAdmin() throws EJBException {
      try {
         return icePrxFinder.getRegistryAdmin(false);
      } catch (Exception var1) {
         log.error("failed to find registry admin proxy", var1);
         return null;
      }
   }

   public static UserPrx findUserPrx(String username) throws EJBException {
      try {
         return icePrxFinder.findUserPrx(username);
      } catch (Exception var2) {
         log.error("failed to find user proxy", var2);
         throw new EJBException(var2.getMessage());
      }
   }

   public static UserPrx findOnewayUserPrx(String username) throws EJBException {
      try {
         return icePrxFinder.findOnewayUserPrx(username);
      } catch (Exception var2) {
         throw new EJBException(var2.getMessage());
      }
   }

   public static ConnectionPrx findConnectionPrx(String sessionId) throws EJBException {
      try {
         return icePrxFinder.findConnectionPrx(sessionId);
      } catch (Exception var2) {
         log.error("failed to find connection proxy", var2);
         throw new EJBException(var2.getMessage());
      }
   }

   public static ChatRoomPrx findChatRoomPrx(String name) throws EJBException {
      try {
         return icePrxFinder.findChatRoomPrx(name);
      } catch (Exception var2) {
         throw new EJBException(var2.getMessage());
      }
   }

   public static ChatRoomPrx[] findChatRoomProxies(String[] names) throws EJBException {
      try {
         return icePrxFinder.findChatRoomProxies(names);
      } catch (Exception var2) {
         throw new EJBException(var2.getMessage());
      }
   }

   public static CallMakerPrx getCallMaker() throws EJBException {
      try {
         return icePrxFinder.getCallMaker();
      } catch (Exception var1) {
         throw new EJBException(var1.getMessage());
      }
   }

   public static SMSSenderPrx getSMSSender() throws EJBException {
      try {
         return icePrxFinder.getSMSSender();
      } catch (Exception var1) {
         throw new EJBException(var1.getMessage());
      }
   }

   public static BlueLabelServicePrx getBlueLabelService() throws EJBException {
      try {
         return icePrxFinder.getBlueLabelServiceProxy();
      } catch (Exception var1) {
         throw new EJBException(var1.getMessage());
      }
   }

   public static ReputationServicePrx getReputationService() throws EJBException {
      try {
         return icePrxFinder.getReputationServiceProxy();
      } catch (Exception var1) {
         throw new EJBException(var1.getMessage());
      }
   }

   public static MessageLoggerPrx getOnewayMessageLoggerPrx() throws EJBException {
      try {
         return icePrxFinder.getOnewayMessageLoggerPrx();
      } catch (Exception var1) {
         throw new EJBException(var1.getMessage());
      }
   }

   public static boolean logMessagesToFile() {
      return logMessagesToFile;
   }

   public static EventSystemPrx getEventSystemProxy() throws EJBException {
      try {
         return icePrxFinder.getEventSystemProxy();
      } catch (Exception var1) {
         throw new EJBException(var1.getMessage());
      }
   }

   public static EventSystemPrx getOnewayEventSystemProxy() throws EJBException {
      try {
         return icePrxFinder.getOnewayEventSystemProxy();
      } catch (Exception var1) {
         throw new EJBException(var1.getMessage());
      }
   }

   public static UserNotificationServicePrx getUserNotificationServiceProxy() throws EJBException {
      try {
         return icePrxFinder.getUserNotificationServiceProxy();
      } catch (Exception var1) {
         throw new EJBException(var1.getMessage(), var1);
      }
   }

   public static AuthenticationServicePrx getAuthenticationServiceProxy() throws EJBException {
      try {
         return icePrxFinder.getAuthenticationServiceProxy();
      } catch (Exception var1) {
         throw new EJBException(var1.getMessage());
      }
   }

   public static ConnectionPrx getConnectionProxy(String sessionId) throws EJBException {
      try {
         return icePrxFinder.findConnectionPrx(sessionId);
      } catch (Exception var2) {
         throw new EJBException(var2.getMessage());
      }
   }

   public static RecommendationDataCollectionServicePrx getRecommendationDataCollectionServicePrx() {
      try {
         return icePrxFinder.getRecommendationDataCollectionServiceProxy();
      } catch (Exception var1) {
         log.error("Unable to get RDCS proxy instance.Exception:" + var1, var1);
         throw new EJBException(var1.getMessage());
      }
   }

   static {
      try {
         String configFile = System.getProperty("ice.config");
         Properties properties = Util.createProperties();
         properties.load(configFile == null ? "Ice.cfg" : configFile);
         InitializationData initializationData = new InitializationData();
         initializationData.properties = properties;
         icePrxFinder = new IcePrxFinder(Util.initialize(new String[0], initializationData), properties);
         logMessagesToFile = properties.getPropertyAsIntWithDefault("LogMessagesToFile", 0) == 1;
      } catch (Exception var3) {
         log.error("failed to initialize ice proxy finder", var3);
      }

   }
}
