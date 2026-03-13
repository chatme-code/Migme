package com.projectgoth.fusion.ejb;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.cache.GiftsReceivedCounter;
import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.cache.RecentChatRoomList;
import com.projectgoth.fusion.clientsession.SSOLogin;
import com.projectgoth.fusion.clientsession.SSOLoginData;
import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.ExceptionHelper;
import com.projectgoth.fusion.common.FloodControl;
import com.projectgoth.fusion.common.HashObjectUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.common.Numerics;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.WebCommon;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.AffiliateData;
import com.projectgoth.fusion.data.BankTransferIntentData;
import com.projectgoth.fusion.data.BlueLabelVoucherData;
import com.projectgoth.fusion.data.BotData;
import com.projectgoth.fusion.data.CallData;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.data.ContentData;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.CreditCardPaymentData;
import com.projectgoth.fusion.data.CreditTransferData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.DiscountTierData;
import com.projectgoth.fusion.data.EmoticonData;
import com.projectgoth.fusion.data.EmoticonPackData;
import com.projectgoth.fusion.data.ExternalDownloadLinkData;
import com.projectgoth.fusion.data.FileData;
import com.projectgoth.fusion.data.GroupAnnouncementData;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.GroupEventData;
import com.projectgoth.fusion.data.GroupInvitationData;
import com.projectgoth.fusion.data.GroupMemberData;
import com.projectgoth.fusion.data.GroupModuleData;
import com.projectgoth.fusion.data.GroupPostData;
import com.projectgoth.fusion.data.HandsetData;
import com.projectgoth.fusion.data.HandsetVendorPrefixesData;
import com.projectgoth.fusion.data.MerchantTagData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.data.MobileOriginatedSMSData;
import com.projectgoth.fusion.data.MoneyTransferData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.ScrapbookData;
import com.projectgoth.fusion.data.StoreItemData;
import com.projectgoth.fusion.data.SubscriptionData;
import com.projectgoth.fusion.data.SystemSMSData;
import com.projectgoth.fusion.data.ThemeData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserPostData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.data.UserRegistrationContextData;
import com.projectgoth.fusion.data.UserSettingData;
import com.projectgoth.fusion.data.VirtualGiftData;
import com.projectgoth.fusion.data.VoucherData;
import com.projectgoth.fusion.ejb.util.LookupUtil;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.gateway.packet.FusionPacketFactory;
import com.projectgoth.fusion.gateway.packet.FusionPktRecharge;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import com.projectgoth.fusion.interfaces.ContactLocal;
import com.projectgoth.fusion.interfaces.ContactLocalHome;
import com.projectgoth.fusion.interfaces.ContentLocal;
import com.projectgoth.fusion.interfaces.ContentLocalHome;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.interfaces.MessageLocal;
import com.projectgoth.fusion.interfaces.MessageLocalHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.interfaces.VoiceLocal;
import com.projectgoth.fusion.interfaces.VoiceLocalHome;
import com.projectgoth.fusion.interfaces.VoucherLocal;
import com.projectgoth.fusion.interfaces.VoucherLocalHome;
import com.projectgoth.fusion.interfaces.WebLocal;
import com.projectgoth.fusion.interfaces.WebLocalHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.paintwars.ItemData;
import com.projectgoth.fusion.paintwars.Painter;
import com.projectgoth.fusion.paintwars.PainterStats;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import com.projectgoth.fusion.restapi.enums.RegistrationType;
import com.projectgoth.fusion.slice.AuthenticationServiceCredentialResponse;
import com.projectgoth.fusion.slice.AuthenticationServicePrx;
import com.projectgoth.fusion.slice.AuthenticationServiceResponseCodeEnum;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.ContactDataIce;
import com.projectgoth.fusion.slice.EmailUserNotification;
import com.projectgoth.fusion.slice.EventSystemPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.Message;
import com.projectgoth.fusion.slice.SMSUserNotification;
import com.projectgoth.fusion.slice.UserEventIce;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.smsengine.SMSControl;
import com.projectgoth.fusion.userevent.EventTextTranslator;
import com.projectgoth.fusion.userevent.domain.UserEvent;
import com.sun.rowset.CachedRowSetImpl;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

public class WebBean implements SessionBean {
   private static final Logger log = Logger.getLogger(WebBean.class);
   private DataSource dataSourceMaster;
   private DataSource dataSourceSlave;
   private static MemCachedClient recentChatRoomMemcache;
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
         this.dataSourceMaster = LookupUtil.getFusionMasterDataSource();
         this.dataSourceSlave = LookupUtil.getFusionSlaveDataSource();
         SystemProperty.ejbInit(this.dataSourceSlave);
      } catch (Exception var2) {
         log.error("Unable to create Web EJB", var2);
         throw new CreateException("Unable to create Web EJB: " + var2.getMessage());
      }
   }

   public Vector getCountries() {
      Vector countries = new Vector();
      List countrylist = null;

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         countrylist = misBean.getCountries();
         Iterator i$ = countrylist.iterator();

         while(i$.hasNext()) {
            CountryData country = (CountryData)i$.next();
            Hashtable countryHash = new Hashtable();
            countryHash.put("id", String.valueOf(country.id));
            countryHash.put("iddCode", String.valueOf(country.iddCode));
            if (country.isoCountryCode != null && country.isoCountryCode.length() > 0) {
               countryHash.put("isoCountryCode", country.isoCountryCode);
            }

            countryHash.put("name", country.name);
            countryHash.put("currency", country.currency);
            countryHash.put("creditCardCurrency", country.creditCardCurrency);
            countryHash.put("bankTransferCurrency", country.bankTransferCurrency);
            countryHash.put("westernUnionCurrency", country.westernUnionCurrency);
            countryHash.put("allowCreditCard", country.allowCreditCard.value());
            countries.add(countryHash);
         }

         return countries;
      } catch (Exception var7) {
         return ExceptionHelper.getRootMessageAsVector(var7);
      }
   }

   public Vector getCurrencies() {
      Vector currencies = new Vector();
      List currencyList = null;

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         currencyList = misBean.getCurrencies();
         Iterator i$ = currencyList.iterator();

         while(i$.hasNext()) {
            CurrencyData currency = (CurrencyData)i$.next();
            Hashtable currencyHash = new Hashtable();
            currencyHash.put("code", currency.code);
            currencyHash.put("name", currency.name);
            currencyHash.put("exchangeRate", String.valueOf(currency.exchangeRate));
            currencies.add(currencyHash);
         }

         return currencies;
      } catch (Exception var7) {
         return ExceptionHelper.getRootMessageAsVector(var7);
      }
   }

   public Vector getRateGrid() {
      Vector rateGrid = new Vector();
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Vector var6;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select id, name, iddcode, callSignallingFee, mobileSignallingFee, mobileRate, callRate from country order by name");
         rs = ps.executeQuery();

         while(rs.next()) {
            Hashtable gridHash = new Hashtable();
            gridHash.put("id", String.valueOf(rs.getInt("id")));
            gridHash.put("country", rs.getString("name"));
            gridHash.put("iddcode", rs.getInt("iddcode"));
            gridHash.put("callSignallingFee", String.valueOf(rs.getDouble("callSignallingFee")));
            gridHash.put("mobileSignallingFee", String.valueOf(rs.getDouble("mobileSignallingFee")));
            gridHash.put("mobileRate", String.valueOf(rs.getDouble("mobileRate")));
            gridHash.put("callRate", String.valueOf(rs.getDouble("callRate")));
            rateGrid.add(gridHash);
         }

         return rateGrid;
      } catch (SQLException var24) {
         var6 = ExceptionHelper.getRootMessageAsVector(var24);
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

   public Hashtable getHistoryEntry(long id, String type) {
      Hashtable entryDataHash = new Hashtable();

      try {
         AccountLocal accountBean;
         if (type.equals("acct")) {
            accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
            return HashObjectUtils.dataObjectToHashtable(accountBean.getAccountEntry(id));
         } else if (type.equals("sms")) {
            MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            MessageData messageData = messageBean.getMessage((int)id);
            Hashtable hashEntry = HashObjectUtils.dataObjectToHashtable(messageData);
            if (messageData.messageDestinations.size() == 1) {
               MessageDestinationData messageDestinationData = (MessageDestinationData)messageData.messageDestinations.get(0);
               hashEntry.put("destination", messageDestinationData.destination);
            }

            return hashEntry;
         } else if (type.equals("call")) {
            VoiceLocal voiceBean = (VoiceLocal)EJBHomeCache.getLocalObject("VoiceLocal", VoiceLocalHome.class);
            return HashObjectUtils.dataObjectToHashtable(voiceBean.getCallEntryWithCost((int)id));
         } else if (type.equals("tt")) {
            accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
            return HashObjectUtils.dataObjectToHashtable(accountBean.getMoneyTransferEntry((int)id));
         } else {
            return entryDataHash;
         }
      } catch (Exception var9) {
         return ExceptionHelper.getRootMessageAsHashtable(var9);
      }
   }

   public String setEmailAlert(String username, boolean flag) {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      String var7;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update user set emailalert = ? where username = ?");
         if (flag) {
            ps.setInt(1, 1);
         } else {
            ps.setInt(1, 0);
         }

         ps.setString(2, username);
         if (ps.executeUpdate() >= 1) {
            return "TRUE";
         }

         String var6 = ExceptionHelper.setErrorMessage("Could not update emailalert: User does not exist");
         return var6;
      } catch (SQLException var28) {
         var7 = ExceptionHelper.setErrorMessage("Could not update emailalert");
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var25) {
            conn = null;
         }

      }

      return var7;
   }

   public String setEmailAlertSent(String username, boolean flag) {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.setEmailAlertSent(username, flag);
         return "TRUE";
      } catch (CreateException var4) {
         return ExceptionHelper.getRootMessage(var4);
      } catch (EJBException var5) {
         return var5.getCausedByException() != null ? ExceptionHelper.setErrorMessage(var5.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessage(var5.getMessage());
      }
   }

   public String sendSMS(String fromUsername, String fromMobilePhone, String toMobilePhone, String messageText, String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      try {
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         MessageData message = new MessageData();
         message.username = fromUsername;
         message.dateCreated = new Date(System.currentTimeMillis());
         message.messageText = messageText;
         message.sendReceive = MessageData.SendReceiveEnum.SEND;
         message.source = fromMobilePhone;
         message.type = MessageType.SMS;
         MessageDestinationData messageDest = new MessageDestinationData();
         messageDest.type = MessageDestinationData.TypeEnum.INDIVIDUAL;
         messageDest.destination = messageBean.cleanAndValidatePhoneNumber(toMobilePhone, true);
         message.messageDestinations = new LinkedList();
         message.messageDestinations.add(messageDest);
         messageBean.sendSMS(message, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return "TRUE";
      } catch (CreateException var12) {
         return ExceptionHelper.getRootMessage(var12);
      } catch (EJBException var13) {
         return var13.getCausedByException() != null ? ExceptionHelper.setErrorMessage(var13.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessage(var13.getMessage());
      }
   }

   public String sendEmail(String senderUsername, String senderPassword, String to, String subject, String content) {
      try {
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         messageBean.sendEmail(senderUsername, senderPassword, to, subject, content);
         return "TRUE";
      } catch (CreateException var7) {
         return ExceptionHelper.getRootMessage(var7);
      } catch (EJBException var8) {
         return ExceptionHelper.setErrorMessage(var8.getMessage());
      }
   }

   public String sendEmailFromNoReply(String destinationAddress, String subject, String content) {
      try {
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         messageBean.sendEmailFromNoReply(destinationAddress, subject, content);
         return "TRUE";
      } catch (CreateException var5) {
         return ExceptionHelper.getRootMessage(var5);
      } catch (EJBException var6) {
         return ExceptionHelper.setErrorMessage(var6.getMessage());
      }
   }

   public String makeCall(String[] keys, String[] values) {
      CallData callData = new CallData();

      try {
         VoiceLocal voiceBean = (VoiceLocal)EJBHomeCache.getLocalObject("VoiceLocal", VoiceLocalHome.class);
         HashObjectUtils.stringArrayToDataObject(keys, values, callData);
         String username = callData.username;
         if (StringUtil.isBlank(username)) {
            throw new Exception("Username cannot be empty");
         } else {
            UserPrx userProxy = EJBIcePrxFinder.findUserPrx(username);

            try {
               FloodControl.detectFlooding(username, userProxy, new FloodControl.Action[]{FloodControl.Action.PHONE_CALL.setMaxHits(SystemProperty.getLong("PhoneCallUserPerSecondRateLimit", 3L))});
            } catch (Exception var9) {
               log.info("[" + username + "] user disconnected and suspended for 1 hour, exceeded 3/second rate limit. Destination[" + callData.destination + "] from source[" + callData.source + "]");
               throw var9;
            }

            if (MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.PHONECALL.toString(), MemCachedKeyUtils.getFullKeyFromStrings("PS", "DEST", callData.destination), SystemProperty.getLong("PhoneCallDestinationPerSecondRateLimit", 1L), 1000L) && MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.PHONECALL.toString(), MemCachedKeyUtils.getFullKeyFromStrings("PH", "DEST", callData.destination), SystemProperty.getLong("PhoneCallDestinationPerHourRateLimit", 60L), 3600000L)) {
               UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               UserData userData = userBean.loadUser(username, false, false);
               if (callData.source == null || callData.source.equals(userData.mobilePhone) || MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.PHONECALL.toString(), MemCachedKeyUtils.getFullKeyFromStrings("PS", "SRC", callData.source), SystemProperty.getLong("PhoneCallSourcePerSecondRateLimit", 1L), 1000L) && MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.PHONECALL.toString(), MemCachedKeyUtils.getFullKeyFromStrings("PH", "SRC", callData.source), SystemProperty.getLong("PhoneCallSourcePerHourRateLimit", 60L), 3600000L)) {
                  voiceBean.initiatePhoneCall(callData);
                  return "TRUE";
               } else if (SystemProperty.getBool("SuspendPhoneCallSourceRateLimitOffender", false)) {
                  log.info("[" + username + "] user disconnected and suspended for 1 hour. Destination Rate Limit exceeded. Destination [" + callData.destination + "] from source[" + callData.source + "]");
                  if (userProxy != null) {
                     userProxy.disconnectFlooder("Flooding. Broke Phone Call Destination Rate Limit. Destination [" + callData.destination + "] from source[" + callData.source + "]");
                  }

                  throw new Exception("You have been disconnected.");
               } else {
                  log.info("[" + username + "] call dropped, exceeded rate limit for source[" + callData.source + "] from destination[" + callData.destination + "]");
                  throw new Exception("System busy. Please try again later.");
               }
            } else if (SystemProperty.getBool("SuspendPhoneCallDestinationRateLimitOffender", false)) {
               log.info(username + ", user disconnected and suspended for 1 hour. Destination Rate Limit exceeded. Destination [" + callData.destination + "] from source[" + callData.source + "]");
               if (userProxy != null) {
                  userProxy.disconnectFlooder("Flooding. Broke Phone Call Destination Rate Limit. Destination [" + callData.destination + "] from source[" + callData.source + "]");
               }

               throw new Exception("You have been disconnected.");
            } else {
               log.info(username + ", call dropped, exceeded rate limit to destination[" + callData.destination + "] from source[" + callData.source + "]");
               throw new Exception("System busy. Please try again later.");
            }
         }
      } catch (Exception var10) {
         return ExceptionHelper.getRootMessage(var10);
      }
   }

   public Hashtable evaluateCall(String[] keys, String[] values) {
      CallData callData = new CallData();
      Hashtable callDetailHash = new Hashtable();
      Double rate = 0.0D;
      Double signallingfee = null;

      try {
         VoiceLocal voiceBean = (VoiceLocal)EJBHomeCache.getLocalObject("VoiceLocal", VoiceLocalHome.class);
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         HashObjectUtils.stringArrayToDataObject(keys, values, callData);
         CallData callDetailData = voiceBean.evaluatePhoneCall(callData);
         CurrencyData currencyData = accountBean.getUsersLocalCurrency(callData.username);
         rate = currencyData.convert(callDetailData.rate);
         signallingfee = currencyData.convert(callDetailData.signallingFee);
         String maxduration = WebCommon.toNiceDuration((long)callDetailData.maxCallDuration * 1000L);
         callDetailHash.put("signallingFee", String.valueOf(signallingfee));
         callDetailHash.put("rate", String.valueOf(rate));
         callDetailHash.put("maxDuration", maxduration);
         return callDetailHash;
      } catch (Exception var12) {
         return ExceptionHelper.getRootMessageAsHashtable(var12);
      }
   }

   public Vector getContactList(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector contactList = new Vector();

      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "select * from contact, user where contact.username = ? and contact.fusionusername = user.username and contact.status = ? ";
         sql = sql + "order by contact.displayname";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setInt(2, ContactData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();

         while(rs.next()) {
            ContactData contact = new ContactData(rs);
            contactList.add(HashObjectUtils.dataObjectToHashtable(contact));
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

      return contactList;
   }

   public Vector getContactListEmailActivated(String username, boolean emailOnly) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector contactList = new Vector();

      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "select * from contact, user where contact.username = ? and contact.fusionusername = user.username and contact.status = ? ";
         if (emailOnly) {
            sql = sql + "and user.emailactivated = ? ";
         }

         sql = sql + "order by contact.displayname";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setInt(2, ContactData.StatusEnum.ACTIVE.value());
         if (emailOnly) {
            ps.setInt(3, 1);
         }

         rs = ps.executeQuery();

         while(rs.next()) {
            ContactData contact = new ContactData(rs);
            contactList.add(HashObjectUtils.dataObjectToHashtable(contact));
         }
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

      return contactList;
   }

   public Vector getAccountEntries(String username, int page, int numEntries) {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      int maxAEPeriodBeforeArchival = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_ACCOUNTENTRY_PERIOD_BEFORE_ARCHIVAL_IN_DAYS);

      Vector var9;
      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         double balance = accountBean.getAccountBalance(username).balance;
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select * from accountentry use index (fk_accountentry_1) where username = ? and amount != 0 and datecreated >= date_sub(curdate(), interval ? day) order by id desc limit ? offset ?");
         ps.setString(1, username);
         ps.setInt(2, maxAEPeriodBeforeArchival);
         ps.setInt(3, numEntries + 1);
         ps.setInt(4, page * numEntries);
         rs = ps.executeQuery();
         Vector accountEntries = new Vector();
         boolean hasMore = false;

         while(true) {
            Hashtable accountEntryDataHash;
            if (rs.next()) {
               if (rs.getRow() <= numEntries) {
                  accountEntryDataHash = new Hashtable();
                  SimpleDateFormat df = new SimpleDateFormat("d MMM yy HH:mm");
                  accountEntryDataHash.put("id", rs.getLong("id"));
                  accountEntryDataHash.put("username", rs.getString("username"));
                  accountEntryDataHash.put("dateCreated", df.format(rs.getTimestamp("dateCreated")));
                  accountEntryDataHash.put("type", AccountEntryData.TypeEnum.fromValue(rs.getInt("type")).toString());
                  accountEntryDataHash.put("reference", rs.getString("reference"));
                  accountEntryDataHash.put("description", rs.getString("description"));
                  accountEntryDataHash.put("currency", rs.getString("currency"));
                  accountEntryDataHash.put("exchangeRate", rs.getDouble("exchangeRate"));
                  accountEntryDataHash.put("amount", rs.getDouble("amount"));
                  accountEntryDataHash.put("tax", rs.getDouble("tax"));
                  accountEntryDataHash.put("runningBalance", balance);
                  accountEntries.add(accountEntryDataHash);
                  balance -= rs.getDouble("amount");
                  continue;
               }

               hasMore = true;
            }

            accountEntryDataHash = new Hashtable();
            accountEntryDataHash.put("page", page);
            accountEntryDataHash.put("hasMore", hasMore);
            accountEntries.add(0, accountEntryDataHash);
            Vector var14 = accountEntries;
            return var14;
         }
      } catch (Exception var32) {
         var9 = ExceptionHelper.getRootMessageAsVector(var32);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var29) {
            connSlave = null;
         }

      }

      return var9;
   }

   public Vector getMoneyTransferEntries(String username, int page, int numEntries) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector moneyEntries = new Vector();
      int numRows = 0;
      int startEntry = page * numEntries + 1;
      int endEntry = startEntry + numEntries - 1;

      Vector var12;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from moneytransfer where username = ? order by DateCreated desc");
         ps.setString(1, username);

         for(rs = ps.executeQuery(); rs.next(); ++numRows) {
         }

         rs.beforeFirst();
         Hashtable markerHash = new Hashtable();
         markerHash.put("page", page);
         markerHash.put("numEntries", numRows - 1);
         if (numRows == 0) {
            markerHash.put("numPages", 0);
         }

         if (numRows > 0 && numRows / numEntries == 0) {
            markerHash.put("numPages", 1);
         } else {
            markerHash.put("numPages", numRows / numEntries);
         }

         moneyEntries.add(markerHash);
         if (endEntry > numRows) {
            endEntry = numRows;
         }

         while(rs.next()) {
            if (rs.getRow() >= startEntry) {
               Hashtable moneyEntryHash = new Hashtable();
               SimpleDateFormat df = new SimpleDateFormat("d MMM yy HH:mm");
               moneyEntryHash.put("id", rs.getInt("id"));
               moneyEntryHash.put("username", rs.getString("username"));
               moneyEntryHash.put("dateCreated", df.format(rs.getTimestamp("dateCreated")));
               moneyEntryHash.put("type", MoneyTransferData.TypeEnum.fromValue(rs.getInt("type")).toString());
               moneyEntryHash.put("receiptNumber", rs.getString("receiptNumber"));
               moneyEntryHash.put("fullName", rs.getString("fullName"));
               moneyEntryHash.put("amount", rs.getDouble("amount"));
               moneyEntries.add(moneyEntryHash);
               if (rs.getRow() >= endEntry) {
                  return moneyEntries;
               }
            }
         }

         return moneyEntries;
      } catch (SQLException var31) {
         var12 = ExceptionHelper.getRootMessageAsVector(var31);
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

      return var12;
   }

   public Vector getSMSHistory(String username, int page, int numEntries) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector smsEntries = new Vector();
      int numRows = 0;
      int startEntry = page * numEntries + 1;
      int endEntry = startEntry + numEntries - 1;

      Vector var12;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select message.id, message.datecreated, message.messagetext, messagedestination.destination, messagedestination.status from message, messagedestination\twhere message.id = messagedestination.messageid and message.type = 2 and username = ? order by datecreated desc");
         ps.setString(1, username);

         for(rs = ps.executeQuery(); rs.next(); ++numRows) {
         }

         rs.beforeFirst();
         Hashtable markerHash = new Hashtable();
         markerHash.put("page", page);
         markerHash.put("numEntries", numRows);
         if (numRows == 0) {
            markerHash.put("numPages", 0);
         }

         if (numRows > 0 && numRows / numEntries == 0) {
            markerHash.put("numPages", 1);
         } else {
            markerHash.put("numPages", numRows / numEntries);
         }

         smsEntries.add(markerHash);
         if (endEntry > numRows) {
            endEntry = numRows;
         }

         while(rs.next()) {
            if (rs.getRow() >= startEntry) {
               Hashtable smsEntryHash = new Hashtable();
               SimpleDateFormat df = new SimpleDateFormat("d MMM yy HH:mm");
               smsEntryHash.put("id", rs.getInt("id"));
               smsEntryHash.put("dateCreated", df.format(rs.getTimestamp("dateCreated")));
               smsEntryHash.put("messageText", rs.getString("messageText"));
               smsEntryHash.put("destination", rs.getString("destination"));
               smsEntryHash.put("status", MessageDestinationData.StatusEnum.fromValue(rs.getInt("status")).toString());
               smsEntries.add(smsEntryHash);
               if (rs.getRow() >= endEntry) {
                  return smsEntries;
               }
            }
         }

         return smsEntries;
      } catch (SQLException var31) {
         var12 = ExceptionHelper.getRootMessageAsVector(var31);
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

      return var12;
   }

   public Vector getCallHistory(String username, int page, int numEntries) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector callEntries = new Vector();
      int numRows = 0;
      int startEntry = page * numEntries + 1;
      int endEntry = startEntry + numEntries - 1;
      int maxAEPeriodBeforeArchival = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_ACCOUNTENTRY_PERIOD_BEFORE_ARCHIVAL_IN_DAYS);

      Vector var13;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select p.id, p.datecreated, p.source, if(p.type = ?, u.username, p.destination) destination, p.billedduration, a.amount, a.currency from phonecall p inner join accountentry a on (p.username = a.username and p.id = a.reference and a.type = ?) left outer join user u on (p.destination = u.mobilephone) where p.status = ? and p.username = ? and a.datecreated >= date_sub(curdate(), interval ? day) order by p.datecreated desc");
         ps.setInt(1, CallData.TypeEnum.MIDLET_ANONYMOUS_CALLBACK.value());
         ps.setInt(2, AccountEntryData.TypeEnum.CALL_CHARGE.value());
         ps.setInt(3, CallData.StatusEnum.COMPLETED.value());
         ps.setString(4, username);
         ps.setInt(5, maxAEPeriodBeforeArchival);

         for(rs = ps.executeQuery(); rs.next(); ++numRows) {
         }

         rs.beforeFirst();
         Hashtable markerHash = new Hashtable();
         markerHash.put("page", page);
         markerHash.put("numEntries", numRows);
         if (numRows == 0) {
            markerHash.put("numPages", 0);
         }

         if (numRows > 0 && numRows / numEntries == 0) {
            markerHash.put("numPages", 1);
         } else {
            markerHash.put("numPages", numRows / numEntries);
         }

         callEntries.add(markerHash);
         if (endEntry > numRows) {
            endEntry = numRows;
         }

         while(rs.next()) {
            if (rs.getRow() >= startEntry) {
               Hashtable callEntryHash = new Hashtable();
               SimpleDateFormat df = new SimpleDateFormat("d MMM yy HH:mm");
               callEntryHash.put("id", rs.getInt("id"));
               callEntryHash.put("dateCreated", df.format(rs.getTimestamp("dateCreated")));
               callEntryHash.put("source", rs.getString("source"));
               callEntryHash.put("destination", rs.getString("destination"));
               callEntryHash.put("billedDuration", rs.getDouble("billedDuration"));
               callEntryHash.put("amount", rs.getDouble("amount"));
               callEntryHash.put("currency", rs.getString("currency"));
               callEntries.add(callEntryHash);
               if (rs.getRow() >= endEntry) {
                  return callEntries;
               }
            }
         }

         return callEntries;
      } catch (SQLException var32) {
         var13 = ExceptionHelper.getRootMessageAsVector(var32);
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var29) {
            conn = null;
         }

      }

      return var13;
   }

   public Hashtable registerUser(String[] keys, String[] values, String[] profileKeys, String[] profileValues, String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      return this.registerUser(keys, values, profileKeys, profileValues, (String)null, ipAddress, sessionID, mobileDevice, userAgent);
   }

   public Hashtable registerUser(String[] keys, String[] values, String[] profileKeys, String[] profileValues, String referrerUsername, String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      return this.registerUser(keys, values, profileKeys, profileValues, (String)null, (String)null, ipAddress, sessionID, mobileDevice, userAgent);
   }

   public Hashtable registerUser(String[] keys, String[] values, String[] profileKeys, String[] profileValues, String referrerUsername, String campaign, String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      new Hashtable();
      UserData userData = new UserData();
      UserProfileData userProfileData = new UserProfileData();
      HashObjectUtils.stringArrayToDataObject(keys, values, userData);
      HashObjectUtils.stringArrayToDataObject(profileKeys, profileValues, userProfileData);

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         if (!StringUtil.isBlank(referrerUsername)) {
            try {
               userBean.inviteFriend(referrerUsername, referrerUsername, userData.mobilePhone, (Integer)null, (String)null, (String)null, (String)null, (AccountEntrySourceData)null);
            } catch (FusionEJBException var16) {
               log.warn("Creating a user referral record failed with: " + var16.getMessage());
            }
         }

         RegistrationType registrationType = StringUtil.isBlank(userData.mobilePhone) ? RegistrationType.EMAIL_LEGACY : RegistrationType.MOBILE_REGISTRATION;
         userData = userBean.createUser(userData, userProfileData, true, new UserRegistrationContextData(campaign, false, registrationType), new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
      } catch (CreateException var17) {
         return ExceptionHelper.getRootMessageAsHashtable(var17);
      } catch (EJBException var18) {
         return ExceptionHelper.getRootMessageAsHashtable(var18);
      }

      if (userData == null) {
         return ExceptionHelper.setErrorMessageAsHashtable("The username entered does not exist.");
      } else {
         Hashtable userDataSOAP = HashObjectUtils.dataObjectToHashtable(userData);
         return userDataSOAP;
      }
   }

   public Hashtable registerUserMerchant(String[] keys, String[] values, String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      new Hashtable();
      UserData userData = new UserData();
      UserProfileData userProfileData = new UserProfileData();
      HashObjectUtils.stringArrayToDataObject(keys, values, userData);

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userData = userBean.createUserMerchant(userData, userProfileData, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
      } catch (CreateException var11) {
         return ExceptionHelper.getRootMessageAsHashtable(var11);
      } catch (EJBException var12) {
         return ExceptionHelper.setErrorMessageAsHashtable(var12.getMessage());
      }

      if (userData == null) {
         return ExceptionHelper.setErrorMessageAsHashtable("The username entered does not exist.");
      } else {
         Hashtable userDataSOAP = HashObjectUtils.dataObjectToHashtable(userData);
         return userDataSOAP;
      }
   }

   public Hashtable loadUserDetails(String username) {
      Hashtable userDataSOAP = null;
      UserData userData = null;

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userData = userBean.loadUser(username, false, false);
      } catch (Exception var5) {
         return ExceptionHelper.getRootMessageAsHashtable(var5);
      }

      if (userData == null) {
         return ExceptionHelper.setErrorMessageAsHashtable("The username entered does not exist.");
      } else {
         userDataSOAP = HashObjectUtils.dataObjectToHashtable(userData);
         return userDataSOAP;
      }
   }

   public Hashtable loadUserDetailsFromMobilePhone(String mobilePhone) {
      Hashtable userDataSOAP = null;
      UserData userData = null;

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userData = userBean.loadUserFromMobilePhone(mobilePhone);
      } catch (Exception var5) {
         return ExceptionHelper.getRootMessageAsHashtable(var5);
      }

      if (userData == null) {
         return ExceptionHelper.setErrorMessageAsHashtable("The username entered does not exist.");
      } else {
         userDataSOAP = HashObjectUtils.dataObjectToHashtable(userData);
         return userDataSOAP;
      }
   }

   public Hashtable loadUserProfile(String requestingUsername, String targetUsername) {
      UserProfileData userProfileData = null;

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userProfileData = userBean.getUserProfile(requestingUsername, targetUsername, false);
      } catch (Exception var5) {
         userProfileData = null;
      }

      if (userProfileData == null) {
         userProfileData = new UserProfileData();
         userProfileData.username = targetUsername;
         userProfileData.status = UserProfileData.StatusEnum.PRIVATE;
      }

      return HashObjectUtils.dataObjectToHashtable(userProfileData);
   }

   public Hashtable getAccountBalance(String username) {
      try {
         Hashtable balance = (Hashtable)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.ACCOUNT_BALANCE, username);
         if (balance == null) {
            AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
            balance = HashObjectUtils.dataObjectToHashtable(accountBean.getAccountBalance(username));
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.ACCOUNT_BALANCE, username, balance);
         }

         return balance;
      } catch (Exception var4) {
         return ExceptionHelper.getRootMessageAsHashtable(var4);
      }
   }

   public String getMerchantTagFromUsername(String username) {
      try {
         String tag = (String)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.MERCHANT_TAG, username);
         if (tag == null) {
            AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            MerchantTagData tagData = accountBean.getMerchantTagFromUsername((Connection)null, username, true);
            if (tagData != null) {
               UserData merchantUserData = userBean.loadUserFromID(tagData.merchantUserID);
               MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.MERCHANT_TAG, username, merchantUserData.username);
               tag = merchantUserData.username;
            }
         }

         return tag;
      } catch (Exception var7) {
         return ExceptionHelper.setErrorMessage(var7.getMessage());
      }
   }

   public Vector searchUserProfiles(String username, String countryIDstring, String minAgeString, String maxAgeString, String homeTown, String keyword, String keywordTypeString, int page, int numEntries, String showAvatar, String gender) {
      return ExceptionHelper.setErrorMessageAsVector("Profile searching has been disabled while we work on a much better system. Check back soon");
   }

   public Vector getUsersWhoViewed(String username) throws EJBException {
      Vector userList = new Vector();
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select distinct usernameviewing from userprofileview where usernameviewed = ?");
         ps.setString(1, username);
         rs = ps.executeQuery();

         while(rs.next()) {
            userList.add(rs.getString(1));
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

      return userList;
   }

   public String updateUser(String[] detailKeys, String[] detailValues, String[] profileKeys, String[] profileValues) {
      UserData userData = new UserData();
      UserProfileData userProfileData = new UserProfileData();
      HashObjectUtils.stringArrayToDataObject(detailKeys, detailValues, userData);
      HashObjectUtils.stringArrayToDataObject(profileKeys, profileValues, userProfileData);

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.updateUserDetail(userData);
         userBean.updateUserProfile(userProfileData);
         return "TRUE";
      } catch (CreateException var8) {
         return ExceptionHelper.getRootMessage(var8);
      } catch (EJBException var9) {
         return var9.getCausedByException() != null ? ExceptionHelper.setErrorMessage(var9.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessage(var9.getMessage());
      }
   }

   public String updateUserDisplayPicture(String username, String displayPictureId) {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.updateDisplayPicture(username, displayPictureId);
         return "TRUE";
      } catch (CreateException var4) {
         return ExceptionHelper.getRootMessage(var4);
      } catch (EJBException var5) {
         return var5.getCausedByException() != null ? ExceptionHelper.setErrorMessage(var5.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessage(var5.getMessage());
      }
   }

   public String updateUserDetails(String[] detailKeys, String[] detailValues) {
      UserData userData = new UserData();
      HashObjectUtils.stringArrayToDataObject(detailKeys, detailValues, userData);

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.updateUserDetail(userData);
         return "TRUE";
      } catch (CreateException var5) {
         return ExceptionHelper.getRootMessage(var5);
      } catch (EJBException var6) {
         return var6.getCausedByException() != null ? ExceptionHelper.setErrorMessage(var6.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessage(var6.getMessage());
      }
   }

   public String updateUserProfile(String[] profileKeys, String[] profileValues) {
      UserProfileData userProfileData = new UserProfileData();
      HashObjectUtils.stringArrayToDataObject(profileKeys, profileValues, userProfileData);

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.updateUserProfile(userProfileData);
         return "TRUE";
      } catch (CreateException var5) {
         return ExceptionHelper.getRootMessage(var5);
      } catch (EJBException var6) {
         return var6.getCausedByException() != null ? ExceptionHelper.setErrorMessage(var6.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessage(var6.getMessage());
      }
   }

   public String updateUserStatusMessage(int userID, String username, String statusMessage) {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.updateStatusMessage(userID, username, statusMessage, ClientType.AJAX1, (SSOEnums.View)null);
         return "TRUE";
      } catch (CreateException var5) {
         return ExceptionHelper.getRootMessage(var5);
      } catch (EJBException var6) {
         return var6.getCausedByException() != null ? ExceptionHelper.setErrorMessage(var6.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessage(var6.getMessage());
      }
   }

   public String referFriendViaGame(String username, String displayName, String friendsNumber, String gameName, String hashKey, String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData invitee = userBean.loadUserFromMobilePhone(friendsNumber);
         if (invitee == null) {
            userBean.inviteFriend(username, displayName, friendsNumber, (Integer)null, (String)null, gameName, hashKey, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
            return "TRUE";
         } else {
            ContactLocal contactBean = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
            AuthenticationServicePrx prx = EJBIcePrxFinder.getAuthenticationServiceProxy();
            int inviterUserID = prx.userIDForFusionUsername(username);
            ContactData contactData = new ContactData();
            contactData.username = username;
            contactData.fusionUsername = invitee.username;
            int showVisible = Math.min(4, invitee.username.length() / 2);
            contactData.displayName = StringUtil.maskString(invitee.username, showVisible, 'X');
            contactData.displayOnPhone = true;
            contactData.mobilePhone = friendsNumber;
            contactData = contactBean.addPendingFusionContact(inviterUserID, contactData);
            return "Congratulations! This person is already on migme and we have sent user " + contactData.displayName + " your friend request";
         }
      } catch (Exception var17) {
         return ExceptionHelper.getRootMessage(var17);
      }
   }

   public String referFriend(String username, String displayName, String friendsNumber, String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      return this.referFriendViaGame(username, displayName, friendsNumber, (String)null, (String)null, ipAddress, sessionID, mobileDevice, userAgent);
   }

   public Hashtable transferCredit(String fromUsername, String toUsername, String amountString, String pin, String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      new Hashtable();
      double amount = Double.valueOf(amountString);

      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         CreditTransferData creditData = accountBean.transferCredit(fromUsername, toUsername, amount, false, pin, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         Hashtable accountEntryDataHash = HashObjectUtils.dataObjectToHashtable(creditData.getAccountEntryData());
         accountEntryDataHash.put("balance", creditData.getAccountBalanceData().balance);
         accountEntryDataHash.put("balanceWithCode", creditData.getAccountBalanceData().formatWithCode());
         return accountEntryDataHash;
      } catch (CreateException var14) {
         return ExceptionHelper.getRootMessageAsHashtable(var14);
      } catch (EJBException var15) {
         return var15.getCausedByException() != null ? ExceptionHelper.setErrorMessageAsHashtable(var15.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessageAsHashtable(var15.getMessage());
      }
   }

   public String cleanAndValidatePhoneNumber(String number) {
      try {
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         messageBean.cleanAndValidatePhoneNumber(number, false);
         return "TRUE";
      } catch (Exception var3) {
         return ExceptionHelper.getRootMessage(var3);
      }
   }

   public Vector getScrapbook(String username, int page, int numEntries) {
      return this.getScrapbook(username, page, numEntries, false);
   }

   public Vector getScrapbook(String username, int page, int numEntries, boolean publishedOnly) {
      Vector vec = new Vector();
      List<ScrapbookData> scrapbookList = null;
      int numRows = false;
      --page;
      int startEntry = page * numEntries;
      int endEntry = startEntry + numEntries;

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         scrapbookList = misBean.getScrapbook(username, publishedOnly);
         int numRows = scrapbookList.size();
         Hashtable markerHash = new Hashtable();
         markerHash.put("page", page + 1);
         markerHash.put("numEntries", numRows);
         if (numRows == 0) {
            markerHash.put("numPages", 0);
         }

         if (numRows > 0 && numRows / numEntries == 0) {
            markerHash.put("numPages", 1);
         } else {
            double numRowsD = (double)numRows;
            double numEntriesD = (double)numEntries;
            markerHash.put("numPages", (int)Math.ceil(numRowsD / numEntriesD));
         }

         vec.add(markerHash);
         if (endEntry > numRows) {
            endEntry = numRows;
         }

         for(int i = startEntry; i < endEntry; ++i) {
            ScrapbookData scrapbookData = (ScrapbookData)scrapbookList.get(i);
            Hashtable hash = HashObjectUtils.dataObjectToHashtable(scrapbookData);
            vec.add(hash);
         }

         return vec;
      } catch (Exception var16) {
         return ExceptionHelper.getRootMessageAsVector(var16);
      }
   }

   public Vector getGallery(String requestingUsername, String targetUsername, int page, int numEntries) {
      Vector vec = new Vector();
      List<ScrapbookData> scrapbookList = null;
      int numRows = false;
      --page;
      int startEntry = page * numEntries;
      int endEntry = startEntry + numEntries;

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         scrapbookList = misBean.getGallery(requestingUsername, targetUsername);
         int numRows = scrapbookList.size();
         Hashtable markerHash = new Hashtable();
         markerHash.put("page", page + 1);
         markerHash.put("numEntries", numRows);
         if (numRows == 0) {
            markerHash.put("numPages", 0);
         }

         if (numRows > 0 && numRows / numEntries == 0) {
            markerHash.put("numPages", 1);
         } else {
            double numRowsD = (double)numRows;
            double numEntriesD = (double)numEntries;
            markerHash.put("numPages", (int)Math.ceil(numRowsD / numEntriesD));
         }

         vec.add(markerHash);
         if (endEntry > numRows) {
            endEntry = numRows;
         }

         for(int i = startEntry; i < endEntry; ++i) {
            ScrapbookData scrapbookData = (ScrapbookData)scrapbookList.get(i);
            Hashtable hash = HashObjectUtils.dataObjectToHashtable(scrapbookData);
            vec.add(hash);
         }

         return vec;
      } catch (Exception var16) {
         return ExceptionHelper.getRootMessageAsVector(var16);
      }
   }

   public boolean reportPhotoAbuse(String reporterUsername, String offenderUsername, int id) {
      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData reporterUserData = userEJB.loadUser(reporterUsername, false, false);
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         if (reporterUserData.chatRoomAdmin) {
            misBean.deleteFileFromScrapbook(offenderUsername, id);
         } else {
            misBean.setFileReportedFromScrapbook(offenderUsername, id);
         }
      } catch (CreateException var7) {
         var7.printStackTrace();
      }

      return true;
   }

   public Hashtable getWall(String username, int page, int numEntries, boolean reportedOnly) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var9;
      try {
         String sql = "select scrapbook.id, scrapbook.datecreated, scrapbook.receivedfrom, scrapbook.description, scrapbook.fileid, file.width, file.height, country.name from scrapbook, file, user, country where scrapbook.username = ? and (scrapbook.status = ? or scrapbook.status = ?) and scrapbook.fileid = file.id and user.username = scrapbook.receivedfrom and country.id = user.countryid order by scrapbook.id desc limit ?, ?";
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setInt(2, reportedOnly ? ScrapbookData.StatusEnum.REPORTED.value() : ScrapbookData.StatusEnum.PUBLIC.value());
         ps.setInt(3, ScrapbookData.StatusEnum.REPORTED.value());
         ps.setInt(4, (page - 1) * numEntries);
         ps.setInt(5, numEntries);
         rs = ps.executeQuery();

         Hashtable wallItem;
         Vector wall;
         for(wall = new Vector(); rs.next(); wall.add(wallItem)) {
            wallItem = new Hashtable();
            String value = rs.getString("id");
            if (value != null) {
               wallItem.put("id", value);
            }

            value = rs.getString("dateCreated");
            if (value != null) {
               wallItem.put("dateCreated", value);
            }

            value = rs.getString("receivedFrom");
            if (value != null) {
               wallItem.put("receivedFrom", value);
            }

            value = rs.getString("description");
            if (value != null) {
               wallItem.put("description", value);
            }

            value = rs.getString("fileID");
            if (value != null) {
               wallItem.put("file.id", value);
            }

            value = rs.getString("file.width");
            if (value != null) {
               wallItem.put("file.width", value);
            }

            value = rs.getString("file.height");
            if (value != null) {
               wallItem.put("file.height", value);
            }

            value = rs.getString("name");
            if (value != null) {
               wallItem.put("countryName", value);
            }
         }

         wallItem = new Hashtable();
         wallItem.put("page", page);
         wallItem.put("wall", wall);
         Hashtable var32 = wallItem;
         return var32;
      } catch (SQLException var29) {
         var9 = ExceptionHelper.getRootMessageAsHashtable(var29);
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

      return var9;
   }

   public Hashtable getPhoto(int id, String username, String viewusername) {
      ScrapbookData scrapbookData = null;
      int total = false;
      int position = 0;
      int nextId = 0;
      int prevId = 0;

      int total;
      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         List scrapbookList;
         if (username.equals(viewusername)) {
            scrapbookList = misBean.getScrapbook(username);
         } else {
            scrapbookList = misBean.getGallery(username, viewusername);
         }

         total = scrapbookList.size();

         for(int i = 0; i < scrapbookList.size(); ++i) {
            ScrapbookData sc = (ScrapbookData)scrapbookList.get(i);
            if (sc.id == id) {
               if (!username.equals(viewusername)) {
                  if (sc.status == ScrapbookData.StatusEnum.PRIVATE) {
                     return null;
                  }

                  if (sc.status == ScrapbookData.StatusEnum.CONTACTS_ONLY && !this.isContactFriend(username, viewusername)) {
                     return null;
                  }
               }

               scrapbookData = sc;
               position = i + 1;
               if (i > 0) {
                  prevId = ((ScrapbookData)scrapbookList.get(i - 1)).id;
               }

               if (i < scrapbookList.size() - 1) {
                  nextId = ((ScrapbookData)scrapbookList.get(i + 1)).id;
               }
               break;
            }
         }
      } catch (Exception var13) {
         throw new EJBException(var13.getMessage());
      }

      if (scrapbookData == null) {
         throw new EJBException("Unable to find photo with id " + id);
      } else {
         Hashtable scrapbookDataHash = HashObjectUtils.dataObjectToHashtable(scrapbookData);
         scrapbookDataHash.put("total", total);
         scrapbookDataHash.put("page", position);
         scrapbookDataHash.put("nextId", nextId);
         scrapbookDataHash.put("prevId", prevId);
         return scrapbookDataHash;
      }
   }

   public Hashtable getScrapbookEntry(int scrapbookID) {
      ScrapbookData scrapbookData = null;

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         scrapbookData = misBean.getFileFromScrapbook(scrapbookID);
      } catch (Exception var4) {
         return ExceptionHelper.getRootMessageAsHashtable(var4);
      }

      Hashtable scrapbookDataHash = HashObjectUtils.dataObjectToHashtable(scrapbookData);
      return scrapbookDataHash;
   }

   public String updateScrapbookEntry(String[] scrapbookKeys, String[] scrapbookValues) {
      ScrapbookData scrapbookData = new ScrapbookData();
      HashObjectUtils.stringArrayToDataObject(scrapbookKeys, scrapbookValues, scrapbookData);

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         misBean.updateFileFromScrapbook(scrapbookData);
         return "TRUE";
      } catch (CreateException var5) {
         return ExceptionHelper.getRootMessage(var5);
      } catch (EJBException var6) {
         return ExceptionHelper.getRootMessage(var6);
      }
   }

   public Hashtable getFile(String fileID) {
      FileData fileData = null;

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         fileData = misBean.getFile(fileID);
      } catch (Exception var4) {
         return ExceptionHelper.getRootMessageAsHashtable(var4);
      }

      Hashtable fileDataHash = HashObjectUtils.dataObjectToHashtable(fileData);
      return fileDataHash;
   }

   public String newFileID() {
      String fileID = "";

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         fileID = misBean.newFileID();
         return fileID;
      } catch (Exception var3) {
         return ExceptionHelper.getRootMessage(var3);
      }
   }

   public String saveFileToScrapbook(String[] keys, String[] values) {
      FileData fileData = new FileData();
      HashObjectUtils.stringArrayToDataObject(keys, values, fileData);
      fileData.dateCreated = new Date(System.currentTimeMillis());

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         misBean.saveFile(fileData, (String)null);
         return "TRUE";
      } catch (Exception var5) {
         return ExceptionHelper.getRootMessage(var5);
      }
   }

   public String saveExistingFileToScrapbooks(String sender, String[] destinations, String fileID, String description) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var9;
      try {
         if (destinations.length == 1 && "wall200712041".equalsIgnoreCase(destinations[0])) {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            ReputationLevelData levelData = userBean.getReputationLevel(sender);
            if (levelData.addToPhotoWall == null || !levelData.addToPhotoWall) {
               throw new EJBException("Invalid user reputation level for this function. User level: [" + levelData.level + "] level name: [" + levelData.name + "]");
            }

            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("select count(*) from scrapbook where username = ? and receivedfrom = ? and fileid = ? and scrapbook.status = ?");
            ps.setString(1, "wall200712041");
            ps.setString(2, sender);
            ps.setString(3, fileID);
            ps.setInt(4, ScrapbookData.StatusEnum.PUBLIC.value());
            rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
               String var10 = "TRUE";
               return var10;
            }
         }

         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         misBean.saveFileToScrapbooks(sender, destinations, fileID, description);
         return "TRUE";
      } catch (Exception var31) {
         var9 = ExceptionHelper.getRootMessage(var31);
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

      return var9;
   }

   public String publishFileFromScrapbook(String username, int id, String description, boolean contactOnly) {
      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         misBean.publishFileFromScrapbook(username, id, description, contactOnly);
         return "TRUE";
      } catch (Exception var6) {
         return ExceptionHelper.getRootMessage(var6);
      }
   }

   public String unpublishFileFromScrapbook(String username, int id) {
      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         misBean.unpublishFileFromScrapbook(username, id);
         return "TRUE";
      } catch (Exception var4) {
         return ExceptionHelper.getRootMessage(var4);
      }
   }

   public String deleteFileFromScrapbook(String username, int id) {
      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         misBean.deleteFileFromScrapbook(username, id);
         return "TRUE";
      } catch (Exception var4) {
         return ExceptionHelper.getRootMessage(var4);
      }
   }

   public Hashtable getContact(int contactID) {
      ContactData returnContactData = null;

      try {
         ContactLocal contactBean = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
         returnContactData = contactBean.getContact(contactID);
      } catch (Exception var4) {
         return ExceptionHelper.getRootMessageAsHashtable(var4);
      }

      return HashObjectUtils.dataObjectToHashtable(returnContactData);
   }

   public Hashtable getContact(String username, String contactName) {
      ContactData returnContactData = null;

      try {
         ContactLocal contactBean = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
         returnContactData = contactBean.getContact(username, contactName);
      } catch (Exception var5) {
         return ExceptionHelper.getRootMessageAsHashtable(var5);
      }

      return HashObjectUtils.dataObjectToHashtable(returnContactData);
   }

   public Hashtable addContact(int userID, String[] keys, String[] values) {
      ContactData contactData = new ContactData();
      ContactData returnContactData = new ContactData();
      HashObjectUtils.stringArrayToDataObject(keys, values, contactData);

      try {
         ContactLocal contactBean = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
         if (contactData.fusionUsername != null) {
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.FOLLOW_ON_ADD_CONTACT_ENABLED)) {
               boolean followOnMiniblog = true;
               returnContactData = contactBean.addFusionUserAsContact(userID, contactData, followOnMiniblog);
            } else {
               contactBean.addPendingFusionContact(userID, contactData);
            }
         } else {
            returnContactData = contactBean.addPhoneContact(userID, contactData);
         }
      } catch (Exception var8) {
         return ExceptionHelper.getRootMessageAsHashtable(var8);
      }

      return HashObjectUtils.dataObjectToHashtable(returnContactData);
   }

   public String updateContact(int userID, String[] keys, String[] values) {
      ContactData contactData = new ContactData();
      HashObjectUtils.stringArrayToDataObject(keys, values, contactData);

      try {
         ContactLocal contactBean = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
         contactBean.updateContactDetail(userID, contactData);
         return "TRUE";
      } catch (Exception var6) {
         return ExceptionHelper.getRootMessage(var6);
      }
   }

   public String blockContact(int userID, String username, String blockUsername) {
      try {
         ContactLocal contactBean = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
         contactBean.blockContact(userID, username, blockUsername);
         return "TRUE";
      } catch (Exception var5) {
         throw new EJBException(var5);
      }
   }

   public Hashtable getContactGroup(int groupID) {
      new ContactGroupData();

      ContactGroupData returnContactGroupData;
      try {
         ContactLocal contactBean = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
         returnContactGroupData = contactBean.getGroup(groupID);
      } catch (Exception var4) {
         return ExceptionHelper.getRootMessageAsHashtable(var4);
      }

      return HashObjectUtils.dataObjectToHashtable(returnContactGroupData);
   }

   public Hashtable addGroup(int userID, String[] keys, String[] values) {
      ContactGroupData contactGroupData = new ContactGroupData();
      new ContactGroupData();
      HashObjectUtils.stringArrayToDataObject(keys, values, contactGroupData);

      ContactGroupData returnContactGroupData;
      try {
         ContactLocal contactBean = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
         returnContactGroupData = contactBean.addGroup(userID, contactGroupData, true);
      } catch (Exception var7) {
         return ExceptionHelper.getRootMessageAsHashtable(var7);
      }

      return HashObjectUtils.dataObjectToHashtable(returnContactGroupData);
   }

   public String updateGroup(int userID, String[] keys, String[] values) {
      ContactGroupData contactGroupData = new ContactGroupData();
      HashObjectUtils.stringArrayToDataObject(keys, values, contactGroupData);

      try {
         ContactLocal contactBean = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
         contactBean.updateGroupDetail(userID, contactGroupData);
         return "TRUE";
      } catch (Exception var6) {
         return ExceptionHelper.getRootMessage(var6);
      }
   }

   public Hashtable creditCardPayment(String[] keys, String[] values, String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      CreditCardPaymentData paymentData = new CreditCardPaymentData();
      new CreditCardPaymentData();
      HashObjectUtils.stringArrayToDataObject(keys, values, paymentData);

      CreditCardPaymentData returnPaymentData;
      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         returnPaymentData = accountBean.creditCardPayment(paymentData, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
      } catch (Exception var10) {
         return ExceptionHelper.getRootMessageAsHashtable(var10);
      }

      return HashObjectUtils.dataObjectToHashtable(returnPaymentData);
   }

   public String sendTTNotification(String[] keys, String[] values) {
      MoneyTransferData moneyTransferData = new MoneyTransferData();
      HashObjectUtils.stringArrayToDataObject(keys, values, moneyTransferData);

      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         accountBean.moneyTransferTopup(moneyTransferData);
         return "TRUE";
      } catch (CreateException var5) {
         return ExceptionHelper.getRootMessage(var5);
      } catch (EJBException var6) {
         return var6.getCausedByException() != null ? ExceptionHelper.setErrorMessage(var6.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessage(var6.getMessage());
      }
   }

   public Hashtable redeemVoucher(String username, String voucherNumber, String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      try {
         VoucherLocal voucherBean = (VoucherLocal)EJBHomeCache.getLocalObject("VoucherLocal", VoucherLocalHome.class);
         VoucherData voucherData = voucherBean.redeemVoucher(username, voucherNumber, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return HashObjectUtils.dataObjectToHashtable(voucherData);
      } catch (CreateException var9) {
         return ExceptionHelper.getRootMessageAsHashtable(var9);
      } catch (EJBException var10) {
         return var10.getCausedByException() != null ? ExceptionHelper.setErrorMessageAsHashtable(var10.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessageAsHashtable(var10.getMessage());
      }
   }

   public Hashtable redeemBlueLabelVoucher(String username, String voucherNumber, String voucherValue) {
      try {
         VoucherLocal voucherBean = (VoucherLocal)EJBHomeCache.getLocalObject("VoucherLocal", VoucherLocalHome.class);
         BlueLabelVoucherData voucherData = voucherBean.redeemBlueLabelVoucher(username, voucherNumber, voucherValue);
         return HashObjectUtils.dataObjectToHashtable(voucherData);
      } catch (CreateException var6) {
         return ExceptionHelper.getRootMessageAsHashtable(var6);
      } catch (EJBException var7) {
         return var7.getCausedByException() != null ? ExceptionHelper.setErrorMessageAsHashtable(var7.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessageAsHashtable(var7.getMessage());
      }
   }

   public Hashtable getAffiliateOverview(String username, String currency) {
      Hashtable affiliateOverview = new Hashtable();
      CachedRowSetImpl crs = null;
      int rsSize = 0;
      DecimalFormat df = new DecimalFormat("0.00");

      try {
         VoucherLocal voucherBean = (VoucherLocal)EJBHomeCache.getLocalObject("VoucherLocal", VoucherLocalHome.class);
         crs = voucherBean.affiliateOverview(username);
      } catch (Exception var16) {
         return ExceptionHelper.getRootMessageAsHashtable(var16);
      }

      if (crs == null) {
         return ExceptionHelper.setErrorMessageAsHashtable("You have no voucher history available");
      } else {
         String totalNumberOfVouchers = null;
         String totalValueInVouchers = null;
         String totalNumberOfActiveVouchers = null;
         String totalValueOfActiveVouchers = null;
         String totalNumberOfCancelledVouchers = null;
         String totalValueOfCancelledVouchers = null;
         String totalNumberOfRedeemedVouchers = null;
         String totalValueOfRedeemedVouchers = null;

         try {
            if (crs.last()) {
               rsSize = crs.getRow();
            }

            if (rsSize > 0) {
               crs.beforeFirst();

               while(crs.next()) {
                  if (crs.getString("Totalvouchers") != null && Integer.parseInt(crs.getString("Totalvouchers")) >= 0) {
                     if (totalNumberOfVouchers == null) {
                        totalNumberOfVouchers = crs.getString("Totalvouchers") + (rsSize > 1 ? " (with " + crs.getString("Currency") + ")" : "");
                     } else {
                        totalNumberOfVouchers = totalNumberOfVouchers + " | " + crs.getString("Totalvouchers") + (rsSize > 1 ? " (with " + crs.getString("Currency") + ")" : "");
                     }
                  }

                  if (crs.getString("amount") != null) {
                     if (totalValueInVouchers == null) {
                        totalValueInVouchers = df.format(crs.getDouble("amount")) + " " + crs.getString("Currency");
                     } else {
                        totalValueInVouchers = totalValueInVouchers + " & " + df.format(crs.getDouble("amount")) + " " + crs.getString("currency");
                     }
                  }

                  if (crs.getString("activevouchers") != null && Integer.parseInt(crs.getString("activevouchers")) >= 0) {
                     if (totalNumberOfActiveVouchers == null) {
                        totalNumberOfActiveVouchers = crs.getString("activevouchers") + (rsSize > 1 ? " (with " + crs.getString("Currency") + ")" : "");
                     } else {
                        totalNumberOfActiveVouchers = totalNumberOfActiveVouchers + " | " + crs.getString("activevouchers") + (rsSize > 1 ? " (with " + crs.getString("Currency") + ")" : "");
                     }
                  }

                  if (crs.getString("activetotalamount") != null) {
                     if (totalValueOfActiveVouchers == null) {
                        totalValueOfActiveVouchers = df.format(crs.getDouble("activetotalamount")) + " " + crs.getString("Currency");
                     } else {
                        totalValueOfActiveVouchers = totalValueOfActiveVouchers + " & " + df.format(crs.getDouble("activetotalamount")) + " " + crs.getString("currency");
                     }
                  }

                  if (crs.getString("cancelledvouchers") != null && Integer.parseInt(crs.getString("cancelledvouchers")) >= 0) {
                     if (totalNumberOfCancelledVouchers == null) {
                        totalNumberOfCancelledVouchers = crs.getString("cancelledvouchers") + (rsSize > 1 ? " (with " + crs.getString("Currency") + ")" : "");
                     } else {
                        totalNumberOfCancelledVouchers = totalNumberOfCancelledVouchers + " | " + crs.getString("cancelledvouchers") + (rsSize > 1 ? " (with " + crs.getString("Currency") + ")" : "");
                     }
                  }

                  if (crs.getString("cancelledtotalamount") != null) {
                     if (totalValueOfCancelledVouchers == null) {
                        totalValueOfCancelledVouchers = df.format(crs.getDouble("cancelledtotalamount")) + " " + crs.getString("Currency");
                     } else {
                        totalValueOfCancelledVouchers = totalValueOfCancelledVouchers + " & " + df.format(crs.getDouble("cancelledtotalamount")) + " " + crs.getString("currency");
                     }
                  }

                  if (crs.getString("redeemedvouchers") != null && Integer.parseInt(crs.getString("redeemedvouchers")) >= 0) {
                     if (totalNumberOfRedeemedVouchers == null) {
                        totalNumberOfRedeemedVouchers = crs.getString("redeemedvouchers") + (rsSize > 1 ? " (with " + crs.getString("Currency") + ")" : "");
                     } else {
                        totalNumberOfRedeemedVouchers = totalNumberOfRedeemedVouchers + " | " + crs.getString("redeemedvouchers") + (rsSize > 1 ? " (with " + crs.getString("Currency") + ")" : "");
                     }
                  }

                  if (crs.getString("redeemedtotalamount") != null) {
                     if (totalValueOfRedeemedVouchers == null) {
                        totalValueOfRedeemedVouchers = df.format(crs.getDouble("redeemedtotalamount")) + " " + crs.getString("Currency");
                     } else {
                        totalValueOfRedeemedVouchers = totalValueOfRedeemedVouchers + " & " + df.format(crs.getDouble("redeemedtotalamount")) + " " + crs.getString("currency");
                     }
                  }
               }
            }
         } catch (SQLException var17) {
            return ExceptionHelper.getRootMessageAsHashtable(var17);
         }

         affiliateOverview.put("Total Number of Vouchers created", totalNumberOfVouchers != null ? totalNumberOfVouchers : "0.00 " + currency);
         affiliateOverview.put("Total Value in Vouchers created", totalValueInVouchers != null ? totalValueInVouchers : "0.00 " + currency);
         affiliateOverview.put("Total Number of Active Vouchers", totalNumberOfActiveVouchers != null ? totalNumberOfActiveVouchers : "0.00 " + currency);
         affiliateOverview.put("Total Value in Active Vouchers", totalValueOfActiveVouchers != null ? totalValueOfActiveVouchers : "0.00 " + currency);
         affiliateOverview.put("Total Number of Cancelled Vouchers", totalNumberOfCancelledVouchers != null ? totalNumberOfCancelledVouchers : "0.00 " + currency);
         affiliateOverview.put("Total Value in Cancelled Vouchers", totalValueOfCancelledVouchers != null ? totalValueOfCancelledVouchers : "0.00 " + currency);
         affiliateOverview.put("Total Number of Redeemed Vouchers", totalNumberOfRedeemedVouchers != null ? totalNumberOfRedeemedVouchers : "0.00 " + currency);
         affiliateOverview.put("Total Value in Redeemed Vouchers", totalValueOfRedeemedVouchers != null ? totalValueOfRedeemedVouchers : "0.00 " + currency);
         return affiliateOverview;
      }
   }

   public Vector getAffiliateRecentActivities(String username, String currency) {
      Vector recentActivities = new Vector();
      CachedRowSetImpl crs = null;
      SimpleDateFormat formatter = new SimpleDateFormat("d MMM yy HH:mm");

      try {
         VoucherLocal voucherBean = (VoucherLocal)EJBHomeCache.getLocalObject("VoucherLocal", VoucherLocalHome.class);
         crs = voucherBean.recentActivities(username, 10);
      } catch (Exception var8) {
         return ExceptionHelper.getRootMessageAsVector(var8);
      }

      if (crs == null) {
         return ExceptionHelper.setErrorMessageAsVector("There are no recent activities.");
      } else {
         try {
            while(crs.next()) {
               Hashtable hash = new Hashtable();
               hash.put("lastupdated", formatter.format(crs.getDate("lastupdated")));
               hash.put("number", crs.getString("number"));
               hash.put("status", crs.getInt("status") == 1 ? "Active" : (crs.getInt("status") == 2 ? "Cancelled" : (crs.getInt("status") == 3 ? "Redeemed" : (crs.getInt("status") == 4 ? "Expired" : (crs.getInt("status") == 0 ? "Inactive" : "Unknown")))));
               hash.put("notes", crs.getString("notes") != null ? crs.getString("notes") : "none");
               recentActivities.add(hash);
            }

            return recentActivities;
         } catch (SQLException var7) {
            return ExceptionHelper.getRootMessageAsVector(var7);
         }
      }
   }

   public Hashtable getAffiliateMoreStatistics(String username, String currency) {
      Hashtable moreStatistics = new Hashtable();
      int redeemNumberToday = false;
      int redeemNumberLast7 = false;
      boolean var6 = false;

      int redeemNumberToday;
      int redeemNumberLast7;
      int redeemNumberLast30;
      try {
         VoucherLocal voucherBean = (VoucherLocal)EJBHomeCache.getLocalObject("VoucherLocal", VoucherLocalHome.class);
         redeemNumberToday = voucherBean.recentRedeem(username, 0);
         redeemNumberLast7 = voucherBean.recentRedeem(username, 7);
         redeemNumberLast30 = voucherBean.recentRedeem(username, 30);
      } catch (Exception var8) {
         return ExceptionHelper.getRootMessageAsHashtable(var8);
      }

      moreStatistics.put("Number of vouchers redeemed today", redeemNumberToday);
      moreStatistics.put("Number of vouchers redeemed last 7 days", redeemNumberLast7);
      moreStatistics.put("Number of vouchers redeemed last 30 days", redeemNumberLast30);
      return moreStatistics;
   }

   public Vector getVoucherBatches(String username, int id, int page, int numEntries) throws EJBException {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;
      Vector voucherBatches = new Vector();
      int numRows = 0;
      int startEntry = page * numEntries + 1;
      int endEntry = startEntry + numEntries - 1;

      Vector var13;
      try {
         conn = this.dataSourceMaster.getConnection();
         String sql = "select vb.datecreated,vb.id, vb.currency, vb.amount, vb.numvoucher,\tvb.notes, vb.expirydate, (select count(*) from voucher where voucher.voucherbatchid = vb.id and voucher.status = 1) as active, (select count(*) from voucher where voucher.voucherbatchid = vb.id and voucher.status = 2) as cancelled, (select count(*) from voucher where voucher.voucherbatchid = vb.id and voucher.status = 3) as redeemed, (select count(*) from voucher where voucher.voucherbatchid = vb.id and voucher.status = 4) as expired from voucherbatch vb\twhere vb.username = ? ";
         if (id != -1) {
            sql = sql + "and vb.id = ? ";
         }

         sql = sql + "order by vb.datecreated desc";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         if (id != -1) {
            ps.setInt(2, id);
         }

         for(rs = ps.executeQuery(); rs.next(); ++numRows) {
         }

         rs.beforeFirst();
         Hashtable markerHash = new Hashtable();
         markerHash.put("page", page);
         markerHash.put("numEntries", numRows);
         markerHash.put("numPages", numRows / numEntries);
         voucherBatches.add(markerHash);
         if (endEntry > numRows) {
            endEntry = numRows;
         }

         while(rs.next()) {
            if (rs.getRow() >= startEntry) {
               Hashtable voucherBatchHash = new Hashtable();
               SimpleDateFormat df = new SimpleDateFormat("d MMM yy HH:mm");
               voucherBatchHash.put("id", rs.getInt("id"));
               voucherBatchHash.put("dateCreated", df.format(rs.getTimestamp("dateCreated")));
               voucherBatchHash.put("currency", rs.getString("currency"));
               voucherBatchHash.put("amount", rs.getDouble("amount"));
               voucherBatchHash.put("numvoucher", rs.getInt("numvoucher"));
               if (rs.getTimestamp("expirydate") != null) {
                  voucherBatchHash.put("expirydate", df.format(rs.getTimestamp("expirydate")));
               }

               if (rs.getString("notes") != null) {
                  voucherBatchHash.put("notes", rs.getString("notes"));
               }

               voucherBatchHash.put("num_active", rs.getInt("active"));
               voucherBatchHash.put("num_cancelled", rs.getInt("cancelled"));
               voucherBatchHash.put("num_redeemed", rs.getInt("redeemed"));
               voucherBatchHash.put("num_expired", rs.getInt("expired"));
               voucherBatches.add(voucherBatchHash);
               if (rs.getRow() >= endEntry) {
                  break;
               }
            }
         }

         Vector var36 = voucherBatches;
         return var36;
      } catch (Exception var33) {
         var13 = ExceptionHelper.getRootMessageAsVector(var33);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var32) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var31) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var30) {
            conn = null;
         }

      }

      return var13;
   }

   public Vector getAllVoucherBatches(String username) throws EJBException {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;
      Vector voucherBatches = new Vector();

      Vector var7;
      try {
         conn = this.dataSourceMaster.getConnection();
         String sql = "select vb.datecreated,vb.id, vb.currency, vb.amount, vb.numvoucher,\tvb.notes, vb.expirydate, (select count(*) from voucher where voucher.voucherbatchid = vb.id and voucher.status = 1) as active, (select count(*) from voucher where voucher.voucherbatchid = vb.id and voucher.status = 2) as cancelled, (select count(*) from voucher where voucher.voucherbatchid = vb.id and voucher.status = 3) as redeemed, (select count(*) from voucher where voucher.voucherbatchid = vb.id and voucher.status = 4) as expired, (select count(*) from voucher where voucher.voucherbatchid = vb.id and voucher.status = 0) as inactive from voucherbatch vb where vb.username = ? order by vb.id desc";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         rs = ps.executeQuery();

         while(rs.next()) {
            Hashtable voucherBatchHash = new Hashtable();
            SimpleDateFormat df = new SimpleDateFormat("d MMM yy");
            voucherBatchHash.put("id", rs.getInt("id"));
            voucherBatchHash.put("dateCreated", df.format(rs.getTimestamp("dateCreated")));
            voucherBatchHash.put("currency", rs.getString("currency"));
            voucherBatchHash.put("amount", rs.getDouble("amount"));
            voucherBatchHash.put("numvoucher", rs.getInt("numvoucher"));
            if (rs.getTimestamp("expirydate") != null) {
               voucherBatchHash.put("expirydate", df.format(rs.getTimestamp("expirydate")));
            }

            if (rs.getString("notes") != null) {
               voucherBatchHash.put("notes", rs.getString("notes"));
            }

            voucherBatchHash.put("num_active", rs.getInt("active"));
            voucherBatchHash.put("num_cancelled", rs.getInt("cancelled"));
            voucherBatchHash.put("num_redeemed", rs.getInt("redeemed"));
            voucherBatchHash.put("num_expired", rs.getInt("expired"));
            voucherBatchHash.put("num_inactive", rs.getInt("inactive"));
            voucherBatches.add(voucherBatchHash);
         }

         var7 = voucherBatches;
         return var7;
      } catch (Exception var26) {
         var7 = ExceptionHelper.getRootMessageAsVector(var26);
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

      return var7;
   }

   public Vector getVouchers(String username, int batchid, int type, int page, int numEntries, String sortCol, String sortDir) throws EJBException {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;
      Vector vouchers = new Vector();
      int numRows = 0;
      int startEntry = page * numEntries + 1;
      int endEntry = startEntry + numEntries - 1;

      Vector var16;
      try {
         conn = this.dataSourceMaster.getConnection();
         String sql = "select amount, currency, numvoucher, notes, (select count(*) from voucherbatch, voucher where voucherbatch.username = ? and voucher.voucherbatchid = voucherbatch.id and voucher.voucherbatchid = ? and voucher.status = 1) as active, (select count(*) from voucherbatch, voucher where voucherbatch.username = ? and voucher.voucherbatchid = voucherbatch.id and voucher.voucherbatchid = ? and voucher.status = 2) as cancelled, (select count(*) from voucherbatch, voucher where voucherbatch.username = ? and voucher.voucherbatchid = voucherbatch.id and voucher.voucherbatchid = ? and voucher.status = 3) as redeemed, (select count(*) from voucherbatch, voucher where voucherbatch.username = ? and voucher.voucherbatchid = voucherbatch.id and voucher.voucherbatchid = ? and voucher.status = 4) as expired, (select count(*) from voucherbatch, voucher where voucherbatch.username = ? and voucher.voucherbatchid = voucherbatch.id and voucher.voucherbatchid = ? and voucher.status = 0) as inactive from voucherbatch where id=? and username=?";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setInt(2, batchid);
         ps.setString(3, username);
         ps.setInt(4, batchid);
         ps.setString(5, username);
         ps.setInt(6, batchid);
         ps.setString(7, username);
         ps.setInt(8, batchid);
         ps.setString(9, username);
         ps.setInt(10, batchid);
         ps.setInt(11, batchid);
         ps.setString(12, username);
         rs = ps.executeQuery();
         Hashtable markerHash = new Hashtable();
         if (rs.first()) {
            markerHash.put("amount", rs.getDouble("amount"));
            markerHash.put("currency", rs.getString("currency"));
            markerHash.put("num_vouchers", rs.getInt("numvoucher"));
            markerHash.put("num_active", rs.getInt("active"));
            markerHash.put("num_cancelled", rs.getInt("cancelled"));
            markerHash.put("num_redeemed", rs.getInt("redeemed"));
            markerHash.put("num_expired", rs.getInt("expired"));
            markerHash.put("num_inactive", rs.getInt("inactive"));
            markerHash.put("notes", rs.getString("notes"));
         }

         rs.close();
         ps.close();
         sql = "select voucher.id, voucher.voucherbatchid, voucher.number, voucher.lastupdated, voucher.status, voucher.notes from voucherbatch, voucher where voucherbatch.username = ? and voucher.voucherbatchid =  voucherbatch.id and voucher.voucherbatchid = ? ";
         if (type != -1) {
            sql = sql + " and voucher.status = ? ";
         }

         if (sortCol != null && sortCol.length() > 0) {
            sql = sql + "order by voucher." + sortCol;
         } else {
            sql = sql + "order by voucher.lastupdated";
         }

         if (sortDir != null && sortDir.length() > 0) {
            sql = sql + " " + sortDir;
         } else {
            sql = sql + " desc";
         }

         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setInt(2, batchid);
         if (type != -1) {
            ps.setInt(3, type);
         }

         for(rs = ps.executeQuery(); rs.next(); ++numRows) {
         }

         if (endEntry > numRows) {
            endEntry = numRows;
         }

         markerHash.put("page", page);
         markerHash.put("numEntries", endEntry - startEntry + 1);
         markerHash.put("totalEntries", numRows);
         markerHash.put("numPages", numRows / numEntries);
         vouchers.add(markerHash);
         rs.beforeFirst();

         while(true) {
            if (rs.next()) {
               if (rs.getRow() < startEntry) {
                  continue;
               }

               Hashtable voucherHash = new Hashtable();
               SimpleDateFormat df = new SimpleDateFormat("d MMM yy HH:mm");
               voucherHash.put("id", rs.getInt("id"));
               voucherHash.put("voucherbatchid", rs.getInt("voucherbatchid"));
               voucherHash.put("number", rs.getString("number"));
               if (rs.getTimestamp("lastupdated") != null) {
                  voucherHash.put("lastupdated", df.format(rs.getTimestamp("lastupdated")));
               }

               voucherHash.put("status", VoucherData.StatusEnum.fromValue(rs.getInt("status")).toString());
               if (rs.getString("notes") != null) {
                  voucherHash.put("notes", rs.getString("notes"));
               }

               vouchers.add(voucherHash);
               if (rs.getRow() < endEntry) {
                  continue;
               }
            }

            Vector var39 = vouchers;
            return var39;
         }
      } catch (Exception var36) {
         var16 = ExceptionHelper.getRootMessageAsVector(var36);
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var33) {
            conn = null;
         }

      }

      return var16;
   }

   public String exportVoucherBatchToCSV(String username, int exportType, int batchID) {
      SimpleDateFormat formatter = new SimpleDateFormat("d MMM yy HH:mm");
      String csvString = "";

      try {
         VoucherLocal voucherBean = (VoucherLocal)EJBHomeCache.getLocalObject("VoucherLocal", VoucherLocalHome.class);
         csvString = "Voucher Batch ID,Voucher Number,Last Updated,Status,Notes\r\n";
         List<VoucherData> vList = null;
         vList = voucherBean.retrieveVouchers(username, batchID, exportType);
         VoucherData voucher;
         if (vList != null) {
            for(Iterator i$ = vList.iterator(); i$.hasNext(); csvString = csvString + voucher.voucherBatchID + "," + voucher.number + "," + (voucher.lastUpdated == null ? "" : formatter.format(voucher.lastUpdated)) + "," + voucher.status.toString() + "," + (voucher.notes == null ? "" : voucher.notes) + "\r\n") {
               voucher = (VoucherData)i$.next();
            }
         }

         return csvString;
      } catch (Exception var10) {
         return ExceptionHelper.getRootMessage(var10);
      }
   }

   public String createVoucherBatch(String username, String currency, String amount, int numVoucher, String notes, boolean initiallyInactive, String ipAddress, String sessionID, String mobileDevice, String userAgent) throws EJBException {
      try {
         VoucherLocal voucherBean = (VoucherLocal)EJBHomeCache.getLocalObject("VoucherLocal", VoucherLocalHome.class);
         int voucherBatchId = voucherBean.createVoucherBatch(username, currency, amount, numVoucher, notes, initiallyInactive, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return Integer.toString(voucherBatchId);
      } catch (CreateException var13) {
         return "Internal server error";
      } catch (FusionEJBException var14) {
         return ExceptionHelper.getRootMessage(var14);
      } catch (EJBException var15) {
         return ExceptionHelper.getRootMessage(var15);
      }
   }

   public Hashtable searchForVoucher(String username, String vouchernumber) throws EJBException {
      try {
         VoucherLocal voucherBean = (VoucherLocal)EJBHomeCache.getLocalObject("VoucherLocal", VoucherLocalHome.class);
         VoucherData voucher = voucherBean.searchForVoucher(username, vouchernumber);
         if (voucher == null) {
            return ExceptionHelper.setErrorMessageAsHashtable("Voucher Not Found");
         } else {
            SimpleDateFormat df = new SimpleDateFormat("d MMM yy HH:mm");
            Hashtable voucherHashEntry = new Hashtable();
            voucherHashEntry.put("id", voucher.id);
            voucherHashEntry.put("voucherbatchid", voucher.voucherBatchID);
            voucherHashEntry.put("number", voucher.number);
            if (voucher.lastUpdated != null) {
               voucherHashEntry.put("lastupdated", df.format(voucher.lastUpdated));
            }

            if (voucher.notes != null) {
               voucherHashEntry.put("notes", voucher.notes);
            }

            voucherHashEntry.put("status", voucher.status.toString());
            return voucherHashEntry;
         }
      } catch (Exception var7) {
         return ExceptionHelper.setErrorMessageAsHashtable("Search failed: " + var7.getMessage());
      }
   }

   public String cancelVoucher(String username, int voucherID, String ipAddress, String sessionID, String mobileDevice, String userAgent) throws EJBException {
      try {
         VoucherLocal voucherBean = (VoucherLocal)EJBHomeCache.getLocalObject("VoucherLocal", VoucherLocalHome.class);
         voucherBean.cancelVoucher(username, voucherID, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return "TRUE";
      } catch (CreateException var8) {
         return "Internal server error";
      } catch (EJBException var9) {
         return ExceptionHelper.getRootMessage(var9);
      }
   }

   public String cancelVoucherBatch(String username, int voucherBatchID, String ipAddress, String sessionID, String mobileDevice, String userAgent) throws EJBException {
      try {
         VoucherLocal voucherBean = (VoucherLocal)EJBHomeCache.getLocalObject("VoucherLocal", VoucherLocalHome.class);
         voucherBean.cancelVoucherBatch(username, voucherBatchID, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return "TRUE";
      } catch (CreateException var8) {
         return "Internal server error";
      } catch (EJBException var9) {
         return ExceptionHelper.getRootMessage(var9);
      }
   }

   public String changeActiveVoucherToInactive(String username, int voucherId) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      String var6;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update voucher, voucherbatch set voucher.status = ?, voucher.LastUpdated = now() where voucher.ID = ? and voucher.status = ? and voucher.voucherbatchid=voucherbatch.id and voucherbatch.username = ?");
         ps.setInt(1, VoucherData.StatusEnum.INACTIVE.value());
         ps.setInt(2, voucherId);
         ps.setInt(3, VoucherData.StatusEnum.ACTIVE.value());
         ps.setString(4, username);
         int numRowsUpdated = ps.executeUpdate();
         if (numRowsUpdated < 1) {
            var6 = ExceptionHelper.setErrorMessage("Unable to mark voucher as INACTIVE. Only vouchers you created that are currently ACTIVE may be marked as INACTIVE");
            return var6;
         }

         return "TRUE";
      } catch (Exception var22) {
         var6 = ExceptionHelper.setErrorMessage("Unable to mark voucher as INACTIVE: " + var22.getMessage());
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

      return var6;
   }

   public String changeInactiveVoucherToActive(String username, int voucherId) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      String var6;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update voucher, voucherbatch set voucher.status = ?, voucher.LastUpdated = now() where voucher.ID = ? and voucher.status = ? and voucher.voucherbatchid=voucherbatch.id and voucherbatch.username = ?");
         ps.setInt(1, VoucherData.StatusEnum.ACTIVE.value());
         ps.setInt(2, voucherId);
         ps.setInt(3, VoucherData.StatusEnum.INACTIVE.value());
         ps.setString(4, username);
         int numRowsUpdated = ps.executeUpdate();
         if (numRowsUpdated >= 1) {
            return "TRUE";
         }

         var6 = ExceptionHelper.setErrorMessage("Unable to mark voucher as ACTIVE. Only vouchers you created that are currently INACTIVE may be marked as ACTIVE");
         return var6;
      } catch (Exception var22) {
         var6 = ExceptionHelper.setErrorMessage("Unable to mark voucher as ACTIVE: " + var22.getMessage());
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

      return var6;
   }

   public String changeActiveVouchersInBatchToInactive(String username, int batchId) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      String var6;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update voucher, voucherbatch set voucher.status = ?, voucher.LastUpdated = now() where voucherbatch.ID = ? and voucher.status = ? and voucher.voucherbatchid=voucherbatch.id and voucherbatch.username = ?");
         ps.setInt(1, VoucherData.StatusEnum.INACTIVE.value());
         ps.setInt(2, batchId);
         ps.setInt(3, VoucherData.StatusEnum.ACTIVE.value());
         ps.setString(4, username);
         ps.executeUpdate();
         return "TRUE";
      } catch (Exception var20) {
         var6 = ExceptionHelper.setErrorMessage("Unable to change ACTIVE vouchers to INACTIVE: " + var20.getMessage());
      } finally {
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

   public String changeInactiveVouchersInBatchToActive(String username, int batchId) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      String var6;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update voucher, voucherbatch set voucher.status = ?, voucher.LastUpdated = now() where voucherbatch.ID = ? and voucher.status = ? and voucher.voucherbatchid=voucherbatch.id and voucherbatch.username = ?");
         ps.setInt(1, VoucherData.StatusEnum.ACTIVE.value());
         ps.setInt(2, batchId);
         ps.setInt(3, VoucherData.StatusEnum.INACTIVE.value());
         ps.setString(4, username);
         ps.executeUpdate();
         return "TRUE";
      } catch (Exception var20) {
         var6 = ExceptionHelper.setErrorMessage("Unable to change INACTIVE vouchers to ACTIVE: " + var20.getMessage());
      } finally {
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

   public String updateVoucherBatchNotes(String username, int voucherBatchId, String notes) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         String var7;
         try {
            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("update voucherbatch set notes = ? where id = ? and username = ?");
            ps.setString(1, notes);
            ps.setInt(2, voucherBatchId);
            ps.setString(3, username);
            int numRowsUpdated = ps.executeUpdate();
            if (numRowsUpdated < 1) {
               var7 = ExceptionHelper.setErrorMessage("Only voucher batches created by yourself may be updated");
               return var7;
            }
         } catch (Exception var23) {
            var7 = ExceptionHelper.setErrorMessage("Unable to update voucher batch: " + var23.getMessage());
            return var7;
         }
      } finally {
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

      return "TRUE";
   }

   public int[] creditCardPaymentFromMidlet(int[] packet, String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      try {
         byte[] ba = new byte[packet.length];

         for(int i = 0; i < packet.length; ++i) {
            ba[i] = (byte)packet[i];
         }

         FusionPacket fusionPkt = new FusionPacket();
         ByteArrayInputStream in = new ByteArrayInputStream(ba);
         fusionPkt.read((InputStream)in);
         FusionRequest fusionRequest = FusionPacketFactory.getSpecificRequest(fusionPkt);
         if (fusionRequest == null) {
            return new int[0];
         } else if (!(fusionRequest instanceof FusionPktRecharge)) {
            return new int[0];
         } else {
            FusionPktRecharge rechargePkt = (FusionPktRecharge)fusionRequest;
            FusionPacket[] returnPkts = rechargePkt.processRequest(new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
            byte[] returnContent = FusionPacket.toByteArray(returnPkts);
            int[] returnPacket = new int[returnContent.length];

            for(int i = 0; i < returnContent.length; ++i) {
               returnPacket[i] = returnContent[i];
            }

            return returnPacket;
         }
      } catch (Exception var15) {
         return new int[0];
      }
   }

   public boolean processSMSDeliveryReport(String providerTransactionID, String destination, int status, String ipAddress, String sessionID, String mobileDevice, String userAgent) throws EJBException {
      try {
         MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         SystemSMSData systemSMSData = messageEJB.getSystemSMS(providerTransactionID, destination);
         if (systemSMSData == null) {
            return false;
         } else {
            if (systemSMSData.type == SystemSMSData.TypeEnum.PREMIUM && systemSMSData.gateway == SystemProperty.getInt("IndosatPremiumSMSGatewayID")) {
               AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
               List<SubscriptionData> subscriptions = accountEJB.getSubscriptions(systemSMSData.username, SystemProperty.getInt("IndosatServiceID"));
               Iterator i$ = subscriptions.iterator();

               while(i$.hasNext()) {
                  SubscriptionData subscriptionData = (SubscriptionData)i$.next();
                  if (subscriptionData.status == SubscriptionData.StatusEnum.PENDING) {
                     accountEJB.updateSubscriptionBillingStatus(subscriptionData.username, subscriptionData.id, status == 2, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
                     break;
                  }
               }
            }

            return true;
         }
      } catch (Exception var14) {
         throw new EJBException(var14.getMessage());
      }
   }

   public String processMobileOrignatedSMS(String receiver, String sender, String text, boolean isPrepaidNumber, String ipAddress) {
      String helpSMS = SystemProperty.get("MobileOriginatedSMSHelpText", "");
      String username = null;
      boolean mobileVerified = false;
      AccountEntrySourceData accountEntrySourceData = new AccountEntrySourceData(WebBean.class);
      accountEntrySourceData.ipAddress = ipAddress;

      try {
         MobileOriginatedSMSData moSMSData = this.parseAndLogMobileOriginatedSMS(receiver, sender, text);
         if (sender.equals(SystemProperty.get("TwoWaySMSNumber"))) {
            return "TRUE";
         }

         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData userData = userEJB.loadUserFromMobilePhone(sender);
         if (userData != null) {
            username = userData.username;
            mobileVerified = userData.mobileVerified;
         }

         if (userData != null && !userData.mobileVerified && userData.merchantCreated != null) {
            userEJB.activateAccount(userData.username, userData.verificationCode, false, accountEntrySourceData);
            mobileVerified = true;
         }

         if (moSMSData.requiresAuthenticatedAccount() && !mobileVerified) {
            log.info("Unable to process MO SMS. Invalid user. Sender: " + sender + ". Text: " + text + ". Username: " + username + ". Authenticated: " + mobileVerified);
            return "TRUE";
         }

         switch(moSMSData.type) {
         case BALANCE_REQUEST:
            this.sendBalanceSMS(userData.username, sender, accountEntrySourceData);
            break;
         case CALLBACK:
            this.processMobileOriginatedSMSCallback(username, userData.password, sender, text);
            break;
         case VOUCHER_REDEMPTION:
            this.processMobileOriginatedSMSVoucherRedemption(username, sender, text, accountEntrySourceData);
            break;
         case INDOSAT_URL_DOWNLOAD:
            this.sendIndosatURLDownloadSMS(username, sender, SystemProperty.get("IndosatURLDownloadSMS"), accountEntrySourceData);
            break;
         case INDOSAT_SUBSCRIPTION:
            this.processIndosatSubscription(username, sender, mobileVerified, isPrepaidNumber, accountEntrySourceData);
            break;
         case INDOSAT_CANCEL_SUBSCRIPTION:
            this.cancelIndosatSubscription(username, sender, accountEntrySourceData);
            break;
         case UNKNOWN:
         default:
            if (moSMSData.text.length() == 0) {
               throw new Exception("No text in the message received");
            }
         }

         return "TRUE";
      } catch (CreateException var13) {
         log.warn("Unable to process MO SMS. Sender: " + sender + ". Text: " + text, var13);
         helpSMS = helpSMS.replaceAll("%1", "Server error");
      } catch (EJBException var14) {
         log.warn("Unable to process MO SMS. Sender: " + sender + ". Text: " + text, var14);
         helpSMS = helpSMS.replaceAll("%1", "Server error");
      } catch (Exception var15) {
         log.warn("Unable to process MO SMS. Sender: " + sender + ". Text: " + text, var15);
         helpSMS = helpSMS.replaceAll("%1", var15.getMessage());
      }

      if (username != null && mobileVerified && helpSMS.length() > 0) {
         this.sendHelpSMS(username, sender, helpSMS, accountEntrySourceData);
      }

      return "FALSE";
   }

   private MobileOriginatedSMSData parseAndLogMobileOriginatedSMS(String receiver, String sender, String text) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      MobileOriginatedSMSData var8;
      try {
         log.info("MO SMS Received. Receiver: " + receiver + " Sender: " + sender + " Text: " + text);
         MobileOriginatedSMSData moSMSData = new MobileOriginatedSMSData();
         moSMSData.dateCreated = new Date();
         moSMSData.receiver = receiver;
         moSMSData.sender = sender;
         moSMSData.text = text;
         text = text == null ? "" : this.stripExcessChars(text).trim().toLowerCase();
         if (text.length() == 0 || text.equals("yes")) {
            moSMSData.type = MobileOriginatedSMSData.TypeEnum.UNKNOWN;
         }

         if (text.matches("^(v|voucher)\\s*[0-9]+")) {
            moSMSData.type = MobileOriginatedSMSData.TypeEnum.VOUCHER_REDEMPTION;
         } else if ((text.startsWith("mi") || text.startsWith("ni")) && SystemProperty.get("IndosatShortCode").equals(receiver)) {
            moSMSData.type = MobileOriginatedSMSData.TypeEnum.INDOSAT_SUBSCRIPTION;
         } else if ((text.startsWith("unreg") || text.startsWith("stop")) && SystemProperty.get("IndosatShortCode").equals(receiver)) {
            moSMSData.type = MobileOriginatedSMSData.TypeEnum.INDOSAT_CANCEL_SUBSCRIPTION;
         } else if (!text.equals("bal") && !text.startsWith("balance")) {
            moSMSData.type = MobileOriginatedSMSData.TypeEnum.CALLBACK;
         } else {
            moSMSData.type = MobileOriginatedSMSData.TypeEnum.BALANCE_REQUEST;
         }

         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("insert into mobileoriginatedsms (datecreated, type, receiver, sender, text) values (?,?,?,?,?)", 1);
         ps.setTimestamp(1, new Timestamp(moSMSData.dateCreated.getTime()));
         ps.setInt(2, moSMSData.type.value());
         ps.setString(3, moSMSData.receiver);
         ps.setString(4, moSMSData.sender);
         ps.setString(5, moSMSData.text);
         ps.executeUpdate();
         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            throw new EJBException("Unable to write to MobileOriginatedSMS table");
         }

         moSMSData.id = rs.getInt(1);
         moSMSData.text = text;
         var8 = moSMSData;
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage());
      } catch (NoSuchFieldException var25) {
         throw new EJBException(var25.getMessage());
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

      return var8;
   }

   private void processMobileOriginatedSMSVoucherRedemption(String username, String sender, String text, AccountEntrySourceData accountEntrySourceData) {
      try {
         String voucherNumber = text.replaceAll("[^0-9]", "");
         if (username == null) {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData userData = userEJB.createPrepaidCardUser(sender, voucherNumber, accountEntrySourceData);
            userEJB.changeMobilePhone(userData.username, sender, accountEntrySourceData);
            username = userData.username;
            log.info("SMS voucher recharge success. Account " + userData.username + " created and voucher redeemed. Sender: " + sender + " Text: " + text);
         } else {
            VoucherLocal voucherEJB = (VoucherLocal)EJBHomeCache.getLocalObject("VoucherLocal", VoucherLocalHome.class);
            voucherEJB.redeemVoucher(username, voucherNumber, accountEntrySourceData);
            log.info("SMS voucher recharge success. Voucher redeemed. Sender: " + sender + " Text: " + text);
         }

         this.sendVoucherRechargeSMS(username, sender, voucherNumber, accountEntrySourceData);
      } catch (Exception var8) {
         log.warn("SMS voucher recharge failed. Sender: " + sender + ". Text: " + text, var8);
      }

   }

   private void processMobileOriginatedSMSCallback(String username, String password, String sender, String text) throws Exception {
      String destination;
      try {
         String origin;
         if (text.indexOf(42) == -1 && text.indexOf(35) == -1) {
            origin = sender;
            destination = text;
         } else {
            String[] parts = text.split("[\\*#]");
            if (parts.length < 2 || parts.length > 3) {
               throw new Exception("Bad instruction");
            }

            if (!parts[0].equalsIgnoreCase(password)) {
               throw new Exception("Bad password");
            }

            origin = parts.length == 3 ? parts[2] : sender;
            destination = parts[1];
         }

         MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);

         try {
            destination = messageEJB.cleanAndValidatePhoneNumber(this.getNumberOnly(destination), false);
            origin = messageEJB.cleanAndValidatePhoneNumber(this.getNumberOnly(origin), false);
         } catch (Exception var9) {
            throw new Exception("Bad number");
         }

         this.initiateCallback(username, origin, destination);
         log.info("SMS callback success. Sender: " + sender + " Text: " + text);
      } catch (EJBException var10) {
         destination = ExceptionHelper.getRootMessage(var10);
         if (destination.indexOf("You do not have enough credit") != -1) {
            throw new Exception("Low balance");
         } else if (destination.indexOf("You need to authenticate") != -1) {
            throw new Exception("A/C inactive");
         } else if (destination.indexOf("Origin and destination") != -1) {
            throw new Exception("Same number");
         } else {
            throw var10;
         }
      }
   }

   private void sendHelpSMS(String username, String mobilePhone, String messageText, AccountEntrySourceData accountEntrySourceData) {
      if (SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.SMS_CALLBACK_HELP, username)) {
         try {
            MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            int smsCount = messageEJB.getSystemSMSCount(SystemSMSData.SubTypeEnum.SMS_CALLBACK_HELP, mobilePhone);
            if (smsCount <= SystemProperty.getInt("MaxSMSCallbackHelpPerDay")) {
               SystemSMSData systemSMSData = new SystemSMSData();
               systemSMSData.username = username;
               systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
               systemSMSData.subType = SystemSMSData.SubTypeEnum.SMS_CALLBACK_HELP;
               systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
               systemSMSData.destination = mobilePhone;
               systemSMSData.messageText = messageText;
               messageEJB.sendSystemSMS(systemSMSData, accountEntrySourceData);
            }
         } catch (Exception var8) {
            System.out.println("SMS Callback Error 7: Unable to send help text to " + mobilePhone + ": " + var8.getMessage());
         }

      }
   }

   private void sendBalanceSMS(String username, String mobilePhone, AccountEntrySourceData accountEntrySourceData) {
      if (SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.SMS_CALLBACK_BALANCE, username)) {
         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData userData = userEJB.loadUserFromMobilePhone(mobilePhone);
            if (userData == null) {
               log.info("SMS callback failed. Unknown sender " + mobilePhone);
               return;
            }

            DecimalFormat df = new DecimalFormat("0.00");
            String balance = df.format(userData.balance) + " " + userData.currency;
            SystemSMSData systemSMSData = new SystemSMSData();
            systemSMSData.username = username;
            systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
            systemSMSData.subType = SystemSMSData.SubTypeEnum.SMS_CALLBACK_BALANCE;
            systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
            systemSMSData.destination = mobilePhone;
            systemSMSData.messageText = "Your migme balance is " + balance;
            MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            messageEJB.sendSystemSMS(systemSMSData, accountEntrySourceData);
            log.info("SMS callback success. Balance requested. Sender: " + mobilePhone);
         } catch (Exception var10) {
            log.warn("SMS callback failed. Sender: " + mobilePhone, var10);
            String helpSMS = SystemProperty.get("MobileOriginatedSMSHelpText", "");
            if (helpSMS.length() > 0) {
               this.sendHelpSMS(username, mobilePhone, helpSMS.replaceAll("%1", "Server error"), accountEntrySourceData);
            }
         }

      }
   }

   private void sendVoucherRechargeSMS(String username, String mobilePhone, String voucherNumber, AccountEntrySourceData accountEntrySourceData) {
      if (SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.SMS_VOUCHER_RECHARGE, username)) {
         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData userData = userEJB.loadUser(username, false, true);
            if (userData == null) {
               System.out.println("SMS Voucher Recharge Error: Unknown user " + username);
               return;
            }

            DecimalFormat df = new DecimalFormat("0.00");
            String balance = df.format(userData.balance) + " " + userData.currency;
            SystemSMSData systemSMSData = new SystemSMSData();
            systemSMSData.username = username;
            systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
            systemSMSData.subType = SystemSMSData.SubTypeEnum.SMS_VOUCHER_RECHARGE;
            systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
            systemSMSData.destination = mobilePhone;
            systemSMSData.messageText = "You redeemed voucher " + voucherNumber + ". Your migme balance is " + balance;
            MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            messageEJB.sendSystemSMS(systemSMSData, accountEntrySourceData);
         } catch (Exception var11) {
            String helpSMS = SystemProperty.get("MobileOriginatedSMSHelpText", "");
            if (helpSMS.length() > 0) {
               this.sendHelpSMS(username, mobilePhone, helpSMS.replaceAll("%1", "Server error"), accountEntrySourceData);
            }

            System.out.println("SMS Voucher Recharge Error: " + var11.getMessage() + " Sender: " + mobilePhone);
         }

      }
   }

   private void sendIndosatURLDownloadSMS(String username, String mobilePhone, String text, AccountEntrySourceData accountEntrySourceData) {
      if (SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.INDOSAT_URL_DOWNLOAD, username)) {
         try {
            SystemSMSData systemSMSData = new SystemSMSData();
            systemSMSData.username = username;
            systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
            systemSMSData.subType = SystemSMSData.SubTypeEnum.INDOSAT_URL_DOWNLOAD;
            systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
            systemSMSData.destination = mobilePhone;
            systemSMSData.messageText = text;
            MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            messageEJB.sendSystemSMS(systemSMSData, accountEntrySourceData);
            log.info("Indosat URL download success. Sender: " + mobilePhone + ". Username: " + username);
         } catch (Exception var7) {
            log.warn("Indosat URL download failed. Sender: " + mobilePhone + ". Username: " + username, var7);
         }

      }
   }

   private void processIndosatSubscription(String username, String mobilePhone, boolean mobileVerified, boolean isPrepaidNumber, AccountEntrySourceData accountEntrySourceData) {
      try {
         if (!isPrepaidNumber) {
            this.sendIndosatURLDownloadSMS(username, mobilePhone, "Mohon maaf, layanan ini hanya diperuntukkan untuk pelannggan Prepaid (IM3, Mentari)", accountEntrySourceData);
         } else if (mobileVerified) {
            int indosatServiceID = SystemProperty.getInt("IndosatServiceID");
            int indosatGroupID = SystemProperty.getInt("IndosatGroupID");
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            GroupMemberData groupMemberData = userEJB.getGroupMember(username, indosatGroupID);
            if (groupMemberData == null || groupMemberData.status == GroupMemberData.StatusEnum.INACTIVE) {
               String[] indosatIPs = SystemProperty.getArray("IndosatIPs");
               String indosatIPAddress = indosatIPs.length == 0 ? "" : indosatIPs[0];
               this.joinGroup(username, indosatGroupID, 0, indosatIPAddress, accountEntrySourceData.sessionID, accountEntrySourceData.mobileDevice, accountEntrySourceData.userAgent, false, true, true, false, false, false);
            }

            AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
            List<SubscriptionData> subscriptions = accountEJB.getSubscriptions(username, indosatServiceID);
            SubscriptionData subscriptionData = null;
            Iterator i$ = subscriptions.iterator();

            while(i$.hasNext()) {
               SubscriptionData subscription = (SubscriptionData)i$.next();
               switch(subscription.status) {
               case PENDING:
                  this.sendHelpSMS(username, mobilePhone, "We are not able to subscribe you to Indosat VIP Access at this time. You already have a pending subscription", accountEntrySourceData);
                  return;
               case ACTIVE:
                  subscriptionData = subscription;
               }
            }

            if (subscriptionData == null) {
               subscriptionData = accountEJB.subscribeService(username, indosatServiceID, accountEntrySourceData);
            }

            if (subscriptionData.type == SubscriptionData.TypeEnum.FREE_TRIAL) {
               this.sendIndosatURLDownloadSMS(username, mobilePhone, "Terima kasih telah mendaftar 7 hari Indosat VIP Access. Klik ke http://m.mig.me/indosat/trial/ untuk keterangan lebih lanjut", accountEntrySourceData);
            } else {
               this.sendIndosatURLDownloadSMS(username, mobilePhone, "Terimakasih telah berlangganan Indosat VIP Access. Sebentar lagi Anda akan menerima SMS untuk konfirmasi aktivasi akses 7 hari Anda", accountEntrySourceData);
            }
         } else {
            this.sendIndosatURLDownloadSMS(username, mobilePhone, "Selamat datang di Indosat VIP Access. Dapatkan GRATIS 7 hari pertama! Aktifkan sekarang di http://m.mig.me/indosat/vip/", accountEntrySourceData);
         }
      } catch (Exception var15) {
         log.warn("Unable to process Indosat subscription for user " + username, var15);
         this.sendHelpSMS(username, mobilePhone, "We are not able to subscribe you to Indosat VIP Access at this time. Server error", accountEntrySourceData);
      }

   }

   private void cancelIndosatSubscription(String username, String mobilePhone, AccountEntrySourceData accountEntrySourceData) {
      try {
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         List<SubscriptionData> subscriptions = accountEJB.getSubscriptions(username, SystemProperty.getInt("IndosatServiceID"));
         Iterator i$ = subscriptions.iterator();

         while(i$.hasNext()) {
            SubscriptionData subscription = (SubscriptionData)i$.next();
            if (subscription.status == SubscriptionData.StatusEnum.ACTIVE) {
               accountEJB.cancelSubscription(username, subscription.id);
            }
         }

         this.sendHelpSMS(username, mobilePhone, "Terima kasih. Anda tidak lagi berlangganan Indosat VIP Access di migme", accountEntrySourceData);
      } catch (Exception var8) {
         log.warn("Unable to cancel Indosat subscription for user " + username, var8);
         this.sendHelpSMS(username, mobilePhone, "We are not able to cancel your Indosat VIP Access at this time. Server error", accountEntrySourceData);
      }

   }

   private void initiateCallback(String username, String origin, String destination) throws CreateException, EJBException {
      VoiceLocal voiceEJB = (VoiceLocal)EJBHomeCache.getLocalObject("VoiceLocal", VoiceLocalHome.class);
      CallData callData = new CallData();
      callData.username = username;
      callData.source = origin;
      callData.destination = destination;
      callData.sourceType = CallData.SourceDestinationTypeEnum.PSTN_PHONE;
      callData.destinationType = CallData.SourceDestinationTypeEnum.PSTN_PHONE;
      callData.type = CallData.TypeEnum.SMS_CALLBACK;
      voiceEJB.initiatePhoneCall(callData);
   }

   private String stripExcessChars(String text) {
      StringBuffer sb = new StringBuffer();

      for(int i = 0; i < text.length(); ++i) {
         char ch = text.charAt(i);
         if (ch == '\n' || ch == '\r') {
            break;
         }

         if (ch != '"' && ch != '<' && ch != '>') {
            sb.append(ch);
         }
      }

      return sb.toString();
   }

   private String getNumberOnly(String text) {
      StringBuffer sb = new StringBuffer();

      for(int i = 0; i < text.length(); ++i) {
         char ch = text.charAt(i);
         if (ch >= '0' && ch <= '9') {
            sb.append(ch);
         } else if (ch == '\n' || ch == '\r') {
            break;
         }
      }

      return sb.toString();
   }

   public Vector getHandsetVendors() throws EJBException {
      Vector vec = new Vector();
      new ArrayList();

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         List<String> handsetVendors = misBean.getHandsetVendors();

         for(int i = 0; i < handsetVendors.size(); ++i) {
            String vendor = (String)handsetVendors.get(i);
            Hashtable hash = new Hashtable();
            hash.put("vendor", vendor);
            vec.add(hash);
         }

         return vec;
      } catch (Exception var7) {
         return ExceptionHelper.getRootMessageAsVector(var7);
      }
   }

   public Vector getHandsetVendorPrefixes() throws EJBException {
      Vector vec = new Vector();
      List handsetVendorPrefixList = null;

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         handsetVendorPrefixList = misBean.getHandsetVendorPrefixes();

         for(int i = 0; i < handsetVendorPrefixList.size(); ++i) {
            HandsetVendorPrefixesData handsetVendorPrefixesData = (HandsetVendorPrefixesData)handsetVendorPrefixList.get(i);
            Hashtable hash = HashObjectUtils.dataObjectToHashtable(handsetVendorPrefixesData);
            vec.add(hash);
         }

         return vec;
      } catch (Exception var7) {
         return ExceptionHelper.getRootMessageAsVector(var7);
      }
   }

   public Vector getHandsetDetails(String vendor) {
      Vector vec = new Vector();
      List handsetList = null;

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         handsetList = misBean.getHandsetDetails(vendor);

         for(int i = 0; i < handsetList.size(); ++i) {
            HandsetData handsetData = (HandsetData)handsetList.get(i);
            Hashtable hash = HashObjectUtils.dataObjectToHashtable(handsetData);
            vec.add(hash);
         }

         return vec;
      } catch (Exception var8) {
         return ExceptionHelper.getRootMessageAsVector(var8);
      }
   }

   public Vector getDefaultHandsetDetails() {
      Vector vec = new Vector();
      List handsetList = null;

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         handsetList = misBean.getDefaultHandsetDetails();

         for(int i = 0; i < handsetList.size(); ++i) {
            HandsetData handsetData = (HandsetData)handsetList.get(i);
            Hashtable hash = HashObjectUtils.dataObjectToHashtableWithNulls(handsetData);
            vec.add(hash);
         }

         return vec;
      } catch (Exception var7) {
         return ExceptionHelper.getRootMessageAsVector(var7);
      }
   }

   public String forgotPasswordWithMobileNumber(String mobile, String ipAddress, String mobileDevice, String userAgent) throws EJBException {
      log.info("forgotPasswordWithMobileNumber: IP [" + ipAddress + "] mobile no [" + mobile + "] device [" + mobileDevice + "] user agent [" + userAgent + "]");

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.forgotPasswordWithMobileNumberOrEmail(mobile, false, new AccountEntrySourceData(ipAddress, (String)null, mobileDevice, userAgent));
         return "TRUE";
      } catch (CreateException var6) {
         return ExceptionHelper.getRootMessage(var6);
      } catch (EJBException var7) {
         return ExceptionHelper.getRootMessage(var7);
      }
   }

   public String forgotPasswordWithEmail(String email, String ipAddress, String mobileDevice, String userAgent) throws EJBException {
      log.info("forgotPasswordWithEmail: IP [" + ipAddress + "] email [" + email + "] device [" + mobileDevice + "] user agent [" + userAgent + "]");

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.forgotPasswordWithMobileNumberOrEmail(email, true, new AccountEntrySourceData(ipAddress, (String)null, mobileDevice, userAgent));
         return "TRUE";
      } catch (CreateException var6) {
         return ExceptionHelper.getRootMessage(var6);
      } catch (EJBException var7) {
         return ExceptionHelper.getRootMessage(var7);
      }
   }

   public String changePassword(String username, String oldPassword, String newPassword) throws EJBException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.changePassword(username, oldPassword, newPassword);
         return "TRUE";
      } catch (CreateException var5) {
         return ExceptionHelper.getRootMessage(var5);
      } catch (EJBException var6) {
         return ExceptionHelper.getRootMessage(var6);
      }
   }

   public Hashtable getCountryFromIPNumber(String ipNumber) {
      Hashtable countryDataSOAP = null;
      CountryData countryData = null;

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         countryData = misBean.getCountryFromIPNumber(Double.parseDouble(ipNumber));
      } catch (Exception var5) {
         return ExceptionHelper.getRootMessageAsHashtable(var5);
      }

      if (countryData == null) {
         return null;
      } else {
         countryDataSOAP = HashObjectUtils.dataObjectToHashtable(countryData);
         return countryDataSOAP;
      }
   }

   public boolean consoleOut(String message) {
      System.out.println("PHP: " + message);
      return true;
   }

   public String loginFailed(String username) {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.loginFailed(username);
         return "TRUE";
      } catch (CreateException var3) {
         return ExceptionHelper.getRootMessage(var3);
      } catch (EJBException var4) {
         return var4.getCausedByException() != null ? ExceptionHelper.setErrorMessage(var4.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessage(var4.getMessage());
      }
   }

   public String loginSucceeded(String username) {
      return "TRUE";
   }

   public String loginSucceeded(String username, String mobileDevice, String userAgent, String language) {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.loginSucceeded(username, mobileDevice, userAgent, language);
         return "TRUE";
      } catch (CreateException var6) {
         return ExceptionHelper.getRootMessage(var6);
      } catch (EJBException var7) {
         return var7.getCausedByException() != null ? ExceptionHelper.setErrorMessage(var7.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessage(var7.getMessage());
      }
   }

   public String getBankTransferProductID(int countryID) {
      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         return String.valueOf(accountBean.getBankTransferProductID(countryID));
      } catch (CreateException var3) {
         return ExceptionHelper.getRootMessage(var3);
      } catch (EJBException var4) {
         return var4.getCausedByException() != null ? ExceptionHelper.setErrorMessage(var4.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessage(var4.getMessage());
      }
   }

   public Hashtable bankTransfer(String username, int paymentProductID, int countryID, String surname, String fiscalNumber, float amount, String currency) {
      try {
         BankTransferIntentData bankTransferIntentData = new BankTransferIntentData();
         bankTransferIntentData.username = username;
         bankTransferIntentData.paymentProductID = paymentProductID;
         bankTransferIntentData.countryID = countryID == 0 ? null : countryID;
         bankTransferIntentData.surname = surname;
         bankTransferIntentData.fiscalNumber = fiscalNumber;
         bankTransferIntentData.amount = (double)amount;
         bankTransferIntentData.currency = currency;
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         bankTransferIntentData = accountBean.bankTransfer(bankTransferIntentData);
         return HashObjectUtils.dataObjectToHashtable(bankTransferIntentData);
      } catch (CreateException var10) {
         return ExceptionHelper.getRootMessageAsHashtable(var10);
      } catch (EJBException var11) {
         return ExceptionHelper.getRootMessageAsHashtable(var11);
      }
   }

   public Hashtable bankTransfer(String username, int paymentProductID, int countryID, String firstname, String middlename, String surname, String fiscalNumber, float amount, String currency) {
      try {
         BankTransferIntentData bankTransferIntentData = new BankTransferIntentData();
         bankTransferIntentData.username = username;
         bankTransferIntentData.paymentProductID = paymentProductID;
         bankTransferIntentData.countryID = countryID == 0 ? null : countryID;
         bankTransferIntentData.firstname = firstname;
         bankTransferIntentData.middlename = middlename;
         bankTransferIntentData.surname = surname;
         bankTransferIntentData.fiscalNumber = fiscalNumber;
         bankTransferIntentData.amount = (double)amount;
         bankTransferIntentData.currency = currency;
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         bankTransferIntentData = accountBean.bankTransfer(bankTransferIntentData);
         return HashObjectUtils.dataObjectToHashtable(bankTransferIntentData);
      } catch (CreateException var12) {
         return ExceptionHelper.getRootMessageAsHashtable(var12);
      } catch (EJBException var13) {
         return ExceptionHelper.getRootMessageAsHashtable(var13);
      }
   }

   public String registerMerchant(String[] keys, String[] values) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      AffiliateData affiliateData = new AffiliateData();
      UserData userData = null;
      HashObjectUtils.stringArrayToDataObject(keys, values, affiliateData);
      Boolean registrationWithoutMobileEnabled = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MERCHANT_REGISTRATION_WITHOUT_MOBILE_ENABLED);

      try {
         String var10;
         try {
            String var73;
            if (affiliateData.username == null) {
               var73 = ExceptionHelper.setErrorMessage("Please enter your migme Username");
               return var73;
            } else if (affiliateData.firstName == null) {
               var73 = ExceptionHelper.setErrorMessage("Please enter a First Name");
               return var73;
            } else if (affiliateData.lastName == null) {
               var73 = ExceptionHelper.setErrorMessage("Please enter a Last Name");
               return var73;
            } else if (affiliateData.emailAddress == null) {
               var73 = ExceptionHelper.setErrorMessage("Please enter your Email Address");
               return var73;
            } else if (affiliateData.mobilePhone == null && !registrationWithoutMobileEnabled) {
               var73 = ExceptionHelper.setErrorMessage("Please enter your Mobile Phone");
               return var73;
            } else {
               UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               userData = userBean.loadUser(affiliateData.username, false, true);
               if (userData == null) {
                  var10 = ExceptionHelper.setErrorMessage("You must be a migme user to register as a merchant");
                  return var10;
               } else {
                  if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.PT74760224_ENABLED)) {
                     String var11;
                     if (StringUtils.hasLength(affiliateData.password)) {
                        AuthenticationServiceResponseCodeEnum res = userBean.validateUserCredential(affiliateData.username, affiliateData.password, PasswordType.FUSION);
                        if (res != AuthenticationServiceResponseCodeEnum.Success) {
                           var11 = ExceptionHelper.setErrorMessage("Please enter the correct password for your username");
                           return var11;
                        }
                     } else {
                        SSOLoginData loginData = SSOLogin.getLoginDataFromMemcache(affiliateData.sessionId);
                        if (loginData == null) {
                           var11 = ExceptionHelper.setErrorMessage("Please login to migme first");
                           return var11;
                        }

                        if (!loginData.username.equals(affiliateData.username)) {
                           var11 = ExceptionHelper.setErrorMessage("Please login to migme first");
                           return var11;
                        }
                     }
                  } else if (!affiliateData.password.equals(userData.password)) {
                     var10 = ExceptionHelper.setErrorMessage("Please enter the correct password for your username");
                     return var10;
                  }

                  if (userData.type != UserData.TypeEnum.MIG33) {
                     var10 = ExceptionHelper.setErrorMessage("You cannot register as a merchant because you are already a merchant");
                     return var10;
                  } else if ((!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MERCHANT_REGISTRATION_NEW_USER_ENABLED) || affiliateData.fromUserRegistration == null || !affiliateData.fromUserRegistration.equalsIgnoreCase("true")) && !AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.REGISTER_AS_MERCHANT, userData) && SystemProperty.getBool("MerchantRegistrationDisabledForUnauthenticatedUsers", false)) {
                     var10 = ExceptionHelper.setErrorMessage("Please login and authenticate your migme account before signing up for the merchant program.");
                     return var10;
                  } else {
                     if (!registrationWithoutMobileEnabled || affiliateData.registerWithoutMobile == null || !affiliateData.registerWithoutMobile.equalsIgnoreCase("true")) {
                        if (StringUtil.isBlank(userData.mobilePhone)) {
                           userBean.changeMobilePhone(userData.username, affiliateData.mobilePhone, true, new AccountEntrySourceData(affiliateData.registrationIpAddress, affiliateData.sessionId, affiliateData.mobileDevice, affiliateData.userAgent));
                           userData.mobilePhone = affiliateData.mobilePhone;
                           userBean.updateUserDetail(userData);
                           log.info(String.format("Updated mobile phone of user %s to %s as part of the merchant registration process", affiliateData.username, affiliateData.mobilePhone));
                        } else if (!userData.mobilePhone.equals(affiliateData.mobilePhone)) {
                           var10 = ExceptionHelper.setErrorMessage("Please enter your correct Mobile Phone");
                           return var10;
                        }
                     }

                     userData.type = UserData.TypeEnum.MIG33_MERCHANT;
                     userBean.updateUserDetail(userData);
                     affiliateData.dateRegistered = new Date();
                     conn = this.dataSourceMaster.getConnection();
                     ps = conn.prepareStatement("insert into affiliate (username, emailaddress, firstname, lastname, additionalinfo, countryIdDetected, registrationIpAddress, dateRegistered)  values (?,?,?,?,?,?,?,?)");
                     ps.setString(1, affiliateData.username);
                     ps.setString(2, affiliateData.emailAddress);
                     ps.setString(3, affiliateData.firstName);
                     ps.setString(4, affiliateData.lastName);
                     ps.setString(5, affiliateData.additionalInfo);
                     ps.setObject(6, affiliateData.countryIdDetected);
                     ps.setString(7, affiliateData.registrationIpAddress);
                     ps.setTimestamp(8, new Timestamp(affiliateData.dateRegistered.getTime()));
                     if (ps.executeUpdate() >= 1) {
                        return "TRUE";
                     } else {
                        var10 = ExceptionHelper.setErrorMessage("There was an error while entering your details");
                        return var10;
                     }
                  }
               }
            }
         } catch (Exception var71) {
            var10 = ExceptionHelper.getRootMessage(var71);
            return var10;
         }
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var70) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var69) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var68) {
            conn = null;
         }

      }
   }

   public Hashtable loadMerchant(String username) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var6;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from affiliate where username = ?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            Hashtable var29 = ExceptionHelper.setErrorMessageAsHashtable("Invalid merchant name " + username);
            return var29;
         }

         AffiliateData affiliateData = new AffiliateData();
         affiliateData.code = rs.getString("code");
         affiliateData.password = rs.getString("password");
         affiliateData.id = rs.getInt("ID");
         affiliateData.name = rs.getString("name");
         affiliateData.commission = (Double)rs.getObject("commission");
         affiliateData.lastlogindate = rs.getTimestamp("lastlogindate");
         affiliateData.referredBy = rs.getString("referredBy");
         affiliateData.username = rs.getString("username");
         affiliateData.emailAddress = rs.getString("emailAddress");
         affiliateData.firstName = rs.getString("firstName");
         affiliateData.lastName = rs.getString("lastName");
         affiliateData.additionalInfo = rs.getString("additionalInfo");
         affiliateData.countryIdDetected = (Integer)rs.getObject("countryIdDetected");
         affiliateData.registrationIpAddress = rs.getString("registrationIpAddress");
         affiliateData.dateRegistered = rs.getTimestamp("dateRegistered");
         affiliateData.mobilePhone = null;
         var6 = HashObjectUtils.dataObjectToHashtable(affiliateData);
         return var6;
      } catch (SQLException var27) {
         var6 = ExceptionHelper.setErrorMessageAsHashtable(var27.getMessage());
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var24) {
            conn = null;
         }

      }

      return var6;
   }

   public String isBuzzPossible(String senderUsername, int contactId) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      ContactData contact;
      try {
         ContactLocal contactBean = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
         contact = contactBean.getContact(contactId);
      } catch (Exception var55) {
         return ExceptionHelper.getRootMessage(var55);
      }

      if (contact != null && contact.fusionUsername != null && contact.fusionUsername.length() != 0) {
         try {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("select id from contact where username=? and fusionusername=? and status=1");
            ps.setString(1, contact.fusionUsername);
            ps.setString(2, senderUsername);
            rs = ps.executeQuery();
            String var58;
            if (!rs.next()) {
               var58 = ExceptionHelper.setErrorMessage(" ");
               return var58;
            }

            rs.close();
            ps.close();
            ps = conn.prepareStatement("select * from blocklist where username=? and blockusername=?");
            ps.setString(1, contact.fusionUsername);
            ps.setString(2, senderUsername);
            rs = ps.executeQuery();
            if (rs.next()) {
               var58 = ExceptionHelper.setErrorMessage(" ");
               return var58;
            }

            rs.close();
            ps.close();
            ps = conn.prepareStatement("select * from blocklist where username=? and blockusername=?");
            ps.setString(1, senderUsername);
            ps.setString(2, contact.fusionUsername);
            rs = ps.executeQuery();
            if (rs.next()) {
               var58 = ExceptionHelper.setErrorMessage("You have " + contact.fusionUsername + " on your block list");
               return var58;
            }

            rs.close();
            ps.close();
            ps = conn.prepareStatement("select mobilephone, mobileverified, allowbuzz from user where username=?");
            ps.setString(1, contact.fusionUsername);
            rs = ps.executeQuery();
            if (!rs.next()) {
               var58 = ExceptionHelper.setErrorMessage("Sorry, an internal error occurred: Unable to load recipient's mobile phone number");
               return var58;
            }

            if (!rs.getBoolean("mobileverified")) {
               var58 = ExceptionHelper.setErrorMessage("You can only Buzz users with authenticated accounts");
               return var58;
            }

            if (!rs.getBoolean("allowbuzz")) {
               var58 = ExceptionHelper.setErrorMessage(" ");
               return var58;
            }

            String recipientMobilePhone = rs.getString("mobilephone");
            rs.close();
            ps.close();
            ps = conn.prepareStatement("select count(*) from systemsms where username=? and type=1 and subtype=14 and datecreated > DATE_SUB(now(), INTERVAL 1 DAY)");
            ps.setString(1, senderUsername);
            rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) >= SystemProperty.getInt("MaxBuzzPerDay")) {
               var58 = ExceptionHelper.setErrorMessage("You have reached the limit of sending Buzzes for today");
               return var58;
            }

            rs.close();
            ps.close();
            ps = conn.prepareStatement("select count(*) from systemsms where username=? and type=1 and subtype=14 and destination=? and datecreated > DATE_SUB(now(), INTERVAL 1 DAY)");
            ps.setString(1, senderUsername);
            ps.setString(2, recipientMobilePhone);
            rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) >= SystemProperty.getInt("MaxBuzzToNumberPerDay")) {
               var58 = ExceptionHelper.setErrorMessage("You have reached the limit of sending a Buzz to " + contact.fusionUsername + " today");
               return var58;
            }

            rs.close();
            ps.close();
            ps = conn.prepareStatement("select balance/exchangerate from user, currency where user.currency=currency.code and user.username=?");
            ps.setString(1, senderUsername);
            rs = ps.executeQuery();
            rs.next();
            if (rs.getDouble(1) < SystemProperty.getDouble("BuzzSMSCost")) {
               var58 = ExceptionHelper.setErrorMessage("You do not have enough credit to send a Buzz");
               return var58;
            }

            rs.close();
            ps.close();
         } catch (Exception var56) {
            String var9 = ExceptionHelper.setErrorMessage("An internal error occurred: " + var56.getMessage());
            return var9;
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var54) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var53) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var52) {
               conn = null;
            }

         }

         return "TRUE";
      } else {
         return ExceptionHelper.setErrorMessage("Please provide a valid migme contact");
      }
   }

   public String sendBuzz(String senderUsername, int contactId, String message, String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      if (!SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.BUZZ, "contact id=" + contactId)) {
         return "Not sent as BUZZ sms sending disabled";
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         double smsBuzzCost = 0.0D;

         ContactData contact;
         try {
            ContactLocal contactBean = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
            contact = contactBean.getContact(contactId);
         } catch (Exception var61) {
            return ExceptionHelper.getRootMessage(var61);
         }

         if (contact.fusionUsername != null && contact.fusionUsername.length() != 0) {
            String recipientMobilePhone;
            label529: {
               String var65;
               try {
                  conn = this.dataSourceSlave.getConnection();
                  ps = conn.prepareStatement("select id from contact where username=? and fusionusername=? and status=1");
                  ps.setString(1, contact.fusionUsername);
                  ps.setString(2, senderUsername);
                  rs = ps.executeQuery();
                  if (!rs.next()) {
                     var65 = ExceptionHelper.setErrorMessage("Sorry, you are not permitted to Buzz " + contact.fusionUsername + " (they may not have you on their contact list, or they may not want to receive Buzz messages)");
                     return var65;
                  }

                  rs.close();
                  ps.close();
                  ps = conn.prepareStatement("select * from blocklist where username=? and blockusername=?");
                  ps.setString(1, contact.fusionUsername);
                  ps.setString(2, senderUsername);
                  rs = ps.executeQuery();
                  if (!rs.next()) {
                     rs.close();
                     ps.close();
                     ps = conn.prepareStatement("select * from blocklist where username=? and blockusername=?");
                     ps.setString(1, senderUsername);
                     ps.setString(2, contact.fusionUsername);
                     rs = ps.executeQuery();
                     if (rs.next()) {
                        var65 = ExceptionHelper.setErrorMessage("You have " + contact.fusionUsername + " on your block list");
                        return var65;
                     }

                     rs.close();
                     ps.close();
                     ps = conn.prepareStatement("select u.mobilephone as mobilephone, u.mobileverified as mobileverified, u.allowbuzz as allowbuzz, c.smsbuzzcost as smsbuzzcost from user u, country c where u.username=? and c.id=u.countryid");
                     ps.setString(1, contact.fusionUsername);
                     rs = ps.executeQuery();
                     if (!rs.next()) {
                        var65 = ExceptionHelper.setErrorMessage("Sorry, an internal error occurred: Unable to load recipient's mobile phone number");
                        return var65;
                     }

                     if (!rs.getBoolean("mobileverified")) {
                        var65 = ExceptionHelper.setErrorMessage("Sorry, you can only Buzz users with authenticated accounts");
                        return var65;
                     }

                     if (!rs.getBoolean("allowbuzz")) {
                        var65 = ExceptionHelper.setErrorMessage("Sorry, you are not permitted to Buzz " + contact.fusionUsername + " (they may not have you on their contact list, or they may not want to receive Buzz messages)");
                        return var65;
                     }

                     recipientMobilePhone = rs.getString("mobilephone");
                     smsBuzzCost = rs.getDouble("smsbuzzcost");
                     rs.close();
                     ps.close();
                     ps = conn.prepareStatement("select count(*) from systemsms where username=? and type=1 and subtype=14 and datecreated > DATE_SUB(now(), INTERVAL 1 DAY)");
                     ps.setString(1, senderUsername);
                     rs = ps.executeQuery();
                     rs.next();
                     if (rs.getInt(1) >= SystemProperty.getInt("MaxBuzzPerDay")) {
                        var65 = ExceptionHelper.setErrorMessage("You have reached the limit of sending Buzzes for today");
                        return var65;
                     }

                     rs.close();
                     ps.close();
                     ps = conn.prepareStatement("select count(*) from systemsms where username=? and type=1 and subtype=14 and destination=? and datecreated > DATE_SUB(now(), INTERVAL 1 DAY)");
                     ps.setString(1, senderUsername);
                     ps.setString(2, recipientMobilePhone);
                     rs = ps.executeQuery();
                     rs.next();
                     if (rs.getInt(1) < SystemProperty.getInt("MaxBuzzToNumberPerDay")) {
                        break label529;
                     }

                     var65 = ExceptionHelper.setErrorMessage("You have reached the limit of sending a Buzz to " + contact.fusionUsername + " today");
                     return var65;
                  }

                  var65 = ExceptionHelper.setErrorMessage("Sorry, you are not permitted to Buzz " + contact.fusionUsername + " (they may not have you on their contact list, or they may not want to receive Buzz messages)");
               } catch (Exception var63) {
                  String var17 = ExceptionHelper.setErrorMessage("Sorry, an internal error occurred: " + var63.getMessage());
                  return var17;
               } finally {
                  try {
                     if (rs != null) {
                        rs.close();
                     }
                  } catch (SQLException var60) {
                     rs = null;
                  }

                  try {
                     if (ps != null) {
                        ps.close();
                     }
                  } catch (SQLException var59) {
                     ps = null;
                  }

                  try {
                     if (conn != null) {
                        conn.close();
                     }
                  } catch (SQLException var58) {
                     conn = null;
                  }

               }

               return var65;
            }

            try {
               String messageText;
               if (message != null && message.length() > 0) {
                  messageText = SystemProperty.get("BuzzSMS").replaceAll("%1", senderUsername).replaceAll("%2", contact.fusionUsername).replaceAll("%3", message);
               } else {
                  messageText = SystemProperty.get("BuzzSMSNoUserMessage").replaceAll("%1", senderUsername).replaceAll("%2", contact.fusionUsername);
               }

               SystemSMSData systemSMSData = new SystemSMSData();
               systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
               systemSMSData.subType = SystemSMSData.SubTypeEnum.BUZZ;
               systemSMSData.username = senderUsername;
               systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
               systemSMSData.destination = recipientMobilePhone;
               systemSMSData.messageText = messageText;
               systemSMSData.cost = smsBuzzCost;
               MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
               messageEJB.sendSystemSMS(systemSMSData, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
               return messageText;
            } catch (Exception var62) {
               return ExceptionHelper.getRootMessage(var62);
            }
         } else {
            return ExceptionHelper.setErrorMessage("Invalid contact");
         }
      }
   }

   public String createLookout(String creatorUsername, String contactUsername) {
      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userEJB.createLookout(creatorUsername, contactUsername);
         return "TRUE";
      } catch (Exception var4) {
         return ExceptionHelper.getRootMessage(var4);
      }
   }

   public String isLookoutPossible(String creatorUsername, String contactUsername) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from blocklist where username=? and blockusername=?");
         ps.setString(1, creatorUsername);
         ps.setString(2, contactUsername);
         rs = ps.executeQuery();
         if (rs.next()) {
            var6 = ExceptionHelper.setErrorMessage("You have " + contactUsername + " on your block list");
            return var6;
         }

         rs.close();
         ps.close();
         ps = conn.prepareStatement("select * from blocklist where username=? and blockusername=?");
         ps.setString(1, contactUsername);
         ps.setString(2, creatorUsername);
         rs = ps.executeQuery();
         if (!rs.next()) {
            rs.close();
            ps.close();
            ps = conn.prepareStatement("select id from contact where username=? and fusionusername=? and status=1");
            ps.setString(1, contactUsername);
            ps.setString(2, creatorUsername);
            rs = ps.executeQuery();
            if (rs.next()) {
               return "TRUE";
            }

            var6 = ExceptionHelper.setErrorMessage(contactUsername + " may not have you on their contact list");
            return var6;
         }

         var6 = ExceptionHelper.setErrorMessage(contactUsername + " may not have you on their contact list");
      } catch (SQLException var34) {
         String var7 = ExceptionHelper.setErrorMessage("Sorry, an internal error occurred: " + var34.getMessage());
         return var7;
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var31) {
            conn = null;
         }

      }

      return var6;
   }

   public int lookoutExists(String creatorUsername, String contactUsername) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      byte var7;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select username from lookout where username=? and contactusername=?");
         ps.setString(1, creatorUsername);
         ps.setString(2, contactUsername);
         rs = ps.executeQuery();
         byte var6;
         if (rs.next()) {
            var6 = 1;
            return var6;
         }

         var6 = 0;
         return var6;
      } catch (SQLException var28) {
         var7 = 0;
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

      return var7;
   }

   public String removeLookout(String creatorUsername, String contactUsername) {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      String var7;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("delete from lookout where username=? and contactusername=?");
         ps.setString(1, creatorUsername);
         ps.setString(2, contactUsername);
         ps.executeUpdate();
         return "TRUE";
      } catch (SQLException var25) {
         var7 = ExceptionHelper.setErrorMessage("Sorry, an internal error occurred: " + var25.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
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

   public Vector getLookouts(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector contacts = new Vector();

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select contactusername from lookout where username=?");
         ps.setString(1, username);
         rs = ps.executeQuery();

         while(rs.next()) {
            contacts.add(rs.getString(1));
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

      return contacts;
   }

   public String setAllowBuzz(String username, boolean allow) {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      String var7;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update user set allowbuzz = ? where username = ?");
         if (allow) {
            ps.setInt(1, 1);
         } else {
            ps.setInt(1, 0);
         }

         ps.setString(2, username);
         if (ps.executeUpdate() < 1) {
            String var6 = ExceptionHelper.setErrorMessage("Sorry, an internal error occurred: User does not exist");
            return var6;
         }

         return "TRUE";
      } catch (SQLException var28) {
         var7 = ExceptionHelper.setErrorMessage("Sorry, an internal error occurred: " + var28.getMessage());
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var25) {
            conn = null;
         }

      }

      return var7;
   }

   public Hashtable getApplicableDiscountTier(int paymentType, String username, double amount) {
      new DiscountTierData();

      DiscountTierData returnDiscountTierData;
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData user = userBean.loadUser(username, false, false);
         if (user == null) {
            throw new Exception("Invalid username " + username);
         }

         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         CountryData country = misBean.getCountry(user.countryID);
         if (country == null) {
            throw new Exception("Invalid country ID " + user.countryID);
         }

         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         CurrencyData currency;
         if (paymentType == Enums.PaymentEnum.BANK_TRANSFER.value()) {
            currency = misBean.getCurrency(country.bankTransferCurrency);
         } else if (paymentType == Enums.PaymentEnum.WESTERN_UNION.value()) {
            currency = misBean.getCurrency(country.westernUnionCurrency);
         } else if (paymentType == Enums.PaymentEnum.CREDIT_CARD.value()) {
            currency = misBean.getCurrency(country.creditCardCurrency);
         } else {
            currency = misBean.getCurrency(user.currency);
         }

         returnDiscountTierData = accountBean.getApplicableDiscountTier(Enums.PaymentEnum.fromValue(paymentType), username, amount, currency, true);
      } catch (Exception var12) {
         return ExceptionHelper.getRootMessageAsHashtable(var12);
      }

      return returnDiscountTierData == null ? null : HashObjectUtils.dataObjectToHashtable(returnDiscountTierData);
   }

   public Vector getDiscountTiers(int paymentType, String username) {
      Vector discountTiers = new Vector();

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData user = userBean.loadUser(username, false, false);
         if (user == null) {
            throw new Exception("Invalid username " + username);
         } else {
            MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
            CountryData country = misBean.getCountry(user.countryID);
            if (country == null) {
               throw new Exception("Invalid country ID " + user.countryID);
            } else {
               AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
               CurrencyData currency;
               if (paymentType == Enums.PaymentEnum.BANK_TRANSFER.value()) {
                  currency = misBean.getCurrency(country.bankTransferCurrency);
               } else if (paymentType == Enums.PaymentEnum.WESTERN_UNION.value()) {
                  currency = misBean.getCurrency(country.westernUnionCurrency);
               } else if (paymentType == Enums.PaymentEnum.CREDIT_CARD.value()) {
                  currency = misBean.getCurrency(country.creditCardCurrency);
               } else {
                  currency = misBean.getCurrency(user.currency);
               }

               Vector<DiscountTierData> discountTierDataObjects = accountBean.getEligibleDiscountTiers(Enums.PaymentEnum.fromValue(paymentType), username, currency);
               if (discountTierDataObjects == null) {
                  return null;
               } else {
                  for(int i = discountTierDataObjects.size() - 1; i >= 0; --i) {
                     ((DiscountTierData)discountTierDataObjects.get(i)).displayMin = ((DiscountTierData)discountTierDataObjects.get(i)).displayMin - ((DiscountTierData)discountTierDataObjects.get(i)).displayMin * (((DiscountTierData)discountTierDataObjects.get(i)).percentageDiscount / 100.0D);
                     ((DiscountTierData)discountTierDataObjects.get(i)).displayMin = accountBean.discountTierRounding(((DiscountTierData)discountTierDataObjects.get(i)).displayMin);
                     ((DiscountTierData)discountTierDataObjects.get(i)).max = ((DiscountTierData)discountTierDataObjects.get(i)).max - ((DiscountTierData)discountTierDataObjects.get(i)).max * (((DiscountTierData)discountTierDataObjects.get(i)).percentageDiscount / 100.0D);
                     ((DiscountTierData)discountTierDataObjects.get(i)).max = accountBean.discountTierRounding(((DiscountTierData)discountTierDataObjects.get(i)).max);
                     discountTiers.add(HashObjectUtils.dataObjectToHashtable(discountTierDataObjects.get(i)));
                  }

                  return discountTiers;
               }
            }
         }
      } catch (Exception var12) {
         return ExceptionHelper.getRootMessageAsVector(var12);
      }
   }

   public double[] getCreditCardPaymentAmounts(String currency) {
      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         return accountBean.getCreditCardPaymentAmounts((String)null, false, currency);
      } catch (Exception var3) {
         return null;
      }
   }

   public double[] getCreditCardPaymentAmounts(String username, boolean isMerchant, String currency) {
      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         return accountBean.getCreditCardPaymentAmounts(username, isMerchant, currency);
      } catch (Exception var5) {
         return null;
      }
   }

   public Vector getPossibleTransferRecipients(String merchantUsername) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector possibleRecipients = new Vector();

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT username FROM user WHERE merchantcreated=? UNION SELECT DISTINCT LCASE(a1.username) username FROM accountentry a1, accountentry a2 WHERE a1.type=14 AND a1.reference=CAST(a2.id AS CHAR) AND a2.type=14 AND a2.username=? AND a2.amount<0 ORDER BY username");
         ps.setString(1, merchantUsername);
         ps.setString(2, merchantUsername);
         rs = ps.executeQuery();

         while(rs.next()) {
            possibleRecipients.add(rs.getString(1));
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

      return possibleRecipients;
   }

   public Vector getResellerStates(int countryId) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector resellerStates = new Vector();

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select distinct state from reseller where countryid=? and status=1 order by state");
         ps.setInt(1, countryId);
         rs = ps.executeQuery();

         while(rs.next()) {
            resellerStates.add(rs.getString("state"));
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

      return resellerStates;
   }

   public Vector getResellersInState(int countryId, String state) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector resellers = new Vector();

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from reseller where countryid=? and state=? and status=1 order by state, city");
         ps.setInt(1, countryId);
         ps.setString(2, state);

         Hashtable resellerHash;
         for(rs = ps.executeQuery(); rs.next(); resellers.add(resellerHash)) {
            resellerHash = new Hashtable();
            resellerHash.put("id", rs.getInt("id"));
            resellerHash.put("state", rs.getString("state"));
            resellerHash.put("city", rs.getString("city"));
            resellerHash.put("name", rs.getString("name"));
            if (rs.getString("address") != null) {
               resellerHash.put("address", rs.getString("address"));
            }

            if (rs.getString("phonenumber") != null) {
               resellerHash.put("phonenumber", rs.getString("phonenumber"));
            }

            if (rs.getString("phonenumbertodisplay") != null) {
               resellerHash.put("phonenumbertodisplay", rs.getString("phonenumbertodisplay"));
            }

            if (rs.getString("phonenumber2") != null) {
               resellerHash.put("phonenumber2", rs.getString("phonenumber2"));
            }

            if (rs.getString("phonenumber2todisplay") != null) {
               resellerHash.put("phonenumber2todisplay", rs.getString("phonenumber2todisplay"));
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

      return resellers;
   }

   public Vector getResellers(int countryId) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector resellers = new Vector();

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from reseller where countryid=? and status=1 order by state, city");
         ps.setInt(1, countryId);

         Hashtable resellerHash;
         for(rs = ps.executeQuery(); rs.next(); resellers.add(resellerHash)) {
            resellerHash = new Hashtable();
            resellerHash.put("id", rs.getInt("id"));
            resellerHash.put("state", rs.getString("state"));
            resellerHash.put("city", rs.getString("city"));
            resellerHash.put("name", rs.getString("name"));
            if (rs.getString("address") != null) {
               resellerHash.put("address", rs.getString("address"));
            }

            if (rs.getString("phonenumber") != null) {
               resellerHash.put("phonenumber", rs.getString("phonenumber"));
            }

            if (rs.getString("phonenumbertodisplay") != null) {
               resellerHash.put("phonenumbertodisplay", rs.getString("phonenumbertodisplay"));
            }

            if (rs.getString("phonenumber2") != null) {
               resellerHash.put("phonenumber2", rs.getString("phonenumber2"));
            }

            if (rs.getString("phonenumber2todisplay") != null) {
               resellerHash.put("phonenumber2todisplay", rs.getString("phonenumber2todisplay"));
            }
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

      return resellers;
   }

   public Vector getFixedCallRates(int countryId) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector fixedRates = new Vector();

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select DestinationCountryID, LandlineToLandline/ExchangeRate LandlineToLandline, LandlineToLandlineSignallingFee/ExchangeRate LandlineToLandlineSignallingFee, LandlineToMobile/ExchangeRate LandlineToMobile, LandlineToMobileSignallingFee/ExchangeRate LandlineToMobileSignallingFee, MobileToLandline/ExchangeRate MobileToLandline, MobileToLandlineSignallingFee/ExchangeRate MobileToLandlineSignallingFee, MobileToMobile/ExchangeRate MobileToMobile, MobileToMobileSignallingFee/ExchangeRate MobileToMobileSignallingFee, CallThroughToLandline/ExchangeRate CallThroughToLandline, CallThroughToLandlineSignallingFee/ExchangeRate CallThroughToLandlineSignallingFee, CallThroughToMobile/ExchangeRate CallThroughToMobile, CallThroughToMobileSignallingFee/ExchangeRate CallThroughToMobileSignallingFee from fixedcallrate, currency where fixedcallrate.currency=currency.code and fixedcallrate.sourcecountryid=?");
         ps.setInt(1, countryId);

         Hashtable fixedRateHash;
         for(rs = ps.executeQuery(); rs.next(); fixedRates.add(fixedRateHash)) {
            fixedRateHash = new Hashtable();
            fixedRateHash.put("DestinationCountryID", rs.getInt("DestinationCountryID"));
            if (rs.getObject("LandlineToLandline") != null) {
               fixedRateHash.put("LandlineToLandline", rs.getDouble("LandlineToLandline"));
            }

            if (rs.getObject("LandlineToLandlineSignallingFee") != null) {
               fixedRateHash.put("LandlineToLandlineSignallingFee", rs.getDouble("LandlineToLandlineSignallingFee"));
            }

            if (rs.getObject("LandlineToMobile") != null) {
               fixedRateHash.put("LandlineToMobile", rs.getDouble("LandlineToMobile"));
            }

            if (rs.getObject("LandlineToMobileSignallingFee") != null) {
               fixedRateHash.put("LandlineToMobileSignallingFee", rs.getDouble("LandlineToMobileSignallingFee"));
            }

            if (rs.getObject("MobileToLandline") != null) {
               fixedRateHash.put("MobileToLandline", rs.getDouble("MobileToLandline"));
            }

            if (rs.getObject("MobileToLandlineSignallingFee") != null) {
               fixedRateHash.put("MobileToLandlineSignallingFee", rs.getDouble("MobileToLandlineSignallingFee"));
            }

            if (rs.getObject("MobileToMobile") != null) {
               fixedRateHash.put("MobileToMobile", rs.getDouble("MobileToMobile"));
            }

            if (rs.getObject("MobileToMobileSignallingFee") != null) {
               fixedRateHash.put("MobileToMobileSignallingFee", rs.getDouble("MobileToMobileSignallingFee"));
            }

            if (rs.getObject("CallThroughToLandline") != null) {
               fixedRateHash.put("CallThroughToLandline", rs.getDouble("CallThroughToLandline"));
            }

            if (rs.getObject("CallThroughToLandlineSignallingFee") != null) {
               fixedRateHash.put("CallThroughToLandlineSignallingFee", rs.getDouble("CallThroughToLandlineSignallingFee"));
            }

            if (rs.getObject("CallThroughToMobile") != null) {
               fixedRateHash.put("CallThroughToMobile", rs.getDouble("CallThroughToMobile"));
            }

            if (rs.getObject("CallThroughToMobileSignallingFee") != null) {
               fixedRateHash.put("CallThroughToMobileSignallingFee", rs.getDouble("CallThroughToMobileSignallingFee"));
            }
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

      return fixedRates;
   }

   public Vector getEmoticonPacks(String username, boolean owned, int groupID, int type) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector emoticonPacks = new Vector();

      try {
         conn = this.dataSourceSlave.getConnection();
         if (owned) {
            if (type == EmoticonPackData.TypeEnum.PREMIUM_PURCHASE.value()) {
               ps = conn.prepareStatement("select emoticonpack.id, emoticonpack.name, emoticonpack.groupid, emoticonpack.groupviponly, cast(count(*) / 3 as signed) numemoticons from emoticon, emoticonpack, emoticonpackowner where emoticonpack.id=emoticonpackowner.emoticonpackid and emoticon.emoticonpackid=emoticonpack.id and emoticonpackowner.username=? and emoticonpack.type=2 and status=1 group by emoticonpack.id");
               ps.setString(1, username);
            } else {
               if (type != EmoticonPackData.TypeEnum.PREMIUM_SUBSCRIPTION.value()) {
                  log.error("getEmoticonPacks(): invalid type -  Username[" + username + "] Owned[" + owned + "] GroupID[" + groupID + "] Type[" + type + "]");
                  throw new EJBException("Unknown Emoticon Pack Type");
               }

               ps = conn.prepareStatement("select emoticonpack.id, emoticonpack.name, emoticonpack.groupid, emoticonpack.groupviponly, cast(count(*) / 3 as signed) numemoticons from emoticon, emoticonpack, service, subscription where subscription.serviceid=service.id and service.id=emoticonpack.serviceid and subscription.username=? and emoticonpack.type=3 and emoticonpack.status=1 and emoticon.emoticonpackid=emoticonpack.id and subscription.status=1 group by emoticonpack.id");
               ps.setString(1, username);
            }
         } else {
            String sql = "select emoticonpack.id, emoticonpack.name, emoticonpack.groupid, emoticonpack.groupviponly, emoticonpack.price * currency.exchangerate price, currency.code currency, cast(count(*) / 3 as signed) numemoticons, if (A.username is null, 0, 1) purchased from emoticonpack left outer join (select * from emoticonpackowner where username=?) A on emoticonpack.id = A.emoticonpackid left outer join emoticon on emoticonpack.id = emoticon.emoticonpackid inner join user on user.username=? inner join currency on user.currency = currency.code where emoticonpack.status=1 and forsale=1 and emoticonpack.type=? ";
            if (groupID > 0) {
               sql = sql + "and groupid=? ";
            } else {
               sql = sql + "and (groupid is null or groupid=0) ";
            }

            sql = sql + "group by emoticonpack.id order by sortorder";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, username);
            ps.setInt(3, type);
            if (groupID > 0) {
               ps.setInt(4, groupID);
            }
         }

         Hashtable pack;
         for(rs = ps.executeQuery(); rs.next(); emoticonPacks.add(pack)) {
            pack = new Hashtable();
            pack.put("id", rs.getInt("id"));
            pack.put("name", rs.getString("name"));
            pack.put("groupid", rs.getInt("groupid"));
            pack.put("groupviponly", rs.getBoolean("groupviponly"));
            pack.put("numemoticons", rs.getInt("numemoticons"));
            if (!owned) {
               pack.put("price", rs.getDouble("price"));
               pack.put("currency", rs.getString("currency"));
               pack.put("purchased", rs.getInt("purchased"));
            }
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

      return emoticonPacks;
   }

   public Vector getEmoticonsInPack(int emoticonPackID) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector emoticons = new Vector();

      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select * from emoticon where emoticonpackid=? and width=16");
         ps.setInt(1, emoticonPackID);
         rs = ps.executeQuery();

         while(rs.next()) {
            Hashtable emoticon = new Hashtable();
            emoticon.put("alias", rs.getString("alias"));
            emoticon.put("type", rs.getInt("type"));
            emoticons.add(emoticon);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var18) {
            connSlave = null;
         }

      }

      return emoticons;
   }

   public Vector getEmoticonDetailsFromHotkeys(String emoticonHotkeys) throws EJBException {
      Vector emoticons = new Vector();

      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         String[] hotkeys = emoticonHotkeys.split(" ");

         for(int i = 0; i < hotkeys.length; ++i) {
            EmoticonData emoticonData = contentBean.getEmoticon(hotkeys[i], 16);
            if (emoticonData != null) {
               Hashtable emoticon = new Hashtable();
               emoticon.put("hotkey", hotkeys[i]);
               emoticon.put("alias", emoticonData.alias);
               emoticon.put("type", emoticonData.type.toString());
               emoticon.put("location", emoticonData.locationPNG);
               emoticons.add(emoticon);
            } else {
               VirtualGiftData virtualGiftData = contentBean.getVirtualGiftByHotKey(hotkeys[i]);
               if (virtualGiftData != null) {
                  Hashtable emoticon = new Hashtable();
                  emoticon.put("hotkey", hotkeys[i]);
                  emoticon.put("alias", virtualGiftData.getName());
                  emoticon.put("type", EmoticonData.TypeEnum.IMAGE.toString());
                  emoticon.put("location", virtualGiftData.getLocation16x16PNG());
                  emoticons.add(emoticon);
               }
            }
         }

         return emoticons;
      } catch (CreateException var9) {
         return ExceptionHelper.getRootMessageAsVector(var9);
      } catch (FusionEJBException var10) {
         return ExceptionHelper.getRootMessageAsVector(var10);
      } catch (EJBException var11) {
         return var11.getCausedByException() != null ? ExceptionHelper.setErrorMessageAsVector(var11.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessageAsVector(var11.getMessage());
      }
   }

   public Hashtable getEmoticonPack(String username, int emoticonPackId) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Hashtable emoticonPack = new Hashtable();

      Hashtable var8;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select emoticonpack.id, emoticonpack.name, emoticonpack.description, emoticonpack.price * currency.exchangerate price, emoticonpack.groupid, emoticonpack.groupviponly, emoticonpack.type, emoticonpack.serviceid, currency.code currency, cast(count(*) / 3 as signed) numemoticons, if (A.username is null, 0, 1) purchased from emoticonpack left outer join (select * from emoticonpackowner where username=?) A on emoticonpack.id = A.emoticonpackid left outer join emoticon on emoticonpack.id = emoticon.emoticonpackid inner join user on user.username=? inner join currency on user.currency = currency.code where emoticonpack.status=1 and emoticonpack.type in (2,3) and emoticonpack.id=? group by emoticonpack.id");
         ps.setString(1, username);
         ps.setString(2, username);
         ps.setInt(3, emoticonPackId);
         rs = ps.executeQuery();
         if (rs.next()) {
            emoticonPack.put("id", rs.getInt("id"));
            emoticonPack.put("name", rs.getString("name"));
            if (rs.getObject("description") != null) {
               emoticonPack.put("description", rs.getString("description"));
            }

            emoticonPack.put("price", rs.getDouble("price"));
            emoticonPack.put("currency", rs.getString("currency"));
            emoticonPack.put("groupid", rs.getInt("groupid"));
            emoticonPack.put("groupviponly", rs.getBoolean("groupviponly"));
            emoticonPack.put("numemoticons", rs.getInt("numemoticons"));
            emoticonPack.put("purchased", rs.getInt("purchased"));
            emoticonPack.put("type", rs.getInt("type"));
            emoticonPack.put("serviceid", rs.getInt("serviceid"));
         }

         if (rs.getInt("type") == EmoticonPackData.TypeEnum.PREMIUM_SUBSCRIPTION.value()) {
            rs.close();
            rs = null;
            ps.close();
            ps = null;
            String sql = "SELECT service.*, service.Cost / currency_sub.ExchangeRate * currency_user.ExchangeRate price, user.Currency usercurrency, subscription.Status purchased FROM user INNER JOIN currency currency_user ON user.Currency=currency_user.Code, service INNER JOIN currency currency_sub ON service.CostCurrency=currency_sub.code LEFT OUTER JOIN subscription ON (subscription.ServiceID=service.ID AND subscription.username=?) WHERE user.Username=? AND service.ID=? AND subscription.Status=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, username);
            ps.setInt(3, (Integer)emoticonPack.get("serviceid"));
            ps.setInt(4, SubscriptionData.StatusEnum.ACTIVE.value());
            rs = ps.executeQuery();
            if (rs.next()) {
               emoticonPack.put("freetrialdays", rs.getInt("freetrialdays"));
               emoticonPack.put("durationdays", rs.getInt("durationdays"));
               emoticonPack.put("price", Numerics.round(rs.getDouble("price"), 2));
               emoticonPack.put("currency", rs.getString("usercurrency"));
               emoticonPack.put("purchased", rs.getInt("purchased") == 1 ? 1 : 0);
            }
         }

         if (emoticonPack.get("groupid") != null && (Integer)emoticonPack.get("groupid") > 0) {
            rs.close();
            rs = null;
            ps.close();
            ps = null;
            conn.close();
            conn = null;
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            GroupData groupData = userEJB.getGroup((Integer)emoticonPack.get("groupid"));
            GroupMemberData groupMemberData = userEJB.getGroupMember(username, (Integer)emoticonPack.get("groupid"));
            if (groupMemberData == null || GroupMemberData.StatusEnum.ACTIVE.value() != groupMemberData.status.value() || !groupMemberData.vip && (Boolean)emoticonPack.get("groupviponly")) {
               String err = "You must be a ";
               if (emoticonPack.get("groupviponly") != null && (Boolean)emoticonPack.get("groupviponly")) {
                  err = err + "VIP ";
               }

               err = err + "member of the " + groupData.name + " group to access the " + emoticonPack.get("name") + " emoticon pack";
               throw new Exception(err);
            }
         }

         return emoticonPack;
      } catch (Exception var28) {
         log.error("Exception in getEmoticonPack()", var28);
         var8 = ExceptionHelper.setErrorMessageAsHashtable(var28.getMessage());
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

      return var8;
   }

   public String buyEmoticonPack(String username, int emoticonPackId, String ipAddress, String sessionID, String mobileDevice, String userAgent) throws EJBException {
      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         contentBean.buyEmoticonPack(username, emoticonPackId, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return "TRUE";
      } catch (CreateException var8) {
         return ExceptionHelper.getRootMessage(var8);
      } catch (FusionEJBException var9) {
         return ExceptionHelper.getRootMessage(var9);
      } catch (EJBException var10) {
         return var10.getCausedByException() != null ? ExceptionHelper.setErrorMessage(var10.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessage(var10.getMessage());
      }
   }

   public Vector getAllEmoticons() throws EJBException {
      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         List<EmoticonData> allEmoticons = contentBean.getAllEmoticons();
         Vector v = new Vector();
         Iterator i$ = allEmoticons.iterator();

         while(true) {
            EmoticonData e;
            do {
               if (!i$.hasNext()) {
                  return v;
               }

               e = (EmoticonData)i$.next();
            } while(e.width != 16);

            v.add(HashObjectUtils.dataObjectToHashtable(e));
            Iterator i$ = e.alternateHotKeys.iterator();

            while(i$.hasNext()) {
               String altHotkey = (String)i$.next();
               Hashtable h = HashObjectUtils.dataObjectToHashtable(e);
               h.put("hotKey", altHotkey);
               v.add(h);
            }
         }
      } catch (CreateException var9) {
         throw new EJBException(ExceptionHelper.getRootMessage(var9));
      } catch (FusionEJBException var10) {
         throw new EJBException(ExceptionHelper.getRootMessage(var10));
      } catch (EJBException var11) {
         throw new EJBException(var11.getCausedByException().getMessage());
      }
   }

   private Vector getContentAsVector(List<ContentData> contents) {
      Vector v = new Vector();

      for(int i = 0; i < contents.size(); ++i) {
         Hashtable hash = HashObjectUtils.dataObjectToHashtable(contents.get(i));
         v.add(hash);
      }

      return v;
   }

   public Vector getTopWallpaper(int count) {
      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         List<ContentData> wallpapers = contentBean.getTopWallpaper(count);
         return this.getContentAsVector(wallpapers);
      } catch (Exception var4) {
         throw new EJBException(ExceptionHelper.getRootMessage(var4));
      }
   }

   public Vector getTopRingtones(int count) {
      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         List<ContentData> ringtones = contentBean.getTopRingtones(count);
         return this.getContentAsVector(ringtones);
      } catch (Exception var4) {
         throw new EJBException(ExceptionHelper.getRootMessage(var4));
      }
   }

   public Vector getMobileContentCategories(String username, int parentContentCategoryID, int groupID, int page, int numEntries) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector categories = new Vector();
      int numRows = 0;
      int startEntry = (page - 1) * numEntries + 1;
      int endEntry = startEntry + numEntries - 1;

      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "select ContentCategory.ID, ContentCategory.Name, count(*) numitems from ContentCategory, content, user where user.username=? and contentcategory.id=content.contentcategoryid and content.status=1 and (content.countryid=user.countryid or content.countryid is null) ";
         if (parentContentCategoryID > 0) {
            sql = sql + "and ParentContentCategoryID=? ";
         } else {
            sql = sql + "and ParentContentCategoryID is null ";
         }

         if (groupID > 0) {
            sql = sql + "and content.groupid=? ";
         } else {
            sql = sql + "and (content.groupid is null or content.groupid=0) ";
         }

         sql = sql + "group by id, name order by Name";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         if (parentContentCategoryID > 0) {
            ps.setInt(2, parentContentCategoryID);
         }

         if (groupID > 0) {
            if (parentContentCategoryID > 0) {
               ps.setInt(3, groupID);
            } else {
               ps.setInt(2, groupID);
            }
         }

         for(rs = ps.executeQuery(); rs.next(); ++numRows) {
         }

         rs.beforeFirst();
         Hashtable markerHash = new Hashtable();
         markerHash.put("page", page);
         markerHash.put("numEntries", numRows);
         markerHash.put("numPages", (numRows + numEntries - 1) / numEntries);
         categories.add(markerHash);
         if (endEntry > numRows) {
            endEntry = numRows;
         }

         while(rs.next()) {
            if (rs.getRow() >= startEntry) {
               Hashtable category = new Hashtable();
               category.put("id", rs.getInt("id"));
               category.put("name", rs.getString("name"));
               category.put("numitems", rs.getString("numitems"));
               categories.add(category);
            }

            if (rs.getRow() >= endEntry) {
               break;
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

      return categories;
   }

   public Vector getMobileContent(String username, int contentCategoryID, int groupID, int page, int numEntries) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector content = new Vector();
      int numRows = 0;
      int startEntry = page * numEntries + 1;
      int endEntry = startEntry + numEntries - 1;

      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "select content.id, content.contentcategoryid, content.type, content.name, content.artist, content.price / currency_content.exchangerate * currency_user.exchangerate price, currency_user.code currency, content.preview, content.groupid, content.groupviponly, content.thumbnail from content, user, currency currency_user, currency currency_content where user.username=? and user.currency=currency_user.code and content.currency=currency_content.code and contentcategoryid=? and content.status=1 and (content.countryid=user.countryid or content.countryid is null) ";
         if (groupID > 0) {
            sql = sql + "and groupid=? ";
         } else {
            sql = sql + "and (groupid is null or groupid=0) ";
         }

         sql = sql + "order by content.artist, content.name";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setInt(2, contentCategoryID);
         if (groupID > 0) {
            ps.setInt(3, groupID);
         }

         for(rs = ps.executeQuery(); rs.next(); ++numRows) {
         }

         rs.beforeFirst();
         Hashtable markerHash = new Hashtable();
         markerHash.put("page", page);
         markerHash.put("numEntries", numRows);
         markerHash.put("numPages", (numRows + numEntries - 1) / numEntries);
         content.add(markerHash);
         if (endEntry > numRows) {
            endEntry = numRows;
         }

         while(rs.next()) {
            if (rs.getRow() >= startEntry) {
               Hashtable contentItem = new Hashtable();
               contentItem.put("id", rs.getInt("id"));
               contentItem.put("contentcategoryid", rs.getInt("contentcategoryid"));
               contentItem.put("type", ContentData.TypeEnum.fromValue(rs.getInt("type")).toString());
               contentItem.put("name", rs.getString("name"));
               contentItem.put("artist", rs.getString("artist") == null ? "" : rs.getString("artist"));
               contentItem.put("price", rs.getDouble("price"));
               contentItem.put("currency", rs.getString("currency"));
               contentItem.put("preview", rs.getString("preview") == null ? "" : rs.getString("preview"));
               contentItem.put("thumbnail", rs.getString("thumbnail") == null ? "" : rs.getString("thumbnail"));
               contentItem.put("groupid", rs.getInt("groupid"));
               contentItem.put("groupviponly", rs.getBoolean("groupviponly"));
               content.add(contentItem);
            }

            if (rs.getRow() >= endEntry) {
               break;
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

      return content;
   }

   public Hashtable getMobileContentItem(String username, int contentID, boolean activeOnly) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Hashtable contentItem = new Hashtable();

      try {
         conn = this.dataSourceSlave.getConnection();
         String limitToActiveOnly = "";
         if (activeOnly) {
            limitToActiveOnly = " and content.status=1";
         }

         ps = conn.prepareStatement("select A.*, if (contentpurchased.id is null, false, true) purchased, contentpurchased.numdownloads, contentpurchased.downloadurl, TIMESTAMPDIFF(HOUR,datecreated,now()) hourssincepurchase from (select content.id, content.contentcategoryid, content.type, content.name, content.artist, content.price / currency_content.exchangerate * currency_user.exchangerate price, currency_user.code currency, content.preview, content.previewwidth, content.previewheight, content.groupid, content.groupviponly, content.thumbnail from content, user, currency currency_user, currency currency_content where user.username=? and user.currency=currency_user.code and content.currency=currency_content.code " + limitToActiveOnly + " and content.id=?) A " + "left outer join contentpurchased on " + "contentpurchased.contentid=A.id and contentpurchased.username=?");
         ps.setString(1, username);
         ps.setInt(2, contentID);
         ps.setString(3, username);
         rs = ps.executeQuery();
         if (rs.next()) {
            contentItem.put("id", rs.getInt("id"));
            contentItem.put("contentcategoryid", rs.getInt("contentcategoryid"));
            contentItem.put("type", ContentData.TypeEnum.fromValue(rs.getInt("type")).toString());
            contentItem.put("name", rs.getString("name"));
            contentItem.put("artist", rs.getString("artist") == null ? "" : rs.getString("artist"));
            contentItem.put("price", rs.getDouble("price"));
            contentItem.put("currency", rs.getString("currency"));
            contentItem.put("preview", rs.getString("preview") == null ? "" : rs.getString("preview"));
            contentItem.put("previewwidth", rs.getString("previewwidth") == null ? "" : rs.getString("previewwidth"));
            contentItem.put("previewheight", rs.getString("previewheight") == null ? "" : rs.getString("previewheight"));
            contentItem.put("groupid", rs.getInt("groupid"));
            contentItem.put("groupviponly", rs.getBoolean("groupviponly"));
            contentItem.put("thumbnail", rs.getString("thumbnail") == null ? "" : rs.getString("thumbnail"));
            contentItem.put("numdownloads", rs.getString("numdownloads") == null ? "" : rs.getString("numdownloads"));
            contentItem.put("downloadurl", rs.getString("downloadurl") == null ? "" : rs.getString("downloadurl"));
            contentItem.put("hourssincepurchase", rs.getInt("hourssincepurchase"));
            if ((Integer)contentItem.get("groupid") > 0) {
               rs.close();
               rs = null;
               ps.close();
               ps = null;
               conn.close();
               conn = null;
               UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               GroupData groupData = userEJB.getGroup((Integer)contentItem.get("groupid"));
               GroupMemberData groupMemberData = userEJB.getGroupMember(username, (Integer)contentItem.get("groupid"));
               if (groupMemberData == null || GroupMemberData.StatusEnum.ACTIVE.value() != groupMemberData.status.value()) {
                  String err = "You must be a ";
                  err = err + "member of the " + groupData.name + " group to access " + contentItem.get("name");
                  throw new Exception(err);
               }
            }
         }
      } catch (Exception var27) {
         throw new EJBException(var27.getMessage());
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var24) {
            conn = null;
         }

      }

      return contentItem;
   }

   public String buyMobileContentItem(String username, int contentID, String ipAddress, String sessionID, String mobileDevice, String userAgent) throws EJBException {
      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         String downloadURL = contentBean.buyMobileContentItem(username, contentID, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         if (downloadURL == null) {
            downloadURL = "";
         }

         return downloadURL;
      } catch (CreateException var9) {
         return ExceptionHelper.getRootMessage(var9);
      } catch (EJBException var10) {
         return var10.getCausedByException() != null ? ExceptionHelper.setErrorMessage(var10.getCausedByException().getMessage()) : ExceptionHelper.setErrorMessage(var10.getMessage());
      }
   }

   public String getMobileContentDownloadURL(String username, int contentID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var6;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select id, downloadurl from contentpurchased where username=? and contentid=?");
         ps.setString(1, username);
         ps.setInt(2, contentID);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var6 = null;
            return var6;
         }

         var6 = rs.getString("downloadurl");
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

   public boolean processILoopAPICall(String providerTransactionId, String destAddr, String body, String ipAddress, String sessionID, String mobileDevice, String userAgent) throws EJBException {
      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         contentBean.processILoopAPICall(providerTransactionId, destAddr, body, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return true;
      } catch (CreateException var9) {
         throw new EJBException(ExceptionHelper.getRootMessage(var9));
      } catch (EJBException var10) {
         throw new EJBException(ExceptionHelper.setErrorMessage(var10.getCausedByException().getMessage()));
      }
   }

   public boolean processOplayoAPICall(int oplayoID, int rputag, int progress, int licenceId, int timestamp) throws EJBException {
      return true;
   }

   public Vector getPurchasedContent(String username, int page, int numEntries) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector content = new Vector();
      int numRows = 0;
      int startEntry = page * numEntries + 1;
      int endEntry = startEntry + numEntries - 1;
      int maxAEPeriodBeforeArchival = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_ACCOUNTENTRY_PERIOD_BEFORE_ARCHIVAL_IN_DAYS);

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from (select contentpurchased.datecreated, content.type, content.id, content.name from contentpurchased inner join content on contentpurchased.contentid=content.id where contentpurchased.username=? union select accountentry.datecreated, 0, accountentry.reference id, emoticonpack.name from accountentry, emoticonpack where accountentry.username=? and accountentry.type=27 and accountentry.reference=emoticonpack.id and accountentry.datecreated >= date_sub(curdate(), interval ? day) ) A order by datecreated desc");
         ps.setString(1, username);
         ps.setString(2, username);
         ps.setInt(3, maxAEPeriodBeforeArchival);

         for(rs = ps.executeQuery(); rs.next(); ++numRows) {
         }

         rs.beforeFirst();
         Hashtable markerHash = new Hashtable();
         markerHash.put("page", page);
         markerHash.put("numEntries", numRows);
         markerHash.put("numPages", (numRows + numEntries - 1) / numEntries);
         content.add(markerHash);
         if (endEntry > numRows) {
            endEntry = numRows;
         }

         SimpleDateFormat df = new SimpleDateFormat("d MMM yy");

         while(rs.next()) {
            if (rs.getRow() >= startEntry) {
               Hashtable contentItem = new Hashtable();
               contentItem.put("dateCreated", df.format(rs.getTimestamp("dateCreated")));
               contentItem.put("type", rs.getInt("type"));
               contentItem.put("id", rs.getInt("id"));
               contentItem.put("name", rs.getString("name"));
               content.add(contentItem);
            }

            if (rs.getRow() >= endEntry) {
               break;
            }
         }
      } catch (SQLException var29) {
         throw new EJBException(var29.getMessage());
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

      return content;
   }

   public Hashtable getSmallProfile(String usernameViewing, String usernameBeingViewed) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var7;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select user.displaypicture, user.mobilephone, user.emailactivated, user.chatroomadmin, user.registrationdate, country.name country, userprofile.city, userprofile.gender, DATE_FORMAT(NOW(), '%Y') - DATE_FORMAT(userprofile.dateofbirth, '%Y') - (DATE_FORMAT(NOW(), '00-%m-%d') < DATE_FORMAT(userprofile.dateofbirth, '00-%m-%d')) AS age, userprofile.relationshipstatus, userprofile.status userprofilestatus from user, userprofile, country where user.username=? and user.username=userprofile.username and user.countryid=country.id");
         ps.setString(1, usernameBeingViewed);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Profile not found");
         }

         Hashtable hash = new Hashtable();
         hash.put("displaypicture", rs.getString("displaypicture"));
         hash.put("mobilephone", rs.getString("mobilephone"));
         hash.put("emailactivated", rs.getInt("emailactivated"));
         hash.put("chatroomadmin", rs.getInt("chatroomadmin"));
         hash.put("registrationdate", rs.getTimestamp("registrationdate"));
         hash.put("country", rs.getString("country"));
         hash.put("city", rs.getString("city"));
         hash.put("gender", rs.getString("gender"));
         hash.put("age", rs.getString("age"));
         hash.put("relationshipstatus", rs.getString("relationshipstatus"));
         hash.put("userprofilestatus", rs.getString("userprofilestatus"));
         var7 = hash;
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

   public Hashtable getFriends(String usernameViewing, String usernameBeingViewed, String searchString, int pageNumber, int resultsPerPage) throws EJBException {
      Connection conn = null;
      Vector friendsVector = new Vector();
      Hashtable hash = new Hashtable();

      Hashtable var10;
      try {
         conn = this.dataSourceSlave.getConnection();
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         Set<String> friends = userBean.loadBroadcastList(usernameBeingViewed, conn);
         if (searchString.length() > 0) {
            searchString = searchString.toLowerCase();
            Iterator i = friends.iterator();

            while(i.hasNext()) {
               if (!((String)i.next()).toLowerCase().contains(searchString)) {
                  i.remove();
               }
            }
         }

         double pages = (double)friends.size() / (double)resultsPerPage;
         if (pages < 1.0D) {
            pages = 1.0D;
         }

         int totalPageCount = (int)Math.ceil(pages);
         if (pageNumber > totalPageCount) {
            pageNumber = totalPageCount;
         }

         int startResultCount = (pageNumber - 1) * resultsPerPage;
         if (startResultCount < 0) {
            startResultCount = 0;
         }

         int endResultCount = startResultCount + resultsPerPage;
         Iterator it = friends.iterator();

         for(int rowPosition = 0; it.hasNext(); ++rowPosition) {
            String username = (String)it.next();
            if (rowPosition >= startResultCount && rowPosition < endResultCount) {
               friendsVector.add(this.getUserContactDetails(usernameBeingViewed, username));
            }
         }

         hash.put("totalResults", friends.size());
         hash.put("pageNumber", pageNumber);
         hash.put("totalPages", totalPageCount);
         hash.put("startResult", startResultCount);
         hash.put("endResult", endResultCount);
         hash.put("friends", friendsVector);
         Hashtable var32 = hash;
         return var32;
      } catch (Exception var28) {
         var10 = new Hashtable();
      } finally {
         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var27) {
            conn = null;
         }

      }

      return var10;
   }

   public Hashtable getCallRates(int sourceCountryId, int destinationCountryId, boolean sourceIsLandline, String currencyCode) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Hashtable callRates = new Hashtable();
      boolean callThroughSupported = false;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select number from didnumber where countryid=? and status=1");
         ps.setInt(1, sourceCountryId);
         rs = ps.executeQuery();
         if (rs.next()) {
            callThroughSupported = true;
         }

         rs.close();
         ps.close();
         String sql = "select ";
         if (sourceIsLandline) {
            sql = sql + "country.callrate * currency.exchangerate sourcerate, ";
            sql = sql + "country.callsignallingfee*currency.exchangerate sourcesignallingfee, ";
         } else {
            sql = sql + "country.mobilerate * currency.exchangerate sourcerate, ";
            sql = sql + "country.mobilesignallingfee * currency.exchangerate sourcesignallingfee, ";
         }

         sql = sql + "country.callthroughrate * currency.exchangerate sourcecallthroughrate, ";
         sql = sql + "country.callthroughsignallingfee * currency.exchangerate sourcecallthroughsignallingfee, ";
         sql = sql + "dest.smscost * currency.exchangerate smscost, country.name ";
         sql = sql + "from country, currency, country dest where country.id=? and currency.code=? and dest.id=?";
         ps = conn.prepareStatement(sql);
         ps.setInt(1, sourceCountryId);
         ps.setString(2, currencyCode);
         ps.setInt(3, destinationCountryId);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Invalid source country or currency");
         }

         callRates.put("CallbackToLandlineRate", rs.getDouble("sourcerate"));
         callRates.put("CallbackToMobileRate", rs.getDouble("sourcerate"));
         callRates.put("CallbackToLandlineSignallingFee", rs.getDouble("sourcesignallingfee"));
         callRates.put("CallbackToMobileSignallingFee", rs.getDouble("sourcesignallingfee"));
         if (callThroughSupported) {
            callRates.put("CallThroughToLandlineRate", rs.getDouble("sourcecallthroughrate"));
            callRates.put("CallThroughToMobileRate", rs.getDouble("sourcecallthroughrate"));
            callRates.put("CallThroughToLandlineSignallingFee", rs.getDouble("sourcecallthroughsignallingfee"));
            callRates.put("CallThroughToMobileSignallingFee", rs.getDouble("sourcecallthroughsignallingfee"));
         }

         callRates.put("SMSCost", rs.getString("smscost"));
         callRates.put("SourceCountry", rs.getString("name"));
         rs.close();
         ps.close();
         sql = "select country.callrate * currency.exchangerate destinationlandlinerate, country.callsignallingfee * currency.exchangerate destinationlandlinesignallingfee, country.mobilerate * currency.exchangerate destinationmobilerate, country.mobilesignallingfee * currency.exchangerate destinationmobilesignallingfee, country.name from country, currency where country.id=? and currency.code=?";
         ps = conn.prepareStatement(sql);
         ps.setInt(1, destinationCountryId);
         ps.setString(2, currencyCode);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Invalid destination country");
         }

         callRates.put("CallbackToLandlineRate", (Double)callRates.get("CallbackToLandlineRate") + rs.getDouble("destinationlandlinerate"));
         callRates.put("CallbackToMobileRate", (Double)callRates.get("CallbackToMobileRate") + rs.getDouble("destinationmobilerate"));
         callRates.put("CallbackToLandlineSignallingFee", (Double)callRates.get("CallbackToLandlineSignallingFee") + rs.getDouble("destinationlandlinesignallingfee"));
         callRates.put("CallbackToMobileSignallingFee", (Double)callRates.get("CallbackToMobileSignallingFee") + rs.getDouble("destinationmobilesignallingfee"));
         if (callThroughSupported) {
            callRates.put("CallThroughToLandlineRate", (Double)callRates.get("CallThroughToLandlineRate") + rs.getDouble("destinationlandlinerate"));
            callRates.put("CallThroughToMobileRate", (Double)callRates.get("CallThroughToMobileRate") + rs.getDouble("destinationmobilerate"));
            callRates.put("CallThroughToLandlineSignallingFee", (Double)callRates.get("CallThroughToLandlineSignallingFee") + rs.getDouble("destinationlandlinesignallingfee"));
            callRates.put("CallThroughToMobileSignallingFee", (Double)callRates.get("CallThroughToMobileSignallingFee") + rs.getDouble("destinationmobilesignallingfee"));
         }

         callRates.put("DestinationCountry", rs.getString("name"));
         rs.close();
         ps.close();
         ps = conn.prepareStatement("select DestinationCountryID, LandlineToLandline / c1.ExchangeRate * c2.ExchangeRate LandlineToLandline, LandlineToLandlineSignallingFee / c1.ExchangeRate * c2.ExchangeRate LandlineToLandlineSignallingFee, LandlineToMobile / c1.ExchangeRate * c2.ExchangeRate LandlineToMobile, LandlineToMobileSignallingFee / c1.ExchangeRate * c2.ExchangeRate LandlineToMobileSignallingFee, MobileToLandline / c1.ExchangeRate * c2.ExchangeRate MobileToLandline, MobileToLandlineSignallingFee / c1.ExchangeRate * c2.ExchangeRate MobileToLandlineSignallingFee, MobileToMobile / c1.ExchangeRate * c2.ExchangeRate MobileToMobile, MobileToMobileSignallingFee / c1.ExchangeRate * c2.ExchangeRate MobileToMobileSignallingFee, CallThroughToLandline / c1.ExchangeRate * c2.ExchangeRate CallThroughToLandline, CallThroughToLandlineSignallingFee / c1.ExchangeRate * c2.ExchangeRate CallThroughToLandlineSignallingFee, CallThroughToMobile / c1.ExchangeRate * c2.ExchangeRate CallThroughToMobile, CallThroughToMobileSignallingFee / c1.ExchangeRate * c2.ExchangeRate CallThroughToMobileSignallingFee from fixedcallrate, currency c1, currency c2 where fixedcallrate.currency=c1.code and fixedcallrate.sourcecountryid=? and fixedcallrate.destinationcountryid=? and c2.code=?");
         ps.setInt(1, sourceCountryId);
         ps.setInt(2, destinationCountryId);
         ps.setString(3, currencyCode);
         rs = ps.executeQuery();
         if (rs.next()) {
            if (sourceIsLandline) {
               if (rs.getObject("LandlineToLandline") != null) {
                  callRates.put("CallbackToLandlineRate", rs.getDouble("LandlineToLandline"));
               }

               if (rs.getObject("LandlineToLandlineSignallingFee") != null) {
                  callRates.put("CallbackToLandlineSignallingFee", rs.getDouble("LandlineToLandlineSignallingFee"));
               }

               if (rs.getObject("LandlineToMobile") != null) {
                  callRates.put("CallbackToMobileRate", rs.getDouble("LandlineToMobile"));
               }

               if (rs.getObject("LandlineToMobileSignallingFee") != null) {
                  callRates.put("CallbackToMobileSignallingFee", rs.getDouble("LandlineToMobileSignallingFee"));
               }
            } else {
               if (rs.getObject("MobileToLandline") != null) {
                  callRates.put("CallbackToLandlineRate", rs.getDouble("MobileToLandline"));
               }

               if (rs.getObject("MobileToLandlineSignallingFee") != null) {
                  callRates.put("CallbackToLandlineSignallingFee", rs.getDouble("MobileToLandlineSignallingFee"));
               }

               if (rs.getObject("MobileToMobile") != null) {
                  callRates.put("CallbackToMobileRate", rs.getDouble("MobileToMobile"));
               }

               if (rs.getObject("MobileToMobileSignallingFee") != null) {
                  callRates.put("CallbackToMobileSignallingFee", rs.getDouble("MobileToMobileSignallingFee"));
               }
            }

            if (callThroughSupported) {
               if (rs.getObject("CallThroughToLandline") != null) {
                  callRates.put("CallThroughToLandlineRate", rs.getDouble("CallThroughToLandline"));
               }

               if (rs.getObject("CallThroughToLandlineSignallingFee") != null) {
                  callRates.put("CallThroughToLandlineSignallingFee", rs.getDouble("CallThroughToLandlineSignallingFee"));
               }

               if (rs.getObject("CallThroughToMobile") != null) {
                  callRates.put("CallThroughToMobileRate", rs.getDouble("CallThroughToMobile"));
               }

               if (rs.getObject("CallThroughToMobileSignallingFee") != null) {
                  callRates.put("CallThroughToMobileSignallingFee", rs.getDouble("CallThroughToMobileSignallingFee"));
               }
            }
         }

         rs.close();
         ps.close();
         ps = conn.prepareStatement("select Cost / c1.ExchangeRate * c2.ExchangeRate smscost from fixedsmscost, currency c1, currency c2 where fixedsmscost.currency=c1.code and fixedsmscost.countryid=? and c2.code=?");
         ps.setInt(1, sourceCountryId);
         ps.setString(2, currencyCode);
         rs = ps.executeQuery();
         if (rs.next()) {
            callRates.put("SMSCost", rs.getString("smscost"));
         }

         callRates.put("Currency", currencyCode);
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

      return callRates;
   }

   public int getUserProfilePrivacySetting(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var5;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select status from userprofile where username=?");
         ps.setString(1, username);
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

   public boolean setUserProfilePrivacySetting(String username, int privacySetting) throws EJBException {
      if (privacySetting >= 1 && privacySetting <= 3) {
         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserProfileData userProfileData = userBean.getUserProfile(username, username, true);
            if (userProfileData == null) {
               userProfileData = new UserProfileData();
            }

            userProfileData.username = username;
            userProfileData.status = UserProfileData.StatusEnum.fromValue(privacySetting);
            userBean.updateUserProfile(userProfileData);
            return true;
         } catch (CreateException var5) {
         } catch (EJBException var6) {
         } catch (FusionEJBException var7) {
         }

         return false;
      } else {
         throw new EJBException("Invalid privacy setting");
      }
   }

   public int getUserBlockedListCount(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var5;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select count(*) from blocklist where username=?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            byte var25 = 0;
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

   public Hashtable getUserBlockedList(String username, String search, int pageNumber, int resultsPerPage, boolean orderByDate, boolean ascending) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector blockVector = new Vector();
      Hashtable hash = new Hashtable();

      Hashtable var14;
      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "select BlockUsername from blocklist where username=?";
         if (search.length() > 0) {
            sql = sql + " and BlockUsername like ?";
         }

         if (orderByDate) {
            sql = sql + " order by BlockUsername ";
         } else {
            sql = sql + " order by BlockUsername ";
         }

         if (ascending) {
            sql = sql + " asc";
         } else {
            sql = sql + " desc";
         }

         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         if (search.length() > 0) {
            search = search + "%";
            ps.setString(2, search);
         }

         rs = ps.executeQuery();
         int i;
         if (rs.next()) {
            rs.absolute((pageNumber - 1) * resultsPerPage + 1);

            for(i = 0; i < resultsPerPage && !rs.isAfterLast(); ++i) {
               blockVector.add(rs.getString(1));
               rs.next();
            }
         }

         i = rs.last() ? rs.getRow() : 0;
         hash.put("totalresults", i);
         hash.put("totalpages", Math.ceil((double)i / (double)resultsPerPage));
         hash.put("page", pageNumber);
         hash.put("blocked_users", blockVector);
         var14 = hash;
      } catch (SQLException var29) {
         throw new EJBException(var29.getMessage());
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

      return var14;
   }

   public boolean isContact(String userName, String contactName) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from contact where username=? and fusionusername=?");
         ps.setString(1, userName);
         ps.setString(2, contactName);
         rs = ps.executeQuery();
         if (rs.next()) {
            var6 = true;
            return var6;
         }

         var6 = false;
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

   public boolean isContactFriend(String username, String contactname) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select c.id from Contact c, Contact x where c.username=? and c.fusionusername=? and x.username = c.fusionusername and x.fusionusername = c.username limit 1");
         ps.setString(1, username);
         ps.setString(2, contactname);
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

   public Hashtable getUserContactDetails(String userName, String contactName) {
      new Hashtable();
      Connection connSlave = null;

      Hashtable var7;
      try {
         Hashtable hash = this.getContact(userName, contactName);
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         connSlave = this.dataSourceSlave.getConnection();
         Set<String> friends = userEJB.loadBroadcastList(contactName, connSlave);
         connSlave.close();
         connSlave = null;
         hash.put("isContact", friends.contains(userName));
         hash.put("privacy", userEJB.getUserProfileStatus(contactName).toString());
         hash.put("numFriends", friends.size());
         var7 = hash;
      } catch (Exception var16) {
         throw new EJBException(var16.getMessage());
      } finally {
         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var15) {
         }

      }

      return var7;
   }

   public boolean unblockUser(String username, String unblockUsername) throws EJBException {
      try {
         ContactLocal contactBean = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
         contactBean.unblockContact(username, unblockUsername, false);
         return true;
      } catch (Exception var4) {
         throw new EJBException(var4.getMessage());
      }
   }

   public boolean unblockAllUsers(String username) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select BlockUsername from blocklist where username=?");
         ps.setString(1, username);
         rs = ps.executeQuery();

         while(rs.next()) {
            String blockname = rs.getString("blockusername");
            this.unblockUser(username, blockname);
         }
      } catch (Exception var20) {
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

      return true;
   }

   private Hashtable createPagingUserEventHashtable(UserEventIce[] userEvents, int pageNumber, int resultsPerPage) {
      try {
         Hashtable hash = new Hashtable();
         int totalPages = (int)Math.ceil((double)userEvents.length / (double)resultsPerPage);
         if (pageNumber <= 0) {
            pageNumber = 0;
         }

         if (pageNumber > totalPages) {
            pageNumber = totalPages;
         }

         int startPage = (pageNumber - 1) * resultsPerPage;
         int endPage = startPage + resultsPerPage;
         if (endPage >= userEvents.length) {
            endPage = userEvents.length;
         }

         hash.put("totalEventCount", userEvents.length);
         hash.put("totalPages", totalPages);
         hash.put("page", pageNumber);
         if (userEvents.length == 0) {
            hash.put("events", new Vector());
            return hash;
         } else {
            EventTextTranslator translator = new EventTextTranslator();
            Vector vect = new Vector();

            for(int i = startPage; i < endPage; ++i) {
               Hashtable eventHash = new Hashtable();
               UserEventIce event = userEvents[i];
               eventHash.put("eventType", UserEvent.getEventType(event).toString());
               eventHash.put("text", translator.translate(event, ClientType.MIDP2, (String)null));
               eventHash.put("timestamp", event.timestamp);
               vect.add(eventHash);
            }

            hash.put("events", vect);
            return hash;
         }
      } catch (Exception var13) {
         throw new EJBException(var13.toString());
      }
   }

   public Hashtable getPagingUserEventGeneratedByUser(String username, int pageNumber, int resultsPerPage) {
      Hashtable hash = null;
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.EVENT_SYSTEM_ENABLED)) {
         return new Hashtable();
      } else {
         EventSystemPrx eventSystem = EJBIcePrxFinder.getEventSystemProxy();

         try {
            UserEventIce[] userEvents = eventSystem.getUserEventsGeneratedByUser(username);
            hash = this.createPagingUserEventHashtable(userEvents, pageNumber, resultsPerPage);
            return hash;
         } catch (FusionException var7) {
            throw new EJBException(var7.getMessage());
         } catch (Exception var8) {
            throw new EJBException(var8.getMessage());
         }
      }
   }

   public Hashtable getPagingUserEvents(String username, int pageNumber, int resultsPerPage) {
      Hashtable hash = null;
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.EVENT_SYSTEM_ENABLED)) {
         return new Hashtable();
      } else {
         EventSystemPrx eventSystem = EJBIcePrxFinder.getEventSystemProxy();

         try {
            UserEventIce[] userEvents = eventSystem.getUserEventsForUser(username);
            hash = this.createPagingUserEventHashtable(userEvents, pageNumber, resultsPerPage);
            return hash;
         } catch (Exception var7) {
            throw new EJBException(var7.getMessage());
         }
      }
   }

   public boolean editPhotoCaption(String username, int itemId, String description) {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update scrapbook set description=? where id=?");
         ps.setString(1, description);
         ps.setInt(2, itemId);
         ps.execute();
         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, String.valueOf(itemId));
         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, username);
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

      return true;
   }

   public Hashtable getThemes(String username, int pageNumber, int resultsPerPage) throws EJBException {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;
      if (pageNumber <= 0) {
         pageNumber = 1;
      }

      Hashtable var8;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from theme where status = ? order by id desc");
         ps.setInt(1, ThemeData.StatusEnum.AVAILABLE.value());
         rs = ps.executeQuery();

         int totalResults;
         for(totalResults = 0; rs.next(); ++totalResults) {
         }

         rs.beforeFirst();
         int totalPages = (int)Math.ceil((double)totalResults / (double)resultsPerPage);
         if (pageNumber > totalPages) {
            pageNumber = totalPages;
         }

         int start = (pageNumber - 1) * resultsPerPage;
         if (start < 0) {
            start = 0;
         }

         int end = start + resultsPerPage - 1;
         if (end > totalResults) {
            end = totalResults - 1;
         }

         Hashtable hash = new Hashtable();
         Vector themes = new Vector();

         Hashtable theme;
         for(int count = 0; rs.next(); ++count) {
            if (count >= start && count <= end) {
               theme = new Hashtable();
               theme.put("id", rs.getInt("id"));
               theme.put("name", rs.getString("name"));
               theme.put("description", rs.getString("description"));
               themes.add(theme);
            }

            if (count > end) {
               break;
            }
         }

         hash.put("totalPages", totalPages);
         hash.put("pageNumber", pageNumber);
         hash.put("totalResults", totalResults);
         hash.put("themes", themes);
         theme = hash;
         return theme;
      } catch (Exception var32) {
         var8 = ExceptionHelper.getRootMessageAsHashtable(var32);
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var29) {
            conn = null;
         }

      }

      return var8;
   }

   public boolean changeTheme(String username, int themeID) throws EJBException {
      Connection connSlave = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      try {
         UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);
         if (userPrx == null) {
            throw new EJBException("Unable to locate user proxy for " + username);
         }

         if (themeID == 0) {
            userPrx.themeChanged((String)null);
         } else {
            connSlave = this.dataSourceSlave.getConnection();
            ps = connSlave.prepareStatement("select * from theme where id = ? and status = ?");
            ps.setInt(1, themeID);
            ps.setInt(2, ThemeData.StatusEnum.AVAILABLE.value());
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new EJBException("Theme ID " + themeID + " is not available to " + username);
            }

            String location = rs.getString("location");
            rs.close();
            ps.close();
            connSlave.close();
            userPrx.themeChanged(location);
         }

         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         contentBean.incrementStoreItemSold(StoreItemData.TypeEnum.THEME, themeID, (Connection)null);
      } catch (Exception var22) {
         throw new EJBException(var22);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var19) {
            connSlave = null;
         }

      }

      return true;
   }

   public Vector getLocationsWithMerchantsInCountry(int countryID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector locations = new Vector();

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select id, name, sum(num) num from ( select l1.id, l1.name, count(*) num from location l1, merchantlocation where l1.countryid=? and l1.level=1 and l1.id=merchantlocation.locationid and merchantlocation.status=1 group by id, name union all select l1.id, l1.name, count(*) num from location l1, location l2, merchantlocation where l1.id=l2.parentlocationid and l2.countryid=? and l2.level=2 and l2.id=merchantlocation.locationid and merchantlocation.status=1 group by id, name union all select l1.id, l1.name, count(*) num from location l1, location l2, location l3, merchantlocation where l1.id=l2.parentlocationid and l2.id=l3.parentlocationid and l3.countryid=? and l3.level=3 and l3.id=merchantlocation.locationid and merchantlocation.status=1 group by id, name ) A group by id, name order by name");
         ps.setInt(1, countryID);
         ps.setInt(2, countryID);
         ps.setInt(3, countryID);
         rs = ps.executeQuery();

         while(rs.next()) {
            Hashtable locationHash = new Hashtable();
            locationHash.put("id", rs.getInt("id"));
            locationHash.put("name", rs.getString("name"));
            locationHash.put("num", rs.getInt("num"));
            locations.add(locationHash);
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

      return locations;
   }

   public Vector getLocationsWithMerchantsInParentLocation(int parentLocationID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector locations = new Vector();

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select id, name, sum(num) num from ( select location.id, location.name, count(*) num from location, merchantlocation where location.parentlocationid=? and location.id=merchantlocation.locationid and merchantlocation.status=1 group by id, name union all select l1.id, l1.name, count(*) num from location l1, location l2, merchantlocation where l1.parentlocationid=? and l1.id=l2.parentlocationid and l2.id=merchantlocation.locationid and merchantlocation.status=1 group by id, name ) A group by id, name order by name");
         ps.setInt(1, parentLocationID);
         ps.setInt(2, parentLocationID);
         rs = ps.executeQuery();

         while(rs.next()) {
            Hashtable locationHash = new Hashtable();
            locationHash.put("id", rs.getInt("id"));
            locationHash.put("name", rs.getString("name"));
            locationHash.put("num", rs.getInt("num"));
            locations.add(locationHash);
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

      return locations;
   }

   public Vector getCountriesWithMerchants() throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector countries = new Vector();

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select country.id, country.name, count(*) num from country, location, merchantlocation where country.id = location.countryid and location.id=merchantlocation.locationid and merchantlocation.status=1 group by id, name order by country.name");
         rs = ps.executeQuery();

         while(rs.next()) {
            Hashtable countryHash = new Hashtable();
            countryHash.put("id", rs.getInt("id"));
            countryHash.put("name", rs.getString("name"));
            countryHash.put("num", rs.getInt("num"));
            countries.add(countryHash);
         }
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

      return countries;
   }

   public Vector getLocationPath(int locationID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector path = new Vector();

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT l1.id AS id1, l1.name AS name1, l2.id AS id2, l2.name AS name2, l3.id AS id3, l3.name AS name3 FROM location AS l1 LEFT JOIN location AS l2 ON l2.id = l1.parentlocationid LEFT JOIN location AS l3 ON l3.id = l2.parentlocationid WHERE l1.id = ?");
         ps.setInt(1, locationID);
         rs = ps.executeQuery();
         if (rs.next()) {
            Hashtable locationHash = new Hashtable();
            if (rs.getInt(5) != 0) {
               locationHash.put("id", rs.getInt(5));
               locationHash.put("name", rs.getString(6));
               path.add(locationHash);
            }

            locationHash = new Hashtable();
            if (rs.getInt(3) != 0) {
               locationHash.put("id", rs.getInt(3));
               locationHash.put("name", rs.getString(4));
               path.add(locationHash);
            }

            locationHash = new Hashtable();
            if (rs.getInt(1) != 0) {
               locationHash.put("id", rs.getInt(1));
               locationHash.put("name", rs.getString(2));
               path.add(locationHash);
            }
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

      return path;
   }

   public Vector getMerchantsInLocation(int locationID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector merchants = new Vector();

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from merchantlocation where locationid=? and status=1 order by name");
         ps.setInt(1, locationID);

         Hashtable merchantsHash;
         for(rs = ps.executeQuery(); rs.next(); merchants.add(merchantsHash)) {
            merchantsHash = new Hashtable();
            merchantsHash.put("id", rs.getInt("id"));
            if (rs.getString("username") != null) {
               merchantsHash.put("username", rs.getString("username"));
            }

            merchantsHash.put("name", rs.getString("name"));
            if (rs.getString("address") != null) {
               merchantsHash.put("address", rs.getString("address"));
            }

            if (rs.getString("phonenumber") != null) {
               merchantsHash.put("phonenumber", rs.getString("phonenumber"));
            }

            if (rs.getString("emailaddress") != null) {
               merchantsHash.put("emailaddress", rs.getString("emailaddress"));
            }

            if (rs.getString("notes") != null) {
               merchantsHash.put("notes", rs.getString("notes"));
            }
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

      return merchants;
   }

   public Hashtable getMerchantLocation(int merchantID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Hashtable merchantHash = new Hashtable();

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from merchantlocation where id=? and status=1");
         ps.setInt(1, merchantID);
         rs = ps.executeQuery();

         while(rs.next()) {
            merchantHash.put("id", rs.getInt("id"));
            if (rs.getString("username") != null) {
               merchantHash.put("username", rs.getString("username"));
            }

            merchantHash.put("name", rs.getString("name"));
            if (rs.getString("address") != null) {
               merchantHash.put("address", rs.getString("address"));
            }

            if (rs.getString("phonenumber") != null) {
               merchantHash.put("phonenumber", rs.getString("phonenumber"));
            }

            if (rs.getString("emailaddress") != null) {
               merchantHash.put("emailaddress", rs.getString("emailaddress"));
            }

            if (rs.getString("notes") != null) {
               merchantHash.put("notes", rs.getString("notes"));
            }
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

      return merchantHash;
   }

   public boolean countryHasMerchantLocations(int countryID) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var5;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from country, location, merchantlocation where country.id=location.countryid and location.id=merchantlocation.locationid and merchantlocation.status=1 and country.id=? limit 1");
         ps.setInt(1, countryID);
         rs = ps.executeQuery();
         if (rs.next()) {
            var5 = true;
            return var5;
         }

         var5 = false;
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

   public String[] getMerchantsUserMayPurchaseFrom(String username) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      String[] usernamesToReturn = new String[4];
      List<String> possibleMerchants = new ArrayList();
      Random random = new Random(System.currentTimeMillis());

      String[] var9;
      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "select distinct ae_merchant.username from accountentry ae_user inner join accountentry ae_merchant on ae_user.reference=ae_merchant.id inner join user on ae_merchant.username=user.username where ae_user.type=14 and ae_user.amount > 0 and ae_user.username=? and user.type=3 and user.status=1 and user.fundedbalance > 0 and user.username != ? union distinct select distinct user.username from accountentry inner join voucher on accountentry.reference=voucher.id inner join voucherbatch on voucher.voucherbatchid=voucherbatch.id inner join user on voucherbatch.username=user.username where accountentry.type=2 and accountentry.username=? and user.type=3 and user.status=1 and user.fundedbalance > 0 and user.username != ?";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setString(2, username);
         ps.setString(3, username);
         ps.setString(4, username);
         rs = ps.executeQuery();

         while(rs.next()) {
            if (possibleMerchants.size() == 0) {
               possibleMerchants.add(rs.getString(1));
            } else {
               possibleMerchants.add(random.nextInt(possibleMerchants.size() + 1), rs.getString(1));
            }
         }

         if (possibleMerchants.size() >= 2) {
            usernamesToReturn[0] = (String)possibleMerchants.get(0);
            usernamesToReturn[2] = (String)possibleMerchants.get(1);
            var9 = usernamesToReturn;
            return var9;
         }

         rs.close();
         ps.close();
         sql = "select user.username from user inner join contact on user.username=contact.fusionusername where user.type=3 and user.status=1 and user.fundedbalance > 0 and contact.username=?";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         rs = ps.executeQuery();

         while(rs.next()) {
            if (!possibleMerchants.contains(rs.getString(1))) {
               if (possibleMerchants.size() == 0) {
                  possibleMerchants.add(rs.getString(1));
               } else {
                  possibleMerchants.add(random.nextInt(possibleMerchants.size() + 1), rs.getString(1));
               }
            }
         }

         if (possibleMerchants.size() < 2) {
            rs.close();
            ps.close();
            if (possibleMerchants.size() == 1) {
               usernamesToReturn[0] = (String)possibleMerchants.get(0);
            }

            var9 = usernamesToReturn;
            return var9;
         }

         usernamesToReturn[0] = (String)possibleMerchants.get(0);
         usernamesToReturn[2] = (String)possibleMerchants.get(1);
         var9 = usernamesToReturn;
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

      return var9;
   }

   public Hashtable getBuzzCost(String username, String contactUsername) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Hashtable hash = new Hashtable();

      Hashtable var7;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select country.SMSBuzzCost / currency_buzz.ExchangeRate * currency_user.ExchangeRate as price, currency_user.Code Currency from user, user contact_user, currency currency_user, currency currency_buzz, country where currency_buzz.code =? and user.username=? and user.Currency=currency_user.Code and contact_user.username=? and country.ID = contact_user.countryID");
         ps.setString(1, CurrencyData.baseCurrency);
         ps.setString(2, username);
         ps.setString(3, contactUsername);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Buzz cost not found.");
         }

         hash.put("price", Double.parseDouble(rs.getString("price")));
         hash.put("currency", rs.getString("Currency"));
         var7 = hash;
      } catch (Exception var22) {
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

   public String getSMSCost(String username, String phoneNumberWithIddCode) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      String value = "";

      try {
         if (StringUtil.isBlank(username) || StringUtil.isBlank(phoneNumberWithIddCode)) {
            String var31 = "";
            return var31;
         }

         MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         CountryData countryData = misEJB.getCountryByIDDCode(messageEJB.getIDDCode(phoneNumberWithIddCode), phoneNumberWithIddCode);
         if (countryData == null) {
            throw new EJBException("Unable to determine the country for mobile phone " + phoneNumberWithIddCode);
         }

         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select concat_ws(' ', format(? * c.exchangerate, 2), u.currency) from currency c, user u where u.username=? and u.currency=c.code");
         ps.setDouble(1, countryData.smsCost);
         ps.setString(2, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Unable to calculate sms cost for destination: " + phoneNumberWithIddCode);
         }

         value = rs.getString(1);
      } catch (CreateException var28) {
         log.error(var28);
         throw new EJBException("Unable to calculate sms cost for destination: " + phoneNumberWithIddCode);
      } catch (SQLException var29) {
         log.error(var29);
         throw new EJBException("Unable to calculate sms cost for destination: " + phoneNumberWithIddCode);
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

      return value;
   }

   public Hashtable getSMSCostTable(String search, int page, int numEntries) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Hashtable hash = new Hashtable();
      Vector sms_countries = new Vector();

      Hashtable h;
      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "select SMSCost  / currency_lookout.ExchangeRate * currency_user.ExchangeRate as price, currency_user.Code Currency, country.name from country, currency currency_user, currency currency_lookout where currency_lookout.code = 'AUD' and currency_user.Code = 'USD'";
         if (search.length() > 0) {
            search = search + "%";
            sql = sql + " and country.name like ?";
         }

         ps = conn.prepareStatement(sql);
         if (search.length() > 0) {
            ps.setString(1, search);
         }

         rs = ps.executeQuery();
         int i;
         if (rs.next()) {
            rs.absolute((page - 1) * numEntries + 1);

            for(i = 0; i < numEntries && !rs.isAfterLast(); ++i) {
               h = new Hashtable();
               h.put("country", rs.getString("country.name"));
               h.put("cost", rs.getFloat("price"));
               sms_countries.add(h);
               rs.next();
            }
         }

         i = rs.last() ? rs.getRow() : 0;
         hash.put("totalresults", i);
         hash.put("totalpages", Math.ceil((double)i / (double)numEntries));
         hash.put("sms_countries", sms_countries);
         h = hash;
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

      return h;
   }

   public Hashtable getLocalSMSCost(String username, int countryId) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Hashtable hash = new Hashtable();

      Hashtable var7;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select c.cost / cc.exchangerate * uc.exchangerate as price, u.currency from currency cc, currency uc, user u, ( \tselect \tif(f.cost is null, c.smscost, f.cost) cost, \tif(f.cost is null, 'AUD', f.currency) currency \tfrom \tcountry c left outer join \tfixedsmscost f on (c.id = f.countryid) \twhere \tc.id = ? ) c where c.currency = cc.code and u.currency = uc.code and u.username = ?");
         ps.setInt(1, countryId);
         ps.setString(2, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Local SMS cost not found.");
         }

         hash.put("price", Double.parseDouble(rs.getString("price")));
         hash.put("currency", rs.getString("Currency"));
         var7 = hash;
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

   public Hashtable getLookoutCost(String username) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Hashtable hash = new Hashtable();

      Hashtable var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select country.SMSLookoutCost / currency_lookout.ExchangeRate * currency_user.ExchangeRate as price, currency_user.Code Currency from user, currency currency_user, currency currency_lookout, country where currency_lookout.code = 'AUD' and user.username=? and user.Currency=currency_user.Code and user.countryID = country.ID");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Lookout cost not found.");
         }

         hash.put("price", Double.parseDouble(rs.getString("price")));
         hash.put("currency", rs.getString("Currency"));
         var6 = hash;
      } catch (Exception var21) {
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

   public Hashtable getGroupSMSNotificationCost(String username) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Hashtable hash = new Hashtable();

      Hashtable var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select ? / currency_lookout.ExchangeRate * currency_user.ExchangeRate as price, currency_user.Code Currency from user, currency currency_user, currency currency_lookout where currency_lookout.code = 'AUD' and user.username=? and user.Currency=currency_user.Code");
         ps.setDouble(1, SystemProperty.getDouble("GroupSMSNotificationCost"));
         ps.setString(2, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Lookout cost not found.");
         }

         hash.put("price", Double.parseDouble(rs.getString("price")));
         hash.put("currency", rs.getString("Currency"));
         var6 = hash;
      } catch (Exception var21) {
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

   public Hashtable getKickCost(String username) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Hashtable hash = new Hashtable();

      Hashtable var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select ? / currency_lookout.ExchangeRate * currency_user.ExchangeRate as price, currency_user.Code Currency from user, currency currency_user, currency currency_lookout where currency_lookout.code = 'AUD' and user.username=? and user.Currency=currency_user.Code");
         ps.setDouble(1, SystemProperty.getDouble("ChatRoomKickCost"));
         ps.setString(2, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("ChatRoomKickCost cost not found.");
         }

         hash.put("price", Double.parseDouble(rs.getString("price")));
         hash.put("currency", rs.getString("Currency"));
         var6 = hash;
      } catch (Exception var21) {
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

   public Hashtable getEmailAlertCost(String username) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Hashtable hash = new Hashtable();

      Hashtable var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select country.SMSEmailAlertCost / currency_emailalert.ExchangeRate * currency_user.ExchangeRate as price, currency_user.Code Currency from user, currency currency_user, currency currency_emailalert, country where currency_emailalert.code = 'AUD' and user.username=? and user.Currency=currency_user.Code and user.countryID = country.ID");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Email alert cost not found.");
         }

         hash.put("price", Double.parseDouble(rs.getString("price")));
         hash.put("currency", rs.getString("Currency"));
         var6 = hash;
      } catch (Exception var21) {
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

   public Hashtable getTransactionSummaryByMonth(String username, int month, int year, String type) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Hashtable hash = new Hashtable();
      if (month < 1) {
         month = 1;
      }

      if (month > 12) {
         month = 12;
      }

      int maxAEPeriodBeforeArchival = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_ACCOUNTENTRY_PERIOD_BEFORE_ARCHIVAL_IN_DAYS);
      Calendar accountEntryArchivalBoundary = new GregorianCalendar();
      accountEntryArchivalBoundary.setTime(new Date());
      accountEntryArchivalBoundary.add(5, -1 * maxAEPeriodBeforeArchival);

      Hashtable var13;
      try {
         Calendar startCal = new GregorianCalendar(year, month - 1, 1);
         Calendar endCal = new GregorianCalendar(year, month, 1);
         if (startCal.before(accountEntryArchivalBoundary)) {
            startCal = accountEntryArchivalBoundary;
         }

         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select count(*), -sum(amount) from accountentry where username = ? and type in (?,?) and datecreated >= ? and datecreated < ? and amount < 0");
         ps.setString(1, username);
         ps.setInt(2, AccountEntryData.TypeEnum.USER_TO_USER_TRANSFER.value());
         ps.setInt(3, AccountEntryData.TypeEnum.VOUCHERS_CREATED.value());
         ps.setTimestamp(4, new Timestamp(startCal.getTime().getTime()));
         ps.setTimestamp(5, new Timestamp(endCal.getTime().getTime()));
         rs = ps.executeQuery();
         rs.next();
         hash.put("totalSales", rs.getDouble(2));
         hash.put("numberOfSales", rs.getInt(1));
         rs.close();
         ps.close();
         ps = conn.prepareStatement("select count(*), sum(amount) from accountentry where username = ? and type in (?,?,?,?,?,?) and datecreated >= ? and datecreated < ? and amount > 0");
         ps.setString(1, username);
         ps.setInt(2, AccountEntryData.TypeEnum.CREDIT_CARD.value());
         ps.setInt(3, AccountEntryData.TypeEnum.VOUCHER_RECHARGE.value());
         ps.setInt(4, AccountEntryData.TypeEnum.TELEGRAPHIC_TRANSFER.value());
         ps.setInt(5, AccountEntryData.TypeEnum.BANK_TRANSFER.value());
         ps.setInt(6, AccountEntryData.TypeEnum.WESTERN_UNION.value());
         ps.setInt(7, AccountEntryData.TypeEnum.BLUE_LABEL_ONE_VOUCHER.value());
         ps.setTimestamp(8, new Timestamp(startCal.getTime().getTime()));
         ps.setTimestamp(9, new Timestamp(endCal.getTime().getTime()));
         rs = ps.executeQuery();
         rs.next();
         hash.put("totalCredits", rs.getDouble(2));
         hash.put("numberOfCredits", rs.getInt(1));
         var13 = hash;
      } catch (Exception var28) {
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

      return var13;
   }

   protected List<AccountEntryData> getAccountEntriesForCustomerByDate(String username, String customername, Date startDate, Date endDate, String accountType) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      double balance = 0.0D;
      String typeString = "";
      String sql;
      if (accountType != null && accountType.length() != 0) {
         String[] types = accountType.split(",");

         for(int i = 0; i < types.length; ++i) {
            sql = types[i].trim();
            typeString = typeString + (typeString.length() > 0 ? "," : "") + AccountEntryData.TypeEnum.valueOf(sql).value();
         }
      }

      int maxAEPeriodBeforeArchival = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_ACCOUNTENTRY_PERIOD_BEFORE_ARCHIVAL_IN_DAYS);
      Calendar accountEntryArchivalBoundary = new GregorianCalendar();
      accountEntryArchivalBoundary.setTime(new Date());
      accountEntryArchivalBoundary.add(5, -1 * maxAEPeriodBeforeArchival);
      if (startDate.getTime() < accountEntryArchivalBoundary.getTimeInMillis()) {
         startDate = accountEntryArchivalBoundary.getTime();
      }

      try {
         conn = this.dataSourceSlave.getConnection();
         sql = "SELECT a1.* FROM accountentry a1, accountentry a2 WHERE a1.datecreated>=? and a1.datecreated<=? and a1.type != 17 and a1.reference=CAST(a2.id AS CHAR) AND a2.username=? AND a2.amount<0 and a1.username=? union select accountentry.* from accountentry join voucher on voucher.voucherbatchid = accountentry.reference where username=? and type=17 and redeemedby=?";
         ps = conn.prepareStatement(sql);
         ps.setDate(1, new java.sql.Date(startDate.getTime()));
         ps.setDate(2, new java.sql.Date(endDate.getTime()));
         ps.setString(3, customername);
         ps.setString(4, username);
         ps.setString(5, username);
         ps.setString(6, customername);
         rs = ps.executeQuery();
         LinkedList accountEntryList = new LinkedList();

         while(rs.next()) {
            AccountEntryData accountEntry = new AccountEntryData();
            accountEntry.id = rs.getLong("id");
            accountEntry.username = rs.getString("username");
            accountEntry.dateCreated = new Date(rs.getTimestamp("dateCreated").getTime());
            accountEntry.type = AccountEntryData.TypeEnum.fromValue(rs.getInt("type"));
            accountEntry.reference = rs.getString("reference");
            accountEntry.description = rs.getString("description");
            accountEntry.currency = rs.getString("currency");
            accountEntry.exchangeRate = rs.getDouble("exchangerate");
            accountEntry.amount = rs.getDouble("amount");
            accountEntry.fundedAmount = rs.getDouble("fundedAmount");
            accountEntry.tax = rs.getDouble("tax");
            accountEntry.costOfGoodsSold = rs.getDouble("costOfGoodsSold");
            accountEntry.costOfTrial = rs.getDouble("costOfTrial");
            accountEntry.runningBalance = balance;
            balance += rs.getDouble("amount");
            accountEntryList.add(accountEntry);
         }

         LinkedList var35 = accountEntryList;
         return var35;
      } catch (SQLException var31) {
         throw new EJBException(var31.getMessage());
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

   public Hashtable getTransactionSummaryForCustomerByDate(String username, String customername, Date startDate, Date endDate, String type) {
      Hashtable hash = new Hashtable();

      try {
         double totalSales = 0.0D;
         int numberOfSales = 0;
         List<AccountEntryData> accountEntries = this.getAccountEntriesForCustomerByDate(username, customername, startDate, endDate, type);
         int i = 0;

         while(i < accountEntries.size()) {
            AccountEntryData.TypeEnum entryType = ((AccountEntryData)accountEntries.get(i)).type;
            switch(entryType) {
            case USER_TO_USER_TRANSFER:
            case VOUCHERS_CREATED:
               if (((AccountEntryData)accountEntries.get(i)).amount < 0.0D) {
                  totalSales += ((AccountEntryData)accountEntries.get(i)).amount * ((AccountEntryData)accountEntries.get(i)).exchangeRate;
                  ++numberOfSales;
               }
            default:
               ++i;
            }
         }

         hash.put("totalSales", totalSales);
         hash.put("numberOfSales", numberOfSales);
         hash.put("totalCredits", 0);
         hash.put("numberOfCredits", 0);
         return hash;
      } catch (Exception var13) {
         throw new EJBException(var13.getMessage());
      }
   }

   public Hashtable getTransactionSummaryByMonthForCustomer(String username, String customername, int month, int year, String type) {
      if (month < 1) {
         month = 1;
      }

      if (month > 12) {
         month = 12;
      }

      try {
         Calendar startCal = new GregorianCalendar(year, month - 1, 1);
         Calendar endCal = new GregorianCalendar(year, month - 1, startCal.getMaximum(5));
         return this.getTransactionSummaryForCustomerByDate(username, customername, startCal.getTime(), endCal.getTime(), type);
      } catch (Exception var8) {
         throw new EJBException(var8.getMessage());
      }
   }

   public Hashtable getTransactionSummaryForCustomer(String username, String customername, String type) {
      Calendar startCal = new GregorianCalendar(1970, 1, 1);
      Calendar endCal = new GregorianCalendar();
      return this.getTransactionSummaryForCustomerByDate(username, customername, startCal.getTime(), endCal.getTime(), type);
   }

   public Hashtable getTransactions(String username, int pagenumber, int resultsperpage, String type) {
      Calendar startCal = new GregorianCalendar(1970, 1, 1);
      Calendar endCal = new GregorianCalendar();
      return this.getTransactionsByDate(username, startCal.getTime(), endCal.getTime(), pagenumber, resultsperpage, type);
   }

   public Hashtable getTransactionsSinceDate(String username, int fromDate, int pagenumber, int resultsperpage, String type) {
      Calendar endCal = new GregorianCalendar();
      Date fromd = new Date((long)fromDate);
      return this.getTransactionsByDate(username, fromd, endCal.getTime(), pagenumber, resultsperpage, type);
   }

   public Hashtable getTransactionsSinceDateAndType(String username, int fromDate, int pagenumber, int resultsperpage, int type) {
      Calendar endCal = new GregorianCalendar();
      Date fromd = new Date((long)fromDate);
      String typeString = "";
      switch(type) {
      case 1:
         typeString = "CREDIT_CARD,VOUCHER_RECHARGE,TELEGRAPHIC_TRANSFER,BANK_TRANSFER,WESTERN_UNION,BLUE_LABEL_ONE_VOUCHER";
         break;
      case 2:
         typeString = "USER_TO_USER_TRANSFER,VOUCHERS_CREATED";
         break;
      case 3:
         typeString = "USER_TO_USER_TRANSFER,VOUCHERS_CREATED,CREDIT_CARD,VOUCHER_RECHARGE,TELEGRAPHIC_TRANSFER,BANK_TRANSFER,WESTERN_UNION,BLUE_LABEL_ONE_VOUCHER";
      }

      return this.getTransactionsByDate(username, fromd, endCal.getTime(), pagenumber, resultsperpage, typeString);
   }

   public Hashtable getTransactionsByDate(String username, Date fromDate, Date toDate, int pagenumber, int resultsperpage, String accountType) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         String typeString = "";
         String sql;
         if (accountType != null && accountType.length() != 0) {
            String[] types = accountType.split(",");

            for(int i = 0; i < types.length; ++i) {
               sql = types[i].trim();
               typeString = typeString + (typeString.length() > 0 ? "," : "") + AccountEntryData.TypeEnum.valueOf(sql).value();
            }
         }

         int maxAEPeriodBeforeArchival = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_ACCOUNTENTRY_PERIOD_BEFORE_ARCHIVAL_IN_DAYS);
         Calendar accountEntryArchivalBoundary = new GregorianCalendar();
         accountEntryArchivalBoundary.setTime(new Date());
         accountEntryArchivalBoundary.add(5, -1 * maxAEPeriodBeforeArchival);
         if (fromDate.getTime() < accountEntryArchivalBoundary.getTimeInMillis()) {
            fromDate = accountEntryArchivalBoundary.getTime();
         }

         conn = this.dataSourceSlave.getConnection();
         sql = "select sourceaccount.*, destaccount.username destusername from accountentry as sourceaccount left join accountentry as destaccount on destaccount.id = sourceaccount.reference where sourceaccount.username=? and sourceaccount.datecreated>=? and sourceaccount.datecreated<=? and sourceaccount.amount != 0 ";
         if (typeString.length() > 0) {
            sql = sql + " and sourceaccount.type in (" + typeString + ") ";
         }

         sql = sql + "order by sourceaccount.datecreated desc limit ? offset ?";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setDate(2, new java.sql.Date(fromDate.getTime()));
         ps.setDate(3, new java.sql.Date(toDate.getTime() + 86400000L));
         ps.setInt(4, resultsperpage + 1);
         ps.setInt(5, (pagenumber - 1) * resultsperpage);
         rs = ps.executeQuery();
         Hashtable hash = new Hashtable();
         Vector v = new Vector();
         double balance = 0.0D;
         boolean hasMore = false;

         while(true) {
            Hashtable acchash;
            if (rs.next()) {
               if (rs.getRow() <= resultsperpage) {
                  acchash = HashObjectUtils.dataObjectToHashtable(new AccountEntryData(rs));
                  acchash.put("destinationUsername", rs.getString("destusername"));
                  acchash.put("runningBalance", balance);
                  v.add(acchash);
                  balance += rs.getDouble("sourceaccount.amount");
                  continue;
               }

               hasMore = true;
            }

            hash.put("totalResults", v.size());
            hash.put("page", pagenumber);
            hash.put("hasMore", hasMore);
            hash.put("accountEntries", v);
            acchash = hash;
            return acchash;
         }
      } catch (SQLException var34) {
         throw new EJBException(var34.getMessage());
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var31) {
            conn = null;
         }

      }
   }

   public int getUserReferralCountByMonth(String username, int month, int year) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      if (month < 1) {
         month = 1;
      }

      if (month > 12) {
         month = 12;
      }

      int var9;
      try {
         Calendar startCal = new GregorianCalendar(year, month - 1, 1);
         Calendar endCal = new GregorianCalendar(year, month - 1, startCal.getMaximum(5));
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select count(id) as referrals from userreferral where username=? and datecreated>=? and datecreated<=?");
         ps.setString(1, username);
         ps.setDate(2, new java.sql.Date(startCal.getTime().getTime()));
         ps.setDate(3, new java.sql.Date(endCal.getTime().getTime()));
         rs = ps.executeQuery();
         if (!rs.next()) {
            byte var29 = 0;
            return var29;
         }

         var9 = rs.getInt("referrals");
      } catch (SQLException var27) {
         throw new EJBException(var27.getMessage());
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var24) {
            conn = null;
         }

      }

      return var9;
   }

   public int getUserReferralCount(String username) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      byte var5;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select count(id) as referrals from userreferral where username=?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (rs.next()) {
            int var25 = rs.getInt("referrals");
            return var25;
         }

         var5 = 0;
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

   public Hashtable getUserReferral(String username, Date startDate, Date endDate, int pagenumber, int resultsperpage) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Hashtable hash = new Hashtable();

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select userreferral.mobilephone as phonenumber, user.username as username, userreferral.datecreated as datecreated from userreferral left join user on user.mobilephone = userreferral.mobilephone where userreferral.username=? and userreferral.datecreated>=? and userreferral.datecreated<=?");
         ps.setString(1, username);
         ps.setDate(2, new java.sql.Date(startDate.getTime()));
         ps.setDate(3, new java.sql.Date(endDate.getTime()));
         rs = ps.executeQuery();

         int totalresults;
         for(totalresults = 0; rs.next(); ++totalresults) {
         }

         rs.beforeFirst();
         int remainder = totalresults % resultsperpage;
         int totalpages = totalresults / resultsperpage + (remainder != 0 ? 1 : 0);
         if (totalpages < 0) {
            totalpages = 0;
         }

         if (pagenumber > totalpages) {
            pagenumber = totalpages;
         }

         int start = (pagenumber - 1) * resultsperpage;
         if (start < 0) {
            start = 0;
         }

         int end = start + resultsperpage - 1;
         if (end >= totalresults) {
            end = totalresults - 1;
         }

         Vector v = new Vector();
         int count = 0;

         while(true) {
            Hashtable referral;
            if (rs.next()) {
               if (count >= start && count <= end) {
                  referral = new Hashtable();
                  referral.put("mobilephone", rs.getString("phonenumber"));
                  referral.put("username", rs.getString("username") == null ? "" : rs.getString("username"));
                  referral.put("datecreated", rs.getDate("datecreated").getTime());
                  v.add(referral);
               }

               if (count <= end) {
                  ++count;
                  continue;
               }
            }

            hash.put("totalresults", totalresults);
            hash.put("pagenumber", pagenumber);
            hash.put("totalpages", totalpages);
            hash.put("invitations", v);
            referral = hash;
            return referral;
         }
      } catch (SQLException var32) {
         throw new EJBException(var32.getMessage());
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var29) {
            conn = null;
         }

      }
   }

   public Hashtable getUserReferralByMonth(String username, int month, int year, int pagenumber, int resultsperpage) {
      if (month < 1) {
         month = 1;
      }

      if (month > 12) {
         month = 12;
      }

      try {
         Calendar startCal = new GregorianCalendar(year, month - 1, 1);
         Calendar endCal = new GregorianCalendar(year, month, 1);
         return this.getUserReferral(username, startCal.getTime(), endCal.getTime(), pagenumber, resultsperpage);
      } catch (EJBException var8) {
         throw new EJBException(var8.getMessage());
      }
   }

   public Hashtable getUserReferral(String username, int pagenumber, int resultsperpage) {
      try {
         Calendar startCal = new GregorianCalendar(1970, 1, 1);
         Calendar endCal = new GregorianCalendar();
         endCal.add(6, 1);
         return this.getUserReferral(username, startCal.getTime(), endCal.getTime(), pagenumber, resultsperpage);
      } catch (EJBException var6) {
         throw new EJBException(var6.getMessage());
      }
   }

   public String getPreviousMobileNumber(String username) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var5;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select activation.mobilephone as mobilephone from user left join activation on activation.username = user.username where user.mobileverified = 0 and user.username=? order by activation.datecreated desc limit 1");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (rs.next()) {
            var5 = rs.getString("mobilephone");
            return var5;
         }

         var5 = "";
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

   public String changeMobileNumber(String username, String mobileNumber, String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.changeMobilePhone(username, mobileNumber, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return "";
      } catch (CreateException var8) {
         return ExceptionHelper.getRootMessage(var8);
      } catch (EJBException var9) {
         return ExceptionHelper.getRootMessage(var9);
      }
   }

   public String cancelMobileNumberChange(String username, String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.cancelChangeMobilePhoneRequest(username, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return "";
      } catch (CreateException var7) {
         return ExceptionHelper.getRootMessage(var7);
      } catch (EJBException var8) {
         return ExceptionHelper.getRootMessage(var8);
      }
   }

   public Hashtable getUserVirtualGift(String session_username, String username, int giftid) {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      Hashtable gift;
      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "select virtualgiftreceived.id as id, virtualgiftreceived.virtualgiftid as giftid, virtualgiftreceived.datecreated as datecreated, virtualgiftreceived.sender as sender, virtualgift.name as name, virtualgift.location64x64png as location, virtualgiftreceived.message as message, virtualgiftreceived.removed as removed, virtualgiftreceived.private as private from virtualgiftreceived, virtualgift where username=? and virtualgift.id = virtualgiftreceived.virtualgiftid and virtualgiftreceived.id = ? ";
         if (!session_username.equals(username)) {
            sql = sql + " and virtualgiftreceived.private=0 ";
         }

         sql = sql + "order by virtualgiftreceived.datecreated desc";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setInt(2, giftid);
         rs = ps.executeQuery();
         gift = new Hashtable();
         if (rs.next()) {
            gift.put("id", rs.getInt("id"));
            gift.put("giftid", rs.getInt("giftid"));
            gift.put("sender", rs.getString("sender"));
            gift.put("datecreated", rs.getTimestamp("datecreated").getTime());
            gift.put("location", rs.getString("location"));
            gift.put("name", rs.getString("name"));
            gift.put("message", rs.getString("message") == null ? "" : rs.getString("message"));
            gift.put("removed", rs.getString("removed"));
            gift.put("private", rs.getInt("private"));
         }

         Hashtable var9 = gift;
         return var9;
      } catch (Exception var27) {
         gift = ExceptionHelper.getRootMessageAsHashtable(var27);
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var24) {
            conn = null;
         }

      }

      return gift;
   }

   public boolean removeUserVirtualGift(String username, int giftID) {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update virtualgiftreceived set removed=1 where username = ? and id=?");
         ps.setString(1, username);
         ps.setInt(2, giftID);
         if (ps.executeUpdate() == 1) {
            if (SystemProperty.getBool("UseRedisDataStore", true)) {
               try {
                  UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                  int userId = userEJB.getUserID(username, (Connection)null);
                  if (userId == -1) {
                     log.error("Unable to retrieve User ID for username [" + username + "]");
                     throw new EJBException("Invalid username specified");
                  }

                  GiftsReceivedCounter.decrementCacheCount(userId);
               } catch (Exception var23) {
                  log.error("Unable to decrement gifts received counter for username [" + username + "]: " + var23);
                  throw new EJBException("Invalid username specified");
               }
            } else {
               MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.NUM_VIRTUAL_GIFTS_RECEIVED, username);
            }
         }
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
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

      return true;
   }

   public Hashtable getVirtualGiftForUser(String session_username, String username, int pageNumber, int resultsPerPage) {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;
      if (pageNumber <= 0) {
         pageNumber = 1;
      }

      Hashtable var9;
      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "select virtualgiftreceived.id as id, virtualgiftreceived.virtualgiftid as giftid, virtualgiftreceived.datecreated as datecreated, virtualgiftreceived.sender as sender, virtualgift.name as name, virtualgift.location16x16gif as location, virtualgiftreceived.message as message, virtualgiftreceived.removed as removed, virtualgiftreceived.private as private from virtualgiftreceived, virtualgift where username=? and virtualgift.id = virtualgiftreceived.virtualgiftid and virtualgiftreceived.removed = 0 ";
         if (!session_username.equals(username)) {
            sql = sql + " and virtualgiftreceived.private=0 ";
         }

         sql = sql + "order by virtualgiftreceived.id desc limit 100";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         rs = ps.executeQuery();

         int totalResults;
         for(totalResults = 0; rs.next(); ++totalResults) {
         }

         rs.beforeFirst();
         int totalPages = (int)Math.ceil((double)totalResults / (double)resultsPerPage);
         if (pageNumber > totalPages) {
            pageNumber = totalPages;
         }

         int start = (pageNumber - 1) * resultsPerPage;
         if (start < 0) {
            start = 0;
         }

         int end = start + resultsPerPage - 1;
         if (end > totalResults) {
            end = totalResults - 1;
         }

         Hashtable hash = new Hashtable();
         Vector gifts = new Vector();
         int count = 0;

         while(true) {
            Hashtable gift;
            if (rs.next()) {
               if (count >= start && count <= end) {
                  gift = new Hashtable();
                  gift.put("id", rs.getInt("id"));
                  gift.put("giftid", rs.getInt("giftid"));
                  gift.put("sender", rs.getString("sender"));
                  gift.put("datecreated", rs.getTimestamp("datecreated").getTime());
                  gift.put("location", rs.getString("location"));
                  gift.put("name", rs.getString("name"));
                  gift.put("message", rs.getString("message") == null ? "" : rs.getString("message"));
                  gift.put("removed", rs.getInt("removed"));
                  gift.put("private", rs.getInt("private"));
                  gifts.add(gift);
               }

               if (count <= end) {
                  ++count;
                  continue;
               }
            }

            hash.put("totalPages", totalPages);
            hash.put("pageNumber", pageNumber);
            hash.put("totalResults", totalResults);
            hash.put("virtualgifts", gifts);
            gift = hash;
            return gift;
         }
      } catch (Exception var34) {
         var9 = ExceptionHelper.getRootMessageAsHashtable(var34);
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var31) {
            conn = null;
         }

      }

      return var9;
   }

   public Hashtable getVirtualGiftReceivedSummary(String session_username, String username) {
      Hashtable hashtable = new Hashtable();
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var8;
      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "SELECT vin.id AS id, vgr.sender AS sender, vg.id AS giftid, vgr.datecreated AS datecreated, vin.giftcount AS giftcount, vg.location16x16gif AS location, vg.name AS NAME FROM virtualgiftreceived vgr, virtualgift vg, (SELECT MAX(id) AS id, COUNT(*) AS giftcount FROM virtualgiftreceived WHERE username=? AND removed = 0  GROUP BY virtualgiftid) vin WHERE vgr.id = vin.id AND vg.id = vgr.virtualgiftid ";
         if (!session_username.equals(username)) {
            sql = sql + " AND private = 0 ";
         }

         sql = sql + " ORDER BY vin.id DESC";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         rs = ps.executeQuery();

         int totalResults;
         for(totalResults = 0; rs.next(); ++totalResults) {
         }

         rs.beforeFirst();
         Vector v = new Vector();

         Hashtable hash;
         while(rs.next()) {
            hash = new Hashtable();
            hash.put("id", rs.getInt("id"));
            hash.put("giftid", rs.getInt("giftid"));
            hash.put("sender", rs.getString("sender"));
            hash.put("datecreated", rs.getDate("datecreated").getTime());
            hash.put("giftcount", rs.getInt("giftcount"));
            hash.put("location", rs.getString("location"));
            hash.put("name", rs.getString("name"));
            v.add(hash);
         }

         hashtable.put("total", totalResults);
         hashtable.put("gifts", v);
         hash = hashtable;
         return hash;
      } catch (Exception var28) {
         var8 = ExceptionHelper.getRootMessageAsHashtable(var28);
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

      return var8;
   }

   public Hashtable getVirtualGift(String username, int giftId) {
      Hashtable content = new Hashtable();

      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         VirtualGiftData gift = contentBean.getVirtualGift(giftId, (String)null, username);
         if (gift != null) {
            content.put("id", gift.getId());
            content.put("name", gift.getName());
            content.put("price", gift.getRoundedPrice());
            content.put("currency", gift.getCurrency());
            content.put("location", gift.getLocation16x16GIF());
            content.put("largelocation", gift.getLocation64x64PNG());
            content.put("status", gift.getStatus().toString());
         } else {
            content.put("error", "Unable to find gift");
         }

         return content;
      } catch (Exception var6) {
         throw new EJBException(var6.getMessage());
      }
   }

   public Hashtable getVirtualGiftContent(String username, int groupID, int pageNumber, int numEntries) throws EJBException {
      Hashtable content = new Hashtable();
      if (pageNumber <= 0) {
         pageNumber = 1;
      }

      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         List<VirtualGiftData> virtualGifts = contentBean.getVirtualGifts(username, groupID, 0);
         int totalGifts = virtualGifts.size();
         int totalPages = (int)Math.ceil((double)totalGifts / (double)numEntries);
         if (pageNumber > totalPages) {
            pageNumber = totalPages;
         }

         int start = (pageNumber - 1) * numEntries;
         if (start < 0) {
            start = 0;
         }

         int end = start + numEntries - 1;
         if (end >= totalGifts) {
            end = totalGifts - 1;
         }

         content.put("page", pageNumber);
         content.put("numEntries", totalGifts);
         content.put("numPages", totalPages);
         Vector v = new Vector();

         for(int i = start; i <= end; ++i) {
            VirtualGiftData gift = (VirtualGiftData)virtualGifts.get(i);
            Hashtable contentItem = new Hashtable();
            contentItem.put("id", gift.getId());
            contentItem.put("name", gift.getName());
            contentItem.put("price", gift.getRoundedPrice());
            contentItem.put("currency", gift.getCurrency());
            contentItem.put("location", gift.getLocation16x16GIF());
            v.add(contentItem);
         }

         content.put("gifts", v);
         return content;
      } catch (Exception var16) {
         throw new EJBException(var16.getMessage());
      }
   }

   public String buyVirtualGift(String username, String receiverUsername, int giftId, int purchaseLocation, boolean privateGift, String message, String chatroomName, String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      try {
         int rateLimitSeconds = SystemProperty.getInt((String)"GiftSingleRateLimitInSeconds", 60);
         if (!MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.VIRTUALGIFT.toString(), MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.RateLimitKeySpace.VIRTUAL_GIFT_RATE_LIMIT, username, receiverUsername, Integer.toString(giftId)), 1L, (long)(rateLimitSeconds * 1000))) {
            return String.format("You can only send the same gift to %s every %s. Try sending a different gift.", receiverUsername, DateTimeUtils.timeInvertalInSecondsToPrettyString(rateLimitSeconds));
         } else {
            ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
            contentBean.buyVirtualGift(username, receiverUsername.trim().toLowerCase(), giftId, purchaseLocation, privateGift, message, chatroomName, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
            return "";
         }
      } catch (EJBException var14) {
         return "Unable to send gift now. Please try again later.";
      } catch (FusionEJBException var15) {
         return var15.getMessage();
      } catch (CreateException var16) {
         return "Unable to send gift now. Please try again later.";
      }
   }

   public String buyAvatarItem(int buyerUserID, int recipientUserID, int avatarItemID, String ipAddress, String sessionID, String mobileDevice, String userAgent) {
      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         contentBean.buyAvatarItem(buyerUserID, recipientUserID, avatarItemID, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return "";
      } catch (Exception var9) {
         return var9.getMessage();
      }
   }

   public Vector getExternalClientDownloadDetail(String version) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector v = new Vector();

      try {
         String sql = "select * from externaldownloadlink where status = 1";
         if (version.length() > 0) {
            sql = sql + " and version=?";
         }

         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement(sql);
         if (version.length() > 0) {
            ps.setString(1, version);
         }

         rs = ps.executeQuery();
         int startRange = 1;

         while(rs.next()) {
            ExternalDownloadLinkData ext = new ExternalDownloadLinkData(rs);
            ext.setRange(startRange);
            startRange = ext.endRange + 1;
            v.add(HashObjectUtils.dataObjectToHashtable(ext));
         }

         Vector var25 = v;
         return var25;
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

   public float getUnredeemedVoucherValue(String merchantUsername) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      float amount;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select u.currency as currency, sum(b.amount / bc.exchangerate * uc.exchangerate) as amount from user u, voucherbatch b, voucher v, currency uc, currency bc where u.username = b.username and b.id = v.voucherbatchid and u.currency = uc.code and b.currency = bc.code and b.username = ? and v.status = 1 group by u.currency");
         ps.setString(1, merchantUsername);
         rs = ps.executeQuery();
         if (rs.next()) {
            amount = rs.getFloat("amount");
            float var6 = amount;
            return var6;
         }

         amount = 0.0F;
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

      return amount;
   }

   public Hashtable getMerchantCustomersAndFriends(String merchantUsername, int pagenumber, int resultsperpage) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Hashtable hash = new Hashtable();

      Hashtable var14;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT username FROM user WHERE merchantcreated=? UNION select fusionusername from contact where username = ? and fusionusername is not null and status = 1 UNION SELECT DISTINCT LCASE(a1.username) username FROM accountentry a1, accountentry a2 WHERE a1.type=14 AND a1.reference=CAST(a2.id AS CHAR) AND a2.type=14 AND a2.username=? AND a2.amount<0 UNION select distinct lcase(accountentry.username) username from accountentry join voucher on voucher.voucherbatchid = accountentry.reference and voucher.status=3 where username=? and type=17 ORDER BY username");
         ps.setString(1, merchantUsername);
         ps.setString(2, merchantUsername);
         ps.setString(3, merchantUsername);
         ps.setString(4, merchantUsername);
         rs = ps.executeQuery();

         int totalresults;
         for(totalresults = 0; rs.next(); ++totalresults) {
         }

         rs.beforeFirst();
         int totalpages = totalresults / resultsperpage + 1;
         if (totalpages < 1) {
            totalpages = 1;
         }

         if (pagenumber > totalpages) {
            pagenumber = totalpages;
         }

         int start = (pagenumber - 1) * resultsperpage;
         if (start < 0) {
            start = 0;
         }

         int end = start + resultsperpage - 1;
         if (end >= totalresults) {
            end = totalresults - 1;
         }

         int count = 0;

         Vector possibleRecipients;
         for(possibleRecipients = new Vector(); rs.next(); ++count) {
            if (count >= start && count <= end) {
               possibleRecipients.add(rs.getString(1));
            }

            if (count > end) {
               break;
            }
         }

         hash.put("totalresults", totalresults);
         hash.put("totalpages", totalpages);
         hash.put("page", pagenumber);
         hash.put("recipients", possibleRecipients);
         var14 = hash;
      } catch (SQLException var29) {
         throw new EJBException(var29.getMessage());
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

      return var14;
   }

   public Hashtable getMerchantCustomers(String merchantUsername, int pagenumber, int resultsperpage) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Hashtable hash = new Hashtable();

      Hashtable var14;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT username FROM user WHERE merchantcreated=? UNION SELECT DISTINCT LCASE(a1.username) username FROM accountentry a1, accountentry a2 WHERE a1.type=14 AND a1.reference=CAST(a2.id AS CHAR) AND a2.type=14 AND a2.username=? AND a2.amount<0 UNION select distinct lcase(accountentry.username) username from accountentry join voucher on voucher.voucherbatchid = accountentry.reference and voucher.status=3 where username=? and type=17 ORDER BY username");
         ps.setString(1, merchantUsername);
         ps.setString(2, merchantUsername);
         ps.setString(3, merchantUsername);
         rs = ps.executeQuery();

         int totalresults;
         for(totalresults = 0; rs.next(); ++totalresults) {
         }

         rs.beforeFirst();
         int totalpages = totalresults / resultsperpage + 1;
         if (totalpages < 1) {
            totalpages = 1;
         }

         if (pagenumber > totalpages) {
            pagenumber = totalpages;
         }

         int start = (pagenumber - 1) * resultsperpage;
         if (start < 0) {
            start = 0;
         }

         int end = start + resultsperpage - 1;
         if (end >= totalresults) {
            end = totalresults - 1;
         }

         int count = 0;

         Vector possibleRecipients;
         for(possibleRecipients = new Vector(); rs.next(); ++count) {
            if (count >= start && count <= end) {
               possibleRecipients.add(rs.getString(1));
            }

            if (count > end) {
               break;
            }
         }

         hash.put("totalresults", totalresults);
         hash.put("totalpages", totalpages);
         hash.put("page", pagenumber);
         hash.put("recipients", possibleRecipients);
         var14 = hash;
      } catch (SQLException var29) {
         throw new EJBException(var29.getMessage());
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

      return var14;
   }

   public Hashtable getMerchantCustomerTransactions(String merchantUsername, String username, int pagenumber, int resultsperpage) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Hashtable hash = new Hashtable();
      int maxAEPeriodBeforeArchival = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_ACCOUNTENTRY_PERIOD_BEFORE_ARCHIVAL_IN_DAYS);

      Hashtable var33;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT a1.* FROM accountentry a1, accountentry a2 WHERE a1.type=14 AND a1.reference=CAST(a2.id AS CHAR) AND a2.type=14 AND a2.username=? AND a2.amount<0 and a1.username=? and a1.datecreated >= date_sub(curdate(), interval ? day) union select accountentry.* from accountentry join voucher on voucher.voucherbatchid = accountentry.reference where username=? and type=17 and redeemedby=? and accountentry.datecreated >= date_sub(curdate(), interval ? day)");
         ps.setString(1, username);
         ps.setString(2, merchantUsername);
         ps.setInt(3, maxAEPeriodBeforeArchival);
         ps.setString(4, merchantUsername);
         ps.setString(5, username);
         ps.setInt(6, maxAEPeriodBeforeArchival);
         rs = ps.executeQuery();

         int totalresults;
         for(totalresults = 0; rs.next(); ++totalresults) {
         }

         rs.beforeFirst();
         int totalpages = totalresults / resultsperpage + 1;
         if (totalpages < 1) {
            totalpages = 1;
         }

         if (pagenumber > totalpages) {
            pagenumber = totalpages;
         }

         int start = (pagenumber - 1) * resultsperpage;
         if (start < 0) {
            start = 0;
         }

         int end = start + resultsperpage - 1;
         if (end >= totalresults) {
            end = totalresults - 1;
         }

         int count = 0;

         Vector entries;
         for(entries = new Vector(); rs.next(); ++count) {
            if (count >= start && count <= end) {
               AccountEntryData aed = new AccountEntryData(rs);
               entries.add(HashObjectUtils.dataObjectToHashtable(aed));
            }

            if (count > end) {
               break;
            }
         }

         hash.put("totalresults", totalresults);
         hash.put("totalpages", totalpages);
         hash.put("page", pagenumber);
         hash.put("account_entries", entries);
         var33 = hash;
      } catch (SQLException var31) {
         throw new EJBException(var31.getMessage());
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

      return var33;
   }

   public String getMostRecentlyVisitedChatroom(String username, String chatroomPrefix) throws EJBException {
      List<String> recentChatRoomList = RecentChatRoomList.getRecentChatRoomList(recentChatRoomMemcache, username);
      if (recentChatRoomList != null) {
         Iterator i$ = recentChatRoomList.iterator();

         while(i$.hasNext()) {
            String recentChatRoom = (String)i$.next();
            if (recentChatRoom.startsWith(chatroomPrefix)) {
               return recentChatRoom;
            }
         }
      }

      return "";
   }

   public Hashtable getGroup(int id) throws EJBException {
      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         GroupData groupData = userEJB.getGroup(id);
         return groupData == null ? new Hashtable() : HashObjectUtils.dataObjectToHashtable(groupData);
      } catch (Exception var4) {
         return ExceptionHelper.getRootMessageAsHashtable(var4);
      }
   }

   public Hashtable getGroupAnnouncements(int groupID, int page, int numEntries) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var8;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select a.*, u.displaypicture from groupannouncement a, user u where a.createdby = u.username and a.groupid = ? and a.status = ? order by id desc");
         ps.setInt(1, groupID);
         ps.setInt(2, GroupAnnouncementData.StatusEnum.ACTIVE.value());
         Vector groupAnnouncements = new Vector();
         rs = ps.executeQuery();
         int i;
         if (rs.next()) {
            rs.absolute((page - 1) * numEntries + 1);

            for(i = 0; i < numEntries && !rs.isAfterLast(); ++i) {
               GroupAnnouncementData announcementData = new GroupAnnouncementData(rs);
               announcementData.picture = rs.getString("displaypicture");
               groupAnnouncements.add(HashObjectUtils.dataObjectToHashtable(announcementData));
               rs.next();
            }
         }

         i = rs.last() ? rs.getRow() : 0;
         Hashtable hash = new Hashtable();
         hash.put("totalresults", i);
         hash.put("totalpages", Math.ceil((double)i / (double)numEntries));
         hash.put("page", page);
         hash.put("group_announcements", groupAnnouncements);
         Hashtable var10 = hash;
         return var10;
      } catch (SQLException var28) {
         var8 = ExceptionHelper.getRootMessageAsHashtable(var28);
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

      return var8;
   }

   public Hashtable getGroupAnnouncement(int id) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var5;
      try {
         Hashtable var6;
         try {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("select a.*, u.displaypicture from groupannouncement a, user u where a.createdby = u.username and a.id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
               GroupAnnouncementData announcementData = new GroupAnnouncementData(rs);
               announcementData.picture = rs.getString("displaypicture");
               var6 = HashObjectUtils.dataObjectToHashtable(announcementData);
               return var6;
            }

            var5 = new Hashtable();
         } catch (SQLException var27) {
            var6 = ExceptionHelper.getRootMessageAsHashtable(var27);
            return var6;
         }
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var24) {
            conn = null;
         }

      }

      return var5;
   }

   public Hashtable createGroupAnnouncement(String username, int groupID, String title, String text, String smsText) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var10;
      try {
         GroupAnnouncementData announcementData = new GroupAnnouncementData();
         announcementData.groupID = groupID;
         announcementData.dateCreated = new Date();
         announcementData.createdBy = username;
         announcementData.title = title;
         announcementData.text = text;
         announcementData.smsText = smsText != null && smsText.length() != 0 ? smsText : null;
         announcementData.lastModifiedDate = announcementData.dateCreated;
         announcementData.lastModifiedBy = username;
         announcementData.status = GroupAnnouncementData.StatusEnum.ACTIVE;
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("insert into groupannouncement (groupid, datecreated, createdby, title, text, smsText, lastmodifieddate, lastmodifiedby, status) values (?,?,?,?,?,?,?,?,?)", 1);
         ps.setObject(1, announcementData.groupID);
         ps.setTimestamp(2, new Timestamp(announcementData.dateCreated.getTime()));
         ps.setString(3, announcementData.createdBy);
         ps.setString(4, announcementData.title);
         ps.setString(5, announcementData.text);
         ps.setString(6, announcementData.smsText);
         ps.setTimestamp(7, new Timestamp(announcementData.lastModifiedDate.getTime()));
         ps.setString(8, announcementData.lastModifiedBy);
         ps.setObject(9, announcementData.status == null ? null : announcementData.status.value());
         ps.executeUpdate();
         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            var10 = ExceptionHelper.setErrorMessageAsHashtable("Failed to insert group announcement into database");
            return var10;
         }

         announcementData.id = rs.getInt(1);
         rs.close();
         ps.close();
         connMaster.close();

         try {
            UserNotificationServicePrx userNotificationService = EJBIcePrxFinder.getUserNotificationServiceProxy();

            try {
               EmailUserNotification note = new EmailUserNotification();
               note.subject = title;
               note.message = text;
               userNotificationService.notifyFusionGroupAnnouncementViaEmail(groupID, note);
            } catch (FusionException var39) {
               log.warn("FusionException while notifying group members of a new announcement via email", var39);
            } catch (Exception var40) {
               log.warn("Exception while notifying group members of a new announcement via email", var40);
            }

            if (StringUtils.hasLength(smsText)) {
               try {
                  SMSUserNotification note = new SMSUserNotification();
                  note.message = smsText;
                  note.smsSubType = SystemSMSData.SubTypeEnum.GROUP_ANNOUNCEMENT_NOTIFICATION.value();
                  userNotificationService.notifyFusionGroupAnnouncementViaSMS(groupID, note);
               } catch (FusionException var37) {
                  log.warn("FusionException while notifying group members of a new announcement via SMS", var37);
               } catch (Exception var38) {
                  log.warn("Exception while notifying group members of a new announcement via SMS", var38);
               }
            }
         } catch (Exception var41) {
            log.warn("Exception while notifying group members of a new announcement", var41);
         }

         var10 = HashObjectUtils.dataObjectToHashtable(announcementData);
      } catch (SQLException var42) {
         var10 = ExceptionHelper.getRootMessageAsHashtable(var42);
         return var10;
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var36) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var35) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var34) {
            connMaster = null;
         }

      }

      return var10;
   }

   public Hashtable updateGroupAnnouncement(int announcementID, String username, int groupID, String title, String text) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      Hashtable var10;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update groupannouncement set title = ?, text = ?, lastmodifieddate = ?, lastmodifiedby = ? where id = ?");
         ps.setString(1, title);
         ps.setString(2, text);
         ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
         ps.setString(4, username);
         ps.setInt(5, announcementID);
         if (ps.executeUpdate() != 1) {
            ExceptionHelper.setErrorMessageAsHashtable("Failed to update group announcement");
         }

         Hashtable var9 = this.getGroupAnnouncement(announcementID);
         return var9;
      } catch (SQLException var28) {
         var10 = ExceptionHelper.getRootMessageAsHashtable(var28);
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var25) {
            conn = null;
         }

      }

      return var10;
   }

   public Hashtable getGroupChatroomCategoriesAndStadiums(int groupID, int parentChatRoomCategoryID, int page, int numEntries) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var9;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "select cc.id, cc.name from chatroomcategory cc, chatroom cr where cr.chatroomcategoryid=cc.id and cr.groupid=? and cr.status=1 and cc.status=1 and cc.groupeventonly=0 ";
         if (parentChatRoomCategoryID > 0) {
            sql = sql + "and cc.parentchatroomcategoryid=? ";
         }

         sql = sql + "union select cc.id, cc.name from chatroomcategory cc, groupevent ge where cc.id = ge.chatroomcategoryid and cc.status=1 and ge.status=1 and ge.groupid=? AND UNIX_TIMESTAMP(now()) >= UNIX_TIMESTAMP(date_add(ge.starttime, interval -2 day)) and UNIX_TIMESTAMP(now()) <= (UNIX_TIMESTAMP(ge.starttime) + (ge.durationminutes*60)) ";
         if (parentChatRoomCategoryID > 0) {
            sql = sql + "and cc.parentchatroomcategoryid=? ";
         }

         sql = sql + "group by id, name order by id";
         ps = connSlave.prepareStatement(sql);
         if (parentChatRoomCategoryID > 0) {
            ps.setInt(1, groupID);
            ps.setInt(2, parentChatRoomCategoryID);
            ps.setInt(3, groupID);
            ps.setInt(4, parentChatRoomCategoryID);
         } else {
            ps.setInt(1, groupID);
            ps.setInt(2, groupID);
         }

         Vector chatroomCategories = new Vector();
         rs = ps.executeQuery();
         int i;
         Hashtable hash;
         if (rs.next()) {
            rs.absolute((page - 1) * numEntries + 1);

            for(i = 0; i < numEntries && !rs.isAfterLast(); ++i) {
               hash = new Hashtable();
               hash.put("id", rs.getInt("id"));
               hash.put("name", rs.getString("name"));
               chatroomCategories.add(hash);
               rs.next();
            }
         }

         i = rs.last() ? rs.getRow() : 0;
         hash = new Hashtable();
         hash.put("totalresults", i);
         hash.put("totalpages", Math.ceil((double)i / (double)numEntries));
         hash.put("page", page);
         hash.put("chatroom_categories", chatroomCategories);
         rs.close();
         ps.close();
         Vector stadiums = new Vector();
         ps = connSlave.prepareStatement("select * from chatroom where groupid = ? and status = 1 and type = 2 order by datecreated");
         ps.setInt(1, groupID);
         rs = ps.executeQuery();

         while(rs.next()) {
            ChatRoomData chatRoomData = new ChatRoomData(rs);
            stadiums.add(HashObjectUtils.dataObjectToHashtable(chatRoomData));
         }

         hash.put("stadiums", stadiums);
         Hashtable var34 = hash;
         return var34;
      } catch (SQLException var31) {
         var9 = ExceptionHelper.getRootMessageAsHashtable(var31);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var28) {
            connSlave = null;
         }

      }

      return var9;
   }

   public Hashtable getGroupChatrooms(int groupID, int chatRoomCategoryID, int page, int numEntries) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var9;
      try {
         conn = this.dataSourceSlave.getConnection();
         if (chatRoomCategoryID > 0) {
            ps = conn.prepareStatement("select * from chatroom where groupid = ? and status = 1 and chatroomcategoryid = ? order by type desc, datecreated");
         } else {
            ps = conn.prepareStatement("select * from chatroom where groupid = ? and status = 1 and chatroomcategoryid is null order by type desc, datecreated");
         }

         ps.setInt(1, groupID);
         if (chatRoomCategoryID > 0) {
            ps.setInt(2, chatRoomCategoryID);
         }

         Vector chatrooms = new Vector();
         rs = ps.executeQuery();
         int i;
         if (rs.next()) {
            rs.absolute((page - 1) * numEntries + 1);

            for(i = 0; i < numEntries && !rs.isAfterLast(); ++i) {
               ChatRoomData chatRoomData = new ChatRoomData(rs);
               ChatRoomPrx chatRoomPrx = EJBIcePrxFinder.findChatRoomPrx(chatRoomData.name);
               if (chatRoomPrx != null) {
                  chatRoomData.size = chatRoomPrx.getNumParticipants();
               }

               chatrooms.add(HashObjectUtils.dataObjectToHashtable(chatRoomData));
               rs.next();
            }
         }

         i = rs.last() ? rs.getRow() : 0;
         Hashtable hash = new Hashtable();
         hash.put("totalresults", i);
         hash.put("totalpages", Math.ceil((double)i / (double)numEntries));
         hash.put("page", page);
         hash.put("group_chatrooms", chatrooms);
         Hashtable var33 = hash;
         return var33;
      } catch (SQLException var29) {
         var9 = ExceptionHelper.getRootMessageAsHashtable(var29);
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

      return var9;
   }

   public Hashtable getGroupDonators(int groupID, int page, int numEntries) throws EJBException {
      Hashtable hash = new Hashtable();
      hash.put("totalresults", 0);
      hash.put("totalpages", 0);
      hash.put("page", 0);
      hash.put("group_donators", new Vector());
      return hash;
   }

   public String joinGroup(String username, int groupID, int locationID, String ipAddress, String sessionID, String mobileDevice, String userAgent, boolean smsNotification, boolean emailNotification, boolean eventNotification, boolean smsGroupEventNotification, boolean emailThreadUpdateNotification, boolean eventThreadUpdateNotification) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         if (groupID == SystemProperty.getInt("IndosatGroupID") && !this.isIndosatIP(ipAddress)) {
            String var66 = ExceptionHelper.setErrorMessage("You must be using Indosat network to join this group");
            return var66;
         } else {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData userData = userBean.loadUser(username, false, false);
            String var19;
            if (userData != null && userData.status == UserData.StatusEnum.ACTIVE) {
               if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.JOIN_GROUP, userData) && SystemProperty.getBool("JoinGroupDisabledForUnauthenticatedUsers", false)) {
                  var19 = ExceptionHelper.setErrorMessage("You must be authenticated before joining a group.");
                  return var19;
               } else {
                  GroupData groupData = userBean.getGroup(groupID);
                  String var20;
                  if (groupData != null && groupData.status == GroupData.StatusEnum.ACTIVE) {
                     if (groupData.countryID != null && !groupData.countryID.equals(userData.countryID)) {
                        var20 = ExceptionHelper.setErrorMessage("You are not allowed to join a group from a different country");
                        return var20;
                     } else {
                        int maxGroupMembership = SystemProperty.getInt((String)"MaxGroupMembership", 200);
                        connSlave = this.dataSourceSlave.getConnection();
                        ps = connSlave.prepareStatement("select count(*) from groupmember where username = ? and status = ?");
                        ps.setString(1, username);
                        ps.setInt(2, GroupMemberData.StatusEnum.ACTIVE.value());
                        rs = ps.executeQuery();
                        String var70;
                        if (rs.next() && rs.getInt(1) >= maxGroupMembership) {
                           var70 = ExceptionHelper.setErrorMessage("You cannot be a member of more than " + maxGroupMembership + " groups. Please leave some groups first");
                           return var70;
                        } else {
                           rs.close();
                           ps.close();
                           connSlave.close();
                           if (groupData.isClosedGroup()) {
                              connSlave = this.dataSourceSlave.getConnection();
                              ps = connSlave.prepareStatement("select count(*) from groupinvitation where username = ? and groupid = ?");
                              ps.setString(1, username);
                              ps.setInt(2, groupID);
                              rs = ps.executeQuery();
                              if (rs.next() && rs.getInt(1) == 0) {
                                 var70 = ExceptionHelper.setErrorMessage("You are not allowed to join this group without an invitation");
                                 return var70;
                              }

                              rs.close();
                              ps.close();
                              connSlave.close();
                           }

                           MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
                           String var22;
                           if (!messageBean.isUserBlackListedInGroup(username, groupID)) {
                              this.joinGroupWithoutValidation(userData, groupID, locationID, ipAddress, sessionID, mobileDevice, userAgent, smsNotification, emailNotification, eventNotification, smsGroupEventNotification, emailThreadUpdateNotification, eventThreadUpdateNotification);
                              var22 = "TRUE";
                              return var22;
                           } else {
                              var22 = ExceptionHelper.setErrorMessage("You have been blacklisted from this group. Please contact the admin or a moderator of this group.");
                              return var22;
                           }
                        }
                     }
                  } else {
                     var20 = ExceptionHelper.setErrorMessage("Invalid group ID " + groupID);
                     return var20;
                  }
               }
            } else {
               var19 = ExceptionHelper.setErrorMessage("Invalid username " + username);
               return var19;
            }
         }
      } catch (Exception var64) {
         String var18 = ExceptionHelper.getRootMessage(var64);
         return var18;
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var63) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var62) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var61) {
            connSlave = null;
         }

      }
   }

   public boolean joinGroupWithoutValidation(UserData userData, int groupID, int locationID, String ipAddress, String sessionID, String mobileDevice, String userAgent, boolean smsNotification, boolean emailNotification, boolean eventNotification, boolean smsGroupEventNotification, boolean emailThreadUpdateNotification, boolean eventThreadUpdateNotification) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         boolean isOldMember = false;
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select status from groupmember where username = ? and groupid = ?");
         ps.setString(1, userData.username);
         ps.setInt(2, groupID);
         rs = ps.executeQuery();
         if (rs.next()) {
            int status = rs.getInt("status");
            if (status == GroupMemberData.StatusEnum.BANNED.value()) {
               throw new EJBException("You are banned from joining this group");
            }

            if (status == GroupMemberData.StatusEnum.ACTIVE.value()) {
               boolean var19 = false;
               return var19;
            }

            isOldMember = true;
         }

         rs.close();
         ps.close();
         ps = connMaster.prepareStatement("update groups set nummembers=nummembers+1 where id=?");
         ps.setInt(1, groupID);
         ps.executeUpdate();
         ps.close();
         if (isOldMember) {
            ps = connMaster.prepareStatement("update groupmember set dateleft = null, locationid = ?, smsnotification = ?, emailnotification = ?, eventnotification = ?, smsgroupeventnotification = ?, emailthreadupdatenotification = ?, eventthreadupdatenotification = ?, status = ? where username = ? and groupid = ?");
            ps.setObject(1, locationID > 0 ? locationID : null);
            ps.setInt(2, smsNotification ? 1 : 0);
            ps.setInt(3, emailNotification ? 1 : 0);
            ps.setInt(4, eventNotification ? 1 : 0);
            ps.setInt(5, smsGroupEventNotification ? 1 : 0);
            ps.setInt(6, emailThreadUpdateNotification ? 1 : 0);
            ps.setInt(7, eventThreadUpdateNotification ? 1 : 0);
            ps.setInt(8, GroupMemberData.StatusEnum.ACTIVE.value());
            ps.setString(9, userData.username);
            ps.setInt(10, groupID);
         } else {
            ps = connMaster.prepareStatement("insert into groupmember (username, groupid, locationid, datecreated, type, smsnotification, emailnotification, eventnotification, smsgroupeventnotification, emailthreadupdatenotification, eventthreadupdatenotification, status) values (?,?,?,?,?,?,?,?,?,?,?,?)");
            ps.setString(1, userData.username);
            ps.setInt(2, groupID);
            ps.setObject(3, locationID > 0 ? locationID : null);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.setInt(5, GroupMemberData.TypeEnum.REGULAR.value());
            ps.setInt(6, smsNotification ? 1 : 0);
            ps.setInt(7, emailNotification ? 1 : 0);
            ps.setInt(8, eventNotification ? 1 : 0);
            ps.setInt(9, smsGroupEventNotification ? 1 : 0);
            ps.setInt(10, emailThreadUpdateNotification ? 1 : 0);
            ps.setInt(11, eventThreadUpdateNotification ? 1 : 0);
            ps.setInt(12, GroupMemberData.StatusEnum.ACTIVE.value());
         }

         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed to add or update group member ship");
         } else {
            ps.close();
            ps = connMaster.prepareStatement("delete from groupinvitation where username = ? and groupid = ?");
            ps.setString(1, userData.username);
            ps.setInt(2, groupID);
            ps.executeUpdate();
            ps.close();
            this.removeGroupInviteNotification(userData.userID, groupID);
            if (!isOldMember && userData.mobileVerified && groupID == SystemProperty.getInt("IndosatGroupID")) {
               ps = connMaster.prepareStatement("select id from mobileoriginatedsms s, user u where s.sender = u.mobilephone and u.username = ? and s.type = ?");
               ps.setString(1, userData.username);
               ps.setInt(2, MobileOriginatedSMSData.TypeEnum.INDOSAT_SUBSCRIPTION.value());
               rs = ps.executeQuery();
               if (rs.next()) {
                  AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
                  accountEJB.subscribeService(userData.username, SystemProperty.getInt("IndosatServiceID"), new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
               }

               rs.close();
               ps.close();
            }

            return true;
         }
      } catch (SQLException var39) {
         throw new EJBException(var39.getMessage());
      } catch (CreateException var40) {
         throw new EJBException(var40.getMessage());
      } catch (NoSuchFieldException var41) {
         throw new EJBException(var41.getMessage());
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
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var36) {
            connMaster = null;
         }

      }
   }

   public String leaveGroup(String username, int groupID) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;

      String var6;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("update groupmember set dateleft = ?, status = ?, type = ? where username = ? and groupid = ?");
         ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
         ps.setInt(2, GroupMemberData.StatusEnum.INACTIVE.value());
         ps.setInt(3, GroupMemberData.TypeEnum.REGULAR.value());
         ps.setString(4, username);
         ps.setInt(5, groupID);
         ps.executeUpdate();
         ps = connMaster.prepareStatement("update groups set nummembers=nummembers-1 where id=?");
         ps.setInt(1, groupID);
         ps.executeUpdate();
         String var5 = "TRUE";
         return var5;
      } catch (SQLException var20) {
         var6 = ExceptionHelper.getRootMessage(var20);
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

      return var6;
   }

   public String declineAllGroupInvitations(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      String var5;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("delete from groupinvitation where username = ?");
         ps.setString(1, username);
         ps.executeUpdate();

         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData userData = userBean.loadUser(username, false, false);
            if (userData != null && userData.status == UserData.StatusEnum.ACTIVE) {
               int userId = userData.userID;
               UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
               unsProxy.clearAllNotificationsByTypeForUser(userId, Enums.NotificationTypeEnum.GROUP_INVITE.getType());
            }
         } catch (Exception var22) {
            log.warn("Failed to remove pending group invite notfication for user: " + username + ", reason: " + var22.getLocalizedMessage());
         }

         String var25 = "TRUE";
         return var25;
      } catch (SQLException var23) {
         var5 = ExceptionHelper.getRootMessage(var23);
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

      return var5;
   }

   public String declineGroupInvitation(String username, int groupID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      String var6;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("delete from groupinvitation where username = ? and groupid = ?");
         ps.setString(1, username);
         ps.setInt(2, groupID);
         ps.executeUpdate();

         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData userData = userBean.loadUser(username, false, false);
            if (userData != null && userData.status == UserData.StatusEnum.ACTIVE) {
               this.removeGroupInviteNotification(userData.userID, groupID);
            }
         } catch (Exception var21) {
            log.warn("Failed to remove pending group invite notfication for user: " + username + ", reason: " + var21.getLocalizedMessage());
         }

         String var24 = "TRUE";
         return var24;
      } catch (SQLException var22) {
         var6 = ExceptionHelper.getRootMessage(var22);
      } finally {
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

      return var6;
   }

   private void removeGroupInviteNotification(int userId, int groupId) {
      try {
         UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
         unsProxy.clearNotificationsForUser(userId, Enums.NotificationTypeEnum.GROUP_INVITE.getType(), new String[]{userId + "/" + groupId});
      } catch (Exception var4) {
         log.warn("Failed to remove pending group invite notfication for user: " + userId + ", reason: " + var4.getLocalizedMessage());
      }

   }

   public String setGroupMemberOptions(String username, int groupID, int locationID, boolean smsNotification, boolean emailNotification, boolean eventNotification, boolean smsGroupEventNotification, boolean emailThreadUpdateNotification, boolean eventThreadUpdateNotification) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      String var13;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update groupmember set locationID = ?, smsnotification = ?, emailnotification = ?, eventnotification = ?, smsgroupeventnotification = ?, emailthreadupdatenotification = ?, eventthreadupdatenotification = ? where username = ? and groupid = ?");
         ps.setObject(1, locationID > 0 ? locationID : null);
         ps.setInt(2, smsNotification ? 1 : 0);
         ps.setInt(3, emailNotification ? 1 : 0);
         ps.setInt(4, eventNotification ? 1 : 0);
         ps.setInt(5, smsGroupEventNotification ? 1 : 0);
         ps.setInt(6, emailThreadUpdateNotification ? 1 : 0);
         ps.setInt(7, eventThreadUpdateNotification ? 1 : 0);
         ps.setString(8, username);
         ps.setInt(9, groupID);
         ps.executeUpdate();
         String var12 = "TRUE";
         return var12;
      } catch (SQLException var27) {
         var13 = ExceptionHelper.getRootMessage(var27);
      } finally {
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

      return var13;
   }

   public Hashtable makeGroupDonation(String username, int groupID, String externalVoucherNumber, boolean visible) throws EJBException {
      return new Hashtable();
   }

   public String inviteMobilePhoneToGroup(String username, String displayName, String mobilePhone, int groupID, String groupName, String ipAddress, String sessionID, String mobileDevice, String userAgent) throws EJBException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.inviteFriend(username, displayName, mobilePhone, groupID, groupName, (String)null, (String)null, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return "TRUE";
      } catch (CreateException var11) {
         return ExceptionHelper.getRootMessage(var11);
      } catch (EJBException var12) {
         return ExceptionHelper.getRootMessage(var12);
      } catch (FusionEJBException var13) {
         return ExceptionHelper.getRootMessage(var13);
      }
   }

   public String inviteUserToGroup(String inviterUsername, String inviteeUsername, int groupID) throws EJBException {
      Connection connMaster = null;
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         String var9;
         try {
            if (inviterUsername != null && inviterUsername.length() == 0) {
               inviterUsername = null;
            }

            UserLocal userBean;
            int inviteeUserId;
            try {
               userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               inviteeUserId = userBean.getUserID(inviteeUsername, (Connection)null);
            } catch (Exception var68) {
               throw new EJBException("Invalid username specified");
            }

            String var10;
            try {
               userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.SEND_GROUP_INVITE, userBean.getUserAuthenticatedAccessControlParameter(inviterUsername)) && SystemProperty.getBool("SendGroupInviteDisabledForUnauthenticatedUsers", false)) {
                  var10 = ExceptionHelper.setErrorMessage("You must authenticate your account before sending a group invite.");
                  return var10;
               }

               if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.RECEIVE_GROUP_INVITE, userBean.getUserAuthenticatedAccessControlParameter(inviteeUsername)) && SystemProperty.getBool("ReceiveGroupInviteDisabledForUnauthenticatedUsers", false)) {
                  var10 = ExceptionHelper.setErrorMessage("You can only invite authenticated users to a group.");
                  return var10;
               }
            } catch (Exception var69) {
               var10 = ExceptionHelper.setErrorMessage("Unable to send invite at this time. Please try again later.");
               return var10;
            }

            connMaster = this.dataSourceMaster.getConnection();
            ps = connMaster.prepareStatement("select * from groupmember where username = ? and groupid = ? and status != ?");
            ps.setString(1, inviteeUsername);
            ps.setInt(2, groupID);
            ps.setInt(3, GroupMemberData.StatusEnum.INACTIVE.value());
            rs = ps.executeQuery();
            if (rs.next()) {
               var9 = ExceptionHelper.setErrorMessage(inviteeUsername + " is already a member of this group");
               return var9;
            } else {
               rs.close();
               ps.close();
               ps = connMaster.prepareStatement("select * from groupinvitation where username = ? and groupid = ? and status = ?");
               ps.setString(1, inviteeUsername);
               ps.setInt(2, groupID);
               ps.setInt(3, GroupInvitationData.StatusEnum.PENDING.value());
               rs = ps.executeQuery();
               if (rs.next()) {
                  var9 = "TRUE";
                  return var9;
               } else {
                  rs.close();
                  ps.close();
                  ps = connMaster.prepareStatement("insert into groupinvitation (username, groupid, datecreated, inviter, status) values (?,?,?,?,?)");
                  ps.setString(1, inviteeUsername);
                  ps.setInt(2, groupID);
                  long currentTimeMillis = System.currentTimeMillis();
                  ps.setTimestamp(3, new Timestamp(currentTimeMillis));
                  ps.setString(4, inviterUsername);
                  ps.setInt(5, GroupInvitationData.StatusEnum.PENDING.value());
                  if (ps.executeUpdate() != 1) {
                     String var74 = ExceptionHelper.setErrorMessage("Failed send invitation to inviterUsername");
                     return var74;
                  } else {
                     HashMap parameters = new HashMap();

                     try {
                        UserPrx userPrx = EJBIcePrxFinder.findUserPrx(inviteeUsername);
                        if (userPrx != null) {
                           String groupInvitationAlert = SystemProperty.get("GroupInvitationAlert", "");
                           if (groupInvitationAlert.length() > 0) {
                              UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                              GroupData groupData = userEJB.getGroup(groupID);
                              groupInvitationAlert = groupInvitationAlert.replaceAll("%inviter%", inviterUsername).replaceAll("%groupname%", groupData.name);
                              parameters.put("alertMessage", groupInvitationAlert);
                           }
                        } else {
                           GroupInvitationData groupInvitation = new GroupInvitationData();
                           groupInvitation.id = null;
                           groupInvitation.username = inviteeUsername;
                           groupInvitation.groupID = groupID;
                           groupInvitation.dateCreated = new Date(currentTimeMillis);
                           groupInvitation.inviter = inviterUsername;
                           groupInvitation.status = GroupInvitationData.StatusEnum.PENDING;
                           MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.GROUP_INVITATION, inviteeUsername, groupInvitation);
                        }
                     } catch (Exception var67) {
                        log.warn("Unable to send group invitation alert message to " + inviteeUsername, var67);
                     }

                     try {
                        UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
                        UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                        GroupData groupData = userEJB.getGroup(groupID);
                        int inviterUserId = userEJB.getUserID(inviterUsername, (Connection)null);
                        parameters.put("groupId", Integer.toString(groupID));
                        parameters.put("inviterUserId", Integer.toString(inviterUserId));
                        parameters.put("groupName", groupData.name);
                        parameters.put("groupPicture", groupData.picture);
                        String key = inviteeUserId + "/" + groupID;
                        unsProxy.notifyFusionUser(new Message(key, inviteeUserId, inviteeUsername, Enums.NotificationTypeEnum.GROUP_INVITE.getType(), currentTimeMillis, parameters));
                     } catch (Exception var66) {
                        log.error("Failed to push group invitation notification to user [" + inviteeUsername + "]", var66);
                     }

                     String var76 = "TRUE";
                     return var76;
                  }
               }
            }
         } catch (SQLException var70) {
            var9 = ExceptionHelper.setErrorMessage(var70.getMessage());
            return var9;
         }
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var65) {
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var64) {
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var63) {
         }

         try {
            if (connSlave != null) {
               ((Connection)connSlave).close();
            }
         } catch (SQLException var62) {
         }

      }
   }

   public String updateStadiumDescription(String stadiumName, String description) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      String var6;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update chatroom set description = ? where type = ? and name = ?");
         ps.setString(1, description);
         ps.setInt(2, ChatRoomData.TypeEnum.STADIUM.value());
         ps.setString(3, stadiumName);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated == 1) {
            ChatRoomUtils.invalidateChatRoomCache(stadiumName);
            ChatRoomPrx chatRoomPrx = EJBIcePrxFinder.findChatRoomPrx(stadiumName);
            if (chatRoomPrx != null) {
               chatRoomPrx.setDescription(description);
            }
         }

         var6 = "TRUE";
         return var6;
      } catch (SQLException var20) {
         var6 = ExceptionHelper.getRootMessage(var20);
      } finally {
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

   public Hashtable getLocations(int countryID, int parentLocationID, int page, int numEntries) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var9;
      try {
         conn = this.dataSourceSlave.getConnection();
         if (parentLocationID > 0) {
            ps = conn.prepareStatement("select * from location where countryid = ? and parentlocationid = ? order by name");
            ps.setInt(1, countryID);
            ps.setInt(2, parentLocationID);
         } else {
            ps = conn.prepareStatement("select * from location where countryid = ? and parentlocationid is null order by name");
            ps.setInt(1, countryID);
         }

         Vector locations = new Vector();
         rs = ps.executeQuery();
         Hashtable ht;
         int i;
         if (rs.next()) {
            rs.absolute((page - 1) * numEntries + 1);

            for(i = 0; i < numEntries && !rs.isAfterLast(); ++i) {
               ht = new Hashtable();
               ht.put("id", rs.getInt("id"));
               ht.put("name", rs.getString("name"));
               locations.add(ht);
               rs.next();
            }
         }

         i = rs.last() ? rs.getRow() : 0;
         ht = new Hashtable();
         ht.put("totalresults", i);
         ht.put("totalpages", Math.ceil((double)i / (double)numEntries));
         ht.put("page", page);
         ht.put("locations", locations);
         ht.put("parentLocationID", parentLocationID);
         Hashtable var11 = ht;
         return var11;
      } catch (SQLException var29) {
         var9 = ExceptionHelper.setErrorMessageAsHashtable(var29.getMessage());
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

      return var9;
   }

   public Hashtable getLocation(int id) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from location where id = ?");
         ps.setInt(1, id);
         Hashtable location = new Hashtable();
         rs = ps.executeQuery();
         if (rs.next()) {
            location.put("id", rs.getInt("id"));
            location.put("parentLocationID", rs.getInt("parentLocationID"));
            location.put("countryID", rs.getInt("countryID"));
            location.put("name", rs.getString("name"));
            location.put("level", rs.getInt("level"));
         }

         var6 = location;
         return var6;
      } catch (SQLException var24) {
         var6 = ExceptionHelper.setErrorMessageAsHashtable(var24.getMessage());
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

   public Hashtable getContactList(String username, int countryID, int page, int numEntries) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var9;
      try {
         conn = this.dataSourceSlave.getConnection();
         if (countryID > 0) {
            ps = conn.prepareStatement("select contact.* from contact, user where contact.fusionusername = user.username and contact.username = ? and user.countryid = ? order by contact.displayname");
            ps.setString(1, username);
            ps.setInt(2, countryID);
         } else {
            ps = conn.prepareStatement("select contact.* from contact, user where contact.fusionusername = user.username and contact.username = ? order by contact.displayname");
            ps.setString(1, username);
         }

         Vector contactList = new Vector();
         rs = ps.executeQuery();
         int i;
         if (rs.next()) {
            rs.absolute((page - 1) * numEntries + 1);

            for(i = 0; i < numEntries && !rs.isAfterLast(); ++i) {
               contactList.add(HashObjectUtils.dataObjectToHashtable(new ContactData(rs)));
               rs.next();
            }
         }

         i = rs.last() ? rs.getRow() : 0;
         Hashtable hash = new Hashtable();
         hash.put("totalresults", i);
         hash.put("totalpages", Math.ceil((double)i / (double)numEntries));
         hash.put("page", page);
         hash.put("contact_list", contactList);
         Hashtable var11 = hash;
         return var11;
      } catch (SQLException var29) {
         var9 = ExceptionHelper.setErrorMessageAsHashtable(var29.getMessage());
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

      return var9;
   }

   public Vector getGroupModules(int groupID) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector groupModules = new Vector();

      Vector var7;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select * from groupmodule where groupid=? and status=1 order by position");
         ps.setInt(1, groupID);
         rs = ps.executeQuery();

         while(rs.next()) {
            groupModules.add(HashObjectUtils.dataObjectToHashtable(new GroupModuleData(rs)));
         }

         return groupModules;
      } catch (SQLException var25) {
         var7 = ExceptionHelper.getRootMessageAsVector(var25);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var22) {
            connSlave = null;
         }

      }

      return var7;
   }

   public Hashtable getGroupEvents(int groupID, int page, int numEntries) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector groupEvents = new Vector();

      Hashtable hash;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select groupevent.*, chatroomcategory.name chatroomcategoryname from groupevent left outer join chatroomcategory on groupevent.chatroomcategoryid=chatroomcategory.id where groupid=? and groupevent.status=1 and (chatroomcategory.status is null or chatroomcategory.status=1) and (durationminutes is null or date_add(starttime, interval durationminutes minute) >= now()) order by starttime");
         ps.setInt(1, groupID);
         rs = ps.executeQuery();
         int i;
         if (rs.next()) {
            rs.absolute((page - 1) * numEntries + 1);

            for(i = 0; i < numEntries && !rs.isAfterLast(); ++i) {
               groupEvents.add(HashObjectUtils.dataObjectToHashtable(GroupEventData.fromResultSetWithChatRoomCategoryName(rs)));
               rs.next();
            }
         }

         i = rs.last() ? rs.getRow() : 0;
         hash = new Hashtable();
         hash.put("totalresults", i);
         hash.put("totalpages", Math.ceil((double)i / (double)numEntries));
         hash.put("page", page);
         hash.put("group_events", groupEvents);
         Hashtable var10 = hash;
         return var10;
      } catch (SQLException var28) {
         hash = ExceptionHelper.getRootMessageAsHashtable(var28);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var25) {
            connSlave = null;
         }

      }

      return hash;
   }

   public Hashtable getGroupModule(int groupModuleID) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var6;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select * from groupmodule where id=? and status=1");
         ps.setInt(1, groupModuleID);
         rs = ps.executeQuery();
         Hashtable var5;
         if (rs.next()) {
            var5 = HashObjectUtils.dataObjectToHashtable(new GroupModuleData(rs));
            return var5;
         }

         var5 = ExceptionHelper.setErrorMessageAsHashtable("Module not found");
         return var5;
      } catch (SQLException var27) {
         var6 = ExceptionHelper.getRootMessageAsHashtable(var27);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var24) {
            connSlave = null;
         }

      }

      return var6;
   }

   public Hashtable getGroupPost(int groupPostID) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var5;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select * from grouppost where id=? and status>0");
         ps.setInt(1, groupPostID);
         rs = ps.executeQuery();
         if (rs.next()) {
            var5 = HashObjectUtils.dataObjectToHashtable(new GroupPostData(rs));
            return var5;
         }

         var5 = ExceptionHelper.setErrorMessageAsHashtable("Post not found");
      } catch (SQLException var27) {
         Hashtable var6 = ExceptionHelper.getRootMessageAsHashtable(var27);
         return var6;
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var24) {
            connSlave = null;
         }

      }

      return var5;
   }

   public Hashtable getGroupPosts(int groupModuleID, int page, int numEntries) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var8;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select id, teaser, datecreated, createdby, length(body) > 0 as hasbody from grouppost where groupmoduleid=? and status=1 order by id desc");
         ps.setInt(1, groupModuleID);
         Vector groupPosts = new Vector();
         rs = ps.executeQuery();
         Hashtable hash;
         if (rs.next()) {
            rs.absolute((page - 1) * numEntries + 1);
            SimpleDateFormat df = new SimpleDateFormat("d MMM yy HH:mm");

            for(int i = 0; i < numEntries && !rs.isAfterLast(); ++i) {
               hash = new Hashtable();
               hash.put("id", rs.getInt("id"));
               hash.put("teaser", rs.getString("teaser"));
               hash.put("datecreated", df.format(rs.getTimestamp("datecreated")));
               hash.put("createdby", rs.getString("createdby"));
               hash.put("hasbody", rs.getBoolean("hasbody"));
               groupPosts.add(hash);
               rs.next();
            }
         }

         int size = rs.last() ? rs.getRow() : 0;
         Hashtable hash = new Hashtable();
         hash.put("totalresults", size);
         hash.put("totalpages", Math.ceil((double)size / (double)numEntries));
         hash.put("page", page);
         hash.put("group_posts", groupPosts);
         hash = hash;
         return hash;
      } catch (SQLException var28) {
         var8 = ExceptionHelper.getRootMessageAsHashtable(var28);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var25) {
            connSlave = null;
         }

      }

      return var8;
   }

   public Hashtable createGroupPost(String username, int groupModuleID, String title, String text) throws EJBException {
      return this.createGroupPost(username, groupModuleID, title, text, GroupPostData.StatusEnum.PREVIEW.value());
   }

   public Hashtable createGroupPost(String username, int groupModuleID, String title, String text, int status) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var10;
      try {
         GroupPostData postData = new GroupPostData();
         postData.groupModuleID = groupModuleID;
         postData.dateCreated = new Date();
         postData.createdBy = username;
         postData.teaser = title;
         postData.body = text;
         postData.lastModifiedDate = postData.dateCreated;
         postData.lastModifiedBy = username;
         postData.status = GroupPostData.StatusEnum.fromValue(status);
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("insert into grouppost (groupmoduleid, datecreated, createdby, teaser, body, lastmodifieddate, lastmodifiedby, status) values (?,?,?,?,?,?,?,?)", 1);
         ps.setObject(1, postData.groupModuleID);
         ps.setTimestamp(2, new Timestamp(postData.dateCreated.getTime()));
         ps.setString(3, postData.createdBy);
         ps.setString(4, postData.teaser);
         ps.setString(5, postData.body);
         ps.setTimestamp(6, new Timestamp(postData.lastModifiedDate.getTime()));
         ps.setString(7, postData.lastModifiedBy);
         ps.setObject(8, postData.status == null ? null : postData.status.value());
         ps.executeUpdate();
         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            var10 = ExceptionHelper.setErrorMessageAsHashtable("Unable to create group post");
            return var10;
         }

         postData.id = rs.getInt(1);
         var10 = HashObjectUtils.dataObjectToHashtable(postData);
      } catch (SQLException var31) {
         var10 = ExceptionHelper.getRootMessageAsHashtable(var31);
         return var10;
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
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var28) {
            connMaster = null;
         }

      }

      return var10;
   }

   public String updateGroupPost(int groupPostID, String username, String title, String text) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var8;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select groupmember.username from groupmember, groups, grouppost, groupmodule where grouppost.groupmoduleid=groupmodule.id and groupmodule.groupid=groups.id and groups.id=groupmember.groupid and groupmember.type=2 and groupmember.username=? and grouppost.id=?");
         ps.setString(1, username);
         ps.setInt(2, groupPostID);
         rs = ps.executeQuery();
         if (rs.next()) {
            rs.close();
            ps.close();
            ps = connMaster.prepareStatement("update grouppost set teaser=?, body=?, lastmodifieddate=?, lastmodifiedby=?, status=? where id=?");
            ps.setString(1, title);
            ps.setString(2, text);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setString(4, username);
            ps.setInt(5, GroupPostData.StatusEnum.PREVIEW.value());
            ps.setInt(6, groupPostID);
            if (ps.executeUpdate() != 1) {
               var8 = ExceptionHelper.setErrorMessage("Failed to update group post");
               return var8;
            }

            var8 = "TRUE";
            return var8;
         }

         var8 = ExceptionHelper.setErrorMessage("You must be an admin of the group");
      } catch (SQLException var33) {
         String var9 = ExceptionHelper.getRootMessage(var33);
         return var9;
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var32) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var31) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var30) {
            connMaster = null;
         }

      }

      return var8;
   }

   public String publishGroupPost(int groupPostID, String username) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var6;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select groupmember.username from groupmember, groups, grouppost, groupmodule where grouppost.groupmoduleid=groupmodule.id and groupmodule.groupid=groups.id and groups.id=groupmember.groupid and groupmember.type=2 and groupmember.username=? and grouppost.id=?");
         ps.setString(1, username);
         ps.setInt(2, groupPostID);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var6 = ExceptionHelper.setErrorMessage("You must be an admin of the group");
            return var6;
         }

         rs.close();
         ps.close();
         ps = connMaster.prepareStatement("update grouppost set status=1 where id=? and status=2");
         ps.setInt(1, groupPostID);
         ps.executeUpdate();
         var6 = "TRUE";
      } catch (SQLException var28) {
         String var7 = ExceptionHelper.getRootMessage(var28);
         return var7;
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

      return var6;
   }

   public String deleteGroupPost(int groupPostID, String username) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var6;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select groupmember.username from groupmember, group, grouppost where grouppost.groupid=group.id and group.id=groupmember.groupid and groupmember.type=2 and groupmember.username=? and grouppost.id=?");
         ps.setString(1, username);
         ps.setInt(2, groupPostID);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var6 = ExceptionHelper.setErrorMessage("You must be an admin of the group");
            return var6;
         }

         rs.close();
         ps.close();
         ps = connMaster.prepareStatement("update grouppost set status=0 where id=?");
         ps.setInt(1, groupPostID);
         if (ps.executeUpdate() == 1) {
            var6 = "TRUE";
            return var6;
         }

         var6 = ExceptionHelper.setErrorMessage("Failed to update group post");
      } catch (SQLException var31) {
         String var7 = ExceptionHelper.getRootMessage(var31);
         return var7;
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
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var28) {
            connMaster = null;
         }

      }

      return var6;
   }

   public Hashtable createGroupModule(String username, int groupID, String title, int position) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var8;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select groupmember.username from groupmember where username=? and groupid=? and type=2");
         ps.setString(1, username);
         ps.setInt(2, groupID);
         rs = ps.executeQuery();
         if (rs.next()) {
            rs.close();
            ps.close();
            GroupModuleData groupModuleData = new GroupModuleData();
            groupModuleData.groupID = groupID;
            groupModuleData.title = title;
            groupModuleData.dateCreated = new Date();
            groupModuleData.createdBy = username;
            groupModuleData.lastModifiedDate = groupModuleData.dateCreated;
            groupModuleData.lastModifiedBy = username;
            groupModuleData.position = position;
            groupModuleData.type = GroupModuleData.TypeEnum.POSTS;
            groupModuleData.status = GroupModuleData.StatusEnum.ACTIVE;
            ps = connMaster.prepareStatement("update groupmodule set position=position+1 where groupid=? and position>=? and status=1");
            ps.setInt(1, groupID);
            ps.setInt(2, position);
            ps.executeUpdate();
            ps.close();
            ps = connMaster.prepareStatement("insert into groupmodule (groupid, title, datecreated, createdby, lastmodifieddate, lastmodifiedby, position, type, status) values (?,?,?,?,?,?,?,?,?)", 1);
            ps.setInt(1, groupModuleData.groupID);
            ps.setString(2, groupModuleData.title);
            ps.setTimestamp(3, new Timestamp(groupModuleData.dateCreated.getTime()));
            ps.setString(4, groupModuleData.createdBy);
            ps.setTimestamp(5, new Timestamp(groupModuleData.lastModifiedDate.getTime()));
            ps.setString(6, groupModuleData.lastModifiedBy);
            ps.setInt(7, groupModuleData.position);
            ps.setInt(8, groupModuleData.type.value());
            ps.setInt(9, groupModuleData.status.value());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (!rs.next()) {
               throw new EJBException("Unable to create group module");
            }

            groupModuleData.id = rs.getInt(1);
            Hashtable var9 = HashObjectUtils.dataObjectToHashtable(groupModuleData);
            return var9;
         }

         var8 = ExceptionHelper.setErrorMessageAsHashtable("You must be an admin of the group");
      } catch (SQLException var27) {
         throw new EJBException(var27.getMessage());
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

      return var8;
   }

   public String updateGroupModule(int groupModuleID, String username, String title, int position) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var8;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select groupmember.username from groupmember, groupmodule where groupmodule.groupid=groupmember.groupid and groupmember.username=? and groupmodule.id=? and groupmember.type=2");
         ps.setString(1, username);
         ps.setInt(2, groupModuleID);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var8 = ExceptionHelper.setErrorMessage("You must be an admin of the group");
            return var8;
         }

         rs.close();
         ps.close();
         ps = connMaster.prepareStatement("select position, groupid from groupmodule where id=?");
         ps.setInt(1, groupModuleID);
         rs = ps.executeQuery();
         if (rs.next()) {
            int oldPosition = rs.getInt("position");
            int groupID = rs.getInt("groupid");
            rs.close();
            ps.close();
            if (position != oldPosition) {
               ps = connMaster.prepareStatement("update groupmodule set position=position-1 where groupid=? and position<=? and position>? and status=1");
               ps.setInt(1, groupID);
               ps.setInt(2, position);
               ps.setInt(3, oldPosition);
               ps.executeUpdate();
               ps.close();
            }

            ps = connMaster.prepareStatement("update groupmodule set title=?, position=?, lastmodifieddate=?, lastmodifiedby=? where id=?");
            ps.setString(1, title);
            ps.setInt(2, position);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setString(4, username);
            ps.setInt(5, groupModuleID);
            if (ps.executeUpdate() != 1) {
               throw new EJBException("Failed to update group module");
            }

            String var10 = "TRUE";
            return var10;
         }

         var8 = ExceptionHelper.setErrorMessage("Module not found");
      } catch (SQLException var31) {
         throw new EJBException(var31.getMessage());
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
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var28) {
            connMaster = null;
         }

      }

      return var8;
   }

   public String deleteGroupModule(int groupModuleID, String username) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var6;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select groupmember.username from groupmember, groupmodule where groupmodule.groupid=groupmember.groupid and groupmember.username=? and groupmodule.id=? and groupmember.type=2");
         ps.setString(1, username);
         ps.setInt(2, groupModuleID);
         rs = ps.executeQuery();
         if (rs.next()) {
            rs.close();
            ps.close();
            ps = connMaster.prepareStatement("select position, groupid from groupmodule where id=?");
            ps.setInt(1, groupModuleID);
            rs = ps.executeQuery();
            if (!rs.next()) {
               var6 = ExceptionHelper.setErrorMessage("Module not found");
               return var6;
            }

            int position = rs.getInt("position");
            int groupID = rs.getInt("groupid");
            rs.close();
            ps.close();
            ps = connMaster.prepareStatement("update groupmodule set position=position-1 where groupid=? and position>? and status=1");
            ps.setInt(1, groupID);
            ps.setInt(2, position);
            ps.executeUpdate();
            ps.close();
            ps = connMaster.prepareStatement("update groupmodule set status=0, position=0 where id=?");
            ps.setInt(1, groupModuleID);
            if (ps.executeUpdate() != 1) {
               throw new EJBException("Failed to delete group module");
            }

            String var8 = "TRUE";
            return var8;
         }

         var6 = ExceptionHelper.setErrorMessage("You must be an admin of the group");
      } catch (SQLException var29) {
         throw new EJBException(var29.getMessage());
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
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var26) {
            connMaster = null;
         }

      }

      return var6;
   }

   public Hashtable createGroupUserPost(String username, int groupID, String body, int parentGroupPostID) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var9;
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData userData = userBean.loadUser(username, false, false);
         Hashtable var50;
         if (userData == null) {
            log.error(String.format("Unable to create user post in group - user '%s' does not exist", username));
            var50 = ExceptionHelper.setErrorMessageAsHashtable("Invalid username");
            return var50;
         }

         if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.CREATE_USER_POST_IN_GROUPS, userData)) {
            var50 = ExceptionHelper.setErrorMessageAsHashtable("You must be authenticated to create a post");
            return var50;
         }

         if (MemCacheOrEJB.getUserReputationLevel(username) < SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MigLevel.GROUP_CREATE_USER_POST_MIN)) {
            var50 = ExceptionHelper.setErrorMessageAsHashtable("You current mig level is not high enough to create a post");
            return var50;
         }

         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select count(*) from userpost where username=? and datecreated >= date_sub(now(), interval 1 hour)");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (rs.next() && rs.getInt(1) > SystemProperty.getInt("MaxUserPostsPerHour")) {
            var50 = ExceptionHelper.setErrorMessageAsHashtable("You are creating too many posts. Please slow down :)");
            return var50;
         }

         rs.close();
         ps.close();
         connSlave.close();
         UserPostData postData = new UserPostData();
         postData.username = username;
         postData.body = body;
         postData.dateCreated = new Date();
         postData.numReplies = 0;
         postData.lastReplyDate = postData.dateCreated;
         if (parentGroupPostID > 0) {
            postData.parentUserPostID = parentGroupPostID;
         }

         postData.status = UserPostData.StatusEnum.ACTIVE;
         WebLocal webBean = (WebLocal)EJBHomeCache.getLocalObject("WebLocal", WebLocalHome.class);
         postData = webBean.createGroupUserPostTransaction(groupID, postData);
         if (parentGroupPostID > 0) {
            this.notifyUsersOfNewGroupUserPost(postData, groupID);
         }

         Hashtable var12 = HashObjectUtils.dataObjectToHashtable(postData);
         return var12;
      } catch (SQLException var46) {
         var9 = ExceptionHelper.getRootMessageAsHashtable(var46);
      } catch (Exception var47) {
         var9 = ExceptionHelper.getRootMessageAsHashtable(var47);
         return var9;
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var45) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var44) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var43) {
            connSlave = null;
         }

      }

      return var9;
   }

   public UserPostData createGroupUserPostTransaction(int groupID, UserPostData postData) throws Exception {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      UserPostData var6;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("insert into userpost (username, body, datecreated, numreplies, lastreplydate, parentuserpostid, status) values (?,?,?,?,?,?,?)", 1);
         ps.setString(1, postData.username);
         ps.setString(2, postData.body);
         ps.setTimestamp(3, new Timestamp(postData.dateCreated.getTime()));
         ps.setInt(4, postData.numReplies);
         ps.setTimestamp(5, new Timestamp(postData.lastReplyDate.getTime()));
         ps.setObject(6, postData.parentUserPostID);
         ps.setInt(7, postData.status.value());
         ps.executeUpdate();
         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            throw new EJBException("Unable to create post");
         }

         postData.id = rs.getInt(1);
         ps.close();
         if (postData.parentUserPostID != null) {
            ps = connMaster.prepareStatement("update userpost set numreplies=numreplies+1, lastreplydate=now() where id=?");
            ps.setInt(1, postData.parentUserPostID);
            ps.executeUpdate();
         }

         ps = connMaster.prepareStatement("insert into groupuserpost (groupid, userpostid) values (?,?)");
         ps.setInt(1, groupID);
         ps.setInt(2, postData.id);
         ps.executeUpdate();
         var6 = postData;
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
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var18) {
            connMaster = null;
         }

      }

      return var6;
   }

   private void notifyUsersOfNewGroupUserPost(UserPostData postData, int groupID) {
   }

   public Hashtable getGroupUserPosts(int groupID, int page, int numEntries) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var8;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select userpost.* from userpost, groupuserpost where userpost.id=groupuserpost.userpostid and groupuserpost.groupid=? and userpost.status=1 and userpost.parentuserpostid is null order by lastreplydate desc");
         ps.setInt(1, groupID);
         Vector groupUserPosts = new Vector();
         rs = ps.executeQuery();
         int i;
         if (rs.next()) {
            rs.absolute((page - 1) * numEntries + 1);

            for(i = 0; i < numEntries && !rs.isAfterLast(); ++i) {
               UserPostData post = new UserPostData(rs);
               groupUserPosts.add(HashObjectUtils.dataObjectToHashtable(post));
               rs.next();
            }
         }

         i = rs.last() ? rs.getRow() : 0;
         Hashtable hash = new Hashtable();
         hash.put("totalresults", i);
         hash.put("totalpages", Math.ceil((double)i / (double)numEntries));
         hash.put("page", page);
         hash.put("group_user_posts", groupUserPosts);
         Hashtable var10 = hash;
         return var10;
      } catch (SQLException var28) {
         var8 = ExceptionHelper.getRootMessageAsHashtable(var28);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var25) {
            connSlave = null;
         }

      }

      return var8;
   }

   public Hashtable getUserPost(int userPostID) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var6;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select * from userpost where id=? and status>0");
         ps.setInt(1, userPostID);
         rs = ps.executeQuery();
         Hashtable var5;
         if (!rs.next()) {
            var5 = ExceptionHelper.setErrorMessageAsHashtable("Post not found");
            return var5;
         }

         var5 = HashObjectUtils.dataObjectToHashtable(new UserPostData(rs));
         return var5;
      } catch (SQLException var27) {
         var6 = ExceptionHelper.getRootMessageAsHashtable(var27);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var24) {
            connSlave = null;
         }

      }

      return var6;
   }

   public Hashtable getUserPostReplies(int userPostID, int page, int numEntries) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var8;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select userpost.* from userpost where userpost.parentuserpostid=? and userpost.status=1 order by datecreated desc");
         ps.setInt(1, userPostID);
         Vector groupUserPosts = new Vector();
         rs = ps.executeQuery();
         int i;
         if (rs.next()) {
            rs.absolute((page - 1) * numEntries + 1);

            for(i = 0; i < numEntries && !rs.isAfterLast(); ++i) {
               UserPostData post = new UserPostData(rs);
               groupUserPosts.add(HashObjectUtils.dataObjectToHashtable(post));
               rs.next();
            }
         }

         i = rs.last() ? rs.getRow() : 0;
         Hashtable hash = new Hashtable();
         hash.put("totalresults", i);
         hash.put("totalpages", Math.ceil((double)i / (double)numEntries));
         hash.put("page", page);
         hash.put("group_user_posts", groupUserPosts);
         Hashtable var10 = hash;
         return var10;
      } catch (SQLException var28) {
         var8 = ExceptionHelper.getRootMessageAsHashtable(var28);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var25) {
            connSlave = null;
         }

      }

      return var8;
   }

   public String deleteGroupUserPost(int groupID, int userPostID, String username) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var8;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select groupmember.username, userpost.parentuserpostid from groupmember, groupuserpost, userpost where groupuserpost.groupid=groupmember.groupid and groupmember.type=2 and groupmember.username=? and groupuserpost.userpostid=userpost.id and userpost.id=? and groupuserpost.groupid=?");
         ps.setString(1, username);
         ps.setInt(2, userPostID);
         ps.setInt(3, groupID);
         rs = ps.executeQuery();
         if (!rs.next()) {
            String var32 = ExceptionHelper.setErrorMessage("You must be an admin of the group");
            return var32;
         }

         int parentUserPostID = rs.getInt("parentuserpostid");
         rs.close();
         ps.close();
         ps = connMaster.prepareStatement("update userpost set status=0 where id=?");
         ps.setInt(1, userPostID);
         if (ps.executeUpdate() == 1) {
            ps.close();
            if (parentUserPostID > 0) {
               ps = connMaster.prepareStatement("select count(*) numreplies, max(datecreated) lastreplydate from userpost where status=1 and parentuserpostid=?");
               ps.setInt(1, parentUserPostID);
               rs = ps.executeQuery();
               if (!rs.next()) {
                  throw new EJBException("Failed to delete group post");
               }

               int numReplies = rs.getInt("numreplies");
               Timestamp lastReplyDate = rs.getTimestamp("lastreplydate");
               rs.close();
               ps.close();
               ps = connMaster.prepareStatement("update userpost set numreplies=?, lastreplydate=? where id=?");
               ps.setInt(1, numReplies);
               ps.setTimestamp(2, lastReplyDate);
               ps.setInt(3, parentUserPostID);
               if (ps.executeUpdate() != 1) {
                  throw new EJBException("Failed to update group post");
               }
            }

            rs.close();
            ps.close();
            connMaster.close();
            var8 = "TRUE";
            return var8;
         }

         var8 = ExceptionHelper.setErrorMessage("Unable to remove group post");
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
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var27) {
            connMaster = null;
         }

      }

      return var8;
   }

   public String createChatroom(String username, String chatRoomName, String language, String description, String keywords, boolean allowKicking) {
      try {
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         ChatRoomData chatRoom = new ChatRoomData();
         chatRoom.creator = username;
         chatRoom.name = chatRoomName;
         chatRoom.language = language;
         chatRoom.description = description;
         chatRoom.allowKicking = allowKicking;
         chatRoom.allowBots = true;
         chatRoom.userOwned = true;
         messageBean.createChatRoom(chatRoom, keywords);
         return "TRUE";
      } catch (CreateException var9) {
         return ExceptionHelper.getRootMessage(var9);
      } catch (EJBException var10) {
         return ExceptionHelper.setErrorMessage(var10.getMessage());
      }
   }

   public String updateRoomDetails(String username, String chatRoomName, String language, String description) throws EJBException {
      try {
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         messageBean.updateRoomDetails(username, chatRoomName, language, description);
         return "TRUE";
      } catch (CreateException var6) {
         return ExceptionHelper.getRootMessage(var6);
      } catch (EJBException var7) {
         return ExceptionHelper.setErrorMessage(var7.getMessage());
      }
   }

   public String updateRoomKickingRule(String username, String chatRoomName, boolean allowKicking) throws EJBException {
      try {
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         messageBean.updateRoomKickingRule(username, chatRoomName, allowKicking);
         return "TRUE";
      } catch (CreateException var5) {
         return ExceptionHelper.getRootMessage(var5);
      } catch (EJBException var6) {
         return ExceptionHelper.setErrorMessage(var6.getMessage());
      }
   }

   public String addRoomModerator(String ownerUsername, String chatRoomName, String moderatorUsername) throws EJBException {
      try {
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         messageBean.addRoomModerator(ownerUsername, chatRoomName, moderatorUsername);
         return "TRUE";
      } catch (CreateException var5) {
         return ExceptionHelper.getRootMessage(var5);
      } catch (EJBException var6) {
         return ExceptionHelper.setErrorMessage(var6.getMessage());
      }
   }

   public String removeRoomModerator(String ownerUsername, String chatRoomName, String moderatorUsername) throws EJBException {
      try {
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         messageBean.removeRoomModerator(ownerUsername, chatRoomName, moderatorUsername);
         return "TRUE";
      } catch (CreateException var5) {
         return ExceptionHelper.getRootMessage(var5);
      } catch (EJBException var6) {
         return ExceptionHelper.setErrorMessage(var6.getMessage());
      }
   }

   public String banUserFromRoom(String ownerUsername, String chatRoomName, String bannedUsername) throws EJBException {
      try {
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         messageBean.banUserFromRoom(ownerUsername, chatRoomName, bannedUsername);
         return "TRUE";
      } catch (CreateException var5) {
         return ExceptionHelper.getRootMessage(var5);
      } catch (EJBException var6) {
         return ExceptionHelper.setErrorMessage(var6.getMessage());
      }
   }

   public String unbanUserFromRoom(String ownerUsername, String chatRoomName, String bannedUsername) throws EJBException {
      try {
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         messageBean.unbanUserFromRoom(ownerUsername, chatRoomName, bannedUsername);
         return "TRUE";
      } catch (CreateException var5) {
         return ExceptionHelper.getRootMessage(var5);
      } catch (EJBException var6) {
         return ExceptionHelper.setErrorMessage(var6.getMessage());
      }
   }

   public String sendChangeRoomOwnerEmail(String oldOwnerUsername, String chatRoomName, String newOwnerUsername) throws EJBException {
      try {
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         messageBean.sendChangeRoomOwnerEmail(oldOwnerUsername, chatRoomName, newOwnerUsername);
         return "TRUE";
      } catch (CreateException var5) {
         return ExceptionHelper.getRootMessage(var5);
      } catch (EJBException var6) {
         return ExceptionHelper.setErrorMessage(var6.getMessage());
      }
   }

   public String changeRoomOwner(String oldOwnerUsername, String chatRoomName, String newOwnerUsername) throws EJBException {
      try {
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         messageBean.changeRoomOwner(oldOwnerUsername, chatRoomName, newOwnerUsername);
         return "TRUE";
      } catch (CreateException var5) {
         return ExceptionHelper.getRootMessage(var5);
      } catch (EJBException var6) {
         return ExceptionHelper.setErrorMessage(var6.getMessage());
      }
   }

   public Vector getRoomModerators(String username, String chatRoomName) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Vector var7;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select chatroommoderator.username from chatroom, chatroommoderator where chatroom.name=? and chatroom.status=1 and chatroom.id=chatroommoderator.chatroomid order by username");
         ps.setString(1, chatRoomName);
         rs = ps.executeQuery();
         Vector moderators = new Vector();

         while(rs.next()) {
            moderators.add(rs.getString("username"));
         }

         var7 = moderators;
         return var7;
      } catch (SQLException var25) {
         var7 = ExceptionHelper.getRootMessageAsVector(var25);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var22) {
            connSlave = null;
         }

      }

      return var7;
   }

   public Hashtable getRoomBannedUsers(String username, String chatRoomName, int page, int numEntries) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var9;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "select chatroombanneduser.username from chatroom, chatroombanneduser where chatroom.name=? and chatroom.creator=? and chatroom.status=1 and chatroom.id=chatroombanneduser.chatroomid union select chatroombanneduser.username from chatroom, chatroombanneduser, chatroommoderator where chatroom.name=? and chatroommoderator.username=? and chatroom.id=chatroommoderator.chatroomid and chatroom.status=1 and chatroom.id=chatroombanneduser.chatroomid order by username";
         ps = connSlave.prepareStatement(sql);
         ps.setString(1, chatRoomName);
         ps.setString(2, username);
         ps.setString(3, chatRoomName);
         ps.setString(4, username);
         Vector bannedUsers = new Vector();
         rs = ps.executeQuery();
         int i;
         if (rs.next()) {
            rs.absolute((page - 1) * numEntries + 1);

            for(i = 0; i < numEntries && !rs.isAfterLast(); ++i) {
               bannedUsers.add(rs.getString(1));
               rs.next();
            }
         }

         i = rs.last() ? rs.getRow() : 0;
         Hashtable hash = new Hashtable();
         hash.put("totalresults", i);
         hash.put("totalpages", Math.ceil((double)i / (double)numEntries));
         hash.put("page", page);
         hash.put("banned_users", bannedUsers);
         Hashtable var12 = hash;
         return var12;
      } catch (SQLException var30) {
         var9 = ExceptionHelper.getRootMessageAsHashtable(var30);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var27) {
            connSlave = null;
         }

      }

      return var9;
   }

   public String updateRoomKeywords(String username, String chatRoomName, String keywords, int allowUserKeywords) throws EJBException {
      try {
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         messageBean.updateRoomKeywords(username, chatRoomName, keywords, allowUserKeywords);
         return "TRUE";
      } catch (CreateException var6) {
         return ExceptionHelper.getRootMessage(var6);
      } catch (EJBException var7) {
         return ExceptionHelper.setErrorMessage(var7.getMessage());
      }
   }

   public Hashtable getChatroom(String chatRoomName) {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var5;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select c.*, (select group_concat(keyword) from chatroomkeyword ck, keyword k where ck.keywordid = k.id and ck.chatroomid = c.id) as keywords from chatroom c where c.name = ? and c.status = ?");
         ps.setString(1, chatRoomName);
         ps.setInt(2, ChatRoomData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();
         if (rs.next()) {
            var5 = HashObjectUtils.dataObjectToHashtable(new ChatRoomData(rs));
            return var5;
         }

         var5 = ExceptionHelper.setErrorMessageAsHashtable("Chat room not found");
      } catch (SQLException var27) {
         Hashtable var6 = ExceptionHelper.getRootMessageAsHashtable(var27);
         return var6;
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var24) {
            connSlave = null;
         }

      }

      return var5;
   }

   public Hashtable getUserOwnedChatrooms(String username, int page, int numEntries) {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var8;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select c.*, (select group_concat(keyword) from chatroomkeyword ck, keyword k where ck.keywordid = k.id and ck.chatroomid = c.id) as keywords from chatroom c where c.userowned = 1 and c.creator = ? and c.status = ?");
         ps.setString(1, username);
         ps.setInt(2, ChatRoomData.StatusEnum.ACTIVE.value());
         Vector chatrooms = new Vector();
         rs = ps.executeQuery();
         int i;
         if (rs.next()) {
            rs.absolute((page - 1) * numEntries + 1);

            for(i = 0; i < numEntries && !rs.isAfterLast(); ++i) {
               chatrooms.add(HashObjectUtils.dataObjectToHashtable(new ChatRoomData(rs)));
               rs.next();
            }
         }

         i = rs.last() ? rs.getRow() : 0;
         Hashtable hash = new Hashtable();
         hash.put("totalresults", i);
         hash.put("totalpages", Math.ceil((double)i / (double)numEntries));
         hash.put("page", page);
         hash.put("chatrooms", chatrooms);
         Hashtable var10 = hash;
         return var10;
      } catch (SQLException var28) {
         var8 = ExceptionHelper.getRootMessageAsHashtable(var28);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var25) {
            connSlave = null;
         }

      }

      return var8;
   }

   public boolean isModeratorOfChatRoom(String userName, String chatRoomName) throws EJBException {
      try {
         MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         return messageEJB.isModeratorOfChatRoom(userName, chatRoomName);
      } catch (Exception var4) {
         throw new EJBException(ExceptionHelper.getRootMessage(var4));
      }
   }

   public Hashtable searchChatroom(int countryId, String search, String language, boolean includeAdultOnly, boolean searchKeywords, int page, int numberOfEntries) {
      Hashtable hash = new Hashtable();

      try {
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         List<ChatRoomData> chatrooms = messageBean.getChatRooms(countryId, search, language, includeAdultOnly, searchKeywords);
         int total_results = chatrooms.size();
         double total_pages = Math.ceil((double)total_results / (double)numberOfEntries);
         if ((double)page > total_pages) {
            page = (int)total_pages;
         }

         if (page < 1) {
            page = 1;
         }

         int start = (page - 1) * numberOfEntries;
         int end = start + numberOfEntries;
         if (end > total_results) {
            end = total_results;
         }

         Vector v = new Vector();

         for(int i = start; i < end; ++i) {
            ChatRoomData chatroom = (ChatRoomData)chatrooms.get(i);
            ChatRoomPrx chatRoomPrx = EJBIcePrxFinder.findChatRoomPrx(chatroom.name);
            if (chatRoomPrx != null) {
               chatroom.size = chatRoomPrx.getNumParticipants();
            }

            v.add(HashObjectUtils.dataObjectToHashtable(chatroom));
         }

         hash.put("totalresults", total_results);
         hash.put("totalpages", total_pages);
         hash.put("page", page);
         hash.put("chatrooms", v);
         return hash;
      } catch (Exception var20) {
         throw new EJBException(var20.getMessage());
      }
   }

   public Hashtable getPendingContact(String username, int index) {
      try {
         Hashtable hash = new Hashtable();
         if (index < 1) {
            return hash;
         } else {
            ContactLocal contactEJB = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
            Vector<String> singleContact = new Vector();
            LinkedList<String> pendingContacts = new LinkedList(contactEJB.getPendingContacts(username));
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.PUSH_NEW_FOLLOWERS_AS_PENDING_CONTACTS_ENABLED)) {
               UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               int userID = userEJB.getUserID(username, (Connection)null);
               pendingContacts.addAll(contactEJB.getRecentFollowers(userID));
            }

            int numberOfPendingContacts = pendingContacts.size();
            if (index - 1 < pendingContacts.size()) {
               singleContact.add(pendingContacts.get(index - 1));
            }

            hash.put("totalresults", numberOfPendingContacts);
            hash.put("totalpages", numberOfPendingContacts);
            hash.put("page", index);
            hash.put("pending_contacts", singleContact);
            if (log.isDebugEnabled()) {
               log.debug("getPendingContact - page[" + index + "] total[" + numberOfPendingContacts + "] pending_contacts[" + singleContact + "]");
            }

            return hash;
         }
      } catch (Exception var9) {
         log.error("Unexpected exception caught : " + var9.getMessage(), var9);
         return ExceptionHelper.getRootMessageAsHashtable(var9);
      }
   }

   public int getPendingContactCount(String username) {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      byte var5;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select count(username) as pendingcount from pendingcontact where username=?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (rs.next()) {
            int var25 = rs.getInt("pendingcount");
            return var25;
         }

         var5 = -1;
      } catch (Exception var23) {
         throw new EJBException(var23);
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

      return var5;
   }

   public String acceptPendingContact(int userID, String username, String contactname, int groupid, boolean shareMobilePhone) {
      try {
         ContactLocal contactBean = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
         ContactData contactData = new ContactData();
         contactData.username = username;
         contactData.fusionUsername = contactname;
         if (groupid > 0) {
            contactData.contactGroupId = groupid;
         } else {
            contactData.contactGroupId = null;
         }

         contactData.displayOnPhone = true;
         contactData.shareMobilePhone = shareMobilePhone;
         contactBean.acceptContactRequest(userID, contactData, false);
         return "TRUE";
      } catch (Exception var8) {
         return ExceptionHelper.getRootMessage(var8);
      }
   }

   public Vector getContactGroupList(String username) {
      try {
         Vector v = new Vector();
         ContactLocal contactBean = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
         List<ContactGroupData> groups = contactBean.getGroupList(username);

         for(int i = 0; i < groups.size(); ++i) {
            v.add(HashObjectUtils.dataObjectToHashtable(groups.get(i)));
         }

         return v;
      } catch (Exception var6) {
         return ExceptionHelper.getRootMessageAsVector(var6);
      }
   }

   public String rejectContactInvitation(int userID, String username, String contactname) {
      try {
         ContactLocal contactBean = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
         contactBean.rejectContactRequest(userID, username, contactname);
         return "TRUE";
      } catch (Exception var5) {
         return var5.getMessage();
      }
   }

   public int getAnonymousCallSetting(String username) throws EJBException {
      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         return userEJB.getAnonymousCallSetting(username).value();
      } catch (CreateException var3) {
         throw new EJBException(var3.getMessage());
      }
   }

   public boolean updateAnonymousCallSetting(String username, int value) throws EJBException {
      try {
         UserSettingData.AnonymousCallEnum settingData = UserSettingData.AnonymousCallEnum.fromValue(value);
         if (settingData == null) {
            throw new EJBException("Invalid setting value " + value);
         } else {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            userEJB.updateAnonymousCallSetting(username, settingData);
            return true;
         }
      } catch (CreateException var5) {
         throw new EJBException(var5.getMessage());
      }
   }

   public int getMessageSetting(String username) throws EJBException {
      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         return userEJB.getMessageSetting(username).value();
      } catch (CreateException var3) {
         throw new EJBException(var3.getMessage());
      }
   }

   public boolean updateMessageSetting(String username, int value) throws EJBException {
      try {
         UserSettingData.MessageEnum settingData = UserSettingData.MessageEnum.fromValue(value);
         if (settingData == null) {
            throw new EJBException("Invalid setting value " + value);
         } else {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            userEJB.updateMessageSetting(username, settingData);
            return true;
         }
      } catch (CreateException var5) {
         throw new EJBException(var5.getMessage());
      }
   }

   public String addIMDetail(String username, int imType, String imUsername, String imPassword) throws EJBException {
      try {
         ImType type = ImType.fromValue(imType);
         if (type == null) {
            return ExceptionHelper.setErrorMessage("Invalid IM type " + imType);
         } else {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            userEJB.updateOtherIMDetail(username, type, imUsername, imPassword);
            if (!StringUtil.isBlank(imUsername)) {
               UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);
               if (userPrx != null) {
                  userPrx.otherIMLogout(imType);
                  userPrx.otherIMLogin(imType, PresenceType.AVAILABLE.value(), false);
               }
            }

            return "TRUE";
         }
      } catch (FusionException var8) {
         return ExceptionHelper.setErrorMessage(var8.message);
      } catch (Exception var9) {
         return ExceptionHelper.getRootMessage(var9);
      }
   }

   public Hashtable getIMContacts(String username, int page, int numEntries) throws EJBException {
      try {
         UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);
         if (userPrx == null) {
            throw new EJBException("You are not online");
         } else {
            ContactDataIce[] otherIMContacts = userPrx.getOtherIMContacts();
            Vector offlineContats = new Vector();
            Vector onlineContacts = new Vector();
            int start = (page - 1) * numEntries;
            int end = Math.min(page * numEntries, otherIMContacts.length);

            for(int i = start; i < end; ++i) {
               ContactData contact = new ContactData(otherIMContacts[i]);
               PresenceType presence = PresenceType.OFFLINE;
               Hashtable hash = new Hashtable();
               if (contact.isMSNOnly()) {
                  hash.put("type", ImType.MSN.value());
                  hash.put("username", contact.msnUsername);
                  presence = contact.msnPresence;
               } else if (contact.isYahooOnly()) {
                  hash.put("type", ImType.YAHOO.value());
                  hash.put("username", contact.yahooUsername);
                  presence = contact.yahooPresence;
               } else if (contact.isAIMOnly()) {
                  hash.put("type", ImType.AIM.value());
                  hash.put("username", contact.aimUsername);
                  presence = contact.aimPresence;
               } else if (contact.isGTalkOnly()) {
                  hash.put("type", ImType.GTALK.value());
                  hash.put("username", contact.gtalkUsername);
                  presence = contact.gtalkPresence;
               } else if (contact.isFacebookOnly()) {
                  hash.put("type", ImType.FACEBOOK.value());
                  hash.put("username", contact.facebookUsername);
                  presence = contact.facebookPresence;
               }

               hash.put("displayName", contact.displayName);
               hash.put("presence", presence.value());
               if (presence == PresenceType.OFFLINE) {
                  offlineContats.add(hash);
               } else {
                  onlineContacts.add(hash);
               }
            }

            onlineContacts.addAll(offlineContats);
            Hashtable hash = new Hashtable();
            hash.put("totalresults", otherIMContacts.length);
            hash.put("totalpages", Math.ceil((double)otherIMContacts.length / (double)numEntries));
            hash.put("page", page);
            hash.put("im_contacts", onlineContacts);
            return hash;
         }
      } catch (Exception var14) {
         return ExceptionHelper.getRootMessageAsHashtable(var14);
      }
   }

   public String inviteIMContact(String username, int imType, String imContact) throws EJBException {
      try {
         ImType type = ImType.fromValue(imType);
         if (type == null) {
            throw new EJBException("Invalid IM type " + imType);
         } else {
            UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);
            if (userPrx == null) {
               return ExceptionHelper.setErrorMessage("You are not online");
            } else {
               userPrx.otherIMSendMessage(imType, imContact, SystemProperty.get("OtherIMInvitationMessage").replaceAll("%im", type.toString()));
               return "TRUE";
            }
         }
      } catch (FusionException var6) {
         return ExceptionHelper.setErrorMessage(var6.message);
      } catch (Exception var7) {
         return ExceptionHelper.getRootMessage(var7);
      }
   }

   public boolean isIndosatIP(String ipAddress) throws EJBException {
      String[] arr$ = SystemProperty.get("IndosatIPs", "").split(";");
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String indosatIP = arr$[i$];
         if (ipAddress.startsWith(indosatIP)) {
            return true;
         }
      }

      return false;
   }

   public Vector getActiveSubscriptions(String username) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector subscriptions = new Vector();

      Vector var7;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select subscription.*, service.name servicename from service, subscription where service.id=subscription.serviceid and username=? and subscription.status=1");
         ps.setString(1, username);
         rs = ps.executeQuery();

         while(rs.next()) {
            subscriptions.add(HashObjectUtils.dataObjectToHashtable(new SubscriptionData(rs)));
         }

         return subscriptions;
      } catch (SQLException var25) {
         var7 = ExceptionHelper.getRootMessageAsVector(var25);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var22) {
            connSlave = null;
         }

      }

      return var7;
   }

   public String cancelSubscription(String username, int subscriptionID) {
      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         accountBean.cancelSubscription(username, subscriptionID);
         return "TRUE";
      } catch (Exception var4) {
         return var4.getMessage();
      }
   }

   public Hashtable getGroupHasExclusiveContent(int groupID) {
      Hashtable hash = new Hashtable();
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Hashtable var7;
      try {
         connSlave = this.dataSourceMaster.getConnection();
         String sql = "select 'Emoticons' as item, count(*) c from emoticonpack where groupid=? and status=1 union select 'Virtual Gifts' as item, count(*) c from virtualgift where groupid=? and status=1 union select 'Ringtones' as item, count(*) c from content where groupid=? and type=? and status=1 union select 'Wallpapers' as item, count(*) c from content where groupid=? and type=? and status=1 union select 'Games' as item, count(*) c from content where groupid=? and type=? and status=1";
         ps = connSlave.prepareStatement(sql);
         ps.setInt(1, groupID);
         ps.setInt(2, groupID);
         ps.setInt(3, groupID);
         ps.setInt(4, ContentData.TypeEnum.RINGTONE.value());
         ps.setInt(5, groupID);
         ps.setInt(6, ContentData.TypeEnum.WALLPAPER.value());
         ps.setInt(7, groupID);
         ps.setInt(8, ContentData.TypeEnum.APPLICATION.value());
         rs = ps.executeQuery();

         while(rs.next()) {
            hash.put(rs.getString("item"), String.valueOf(rs.getInt("c")));
         }

         return hash;
      } catch (SQLException var25) {
         var7 = ExceptionHelper.getRootMessageAsHashtable(var25);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var22) {
            connSlave = null;
         }

      }

      return var7;
   }

   public Vector getLanguages() throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector languages = new Vector();

      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select code, name from language where status=1");
         rs = ps.executeQuery();

         while(rs.next()) {
            Hashtable languageHash = new Hashtable();
            languageHash.put("code", rs.getString("code"));
            languageHash.put("name", rs.getString("name"));
            languages.add(languageHash);
         }
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var17) {
            connSlave = null;
         }

      }

      return languages;
   }

   public Hashtable getUserLevel(String username) {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         ReputationLevelData userLevel = userBean.getReputationLevel(username);
         return HashObjectUtils.dataObjectToHashtable(userLevel);
      } catch (Exception var4) {
         throw new EJBException(var4.getMessage());
      }
   }

   public Hashtable getBotList(int page, int numEntries) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector bots = new Vector();

      Hashtable h;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select * from bot where status = 1 order by game");
         rs = ps.executeQuery();
         int i;
         if (rs.next()) {
            rs.absolute((page - 1) * numEntries + 1);

            for(i = 0; i < numEntries && !rs.isAfterLast(); ++i) {
               BotData botData = new BotData(rs);
               h = new Hashtable();
               h.put("id", botData.getId());
               h.put("displayName", botData.getGame());
               h.put("description", botData.getDescription());
               bots.add(h);
               rs.next();
            }
         }

         i = rs.last() ? rs.getRow() : 0;
         Hashtable hash = new Hashtable();
         hash.put("totalresults", i);
         hash.put("totalpages", Math.ceil((double)i / (double)numEntries));
         hash.put("page", page);
         hash.put("bots", bots);
         h = hash;
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var21) {
            connSlave = null;
         }

      }

      return h;
   }

   public Hashtable chargeUserForGameItem(String username, String reference, String description, double amount, String currency, String ipAddress, String sessionID, String mobileDevice, String userAgent) throws EJBException {
      try {
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         AccountEntryData accountEntryData = accountEJB.chargeUserForGameItem(username, reference, description, amount, currency, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return HashObjectUtils.dataObjectToHashtable(accountEntryData);
      } catch (Exception var13) {
         String error = var13.getMessage().split(";")[0];
         return ExceptionHelper.setErrorMessageAsHashtable(error);
      }
   }

   public Hashtable giveGameReward(String username, String reference, String description, double amount, double fundedAmount, String currency, String ipAddress, String sessionID, String mobileDevice, String userAgent) throws EJBException {
      try {
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         AccountEntryData accountEntryData = accountEJB.giveGameReward(username, reference, description, amount, fundedAmount, currency, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return HashObjectUtils.dataObjectToHashtable(accountEntryData);
      } catch (Exception var15) {
         String error = var15.getMessage().split(";")[0];
         return ExceptionHelper.setErrorMessageAsHashtable(error);
      }
   }

   public Hashtable thirdPartyAPIDebit(String username, String reference, String description, double amount, String currency, String ipAddress, String sessionID, String mobileDevice, String userAgent) throws EJBException {
      try {
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         AccountEntryData accountEntryData = accountEJB.thirdPartyAPIDebit(username, reference, description, amount, currency, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return HashObjectUtils.dataObjectToHashtable(accountEntryData);
      } catch (Exception var13) {
         String error = var13.getMessage().split(";")[0];
         return ExceptionHelper.setErrorMessageAsHashtable(error);
      }
   }

   public String activateAccount(String username, String verificationCode, String ipAddress, String sessionID, String mobileDevice, String userAgent) throws EJBException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.activateAccount(username, verificationCode, false, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return "TRUE";
      } catch (Exception var8) {
         return var8.getMessage();
      }
   }

   public boolean updateEmoticonPackStatus(String username, int emoticonPackId, int status) throws EJBException {
      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         contentBean.updateEmoticonPackStatus(username, emoticonPackId, status);
         return true;
      } catch (Exception var5) {
         throw new EJBException(var5.getMessage());
      }
   }

   public String approveCreditCardPayment(String staffUsername, int creditCardPaymentId, String ipAddress, String sessionID, String mobileDevice, String userAgent) throws EJBException {
      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         accountBean.approveCreditCardPayment(staffUsername, creditCardPaymentId, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return "TRUE";
      } catch (Exception var8) {
         throw new EJBException(var8.getMessage());
      }
   }

   public String rejectCreditCardPayment(String staffUsername, int creditCardPaymentId, String reason, String ipAddress, String sessionID, String mobileDevice, String userAgent) throws EJBException {
      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         accountBean.rejectCreditCardPayment(staffUsername, creditCardPaymentId, reason, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return "TRUE";
      } catch (Exception var9) {
         throw new EJBException(var9.getMessage());
      }
   }

   public String creditUserAndSendSMS(String username, double amountSent, double amountCredit, String cashReceiptID, String ipAddress, String sessionID, String mobileDevice, String userAgent) throws EJBException {
      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         accountBean.creditAndNotifyUser(username, amountSent, amountCredit, cashReceiptID, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent), true);
         return "TRUE";
      } catch (Exception var12) {
         throw new EJBException(var12.getMessage());
      }
   }

   public Vector getCreditCardTransactions(String startDate, String endDate, String sortBy, String sortOrder, String showAuth, String showPend, String showRej, String username, int displayLimit) throws Exception {
      try {
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         return accountEJB.getCreditCardTransactions(startDate, endDate, sortBy, sortOrder, showAuth, showPend, showRej, username, displayLimit);
      } catch (Exception var11) {
         throw new EJBException(var11.getMessage());
      }
   }

   /** @deprecated */
   public String disconnectUserIce(String username, String comment) throws EJBException {
      try {
         UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);
         if (userPrx != null) {
            userPrx.disconnect(comment);
         }

         return "TRUE";
      } catch (Exception var4) {
         throw new EJBException(var4.getMessage());
      }
   }

   public String updateUserDetailsIce(String username) throws EJBException {
      UserData userData = null;

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userData = userBean.loadUser(username, false, true);
         UserPrx userPrx = EJBIcePrxFinder.findUserPrx(userData.username);
         userPrx.userDetailChanged(userData.toIceObject());
         return "TRUE";
      } catch (Exception var5) {
         throw new EJBException(var5.getMessage());
      }
   }

   public String deregisterChatroomIce(String chatroom) throws EJBException {
      try {
         EJBIcePrxFinder.getRegistry().deregisterChatRoomObject(chatroom);
         return "TRUE";
      } catch (Exception var3) {
         throw new EJBException(var3.getMessage());
      }
   }

   public String resetMerchantPin(String username) throws EJBException {
      try {
         AuthenticationServicePrx prx = EJBIcePrxFinder.getAuthenticationServiceProxy();
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         int userId = prx.userIDForFusionUsername(username);
         misBean.sendMerchantResetPinNotification(userId);
         misBean.removeMerchantPinAuthentication(userId);
         AuthenticationServiceCredentialResponse credential;
         if (prx.exists(userId, (byte)15) == AuthenticationServiceResponseCodeEnum.Success) {
            credential = prx.getCredential(userId, (byte)15);
            prx.removeCredential(credential.userCredential);
         }

         if (prx.exists(userId, (byte)16) == AuthenticationServiceResponseCodeEnum.Success) {
            credential = prx.getCredential(userId, (byte)16);
            prx.removeCredential(credential.userCredential);
         }

         return "true";
      } catch (Exception var6) {
         throw new EJBException(var6.getMessage());
      }
   }

   public boolean resendVerificationCode(String username, String mobilePhone, String ipAddress, String sessionID, String mobileDevice, String userAgent) throws EJBException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.resendVerificationCode(username, mobilePhone, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return true;
      } catch (Exception var8) {
         throw new EJBException(var8.getMessage());
      }
   }

   public String blacklistUsersFromGroup(int groupId, String blacklistedByUser, String[] blacklistedUsernames) throws EJBException {
      try {
         if (blacklistedUsernames != null && blacklistedUsernames.length != 0) {
            if (StringUtil.isBlank(blacklistedByUser)) {
               throw new IllegalArgumentException("Please provide the blacklisting user");
            } else {
               UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               if (blacklistedUsernames.length == 1) {
                  userBean.getUserID(blacklistedUsernames[0], (Connection)null);
               }

               GroupData groupData = userBean.getGroup(groupId);
               if (groupData == null) {
                  throw new EJBException("Group doesn't exist");
               } else if (!groupData.isOpenGroup()) {
                  throw new EJBException(groupData.name + " is not a public group. Users can be blacklisted only from public groups");
               } else {
                  MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
                  List<String> failed = new ArrayList();
                  String[] arr$ = blacklistedUsernames;
                  int len$ = blacklistedUsernames.length;

                  for(int i$ = 0; i$ < len$; ++i$) {
                     String username = arr$[i$];

                     try {
                        messageBean.banGroupMember(blacklistedByUser, groupData, username);
                     } catch (Exception var13) {
                        failed.add(username);
                     }
                  }

                  if (!failed.isEmpty()) {
                     throw new EJBException("Failed to blacklist the following users: " + failed);
                  } else {
                     return "TRUE";
                  }
               }
            }
         } else {
            throw new IllegalArgumentException("Please provide the usernames to blacklist");
         }
      } catch (Exception var14) {
         return ExceptionHelper.getRootMessage(var14);
      }
   }

   public String removeUsersFromGroupBlacklist(int groupId, String removingUser, String[] usersToBeRemoved) throws EJBException {
      try {
         if (usersToBeRemoved != null && usersToBeRemoved.length != 0) {
            if (StringUtil.isBlank(removingUser)) {
               throw new IllegalArgumentException("Please provide the session user");
            } else {
               UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               GroupData groupData = userBean.getGroup(groupId);
               if (groupData == null) {
                  throw new EJBException("Group doesn't exist");
               } else if (!groupData.isOpenGroup()) {
                  throw new EJBException(groupData.name + " is not a public group.");
               } else {
                  MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
                  List<String> failed = new ArrayList();
                  String[] arr$ = usersToBeRemoved;
                  int len$ = usersToBeRemoved.length;

                  for(int i$ = 0; i$ < len$; ++i$) {
                     String username = arr$[i$];

                     try {
                        messageBean.unbanGroupMember(removingUser, groupData, username);
                     } catch (Exception var13) {
                        failed.add(username);
                     }
                  }

                  if (!failed.isEmpty()) {
                     throw new EJBException("Failed to remove the following users from the blacklist: " + failed);
                  } else {
                     return "TRUE";
                  }
               }
            }
         } else {
            throw new IllegalArgumentException("Please provide the usernames to be removed from blacklist");
         }
      } catch (Exception var14) {
         return ExceptionHelper.getRootMessage(var14);
      }
   }

   public String sendApplicationEvent(String username, String applicationID, String jsonEncodedActivity) throws EJBException {
      try {
         JSONObject activity = new JSONObject(jsonEncodedActivity);
         String eventTitle = activity.getString("title");
         JSONArray jsonEncodedDeviceURLs = activity.optJSONArray("deviceCustomUrls");
         Map<String, String> eventDeviceURLs = new HashMap();
         String type;
         if (jsonEncodedDeviceURLs != null) {
            for(int i = 0; i < jsonEncodedDeviceURLs.length(); ++i) {
               JSONObject jsonEncodedDeviceURL = jsonEncodedDeviceURLs.getJSONObject(i);
               type = jsonEncodedDeviceURL.getString("type");
               String url = jsonEncodedDeviceURL.getString("url");
               eventDeviceURLs.put(type, url);
            }
         }

         String templateParams = activity.optString("templateParams");
         if (templateParams != null && templateParams.length() != 0) {
            String mediaItems = activity.getString("mediaItems");
            type = activity.getString("applicationInfo");
         } else if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.EVENT_SYSTEM_ENABLED)) {
            EventSystemPrx eventSystem = EJBIcePrxFinder.getEventSystemProxy();
            eventSystem.genericApplicationEvent(username, applicationID, eventTitle, eventDeviceURLs);
         }

         return "TRUE";
      } catch (Exception var12) {
         log.error("Failed to sendApplicationEvent for username [" + username + "] applicationID [" + applicationID + "] Activity [" + jsonEncodedActivity + "]", var12);
         return ExceptionHelper.getRootMessage(var12);
      }
   }

   public Hashtable getPaintWarsStats(String username) throws EJBException {
      PainterStats stats = null;

      try {
         stats = Painter.getStats(username);
      } catch (FusionException var4) {
         log.error("Unable to retrieve user stats: " + var4.message);
         throw new EJBException("Unable to retrieve stats. Please try again later.");
      }

      Hashtable<String, Integer> statsData = new Hashtable();
      statsData.put("TotalPaintWarsPoints", stats.getTotalPaintWarsPoints());
      statsData.put("TotalPaintsSent", stats.getTotalPaintsSent());
      statsData.put("TotalPaintsReceived", stats.getTotalPaintsReceived());
      statsData.put("TotalCleansSent", stats.getTotalCleansSent());
      statsData.put("TotalCleansReceived", stats.getTotalCleansReceived());
      statsData.put("PaintsRemaining", stats.getPaintsRemaining());
      statsData.put("CleansRemaining", stats.getCleansRemaining());
      return statsData;
   }

   public String getPaintWarsUserIdenticonIndex(String username) throws EJBException {
      try {
         return Painter.getUserIdenticonIndex(username);
      } catch (FusionException var3) {
         log.error("Unable to retrieve Paint Wars icon: " + var3.message);
         throw new EJBException("Unable to retrieve Paint Wars icon. Please try again later.");
      }
   }

   public String executePaintWarsPaint(String username, String targetUsername) throws EJBException {
      try {
         String message = "";
         if (!username.equals(targetUsername)) {
            if (Painter.isClean(targetUsername)) {
               if (!Painter.isPaintProof(targetUsername)) {
                  if (!Painter.hadInteraction(username, targetUsername)) {
                     if (!Painter.hasFreePaintCredits(username)) {
                        try {
                           Painter.buyPaintCredit(username);
                        } catch (FusionException var5) {
                           throw new EJBException(var5.message);
                        }
                     }

                     int points = Painter.paint(username, targetUsername);
                     return MessageFormat.format("{0} has painted {1}. {0} received {2} points.", username, targetUsername, points);
                  } else {
                     throw new EJBException("You have already interacted with " + targetUsername + " today. Please try again in 24 hours.");
                  }
               } else {
                  throw new EJBException(targetUsername + " is currently paint proof");
               }
            } else {
               throw new EJBException(targetUsername + " has already been painted");
            }
         } else {
            throw new EJBException("You cannot paint yourself");
         }
      } catch (FusionException var6) {
         log.error(username + " was unable to paint " + targetUsername + ": " + var6.message);
         throw new EJBException("Unable to paint " + targetUsername + ". Please try again later.");
      }
   }

   public String executePaintWarsClean(String username, String targetUsername) throws EJBException {
      try {
         String message = "";
         if (!Painter.isClean(targetUsername)) {
            if (!Painter.hadInteraction(username, targetUsername)) {
               if (!Painter.hasFreeCleanCredits(username)) {
                  try {
                     Painter.buyCleanCredit(username);
                  } catch (FusionException var5) {
                     throw new EJBException(var5.message);
                  }
               }

               int points = Painter.clean(username, targetUsername);
               return MessageFormat.format("{0} has cleaned paint on {1}. {0} received {2} points.", username, targetUsername, points);
            } else {
               throw new EJBException("You have already interacted with " + targetUsername + " today. Please try again in 24 hours.");
            }
         } else {
            throw new EJBException(targetUsername + " is already clean");
         }
      } catch (FusionException var6) {
         log.error(username + " was unable to clean " + targetUsername + ": " + var6.message);
         throw new EJBException("Unable to clean " + targetUsername + ". Please try again later.");
      }
   }

   public String isPaintWarsClean(String username) throws EJBException {
      try {
         return Painter.isClean(username) ? "TRUE" : "FALSE";
      } catch (FusionException var3) {
         log.error("Unable to check if user [" + username + "] is clean: " + var3.message);
         throw new EJBException("Unable to retrieve user details. Please try again later.");
      }
   }

   public String hadPaintWarsInteraction(String username1, String username2) throws EJBException {
      try {
         return Painter.hadInteraction(username1, username2) ? "TRUE" : "FALSE";
      } catch (FusionException var4) {
         log.error("Unable to check if user [" + username1 + "] had an interaction with [" + username2 + "]: " + var4.message);
         throw new EJBException("Unable to retrieve user details. Please try again later.");
      }
   }

   public String hasPaintWarsFreePaintCredits(String username) throws EJBException {
      try {
         return Painter.hasFreePaintCredits(username) ? "TRUE" : "FALSE";
      } catch (FusionException var3) {
         log.error("Unable to check if user [" + username + "] has free paint credits: " + var3.message);
         throw new EJBException("Unable to retrieve user details. Please try again later.");
      }
   }

   public String hasPaintWarsFreeCleanCredits(String username) throws EJBException {
      try {
         return Painter.hasFreeCleanCredits(username) ? "TRUE" : "FALSE";
      } catch (FusionException var3) {
         log.error("Unable to check if user [" + username + "] has free clean credits: " + var3.message);
         throw new EJBException("Unable to retrieve user details. Please try again later.");
      }
   }

   public String getPaintWarsPriceOfPaint() {
      return Painter.getPriceOfPaint();
   }

   public String getPaintWarsPriceOfClean() {
      return Painter.getPriceOfClean();
   }

   public String getPaintWarsPriceOfIdenticon() {
      return Painter.getPriceOfIdenticon();
   }

   public Vector getPaintWarsUserPaint(String username) throws EJBException {
      try {
         return Painter.getUserPaint(username);
      } catch (FusionException var3) {
         log.error("Unable to retrieve Paint Wars icon: " + var3.message);
         throw new EJBException("Unable to retrieve Paint Wars icon. Please try again later.");
      }
   }

   public String buyPaintWarsIdenticon(String username) throws EJBException {
      try {
         Painter.buyIdenticon(username);
         return "TRUE";
      } catch (FusionException var3) {
         throw new EJBException(var3.message);
      }
   }

   public Vector getPaintWarsStatsDetails(String username, int type, int offset, int numberOfEntries) throws EJBException {
      try {
         return Painter.getStatsDetails(username, type, offset, numberOfEntries);
      } catch (FusionException var6) {
         log.error("Unable to retrieve user [" + username + "] stats details: " + var6.message);
         throw new EJBException("Unable to retrieve user details. Please try again later.");
      }
   }

   public Vector getPaintWarsSpecialItems() throws EJBException {
      try {
         Vector<ItemData> specialItems = Painter.getSpecialItems();
         Iterator<ItemData> itr = specialItems.iterator();
         Vector itemVector = new Vector();

         while(itr.hasNext()) {
            ItemData itemData = (ItemData)itr.next();
            Hashtable<String, String> item = new Hashtable();
            item.put("ID", Integer.toString(itemData.getId()));
            item.put("Name", itemData.getName());
            item.put("Description", itemData.getDescription());
            item.put("Currency", itemData.getCurrency());
            item.put("Price", Double.toString(itemData.getPrice()));
            itemVector.add(item);
         }

         return itemVector;
      } catch (FusionException var6) {
         log.error("Unable to retrieve item details: " + var6.message);
         throw new EJBException("Unable to retrieve item details. Please try again later.");
      }
   }

   public Vector getPaintWarsUserInventory(String username) throws EJBException {
      try {
         return Painter.getUserInventory(username);
      } catch (FusionException var3) {
         log.error("Unable to retrieve user [" + username + "] inventory: " + var3.message);
         throw new EJBException("Unable to retrieve inventory details. Please try again later.");
      }
   }

   public String buyPaintWarsSpecialItem(String username, int itemId) throws EJBException {
      try {
         Painter.buySpecialItem(username, itemId);
         return "TRUE";
      } catch (FusionException var4) {
         throw new EJBException(var4.message);
      }
   }

   public String usePaintWarsSpecialItem(String username, int itemId) throws EJBException {
      try {
         Painter.useSpecialItem(username, itemId);
         return "TRUE";
      } catch (FusionException var4) {
         throw new EJBException(var4.message);
      }
   }

   public String isPaintWarsPaintProof(String username) throws EJBException {
      try {
         return Painter.isPaintProof(username) ? "TRUE" : "FALSE";
      } catch (FusionException var3) {
         log.error("Unable to check if user [" + username + "] is paint proof: " + var3.message);
         throw new EJBException("Unable to retrieve user details. Please try again later.");
      }
   }

   public String hasPaintWarsDualPaint(String username) throws EJBException {
      try {
         return Painter.hasDualPaint(username) ? "TRUE" : "FALSE";
      } catch (FusionException var3) {
         log.error("Unable to check if user [" + username + "] has dual paints: " + var3.message);
         throw new EJBException("Unable to retrieve user details. Please try again later.");
      }
   }

   public String hasPaintWarsStealthPaint(String username) throws EJBException {
      try {
         return Painter.hasStealthPaint(username) ? "TRUE" : "FALSE";
      } catch (FusionException var3) {
         log.error("Unable to check if user [" + username + "] has stealth paint: " + var3.message);
         throw new EJBException("Unable to retrieve user details. Please try again later.");
      }
   }

   public String giveUnfundedCredits(String username, String reference, String description, double amount, String currency, String ipAddress, String userAgent) throws EJBException {
      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         return accountBean.giveUnfundedCredits(username, reference, description, amount, currency, new AccountEntrySourceData(ipAddress, (String)null, (String)null, userAgent)) != null ? "TRUE" : "FALSE";
      } catch (CreateException var10) {
         throw new EJBException("Unable to give unfunded credits to user [" + username + "]: " + var10.getMessage());
      } catch (EJBException var11) {
         log.error("Unable to give unfunded credits to user [" + username + "]: " + var11.getMessage());
         throw new EJBException("Unable to give unfunded credits to user [" + username + "]: " + var11.getMessage());
      }
   }

   static {
      recentChatRoomMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.recentChatRooms);
   }
}
