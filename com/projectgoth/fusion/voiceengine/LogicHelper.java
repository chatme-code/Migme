package com.projectgoth.fusion.voiceengine;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.AccountBalanceData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.CallData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.VoucherData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.interfaces.Voice;
import com.projectgoth.fusion.interfaces.VoiceHome;
import com.projectgoth.fusion.interfaces.Voucher;
import com.projectgoth.fusion.interfaces.VoucherHome;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class LogicHelper {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(LogicHelper.class));
   protected FastAGIWorker worker = null;
   protected FastAGIServer server = null;
   protected FastAGICommand command = null;
   protected ExtendedControl control = null;
   protected CallMakerI callMaker = null;

   public LogicHelper(FastAGIWorker worker, FastAGIServer server, FastAGICommand command, ExtendedControl control, CallMakerI callMaker) {
      this.worker = worker;
      this.server = server;
      this.command = command;
      this.control = control;
      this.callMaker = callMaker;
   }

   public FastAGIWorker getWorker() {
      return this.worker;
   }

   public FastAGICommand getCommand() {
      return this.command;
   }

   public CallMakerI getCallMaker() {
      return this.callMaker;
   }

   public CallData getPendingCallThrough(String username, boolean logWarnings) throws CreateException, RemoteException, IOException {
      Voice voiceEJB = (Voice)EJBHomeCache.getObject("ejb/Voice", VoiceHome.class);
      List<CallData> pendingCalls = voiceEJB.getCallEntries(username, CallData.StatusEnum.PENDING.value());
      CallData pendingCallThrough = null;
      CallData expiredCall;
      if (pendingCalls != null) {
         Iterator i$ = pendingCalls.iterator();

         while(i$.hasNext()) {
            CallData callData = (CallData)i$.next();
            if (callData.isCallThrough()) {
               expiredCall = null;
               if (System.currentTimeMillis() - callData.dateCreated.getTime() > (long)this.server.getCallThroughValidPeriod()) {
                  expiredCall = callData;
               } else if (pendingCallThrough == null) {
                  pendingCallThrough = callData;
               } else if (callData.id > pendingCallThrough.id) {
                  expiredCall = pendingCallThrough;
                  pendingCallThrough = callData;
               } else {
                  expiredCall = callData;
               }

               if (expiredCall != null) {
               }
            }
         }
      }

      if (logWarnings && pendingCallThrough == null) {
         StringBuilder builder = (new StringBuilder("No pending call-through found for ")).append(username).append(". Calls examined: ");
         if (pendingCalls != null) {
            Iterator i$ = pendingCalls.iterator();

            while(i$.hasNext()) {
               expiredCall = (CallData)i$.next();
               builder.append(expiredCall.id).append(" ");
            }
         }

         log.debug(builder);
      }

      return pendingCallThrough;
   }

   public CallRequest getPendingMidletRequest(UserData userData, String didNumber) throws CreateException, RemoteException, IOException {
      log.info("Getting pending midlet request for user '" + userData.username + "'");
      CallData callData = this.getPendingCallThrough(userData.username, true);
      if (callData == null) {
         log.warn("No pending request found for " + userData.username);
         return null;
      } else {
         callData.didNumber = didNumber;
         return new CallRequest(callData, (String)null, (String)null, 0L, 0.0D, 0L, 0L, 0);
      }
   }

   public CallRequest createDirectCallThroughRequest(UserData userData, String callerId, String didNumber) {
      log.info("Creating direct call through request for user '" + userData.username + "'");
      CallData callData = new CallData();
      callData.username = userData.username;
      callData.type = CallData.TypeEnum.DIRECT_CALL_THROUGH;
      callData.didNumber = didNumber;
      callData.sourceType = CallData.SourceDestinationTypeEnum.PSTN_PHONE;
      if (callerId != null && callerId.length() >= 1) {
         callData.source = callerId;
      } else {
         callData.source = null;
      }

      return new CallRequest(callData, (String)null, (String)null, 0L, 0.0D, 0L, 0L, 0);
   }

   public CallRequest createMissedCallCallbackRequest(UserData userData, String callerId, Integer sourceProvider) {
      log.info("Creating missed call callback request for user '" + userData.username + "'");
      CallData callData = new CallData();
      callData.username = userData.username;
      callData.type = CallData.TypeEnum.MISSED_CALL_CALLBACK;
      callData.sourceType = CallData.SourceDestinationTypeEnum.PSTN_PHONE;
      callData.source = callerId;
      callData.sourceProvider = sourceProvider;
      callData.destinationType = CallData.SourceDestinationTypeEnum.PSTN_PHONE;
      return new CallRequest(callData, (String)null, (String)null, 0L, 0.0D, 0L, 0L, 0);
   }

   public CallRequest evaluateCallRequest(CallRequest request) throws CreateException, RemoteException, IOException {
      CallData callData = request.getCallData();
      log.info("Evaluating call request for user '" + callData.username + "'");
      Voice voiceEJB = (Voice)EJBHomeCache.getObject("ejb/Voice", VoiceHome.class);
      callData = voiceEJB.evaluatePhoneCall(callData);
      log.info("Filling in call request data from call data");
      request = this.callMaker.fillInCallRequest(callData, Integer.parseInt(this.command.getParameter("gw_id")));
      if (request != null) {
         log.info("Call data for '" + callData.destination + "', user '" + callData.username + "'': " + "callerid='" + callData.source + "', " + "dialcommand='" + request.getDialCommand() + ", limitdur='" + request.getLimitDuration() + "', " + "limitrate='" + request.getLimitRate() + "', warningdur='" + request.getLimitTimeoutWarning() + "', " + "repeatdur='" + request.getLimitTimeoutRepeat() + "', callid='" + request.getCallId() + "'");
      } else {
         log.warn("Unable to fill in call request");
      }

      return request;
   }

   public CallRequest initiateCallRequest(CallRequest request) throws CreateException, RemoteException, IOException {
      CallData callData = request.getCallData();
      boolean ok = false;

      CallRequest var9;
      try {
         String channel = this.command.getChannel();
         Voice voiceEJB;
         if (channel == null || channel.length() == 0) {
            log.warn("No channel data, so can't process missed call callback request");
            voiceEJB = null;
            return voiceEJB;
         }

         log.info("Initiating call request for user '" + callData.username + "'");
         voiceEJB = (Voice)EJBHomeCache.getObject("ejb/Voice", VoiceHome.class);
         if (callData.id == null) {
            log.info("Persisting call request for user '" + callData.username + "'");
            callData = voiceEJB.initiatePhoneCall(callData);
         }

         log.info("Setting the proper callerId to link call completion");
         this.control.setAsteriskCallerId(callData.source);
         this.control.setAsteriskCallerIdName(callData.source);
         String astCallerId = this.control.getAsteriskCallerId();
         String astCallerIdName = this.control.getAsteriskCallerIdName();
         String astCallerDid = this.control.getAsteriskCallerDid();
         log.info("Asterisk CallerDID is set to '" + astCallerDid + "'");
         log.info("Asterisk CallerID is set to '" + astCallerId + "'");
         log.info("Asterisk CallerIDName is set to '" + astCallerIdName + "'");
         this.control.setCDRUserField("" + callData.id);
         this.control.addSIPHeader("X-CallRef: " + callData.id);
         this.control.addSIPHeader("X-CallLeg: A");
         this.control.addSIPHeader("X-CallType: 2");
         this.control.resetCDR();

         try {
            callData.sourceChannel = channel;
            callData = this.callMaker.requestCall(callData, Integer.parseInt(this.command.getParameter("gw_id")));
         } catch (Exception var16) {
            log.warn("Error int callMaker.requestCall(), " + var16.getMessage());
            callData.status = CallData.StatusEnum.FAILED;
            callData.failReason = var16.getMessage();
         }

         voiceEJB.updateCallDetail(callData);
         if (callData.status != CallData.StatusEnum.FAILED) {
            log.info("Filling in call request data from call data");
            request = this.callMaker.fillInCallRequest(callData, Integer.parseInt(this.command.getParameter("gw_id")));
            if (request != null) {
               log.info("Call data for '" + callData.destination + "', user '" + callData.username + "'': " + "callerid='" + callData.source + "', " + "dialcommand='" + request.getDialCommand() + ", limitdur='" + request.getLimitDuration() + "', " + "limitrate='" + request.getLimitRate() + "', warningdur='" + request.getLimitTimeoutWarning() + "', " + "repeatdur='" + request.getLimitTimeoutRepeat() + "', callid='" + request.getCallId() + "'");
            } else {
               log.warn("Unable to fill in call request");
            }

            ok = true;
            var9 = request;
            return var9;
         }

         log.warn("Error initiating a call request");
         var9 = null;
      } catch (CreateException var17) {
         log.warn("Error initiating a call for user '" + callData.username + "', (ex1); " + var17.getMessage());
         throw var17;
      } catch (RemoteException var18) {
         log.warn("Error initiating a call for user '" + callData.username + "', (ex2); " + var18.getMessage());
         throw var18;
      } finally {
         if (!ok && callData != null && callData.status == CallData.StatusEnum.IN_PROGRESS) {
            log.warn("Error initiating a call for user '" + callData.username + "', so cancelling");
            this.callMaker.cancelCall(callData);
         }

      }

      return var9;
   }

   public CallRequest updateCallRequest(CallRequest request) throws CreateException, RemoteException, IOException {
      CallData callData = request.getCallData();
      log.info("Updating call request for user '" + callData.username + "'");
      Voice voiceEJB = (Voice)EJBHomeCache.getObject("ejb/Voice", VoiceHome.class);
      voiceEJB.updateCallDetail(callData);

      try {
         this.callMaker.cancelCall(callData);
         this.callMaker.requestCall(callData, Integer.parseInt(this.command.getParameter("gw_id")));
         return request;
      } catch (Exception var5) {
         log.warn("Unable to re-register call with CallMaker");
         return null;
      }
   }

   public void processBillingStart(UserData userData, String destination) {
      if (userData == null) {
         log.warn("No channel data, so can't process billing start");
      } else if (destination != null && destination.length() >= 1) {
         log.info("Processing billing start for '" + userData.username + "'");
      } else {
         log.warn("No destination, so can't process billing start request");
      }
   }

   public void processBillingEnd(UserData userData, String destination) throws CreateException, RemoteException, IOException {
      CallData callData = null;
      boolean ok = false;

      try {
         String channel = this.command.getChannel();
         if (channel == null || channel.length() == 0) {
            log.warn("No channel data, so can't process billing end request");
            return;
         }

         if (userData != null) {
            if (destination != null && destination.length() >= 1) {
               log.info("Processing billing end to '" + destination + "', user '" + userData.username + "'");
               callData = this.getPendingCallThrough(userData.username, false);
               if (callData != null && destination.equals(callData.destination)) {
                  this.callMaker.callCompleted(callData, false);
                  ok = true;
                  return;
               }

               log.info("No call data or destination doesn't match to '" + destination + "', user '" + userData.username + "'");
               return;
            }

            log.warn("No destination, so can't process billing end request");
            return;
         }

         log.warn("No user data, so can't process billing end request");
      } catch (CreateException var11) {
         log.warn("Ex1- Error with billing end to '" + destination + "', user '" + userData.username + "'; " + var11.getMessage());
         throw var11;
      } catch (RemoteException var12) {
         log.warn("Ex2- Error with billing end to '" + destination + "', user '" + userData.username + "'; " + var12.getMessage());
         return;
      } finally {
         if (!ok && callData != null && callData.status == CallData.StatusEnum.IN_PROGRESS) {
            log.warn("Error completing billing end to '" + destination + "', user '" + userData.username + "', so cancelling");
            this.callMaker.cancelCall(callData);
         }

      }

   }

   public void processBillingCancel(UserData userData, String destination) throws CreateException, RemoteException, IOException {
      CallData callData = null;

      try {
         String channel = this.command.getChannel();
         if (channel == null || channel.length() == 0) {
            log.warn("No channel data, so can't process billing cancel request");
         } else if (userData == null) {
            log.warn("No user data, so can't process billing cancel request");
         } else if (destination != null && destination.length() >= 1) {
            log.info("Processing billing cancel to '" + destination + "', user '" + userData.username + "'");
            callData = this.getPendingCallThrough(userData.username, false);
            if (callData != null && destination.equals(callData.destination)) {
               this.callMaker.cancelCall(callData);
               Voice voiceEJB = (Voice)EJBHomeCache.getObject("ejb/Voice", VoiceHome.class);
               callData.status = CallData.StatusEnum.FAILED;
               if (callData.failReason == null || callData.failReason.length() < 1) {
                  callData.failReason = "Call Cancelled";
               }

               voiceEJB.updateCallDetail(callData);
            } else {
               log.info("No call data or destination doesn't match to '" + destination + "', user '" + userData.username + "'");
            }
         } else {
            log.warn("No destination, so can't process billing cancel request");
         }
      } catch (CreateException var6) {
         log.warn("Ex1- Error with billing cancel to '" + destination + "', user '" + userData.username + "'; " + var6.getMessage());
         throw var6;
      } catch (RemoteException var7) {
         log.warn("Ex2- Error with billing cancel to '" + destination + "', user '" + userData.username + "'; " + var7.getMessage());
      }
   }

   public AccountBalanceData getBalance(UserData userData) throws CreateException, RemoteException, IOException {
      AccountBalanceData userBalance = new AccountBalanceData();
      userBalance.currency = new CurrencyData();
      userBalance.currency.code = "usd";
      userBalance.balance = 0.0D;
      userBalance.fundedBalance = 0.0D;

      try {
         if (userData == null) {
            log.warn("No user data, so can't process get balance request");
            return userBalance;
         } else {
            log.info("Retrieving balance for username '" + userData.username + "'");
            Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
            AccountBalanceData realUserBalance = accountEJB.getAccountBalance(userData.username);
            if (realUserBalance != null) {
               log.info("User '" + userData.username + "' has a balance of '" + realUserBalance.balance + "', funded '" + realUserBalance.fundedBalance + "', in currency '" + (realUserBalance.currency == null ? "" : realUserBalance.currency.code) + "'");
            } else {
               log.warn("Could not get user's balance");
            }

            return realUserBalance;
         }
      } catch (CreateException var5) {
         log.warn("Error with get balance with user '" + userData.username + "' (ex1); " + var5.getMessage());
         throw var5;
      } catch (RemoteException var6) {
         log.warn("Error with get balance with user '" + userData.username + "' (ex2); " + var6.getMessage());
         return userBalance;
      }
   }

   public long getLimitDuration(UserData userData, CallRequest callRequest) throws CreateException, RemoteException, IOException {
      try {
         if (userData == null) {
            log.warn("No user data, so can't process get limit duration request");
            return 0L;
         } else if (callRequest == null) {
            log.warn("No call request data, so can't process get limit duration request");
            return 0L;
         } else {
            log.info("Retrieving limit duration for username '" + userData.username + "'");
            log.info("Retrieving pending call-through for " + userData.username);
            CallData callData = this.getPendingCallThrough(userData.username, true);
            if (callData == null) {
               log.info("No pending call-through found for " + userData.username);
            } else {
               log.info("Pending call-through " + callData.id + " found for " + userData.username + ". Destination " + callData.destination);
            }

            return callRequest.limitDuration;
         }
      } catch (CreateException var4) {
         log.warn("Ex1- Error with get limit duration with user '" + userData.username + "'; " + var4.getMessage());
         throw var4;
      } catch (RemoteException var5) {
         log.warn("Ex2- Error with get limit duration with user '" + userData.username + "'; " + var5.getMessage());
         return 0L;
      }
   }

   public UserData loadUserFromUsername(String username) throws CreateException, RemoteException, IOException {
      try {
         if (username != null && username.length() >= 1) {
            log.info("Loading user from username '" + username + "'");
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            UserData userData = userEJB.loadUser(username, false, false);
            if (userData == null || !userData.username.equals(username)) {
               log.warn("Could not load the user from username '" + username + "'");
               userData = null;
            }

            return userData;
         } else {
            log.warn("No user name, so can't load user from suername");
            return null;
         }
      } catch (CreateException var4) {
         log.warn("Ex1- Could not load the user from username '" + username + "'; " + var4.getMessage());
         throw var4;
      } catch (RemoteException var5) {
         log.warn("Ex2- Could not load the user from username '" + username + "'; " + var5.getMessage());
         return null;
      }
   }

   public UserData loadUserFromMobilePhone(String mobilePhone) throws CreateException, RemoteException, IOException {
      try {
         if (mobilePhone != null && mobilePhone.length() >= 1) {
            log.info("Loading user from mobile phone '" + mobilePhone + "'");
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            UserData userData = userEJB.loadUserFromMobilePhone(mobilePhone);
            if (userData == null || !userData.mobilePhone.equals(mobilePhone)) {
               log.warn("Could not load the user from mobile phone '" + mobilePhone + "'");
               userData = null;
            }

            return userData;
         } else {
            log.warn("No mobile phone, so can't load user from mobile phone");
            return null;
         }
      } catch (CreateException var4) {
         log.warn("Ex1- Could not load the user from mobile phone '" + mobilePhone + "'; " + var4.getMessage());
         throw var4;
      } catch (RemoteException var5) {
         log.warn("Ex2- Could not load the user from mobile phone '" + mobilePhone + "'; " + var5.getMessage());
         return null;
      }
   }

   public UserData loadUserFromVoucherNumber(String voucherNumber) throws CreateException, RemoteException, IOException {
      try {
         if (voucherNumber != null && voucherNumber.length() >= 1) {
            log.info("Loading user from voucher number '" + voucherNumber + "'");
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            UserData userData = userEJB.loadUserFromVoucherNumber(voucherNumber);
            if (userData == null) {
               log.warn("Could not load the user from voucher number '" + voucherNumber + "' with normal method, trying another method");
               String username = UserData.TypeEnum.MIG33_PREPAID_CARD.toString().toLowerCase() + "_" + voucherNumber;
               userData = userEJB.loadUser(username, false, false);
               if (userData == null || !userData.username.equals(username)) {
                  log.warn("Could not load the user from voucher number '" + voucherNumber + "'");
                  userData = null;
               }
            }

            return userData;
         } else {
            log.warn("No voucher number, so can't load user from voucher number");
            return null;
         }
      } catch (CreateException var5) {
         log.warn("Ex1- Could not load the user from voucher number '" + voucherNumber + "'; " + var5.getMessage());
         throw var5;
      } catch (RemoteException var6) {
         log.warn("Ex2- Could not load the user from voucher number '" + voucherNumber + "'; " + var6.getMessage());
         return null;
      }
   }

   public UserData createUserFromVoucher(String voucherNumber, String fromDID) throws CreateException, RemoteException, IOException {
      try {
         if (voucherNumber != null && voucherNumber.length() >= 1) {
            log.info("Creating user from voucherNumber '" + voucherNumber + "'");
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            UserData userData = userEJB.createPrepaidCardUser(fromDID, voucherNumber, new AccountEntrySourceData(VoiceEngine.class));
            if (userData == null) {
               log.warn("Could not create user from voucher number '" + voucherNumber + "'");
            }

            return userData;
         } else {
            log.warn("No voucher number, so can't create user from voucher number");
            return null;
         }
      } catch (CreateException var5) {
         log.warn("Ex1- Could not create user from voucher number '" + voucherNumber + "'; " + var5.getMessage());
         throw var5;
      } catch (RemoteException var6) {
         log.warn("Ex2- Could not create user from voucher number '" + voucherNumber + "'; " + var6.getMessage());
         return null;
      }
   }

   public UserData updateUserMobilePhone(UserData userData, String mobilePhone) throws CreateException, RemoteException, IOException {
      try {
         if (userData == null) {
            log.warn("No user data, so can't update user's mobile phone");
            return userData;
         } else {
            log.info("Updating user '" + userData.username + "' mobile phone to '" + mobilePhone + "'");
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            if (mobilePhone != null && mobilePhone.length() < 1) {
               mobilePhone = null;
            }

            userEJB.changeMobilePhone(userData.username, mobilePhone, new AccountEntrySourceData(VoiceEngine.class));
            userData.mobilePhone = mobilePhone;
            if (mobilePhone == null) {
               if (userData == null || userData.mobilePhone != null) {
                  log.warn("Could not update user mobile phone to '" + mobilePhone + "'");
               }
            } else if (userData == null || userData.mobilePhone == null || !userData.mobilePhone.equals(mobilePhone)) {
               log.warn("Could not update user mobile phone to '" + mobilePhone + "'");
            }

            return userData;
         }
      } catch (CreateException var4) {
         log.warn("Ex1- Could not update user mobile phone to '" + mobilePhone + "'; " + var4.getMessage());
         throw var4;
      } catch (RemoteException var5) {
         log.warn("Ex2- Could not update user mobile phone to '" + mobilePhone + "'; " + var5.getMessage());
         return userData;
      }
   }

   public VoucherData getVoucher(String voucherNumber) throws CreateException, RemoteException, IOException {
      try {
         if (voucherNumber != null && voucherNumber.length() >= 1) {
            log.info("Get voucher from voucherNumber '" + voucherNumber + "'");
            Voucher voucherEJB = (Voucher)EJBHomeCache.getObject("ejb/Voucher", VoucherHome.class);
            VoucherData voucherData = voucherEJB.getVoucher(voucherNumber);
            if (voucherData == null) {
               log.warn("Could not get voucher number '" + voucherNumber + "'");
            }

            return voucherData;
         } else {
            log.warn("No voucher number, so can't get voucher from voucher number");
            return null;
         }
      } catch (CreateException var4) {
         log.warn("Ex1- Could not get voucher number '" + voucherNumber + "'; " + var4.getMessage());
         throw var4;
      } catch (RemoteException var5) {
         log.warn("Ex2- Could not get voucher number '" + voucherNumber + "'; " + var5.getMessage());
         return null;
      }
   }

   public VoucherData redeemVoucher(String username, String voucherNumber) throws CreateException, RemoteException, IOException {
      try {
         if (username != null && username.length() >= 1) {
            if (voucherNumber != null && voucherNumber.length() >= 1) {
               log.info("Get voucher for username '" + username + "' with voucherNumber '" + voucherNumber + "'");
               Voucher voucherEJB = (Voucher)EJBHomeCache.getObject("ejb/Voucher", VoucherHome.class);
               VoucherData voucherData = voucherEJB.redeemVoucher(username, voucherNumber, new AccountEntrySourceData(VoiceEngine.class));
               if (voucherData == null) {
                  log.warn("Could not redeem voucher number '" + voucherNumber + "'");
               }

               return voucherData;
            } else {
               log.warn("No voucher number, so can't get voucher with voucher number");
               return null;
            }
         } else {
            log.warn("No username, so can't get voucher with username");
            return null;
         }
      } catch (CreateException var5) {
         log.warn("Ex1- Could not redeem voucher number '" + voucherNumber + "'; " + var5.getMessage());
         throw var5;
      } catch (RemoteException var6) {
         log.warn("Ex2- Could not redeem voucher number '" + voucherNumber + "'; " + var6.getMessage());
         return null;
      }
   }

   public String stripNonNumeric(String phoneNumber) {
      String newPhoneNumber = "";
      if (phoneNumber != null && phoneNumber.length() >= 1) {
         for(int i = 0; i < phoneNumber.length(); ++i) {
            char ch = phoneNumber.charAt(i);
            if (ch >= '0' && ch <= '9') {
               newPhoneNumber = newPhoneNumber + ch;
            }
         }

         log.info("Stripped phone number: original='" + phoneNumber + "', cleaned='" + newPhoneNumber + "'");
         return newPhoneNumber;
      } else {
         log.warn("Phone number is null or invalid; can't strip non-numeric");
         return null;
      }
   }

   public String cleanPhoneNumber(String phoneNumber) throws CreateException, RemoteException, IOException {
      String newPhoneNumber = this.stripNonNumeric(phoneNumber);
      if (newPhoneNumber != null && newPhoneNumber.length() >= 1) {
         Message messageEJB;
         try {
            messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            newPhoneNumber = messageEJB.cleanAndValidatePhoneNumber(newPhoneNumber, true);
         } catch (Exception var5) {
         }

         if (newPhoneNumber.length() > 3 && newPhoneNumber.substring(0, 4).equals("0011")) {
            newPhoneNumber = newPhoneNumber.substring(4);
         } else if (newPhoneNumber.length() > 2 && newPhoneNumber.substring(0, 3).equals("000")) {
            newPhoneNumber = newPhoneNumber.substring(3);
         } else if (newPhoneNumber.length() > 2 && newPhoneNumber.substring(0, 3).equals("011")) {
            newPhoneNumber = newPhoneNumber.substring(3);
         } else if (newPhoneNumber.length() > 1 && newPhoneNumber.substring(0, 2).equals("00")) {
            newPhoneNumber = newPhoneNumber.substring(2);
         } else if (newPhoneNumber.length() > 0 && newPhoneNumber.substring(0, 1).equals("0")) {
            newPhoneNumber = newPhoneNumber.substring(1);
         }

         try {
            messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            newPhoneNumber = messageEJB.cleanAndValidatePhoneNumber(newPhoneNumber, true);
         } catch (Exception var4) {
         }

         log.info("Cleaning phone number: original='" + phoneNumber + "', cleaned='" + newPhoneNumber + "'");
         return newPhoneNumber;
      } else {
         log.warn("Phone number is null or invalid; can't clean number");
         return null;
      }
   }

   public String fixCallerCountryCode(String phoneNumber, String didNumber) throws CreateException, RemoteException, IOException {
      String newPhoneNumber = this.stripNonNumeric(phoneNumber);
      String newDIDNumber = this.stripNonNumeric(didNumber);
      if (newPhoneNumber != null && newPhoneNumber.length() >= 1) {
         if (newDIDNumber != null && newDIDNumber.length() >= 1) {
            try {
               newDIDNumber = this.cleanPhoneNumber(newDIDNumber);
               Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
               int phoneIDD = -1;
               int didIDD = -1;

               try {
                  phoneIDD = messageEJB.getIDDCode(newPhoneNumber);
               } catch (Exception var10) {
               }

               try {
                  didIDD = messageEJB.getIDDCode(newDIDNumber);
               } catch (Exception var9) {
               }

               if (newPhoneNumber.length() > 3 && newPhoneNumber.substring(0, 4).equals("0000")) {
                  newPhoneNumber = newPhoneNumber.substring(4);
               } else if (newPhoneNumber.length() > 2 && newPhoneNumber.substring(0, 3).equals("000")) {
                  newPhoneNumber = newPhoneNumber.substring(3);
               } else if (newPhoneNumber.length() > 1 && newPhoneNumber.substring(0, 2).equals("00")) {
                  newPhoneNumber = newPhoneNumber.substring(2);
               } else if (newPhoneNumber.length() > 1 && !newPhoneNumber.substring(0, 1).equals("0")) {
                  if (didIDD > 0 && phoneIDD != didIDD) {
                     newPhoneNumber = new String("" + didIDD) + newPhoneNumber;
                  }
               } else if (newPhoneNumber.substring(0, 1).equals("0")) {
                  newPhoneNumber = "" + didIDD + newPhoneNumber.substring(1);
               }
            } catch (CreateException var11) {
               log.warn("Error prefixing number (ex1); " + var11.getMessage());
            } catch (Exception var12) {
               log.warn("Error prefixing number (ex2); " + var12.getMessage());
            }

            log.info("Fixing country code: original='" + phoneNumber + "', fixed='" + newPhoneNumber + "' (DID is '" + newDIDNumber + "')");
            return newPhoneNumber;
         } else {
            log.warn("DID number is null or invalid; can't fix phone number");
            return null;
         }
      } else {
         log.warn("Phone number is null or invalid; can't fix phone number");
         return null;
      }
   }

   public String fixValidateNumber(String phoneNumber) throws CreateException, RemoteException, IOException {
      String newPhoneNumber = this.stripNonNumeric(phoneNumber);
      if (newPhoneNumber != null && newPhoneNumber.length() >= 1) {
         try {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);

            try {
               newPhoneNumber = messageEJB.cleanAndValidatePhoneNumber(newPhoneNumber, true);
            } catch (Exception var5) {
               newPhoneNumber = null;
            }
         } catch (CreateException var6) {
            log.warn("Error fixvalidate number (ex1); " + var6.getMessage());
            newPhoneNumber = null;
         } catch (Exception var7) {
            log.warn("Error fixvalidate number (ex2); " + var7.getMessage());
            newPhoneNumber = null;
         }

         if (newPhoneNumber == null || newPhoneNumber.length() < 1) {
            newPhoneNumber = null;
         }

         log.info("Validate and fix number: original='" + phoneNumber + "', fixed='" + newPhoneNumber + "'");
         return newPhoneNumber;
      } else {
         log.warn("Phone number is null or invalid; can't fix phone number");
         return null;
      }
   }
}
