package com.projectgoth.fusion.interfaces;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import javax.ejb.EJBObject;

public interface Web extends EJBObject {
   Vector getCountries() throws RemoteException;

   Vector getCurrencies() throws RemoteException;

   Vector getRateGrid() throws RemoteException;

   Hashtable getHistoryEntry(long var1, String var3) throws RemoteException;

   String setEmailAlert(String var1, boolean var2) throws RemoteException;

   String setEmailAlertSent(String var1, boolean var2) throws RemoteException;

   String sendSMS(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8) throws RemoteException;

   String sendEmail(String var1, String var2, String var3, String var4, String var5) throws RemoteException;

   String sendEmailFromNoReply(String var1, String var2, String var3) throws RemoteException;

   String makeCall(String[] var1, String[] var2) throws RemoteException;

   Hashtable evaluateCall(String[] var1, String[] var2) throws RemoteException;

   Vector getContactList(String var1) throws RemoteException;

   Vector getContactListEmailActivated(String var1, boolean var2) throws RemoteException;

   Vector getAccountEntries(String var1, int var2, int var3) throws RemoteException;

   Vector getMoneyTransferEntries(String var1, int var2, int var3) throws RemoteException;

   Vector getSMSHistory(String var1, int var2, int var3) throws RemoteException;

   Vector getCallHistory(String var1, int var2, int var3) throws RemoteException;

   Hashtable registerUser(String[] var1, String[] var2, String[] var3, String[] var4, String var5, String var6, String var7, String var8) throws RemoteException;

   Hashtable registerUser(String[] var1, String[] var2, String[] var3, String[] var4, String var5, String var6, String var7, String var8, String var9) throws RemoteException;

   Hashtable registerUser(String[] var1, String[] var2, String[] var3, String[] var4, String var5, String var6, String var7, String var8, String var9, String var10) throws RemoteException;

   Hashtable registerUserMerchant(String[] var1, String[] var2, String var3, String var4, String var5, String var6) throws RemoteException;

   Hashtable loadUserDetails(String var1) throws RemoteException;

   Hashtable loadUserDetailsFromMobilePhone(String var1) throws RemoteException;

   Hashtable loadUserProfile(String var1, String var2) throws RemoteException;

   Hashtable getAccountBalance(String var1) throws RemoteException;

   String getMerchantTagFromUsername(String var1) throws RemoteException;

   Vector searchUserProfiles(String var1, String var2, String var3, String var4, String var5, String var6, String var7, int var8, int var9, String var10, String var11) throws RemoteException;

   Vector getUsersWhoViewed(String var1) throws RemoteException;

   String updateUser(String[] var1, String[] var2, String[] var3, String[] var4) throws RemoteException;

   String updateUserDisplayPicture(String var1, String var2) throws RemoteException;

   String updateUserDetails(String[] var1, String[] var2) throws RemoteException;

   String updateUserProfile(String[] var1, String[] var2) throws RemoteException;

   String updateUserStatusMessage(int var1, String var2, String var3) throws RemoteException;

   String referFriendViaGame(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9) throws RemoteException;

   String referFriend(String var1, String var2, String var3, String var4, String var5, String var6, String var7) throws RemoteException;

   Hashtable transferCredit(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8) throws RemoteException;

   String cleanAndValidatePhoneNumber(String var1) throws RemoteException;

   Vector getScrapbook(String var1, int var2, int var3) throws RemoteException;

   Vector getScrapbook(String var1, int var2, int var3, boolean var4) throws RemoteException;

   Vector getGallery(String var1, String var2, int var3, int var4) throws RemoteException;

   boolean reportPhotoAbuse(String var1, String var2, int var3) throws RemoteException;

   Hashtable getWall(String var1, int var2, int var3, boolean var4) throws RemoteException;

   Hashtable getPhoto(int var1, String var2, String var3) throws RemoteException;

   Hashtable getScrapbookEntry(int var1) throws RemoteException;

