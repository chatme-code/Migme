package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlParameter;
import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.GroupMemberData;
import com.projectgoth.fusion.data.MerchantDetailsData;
import com.projectgoth.fusion.data.RegistrationContextData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.SecurityQuestion;
import com.projectgoth.fusion.data.ThirdPartyApplicationData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserDataAndRegistrationContextData;
import com.projectgoth.fusion.data.UserEmailAddressData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.data.UserRegistrationContextData;
import com.projectgoth.fusion.data.UserReputationScoreAndLevelData;
import com.projectgoth.fusion.data.UserSettingData;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.invitation.CreateInvitationsResult;
import com.projectgoth.fusion.invitation.InvitationData;
import com.projectgoth.fusion.invitation.InvitationResponseData;
import com.projectgoth.fusion.invitation.restapi.data.InvitationDetailsData;
import com.projectgoth.fusion.invitation.restapi.data.SendingInvitationData;
import com.projectgoth.fusion.restapi.data.RegistrationTokenData;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import com.projectgoth.fusion.restapi.data.UserActivationData;
import com.projectgoth.fusion.restapi.data.UserMigboProfileData;
import com.projectgoth.fusion.restapi.data.UserVerificationData;
import com.projectgoth.fusion.restapi.enums.RegistrationType;
import com.projectgoth.fusion.slice.AuthenticationServiceResponseCodeEnum;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;

public interface UserLocal extends EJBLocalObject {
   void checkUsername(String var1) throws EJBException, NoSuchFieldException;

   UserDataAndRegistrationContextData getUserDataFromUserRegistrationTable(String var1, UserData var2) throws EJBException;

   UserActivationData getVerificationDataFromUserRegistrationTable(String var1) throws EJBException;

   UserData createPrepaidCardUser(String var1, String var2, AccountEntrySourceData var3) throws EJBException;

   Map getRecentMobileActivationCountsWithMobilePrefixByIPAddress(String var1, int var2) throws EJBException;

   boolean checkRecentCommonPrefixActivations(Integer var1, String var2);

   UserData createUser(UserData var1, UserProfileData var2, boolean var3, UserRegistrationContextData var4, AccountEntrySourceData var5) throws EJBException;

   UserData createUser(UserData var1, UserProfileData var2, boolean var3, UserRegistrationContextData var4, AccountEntrySourceData var5, boolean var6, boolean var7, boolean var8) throws EJBException;

   /** @deprecated */
   UserData postCreateUser(UserData var1, UserProfileData var2, boolean var3, AccountEntrySourceData var4, UserRegistrationContextData var5) throws EJBException;

   UserData postCreateUser(UserData var1, UserProfileData var2, boolean var3, AccountEntrySourceData var4, UserRegistrationContextData var5, boolean var6) throws EJBException;

   void sendVerificationToken(String var1, String var2, String var3, RegistrationType var4) throws EJBException;

   boolean isUsernameAvailable(String var1);

   boolean emailAddressExists(String var1) throws EJBException;

   UserVerificationData getVerificationDataFromToken(String var1, boolean var2) throws EJBException;

   UserVerificationData getVerificationDataFromToken(String var1) throws EJBException;

   boolean validateRegistrationToken(UserVerificationData var1);

   void insertPartnerUser(int var1, int var2) throws EJBException;

   Set loadBroadcastList(String var1, Connection var2) throws SQLException;

   Set checkAndLoadBroadcastList(String var1, Connection var2) throws SQLException, CreateException, EJBException, NoSuchFieldException;

   List getUserSettings(String var1) throws SQLException;

   void checkUserAliasByUserid(int var1, String var2, boolean var3, Connection var4) throws EJBException;

   void setUserAliasByUserid(int var1, String var2) throws EJBException;

   String getUserAliasByUsername(String var1, Connection var2) throws EJBException;

   String getUsernameByAlias(String var1, Connection var2) throws EJBException;

   int getUseridByAlias(String var1, Connection var2) throws EJBException;

