package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.CreditTransferData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.data.UserRegistrationContextData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.restapi.data.UserCreditData;
import com.projectgoth.fusion.restapi.data.UserRegistrationData;
import com.projectgoth.fusion.restapi.enums.RegistrationType;
import java.util.Date;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;

@Provider
@Path("/partner")
public class PartnerResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PartnerResource.class));

   @POST
   @Path("/registerNewUser")
   @Produces({"application/json"})
   @Consumes({"application/json"})
   public DataHolder<UserRegistrationData> registerNewUser(UserRegistrationData userRegnData) throws FusionRestException {
      if (StringUtil.isBlank(userRegnData.userName)) {
         throw new FusionRestException(202, "Username is required");
      } else if (StringUtil.isBlank(userRegnData.password)) {
         throw new FusionRestException(202, "Password is required");
      } else {
         UserData userData = new UserData();
         userData.username = userRegnData.userName;
         userData.password = userRegnData.password;
         userData.mobilePhone = userRegnData.mobilePhone;
         userData.emailAddress = userRegnData.emailAddress;
         userData.registrationIPAddress = userRegnData.regnIPAddress;
         userData.type = UserData.TypeEnum.MIG33;
         userData.mobileVerified = false;
         userData.dateRegistered = new Date();
         userData.accountType = UserData.AccountTypeEnum.INDIVIDUAL;
         userData.accountVerified = UserData.AccountVerifiedEnum.PENDING;

         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            userData = userEJB.createUser(userData, new UserProfileData(), false, new UserRegistrationContextData((String)null, false, RegistrationType.MOBILE_REGISTRATION), new AccountEntrySourceData(this.getClass()));
            userRegnData.userId = userData.userID;

            try {
               userEJB.activateAccount(userData.username, userData.verificationCode, true, new AccountEntrySourceData(this.getClass()));
            } catch (Exception var6) {
               log.error("Failed to activate account for user: " + userData.username + "[" + userData.userID + "]");
            }

            try {
               userEJB.insertPartnerUser(userRegnData.partnerId, userData.userID);
            } catch (Exception var5) {
               log.error("Failed to associate user: " + userData.username + "[" + userData.userID + "] to partner: " + userRegnData.partnerId);
            }
         } catch (CreateException var7) {
            log.error("Failed to create UserBean", var7);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         } catch (EJBException var8) {
            log.error("Transaction failed due to", var8);
            throw new FusionRestException(201, var8.getMessage());
         }

         return new DataHolder(userRegnData);
      }
   }

   @POST
   @Path("/registerUser")
   @Produces({"application/json"})
   @Consumes({"application/json"})
   public DataHolder<UserRegistrationData> registerExistingUser(UserRegistrationData userRegnData) throws FusionRestException {
      String mobilePhone = userRegnData.mobilePhone;

      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData userData = userEJB.loadUserFromMobilePhone(mobilePhone);
         if (userData == null) {
            throw new FusionRestException(203, "Unknown mobile number: " + mobilePhone);
         }

         userRegnData.userId = userData.userID;
         userEJB.insertPartnerUser(userRegnData.partnerId, userData.userID);
      } catch (CreateException var5) {
         log.error("Failed to create UserBean", var5);
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
      } catch (EJBException var6) {
         log.error("Transaction failed due to", var6);
         throw new FusionRestException(201, var6.getMessage());
      }

      return new DataHolder(userRegnData);
   }

   @POST
   @Path("/redeem")
   @Produces({"application/json"})
   @Consumes({"application/json"})
   public DataHolder<UserCreditData> redeemCredits(UserCreditData creditData) throws FusionRestException {
      try {
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         CreditTransferData creditTransferData = accountEJB.transferPartnerCredit(creditData.partnerId, creditData.mobilePhone, creditData.amount, creditData.transactionId, AccountEntryData.TypeEnum.USSD_PARTNER_REDEMPTION, new AccountEntrySourceData(this.getClass()));
         creditData.balance = creditTransferData.getAccountBalanceData().balance;
         creditData.accountEntryId = creditTransferData.getAccountEntryData().id;
      } catch (CreateException var4) {
         log.error("Failed to create AccountBean", var4);
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
      } catch (EJBException var5) {
         log.error("Transaction failed due to", var5);
         throw new FusionRestException(201, var5.getMessage());
      }

      return new DataHolder(creditData);
   }

   @POST
   @Path("/bonus")
   @Produces({"application/json"})
   @Consumes({"application/json"})
   public DataHolder<UserCreditData> redeemBonusCredits(UserCreditData creditData) throws FusionRestException {
      try {
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         CreditTransferData creditTransferData = accountEJB.transferPartnerCredit(creditData.partnerId, creditData.mobilePhone, creditData.amount, creditData.transactionId, AccountEntryData.TypeEnum.USSD_PARTNER_BONUS, new AccountEntrySourceData(this.getClass()));
         creditData.balance = creditTransferData.getAccountBalanceData().balance;
         creditData.accountEntryId = creditTransferData.getAccountEntryData().id;
      } catch (CreateException var4) {
         log.error("Failed to create AccountBean", var4);
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
      } catch (EJBException var5) {
         log.error("Transaction failed due to", var5);
         throw new FusionRestException(201, var5.getMessage());
      }

      return new DataHolder(creditData);
   }

   @POST
   @Path("/purchaseCredit")
   @Produces({"application/json"})
   @Consumes({"application/json"})
   public DataHolder<UserCreditData> purchaseCredits(UserCreditData creditData) throws FusionRestException {
      try {
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         CreditTransferData creditTransferData = accountEJB.transferPartnerCredit(creditData.partnerId, creditData.mobilePhone, creditData.amount, creditData.transactionId, AccountEntryData.TypeEnum.USSD_PARTNER_PURCHASE, new AccountEntrySourceData(this.getClass()));
         creditData.balance = creditTransferData.getAccountBalanceData().balance;
         creditData.accountEntryId = creditTransferData.getAccountEntryData().id;
      } catch (CreateException var4) {
         log.error("Failed to create AccountBean", var4);
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
      } catch (EJBException var5) {
         log.error("Transaction failed due to", var5);
         throw new FusionRestException(201, var5.getMessage());
      }

      return new DataHolder(creditData);
   }
}
