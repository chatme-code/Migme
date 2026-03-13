package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _EventSystemDel extends _ObjectDel {
   UserEventIce[] getUserEventsForUser(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   UserEventIce[] getUserEventsGeneratedByUser(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void updateAllowList(String var1, String[] var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void streamEventsToLoggingInUser(String var1, ConnectionPrx var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void deleteUserEvents(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void madePhotoPublic(String var1, int var2, String var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void setProfileStatus(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void createdPublicChatroom(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void addedFriend(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void updatedProfile(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void purchasedVirtualGoods(String var1, byte var2, int var3, String var4, Map<String, String> var5) throws LocalExceptionWrapper, FusionException;

   void virtualGift(String var1, String var2, String var3, int var4, Map<String, String> var5) throws LocalExceptionWrapper;

   void userWallPost(String var1, String var2, String var3, int var4, Map<String, String> var5) throws LocalExceptionWrapper, FusionException;

   void groupDonation(String var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void groupJoined(String var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void groupAnnouncement(String var1, int var2, int var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void madeGroupUserPost(String var1, int var2, int var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void genericApplicationEvent(String var1, String var2, String var3, Map<String, String> var4, Map<String, String> var5) throws LocalExceptionWrapper, FusionException;

   void giftShowerEvent(String var1, String var2, String var3, int var4, int var5, Map<String, String> var6) throws LocalExceptionWrapper;

   EventPrivacySettingIce getPublishingPrivacyMask(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void setPublishingPrivacyMask(String var1, EventPrivacySettingIce var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   EventPrivacySettingIce getReceivingPrivacyMask(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void setReceivingPrivacyMask(String var1, EventPrivacySettingIce var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;
}
