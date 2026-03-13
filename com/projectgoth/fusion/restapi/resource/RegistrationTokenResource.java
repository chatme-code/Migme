package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DataUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.data.UserRegistrationContextData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.invitation.InvitationData;
import com.projectgoth.fusion.invitation.InvitationUtils;
import com.projectgoth.fusion.invitation.restapi.data.InvitationDetailsData;
import com.projectgoth.fusion.restapi.data.BooleanData;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.restapi.data.UserCreationData;
import com.projectgoth.fusion.restapi.data.UserVerificationData;
import com.projectgoth.fusion.restapi.enums.RegistrationType;
import java.sql.Timestamp;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;

@Provider
@Path("/registration_token")
public class RegistrationTokenResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RegistrationTokenResource.class));

   @POST
   @Path("")
   @Produces({"application/json"})
   @Consumes({"application/json"})
   public DataHolder<BooleanData> generateVerificationToken(DataHolder<UserCreationData> dataholder) throws FusionRestException {
      UserCreationData userCreationData = (UserCreationData)dataholder.data;
      if (log.isDebugEnabled()) {
         log.debug("generateVerificationToken:UserCreationData:username=[" + userCreationData.username + "],invitationToken=[" + userCreationData.invitationToken + "]" + ",campaign=[" + userCreationData.campaign + "]" + ",userAgent=[" + userCreationData.userAgent + "]" + ",emailAddress=[" + userCreationData.emailAddress + "]" + ",fbid=[" + userCreationData.fbid + "]" + ",accessToken=[" + userCreationData.accessToken + "]" + ",countryISOCode=[" + userCreationData.countryISOCode + "]" + ",registrationIPAddress=[" + userCreationData.registrationIPAddress + "]" + ",registrationDevice=[" + userCreationData.registrationDevice + "]" + ",registrationToken=[" + userCreationData.registrationToken + "]" + ",registrationType=[" + userCreationData.registrationType + "]");
      }

      Integer invitationID = null;
      if (InvitationUtils.isInvitationEngineEnabled((InvitationData.ChannelType)null)) {
         String invitationToken = userCreationData.invitationToken;
         if (!StringUtil.isBlank(invitationToken)) {
            invitationID = InvitationUtils.decryptReferralInvitation(invitationToken);
            if (invitationID < 0) {
               throw new FusionRestException(FusionRestException.RestException.INVALID_REFERRAL_TOKEN);
            }
         }
      }

      userCreationData.initializeToDefaultValues();
      UserData userData = userCreationData.getUserData();
      if ((Boolean)SystemPropertyEntities.Temp.Cache.ER170_ENABLED.getValue()) {
         String mobileDevice = DataUtils.truncateMobileDevice(userData.getRegistrationDevice(), true, String.format("new user registration '%s'", userData.username));
         userData.registrationDevice = mobileDevice;
      }

      UserProfileData userProfileData = new UserProfileData();
      String registrationType = userCreationData.registrationType;
      String campaign = userCreationData.campaign;
      RegistrationType registrationTypeEnum = RegistrationType.fromValue(registrationType);
      if (registrationTypeEnum == null) {
         throw new FusionRestException(101, "Registration failed:Please provide a registrationType(email1 or email2)");
      } else {
         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            AccountEntrySourceData accountEntrySourceData = new AccountEntrySourceData(userData.registrationIPAddress, (String)null, userData.registrationDevice, userData.userAgent);
            userBean.createUser(userData, userProfileData, true, new UserRegistrationContextData(campaign, false, registrationTypeEnum, invitationID), accountEntrySourceData);
         } catch (CreateException var11) {
            throw new FusionRestException(101, "Registration failed:" + var11.getMessage());
         } catch (EJBException var12) {
            throw new FusionRestException(101, "Registration failed, EJBException message:" + var12.getMessage());
         } catch (Exception var13) {
            throw new FusionRestException(101, "Registration failed:" + var13.getMessage());
         }

         return new DataHolder(new BooleanData(true));
      }
   }

   @GET
   @Path("/{verificationToken}")
   @Produces({"application/json"})
   public DataHolder<UserVerificationData> verifyToken(@PathParam("verificationToken") String token, @QueryParam("includingExpiredToken") String includingExpiredTokenStr) throws FusionRestException {
      boolean includingExpiredToken = StringUtil.toBooleanOrDefault(includingExpiredTokenStr, false);

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserVerificationData userVerificationData = userBean.getVerificationDataFromToken(token, includingExpiredToken);
         return new DataHolder(userVerificationData);
      } catch (CreateException var6) {
         throw new FusionRestException(101, "Verification failed:" + var6.getMessage());
      } catch (EJBException var7) {
         throw new FusionRestException(101, "Verification failed, EJBException message:" + var7.getMessage());
      } catch (Exception var8) {
         throw new FusionRestException(101, "Verification failed:" + var8.getMessage());
      }
   }

   @PUT
   @Path("/{verificationToken}")
   @Produces({"application/json"})
   public DataHolder<BooleanData> validateRegistrationToken(@PathParam("verificationToken") String token) throws FusionRestException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserVerificationData userVerificationData = userBean.getVerificationDataFromToken(token);
         if (!userBean.validateRegistrationToken(userVerificationData)) {
            throw new Exception("Token is not valid");
         } else {
            return new DataHolder(new BooleanData(true));
         }
      } catch (CreateException var4) {
         throw new FusionRestException(101, "Verification failed:" + var4.getMessage());
      } catch (EJBException var5) {
         throw new FusionRestException(101, "Verification failed, EJBException message:" + var5.getMessage());
      } catch (Exception var6) {
         throw new FusionRestException(101, "Verification failed:" + var6.getMessage());
      }
   }

   @GET
   @Path("/username_check/{username}")
   @Produces({"application/json"})
   public DataHolder<BooleanData> checkUsernameAvailability(@PathParam("username") String username) throws FusionRestException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         boolean isAvailable = userBean.isUsernameAvailable(username);
         return new DataHolder(new BooleanData(isAvailable));
      } catch (CreateException var4) {
         throw new FusionRestException(101, "Verification failed:" + var4.getMessage());
      } catch (EJBException var5) {
         throw new FusionRestException(101, "Verification failed, EJBException message:" + var5.getMessage());
      } catch (Exception var6) {
         throw new FusionRestException(101, "Verification failed:" + var6.getMessage());
      }
   }

   @GET
   @Path("/email_check/{email}")
   @Produces({"application/json"})
   public DataHolder<BooleanData> checkEmailAvailability(@PathParam("email") String email) throws FusionRestException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         boolean isAvailable = userBean.emailAddressExists(email);
         return new DataHolder(new BooleanData(isAvailable));
      } catch (Exception var4) {
         log.error("Unable to check the existence of email address [" + email + "]: " + var4);
         throw new FusionRestException(FusionRestException.RestException.ERROR);
      }
   }

   @GET
   @Path("/invitation_token/{invitationToken}")
   @Produces({"application/json"})
   public DataHolder<InvitationDetailsData> getInvitationTokenData(@PathParam("invitationToken") String invitationTokenCode, @QueryParam("fetchExtraParameters") String fetchExtraParametersStr) throws FusionRestException {
      if (!InvitationUtils.isInvitationEngineEnabled((InvitationData.ChannelType)null)) {
         throw new FusionRestException(FusionRestException.RestException.INVITATION_DISABLED);
      } else {
         try {
            boolean fetchExtraParameters = StringUtil.toBooleanOrDefault(fetchExtraParametersStr, false);
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            return new DataHolder(userBean.getInvitationDetailsData(invitationTokenCode, fetchExtraParameters, new Timestamp(System.currentTimeMillis())));
         } catch (CreateException var5) {
            log.error("CreateException on getInvitationTokenData", var5);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         } catch (EJBException var6) {
            log.error("CreateException on getInvitationTokenData", var6);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         } catch (Exception var7) {
            log.error("Unhandled exception on getInvitationTokenData", var7);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         }
      }
   }

   @GET
   @Path("/facebook_invite/{facebookRequestId}/{facebookUserId}")
   @Produces({"application/json"})
   public DataHolder<InvitationDetailsData> getInvitationTokenData(@PathParam("facebookRequestId") String facebookRequestId, @PathParam("facebookUserId") String facebookUserId, @QueryParam("fetchExtraParameters") String fetchExtraParametersStr) throws FusionRestException {
      if (!InvitationUtils.isInvitationEngineEnabled(InvitationData.ChannelType.FB)) {
         throw new FusionRestException(FusionRestException.RestException.INVITATION_DISABLED);
      } else {
         try {
            boolean fetchExtraParameters = StringUtil.toBooleanOrDefault(fetchExtraParametersStr, false);
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            return new DataHolder(userBean.getInvitationDetailsDataForFBInvite(facebookRequestId, facebookUserId, fetchExtraParameters, new Timestamp(System.currentTimeMillis())));
         } catch (CreateException var6) {
            log.error("CreateException on getInvitationTokenData", var6);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         } catch (EJBException var7) {
            log.error("CreateException on getInvitationTokenData", var7);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         } catch (Exception var8) {
            log.error("Unhandled exception on getInvitationTokenData", var8);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         }
      }
   }
}
