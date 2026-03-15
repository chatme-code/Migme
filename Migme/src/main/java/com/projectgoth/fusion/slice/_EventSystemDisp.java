package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.DispatchStatus;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectImpl;
import Ice.OperationMode;
import Ice.OperationNotExistException;
import Ice.OutputStream;
import IceInternal.BasicStream;
import IceInternal.Incoming;
import java.util.Arrays;
import java.util.Map;

public abstract class _EventSystemDisp extends ObjectImpl implements EventSystem {
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::EventSystem"};
   private static final String[] __all = new String[]{"addedFriend", "createdPublicChatroom", "deleteUserEvents", "genericApplicationEvent", "getPublishingPrivacyMask", "getReceivingPrivacyMask", "getUserEventsForUser", "getUserEventsGeneratedByUser", "giftShowerEvent", "groupAnnouncement", "groupDonation", "groupJoined", "ice_id", "ice_ids", "ice_isA", "ice_ping", "madeGroupUserPost", "madePhotoPublic", "purchasedVirtualGoods", "setProfileStatus", "setPublishingPrivacyMask", "setReceivingPrivacyMask", "streamEventsToLoggingInUser", "updateAllowList", "updatedProfile", "userWallPost", "virtualGift"};

   protected void ice_copyStateFrom(Object __obj) throws CloneNotSupportedException {
      throw new CloneNotSupportedException();
   }

   public boolean ice_isA(String s) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public boolean ice_isA(String s, Current __current) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public String[] ice_ids() {
      return __ids;
   }

   public String[] ice_ids(Current __current) {
      return __ids;
   }

   public String ice_id() {
      return __ids[1];
   }

   public String ice_id(Current __current) {
      return __ids[1];
   }

   public static String ice_staticId() {
      return __ids[1];
   }

   public final void addedFriend(String username, String friend) throws FusionException {
      this.addedFriend(username, friend, (Current)null);
   }

   public final void createdPublicChatroom(String username, String chatroomName) throws FusionException {
      this.createdPublicChatroom(username, chatroomName, (Current)null);
   }

   public final void deleteUserEvents(String username) throws FusionException {
      this.deleteUserEvents(username, (Current)null);
   }

   public final void genericApplicationEvent(String username, String appId, String text, Map<String, String> customDeviceURL) throws FusionException {
      this.genericApplicationEvent(username, appId, text, customDeviceURL, (Current)null);
   }

   public final EventPrivacySettingIce getPublishingPrivacyMask(String username) throws FusionException {
      return this.getPublishingPrivacyMask(username, (Current)null);
   }

   public final EventPrivacySettingIce getReceivingPrivacyMask(String username) throws FusionException {
      return this.getReceivingPrivacyMask(username, (Current)null);
   }

   public final UserEventIce[] getUserEventsForUser(String username) throws FusionException {
      return this.getUserEventsForUser(username, (Current)null);
   }

   public final UserEventIce[] getUserEventsGeneratedByUser(String username) throws FusionException {
      return this.getUserEventsGeneratedByUser(username, (Current)null);
   }

   public final void giftShowerEvent(String username, String recipient, String giftName, int virtualGiftReceivedId, int totalRecipients) {
      this.giftShowerEvent(username, recipient, giftName, virtualGiftReceivedId, totalRecipients, (Current)null);
   }

   public final void groupAnnouncement(String username, int groupId, int groupAnnoucementId) throws FusionException {
      this.groupAnnouncement(username, groupId, groupAnnoucementId, (Current)null);
   }

   public final void groupDonation(String username, int groupId) throws FusionException {
      this.groupDonation(username, groupId, (Current)null);
   }

   public final void groupJoined(String username, int groupId) throws FusionException {
      this.groupJoined(username, groupId, (Current)null);
   }

   public final void madeGroupUserPost(String username, int userPostId, int groupId) throws FusionException {
      this.madeGroupUserPost(username, userPostId, groupId, (Current)null);
   }

