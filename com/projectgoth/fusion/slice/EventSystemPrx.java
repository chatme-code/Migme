package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface EventSystemPrx extends ObjectPrx {
   UserEventIce[] getUserEventsForUser(String var1) throws FusionException;

   UserEventIce[] getUserEventsForUser(String var1, Map<String, String> var2) throws FusionException;

   UserEventIce[] getUserEventsGeneratedByUser(String var1) throws FusionException;

   UserEventIce[] getUserEventsGeneratedByUser(String var1, Map<String, String> var2) throws FusionException;

   void updateAllowList(String var1, String[] var2) throws FusionException;

   void updateAllowList(String var1, String[] var2, Map<String, String> var3) throws FusionException;

   void streamEventsToLoggingInUser(String var1, ConnectionPrx var2) throws FusionException;

   void streamEventsToLoggingInUser(String var1, ConnectionPrx var2, Map<String, String> var3) throws FusionException;

   void deleteUserEvents(String var1) throws FusionException;

   void deleteUserEvents(String var1, Map<String, String> var2) throws FusionException;

   void madePhotoPublic(String var1, int var2, String var3) throws FusionException;

   void madePhotoPublic(String var1, int var2, String var3, Map<String, String> var4) throws FusionException;

   void setProfileStatus(String var1, String var2) throws FusionException;

   void setProfileStatus(String var1, String var2, Map<String, String> var3) throws FusionException;

   void createdPublicChatroom(String var1, String var2) throws FusionException;

   void createdPublicChatroom(String var1, String var2, Map<String, String> var3) throws FusionException;

   void addedFriend(String var1, String var2) throws FusionException;

   void addedFriend(String var1, String var2, Map<String, String> var3) throws FusionException;

   void updatedProfile(String var1) throws FusionException;

   void updatedProfile(String var1, Map<String, String> var2) throws FusionException;

   void purchasedVirtualGoods(String var1, byte var2, int var3, String var4) throws FusionException;

   void purchasedVirtualGoods(String var1, byte var2, int var3, String var4, Map<String, String> var5) throws FusionException;

   void virtualGift(String var1, String var2, String var3, int var4);

   void virtualGift(String var1, String var2, String var3, int var4, Map<String, String> var5);

   void userWallPost(String var1, String var2, String var3, int var4) throws FusionException;

   void userWallPost(String var1, String var2, String var3, int var4, Map<String, String> var5) throws FusionException;

   void groupDonation(String var1, int var2) throws FusionException;

   void groupDonation(String var1, int var2, Map<String, String> var3) throws FusionException;

   void groupJoined(String var1, int var2) throws FusionException;

   void groupJoined(String var1, int var2, Map<String, String> var3) throws FusionException;

   void groupAnnouncement(String var1, int var2, int var3) throws FusionException;

   void groupAnnouncement(String var1, int var2, int var3, Map<String, String> var4) throws FusionException;

   void madeGroupUserPost(String var1, int var2, int var3) throws FusionException;

   void madeGroupUserPost(String var1, int var2, int var3, Map<String, String> var4) throws FusionException;

   void genericApplicationEvent(String var1, String var2, String var3, Map<String, String> var4) throws FusionException;

   void genericApplicationEvent(String var1, String var2, String var3, Map<String, String> var4, Map<String, String> var5) throws FusionException;

   void giftShowerEvent(String var1, String var2, String var3, int var4, int var5);

   void giftShowerEvent(String var1, String var2, String var3, int var4, int var5, Map<String, String> var6);

   EventPrivacySettingIce getPublishingPrivacyMask(String var1) throws FusionException;

   EventPrivacySettingIce getPublishingPrivacyMask(String var1, Map<String, String> var2) throws FusionException;

   void setPublishingPrivacyMask(String var1, EventPrivacySettingIce var2) throws FusionException;

   void setPublishingPrivacyMask(String var1, EventPrivacySettingIce var2, Map<String, String> var3) throws FusionException;

   EventPrivacySettingIce getReceivingPrivacyMask(String var1) throws FusionException;

   EventPrivacySettingIce getReceivingPrivacyMask(String var1, Map<String, String> var2) throws FusionException;

   void setReceivingPrivacyMask(String var1, EventPrivacySettingIce var2) throws FusionException;

   void setReceivingPrivacyMask(String var1, EventPrivacySettingIce var2, Map<String, String> var3) throws FusionException;
}