   SecurityQuestion getSecurityQuestion(int var1) throws EJBException;

   String getUserAliasByUserid(int var1, Connection var2) throws EJBException;

   String getUsernameByUserid(int var1, Connection var2) throws EJBException;

   boolean isBounceEmailAddress(String var1) throws FusionEJBException;

   Map getEmailUserIdMapping(Collection var1) throws FusionEJBException;

   CreateInvitationsResult createInvitation(int var1, SendingInvitationData var2, Connection var3) throws EJBException, FusionEJBException;

   CreateInvitationsResult createInvitationForFBInvite(int var1, SendingInvitationData var2, Connection var3);

   InvitationData getInvitationData(int var1, boolean var2, Connection var3);

   ThirdPartyApplicationData getThirdPartyApplicationData(int var1, Connection var2);

   InvitationData getInvitationDataForFBInvite(String var1, String var2, boolean var3, Connection var4);

   InvitationResponseData logInvitationResponse(Connection var1, Date var2, InvitationData var3, InvitationResponseData.ResponseType var4, String var5, InvitationData.StatusFieldValue var6);

   int getUserID(String var1, Connection var2) throws EJBException;

   int getUserID(String var1, Connection var2, boolean var3) throws EJBException;

   Map getLastLoggedInUsersWithVerifiedEmail(UserEmailAddressData.UserEmailAddressTypeEnum var1, Timestamp var2, Timestamp var3, Connection var4) throws EJBException;

   UserData loadUserByUsernameOrAlias(String var1, boolean var2, boolean var3) throws EJBException;

   UserData loadUser(String var1, boolean var2, boolean var3) throws EJBException;

   UserData loadUserFromID(int var1);

   UserData loadUserFromMobilePhone(String var1) throws EJBException;

   UserData loadUserFromVoucherNumber(String var1) throws EJBException;

   void updateUserDetail(UserData var1) throws EJBException;

   void updateOtherIMDetail(String var1, ImType var2, String var3, String var4) throws EJBException;

   void updateStatusMessage(int var1, String var2, String var3, ClientType var4, SSOEnums.View var5) throws EJBException;

   void updateDisplayPicture(String var1, String var2) throws EJBException;

   void removeDisplayPicture(String var1) throws EJBException;

   UserProfileData.StatusEnum getUserProfileStatus(String var1) throws EJBException;

   UserProfileData getUserProfile(String var1, String var2, boolean var3) throws EJBException, FusionEJBException;

   boolean updateUserProfile(UserProfileData var1) throws EJBException;

   void activateAccount(String var1, String var2, boolean var3, AccountEntrySourceData var4) throws EJBException;

   void inviteFriend(String var1, String var2, String var3, Integer var4, String var5, String var6, String var7, AccountEntrySourceData var8) throws EJBException, FusionEJBException;

   void resendVerificationCode(String var1, String var2, AccountEntrySourceData var3) throws EJBException;

   void sendEmailVerification(int var1, String var2) throws EJBException;

   void changeMobilePhone(String var1, String var2, AccountEntrySourceData var3) throws EJBException;

   void changeMobilePhone(String var1, String var2, boolean var3, AccountEntrySourceData var4) throws EJBException;

   void postChangeMobilePhone(UserData var1, String var2, String var3, String var4, String var5, boolean var6, AccountEntrySourceData var7) throws EJBException;

   void cancelChangeMobilePhoneRequest(String var1, AccountEntrySourceData var2) throws EJBException;

   String forgotPasswordViaEmail(String var1, String var2) throws FusionEJBException;

   String forgotPasswordViaSecurityQuestion(String var1, String var2, int var3) throws FusionEJBException, EJBException;

   boolean changePassword(String var1, String var2, String var3, String var4) throws FusionEJBException;

   String forgotPasswordViaSMS(String var1, AccountEntrySourceData var2) throws FusionEJBException;

   boolean allowForgotPasswordViaSMS(String var1) throws FusionEJBException;

