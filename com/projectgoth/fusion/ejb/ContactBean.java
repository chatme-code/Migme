package com.projectgoth.fusion.ejb;

import Ice.LocalException;
import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.cache.BroadcastList;
import com.projectgoth.fusion.cache.BroadcastListPersisted;
import com.projectgoth.fusion.cache.ContactList;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.data.DisplayPictureAndStatusMessage;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.util.LookupUtil;
import com.projectgoth.fusion.eventqueue.EventQueue;
import com.projectgoth.fusion.eventqueue.events.FriendAddedEvent;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.restapi.util.MigboApiUtil;
import com.projectgoth.fusion.slice.ContactDataIce;
import com.projectgoth.fusion.slice.EventSystemPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.Message;
import com.projectgoth.fusion.slice.PresenceAndCapabilityIce;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import com.projectgoth.fusion.slice.UserPrx;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

public class ContactBean implements SessionBean {
   public static final String CONTACT_LIST_NAMESPACE = "CL";
   public static final String PENDING_CONTACT_MESSAGE = "<pending>";
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ContactBean.class));
   private static final Logger contactDeletedLog = Logger.getLogger("ContactDeletedLog");
   private static MemCachedClient contactListMemcache;
   private static MemCachedClient broadcastListMemcache;
   private static MemCachedClient bclPersistedMemcache;
   private static MemCachedClient displayPictureAndStatusMessageMemcache;
   private DataSource dataSourceMaster;
   private DataSource dataSourceSlave;
   private SessionContext context;
   private static boolean checkAndPopulateBCL;

   public void setSessionContext(SessionContext newContext) throws EJBException {
      this.context = newContext;
   }

   public void ejbRemove() throws EJBException, RemoteException {
   }

   public void ejbActivate() throws EJBException, RemoteException {
   }

   public void ejbPassivate() throws EJBException, RemoteException {
   }

   public void ejbCreate() throws CreateException {
      try {
         this.dataSourceMaster = LookupUtil.getFusionMasterDataSource();
         this.dataSourceSlave = LookupUtil.getFusionSlaveDataSource();
         SystemProperty.ejbInit(this.dataSourceSlave);
      } catch (Exception var2) {
         log.error("Unable to create Contact EJB", var2);
         throw new CreateException("Unable to create Contact EJB: " + var2.getMessage());
      }
   }

   private void onContactAccepted(String username, String contactUsername) {
      if (SystemProperty.getBool("AddFriendEventEnabled", true)) {
         try {
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.EVENT_SYSTEM_ENABLED)) {
               EventSystemPrx eventSystem = EJBIcePrxFinder.getEventSystemProxy();
               eventSystem.addedFriend(username, contactUsername);
               eventSystem.addedFriend(contactUsername, username);
            }

            EventQueue.enqueueSingleEvent(new FriendAddedEvent(username, contactUsername));
         } catch (Exception var4) {
            log.error("failed to log add friend event for user [" + username + "]", var4);
         }
      }

   }

   public ContactData getContact(int contactID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from contact where id = ?");
         ps.setInt(1, contactID);
         rs = ps.executeQuery();
         if (rs.next()) {
            ContactData contactData = new ContactData(rs);
            this.assignDisplayPictureAndStatusMessageToContacts(conn, Collections.nCopies(1, contactData));
            ContactData var6 = contactData;
            return var6;
         }
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
            conn = null;
         }

      }

      return null;
   }

   public ContactData getContact(String username, String contactUsername) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from contact where username = ? and fusionUsername = ?");
         ps.setString(1, username);
         ps.setString(2, contactUsername);
         rs = ps.executeQuery();
         if (rs.next()) {
            ContactData contactData = new ContactData(rs);
            this.assignDisplayPictureAndStatusMessageToContacts(conn, Collections.nCopies(1, contactData));
            ContactData var7 = contactData;
            return var7;
         }
      } catch (SQLException var25) {
         throw new EJBException(var25.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var24) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var22) {
            conn = null;
         }

      }

      return new ContactData();
   }

   public boolean isFriend(String username, String contactUsername) throws FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select 1 from broadcastlist where username = ? and broadcastUsername = ?");
         ps.setString(1, username);
         ps.setString(2, contactUsername);
         rs = ps.executeQuery();
         var6 = rs.next();
      } catch (SQLException var21) {
         log.error(String.format("unable to check where '%s' is a friend of '%s': %s", contactUsername, username, var21.getMessage()));
         throw new FusionEJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return var6;
   }

   public boolean isBlocking(String username, String blockedUsername) throws FusionEJBException {
      Connection conn = null;

      boolean var5;
      try {
         UserPrx userPrx = null;

         try {
            userPrx = EJBIcePrxFinder.findUserPrx(username);
         } catch (Exception var15) {
            log.warn(String.format("Exception caught while trying to find user proxy of [%s] when checking whether [%s] is on blocklist, falling back to slave database", username, blockedUsername));
         }

         conn = this.dataSourceSlave.getConnection();
         var5 = this.isOnBlockList(username, blockedUsername, conn, userPrx, true);
      } catch (Exception var16) {
         log.error(String.format("Unable to check whether '%s' is blocking  '%s': %s", username, blockedUsername, var16.getMessage()), var16);
         throw new FusionEJBException(var16.getMessage());
      } finally {
         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var14) {
            conn = null;
         }

      }

      return var5;
   }

   public boolean isFriend(int userid, int contactUserid) throws FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select 1 from broadcastlist bcl, userid uid1, userid uid2 where bcl.username = uid1.username and bcl.broadcastUsername = uid2.username and uid1.id = ? and uid2.id = ?");
         ps.setInt(1, userid);
         ps.setInt(2, contactUserid);
         rs = ps.executeQuery();
         var6 = rs.next();
      } catch (SQLException var21) {
         log.error(String.format("unable to check where '%d' is a friend of '%d': %s", contactUserid, userid, var21.getMessage()));
         throw new FusionEJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return var6;
   }

   public ContactGroupData getGroup(int groupID) throws EJBException {
      ContactGroupData contactGroup = new ContactGroupData();
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from contactgroup where id = ?");
         ps.setInt(1, groupID);
         rs = ps.executeQuery();
         if (rs.next()) {
            contactGroup = new ContactGroupData(rs);
         }
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return contactGroup;
   }

   private String getDefaultDisplayName(Connection conn, ContactData contactData) throws EJBException {
      String displayName = null;
      if (contactData.firstName != null) {
         displayName = contactData.firstName;
         if (contactData.lastName != null) {
            displayName = displayName + " " + contactData.lastName;
         }
      } else if (contactData.lastName != null) {
         displayName = contactData.lastName;
      } else if (contactData.fusionUsername != null) {
         displayName = contactData.fusionUsername;
      } else if (contactData.msnUsername != null) {
         displayName = contactData.msnUsername;
      } else if (contactData.aimUsername != null) {
         displayName = contactData.aimUsername;
      } else if (contactData.yahooUsername != null) {
         displayName = contactData.yahooUsername;
      } else if (contactData.gtalkUsername != null) {
         displayName = contactData.gtalkUsername;
      } else if (contactData.facebookUsername != null) {
         displayName = contactData.facebookUsername;
      } else if (contactData.emailAddress != null) {
         displayName = contactData.emailAddress;
      } else if (contactData.mobilePhone != null) {
         displayName = contactData.mobilePhone;
      } else if (contactData.homePhone != null) {
         displayName = contactData.homePhone;
      } else if (contactData.officePhone != null) {
         displayName = contactData.officePhone;
      }

      return displayName;
   }

   public void assignDisplayPictureAndStatusMessageToContacts(Connection conn, Collection<ContactData> contactList) throws SQLException {
      Map<String, ContactData> contactMap = new HashMap();
      Iterator i$ = contactList.iterator();

      ContactData contact;
      while(i$.hasNext()) {
         contact = (ContactData)i$.next();
         if (contact.fusionUsername != null) {
            DisplayPictureAndStatusMessage avatar = DisplayPictureAndStatusMessage.getDisplayPictureAndStatusMessage(displayPictureAndStatusMessageMemcache, contact.fusionUsername);
            if (avatar == null) {
               contactMap.put(contact.fusionUsername, contact);
            } else {
               if (log.isDebugEnabled()) {
                  log.debug("AVATAR for " + contact.fusionUsername + " from Memcached");
               }

               contact.displayPicture = avatar.getDisplayPicture();
               contact.statusMessage = StringUtil.stripHTML(avatar.getStatusMessage());
               contact.statusTimeStamp = avatar.getStatusTimestamp();
            }
         }
      }

      if (contactMap.size() != 0) {
         PreparedStatement ps = null;
         ResultSet rs = null;
         boolean needToCloseConn = false;

         try {
            String parameters = "?" + StringUtil.repeat(",?", contactMap.size() - 1);
            if (conn == null) {
               conn = this.dataSourceSlave.getConnection();
               needToCloseConn = true;
            }

            ps = conn.prepareStatement("select username, displayPicture, statusMessage, statusTimeStamp, dateRegistered from user where username in (" + parameters + ")");
            int i = 0;
            Iterator i$ = contactMap.keySet().iterator();

            while(i$.hasNext()) {
               String key = (String)i$.next();
               ++i;
               ps.setString(i, key);
            }

            rs = ps.executeQuery();

            while(rs.next()) {
               ContactData contact = (ContactData)contactMap.get(rs.getString("username"));
               if (contact != null) {
                  if (log.isDebugEnabled()) {
                     log.debug("AVATAR for " + contact.fusionUsername + " from DB");
                  }

                  contact.displayPicture = rs.getString("displayPicture");
                  contact.statusMessage = StringUtil.stripHTML(rs.getString("statusMessage"));

                  try {
                     contact.statusTimeStamp = rs.getTimestamp("statusTimeStamp");
                  } catch (Exception var25) {
                     contact.statusTimeStamp = rs.getTimestamp("dateRegistered");
                  }

                  DisplayPictureAndStatusMessage avatar = new DisplayPictureAndStatusMessage();
                  avatar.setDisplayPicture(contact.displayPicture);
                  avatar.setStatusMessage(contact.statusMessage);
                  avatar.setStatusTimestamp(contact.statusTimeStamp);
                  DisplayPictureAndStatusMessage.setDisplayPictureAndStatusMessage(displayPictureAndStatusMessageMemcache, contact.fusionUsername, avatar);
               }
            }

         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var24) {
               contact = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var23) {
               i$ = null;
            }

            try {
               if (needToCloseConn && conn != null) {
                  conn.close();
               }
            } catch (SQLException var22) {
               conn = null;
            }

         }
      }
   }

   private int updateContactListVersion(int userID, Connection conn) throws SQLException {
      PreparedStatement ps = null;
      Object rs = null;

      int var6;
      try {
         int version = this.getContactListVersion(userID, conn);
         if (version == 0) {
            ps = conn.prepareStatement("insert into contactlistversion (userid, version) values (?, 1)");
            ps.setInt(1, userID);
         } else {
            ps = conn.prepareStatement("update contactlistversion set version = ? where userid = ? and version = ?");
            ps.setInt(1, version + 1);
            ps.setInt(2, userID);
            ps.setInt(3, version);
         }

         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.CONTACT_LIST_VERSION, String.valueOf(userID));
         if (ps.executeUpdate() != 1) {
            throw new SQLException("Unable to update contact list version");
         }

         var6 = version + 1;
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var16) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var15) {
            ps = null;
         }

      }

      return var6;
   }

   private void checkContactGroupOwnership(String username, int contactGroupId, Connection conn) throws Exception {
      if (contactGroupId != -1 && contactGroupId != -2 && contactGroupId != -3 && contactGroupId != -4 && contactGroupId != -5 && contactGroupId != -6) {
         PreparedStatement ps = conn.prepareStatement("select id from contactgroup where username=? and id=?");
         ResultSet rs = null;

         try {
            ps.setString(1, username);
            ps.setInt(2, contactGroupId);
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new Exception("An invalid group was specified. If this problem persists, please log out and log back in again");
            }
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var15) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var14) {
               ps = null;
            }

         }

      }
   }

   private boolean isOnDBStringList(String username, String qualifyingUsername, String tableName, String fieldName, Connection conn) throws SQLException {
      PreparedStatement ps = conn.prepareStatement("select * from " + tableName + " where username = ? and " + fieldName + " = ?");
      ResultSet rs = null;

      boolean var8;
      try {
         ps.setString(1, username);
         ps.setString(2, qualifyingUsername);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var8 = false;
            return var8;
         }

         var8 = true;
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

      }

      return var8;
   }

   private boolean isOnBlockList(String username, String contactUsername, Connection conn, UserPrx userPrx, boolean fallbackToDB) throws Exception {
      if (userPrx != null) {
         return userPrx.isOnBlockList(contactUsername);
      } else {
         return fallbackToDB ? this.isOnDBStringList(username, contactUsername, "blocklist", "blockusername", conn) : false;
      }
   }

   private boolean isOnContactList(String username, String contactUsername, Connection conn, UserPrx userPrx, boolean fallbackToDB) throws Exception {
      if (userPrx != null) {
         return userPrx.isOnContactList(contactUsername);
      } else {
         return fallbackToDB ? this.isOnDBStringList(username, contactUsername, "contact", "fusionusername", conn) : false;
      }
   }

   private boolean isOnContactListForIdOtherThan(String username, String contactUsername, int id, Connection conn) throws SQLException {
      PreparedStatement ps = conn.prepareStatement("select id from contact where id != ? and username = ? and fusionusername = ?");

      boolean var7;
      try {
         ps.setInt(1, id);
         ps.setString(2, username);
         ps.setString(3, contactUsername);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            var7 = true;
            return var7;
         }

         var7 = false;
      } finally {
         if (ps != null) {
            ps.close();
         }

      }

      return var7;
   }

   private String hasMobilePhoneNumber(String username, Connection conn) throws Exception {
      if (username == null) {
         return null;
      } else {
         PreparedStatement ps = conn.prepareStatement("select mobilephone from user where username = ?");
         ResultSet rs = null;

         String var5;
         try {
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new Exception(username + " is not a valid user");
            }

            var5 = rs.getString("mobilephone");
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var15) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var14) {
               ps = null;
            }

         }

         return var5;
      }
   }

   private void persistContact(ContactData contactData, Connection conn) throws Exception {
      String statement = "insert into contact (username,displayname,firstname,lastname,fusionusername,msnusername,aimusername,yahoousername,icqusername,jabberusername,emailaddress,mobilephone,homephone,officephone,defaultim,defaultphonenumber,contactgroupid,sharemobilephone,displayonphone,status) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      PreparedStatement ps = conn.prepareStatement(statement, 1);
      ResultSet rs = null;

      try {
         ps.setString(1, contactData.username);
         ps.setString(2, contactData.displayName);
         ps.setString(3, contactData.firstName);
         ps.setString(4, contactData.lastName);
         ps.setString(5, contactData.fusionUsername);
         ps.setString(6, contactData.msnUsername);
         ps.setString(7, contactData.aimUsername);
         ps.setString(8, contactData.yahooUsername);
         ps.setString(9, contactData.facebookUsername);
         ps.setString(10, contactData.gtalkUsername);
         ps.setString(11, contactData.emailAddress);
         ps.setString(12, contactData.mobilePhone);
         ps.setString(13, contactData.homePhone);
         ps.setString(14, contactData.officePhone);
         ps.setObject(15, contactData.defaultIM == null ? null : contactData.defaultIM.value());
         ps.setObject(16, contactData.defaultPhoneNumber == null ? null : contactData.defaultPhoneNumber.value());
         ps.setObject(17, contactData.contactGroupId != null && contactData.contactGroupId != -1 && contactData.contactGroupId != -2 && contactData.contactGroupId != -3 && contactData.contactGroupId != -4 && contactData.contactGroupId != -5 && contactData.contactGroupId != -6 ? contactData.contactGroupId : null);
         ps.setObject(18, contactData.shareMobilePhone == null ? null : contactData.shareMobilePhone ? 1 : 0);
         ps.setObject(19, contactData.displayOnPhone == null ? null : contactData.displayOnPhone ? 1 : 0);
         ps.setObject(20, contactData.status == null ? null : contactData.status.value());
         ps.executeUpdate();
         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            throw new Exception("Failed to add a new contact to database");
         }

         contactData.id = rs.getInt(1);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var15) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var14) {
            ps = null;
         }

      }

   }

   private boolean persistPendingContact(String contactUsername, String username, Connection conn) throws SQLException {
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var6;
      try {
         ps = conn.prepareStatement("select * from pendingcontact where username = ? and pendingContact = ?");
         ps.setString(1, contactUsername);
         ps.setString(2, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            rs.close();
            ps.close();
            ps = conn.prepareStatement("select * from blocklist where username = ? and blockusername = ?");
            ps.setString(1, contactUsername);
            ps.setString(2, username);
            rs = ps.executeQuery();
            if (!rs.next()) {
               ps.close();
               ps = conn.prepareStatement("insert into pendingcontact (username, pendingContact) values (?,?)");
               ps.setString(1, contactUsername);
               ps.setString(2, username);
               int result = ps.executeUpdate();
               boolean var7 = result == 1;
               return var7;
            }
         }

         var6 = false;
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

      }

      return var6;
   }

   private boolean contactSharesMobilePhone(String username, String contactUsername) throws SQLException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select sharemobilephone from contact where username = ? and fusionusername = ?");
         ps.setString(1, username);
         ps.setString(2, contactUsername);
         rs = ps.executeQuery();
         if (rs.next() && rs.getInt("sharemobilephone") == 1) {
            boolean var6 = true;
            return var6;
         }
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }

      return false;
   }

   private void unmaskFusionContact(String username, String mobilephone, String fusionusername, Connection conn) throws SQLException {
      log.debug("Executing: update contact set displayname = " + fusionusername + ", fusionusername = " + fusionusername + " where username = " + username + " and mobilephone = " + mobilephone);
      PreparedStatement psUpdateRow = conn.prepareStatement("update contact set displayname = ?, fusionusername = ? where username = ? and mobilephone = ?");
      psUpdateRow.setString(1, fusionusername);
      psUpdateRow.setString(2, fusionusername);
      psUpdateRow.setString(3, username);
      psUpdateRow.setString(4, mobilephone);
      psUpdateRow.executeUpdate();
      psUpdateRow.close();
   }

   private ContactData getMaskedFusionContact(String username, String maskedusername, String mobilephone) throws SQLException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      ContactData var8;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from contact where username = ? and mobilephone = ? and fusionusername is null");
         ps.setString(1, username);
         ps.setString(2, mobilephone);
         rs = ps.executeQuery();
         if (!rs.next()) {
            return null;
         }

         ContactData contactData = new ContactData(rs);
         this.assignDisplayPictureAndStatusMessageToContacts(conn, Collections.nCopies(1, contactData));
         contactData.fusionUsername = maskedusername;
         contactData.displayName = maskedusername;
         contactData.mobilePhone = null;
         log.debug("contactdata.fusionusername:" + contactData.fusionUsername + " and ID: " + contactData.id);
         var8 = contactData;
      } catch (SQLException var26) {
         throw new EJBException(var26.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var25) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var23) {
            conn = null;
         }

      }

      return var8;
   }

   private String updateContactsMobilePhone(String username, String contactUsername, Connection masterConn) throws SQLException {
      if (log.isDebugEnabled()) {
         log.debug("sharing contact [" + username + "] mobilephone with user [" + contactUsername + "]");
      }

      PreparedStatement psGetMobilePhone = null;
      ResultSet rsGetMobilePhone = null;
      ResultSet rsGetIDs = null;
      String mobilePhone = null;
      Connection slaveConn = null;
      PreparedStatement psGetIDs = null;
      PreparedStatement psUpdateRow = null;

      try {
         slaveConn = this.dataSourceSlave.getConnection();
         psGetMobilePhone = slaveConn.prepareStatement("select mobilephone from user where username = ?");
         psGetMobilePhone.setString(1, username);
         rsGetMobilePhone = psGetMobilePhone.executeQuery();
         if (rsGetMobilePhone.next()) {
            mobilePhone = rsGetMobilePhone.getString(1);
         }

         if (mobilePhone != null) {
            psGetIDs = slaveConn.prepareStatement("select id from contact where mobilephone is null and username = ? and fusionusername = ?");
            psGetIDs.setString(1, contactUsername);
            psGetIDs.setString(2, username);
            rsGetIDs = psGetIDs.executeQuery();
            psUpdateRow = masterConn.prepareStatement("update contact set mobilephone = ? where id = ?");

            while(rsGetIDs.next()) {
               psUpdateRow.setString(1, mobilePhone);
               psUpdateRow.setInt(2, rsGetIDs.getInt(1));
               if (psUpdateRow.executeUpdate() < 1) {
                  log.warn("unable to share mobile phone number [" + mobilePhone + "] belonging to contact [" + username + "] with user [" + contactUsername + "]");
               }
            }
         }
      } finally {
         if (rsGetMobilePhone != null) {
            try {
               rsGetMobilePhone.close();
            } catch (SQLException var32) {
               rsGetMobilePhone = null;
            }
         }

         if (rsGetIDs != null) {
            try {
               rsGetIDs.close();
            } catch (SQLException var31) {
               rsGetIDs = null;
            }
         }

         if (psGetMobilePhone != null) {
            try {
               psGetMobilePhone.close();
            } catch (SQLException var30) {
               psGetMobilePhone = null;
            }
         }

         if (psUpdateRow != null) {
            try {
               psUpdateRow.close();
            } catch (SQLException var29) {
               psUpdateRow = null;
            }
         }

         if (psGetIDs != null) {
            try {
               psGetIDs.close();
            } catch (SQLException var28) {
               psGetIDs = null;
            }
         }

         if (slaveConn != null) {
            try {
               slaveConn.close();
            } catch (SQLException var27) {
               slaveConn = null;
            }
         }

      }

      return mobilePhone;
   }

   private void setContactOffline(ContactData contactData) {
      contactData.fusionPresence = PresenceType.OFFLINE;
      contactData.aimPresence = PresenceType.OFFLINE;
      contactData.gtalkPresence = PresenceType.OFFLINE;
      contactData.msnPresence = PresenceType.OFFLINE;
      contactData.yahooPresence = PresenceType.OFFLINE;
      contactData.facebookPresence = PresenceType.OFFLINE;
   }

   private boolean hasTooManyPendingContacts(String username, Connection conn) throws SQLException, NoSuchFieldException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var6;
      try {
         ch = new ConnectionHolder(this.dataSourceSlave, conn);
         ps = ch.getConnection().prepareStatement("select count(*) from pendingcontact where username=?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next() || rs.getInt(1) < SystemProperty.getInt("MaxPendingContacts")) {
            return false;
         }

         var6 = true;
      } finally {
         if (ch != null) {
            ch.close();
         }

      }

      return var6;
   }

   private boolean hasTooManyFusionContacts(String username, Connection conn) throws SQLException, NoSuchFieldException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var6;
      try {
         ch = new ConnectionHolder(this.dataSourceSlave, conn);
         ps = ch.getConnection().prepareStatement("select count(*) from contact where username=?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next() || rs.getInt(1) < SystemProperty.getInt("MaxFusionContacts")) {
            return false;
         }

         var6 = true;
      } finally {
         if (ch != null) {
            ch.close();
         }

      }

      return var6;
   }

   public ContactData addFusionUserAsContact(int userID, ContactData contactData, boolean followContactOnMiniblog) throws EJBException, FusionEJBException {
      Connection connMaster = null;
      Connection connSlave = null;
      UserTransaction userTransaction = null;

      try {
         if (userID < 1) {
            throw new FusionEJBException("Invalid userID provided");
         }

         if (contactData == null) {
            throw new FusionEJBException("Invalid contact data provided");
         }

         if (StringUtil.isBlank(contactData.username)) {
            throw new FusionEJBException("Please provide a valid username.");
         }

         if (this.hasTooManyFusionContacts(contactData.username, (Connection)null)) {
            throw new FusionEJBException("You cannot add any more migme contacts. Please remove some first");
         }

         if (StringUtil.isBlank(contactData.fusionUsername)) {
            throw new FusionEJBException("Please provide a valid username to add.");
         }

         if (contactData.username.equalsIgnoreCase(contactData.fusionUsername)) {
            throw new FusionEJBException("You cannot add yourself to your contact list.");
         }

         log.info(String.format("addFusionUserAsContact  %s add %s as contact. userid %d followContactOnMiniblog %s", contactData.username, contactData.fusionUsername, userID, followContactOnMiniblog));
         connSlave = this.dataSourceSlave.getConnection();
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         int toUserId = userEJB.getUserID(contactData.fusionUsername, connSlave, false);
         if (toUserId < 0) {
            throw new FusionEJBException(String.format("Unable to find user '%s'", contactData.fusionUsername));
         }

         if (!AuthenticatedAccessControl.hasAccessByUsernameLocal(AuthenticatedAccessControlTypeEnum.ADD_FRIEND, contactData.username)) {
            throw new FusionEJBException("You must be authenticated before you can add new contacts.");
         }

         if (!AuthenticatedAccessControl.hasAccessByUsernameLocal(AuthenticatedAccessControlTypeEnum.BE_ADDED_AS_FRIEND, contactData.fusionUsername)) {
            throw new FusionEJBException("You can only add authenticated accounts to your contact list.");
         }

         if (contactData.displayName == null) {
            contactData.displayName = this.getDefaultDisplayName(connMaster, contactData);
         }

         if (contactData.displayOnPhone == null) {
            contactData.displayOnPhone = true;
         }

         UserPrx userPrx = EJBIcePrxFinder.findUserPrx(contactData.username);
         UserPrx contactPrx = EJBIcePrxFinder.findUserPrx(contactData.fusionUsername);
         if (this.isOnBlockList(contactData.username, contactData.fusionUsername, connSlave, userPrx, true)) {
            throw new FusionEJBException(contactData.displayName + " is blocked, you need to unblock them before adding them to your contact list.");
         }

         if (this.isOnContactList(contactData.username, contactData.fusionUsername, connSlave, userPrx, true)) {
            throw new FusionEJBException(contactData.displayName + " is already on the contact list");
         }

         if (this.isOnBlockList(contactData.fusionUsername, contactData.username, connSlave, contactPrx, true)) {
            throw new FusionEJBException(contactData.displayName + " is not accepting invitations currently.");
         }

         if (contactData.contactGroupId != null) {
            this.checkContactGroupOwnership(contactData.username, contactData.contactGroupId, connSlave);
         }

         boolean friendingEventOccurred = this.isOnContactList(contactData.fusionUsername, contactData.username, connSlave, contactPrx, true);
         int newInviterContactListVersion = -1;
         if (connSlave != null) {
            connSlave.close();
            connSlave = null;
         }

         contactData.status = ContactData.StatusEnum.ACTIVE;
         this.setContactOffline(contactData);
         connMaster = this.dataSourceMaster.getConnection();
         userTransaction = this.context.getUserTransaction();
         userTransaction.begin();
         this.persistContact(contactData, connMaster);
         contactData.statusMessage = "";
         if (friendingEventOccurred) {
            log.debug(contactData.fusionUsername + " aready have " + contactData.username + " on contact list, updating broadcastlist in database");
            this.persistBroadcastListEntry(contactData.username, contactData.fusionUsername, connMaster);
            this.persistBroadcastListEntry(contactData.fusionUsername, contactData.username, connMaster);
            newInviterContactListVersion = this.onContactListModified(toUserId, contactData.fusionUsername, connMaster);
         }

         this.removeFromPendingContacts(contactData.username, contactData.fusionUsername, connMaster);
         int newContactListVersion = this.onContactListModified(userID, contactData.username, connMaster);
         userTransaction.commit();
         if (friendingEventOccurred) {
            log.debug(contactData.fusionUsername + " aready have " + contactData.username + " on contact list, updating broadcastlist in memcache");
            this.updateBroadcastListEntryInMemCached(contactData.username, contactData.fusionUsername);
            this.updateBroadcastListEntryInMemCached(contactData.fusionUsername, contactData.username);

            try {
               this.onContactAccepted(contactData.username, contactData.fusionUsername);
               this.onContactAccepted(contactData.fusionUsername, contactData.username);
            } catch (Exception var40) {
               log.error(String.format("Failed to submit friend-added event from %d to %d : %s", userID, toUserId, var40.getMessage()), var40);
            }
         }

         MigboApiUtil updatedContactDataWithPresence;
         try {
            if (followContactOnMiniblog) {
               updatedContactDataWithPresence = MigboApiUtil.getInstance();
               long curTime = System.currentTimeMillis();
               boolean oneWayCall = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.ONEWAY_MIGBO_API_CALLS_ENABLED);
               String pathPrefix = String.format("/user/%d/following_request/%d?requestingUserid=%s&action=%s", userID, toUserId, userID, "follow");
               if (oneWayCall) {
                  updatedContactDataWithPresence.postOneWay(pathPrefix, "");
                  log.info(String.format("Follow request from %d to %d completed in %d ms - result ONEWAY", userID, toUserId, System.currentTimeMillis() - curTime));
               } else {
                  JSONObject result = updatedContactDataWithPresence.post(pathPrefix, "");
                  log.info(String.format("Follow request from %d to %d completed in %d ms - result %s", userID, toUserId, System.currentTimeMillis() - curTime, result == null ? "FAILED" : result.toString()));
               }
            }
         } catch (Exception var39) {
            log.error(String.format("Failed to push following request from %d to %d : %s", userID, toUserId, var39.getMessage()), var39);
         }

         if (userPrx != null) {
            if (friendingEventOccurred) {
               updatedContactDataWithPresence = null;
               log.debug(String.format("Updating userproxy of [%s] with new contact list information & version", contactData.username));
               ContactDataIce updatedContactDataWithPresence = userPrx.acceptContactRequest(contactData.toIceObject(), contactPrx, newInviterContactListVersion, newContactListVersion);
               if (contactPrx != null) {
                  if (updatedContactDataWithPresence != null) {
                     log.debug("Getting presence for contact and sending back to acceptor");
                     contactData.copyPresenceAndCapability(updatedContactDataWithPresence);
                  }
               } else {
                  contactData.fusionPresence = PresenceType.OFFLINE;
               }
            } else {
               if (contactData.contactGroupId != null && contactData.contactGroupId < 0) {
                  contactData.contactGroupId = -1;
               }

               userPrx.addContact(contactData.toIceObject(), newContactListVersion);
            }
         }
      } catch (LocalException var41) {
         log.warn("Failed to update User object in ObjectCache, ignoring.", var41);
      } catch (FusionEJBException var42) {
         log.error("Failed to addFusionUserAsContact - FusionEJBException caught. Username [" + contactData.username + "]. Contact username [" + contactData.fusionUsername + "]", var42);
         throw var42;
      } catch (Exception var43) {
         log.error("Failed to addFusionUserAsContact - EJBException caught. Username [" + contactData.username + "]. Contact username [" + contactData.fusionUsername + "]", var43);

         try {
            if (userTransaction != null) {
               userTransaction.rollback();
            }
         } catch (Exception var38) {
         }

         throw new EJBException(var43.getMessage(), var43);
      } finally {
         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var37) {
            connMaster = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var36) {
            connSlave = null;
         }

      }

      return contactData;
   }

   public Set<String> getRecentFollowers(int userID) throws EJBException {
      if (userID < 1) {
         throw new EJBException("Invalid userID provided");
      } else {
         HashSet recentFollowerUsernames = new HashSet();

         try {
            UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
            Map<String, Map<String, String>> newFollowerAlerts = unsProxy.getPendingNotificationDataForUserByType(userID, Enums.NotificationTypeEnum.NEW_FOLLOWER_ALERT.getType());
            Iterator i$ = newFollowerAlerts.values().iterator();

            while(i$.hasNext()) {
               Map<String, String> p = (Map)i$.next();
               String username = (String)p.get("otherUsername");
               if (!StringUtil.isBlank(username)) {
                  recentFollowerUsernames.add(username);
               }
            }
         } catch (FusionException var13) {
            log.warn("Unexpected FusionException while retrieving recent followers :" + var13.message, var13);
         } catch (Exception var14) {
            log.warn("Unexpected exception while retrieving recent followers :" + var14.getMessage(), var14);
         } finally {
            ;
         }

         if (log.isDebugEnabled()) {
            log.debug("Returning [" + recentFollowerUsernames.size() + "] recent followers");
         }

         return recentFollowerUsernames;
      }
   }

   public void removeFusionUserFromContact(int userID, String username, int contactID, boolean unfollowContactOnMiniblog) throws EJBException, FusionEJBException {
      if (userID < 1) {
         throw new FusionEJBException("Invalid userID provided");
      } else if (contactID < 1) {
         log.info(String.format("Invalid contactID provided [%d] skipping.", contactID));
      } else if (StringUtil.isBlank(username)) {
         throw new FusionEJBException("Invalid username provided");
      } else {
         log.info(String.format("removeFusionUserFromContact userid %d username %s contactID %d followContactOnMiniblog %s", userID, username, contactID, unfollowContactOnMiniblog ? "true" : "false"));
         Connection connMaster = null;
         Connection connSlave = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         UserTransaction userTransaction = this.context.getUserTransaction();

         try {
            connMaster = this.dataSourceMaster.getConnection();
            connSlave = this.dataSourceSlave.getConnection();
            ps = connSlave.prepareStatement("select username, fusionusername from contact where id = ?");
            ps.setInt(1, contactID);
            rs = ps.executeQuery();
            if (rs.next()) {
               if (!username.equalsIgnoreCase(rs.getString("username"))) {
                  throw new FusionEJBException("You cannot remove contact that does not belong to you.");
               }

               String fusionUsername = rs.getString("fusionUsername");
               ps.close();
               UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               int contactUserId = userEJB.getUserID(fusionUsername, connSlave);
               userTransaction.begin();
               this.removeContact(contactID, connMaster);
               if (StringUtils.hasLength(fusionUsername)) {
                  this.removeFromBroadcastList(username, fusionUsername, connMaster);
                  this.removeFromBroadcastListInMemCached(username, fusionUsername);
                  this.removeFromBroadcastList(fusionUsername, username, connMaster);
                  this.removeFromBroadcastListInMemCached(fusionUsername, username);
               }

               int newContactListVersion = this.onContactListModified(userID, username, connMaster);
               userTransaction.commit();
               if (StringUtils.hasLength(fusionUsername)) {
                  contactDeletedLog.info(username + " removed " + fusionUsername + "(ID: " + contactID + ")");
               }

               UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);
               if (userPrx != null) {
                  userPrx.removeContact(contactID, newContactListVersion);
               }

               UserPrx contactPrx = EJBIcePrxFinder.findUserPrx(fusionUsername);
               if (contactPrx != null) {
                  contactPrx.stopBroadcastingTo(username);
               }

               try {
                  if (unfollowContactOnMiniblog) {
                     MigboApiUtil apiUtil = MigboApiUtil.getInstance();
                     long curTime = System.currentTimeMillis();
                     boolean oneWayCall = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.ONEWAY_MIGBO_API_CALLS_ENABLED);
                     String pathPrefix = String.format("/user/%d/following_request/%d?requestingUserid=%s&action=%s", userID, contactUserId, userID, "unfollow");
                     if (oneWayCall) {
                        apiUtil.postOneWay(pathPrefix, "");
                        log.info(String.format("Unfollow request from %d to %d completed in %d ms - result ONEWAY", userID, contactUserId, System.currentTimeMillis() - curTime));
                     } else {
                        JSONObject result = apiUtil.post(pathPrefix, "");
                        log.info(String.format("Unfollow request from %d to %d completed in %d ms - result %s", userID, contactUserId, System.currentTimeMillis() - curTime, result == null ? "FAILED" : result.toString()));
                     }

                     return;
                  }
               } catch (Exception var46) {
                  log.error(String.format("Failed to push following request from %d to %d : %s", userID, contactUserId, var46.getMessage()), var46);
               }

               return;
            }

            log.warn("contact id [" + contactID + "] was previously deleted from contact [" + username + "] or never was a contact?");
         } catch (LocalException var47) {
            return;
         } catch (Exception var48) {
            log.error("Failed to remove contact", var48);

            try {
               if (userTransaction != null) {
                  userTransaction.rollback();
               }
            } catch (Exception var45) {
            }

            throw new EJBException(var48.getMessage());
         } finally {
            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var44) {
               ps = null;
            }

            try {
               if (connMaster != null) {
                  connMaster.close();
               }
            } catch (SQLException var43) {
               connMaster = null;
            }

            try {
               if (connSlave != null) {
                  connSlave.close();
               }
            } catch (SQLException var42) {
               connSlave = null;
            }

         }

      }
   }

   public ContactData addPendingFusionContact(int userID, ContactData contactData) throws EJBException {
      Connection connMaster = null;
      Connection connSlave = null;
      UserTransaction userTransaction = null;
      if (StringUtil.isBlank(contactData.username)) {
         throw new EJBException("Please provide a contact to add.");
      } else if (contactData.username.equalsIgnoreCase(contactData.fusionUsername)) {
         throw new EJBException("You cannot add yourself to your contact list.");
      } else {
         try {
            if (contactData.displayName == null) {
               contactData.displayName = this.getDefaultDisplayName(connMaster, contactData);
            }

            if (!MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.CONTACT.toString(), contactData.fusionUsername, 1L, 10000L)) {
               log.error("[" + contactData.username + "] tried to add [" + contactData.fusionUsername + "] too soon");
               throw new EJBException(contactData.displayName + " is unable to receive invites at this time. Please try again a little bit later");
            }

            if (this.hasTooManyPendingContacts(contactData.fusionUsername, (Connection)null)) {
               log.error("[" + contactData.username + "] tried to add [" + contactData.fusionUsername + "] who has too many invites");
               throw new EJBException(contactData.displayName + " is unable to receive invites at this time. Please try again later");
            }

            if (this.hasTooManyFusionContacts(contactData.username, (Connection)null)) {
               throw new EJBException("You cannot add any more migme contacts. Please remove some first");
            }

            if (!AuthenticatedAccessControl.hasAccessByUsernameLocal(AuthenticatedAccessControlTypeEnum.ADD_FRIEND, contactData.username) && SystemProperty.getBool("AddContactDisabledForUnauthenticatedUsers", false)) {
               throw new EJBException("You must be authenticated before you can add new contacts.");
            }

            if (!AuthenticatedAccessControl.hasAccessByUsernameLocal(AuthenticatedAccessControlTypeEnum.BE_ADDED_AS_FRIEND, contactData.fusionUsername) && SystemProperty.getBool("OnlyAddAuthenticatedContacts", false)) {
               throw new EJBException("You can only add authenticated accounts to your contact list.");
            }

            UserPrx userPrx = EJBIcePrxFinder.findUserPrx(contactData.username);
            UserPrx contactPrx = EJBIcePrxFinder.findUserPrx(contactData.fusionUsername);
            connSlave = this.dataSourceSlave.getConnection();
            if (this.isOnBlockList(contactData.username, contactData.fusionUsername, connSlave, userPrx, false)) {
               throw new EJBException(contactData.displayName + " is blocked, you need to unblock them before adding them to your contact list.");
            }

            if (this.isOnContactList(contactData.username, contactData.fusionUsername, connSlave, userPrx, true)) {
               throw new Exception(contactData.displayName + " is already on the contact list");
            }

            ContactData maskedUserData = this.getMaskedFusionContact(contactData.username, contactData.fusionUsername, contactData.mobilePhone);
            if (maskedUserData != null) {
               throw new Exception(contactData.mobilePhone + " is already on the contact list");
            }

            if (this.isOnBlockList(contactData.fusionUsername, contactData.username, connSlave, contactPrx, true)) {
               throw new EJBException(contactData.displayName + " is not accepting invitations currently.");
            }

            if (this.isOnContactList(contactData.fusionUsername, contactData.username, connSlave, contactPrx, true)) {
               log.debug(contactData.fusionUsername + " aready have " + contactData.username + " on their contact list, going straight to accept");
               contactData.displayName = contactData.fusionUsername;
               ContactData var38 = this.acceptContactRequest(userID, contactData, true);
               return var38;
            }

            if (contactData.contactGroupId != null) {
               this.checkContactGroupOwnership(contactData.username, contactData.contactGroupId, connSlave);
            }

            this.hasMobilePhoneNumber(contactData.fusionUsername, connSlave);
            if (connSlave != null) {
               connSlave.close();
               connSlave = null;
            }

            contactData.status = ContactData.StatusEnum.ACTIVE;
            this.setContactOffline(contactData);
            connMaster = this.dataSourceMaster.getConnection();
            userTransaction = this.context.getUserTransaction();
            userTransaction.begin();
            if (StringUtils.hasLength(contactData.fusionUsername)) {
               this.persistPendingContact(contactData.fusionUsername, contactData.username, connMaster);
            }

            String originalfusionUsername = null;
            if (!contactData.fusionUsername.equalsIgnoreCase(contactData.displayName) && contactData.mobilePhone != null) {
               originalfusionUsername = contactData.fusionUsername;
               contactData.fusionUsername = null;
            }

            this.persistContact(contactData, connMaster);
            if (originalfusionUsername != null) {
               contactData.fusionUsername = originalfusionUsername;
            }

            contactData.statusMessage = "<pending>";
            int newContactListVersion = this.onContactListModified(userID, contactData.username, connMaster);
            userTransaction.commit();

            try {
               UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               int toUserId = userEJB.getUserID(contactData.fusionUsername, connMaster);
               UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
               Map<String, String> parameters = new HashMap();
               parameters.put("requestorId", Integer.toString(userID));
               parameters.put("requestorUserName", contactData.username);
               unsProxy.notifyFusionUser(new Message(contactData.username, toUserId, contactData.fusionUsername, Enums.NotificationTypeEnum.FRIEND_INVITE.getType(), System.currentTimeMillis(), parameters));
            } catch (Exception var34) {
               log.error("Failed to push friend invite notification for user [" + contactData.username + "]", var34);
            }

            if (userPrx != null) {
               if (contactData.contactGroupId != null && contactData.contactGroupId < 0) {
                  contactData.contactGroupId = -1;
               }

               if (originalfusionUsername != null) {
                  contactData.fusionUsername = null;
               }

               userPrx.addContact(contactData.toIceObject(), newContactListVersion);
            }

            if (contactPrx != null) {
               contactPrx.addPendingContact(contactData.username);
            }
         } catch (LocalException var35) {
            log.warn("Failed to update User object in ObjectCache, ignore", var35);
         } catch (Exception var36) {
            log.error("Failed to add pending contact. Username [" + contactData.username + "]. Contact username [" + contactData.fusionUsername + "]", var36);

            try {
               if (userTransaction != null) {
                  userTransaction.rollback();
               }
            } catch (Exception var33) {
            }

            throw new EJBException(var36.getMessage());
         } finally {
            try {
               if (connMaster != null) {
                  connMaster.close();
               }
            } catch (SQLException var32) {
               connMaster = null;
            }

            try {
               if (connSlave != null) {
                  connSlave.close();
               }
            } catch (SQLException var31) {
               connSlave = null;
            }

         }

         return contactData;
      }
   }

   public ContactData acceptContactRequest(int userID, ContactData contactData, boolean ignoreMissingContactRequest) throws EJBException {
      if (log.isDebugEnabled()) {
         log.debug("user [" + contactData.username + "] accepting contact [" + contactData.fusionUsername + "]");
      }

      Connection masterConnection = null;
      UserTransaction userTransaction = null;

      try {
         if (this.hasTooManyFusionContacts(contactData.username, (Connection)null)) {
            throw new EJBException("You cannot accept any more migme contact requests. Please remove some contacts first");
         }

         UserPrx userPrx = EJBIcePrxFinder.findUserPrx(contactData.username);
         UserPrx contactPrx = EJBIcePrxFinder.findUserPrx(contactData.fusionUsername);
         masterConnection = this.dataSourceMaster.getConnection();
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData userData = userEJB.loadUserFromID(userID);
         if (contactData.contactGroupId != null) {
            this.checkContactGroupOwnership(contactData.username, contactData.contactGroupId, masterConnection);
         }

         if (this.isOnContactList(contactData.username, contactData.fusionUsername, masterConnection, userPrx, false)) {
            log.warn("contact [" + contactData.fusionUsername + "] is already on the user's [" + contactData.username + "] contact list");
            throw new EJBException(contactData.fusionUsername + " is already on the contact list");
         }

         Boolean usernameBCLHasBeenPersisted = isBroadcastListPersisted(contactData.username);
         Boolean fusionUsernameBCLHasBeenPersisted = isBroadcastListPersisted(contactData.fusionUsername);
         this.hasMobilePhoneNumber(contactData.fusionUsername, masterConnection);
         if (contactData.displayName == null) {
            contactData.displayName = this.getDefaultDisplayName(masterConnection, contactData);
         }

         contactData.status = ContactData.StatusEnum.ACTIVE;
         ContactData maskedContactData = this.getMaskedFusionContact(contactData.fusionUsername, contactData.username, userData.mobilePhone);
         int inviterUserID = userEJB.getUserID(contactData.fusionUsername, masterConnection);
         boolean sharesMobilePhone = this.contactSharesMobilePhone(contactData.fusionUsername, contactData.username);
         userTransaction = this.context.getUserTransaction();
         userTransaction.begin();
         this.persistContact(contactData, masterConnection);
         int newInviterContactListVersion = -1;
         if (maskedContactData != null) {
            this.unmaskFusionContact(contactData.fusionUsername, userData.mobilePhone, contactData.username, masterConnection);
         }

         if (contactData.shareMobilePhone != null && contactData.shareMobilePhone) {
            this.updateContactsMobilePhone(contactData.username, contactData.fusionUsername, masterConnection);
         }

         if (maskedContactData != null || contactData.shareMobilePhone != null && contactData.shareMobilePhone) {
            newInviterContactListVersion = this.onContactListModified(inviterUserID, contactData.fusionUsername, masterConnection);
         }

         if (sharesMobilePhone) {
            contactData.mobilePhone = this.updateContactsMobilePhone(contactData.fusionUsername, contactData.username, masterConnection);
         }

         if (!this.removeFromPendingContacts(contactData.username, contactData.fusionUsername, masterConnection) && !ignoreMissingContactRequest) {
            throw new Exception("There is no longer an invitation from " + contactData.fusionUsername + " to accept");
         }

         if (usernameBCLHasBeenPersisted) {
            this.persistBroadcastListEntry(contactData.username, contactData.fusionUsername, masterConnection);
         }

         this.updateBroadcastListEntryInMemCached(contactData.username, contactData.fusionUsername);
         if (checkAndPopulateBCL && fusionUsernameBCLHasBeenPersisted) {
            Collection<String> bcl = this.checkAndPopulateBCL(contactData.fusionUsername, masterConnection);
            if (!bcl.contains(contactData.username)) {
               this.persistBroadcastListEntry(contactData.fusionUsername, contactData.username, masterConnection);
            }
         }

         log.debug(String.format("Updating broadcastlist entry in memcached [%s] [%s]", contactData.fusionUsername, contactData.username));
         this.updateBroadcastListEntryInMemCached(contactData.fusionUsername, contactData.username);
         log.debug("Assigning display picture and status message");
         this.assignDisplayPictureAndStatusMessageToContacts(masterConnection, Collections.nCopies(1, contactData));
         log.debug(String.format("updating contactlist version for userid [%d] username [%s]", userID, contactData.username));
         int newContactListVersion = this.onContactListModified(userID, contactData.username, masterConnection);
         log.debug("Committing transaction");
         userTransaction.commit();

         try {
            if (masterConnection != null) {
               masterConnection.close();
               masterConnection = null;
            }
         } catch (SQLException var31) {
         }

         log.debug(String.format("Create user events for both sides of the relationship [%s] [%s]", contactData.username, contactData.fusionUsername));
         this.onContactAccepted(contactData.username, contactData.fusionUsername);
         log.debug(String.format("Remove Friend Invite Notification for userid [%d] username[%s]", userID, contactData.fusionUsername));
         this.removeFriendInviteAndNewFollowerNotifications(userID, contactData.fusionUsername, inviterUserID);
         ContactDataIce updatedContactDataWithPresence = null;
         if (userPrx != null) {
            log.debug(String.format("Updating userproxy of [%s] with new contact list version", contactData.username));
            updatedContactDataWithPresence = userPrx.acceptContactRequest(contactData.toIceObject(), contactPrx, newInviterContactListVersion, newContactListVersion);
         }

         if (contactPrx != null) {
            if (updatedContactDataWithPresence != null) {
               log.debug("Getting presence for contact and sending back to acceptor");
               contactData.copyPresenceAndCapability(updatedContactDataWithPresence);
            }

            if (maskedContactData != null) {
               log.debug(String.format("Sending alert to  [%s] ", contactData.fusionUsername));
               contactPrx.putAlertMessage(contactData.username + " (" + userData.mobilePhone + ") has accepted your invitation. Please relogin to refresh your contact list.", "Friends Invite", (short)0);
            }
         } else {
            contactData.fusionPresence = PresenceType.OFFLINE;
         }

         if (userPrx != null) {
            log.debug(String.format("Notifying new contact to session belonging to [%s] ", contactData.username));
            userPrx.notifySessionsOfNewContact(contactData.toIceObject(), newContactListVersion, false);
         }
      } catch (LocalException var32) {
         log.warn("Failed to update User object in ObjectCache, ignore", var32);
      } catch (Exception var33) {
         log.error("Failed to accept contact request. username: " + contactData.username + " fusionUsername: " + contactData.fusionUsername + " Exception: " + var33.getMessage(), var33);

         try {
            if (userTransaction != null) {
               userTransaction.rollback();
            }
         } catch (Exception var30) {
         }

         throw new EJBException(var33.getMessage(), var33);
      } finally {
         try {
            if (masterConnection != null) {
               masterConnection.close();
            }
         } catch (SQLException var29) {
         }

      }

      if (log.isDebugEnabled()) {
         log.debug("acceptContactRequest, returning contact [" + contactData.fusionUsername + "] with presence [" + contactData.fusionPresence + "]");
      }

      return contactData;
   }

   private void removeFriendInviteAndNewFollowerNotifications(int userId, String inviterUsername, int inviterUserid) {
      try {
         UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
         unsProxy.clearNotificationsForUser(userId, Enums.NotificationTypeEnum.FRIEND_INVITE.getType(), new String[]{inviterUsername});
         unsProxy.clearNotificationsForUser(userId, Enums.NotificationTypeEnum.NEW_FOLLOWER_ALERT.getType(), new String[]{Integer.toString(inviterUserid)});
      } catch (Exception var5) {
         log.warn("Failed to remove friend invite / new follower notification for user [" + userId + "]", var5);
      }

   }

   public void makeReferrerAndReferreeFriends(int referreeUserID, String referreeUsername, String referreeMobilePhone, int referrerUserID, String referrerUsername, String referrerDisplayName, String referrerMobilePhone) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      UserTransaction userTransaction = null;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         userTransaction = this.context.getUserTransaction();
         userTransaction.begin();
         this.onContactListModified(referreeUserID, referreeUsername, connMaster);
         int newReferrerContactListVersion = this.onContactListModified(referrerUserID, referrerUsername, connMaster);
         ContactData referreesNewContact = new ContactData();
         referreesNewContact.username = referreeUsername;
         referreesNewContact.displayName = referrerDisplayName == null ? referrerUsername : referrerDisplayName;
         referreesNewContact.fusionUsername = referrerUsername;
         referreesNewContact.mobilePhone = referrerMobilePhone;
         referreesNewContact.status = ContactData.StatusEnum.ACTIVE;
         referreesNewContact.displayOnPhone = true;
         this.persistContact(referreesNewContact, connMaster);
         ContactData referrersNewContact = new ContactData();
         referrersNewContact.username = referrerUsername;
         referrersNewContact.displayName = referreeUsername;
         referrersNewContact.fusionUsername = referreeUsername;
         referrersNewContact.mobilePhone = referreeMobilePhone;
         referrersNewContact.status = ContactData.StatusEnum.ACTIVE;
         referrersNewContact.displayOnPhone = true;
         this.persistContact(referrersNewContact, connMaster);
         this.persistBroadcastListEntry(referreeUsername, referrerUsername, connMaster);
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.BCLPERSISTED_ENABLED)) {
            BroadcastListPersisted.setBroadcastListPersisted(bclPersistedMemcache, referreeUsername, 1);
         }

         Set<String> referreeBCL = BroadcastList.newBroadcastList();
         referreeBCL.add(referrerUsername);
         BroadcastList.setBroadcastList(broadcastListMemcache, referreeUsername, referreeBCL);
         if (checkAndPopulateBCL && isBroadcastListPersisted(referrerUsername)) {
            this.persistBroadcastListEntry(referrerUsername, referreeUsername, connMaster);
         }

         userTransaction.commit();
         this.updateBroadcastListEntryInMemCached(referrerUsername, referreeUsername);

         try {
            this.onContactAccepted(referreeUsername, referrerUsername);
            UserPrx referrerUserPrx = EJBIcePrxFinder.findUserPrx(referrerUsername);
            if (referrerUserPrx != null) {
               referrersNewContact.fusionPresence = PresenceType.OFFLINE;
               referrerUserPrx.addToContactAndBroadcastLists(referrersNewContact.toIceObject(), newReferrerContactListVersion);
            }
         } catch (Exception var37) {
            log.warn("Failed post processing in makeReferrerAndReferreeFriends()", var37);
         }
      } catch (SQLException var38) {
         log.warn("SQLException in makeReferrerAndReferreeFriends", var38);

         try {
            if (userTransaction != null) {
               userTransaction.rollback();
            }
         } catch (Exception var35) {
         }

         throw new EJBException(var38.getMessage());
      } catch (Exception var39) {
         log.warn("Exception in makeReferrerAndReferreeFriends", var39);

         try {
            if (userTransaction != null) {
               userTransaction.rollback();
            }
         } catch (Exception var36) {
         }

         throw new EJBException(var39.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var34) {
            rs = null;
         }

         try {
            if (ps != null) {
               ((PreparedStatement)ps).close();
            }
         } catch (SQLException var33) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var32) {
            connMaster = null;
         }

      }

      if (log.isDebugEnabled()) {
         log.debug("makeReferrerAndReferreeFriends finished");
      }

   }

   public Set<String> checkAndPopulateBCL(String username, Connection slaveConnection) throws EJBException {
      label88: {
         Set var6;
         try {
            if (!MemCachedUtils.getLock(broadcastListMemcache, "BLDL", username, 15000)) {
               log.error("Failed to get a lock to update user [" + username + "]'s BCL in 10 seconds");
               break label88;
            }

            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            Set<String> broadcastList = userEJB.loadBroadcastList(username, slaveConnection);
            Set bcl;
            if (broadcastList != null && !broadcastList.isEmpty()) {
               log.debug(username + " already has a BCL, not generating, using the existing one");
               bcl = broadcastList;
               return bcl;
            }

            bcl = this.generateBCL(username, slaveConnection);
            BroadcastList.setBroadcastList(broadcastListMemcache, username, bcl);
            var6 = bcl;
         } catch (Exception var11) {
            log.error("failed to commit new BCL, rolling back", var11);
            throw new EJBException("failed to persist new BCL for user [" + username + "]");
         } finally {
            MemCachedUtils.releaseLock(broadcastListMemcache, "BLDL", username);
         }

         return var6;
      }

      if (log.isDebugEnabled()) {
         log.debug("broadcast list for user [" + username + "] not persisted as it's empty");
      }

      return Collections.emptySet();
   }

   public void persistBroadcastList(String username, Set<String> bcl, Connection connection) throws SQLException {
      if (log.isDebugEnabled()) {
         log.debug("persisting BCL for user [" + username + "]");
      }

      PreparedStatement ps = connection.prepareStatement("insert into broadcastlist values (?,?)");
      Iterator i$ = bcl.iterator();

      while(i$.hasNext()) {
         String receiver = (String)i$.next();
         ps.setString(1, username);
         ps.setString(2, receiver);
         ps.addBatch();
      }

      if (ps.executeBatch().length < 1) {
         log.warn("failed to persist BCL for user [" + username + "]");
      }

      ps.close();
   }

   private Set<String> generateBCL(String username, Connection conn) throws SQLException {
      if (log.isDebugEnabled()) {
         log.debug("generating BCL for user [" + username + "]");
      }

      Set<String> contacts = ContactList.getFusionContactUsernames(contactListMemcache, username);
      PreparedStatement ps;
      ResultSet rs;
      if (contacts == null) {
         if (log.isDebugEnabled()) {
            log.debug("no contact list found in memcached for user [" + username + "], falling back to DB");
         }

         contacts = new HashSet();
         ps = conn.prepareStatement("select fusionUsername from contact where username = ?");
         ps.setString(1, username);
         rs = ps.executeQuery();

         while(rs.next()) {
            ((Set)contacts).add(rs.getString(1));
         }

         rs.close();
         ps.close();
      }

      ps = conn.prepareStatement("select username from contact where fusionUsername = ?");
      ps.setString(1, username);
      rs = ps.executeQuery();
      HashSet contactsContacts = new HashSet();

      while(rs.next()) {
         contactsContacts.add(rs.getString(1));
      }

      ((Set)contacts).retainAll(contactsContacts);
      ps = conn.prepareStatement("select blockusername from blocklist where username = ?");
      ps.setString(1, username);
      rs = ps.executeQuery();

      while(rs.next()) {
         ((Set)contacts).remove(rs.getString(1));
      }

      ps = conn.prepareStatement("select username from blocklist where blockusername = ?");
      ps.setString(1, username);
      rs = ps.executeQuery();

      while(rs.next()) {
         ((Set)contacts).remove(rs.getString(1));
      }

      if (log.isDebugEnabled()) {
         log.debug("found " + ((Set)contacts).size() + " BCL entries for user [" + username + "]");
      }

      return (Set)contacts;
   }

   public ContactData addIMContact(int userID, ContactData contactData, boolean notifyObjectCache) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      UserTransaction userTransaction = null;

      try {
         if (!AuthenticatedAccessControl.hasAccessByUseridLocal(AuthenticatedAccessControlTypeEnum.ADD_FRIEND, userID) && SystemProperty.getBool("AddContactDisabledForUnauthenticatedUsers", false)) {
            throw new EJBException("You must be authenticated before you can add new contacts.");
         }

         conn = this.dataSourceMaster.getConnection();
         if (contactData.contactGroupId != null) {
            this.checkContactGroupOwnership(contactData.username, contactData.contactGroupId, conn);
         }

         if (contactData.displayName == null) {
            contactData.displayName = this.getDefaultDisplayName(conn, contactData);
         }

         contactData.status = ContactData.StatusEnum.ACTIVE;
         userTransaction = this.context.getUserTransaction();
         userTransaction.begin();
         this.persistContact(contactData, conn);
         int newContactListVersion = this.onContactListModified(userID, contactData.username, conn);
         userTransaction.commit();
         if (notifyObjectCache) {
            UserPrx userPrx = EJBIcePrxFinder.findUserPrx(contactData.username);
            if (userPrx != null) {
               userPrx.addContact(contactData.toIceObject(), newContactListVersion);
            }
         }
      } catch (LocalException var30) {
         log.warn("Failed to update User object in ObjectCache, ignore", var30);
      } catch (Exception var31) {
         log.error("Failed to add IM contact. Username [" + contactData.username + "]. IM display name [" + contactData.displayName + "]", var31);

         try {
            if (userTransaction != null) {
               userTransaction.rollback();
            }
         } catch (Exception var29) {
         }

         throw new EJBException(var31.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var28) {
            rs = null;
         }

         try {
            if (ps != null) {
               ((PreparedStatement)ps).close();
            }
         } catch (SQLException var27) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var26) {
            conn = null;
         }

      }

      return contactData;
   }

   public ContactData addPhoneContact(int userID, ContactData contactData) throws EJBException {
      return this.addIMContact(userID, contactData, true);
   }

   public void removeContact(int userID, String username, int contactID) throws EJBException {
      if (contactID < 0) {
         log.info("Looks like an other IM contact... skipping");
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         UserTransaction userTransaction = this.context.getUserTransaction();

         try {
            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("select username, fusionusername from contact where id = ?");
            ps.setInt(1, contactID);
            rs = ps.executeQuery();
            if (rs.next()) {
               if (!username.equalsIgnoreCase(rs.getString("username"))) {
                  throw new Exception("You cannot remove contact that does not belong to you.");
               }

               String fusionUsername = rs.getString("fusionUsername");
               ps.close();
               userTransaction.begin();
               Boolean usernameBCLHasBeenPersisted = isBroadcastListPersisted(username);
               Boolean fusionUsernameBCLHasBeenPersisted = isBroadcastListPersisted(fusionUsername);
               this.removeContact(contactID, conn);
               if (StringUtils.hasLength(fusionUsername)) {
                  this.removeFromPendingContacts(fusionUsername, username, conn);
                  if (usernameBCLHasBeenPersisted) {
                     this.removeFromBroadcastList(username, fusionUsername, conn);
                  }

                  this.removeFromBroadcastListInMemCached(username, fusionUsername);
                  if (fusionUsernameBCLHasBeenPersisted) {
                     this.removeFromBroadcastList(fusionUsername, username, conn);
                  }

                  this.removeFromBroadcastListInMemCached(fusionUsername, username);
               }

               int newContactListVersion = this.onContactListModified(userID, username, conn);
               userTransaction.commit();
               if (StringUtils.hasLength(fusionUsername)) {
                  contactDeletedLog.info(username + " removed " + fusionUsername + "(ID: " + contactID + ")");
               }

               UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);
               if (userPrx != null) {
                  userPrx.removeContact(contactID, newContactListVersion);
               }

               UserPrx contactPrx = EJBIcePrxFinder.findUserPrx(fusionUsername);
               if (contactPrx != null) {
                  contactPrx.stopBroadcastingTo(username);
               }

               return;
            }

            log.warn("contact id [" + contactID + "] was previously deleted from contact [" + username + "] or never was a contact?");
         } catch (LocalException var32) {
            return;
         } catch (Exception var33) {
            log.error("Failed to remove contact", var33);

            try {
               userTransaction.rollback();
            } catch (Exception var31) {
            }

            throw new EJBException(var33.getMessage());
         } finally {
            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var30) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var29) {
               conn = null;
            }

         }

      }
   }

   private void persistUpdatedContact(ContactData contactData, Connection conn) throws Exception {
      PreparedStatement ps = conn.prepareStatement("update contact set displayname=?, firstname=?, lastname=?, fusionusername=?, msnusername=?, aimusername=?, yahoousername=?, icqusername=?, jabberusername=?, emailaddress=?, mobilephone=?, homephone=?, officephone=?, defaultim=?, defaultphonenumber=? , contactgroupid=? where id = ?");

      try {
         ps.setString(1, contactData.displayName);
         ps.setString(2, contactData.firstName);
         ps.setString(3, contactData.lastName);
         ps.setString(4, contactData.fusionUsername);
         ps.setString(5, contactData.msnUsername);
         ps.setString(6, contactData.aimUsername);
         ps.setString(7, contactData.yahooUsername);
         ps.setString(8, contactData.facebookUsername);
         ps.setString(9, contactData.gtalkUsername);
         ps.setString(10, contactData.emailAddress);
         ps.setString(11, contactData.mobilePhone);
         ps.setString(12, contactData.homePhone);
         ps.setString(13, contactData.officePhone);
         ps.setObject(14, contactData.defaultIM == null ? null : contactData.defaultIM.value());
         ps.setObject(15, contactData.defaultPhoneNumber == null ? null : contactData.defaultPhoneNumber.value());
         ps.setObject(16, contactData.contactGroupId != null && contactData.contactGroupId != -1 && contactData.contactGroupId != -2 && contactData.contactGroupId != -3 && contactData.contactGroupId != -4 && contactData.contactGroupId != -5 && contactData.contactGroupId != -6 ? contactData.contactGroupId : null);
         ps.setObject(17, contactData.id);
         if (ps.executeUpdate() != 1) {
            throw new Exception("Failed to update contact detail, does the contact exist?");
         }
      } finally {
         if (ps != null) {
            ps.close();
         }

      }

   }

   public ContactData updateContactDetail(int userID, ContactData contactData) throws EJBException {
      Connection connMaster = null;
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      if (StringUtil.isBlank(contactData.username)) {
         throw new EJBException("Please provide a contact to add.");
      } else {
         UserTransaction userTransaction = this.context.getUserTransaction();

         try {
            connSlave = this.dataSourceSlave.getConnection();
            ps = connSlave.prepareStatement("SELECT contact.id, contact.contactgroupid FROM contact, userid WHERE contact.username = userid.username AND userid.id = ? AND contact.id = ?");
            ps.setInt(1, userID);
            ps.setInt(2, contactData.id);
            rs = ps.executeQuery();
            if (!rs.next()) {
               log.warn("User with ID " + userID + " attempted to update contact with ID " + contactData.id + " that does not belong to them");
               throw new EJBException("Unable to update contact");
            }

            int originalContactGroupID = rs.getInt(2);
            rs.close();
            ps.close();
            if (contactData.contactGroupId != null && contactData.contactGroupId != -1 && contactData.contactGroupId != originalContactGroupID) {
               ps = connSlave.prepareStatement("SELECT userid.id FROM contactgroup, userid WHERE contactgroup.username = userid.username AND contactgroup.id = ?");
               ps.setInt(1, contactData.contactGroupId);
               rs = ps.executeQuery();
               if (!rs.next()) {
                  log.warn("User with ID " + userID + " attempted to change contactgroupid to " + contactData.contactGroupId + " for the contact with ID " + contactData.id + ", but the group does not exist");
                  throw new EJBException("Unable to update contact.");
               }

               int groupOwnerUserID = rs.getInt(1);
               rs.close();
               ps.close();
               if (groupOwnerUserID != userID) {
                  log.warn("User with ID " + userID + " attempted to change contactgroupid to " + contactData.contactGroupId + " for the contact with ID " + contactData.id + ", but they do not own that group");
                  throw new FusionEJBException("Unable to update contact details");
               }
            }

            ContactData existingContactData = this.getContact(contactData.id);
            if (existingContactData == null) {
               throw new Exception("Contact " + contactData.id + " does not exist in database");
            }

            if (null != existingContactData.fusionUsername && !existingContactData.fusionUsername.equalsIgnoreCase(contactData.fusionUsername)) {
               log.warn("User with ID " + userID + "attempted to change contact with username '" + existingContactData.fusionUsername + "' to '" + contactData.fusionUsername + "'");
               throw new Exception("Unable to edit the username of an existing migme contact.");
            }

            String dbFusionUsername = existingContactData.fusionUsername;
            UserPrx userPrx = EJBIcePrxFinder.findUserPrx(contactData.username);
            UserPrx oldContactUserPrx = EJBIcePrxFinder.findUserPrx(dbFusionUsername);
            UserPrx newContactUserPrx = EJBIcePrxFinder.findUserPrx(contactData.fusionUsername);
            if (log.isDebugEnabled()) {
               log.debug("userPrx [" + userPrx + "] oldContactUserPrx [" + oldContactUserPrx + "] newContactUserPrx [" + newContactUserPrx + "]");
            }

            boolean newContactAlreadyOnContactList = this.isOnContactList(contactData.fusionUsername, contactData.username, connSlave, newContactUserPrx, true);
            connSlave.close();
            connSlave = null;
            userTransaction.begin();
            connMaster = this.dataSourceMaster.getConnection();
            if (StringUtils.hasLength(contactData.fusionUsername)) {
               if (this.isOnContactListForIdOtherThan(contactData.username, contactData.fusionUsername, contactData.id, connMaster)) {
                  throw new Exception(contactData.fusionUsername + " is already on the contact list");
               }

               if (dbFusionUsername == null || !contactData.fusionUsername.equalsIgnoreCase(dbFusionUsername)) {
                  this.hasMobilePhoneNumber(contactData.fusionUsername, connMaster);
               }
            }

            Boolean usernameBCLHasBeenPersisted = isBroadcastListPersisted(contactData.username);
            Boolean dbFusionUsernameBCLHasBeenPersisted = isBroadcastListPersisted(dbFusionUsername);
            Boolean fusionUsernameBCLHasBeenPersisted = isBroadcastListPersisted(contactData.fusionUsername);
            this.persistUpdatedContact(contactData, connMaster);
            boolean changedFusionContact = false;
            boolean acceptedContactRequest = false;
            if (contactData.fusionUsername != null && !contactData.fusionUsername.equalsIgnoreCase(dbFusionUsername)) {
               changedFusionContact = true;
               this.removeFromPendingContacts(dbFusionUsername, contactData.username, connMaster);
               if (usernameBCLHasBeenPersisted) {
                  this.removeFromBroadcastList(contactData.username, dbFusionUsername, connMaster);
               }

               this.removeFromBroadcastListInMemCached(contactData.username, dbFusionUsername);
               if (dbFusionUsernameBCLHasBeenPersisted) {
                  this.removeFromBroadcastList(dbFusionUsername, contactData.username, connMaster);
               }

               this.removeFromBroadcastListInMemCached(dbFusionUsername, contactData.username);
               if (newContactAlreadyOnContactList) {
                  this.removeFromPendingContacts(contactData.fusionUsername, contactData.username, connMaster);
                  if (fusionUsernameBCLHasBeenPersisted) {
                     this.persistBroadcastListEntry(contactData.fusionUsername, contactData.username, connMaster);
                  }

                  this.updateBroadcastListEntryInMemCached(contactData.fusionUsername, contactData.username);
                  if (usernameBCLHasBeenPersisted) {
                     this.persistBroadcastListEntry(contactData.username, contactData.fusionUsername, connMaster);
                  }

                  this.updateBroadcastListEntryInMemCached(contactData.username, contactData.fusionUsername);
                  acceptedContactRequest = true;
               } else {
                  this.persistPendingContact(contactData.fusionUsername, contactData.username, connMaster);
               }
            }

            this.assignDisplayPictureAndStatusMessageToContacts(connMaster, Collections.nCopies(1, contactData));
            int newContactListVersion = this.onContactListModified(userID, contactData.username, connMaster);
            userTransaction.commit();
            PresenceAndCapabilityIce presence = null;
            if (userPrx != null) {
               presence = userPrx.contactUpdated(contactData.toIceObject(), dbFusionUsername, acceptedContactRequest, changedFusionContact, newContactUserPrx, newContactListVersion);
            }

            if (changedFusionContact) {
               if (oldContactUserPrx != null) {
                  oldContactUserPrx.oldUserContactUpdated(contactData.username);
               }

               if (newContactUserPrx != null && newContactAlreadyOnContactList) {
                  newContactUserPrx.newUserContactUpdated(contactData.username, acceptedContactRequest);
               }
            }

            contactData.assignPresence(presence);
         } catch (LocalException var46) {
            log.error("failed to update objectcache with updated contact details", var46);
         } catch (Exception var47) {
            log.error("Failed to update contact", var47);

            try {
               userTransaction.rollback();
            } catch (Exception var45) {
            }

            throw new EJBException(var47.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var44) {
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var43) {
            }

            try {
               if (connMaster != null) {
                  connMaster.close();
               }
            } catch (SQLException var42) {
            }

            try {
               if (connSlave != null) {
                  connSlave.close();
               }
            } catch (SQLException var41) {
            }

         }

         return contactData;
      }
   }

   public void unblockContact(String username, String allowUsername, boolean shareMobilePhone) throws EJBException {
      if (log.isDebugEnabled()) {
         log.debug("user [" + username + "] unblocking [" + allowUsername + "]");
      }

      Connection conn = null;
      UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);
      UserPrx allowUserPrx = EJBIcePrxFinder.findUserPrx(allowUsername);
      UserTransaction userTransaction = this.context.getUserTransaction();

      try {
         userTransaction.begin();
         conn = this.dataSourceMaster.getConnection();
         this.removeFromBlockList(username, allowUsername, conn);
         if (shareMobilePhone) {
            this.updateContactsMobilePhone(allowUsername, username, conn);
         }

         userTransaction.commit();
         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.BLOCK_LIST, username);
         if (userPrx != null) {
            userPrx.unblockUser(allowUsername);
         }

         if (allowUserPrx != null && userPrx != null) {
            allowUserPrx.contactChangedPresenceOneWay(ImType.FUSION.value(), username, userPrx.getOverallFusionPresence(allowUsername));
         }
      } catch (LocalException var22) {
         log.error("failed to update userproxy [" + userPrx + "] in objectcache", var22);
      } catch (Exception var23) {
         log.error("Failed to unblock contact", var23);

         try {
            userTransaction.rollback();
         } catch (Exception var21) {
         }

         throw new EJBException(var23.getMessage());
      } finally {
         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }

   }

   public void blockContact(int userID, String username, String blockUsername) throws EJBException {
      UserLocal userEJB;
      try {
         userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userEJB.getUserID(blockUsername, (Connection)null);
      } catch (Exception var28) {
         throw new EJBException("Invalid username specified");
      }

      Connection conn = null;
      UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);
      UserPrx blockUserPrx = EJBIcePrxFinder.findUserPrx(blockUsername);
      UserTransaction userTransaction = this.context.getUserTransaction();
      Boolean usernameBCLHasBeenPersisted = isBroadcastListPersisted(username);
      Boolean blockUsernameBCLHasBeenPersisted = isBroadcastListPersisted(blockUsername);

      try {
         userTransaction.begin();
         conn = this.dataSourceMaster.getConnection();
         boolean removed = this.removeFusionContact(username, blockUsername, conn);
         this.removeFromPendingContacts(username, blockUsername, conn);
         this.persistBlockListEntry(username, blockUsername, conn);
         if (removed) {
            if (usernameBCLHasBeenPersisted) {
               this.removeFromBroadcastList(username, blockUsername, conn);
            }

            this.removeFromBroadcastListInMemCached(username, blockUsername);
         }

         if (blockUsernameBCLHasBeenPersisted) {
            this.removeFromBroadcastList(blockUsername, username, conn);
         }

         this.removeFromBroadcastListInMemCached(blockUsername, username);
         int newContactListVersion = this.onContactListModified(userID, username, conn);
         userTransaction.commit();
         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.BLOCK_LIST, username);
         contactDeletedLog.info(username + " blocked " + blockUsername);
         if (userPrx != null) {
            userPrx.blockUser(blockUsername, newContactListVersion);
         }

         if (blockUserPrx != null) {
            blockUserPrx.stopBroadcastingTo(username);
         }
      } catch (LocalException var25) {
      } catch (Exception var26) {
         log.error("Failed to block contact", var26);

         try {
            userTransaction.rollback();
         } catch (Exception var24) {
         }

         throw new EJBException(var26.getMessage());
      } finally {
         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var23) {
            userEJB = null;
         }

      }

   }

   public void rejectContactRequest(int inviteeUserID, String inviteeUsername, String inviterUsername) throws EJBException {
      if (log.isDebugEnabled()) {
         log.debug("[" + inviteeUsername + "] rejecting contact request from user [" + inviterUsername + "]");
      }

      Connection conn = null;
      UserPrx inviterPrx = EJBIcePrxFinder.findUserPrx(inviterUsername);
      UserPrx inviteePrx = EJBIcePrxFinder.findUserPrx(inviteeUsername);
      UserTransaction userTransaction = this.context.getUserTransaction();
      Boolean usernameBCLHasBeenPersisted = isBroadcastListPersisted(inviteeUsername);
      Boolean inviterBCLHasBeenPersisted = isBroadcastListPersisted(inviterUsername);

      try {
         userTransaction.begin();
         conn = this.dataSourceMaster.getConnection();
         boolean removed = this.removeFusionContact(inviterUsername, inviteeUsername, conn);
         this.removeFromPendingContacts(inviteeUsername, inviterUsername, conn);
         if (removed) {
            if (usernameBCLHasBeenPersisted) {
               this.removeFromBroadcastList(inviteeUsername, inviterUsername, conn);
            }

            this.removeFromBroadcastListInMemCached(inviteeUsername, inviterUsername);
         }

         if (inviterBCLHasBeenPersisted) {
            this.removeFromBroadcastList(inviterUsername, inviteeUsername, conn);
         }

         this.removeFromBroadcastListInMemCached(inviterUsername, inviteeUsername);
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         int inviterUserID = userEJB.getUserID(inviterUsername, conn);
         int newContactListVersion = this.onContactListModified(inviterUserID, inviterUsername, conn);
         userTransaction.commit();
         contactDeletedLog.info(inviteeUsername + " rejected " + inviterUsername);
         this.removeFriendInviteAndNewFollowerNotifications(inviteeUserID, inviterUsername, inviterUserID);
         if (inviterPrx != null) {
            inviterPrx.contactRequestWasRejected(inviteeUsername, newContactListVersion);
         }

         if (inviteePrx != null) {
            inviteePrx.rejectContactRequest(inviterUsername);
         }
      } catch (LocalException var26) {
      } catch (Exception var27) {
         log.error("Failed to block contact", var27);

         try {
            userTransaction.rollback();
         } catch (Exception var25) {
         }

         throw new EJBException(var27.getMessage());
      } finally {
         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var24) {
            conn = null;
         }

      }

   }

   private void updateContactListInMemCached(String username, Set<ContactData> contacts) {
      if (contactListMemcache != null && contacts != null) {
         Calendar now = Calendar.getInstance();
         now.add(6, 5);
         contactListMemcache.set(MemCachedUtils.getCacheKeyInNamespace("CL", username), contacts, now.getTime());
      }

   }

   public int getContactListVersion(int userID, Connection conn) throws EJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var7;
      try {
         Integer version = MemCachedClientWrapper.getInt(MemCachedKeySpaces.CommonKeySpace.CONTACT_LIST_VERSION, String.valueOf(userID));
         if (version == null) {
            ch = new ConnectionHolder(this.dataSourceMaster, conn);
            ps = ch.getConnection().prepareStatement("select version from contactlistversion where userid = ?");
            ps.setInt(1, userID);
            rs = ps.executeQuery();
            version = rs.next() ? rs.getInt("version") : 0;
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CONTACT_LIST_VERSION, String.valueOf(userID), version);
         }

         var7 = version;
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var21) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var19) {
            ch = null;
         }

      }

      return var7;
   }

   public Set<ContactData> getContactList(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      HashSet var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         Set<ContactData> contacts = ContactList.getContactList(contactListMemcache, username);
         if (contacts != null) {
            log.debug("contact list cache HIT for user [" + username + "] contacts [" + contacts.size() + "]");
            this.assignDisplayPictureAndStatusMessageToContacts(conn, contacts);
            Set var27 = contacts;
            return var27;
         }

         log.debug("contact list cache MISS for user [" + username + "] or memcache is disabled");
         ps = conn.prepareStatement("select * from contact where username = ?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         HashSet contacts = new HashSet();

         while(rs.next()) {
            contacts.add(new ContactData(rs));
         }

         if (contacts != null) {
            this.assignDisplayPictureAndStatusMessageToContacts(conn, contacts);
            this.updateContactListInMemCached(username, contacts);
         }

         var6 = contacts;
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
            conn = null;
         }

      }

      return var6;
   }

   public Set<String> getPendingContacts(String username) throws Exception {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Set<String> pendingContacts = new HashSet();
      if (log.isDebugEnabled()) {
         log.debug("Retrieving pending contacts for " + username);
      }

      try {
         conn = this.dataSourceSlave.getConnection();
         ConnectionHolder ch = new ConnectionHolder(this.dataSourceMaster, conn);
         ps = ch.getConnection().prepareStatement("SELECT pendingContact FROM pendingcontact WHERE username = ?");
         ps.setString(1, username);
         rs = ps.executeQuery();

         while(rs.next()) {
            pendingContacts.add(rs.getString("pendingContact"));
         }
      } catch (Exception var15) {
         log.error("Unable to retrieve pending contact list - " + var15.getMessage());
         throw new Exception("Unable to retrieve pending contact list.");
      } finally {
         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var14) {
            conn = null;
         }

      }

      if (log.isDebugEnabled()) {
         log.debug("Returning " + pendingContacts.size() + " pending contacts for " + username);
      }

      return pendingContacts;
   }

   public ContactGroupData addGroup(int userID, ContactGroupData groupData, boolean notifyUserObject) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      UserPrx userPrx = null;
      UserTransaction userTransaction = null;
      if (groupData.name != null && groupData.name.trim().length() != 0) {
         if (notifyUserObject) {
            userPrx = EJBIcePrxFinder.findUserPrx(groupData.username);
         }

         try {
            if (!AuthenticatedAccessControl.hasAccessByUsernameLocal(AuthenticatedAccessControlTypeEnum.ADD_CONTACT_GROUP, groupData.username) && SystemProperty.getBool("AddContactDisabledForUnauthenticatedUsers", false)) {
               throw new EJBException("You must be authenticated before you can create a contact group.");
            }

            conn = this.dataSourceMaster.getConnection();
            groupData.name = groupData.name.trim();
            ps = conn.prepareStatement("select id from contactgroup where username=? and name=?");
            ps.setString(1, groupData.username);
            ps.setString(2, groupData.name);
            rs = ps.executeQuery();
            if (rs.next()) {
               throw new EJBException("Group " + groupData.name + " already exist");
            }

            rs.close();
            ps.close();
            userTransaction = this.context.getUserTransaction();
            userTransaction.begin();
            ps = conn.prepareStatement("insert into contactgroup (username, name) values (?,?)", 1);
            ps.setString(1, groupData.username);
            ps.setString(2, groupData.name);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (!rs.next()) {
               throw new EJBException("Failed to add a new contact group to database");
            }

            groupData.id = rs.getInt(1);
            int newContactListVersion = this.updateContactListVersion(userID, conn);
            MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.CONTACT_GROUP, groupData.username);
            userTransaction.commit();
            if (notifyUserObject && userPrx != null) {
               userPrx.contactGroupDetailChanged(groupData.toIceObject(), newContactListVersion);
            }
         } catch (LocalException var31) {
         } catch (Exception var32) {
            try {
               if (userTransaction != null) {
                  userTransaction.rollback();
               }
            } catch (Exception var30) {
            }

            throw new EJBException(var32.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var29) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var28) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var27) {
               conn = null;
            }

         }

         return groupData;
      } else {
         throw new EJBException("A group name was not specified");
      }
   }

   public void removeGroup(int userID, String username, int groupID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);
      UserTransaction userTransaction = this.context.getUserTransaction();

      try {
         userTransaction.begin();
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update contact set contactgroupid = null where contactgroupid = ?");
         ps.setInt(1, groupID);
         if (ps.executeUpdate() > 0) {
            throw new Exception("The group is not empty (it may contain hidden contacts).");
         }

         ps.close();
         ps = conn.prepareStatement("delete from contactgroup where id = ? and username = ?");
         ps.setInt(1, groupID);
         ps.setString(2, username);
         if (ps.executeUpdate() != 1) {
            throw new Exception("Failed to remove group ID " + groupID + " for " + username);
         }

         ps.close();
         int newContactListVersion = this.updateContactListVersion(userID, conn);
         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.CONTACT_GROUP, username);
         userTransaction.commit();
         if (userPrx != null) {
            userPrx.contactGroupDeleted(groupID, newContactListVersion);
         }
      } catch (LocalException var26) {
      } catch (Exception var27) {
         try {
            if (userTransaction != null) {
               userTransaction.rollback();
            }
         } catch (Exception var25) {
         }

         throw new EJBException(var27.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var23) {
            conn = null;
         }

      }

   }

   public void updateGroupDetail(int userID, ContactGroupData groupData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      if (groupData.name != null && groupData.name.trim().length() != 0) {
         UserPrx userPrx = EJBIcePrxFinder.findUserPrx(groupData.username);
         UserTransaction userTransaction = this.context.getUserTransaction();

         try {
            groupData.name = groupData.name.trim();
            userTransaction.begin();
            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("select id from contactgroup where username=? and name=? and id<>?");
            ps.setString(1, groupData.username);
            ps.setString(2, groupData.name);
            ps.setObject(3, groupData.id);
            rs = ps.executeQuery();
            if (rs.next()) {
               throw new EJBException("Group " + groupData.name + " already exist");
            }

            rs.close();
            ps.close();
            ps = conn.prepareStatement("update contactgroup set name = ? where id = ? and username = ?");
            ps.setString(1, groupData.name);
            ps.setObject(2, groupData.id);
            ps.setString(3, groupData.username);
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated != 1) {
               throw new EJBException("Failed to update group detail");
            }

            int newContactListVersion = this.updateContactListVersion(userID, conn);
            MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.CONTACT_GROUP, groupData.username);
            userTransaction.commit();
            if (userPrx != null) {
               userPrx.contactGroupDetailChanged(groupData.toIceObject(), newContactListVersion);
            }
         } catch (LocalException var30) {
         } catch (Exception var31) {
            try {
               if (userTransaction != null) {
                  userTransaction.rollback();
               }
            } catch (Exception var29) {
            }

            throw new EJBException(var31.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var28) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var27) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var26) {
               conn = null;
            }

         }

      } else {
         throw new EJBException("A group name was not specified");
      }
   }

   public List<ContactGroupData> getGroupList(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Object var6;
      try {
         List<ContactGroupData> groupList = (List)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CONTACT_GROUP, username);
         if (groupList == null) {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("select * from contactgroup where username = ?");
            ps.setString(1, username);
            rs = ps.executeQuery();
            groupList = new LinkedList();

            while(rs.next()) {
               ((List)groupList).add(new ContactGroupData(rs));
            }

            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CONTACT_GROUP, username, groupList);
         }

         var6 = groupList;
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return (List)var6;
   }

   public void moveContactToGroup(int userID, String username, int contactID, Integer groupID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      UserTransaction userTransaction = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ContactData contact = this.getContact(contactID);
         if (contact == null) {
            throw new EJBException("Invalid contact ID " + contactID);
         }

         if (!contact.username.equalsIgnoreCase(username)) {
            throw new EJBException("Contact ID " + contactID + " does not belong to " + username);
         }

         UserPrx userPrx = EJBIcePrxFinder.findUserPrx(contact.username);
         userTransaction = this.context.getUserTransaction();
         userTransaction.begin();
         contact.contactGroupId = groupID;
         if (groupID != null && groupID != -1 && groupID != -2 && groupID != -3 && groupID != -4 && groupID != -5 && groupID != -6) {
            ps = conn.prepareStatement("update contact set contactgroupid = ? where exists (select * from contactgroup where id = ? and username = contact.username) and id = ?");
            ps.setInt(1, groupID);
            ps.setInt(2, groupID);
            ps.setInt(3, contactID);
         } else {
            ps = conn.prepareStatement("update contact set contactgroupid = null where id = ?");
            ps.setInt(1, contactID);
         }

         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed to add contact " + contactID + " to group " + groupID);
         }

         int newContactListVersion = this.onContactListModified(userID, username, conn);
         userTransaction.commit();
         if (userPrx != null) {
            userPrx.contactDetailChanged(contact.toIceObject(), newContactListVersion);
         }
      } catch (LocalException var27) {
      } catch (Exception var28) {
         try {
            if (userTransaction != null) {
               userTransaction.rollback();
            }
         } catch (Exception var26) {
         }

         throw new EJBException(var28.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var25) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var24) {
            conn = null;
         }

      }

   }

   private int onContactListModified(int userID, String username, Connection conn) throws SQLException {
      int newContactListVersion = this.updateContactListVersion(userID, conn);
      if (contactListMemcache != null) {
         contactListMemcache.delete(MemCachedUtils.getCacheKeyInNamespace("CL", username));
      }

      return newContactListVersion;
   }

   private int removeFromStringList(String username, String qualifyingUsername, String tableName, String fieldName, Connection conn) throws SQLException {
      PreparedStatement ps = conn.prepareStatement("delete from " + tableName + " where username = ? and " + fieldName + " = ?");
      boolean var7 = false;

      int rowCount;
      try {
         ps.setString(1, username);
         ps.setString(2, qualifyingUsername);
         rowCount = ps.executeUpdate();
      } finally {
         ps.close();
      }

      return rowCount;
   }

   private boolean removeFromBlockList(String username, String blockedUsername, Connection conn) throws SQLException {
      return this.removeFromStringList(username, blockedUsername, "blocklist", "blockUsername", conn) > 0;
   }

   private boolean removeFromPendingContacts(String username, String pendingContact, Connection conn) throws SQLException, CreateException {
      if (!StringUtil.isBlank(pendingContact) && !StringUtil.isBlank(username)) {
         boolean ret = this.removeFromStringList(username, pendingContact, "pendingcontact", "pendingContact", conn) > 0;

         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            int userId = userEJB.getUserID(username, conn);
            int pendingContactUserID = userEJB.getUserID(pendingContact, conn);
            this.removeFriendInviteAndNewFollowerNotifications(userId, pendingContact, pendingContactUserID);
         } catch (Exception var8) {
            log.warn(String.format("Unable to remove notifications for user[%s] pendingcontact[%s] : %s", username, pendingContact, var8.getMessage()));
         }

         return ret;
      } else {
         log.warn(String.format("Unable to remove pending contacts. Both user and pendingcontact must not be null. user[%s] pendingcontact[%s]", username, pendingContact));
         return false;
      }
   }

   private boolean removeFromBroadcastList(String username, String broadcastUsername, Connection conn) throws SQLException {
      boolean persisted = this.removeFromStringList(username, broadcastUsername, "broadcastlist", "broadcastUsername", conn) > 0;
      return persisted;
   }

   private boolean removeFromBroadcastListInMemCached(String username, String broadcastUsername) {
      if (log.isDebugEnabled()) {
         log.debug("removing contact [" + broadcastUsername + "] from the broadcast list for user [" + username + "] in memcached");
      }

      boolean removed = false;

      try {
         if (MemCachedUtils.getLock(broadcastListMemcache, "BLDL", username, 15000)) {
            Set<String> bcl = BroadcastList.getBroadcastList(broadcastListMemcache, username);
            if (bcl != null && bcl.remove(broadcastUsername)) {
               removed = BroadcastList.setBroadcastList(broadcastListMemcache, username, bcl);
            }
         } else {
            log.error("Failed to get a lock to update user [" + username + "]'s BCL in 10 seconds");
            BroadcastList.deleteBroadcastList(broadcastListMemcache, username);
         }
      } finally {
         MemCachedUtils.releaseLock(broadcastListMemcache, "BLDL", username);
      }

      return removed;
   }

   private int persistStringListEntry(String username, String qualifyingUsername, String tableName, String fieldName, Connection conn) throws SQLException {
      PreparedStatement ps = conn.prepareStatement("select * from " + tableName + " where username = ? and " + fieldName + " = ?");
      ps.setString(1, username);
      ps.setString(2, qualifyingUsername);
      ResultSet rs = ps.executeQuery();
      if (!rs.next()) {
         ps.close();
         ps = conn.prepareStatement("insert into " + tableName + " (username, " + fieldName + ") values (?,?)");
         ps.setString(1, username);
         ps.setString(2, qualifyingUsername);
         int result = ps.executeUpdate();
         return result;
      } else {
         ps.close();
         return 0;
      }
   }

   private boolean persistBroadcastListEntry(String username, String broadcastUsername, Connection conn) throws SQLException {
      if (log.isDebugEnabled()) {
         log.debug("persisting broadcast entry for user [" + username + "] and broadcastUsername [" + broadcastUsername + "]");
      }

      boolean persisted = this.persistStringListEntry(username, broadcastUsername, "broadcastlist", "broadcastUsername", conn) > 0;
      return persisted;
   }

   private boolean updateBroadcastListEntryInMemCached(String username, String broadcastUsername) {
      if (log.isDebugEnabled()) {
         log.debug("adding contact [" + broadcastUsername + "] to the broadcast list for user [" + username + "] in memcached");
      }

      boolean added = false;

      try {
         if (MemCachedUtils.getLock(broadcastListMemcache, "BLDL", username, 15000)) {
            Set<String> bcl = BroadcastList.getBroadcastList(broadcastListMemcache, username);
            if (bcl != null) {
               bcl.add(broadcastUsername);
               added = BroadcastList.setBroadcastList(broadcastListMemcache, username, bcl);
            }
         } else {
            log.error("Failed to get a lock to update user [" + username + "]'s BCL in 10 seconds");
            BroadcastList.deleteBroadcastList(broadcastListMemcache, username);
         }
      } finally {
         MemCachedUtils.releaseLock(broadcastListMemcache, "BLDL", username);
      }

      return added;
   }

   private boolean persistBlockListEntry(String username, String blockedUsername, Connection conn) throws SQLException {
      return this.persistStringListEntry(username, blockedUsername, "blocklist", "blockusername", conn) > 0;
   }

   private boolean removeContact(int contactId, Connection conn) throws SQLException {
      PreparedStatement ps = conn.prepareStatement("delete from contact where id = ?");
      ps.setInt(1, contactId);
      return ps.executeUpdate() >= 1;
   }

   private boolean removeFusionContact(String username, String contactUsername, Connection conn) throws SQLException {
      PreparedStatement psGetIDs = null;
      PreparedStatement psDeleteRow = null;
      ResultSet rs = null;
      boolean rowsRemoved = false;
      Connection slaveConn = null;

      try {
         slaveConn = this.dataSourceSlave.getConnection();
         psGetIDs = slaveConn.prepareStatement("select id from contact where username = ? and fusionUsername = ?");
         psGetIDs.setString(1, username);
         psGetIDs.setString(2, contactUsername);
         rs = psGetIDs.executeQuery();
         psDeleteRow = conn.prepareStatement("delete from contact where id = ?");

         while(rs.next()) {
            psDeleteRow.setInt(1, rs.getInt(1));
            if (psDeleteRow.executeUpdate() >= 1) {
               rowsRemoved = true;
            }
         }

         boolean var9 = rowsRemoved;
         return var9;
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var25) {
         }

         try {
            if (psGetIDs != null) {
               psGetIDs.close();
            }
         } catch (SQLException var24) {
         }

         try {
            if (psDeleteRow != null) {
               psDeleteRow.close();
            }
         } catch (SQLException var23) {
         }

         try {
            if (slaveConn != null) {
               slaveConn.close();
            }
         } catch (SQLException var22) {
         }

      }
   }

   private static Boolean isBroadcastListPersisted(String username) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.BCLPERSISTED_ENABLED)) {
         Boolean persisted = bclPersistedMemcache.keyExists(username);
         if (!persisted) {
            log.info("found unpersisted bcl for user : " + username);
         }

         return persisted;
      } else {
         return true;
      }
   }

   static {
      contactListMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.contactList);
      broadcastListMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.broadcastList);
      bclPersistedMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.bclPersisted);
      displayPictureAndStatusMessageMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.userDisplayPictureAndStatus);
      checkAndPopulateBCL = true;
   }
}
