package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.data.PaidEmoteData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.exception.UserNotOnlineException;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardDispatcher;
import com.projectgoth.fusion.objectcache.ChatSourceGroup;
import com.projectgoth.fusion.objectcache.ChatSourceRoom;
import com.projectgoth.fusion.objectcache.ChatSourceSession;
import com.projectgoth.fusion.objectcache.ChatSourceUser;
import com.projectgoth.fusion.objectcache.OfflineMessageHelper;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserPrx;
import java.sql.Connection;
import org.apache.log4j.Logger;

public abstract class ChatSource {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatSource.class));
   protected ChatSource.ChatType chatType;
   protected ChatSource.ChatSourceType chatSourceType;
   protected SessionPrx sessionPrx;
   protected ChatSourceSession sessionI;
   protected String parentUsername;

   private ChatSource(ChatSource.ChatType chatType, ChatSourceSession session) {
      this.chatSourceType = ChatSource.ChatSourceType.LOCAL_SESSION;
      this.chatType = chatType;
      this.sessionI = session;
   }

   private ChatSource(ChatSource.ChatType chatType, SessionPrx session) {
      this.chatSourceType = ChatSource.ChatSourceType.REMOTE_SESSION;
      this.chatType = chatType;
      this.sessionPrx = session;
   }

   public static ChatSource createChatSourceForChatRoom(ChatSourceSession session, ChatRoomPrx chatRoomPrx) {
      return new ChatSource.LocalChatRoomChatSource(session, chatRoomPrx);
   }

   public static ChatSource createChatSourceForChatRoom(SessionPrx session, ChatSourceRoom chatRoom) {
      return new ChatSource.RemoteChatRoomChatSource(session, chatRoom);
   }

   public static ChatSource createChatSourceForGroupChat(ChatSourceSession session, GroupChatPrx groupChatPrx) {
      return new ChatSource.LocalGroupChatChatSource(session, groupChatPrx);
   }

   public static ChatSource createChatSourceForGroupChat(SessionPrx session, ChatSourceGroup chatGroup) {
      return new ChatSource.RemoteGroupChatChatSource(session, chatGroup);
   }

   public static ChatSource createChatSourceForPrivateChat(ChatSourceSession session, String destinationUsername) {
      return new ChatSource.LocalPrivateChatChatSource(session, destinationUsername);
   }

   public static ChatSource createChatSourceForPrivateChat(SessionPrx session, ChatSourceUser userI, String senderUsername, String destinationUsername) {
      return new ChatSource.RemotePrivateChatChatSource(session, userI, senderUsername, destinationUsername);
   }

   public ChatSource.ChatType getChatType() {
      return this.chatType;
   }

   public ChatSourceSession getSessionI() {
      return this.sessionI;
   }

   protected String getTruncatedMessage(String messageText, String subMessageToTruncate, int maxLength, String parentUsername) throws FusionException {
      String msg = EmoteCommand.processMessageVariables(String.format(messageText, subMessageToTruncate), parentUsername, this);
      if (msg.length() > maxLength) {
         String newSubMsg = "";
         int subMessageToTruncateLen = subMessageToTruncate.length();
         if (subMessageToTruncateLen > msg.length() - maxLength + 3) {
            newSubMsg = subMessageToTruncate.substring(0, subMessageToTruncateLen - (msg.length() - maxLength) - 3) + "...";
         }

         msg = EmoteCommand.processMessageVariables(String.format(messageText, newSubMsg), parentUsername, this);
      }

      return msg;
   }

   public SessionPrx getSessionPrx() {
      return this.chatSourceType == ChatSource.ChatSourceType.REMOTE_SESSION ? this.sessionPrx : this.sessionI.findSessionPrx(this.sessionI.getSessionID());
   }

   public abstract boolean isUserInChat(String var1) throws FusionException;

   public abstract boolean isUserVisibleInChat(String var1) throws FusionException;

   public abstract String[] getVisibleUsernamesInChat(boolean var1) throws FusionException;

   public abstract String[] getAllUsernamesInChat(boolean var1) throws FusionException;

   public void sendMessageToAllUsersInChat(MessageData messageData) throws FusionException {
      try {
         MessageSwitchboardDispatcher.getInstance().onSendMessageToAllUsersInChat(messageData, messageData.username, this.sessionPrx, this.sessionI);
      } catch (Exception var3) {
         log.error("While storing V2 emote in chatsync: ", var3);
         throw new FusionException(var3.getMessage());
      }
   }

   public abstract void sendToSender(MessageDataIce var1) throws FusionException;

   public abstract void sendToCounterParties(MessageDataIce var1) throws FusionException;

   public void sendMessageWithTruncationToAllUsersInChat(MessageData messageData, String subMessageToTruncate) throws FusionException {
      messageData.messageText = String.format(messageData.messageText, subMessageToTruncate);
      this.sendMessageToAllUsersInChat(messageData);
   }

   public abstract void sendMessageToSender(MessageData var1) throws FusionException;

   public abstract PaidEmoteData.EmotePurchaseLocationEnum getEmotePurchaseLocation();

   protected String getParentUsernameInternal() throws FusionException {
      return null;
   }

   public String getParentUsername() throws FusionException {
      if (this.parentUsername == null) {
         this.parentUsername = this.getParentUsernameInternal();
      }

      return this.parentUsername;
   }

   public EmoteCommand.ResultType executeEmoteCommandWithState(String emoteCommand, MessageDataIce message) throws FusionException {
      throw new FusionException("unimplemented execution of emote command with state");
   }

   public abstract void accept(ChatSourceVisitor var1) throws FusionException;

   public abstract ClientType getSenderDeviceType();

   public abstract short getSenderClientVersion();

   public ChatSource.LocalChatRoomChatSource castToLocalChatRoomChatSource() {
      return (ChatSource.LocalChatRoomChatSource)this;
   }

   private MessageDataIce createStickerEmotesForSender(MessageData messageData, String msgToInstigator) throws FusionException {
      return Sticker.createStickerEmotesForSender(messageData, msgToInstigator, this.getSenderDeviceType(), this.getSenderClientVersion());
   }

   public final void sendStickerEmotes(StickerDeliveredMessageData forDelivery) throws FusionException {
      this.sendToSender(this.createStickerEmotesForSender(forDelivery.getMessageData(), forDelivery.getMessageToInstigator()));
      this.sendToCounterParties(Sticker.createStickerEmotesForRecipients(forDelivery.getMessageData(), forDelivery.getMessageToRecipients()));
   }

   // $FF: synthetic method
   ChatSource(ChatSource.ChatType x0, ChatSourceSession x1, Object x2) {
      this(x0, x1);
   }

   // $FF: synthetic method
   ChatSource(ChatSource.ChatType x0, SessionPrx x1, Object x2) {
      this(x0, x1);
   }

   public static class RemotePrivateChatChatSource extends ChatSource {
      private String senderUsername;
      private String destinationUsername;
      protected ChatSourceUser currentUserI;

      RemotePrivateChatChatSource(SessionPrx session, ChatSourceUser userI, String senderUsername, String destinationUsername) {
         super(ChatSource.ChatType.PRIVATE_CHAT, (SessionPrx)session, null);
         this.currentUserI = userI;
         this.senderUsername = senderUsername;
         this.destinationUsername = destinationUsername;
      }

      public boolean isUserInChat(String username) throws FusionException {
         return !StringUtil.isBlank(username) && (username.equals(this.destinationUsername) || username.equals(this.senderUsername));
      }

      public boolean isUserVisibleInChat(String username) throws FusionException {
         return this.isUserInChat(username);
      }

      public String[] getVisibleUsernamesInChat(boolean includeParentUser) throws FusionException {
         return this.getAllUsernamesInChat(includeParentUser);
      }

      public String[] getAllUsernamesInChat(boolean includeParentUser) throws FusionException {
         return includeParentUser ? new String[]{this.senderUsername, this.destinationUsername} : new String[]{this.destinationUsername};
      }

      public void sendMessageToAllUsersInChat(MessageData messageData) throws FusionException {
         super.sendMessageToAllUsersInChat(messageData);
         boolean isInSenderUserI = this.senderUsername.equals(this.currentUserI.getUsername());
         String messageText = messageData.messageText;
         MessageDataIce messageIce = messageData.toIceObject();
         messageIce.messageText = EmoteCommand.processMessageVariables(messageText, (String)null, (ChatSource)this);
         if (isInSenderUserI) {
            UserPrx destinationUserPrx = this.sessionPrx.getUserProxy(this.destinationUsername);
            if (destinationUserPrx == null) {
               throw new FusionException(this.destinationUsername + " is not in the chat");
            }

            destinationUserPrx.putMessage(messageIce);
         } else {
            this.currentUserI.putMessage(messageIce);
         }

         messageIce.messageText = EmoteCommand.processMessageVariables(messageText, this.senderUsername, (ChatSource)this);
         String tmp = messageIce.source;
         messageIce.source = messageIce.messageDestinations[0].destination;
         messageIce.messageDestinations[0].destination = tmp;
         this.sessionPrx.putMessage(messageIce);
      }

      public void sendMessageToSender(MessageData messageData) throws FusionException {
         String messageText = messageData.messageText;
         MessageDataIce messageIce = messageData.toIceObject();
         messageIce.messageText = EmoteCommand.processMessageVariables(messageText, this.senderUsername, (ChatSource)this);
         String tmp = messageIce.source;
         messageIce.source = messageIce.messageDestinations[0].destination;
         messageIce.messageDestinations[0].destination = tmp;
         this.sessionPrx.putMessage(messageIce);
      }

      public PaidEmoteData.EmotePurchaseLocationEnum getEmotePurchaseLocation() {
         return PaidEmoteData.EmotePurchaseLocationEnum.PRIVATE_CHAT_COMMAND;
      }

      protected String getParentUsernameInternal() throws FusionException {
         return this.sessionPrx.getParentUsername();
      }

      public void accept(ChatSourceVisitor visitor) throws FusionException {
         visitor.visit(this);
      }

      public void sendToSender(MessageDataIce messageDataIce) throws FusionException {
         this.sessionPrx.putMessage(messageDataIce);
      }

      public void sendToCounterParties(MessageDataIce messageDataIce) throws FusionException {
         this.currentUserI.putMessage(messageDataIce);
      }

      public ClientType getSenderDeviceType() {
         return ClientType.fromValue(this.sessionPrx.getDeviceTypeAsInt());
      }

      public short getSenderClientVersion() {
         return this.sessionPrx.getClientVersionIce();
      }
   }

   public static class LocalPrivateChatChatSource extends ChatSource {
      protected String destinationUsername;
      protected UserPrx destinationUserPrx;

      LocalPrivateChatChatSource(ChatSourceSession session, String destinationUsername) {
         super(ChatSource.ChatType.PRIVATE_CHAT, (ChatSourceSession)session, null);
         this.destinationUsername = destinationUsername;
      }

      public boolean isUserInChat(String username) throws FusionException {
         return !StringUtil.isBlank(username) && (username.equals(this.destinationUsername) || username.equals(this.getParentUsername()));
      }

      public boolean isUserVisibleInChat(String username) throws FusionException {
         return this.isUserInChat(username);
      }

      public String[] getVisibleUsernamesInChat(boolean includeParentUser) throws FusionException {
         return this.getAllUsernamesInChat(includeParentUser);
      }

      public String[] getAllUsernamesInChat(boolean includeParentUser) throws FusionException {
         return includeParentUser ? new String[]{this.getParentUsername(), this.destinationUsername} : new String[]{this.destinationUsername};
      }

      public void sendMessageToAllUsersInChat(MessageData messageData) throws FusionException {
         super.sendMessageToAllUsersInChat(messageData);
         this.sessionI.sendMessageBackToUserAsEmote(messageData, EmoteCommand.processMessageVariables(messageData.messageText, this.getParentUsername(), (ChatSource)this));
         MessageDataIce messageIce = messageData.toIceObject();
         messageIce.messageText = EmoteCommand.processMessageVariables(messageData.messageText, (String)null, (ChatSource)this);

         try {
            this.getDestinationUserPrx().putMessage(messageIce);
         } catch (FusionException var4) {
            if (var4.message == null || !(var4 instanceof UserNotOnlineException)) {
               throw var4;
            }

            if (ChatSource.log.isDebugEnabled()) {
               ChatSource.log.debug("Recipient is offline. Ex=" + var4);
            }

            this.sendMessageToOfflineRecipient(messageData);
         }

      }

      public void sendMessageToOfflineRecipient(MessageData messageData) throws FusionException {
         try {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            int srcId = userEJB.getUserID(messageData.source, (Connection)null);
            if (ChatSource.log.isDebugEnabled()) {
               ChatSource.log.debug("Sender=" + messageData.source + " id=" + srcId);
            }

            int destId = userEJB.getUserID(((MessageDestinationData)messageData.messageDestinations.get(0)).destination, (Connection)null);
            if (ChatSource.log.isDebugEnabled()) {
               ChatSource.log.debug("Recipient=" + ((MessageDestinationData)messageData.messageDestinations.get(0)).destination + " id=" + destId);
            }

            OfflineMessageHelper.StorageResult result = OfflineMessageHelper.getInstance().scheduleOfflineMessageStorageAndWait(messageData, srcId, destId);
            if (result.failed()) {
               throw new FusionException(result.getError());
            }
         } catch (FusionException var6) {
            throw var6;
         } catch (Exception var7) {
            ChatSource.log.error("Exception in ChatSource.sendMessageToOfflineRecipient(): " + var7);
            throw new FusionException(var7.getMessage());
         }
      }

      public void sendMessageToSender(MessageData messageData) throws FusionException {
         this.sessionI.sendMessageBackToUserAsEmote(messageData, EmoteCommand.processMessageVariables(messageData.messageText, this.getParentUsername(), (ChatSource)this));
      }

      public PaidEmoteData.EmotePurchaseLocationEnum getEmotePurchaseLocation() {
         return PaidEmoteData.EmotePurchaseLocationEnum.PRIVATE_CHAT_COMMAND;
      }

      private UserPrx getDestinationUserPrx() throws FusionException {
         if (this.destinationUserPrx == null) {
            this.destinationUserPrx = this.sessionI.findUserPrx(this.destinationUsername);
         }

         return this.destinationUserPrx;
      }

      protected String getParentUsernameInternal() throws FusionException {
         return this.sessionI.getUsername();
      }

      public EmoteCommand.ResultType executeEmoteCommandWithState(String emoteCommand, MessageDataIce message) throws FusionException {
         UserPrx userPrx = null;
         String senderUsername = this.getParentUsername();
         if (this.destinationUsername.compareTo(senderUsername) > 0) {
            userPrx = this.sessionI.findUserPrx(senderUsername);
         } else {
            userPrx = this.getDestinationUserPrx();
         }

         if (userPrx == null) {
            ChatSource.log.error(String.format("Unable to obtain UserPrx to execute emote command with state '%s', sender='%s', dest='%s', msg=%s", emoteCommand, senderUsername, this.destinationUsername, MessageData.toString(message)));
            return EmoteCommand.ResultType.NOTHANDLED;
         } else {
            return EmoteCommand.ResultType.fromValue(userPrx.executeEmoteCommandWithState(emoteCommand, message, this.getSessionPrx()));
         }
      }

      public void accept(ChatSourceVisitor visitor) throws FusionException {
         visitor.visit(this);
      }

      public void sendToSender(MessageDataIce messageDataIce) throws FusionException {
         this.sessionI.putMessage(messageDataIce);
      }

      public void sendToCounterParties(MessageDataIce messageDataIce) throws FusionException {
         try {
            this.getDestinationUserPrx().putMessage(messageDataIce);
         } catch (UserNotOnlineException var3) {
            if (ChatSource.log.isDebugEnabled()) {
               ChatSource.log.debug("Recipient of sticker from " + messageDataIce.source + " is offline: e=" + var3);
            }
         }

      }

      public ClientType getSenderDeviceType() {
         return this.sessionI.getDeviceType();
      }

      public short getSenderClientVersion() {
         return this.sessionI.getClientVersion();
      }
   }

   public static class RemoteGroupChatChatSource extends ChatSource {
      protected ChatSourceGroup chatGroup;

      public RemoteGroupChatChatSource(SessionPrx session, ChatSourceGroup chatGroup) {
         super(ChatSource.ChatType.GROUP_CHAT, (SessionPrx)session, null);
         this.chatGroup = chatGroup;
      }

      public boolean isUserInChat(String username) throws FusionException {
         return !StringUtil.isBlank(username) && this.chatGroup.isParticipant(username);
      }

      public boolean isUserVisibleInChat(String username) throws FusionException {
         return this.isUserInChat(username);
      }

      public String[] getVisibleUsernamesInChat(boolean includeParentUser) throws FusionException {
         return this.getAllUsernamesInChat(includeParentUser);
      }

      public String[] getAllUsernamesInChat(boolean includeParentUser) throws FusionException {
         return this.chatGroup.getParticipants(includeParentUser ? null : this.getParentUsername());
      }

      public void sendMessageToAllUsersInChat(MessageData messageData) throws FusionException {
         super.sendMessageToAllUsersInChat(messageData);
         String messageText = messageData.messageText;
         MessageDataIce messageIce = messageData.toIceObject();
         messageIce.messageText = EmoteCommand.processMessageVariables(messageText, this.getParentUsername(), (ChatSource)this);
         this.sessionPrx.sendMessageBackToUserAsEmote(messageIce);
         messageIce.messageText = EmoteCommand.processMessageVariables(messageText, (String)null, (ChatSource)this);
         this.chatGroup.putMessage(messageIce);
      }

      public void sendMessageToSender(MessageData messageData) throws FusionException {
         String messageText = messageData.messageText;
         MessageDataIce messageIce = messageData.toIceObject();
         messageIce.messageText = EmoteCommand.processMessageVariables(messageText, this.getParentUsername(), (ChatSource)this);
         this.sessionPrx.sendMessageBackToUserAsEmote(messageIce);
      }

      public PaidEmoteData.EmotePurchaseLocationEnum getEmotePurchaseLocation() {
         return PaidEmoteData.EmotePurchaseLocationEnum.GROUP_CHAT_COMMAND;
      }

      protected String getParentUsernameInternal() throws FusionException {
         return this.sessionPrx.getParentUsername();
      }

      public void accept(ChatSourceVisitor visitor) throws FusionException {
         visitor.visit(this);
      }

      public void sendToSender(MessageDataIce messageDataIce) throws FusionException {
         this.sessionPrx.putMessage(messageDataIce);
      }

      public void sendToCounterParties(MessageDataIce messageDataIce) throws FusionException {
         this.chatGroup.putMessage(messageDataIce);
      }

      public ClientType getSenderDeviceType() {
         return ClientType.fromValue(this.sessionPrx.getDeviceTypeAsInt());
      }

      public short getSenderClientVersion() {
         return this.sessionPrx.getClientVersionIce();
      }
   }

   public static class LocalGroupChatChatSource extends ChatSource {
      protected GroupChatPrx groupChatPrx;

      LocalGroupChatChatSource(ChatSourceSession session, GroupChatPrx groupChatPrx) {
         super(ChatSource.ChatType.GROUP_CHAT, (ChatSourceSession)session, null);
         this.groupChatPrx = groupChatPrx;
      }

      public boolean isUserInChat(String username) throws FusionException {
         return !StringUtil.isBlank(username) && this.groupChatPrx.isParticipant(username);
      }

      public boolean isUserVisibleInChat(String username) throws FusionException {
         return this.isUserInChat(username);
      }

      public String[] getVisibleUsernamesInChat(boolean includeParentUser) throws FusionException {
         return this.getAllUsernamesInChat(includeParentUser);
      }

      public String[] getAllUsernamesInChat(boolean includeParentUser) throws FusionException {
         return this.groupChatPrx.getParticipants(includeParentUser ? null : this.getParentUsername());
      }

      public void sendMessageToAllUsersInChat(MessageData messageData) throws FusionException {
         super.sendMessageToAllUsersInChat(messageData);
         this.sessionI.sendMessageBackToUserAsEmote(messageData, EmoteCommand.processMessageVariables(messageData.messageText, this.getParentUsername(), (ChatSource)this));
         MessageDataIce messageIce = messageData.toIceObject();
         messageIce.messageText = EmoteCommand.processMessageVariables(messageData.messageText, (String)null, (ChatSource)this);
         this.groupChatPrx.putMessage(messageIce);
      }

      public void sendMessageToSender(MessageData messageData) throws FusionException {
         this.sessionI.sendMessageBackToUserAsEmote(messageData, EmoteCommand.processMessageVariables(messageData.messageText, this.getParentUsername(), (ChatSource)this));
      }

      public PaidEmoteData.EmotePurchaseLocationEnum getEmotePurchaseLocation() {
         return PaidEmoteData.EmotePurchaseLocationEnum.GROUP_CHAT_COMMAND;
      }

      protected String getParentUsernameInternal() throws FusionException {
         return this.sessionI.getUsername();
      }

      public EmoteCommand.ResultType executeEmoteCommandWithState(String emoteCommand, MessageDataIce message) throws FusionException {
         return EmoteCommand.ResultType.fromValue(this.groupChatPrx.executeEmoteCommandWithState(emoteCommand, message, this.getSessionPrx()));
      }

      public void accept(ChatSourceVisitor visitor) throws FusionException {
         visitor.visit(this);
      }

      public void sendToSender(MessageDataIce messageDataIce) throws FusionException {
         this.sessionI.putMessage(messageDataIce);
      }

      public void sendToCounterParties(MessageDataIce messageDataIce) throws FusionException {
         this.groupChatPrx.putMessage(messageDataIce);
      }

      public ClientType getSenderDeviceType() {
         return this.sessionI.getDeviceType();
      }

      public short getSenderClientVersion() {
         return this.sessionI.getClientVersion();
      }
   }

   public static class RemoteChatRoomChatSource extends ChatSource {
      protected ChatSourceRoom chatRoom;

      public RemoteChatRoomChatSource(SessionPrx session, ChatSourceRoom chatRoom) {
         super(ChatSource.ChatType.CHATROOM_CHAT, (SessionPrx)session, null);
         this.chatRoom = chatRoom;
      }

      public boolean isUserInChat(String username) throws FusionException {
         return !StringUtil.isBlank(username) && this.chatRoom.isParticipant(username);
      }

      public boolean isUserVisibleInChat(String username) throws FusionException {
         return !StringUtil.isBlank(username) && this.chatRoom.isVisibleParticipant(username);
      }

      public String[] getVisibleUsernamesInChat(boolean includeParentUser) throws FusionException {
         return this.chatRoom.getParticipants(includeParentUser ? null : this.getParentUsername());
      }

      public String[] getAllUsernamesInChat(boolean includeParentUser) throws FusionException {
         return this.chatRoom.getAllParticipants(includeParentUser ? null : this.getParentUsername());
      }

      public void sendMessageToAllUsersInChat(MessageData messageData) throws FusionException {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHATROOM_SYNC_ENABLED)) {
            super.sendMessageToAllUsersInChat(messageData);
         }

         String messageText = messageData.messageText;
         MessageDataIce messageIce = messageData.toIceObject();
         messageIce.messageText = EmoteCommand.processMessageVariables(messageText, this.getParentUsername(), (ChatSource)this);
         this.sessionPrx.sendMessageBackToUserAsEmote(messageIce);
         messageIce = messageData.toIceObject();
         messageIce.messageText = EmoteCommand.processMessageVariables(messageText, (String)null, (ChatSource)this);
         this.chatRoom.putMessage(messageIce, this.sessionPrx.getSessionID());
      }

      public void sendMessageWithTruncationToAllUsersInChat(MessageData messageData, String subMessageToTruncate) throws FusionException {
         MessageDataIce messageIce = messageData.toIceObject();
         int maxLength = this.chatRoom.getMaximumMessageLength(this.getParentUsername());
         messageIce.messageText = this.getTruncatedMessage(messageData.messageText, subMessageToTruncate, maxLength, this.getParentUsername());
         this.sessionPrx.sendMessageBackToUserAsEmote(messageIce);
         messageIce = messageData.toIceObject();
         messageIce.messageText = this.getTruncatedMessage(messageData.messageText, subMessageToTruncate, maxLength, (String)null);
         this.chatRoom.putMessage(messageIce, this.sessionPrx.getSessionID());
      }

      public void sendMessageToSender(MessageData messageData) throws FusionException {
         String messageText = messageData.messageText;
         MessageDataIce messageIce = messageData.toIceObject();
         messageIce.messageText = EmoteCommand.processMessageVariables(messageText, this.getParentUsername(), (ChatSource)this);
         this.sessionPrx.sendMessageBackToUserAsEmote(messageIce);
      }

      public PaidEmoteData.EmotePurchaseLocationEnum getEmotePurchaseLocation() {
         return PaidEmoteData.EmotePurchaseLocationEnum.CHATROOM_COMMAND;
      }

      protected String getParentUsernameInternal() throws FusionException {
         return this.sessionPrx.getParentUsername();
      }

      public void accept(ChatSourceVisitor visitor) throws FusionException {
         visitor.visit(this);
      }

      public void sendToSender(MessageDataIce messageDataIce) throws FusionException {
         this.sessionPrx.putMessage(messageDataIce);
      }

      public void sendToCounterParties(MessageDataIce messageDataIce) throws FusionException {
         this.chatRoom.putMessage(messageDataIce, this.sessionI.getSessionID());
      }

      public ClientType getSenderDeviceType() {
         return ClientType.fromValue(this.sessionPrx.getDeviceTypeAsInt());
      }

      public short getSenderClientVersion() {
         return this.sessionPrx.getClientVersionIce();
      }
   }

   public static class LocalChatRoomChatSource extends ChatSource {
      protected ChatRoomPrx chatRoomPrx;

      LocalChatRoomChatSource(ChatSourceSession session, ChatRoomPrx chatRoomPrx) {
         super(ChatSource.ChatType.CHATROOM_CHAT, (ChatSourceSession)session, null);
         this.chatRoomPrx = chatRoomPrx;
      }

      public boolean isUserInChat(String username) throws FusionException {
         return !StringUtil.isBlank(username) && this.chatRoomPrx.isParticipant(username);
      }

      public boolean isUserVisibleInChat(String username) throws FusionException {
         return !StringUtil.isBlank(username) && this.chatRoomPrx.isVisibleParticipant(username);
      }

      public String[] getVisibleUsernamesInChat(boolean includeParentUser) throws FusionException {
         return this.chatRoomPrx.getParticipants(includeParentUser ? null : this.getParentUsername());
      }

      public String[] getAllUsernamesInChat(boolean includeParentUser) throws FusionException {
         return this.chatRoomPrx.getAllParticipants(includeParentUser ? null : this.getParentUsername());
      }

      public void sendMessageToAllUsersInChat(MessageData messageData) throws FusionException {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHATROOM_SYNC_ENABLED)) {
            super.sendMessageToAllUsersInChat(messageData);
         }

         MessageDataIce messageIce = messageData.toIceObject();
         this.sessionI.sendMessageBackToUserAsEmote(messageData, EmoteCommand.processMessageVariables(messageData.messageText, this.getParentUsername(), (ChatSource)this));
         messageIce.messageText = EmoteCommand.processMessageVariables(messageData.messageText, (String)null, (ChatSource)this);
         this.chatRoomPrx.putMessage(messageIce, this.sessionI.getSessionID());
      }

      public void sendMessageWithTruncationToAllUsersInChat(MessageData messageData, String subMessageToTruncate) throws FusionException {
         MessageDataIce messageIce = messageData.toIceObject();
         int maxLength = this.chatRoomPrx.getMaximumMessageLength(this.getParentUsername());
         this.sessionI.sendMessageBackToUserAsEmote(messageData, this.getTruncatedMessage(messageData.messageText, subMessageToTruncate, maxLength, this.getParentUsername()));
         messageIce.messageText = this.getTruncatedMessage(messageData.messageText, subMessageToTruncate, maxLength, (String)null);
         this.chatRoomPrx.putMessage(messageIce, this.sessionI.getSessionID());
      }

      public void sendMessageToSender(MessageData messageData) throws FusionException {
         this.sessionI.sendMessageBackToUserAsEmote(messageData, EmoteCommand.processMessageVariables(messageData.messageText, this.getParentUsername(), (ChatSource)this));
      }

      public PaidEmoteData.EmotePurchaseLocationEnum getEmotePurchaseLocation() {
         return PaidEmoteData.EmotePurchaseLocationEnum.CHATROOM_COMMAND;
      }

      protected String getParentUsernameInternal() throws FusionException {
         return this.sessionI.getUsername();
      }

      public EmoteCommand.ResultType executeEmoteCommandWithState(String emoteCommand, MessageDataIce message) throws FusionException {
         return EmoteCommand.ResultType.fromValue(this.chatRoomPrx.executeEmoteCommandWithState(emoteCommand, message, this.getSessionPrx()));
      }

      public void accept(ChatSourceVisitor visitor) throws FusionException {
         visitor.visit(this);
      }

      public ChatRoomPrx getChatRoomPrx() {
         return this.chatRoomPrx;
      }

      public void sendToSender(MessageDataIce messageDataIce) throws FusionException {
         this.sessionI.putMessage(messageDataIce);
      }

      public void sendToCounterParties(MessageDataIce messageDataIce) throws FusionException {
         this.chatRoomPrx.putMessage(messageDataIce, this.sessionI.getSessionID());
      }

      public ClientType getSenderDeviceType() {
         return this.sessionI.getDeviceType();
      }

      public short getSenderClientVersion() {
         return this.sessionI.getClientVersion();
      }
   }

   public static enum ChatSourceType {
      LOCAL_SESSION,
      REMOTE_SESSION;
   }

   public static enum ChatType {
      PRIVATE_CHAT(1),
      GROUP_CHAT(2),
      CHATROOM_CHAT(4);

      private int value;

      private ChatType(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static ChatSource.ChatType fromValue(int value) {
         ChatSource.ChatType[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ChatSource.ChatType e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }

      public static ChatSource.ChatType fromDestinationType(MessageDestinationData.TypeEnum messageDestinationType) {
         if (messageDestinationType == MessageDestinationData.TypeEnum.INDIVIDUAL) {
            return PRIVATE_CHAT;
         } else if (messageDestinationType == MessageDestinationData.TypeEnum.CHAT_ROOM) {
            return CHATROOM_CHAT;
         } else {
            return messageDestinationType == MessageDestinationData.TypeEnum.GROUP ? GROUP_CHAT : null;
         }
      }

      public static int or(ChatSource.ChatType... types) {
         int v = 0;

         for(int i = 0; i < types.length; ++i) {
            v |= types[i].value();
         }

         return v;
      }

      public static int allTypes() {
         return or(values());
      }

      public boolean isSupported(int types) {
         return (types & this.value) == this.value;
      }
   }
}