   void forgotPasswordWithMobileNumberOrEmail(String var1, boolean var2, AccountEntrySourceData var3) throws EJBException;

   AuthenticationServiceResponseCodeEnum validateUserCredential(String var1, String var2, PasswordType var3);

   void changePassword(String var1, String var2, String var3) throws EJBException;

   void loginSucceeded(String var1, String var2, String var3, String var4) throws EJBException;

   void loginFailed(String var1) throws EJBException;

   void activateAccountFailed(String var1) throws EJBException;

   void setAccountStatus(String var1, boolean var2) throws EJBException;

   boolean getAccountStatus(String var1) throws EJBException;

   void setFailedActivationAttempts(String var1, int var2) throws EJBException;

   void setEmailAlertSent(String var1, boolean var2) throws EJBException;

   UserData createUserMerchant(UserData var1, UserProfileData var2, AccountEntrySourceData var3) throws EJBException;

   boolean midletRegistrationDisabled(String var1, String var2, String var3) throws EJBException;

   GroupMemberData getGroupMember(String var1, int var2) throws EJBException;

   GroupData getGroup(int var1) throws EJBException;

   boolean isUserMobileVerified(String var1) throws EJBException;

   boolean isUserMobileOrEmailVerified(String var1) throws EJBException;

   boolean isUserMobileOrEmailVerifiedWithTxSupported(String var1) throws EJBException;

   boolean isUserEmailVerified(String var1) throws FusionEJBException;

   boolean isUserEmailVerifiedWithTxSupport(String var1) throws FusionEJBException;

   AuthenticatedAccessControlParameter getUserAuthenticatedAccessControlParameter(String var1) throws EJBException;

   boolean userAttemptedVerification(String var1, int var2) throws EJBException;

   void updateUserSetting(String var1, UserSettingData.TypeEnum var2, int var3) throws EJBException;

   UserSettingData.AnonymousCallEnum getAnonymousCallSetting(String var1) throws EJBException;

   void updateAnonymousCallSetting(String var1, UserSettingData.AnonymousCallEnum var2) throws EJBException;

   UserSettingData.MessageEnum getMessageSetting(String var1) throws EJBException;

   void updateMessageSetting(String var1, UserSettingData.MessageEnum var2) throws EJBException;

   UserSettingData.EmailSettingEnum getEmailNotificationSetting(String var1, UserSettingData.TypeEnum var2) throws EJBException;

   void updateEmailNotificationSetting(String var1, UserSettingData.TypeEnum var2, UserSettingData.EmailSettingEnum var3) throws EJBException;

   ReputationLevelData getReputationLevelByUserid(int var1) throws EJBException;

   ReputationLevelData getReputationLevelByUserid(int var1, boolean var2) throws EJBException;

   ReputationLevelData getReputationLevel(String var1) throws EJBException;

   ReputationLevelData getReputationLevel(String var1, boolean var2) throws EJBException;

   /** @deprecated */
   void addReputationScore(int var1, int var2, Connection var3) throws EJBException;

   void updateReputationScore(int var1, int var2, boolean var3) throws EJBException;

   UserReputationScoreAndLevelData updateReputationScoreAndGet(int var1, int var2, boolean var3) throws EJBException;

   void invalidateCacheAndNotifyReputationScoreUpdated(int var1);

   UserReputationScoreAndLevelData getReputationScoreAndLevel(int var1, Connection var2);

   UserReputationScoreAndLevelData getReputationScoreAndLevel(int var1, Connection var2, boolean var3) throws EJBException;

   UserReputationScoreAndLevelData getReputationScoreAndLevel(boolean var1, int var2, boolean var3) throws EJBException;

   Map getReputationScoreAndLevelForUsers(List var1, Connection var2) throws EJBException;

   void createLookout(String var1, String var2) throws EJBException;

   double getPriceInUserCurrency(double var1, String var3, String var4) throws FusionEJBException;

   UserMigboProfileData.AdminEnum getAdminLabel(String var1) throws FusionEJBException;