   String updateScrapbookEntry(String[] var1, String[] var2) throws RemoteException;

   Hashtable getFile(String var1) throws RemoteException;

   String newFileID() throws RemoteException;

   String saveFileToScrapbook(String[] var1, String[] var2) throws RemoteException;

   String saveExistingFileToScrapbooks(String var1, String[] var2, String var3, String var4) throws RemoteException;

   String publishFileFromScrapbook(String var1, int var2, String var3, boolean var4) throws RemoteException;

   String unpublishFileFromScrapbook(String var1, int var2) throws RemoteException;

   String deleteFileFromScrapbook(String var1, int var2) throws RemoteException;

   Hashtable getContact(int var1) throws RemoteException;

   Hashtable getContact(String var1, String var2) throws RemoteException;

   Hashtable addContact(int var1, String[] var2, String[] var3) throws RemoteException;

   String updateContact(int var1, String[] var2, String[] var3) throws RemoteException;

   String blockContact(int var1, String var2, String var3) throws RemoteException;

   Hashtable getContactGroup(int var1) throws RemoteException;

   Hashtable addGroup(int var1, String[] var2, String[] var3) throws RemoteException;

   String updateGroup(int var1, String[] var2, String[] var3) throws RemoteException;

   Hashtable creditCardPayment(String[] var1, String[] var2, String var3, String var4, String var5, String var6) throws RemoteException;

   String sendTTNotification(String[] var1, String[] var2) throws RemoteException;

   Hashtable redeemVoucher(String var1, String var2, String var3, String var4, String var5, String var6) throws RemoteException;

   Hashtable redeemBlueLabelVoucher(String var1, String var2, String var3) throws RemoteException;

   Hashtable getAffiliateOverview(String var1, String var2) throws RemoteException;

   Vector getAffiliateRecentActivities(String var1, String var2) throws RemoteException;

   Hashtable getAffiliateMoreStatistics(String var1, String var2) throws RemoteException;

   Vector getVoucherBatches(String var1, int var2, int var3, int var4) throws RemoteException;

   Vector getAllVoucherBatches(String var1) throws RemoteException;

   Vector getVouchers(String var1, int var2, int var3, int var4, int var5, String var6, String var7) throws RemoteException;

   String exportVoucherBatchToCSV(String var1, int var2, int var3) throws RemoteException;

   String createVoucherBatch(String var1, String var2, String var3, int var4, String var5, boolean var6, String var7, String var8, String var9, String var10) throws RemoteException;

   Hashtable searchForVoucher(String var1, String var2) throws RemoteException;

   String cancelVoucher(String var1, int var2, String var3, String var4, String var5, String var6) throws RemoteException;

   String cancelVoucherBatch(String var1, int var2, String var3, String var4, String var5, String var6) throws RemoteException;

   String changeActiveVoucherToInactive(String var1, int var2) throws RemoteException;

   String changeInactiveVoucherToActive(String var1, int var2) throws RemoteException;

   String changeActiveVouchersInBatchToInactive(String var1, int var2) throws RemoteException;

   String changeInactiveVouchersInBatchToActive(String var1, int var2) throws RemoteException;

   String updateVoucherBatchNotes(String var1, int var2, String var3) throws RemoteException;

   int[] creditCardPaymentFromMidlet(int[] var1, String var2, String var3, String var4, String var5) throws RemoteException;

   boolean processSMSDeliveryReport(String var1, String var2, int var3, String var4, String var5, String var6, String var7) throws RemoteException;

   String processMobileOrignatedSMS(String var1, String var2, String var3, boolean var4, String var5) throws RemoteException;

   Vector getHandsetVendors() throws RemoteException;

   Vector getHandsetVendorPrefixes() throws RemoteException;

   Vector getHandsetDetails(String var1) throws RemoteException;

   Vector getDefaultHandsetDetails() throws RemoteException;

