package com.projectgoth.fusion.app.dao.db.memcache;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.UserDataDAOChain;
import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.cache.BroadcastList;
import com.projectgoth.fusion.cache.ContactList;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedHelper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.data.DisplayPictureAndStatusMessage;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.UserReputationScoreAndLevelData;
import com.projectgoth.fusion.data.UserSettingData;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MemcacheUserDataDAOChain extends UserDataDAOChain {
   public Set<String> getBroadcastList(UserObject user) throws DAOException {
      Set<String> bcl = BroadcastList.getBroadcastList((MemCachedClient)null, user.getUsername());
      if (bcl == null) {
         bcl = super.getBroadcastList(user);
         BroadcastList.setBroadcastList((MemCachedClient)null, user.getUsername(), bcl);
      }

      return bcl;
   }

   public List<UserSettingData> getUserSettings(UserObject user) throws DAOException {
      List<UserSettingData> settings = (List)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.USER_SETTING, user.getUsername());
      if (settings == null) {
         settings = super.getUserSettings(user);
         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.USER_SETTING, user.getUsername(), settings);
      }

      return settings;
   }

   public List<ContactGroupData> getGroupList(UserObject user) throws DAOException {
      List<ContactGroupData> groupList = (List)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CONTACT_GROUP, user.getUsername());
      if (groupList == null) {
         groupList = super.getGroupList(user);
         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CONTACT_GROUP, user.getUsername(), groupList);
      }

      return groupList;
   }

   public Set<ContactData> getContactList(UserObject user) throws DAOException {
      Set<ContactData> contacts = ContactList.getContactList((MemCachedClient)null, user.getUsername());
      if (contacts == null) {
         contacts = super.getContactList(user);
         ContactList.setContactList((MemCachedClient)null, user.getUsername(), contacts);
      }

      return contacts;
   }

   public void assignDisplayPictureAndStatusMessageToContacts(Collection<ContactData> contactList) throws DAOException {
      Collection<ContactData> contactsNonInMemcached = new HashSet();
      MemCachedClient client = MemCachedClientWrapper.getMemCachedClient(MemCachedUtils.Instance.userDisplayPictureAndStatus);
      Iterator i$ = contactList.iterator();

      while(i$.hasNext()) {
         ContactData contact = (ContactData)i$.next();
         if (contact.fusionUsername != null) {
            DisplayPictureAndStatusMessage avatar = DisplayPictureAndStatusMessage.getDisplayPictureAndStatusMessage(client, contact.fusionUsername);
            if (avatar == null) {
               contactsNonInMemcached.add(contact);
            } else {
               contact.displayPicture = avatar.getDisplayPicture();
               contact.statusMessage = StringUtil.stripHTML(avatar.getStatusMessage());
               contact.statusTimeStamp = avatar.getStatusTimestamp();
            }
         }
      }

      if (!contactsNonInMemcached.isEmpty()) {
         super.assignDisplayPictureAndStatusMessageToContacts(contactsNonInMemcached);
         DisplayPictureAndStatusMessage.setMultiDisplayPictureAndStatusMessage(client, contactsNonInMemcached);
      }
   }

   public int getUserID(UserObject user, boolean throwExceptionWhenNotFound) throws DAOException {
      Integer userID = MemCachedClientWrapper.getInt(MemCachedKeySpaces.CommonKeySpace.USER_ID, user.getUsername());
      if (userID == null) {
         userID = super.getUserID(user, throwExceptionWhenNotFound);
         MemCachedHelper.setUsernameIdMapping(user.getUsername(), userID);
      }

      return userID;
   }

   public String getUsername(int userid) throws DAOException {
      String username = MemCachedClientWrapper.getString(MemCachedKeySpaces.CommonKeySpace.USER_NAME_BY_ID, "" + userid);
      if (username == null) {
         username = super.getUsername(userid);
         MemCachedHelper.setUsernameIdMapping(username, userid);
      }

      return username;
   }

   public UserReputationScoreAndLevelData getReputationScoreAndLevel(int userid, boolean mustUseMaster, boolean skipCacheCheck) throws DAOException {
      boolean needToRefreshCache = true;
      UserReputationScoreAndLevelData data = null;
      if (!skipCacheCheck) {
         data = (UserReputationScoreAndLevelData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.REPUTATION_SCORE, Integer.toString(userid));
         needToRefreshCache = data == null || !data.isCompatible(mustUseMaster);
      }

      if (needToRefreshCache) {
         data = super.getReputationScoreAndLevel(userid, mustUseMaster, skipCacheCheck);
         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.REPUTATION_SCORE, userid + "", data);
      }

      return data;
   }

   public ReputationLevelData getReputationLevel(int level, boolean skipCacheCheck) throws DAOException {
      ReputationLevelData data = null;
      if (!skipCacheCheck) {
         data = (ReputationLevelData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.REPUTATION_LEVEL_DATA, Integer.toString(level));
      }

      if (data == null) {
         data = super.getReputationLevel(level, skipCacheCheck);
         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.REPUTATION_LEVEL_DATA, "" + level, data);
      }

      return data;
   }

   public List<Integer> getEmoticonPacks(UserObject user) throws DAOException {
      List<Integer> emoIDs = (List)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.EMOTICON_PACKS_OWNED, user.getUsername());
      if (emoIDs == null) {
         emoIDs = super.getEmoticonPacks(user);
         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.EMOTICON_PACKS_OWNED, user.getUsername(), emoIDs);
      }

      return emoIDs;
   }

   public void setAlias(UserObject user, String alias) throws DAOException {
      try {
         super.setAlias(user, alias);
         MemCachedHelper.setUserAlias(user.getUsername(), user.getUserID(), alias);
      } catch (DAOException var4) {
         throw var4;
      }
   }
}
