package com.projectgoth.fusion.ejb;

import Ice.LocalException;
import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.cache.ChatRoomSearch;
import com.projectgoth.fusion.cache.RecentChatRoomList;
import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.ChatRoomValidationException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.BotData;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.ChatroomCategoryData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.EmailTemplateData;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.GroupMemberData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.data.MobilePrefixData;
import com.projectgoth.fusion.data.PremiumSMSPaymentData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.SMSGatewayData;
import com.projectgoth.fusion.data.SMSRouteData;
import com.projectgoth.fusion.data.SystemSMSData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserEmailAddressData;
import com.projectgoth.fusion.ejb.util.LookupUtil;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.messagelogger.MessageToLog;
import com.projectgoth.fusion.rewardsystem.RewardCentre;
import com.projectgoth.fusion.rewardsystem.triggers.EmailSentTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.SMSSentTrigger;
import com.projectgoth.fusion.search.ChatRoomsIndex;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.EmailUserNotification;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.smsengine.SMSControl;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class MessageBean implements SessionBean {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MessageBean.class));
   private DataSource dataSourceMaster;
   private DataSource dataSourceSlave;
   private SecureRandom secureRandom;
   private Map<Integer, Double> iddCodes;
   private Vector<Integer> iddCodesAllowZero;
   private AccountLocalHome accountLocalHome;
   private String adultWordFilter;
   private static MemCachedClient recentChatRoomMemcache;
   private static MemCachedClient chatRoomSearchMemcache;
   private SessionContext context;

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
         InitialContext ctx = new InitialContext();
         this.dataSourceMaster = LookupUtil.getFusionMasterDataSource();
         this.dataSourceSlave = LookupUtil.getFusionSlaveDataSource();
         this.accountLocalHome = (AccountLocalHome)ctx.lookup("AccountLocal");
         ctx.close();
         this.secureRandom = new SecureRandom();
         SystemProperty.ejbInit(this.dataSourceSlave);
      } catch (Exception var2) {
         log.error("Unable to create Message EJB", var2);
         throw new CreateException("Unable to create Message EJB: " + var2.getMessage());
      }
   }

   private Vector<Integer> getIDDCodesAllowZeroVector() throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      if (this.iddCodesAllowZero == null || this.iddCodesAllowZero.isEmpty()) {
         this.iddCodesAllowZero = new Vector();

         try {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("select iddcode from country where allowzeroafteriddcode = 1");
            rs = ps.executeQuery();

            while(rs.next()) {
               this.iddCodesAllowZero.add(rs.getInt(1));
            }
         } catch (SQLException var19) {
            throw new EJBException("Unable to check db for countries where zero is allowed after iddcode: " + var19.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var18) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var17) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var16) {
               conn = null;
            }

         }
      }

      return this.iddCodesAllowZero;
   }

   private Map<Integer, Double> getIDDCodesHashMap() throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      if (this.iddCodes == null || this.iddCodes.isEmpty()) {
         this.iddCodes = new HashMap();

         try {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("select IDDCode, SMSCost from country");
            rs = ps.executeQuery();

            while(rs.next()) {
               this.iddCodes.put(rs.getInt("IDDCode"), rs.getDouble("SMSCost"));
            }

            if (this.iddCodes.size() == 0) {
               throw new EJBException("Unable to load IDD and SMS cost details. No records found");
            }
         } catch (SQLException var19) {
            throw new EJBException("Unable to load IDD and SMS cost details: " + var19.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var18) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var17) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var16) {
               conn = null;
            }

         }
      }

      return this.iddCodes;
   }

   private String getAdultWordFilter() throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      if (StringUtil.isBlank(this.adultWordFilter)) {
         StringBuilder builder = new StringBuilder();

         try {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("select * from adultword");
            rs = ps.executeQuery();
            if (rs.next()) {
               builder.append(".*(").append(rs.getString("word"));

               do {
                  builder.append("|").append(rs.getString("word"));
               } while(rs.next());

               builder.append(").*");
            }

            this.adultWordFilter = builder.toString();
         } catch (SQLException var20) {
            throw new EJBException("Unable to load adult words: " + var20.getMessage());
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

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var17) {
               conn = null;
            }

         }
      }

      return this.adultWordFilter;
   }

   private Double getFixedSMSCost(int countryID) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Double cost = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select fixedsmscost.Cost/currency.ExchangeRate Cost from fixedsmscost, currency where fixedsmscost.countryid=? and fixedsmscost.currency=currency.code");
         ps.setInt(1, countryID);
         rs = ps.executeQuery();
         if (rs.next()) {
            cost = rs.getDouble("Cost");
         }
      } catch (SQLException var24) {
         System.err.println("Unable to check whether there is a fixed SMS cost. Details: " + var24.getMessage());
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

      return cost;
   }

   private double getWholesaleSMSCost(Connection conn, int smsGatewayID, int iddCode, String mobilePhone) throws SQLException {
      PreparedStatement ps = null;
      ResultSet rs = null;

      double var7;
      try {
         ps = conn.prepareStatement("select smswholesalecost.cost / currency.exchangerate cost from smswholesalecost, currency where smswholesalecost.currency = currency.code and smswholesalecost.gatewayid = ? and smswholesalecost.iddcode = ? and (smswholesalecost.areacode = '' or ? like concat(smswholesalecost.iddcode, smswholesalecost.areacode, '%')) order by smswholesalecost.areacode desc");
         ps.setInt(1, smsGatewayID);
         ps.setInt(2, iddCode);
         ps.setString(3, mobilePhone);
         rs = ps.executeQuery();
         if (rs.next()) {
            var7 = rs.getDouble("cost");
            return var7;
         }

         var7 = 0.0D;
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

      return var7;
   }

   public MessageData saveSentMessage(MessageData messageData) throws EJBException {
      if (messageData.messageDestinations == null) {
         throw new EJBException("The message must have at least one destination");
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("insert into message (username, datecreated, type, messagetext, sendreceive, sourcecontactid, source) values (?, ?, ?, ?, ?, ?, ?)", 1);
            ps.setString(1, messageData.username);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setObject(3, messageData.type == null ? null : new Integer(messageData.type.value()));
            ps.setString(4, messageData.messageText);
            ps.setObject(5, MessageData.SendReceiveEnum.SEND.value());
            ps.setObject(6, messageData.sourceContactID);
            ps.setString(7, messageData.source);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (!rs.next()) {
               throw new EJBException("Unable to obtain the ID of the inserted message");
            }

            messageData.id = rs.getInt(1);
            rs.close();
            ps.close();
            ps = conn.prepareStatement("insert into messagedestination (messageid, contactid, type, destination, iddcode, cost, gateway, datedispatched, status) values (?, ?, ?, ?, ?, ?, ?, ?, ?)", 1);
            Iterator i$ = messageData.messageDestinations.iterator();

            while(i$.hasNext()) {
               MessageDestinationData messageDestData = (MessageDestinationData)i$.next();
               if (messageData.type == MessageType.FUSION || messageData.type == MessageType.MSN || messageData.type == MessageType.YAHOO || messageData.type == MessageType.AIM) {
                  if (messageDestData.cost == null) {
                     messageDestData.cost = 0.0D;
                  }

                  if (messageData.type == MessageType.FUSION && messageDestData.status == null) {
                     messageDestData.status = MessageDestinationData.StatusEnum.PENDING;
                  }
               }

               messageDestData.contactID = null;
               ps.setInt(1, messageData.id);
               ps.setObject(2, messageDestData.contactID);
               ps.setObject(3, messageDestData.type == null ? null : new Integer(messageDestData.type.value()));
               ps.setString(4, messageDestData.destination);
               ps.setObject(5, messageDestData.IDDCode);
               ps.setObject(6, messageDestData.cost);
               ps.setObject(7, messageDestData.gateway);
               ps.setTimestamp(8, messageDestData.dateDispatched == null ? null : new Timestamp(messageDestData.dateDispatched.getTime()));
               ps.setObject(9, messageDestData.status == null ? null : messageDestData.status.value());
               ps.executeUpdate();
               rs = ps.getGeneratedKeys();
               if (!rs.next()) {
                  throw new EJBException("Unable to obtain the ID of an inserted message destination");
               }

               messageDestData.id = rs.getInt(1);
               rs.close();
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

         return messageData;
      }
   }

   public MessageData saveReceivedMessage(MessageData messageData, Integer sentMessageDestinationID) throws EJBException {
      if (messageData.messageDestinations != null) {
         throw new EJBException("The received message must not have a MessageDestination attached");
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         Object rs = null;

         try {
            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("insert into message (username, datecreated, type, messagetext, sendreceive, sourcecontactid, source) values (?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, messageData.username);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setObject(3, messageData.type == null ? null : new Integer(messageData.type.value()));
            ps.setString(4, messageData.messageText);
            ps.setObject(5, MessageData.SendReceiveEnum.RECEIVE.value());
            ps.setObject(6, messageData.sourceContactID);
            ps.setString(7, messageData.source);
            ps.executeUpdate();
            if (sentMessageDestinationID != null) {
               this.changePendingMessageToSent(conn, ps, messageData.type, messageData.id, sentMessageDestinationID, (Integer)null, (Integer)null, (String)null, (String)null);
            }
         } catch (CreateException var22) {
            throw new EJBException(var22.getMessage());
         } catch (SQLException var23) {
            throw new EJBException(var23.getMessage());
         } finally {
            try {
               if (rs != null) {
                  ((ResultSet)rs).close();
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
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var19) {
               conn = null;
            }

         }

         return messageData;
      }
   }

   public MessageData sendSMS(MessageData messageData, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      if (messageData.messageDestinations == null) {
         throw new EJBException("The message must have at least one destination");
      } else if (messageData.type != MessageType.SMS) {
         throw new EJBException("The message type must be SMS");
      } else {
         short maxLen;
         if (messageData.messageText.matches("^[\\x00-\\xFF]*$")) {
            maxLen = 160;
            if (messageData.messageText.length() > maxLen) {
               throw new EJBException("The message exceeded " + maxLen + " character limit");
            }
         } else {
            maxLen = 69;
            if (messageData.messageText.length() > maxLen) {
               throw new EJBException("Unicode message must not contain more than " + maxLen + " characters");
            }
         }

         UserData userData = null;
         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.PT73368964_ENABLED)) {
            try {
               UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               userData = userBean.loadUser(messageData.username, false, false);
               if (messageData.messageText.toLowerCase().contains(userData.password.toLowerCase())) {
                  MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
                  throw new EJBException(misBean.getInfoText(22));
               }
            } catch (CreateException var14) {
               throw new EJBException("Unable to perform password check (CreateException): " + var14.getMessage());
            }
         }

         messageData.source = userData.mobilePhone;
         String smsBrand = SystemProperty.get("SMSBrand", "");
         if (messageData.messageText.length() + smsBrand.length() < maxLen) {
            messageData.messageText = messageData.messageText + "\n" + smsBrand;
         }

         double totalBillingAmount = 0.0D;
         List<String> blockedSMSDestinations = Arrays.asList(SystemProperty.getArray("BlockedSMSDestinations", new String[0]));

         Iterator i$;
         MessageDestinationData messageDestData;
         for(i$ = messageData.messageDestinations.iterator(); i$.hasNext(); totalBillingAmount += messageDestData.cost) {
            messageDestData = (MessageDestinationData)i$.next();
            if (blockedSMSDestinations.contains(messageDestData.destination)) {
               throw new EJBException("Unable to send SMS to " + messageDestData.destination);
            }

            messageDestData.status = MessageDestinationData.StatusEnum.PENDING;
            messageDestData = this.assignIDDAndCost(messageDestData, userData.countryID);
         }

         i$ = null;

         AccountLocal accountBean;
         try {
            accountBean = this.accountLocalHome.create();
         } catch (CreateException var13) {
            throw new EJBException("Unable to charge user (CreateException): " + var13.getMessage());
         }

         if (totalBillingAmount != 0.0D) {
            double balance = accountBean.getAccountBalance(messageData.username).getBaseBalance();
            if (balance < totalBillingAmount) {
               throw new EJBException("You do not have enough credit. Please recharge your account");
            }
         }

         messageData = this.saveSentMessage(messageData);
         String accountEntryDescription = "SMS sent to ";
         if (messageData.messageDestinations.size() == 1) {
            accountEntryDescription = accountEntryDescription + ((MessageDestinationData)messageData.messageDestinations.get(0)).destination;
         } else {
            accountEntryDescription = accountEntryDescription + messageData.messageDestinations.size() + " recipients";
         }

         accountBean.chargeUserForSMS(messageData.username, messageData.id.toString(), accountEntryDescription, totalBillingAmount, accountEntrySourceData);
         this.sendSMSToSMSSender(messageData);
         this.sendSMSToMessageLogger(messageData, userData);

         try {
            SMSSentTrigger trigger = new SMSSentTrigger(userData);
            trigger.amountDelta = totalBillingAmount;
            trigger.currency = CurrencyData.baseCurrency;
            trigger.quantityDelta = 1;
            RewardCentre.getInstance().sendTrigger(trigger);
         } catch (Exception var12) {
            log.warn("Unable to notify reward system", var12);
         }

         return messageData;
      }
   }

   private void sendSMSToSMSSender(MessageData messageData) {
      try {
         EJBIcePrxFinder.getSMSSender().sendSMS(messageData.toIceObject(), 0L);
      } catch (FusionException var3) {
         System.err.println("Unable to send SMS to the SMS sending application: " + var3.message);
      } catch (Exception var4) {
         System.err.println("Unable to send SMS to the SMS sending application: " + var4.getMessage());
      }

   }

   private void sendSMSToMessageLogger(MessageData messageData, UserData userData) {
      try {
         if (!EJBIcePrxFinder.logMessagesToFile()) {
            return;
         }

         EJBIcePrxFinder.getOnewayMessageLoggerPrx().logMessage(MessageToLog.TypeEnum.SMS.value(), userData.countryID, messageData.username, ((MessageDestinationData)messageData.messageDestinations.get(0)).destination, 1, messageData.messageText);
      } catch (Exception var4) {
         System.err.println("Unable to send SMS to the MessageLogger application: " + var4.getMessage());
      }

   }

   private void sendSystemSMSToSMSSender(SystemSMSData systemSMSData, long delay) {
      try {
         EJBIcePrxFinder.getSMSSender().sendSystemSMS(systemSMSData.toIceObject(), delay);
      } catch (FusionException var5) {
         System.err.println("Unable to send system SMS to the SMS sending application: " + var5.message);
      } catch (Exception var6) {
         System.err.println("Unable to send system SMS to the SMS sending application: " + var6.getMessage());
      }

   }

   public void sendSystemSMS(SystemSMSData systemSMSData, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      this.sendSystemSMS(systemSMSData, 0L, accountEntrySourceData);
   }

   public void sendSystemSMSNoTransaction(SystemSMSData systemSMSData, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      this.sendSystemSMS(systemSMSData, 0L, accountEntrySourceData);
   }

   public void sendSystemSMS(SystemSMSData systemSMSData, long delay, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SmsSettings.SMS_ENGINE_ENABLED)) {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SmsSettings.LOG_REFUSED_TO_SEND)) {
            log.info("sendSystemSMS: Not sending SMS : SMSEngine disabled");
         }

      } else if (SMSControl.isSendEnabledForSubtype(systemSMSData.subType)) {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            List<String> blockedSMSDestinations = Arrays.asList(SystemProperty.getArray("BlockedSMSDestinations", new String[0]));
            if (blockedSMSDestinations.contains(systemSMSData.destination)) {
               throw new EJBException("Unable to send SMS to " + systemSMSData.destination);
            }

            systemSMSData.destination = this.cleanAndValidatePhoneNumber(systemSMSData.destination, true);
            systemSMSData.dateCreated = new Date();
            systemSMSData.IDDCode = this.getIDDCode(systemSMSData.destination);
            systemSMSData.status = SystemSMSData.StatusEnum.PENDING;
            double accountEntryBillingAmount;
            String accountEntryDescription;
            switch(systemSMSData.subType) {
            case EMAIL_ALERT:
               accountEntryBillingAmount = systemSMSData.cost;
               accountEntryDescription = "Email Alert SMS sent to " + systemSMSData.destination;
               break;
            case BUZZ:
               accountEntryBillingAmount = systemSMSData.cost;
               accountEntryDescription = "Buzz SMS sent";
               break;
            case LOOKOUT:
               accountEntryBillingAmount = systemSMSData.cost;
               accountEntryDescription = "Lookout SMS sent to " + systemSMSData.destination;
               break;
            case GROUP_ANNOUNCEMENT_NOTIFICATION:
               accountEntryBillingAmount = SystemProperty.getDouble("GroupSMSNotificationCost");
               accountEntryDescription = "Group announcement SMS sent to " + systemSMSData.destination;
               break;
            case GROUP_EVENT_NOTIFICATION:
               accountEntryBillingAmount = SystemProperty.getDouble("GroupSMSNotificationCost");
               accountEntryDescription = "Group event SMS sent to " + systemSMSData.destination;
               break;
            default:
               accountEntryBillingAmount = 0.0D;
               accountEntryDescription = "System SMS sent to " + systemSMSData.destination;
            }

            AccountLocal accountBean = this.accountLocalHome.create();
            if (accountEntryBillingAmount > 0.0D) {
               double balance = accountBean.getAccountBalance(systemSMSData.username).getBaseBalance();
               if (balance < accountEntryBillingAmount) {
                  throw new EJBException("Insufficient credit");
               }
            }

            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("insert into systemsms (username, datecreated, type, subtype, source, destination, iddcode, messagetext, status) values (?, ?, ?, ?, ?, ?, ?, ?, ?)", 1);
            ps.setString(1, systemSMSData.username);
            ps.setTimestamp(2, new Timestamp(systemSMSData.dateCreated.getTime()));
            ps.setObject(3, systemSMSData.type == null ? null : new Integer(systemSMSData.type.value()));
            ps.setObject(4, systemSMSData.subType == null ? null : new Integer(systemSMSData.subType.value()));
            ps.setString(5, systemSMSData.source);
            ps.setString(6, systemSMSData.destination);
            ps.setObject(7, systemSMSData.IDDCode);
            ps.setString(8, systemSMSData.messageText);
            ps.setInt(9, systemSMSData.status.value());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (!rs.next()) {
               throw new EJBException("Failed to create system SMS entry");
            }

            systemSMSData.id = rs.getInt(1);
            if (systemSMSData.username != null) {
               accountBean.chargeUserForSystemSMS(systemSMSData.username, systemSMSData.id.toString(), accountEntryDescription, accountEntryBillingAmount, accountEntrySourceData);
            }

            this.sendSystemSMSToSMSSender(systemSMSData, delay);
         } catch (CreateException var31) {
            throw new EJBException(var31.getMessage());
         } catch (SQLException var32) {
            throw new EJBException(var32.getMessage());
         } catch (NoSuchFieldException var33) {
            throw new EJBException(var33.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var30) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var29) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var28) {
               conn = null;
            }

         }

      }
   }

   public void systemSMSFailed(int id, Integer gateway, String source) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update systemsms set Status = ?, Gateway = ?, Source = ? where ID = ?");
         ps.setInt(1, SystemSMSData.StatusEnum.FAILED.value());
         ps.setObject(2, gateway);
         ps.setString(3, source);
         ps.setInt(4, id);
         ps.executeUpdate();
         ps.close();
         ps = conn.prepareStatement("update premiumsmspayment set Status = ? where SystemSMSID = ?");
         ps.setInt(1, PremiumSMSPaymentData.StatusEnum.FAILED.value());
         ps.setInt(2, id);
         ps.executeUpdate();
      } catch (SQLException var18) {
         throw new EJBException(var18.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var17) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var16) {
            conn = null;
         }

      }

   }

   public void smsFailed(int id, Integer gateway, String username, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select destination, cost from messagedestination where ID = ?");
         ps.setInt(1, id);
         rs = ps.executeQuery();
         if (rs.next()) {
            String destination = rs.getString("destination");
            rs.close();
            ps.close();
            ps = conn.prepareStatement("update messagedestination set Status = ?, Gateway = ? where ID = ?");
            ps.setInt(1, MessageDestinationData.StatusEnum.FAILED.value());
            ps.setObject(2, gateway);
            ps.setInt(3, id);
            ps.executeUpdate();
            AccountLocal accountBean = this.accountLocalHome.create();
            accountBean.refundUserForSMS(id, String.valueOf(id), "Failed to deliver SMS to " + destination, accountEntrySourceData);
         }
      } catch (SQLException var25) {
         throw new EJBException(var25.getMessage());
      } catch (CreateException var26) {
         throw new EJBException("Unable to refund user (CreateException): " + var26.getMessage());
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

   }

   private MessageDestinationData assignIDDAndCost(MessageDestinationData messageDestData, int countryID) throws EJBException {
      if (messageDestData.destination != null && messageDestData.destination.length() != 0) {
         messageDestData.destination = this.cleanAndValidatePhoneNumber(messageDestData.destination, true);
         messageDestData.IDDCode = this.getIDDCode(messageDestData.destination);
         if (messageDestData.cost == null) {
            Double fixedCost = this.getFixedSMSCost(countryID);
            if (fixedCost != null) {
               messageDestData.cost = fixedCost;
            } else {
               messageDestData.cost = (Double)this.getIDDCodesHashMap().get(messageDestData.IDDCode);
            }
         }

         return messageDestData;
      } else {
         throw new EJBException("No destination mobile phone number was specified");
      }
   }

   public int getSystemSMSCount(SystemSMSData.SubTypeEnum subType, String username) throws EJBException {
      return this.getSystemSMSCount(subType, username, (String)null);
   }

   public int getSystemSMSCount(SystemSMSData.SubTypeEnum subType, String username, String destination) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var7;
      try {
         conn = this.dataSourceMaster.getConnection();
         if (destination == null) {
            ps = conn.prepareStatement("select count(*) from systemsms where username = ? and type = ? and subtype = ? and datecreated > curdate()");
         } else {
            ps = conn.prepareStatement("select count(*) from systemsms where username = ? and type = ? and subtype = ? and destination = ? and datecreated > curdate()");
            ps.setString(4, destination);
         }

         ps.setString(1, username);
         ps.setInt(2, SystemSMSData.TypeEnum.STANDARD.value());
         ps.setInt(3, subType.value());
         rs = ps.executeQuery();
         rs.next();
         var7 = rs.getInt(1);
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }

      return var7;
   }

   public String cleanPhoneNumber(String phoneNumber) throws EJBException {
      if (!StringUtils.hasLength(phoneNumber)) {
         throw new EJBException("Invalid phone number");
      } else {
         String cleanNumber = phoneNumber;
         if (phoneNumber.charAt(0) == '+') {
            cleanNumber = phoneNumber.substring(1);
         }

         cleanNumber = cleanNumber.replaceAll("[()-\\. ]", "");
         return cleanNumber;
      }
   }

   public String cleanAndValidatePhoneNumber(String phoneNumber, boolean isMobile) throws EJBException {
      String cleanNumber = this.cleanPhoneNumber(phoneNumber);
      Integer iddCode = this.getIDDCode(cleanNumber);
      if (cleanNumber.length() - iddCode.toString().length() >= 5 && this.isNumeric(cleanNumber)) {
         boolean allowZeroInIDD = this.getIDDCodesAllowZeroVector().contains(iddCode);
         if (!allowZeroInIDD && isMobile && cleanNumber.substring(iddCode.toString().length()).startsWith("0")) {
            throw new EJBException(cleanNumber + " is not a valid phone number. Please remove 0 after the international code");
         } else if (isMobile && !this.isMobileNumber(cleanNumber, true)) {
            throw new EJBException(cleanNumber + " is not a valid mobile phone number");
         } else {
            return cleanNumber;
         }
      } else {
         throw new EJBException(cleanNumber + " is not a valid phone number");
      }
   }

   public Integer getIDDCode(String phoneNumber) throws EJBException {
      if (StringUtil.isBlank(phoneNumber)) {
         throw new EJBException("empty number");
      } else if (phoneNumber.charAt(0) == '0') {
         throw new EJBException("A country code was not specified in the number " + phoneNumber);
      } else {
         for(int i = 4; i >= 0; --i) {
            Integer possibleIDDCode;
            try {
               possibleIDDCode = Integer.parseInt(phoneNumber.substring(0, i));
            } catch (Exception var5) {
               throw new EJBException("Unable to determine the country code of the number " + phoneNumber);
            }

            if (this.getIDDCodesHashMap().containsKey(possibleIDDCode)) {
               return possibleIDDCode;
            }
         }

         throw new EJBException("Unable to determine the country code of the number " + phoneNumber);
      }
   }

   public MobilePrefixData getMobilePrefixData(int IDDCode, int mobilePrefix) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      MobilePrefixData var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from mobileprefix where IDDCode=? and prefix=?");
         ps.setInt(1, IDDCode);
         ps.setInt(2, mobilePrefix);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var6 = null;
            return var6;
         }

         var6 = new MobilePrefixData(rs);
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

   public int getMinimumMobileNumberLength(int IDDCode) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var5;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select min(minlength) from mobileprefix where IDDCode=?");
         ps.setInt(1, IDDCode);
         rs = ps.executeQuery();
         if (!rs.next()) {
            byte var25 = -1;
            return var25;
         }

         var5 = rs.getInt(1);
      } catch (SQLException var23) {
         throw new EJBException(var23.getMessage());
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

      return var5;
   }

   public boolean isMobileNumber(String phoneNumber, boolean verifyLength) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var8;
      try {
         Integer iddCode = this.getIDDCode(phoneNumber);
         int totalLength = phoneNumber.length();
         phoneNumber = phoneNumber.substring(iddCode.toString().length());
         if (iddCode.equals(1)) {
            var8 = phoneNumber.length() == 10;
            return var8;
         }

         conn = this.dataSourceSlave.getConnection();
         if (verifyLength) {
            ps = conn.prepareStatement("select count(*), sum(if(cast(? as unsigned) like concat(prefix, '%') and ? >= minlength and ? <= maxlength, 1, 0)) from mobileprefix where iddcode = ?");
            ps.setString(1, phoneNumber);
            ps.setInt(2, totalLength);
            ps.setInt(3, totalLength);
            ps.setInt(4, iddCode);
         } else {
            ps = conn.prepareStatement("select count(*), sum(if(cast(? as unsigned) like concat(prefix, '%'), 1, 0)) from mobileprefix where iddcode = ?");
            ps.setString(1, phoneNumber);
            ps.setInt(2, iddCode);
         }

         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Unable to query mobile prefix table");
         }

         var8 = rs.getInt(1) == 0 || rs.getInt(2) > 0;
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

   private boolean isNumeric(String str) {
      try {
         Long.parseLong(str);
         return true;
      } catch (NumberFormatException var3) {
         return false;
      }
   }

   public void changePendingSystemSMSToSent(int type, int id, int gateway, String source, int iddCode, String destination, String transactionID, boolean billed) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update systemsms set Status = ?, DateDispatched = ?, Gateway = ?, ProviderTransactionID = ?, Source = ? where ID = ? and Status = ?");
         ps.setInt(1, SystemSMSData.StatusEnum.SENT.value());
         ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
         ps.setObject(3, gateway);
         ps.setString(4, transactionID);
         ps.setString(5, source);
         ps.setInt(6, id);
         ps.setInt(7, SystemSMSData.StatusEnum.PENDING.value());
         ps.executeUpdate();
         AccountLocal accountBean = this.accountLocalHome.create();
         accountBean.updateWholesaleSystemSMSCost(String.valueOf(id), this.getWholesaleSMSCost(conn, gateway, iddCode, destination));
      } catch (CreateException var24) {
         throw new EJBException(var24.getMessage());
      } catch (SQLException var25) {
         throw new EJBException(var25.getMessage());
      } finally {
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

   }

   public void changePendingMessageToSent(MessageType messageType, int messageID, int messageDestinationID, Integer gateway, Integer iddCode, String destination, String transactionID) throws EJBException {
      Connection conn = null;
      Object ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         this.changePendingMessageToSent(conn, (PreparedStatement)ps, messageType, messageID, messageDestinationID, gateway, iddCode, destination, transactionID);
      } catch (CreateException var23) {
         throw new EJBException(var23.getMessage());
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage());
      } finally {
         try {
            if (ps != null) {
               ((PreparedStatement)ps).close();
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

   }

   private void changePendingMessageToSent(Connection conn, PreparedStatement ps, MessageType messageType, int messageID, int messageDestinationID, Integer gateway, Integer iddCode, String destination, String transactionID) throws CreateException, SQLException {
      ps = conn.prepareStatement("update messagedestination set Status = ?, DateDispatched = ?, Gateway = ?, ProviderTransactionID = ? where ID = ? and Status = ?");
      ps.setInt(1, MessageDestinationData.StatusEnum.SENT.value());
      ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
      ps.setObject(3, gateway);
      ps.setObject(4, transactionID);
      ps.setInt(5, messageDestinationID);
      ps.setInt(6, MessageDestinationData.StatusEnum.PENDING.value());
      ps.executeUpdate();
      if (messageType == MessageType.SMS) {
         AccountLocal accountBean = this.accountLocalHome.create();
         accountBean.updateWholesaleSMSCost(String.valueOf(messageID), this.getWholesaleSMSCost(conn, gateway, iddCode, destination));
      }

   }

   public MessageData getMessage(int messageID) throws EJBException {
      MessageData messageData = new MessageData();
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from message where id = ?");
         ps.setInt(1, messageID);
         rs = ps.executeQuery();
         if (rs.next()) {
            messageData.id = messageID;
            messageData.username = rs.getString("Username");
            messageData.dateCreated = rs.getTimestamp("dateCreated");
            Integer intVal = (Integer)rs.getObject("Type");
            if (intVal != null) {
               messageData.type = MessageType.fromValue(intVal);
            }

            messageData.messageText = rs.getString("MessageText");
            intVal = (Integer)rs.getObject("SendReceive");
            if (intVal != null) {
               messageData.sendReceive = MessageData.SendReceiveEnum.fromValue(intVal);
            }

            messageData.sourceContactID = (Integer)rs.getObject("SourceContactID");
            messageData.source = rs.getString("Source");
            rs.close();
            ps.close();
            ps = conn.prepareStatement("select * from messagedestination where MessageID = ?");
            ps.setInt(1, messageID);

            MessageDestinationData messageDestData;
            for(rs = ps.executeQuery(); rs.next(); messageData.messageDestinations.add(messageDestData)) {
               messageDestData = new MessageDestinationData();
               messageDestData.id = (Integer)rs.getObject("ID");
               messageDestData.messageID = messageID;
               messageDestData.contactID = (Integer)rs.getObject("ContactID");
               intVal = (Integer)rs.getObject("Type");
               if (intVal != null) {
                  messageDestData.type = MessageDestinationData.TypeEnum.fromValue(intVal);
               }

               messageDestData.destination = rs.getString("Destination");
               messageDestData.IDDCode = (Integer)rs.getObject("IDDCode");
               messageDestData.cost = (Double)rs.getObject("Cost");
               messageDestData.gateway = (Integer)rs.getObject("Gateway");
               messageDestData.dateDispatched = rs.getDate("DateDispatched");
               intVal = (Integer)rs.getObject("Status");
               if (intVal != null) {
                  messageDestData.status = MessageDestinationData.StatusEnum.fromValue(intVal);
               }

               if (messageData.messageDestinations == null) {
                  messageData.messageDestinations = new LinkedList();
               }
            }
         }
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }

      return messageData;
   }

   public List<MessageData> getMessages(String username, Integer type, Integer sendReceive, Date fromDate, Integer contactID, Integer status) throws EJBException {
      LinkedList<MessageData> messages = null;
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         String sql = "select message.ID as MessageID, message.Username, message.DateCreated, message.Type as MessageType, message.MessageText, message.SendReceive, message.SourceContactID, message.Source, messagedestination.ID as MessageDestinationID, messagedestination.ContactID, messagedestination.Type as MessageDestType, messagedestination.Destination, messagedestination.IDDCode, messagedestination.Cost, messagedestination.Gateway, messagedestination.DateDispatched, messagedestination.Status from message left outer join messagedestination on message.ID = messagedestination.MessageID ";
         String criteria = "";
         if (username != null) {
            criteria = criteria + " message.username = ?";
         }

         if (type != null) {
            if (criteria.length() > 0) {
               criteria = criteria + " and";
            }

            criteria = criteria + " message.Type = " + type;
         }

         if (sendReceive != null) {
            if (criteria.length() > 0) {
               criteria = criteria + " and";
            }

            criteria = criteria + " message.SendReceive = " + sendReceive;
         }

         if (fromDate != null) {
            if (criteria.length() > 0) {
               criteria = criteria + " and";
            }

            criteria = criteria + " message.DateCreated >= ?";
         }

         if (contactID != null) {
            if (criteria.length() > 0) {
               criteria = criteria + " and";
            }

            criteria = criteria + " (message.SourceContactID = " + contactID + " or messagedestination.ContactID = " + contactID + ")";
         }

         if (status != null) {
            if (criteria.length() > 0) {
               criteria = criteria + " and";
            }

            criteria = criteria + " messagedestination.Status = " + status;
         }

         if (criteria.length() > 0) {
            sql = sql + " where " + criteria;
         }

         sql = sql + " order by message.DateCreated desc";
         ps = conn.prepareStatement(sql);
         if (username == null) {
            if (fromDate != null) {
               ps.setTimestamp(1, new Timestamp(fromDate.getTime()));
            }
         } else {
            ps.setString(1, username);
            if (fromDate != null) {
               ps.setTimestamp(2, new Timestamp(fromDate.getTime()));
            }
         }

         rs = ps.executeQuery();
         MessageData messageData = null;

         while(rs.next()) {
            if (messages == null) {
               messages = new LinkedList();
            }

            if (messageData == null || messageData.id != (Integer)rs.getObject("MessageID")) {
               messageData = new MessageData();
               messageData.id = (Integer)rs.getObject("MessageID");
               messageData.username = rs.getString("Username");
               messageData.dateCreated = rs.getTimestamp("DateCreated");
               Integer intVal = (Integer)rs.getObject("MessageType");
               if (intVal != null) {
                  messageData.type = MessageType.fromValue(intVal);
               }

               messageData.messageText = rs.getString("MessageText");
               intVal = (Integer)rs.getObject("SendReceive");
               if (intVal != null) {
                  messageData.sendReceive = MessageData.SendReceiveEnum.fromValue(intVal);
               }

               messageData.sourceContactID = (Integer)rs.getObject("SourceContactID");
               messageData.source = rs.getString("Source");
               messages.add(messageData);
            }

            if (rs.getObject("MessageDestinationID") != null) {
               MessageDestinationData messageDestData = new MessageDestinationData();
               messageDestData.id = (Integer)rs.getObject("MessageDestinationID");
               messageDestData.messageID = messageData.id;
               messageDestData.contactID = (Integer)rs.getObject("ContactID");
               Integer intVal1 = (Integer)rs.getObject("MessageDestType");
               if (intVal1 != null) {
                  messageDestData.type = MessageDestinationData.TypeEnum.fromValue(intVal1);
               }

               messageDestData.destination = rs.getString("Destination");
               messageDestData.IDDCode = (Integer)rs.getObject("IDDCode");
               messageDestData.cost = (Double)rs.getObject("Cost");
               messageDestData.gateway = (Integer)rs.getObject("Gateway");
               messageDestData.dateDispatched = rs.getDate("DateDispatched");
               intVal1 = (Integer)rs.getObject("Status");
               if (intVal1 != null) {
                  messageDestData.status = MessageDestinationData.StatusEnum.fromValue(intVal1);
               }

               if (messageData.messageDestinations == null) {
                  messageData.messageDestinations = new LinkedList();
               }

               messageData.messageDestinations.add(messageDestData);
            }
         }
      } catch (SQLException var30) {
         throw new EJBException(var30.getMessage());
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

      return messages;
   }

   public SystemSMSData getSystemSMS(String providerTransactionID, String destination) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      SystemSMSData var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from systemsms where providertransactionid = ? and destination = ?");
         ps.setString(1, providerTransactionID);
         ps.setString(2, destination);
         rs = ps.executeQuery();
         var6 = rs.next() ? new SystemSMSData(rs) : null;
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

      return var6;
   }

   public List<SystemSMSData> getSystemSMS(String username, Integer type, Date fromDate, Integer status) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         String sql = "select * from systemsms";
         String criteria = "";
         if (username != null) {
            criteria = criteria + " username = ?";
         }

         if (type != null) {
            if (criteria.length() > 0) {
               criteria = criteria + " and";
            }

            criteria = criteria + " Type = " + type;
         }

         if (fromDate != null) {
            if (criteria.length() > 0) {
               criteria = criteria + " and";
            }

            criteria = criteria + " DateCreated >= ?";
         }

         if (status != null) {
            if (criteria.length() > 0) {
               criteria = criteria + " and";
            }

            criteria = criteria + " Status = " + status;
         }

         if (criteria.length() > 0) {
            sql = sql + " where " + criteria;
         }

         sql = sql + " order by DateCreated";
         ps = conn.prepareStatement(sql);
         if (username == null) {
            if (fromDate != null) {
               ps.setTimestamp(1, new Timestamp(fromDate.getTime()));
            }
         } else {
            ps.setString(1, username);
            if (fromDate != null) {
               ps.setTimestamp(2, new Timestamp(fromDate.getTime()));
            }
         }

         List<SystemSMSData> systemSMSList = new LinkedList();
         rs = ps.executeQuery();

         while(rs.next()) {
            systemSMSList.add(new SystemSMSData(rs));
         }

         LinkedList var11 = systemSMSList;
         return var11;
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
   }

   public List<SMSGatewayData> getSMSGateways() throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         String sql = "select * from smsgateway inner join smsroute on smsgateway.id = smsroute.gatewayid where smsgateway.status = ? order by smsgateway.id, smsroute.iddcode, smsroute.priority";
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement(sql);
         ps.setInt(1, SMSGatewayData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();
         List<SMSGatewayData> gateways = new LinkedList();
         SMSGatewayData gateway = null;

         int gatewayID;
         for(int previousGatewayID = Integer.MIN_VALUE; rs.next(); previousGatewayID = gatewayID) {
            gatewayID = rs.getInt("id");
            if (gatewayID != previousGatewayID) {
               gateway = new SMSGatewayData();
               gateway.id = gatewayID;
               gateway.name = rs.getString("name");
               gateway.type = SMSGatewayData.TypeEnum.fromValue(rs.getInt("smsgateway.type"));
               gateway.url = rs.getString("url");
               gateway.port = (Integer)rs.getObject("port");
               gateway.method = SMSGatewayData.MethodEnum.fromValue(rs.getInt("method"));
               gateway.iddPrefix = rs.getString("iddPrefix");
               gateway.authorization = rs.getString("authorization");
               gateway.usernameParam = rs.getString("usernameParam");
               gateway.passwordParam = rs.getString("passwordParam");
               gateway.sourceParam = rs.getString("sourceParam");
               gateway.destinationParam = rs.getString("destinationParam");
               gateway.messageParam = rs.getString("messageParam");
               gateway.unicodeMessageParam = rs.getString("unicodeMessageParam");
               gateway.unicodeParam = rs.getString("unicodeParam");
               gateway.extraParam = rs.getString("extraParam");
               gateway.unicodeCharset = rs.getString("unicodeCharset");
               gateway.successPattern = rs.getString("successPattern");
               gateway.errorPattern = rs.getString("errorPattern");
               gateway.deliveryReporting = rs.getInt("deliveryReporting") != 0;
               gateway.status = SMSGatewayData.StatusEnum.ACTIVE;
               gateway.smsRoutes = new LinkedList();
               gateways.add(gateway);
            }

            SMSRouteData route = new SMSRouteData();
            route.iddCode = rs.getInt("iddCode");
            route.areaCode = rs.getString("areaCode");
            route.type = SMSRouteData.TypeEnum.fromValue(rs.getInt("smsroute.type"));
            route.gatewayID = gatewayID;
            route.priority = rs.getInt("priority");
            gateway.smsRoutes.add(route);
         }

         LinkedList var26 = gateways;
         return var26;
      } catch (SQLException var24) {
         log.error("Failed to generate SMSGatewayData, due to:" + var24, var24);
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
   }

   public ChatRoomData getSimpleChatRoomData(Integer chatroomId, Connection conn) throws EJBException {
      ConnectionHolder slaveConnection = new ConnectionHolder(this.dataSourceSlave, conn);
      PreparedStatement ps = null;
      ResultSet rs = null;

      ChatRoomData var6;
      try {
         ps = slaveConnection.getConnection().prepareStatement("select name from chatroom where id = ?");
         ps.setInt(1, chatroomId);
         rs = ps.executeQuery();
         if (!rs.next()) {
            return null;
         }

         var6 = this.getSimpleChatRoomData(rs.getString("name"), conn);
      } catch (SQLException var27) {
         log.error("failed to retrieve chatroom data for chatroom id " + chatroomId, var27);
         return null;
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var26) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var25) {
            ps = null;
         }

         try {
            if (slaveConnection != null) {
               slaveConnection.close();
            }
         } catch (SQLException var24) {
            slaveConnection = null;
         }

      }

      return var6;
   }

   public ChatRoomData getSimpleChatRoomData(String normalizedChatRoomName, Connection conn) throws EJBException {
      ConnectionHolder c = new ConnectionHolder(this.dataSourceSlave, conn);
      PreparedStatement ps = null;
      ResultSet rs = null;
      boolean negativeCacheEnabled = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.NEGATIVE_CACHE_ENABLED);
      if (negativeCacheEnabled && MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CHATROOM_NEGATIVE_CACHE, normalizedChatRoomName) != null) {
         if (log.isDebugEnabled()) {
            log.debug("Attempt to get invalid chatroom : " + normalizedChatRoomName + ",found in negative cache");
         }

         return null;
      } else {
         ChatRoomData var8;
         try {
            ChatRoomData room = (ChatRoomData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CHATROOM, normalizedChatRoomName);
            if (room == null) {
               ps = c.getConnection().prepareStatement("select * from chatroom where name = ? and status = ?");
               ps.setString(1, normalizedChatRoomName);
               ps.setInt(2, ChatRoomData.StatusEnum.ACTIVE.value());
               rs = ps.executeQuery();
               if (!rs.next() || !normalizedChatRoomName.equalsIgnoreCase(rs.getString("name"))) {
                  if (negativeCacheEnabled) {
                     MemCachedClientWrapper.add(MemCachedKeySpaces.CommonKeySpace.CHATROOM_NEGATIVE_CACHE, normalizedChatRoomName, 1);
                  }

                  var8 = null;
                  return var8;
               }

               room = new ChatRoomData(rs);
               rs.close();
               ps.close();
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MULTI_ID_CONTROL_ENABLED)) {
                  ps = c.getConnection().prepareStatement("select * from chatroomextradata where chatroomid = ?");
                  ps.setInt(1, room.id);
                  rs = ps.executeQuery();
                  room.updateExtraData(rs);
                  rs.close();
                  ps.close();
               }

               MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CHATROOM, normalizedChatRoomName, room);
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHATROOM_SYNC_ENABLED)) {
                  MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BY_ID, Integer.toString(room.id), room);
               }
            }

            var8 = room;
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
               if (c != null) {
                  c.close();
               }
            } catch (SQLException var23) {
               c = null;
            }

         }

         return var8;
      }
   }

   public List<ChatroomCategoryData> getLoginChatroomCategories() throws Exception {
      List<ChatroomCategoryData> chatroomCategories = new ArrayList();
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         String sql = "SELECT id        ,name        ,itemscanbedeleted        ,initiallycollapsed        ,maxmiglevel        ,orderindex        ,refreshdisplaystring        ,status        ,refreshmethod FROM chatroomcategorylist WHERE status = ? ORDER BY OrderIndex DESC";
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement(sql);
         ps.setInt(1, ChatroomCategoryData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();

         while(rs.next()) {
            ChatroomCategoryData catroomCategory = new ChatroomCategoryData(rs);
            chatroomCategories.add(catroomCategory);
         }

         ArrayList var23 = chatroomCategories;
         return var23;
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
   }

   public ChatroomCategoryData getChatroomCategory(Integer categoryId) throws Exception {
      if (categoryId == null) {
         throw new Exception("Id is required.");
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         ChatroomCategoryData var6;
         try {
            String sql = "SELECT * FROM chatroomcategorylist WHERE id = ?";
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, categoryId);
            rs = ps.executeQuery();
            if (!rs.next()) {
               log.error("Error in retrieving chatroom category details for [" + categoryId + "]");
               throw new Exception("Unable to retrieve chatroom category.");
            }

            var6 = new ChatroomCategoryData(rs);
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

         return var6;
      }
   }

   public Map<Integer, List<String>> getChatroomNamesPerCategory(boolean activeChatroomsAndCategoriesOnly) throws Exception {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      HashMap chatroomNamesPerCategory = new HashMap();

      try {
         String sql = "SELECT c2crl.chatroomcategorylistid, c.name FROM chatroom c     ,chatroomcategorylist crl \t   ,chatroomtochatroomcategorylist c2crl WHERE c.id = c2crl.chatroomid AND crl.id = c2crl.chatroomcategorylistid ";
         if (activeChatroomsAndCategoriesOnly) {
            sql = sql + "AND crl.status = ? AND c.status = ? ";
         }

         sql = sql + "ORDER BY c2crl.orderIndex DESC";
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement(sql);
         if (activeChatroomsAndCategoriesOnly) {
            ps.setInt(1, ChatroomCategoryData.StatusEnum.ACTIVE.value());
            ps.setInt(2, ChatRoomData.StatusEnum.ACTIVE.value());
         }

         int categoryID;
         for(rs = ps.executeQuery(); rs.next(); ((List)chatroomNamesPerCategory.get(categoryID)).add(rs.getString("name"))) {
            categoryID = rs.getInt("chatroomcategorylistid");
            if (!chatroomNamesPerCategory.containsKey(categoryID)) {
               chatroomNamesPerCategory.put(categoryID, new ArrayList());
            }
         }

         HashMap var24 = chatroomNamesPerCategory;
         return var24;
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }
   }

   public String[] getChatroomNamesInCategory(int categoryId) throws Exception {
      return this.getChatroomNamesInCategory(categoryId, true);
   }

   public String[] getChatroomNamesInCategory(int categoryId, boolean activeChatroomsAndCategoriesOnly) throws Exception {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         String sql = "SELECT c.name FROM chatroom c      ,chatroomcategorylist cl      ,chatroomtochatroomcategorylist c2cl WHERE c.id = c2cl.chatroomid AND cl.id = c2cl.chatroomcategorylistid AND cl.id = ? ";
         if (activeChatroomsAndCategoriesOnly) {
            sql = sql + "AND cl.status = ? AND c.status = ?";
         }

         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement(sql);
         ps.setInt(1, categoryId);
         if (activeChatroomsAndCategoriesOnly) {
            ps.setInt(2, ChatroomCategoryData.StatusEnum.ACTIVE.value());
            ps.setInt(3, ChatRoomData.StatusEnum.ACTIVE.value());
         }

         rs = ps.executeQuery();
         ArrayList chatroomNames = new ArrayList();

         while(rs.next()) {
            chatroomNames.add(rs.getString("name"));
         }

         String[] var8 = (String[])chatroomNames.toArray(new String[chatroomNames.size()]);
         return var8;
      } catch (SQLException var23) {
         throw new EJBException(var23.getMessage());
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
   }

   public ChatRoomData getChatRoom(String name) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      String normalizedChatRoomName = ChatRoomUtils.normalizeChatRoomName(name);

      Integer pageSize;
      try {
         conn = this.dataSourceSlave.getConnection();
         ChatRoomData room = this.getSimpleChatRoomData(normalizedChatRoomName, conn);
         if (room != null) {
            if (room.themeID != null) {
               room.theme = (Map)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CHATROOM_THEME, String.valueOf(room.themeID));
               if (room.theme == null) {
                  ps = conn.prepareStatement("select attributekey,attributevalue from chatroomtheme, chatroomthemeattribute where chatroomtheme.id=? and chatroomtheme.status=? and chatroomtheme.id=chatroomthemeattribute.chatroomthemeid");
                  ps.setInt(1, room.themeID);
                  ps.setInt(2, 1);
                  rs = ps.executeQuery();
                  room.theme = new HashMap();

                  while(true) {
                     if (!rs.next()) {
                        rs.close();
                        ps.close();
                        MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CHATROOM_THEME, String.valueOf(room.themeID), room.theme);
                        break;
                     }

                     room.theme.put(rs.getString("attributekey"), rs.getString("attributevalue"));
                  }
               }
            }

            if (room.userOwned) {
               room.moderators = (HashSet)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CHATROOM_MODERATORS, normalizedChatRoomName);
               if (room.moderators == null) {
                  ps = conn.prepareStatement("select username from chatroommoderator where chatroomid=?");
                  ps.setInt(1, room.id);
                  rs = ps.executeQuery();
                  room.moderators = new HashSet();

                  while(rs.next()) {
                     room.moderators.add(rs.getString("username"));
                  }

                  rs.close();
                  ps.close();
                  MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CHATROOM_MODERATORS, normalizedChatRoomName, room.moderators);
               }

               room.bannedUsers = (HashSet)MemCachedClientWrapper.getPaged(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BANNED_USERS, normalizedChatRoomName);
               if (room.bannedUsers == null) {
                  ps = conn.prepareStatement("select username from chatroombanneduser where chatroomid=?");
                  ps.setInt(1, room.id);
                  rs = ps.executeQuery();
                  room.bannedUsers = new HashSet();

                  while(rs.next()) {
                     room.bannedUsers.add(rs.getString("username"));
                  }

                  pageSize = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.BANNED_USERS_PAGE_SIZE);
                  MemCachedClientWrapper.setPaged(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BANNED_USERS, normalizedChatRoomName, room.bannedUsers, pageSize);
               }
            }

            ChatRoomData var27 = room;
            return var27;
         }

         pageSize = null;
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

      return pageSize;
   }

   public void updateRoomExtraData(ChatRoomData newData) throws EJBException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MULTI_ID_CONTROL_ENABLED)) {
         Connection connMaster = null;
         PreparedStatement ps = null;
         Object rs = null;

         try {
            connMaster = this.dataSourceMaster.getConnection();
            String normalizedChatRoomName = ChatRoomUtils.normalizeChatRoomName(newData.name);
            ChatRoomData oldData = this.getSimpleChatRoomData(normalizedChatRoomName, connMaster);
            Map<Integer, String> m = newData.convertExtraDataDifferenceToIntegerAndStringMap(oldData);
            if (!m.isEmpty()) {
               ps = connMaster.prepareStatement("insert into chatroomextradata (chatroomid, type, value) values (?,?,?) on duplicate key update value=?");
               Iterator i$ = m.entrySet().iterator();

               while(i$.hasNext()) {
                  Entry<Integer, String> entry = (Entry)i$.next();
                  ps.setInt(1, newData.id);
                  ps.setInt(2, (Integer)entry.getKey());
                  ps.setString(3, (String)entry.getValue());
                  ps.setString(4, (String)entry.getValue());
                  ps.addBatch();
               }

               int[] batchResults = ps.executeBatch();
               if (batchResults == null || batchResults.length != m.size()) {
                  throw new SQLException("Unable to update chatroom extra data");
               }

               int[] arr$ = batchResults;
               int len$ = batchResults.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  int batchResult = arr$[i$];
                  if (batchResult <= 0) {
                     log.error(String.format("batch result '%d' is not 1 when adding/updating chatroom extra data", batchResult));
                     throw new SQLException("Unable to update chatroom extra data");
                  }
               }

               ps.close();
               ChatRoomUtils.invalidateChatRoomCache(newData.name);
               ChatRoomPrx chatRoomPrx = EJBIcePrxFinder.findChatRoomPrx(newData.name);
               if (chatRoomPrx != null) {
                  chatRoomPrx.updateExtraData(newData.toIceObject());
               }
            }
         } catch (SQLException var28) {
            log.error("SQLException in updateChatRoomExtraData()", var28);
            throw new EJBException("Unable to update room extra data");
         } catch (LocalException var29) {
            log.error("Ice.LocalException in updateChatRoomExtraData()", var29);
            throw new EJBException("Unable to update room extra data");
         } finally {
            try {
               if (rs != null) {
                  ((ResultSet)rs).close();
               }
            } catch (SQLException var27) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var26) {
               ps = null;
            }

            try {
               if (connMaster != null) {
                  connMaster.close();
               }
            } catch (SQLException var25) {
               connMaster = null;
            }

         }

      }
   }

   public void updateRoomDetails(String username, String chatRoomName, String language, String description) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select id, language, description from chatroom where name=? and creator=? and userowned=1");
         ps.setString(1, chatRoomName);
         ps.setString(2, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("You cannot modify a room unless you own the room");
         }

         boolean descriptionChange = !description.equals(rs.getString("description"));
         boolean languageChange = StringUtils.hasLength(language) && !language.equals(rs.getString("language"));
         if (descriptionChange || languageChange) {
            rs.close();
            ps.close();
            if (StringUtils.hasLength(description) && description.length() > 128) {
               description = description.substring(0, 128);
            }

            String sql = "update chatroom set description=? ";
            if (languageChange) {
               sql = sql + ", language=? ";
            }

            sql = sql + "where name=?";
            ps = connMaster.prepareStatement(sql);
            ps.setString(1, description);
            if (languageChange) {
               ps.setString(2, language);
            }

            ps.setString(languageChange ? 3 : 2, chatRoomName);
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 1) {
               ChatRoomUtils.invalidateChatRoomCache(chatRoomName);
               ChatRoomPrx chatRoomPrx = EJBIcePrxFinder.findChatRoomPrx(chatRoomName);
               if (chatRoomPrx != null) {
                  chatRoomPrx.setDescription(description);
               }
            }

            String subject = "Chat room " + chatRoomName + ": ";
            if (descriptionChange) {
               subject = subject + "Description ";
            }

            if (languageChange) {
               if (descriptionChange) {
                  subject = subject + " and ";
               }

               subject = subject + "Language ";
            }

            subject = subject + "Changed";
            String body = "Your chat room " + chatRoomName + " has been modified.\n\n";
            if (descriptionChange) {
               body = body + "New description: " + description + "\n\n";
            }

            if (languageChange) {
               body = body + "New language: " + language + "\n\n";
            }

            body = body + "If this was done in error or you want to change this please go to your room and select Room Settings from the menu.";
            this.sendSystemEmail(username, subject, body);
            return;
         }
      } catch (SQLException var32) {
         log.error("SQLException in updateRoomDetails()", var32);
         throw new EJBException("Unable to update room");
      } catch (LocalException var33) {
         log.error("Ice.LocalException in updateRoomDetails()", var33);
         throw new EJBException("Unable to update room.");
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var31) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var30) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var29) {
            connMaster = null;
         }

      }

   }

   public void updateRoomKickingRule(String username, String chatRoomName, boolean allowKicking) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select id from chatroom where name=? and creator=? and userowned=1");
         ps.setString(1, chatRoomName);
         ps.setString(2, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("You cannot modify a room unless you own the room");
         }

         rs.close();
         ps.close();
         ps = connMaster.prepareStatement("update chatroom set allowkicking=? where name=?");
         ps.setInt(1, allowKicking ? 1 : 0);
         ps.setString(2, chatRoomName);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated == 1) {
            ChatRoomUtils.invalidateChatRoomCache(chatRoomName);
            ChatRoomPrx chatRoomPrx = EJBIcePrxFinder.findChatRoomPrx(chatRoomName);
            if (chatRoomPrx != null) {
               chatRoomPrx.setAllowKicking(allowKicking);
            }
         }

         String subject = "Chat room " + chatRoomName + ": Kicking has been " + (allowKicking ? "enabled" : "disabled");
         String body = "Your chat room " + chatRoomName + " has been modified.\n\n";
         body = body + "Kicking has been " + (allowKicking ? "enabled" : "disabled") + "\n\n";
         if (allowKicking) {
            body = body + "Users will be allowed to initiate a kick vote.\n\n";
         } else {
            body = body + "Users will not be allowed to initiate a kick vote.\n\n";
         }

         body = body + "If this was done in error or you want to change this please go to your room and select Room Settings from the menu.";
         this.sendSystemEmail(username, subject, body);
      } catch (SQLException var25) {
         log.error("SQLException in updateRoomDetails()", var25);
         throw new EJBException("Unable to update room");
      } catch (LocalException var26) {
         log.error("Ice.LocalException in updateRoomDetails()", var26);
         throw new EJBException("Unable to update room");
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
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var22) {
            connMaster = null;
         }

      }

   }

   public void updateRoomAdultOnlyFlag(String username, String chatRoomName, boolean adultOnly) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select username, chatroom.id from user, chatroom where username=? and chatroomadmin=1 and chatroom.name=?");
         ps.setString(1, username);
         ps.setString(2, chatRoomName);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("You cannot modify room's adult only flag unless you are an admin");
         }

         int chatRoomID = rs.getInt("id");
         rs.close();
         ps.close();
         ps = connMaster.prepareStatement("update chatroom set adultonly=? where name=?");
         ps.setInt(1, adultOnly ? 1 : 0);
         ps.setString(2, chatRoomName);
         int rowsUpdated = ps.executeUpdate();
         connMaster.close();
         connMaster = null;
         if (rowsUpdated == 1) {
            ChatRoomUtils.invalidateChatRoomCache(chatRoomName);
            ChatRoomPrx chatRoomPrx = EJBIcePrxFinder.findChatRoomPrx(chatRoomName);
            if (chatRoomPrx != null) {
               chatRoomPrx.setAdultOnly(adultOnly);
            }

            if (!SystemProperty.getBool("DisableElasticSearch", false)) {
               try {
                  ChatRoomsIndex.updateChatRoomAdultOnlyFlag(chatRoomID, chatRoomName, adultOnly);
               } catch (Exception var27) {
                  log.error("Unable to update chat room '" + chatRoomName + "' adult only flag in ElasticSearch", var27);
               }
            }
         }
      } catch (SQLException var28) {
         throw new EJBException("Unable to update room");
      } catch (LocalException var29) {
         throw new EJBException("Unable to update room");
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var26) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var25) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var24) {
            connMaster = null;
         }

      }

   }

   public void updateRoomKeywords(String username, String chatRoomName, String keywords, int allowUserKeywords) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      boolean modifiedByModerator = false;
      boolean modifiedByUser = false;
      String ownerUsername = username;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select id from chatroom where name=? and creator=? and userowned=1");
         ps.setString(1, chatRoomName);
         ps.setString(2, username);
         rs = ps.executeQuery();
         int chatRoomID;
         if (!rs.next()) {
            rs.close();
            ps.close();
            ps = connMaster.prepareStatement("SELECT chatroom.id, chatroom.creator, chatroom.allowuserkeywords, chatroommoderator.username moderatorusername FROM chatroom LEFT OUTER JOIN chatroommoderator ON chatroom.id=chatroommoderator.chatroomid AND chatroommoderator.username=? WHERE chatroom.name=? AND chatroom.status=1 AND chatroom.userowned=1");
            ps.setString(1, username);
            ps.setString(2, chatRoomName);
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new EJBException("Unable to modify chat room keywords");
            }

            if (rs.getInt("allowuserkeywords") == 0 && !StringUtils.hasLength(rs.getString("moderatorusername"))) {
               throw new EJBException("You cannot modify the room's keywords unless you own the room or are a moderator");
            }

            chatRoomID = rs.getInt("id");
            ownerUsername = rs.getString("creator");
            if (StringUtils.hasLength(rs.getString("moderatorusername"))) {
               modifiedByModerator = true;
            } else {
               modifiedByUser = true;
            }
         } else {
            chatRoomID = rs.getInt("id");
         }

         rs.close();
         ps.close();
         Set<String> oldKeywords = new HashSet();
         Set<String> newKeywords = new HashSet();
         Set<String> addedKeywords = new HashSet();
         Set<String> removedKeywords = new HashSet();
         ps = connMaster.prepareStatement("select keyword.keyword from keyword, chatroomkeyword, chatroom where chatroom.name=? and chatroom.id=chatroomkeyword.chatroomid and chatroomkeyword.keywordid=keyword.id and chatroom.status=1");
         ps.setString(1, chatRoomName);
         rs = ps.executeQuery();

         String subject;
         while(rs.next()) {
            subject = rs.getString("keyword").trim().toLowerCase();
            oldKeywords.add(subject);
            removedKeywords.add(subject);
         }

         rs.close();
         ps.close();
         if (StringUtils.hasLength(keywords)) {
            String[] keywordsArray = keywords.split(",");

            for(int i = 0; i < keywordsArray.length; ++i) {
               String keyword = keywordsArray[i].trim().toLowerCase();
               if (keyword.length() > 64) {
                  keyword = keyword.substring(0, 64);
               }

               if (StringUtils.hasLength(keyword)) {
                  newKeywords.add(keyword);
                  addedKeywords.add(keyword);
               }
            }
         }

         addedKeywords.removeAll(oldKeywords);
         removedKeywords.removeAll(newKeywords);
         if (allowUserKeywords != -1 && !modifiedByUser) {
            ps = connMaster.prepareStatement("update chatroom set allowuserkeywords=? where name=?");
            ps.setBoolean(1, allowUserKeywords == 1);
            ps.setString(2, chatRoomName);
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 1) {
               ChatRoomUtils.invalidateChatRoomCache(chatRoomName);
            }

            ps.close();
         }

         if (!modifiedByUser) {
            ps = connMaster.prepareStatement("delete from chatroomkeyword where chatroomid=?");
            ps.setInt(1, chatRoomID);
            ps.executeUpdate();
         }

         this.addChatRoomKeywords(chatRoomID, keywords, connMaster);
         connMaster.close();
         connMaster = null;
         if (!SystemProperty.getBool("DisableElasticSearch", false)) {
            try {
               ChatRoomsIndex.updateChatRoomTags(chatRoomID, chatRoomName, newKeywords);
            } catch (Exception var36) {
               log.error("Unable to update chat room '" + chatRoomName + "' tags in ElasticSearch", var36);
            }
         }

         subject = "Chat room " + chatRoomName + ": Keywords have been modified";
         String body = "Your chat room " + chatRoomName + " has been modified.\n\n";
         if (modifiedByModerator) {
            body = body + "This change was made by the moderator " + username + "\n\n";
         }

         if (modifiedByUser) {
            body = body + "This change was made by the user " + username + "\n\n";
         }

         String keyword;
         Iterator i$;
         if (addedKeywords.size() > 0) {
            body = body + "The following keywords were added:\n";

            for(i$ = addedKeywords.iterator(); i$.hasNext(); body = body + keyword + "\n") {
               keyword = (String)i$.next();
            }

            body = body + "\n";
         }

         if (!modifiedByUser && removedKeywords.size() > 0) {
            body = body + "The following keywords were removed:\n";

            for(i$ = removedKeywords.iterator(); i$.hasNext(); body = body + keyword + "\n") {
               keyword = (String)i$.next();
            }

            body = body + "\n";
         }

         if (allowUserKeywords != -1) {
            body = body + "Users are ";
            if (allowUserKeywords != 1) {
               body = body + " not ";
            }

            body = body + "allowed to add keywords.\n\n";
         }

         body = body + "If this was done in error or you want to change this please go to your room and select Room Settings from the menu.";
         this.sendSystemEmail(ownerUsername, subject, body);
      } catch (SQLException var37) {
         log.error("SQLException in updateRoomDetails()", var37);
         throw new EJBException("Unable to update room");
      } catch (LocalException var38) {
         log.error("Ice.LocalException in updateRoomDetails()", var38);
         throw new EJBException("Unable to update room");
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var35) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var34) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var33) {
            connMaster = null;
         }

      }

   }

   public List<ChatRoomData> getChatRooms(int countryID, String search) throws EJBException {
      return this.getChatRooms(countryID, search, (String)null, true, false);
   }

   public List<ChatRoomData> getChatRooms(int countryID, String search, String language, boolean includeAdultOnly, boolean searchKeywords) throws EJBException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.ENABLED_REFINED_SQL_FOR_SEARCHING_CHATROOM)) {
         return this.getChatRoomsV2(countryID, search, language, includeAdultOnly, searchKeywords);
      } else {
         Connection connSlave = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         boolean performingSearch = StringUtils.hasLength(search);
         List<ChatRoomData> chatRooms = new LinkedList();
         if (performingSearch) {
            List<ChatRoomData> cachedChatRooms = ChatRoomSearch.getChatRoomSearch(chatRoomSearchMemcache, countryID, search, language, includeAdultOnly, searchKeywords);
            if (cachedChatRooms != null) {
               return cachedChatRooms;
            }
         }

         Object var42;
         try {
            int maxChatRoomsReturned = SystemProperty.getInt("MaxChatRoomsReturned");
            if (!SystemProperty.getBool("DisableElasticSearch", false) && !SystemProperty.getBool("DisableElasticSearchQueries", false)) {
               chatRooms = ChatRoomsIndex.searchChatRooms(countryID, search, language, includeAdultOnly, searchKeywords, maxChatRoomsReturned);
            } else {
               List<ChatRoomData> adultRooms = new LinkedList();
               connSlave = this.dataSourceSlave.getConnection();
               int chatRoomPageSize;
               if (performingSearch) {
                  String searchLike = search.trim().replaceAll("[\\*%]", "");
                  chatRoomPageSize = SystemProperty.getInt((String)"MinChatRoomSearchLength", 0);
                  if (searchLike.length() < chatRoomPageSize) {
                     throw new EJBException("Search string must have at least " + chatRoomPageSize + " characters");
                  }

                  searchLike = searchLike.replaceAll("_", "\\\\_") + "%";
                  String sql = "(select chatroom.*, null as keywords, 1 as sortorder from chatroom where name = ? and status = 1) union (select chatroom.*, null as keywords, 2 as sortorder from chatroom where name like ? and status = 1 ";
                  if (!includeAdultOnly) {
                     sql = sql + "and chatroom.adultonly=0 ";
                  }

                  sql = sql + "order by datelastaccessed desc limit ?) ";
                  if (searchKeywords) {
                     sql = sql + "union (select chatroom.*, group_concat(keyword.keyword) as keywords, 2 as sortorder from chatroom left join chatroomkeyword on chatroom.id=chatroomkeyword.chatroomid left join keyword on chatroomkeyword.keywordid=keyword.id where keyword.keyword = ? and chatroom.status = 1 ";
                     if (!includeAdultOnly) {
                        sql = sql + "and chatroom.adultonly=0 ";
                     }

                     sql = sql + "group by id, name, description, type, creator, chatroomcategoryid, primarycountryid, secondarycountryid, locationid, groupid, adultonly, maximumsize, userowned, allowkicking, allowuserkeywords, allowBots, language, datecreated, datelastaccessed, status order by datelastaccessed desc limit ?) ";
                  }

                  sql = sql + "order by sortorder, datelastaccessed desc limit ?";
                  ps = connSlave.prepareStatement(sql);
                  ps.setString(1, search);
                  ps.setString(2, searchLike);
                  ps.setInt(3, maxChatRoomsReturned);
                  if (searchKeywords) {
                     ps.setString(4, search);
                     ps.setInt(5, maxChatRoomsReturned);
                     ps.setInt(6, maxChatRoomsReturned);
                  } else {
                     ps.setInt(4, maxChatRoomsReturned);
                  }

                  rs = ps.executeQuery();
                  boolean languageSearch = performingSearch && StringUtils.hasLength(language);
                  List<ChatRoomData> languageMatchRooms = null;
                  if (languageSearch) {
                     languageMatchRooms = new LinkedList();
                  }

                  ChatRoomData exactMatch = null;

                  while(true) {
                     while(rs.next()) {
                        ChatRoomData room = new ChatRoomData(rs);
                        if (room.name.equalsIgnoreCase(search)) {
                           exactMatch = room;
                        } else if (room.adultOnly != null && !room.adultOnly) {
                           if (languageSearch && room.language != null && room.language.equals(language)) {
                              languageMatchRooms.add(room);
                           } else {
                              ((List)chatRooms).add(room);
                           }
                        } else {
                           adultRooms.add(room);
                        }
                     }

                     if (languageSearch) {
                        ((List)chatRooms).addAll(0, languageMatchRooms);
                     }

                     if (exactMatch != null) {
                        ((List)chatRooms).add(0, exactMatch);
                     }
                     break;
                  }
               } else {
                  ps = connSlave.prepareStatement("select chatroom.*, null as keywords from chatroom where status = ? order by datelastaccessed desc limit ?");
                  ps.setInt(1, ChatRoomData.StatusEnum.ACTIVE.value());
                  ps.setInt(2, maxChatRoomsReturned);
                  rs = ps.executeQuery();

                  label392:
                  while(true) {
                     while(true) {
                        if (!rs.next()) {
                           break label392;
                        }

                        ChatRoomData room = new ChatRoomData(rs);
                        if (room.adultOnly != null && !room.adultOnly) {
                           ((List)chatRooms).add(room);
                        } else {
                           adultRooms.add(room);
                        }
                     }
                  }
               }

               if (adultRooms.size() > 0) {
                  int chatRoomInitialPageSize = SystemProperty.getInt("ChatRoomInitialPageSize");
                  chatRoomPageSize = SystemProperty.getInt("ChatRoomPageSize");
                  int chatRoomCleanPages = SystemProperty.getInt("ChatRoomCleanPages");
                  int cleanRoomsToShow = chatRoomInitialPageSize + chatRoomPageSize * chatRoomCleanPages;
                  double adultRoomsPerPage = (double)adultRooms.size() / ((double)(((List)chatRooms).size() - cleanRoomsToShow) / (double)chatRoomPageSize);
                  double roundingRatio = adultRoomsPerPage - (double)((int)adultRoomsPerPage);
                  int i = cleanRoomsToShow;

                  while(true) {
                     if (i >= ((List)chatRooms).size() || adultRooms.size() <= 0) {
                        ((List)chatRooms).addAll(adultRooms);
                        break;
                     }

                     double noOfRooms = this.secureRandom.nextDouble() > roundingRatio ? Math.floor(adultRoomsPerPage) : Math.ceil(adultRoomsPerPage);

                     for(int j = 0; (double)j < noOfRooms && adultRooms.size() > 0; ++j) {
                        ((List)chatRooms).add(i, adultRooms.remove(0));
                     }

                     i += chatRoomPageSize;
                  }
               }
            }

            if (performingSearch) {
               ChatRoomSearch.setChatRoomSearchList(chatRoomSearchMemcache, countryID, search, language, includeAdultOnly, searchKeywords, (List)chatRooms);
            }

            var42 = chatRooms;
         } catch (Exception var39) {
            throw new EJBException(var39.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var38) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var37) {
               ps = null;
            }

            try {
               if (connSlave != null) {
                  connSlave.close();
               }
            } catch (SQLException var36) {
               connSlave = null;
            }

         }

         return (List)var42;
      }
   }

   public List<ChatRoomData> getChatRoomsV2(int countryID, String search, String language, boolean includeAdultOnly, boolean searchKeywords) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      Statement stmt = null;
      ResultSet rs = null;
      boolean performingSearch = StringUtils.hasLength(search);
      List<ChatRoomData> chatRooms = new LinkedList();
      if (performingSearch) {
         List<ChatRoomData> cachedChatRooms = ChatRoomSearch.getChatRoomSearch(chatRoomSearchMemcache, countryID, search, language, includeAdultOnly, searchKeywords);
         if (cachedChatRooms != null) {
            return cachedChatRooms;
         }
      }

      try {
         int maxChatRoomsReturned = SystemProperty.getInt("MaxChatRoomsReturned");
         if (!SystemProperty.getBool("DisableElasticSearch", false) && !SystemProperty.getBool("DisableElasticSearchQueries", false)) {
            chatRooms = ChatRoomsIndex.searchChatRooms(countryID, search, language, includeAdultOnly, searchKeywords, maxChatRoomsReturned);
         } else {
            List<ChatRoomData> adultRooms = new LinkedList();
            connSlave = this.dataSourceSlave.getConnection();
            int chatRoomPageSize;
            if (performingSearch) {
               String searchLike = search.trim().replaceAll("[\\*%]", "");
               chatRoomPageSize = SystemProperty.getInt((String)"MinChatRoomSearchLength", 0);
               if (searchLike.length() < chatRoomPageSize) {
                  throw new EJBException("Search string must have at least " + chatRoomPageSize + " characters");
               }

               searchLike = searchLike.replaceAll("_", "\\\\_") + "%";
               String retrieveChatroomID = "(select chatroom.id, chatroom.adultonly, datelastaccessed from chatroom where name like ? and status = 1 order by datelastaccessed desc limit ?) ";
               if (searchKeywords) {
                  retrieveChatroomID = retrieveChatroomID + " union all ";
                  retrieveChatroomID = retrieveChatroomID + "(select chatroom.id, chatroom.adultonly, datelastaccessed from chatroom join chatroomkeyword on chatroom.id=chatroomkeyword.chatroomid join keyword on chatroomkeyword.keywordid=keyword.id where keyword.keyword = ? and chatroom.status = 1 order by datelastaccessed desc limit ?) order by datelastaccessed desc";
               }

               ps = connSlave.prepareStatement(retrieveChatroomID);
               ps.setString(1, searchLike);
               ps.setInt(2, maxChatRoomsReturned);
               if (searchKeywords) {
                  ps.setString(3, search);
                  ps.setInt(4, maxChatRoomsReturned);
               }

               rs = ps.executeQuery();
               Set<Integer> chatroomIDSet = new LinkedHashSet();
               int chatroomIdCount = 0;

               while(rs.next() && chatroomIdCount < maxChatRoomsReturned) {
                  if (includeAdultOnly || rs.getInt(2) != 1) {
                     chatroomIDSet.add(rs.getInt(1));
                     ++chatroomIdCount;
                  }
               }

               if (chatroomIDSet.isEmpty()) {
                  List var60 = Collections.emptyList();
                  return var60;
               }

               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var50) {
                  rs = null;
               }

               try {
                  if (ps != null) {
                     ps.close();
                  }
               } catch (SQLException var49) {
                  ps = null;
               }

               String retrieveChatroomInfo = "select chatroom.*, group_concat(keyword.keyword) as keywords from chatroom left join chatroomkeyword on chatroom.id=chatroomkeyword.chatroomid left join keyword on chatroomkeyword.keywordid=keyword.id where chatroom.id in (%s) group by chatroom.id order by field(chatroom.id, %s);";
               StringBuilder sb = new StringBuilder();
               Iterator i$ = chatroomIDSet.iterator();

               while(i$.hasNext()) {
                  Integer id = (Integer)i$.next();
                  sb.append(id + ",");
               }

               String chatroomIDList = sb.substring(0, sb.length() - 1);
               retrieveChatroomInfo = String.format(retrieveChatroomInfo, chatroomIDList, chatroomIDList);
               stmt = connSlave.createStatement();
               rs = stmt.executeQuery(retrieveChatroomInfo);
               boolean languageSearch = performingSearch && StringUtils.hasLength(language);
               List<ChatRoomData> languageMatchRooms = null;
               if (languageSearch) {
                  languageMatchRooms = new LinkedList();
               }

               ChatRoomData exactMatch = null;

               while(true) {
                  while(rs.next()) {
                     ChatRoomData room = new ChatRoomData(rs);
                     if (room.name.equalsIgnoreCase(search)) {
                        exactMatch = room;
                     } else if (room.adultOnly != null && !room.adultOnly) {
                        if (languageSearch && room.language != null && room.language.equals(language)) {
                           languageMatchRooms.add(room);
                        } else {
                           ((List)chatRooms).add(room);
                        }
                     } else {
                        adultRooms.add(room);
                     }
                  }

                  if (languageSearch) {
                     ((List)chatRooms).addAll(0, languageMatchRooms);
                  }

                  if (exactMatch != null) {
                     ((List)chatRooms).add(0, exactMatch);
                  }
                  break;
               }
            } else {
               ps = connSlave.prepareStatement("select chatroom.*, null as keywords from chatroom where status = ? order by datelastaccessed desc limit ?");
               ps.setInt(1, ChatRoomData.StatusEnum.ACTIVE.value());
               ps.setInt(2, maxChatRoomsReturned);
               rs = ps.executeQuery();

               while(rs.next()) {
                  ChatRoomData room = new ChatRoomData(rs);
                  if (room.adultOnly) {
                     adultRooms.add(room);
                  } else {
                     ((List)chatRooms).add(room);
                  }
               }
            }

            if (adultRooms.size() > 0) {
               int chatRoomInitialPageSize = SystemProperty.getInt("ChatRoomInitialPageSize");
               chatRoomPageSize = SystemProperty.getInt("ChatRoomPageSize");
               int chatRoomCleanPages = SystemProperty.getInt("ChatRoomCleanPages");
               int cleanRoomsToShow = chatRoomInitialPageSize + chatRoomPageSize * chatRoomCleanPages;
               double adultRoomsPerPage = (double)adultRooms.size() / ((double)(((List)chatRooms).size() - cleanRoomsToShow) / (double)chatRoomPageSize);
               double roundingRatio = adultRoomsPerPage - (double)((int)adultRoomsPerPage);
               int i = cleanRoomsToShow;

               while(true) {
                  if (i >= ((List)chatRooms).size() || adultRooms.size() <= 0) {
                     ((List)chatRooms).addAll(adultRooms);
                     break;
                  }

                  double noOfRooms = this.secureRandom.nextDouble() > roundingRatio ? Math.floor(adultRoomsPerPage) : Math.ceil(adultRoomsPerPage);

                  for(int j = 0; (double)j < noOfRooms && adultRooms.size() > 0; ++j) {
                     ((List)chatRooms).add(i, adultRooms.remove(0));
                  }

                  i += chatRoomPageSize;
               }
            }
         }

         if (performingSearch) {
            ChatRoomSearch.setChatRoomSearchList(chatRoomSearchMemcache, countryID, search, language, includeAdultOnly, searchKeywords, (List)chatRooms);
         }

         Object var54 = chatRooms;
         return (List)var54;
      } catch (Exception var51) {
         log.error("Failed to getChatroom, due to:" + var51, var51);
         throw new EJBException(var51.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var48) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var47) {
            ps = null;
         }

         try {
            if (stmt != null) {
               stmt.close();
            }
         } catch (SQLException var46) {
            stmt = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var45) {
            connSlave = null;
         }

      }
   }

   public List<ChatRoomData> getFavouriteChatRooms(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select c.* from chatroom c, chatroombookmark b where c.name = b.chatroomname and b.username = ? order by b.datecreated desc limit ?");
         ps.setString(1, username);
         ps.setInt(2, SystemProperty.getInt("MaxChatRoomBookmarks"));
         rs = ps.executeQuery();
         LinkedList chatRooms = new LinkedList();

         while(rs.next()) {
            chatRooms.add(new ChatRoomData(rs));
         }

         LinkedList var6 = chatRooms;
         return var6;
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
      } catch (NoSuchFieldException var23) {
         throw new EJBException(var23.getMessage());
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }
   }

   public List<ChatRoomData> getRecentChatRooms(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      List var30;
      try {
         conn = this.dataSourceSlave.getConnection();
         List<String> recentChatRooms = RecentChatRoomList.getRecentChatRoomList(recentChatRoomMemcache, username);
         if (recentChatRooms == null) {
            recentChatRooms = RecentChatRoomList.newRecentChatRoomList();
            ps = conn.prepareStatement("select chatroomnames from recentchatrooms where username = ?");
            ps.setString(1, username);
            rs = ps.executeQuery();

            while(true) {
               if (!rs.next()) {
                  RecentChatRoomList.setRecentChatRoomList(recentChatRoomMemcache, username, recentChatRooms);
                  break;
               }

               String[] rooms = StringUtil.asArray(rs.getString("chatroomnames"));
               String[] arr$ = rooms;
               int len$ = rooms.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  String room = arr$[i$];
                  recentChatRooms.add(room);
               }
            }
         }

         if (!recentChatRooms.isEmpty()) {
            ps = conn.prepareStatement("select * from chatroom where status = ? and name in (" + RecentChatRoomList.asString(recentChatRooms) + ")");
            ps.setInt(1, ChatRoomData.StatusEnum.ACTIVE.value());
            rs = ps.executeQuery();
            HashMap chatRoomMap = new HashMap();

            while(rs.next()) {
               ChatRoomData chatRoomData = new ChatRoomData(rs);
               chatRoomMap.put(chatRoomData.name.toLowerCase(), chatRoomData);
            }

            List<ChatRoomData> chatRoomList = new LinkedList();
            Iterator i$ = recentChatRooms.iterator();

            while(i$.hasNext()) {
               String recentChatRoom = (String)i$.next();
               ChatRoomData chatRoomData = (ChatRoomData)chatRoomMap.get(recentChatRoom.toLowerCase());
               if (chatRoomData != null) {
                  chatRoomList.add(chatRoomData);
               }
            }

            LinkedList var35 = chatRoomList;
            return var35;
         }

         var30 = Collections.EMPTY_LIST;
      } catch (SQLException var28) {
         throw new EJBException(var28.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var25) {
            conn = null;
         }

      }

      return var30;
   }

   public int getRecentlyAccessedChatRoomCount() throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var24;
      try {
         Integer count = MemCachedClientWrapper.getInt(MemCachedKeySpaces.CommonKeySpace.RECENT_CHATROOM_COUNT, "");
         if (count == null) {
            Calendar from = Calendar.getInstance();
            from.add(13, -SystemProperty.getInt("RecentlyAccessedChatRoomInterval"));
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("select count(*) from chatroom where datelastaccessed > ?");
            ps.setTimestamp(1, new Timestamp(from.getTimeInMillis()));
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new EJBException("Unable to get recently accessed chat room count from database");
            }

            count = rs.getInt(1);
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.RECENT_CHATROOM_COUNT, "", count);
         }

         var24 = count;
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } catch (NoSuchFieldException var22) {
         throw new EJBException(var22.getMessage());
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

      return var24;
   }

   public int getActiveGroupsCount() throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var5;
      try {
         Integer count = MemCachedClientWrapper.getInt(MemCachedKeySpaces.CommonKeySpace.GROUP_COUNT, "");
         if (count == null) {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("select count(*) from groups where status=?");
            ps.setInt(1, GroupData.StatusEnum.ACTIVE.value());
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new EJBException("Unable to get groups count from database");
            }

            count = rs.getInt(1);
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.GROUP_COUNT, "", count);
         }

         var5 = count;
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
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

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

      return var5;
   }

   private void onChatRoomCreated(String creator, String chatRoomName) {
   }

   public void createChatRoom(ChatRoomData chatRoom, String keywords) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         chatRoom.name = chatRoom.name.trim();
         ChatRoomUtils.validateChatRoomNameForCreation(chatRoom.name);
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         ReputationLevelData levelData = userBean.getReputationLevel(chatRoom.creator);
         if (levelData.createChatRoom != null && levelData.createChatRoom) {
            connMaster = this.dataSourceMaster.getConnection();
            ps = connMaster.prepareStatement("select id from chatroom where name=?");
            ps.setString(1, chatRoom.name);
            rs = ps.executeQuery();
            if (!rs.next()) {
               rs.close();
               ps.close();
               boolean adultOnly = false;
               if (!StringUtil.isBlank(this.getAdultWordFilter())) {
                  String nameToCheck = " " + chatRoom.name.toLowerCase().replaceAll("[^a-z0-9\\s]", "") + " ";
                  adultOnly = nameToCheck.matches(this.adultWordFilter);
               }

               if (StringUtils.hasLength(chatRoom.description) && chatRoom.description.length() > 128) {
                  chatRoom.description = chatRoom.description.substring(0, 128);
               }

               ps = connMaster.prepareStatement("insert into ChatRoom (name, description, type, creator, adultonly, maximumsize, userowned, allowkicking, allowuserkeywords, allowbots, language, datecreated, datelastaccessed, status) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 1);
               ps.setString(1, chatRoom.name);
               ps.setString(2, StringUtils.hasLength(chatRoom.description) ? chatRoom.description : null);
               ps.setInt(3, ChatRoomData.TypeEnum.CHATROOM.value());
               ps.setString(4, chatRoom.creator.toLowerCase());
               ps.setInt(5, adultOnly ? 1 : 0);
               if (levelData.chatRoomSize == null) {
                  ps.setInt(6, SystemProperty.getInt("DefaultChatRoomSize"));
               } else {
                  ps.setInt(6, levelData.chatRoomSize);
               }

               ps.setBoolean(7, chatRoom.userOwned == null ? false : chatRoom.userOwned);
               ps.setBoolean(8, chatRoom.allowKicking == null ? true : chatRoom.allowKicking);
               ps.setBoolean(9, chatRoom.allowUserKeywords == null ? false : chatRoom.allowUserKeywords);
               ps.setBoolean(10, chatRoom.allowBots == null ? false : chatRoom.allowBots);
               ps.setString(11, StringUtils.hasLength(chatRoom.language) ? chatRoom.language : null);
               Timestamp timeNow = new Timestamp(System.currentTimeMillis());
               ps.setTimestamp(12, timeNow);
               ps.setTimestamp(13, timeNow);
               ps.setInt(14, ChatRoomData.StatusEnum.ACTIVE.value());
               int rowsUpdated = ps.executeUpdate();
               if (rowsUpdated != 1) {
                  log.warn("Unable to create chat room " + chatRoom.name + " (rowsUpdated != 1)");
                  throw new EJBException("Internal Server Error (Unable to create chat room)");
               } else {
                  rs = ps.getGeneratedKeys();
                  if (!rs.next()) {
                     log.warn("Unable to create chat room " + chatRoom.name + " (unable to obtain chat room ID)");
                     throw new EJBException("Internal Server Error (Unable to obtain chat room ID)");
                  } else {
                     chatRoom.id = rs.getInt(1);
                     rs.close();
                     ps.close();
                     if (StringUtils.hasLength(keywords)) {
                        this.addChatRoomKeywords(chatRoom.id, keywords, connMaster);
                     }

                     connMaster.close();
                     connMaster = null;

                     try {
                        this.addFavouriteChatRoom(chatRoom.creator, chatRoom.name);
                     } catch (EJBException var33) {
                     }

                     if (!SystemProperty.getBool("DisableElasticSearch", false)) {
                        Set<String> keywordSet = null;
                        if (StringUtils.hasLength(keywords)) {
                           keywordSet = new HashSet();
                           keywordSet.addAll(Arrays.asList(keywords.split(",")));
                        }

                        try {
                           ChatRoomsIndex.indexNewChatRoom(chatRoom.id, chatRoom.name, adultOnly, keywordSet);
                        } catch (Exception var32) {
                           log.error("Unable to add chat room '" + chatRoom.name + "' to ElasticSearch", var32);
                        }
                     }

                     if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.NEGATIVE_CACHE_ENABLED)) {
                        String normalizeChatRoomName = ChatRoomUtils.normalizeChatRoomName(chatRoom.name);
                        MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.CHATROOM_NEGATIVE_CACHE, normalizeChatRoomName);
                     }

                     this.onChatRoomCreated(chatRoom.creator, chatRoom.name);
                  }
               }
            } else {
               throw new EJBException("A chat room with the name " + chatRoom.name + " already exists. Please choose a different name");
            }
         } else {
            throw new EJBException(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.INSUFFICIENT_MIGLEVEL_TO_CREATE_CHATROOM_MESSAGE));
         }
      } catch (ChatRoomValidationException var34) {
         throw new EJBException(var34.getMessage(), var34);
      } catch (CreateException var35) {
         throw new EJBException(var35.getMessage());
      } catch (SQLException var36) {
         throw new EJBException(var36.getMessage());
      } catch (NoSuchFieldException var37) {
         throw new EJBException(var37.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var31) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var30) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var29) {
            connMaster = null;
         }

      }
   }

   private int getKeywordID(Connection connMaster, String keyword) throws Exception {
      PreparedStatement psGetKeyword = null;
      PreparedStatement psAddKeyword = null;
      ResultSet rs = null;
      boolean var6 = true;

      int keywordID;
      try {
         psGetKeyword = connMaster.prepareStatement("select id from keyword where keyword = ?");
         psAddKeyword = connMaster.prepareStatement("insert into keyword (keyword) values (?)", 1);
         keyword = keyword.trim().toLowerCase();
         if (keyword.length() > 64) {
            keyword = keyword.substring(0, 64);
         }

         psGetKeyword.setString(1, keyword);
         rs = psGetKeyword.executeQuery();
         if (rs.next()) {
            keywordID = rs.getInt("id");
            rs.close();
         } else {
            psAddKeyword.setString(1, keyword);
            psAddKeyword.executeUpdate();
            rs.close();
            rs = psAddKeyword.getGeneratedKeys();
            if (!rs.next()) {
               throw new Exception("Unable to add new keyword");
            }

            keywordID = rs.getInt(1);
            rs.close();
         }
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
         }

         try {
            if (psGetKeyword != null) {
               psGetKeyword.close();
            }
         } catch (SQLException var18) {
         }

         try {
            if (psAddKeyword != null) {
               psAddKeyword.close();
            }
         } catch (SQLException var17) {
         }

      }

      return keywordID;
   }

   private void addChatRoomKeywords(int chatRoomID, String keywords, Connection connMaster) {
      PreparedStatement psAddKeywordToChatRoom = null;
      Object rs = null;

      try {
         psAddKeywordToChatRoom = connMaster.prepareStatement("insert into chatroomkeyword (chatroomid, keywordid) values (?,?)");
         psAddKeywordToChatRoom.setInt(1, chatRoomID);
         Set<String> keywordSet = new HashSet();
         keywordSet.addAll(Arrays.asList(keywords.split(",")));
         Iterator i$ = keywordSet.iterator();

         while(i$.hasNext()) {
            String keyword = (String)i$.next();

            int keywordID;
            try {
               keywordID = this.getKeywordID(connMaster, keyword);
            } catch (Exception var25) {
               continue;
            }

            psAddKeywordToChatRoom.setInt(2, keywordID);
            psAddKeywordToChatRoom.executeUpdate();
         }

         return;
      } catch (SQLException var26) {
         log.warn("Unable to add keywords to the chat room with ID " + chatRoomID, var26);
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var24) {
         }

         try {
            if (psAddKeywordToChatRoom != null) {
               psAddKeywordToChatRoom.close();
            }
         } catch (SQLException var23) {
         }

      }

   }

   public void chatRoomAccessed(int chatRoomID, String chatRoomName, Integer primaryCountryID, Integer secondaryCountryID) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      if (primaryCountryID != null && primaryCountryID <= 0) {
         primaryCountryID = null;
      }

      if (secondaryCountryID != null && secondaryCountryID <= 0) {
         secondaryCountryID = null;
      }

      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("update chatroom set datelastaccessed = now(), primarycountryid = ?, secondarycountryid = ? where name = ?");
         ps.setObject(1, primaryCountryID);
         ps.setObject(2, secondaryCountryID);
         ps.setString(3, chatRoomName);
         ps.executeUpdate();
         ps.close();
         ps = null;
         connMaster.close();
         connMaster = null;
         if (!SystemProperty.getBool("DisableElasticSearch", false)) {
            try {
               ChatRoomsIndex.chatRoomAccessed(chatRoomID, chatRoomName, primaryCountryID, secondaryCountryID);
            } catch (Exception var22) {
               log.error("Unable to update chat room '" + chatRoomName + "' last accessed in ElasticSearch", var22);
            }
         }
      } catch (SQLException var23) {
         log.warn("chatRoomAccessed(" + chatRoomName + ", " + primaryCountryID + ", " + secondaryCountryID + ") threw SQLException", var23);
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var20) {
            connMaster = null;
         }

      }

   }

   public void addFavouriteChatRoom(String username, String chatRoomName) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select count(*), sum(if(chatroomname = ?, 1, 0)) from chatroombookmark where username = ?");
         ps.setString(1, chatRoomName);
         ps.setString(2, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Unable to retrieve chat room bookmarks");
         }

         if (rs.getInt(2) > 0) {
            throw new EJBException("Room " + chatRoomName + " is already in your favorite list");
         }

         int maxChatRoomBookmarks = SystemProperty.getInt("MaxChatRoomBookmarks");
         if (rs.getInt(1) >= maxChatRoomBookmarks) {
            throw new EJBException("You have reached maximum limit of " + maxChatRoomBookmarks + " favorite chat rooms");
         }

         rs.close();
         ps.close();
         ps = conn.prepareStatement("insert into chatroombookmark (username, chatroomname, datecreated) values (?,?,?)");
         ps.setString(1, username);
         ps.setString(2, chatRoomName);
         ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Unable to add new chat room bookmark");
         }
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
      } catch (NoSuchFieldException var23) {
         throw new EJBException(var23.getMessage());
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }

   }

   public void removeFavouriteChatRoom(String username, String chatRoomName) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("delete from chatroombookmark where username = ? and chatroomname = ?");
         ps.setString(1, username);
         ps.setString(2, chatRoomName);
         ps.executeUpdate();
      } catch (SQLException var17) {
         throw new EJBException(var17.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var16) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var15) {
            conn = null;
         }

      }

   }

   public void logMessageStats(Date dateOfStats, int countryID, int numPrivate, int numGroupChatSent, int numGroupChatReceived, int numChatRoomSent, int numChatRoomReceived, int numSMS, int numMSNSent, int numMSNReceived, int numYahooSent, int numYahooReceived, int numAIMSent, int numAIMReceived, int numGTalkSent, int numGTalkReceived, int numFacebookSent, int numFacebookReceived) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update MessageStats set Private=Private+?, GroupChatSent=GroupChatSent+?, GroupChatReceived=GroupChatReceived+?, ChatRoomSent=ChatRoomSent+?, ChatRoomReceived=ChatRoomReceived+?, SMS=SMS+?, MSNSent=MSNSent+?, MSNReceived=MSNReceived+?, YahooSent=YahooSent+?, YahooReceived=YahooReceived+?, AIMSent=AIMSent+?, AIMReceived=AIMReceived+?, GTalkSent=GTalkSent+?, GTalkReceived=GTalkReceived+?, FacebookSent=FacebookSent+?, FacebookReceived=FacebookReceived+? where StatsDate=? and CountryID=?");
         ps.setInt(1, numPrivate);
         ps.setInt(2, numGroupChatSent);
         ps.setInt(3, numGroupChatReceived);
         ps.setInt(4, numChatRoomSent);
         ps.setInt(5, numChatRoomReceived);
         ps.setInt(6, numSMS);
         ps.setInt(7, numMSNSent);
         ps.setInt(8, numMSNReceived);
         ps.setInt(9, numYahooSent);
         ps.setInt(10, numYahooReceived);
         ps.setInt(11, numAIMSent);
         ps.setInt(12, numAIMReceived);
         ps.setInt(13, numGTalkSent);
         ps.setInt(14, numGTalkReceived);
         ps.setInt(15, numFacebookSent);
         ps.setInt(16, numFacebookReceived);
         ps.setDate(17, new java.sql.Date(dateOfStats.getTime()));
         ps.setInt(18, countryID);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated == 0) {
            ps.close();
            ps = conn.prepareStatement("insert into MessageStats (StatsDate, CountryID, Private, GroupChatSent, GroupChatReceived, ChatRoomSent, ChatRoomReceived, SMS, MSNSent, MSNReceived, YahooSent, YahooReceived, AIMSent, AIMReceived, GTalkSent, GTalkReceived, FacebookSent, FacebookReceived) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            ps.setDate(1, new java.sql.Date(dateOfStats.getTime()));
            ps.setInt(2, countryID);
            ps.setInt(3, numPrivate);
            ps.setInt(4, numGroupChatSent);
            ps.setInt(5, numGroupChatReceived);
            ps.setInt(6, numChatRoomSent);
            ps.setInt(7, numChatRoomReceived);
            ps.setInt(8, numSMS);
            ps.setInt(9, numMSNSent);
            ps.setInt(10, numMSNReceived);
            ps.setInt(11, numYahooSent);
            ps.setInt(12, numYahooReceived);
            ps.setInt(13, numAIMSent);
            ps.setInt(14, numAIMReceived);
            ps.setInt(15, numGTalkSent);
            ps.setInt(16, numGTalkReceived);
            ps.setInt(17, numFacebookSent);
            ps.setInt(18, numFacebookReceived);
            rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0) {
               throw new EJBException("Unable to insert message stats");
            }
         }
      } catch (SQLException var33) {
         throw new EJBException(var33.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var32) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var31) {
            conn = null;
         }

      }

   }

   public void sendSystemEmail(String to, String subject, String content) throws EJBException {
      this.sendSystemEmail(to, subject, content, (UserEmailAddressData.UserEmailAddressTypeEnum)null);
   }

   public void sendSystemEmail(String to, String subject, String content, UserEmailAddressData.UserEmailAddressTypeEnum emailType) throws EJBException {
      if (log.isDebugEnabled()) {
         log.debug("Sending system email to " + to + ".\nSubject: " + subject + "\nContent: " + content + "\nType: " + emailType);
      }

      UserEmailAddressData userEmailAddressData = null;
      if (emailType != null) {
         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            int userid = userBean.getUserID(to, (Connection)null, false);
            if (userid > 0) {
               userEmailAddressData = userBean.getUserEmailAddressByType(userid, emailType);
            }
         } catch (Exception var10) {
            log.warn("Unable to find verified external email address for [" + to + "] to send system email. Using @mig33.com email instead :" + var10.getMessage());
         }
      }

      EmailUserNotification note = new EmailUserNotification();
      note.subject = subject;
      note.message = content;
      if (userEmailAddressData != null && userEmailAddressData.verified) {
         note.emailAddress = userEmailAddressData.emailAddress;

         try {
            EJBIcePrxFinder.getUserNotificationServiceProxy().notifyUserViaEmail(note);
         } catch (FusionException var9) {
            throw new EJBException(var9.message);
         }
      } else {
         try {
            EJBIcePrxFinder.getUserNotificationServiceProxy().notifyFusionUserViaEmail(to, note);
         } catch (FusionException var8) {
            throw new EJBException(var8.message);
         }
      }

   }

   public void sendEmailFromNoReply(String destinationAddress, String subject, String content) throws EJBException {
      if (log.isDebugEnabled()) {
         log.debug("Sending email from noreply to " + destinationAddress + ".\nSubject: " + subject + "\nContent: " + content);
      }

      try {
         EJBIcePrxFinder.getUserNotificationServiceProxy().sendEmailFromNoReply(destinationAddress, subject, content);
      } catch (FusionException var5) {
         throw new EJBException(var5.message);
      }
   }

   public String getUserEmailAddress(String username) throws EJBException {
      try {
         String emailAddress = username + "@" + SystemProperty.get("MailDomain");
         if (StringUtil.isValidEmail(emailAddress)) {
            return emailAddress;
         } else {
            throw new EJBException("Invalid email address");
         }
      } catch (NoSuchFieldException var3) {
         throw new EJBException(var3.getMessage());
      }
   }

   public void sendEmail(String senderUsername, String senderPassword, String to, String subject, String content) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         int maxEmailLength = SystemProperty.getInt("MaxEmailLength");
         if (to.length() > maxEmailLength || subject.length() > maxEmailLength || content.length() > maxEmailLength) {
            throw new EJBException("TO, Subject and messagebody must not be greater than " + maxEmailLength + " chars");
         }

         if (subject.contains(senderPassword)) {
            throw new EJBException("You may not send your password in the subject of an email");
         }

         if (content.contains(senderPassword)) {
            throw new EJBException("You may not send your password in the message body of an email");
         }

         List<String> recipientList = Arrays.asList(convertRecipientsToValidEmailAddresses(to.split("[,; ]")));
         List<String> supportEmailAliases = Arrays.asList(SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.SUPPORT_EMAIL_ALIASES));
         if (!Collections.disjoint(recipientList, supportEmailAliases)) {
            String rateLimitEmailContact = SystemProperty.get("RateLimitEmailContact", "10/1D");

            try {
               MemCachedRateLimiter.hit("EML_CT", senderUsername, rateLimitEmailContact);
            } catch (MemCachedRateLimiter.LimitExceeded var48) {
               throw new EJBException("You are not allowed to send more than " + String.format(var48.getPrettyMessage(), "emails to contact"), var48);
            } catch (MemCachedRateLimiter.FormatError var49) {
               throw new EJBException("Internal Error");
            }
         }

         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData userData = userBean.loadUser(senderUsername, false, false);
         if (userData == null) {
            log.error(String.format("Unable to send email - user '%s' does not exist", senderUsername));
            throw new EJBException("Invalid sender username");
         }

         if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.SEND_MIG33_EMAIL, userData)) {
            throw new EJBException("You must authenticate your account before you can send email");
         }

         Integer senderID = userData.userID;
         Integer senderCountryID = userData.countryID;
         String[] reservedAliases = SystemProperty.getArray("MailReservedAliases");
         conn = this.dataSourceSlave.getConnection();
         String sql = "select broadcastUsername from broadcastlist where username = ? and broadcastUsername in (";

         int i;
         for(i = 0; i < recipientList.size(); ++i) {
            if (i == 0) {
               sql = sql + "?";
            } else {
               sql = sql + ",?";
            }
         }

         sql = sql + ")";
         ps = conn.prepareStatement(sql);
         ps.setString(1, senderUsername);

         for(i = 0; i < recipientList.size(); ++i) {
            ps.setString(i + 2, ((String)recipientList.get(i)).substring(0, ((String)recipientList.get(i)).indexOf("@")));
         }

         rs = ps.executeQuery();
         LinkedList broadcastList = new LinkedList();

         while(rs.next()) {
            broadcastList.add(rs.getString("broadcastUsername"));
         }

         log.debug("In broadcast list: " + broadcastList);
         List<String> finalRecipientList = new LinkedList();
         Iterator i = recipientList.iterator();

         label381:
         while(i.hasNext()) {
            String recipient = (String)i.next();
            boolean onReservedList = false;
            int z = 0;

            while(true) {
               if (z < reservedAliases.length) {
                  if (!reservedAliases[z].equalsIgnoreCase(recipient.substring(0, recipient.indexOf("@")))) {
                     ++z;
                     continue;
                  }

                  onReservedList = true;
                  finalRecipientList.add(recipient);
               }

               if (onReservedList) {
                  break;
               }

               Iterator i$ = broadcastList.iterator();

               String username;
               do {
                  if (!i$.hasNext()) {
                     continue label381;
                  }

                  username = (String)i$.next();
               } while(!username.equalsIgnoreCase(recipient.substring(0, recipient.indexOf("@"))));

               finalRecipientList.add(recipient);
               break;
            }
         }

         log.debug("Final Recipient list: " + finalRecipientList);
         if (finalRecipientList.size() != recipientList.size()) {
            throw new EJBException("Please ensure that all recipients entered are on your contact list");
         }

         if (finalRecipientList.size() == 0) {
            return;
         }

         String[] recipientArray = (String[])finalRecipientList.toArray(new String[finalRecipientList.size()]);
         EmailUserNotification note = new EmailUserNotification();
         note.subject = subject;
         note.message = content;
         EJBIcePrxFinder.getUserNotificationServiceProxy().notifyUsersViaFusionEmail(senderUsername, senderPassword, recipientArray, note);

         try {
            EmailSentTrigger trigger = new EmailSentTrigger(userData);
            trigger.amountDelta = 0.0D;
            trigger.quantityDelta = 1;
            RewardCentre.getInstance().sendTrigger(trigger);
         } catch (Exception var47) {
            log.warn("Unable to notify reward system for email sent", var47);
         }
      } catch (FusionException var50) {
         throw new EJBException("There has been an error sending your mail: " + var50.message, var50);
      } catch (NullPointerException var51) {
         throw new EJBException("There has been an error sending your mail: Internal server error", var51);
      } catch (Exception var52) {
         throw new EJBException("There has been an error sending your mail: " + var52.getMessage(), var52);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var46) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var45) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var44) {
            conn = null;
         }

      }

   }

   public static String[] convertRecipientsToValidEmailAddresses(String[] rawRecipientList) throws NoSuchFieldException {
      if (rawRecipientList != null && rawRecipientList.length >= 1) {
         String[] recipientList = new String[rawRecipientList.length];
         System.arraycopy(rawRecipientList, 0, recipientList, 0, rawRecipientList.length);
         String mailDomain = "@" + SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.DEFAULT_EMAIL_DOMAIN);
         List<String> whitelistedEmailDomains = Arrays.asList(SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.WHITELISTED_RECIPIENT_EMAIL_DOMAINS));

         for(int i = 0; i < recipientList.length; ++i) {
            if (StringUtil.isBlank(recipientList[i])) {
               throw new InvalidMig33EmailRecipientEJBException("Please ensure that all recipients entered are valid migme usernames");
            }

            int indexOfAtSign = recipientList[i].indexOf("@");
            if (indexOfAtSign < 0) {
               if (!StringUtil.VALID_MIG33_USERNAME_OLD_STYLE.matcher(recipientList[i]).find()) {
                  throw new InvalidMig33EmailRecipientEJBException("Please ensure that all recipients entered are valid migme usernames");
               }

               recipientList[i] = recipientList[i] + mailDomain;
            } else {
               String recipientEmailDomain = recipientList[i].substring(indexOfAtSign + 1).trim().toLowerCase();
               if (!whitelistedEmailDomains.contains(recipientEmailDomain)) {
                  throw new InvalidMig33EmailRecipientEJBException(String.format("Invalid recipient email address provided: %s", recipientList[i]));
               }

               if (!StringUtil.isValidEmail(recipientList[i])) {
                  throw new InvalidMig33EmailRecipientEJBException(String.format("Invalid recipient email address provided: %s", recipientList[i]));
               }
            }
         }

         return recipientList;
      } else {
         return StringUtil.EMPTY_STRING_ARRAY;
      }
   }

   public void updateRoomDescriptions(String[] roomNames, String description) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update chatroom set description = ? where name in (" + StringUtil.asString(roomNames) + ")");
         ps.setString(1, description);
         ps.executeUpdate();
         String[] arr$ = roomNames;
         int len$ = roomNames.length;

         int len$;
         for(len$ = 0; len$ < len$; ++len$) {
            String roomName = arr$[len$];
            ChatRoomUtils.invalidateChatRoomCache(roomName);
         }

         ChatRoomPrx[] chatRoomProxies = EJBIcePrxFinder.findChatRoomProxies(roomNames);
         ChatRoomPrx[] arr$ = chatRoomProxies;
         len$ = chatRoomProxies.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ChatRoomPrx chatRoomPrx = arr$[i$];
            if (chatRoomPrx != null) {
               chatRoomPrx.setDescription(description);
            }
         }

      } catch (SQLException var22) {
         throw new EJBException("Failed to update room description " + var22.getMessage());
      } catch (LocalException var23) {
         throw new EJBException("Failed to update room description with objectcache " + var23.getMessage());
      } finally {
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
   }

   public String[] getGroupChatRooms(int groupId) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         List<String> chatrooms = new LinkedList();
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select name from chatroom where groupid = ? and status = ?");
         ps.setInt(1, groupId);
         ps.setInt(2, ChatRoomData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();

         while(rs.next()) {
            chatrooms.add(rs.getString("name"));
         }

         rs.close();
         String[] chatroomArr = (String[])chatrooms.toArray(new String[chatrooms.size()]);
         String[] var7 = chatroomArr;
         return var7;
      } catch (SQLException var22) {
         throw new EJBException("Failed to get GroupChatRooms: " + var22.getMessage());
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }
   }

   public Integer[] announceMessageToChatrooms(String[] chatroomNames, String message, int waitTime) throws EJBException {
      if (!SystemProperty.getBool("ChatroomAdminAnnouncementEnabled", false)) {
         throw new EJBException("Chatroom admin announcements are disabled.");
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         LinkedList chatroomsAnnounced = new LinkedList();

         try {
            List<String> chatrooms = new LinkedList();
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("SELECT name FROM chatroom WHERE name IN (" + StringUtil.asString(chatroomNames) + ") " + "AND status = ?");
            ps.setInt(1, ChatRoomData.StatusEnum.ACTIVE.value());
            rs = ps.executeQuery();

            while(rs.next()) {
               chatrooms.add(rs.getString("name"));
            }

            rs.close();
            if (chatrooms.size() <= 0) {
               return (Integer[])chatroomsAnnounced.toArray(new Integer[chatroomsAnnounced.size()]);
            } else {
               ChatRoomPrx[] chatRoomProxies = EJBIcePrxFinder.findChatRoomProxies((String[])chatrooms.toArray(new String[chatrooms.size()]));
               ChatRoomPrx[] arr$ = chatRoomProxies;
               int len$ = chatRoomProxies.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  ChatRoomPrx chatRoomPrx = arr$[i$];
                  if (chatRoomPrx != null) {
                     log.info("Sending announcement to [" + chatRoomPrx.getRoomData().name + "] :: " + message);
                     chatRoomPrx.adminAnnounce(message, waitTime);
                     chatroomsAnnounced.add(chatRoomPrx.getRoomData().id);
                  }
               }

               return (Integer[])chatroomsAnnounced.toArray(new Integer[chatroomsAnnounced.size()]);
            }
         } catch (SQLException var30) {
            throw new EJBException("Failed to broadcast message: " + var30.getMessage());
         } catch (LocalException var31) {
            throw new EJBException("Failed to broadcast message to chat rooms: " + var31.getMessage());
         } catch (FusionException var32) {
            throw new EJBException("Failed to broadcast message to chat rooms: " + var32.getMessage());
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
      }
   }

   public Integer[] announceMessageToUserOwnedChatrooms(String message, int waitTime) throws EJBException {
      if (!SystemProperty.getBool("ChatroomAdminAnnouncementEnabled", false)) {
         throw new EJBException("Chatroom admin announcements are disabled.");
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         LinkedList chatroomsAnnounced = new LinkedList();

         try {
            List<String> chatrooms = new LinkedList();
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("SELECT name FROM chatroom WHERE status = ? AND creator IS NOT NULL AND groupid IS NULL AND type = 1 AND userowned = 1");
            ps.setInt(1, ChatRoomData.StatusEnum.ACTIVE.value());
            rs = ps.executeQuery();

            while(rs.next()) {
               log.error(rs.getString("name"));
               chatrooms.add(rs.getString("name"));
            }

            rs.close();
            if (chatrooms.size() <= 0) {
               return (Integer[])chatroomsAnnounced.toArray(new Integer[chatroomsAnnounced.size()]);
            } else {
               ChatRoomPrx[] chatRoomProxies = EJBIcePrxFinder.findChatRoomProxies((String[])chatrooms.toArray(new String[chatrooms.size()]));
               ChatRoomPrx[] arr$ = chatRoomProxies;
               int len$ = chatRoomProxies.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  ChatRoomPrx chatRoomPrx = arr$[i$];
                  if (chatRoomPrx != null) {
                     log.info("Sending announcement to [" + chatRoomPrx.getRoomData().name + "] :: " + message);
                     chatRoomPrx.adminAnnounce(message, waitTime);
                     chatroomsAnnounced.add(chatRoomPrx.getRoomData().id);
                  }
               }

               return (Integer[])chatroomsAnnounced.toArray(new Integer[chatroomsAnnounced.size()]);
            }
         } catch (SQLException var29) {
            throw new EJBException("Failed to announce message: " + var29.getMessage());
         } catch (LocalException var30) {
            log.error(var30, var30);
            throw new EJBException("Failed to announce message to chat rooms: " + var30.getMessage());
         } catch (FusionException var31) {
            throw new EJBException("Failed to broadcast message to chat rooms: " + var31.getMessage());
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
      }
   }

   public void updateRoomDescription(String roomName, String description) throws EJBException {
      this.updateRoomDescriptions(new String[]{roomName}, description);
   }

   public void sendChangeRoomOwnerEmail(String oldOwner, String roomName, String newOwner) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData newOwnerData = userBean.loadUser(newOwner, false, false);
         if (newOwnerData == null) {
            throw new EJBException("User '" + newOwner + "' does not exist");
         }

         if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.CHANGE_ROOM_OWNER_EMAIL, newOwnerData)) {
            throw new EJBException("You cannot change room ownership to a non-authenticated user");
         }

         ps = connMaster.prepareStatement("select c.newowner, u.password from chatroom c, user u where c.creator=u.username and c.name=? and c.creator=? and c.userowned=1");
         ps.setString(1, roomName);
         ps.setString(2, oldOwner);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("You cannot change room ownership unless you own the room");
         }

         if (newOwner.equals(rs.getString("newOwner"))) {
            return;
         }

         String oldOwnerPassword = rs.getString("password");
         rs.close();
         ps.close();
         ps = connMaster.prepareStatement("update chatroom set newowner = ? where name = ?");
         ps.setString(1, newOwner);
         ps.setString(2, roomName);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Unable to update new owner for chat room " + roomName);
         }

         ChatRoomUtils.invalidateChatRoomCache(roomName);
         String email = SystemProperty.get("ChatRoomOwnershipChangeEmail").replaceAll("%OldOwner", oldOwner).replaceAll("%RoomName", roomName);
         this.sendEmail(oldOwner, oldOwnerPassword, newOwner, "Chat room ownership change", email);
      } catch (CreateException var30) {
         throw new EJBException("Failed to send room ownership change email. " + var30.getMessage());
      } catch (SQLException var31) {
         throw new EJBException("Failed to send room ownership change email. " + var31.getMessage());
      } catch (NoSuchFieldException var32) {
         throw new EJBException("Failed to send room ownership change email. " + var32.getMessage());
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
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var27) {
            connMaster = null;
         }

      }

   }

   public void changeRoomOwner(String oldOwner, String roomName, String newOwner) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select newowner from chatroom where name=? and creator=? and userowned=1");
         ps.setString(1, roomName);
         ps.setString(2, oldOwner);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("You cannot change room ownership unless you own the room");
         }

         if (!newOwner.equals(rs.getString("newOwner"))) {
            throw new EJBException(newOwner + " is not the newly appointed owner of the room");
         }

         rs.close();
         ps.close();
         ps = connMaster.prepareStatement("update chatroom set creator = ?, newowner = null where name = ?");
         ps.setString(1, newOwner);
         ps.setString(2, roomName);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated == 1) {
            ChatRoomUtils.invalidateChatRoomCache(roomName);
            ChatRoomPrx chatRoomPrx = EJBIcePrxFinder.findChatRoomPrx(roomName);
            if (chatRoomPrx != null) {
               chatRoomPrx.changeOwner(oldOwner, newOwner);
               chatRoomPrx.putSystemMessage(newOwner + " is the new owner of this room", (String[])null);
            }
         }
      } catch (SQLException var23) {
         throw new EJBException("Failed to change room owner " + var23.getMessage());
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
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var20) {
            connMaster = null;
         }

      }

   }

   public void addRoomModerator(String ownerUsername, String chatRoomName, String moderatorUsername) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select id from chatroom where name=? and creator=? and userowned=1");
         ps.setString(1, chatRoomName);
         ps.setString(2, ownerUsername);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("You cannot add moderators to a room unless you own the room");
         }

         rs.close();
         ps.close();
         ps = connMaster.prepareStatement("insert ignore into chatroommoderator select chatroom.id, user.username from chatroom, user where chatroom.name=? and user.username=?");
         ps.setString(1, chatRoomName);
         ps.setString(2, moderatorUsername.toLowerCase());
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated == 1) {
            MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.CHATROOM_MODERATORS, chatRoomName);
            ChatRoomPrx chatRoomPrx = EJBIcePrxFinder.findChatRoomPrx(chatRoomName);
            if (chatRoomPrx != null) {
               chatRoomPrx.addModerator(moderatorUsername);
               chatRoomPrx.putSystemMessage(moderatorUsername + " is now a moderator of this room", (String[])null);
            }
         }

         String subject = "Chat room " + chatRoomName + ": Moderator " + moderatorUsername + " added";
         String body = "Your chat room " + chatRoomName + " has been modified.\n\n";
         body = body + "The user " + moderatorUsername + " is now a moderator of the room.\n\n";
         body = body + "If this was done in error or you want to change this please go to your room and select Room Settings from the menu.";
         this.sendSystemEmail(ownerUsername, subject, body);
      } catch (SQLException var25) {
         log.error("SQLException in addRoomModerator()", var25);
         throw new EJBException("Unable to add moderator");
      } catch (LocalException var26) {
         log.error("Ice.LocalException in addRoomModerator()", var26);
         throw new EJBException("Unable to add moderator");
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
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var22) {
            connMaster = null;
         }

      }

   }

   public void resetRoomModerators(String chatRoomName) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("delete chatroommoderator from chatroommoderator inner join chatroom on chatroommoderator.chatroomid=chatroom.id where chatroom.name=?");
         ps.setString(1, chatRoomName);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated == 1) {
            MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.CHATROOM_MODERATORS, chatRoomName);
         }
      } catch (SQLException var20) {
         log.error("SQLException in resetRoomModerators()", var20);
         throw new EJBException("Unable to reset moderators");
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
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

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var17) {
            connMaster = null;
         }

      }

   }

   public void removeRoomModerator(String ownerUsername, String chatRoomName, String moderatorUsername) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select id from chatroom where name=? and creator=? and userowned=1");
         ps.setString(1, chatRoomName);
         ps.setString(2, ownerUsername);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("You cannot remove moderators from a room unless you own the room");
         }

         rs.close();
         ps.close();
         ps = connMaster.prepareStatement("delete chatroommoderator from chatroommoderator inner join chatroom on chatroommoderator.chatroomid=chatroom.id where chatroommoderator.username=? and chatroom.name=?");
         ps.setString(1, moderatorUsername);
         ps.setString(2, chatRoomName);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated == 1) {
            MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.CHATROOM_MODERATORS, chatRoomName);
            ChatRoomPrx chatRoomPrx = EJBIcePrxFinder.findChatRoomPrx(chatRoomName);
            if (chatRoomPrx != null) {
               chatRoomPrx.removeModerator(moderatorUsername);
               chatRoomPrx.putSystemMessage(moderatorUsername + " is no longer a moderator of this room", (String[])null);
            }
         }

         String subject = "Chat room " + chatRoomName + ": Moderator " + moderatorUsername + " removed";
         String body = "Your chat room " + chatRoomName + " has been modified.\n\n";
         body = body + "The user " + moderatorUsername + " is no longer a moderator of the room.\n\n";
         body = body + "If this was done in error or you want to change this please go to your room and select Room Settings from the menu.";
         this.sendSystemEmail(ownerUsername, subject, body);
      } catch (SQLException var25) {
         log.error("SQLException in removeRoomModerator()", var25);
         throw new EJBException("Unable to remove moderator");
      } catch (LocalException var26) {
         log.error("Ice.LocalException in removeRoomModerator()", var26);
         throw new EJBException("Unable to remove moderator");
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
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var22) {
            connMaster = null;
         }

      }

   }

   public void banGroupMember(String bannedByUsername, GroupData groupData, String bannedUsername) throws EJBException {
      Connection connMaster = null;
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         bannedByUsername = bannedByUsername.toLowerCase();
         bannedUsername = bannedUsername.toLowerCase();
         boolean instigatorIsGroupAdmin = false;
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select chatroomadmin from user where username = ?");
         ps.setString(1, bannedUsername);
         rs = ps.executeQuery();
         if (!rs.next()) {
            log.warn(bannedByUsername + " is trying to ban a non-existant user: " + bannedUsername);
            throw new EJBException("Unable to ban user " + bannedUsername);
         }

         if (rs.getBoolean("chatroomadmin")) {
            throw new EJBException("You cannot ban an admin");
         }

         rs.close();
         ps.close();
         ps = connSlave.prepareStatement("select * from groupmember  where username = ? and groupid = ? and status = ?");
         ps.setString(1, bannedByUsername);
         ps.setInt(2, groupData.id);
         ps.setInt(3, GroupMemberData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("You cannot ban a user from the group unless you are an admin or moderator");
         }

         int type = rs.getInt("type");
         if (type != GroupMemberData.TypeEnum.ADMINISTRATOR.value() && type != GroupMemberData.TypeEnum.MODERATOR.value()) {
            throw new EJBException("You cannot ban a user from the group unless you are an admin or moderator");
         }

         if (type == GroupMemberData.TypeEnum.ADMINISTRATOR.value()) {
            instigatorIsGroupAdmin = true;
         }

         rs.close();
         ps.close();
         ps = connSlave.prepareStatement("select * from groupmember  where username = ? and groupid = ? and status in (?, ?)");
         ps.setString(1, bannedUsername);
         ps.setInt(2, groupData.id);
         ps.setInt(3, GroupMemberData.StatusEnum.ACTIVE.value());
         ps.setInt(4, GroupMemberData.StatusEnum.BANNED.value());
         rs = ps.executeQuery();
         connMaster = this.dataSourceMaster.getConnection();
         if (!rs.next()) {
            if (!groupData.isOpenGroup()) {
               log.error(bannedUsername + " is not part of this group");
               throw new EJBException(bannedUsername + " is not part of this group");
            }

            ps = connMaster.prepareStatement("insert ignore into groupblacklist values (?, ?, ?)");
            ps.setInt(1, groupData.id);
            ps.setString(2, bannedUsername);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            ps.close();
            return;
         }

         type = rs.getInt("type");
         int status = rs.getInt("status");
         if (GroupMemberData.StatusEnum.BANNED.value() != status) {
            if (type != GroupMemberData.TypeEnum.ADMINISTRATOR.value() && (type != GroupMemberData.TypeEnum.MODERATOR.value() || instigatorIsGroupAdmin)) {
               StringBuilder updateQuery = new StringBuilder("update groupmember set status = ?");
               if (type == GroupMemberData.TypeEnum.MODERATOR.value() && instigatorIsGroupAdmin) {
                  updateQuery.append(", type = " + GroupMemberData.TypeEnum.REGULAR.value());
               }

               updateQuery.append(" where groupid = ? and username = ? and status = ?");
               ps = connMaster.prepareStatement(updateQuery.toString());
               ps.setInt(1, GroupMemberData.StatusEnum.BANNED.value());
               ps.setInt(2, groupData.id);
               ps.setString(3, bannedUsername);
               ps.setInt(4, GroupMemberData.StatusEnum.ACTIVE.value());
               int count = ps.executeUpdate();
               ps.close();
               if (count > 0) {
                  ps = connMaster.prepareStatement("update groups set nummembers=nummembers-? where id=?");
                  ps.setInt(1, count);
                  ps.setInt(2, groupData.id);
                  ps.executeUpdate();
                  ps.close();
               }

               return;
            }

            throw new EJBException("You cannot ban an group admin or moderator");
         }
      } catch (SQLException var34) {
         log.error("SQLException in banGroupMember()", var34);
         throw new EJBException("Unable to ban group member");
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var33) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var32) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var31) {
            connMaster = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var30) {
            connSlave = null;
         }

      }

   }

   public void unbanGroupMember(String unbannedByUsername, GroupData groupData, String unbannedUsername) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         unbannedByUsername = unbannedByUsername.toLowerCase();
         unbannedUsername = unbannedUsername.toLowerCase();
         connMaster = this.dataSourceMaster.getConnection();
         String sql = "select * from groupmember  where username = ? and groupid = ? and status = ?";
         ps = connMaster.prepareStatement(sql);
         ps.setString(1, unbannedByUsername);
         ps.setInt(2, groupData.id);
         ps.setInt(3, GroupMemberData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("You cannot unban a user from the group unless you are an admin or moderator");
         }

         int count = rs.getInt("type");
         if (count != GroupMemberData.TypeEnum.ADMINISTRATOR.value() && count != GroupMemberData.TypeEnum.MODERATOR.value()) {
            throw new EJBException("You cannot unban a user from the group unless you are an admin or moderator");
         }

         rs.close();
         ps.setString(1, unbannedUsername);
         ps.setInt(2, groupData.id);
         ps.setInt(3, GroupMemberData.StatusEnum.BANNED.value());
         rs = ps.executeQuery();
         if (!rs.next()) {
            if (!groupData.isOpenGroup()) {
               throw new EJBException(unbannedUsername + " is either active or not a member of this group");
            }

            ps = connMaster.prepareStatement("delete from groupblacklist where groupid = ? and username = ?");
            ps.setInt(1, groupData.id);
            ps.setString(2, unbannedUsername);
            ps.executeUpdate();
            ps.close();
         } else {
            ps = connMaster.prepareStatement("update groupmember set status = ? where groupid = ? and username = ?");
            ps.setInt(1, GroupMemberData.StatusEnum.ACTIVE.value());
            ps.setInt(2, groupData.id);
            ps.setString(3, unbannedUsername);
            count = ps.executeUpdate();
            ps.close();
            ps = connMaster.prepareStatement("update groups set nummembers=nummembers+? where id=?");
            ps.setInt(1, count);
            ps.setInt(2, groupData.id);
            ps.executeUpdate();
            ps.close();
         }

         rs.close();
      } catch (SQLException var23) {
         log.error("SQLException in unbanGroupMember()", var23);
         throw new EJBException("Unable to ban group member");
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
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var20) {
            connMaster = null;
         }

      }

   }

   public boolean isUserBlackListedInGroup(String username, int groupId) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select * from groupblacklist where groupid = ? and username = ?");
         ps.setInt(1, groupId);
         ps.setString(2, username);
         rs = ps.executeQuery();
         if (rs.next()) {
            boolean var6 = true;
            return var6;
         }
      } catch (SQLException var24) {
         throw new EJBException(var24);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var21) {
            connSlave = null;
         }

      }

      return false;
   }

   public boolean isModeratorOfChatRoom(String userName, String chatRoomName) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select username from chatroom, chatroommoderator where chatroom.name=? and chatroom.id=chatroommoderator.chatroomid and chatroommoderator.username=?");
         ps.setString(1, chatRoomName);
         ps.setString(2, userName);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var6 = false;
            return var6;
         }

         var6 = true;
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

   public void addChatRoomEmoteLog(ChatRoomEmoteLogData data) throws EJBException {
      if (data == null) {
         throw new EJBException("ChatRoomEmoteLogData is null");
      } else {
         Connection connMaster = null;
         PreparedStatement ps = null;

         try {
            connMaster = this.dataSourceMaster.getConnection();
            String sql = "insert into chatroomemotelog (instigator, target, emote, chatroomid, groupid, reasoncode, datecreated, parameters) values (?, ?, ?, ?, ?, ?, ?, ?)";
            ps = connMaster.prepareStatement(sql);
            ps.setString(1, data.getInstigator());
            ps.setString(2, data.getTarget());
            ps.setString(3, data.getEmote());
            ps.setInt(4, data.getChatroomId());
            if (data.getGroupId() > 0) {
               ps.setInt(5, data.getGroupId());
            } else {
               ps.setNull(5, 4);
            }

            if (data.getReasonCode() > 0) {
               ps.setInt(6, data.getReasonCode());
            } else {
               ps.setNull(6, 4);
            }

            ps.setTimestamp(7, new Timestamp(data.getDateCreated().getTime()));
            ps.setString(8, data.getParameters());
            ps.executeUpdate();
         } catch (SQLException var16) {
            throw new EJBException(var16.getMessage());
         } finally {
            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var15) {
               ps = null;
            }

            try {
               if (connMaster != null) {
                  connMaster.close();
               }
            } catch (SQLException var14) {
               connMaster = null;
            }

         }

      }
   }

   public void banUserFromRoom(String bannedByUsername, String chatRoomName, String bannedUsername) throws EJBException {
      boolean modifiedByModerator = false;

      try {
         bannedByUsername = bannedByUsername.toLowerCase();
         bannedUsername = bannedUsername.toLowerCase();

         String ownerUsername;
         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData targetData = userEJB.loadUser(bannedUsername, false, false);
            if (null == targetData) {
               throw new EJBException("User doesn't exist");
            }

            ChatRoomData roomData = this.getChatRoom(chatRoomName);
            if (null == roomData) {
               throw new EJBException("Room doesn't exist");
            }

            ownerUsername = roomData.creator;
            if (!bannedByUsername.equals(ownerUsername) && !roomData.moderators.contains(bannedByUsername)) {
               throw new EJBException("You do not have sufficient rights to ban in this room");
            }

            if (roomData.creator.equals(bannedUsername) || targetData.chatRoomAdmin || roomData.moderators.contains(bannedUsername)) {
               throw new EJBException("You do not have sufficient rights to ban the user " + bannedUsername);
            }
         } catch (CreateException var9) {
            throw new EJBException("EJBCreation error in banUserFromRoom");
         }

         if (this.updateChatroomBannedList(chatRoomName, bannedUsername)) {
            ChatRoomPrx chatRoomPrx = EJBIcePrxFinder.findChatRoomPrx(chatRoomName);
            if (chatRoomPrx != null) {
               chatRoomPrx.banUser(bannedUsername);
               chatRoomPrx.putSystemMessage(bannedUsername + " has been banned from this room by " + bannedByUsername, (String[])null);
            }
         }

         if (SystemProperty.getBool("ChatroomBanEmailEnabled", true)) {
            String subject = "Chat room " + chatRoomName + ": User " + bannedUsername + " banned";
            String body = "Your chat room " + chatRoomName + " has been modified.\n\n";
            if (modifiedByModerator) {
               body = body + "This change was made by the moderator " + bannedByUsername + "\n\n";
            }

            body = body + "The user " + bannedUsername + " has been banned from the room.\n\n";
            body = body + "If this was done in error or you want to change this please go to your room and select Room Settings from the menu.";
            this.sendSystemEmail(ownerUsername, subject, body);
         }

      } catch (LocalException var10) {
         log.error("Ice.LocalException in banUserFromRoom()", var10);
         throw new EJBException("Unable to ban user");
      }
   }

   public boolean updateChatroomBannedList(String chatRoomName, String bannedUsername) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("insert ignore into chatroombanneduser select chatroom.id, user.username, ? from chatroom, user where chatroom.name=? and user.username=?");
         ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
         ps.setString(2, chatRoomName);
         ps.setString(3, bannedUsername.toLowerCase());
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated == 1) {
            MemCachedClientWrapper.deletePaged(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BANNED_USERS, chatRoomName);
            boolean var6 = true;
            return var6;
         }
      } catch (SQLException var20) {
         log.error("SQLException in banUserFromRoom()", var20);
         throw new EJBException("Unable to ban user");
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var18) {
            connMaster = null;
         }

      }

      return false;
   }

   public void unbanUserFromRoom(String unbannedByUsername, String chatRoomName, String bannedUsername) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      boolean modifiedByModerator = false;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         String sql = "SELECT id, creator, NULL AS moderatorusername FROM chatroom WHERE NAME=? AND creator=? AND userowned=1 UNION SELECT chatroom.id, chatroom.creator, chatroommoderator.username AS moderatorusername FROM chatroom, chatroommoderator WHERE chatroom.name=? AND chatroom.id=chatroommoderator.chatroomid AND chatroommoderator.username=?";
         ps = connMaster.prepareStatement(sql);
         ps.setString(1, chatRoomName);
         ps.setString(2, unbannedByUsername);
         ps.setString(3, chatRoomName);
         ps.setString(4, unbannedByUsername);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("You cannot unban a user from the room unless you are an admin or moderator");
         }

         if (StringUtils.hasLength(rs.getString("moderatorusername"))) {
            modifiedByModerator = true;
         }

         String ownerUsername = rs.getString("creator");
         rs.close();
         ps.close();
         ps = connMaster.prepareStatement("delete chatroombanneduser from chatroombanneduser inner join chatroom on chatroombanneduser.chatroomid=chatroom.id where chatroombanneduser.username=? and chatroom.name=?");
         ps.setString(1, bannedUsername);
         ps.setString(2, chatRoomName);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated == 1) {
            MemCachedClientWrapper.deletePaged(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BANNED_USERS, chatRoomName);
            ChatRoomPrx chatRoomPrx = EJBIcePrxFinder.findChatRoomPrx(chatRoomName);
            if (chatRoomPrx != null) {
               chatRoomPrx.unbanUser(bannedUsername);
               chatRoomPrx.putSystemMessage(bannedUsername + " is no longer banned from this room (unbanned by " + unbannedByUsername + ")", (String[])null);
            }
         }

         String subject = "Chat room " + chatRoomName + ": User " + bannedUsername + " no longer banned";
         String body = "Your chat room " + chatRoomName + " has been modified.\n\n";
         if (modifiedByModerator) {
            body = body + "This change was made by the moderator " + unbannedByUsername + "\n\n";
         }

         body = body + "The user " + bannedUsername + " is no longer banned from the room.\n\n";
         body = body + "If this was done in error or you want to change this please go to your room and select Room Settings from the menu.";
         this.sendSystemEmail(ownerUsername, subject, body);
      } catch (SQLException var28) {
         log.error("SQLException in unbanUserFromRoom()", var28);
         throw new EJBException("Unable to unban user");
      } catch (LocalException var29) {
         log.error("Ice.LocalException in unbanUserFromRoom()", var29);
         throw new EJBException("Unable to unban user");
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var25) {
            connMaster = null;
         }

      }

   }

   public List<BotData> getBots() throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from bot where status = 1");
         rs = ps.executeQuery();
         LinkedList bots = new LinkedList();

         while(rs.next()) {
            bots.add(new BotData(rs));
         }

         LinkedList var5 = bots;
         return var5;
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
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

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }
   }

   public BotData getBot(int id) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      BotData var5;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from bot where id = ? and status = 1");
         ps.setInt(1, id);
         rs = ps.executeQuery();
         var5 = rs.next() ? new BotData(rs) : null;
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
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

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

      return var5;
   }

   public BotData getBotFromCommandName(String commandName) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      BotData var5;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from bot where commandname = ? and status = 1");
         ps.setString(1, commandName);
         rs = ps.executeQuery();
         var5 = rs.next() ? new BotData(rs) : null;
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
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

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

      return var5;
   }

   public EmailTemplateData getEmailTemplateData(int emailTemplateID) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      EmailTemplateData var7;
      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = " select  t.id as id, t.name as name, t.templatetype as templatetype, t.subjectTemplate as subjecttemplate,  t.bodyTemplate as bodytemplate,  t.mimeType as mimetype,  p.id as partid,  p.templateid as parenttemplateid,  p.sequence as partsequence,  p.contentTemplate as partcontenttemplate, p.mimeType as partmimetype  from emailtemplate t left join emailtemplatepart p on (t.id = p.templateid)  where t.id = ?  order by p.sequence desc;";
         ps = conn.prepareStatement(" select  t.id as id, t.name as name, t.templatetype as templatetype, t.subjectTemplate as subjecttemplate,  t.bodyTemplate as bodytemplate,  t.mimeType as mimetype,  p.id as partid,  p.templateid as parenttemplateid,  p.sequence as partsequence,  p.contentTemplate as partcontenttemplate, p.mimeType as partmimetype  from emailtemplate t left join emailtemplatepart p on (t.id = p.templateid)  where t.id = ?  order by p.sequence desc;");
         ps.setInt(1, emailTemplateID);
         rs = ps.executeQuery();
         EmailTemplateData result;
         if (!rs.next()) {
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.EMAIL_TEMPLATE, Integer.toString(emailTemplateID), (Object)null);
            result = null;
            return result;
         }

         result = new EmailTemplateData(rs);
         if (result.addPartTemplate(rs)) {
            while(rs.next()) {
               result.addPartTemplate(rs);
            }
         }

         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.EMAIL_TEMPLATE, Integer.toString(emailTemplateID), result);
         var7 = result;
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

      return var7;
   }

   public void setChatRoomMaxSize(String chatRoomName, int maxSize) throws FusionEJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("update chatroom set MaximumSize=? where chatroom.name=?");
         ps.setInt(1, maxSize);
         ps.setString(2, chatRoomName);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated != 1) {
            String err = "Failed to update maximum size of chatroom=" + chatRoomName + " update modified zero rows";
            log.error(err);
            throw new FusionEJBException(err);
         }

         ChatRoomPrx chatRoomProxy = EJBIcePrxFinder.findChatRoomPrx(chatRoomName);
         if (chatRoomProxy != null) {
            chatRoomProxy.setMaximumSize(maxSize);
         }
      } catch (FusionEJBException var19) {
         throw var19;
      } catch (Exception var20) {
         log.error("Exception in setChatRoomMaximumSize() for room=" + chatRoomName + ": " + var20, var20);
         throw new FusionEJBException("Unable to set maximum size of chatroom=" + chatRoomName, var20);
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var17) {
            connMaster = null;
         }

      }

   }

   static {
      recentChatRoomMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.recentChatRooms);
      chatRoomSearchMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.chatRoomSearch);
   }
}