   String forgotPasswordWithMobileNumber(String var1, String var2, String var3, String var4) throws RemoteException;

   String forgotPasswordWithEmail(String var1, String var2, String var3, String var4) throws RemoteException;

   String changePassword(String var1, String var2, String var3) throws RemoteException;

   Hashtable getCountryFromIPNumber(String var1) throws RemoteException;

   boolean consoleOut(String var1) throws RemoteException;

   String loginFailed(String var1) throws RemoteException;

   String loginSucceeded(String var1) throws RemoteException;

   String loginSucceeded(String var1, String var2, String var3, String var4) throws RemoteException;

   String getBankTransferProductID(int var1) throws RemoteException;

   Hashtable bankTransfer(String var1, int var2, int var3, String var4, String var5, float var6, String var7) throws RemoteException;

   Hashtable bankTransfer(String var1, int var2, int var3, String var4, String var5, String var6, String var7, float var8, String var9) throws RemoteException;

   String registerMerchant(String[] var1, String[] var2) throws RemoteException;

   Hashtable loadMerchant(String var1) throws RemoteException;

   String isBuzzPossible(String var1, int var2) throws RemoteException;

   String sendBuzz(String var1, int var2, String var3, String var4, String var5, String var6, String var7) throws RemoteException;

   String createLookout(String var1, String var2) throws RemoteException;

   String isLookoutPossible(String var1, String var2) throws RemoteException;

   int lookoutExists(String var1, String var2) throws RemoteException;

   String removeLookout(String var1, String var2) throws RemoteException;

   Vector getLookouts(String var1) throws RemoteException;

   String setAllowBuzz(String var1, boolean var2) throws RemoteException;

   Hashtable getApplicableDiscountTier(int var1, String var2, double var3) throws RemoteException;

   Vector getDiscountTiers(int var1, String var2) throws RemoteException;

   double[] getCreditCardPaymentAmounts(String var1) throws RemoteException;

   double[] getCreditCardPaymentAmounts(String var1, boolean var2, String var3) throws RemoteException;

   Vector getPossibleTransferRecipients(String var1) throws RemoteException;

   Vector getResellerStates(int var1) throws RemoteException;

   Vector getResellersInState(int var1, String var2) throws RemoteException;

   Vector getResellers(int var1) throws RemoteException;

   Vector getFixedCallRates(int var1) throws RemoteException;

   Vector getEmoticonPacks(String var1, boolean var2, int var3, int var4) throws RemoteException;

   Vector getEmoticonsInPack(int var1) throws RemoteException;

   Vector getEmoticonDetailsFromHotkeys(String var1) throws RemoteException;

   Hashtable getEmoticonPack(String var1, int var2) throws RemoteException;

   String buyEmoticonPack(String var1, int var2, String var3, String var4, String var5, String var6) throws RemoteException;

   Vector getAllEmoticons() throws RemoteException;

   Vector getTopWallpaper(int var1) throws RemoteException;

   Vector getTopRingtones(int var1) throws RemoteException;

   Vector getMobileContentCategories(String var1, int var2, int var3, int var4, int var5) throws RemoteException;

   Vector getMobileContent(String var1, int var2, int var3, int var4, int var5) throws RemoteException;

   Hashtable getMobileContentItem(String var1, int var2, boolean var3) throws RemoteException;

   String buyMobileContentItem(String var1, int var2, String var3, String var4, String var5, String var6) throws RemoteException;

   String getMobileContentDownloadURL(String var1, int var2) throws RemoteException;

   boolean processILoopAPICall(String var1, String var2, String var3, String var4, String var5, String var6, String var7) throws RemoteException;

   boolean processOplayoAPICall(int var1, int var2, int var3, int var4, int var5) throws RemoteException;

   Vector getPurchasedContent(String var1, int var2, int var3) throws RemoteException;

   Hashtable getSmallProfile(String var1, String var2) throws RemoteException;

