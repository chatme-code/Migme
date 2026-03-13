package com.projectgoth.fusion.app.dao.db.fusiondb;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.DAOFactory;
import com.projectgoth.fusion.app.dao.base.UserDataDAOChain;
import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.common.MemCachedHelper;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountBalanceData;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.EmoticonData;
import com.projectgoth.fusion.data.EmoticonPackData;
import com.projectgoth.fusion.data.GroupMemberData;
import com.projectgoth.fusion.data.MerchantDetailsData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.ServiceData;
import com.projectgoth.fusion.data.SubscriptionData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserEmailAddressData;
import com.projectgoth.fusion.data.UserReputationScoreAndLevelData;
import com.projectgoth.fusion.data.UserSettingData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

public class FusionDbUserDataDAOChain extends UserDataDAOChain {
   private static final Logger log = Logger.getLogger(FusionDbUserDataDAOChain.class);
   private static final String sql_retrieve_user_data = "select u.*, uid.id as uid, a.headuuid as avatar, a.bodyuuid as fullbodyavatar, ua.alias,uv.type accountType, uv.verified accountVerified, uv.description verifiedProfile, uea.emailaddress primaryEmail, uea.verified emailVerified from user u, userid uid LEFT OUTER JOIN avataruserbody a ON uid.id = a.userid and a.used = 1 LEFT OUTER JOIN useralias ua ON uid.username=ua.username LEFT OUTER JOIN userverified uv ON uv.userid = uid.id LEFT OUTER JOIN useremailaddress uea ON uea.userid = uid.id and uea.type = ? where u.username = uid.username and u.username = ? ";

   public UserData getUserData(UserObject user, boolean fullyLoadUserObject, boolean loadFromMasterDB) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      UserData var9;
      try {
         UserData userData;
         try {
            String username = user.getUsername();
            conn = loadFromMasterDB ? DBUtils.getFusionWriteConnection() : DBUtils.getFusionReadConnection();
            ps = conn.prepareStatement("select u.*, uid.id as uid, a.headuuid as avatar, a.bodyuuid as fullbodyavatar, ua.alias,uv.type accountType, uv.verified accountVerified, uv.description verifiedProfile, uea.emailaddress primaryEmail, uea.verified emailVerified from user u, userid uid LEFT OUTER JOIN avataruserbody a ON uid.id = a.userid and a.used = 1 LEFT OUTER JOIN useralias ua ON uid.username=ua.username LEFT OUTER JOIN userverified uv ON uv.userid = uid.id LEFT OUTER JOIN useremailaddress uea ON uea.userid = uid.id and uea.type = ? where u.username = uid.username and u.username = ? ");
            ps.setInt(1, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY.value);
            ps.setString(2, username);
            rs = ps.executeQuery();
            if (!rs.next() || !username.toLowerCase().equals(rs.getString("u.username"))) {
               userData = super.getUserData(user, fullyLoadUserObject, loadFromMasterDB);
               return userData;
            }

            userData = new UserData(rs);
            this.populateUserSettings(user, userData);
            if (fullyLoadUserObject) {
               rs.close();
               ps.close();
               userData.pendingContacts = this.loadPendingContacts(userData.username, conn);
               userData.blockList = this.loadBlockList(userData.username, conn);
               userData.broadcastList = DAOFactory.getInstance().getUserDataDAO().getBroadcastList(user);
            }

            MemCachedHelper.setUserAlias(username, userData.userID, userData.alias);
            if (log.isDebugEnabled()) {
               log.debug(String.format("DAO: Successfully getUserData:[%s] for user:%s, fullyLoadUserObject:%s, loadFromMasterDB:%s", userData, user, fullyLoadUserObject, loadFromMasterDB));
            }

            var9 = userData;
         } catch (Exception var14) {
            log.error(String.format("Failed to retrieve UserData, username:%s", user), var14);
            userData = super.getUserData(user, fullyLoadUserObject, loadFromMasterDB);
            return userData;
         }
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var9;
   }

   public List<UserSettingData> getUserSettings(UserObject user) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      List var6;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select * from usersetting where username = ?");
         ps.setString(1, user.getUsername());
         rs = ps.executeQuery();
         ArrayList settings = new ArrayList();

         while(rs.next()) {
            settings.add(new UserSettingData(rs));
         }

         if (log.isDebugEnabled()) {
            log.debug(String.format("DAO: Successfully getUserSettings:[%s] for user:%s", settings, user));
         }

