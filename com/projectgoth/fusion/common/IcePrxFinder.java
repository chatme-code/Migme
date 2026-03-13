package com.projectgoth.fusion.common;

import Ice.Communicator;
import Ice.ObjectPrx;
import Ice.Properties;
import com.projectgoth.fusion.recommendation.collector.DataCollectorUtils;
import com.projectgoth.fusion.slice.AuthenticationServicePrx;
import com.projectgoth.fusion.slice.AuthenticationServicePrxHelper;
import com.projectgoth.fusion.slice.BlueLabelServicePrx;
import com.projectgoth.fusion.slice.BlueLabelServicePrxHelper;
import com.projectgoth.fusion.slice.CallMakerPrx;
import com.projectgoth.fusion.slice.CallMakerPrxHelper;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.EmailAlertPrx;
import com.projectgoth.fusion.slice.EmailAlertPrxHelper;
import com.projectgoth.fusion.slice.EventSystemPrx;
import com.projectgoth.fusion.slice.EventSystemPrxHelper;
import com.projectgoth.fusion.slice.MessageLoggerPrx;
import com.projectgoth.fusion.slice.MessageLoggerPrxHelper;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RecommendationDataCollectionServicePrx;
import com.projectgoth.fusion.slice.RecommendationGenerationServicePrx;
import com.projectgoth.fusion.slice.RecommendationGenerationServicePrxHelper;
import com.projectgoth.fusion.slice.RegistryAdminPrx;
import com.projectgoth.fusion.slice.RegistryAdminPrxHelper;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.RegistryPrxHelper;
import com.projectgoth.fusion.slice.ReputationServicePrx;
import com.projectgoth.fusion.slice.ReputationServicePrxHelper;
import com.projectgoth.fusion.slice.SMSSenderPrx;
import com.projectgoth.fusion.slice.SMSSenderPrxHelper;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import com.projectgoth.fusion.slice.UserNotificationServicePrxHelper;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.slice.UserPrxHelper;
import java.io.Serializable;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class IcePrxFinder implements Serializable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(IcePrxFinder.class));
   private transient Communicator iceCommunicator;
   private String registryLocation;
   private String registryAdminLocation;
   private String callMakerLocation;
   private String smsSenderLocation;
   private String messageLoggerLocation;
   private String eventSystemLocation;
   private String blueLabelServiceLocation;
   private String userNotificationServiceLocation;
   private String emailAlertLocation;
   private String authenticationServiceLocation;
   private String reputationServiceLocation;
   private String recommendationGenerationServiceLocation;
   private transient RegistryPrx registryPrx;
   private transient RegistryAdminPrx registryAdminPrx;
   private transient CallMakerPrx callMakerPrx;
   private transient SMSSenderPrx smsSenderPrx;
   private transient MessageLoggerPrx messageLoggerPrx;
   private transient EventSystemPrx eventSystemPrx;
   private transient EventSystemPrx oneWayEventSystemPrx;
   private transient BlueLabelServicePrx blueLabelServicePrx;
   private transient UserNotificationServicePrx userNotificationServicePrx;
   private transient EmailAlertPrx emailAlertPrx;
   private transient AuthenticationServicePrx authenticationServicePrx;
   private transient ReputationServicePrx reputationServicePrx;
   private transient RecommendationGenerationServicePrx recommendationGenerationServicePrx;

   public IcePrxFinder(Communicator iceCommunicator, Properties iceProperties) {
      this.iceCommunicator = iceCommunicator;
      this.registryLocation = iceProperties.getProperty("RegistryProxy");
      this.registryAdminLocation = iceProperties.getProperty("RegistryAdminProxy");
      this.callMakerLocation = iceProperties.getProperty("CallMakerProxy");
      this.smsSenderLocation = iceProperties.getProperty("SMSSenderProxy");
      this.messageLoggerLocation = iceProperties.getProperty("MessageLoggerProxy");
      this.eventSystemLocation = iceProperties.getProperty("EventSystemProxy");
      this.blueLabelServiceLocation = iceProperties.getProperty("BlueLabelServiceProxy");
      this.userNotificationServiceLocation = iceProperties.getProperty("UserNotificationServiceProxy");
      this.emailAlertLocation = iceProperties.getProperty("EmailAlertProxy");
      this.authenticationServiceLocation = iceProperties.getProperty("AuthenticationServiceProxy");
      this.reputationServiceLocation = iceProperties.getProperty("ReputationServiceProxy");
      this.recommendationGenerationServiceLocation = iceProperties.getProperty("RecommendationGenerationServiceProxy");
   }

   public void relocate(Communicator comm) {
      this.iceCommunicator = comm;
      this.registryPrx = null;
      this.registryAdminPrx = null;
      this.callMakerPrx = null;
      this.smsSenderPrx = null;
      this.messageLoggerPrx = null;
      this.eventSystemPrx = null;
      this.oneWayEventSystemPrx = null;
      this.blueLabelServicePrx = null;
      this.userNotificationServicePrx = null;
      this.emailAlertPrx = null;
      this.authenticationServicePrx = null;
      this.reputationServicePrx = null;
      this.recommendationGenerationServicePrx = null;
   }

   public RegistryPrx getRegistry(boolean relocate) throws IcePrxFinder.RegistryNotFoundException {
      if (this.registryPrx == null || relocate) {
         if (this.iceCommunicator == null) {
            throw new IcePrxFinder.RegistryNotFoundException("Ice communicator has not been initialized");
         }

         if (this.registryLocation == null) {
            throw new IcePrxFinder.RegistryNotFoundException("No registry location found in configuration file");
         }

         ObjectPrx base = this.iceCommunicator.stringToProxy(this.registryLocation);
         this.registryPrx = RegistryPrxHelper.checkedCast(base);
         if (this.registryPrx == null) {
            throw new IcePrxFinder.RegistryNotFoundException("Invalid registry proxy");
         }
      }

      return this.registryPrx;
   }

   public RegistryAdminPrx getRegistryAdmin(boolean relocate) throws Exception {
      if (this.registryAdminPrx == null || relocate) {
         if (this.iceCommunicator == null) {
            throw new Exception("Ice communicator has not been initialized");
         }

         if (this.registryAdminLocation == null) {
            throw new Exception("No registry admin location found in configuration file");
         }

         ObjectPrx base = this.iceCommunicator.stringToProxy(this.registryAdminLocation);
         this.registryAdminPrx = RegistryAdminPrxHelper.checkedCast(base);
         if (this.registryAdminPrx == null) {
            throw new Exception("Invalid registry admin proxy");
         }
      }

      return this.registryAdminPrx;
   }

   public UserPrx findUserPrx(String username) throws Exception {
      if (username == null) {
         return null;
      } else {
         try {
            return this.getRegistry(false).findUserObject(username);
         } catch (IcePrxFinder.RegistryNotFoundException var6) {
            log.error("failed to get user proxy from registry ", var6);
            throw new Exception("Failed to get user proxy from registry - " + var6.getMessage());
         } catch (ObjectNotFoundException var7) {
            return null;
         } catch (Exception var8) {
            try {
               return this.getRegistry(true).findUserObject(username);
            } catch (ObjectNotFoundException var4) {
               return null;
            } catch (Exception var5) {
               log.error("failed to get user proxy from registry ", var5);
               throw new Exception("Failed to get user proxy from registry - " + var5.getMessage());
            }
         }
      }
   }

   public ConnectionPrx findConnectionPrx(String sessionId) throws Exception {
      if (sessionId == null) {
         return null;
      } else {
         try {
            return this.getRegistry(false).findConnectionObject(sessionId);
         } catch (IcePrxFinder.RegistryNotFoundException var6) {
            log.error("failed to get connection proxy from registry ", var6);
            throw new Exception("Failed to get connection proxy from registry - " + var6.getMessage());
         } catch (ObjectNotFoundException var7) {
            return null;
         } catch (Exception var8) {
            try {
               return this.getRegistry(true).findConnectionObject(sessionId);
            } catch (ObjectNotFoundException var4) {
               return null;
            } catch (Exception var5) {
               log.error("failed to get connection proxy from registry ", var5);
               throw new Exception("Failed to get connection proxy from registry - " + var5.getMessage());
            }
         }
      }
   }

   public ChatRoomPrx findChatRoomPrx(String name) throws Exception {
      if (name == null) {
         return null;
      } else {
         try {
            return this.getRegistry(false).findChatRoomObject(name);
         } catch (IcePrxFinder.RegistryNotFoundException var6) {
            throw new Exception("Failed to get chat room proxy from registry - " + var6.getMessage());
         } catch (ObjectNotFoundException var7) {
            return null;
         } catch (Exception var8) {
            try {
               return this.getRegistry(true).findChatRoomObject(name);
            } catch (ObjectNotFoundException var4) {
               return null;
            } catch (Exception var5) {
               throw new Exception("Failed to get chat room proxy from registry - " + var5.getMessage());
            }
         }
      }
   }

   public ChatRoomPrx[] findChatRoomProxies(String[] names) throws Exception {
      if (names != null && names.length != 0) {
         try {
            return this.getRegistry(false).findChatRoomObjects(names);
         } catch (IcePrxFinder.RegistryNotFoundException var5) {
            throw new Exception("Failed to get chat room proxy from registry - " + var5.getMessage());
         } catch (Exception var6) {
            try {
               return this.getRegistry(true).findChatRoomObjects(names);
            } catch (Exception var4) {
               throw new Exception("Failed to get chat room proxy from registry - " + var4.getMessage());
            }
         }
      } else {
         return null;
      }
   }

   public UserPrx findOnewayUserPrx(String username) throws Exception {
      UserPrx userPrx = this.findUserPrx(username);
      if (userPrx == null) {
         return null;
      } else {
         try {
            UserPrx oneWay = UserPrxHelper.uncheckedCast(userPrx.ice_oneway());
            return (UserPrx)oneWay.ice_connectionId("OneWayProxyGroup");
         } catch (Exception var4) {
            throw new Exception("Failed to get user proxy from registry - " + var4.getMessage());
         }
      }
   }

   public CallMakerPrx getCallMaker() throws Exception {
      if (this.callMakerPrx == null) {
         if (this.iceCommunicator == null) {
            throw new Exception("Ice communicator has not been initialized");
         }

         if (this.callMakerLocation == null) {
            throw new Exception("No CallMakerProxy location found in configuration file");
         }

         ObjectPrx base = this.iceCommunicator.stringToProxy(this.callMakerLocation);
         this.callMakerPrx = CallMakerPrxHelper.checkedCast(base);
         if (this.callMakerPrx == null) {
            throw new Exception("Invalid CallMaker proxy");
         }
      }

      return this.callMakerPrx;
   }

   public SMSSenderPrx getSMSSender() throws Exception {
      if (this.smsSenderPrx == null) {
         if (this.iceCommunicator == null) {
            throw new Exception("Ice communicator has not been initialized");
         }

         if (this.smsSenderLocation == null) {
            throw new Exception("No SMSSenderProxy location found in configuration file");
         }

         ObjectPrx base = this.iceCommunicator.stringToProxy(this.smsSenderLocation);
         this.smsSenderPrx = SMSSenderPrxHelper.checkedCast(base);
         if (this.smsSenderPrx == null) {
            throw new Exception("Invalid SMSSender proxy");
         }
      }

      return this.smsSenderPrx;
   }

   public MessageLoggerPrx getOnewayMessageLoggerPrx() throws Exception {
      if (this.messageLoggerPrx == null) {
         if (this.iceCommunicator == null) {
            throw new Exception("Ice communicator has not been initialized");
         }

         if (this.messageLoggerLocation == null) {
            throw new Exception("No MessageLoggerProxy location found in configuration file");
         }

         ObjectPrx base = this.iceCommunicator.stringToProxy(this.messageLoggerLocation);
         this.messageLoggerPrx = MessageLoggerPrxHelper.uncheckedCast(base.ice_oneway());
         this.messageLoggerPrx = (MessageLoggerPrx)this.messageLoggerPrx.ice_connectionId("OneWayProxyGroup");
         if (this.messageLoggerPrx == null) {
            throw new Exception("Invalid MessageLogger proxy");
         }
      }

      return this.messageLoggerPrx;
   }

   public EventSystemPrx getEventSystemProxy() throws Exception {
      if (this.eventSystemPrx == null) {
         if (this.iceCommunicator == null) {
            throw new Exception("Ice communicator has not been initialized");
         }

         if (this.eventSystemLocation == null) {
            throw new Exception("No EventSystemPrx location found in configuration file");
         }

         ObjectPrx base = this.iceCommunicator.stringToProxy(this.eventSystemLocation);
         this.eventSystemPrx = EventSystemPrxHelper.checkedCast(base);
         if (this.eventSystemPrx == null) {
            throw new Exception("Invalid EventSystem proxy");
         }
      }

      return this.eventSystemPrx;
   }

   public EventSystemPrx getOnewayEventSystemProxy() throws Exception {
      if (this.oneWayEventSystemPrx == null) {
         if (this.iceCommunicator == null) {
            throw new Exception("Ice communicator has not been initialized");
         }

         if (this.eventSystemLocation == null) {
            throw new Exception("No EventSystemPrx location found in configuration file");
         }

         ObjectPrx base = this.iceCommunicator.stringToProxy(this.eventSystemLocation);
         this.oneWayEventSystemPrx = EventSystemPrxHelper.uncheckedCast(base.ice_oneway());
         this.oneWayEventSystemPrx = (EventSystemPrx)this.oneWayEventSystemPrx.ice_connectionId("OneWayProxyGroup");
         if (this.oneWayEventSystemPrx == null) {
            throw new Exception("Invalid EventSystem proxy");
         }
      }

      return this.oneWayEventSystemPrx;
   }

   public BlueLabelServicePrx getBlueLabelServiceProxy() throws Exception {
      if (this.blueLabelServicePrx == null) {
         if (this.iceCommunicator == null) {
            throw new Exception("Ice communicator has not been initialized");
         }

         if (this.blueLabelServiceLocation == null) {
            throw new Exception("No BlueLabelPrx location found in configuration file");
         }

         ObjectPrx base = this.iceCommunicator.stringToProxy(this.blueLabelServiceLocation);
         this.blueLabelServicePrx = BlueLabelServicePrxHelper.checkedCast(base);
         if (this.blueLabelServicePrx == null) {
            throw new Exception("Invalid BlueLabel proxy");
         }
      }

      return this.blueLabelServicePrx;
   }

   public synchronized UserNotificationServicePrx getUserNotificationServiceProxy() throws Exception {
      if (this.userNotificationServicePrx == null) {
         if (this.iceCommunicator == null) {
            throw new Exception("Ice communicator has not been initialized");
         }

         if (this.userNotificationServiceLocation == null) {
            throw new Exception("No UserNotificationServiceProxy location found in configuration file");
         }

         ObjectPrx base = this.iceCommunicator.stringToProxy(this.userNotificationServiceLocation);
         this.userNotificationServicePrx = UserNotificationServicePrxHelper.checkedCast(base);
         if (this.userNotificationServicePrx == null) {
            throw new Exception("Invalid UserNotificationService proxy");
         }
      }

      return this.userNotificationServicePrx;
   }

   public synchronized UserNotificationServicePrx waitForUserNotificationServiceProxy() {
      while(this.userNotificationServicePrx == null) {
         try {
            this.getUserNotificationServiceProxy();
            if (this.userNotificationServicePrx == null) {
               log.warn("Still waiting for user notification service...");
               Thread.sleep(500L);
            }
         } catch (Exception var4) {
            log.error("retrying after failure to get user notification service", var4);

            try {
               Thread.sleep(500L);
            } catch (InterruptedException var3) {
            }
         }
      }

      return this.userNotificationServicePrx;
   }

   public EmailAlertPrx getEmailAlert() throws Exception {
      if (this.emailAlertPrx == null) {
         if (this.iceCommunicator == null) {
            throw new Exception("Ice communicator has not been initialized");
         }

         if (this.emailAlertLocation == null) {
            throw new Exception("No EmailAlertProxy location found in configuration file");
         }

         ObjectPrx base = this.iceCommunicator.stringToProxy(this.emailAlertLocation);
         this.emailAlertPrx = EmailAlertPrxHelper.checkedCast(base);
         if (this.emailAlertPrx == null) {
            throw new Exception("Invalid EmailAlert proxy");
         }
      }

      return this.emailAlertPrx;
   }

   public synchronized AuthenticationServicePrx getAuthenticationServiceProxy() throws Exception {
      if (this.authenticationServicePrx == null) {
         if (this.iceCommunicator == null) {
            throw new Exception("Ice communicator has not been initialized");
         }

         if (!StringUtils.hasLength(this.authenticationServiceLocation)) {
            throw new Exception("No AuthenticationServiceProxy location found in configuration file");
         }

         ObjectPrx base = this.iceCommunicator.stringToProxy(this.authenticationServiceLocation);
         this.authenticationServicePrx = AuthenticationServicePrxHelper.checkedCast(base);
         if (this.authenticationServicePrx == null) {
            throw new Exception("Invalid AuthenticationService proxy");
         }
      }

      return this.authenticationServicePrx;
   }

   public synchronized AuthenticationServicePrx waitForAuthenticationServiceProxy() {
      while(this.authenticationServicePrx == null) {
         try {
            this.getAuthenticationServiceProxy();
            if (this.authenticationServicePrx == null) {
               log.warn("Still waiting for authentication service...");
               Thread.sleep(500L);
            }
         } catch (Exception var4) {
            log.error("retrying after failure to get authentication service", var4);

            try {
               Thread.sleep(500L);
            } catch (InterruptedException var3) {
            }
         }
      }

      return this.authenticationServicePrx;
   }

   public synchronized ReputationServicePrx getReputationServiceProxy() throws Exception {
      if (this.reputationServicePrx == null) {
         if (this.iceCommunicator == null) {
            throw new Exception("Ice communicator has not been initialized");
         }

         if (!StringUtils.hasLength(this.reputationServiceLocation)) {
            throw new Exception("No ReputationServiceProxy location found in configuration file");
         }

         ObjectPrx base = this.iceCommunicator.stringToProxy(this.reputationServiceLocation);
         this.reputationServicePrx = ReputationServicePrxHelper.checkedCast(base);
         if (this.reputationServicePrx == null) {
            throw new Exception("Invalid ReputationService proxy");
         }
      }

      return this.reputationServicePrx;
   }

   public synchronized ReputationServicePrx waitForReputationServiceProxy() {
      while(this.reputationServicePrx == null) {
         try {
            this.getReputationServiceProxy();
            if (this.reputationServicePrx == null) {
               log.warn("Still waiting for reputation service...");
               Thread.sleep(500L);
            }
         } catch (Exception var4) {
            log.error("retrying after failure to get reputation service", var4);

            try {
               Thread.sleep(500L);
            } catch (InterruptedException var3) {
            }
         }
      }

      return this.reputationServicePrx;
   }

   public synchronized RecommendationGenerationServicePrx getRecommendationGenerationServiceProxy() throws Exception {
      if (this.recommendationGenerationServicePrx == null) {
         if (this.iceCommunicator == null) {
            throw new Exception("Ice communicator has not been initialized");
         }

         if (!StringUtils.hasLength(this.recommendationGenerationServiceLocation)) {
            throw new Exception("No RecommendationGenerationServiceProxy location found in configuration file");
         }

         ObjectPrx base = this.iceCommunicator.stringToProxy(this.recommendationGenerationServiceLocation);
         this.recommendationGenerationServicePrx = RecommendationGenerationServicePrxHelper.checkedCast(base);
         if (this.recommendationGenerationServicePrx == null) {
            throw new Exception("Invalid RecommendationGenerationService proxy");
         }
      }

      return this.recommendationGenerationServicePrx;
   }

   public synchronized RecommendationGenerationServicePrx waitForRecommendationGenerationServiceProxy() {
      while(this.recommendationGenerationServicePrx == null) {
         try {
            this.getRecommendationGenerationServiceProxy();
            if (this.recommendationGenerationServicePrx == null) {
               log.warn("Still waiting for recommendation generation service...");
               Thread.sleep(500L);
            }
         } catch (Exception var4) {
            log.error("retrying after failure to get recommendation generation service", var4);

            try {
               Thread.sleep(500L);
            } catch (InterruptedException var3) {
            }
         }
      }

      return this.recommendationGenerationServicePrx;
   }

   public RecommendationDataCollectionServicePrx getRecommendationDataCollectionServiceProxy() {
      return DataCollectorUtils.getRDCSProxy(this.iceCommunicator);
   }

   private class RegistryNotFoundException extends Exception {
      public RegistryNotFoundException(String description) {
         super(description);
      }
   }
}