   Hashtable getFriends(String var1, String var2, String var3, int var4, int var5) throws RemoteException;

   Hashtable getCallRates(int var1, int var2, boolean var3, String var4) throws RemoteException;

   int getUserProfilePrivacySetting(String var1) throws RemoteException;

   boolean setUserProfilePrivacySetting(String var1, int var2) throws RemoteException;

   int getUserBlockedListCount(String var1) throws RemoteException;

   Hashtable getUserBlockedList(String var1, String var2, int var3, int var4, boolean var5, boolean var6) throws RemoteException;

   boolean isContact(String var1, String var2) throws RemoteException;

   boolean isContactFriend(String var1, String var2) throws RemoteException;

   Hashtable getUserContactDetails(String var1, String var2) throws RemoteException;

   boolean unblockUser(String var1, String var2) throws RemoteException;

   boolean unblockAllUsers(String var1) throws RemoteException;

   Hashtable getPagingUserEventGeneratedByUser(String var1, int var2, int var3) throws RemoteException;

   Hashtable getPagingUserEvents(String var1, int var2, int var3) throws RemoteException;

   boolean editPhotoCaption(String var1, int var2, String var3) throws RemoteException;

   Hashtable getThemes(String var1, int var2, int var3) throws RemoteException;

   boolean changeTheme(String var1, int var2) throws RemoteException;

   Vector getLocationsWithMerchantsInCountry(int var1) throws RemoteException;

   Vector getLocationsWithMerchantsInParentLocation(int var1) throws RemoteException;

   Vector getCountriesWithMerchants() throws RemoteException;

   Vector getLocationPath(int var1) throws RemoteException;

   Vector getMerchantsInLocation(int var1) throws RemoteException;

   Hashtable getMerchantLocation(int var1) throws RemoteException;

   boolean countryHasMerchantLocations(int var1) throws RemoteException;

   String[] getMerchantsUserMayPurchaseFrom(String var1) throws RemoteException;

   Hashtable getBuzzCost(String var1, String var2) throws RemoteException;

   String getSMSCost(String var1, String var2) throws RemoteException;

   Hashtable getSMSCostTable(String var1, int var2, int var3) throws RemoteException;

   Hashtable getLocalSMSCost(String var1, int var2) throws RemoteException;

   Hashtable getLookoutCost(String var1) throws RemoteException;

   Hashtable getGroupSMSNotificationCost(String var1) throws RemoteException;

   Hashtable getKickCost(String var1) throws RemoteException;

   Hashtable getEmailAlertCost(String var1) throws RemoteException;

   Hashtable getTransactionSummaryByMonth(String var1, int var2, int var3, String var4) throws RemoteException;

   Hashtable getTransactionSummaryForCustomerByDate(String var1, String var2, Date var3, Date var4, String var5) throws RemoteException;

   Hashtable getTransactionSummaryByMonthForCustomer(String var1, String var2, int var3, int var4, String var5) throws RemoteException;

   Hashtable getTransactionSummaryForCustomer(String var1, String var2, String var3) throws RemoteException;

   Hashtable getTransactions(String var1, int var2, int var3, String var4) throws RemoteException;

   Hashtable getTransactionsSinceDate(String var1, int var2, int var3, int var4, String var5) throws RemoteException;

   Hashtable getTransactionsSinceDateAndType(String var1, int var2, int var3, int var4, int var5) throws RemoteException;

   Hashtable getTransactionsByDate(String var1, Date var2, Date var3, int var4, int var5, String var6) throws RemoteException;

   int getUserReferralCountByMonth(String var1, int var2, int var3) throws RemoteException;

   int getUserReferralCount(String var1) throws RemoteException;

   Hashtable getUserReferral(String var1, Date var2, Date var3, int var4, int var5) throws RemoteException;

   Hashtable getUserReferralByMonth(String var1, int var2, int var3, int var4, int var5) throws RemoteException;