   public final void madePhotoPublic(String username, int scrapbookid, String title) throws FusionException {
      this.madePhotoPublic(username, scrapbookid, title, (Current)null);
   }

   public final void purchasedVirtualGoods(String username, byte itemType, int itemid, String itemName) throws FusionException {
      this.purchasedVirtualGoods(username, itemType, itemid, itemName, (Current)null);
   }

   public final void setProfileStatus(String username, String status) throws FusionException {
      this.setProfileStatus(username, status, (Current)null);
   }

   public final void setPublishingPrivacyMask(String username, EventPrivacySettingIce mask) throws FusionException {
      this.setPublishingPrivacyMask(username, mask, (Current)null);
   }

   public final void setReceivingPrivacyMask(String username, EventPrivacySettingIce mask) throws FusionException {
      this.setReceivingPrivacyMask(username, mask, (Current)null);
   }

   public final void streamEventsToLoggingInUser(String username, ConnectionPrx connectionProxy) throws FusionException {
      this.streamEventsToLoggingInUser(username, connectionProxy, (Current)null);
   }

   public final void updateAllowList(String username, String[] watchers) throws FusionException {
      this.updateAllowList(username, watchers, (Current)null);
   }

   public final void updatedProfile(String username) throws FusionException {
      this.updatedProfile(username, (Current)null);
   }

   public final void userWallPost(String username, String wallOwnerUsername, String postContent, int userWallPostId) throws FusionException {
      this.userWallPost(username, wallOwnerUsername, postContent, userWallPostId, (Current)null);
   }

   public final void virtualGift(String username, String recipient, String giftName, int virtualGiftReceivedId) {
      this.virtualGift(username, recipient, giftName, virtualGiftReceivedId, (Current)null);
   }

