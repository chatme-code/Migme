package com.projectgoth.fusion.objectcache;

import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.cache.BroadcastList;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.MerchantDetailsData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserSettingData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

public class ChatUserData {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatUserData.class));
   private UserData userData;
   private MerchantDetailsData merchantDetailsData = null;
   Integer messageColor = null;
   int unreadEmailCount;

   public ChatUserData(UserData userData) {
      this.userData = userData;
   }

   public String getUsername() {
      synchronized(this.userData) {
         return this.userData.username;
      }
   }

   public String getPassword() {
      synchronized(this.userData) {
         return this.userData.password;
      }
   }

   public String getLanguage() {
      synchronized(this.userData) {
         return this.userData.language;
      }
   }

   public Integer getUserID() {
      synchronized(this.userData) {
         return this.userData.userID;
      }
   }

   public UserDataIce getUserDataIce() {
      synchronized(this.userData) {
         return this.userData.toIceObject();
      }
   }

   public UserData getNewUserData() {
      return new UserData(this.getUserDataIce());
   }

   public UserData getUserData() {
      return this.userData;
   }

   public UserSettingData.MessageEnum getMessageSetting() {
      return this.userData.messageSetting;
   }

   public void blockUser(String blockUsername) {
      synchronized(this.userData) {
         this.userData.blockList.add(blockUsername);
         this.userData.broadcastList.remove(blockUsername);
         this.userData.pendingContacts.remove(blockUsername);
      }
   }

   public boolean unblockUser(String username) {
      synchronized(this.userData) {
         return this.userData.blockList.remove(username);
      }
   }

   public boolean isOnBlockList(String username) {
      synchronized(this.userData) {
         return this.userData.blockList.contains(username);
      }
   }

   public boolean isOnBroadcastList(String source) {
      synchronized(this.userData) {
         return this.userData.broadcastList.contains(source);
      }
   }

   public void addToBroadcastList(String username) {
      synchronized(this.userData) {
         this.userData.broadcastList.add(username);
      }
   }

   public void removeFromBroadcastList(String username) {
      synchronized(this.userData) {
         this.userData.broadcastList.remove(username);
      }
   }

   public void update(UserData newUserData) {
      synchronized(this.userData) {
         newUserData.pendingContacts = this.userData.pendingContacts;
         newUserData.blockList = this.userData.blockList;
         newUserData.broadcastList = this.userData.broadcastList;
         newUserData.messageSetting = this.userData.messageSetting;
         newUserData.anonymousCallSetting = this.userData.anonymousCallSetting;
         this.userData = newUserData;
      }
   }

   public void update(UserDataIce user) {
      synchronized(this.userData) {
         UserData newUserData = new UserData(user);
         newUserData.pendingContacts = this.userData.pendingContacts;
         newUserData.blockList = this.userData.blockList;
         newUserData.broadcastList = this.userData.broadcastList;
         newUserData.messageSetting = this.userData.messageSetting;
         newUserData.anonymousCallSetting = this.userData.anonymousCallSetting;
         this.userData = newUserData;
      }
   }

   public String[] getBroadcastList() {
      synchronized(this.userData) {
         String[] broadcastListArray = BroadcastList.asArray(this.userData.broadcastList);
         return broadcastListArray;
      }
   }

   public String getDisplayPicture() {
      return this.userData.displayPicture;
   }

   public void updateStatusMessage(String statusMessage, long timeStamp) {
      synchronized(this.userData) {
         this.userData.statusMessage = statusMessage;
         this.userData.statusTimeStamp = new Date(timeStamp);
      }
   }

   public String getStatusMessage() {
      return this.userData.statusMessage;
   }

   public void updateDisplayPicture(String displayPicture, long timeStamp) {
      synchronized(this.userData) {
         this.userData.displayPicture = displayPicture;
         this.userData.statusTimeStamp = new Date(timeStamp);
      }
   }

   public int contactRequestAccept(ContactData newContact) {
      return this.contactRequestAccept(newContact.fusionUsername);
   }

   public int contactRequestAccept(String username) {
      synchronized(this.userData) {
         this.userData.pendingContacts.remove(username);
         this.userData.broadcastList.add(username);
         return this.userData.pendingContacts.size();
      }
   }

   public int contactRequestReject(String inviterUsername) {
      synchronized(this.userData) {
         this.userData.broadcastList.remove(inviterUsername);
         this.userData.pendingContacts.remove(inviterUsername);
         return this.userData.pendingContacts.size();
      }
   }

   public String[] findAllowedWatchers() {
      String[] allowedWatchersArray;
      synchronized(this.userData) {
         allowedWatchersArray = (String[])this.userData.broadcastList.toArray(new String[this.userData.broadcastList.size()]);
      }

      if (log.isDebugEnabled()) {
         log.debug("found " + (allowedWatchersArray == null ? 0 : allowedWatchersArray.length) + " watchers for user [" + this.userData.username + "]");
      }

      return allowedWatchersArray;
   }

   public void setMessageAvatar(MessageDataIce message) {
      synchronized(this.userData) {
         String displayPicture = this.userData.avatar;
         if (displayPicture != null) {
            message.sourceDisplayPicture = displayPicture;
         }

      }
   }

   public Integer getCountryID() {
      return this.userData.countryID;
   }

   public boolean isOfType(UserData.TypeEnum typeEnum) {
      return this.userData.type == typeEnum;
   }

   public boolean isMobileVerified() {
      return this.userData.mobileVerified == null ? false : this.userData.mobileVerified;
   }

   public String[] getBlockList() {
      synchronized(this.userData) {
         return (String[])this.userData.blockList.toArray(new String[this.userData.blockList.size()]);
      }
   }

   public void addPendingContact(String username) {
      synchronized(this.userData) {
         this.userData.pendingContacts.add(username);
      }
   }

   public Set<String> getPendingContacts() {
      Set<String> userList = new HashSet();
      synchronized(this.userData) {
         userList.addAll(this.userData.pendingContacts);
         return userList;
      }
   }

   public void contactUpdated(String usernameThatWasModified, boolean acceptedContactRequest) {
      synchronized(this.userData) {
         if (acceptedContactRequest) {
            this.userData.pendingContacts.remove(usernameThatWasModified);
         } else {
            this.userData.pendingContacts.add(usernameThatWasModified);
         }

         this.userData.broadcastList.add(usernameThatWasModified);
      }
   }

   public void contactUpdated(String usernameThatWasModified) {
      synchronized(this.userData) {
         this.userData.pendingContacts.remove(usernameThatWasModified);
         this.userData.broadcastList.remove(usernameThatWasModified);
      }
   }

   public void setMessageSetting(UserSettingData.MessageEnum fromValue) {
      synchronized(this.userData) {
         this.userData.messageSetting = fromValue;
      }
   }

   public void setAnonymousCallSetting(int setting) {
      synchronized(this.userData) {
         this.userData.anonymousCallSetting = UserSettingData.AnonymousCallEnum.fromValue(setting);
      }
   }

   public void updateContact(String oldusername, String fusionUsername, boolean acceptedContactRequest) {
      synchronized(this.userData) {
         this.userData.pendingContacts.remove(oldusername);
         if (acceptedContactRequest) {
            this.userData.broadcastList.add(fusionUsername);
         } else {
            this.userData.pendingContacts.add(fusionUsername);
            this.userData.broadcastList.remove(oldusername);
         }

      }
   }

   public UserSettingData.AnonymousCallEnum getAnonymousCallSetting() {
      synchronized(this.userData) {
         return this.userData.anonymousCallSetting;
      }
   }

   public String[] getBlocklistFromUsernames(String[] usernames) {
      Set<String> blockList = new HashSet(Arrays.asList(usernames));
      synchronized(this.userData) {
         blockList.retainAll(this.userData.blockList);
      }

      return (String[])blockList.toArray(new String[blockList.size()]);
   }

   public void updateBalance(double balance, double fundedBalance, String currency) {
      synchronized(this.userData) {
         this.userData.balance = balance;
         this.userData.fundedBalance = fundedBalance;
         this.userData.currency = currency;
      }
   }

   public int rejectContactRequest(String inviterUsername) {
      synchronized(this.userData) {
         this.userData.broadcastList.remove(inviterUsername);
         this.userData.pendingContacts.remove(inviterUsername);
         return this.userData.pendingContacts.size();
      }
   }

   public boolean isChatRoomAdmin() {
      return this.userData.chatRoomAdmin;
   }

   public int getUnreadEmailCount() {
      return this.unreadEmailCount;
   }

   public void setUnreadEmailCount(int unreadEmailCount) {
      this.unreadEmailCount = unreadEmailCount;
   }

   public UserData.TypeEnum getUserType() {
      return this.userData.type;
   }

   public double getBalance() {
      return this.userData.balance;
   }

   public String getAvatar() {
      return this.userData.avatar;
   }

   public void applyMessageColor(MessageDataIce message) {
      if (this.messageColor == null) {
         this.messageColor = message.messageColour;
         this.updateMessageColor();
      }

      message.sourceColour = this.messageColor;
   }

   private void updateMessageColor() {
      if (this.getUserType() == UserData.TypeEnum.MIG33_TOP_MERCHANT) {
         if (this.merchantDetailsData == null) {
            try {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
                  UserObject user = new UserObject(this.userData.username);
                  this.merchantDetailsData = user.getBasicMerchantDetails();
                  if (log.isDebugEnabled()) {
                     log.debug(String.format("DAO: getBasicMerchantDetails for user:%s. data:%s ", user, this.merchantDetailsData));
                  }
               } else {
                  User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
                  this.merchantDetailsData = userEJB.getBasicMerchantDetails(this.getUsername());
               }
            } catch (Exception var2) {
            }
         }

         if (this.merchantDetailsData != null) {
            this.messageColor = this.merchantDetailsData.getChatColorHex();
         } else {
            this.messageColor = MerchantDetailsData.UserNameColorTypeEnum.DEFAULT.hex();
         }
      } else if (this.isChatRoomAdmin()) {
         this.messageColor = MessageData.SourceTypeEnum.GLOBAL_ADMIN.colorHex();
      }

   }

   public void updatePendingContacts(Contact contactEJB) throws Exception {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.PUSH_NEW_FOLLOWERS_AS_PENDING_CONTACTS_ENABLED)) {
         Set<String> recentFollowers = contactEJB.getRecentFollowers(this.userData.userID);
         if (recentFollowers != null && recentFollowers.size() > 0) {
            if (this.userData.pendingContacts == null) {
               this.userData.pendingContacts = new HashSet();
            }

            int maxPendingContacts = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.MAX_PENDING_CONTACTS_TO_RETRIEVE);
            Iterator i$ = recentFollowers.iterator();

            while(i$.hasNext()) {
               String recentFollower = (String)i$.next();
               if (this.userData.pendingContacts.size() >= maxPendingContacts) {
                  break;
               }

               this.userData.pendingContacts.add(recentFollower);
            }
         }
      }

   }

   public void updatePendingContacts(UserNotificationServicePrx uns) throws Exception {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.PUSH_NEW_FOLLOWERS_AS_PENDING_CONTACTS_ENABLED)) {
         Map<String, Map<String, String>> newFollowerAlerts = uns.getPendingNotificationDataForUserByType(this.userData.userID, Enums.NotificationTypeEnum.NEW_FOLLOWER_ALERT.getType());
         Set<String> recentFollowers = new HashSet();
         Iterator i$ = newFollowerAlerts.values().iterator();

         String recentFollower;
         while(i$.hasNext()) {
            Map<String, String> p = (Map)i$.next();
            recentFollower = (String)p.get("otherUsername");
            if (!StringUtil.isBlank(recentFollower)) {
               recentFollowers.add(recentFollower);
            }
         }

         if (recentFollowers != null && recentFollowers.size() > 0) {
            if (this.userData.pendingContacts == null) {
               this.userData.pendingContacts = new HashSet();
            }

            int maxPendingContacts = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.MAX_PENDING_CONTACTS_TO_RETRIEVE);
            Iterator i$ = recentFollowers.iterator();

            while(i$.hasNext()) {
               recentFollower = (String)i$.next();
               if (this.userData.pendingContacts.size() >= maxPendingContacts) {
                  break;
               }

               this.userData.pendingContacts.add(recentFollower);
            }
         }
      }

   }
}
