package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.LocalException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDel;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import IceInternal.LocalExceptionWrapper;
import IceInternal.OutgoingAsync;
import java.util.Map;

public final class EventSystemPrxHelper extends ObjectPrxHelperBase implements EventSystemPrx {
   public void addedFriend(String username, String friend) throws FusionException {
      this.addedFriend(username, friend, (Map)null, false);
   }

   public void addedFriend(String username, String friend, Map<String, String> __ctx) throws FusionException {
      this.addedFriend(username, friend, __ctx, true);
   }

   private void addedFriend(String username, String friend, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("addedFriend");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.addedFriend(username, friend, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void createdPublicChatroom(String username, String chatroomName) throws FusionException {
      this.createdPublicChatroom(username, chatroomName, (Map)null, false);
   }

   public void createdPublicChatroom(String username, String chatroomName, Map<String, String> __ctx) throws FusionException {
      this.createdPublicChatroom(username, chatroomName, __ctx, true);
   }

   private void createdPublicChatroom(String username, String chatroomName, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("createdPublicChatroom");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.createdPublicChatroom(username, chatroomName, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void deleteUserEvents(String username) throws FusionException {
      this.deleteUserEvents(username, (Map)null, false);
   }

   public void deleteUserEvents(String username, Map<String, String> __ctx) throws FusionException {
      this.deleteUserEvents(username, __ctx, true);
   }

   private void deleteUserEvents(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("deleteUserEvents");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.deleteUserEvents(username, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void genericApplicationEvent(String username, String appId, String text, Map<String, String> customDeviceURL) throws FusionException {
      this.genericApplicationEvent(username, appId, text, customDeviceURL, (Map)null, false);
   }

   public void genericApplicationEvent(String username, String appId, String text, Map<String, String> customDeviceURL, Map<String, String> __ctx) throws FusionException {
      this.genericApplicationEvent(username, appId, text, customDeviceURL, __ctx, true);
   }

   private void genericApplicationEvent(String username, String appId, String text, Map<String, String> customDeviceURL, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("genericApplicationEvent");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.genericApplicationEvent(username, appId, text, customDeviceURL, __ctx);
            return;
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public EventPrivacySettingIce getPublishingPrivacyMask(String username) throws FusionException {
      return this.getPublishingPrivacyMask(username, (Map)null, false);
   }

   public EventPrivacySettingIce getPublishingPrivacyMask(String username, Map<String, String> __ctx) throws FusionException {
      return this.getPublishingPrivacyMask(username, __ctx, true);
   }

   private EventPrivacySettingIce getPublishingPrivacyMask(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getPublishingPrivacyMask");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            return __del.getPublishingPrivacyMask(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public EventPrivacySettingIce getReceivingPrivacyMask(String username) throws FusionException {
      return this.getReceivingPrivacyMask(username, (Map)null, false);
   }

   public EventPrivacySettingIce getReceivingPrivacyMask(String username, Map<String, String> __ctx) throws FusionException {
      return this.getReceivingPrivacyMask(username, __ctx, true);
   }

   private EventPrivacySettingIce getReceivingPrivacyMask(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getReceivingPrivacyMask");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            return __del.getReceivingPrivacyMask(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public UserEventIce[] getUserEventsForUser(String username) throws FusionException {
      return this.getUserEventsForUser(username, (Map)null, false);
   }

   public UserEventIce[] getUserEventsForUser(String username, Map<String, String> __ctx) throws FusionException {
      return this.getUserEventsForUser(username, __ctx, true);
   }

   private UserEventIce[] getUserEventsForUser(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getUserEventsForUser");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            return __del.getUserEventsForUser(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public UserEventIce[] getUserEventsGeneratedByUser(String username) throws FusionException {
      return this.getUserEventsGeneratedByUser(username, (Map)null, false);
   }

   public UserEventIce[] getUserEventsGeneratedByUser(String username, Map<String, String> __ctx) throws FusionException {
      return this.getUserEventsGeneratedByUser(username, __ctx, true);
   }

   private UserEventIce[] getUserEventsGeneratedByUser(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getUserEventsGeneratedByUser");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            return __del.getUserEventsGeneratedByUser(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void giftShowerEvent(String username, String recipient, String giftName, int virtualGiftReceivedId, int totalRecipients) {
      this.giftShowerEvent(username, recipient, giftName, virtualGiftReceivedId, totalRecipients, (Map)null, false);
   }

   public void giftShowerEvent(String username, String recipient, String giftName, int virtualGiftReceivedId, int totalRecipients, Map<String, String> __ctx) {
      this.giftShowerEvent(username, recipient, giftName, virtualGiftReceivedId, totalRecipients, __ctx, true);
   }

   private void giftShowerEvent(String username, String recipient, String giftName, int virtualGiftReceivedId, int totalRecipients, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.giftShowerEvent(username, recipient, giftName, virtualGiftReceivedId, totalRecipients, __ctx);
            return;
         } catch (LocalExceptionWrapper var11) {
            this.__handleExceptionWrapper(__delBase, var11, (OutgoingAsync)null);
         } catch (LocalException var12) {
            __cnt = this.__handleException(__delBase, var12, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void groupAnnouncement(String username, int groupId, int groupAnnoucementId) throws FusionException {
      this.groupAnnouncement(username, groupId, groupAnnoucementId, (Map)null, false);
   }

   public void groupAnnouncement(String username, int groupId, int groupAnnoucementId, Map<String, String> __ctx) throws FusionException {
      this.groupAnnouncement(username, groupId, groupAnnoucementId, __ctx, true);
   }

   private void groupAnnouncement(String username, int groupId, int groupAnnoucementId, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("groupAnnouncement");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.groupAnnouncement(username, groupId, groupAnnoucementId, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void groupDonation(String username, int groupId) throws FusionException {
      this.groupDonation(username, groupId, (Map)null, false);
   }

   public void groupDonation(String username, int groupId, Map<String, String> __ctx) throws FusionException {
      this.groupDonation(username, groupId, __ctx, true);
   }

   private void groupDonation(String username, int groupId, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("groupDonation");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.groupDonation(username, groupId, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void groupJoined(String username, int groupId) throws FusionException {
      this.groupJoined(username, groupId, (Map)null, false);
   }

   public void groupJoined(String username, int groupId, Map<String, String> __ctx) throws FusionException {
      this.groupJoined(username, groupId, __ctx, true);
   }

   private void groupJoined(String username, int groupId, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("groupJoined");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.groupJoined(username, groupId, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void madeGroupUserPost(String username, int userPostId, int groupId) throws FusionException {
      this.madeGroupUserPost(username, userPostId, groupId, (Map)null, false);
   }

   public void madeGroupUserPost(String username, int userPostId, int groupId, Map<String, String> __ctx) throws FusionException {
      this.madeGroupUserPost(username, userPostId, groupId, __ctx, true);
   }

   private void madeGroupUserPost(String username, int userPostId, int groupId, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("madeGroupUserPost");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.madeGroupUserPost(username, userPostId, groupId, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void madePhotoPublic(String username, int scrapbookid, String title) throws FusionException {
      this.madePhotoPublic(username, scrapbookid, title, (Map)null, false);
   }

   public void madePhotoPublic(String username, int scrapbookid, String title, Map<String, String> __ctx) throws FusionException {
      this.madePhotoPublic(username, scrapbookid, title, __ctx, true);
   }

   private void madePhotoPublic(String username, int scrapbookid, String title, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("madePhotoPublic");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.madePhotoPublic(username, scrapbookid, title, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void purchasedVirtualGoods(String username, byte itemType, int itemid, String itemName) throws FusionException {
      this.purchasedVirtualGoods(username, itemType, itemid, itemName, (Map)null, false);
   }

   public void purchasedVirtualGoods(String username, byte itemType, int itemid, String itemName, Map<String, String> __ctx) throws FusionException {
      this.purchasedVirtualGoods(username, itemType, itemid, itemName, __ctx, true);
   }

   private void purchasedVirtualGoods(String username, byte itemType, int itemid, String itemName, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("purchasedVirtualGoods");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.purchasedVirtualGoods(username, itemType, itemid, itemName, __ctx);
            return;
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void setProfileStatus(String username, String status) throws FusionException {
      this.setProfileStatus(username, status, (Map)null, false);
   }

   public void setProfileStatus(String username, String status, Map<String, String> __ctx) throws FusionException {
      this.setProfileStatus(username, status, __ctx, true);
   }

   private void setProfileStatus(String username, String status, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("setProfileStatus");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.setProfileStatus(username, status, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void setPublishingPrivacyMask(String username, EventPrivacySettingIce mask) throws FusionException {
      this.setPublishingPrivacyMask(username, mask, (Map)null, false);
   }

   public void setPublishingPrivacyMask(String username, EventPrivacySettingIce mask, Map<String, String> __ctx) throws FusionException {
      this.setPublishingPrivacyMask(username, mask, __ctx, true);
   }

   private void setPublishingPrivacyMask(String username, EventPrivacySettingIce mask, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("setPublishingPrivacyMask");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.setPublishingPrivacyMask(username, mask, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void setReceivingPrivacyMask(String username, EventPrivacySettingIce mask) throws FusionException {
      this.setReceivingPrivacyMask(username, mask, (Map)null, false);
   }

   public void setReceivingPrivacyMask(String username, EventPrivacySettingIce mask, Map<String, String> __ctx) throws FusionException {
      this.setReceivingPrivacyMask(username, mask, __ctx, true);
   }

   private void setReceivingPrivacyMask(String username, EventPrivacySettingIce mask, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("setReceivingPrivacyMask");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.setReceivingPrivacyMask(username, mask, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void streamEventsToLoggingInUser(String username, ConnectionPrx connectionProxy) throws FusionException {
      this.streamEventsToLoggingInUser(username, connectionProxy, (Map)null, false);
   }

   public void streamEventsToLoggingInUser(String username, ConnectionPrx connectionProxy, Map<String, String> __ctx) throws FusionException {
      this.streamEventsToLoggingInUser(username, connectionProxy, __ctx, true);
   }

   private void streamEventsToLoggingInUser(String username, ConnectionPrx connectionProxy, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("streamEventsToLoggingInUser");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.streamEventsToLoggingInUser(username, connectionProxy, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void updateAllowList(String username, String[] watchers) throws FusionException {
      this.updateAllowList(username, watchers, (Map)null, false);
   }

   public void updateAllowList(String username, String[] watchers, Map<String, String> __ctx) throws FusionException {
      this.updateAllowList(username, watchers, __ctx, true);
   }

   private void updateAllowList(String username, String[] watchers, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("updateAllowList");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.updateAllowList(username, watchers, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void updatedProfile(String username) throws FusionException {
      this.updatedProfile(username, (Map)null, false);
   }

   public void updatedProfile(String username, Map<String, String> __ctx) throws FusionException {
      this.updatedProfile(username, __ctx, true);
   }

   private void updatedProfile(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("updatedProfile");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.updatedProfile(username, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void userWallPost(String username, String wallOwnerUsername, String postContent, int userWallPostId) throws FusionException {
      this.userWallPost(username, wallOwnerUsername, postContent, userWallPostId, (Map)null, false);
   }

   public void userWallPost(String username, String wallOwnerUsername, String postContent, int userWallPostId, Map<String, String> __ctx) throws FusionException {
      this.userWallPost(username, wallOwnerUsername, postContent, userWallPostId, __ctx, true);
   }

   private void userWallPost(String username, String wallOwnerUsername, String postContent, int userWallPostId, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("userWallPost");
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.userWallPost(username, wallOwnerUsername, postContent, userWallPostId, __ctx);
            return;
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void virtualGift(String username, String recipient, String giftName, int virtualGiftReceivedId) {
      this.virtualGift(username, recipient, giftName, virtualGiftReceivedId, (Map)null, false);
   }

   public void virtualGift(String username, String recipient, String giftName, int virtualGiftReceivedId, Map<String, String> __ctx) {
      this.virtualGift(username, recipient, giftName, virtualGiftReceivedId, __ctx, true);
   }

   private void virtualGift(String username, String recipient, String giftName, int virtualGiftReceivedId, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _EventSystemDel __del = (_EventSystemDel)__delBase;
            __del.virtualGift(username, recipient, giftName, virtualGiftReceivedId, __ctx);
            return;
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static EventSystemPrx checkedCast(ObjectPrx __obj) {
      EventSystemPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventSystemPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::EventSystem")) {
               EventSystemPrxHelper __h = new EventSystemPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (EventSystemPrx)__d;
   }

   public static EventSystemPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      EventSystemPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventSystemPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::EventSystem", __ctx)) {
               EventSystemPrxHelper __h = new EventSystemPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (EventSystemPrx)__d;
   }

   public static EventSystemPrx checkedCast(ObjectPrx __obj, String __facet) {
      EventSystemPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::EventSystem")) {
               EventSystemPrxHelper __h = new EventSystemPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static EventSystemPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      EventSystemPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::EventSystem", __ctx)) {
               EventSystemPrxHelper __h = new EventSystemPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static EventSystemPrx uncheckedCast(ObjectPrx __obj) {
      EventSystemPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventSystemPrx)__obj;
         } catch (ClassCastException var4) {
            EventSystemPrxHelper __h = new EventSystemPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (EventSystemPrx)__d;
   }

   public static EventSystemPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      EventSystemPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         EventSystemPrxHelper __h = new EventSystemPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _EventSystemDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _EventSystemDelD();
   }

   public static void __write(BasicStream __os, EventSystemPrx v) {
      __os.writeProxy(v);
   }

   public static EventSystemPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         EventSystemPrxHelper result = new EventSystemPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