   public static DispatchStatus ___getUserEventsForUser(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         UserEventIce[] __ret = __obj.getUserEventsForUser(username, __current);
         UserEventIceArrayHelper.write(__os, __ret);
         __os.writePendingObjects();
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getUserEventsGeneratedByUser(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         UserEventIce[] __ret = __obj.getUserEventsGeneratedByUser(username, __current);
         UserEventIceArrayHelper.write(__os, __ret);
         __os.writePendingObjects();
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___updateAllowList(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      String[] watchers = StringArrayHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.updateAllowList(username, watchers, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___streamEventsToLoggingInUser(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      ConnectionPrx connectionProxy = ConnectionPrxHelper.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.streamEventsToLoggingInUser(username, connectionProxy, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___deleteUserEvents(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.deleteUserEvents(username, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___madePhotoPublic(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      int scrapbookid = __is.readInt();
      String title = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.madePhotoPublic(username, scrapbookid, title, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___setProfileStatus(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      String status = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.setProfileStatus(username, status, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___createdPublicChatroom(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      String chatroomName = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.createdPublicChatroom(username, chatroomName, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___addedFriend(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      String friend = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.addedFriend(username, friend, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___updatedProfile(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.updatedProfile(username, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___purchasedVirtualGoods(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      byte itemType = __is.readByte();
      int itemid = __is.readInt();
      String itemName = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.purchasedVirtualGoods(username, itemType, itemid, itemName, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var10) {
         __os.writeUserException(var10);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___virtualGift(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      String recipient = __is.readString();
      String giftName = __is.readString();
      int virtualGiftReceivedId = __is.readInt();
      __is.endReadEncaps();
      __obj.virtualGift(username, recipient, giftName, virtualGiftReceivedId, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___userWallPost(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      String wallOwnerUsername = __is.readString();
      String postContent = __is.readString();
      int userWallPostId = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.userWallPost(username, wallOwnerUsername, postContent, userWallPostId, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var10) {
         __os.writeUserException(var10);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___groupDonation(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      int groupId = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.groupDonation(username, groupId, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___groupJoined(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      int groupId = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.groupJoined(username, groupId, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___groupAnnouncement(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      int groupId = __is.readInt();
      int groupAnnoucementId = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.groupAnnouncement(username, groupId, groupAnnoucementId, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___madeGroupUserPost(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      int userPostId = __is.readInt();
      int groupId = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.madeGroupUserPost(username, userPostId, groupId, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___genericApplicationEvent(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      String appId = __is.readString();
      String text = __is.readString();
      Map<String, String> customDeviceURL = ParamMapHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.genericApplicationEvent(username, appId, text, customDeviceURL, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var10) {
         __os.writeUserException(var10);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___giftShowerEvent(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      String recipient = __is.readString();
      String giftName = __is.readString();
      int virtualGiftReceivedId = __is.readInt();
      int totalRecipients = __is.readInt();
      __is.endReadEncaps();
      __obj.giftShowerEvent(username, recipient, giftName, virtualGiftReceivedId, totalRecipients, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getPublishingPrivacyMask(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         EventPrivacySettingIce __ret = __obj.getPublishingPrivacyMask(username, __current);
         __ret.__write(__os);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___setPublishingPrivacyMask(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      EventPrivacySettingIce mask = new EventPrivacySettingIce();
      mask.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.setPublishingPrivacyMask(username, mask, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getReceivingPrivacyMask(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         EventPrivacySettingIce __ret = __obj.getReceivingPrivacyMask(username, __current);
         __ret.__write(__os);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___setReceivingPrivacyMask(EventSystem __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      EventPrivacySettingIce mask = new EventPrivacySettingIce();
      mask.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.setReceivingPrivacyMask(username, mask, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public DispatchStatus __dispatch(Incoming in, Current __current) {
      int pos = Arrays.binarySearch(__all, __current.operation);
      if (pos < 0) {
         throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
      } else {
         switch(pos) {
         case 0:
            return ___addedFriend(this, in, __current);
         case 1:
            return ___createdPublicChatroom(this, in, __current);
         case 2:
            return ___deleteUserEvents(this, in, __current);
         case 3:
            return ___genericApplicationEvent(this, in, __current);
         case 4:
            return ___getPublishingPrivacyMask(this, in, __current);
         case 5:
            return ___getReceivingPrivacyMask(this, in, __current);
         case 6:
            return ___getUserEventsForUser(this, in, __current);
         case 7:
            return ___getUserEventsGeneratedByUser(this, in, __current);
         case 8:
            return ___giftShowerEvent(this, in, __current);
         case 9:
            return ___groupAnnouncement(this, in, __current);
         case 10:
            return ___groupDonation(this, in, __current);
         case 11:
            return ___groupJoined(this, in, __current);
         case 12:
            return ___ice_id(this, in, __current);
         case 13:
            return ___ice_ids(this, in, __current);
         case 14:
            return ___ice_isA(this, in, __current);
         case 15:
            return ___ice_ping(this, in, __current);
         case 16:
            return ___madeGroupUserPost(this, in, __current);
         case 17:
            return ___madePhotoPublic(this, in, __current);
         case 18:
            return ___purchasedVirtualGoods(this, in, __current);
         case 19:
            return ___setProfileStatus(this, in, __current);
         case 20:
            return ___setPublishingPrivacyMask(this, in, __current);
         case 21:
            return ___setReceivingPrivacyMask(this, in, __current);
         case 22:
            return ___streamEventsToLoggingInUser(this, in, __current);
         case 23:
            return ___updateAllowList(this, in, __current);
         case 24:
            return ___updatedProfile(this, in, __current);
         case 25:
            return ___userWallPost(this, in, __current);
         case 26:
            return ___virtualGift(this, in, __current);
         default:
            assert false;

            throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
         }
      }
   }

   public void __write(BasicStream __os) {
      __os.writeTypeId(ice_staticId());
      __os.startWriteSlice();
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::EventSystem was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::EventSystem was not generated with stream support";
      throw ex;
   }
}