   Hashtable getUserReferral(String var1, int var2, int var3) throws RemoteException;

   String getPreviousMobileNumber(String var1) throws RemoteException;

   String changeMobileNumber(String var1, String var2, String var3, String var4, String var5, String var6) throws RemoteException;

   String cancelMobileNumberChange(String var1, String var2, String var3, String var4, String var5) throws RemoteException;

   Hashtable getUserVirtualGift(String var1, String var2, int var3) throws RemoteException;

   boolean removeUserVirtualGift(String var1, int var2) throws RemoteException;

   Hashtable getVirtualGiftForUser(String var1, String var2, int var3, int var4) throws RemoteException;

   Hashtable getVirtualGiftReceivedSummary(String var1, String var2) throws RemoteException;

   Hashtable getVirtualGift(String var1, int var2) throws RemoteException;

   Hashtable getVirtualGiftContent(String var1, int var2, int var3, int var4) throws RemoteException;

   String buyVirtualGift(String var1, String var2, int var3, int var4, boolean var5, String var6, String var7, String var8, String var9, String var10, String var11) throws RemoteException;

   String buyAvatarItem(int var1, int var2, int var3, String var4, String var5, String var6, String var7) throws RemoteException;

   Vector getExternalClientDownloadDetail(String var1) throws RemoteException;

   float getUnredeemedVoucherValue(String var1) throws RemoteException;

   Hashtable getMerchantCustomersAndFriends(String var1, int var2, int var3) throws RemoteException;

   Hashtable getMerchantCustomers(String var1, int var2, int var3) throws RemoteException;

   Hashtable getMerchantCustomerTransactions(String var1, String var2, int var3, int var4) throws RemoteException;

   String getMostRecentlyVisitedChatroom(String var1, String var2) throws RemoteException;

   Hashtable getGroup(int var1) throws RemoteException;

   Hashtable getGroupAnnouncements(int var1, int var2, int var3) throws RemoteException;

   Hashtable getGroupAnnouncement(int var1) throws RemoteException;

   Hashtable createGroupAnnouncement(String var1, int var2, String var3, String var4, String var5) throws RemoteException;

   Hashtable updateGroupAnnouncement(int var1, String var2, int var3, String var4, String var5) throws RemoteException;

   Hashtable getGroupChatroomCategoriesAndStadiums(int var1, int var2, int var3, int var4) throws RemoteException;

   Hashtable getGroupChatrooms(int var1, int var2, int var3, int var4) throws RemoteException;

   Hashtable getGroupDonators(int var1, int var2, int var3) throws RemoteException;

   String joinGroup(String var1, int var2, int var3, String var4, String var5, String var6, String var7, boolean var8, boolean var9, boolean var10, boolean var11, boolean var12, boolean var13) throws RemoteException;

   String leaveGroup(String var1, int var2) throws RemoteException;

   String declineAllGroupInvitations(String var1) throws RemoteException;

   String declineGroupInvitation(String var1, int var2) throws RemoteException;

   String setGroupMemberOptions(String var1, int var2, int var3, boolean var4, boolean var5, boolean var6, boolean var7, boolean var8, boolean var9) throws RemoteException;

   Hashtable makeGroupDonation(String var1, int var2, String var3, boolean var4) throws RemoteException;

   String inviteMobilePhoneToGroup(String var1, String var2, String var3, int var4, String var5, String var6, String var7, String var8, String var9) throws RemoteException;

   String inviteUserToGroup(String var1, String var2, int var3) throws RemoteException;

   String updateStadiumDescription(String var1, String var2) throws RemoteException;

   Hashtable getLocations(int var1, int var2, int var3, int var4) throws RemoteException;

   Hashtable getLocation(int var1) throws RemoteException;

   Hashtable getContactList(String var1, int var2, int var3, int var4) throws RemoteException;

   Vector getGroupModules(int var1) throws RemoteException;