         ArrayList var13 = settings;
         return var13;
      } catch (SQLException var11) {
         log.error(String.format("Unable to retrieve user setting data from user%s", user), var11);
         var6 = super.getUserSettings(user);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var6;
   }

   private void populateUserSettings(UserObject user, UserData userData) throws DAOException {
      List<UserSettingData> settings = DAOFactory.getInstance().getUserDataDAO().getUserSettings(user);
      Iterator i$ = settings.iterator();

      while(i$.hasNext()) {
         UserSettingData setting = (UserSettingData)i$.next();
         if (setting.type != null) {
            switch(setting.type) {
            case MESSAGE:
               userData.messageSetting = UserSettingData.MessageEnum.fromValue(setting.value);
               break;
            case ANONYMOUS_CALL:
               userData.anonymousCallSetting = UserSettingData.AnonymousCallEnum.fromValue(setting.value);
               break;
            case EMAIL_ALL:
               userData.emailAllSetting = UserSettingData.EmailSettingEnum.fromValue(setting.value);
               break;
            case EMAIL_MENTION:
               userData.emailMentionSetting = UserSettingData.EmailSettingEnum.fromValue(setting.value);
               break;
            case EMAIL_NEW_FOLLOWER:
               userData.emailNewFollowerSetting = UserSettingData.EmailSettingEnum.fromValue(setting.value);
               break;
            case EMAIL_RECEIVE_GIFT:
               userData.emailReceiveGiftSetting = UserSettingData.EmailSettingEnum.fromValue(setting.value);
               break;
            case EMAIL_REPLY_TO_POST:
               userData.emailReplyToPostSetting = UserSettingData.EmailSettingEnum.fromValue(setting.value);
            }
         }
      }

   }

   private Set<String> loadPendingContacts(String username, Connection connection) throws SQLException {
      return this.loadStringListForUser(username, "pendingContact", "pendingcontact", "username", connection);
   }

   private Set<String> loadBlockList(String username, Connection connection) throws SQLException {
      return this.loadStringListForUser(username, "blockUsername", "blocklist", "username", connection);
   }

   private Set<String> checkAndLoadBroadcastList(String username, Connection conn) throws DAOException {
      try {
         String FIELD = "fusionUsername";
         String QUERY = "select myContacts.fusionUsername from contact myContacts inner join contact contactsContacts on contactsContacts.fusionUsername = myContacts.username and contactsContacts.username = myContacts.fusionUsername left join blocklist myBlocklist on myBlocklist.username = myContacts.username and myBlocklist.blockusername = myContacts.fusionUsername left join blocklist contactsBlocklists on contactsBlocklists.blockusername = myContacts.username and contactsBlocklists.username = myContacts.fusionUsername where myContacts.username = ? and myBlocklist.blockusername is null and contactsBlocklists.blockusername is null";
         PreparedStatement ps = null;
         ResultSet rs = null;

         HashSet var8;
         try {
            ps = conn.prepareStatement("select myContacts.fusionUsername from contact myContacts inner join contact contactsContacts on contactsContacts.fusionUsername = myContacts.username and contactsContacts.username = myContacts.fusionUsername left join blocklist myBlocklist on myBlocklist.username = myContacts.username and myBlocklist.blockusername = myContacts.fusionUsername left join blocklist contactsBlocklists on contactsBlocklists.blockusername = myContacts.username and contactsBlocklists.username = myContacts.fusionUsername where myContacts.username = ? and myBlocklist.blockusername is null and contactsBlocklists.blockusername is null");
            ps.setString(1, username);
            rs = ps.executeQuery();
            HashSet contacts = new HashSet();

            while(rs.next()) {
               contacts.add(rs.getString("fusionUsername"));
            }

            if (log.isDebugEnabled()) {
               log.debug(String.format("DAO: Loaded BCL of length=" + contacts.size() + " for username=" + username));
            }

            var8 = contacts;
         } finally {
            DBUtils.closeResource(rs, ps, (Connection)null, log);
         }

         return var8;
      } catch (Exception var14) {
         log.error(String.format("Failed to retrieve broadcast list for username:%s", username), var14);
         throw new DAOException(String.format("Failed to retrieve broadcast list for username:%s", username));
      }
   }

   private Set<String> loadStringListForUser(String whereValue, String field, String tableName, String whereCondition, Connection connection) throws SQLException {
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         ps = connection.prepareStatement("select " + field + " from " + tableName + " where " + whereCondition + " = ?");
         ps.setString(1, whereValue);
         rs = ps.executeQuery();
         HashSet set = new HashSet();

         while(rs.next()) {
            set.add(rs.getString(field));
         }

         if (log.isDebugEnabled()) {
            log.debug(String.format("DAO: Load field:%s from table:%s where %s = %s, with result set as:%s", field, tableName, whereCondition, whereValue, set));
         }

         HashSet var9 = set;
         return var9;
      } finally {
         DBUtils.closeResource(rs, ps, (Connection)null, log);
      }
   }

   public Set<String> getBroadcastList(UserObject user) throws DAOException {
      Connection conn = null;

      Set var4;
      try {
         conn = DBUtils.getFusionReadConnection();
         Set<String> bcl = null;
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.CHECK_AND_POPULATEBCL)) {
            bcl = this.checkAndLoadBroadcastList(user.getUsername(), conn);
         } else {
            bcl = this.loadStringListForUser(user.getUsername(), "broadcastUsername", "broadcastlist", "username", conn);
         }

         if (log.isDebugEnabled()) {
            log.debug(String.format("DAO: Successfully getBroadcastList:[%s] for user:%s", bcl, user));
         }

         var4 = bcl;
         return var4;
      } catch (Exception var9) {
         log.error(String.format("Failed to retrieve broadcast list for username:%s", user), var9);
         var4 = super.getBroadcastList(user);
      } finally {
         DBUtils.closeResource((ResultSet)null, (Statement)null, conn, log);
      }

      return var4;
   }

   public List<ContactGroupData> getGroupList(UserObject user) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      String username = user.getUsername();

      List var7;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select * from contactgroup where username = ?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         LinkedList groupList = new LinkedList();

         while(rs.next()) {
            groupList.add(new ContactGroupData(rs));
         }

         if (log.isDebugEnabled()) {
            log.debug(String.format("DAO: Successfully getGroupList:[%s] for user:%s", groupList, user));
         }

         LinkedList var14 = groupList;
         return var14;
      } catch (SQLException var12) {
         log.error(String.format("Failed to retrieve group list for username:%s", username), var12);
         var7 = super.getGroupList(user);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var7;
   }

   public Set<ContactData> getContactList(UserObject user) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      String username = user.getUsername();

      Set var7;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select * from contact where username = ?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         HashSet contacts = new HashSet();

         while(rs.next()) {
            contacts.add(new ContactData(rs));
         }

         if (log.isDebugEnabled()) {
            log.debug(String.format("DAO: Successfully getContactList:[%s] for user:%s", contacts, user));
         }

         HashSet var14 = contacts;
         return var14;
      } catch (SQLException var12) {
         log.error(String.format("Failed to retrieve contact list for username:%s", username), var12);
         var7 = super.getContactList(user);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var7;
   }

   public void assignDisplayPictureAndStatusMessageToContacts(Collection<ContactData> contactList) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         Map<String, ContactData> contactMap = new HashMap();
         Iterator i$ = contactList.iterator();

         while(i$.hasNext()) {
            ContactData contact = (ContactData)i$.next();
            if (!StringUtil.isBlank(contact.fusionUsername)) {
               contactMap.put(contact.fusionUsername, contact);
            }
         }

         conn = DBUtils.getFusionReadConnection();
         String parameters = StringUtil.generateQuestionMarksForSQLStatement(contactMap.size());
         ps = conn.prepareStatement("select username, displayPicture, statusMessage, statusTimeStamp, dateRegistered from user where username in (" + parameters + ")");
         int i = 1;
         Iterator i$ = contactMap.keySet().iterator();

         while(i$.hasNext()) {
            String key = (String)i$.next();
            ps.setString(i++, key);
         }

         rs = ps.executeQuery();

         while(rs.next()) {
            ContactData contact = (ContactData)contactMap.get(rs.getString("username"));
            if (contact != null) {
               contact.displayPicture = rs.getString("displayPicture");
               contact.statusMessage = StringUtil.stripHTML(rs.getString("statusMessage"));

               try {
                  contact.statusTimeStamp = rs.getTimestamp("statusTimeStamp");
               } catch (Exception var15) {
                  contact.statusTimeStamp = rs.getTimestamp("dateRegistered");
               }
            }
         }
      } catch (SQLException var16) {
         log.error(String.format("Failed to assign picture + status message + status time stamp for contacts:%s", contactList), var16);
         super.assignDisplayPictureAndStatusMessageToContacts(contactList);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

   }

   public int getUserID(UserObject user, boolean throwExceptionWhenNotFound) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var7;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select id from userid where username = ?");
         ps.setString(1, user.getUsername());
         rs = ps.executeQuery();
         int var6;
         if (!rs.next()) {
            log.warn(String.format("FIXME: Failed to find userid in userid table for user:%s in fusion database", user));
            if (!throwExceptionWhenNotFound) {
               byte var14 = -1;
               return var14;
            }

            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_CHAIN_ON_IF_SHOULD_NOT_REACH_HERE_HAPPENS)) {
               var6 = super.getUserID(user, throwExceptionWhenNotFound);
               return var6;
            }

            throw new DAOException(String.format("Failed to find userid in userid table for user:%s in fusion database", user));
         }

         var6 = rs.getInt("id");
         return var6;
      } catch (SQLException var12) {
         log.error(String.format("Failed to get userid for user:%s", user), var12);
         var7 = super.getUserID(user, throwExceptionWhenNotFound);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var7;
   }

   public String getUsername(int userid) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var5;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select username from userid where id = ?");
         ps.setInt(1, userid);
         rs = ps.executeQuery();
         if (!rs.next()) {
            log.warn(String.format("FIXME: Failed to find username in userid table for user:%s in fusion database", userid));
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_CHAIN_ON_IF_SHOULD_NOT_REACH_HERE_HAPPENS)) {
               var5 = super.getUsername(userid);
               return var5;
            }

            throw new DAOException(String.format("Failed to find username for useridid:%s in fusion database", userid));
         }

         var5 = rs.getString("username");
      } catch (SQLException var11) {
         log.error(String.format("Failed to get username for user:%s", userid), var11);
         String var6 = super.getUsername(userid);
         return var6;
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var5;
   }

   public UserReputationScoreAndLevelData getReputationScoreAndLevel(int userid, boolean mustUseMaster, boolean skipCacheCheck) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      UserReputationScoreAndLevelData var8;
      try {
         conn = mustUseMaster ? DBUtils.getFusionWriteConnection() : DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select s.*, max(r.level) as level from score s, reputationscoretolevel r where s.userid = ? and s.score >= r.score;");
         ps.setInt(1, userid);
         rs = ps.executeQuery();
         UserReputationScoreAndLevelData data;
         if (rs.next() && rs.getInt("level") != 0) {
            data = new UserReputationScoreAndLevelData(rs, mustUseMaster);
            if (log.isDebugEnabled()) {
               log.debug("Found score data for user: " + userid + " score: " + data.score);
            }
         } else {
            data = new UserReputationScoreAndLevelData(userid, 0, 1, new Date(0L), mustUseMaster);
            if (log.isDebugEnabled()) {
               log.debug("No score data found for user : " + userid + " using default values");
            }
         }

         if (log.isDebugEnabled()) {
            log.debug(String.format("DAO: Successfully getReputationScoreAndLevel:[%s] for user:%s, mustUseMaster:%s, skipCacheCheck:%s", data, userid, mustUseMaster, skipCacheCheck));
         }

         var8 = data;
         return var8;
      } catch (SQLException var13) {
         log.error(String.format("Failed to get UserReputationScoreAndLevelData for user:%s", userid), var13);
         var8 = super.getReputationScoreAndLevel(userid, mustUseMaster, skipCacheCheck);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var8;
   }

   public ReputationLevelData getReputationLevel(int level, boolean skipCacheCheck) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      ReputationLevelData var7;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select * from reputationscoretolevel where level = ?");
         ps.setInt(1, level);
         rs = ps.executeQuery();
         ReputationLevelData data = null;
         if (!rs.next()) {
            log.warn(String.format("FIXME: Failed to get result from table reputationscoretolevel where level = %s in fusion database", level));
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_CHAIN_ON_IF_SHOULD_NOT_REACH_HERE_HAPPENS)) {
               var7 = super.getReputationLevel(level, skipCacheCheck);
               return var7;
            }

            throw new DAOException(String.format("Failed to find ReputationLevelData for level:%s in fusion database", level));
         }

         data = new ReputationLevelData(rs);
         var7 = data;
      } catch (SQLException var12) {
         log.error(String.format("Failed to get ReputationLevelData for level:%s", level), var12);
         var7 = super.getReputationLevel(level, skipCacheCheck);
         return var7;
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var7;
   }

   public MerchantDetailsData getBasicMerchantDetails(UserObject user) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      MerchantDetailsData var6;
      try {
         conn = DBUtils.getFusionReadConnection();
         String sql = "select merchantdetails.* from merchantdetails join userid on merchantdetails.id = userid.id where userid.username = ?;";
         ps = conn.prepareStatement(sql);
         ps.setString(1, user.getUsername());
         rs = ps.executeQuery();
         if (rs.next()) {
            var6 = new MerchantDetailsData(rs, false);
            return var6;
         }

         var6 = null;
      } catch (SQLException var11) {
         log.error(String.format("Failed to get BasicMerchantDetails for user:%s", user), var11);
         var6 = super.getBasicMerchantDetails(user);
         return var6;
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var6;
   }

   public List<Integer> getEmoticonPacks(UserObject user) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      List var6;
      try {
         conn = DBUtils.getFusionReadConnection();
         String sql = "select emoticonpackid from emoticonpackowner where username = ? and status = 1 union select p.id from emoticonpack p, subscription s where p.serviceid = s.serviceid and p.type = ? and s.username = ? and s.status = ?";
         String username = user.getUsername();
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setInt(2, EmoticonPackData.TypeEnum.PREMIUM_SUBSCRIPTION.value());
         ps.setString(3, username);
         ps.setInt(4, SubscriptionData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();
         ArrayList emoIDs = new ArrayList();

         while(rs.next()) {
            emoIDs.add(rs.getInt(1));
         }

         ArrayList var8 = emoIDs;
         return var8;
      } catch (SQLException var13) {
         log.error(String.format("Failed to get EmoticonIDs for user:%s", user), var13);
         var6 = super.getEmoticonPacks(user);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var6;
   }

   public List<EmoticonData> getEmoticons(UserObject user) throws DAOException {
      try {
         List<Integer> emoticonPackIDs = user.getEmoticonPacks();
         emoticonPackIDs.add(0, 1);
         List<EmoticonData> emoticons = new ArrayList();
         Map<Integer, EmoticonPackData> emoPacksMap = DAOFactory.getInstance().getEmoAndStickerDAO().loadEmoticonPacks();
         Iterator i$ = emoticonPackIDs.iterator();

         while(i$.hasNext()) {
            Integer emoticonPackID = (Integer)i$.next();
            if (emoPacksMap.containsKey(emoticonPackID)) {
               emoticons.addAll(this.getEmoticons(((EmoticonPackData)emoPacksMap.get(emoticonPackID)).getEmoticonIDs()));
            }
         }

         return emoticons;
      } catch (DAOException var7) {
         log.error(String.format("Failed to get Emoticons for user:%s", user), var7);
         return super.getEmoticons(user);
      }
   }

   private List<EmoticonData> getEmoticons(List<Integer> emoticonIDList) throws DAOException {
      List<EmoticonData> emoticonDatas = new ArrayList(emoticonIDList.size());
      Map<Integer, EmoticonData> emoticons = DAOFactory.getInstance().getEmoAndStickerDAO().loadEmoticons();
      Iterator i$ = emoticonIDList.iterator();

      while(i$.hasNext()) {
         Integer emoticonID = (Integer)i$.next();
         EmoticonData emoticonData = (EmoticonData)emoticons.get(emoticonID);
         if (emoticonData != null) {
            emoticonDatas.add(emoticonData);
         }
      }

      return emoticonDatas;
   }

   public boolean isUserInMigboAccessList(UserObject user, int accessListType, int guardCapabilityType) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var8;
      try {
         conn = DBUtils.getFusionReadConnection();
         String sql = "SELECT COUNT(*) ctr_capability FROM guardcapability gc, guardsetcapability gsc, guardsetmember gsm, guardset gs WHERE gc.id = gsc.guardcapabilityid AND gsm.guardsetid = gsc.guardsetid AND gsm.guardsetid = gs.id AND gsm.membertype = ? AND gc.id = ? AND gsm.memberid = ?";
         ps = conn.prepareStatement(sql);
         ps.setInt(1, accessListType);
         ps.setInt(2, guardCapabilityType);
         ps.setInt(3, user.getUserID());
         rs = ps.executeQuery();
         if (rs.next()) {
            int value = rs.getInt(1);
            if (value > 0) {
               boolean var9 = true;
               return var9;
            }
         }

         var8 = false;
      } catch (SQLException var14) {
         log.error(String.format("Failed to check isUserInMigboAccessList for user:%s, accessListType:%s, guardCapabilityType:%s", user, accessListType, guardCapabilityType), var14);
         var8 = super.isUserInMigboAccessList(user, accessListType, guardCapabilityType);
         return var8;
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var8;
   }

   public AccountBalanceData getAccountBalance(UserObject user) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      AccountBalanceData balanceData;
      try {
         AccountBalanceData var6;
         try {
            conn = DBUtils.getFusionReadConnection();
            ps = conn.prepareStatement("select user.balance, user.fundedbalance, currency.* from user, currency where user.currency = currency.code and username = ?");
            ps.setString(1, user.getUsername());
            rs = ps.executeQuery();
            if (rs.next()) {
               balanceData = new AccountBalanceData();
               balanceData.currency = new CurrencyData(rs);
               balanceData.balance = rs.getDouble("balance");
               balanceData.fundedBalance = rs.getDouble("fundedBalance");
               var6 = balanceData;
               return var6;
            }

            log.warn(String.format("FIXME: Failed to get AccountBalance data for user:%s in fusion db", user));
            if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_CHAIN_ON_IF_SHOULD_NOT_REACH_HERE_HAPPENS)) {
               throw new DAOException(String.format("Failed to get AccountBalanceData for user:%s", user));
            }

            balanceData = super.getAccountBalance(user);
         } catch (SQLException var11) {
            log.error(String.format("Failed to get AccountBalanceData for user:%s", user), var11);
            var6 = super.getAccountBalance(user);
            return var6;
         }
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return balanceData;
   }

   public GroupMemberData getGroupMember(UserObject user, int groupID) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      GroupMemberData memberData;
      try {
         conn = DBUtils.getFusionReadConnection();
         String sql = "select m.*, u.displaypicture, sub.id vipsubscriptionid from groupmember m inner join user u on m.username=u.username inner join groups g on m.groupid=g.id left outer join service on (g.vipserviceid=service.id and service.status=?) left outer join subscription sub on (sub.serviceid=service.id and sub.status=? and sub.username=u.username) where m.username=? and m.groupid=? and m.status in (?, ?)";
         ps = conn.prepareStatement(sql);
         ps.setInt(1, ServiceData.StatusEnum.ACTIVE.value());
         ps.setInt(2, SubscriptionData.StatusEnum.ACTIVE.value());
         ps.setString(3, user.getUsername());
         ps.setInt(4, groupID);
         ps.setInt(5, GroupMemberData.StatusEnum.ACTIVE.value());
         ps.setInt(6, GroupMemberData.StatusEnum.BANNED.value());
         rs = ps.executeQuery();
         if (rs.next()) {
            memberData = new GroupMemberData(rs);
            memberData.displayPicture = rs.getString("displayPicture");
            GroupMemberData var8 = memberData;
            return var8;
         }

         memberData = null;
      } catch (SQLException var13) {
         log.error(String.format("Failed to get GroupMemberData for user:%s, group:%s", user, groupID), var13);
         memberData = super.getGroupMember(user, groupID);
         return memberData;
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return memberData;
   }

   public boolean isUserBlackListedInGroup(UserObject user, int groupId) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var7;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select * from groupblacklist where groupid = ? and username = ?");
         ps.setInt(1, groupId);
         ps.setString(2, user.getUsername());
         rs = ps.executeQuery();
         boolean var6;
         if (!rs.next()) {
            var6 = false;
            return var6;
         }

         var6 = true;
         return var6;
      } catch (SQLException var12) {
         log.error(String.format("Failed to check isUserBlackListedInGroup for user:%s, group:%s", user, groupId), var12);
         var7 = super.isUserBlackListedInGroup(user, groupId);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var7;
   }

   public void setAlias(UserObject user, String alias) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = DBUtils.getFusionWriteConnection();
         ps = conn.prepareStatement("INSERT IGNORE INTO useralias (username, alias, dateupdated) values (?, ?, now())");
         ps.setString(1, user.getUsername());
         ps.setString(2, alias);
         if (ps.executeUpdate() < 1) {
            throw new DAOException(String.format("Failed to set alias:%s to user:%s, Due to either duplicate alias or user already has an alias", alias, user));
         }

         super.setAlias(user, alias);
      } catch (SQLException var11) {
         log.error(String.format("Failed to set alias:%s to user:%s", alias, user), var11);
         throw new DAOException(String.format("Failed to set alias:%s to user:%s", alias, user), var11);
      } finally {
         DBUtils.closeResource((ResultSet)rs, ps, conn, log);
      }

   }
}
