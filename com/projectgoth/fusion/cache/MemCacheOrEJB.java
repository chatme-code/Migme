package com.projectgoth.fusion.cache;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.DAOFactory;
import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.EmailTemplateData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserReputationScoreAndLevelData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.ejb.GuardsetBean;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.interfaces.Guardset;
import com.projectgoth.fusion.interfaces.GuardsetHome;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import com.projectgoth.fusion.slice.FusionException;
import java.rmi.RemoteException;
import java.sql.Connection;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import org.apache.log4j.Logger;

public class MemCacheOrEJB {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MemCacheOrEJB.class));

   public static int getUserReputationLevel(String username) throws EJBException, CreateException, RemoteException {
      return getUserReputationLevel(username, (Integer)null);
   }

   public static int getUserReputationLevel(int userid) throws EJBException, CreateException, RemoteException {
      return getUserReputationLevel((String)null, userid, (User)null);
   }

   public static int getUserReputationLevel(String username, Integer userid) throws EJBException, CreateException, RemoteException {
      return getUserReputationLevel(username, userid, (User)null);
   }

   public static int getUserReputationLevel(String username, Integer userid, User userEJB) throws EJBException, CreateException, RemoteException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
         UserObject user = null;
         if (userid == null) {
            user = new UserObject(username);
         } else {
            try {
               user = new UserObject(userid);
            } catch (DAOException var5) {
               log.warn(String.format("DAO: Failed to create UserObject for userid:%s", userid), var5);
            }
         }

         if (user != null) {
            try {
               return user.getReputationLevel().level;
            } catch (DAOException var6) {
               log.warn(String.format("DAO: Failed to get level for user:%s", user), var6);
            }
         }
      }

      UserReputationScoreAndLevelData scoreData = null;
      if (userid != null) {
         scoreData = (UserReputationScoreAndLevelData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.REPUTATION_SCORE, Integer.toString(userid));
      }

      if (scoreData != null) {
         return scoreData.level;
      } else {
         if (userEJB == null) {
            userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         }

         ReputationLevelData userReputation = null;
         if (userid != null) {
            userReputation = userEJB.getReputationLevelByUserid(userid, true);
         } else {
            userReputation = userEJB.getReputationLevel(username, false);
         }

         return userReputation.level;
      }
   }

   public static ReputationLevelData getUserReputationLevelData(UserData ud, User userEJB) throws EJBException, CreateException, RemoteException {
      return getUserReputationLevelData(ud.username, ud.userID, userEJB, (UserLocal)null);
   }

   public static ReputationLevelData getUserReputationLevelData(UserData ud) throws EJBException, CreateException, RemoteException {
      return getUserReputationLevelData(ud.username, ud.userID, (User)null, (UserLocal)null);
   }

   public static ReputationLevelData getUserReputationLevelData(UserData ud, UserLocal userLocal) throws EJBException, CreateException, RemoteException {
      return getUserReputationLevelData(ud.username, ud.userID, (User)null, userLocal);
   }

   public static UserReputationScoreAndLevelData getUserReputationScoreAndLevelData(boolean expectFromMaster, int userid) throws CreateException, RemoteException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
         try {
            return (new UserObject(userid)).getReputationScoreAndLevel(expectFromMaster, false);
         } catch (DAOException var5) {
            log.warn(String.format("DAO: Failed to get UserReputationScoreAndLevelData for userid:%s, expectFromMaster:%s", userid, expectFromMaster), var5);
         }
      }

      UserReputationScoreAndLevelData cachedData;
      try {
         cachedData = (UserReputationScoreAndLevelData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.REPUTATION_SCORE, Integer.toString(userid));
      } catch (Exception var4) {
         log.warn("Unable to fetch reputation for userid:[" + userid + "].Exception:[" + var4 + "]");
         cachedData = null;
      }

      if (cachedData != null && cachedData.isCompatible(expectFromMaster)) {
         return cachedData;
      } else {
         User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         return userEJB.getReputationScoreAndLevel(expectFromMaster, userid, false);
      }
   }

   public static UserReputationScoreAndLevelData getUserReputationScoreAndLevelData(boolean fetchUserReputationFromMaster, UserData userData) throws CreateException, RemoteException {
      if (userData == null) {
         throw new IllegalArgumentException("userdata must not be null");
      } else {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
            try {
               if (!StringUtil.isBlank(userData.username)) {
                  return (new UserObject(userData.username)).getReputationScoreAndLevel(fetchUserReputationFromMaster, false);
               }

               return (new UserObject(userData.userID)).getReputationScoreAndLevel(fetchUserReputationFromMaster, false);
            } catch (DAOException var4) {
               log.warn(String.format("DAO: Failed to get UserReputationScoreAndLevelData for user:%s, expectFromMaster:%s", StringUtil.isBlank(userData.username) ? userData.userID : userData.username, fetchUserReputationFromMaster), var4);
            }
         }

         User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         Integer userid;
         if (userData.userID != null) {
            userid = userData.userID;
         } else {
            if (userData.username == null) {
               throw new EJBException("userid and username are both null");
            }

            userid = userEJB.getUserID(userData.username, (Connection)null, false);
            if (userid == null) {
               throw new EJBException("username [" + userData.username + "] is unknown");
            }
         }

         return getUserReputationScoreAndLevelData(fetchUserReputationFromMaster, userid);
      }
   }

   public static ReputationLevelData getUserReputationLevelData(String username, Integer userid, User userEJB) throws EJBException, CreateException, RemoteException {
      return getUserReputationLevelData(username, userid, userEJB, (UserLocal)null);
   }

   public static ReputationLevelData getUserReputationLevelData(String username, Integer userid, User userEJB, UserLocal userLocal) throws EJBException, CreateException, RemoteException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
         UserObject user = null;
         if (userid == null) {
            user = new UserObject(username);
         } else {
            try {
               user = new UserObject(userid);
            } catch (DAOException var6) {
               log.warn(String.format("DAO: Failed to create UserObject for userid:%s", userid), var6);
            }
         }

         if (user != null) {
            try {
               return user.getReputationLevel();
            } catch (DAOException var7) {
               log.warn(String.format("DAO: Failed to get ReputationLevelData for user:%s", user), var7);
            }
         }
      }

      ReputationLevelData repuData = null;
      if (userid != null) {
         UserReputationScoreAndLevelData scoreData = (UserReputationScoreAndLevelData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.REPUTATION_SCORE, Integer.toString(userid));
         if (scoreData != null) {
            repuData = (ReputationLevelData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.REPUTATION_LEVEL_DATA, "" + scoreData.level);
         }
      }

      if (repuData == null) {
         if (userEJB == null) {
            userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         }

         if (userLocal != null) {
            if (userid != null) {
               repuData = userLocal.getReputationLevelByUserid(userid, true);
            } else {
               repuData = userLocal.getReputationLevel(username, false);
            }
         } else if (userid != null) {
            repuData = userEJB.getReputationLevelByUserid(userid, true);
         } else {
            repuData = userEJB.getReputationLevel(username, false);
         }
      }

      return repuData;
   }

   public static Short getMinimumClientVersionForAccess(ClientType deviceEnum, GuardCapabilityEnum gc) throws EJBException, RemoteException, CreateException, FusionEJBException {
      return deviceEnum != null && gc != null ? getMinimumClientVersionForAccess(deviceEnum.value(), gc.value()) : null;
   }

   public static Short getMinimumClientVersionForAccess(int clientType, int guardCapability) throws EJBException, CreateException, FusionEJBException, RemoteException {
      if (ClientType.fromValue(clientType) == null) {
         return null;
      } else {
         return GuardCapabilityEnum.fromValue(guardCapability) == null ? null : getMinimumClientVersionForAccess(clientType, guardCapability, (Guardset)null);
      }
   }

   public static boolean canAccess(ClientType deviceEnum, int clientVersion, GuardCapabilityEnum gc) throws FusionException {
      if (deviceEnum != null && gc != null) {
         try {
            Short minimumClientVersion = getMinimumClientVersionForAccess(deviceEnum.value(), gc.value());
            if (minimumClientVersion == null) {
               return false;
            } else {
               return clientVersion >= minimumClientVersion;
            }
         } catch (Exception var4) {
            log.error("Unable to get minimum client version for [" + deviceEnum + "], guard capability: [" + gc + "]", var4);
            throw new FusionException("Unable to retrieve guardset info: " + gc);
         }
      } else {
         return false;
      }
   }

   public static Short getMinimumClientVersionForAccess(int clientType, int guardCapability, Guardset guardsetEJB) throws EJBException, CreateException, FusionEJBException, RemoteException {
      Short minClientVersion = null;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_GUARDSET_DAO)) {
         try {
            minClientVersion = DAOFactory.getInstance().getGuardsetDAO().getMinimumClientVersionForAccess(clientType, guardCapability);
         } catch (DAOException var5) {
            log.error(String.format("Failed to get min client version for access for clientType:%s, guardCapability:%s", clientType, guardCapability));
            new EJBException(var5);
         }
      } else {
         String sCachedVer = (String)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.MINIMUM_CLIENT_VERSION, GuardsetBean.makeMemcacheKey(clientType, guardCapability));
         if (sCachedVer != null) {
            minClientVersion = Short.parseShort(sCachedVer);
         } else {
            if (guardsetEJB == null) {
               guardsetEJB = (Guardset)EJBHomeCache.getObject("ejb/Guardset", GuardsetHome.class);
            }

            minClientVersion = guardsetEJB.getMinimumClientVersionForAccess(clientType, guardCapability);
         }
      }

      return minClientVersion != null && minClientVersion >= 32767 ? null : minClientVersion;
   }

   public static void invalidateMinimumClientVersionForAccessCache(int clientType, int guardCapability) {
      MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.MINIMUM_CLIENT_VERSION, GuardsetBean.makeMemcacheKey(clientType, guardCapability));
   }

   public static EmailTemplateData getEmailTemplateData(int emailTemplateID) throws Exception {
      EmailTemplateData cachedTemplate = (EmailTemplateData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.EMAIL_TEMPLATE, Integer.toString(emailTemplateID));
      if (cachedTemplate != null) {
         return cachedTemplate;
      } else {
         Message msgEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         return msgEJB.getEmailTemplateData(emailTemplateID);
      }
   }

   public static int getChatRoomID(String nonNormalizedChatRoomName) throws Exception {
      String normName = ChatRoomUtils.normalizeChatRoomName(nonNormalizedChatRoomName);
      ChatRoomData cached = (ChatRoomData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CHATROOM, normName);
      if (cached != null) {
         return cached.id;
      } else {
         Message msgEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         ChatRoomData crd = msgEJB.getSimpleChatRoomData((String)normName, (Connection)null);
         if (crd == null) {
            String errStr = "No such chatroom with normalized name=" + normName + " non-normalized name=" + nonNormalizedChatRoomName;
            log.error(errStr);
            throw new FusionException(errStr);
         } else {
            return crd.id;
         }
      }
   }

   public static String getChatRoomName(int chatRoomID) throws Exception {
      ChatRoomData cached = (ChatRoomData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BY_ID, Integer.toString(chatRoomID));
      if (cached != null) {
         return cached.name;
      } else {
         Message msgEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         ChatRoomData crd = msgEJB.getSimpleChatRoomData((Integer)chatRoomID, (Connection)null);
         if (crd == null) {
            String errStr = "No such chatroom with id=" + chatRoomID;
            log.error(errStr);
            throw new FusionException(errStr);
         } else {
            return crd.name;
         }
      }
   }

   public static ReputationLevelData getReputationLevelDataForLevel(int level) throws Exception {
      return getReputationLevelDataForLevel(level, false);
   }

   public static ReputationLevelData getReputationLevelDataForLevel(int level, boolean skipCacheCheck) throws Exception {
      if (!skipCacheCheck) {
         ReputationLevelData cached = (ReputationLevelData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.REPUTATION_LEVEL_DATA, Integer.toString(level));
         if (cached != null) {
            return cached;
         }
      }

      MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
      return misEJB.getReputationLevelDataForLevel(level);
   }
}
