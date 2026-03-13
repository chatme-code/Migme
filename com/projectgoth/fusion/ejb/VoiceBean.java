package com.projectgoth.fusion.ejb;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.CallData;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.VoiceGatewayData;
import com.projectgoth.fusion.data.VoiceRouteData;
import com.projectgoth.fusion.ejb.util.LookupUtil;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.interfaces.MessageLocal;
import com.projectgoth.fusion.interfaces.MessageLocalHome;
import com.projectgoth.fusion.slice.CallDataIce;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.UserPrx;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

public class VoiceBean implements SessionBean {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(VoiceBean.class));
   private DataSource dataSourceMaster;
   private DataSource dataSourceSlave;
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
         log.error("Unable to create Voice EJB", var2);
         throw new CreateException("Unable to create Voice EJB: " + var2.getMessage());
      }
   }

   public String getDIDNumber(int countryID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var6;
      try {
         String number = MemCachedClientWrapper.getString(MemCachedKeySpaces.CommonKeySpace.DID_NUMBER, String.valueOf(countryID));
         if (number == null) {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("select number from didnumber where countryid = ? and status = 1");
            ps.setInt(1, countryID);
            rs = ps.executeQuery();
            number = rs.next() ? rs.getString("number") : "0";
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.DID_NUMBER, String.valueOf(countryID), number);
         }

         var6 = number.equals("0") ? null : number;
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

   public String getFullDIDNumber(int countryID) throws EJBException {
      try {
         String didNumber = this.getDIDNumber(countryID);
         if (didNumber != null) {
            while(true) {
               if (didNumber.charAt(0) != '0') {
                  MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
                  CountryData countryData = misBean.getCountry(countryID);
                  if (!didNumber.startsWith(countryData.iddCode.toString())) {
                     didNumber = countryData.iddCode.toString() + didNumber;
                  }
                  break;
               }

               didNumber = didNumber.substring(1);
            }
         }

         return didNumber;
      } catch (CreateException var5) {
         throw new EJBException(var5.getMessage());
      }
   }

   private CountryData getVoiceRate(Connection conn, String phoneNumber) throws SQLException, CreateException {
      PreparedStatement ps = null;
      ResultSet rs = null;

      CountryData var9;
      try {
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         Integer iddCode = messageBean.getIDDCode(phoneNumber);
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         CountryData countryData = misBean.getCountryByIDDCode(iddCode, phoneNumber);
         if (countryData == null) {
            var9 = null;
            return var9;
         }

         ps = conn.prepareStatement("select * from country where id = ?");
         ps.setInt(1, countryData.id);
         rs = ps.executeQuery();
         if (rs.next()) {
            var9 = new CountryData(rs);
            return var9;
         }

         var9 = null;
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

      }

      return var9;
   }

   public CallData evaluatePhoneCall(CallData callData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      boolean sourceIsMobileNumber = false;
      boolean destinationIsMobileNumber = false;

      CallData var35;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select user.type, user.mobileverified, user.balance, user.status, country.name, country.allowphonecall, currency.exchangerate from user, country, currency where user.countryid = country.id and user.currency = currency.code and user.username = ?");
         ps.setString(1, callData.username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Invalid user " + callData.username);
         }

         if (rs.getInt("status") != UserData.StatusEnum.ACTIVE.value()) {
            throw new EJBException("Your account is suspended. Please contact migme");
         }

         if (!rs.getBoolean("mobileverified") && rs.getInt("type") != UserData.TypeEnum.MIG33_PREPAID_CARD.value()) {
            throw new EJBException("You need to authenticate your account first");
         }

         if (!rs.getBoolean("allowphonecall")) {
            throw new EJBException("Calls from " + rs.getString("name") + " are temporarily unavailable.  We apologize for the inconvenience");
         }

         double balance = rs.getDouble("balance") / rs.getDouble("exchangerate");
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         callData.rate = 0.0D;
         callData.signallingFee = 0.0D;
         callData.retries = 0;
         callData.initialLeg = CallData.InitialLegEnum.SOURCE;
         CountryData sourceCountryData = null;
         CountryData destinationCountryData = null;
         if (callData.isCallThrough()) {
            if (callData.source != null && !callData.source.equalsIgnoreCase("UNKNOWN")) {
               callData.source = messageBean.cleanAndValidatePhoneNumber(callData.source, false);
               callData.sourceIDDCode = messageBean.getIDDCode(callData.source);
            } else {
               callData.source = "UNKNOWN";
               callData.sourceIDDCode = null;
            }

            if (callData.didNumber == null) {
               throw new EJBException("DID number not specified");
            }

            sourceCountryData = this.getVoiceRate(conn, callData.didNumber);
            if (sourceCountryData == null) {
               throw new EJBException("Unable to determine country from DID number " + callData.didNumber);
            }

            if (sourceCountryData.callThroughSignallingFee == null || sourceCountryData.callThroughRate == null) {
               throw new EJBException("Unable to determine the call through rate for " + sourceCountryData.name);
            }

            callData.signallingFee = sourceCountryData.callThroughSignallingFee;
            callData.rate = sourceCountryData.callThroughRate;
         }

         if (callData.isCallback() && callData.sourceType == CallData.SourceDestinationTypeEnum.PSTN_PHONE) {
            callData.source = messageBean.cleanAndValidatePhoneNumber(callData.source, false);
            sourceCountryData = this.getVoiceRate(conn, callData.source);
            if (sourceCountryData == null) {
               throw new EJBException("Unable to determine country from source number " + callData.source);
            }

            callData.sourceIDDCode = sourceCountryData.iddCode;
            if (messageBean.isMobileNumber(callData.source, false)) {
               callData.signallingFee = sourceCountryData.mobileSignallingFee;
               callData.rate = sourceCountryData.mobileRate;
               sourceIsMobileNumber = true;
            } else {
               callData.signallingFee = sourceCountryData.callSignallingFee;
               callData.rate = sourceCountryData.callRate;
            }

            if (callData.signallingFee == null || callData.rate == null) {
               throw new EJBException("Unable to determine the call rate to " + callData.source);
            }

            callData.retries = sourceCountryData.callRetries;
         }

         if (callData.destinationType == CallData.SourceDestinationTypeEnum.PSTN_PHONE) {
            if (callData.destination != null && !callData.destination.equals("UNKNOWN")) {
               callData.destination = messageBean.cleanAndValidatePhoneNumber(callData.destination, false);
               destinationCountryData = this.getVoiceRate(conn, callData.destination);
               if (destinationCountryData == null) {
                  throw new EJBException("Unable to determine country from destination number " + callData.destination);
               }

               callData.destinationIDDCode = destinationCountryData.iddCode;
               Double secondLegRate;
               if (messageBean.isMobileNumber(callData.destination, false)) {
                  secondLegRate = destinationCountryData.mobileRate;
                  destinationIsMobileNumber = true;
               } else {
                  secondLegRate = destinationCountryData.callRate;
               }

               if (secondLegRate == null) {
                  throw new EJBException("Unable to determine the call rate to " + callData.destination);
               }

               callData.rate = callData.rate + secondLegRate;
            } else {
               if (callData.type != CallData.TypeEnum.MISSED_CALL_CALLBACK) {
                  throw new EJBException("Destination not specified");
               }

               callData.destination = "UNKNOWN";
               callData.destinationIDDCode = null;
            }
         }

         if (SystemProperty.getBool("VoiceRouteWhitelistEnabled", false)) {
            rs.close();
            ps.close();
            ps = conn.prepareStatement("select * from voiceroutewhitelist where sourceidd = ? and destinationidd = ?");
            ps.setInt(1, sourceCountryData != null && sourceCountryData.iddCode != null ? sourceCountryData.iddCode : -1);
            ps.setInt(2, destinationCountryData != null && destinationCountryData.iddCode != null ? destinationCountryData.iddCode : -1);
            rs = ps.executeQuery();
            if (!rs.next()) {
               StringBuffer message = new StringBuffer();
               message.append("Voice call ");
               if (!StringUtil.isBlank(sourceCountryData.name)) {
                  message.append("from " + sourceCountryData.name + " ");
               }

               if (!StringUtil.isBlank(destinationCountryData.name)) {
                  message.append("to " + destinationCountryData.name + " ");
               }

               message.append("is not supported");
               throw new EJBException(message.toString());
            }
         }

         if (sourceCountryData != null && destinationCountryData != null) {
            rs.close();
            ps.close();
            String fixedRateSQL = null;
            if (callData.isCallback()) {
               if (!sourceIsMobileNumber && !destinationIsMobileNumber) {
                  fixedRateSQL = "select LandlineToLandline/ExchangeRate Rate, LandlineToLandlineSignallingFee/ExchangeRate SignallingFee";
               } else if (!sourceIsMobileNumber && destinationIsMobileNumber) {
                  fixedRateSQL = "select LandlineToMobile/ExchangeRate Rate, LandlineToMobileSignallingFee/ExchangeRate SignallingFee";
               } else if (sourceIsMobileNumber && !destinationIsMobileNumber) {
                  fixedRateSQL = "select MobileToLandline/ExchangeRate Rate, MobileToLandlineSignallingFee/ExchangeRate SignallingFee";
               } else if (sourceIsMobileNumber && destinationIsMobileNumber) {
                  fixedRateSQL = "select MobileToMobile/ExchangeRate Rate, MobileToMobileSignallingFee/ExchangeRate SignallingFee";
               }
            } else if (callData.isCallThrough()) {
               if (!destinationIsMobileNumber) {
                  fixedRateSQL = "select CallThroughToLandline/ExchangeRate Rate, CallThroughToLandlineSignallingFee/ExchangeRate SignallingFee";
               } else {
                  fixedRateSQL = "select CallThroughToMobile/ExchangeRate Rate, CallThroughToMobileSignallingFee/ExchangeRate SignallingFee";
               }
            }

            fixedRateSQL = fixedRateSQL + " from fixedcallrate, currency where fixedcallrate.sourcecountryid=? and fixedcallrate.destinationcountryid=? and fixedcallrate.currency=currency.code";
            ps = conn.prepareStatement(fixedRateSQL);
            ps.setInt(1, sourceCountryData.id);
            ps.setInt(2, destinationCountryData.id);
            rs = ps.executeQuery();
            if (rs.next()) {
               if (rs.getObject("Rate") != null) {
                  callData.rate = rs.getDouble("Rate");
               }

               if (rs.getObject("SignallingFee") != null) {
                  callData.signallingFee = rs.getDouble("SignallingFee");
               }
            }
         }

         if (callData.isCallback() && callData.source.equals(callData.destination)) {
            throw new EJBException("Origin and destination numbers cannot be the same");
         }

         if (callData.rate == 0.0D) {
            callData.maxCallDuration = 32767;
            if (balance < callData.signallingFee) {
               throw new EJBException("You do not have enough credit. Please recharge your account");
            }
         } else {
            int billingBlock = SystemProperty.getInt((String)"CallBillingBlock", 1);
            callData.maxCallDuration = (int)Math.floor((balance - callData.signallingFee) / (callData.rate * (double)billingBlock / 60.0D)) * billingBlock;
            if (callData.maxCallDuration < 1) {
               throw new EJBException("You do not have enough credit. Please recharge your account");
            }
         }

         var35 = callData;
      } catch (CreateException var29) {
         throw new EJBException("Call failed: " + var29.getMessage());
      } catch (SQLException var30) {
         throw new EJBException("Call failed: " + var30.getMessage());
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

      return var35;
   }

   public CallData initiatePhoneCall(CallData callData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         callData = this.evaluatePhoneCall(callData);
         conn = this.dataSourceMaster.getConnection();
         callData.contactID = null;
         callData.dateCreated = new Date();
         ps = conn.prepareStatement("insert into phonecall (Username, ContactID, DateCreated, Source, SourceType, SourceIDDCode, Destination, DestinationType, DestinationIDDCode, MakeReceive, InitialLeg, SignallingFee, Rate, Type, Claimable, Gateway, Status) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 1);
         ps.setString(1, callData.username);
         ps.setObject(2, callData.contactID);
         ps.setTimestamp(3, new Timestamp(callData.dateCreated.getTime()));
         ps.setString(4, callData.source);
         ps.setObject(5, callData.sourceType == null ? null : callData.sourceType.value());
         ps.setObject(6, callData.sourceIDDCode);
         ps.setString(7, callData.destination);
         ps.setObject(8, callData.destinationType == null ? null : callData.destinationType.value());
         ps.setObject(9, callData.destinationIDDCode);
         ps.setObject(10, CallData.MakeReceiveEnum.MAKE.value());
         ps.setObject(11, callData.initialLeg == null ? null : callData.initialLeg.value());
         ps.setDouble(12, callData.signallingFee);
         ps.setDouble(13, callData.rate);
         ps.setObject(14, callData.type == null ? null : callData.type.value());
         ps.setObject(15, callData.claimable == null ? Boolean.FALSE : callData.claimable);
         ps.setObject(16, callData.gateway);
         ps.setInt(17, CallData.StatusEnum.PENDING.value());
         ps.executeUpdate();
         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            throw new EJBException("Unable to obtain the ID of the inserted call record");
         }

         callData.id = rs.getInt(1);
      } catch (SQLException var44) {
         throw new EJBException("Unable to initiate the callback: " + var44.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var40) {
            ps = null;
         }

         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var39) {
            rs = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var38) {
            conn = null;
         }

      }

      if (callData.isCallback() && callData.type != CallData.TypeEnum.MISSED_CALL_CALLBACK) {
         try {
            CallDataIce callDataIce = EJBIcePrxFinder.getCallMaker().requestCallback(callData.toIceObject(), callData.maxCallDuration, callData.retries);
            callData = new CallData(callDataIce);
         } catch (FusionException var41) {
            callData.status = CallData.StatusEnum.FAILED;
            callData.failReason = var41.message;
            throw new EJBException("Unable to request the callback: " + var41.message);
         } catch (Exception var42) {
            var42.printStackTrace();
            callData.status = CallData.StatusEnum.FAILED;
            callData.failReason = var42.getMessage();
            if (callData.failReason == null) {
               callData.failReason = "Exception: " + var42.getClass().getName();
            }

            throw new EJBException("Unable to request the callback: Internal server error");
         } finally {
            try {
               this.updateCallDetail(callData);
            } catch (Exception var37) {
               var37.printStackTrace();
            }

         }
      }

      return callData;
   }

   public CallData getCallEntryWithCost(int callEntryID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      CallData var7;
      try {
         String sql = "select phonecall.*,accountentry.amount, accountentry.currency from phonecall,accountentry where phonecall.username = accountentry.username and accountentry.reference = ? and phonecall.status = 2 and accountentry.type = " + AccountEntryData.TypeEnum.CALL_CHARGE.value() + " " + "and phonecall.id = ?";
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement(sql);
         ps.setString(1, String.valueOf(callEntryID));
         ps.setInt(2, callEntryID);
         rs = ps.executeQuery();
         CallData callData;
         if (!rs.next()) {
            callData = null;
            return callData;
         }

         callData = new CallData(rs);
         callData.amount = rs.getDouble("amount");
         callData.currency = rs.getString("currency");
         var7 = callData;
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

   public List<CallData> getCallEntries(String username) throws EJBException {
      return this.getCallEntries(username, (Integer)null);
   }

   public List<CallData> getCallEntriesWithCost(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         String sql = "select phonecall.*,accountentry.amount, accountentry.currency from phonecall,accountentry where phonecall.username = accountentry.username and phonecall.id = accountentry.reference and phonecall.status = 2 and phonecall.username = ? and accountentry.type = " + AccountEntryData.TypeEnum.CALL_CHARGE.value() + " " + "order by " + "phonecall.datecreated desc";
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement(sql);
         if (username != null) {
            ps.setString(1, username);
         }

         rs = ps.executeQuery();
         LinkedList callEntries = new LinkedList();

         while(rs.next()) {
            CallData callData = new CallData(rs);
            callData.amount = rs.getDouble("amount");
            callData.currency = rs.getString("currency");
            callEntries.add(callData);
         }

         LinkedList var24 = callEntries;
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

   public List<CallData> getCallEntries(String username, Integer status) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         String sql = "select * from phonecall";
         String criteria = "";
         if (username != null) {
            criteria = "username = ?";
         }

         if (status != null) {
            if (criteria.length() != 0) {
               criteria = criteria + " and ";
            }

            criteria = criteria + "status = " + status;
         }

         if (criteria.length() > 0) {
            sql = sql + " where " + criteria;
         }

         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement(sql);
         if (username != null) {
            ps.setString(1, username);
         }

         rs = ps.executeQuery();
         LinkedList callEntries = new LinkedList();

         while(rs.next()) {
            callEntries.add(new CallData(rs));
         }

         LinkedList var9 = callEntries;
         return var9;
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
   }

   public void updateCallDetail(CallData callData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update phonecall set Destination=?, DestinationType=?, DestinationIDDCode=?, SignallingFee=?, Rate=?, SourceDuration=?, DestinationDuration=?, BilledDuration=?, Gateway=?, SourceProvider=?, DestinationProvider=?, FailReasonCode=?, FailReason=?, Status=? where ID=? and Status not in (?,?)");
         ps.setObject(1, callData.destination);
         ps.setObject(2, callData.destinationType == null ? null : callData.destinationType.value());
         ps.setObject(3, callData.destinationIDDCode);
         ps.setObject(4, callData.signallingFee);
         ps.setObject(5, callData.rate);
         ps.setObject(6, callData.sourceDuration);
         ps.setObject(7, callData.destinationDuration);
         ps.setObject(8, callData.billedDuration);
         ps.setObject(9, callData.gateway);
         ps.setObject(10, callData.sourceProvider);
         ps.setObject(11, callData.destinationProvider);
         ps.setObject(12, callData.failReasonCode);
         ps.setObject(13, callData.failReason);
         ps.setInt(14, callData.status.value());
         ps.setInt(15, callData.id);
         ps.setInt(16, CallData.StatusEnum.COMPLETED.value());
         ps.setInt(17, CallData.StatusEnum.FAILED.value());
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
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var14) {
            conn = null;
         }

      }

   }

   private void notifyCallCompletion(CallData callData, AccountEntryData accountEntryData) throws CreateException, FusionException {
      UserPrx userPrx = EJBIcePrxFinder.findUserPrx(callData.username);
      if (userPrx != null) {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         String message = null;
         if (callData.billedDuration == 0L) {
            message = misBean.getInfoText(21);
         } else {
            message = misBean.getInfoText(28);
         }

         if (message != null) {
            String failReason = callData.failReason;
            if (failReason == null || failReason.length() == 0) {
               failReason = "Unknown reason";
            }

            String cost;
            if (accountEntryData == null) {
               cost = "0 " + userPrx.getUserData().currency;
            } else {
               cost = (new DecimalFormat("0.00")).format(-accountEntryData.amount) + " " + accountEntryData.currency;
            }

            message = message.replaceAll("%s", callData.source).replaceAll("%d", callData.destination).replaceAll("%r", failReason).replaceAll("%t", callData.billedDuration / 60L + " min " + callData.billedDuration % 60L + " sec").replaceAll("%c", cost);
            userPrx.putAlertMessage(message, (String)null, (short)0);
         }
      }

   }

   public void chargeCall(CallData callData, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         callData.status = CallData.StatusEnum.COMPLETED;
         if (callData.sourceDuration == null || callData.destinationDuration == null) {
            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("select duration, billsec from cdr where userfield = ? and (src = ? or src = ?)");
            ps.setString(1, callData.id.toString());
            ps.setString(2, callData.id.toString());
            ps.setString(3, callData.source);
            rs = ps.executeQuery();
            callData.sourceDuration = 0L;
            callData.destinationDuration = 0L;
            if (rs.next()) {
               if (callData.initialLeg == CallData.InitialLegEnum.SOURCE) {
                  callData.sourceDuration = (long)rs.getInt("duration");
                  callData.destinationDuration = (long)rs.getInt("billsec");
               } else {
                  callData.sourceDuration = (long)rs.getInt("billsec");
                  callData.destinationDuration = (long)rs.getInt("duration");
               }
            }

            rs.close();
            rs = null;
            ps.close();
            ps = null;
            conn.close();
            conn = null;
         }

         if (callData.sourceDuration > 0L && callData.destinationDuration > 0L) {
            callData.billedDuration = Math.max(callData.sourceDuration, callData.destinationDuration);
            callData.failReasonCode = null;
            callData.failReason = null;
         } else {
            callData.billedDuration = 0L;
         }

         this.updateCallDetail(callData);
         int billingBlock = SystemProperty.getInt((String)"CallBillingBlock", 1);
         double billingAmount = callData.rate * (double)billingBlock / 60.0D * Math.ceil((double)callData.billedDuration / (double)billingBlock);
         if (callData.sourceDuration > 0L || callData.destinationDuration > 0L) {
            billingAmount += callData.signallingFee;
         }

         AccountEntryData accountEntryData = null;
         double wholesaleCost = 0.0D;
         if (callData.sourceType == CallData.SourceDestinationTypeEnum.PSTN_PHONE && callData.sourceProvider != null) {
            wholesaleCost += this.getWholesaleCost(callData.sourceProvider, callData.sourceIDDCode, callData.source, callData.sourceDuration);
         }

         if (callData.destinationType == CallData.SourceDestinationTypeEnum.PSTN_PHONE && callData.destinationProvider != null) {
            wholesaleCost += this.getWholesaleCost(callData.destinationProvider, callData.destinationIDDCode, callData.destination, callData.destinationDuration);
         }

         if (callData.type == CallData.TypeEnum.MIDLET_ANONYMOUS_CALLBACK) {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("select username from user where mobilephone = ?");
            ps.setString(1, callData.destination);
            rs = ps.executeQuery();
            callData.destination = rs.next() ? rs.getString("username") : "unknown user";
            rs.close();
            rs = null;
            ps.close();
            ps = null;
            conn.close();
            conn = null;
         }

         if (billingAmount > 0.0D || wholesaleCost > 0.0D) {
            StringBuilder description = new StringBuilder();
            description.append(callData.isCallback() ? "Callback" : "Call-through").append(" from ").append(callData.source).append(" to ").append(callData.destination);
            description.append(" (").append(callData.billedDuration / 60L).append(" min ").append(callData.billedDuration % 60L).append(" sec)");
            AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
            accountEntryData = accountBean.chargeUserForCall(callData.username, callData.id.toString(), description.toString(), billingAmount, wholesaleCost, accountEntrySourceData);
         }

         try {
            this.notifyCallCompletion(callData, accountEntryData);
         } catch (Exception var30) {
            var30.printStackTrace();
         }
      } catch (CreateException var31) {
         throw new EJBException("Unable to charge user: " + var31.getMessage());
      } catch (SQLException var32) {
         throw new EJBException("Unable to charge user: " + var32.getMessage());
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

   private double getWholesaleCost(int providerID, int iddCode, String phoneNumber, long duration) throws SQLException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      double var11;
      try {
         double rate;
         if (duration == 0L) {
            rate = 0.0D;
            return rate;
         }

         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select voicewholesalerate.rate / currency.exchangerate rate from voicewholesalerate, currency where voicewholesalerate.currency = currency.code and voicewholesalerate.providerid = ? and voicewholesalerate.iddCode = ? and (voicewholesalerate.areacode = '' or ? like concat(voicewholesalerate.iddcode, voicewholesalerate.areacode, '%')) order by voicewholesalerate.areacode desc");
         ps.setInt(1, providerID);
         ps.setInt(2, iddCode);
         ps.setString(3, phoneNumber);
         rs = ps.executeQuery();
         rate = 0.0D;
         if (rs.next()) {
            rate = rs.getDouble("rate");
         }

         var11 = rate / 60.0D * (double)duration;
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

      return var11;
   }

   public List<VoiceGatewayData> getVoiceGateways() throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      LinkedList var25;
      try {
         String sql = "select voicegateway.*, voiceroute.*, voiceprovider.dialcommand from voicegateway left outer join voiceroute on voicegateway.id = voiceroute.gatewayid left outer join voiceprovider on voiceroute.providerid = voiceprovider.id where voicegateway.status = ? order by voicegateway.id, voiceroute.iddcode, voiceroute.areacode, voiceroute.priority";
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement(sql);
         ps.setInt(1, VoiceGatewayData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();
         List<VoiceGatewayData> gateways = new LinkedList();
         int previousGatewayID = Integer.MIN_VALUE;
         VoiceGatewayData gateway = null;

         while(rs.next()) {
            int gatewayID = rs.getInt("id");
            if (gatewayID != previousGatewayID) {
               gateway = new VoiceGatewayData(rs);
               gateway.voiceRoutes = new LinkedList();
               gateways.add(gateway);
               previousGatewayID = gatewayID;
            }

            if (rs.getInt("iddCode") != 0) {
               gateway.voiceRoutes.add(new VoiceRouteData(rs));
            }
         }

         var25 = gateways;
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

      return var25;
   }

   public List<VoiceGatewayData> getVoiceGateways(int iddCode) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         String sql = "select voicegateway.* from voicegateway inner join voiceroute on voicegateway.id = voiceroute.gatewayid where voicegateway.status = ? and voiceroute.iddcode = ? and voiceroute.areacode = '' order by voiceroute.priority, rand()";
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement(sql);
         ps.setInt(1, VoiceGatewayData.StatusEnum.ACTIVE.value());
         ps.setInt(2, iddCode);
         rs = ps.executeQuery();
         LinkedList gateways = new LinkedList();

         while(rs.next()) {
            gateways.add(new VoiceGatewayData(rs));
         }

         LinkedList var7 = gateways;
         return var7;
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
}
