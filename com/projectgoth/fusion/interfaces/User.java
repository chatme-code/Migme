package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlParameter;
import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.AlertMessageData;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.GroupMemberData;
import com.projectgoth.fusion.data.MerchantDetailsData;
import com.projectgoth.fusion.data.RegistrationContextData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.SecurityQuestion;
import com.projectgoth.fusion.data.ThirdPartyApplicationData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserEmailAddressData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.data.UserRegistrationContextData;
import com.projectgoth.fusion.data.UserReputationScoreAndLevelData;
import com.projectgoth.fusion.data.UserSettingData;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.fdl.enums.AlertContentType;
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
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.CreateException;
import javax.ejb.EJBObject;

public interface User extends EJBObject {
   UserActivationData getVerificationDataFromUserRegistrationTable(String var1) throws RemoteException;

   UserData createPrepaidCardUser(String var1, String var2, AccountEntrySourceData var3) throws RemoteException;

   Map getRecentMobileActivationCountsWithMobilePrefixByIPAddress(String var1, int var2) throws RemoteException;

   boolean checkRecentCommonPrefixActivations(Integer var1, String var2) throws RemoteException;

   UserData createUser(UserData var1, UserProfileData var2, boolean var3, UserRegistrationContextData var4, AccountEntrySourceData var5) throws RemoteException;

   UserData createUser(UserData var1, UserProfileData var2, boolean var3, UserRegistrationContextData var4, AccountEntrySourceData var5, boolean var6, boolean var7, boolean var8) throws RemoteException;

   /** @deprecated */
   UserData postCreateUser(UserData var1, UserProfileData var2, boolean var3, AccountEntrySourceData var4, UserRegistrationContextData var5) throws RemoteException;

   UserData postCreateUser(UserData var1, UserProfileData var2, boolean var3, AccountEntrySourceData var4, UserRegistrationContextData var5, boolean var6) throws RemoteException;

   void sendVerificationToken(String var1, String var2, String var3, RegistrationType var4) throws RemoteException;

   boolean isUsernameAvailable(String var1) throws RemoteException;

   boolean emailAddressExists(String var1) throws RemoteException;

   UserVerificationData getVerificationDataFromToken(String var1, boolean var2) throws RemoteException;

   UserVerificationData getVerificationDataFromToken(String var1) throws RemoteException;

   boolean validateRegistrationToken(UserVerificationData var1) throws RemoteException;

   void insertPartnerUser(int var1, int var2) throws RemoteException;

   UserData loadStaff(String var1) throws RemoteException;

   Set loadBroadcastList(String var1, Connection var2) throws SQLException, RemoteException;

   Set checkAndLoadBroadcastList(String var1, Connection var2) throws SQLException, CreateException, NoSuchFieldException, RemoteException;

   List getUserSettings(String var1) throws SQLException, RemoteException;

   void checkUserAliasByUserid(int var1, String var2, boolean var3, Connection var4) throws RemoteException;

   void setUserAliasByUserid(int var1, String var2) throws RemoteException;

   String getUserAliasByUsername(String var1, Connection var2) throws RemoteException;

   String getUsernameByAlias(String var1, Connection var2) throws RemoteException;

   int getUseridByAlias(String var1, Connection var2) throws RemoteException;

   SecurityQuestion getSecurityQuestion(int var1) throws RemoteException;

   String getUserAliasByUserid(int var1, Connection var2) throws RemoteException;

   String getUsernameByUserid(int var1, Connection var2) throws RemoteException;

   boolean isBounceEmailAddress(String var1) throws FusionEJBException, RemoteException;

   Map getEmailUserIdMapping(Collection var1) throws FusionEJBException, RemoteException;

   CreateInvitationsResult createInvitation(int var1, SendingInvitationData var2, Connection var3) throws FusionEJBException, RemoteException;

   CreateInvitationsResult createInvitationForFBInvite(int var1, SendingInvitationData var2, Connection var3) throws RemoteException;

   InvitationData getInvitationData(int var1, boolean var2, Connection var3) throws RemoteException;

   ThirdPartyApplicationData getThirdPartyApplicationData(int var1, Connection var2) throws RemoteException;

   InvitationData getInvitationDataForFBInvite(String var1, String var2, boolean var3, Connection var4) throws RemoteException;

