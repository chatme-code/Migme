package com.projectgoth.fusion.app.dao.db.ejb;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.UserDataDAOChain;
import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.app.dao.db.fusiondb.FusionDbUserDataDAOChain;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountBalanceData;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.data.GroupMemberData;
import com.projectgoth.fusion.data.MerchantDetailsData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserReputationScoreAndLevelData;
import com.projectgoth.fusion.data.UserSettingData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

public class EJBUserDataDAOChain extends UserDataDAOChain {
   private static final Logger log = Logger.getLogger(FusionDbUserDataDAOChain.class);

   public UserData getUserData(UserObject user, boolean fullyLoadUserObject, boolean loadFromMasterDB) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETUSERDATA)) {
         return super.getUserData(user, fullyLoadUserObject, loadFromMasterDB);
      } else {
         try {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            UserData data = userEJB.loadUser(user.getUsername(), fullyLoadUserObject, loadFromMasterDB);
            if (log.isDebugEnabled()) {
               log.debug(String.format("DAO: Successfully getUserData:[%s] for user:%s, fullyLoadUserObject:%s, loadFromMasterDB:%s", data, user, fullyLoadUserObject, loadFromMasterDB));
            }

            return data;
         } catch (Exception var6) {
            log.warn(String.format("Failed to retrieve UserData, username:%s", user), var6);
            return super.getUserData(user, fullyLoadUserObject, loadFromMasterDB);
         }
      }
   }

   public Set<String> getBroadcastList(UserObject user) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETBCL)) {
         return super.getBroadcastList(user);
      } else {
         try {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            Set<String> bcl = userEJB.checkAndLoadBroadcastList(user.getUsername(), (Connection)null);
            if (log.isDebugEnabled()) {
               log.debug(String.format("DAO: Successfully getBroadcastList:[%s] for user:%s", bcl, user));
            }

            return bcl;
         } catch (Exception var4) {
            log.warn(String.format("Failed to retrieve UserData, username:%s", user), var4);
            return super.getBroadcastList(user);
         }
      }
   }

   public List<ContactGroupData> getGroupList(UserObject user) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETGROUPLIST)) {
         return super.getGroupList(user);
      } else {
         try {
            Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
            List<ContactGroupData> groupList = contactEJB.getGroupList(user.getUsername());
            if (log.isDebugEnabled()) {
               log.debug(String.format("DAO: Successfully getGroupList:[%s] for user:%s", groupList, user));
            }

            return groupList;
         } catch (Exception var4) {
            log.warn(String.format("Failed to retrieve group list for username:%s", user), var4);
            return super.getGroupList(user);
         }
      }
   }

   public void assignDisplayPictureAndStatusMessageToContacts(Collection<ContactData> contactList) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_ASSIGNDPSM)) {
         super.assignDisplayPictureAndStatusMessageToContacts(contactList);
      } else {
         try {
            Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
            contactEJB.assignDisplayPictureAndStatusMessageToContacts((Connection)null, contactList);
         } catch (Exception var3) {
            log.warn(String.format("Failed to assign picture + status message + status time stamp for contacts:%s", contactList), var3);
            super.assignDisplayPictureAndStatusMessageToContacts(contactList);
         }

      }
   }

   public int getUserID(UserObject user, boolean throwExceptionWhenNotFound) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETUSERID)) {
         return super.getUserID(user, throwExceptionWhenNotFound);
      } else {
         try {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            return userEJB.getUserID(user.getUsername(), (Connection)null, throwExceptionWhenNotFound);
         } catch (Exception var4) {
            log.warn(String.format("Failed to get userid for user:%s", user), var4);
            return super.getUserID(user, throwExceptionWhenNotFound);
         }
      }
   }

   public String getUsername(int userid) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETUSERNAME)) {
         return super.getUsername(userid);
      } else {
         try {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            return userEJB.getUsernameByUserid(userid, (Connection)null);
         } catch (Exception var3) {
            log.warn(String.format("Failed to get username for user:%s", userid), var3);
            return super.getUsername(userid);
         }
      }
   }

   public Set<ContactData> getContactList(UserObject user) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GECONTACTLIST)) {
         return super.getContactList(user);
      } else {
         try {
            Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
            Set<ContactData> contacts = contactEJB.getContactList(user.getUsername());
            if (log.isDebugEnabled()) {
               log.debug(String.format("DAO: Successfully getContactList:[%s] for user:%s", contacts, user));
            }

            return contacts;
         } catch (Exception var4) {
            log.warn(String.format("Failed to retrieve contact list for user:%s", user), var4);
            return super.getContactList(user);
         }
      }
   }

   public List<UserSettingData> getUserSettings(UserObject user) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETUSERSETTINGS)) {
         return super.getUserSettings(user);
      } else {
         try {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            List<UserSettingData> settings = userEJB.getUserSettings(user.getUsername());
            if (log.isDebugEnabled()) {
               log.debug(String.format("DAO: Successfully getUserSettings:[%s] for user:%s", settings, user));
            }

            return settings;
         } catch (Exception var4) {
            log.warn(String.format("Failed to get usersettings for user:%s", user), var4);
            return super.getUserSettings(user);
         }
      }
   }

   public UserReputationScoreAndLevelData getReputationScoreAndLevel(int userid, boolean mustUseMaster, boolean skipCacheCheck) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETUSERREPUTATIONSCOREANDLEVEL)) {
         return super.getReputationScoreAndLevel(userid, mustUseMaster, skipCacheCheck);
      } else {
         try {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            UserReputationScoreAndLevelData data = userEJB.getReputationScoreAndLevel(mustUseMaster, userid, skipCacheCheck);
            if (log.isDebugEnabled()) {
               log.debug(String.format("DAO: Successfully getReputationScoreAndLevel:[%s] for user:%s, mustUseMaster:%s, skipCacheCheck:%s", data, userid, mustUseMaster, skipCacheCheck));
            }

            return data;
         } catch (Exception var6) {
            log.warn(String.format("Failed to get UserReputationScoreAndLevelData for user:%s", userid), var6);
            return super.getReputationScoreAndLevel(userid, mustUseMaster, skipCacheCheck);
         }
      }
   }

   public ReputationLevelData getReputationLevel(int level, boolean skipCacheCheck) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETREPUTATIONLEVELDATA)) {
         return super.getReputationLevel(level, skipCacheCheck);
      } else {
         try {
            MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
            return misEJB.getReputationLevelDataForLevel(level);
         } catch (Exception var4) {
            log.warn(String.format("Failed to get ReputationLevelData for level:%s", level), var4);
            return super.getReputationLevel(level, skipCacheCheck);
         }
      }
   }

   public MerchantDetailsData getBasicMerchantDetails(UserObject user) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETBASICMERCHANTDETAILS)) {
         return super.getBasicMerchantDetails(user);
      } else {
         try {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            return userEJB.getBasicMerchantDetails(user.getUsername());
         } catch (Exception var3) {
            log.warn(String.format("Failed to get MerchantDetailsData for user:%s", user), var3);
            return super.getBasicMerchantDetails(user);
         }
      }
   }

   public boolean isUserInMigboAccessList(UserObject user, int accessListType, int guardCapabilityType) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_ISUSERINMIGBOACCESSLIST)) {
         return super.isUserInMigboAccessList(user, accessListType, guardCapabilityType);
      } else {
         try {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            return userEJB.isUserInMigboAccessList(user.getUserID(), accessListType, guardCapabilityType);
         } catch (Exception var5) {
            log.warn(String.format("Failed to check isUserInMigboAccessList for user:%s, accessListType:%s, guardCapabilityType:%s", user, accessListType, guardCapabilityType), var5);
            return super.isUserInMigboAccessList(user, accessListType, guardCapabilityType);
         }
      }
   }

   public AccountBalanceData getAccountBalance(UserObject user) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETACCOUNTBALANCE)) {
         return super.getAccountBalance(user);
      } else {
         try {
            Account AccountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
            return AccountEJB.getAccountBalance(user.getUsername());
         } catch (Exception var3) {
            log.warn(String.format("Failed to get AccountBalance Data for user:%s", user), var3);
            return super.getAccountBalance(user);
         }
      }
   }

   public GroupMemberData getGroupMember(UserObject user, int groupID) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETGROUPMEMBER)) {
         return super.getGroupMember(user, groupID);
      } else {
         try {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            return userEJB.getGroupMember(user.getUsername(), groupID);
         } catch (Exception var4) {
            log.warn(String.format("Failed to get GroupMemberData for user:%s, group:%s", user, groupID), var4);
            return super.getGroupMember(user, groupID);
         }
      }
   }

   public boolean isUserBlackListedInGroup(UserObject user, int groupId) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_ISUSERBLACKLISTEDINGROUP)) {
         return super.isUserBlackListedInGroup(user, groupId);
      } else {
         try {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            return messageEJB.isUserBlackListedInGroup(user.getUsername(), groupId);
         } catch (Exception var4) {
            log.warn(String.format("Failed to check isUserBlackListedInGroup for user:%s, group:%s", user, groupId), var4);
            return super.isUserBlackListedInGroup(user, groupId);
         }
      }
   }
}