   UserMigboProfileData.MerchantEnum getMerchantLabel(String var1) throws FusionEJBException;

   MerchantDetailsData getBasicMerchantDetails(String var1) throws FusionEJBException;

   boolean isMerchantMentor(String var1) throws FusionEJBException;

   double getUserReferralSuccessRate(String var1, boolean var2) throws FusionEJBException;

   boolean isUserInMigboAccessList(int var1, int var2, int var3) throws FusionEJBException;

   boolean isUserLevelAllowedMigboAccess(int var1, int var2) throws FusionEJBException;

   boolean addUserEmailAddress(int var1, String var2, UserEmailAddressData.UserEmailAddressTypeEnum var3) throws FusionEJBException;

   boolean removeUserEmailAddressByType(int var1, UserEmailAddressData.UserEmailAddressTypeEnum var2) throws FusionEJBException;

   boolean removeUserEmailAddress(int var1, String var2) throws FusionEJBException;

   boolean updateUserEmailAddress(int var1, String var2, String var3, UserEmailAddressData.UserEmailAddressTypeEnum var4) throws FusionEJBException;

   RegistrationTokenData verifyExternalEmailAddress(Integer var1, String var2) throws FusionEJBException;

   void saveVerifiedUserPrimaryEmailAddressOnDatabase(int var1, String var2);

   boolean isEmailRegistrationNotVerifiedForUsername(String var1);

   boolean isEmailRegistrationNotVerifiedForUsername(String var1, String var2);

   boolean isExternalEmailVerified(int var1, String var2) throws FusionEJBException;

   UserEmailAddressData getUserEmailAddressByType(int var1, UserEmailAddressData.UserEmailAddressTypeEnum var2) throws FusionEJBException;

   String getUsernameByEmailAddress(String var1, UserEmailAddressData.UserEmailAddressTypeEnum var2) throws FusionEJBException;

   void forgotUsernameViaEmailAddress(String var1) throws FusionEJBException;

   String[] getUsersInUserCategory(int var1) throws EJBException;

   Map getUserCategoryNames(int var1) throws FusionEJBException;

   boolean blacklistUser(int var1);

   boolean removeUserFromBlacklist(int var1);

   boolean banUser(int var1);

   boolean suspendUser(int var1, int var2);

   boolean disconnectUser(String var1, String var2);

   int getChatroomsOwnedCount(int var1);

   int getGiftsReceivedCount(int var1);

   int getGroupsJoinedCount(int var1);

   int getPhotosUploadedCount(int var1);

   RegistrationContextData getRegistrationContextData(int var1);

   Map getTaggedUsers(int var1, int var2, int var3) throws FusionEJBException;

   Map getExpiringTaggedUsers(int var1, int var2) throws FusionEJBException;

   MerchantDetailsData getFullMerchantDetails(String var1) throws FusionEJBException;

   boolean setMerchantColorType(Integer var1, MerchantDetailsData.UserNameColorTypeEnum var2) throws FusionEJBException;

   void responseToAlert(int var1, String var2, int var3, InvitationResponseData.ResponseType var4) throws FusionEJBException;

   void addMutualFollowing(int var1, int var2) throws FusionEJBException;

   void addMutualFollowingAndTriggerMigAlerts(UserData var1, UserData var2) throws FusionEJBException;

   void addMutualFollowingAndTriggerMigAlertsToAllInviters(InvitationData var1, UserData var2) throws FusionEJBException;

   InvitationDetailsData getInvitationDetailsData(String var1, boolean var2, Date var3) throws FusionEJBException;

   InvitationDetailsData getInvitationDetailsDataForFBInvite(String var1, String var2, boolean var3, Date var4) throws FusionEJBException;

   InvitationDetailsData convertInvitationDataToInvitationDetailsData(InvitationData var1, boolean var2, Date var3) throws FusionEJBException;

   String getDisplayPicture(String var1);

   UserData getInviterForSignUp(int var1);
}