   InvitationResponseData logInvitationResponse(Connection var1, Date var2, InvitationData var3, InvitationResponseData.ResponseType var4, String var5, InvitationData.StatusFieldValue var6) throws RemoteException;

   int getUserID(String var1, Connection var2) throws RemoteException;

   int getUserID(String var1, Connection var2, boolean var3) throws RemoteException;

   Map getLastLoggedInUsersWithVerifiedEmail(UserEmailAddressData.UserEmailAddressTypeEnum var1, Timestamp var2, Timestamp var3, Connection var4) throws RemoteException;

   UserData loadUserByUsernameOrAlias(String var1, boolean var2, boolean var3) throws RemoteException;

   UserData loadUser(String var1, boolean var2, boolean var3) throws RemoteException;

   UserData loadUserFromID(int var1) throws RemoteException;

   UserData loadUserFromMobilePhone(String var1) throws RemoteException;

   UserData loadUserFromVoucherNumber(String var1) throws RemoteException;

   void updateUserDetail(UserData var1) throws RemoteException;

   void updateOtherIMDetail(String var1, ImType var2, String var3, String var4) throws RemoteException;

   void updateStatusMessage(int var1, String var2, String var3, ClientType var4, SSOEnums.View var5) throws RemoteException;

   void updateDisplayPicture(String var1, String var2) throws RemoteException;

   void removeDisplayPicture(String var1) throws RemoteException;

   UserProfileData.StatusEnum getUserProfileStatus(String var1) throws RemoteException;

   UserProfileData getUserProfile(String var1, String var2, boolean var3) throws FusionEJBException, RemoteException;

   boolean updateUserProfile(UserProfileData var1) throws RemoteException;

   void activateAccount(String var1, String var2, boolean var3, AccountEntrySourceData var4) throws RemoteException;

   void inviteFriend(String var1, String var2, String var3, Integer var4, String var5, String var6, String var7, AccountEntrySourceData var8) throws FusionEJBException, RemoteException;

   void sendVerificationCode(String var1, String var2, String var3, String var4, String var5, String var6, AccountEntrySourceData var7) throws RemoteException;

   void resendVerificationCode(String var1, String var2, AccountEntrySourceData var3) throws RemoteException;

   void sendMerchantActivatedUserSMS(String var1, String var2, String var3, AccountEntrySourceData var4) throws RemoteException;

   void sendEmailVerification(int var1, String var2) throws RemoteException;

   void changeMobilePhone(String var1, String var2, AccountEntrySourceData var3) throws RemoteException;

   void changeMobilePhone(String var1, String var2, boolean var3, AccountEntrySourceData var4) throws RemoteException;

   void cancelChangeMobilePhoneRequest(String var1, AccountEntrySourceData var2) throws RemoteException;

   String forgotPasswordViaEmail(String var1, String var2) throws FusionEJBException, RemoteException;

   String forgotPasswordViaSecurityQuestion(String var1, String var2, int var3) throws FusionEJBException, RemoteException;

   boolean changePassword(String var1, String var2, String var3, String var4) throws FusionEJBException, RemoteException;

   String forgotPasswordViaSMS(String var1, AccountEntrySourceData var2) throws FusionEJBException, RemoteException;

   boolean allowForgotPasswordViaSMS(String var1) throws FusionEJBException, RemoteException;

   void forgotPasswordWithMobileNumberOrEmail(String var1, boolean var2, AccountEntrySourceData var3) throws RemoteException;

   AuthenticationServiceResponseCodeEnum validateUserCredential(String var1, String var2, PasswordType var3) throws RemoteException;

   void changePassword(String var1, String var2, String var3) throws RemoteException;

   void loginSucceeded(String var1, String var2, String var3, String var4) throws RemoteException;

   void loginFailed(String var1) throws RemoteException;

   void activateAccountFailed(String var1) throws RemoteException;

   AlertMessageData getLatestAlertMessage(int var1, AlertMessageData.TypeEnum var2, int var3, Date var4, AlertContentType var5, int var6) throws RemoteException;

   void setAccountStatus(String var1, boolean var2) throws RemoteException;

   boolean getAccountStatus(String var1) throws RemoteException;

