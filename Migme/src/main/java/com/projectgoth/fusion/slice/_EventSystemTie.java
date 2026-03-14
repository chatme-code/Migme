package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;
import java.util.Map;

public class _EventSystemTie extends _EventSystemDisp implements TieBase {
   private _EventSystemOperations _ice_delegate;

   public _EventSystemTie() {
   }

   public _EventSystemTie(_EventSystemOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_EventSystemOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _EventSystemTie) ? false : this._ice_delegate.equals(((_EventSystemTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public void addedFriend(String username, String friend, Current __current) throws FusionException {
      this._ice_delegate.addedFriend(username, friend, __current);
   }

   public void createdPublicChatroom(String username, String chatroomName, Current __current) throws FusionException {
      this._ice_delegate.createdPublicChatroom(username, chatroomName, __current);
   }

   public void deleteUserEvents(String username, Current __current) throws FusionException {
      this._ice_delegate.deleteUserEvents(username, __current);
   }

   public void genericApplicationEvent(String username, String appId, String text, Map<String, String> customDeviceURL, Current __current) throws FusionException {
      this._ice_delegate.genericApplicationEvent(username, appId, text, customDeviceURL, __current);
   }

   public EventPrivacySettingIce getPublishingPrivacyMask(String username, Current __current) throws FusionException {
      return this._ice_delegate.getPublishingPrivacyMask(username, __current);
   }

   public EventPrivacySettingIce getReceivingPrivacyMask(String username, Current __current) throws FusionException {
      return this._ice_delegate.getReceivingPrivacyMask(username, __current);
   }

   public UserEventIce[] getUserEventsForUser(String username, Current __current) throws FusionException {
      return this._ice_delegate.getUserEventsForUser(username, __current);
   }

   public UserEventIce[] getUserEventsGeneratedByUser(String username, Current __current) throws FusionException {
      return this._ice_delegate.getUserEventsGeneratedByUser(username, __current);
   }

   public void giftShowerEvent(String username, String recipient, String giftName, int virtualGiftReceivedId, int totalRecipients, Current __current) {
      this._ice_delegate.giftShowerEvent(username, recipient, giftName, virtualGiftReceivedId, totalRecipients, __current);
   }

   public void groupAnnouncement(String username, int groupId, int groupAnnoucementId, Current __current) throws FusionException {
      this._ice_delegate.groupAnnouncement(username, groupId, groupAnnoucementId, __current);
   }

   public void groupDonation(String username, int groupId, Current __current) throws FusionException {
      this._ice_delegate.groupDonation(username, groupId, __current);
   }

   public void groupJoined(String username, int groupId, Current __current) throws FusionException {
      this._ice_delegate.groupJoined(username, groupId, __current);
   }

   public void madeGroupUserPost(String username, int userPostId, int groupId, Current __current) throws FusionException {
      this._ice_delegate.madeGroupUserPost(username, userPostId, groupId, __current);
   }

   public void madePhotoPublic(String username, int scrapbookid, String title, Current __current) throws FusionException {
      this._ice_delegate.madePhotoPublic(username, scrapbookid, title, __current);
   }

   public void purchasedVirtualGoods(String username, byte itemType, int itemid, String itemName, Current __current) throws FusionException {
      this._ice_delegate.purchasedVirtualGoods(username, itemType, itemid, itemName, __current);
   }

   public void setProfileStatus(String username, String status, Current __current) throws FusionException {
      this._ice_delegate.setProfileStatus(username, status, __current);
   }

   public void setPublishingPrivacyMask(String username, EventPrivacySettingIce mask, Current __current) throws FusionException {
      this._ice_delegate.setPublishingPrivacyMask(username, mask, __current);
   }

   public void setReceivingPrivacyMask(String username, EventPrivacySettingIce mask, Current __current) throws FusionException {
      this._ice_delegate.setReceivingPrivacyMask(username, mask, __current);
   }

   public void streamEventsToLoggingInUser(String username, ConnectionPrx connectionProxy, Current __current) throws FusionException {
      this._ice_delegate.streamEventsToLoggingInUser(username, connectionProxy, __current);
   }

   public void updateAllowList(String username, String[] watchers, Current __current) throws FusionException {
      this._ice_delegate.updateAllowList(username, watchers, __current);
   }

   public void updatedProfile(String username, Current __current) throws FusionException {
      this._ice_delegate.updatedProfile(username, __current);
   }

   public void userWallPost(String username, String wallOwnerUsername, String postContent, int userWallPostId, Current __current) throws FusionException {
      this._ice_delegate.userWallPost(username, wallOwnerUsername, postContent, userWallPostId, __current);
   }

   public void virtualGift(String username, String recipient, String giftName, int virtualGiftReceivedId, Current __current) {
      this._ice_delegate.virtualGift(username, recipient, giftName, virtualGiftReceivedId, __current);
   }
}