   Hashtable getGroupEvents(int var1, int var2, int var3) throws RemoteException;

   Hashtable getGroupModule(int var1) throws RemoteException;

   Hashtable getGroupPost(int var1) throws RemoteException;

   Hashtable getGroupPosts(int var1, int var2, int var3) throws RemoteException;

   Hashtable createGroupPost(String var1, int var2, String var3, String var4) throws RemoteException;

   Hashtable createGroupPost(String var1, int var2, String var3, String var4, int var5) throws RemoteException;

   String updateGroupPost(int var1, String var2, String var3, String var4) throws RemoteException;

   String publishGroupPost(int var1, String var2) throws RemoteException;

   String deleteGroupPost(int var1, String var2) throws RemoteException;

   Hashtable createGroupModule(String var1, int var2, String var3, int var4) throws RemoteException;

   String updateGroupModule(int var1, String var2, String var3, int var4) throws RemoteException;

   String deleteGroupModule(int var1, String var2) throws RemoteException;

   Hashtable createGroupUserPost(String var1, int var2, String var3, int var4) throws RemoteException;

   Hashtable getGroupUserPosts(int var1, int var2, int var3) throws RemoteException;

   Hashtable getUserPost(int var1) throws RemoteException;

   Hashtable getUserPostReplies(int var1, int var2, int var3) throws RemoteException;

   String deleteGroupUserPost(int var1, int var2, String var3) throws RemoteException;

   String createChatroom(String var1, String var2, String var3, String var4, String var5, boolean var6) throws RemoteException;

   String updateRoomDetails(String var1, String var2, String var3, String var4) throws RemoteException;

   String updateRoomKickingRule(String var1, String var2, boolean var3) throws RemoteException;

   String addRoomModerator(String var1, String var2, String var3) throws RemoteException;

   String removeRoomModerator(String var1, String var2, String var3) throws RemoteException;

   String banUserFromRoom(String var1, String var2, String var3) throws RemoteException;

   String unbanUserFromRoom(String var1, String var2, String var3) throws RemoteException;

   String sendChangeRoomOwnerEmail(String var1, String var2, String var3) throws RemoteException;

   String changeRoomOwner(String var1, String var2, String var3) throws RemoteException;

   Vector getRoomModerators(String var1, String var2) throws RemoteException;

   Hashtable getRoomBannedUsers(String var1, String var2, int var3, int var4) throws RemoteException;

   String updateRoomKeywords(String var1, String var2, String var3, int var4) throws RemoteException;

   Hashtable getChatroom(String var1) throws RemoteException;

   Hashtable getUserOwnedChatrooms(String var1, int var2, int var3) throws RemoteException;

   boolean isModeratorOfChatRoom(String var1, String var2) throws RemoteException;

   Hashtable searchChatroom(int var1, String var2, String var3, boolean var4, boolean var5, int var6, int var7) throws RemoteException;

   Hashtable getPendingContact(String var1, int var2) throws RemoteException;

   int getPendingContactCount(String var1) throws RemoteException;

   String acceptPendingContact(int var1, String var2, String var3, int var4, boolean var5) throws RemoteException;

   Vector getContactGroupList(String var1) throws RemoteException;

   String rejectContactInvitation(int var1, String var2, String var3) throws RemoteException;

   int getAnonymousCallSetting(String var1) throws RemoteException;

   boolean updateAnonymousCallSetting(String var1, int var2) throws RemoteException;

   int getMessageSetting(String var1) throws RemoteException;

   boolean updateMessageSetting(String var1, int var2) throws RemoteException;

   String addIMDetail(String var1, int var2, String var3, String var4) throws RemoteException;

   Hashtable getIMContacts(String var1, int var2, int var3) throws RemoteException;

   String inviteIMContact(String var1, int var2, String var3) throws RemoteException;

   boolean isIndosatIP(String var1) throws RemoteException;

   Vector getActiveSubscriptions(String var1) throws RemoteException;

