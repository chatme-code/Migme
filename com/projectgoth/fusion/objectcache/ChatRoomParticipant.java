package com.projectgoth.fusion.objectcache;

import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.MerchantDetailsData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.emote.EmoteCommandUtils;
import com.projectgoth.fusion.emote.GiftAllTask;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.SessionPrxHelper;
import com.projectgoth.fusion.slice.UserPrx;
import java.util.Date;
import org.apache.log4j.Logger;

public class ChatRoomParticipant extends ChatParticipant {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatRoom.class));
   private Short clientVersion = null;
   private Integer deviceType = null;
   private SessionPrx sessionPrx;
   private SessionPrx oneWaySessionPrx;
   private UserPrx userPrx;
   private String sessionID;
   private String ipAddress;
   private String mobileDevice;
   private String userAgent;
   private UserData userData;
   private boolean appearsOffline;
   private long timeCreated = System.currentTimeMillis();
   private long lastTimeKickVoteInitiated;
   private long lastTimeTargetOfKickVote;
   private long lastTimeMessageSent = System.currentTimeMillis();
   private String lastMessageSent;
   private int messageRepetitions;
   private boolean groupAdmin = false;
   private boolean groupMod = false;
   ChatRoomDataWrapper chatRoomData;
   private MerchantDetailsData merchantDetailsData;
   private ChatRoomParticipantListener onRemoveListener;

   public ChatRoomParticipant(ChatRoomDataWrapper chatRoomData, UserPrx userPrx, SessionPrx sessionPrx, String sessionID, String ipAddress, String mobileDevice, String userAgent, UserData userData) {
      super(userData.username);
      this.chatRoomData = chatRoomData;
      this.userPrx = userPrx;
      this.sessionPrx = sessionPrx;
      this.oneWaySessionPrx = SessionPrxHelper.uncheckedCast(sessionPrx.ice_oneway());
      this.oneWaySessionPrx = (SessionPrx)this.oneWaySessionPrx.ice_connectionId("OneWayProxyGroup");
      this.sessionID = sessionID;
      this.ipAddress = ipAddress;
      this.mobileDevice = mobileDevice;
      this.userAgent = userAgent;
      this.userData = userData;
      this.appearsOffline = userPrx.getOverallFusionPresence((String)null) == PresenceType.OFFLINE.value();
   }

   public ChatRoomParticipant(ChatRoomDataWrapper chatRoomData, UserPrx userPrx, SessionPrx sessionPrx, String sessionID, String ipAddress, String mobileDevice, String userAgent, UserData userData, short clientVersion, int deviceType) {
      super(userData.username);
      this.chatRoomData = chatRoomData;
      this.userPrx = userPrx;
      this.sessionPrx = sessionPrx;
      this.oneWaySessionPrx = SessionPrxHelper.uncheckedCast(sessionPrx.ice_oneway());
      this.oneWaySessionPrx = (SessionPrx)this.oneWaySessionPrx.ice_connectionId("OneWayProxyGroup");
      this.sessionID = sessionID;
      this.ipAddress = ipAddress;
      this.mobileDevice = mobileDevice;
      this.userAgent = userAgent;
      this.userData = userData;
      this.clientVersion = clientVersion;
      this.deviceType = deviceType;
      this.appearsOffline = userPrx.getOverallFusionPresence((String)null) == PresenceType.OFFLINE.value();
   }

   private UserPrx getUserPrx() {
      return this.userPrx;
   }

   private SessionPrx getSessionPrx() {
      return this.sessionPrx;
   }

   public String getSessionID() {
      return this.sessionID;
   }

   public String getIPAddress() {
      return this.ipAddress;
   }

   public String getMobileDevice() {
      return this.mobileDevice;
   }

   public String getUserAgent() {
      return this.userAgent;
   }

   public Integer getUserID() {
      return this.userData.userID;
   }

   public Integer getCountryID() {
      return this.userData.countryID;
   }

   public long getIdleTimeMillis() {
      return System.currentTimeMillis() - Math.max(this.lastTimeMessageSent, this.lastTimeKickVoteInitiated);
   }

   public long getTimeInRoomMillis() {
      return System.currentTimeMillis() - this.timeCreated;
   }

   public long getLastTimeKickVoteInitiated() {
      return this.lastTimeKickVoteInitiated;
   }

   public long getLastTimeTargetOfKickVote() {
      return this.lastTimeTargetOfKickVote;
   }

   public void setLastTimeKickVoteInitiated(long time) {
      this.lastTimeKickVoteInitiated = time;
   }

   public void setLastTimeTargetOfKickVote(long time) {
      this.lastTimeTargetOfKickVote = time;
   }

   private void setLastTimeMessageSent(long time) {
      this.lastTimeMessageSent = time;
   }

   public void setGroupAdmin(boolean flag) {
      this.groupAdmin = flag;
   }

   public void setGroupMod(boolean flag) {
      this.groupMod = flag;
   }

   public boolean isGlobalAdmin() {
      return this.userData.chatRoomAdmin;
   }

   public boolean hasAdminOrModeratorRights() {
      return this.isGlobalAdmin() || this.isGroupAdmin() || this.isGroupMod() || this.isRoomOwner() || this.isModerator();
   }

   public boolean isHiddenAdmin() {
      return this.isGlobalAdmin() && this.appearsOffline;
   }

   public boolean isRoomOwner() {
      return this.isUserOwned() && this.userData.username.equals(this.getCreator());
   }

   public boolean isTopMerchant() {
      return this.userData.type == UserData.TypeEnum.MIG33_TOP_MERCHANT;
   }

   public int getNumOfPriorKicks() {
      return this.userData.chatRoomBans;
   }

   public MerchantDetailsData getMerchantDetailsData() {
      return this.merchantDetailsData;
   }

   public int getLevel() {
      try {
         return MemCacheOrEJB.getUserReputationLevel(this.getUsername(), this.getUserID());
      } catch (Exception var2) {
         return 0;
      }
   }

   public void setOnRemoveListener(ChatRoomParticipantListener onRemoveListener) {
      this.onRemoveListener = onRemoveListener;
   }

   private void removeParticipantOnException() {
      if (this.onRemoveListener != null) {
         this.onRemoveListener.removeParticipantOnException(this);
      }
   }

   public boolean isSpamming(String message, int maxMessageRepetitions, int minSpamMessageLength) {
      if (!message.equalsIgnoreCase(this.lastMessageSent)) {
         this.lastMessageSent = message;
         this.messageRepetitions = 1;
         return false;
      } else {
         return ++this.messageRepetitions >= maxMessageRepetitions && message.length() > minSpamMessageLength;
      }
   }

   private String getRoomName() {
      return this.chatRoomData.getName();
   }

   public boolean isModerator() {
      return this.chatRoomData.isModerator(this.getUsername());
   }

   public String getCreator() {
      return this.chatRoomData.getCreator();
   }

   public boolean isUserOwned() {
      return this.chatRoomData.isUserOwned();
   }

   public boolean isGroupAdmin() {
      return this.chatRoomData.hasGroupData() && this.groupAdmin;
   }

   public boolean isGroupMod() {
      return this.chatRoomData.hasGroupData() && this.groupMod;
   }

   public void setMerchantDetails(MerchantDetailsData data) {
      this.merchantDetailsData = data;
   }

   public boolean isMerchantMentor() {
      return this.merchantDetailsData != null && this.merchantDetailsData.isMerchantMentor();
   }

   public void putMessage(MessageDataIce message) throws FusionException {
      this.getSessionPrx().putMessage(message);
   }

   public void sendMessageBackToUserAsEmote(MessageDataIce message) throws FusionException {
      this.getSessionPrx().sendMessageBackToUserAsEmote(message);
   }

   public ClientType getDeviceType() {
      return ClientType.fromValue(this.getDeviceTypeAsInt());
   }

   public int getClientVersionIce() {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.PARTICIPANT_CACHE_DATA, true) && this.clientVersion != null) {
         return this.clientVersion;
      } else {
         this.clientVersion = this.getSessionPrx().getClientVersionIce();
         return this.clientVersion;
      }
   }

   public int getDeviceTypeAsInt() {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.PARTICIPANT_CACHE_DATA, true) && this.deviceType != null) {
         return this.deviceType;
      } else {
         this.deviceType = this.getSessionPrx().getDeviceTypeAsInt();
         return this.deviceType;
      }
   }

   public boolean isMobileClientV2AndNewVersionOrAjax() {
      return ClientType.isMobileClientV2AndNewVersionOrAjax(this.getDeviceType(), this.clientVersion);
   }

   public void silentlyDropIncomingPackets() {
      this.getSessionPrx().silentlyDropIncomingPackets();
   }

   public void verifyClientMeetsMinVersion(String command) throws FusionException {
      if (!EmoteCommandUtils.clientMeetsMinVersion(this.getClientVersionIce(), this.getDeviceType())) {
         throw new FusionException(command + " is not supported on this client device and version");
      }
   }

   public boolean isIdle(long userIdleTimeoutInMs, long maxUserDurationInMs) {
      return this.getIdleTimeMillis() > userIdleTimeoutInMs && !this.hasAdminOrModeratorRights() || this.getTimeInRoomMillis() > maxUserDurationInMs;
   }

   public void updateLastTimeMessageSent() {
      this.setLastTimeMessageSent(System.currentTimeMillis());
   }

   public GiftAllTask createGiftAllTask(int giftId, String giftMessage, MessageData messageData, ChatRoom chatRoom, ChatObjectManagerRoom objectManager) {
      return new GiftAllTask(giftId, giftMessage, messageData, chatRoom, objectManager, this.getSessionPrx());
   }

   public void removeFromUsersCurrentChatroomList() {
      this.getUserPrx().removeFromCurrentChatroomList(this.getRoomName());
   }

   public Date getLastLoginDate() {
      return this.userData.lastLoginDate;
   }

   public void notifyUserLeftChatRoom_async(String username) {
      if (this.isMobileClientV2AndNewVersionOrAjax()) {
         try {
            this.oneWaySessionPrx.notifyUserLeftChatRoomOneWay(this.getRoomName(), username);
         } catch (Exception var3) {
         }
      }

   }

   public void notifyUserJoinedChatRoom_async(String username, boolean administrator, boolean banned) {
      if (this.isMobileClientV2AndNewVersionOrAjax()) {
         try {
            this.oneWaySessionPrx.notifyUserJoinedChatRoomOneWay(this.getRoomName(), username, administrator, banned);
         } catch (Exception var5) {
         }
      }

   }

   public void putAlertMessage_async(String messageText) {
      try {
         this.oneWaySessionPrx.putAlertMessageOneWay(messageText, this.getRoomName(), (short)5);
      } catch (Exception var3) {
      }

   }

   public void putMessage_async(MessageDataIce message) {
      try {
         this.oneWaySessionPrx.putMessageOneWay(message);
      } catch (Exception var3) {
      }

   }

   public void addToUsersCurrentChatroomList() throws FusionException {
      this.getUserPrx().addToCurrentChatroomList(this.getRoomName());
   }

   public Integer getMessageSourceColorOverride() {
      if (this.isTopMerchant()) {
         MerchantDetailsData merchantDetailsData = this.getMerchantDetailsData();
         if (merchantDetailsData != null) {
            if (log.isDebugEnabled()) {
               log.debug("Username [" + this.getUsername() + "] Color: " + merchantDetailsData.usernameColorType.name());
            }

            return merchantDetailsData.getChatColorHex();
         } else {
            if (log.isDebugEnabled()) {
               log.debug("Top Merchant [" + this.getUsername() + "] has no merchantdetails attached. Using default color " + MerchantDetailsData.UserNameColorTypeEnum.DEFAULT.name());
            }

            return MerchantDetailsData.UserNameColorTypeEnum.DEFAULT.hex();
         }
      } else if (this.hasAdminOrModeratorRights()) {
         if (this.isGlobalAdmin()) {
            return MessageData.SourceTypeEnum.GLOBAL_ADMIN.colorHex();
         } else {
            return this.isModerator() ? MessageData.SourceTypeEnum.MODERATOR_USER.colorHex() : MessageData.SourceTypeEnum.GROUP_ADMIN_USER.colorHex();
         }
      } else {
         return null;
      }
   }
}
