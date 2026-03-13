package com.projectgoth.fusion.app.dao.data;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.DAOFactory;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountBalanceData;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.data.EmoticonData;
import com.projectgoth.fusion.data.GroupMemberData;
import com.projectgoth.fusion.data.MerchantDetailsData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserReputationScoreAndLevelData;
import com.projectgoth.fusion.data.UserSettingData;
import java.util.List;
import java.util.Set;
import org.jboss.logging.Logger;

public class UserObject {
   private static final Logger log = Logger.getLogger(UserObject.class);
   private String username;
   private int userid = -1;

   public UserObject(int userid) throws DAOException {
      this.userid = userid;
      this.username = DAOFactory.getInstance().getUserDataDAO().getUsername(userid);
   }

   public UserObject(String username) {
      this.username = username;
   }

   public int getUserID() throws DAOException {
      return this.getUserID(false);
   }

   public int getUserID(boolean throwExceptionWhenNotFound) throws DAOException {
      if (this.userid == -1) {
         this.userid = DAOFactory.getInstance().getUserDataDAO().getUserID(this, throwExceptionWhenNotFound);
      }

      return this.userid;
   }

   public static UserObject createUserObject(String useridOrUsername) throws DAOException {
      int userid = StringUtil.toIntOrDefault(useridOrUsername, -1);
      UserObject user = null;
      if (userid == -1) {
         user = new UserObject(useridOrUsername);
         user.validate();
      } else {
         user = new UserObject(userid);
      }

      return user;
   }

   private void validate() throws DAOException {
      if (this.userid == -1) {
         this.userid = DAOFactory.getInstance().getUserDataDAO().getUserID(this, true);
      }

   }

   public String getUsername() {
      return this.username;
   }

   public String toString() {
      return String.format("[username:%s, userid:%s]", this.username, this.userid);
   }

   public UserData getUserData() throws DAOException {
      return this.getUserData(true, false);
   }

   public UserData getUserData(boolean fullyLoadUserObject, boolean loadFromMasterDB) throws DAOException {
      return DAOFactory.getInstance().getUserDataDAO().getUserData(this, fullyLoadUserObject, loadFromMasterDB);
   }

   public Set<String> getBroadcastList() throws DAOException {
      return DAOFactory.getInstance().getUserDataDAO().getBroadcastList(this);
   }

   public List<UserSettingData> getUserSettings() throws DAOException {
      return DAOFactory.getInstance().getUserDataDAO().getUserSettings(this);
   }

   public List<ContactGroupData> getGroupList() throws DAOException {
      return DAOFactory.getInstance().getUserDataDAO().getGroupList(this);
   }

   public Set<ContactData> getContactList() throws DAOException {
      Set contacts = DAOFactory.getInstance().getUserDataDAO().getContactList(this);

      try {
         DAOFactory.getInstance().getUserDataDAO().assignDisplayPictureAndStatusMessageToContacts(contacts);
      } catch (DAOException var3) {
         log.warn(String.format("Failed to assign picture + status message + status time stamp for contacts of user:%s, ignoring...", this), var3);
      }

      return contacts;
   }

   public UserReputationScoreAndLevelData getReputationScoreAndLevel(boolean mustUseMaster, boolean skipCacheCheck) throws DAOException {
      return DAOFactory.getInstance().getUserDataDAO().getReputationScoreAndLevel(this.getUserID(), mustUseMaster, skipCacheCheck);
   }

   public UserReputationScoreAndLevelData getReputationScoreAndLevel() throws DAOException {
      return DAOFactory.getInstance().getUserDataDAO().getReputationScoreAndLevel(this.getUserID(), false, false);
   }

   public ReputationLevelData getReputationLevel(boolean skipCacheCheck) throws DAOException {
      UserReputationScoreAndLevelData data = this.getReputationScoreAndLevel(false, skipCacheCheck);
      return DAOFactory.getInstance().getUserDataDAO().getReputationLevel(data.level, skipCacheCheck);
   }

   public ReputationLevelData getReputationLevel() throws DAOException {
      return this.getReputationLevel(SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.DEFAULT_GETREPUTATIONDATA_USING_CACHE));
   }

   public MerchantDetailsData getBasicMerchantDetails() throws DAOException {
      return DAOFactory.getInstance().getUserDataDAO().getBasicMerchantDetails(this);
   }

   public List<Integer> getEmoticonPacks() throws DAOException {
      return DAOFactory.getInstance().getUserDataDAO().getEmoticonPacks(this);
   }

   public List<EmoticonData> getEmoticons() throws DAOException {
      return DAOFactory.getInstance().getUserDataDAO().getEmoticons(this);
   }

   public boolean isUserInMigboAccessList(int accessListType, int guardCapabilityType) throws DAOException {
      return DAOFactory.getInstance().getUserDataDAO().isUserInMigboAccessList(this, accessListType, guardCapabilityType);
   }

   public AccountBalanceData getAccountBalance() throws DAOException {
      return DAOFactory.getInstance().getUserDataDAO().getAccountBalance(this);
   }

   public GroupMemberData getGroupMember(int groupID) throws DAOException {
      return DAOFactory.getInstance().getUserDataDAO().getGroupMember(this, groupID);
   }

   public boolean isUserBlackListedInGroup(int groupId) throws DAOException {
      return DAOFactory.getInstance().getUserDataDAO().isUserBlackListedInGroup(this, groupId);
   }

   public List<ChatRoomData> getFavouriteChatRooms() throws DAOException {
      return DAOFactory.getInstance().getChatRoomDAO().getFavouriteChatRooms(this);
   }

   public List<ChatRoomData> getRecentChatRooms() throws DAOException {
      return DAOFactory.getInstance().getChatRoomDAO().getRecentChatRooms(this);
   }

   public void setAlias(String alias) throws DAOException {
      DAOFactory.getInstance().getUserDataDAO().setAlias(this, alias);
   }
}