   void setFailedActivationAttempts(String var1, int var2) throws RemoteException;

   void bannedFromChatRoom(String var1, String var2, String var3, String var4) throws RemoteException;

   void decrementChatroomBanCounter(String var1) throws RemoteException;

   String processEmailNotification(String var1, String var2, String var3, AccountEntrySourceData var4) throws RemoteException;

   void setEmailAlertSent(String var1, boolean var2) throws RemoteException;

   UserData createUserMerchant(UserData var1, UserProfileData var2, AccountEntrySourceData var3) throws RemoteException;

   void sendLookouts(String var1, AccountEntrySourceData var2) throws RemoteException;

   boolean midletRegistrationDisabled(String var1, String var2, String var3) throws RemoteException;

   GroupMemberData getGroupMember(String var1, int var2) throws RemoteException;

   GroupData getGroup(int var1) throws RemoteException;

   boolean isUserMobileVerified(String var1) throws RemoteException;

   boolean isUserMobileOrEmailVerified(String var1) throws RemoteException;

   boolean isUserMobileOrEmailVerifiedWithTxSupported(String var1) throws RemoteException;

   boolean isUserEmailVerified(String var1) throws FusionEJBException, RemoteException;

   boolean isUserEmailVerifiedWithTxSupport(String var1) throws FusionEJBException, RemoteException;

   AuthenticatedAccessControlParameter getUserAuthenticatedAccessControlParameter(String var1) throws RemoteException;

   boolean userAttemptedVerification(String var1, int var2) throws RemoteException;

   void updateUserSetting(String var1, UserSettingData.TypeEnum var2, int var3) throws RemoteException;

   UserSettingData.AnonymousCallEnum getAnonymousCallSetting(String var1) throws RemoteException;

   void updateAnonymousCallSetting(String var1, UserSettingData.AnonymousCallEnum var2) throws RemoteException;

   UserSettingData.MessageEnum getMessageSetting(String var1) throws RemoteException;

   void updateMessageSetting(String var1, UserSettingData.MessageEnum var2) throws RemoteException;

   UserSettingData.EmailSettingEnum getEmailNotificationSetting(String var1, UserSettingData.TypeEnum var2) throws RemoteException;

   void updateEmailNotificationSetting(String var1, UserSettingData.TypeEnum var2, UserSettingData.EmailSettingEnum var3) throws RemoteException;

   ReputationLevelData getReputationLevelByUserid(int var1) throws RemoteException;

   ReputationLevelData getReputationLevelByUserid(int var1, boolean var2) throws RemoteException;

   ReputationLevelData getReputationLevel(String var1) throws RemoteException;

   ReputationLevelData getReputationLevel(String var1, boolean var2) throws RemoteException;

   /** @deprecated */
   void addReputationScore(int var1, int var2, Connection var3) throws RemoteException;

   void updateReputationScore(int var1, int var2, boolean var3) throws RemoteException;

   UserReputationScoreAndLevelData updateReputationScoreAndGet(int var1, int var2, boolean var3) throws RemoteException;

   void invalidateCacheAndNotifyReputationScoreUpdated(int var1) throws RemoteException;

   UserReputationScoreAndLevelData getReputationScoreAndLevel(int var1, Connection var2) throws RemoteException;

   UserReputationScoreAndLevelData getReputationScoreAndLevel(int var1, Connection var2, boolean var3) throws RemoteException;

   UserReputationScoreAndLevelData getReputationScoreAndLevel(boolean var1, int var2, boolean var3) throws RemoteException;

   Map getReputationScoreAndLevelForUsers(List var1, Connection var2) throws RemoteException;

   void createLookout(String var1, String var2) throws RemoteException;

   double getPriceInUserCurrency(double var1, String var3, String var4) throws FusionEJBException, RemoteException;

   UserMigboProfileData.AdminEnum getAdminLabel(String var1) throws FusionEJBException, RemoteException;

   UserMigboProfileData.MerchantEnum getMerchantLabel(String var1) throws FusionEJBException, RemoteException;

   MerchantDetailsData getBasicMerchantDetails(String var1) throws FusionEJBException, RemoteException;

   boolean isMerchantMentor(String var1) throws FusionEJBException, RemoteException;