   String cancelSubscription(String var1, int var2) throws RemoteException;

   Hashtable getGroupHasExclusiveContent(int var1) throws RemoteException;

   Vector getLanguages() throws RemoteException;

   Hashtable getUserLevel(String var1) throws RemoteException;

   Hashtable getBotList(int var1, int var2) throws RemoteException;

   Hashtable chargeUserForGameItem(String var1, String var2, String var3, double var4, String var6, String var7, String var8, String var9, String var10) throws RemoteException;

   Hashtable giveGameReward(String var1, String var2, String var3, double var4, double var6, String var8, String var9, String var10, String var11, String var12) throws RemoteException;

   Hashtable thirdPartyAPIDebit(String var1, String var2, String var3, double var4, String var6, String var7, String var8, String var9, String var10) throws RemoteException;

   String activateAccount(String var1, String var2, String var3, String var4, String var5, String var6) throws RemoteException;

   boolean updateEmoticonPackStatus(String var1, int var2, int var3) throws RemoteException;

   String approveCreditCardPayment(String var1, int var2, String var3, String var4, String var5, String var6) throws RemoteException;

   String rejectCreditCardPayment(String var1, int var2, String var3, String var4, String var5, String var6, String var7) throws RemoteException;

   String creditUserAndSendSMS(String var1, double var2, double var4, String var6, String var7, String var8, String var9, String var10) throws RemoteException;

   Vector getCreditCardTransactions(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, int var9) throws Exception, RemoteException;

   /** @deprecated */
   String disconnectUserIce(String var1, String var2) throws RemoteException;

   String updateUserDetailsIce(String var1) throws RemoteException;

   String deregisterChatroomIce(String var1) throws RemoteException;

   String resetMerchantPin(String var1) throws RemoteException;

   boolean resendVerificationCode(String var1, String var2, String var3, String var4, String var5, String var6) throws RemoteException;

   String blacklistUsersFromGroup(int var1, String var2, String[] var3) throws RemoteException;

   String removeUsersFromGroupBlacklist(int var1, String var2, String[] var3) throws RemoteException;

   String sendApplicationEvent(String var1, String var2, String var3) throws RemoteException;

   Hashtable getPaintWarsStats(String var1) throws RemoteException;

   String getPaintWarsUserIdenticonIndex(String var1) throws RemoteException;

   String executePaintWarsPaint(String var1, String var2) throws RemoteException;

   String executePaintWarsClean(String var1, String var2) throws RemoteException;

   String isPaintWarsClean(String var1) throws RemoteException;

   String hadPaintWarsInteraction(String var1, String var2) throws RemoteException;

   String hasPaintWarsFreePaintCredits(String var1) throws RemoteException;

   String hasPaintWarsFreeCleanCredits(String var1) throws RemoteException;

   String getPaintWarsPriceOfPaint() throws RemoteException;

   String getPaintWarsPriceOfClean() throws RemoteException;

   String getPaintWarsPriceOfIdenticon() throws RemoteException;

   Vector getPaintWarsUserPaint(String var1) throws RemoteException;

   String buyPaintWarsIdenticon(String var1) throws RemoteException;

   Vector getPaintWarsStatsDetails(String var1, int var2, int var3, int var4) throws RemoteException;

   Vector getPaintWarsSpecialItems() throws RemoteException;

   Vector getPaintWarsUserInventory(String var1) throws RemoteException;

   String buyPaintWarsSpecialItem(String var1, int var2) throws RemoteException;

   String usePaintWarsSpecialItem(String var1, int var2) throws RemoteException;

   String isPaintWarsPaintProof(String var1) throws RemoteException;

   String hasPaintWarsDualPaint(String var1) throws RemoteException;

   String hasPaintWarsStealthPaint(String var1) throws RemoteException;

   String giveUnfundedCredits(String var1, String var2, String var3, double var4, String var6, String var7, String var8) throws RemoteException;
}
