package com.projectgoth.fusion.bl1;

import Ice.Current;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.HashUtils;
import com.projectgoth.fusion.common.PasswordGenerator;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.dao.CountryDAO;
import com.projectgoth.fusion.dao.LiveIdCredentialDAO;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.domain.LiveIdCredential;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.slice.BlueLabelOneVoucher;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.WebServiceResponse;
import com.projectgoth.fusion.slice._BlueLabelServiceDisp;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import javax.ejb.CreateException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import mibli.Mibli;
import mibli.MibliService;
import mibli.MibliServiceLocator;
import mibli.Response;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;

public class BlueLabelServiceI extends _BlueLabelServiceDisp {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(BlueLabelServiceI.class));
   static final int KEEP_ERRORS_FOR_HOURS = 6;
   private LiveIdCredentialDAO liveIdCredentialDAO;
   private CountryDAO countryDAO;
   private String webServiceKey;
   private String mibliUsername;
   private String mibliPassword;
   private String mibliWSDLURL;
   private long serviceKeyUpdateInterval = 82800000L;
   private MibliService serviceLocator;
   private boolean createAccountEntries;
   private PasswordGenerator passwordGenerator = new PasswordGenerator();
   private LinkedList<BlueLabelError> recentErrors = new LinkedList();

   public void configureService() throws Exception {
      log.info("initialising web service");
      this.serviceLocator = new MibliServiceLocator(this.mibliWSDLURL, new QName("urn:mibli", "MibliService"));
      log.info("WSDL location [" + this.serviceLocator.getWSDLDocumentLocation() + "] ");
      this.initializeSession();
      log.info("Web Service has been initialized, web service key is [" + this.webServiceKey + "]");
      log.info("Account Entry creation [" + this.createAccountEntries + "]");
      this.passwordGenerator.setAlphabet(PasswordGenerator.NONCONFUSING_ALPHABET);
      this.passwordGenerator.setFirstAlphabet((char[])null);
      this.passwordGenerator.setLastAlphabet((char[])null);
      this.passwordGenerator.setMaxRepetition(0);
      BlueLabelServiceI.UpdateWebserviceKeyThread task = new BlueLabelServiceI.UpdateWebserviceKeyThread(this);
      Timer timer = new Timer(true);
      timer.schedule(task, this.serviceKeyUpdateInterval, this.serviceKeyUpdateInterval);
   }

   public void shutdown() {
   }

   public String initializeSession() throws FusionException {
      try {
         Mibli stub = this.serviceLocator.getmibli();
         Response ticket = stub.initialize(this.mibliUsername, this.mibliPassword);
         this.webServiceKey = ticket.getResponseData();
         return this.webServiceKey;
      } catch (ServiceException var3) {
         log.error("failed to contact mibli", var3);
         throw new FusionException("failed to initialize session");
      } catch (RemoteException var4) {
         log.error("failed to contact mibli", var4);
         throw new FusionException("failed to initialize session");
      }
   }

   public WebServiceResponse registerAccount(String username, String password, int countryCode, String mobileNumber, int secretQuestionCode, String secretQuestionAnswer, String firstName, String lastName, String nickName, String dateOfBirth, String sex, String emailAddress, Current __current) throws FusionException {
      try {
         if (log.isDebugEnabled()) {
            log.debug("attempting to register user with live id [" + username + "] p [" + password + "] country code [" + countryCode + "] mobile number [" + mobileNumber + "] question [" + secretQuestionCode + "] secretQuestionAnswer [" + secretQuestionAnswer + "] first name [" + firstName + "] last name [" + lastName + "] nick name [" + nickName + "] date of birth [" + dateOfBirth + "] sex [" + sex + "] email [" + emailAddress + "]");
         }

         Mibli stub = this.serviceLocator.getmibli();
         Response response = stub.registerNewAccountSync(this.webServiceKey, username, password, countryCode, mobileNumber, secretQuestionCode, secretQuestionAnswer, firstName, lastName, nickName, dateOfBirth, sex, emailAddress);
         if (response.getResponseCode() == 0) {
            log.info("account registration response data [" + response.getResponseData() + "] for username [" + username + "]");
         }

         return new WebServiceResponse(response.getResponseData(), response.getResponseCode());
      } catch (ServiceException var16) {
         log.error("failed to register account synchronously", var16);
         throw new FusionException("failed to register account syncronously");
      } catch (RemoteException var17) {
         log.error("failed to register account synchronously", var17);
         throw new FusionException("failed to register account syncronously");
      }
   }

   public WebServiceResponse getAccountStatus(String username, Current __current) throws FusionException {
      try {
         LiveIdCredential liveIdCredential = this.liveIdCredentialDAO.getCredential(username);
         if (liveIdCredential != null) {
            if (log.isDebugEnabled()) {
               log.debug("getting account status, found live id [" + liveIdCredential.getLiveId() + " for username [" + username + "]");
            }

            Mibli stub = this.serviceLocator.getmibli();
            Response response = stub.getStatus(this.webServiceKey, liveIdCredential.getLiveId());
            if (log.isDebugEnabled()) {
               log.debug("account status for username [" + username + "] and live id [" + liveIdCredential.getLiveId() + "] response code [" + response.getResponseCode() + "] and response data [" + response.getResponseData() + "]");
            }

            return new WebServiceResponse(response.getResponseData(), response.getResponseCode());
         } else {
            if (log.isDebugEnabled()) {
               log.debug("no live id found for username [" + username + "]");
            }

            return new WebServiceResponse((String)null, BlueLabelResponseCodes.USER_ACCOUNT_DOES_NOT_EXIST.value());
         }
      } catch (ServiceException var6) {
         log.error("failed to get account status", var6);
         throw new FusionException("failed to get account status");
      } catch (RemoteException var7) {
         log.error("failed to get account status", var7);
         throw new FusionException("failed to get account status");
      }
   }

   private String liveIdForUsername(String username) {
      return HashUtils.asHex(HashUtils.md5(username));
   }

   public WebServiceResponse authenticate(String username, Current __current) throws FusionException {
      if (!StringUtils.hasLength(username)) {
         log.error("supplied username was empty");
         throw new FusionException("supplied username was empty");
      } else {
         try {
            LiveIdCredential liveIdCredential = this.liveIdCredentialDAO.getCredential(username);
            if (liveIdCredential == null) {
               if (log.isDebugEnabled()) {
                  log.debug("creating new live id credential for username [" + username + "]");
               }

               liveIdCredential = new LiveIdCredential(username, this.liveIdForUsername(username), this.passwordGenerator.getPass(10));
               CountryData countryData = this.countryDAO.getCountryForUser(username);
               WebServiceResponse response = this.registerAccount(liveIdCredential.getLiveId(), liveIdCredential.getPassword(), countryData.iddCode, "1234567890", 0, "maidenname", "", "", "", "", "", "");
               if (response.responseCode != BlueLabelResponseCodes.SUCCESS.value()) {
                  log.error("failed to register live id for username [" + username + "], response [" + BlueLabelResponseCodes.fromValue(response.responseCode) + "]");
                  throw new FusionException("failed to register account with mibli");
               }

               liveIdCredential.setLiveId(response.responseData);
               this.liveIdCredentialDAO.persistCredential(liveIdCredential);
            }

            if (log.isDebugEnabled()) {
               log.debug("authenticating live id [" + liveIdCredential.getLiveId() + "] for username [" + username + "]");
            }

            Mibli stub = this.serviceLocator.getmibli();
            Response response = stub.authenticate(this.webServiceKey, liveIdCredential.getLiveId(), liveIdCredential.getPassword());
            return new WebServiceResponse(response.getResponseData(), response.getResponseCode());
         } catch (ServiceException var6) {
            log.error("failed to authenticate user [" + username + "] with mibli", var6);
            throw new FusionException("failed to authenticate with mibli");
         } catch (RemoteException var7) {
            log.error("failed to authenticate user [" + username + "] with mibli", var7);
            throw new FusionException("failed to authenticate with mibli");
         }
      }
   }

   public static String blueLabelOneVoucherToString(BlueLabelOneVoucher voucher) {
      StringBuilder stringBuilder = new StringBuilder("voucher: number [");
      stringBuilder.append(voucher.number).append("] amount redeemed [").append(voucher.amountRedeemed).append("] total value [").append(voucher.value).append("] currency [").append(voucher.currency).append("] transaction reference [").append(voucher.transactionReference).append("]");
      return stringBuilder.toString();
   }

   private void createAccountEntry(String username, BlueLabelOneVoucher voucher) throws Exception {
      try {
         if (log.isDebugEnabled()) {
            log.debug("user [" + username + "] redeemed " + blueLabelOneVoucherToString(voucher));
         }

         Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         AccountEntryData accountEntry = new AccountEntryData();
         Double amount = Double.valueOf(voucher.amountRedeemed);
         log.debug("voucher amount for account entry " + amount);
         DecimalFormat df = new DecimalFormat("0.00");
         accountEntry.username = username;
         accountEntry.type = AccountEntryData.TypeEnum.BLUE_LABEL_ONE_VOUCHER;
         accountEntry.reference = String.valueOf(voucher.transactionReference);
         accountEntry.description = "Blue Label One Voucher[";
         accountEntry.description = accountEntry.description + voucher.number;
         accountEntry.description = accountEntry.description + "] redemption ";
         accountEntry.description = accountEntry.description + df.format(amount);
         accountEntry.description = accountEntry.description + " of total value " + df.format(Double.valueOf(voucher.value)) + " " + voucher.currency;
         accountEntry.amount = amount;
         accountEntry.fundedAmount = amount;
         accountEntry.currency = voucher.currency;
         accountEntry.tax = 0.0D;
         accountEJB.createAccountEntry((Connection)null, accountEntry, new AccountEntrySourceData(BlueLabelService.class));
      } catch (CreateException var7) {
         log.error("failed to contact account bean", var7);
         throw var7;
      }
   }

   public WebServiceResponse fullVoucherRedemption(String migUsername, String userTicket, BlueLabelOneVoucher voucher, Current __current) throws FusionException {
      try {
         voucher.value = StringUtil.scrubDoubleAmount(voucher.value);
         voucher.value = Double.valueOf(voucher.value).toString();
         if (!StringUtils.hasLength(userTicket)) {
            log.error("invalid empty user ticket supplied");
            throw new FusionException("invalid empty user ticket supplied");
         }

         voucher.amountRedeemed = voucher.value;
         voucher.currency = "ZAR";
         boolean isTestVoucher = voucher.currency.equals("ZAR") && voucher.number.startsWith("999999") || voucher.currency.equals("EUR") && voucher.number.startsWith("633718019") || voucher.currency.equals("GBP") && voucher.number.startsWith("633718009");
         Mibli stub = this.serviceLocator.getmibli();
         Response response = stub.fullVoucherRedemption(this.webServiceKey, userTicket, voucher.number, voucher.value);
         log.info("redemption results for user [" + migUsername + "]  and voucher [" + blueLabelOneVoucherToString(voucher) + "] = [" + BlueLabelResponseCodes.fromValue(response.getResponseCode()) + " (" + response.getResponseCode() + ") ] and reference [" + response.getResponseData() + "] and is test voucher? " + (isTestVoucher ? "yes" : "no"));
         synchronized(this.recentErrors) {
            while(!this.recentErrors.isEmpty() && ((BlueLabelError)this.recentErrors.getFirst()).isOlderThanHours(6)) {
               this.recentErrors.removeFirst();
            }
         }

         if (response.getResponseCode() == BlueLabelResponseCodes.SUCCESS.value()) {
            if (StringUtils.hasLength(response.getResponseData())) {
               try {
                  voucher.transactionReference = response.getResponseData();
                  if (this.createAccountEntries && !isTestVoucher) {
                     this.createAccountEntry(migUsername, voucher);
                  }
               } catch (Exception var12) {
                  log.error("voucher successfully redeemed but failed to create account entry!! username [" + migUsername + "] " + blueLabelOneVoucherToString(voucher), var12);
               }
            } else {
               log.error("voucher successfully redeemed but no transaction reference returned? not creating account entry. username [" + migUsername + "] " + blueLabelOneVoucherToString(voucher));
            }
         } else if (response.getResponseCode() != BlueLabelResponseCodes.INVALID_VOUCHER.value() && response.getResponseCode() != BlueLabelResponseCodes.INVALID_VOUCHER_CODE.value()) {
            log.info("Adding error [" + BlueLabelResponseCodes.fromValue(response.getResponseCode()).toString() + "] to recent errors, we now have " + (this.recentErrors.size() + 1) + " errors");
            synchronized(this.recentErrors) {
               this.recentErrors.add(new BlueLabelError(System.currentTimeMillis(), BlueLabelResponseCodes.fromValue(response.getResponseCode())));
            }
         }

         return new WebServiceResponse(response.getResponseData(), response.getResponseCode());
      } catch (ServiceException var14) {
         log.error("failed to redeem voucher for username [" + migUsername + "] " + blueLabelOneVoucherToString(voucher), var14);
      } catch (RemoteException var15) {
         log.error("failed to redeem voucher for username [" + migUsername + "] " + blueLabelOneVoucherToString(voucher), var15);
      } catch (NumberFormatException var16) {
         log.error("failed to redeem voucher (invalid voucher value) for username [" + migUsername + "] " + blueLabelOneVoucherToString(voucher));
         return new WebServiceResponse("Invalid voucher amount", BlueLabelResponseCodes.INVALID_VOUCHER_VALUE.value());
      }

      throw new FusionException("Unable to redeem voucher, an unknown problem occurred");
   }

   @Required
   public void setMibliUsername(String mibliUsername) {
      this.mibliUsername = mibliUsername;
   }

   @Required
   public void setMibliPassword(String milbiPassword) {
      this.mibliPassword = milbiPassword;
   }

   @Required
   public void setMibliWSDLURL(String mibliServiceURL) {
      this.mibliWSDLURL = mibliServiceURL;
   }

   @Required
   public void setCreateAccountEntries(boolean createAccountEntries) {
      this.createAccountEntries = createAccountEntries;
   }

   @Required
   public void setLiveIdCredentialDAO(LiveIdCredentialDAO liveIdCredentialDAO) {
      this.liveIdCredentialDAO = liveIdCredentialDAO;
   }

   @Required
   public void setCountryDAO(CountryDAO countryDataDAO) {
      this.countryDAO = countryDataDAO;
   }

   public void setServiceKeyUpdateInterval(long serviceKeyUpdateInterval) {
      this.serviceKeyUpdateInterval = serviceKeyUpdateInterval;
   }

   public int getRecentErrorCount() {
      return this.recentErrors.size();
   }

   public class UpdateWebserviceKeyThread extends TimerTask {
      private BlueLabelServiceI blueLabelService;

      public UpdateWebserviceKeyThread(BlueLabelServiceI blueLabelService) {
         this.blueLabelService = blueLabelService;
      }

      public void run() {
         try {
            if (this.blueLabelService.initializeSession() != null) {
               BlueLabelServiceI.log.info("successfully updated the web service key!");
            }
         } catch (FusionException var2) {
            BlueLabelServiceI.log.fatal("failed to update web service key!!!");
         }

      }
   }
}