   double getUserReferralSuccessRate(String var1, boolean var2) throws FusionEJBException, RemoteException;

   boolean isUserInMigboAccessList(int var1, int var2, int var3) throws FusionEJBException, RemoteException;

   boolean isUserLevelAllowedMigboAccess(int var1, int var2) throws FusionEJBException, RemoteException;

   boolean addUserEmailAddress(int var1, String var2, UserEmailAddressData.UserEmailAddressTypeEnum var3) throws FusionEJBException, RemoteException;

   boolean removeUserEmailAddressByType(int var1, UserEmailAddressData.UserEmailAddressTypeEnum var2) throws FusionEJBException, RemoteException;

   boolean removeUserEmailAddress(int var1, String var2) throws FusionEJBException, RemoteException;

   boolean updateUserEmailAddress(int var1, String var2, String var3, UserEmailAddressData.UserEmailAddressTypeEnum var4) throws FusionEJBException, RemoteException;

   RegistrationTokenData verifyExternalEmailAddress(Integer var1, String var2) throws FusionEJBException, RemoteException;

   boolean isEmailRegistrationNotVerifiedForUsername(String var1) throws RemoteException;

   boolean isEmailRegistrationNotVerifiedForUsername(String var1, String var2) throws RemoteException;

   boolean isExternalEmailVerified(int var1, String var2) throws FusionEJBException, RemoteException;

   UserEmailAddressData getUserEmailAddressByType(int var1, UserEmailAddressData.UserEmailAddressTypeEnum var2) throws FusionEJBException, RemoteException;

   String getUsernameByEmailAddress(String var1, UserEmailAddressData.UserEmailAddressTypeEnum var2) throws FusionEJBException, RemoteException;

   void forgotUsernameViaEmailAddress(String var1) throws FusionEJBException, RemoteException;

   String[] getUsersInUserCategory(int var1) throws RemoteException;

   Map getUserCategoryNames(int var1) throws FusionEJBException, RemoteException;

   boolean blacklistUser(int var1) throws RemoteException;

   boolean removeUserFromBlacklist(int var1) throws RemoteException;

   boolean banUser(int var1) throws RemoteException;

   boolean suspendUser(int var1, int var2) throws RemoteException;

   boolean disconnectUser(String var1, String var2) throws RemoteException;

   int getChatroomsOwnedCount(int var1) throws RemoteException;

   int getGiftsReceivedCount(int var1) throws RemoteException;

   int getGroupsJoinedCount(int var1) throws RemoteException;

   int getPhotosUploadedCount(int var1) throws RemoteException;

   RegistrationContextData getRegistrationContextData(int var1) throws RemoteException;

   Map getTaggedUsers(int var1, int var2, int var3) throws FusionEJBException, RemoteException;

   Map getExpiringTaggedUsers(int var1, int var2) throws FusionEJBException, RemoteException;

   MerchantDetailsData getFullMerchantDetails(String var1) throws FusionEJBException, RemoteException;

   boolean setMerchantColorType(Integer var1, MerchantDetailsData.UserNameColorTypeEnum var2) throws FusionEJBException, RemoteException;

   void responseToAlert(int var1, String var2, int var3, InvitationResponseData.ResponseType var4) throws FusionEJBException, RemoteException;

   void addMutualFollowing(int var1, int var2) throws FusionEJBException, RemoteException;

   void addMutualFollowingAndTriggerMigAlerts(UserData var1, UserData var2) throws FusionEJBException, RemoteException;

   void addMutualFollowingAndTriggerMigAlertsToAllInviters(InvitationData var1, UserData var2) throws FusionEJBException, RemoteException;

   InvitationDetailsData getInvitationDetailsData(String var1, boolean var2, Date var3) throws FusionEJBException, RemoteException;

   InvitationDetailsData getInvitationDetailsDataForFBInvite(String var1, String var2, boolean var3, Date var4) throws FusionEJBException, RemoteException;

   InvitationDetailsData convertInvitationDataToInvitationDetailsData(InvitationData var1, boolean var2, Date var3) throws FusionEJBException, RemoteException;

   String getDisplayPicture(String var1) throws RemoteException;

   UserData getInviterForSignUp(int var1) throws RemoteException;
}
