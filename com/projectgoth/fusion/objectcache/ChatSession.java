package com.projectgoth.fusion.objectcache;

import Ice.LocalException;
import Ice.ObjectNotExistException;
import com.projectgoth.fusion.chat.CoreChatStats;
import com.projectgoth.fusion.chatsync.ChatList;
import com.projectgoth.fusion.chatsync.MessageStatusEvent;
import com.projectgoth.fusion.clientsession.SSOLogin;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.data.FileData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.emote.ChatSource;
import com.projectgoth.fusion.emote.EmoteCommand;
import com.projectgoth.fusion.emote.EmoteCommandFactory;
import com.projectgoth.fusion.exception.GroupChatNotMemberException;
import com.projectgoth.fusion.exception.UserNotOnlineException;
import com.projectgoth.fusion.fdl.enums.ChatParticipantType;
import com.projectgoth.fusion.fdl.enums.ChatUserStatusType;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.gateway.packet.FusionPktChatRoomUserStatusOld;
import com.projectgoth.fusion.gateway.packet.FusionPktChatroomUserStatus;
import com.projectgoth.fusion.gateway.packet.FusionPktGroupChatParticipants;
import com.projectgoth.fusion.gateway.packet.FusionPktGroupChatParticipantsOld;
import com.projectgoth.fusion.gateway.packet.FusionPktGroupChatUserStatus;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.messagelogger.MessageToLog;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardDispatcher;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.ChatRoomPrxHelper;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.CurrencyDataIce;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.Message;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.SessionIce;
import com.projectgoth.fusion.slice.SessionMetricsIce;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserEventIce;
import com.projectgoth.fusion.slice.UserPrx;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;

public class ChatSession implements ChatSourceSession {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatSession.class));
   private String sessionID;
   private long timeLastTouched;
   private long timeCreated;
   private final int maxMessageSize;
   private int chatListVersion = -1;
   private final ChatSessionMetrics metrics;
   ChatSession.ChatSessionRooms chatRooms;
   private Enums.ConnectionEnum connectionType;
   private final ClientType deviceType;
   private final short clientVersion;
   private String language;
   private ImType imType;
   private int port;
   private int remotePort;
   private String remoteAddress;
   private String mobileDevice;
   private String userAgent;
   private PresenceType presence;
   private ChatConnection chatConnection;
   ChatUser user;
   private ChatObjectManagerSession objectManager;

   public static ChatSession create(ChatObjectManagerSession objectManager, ChatUser chatUser, String sessionID, int presence, int deviceType, int connectionType, int imType, int port, int remotePort, String remoteAddress, String mobileDevice, String userAgent, short clientVersion, String language, ConnectionPrx connectionProxy) {
      ChatSession session = new ChatSession(objectManager, chatUser, sessionID, PresenceType.fromValue(presence), ClientType.fromValue(deviceType), Enums.ConnectionEnum.fromValue(connectionType), ImType.fromValue(imType), port, remotePort, remoteAddress, mobileDevice, userAgent, clientVersion, language, connectionProxy);
      return session;
   }

   public static ChatSession create(ChatObjectManagerSession objectManager, ChatUser chatUser, ConnectionPrx connectionProxy, ChatSessionState state) {
      ChatSession session = new ChatSession(objectManager, chatUser, state.sessionID, PresenceType.fromValue(state.presence), ClientType.fromValue(state.deviceType), Enums.ConnectionEnum.fromValue(state.connectionType), ImType.fromValue(state.imType), state.port, state.remotePort, state.remoteAddress, state.mobileDevice, state.userAgent, state.clientVersion, state.language, connectionProxy);
      return session;
   }

   public ChatSession(ChatObjectManagerSession objectManager, ChatUser user, String sessionID, PresenceType presence, ClientType deviceType, Enums.ConnectionEnum connectionType, ImType imType, int port, int remotePort, String remoteAddress, String mobileDevice, String userAgent, short clientVersion, String language, ConnectionPrx connectionProxy) {
      this.objectManager = objectManager;
      this.user = user;
      this.sessionID = sessionID;
      this.chatConnection = new ChatConnection(connectionProxy);
      this.metrics = new ChatSessionMetrics(objectManager.getRequestCounter());
      this.chatRooms = new ChatSession.ChatSessionRooms(objectManager);
      this.presence = presence;
      this.deviceType = deviceType;
      this.connectionType = connectionType;
      this.imType = imType;
      this.port = port;
      this.remotePort = remotePort;
      this.remoteAddress = remoteAddress;
      this.mobileDevice = mobileDevice;
      this.userAgent = userAgent;
      this.clientVersion = clientVersion;
      this.language = language;
      this.timeLastTouched = this.timeCreated = System.currentTimeMillis();
      this.maxMessageSize = objectManager.getProperties().getPropertyAsIntWithDefault("MaxMessageSize", 320);
   }

   public String getSessionID() {
      return this.sessionID;
   }

   public String getUsername() {
      return this.user.getUsername();
   }

   public void touch() {
      this.timeLastTouched = System.currentTimeMillis();
   }

   public long getTimeLastTouched() {
      return this.timeLastTouched;
   }

   public PresenceType getPresence() {
      return this.presence;
   }

   public void metricsProfileEdited() {
      this.metrics.profileEdited();
   }

   public SessionMetricsIce getSessionMetrics() {
      return this.metrics.getSessionMetricsIce();
   }

   public boolean hasPrivateChattedWith(String username) {
      return this.metrics.hasPrivateChattedWith(username);
   }

   public void disconnect(String reason) throws FusionException {
      try {
         this.chatConnection.disconnect(reason);

         try {
            SSOLogin.removeDataFromMemcache(this.sessionID);
         } catch (Exception var3) {
         }
      } catch (ObjectNotExistException var4) {
         if (log.isDebugEnabled()) {
            log.debug("failed to disconnect user ", var4);
         }
      }

      this.user.endSession(this);
   }

   public void endSession() {
      if (log.isDebugEnabled()) {
         log.debug("ending session [" + this.sessionID + "] for user [" + this.getUsername() + "]");
      }

      this.chatRooms.endSession(this.getUsername());
      this.user.endSession(this);
      this.metrics.request();
   }

   public boolean isMobileClientV2AndNewVersionOrAjax() {
      return ClientType.isMobileClientV2AndNewVersionOrAjax(this.deviceType, this.clientVersion);
   }

   public void otherIMLoggedIn(int imType) throws FusionException {
      this.chatConnection.otherIMLoggedIn(imType);
   }

   public void otherIMLoggedOut(int imType, String reason) throws FusionException {
      this.chatConnection.otherIMLoggedOut(imType, reason);
   }

   public void contactAdded(ContactData contact, int contactListVersion, boolean guaranteedIsNew) throws Exception {
      this.chatConnection.contactAdded(contact, contactListVersion, guaranteedIsNew);
   }

   public void contactRemoved(int contactID, int contactListVersion) throws Exception {
      this.chatConnection.contactRemoved(contactID, contactListVersion);
   }

   public void contactChangedPresence(int contactID, int imType, int presence) throws Exception {
      this.chatConnection.contactChangedPresence(contactID, imType, presence);
   }

   public void contactChangedDisplayPicture(int contactID, String displayPicture, long timeStamp) throws Exception {
      this.chatConnection.contactChangedDisplayPicture(contactID, displayPicture, timeStamp);
   }

   public void contactChangedStatusMessage(int contactID, String statusMessage, long timeStamp) throws Exception {
      this.chatConnection.contactChangedStatusMessage(contactID, statusMessage, timeStamp);
   }

   public void contactRequest(String username, int outstandingRequests) throws Exception {
      this.chatConnection.contactRequest(username, outstandingRequests);
   }

   public void contactRequestAccepted(ContactData contact, int contactListVersion, int outstandingRequests) throws Exception {
      this.chatConnection.contactRequestAccepted(contact, contactListVersion, outstandingRequests);
   }

   public void contactRequestRejected(String username, int outstandingRequests) throws Exception {
      this.chatConnection.contactRequestRejected(username, outstandingRequests);
   }

   public void contactGroupAdded(ContactGroupData contactGroup, int contactListVersion) throws FusionException {
      this.chatConnection.contactGroupAdded(contactGroup, contactListVersion);
   }

   public void contactGroupRemoved(int contactGroupID, int contactListVersion) throws FusionException {
      this.chatConnection.contactGroupRemoved(contactGroupID, contactListVersion);
   }

   public void avatarChanged(String displayPicture, String statusMessage) throws FusionException {
      this.chatConnection.avatarChanged(displayPicture, statusMessage);
   }

   public void emoticonsChanged(String[] hotKeys, String[] alternateKeys) throws FusionException {
      this.chatConnection.emoticonsChanged(hotKeys, alternateKeys);
   }

   public void otherIMConferenceCreated(ImType imType, String conferenceID, String creator) throws FusionException {
      this.chatConnection.otherIMConferenceCreated(imType, conferenceID, creator);
   }

   public void privateChatNowAGroupChat(String groupChatID, String creator) throws FusionException {
      this.chatConnection.privateChatNowAGroupChat(groupChatID, creator);
   }

   public void accountBalanceChanged(double balance, double fundedBalance, CurrencyDataIce currency) throws FusionException {
      this.chatConnection.accountBalanceChanged(balance, fundedBalance, currency);
   }

   public void putAnonymousCallNotification(String requestingUsername, String requestingMobilePhone) throws FusionException {
      this.chatConnection.putAnonymoustCallNotification(requestingUsername, requestingMobilePhone);
   }

   public void putEvent(UserEventIce event) throws FusionException {
      this.chatConnection.putEvent(event);
   }

   public void putAlertMessage(String message, String title, short timeout) throws FusionException {
      this.chatConnection.putAlertMessage(message, title, timeout);
   }

   public void putAlertMessageOneWay(String message, String title, short timeout) {
      this.chatConnection.putAlertMessageOneWay(message, title, timeout);
   }

   public void putServerQuestion(String message, String url) throws FusionException {
      this.chatConnection.putServerQuestion(message, url);
   }

   public void putFileReceived(MessageDataIce message) throws FusionException {
      this.chatConnection.putFileReceived(message);
   }

   public void putSerializedPacket(byte[] packet) throws FusionException {
      this.chatConnection.putSerializedPacket(packet);
   }

   public void putSerializedPacketOneWay(byte[] packet) {
      this.chatConnection.putSerializedPacketOneWay(packet);
   }

   public void putMessage(MessageDataIce message) throws FusionException {
      if (this.user.validatePutMessage(message)) {
         try {
            this.chatConnection.putMessage(message);
         } catch (LocalException var3) {
            throw new FusionException("The user is no longer connected");
         }

         this.metrics.request();
      }
   }

   public void putMessageOneWay(MessageDataIce message) {
      if (this.user.validatePutMessage(message)) {
         try {
            this.chatConnection.putMessageOneWay(message);
         } catch (LocalException var3) {
         }

         this.metrics.request();
      }
   }

   public void putMessageLocal(MessageDataIce message) throws FusionException {
      try {
         this.chatConnection.putMessage(message);
      } catch (LocalException var3) {
         throw new FusionException("The user is no longer connected");
      }

      this.metrics.request();
   }

   public void notifyUserLeftChatRoomOneWay(String chatroomname, String username) {
      if (this.isMobileClientV2AndNewVersionOrAjax()) {
         if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week3UseAutogeneratedPacketsEnabled.getValue()) {
            FusionPktChatroomUserStatus pkt = new FusionPktChatroomUserStatus();
            pkt.setChatroomName(chatroomname);
            pkt.setUsername(username);
            pkt.setUserStatus(ChatUserStatusType.LEFT);

            try {
               this.putSerializedPacketOneWay(pkt.toSerializedBytes());
            } catch (Exception var6) {
            }
         } else {
            FusionPktChatRoomUserStatusOld pkt = new FusionPktChatRoomUserStatusOld();
            pkt.setChatRoomName(chatroomname);
            pkt.setFusionUserName(username);
            pkt.setUserStatusType(FusionPktChatRoomUserStatusOld.UserStatusTypeEnum.LEFT.value());

            try {
               this.putSerializedPacketOneWay(pkt.toSerializedBytes());
            } catch (Exception var5) {
            }
         }
      }

   }

   public void notifyUserJoinedChatRoomOneWay(String chatroomname, String username, boolean isAdministrator, boolean isMuted) {
      if (this.isMobileClientV2AndNewVersionOrAjax()) {
         if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week3UseAutogeneratedPacketsEnabled.getValue()) {
            FusionPktChatroomUserStatus pkt = new FusionPktChatroomUserStatus();
            pkt.setChatroomName(chatroomname);
            pkt.setUsername(username);
            pkt.setUserStatus(ChatUserStatusType.JOINED);
            ChatParticipantType participantType = isAdministrator ? ChatParticipantType.ADMINISTRATOR : (isMuted ? ChatParticipantType.MUTED : ChatParticipantType.NORMAL);
            pkt.setParticipantType(participantType);

            try {
               this.putSerializedPacketOneWay(pkt.toSerializedBytes());
            } catch (Exception var9) {
            }
         } else {
            FusionPktChatRoomUserStatusOld pkt = new FusionPktChatRoomUserStatusOld();
            pkt.setChatRoomName(chatroomname);
            pkt.setFusionUserName(username);
            pkt.setUserStatusType(FusionPktChatRoomUserStatusOld.UserStatusTypeEnum.JOIN.value());
            pkt.isAdministrator(isAdministrator);
            pkt.isMuted(isMuted);

            try {
               this.putSerializedPacketOneWay(pkt.toSerializedBytes());
            } catch (Exception var8) {
            }
         }
      }

   }

   public void notifyUserLeftGroupChat(String groupChatId, String username) throws FusionException {
      if (this.isMobileClientV2AndNewVersionOrAjax()) {
         FusionPktGroupChatUserStatus pkt = new FusionPktGroupChatUserStatus();
         pkt.setChatRoomName(groupChatId);
         pkt.setFusionUserName(username);
         pkt.setUserStatusType(FusionPktGroupChatUserStatus.UserStatusTypeEnum.LEFT.value());
         this.putSerializedPacket(pkt.toSerializedBytes());
      }

   }

   public void notifyUserJoinedGroupChat(String groupChatId, String username, boolean isMuted) throws FusionException {
      if (this.isMobileClientV2AndNewVersionOrAjax()) {
         FusionPktGroupChatUserStatus pkt = new FusionPktGroupChatUserStatus();
         pkt.setChatRoomName(groupChatId);
         pkt.setFusionUserName(username);
         pkt.setUserStatusType(FusionPktGroupChatUserStatus.UserStatusTypeEnum.JOIN.value());
         pkt.isMuted(isMuted);
         this.putSerializedPacket(pkt.toSerializedBytes());
      }

   }

   public void sendGroupChatParticipantArrays(String groupChatId, byte imType, String[] participants, String[] mutedParticipants) throws FusionException {
      if (this.isMobileClientV2AndNewVersionOrAjax()) {
         FusionPktGroupChatParticipants pkt = new FusionPktGroupChatParticipants();
         pkt.setGroupChatId(groupChatId);
         pkt.setImType(ImType.fromValue(imType));
         pkt.setParticipantList(participants);
         pkt.setMutedList(mutedParticipants);
         this.putSerializedPacket(pkt.toSerializedBytes());
      }

   }

   public void sendGroupChatParticipants(String groupChatId, byte imType, String participants, String mutedParticipants) throws FusionException {
      if (this.isMobileClientV2AndNewVersionOrAjax()) {
         FusionPktGroupChatParticipantsOld pkt = new FusionPktGroupChatParticipantsOld();
         pkt.setGroupChatId(groupChatId);
         pkt.setIMType(imType);
         pkt.setParticipants(participants + ";");
         pkt.setMutedParticipants(mutedParticipants + ";");
         this.putSerializedPacket(pkt.toSerializedBytes());
      }

   }

   public void pushNotification(Message message) throws FusionException {
      this.chatConnection.pushNotification(message);
   }

   public void emailNotification(int unreadEmailCount) throws FusionException {
      this.chatConnection.emailNotification(unreadEmailCount);
   }

   public void chatroomJoined(ChatRoomPrx roomProxy, String name) {
      this.metrics.chatRoomJoined(name);
      int roomCount = this.chatRooms.join(roomProxy, name);
      if (log.isDebugEnabled()) {
         log.debug("added chatroom proxy [" + roomProxy + "] to session [" + this.sessionID + "], now we have " + roomCount + " proxies");
      }

   }

   private String storeImage(String sender, byte[] image) throws Exception {
      return this.objectManager.getFileStore().storeImage(sender, image);
   }

   public void setPresence(int presence) throws FusionException {
      PresenceType newPresence = PresenceType.fromValue(presence);
      if (this.presence != newPresence) {
         this.presence = newPresence;
         if (this.imType == ImType.FUSION) {
            this.user.updateOverallFusionPresence();
         }

         this.metrics.request();
      }
   }

   public void sendOfflineMessageStoredConfirmation(MessageData messageData, String destinationUsername) {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Sending storage confirmation back to OLM sender " + destinationUsername);
         }

         MessageDataIce messageClone = messageData.toIceObject();
         messageClone.contentType = MessageData.ContentTypeEnum.TEXT.value();
         messageClone.source = ((MessageDestinationData)messageData.messageDestinations.get(0)).destination;
         messageClone.messageDestinations[0].destination = messageData.source;
         messageClone.messageText = "Your message for me has been stored as an offline message";
         this.putMessage(messageClone);
         if (log.isDebugEnabled()) {
            log.debug("Sent storage confirmation back to OLM sender " + destinationUsername);
         }
      } catch (Exception var4) {
         log.error("Exception sending OLM confirmation back to OLM sender " + destinationUsername + " (but OLM stored ok)", var4);
      }

   }

   public void sendMessage(MessageDataIce message) throws FusionException {
      if (message.contentType == MessageData.ContentTypeEnum.TEXT.value()) {
         ChatPrivacyController.truncateMessageText(message, this.maxMessageSize);
         if (ChatPrivacyController.containsBlacklistedPatterns(message.messageText)) {
            String reason = SystemProperty.get("TempLoginSuspensionErrorMessage", "Can't connect with this client right now. Please try again later.");
            this.user.disconnectAndSuspend(reason, message.messageText);
            return;
         }

         if (!ChatPrivacyController.scrubMessageText(message.messageText, this.user.getPassword())) {
            return;
         }

         ChatPrivacyController.cleanMessageText(message);
      }

      switch(MessageType.fromValue(message.type)) {
      case FUSION:
         this.sendFusionMessage(message);
         break;
      case MSN:
      case YAHOO:
      case AIM:
      case GTALK:
      case FACEBOOK:
         this.user.sessionSendOtherIMMessage(this, message);
         break;
      default:
         FusionException fe = new FusionException();
         fe.message = "Message type " + MessageType.fromValue(message.type).toString() + " is not supported";
         log.warn(fe.message);
         throw fe;
      }

      this.metrics.request();
   }

   public void sendMessageBackToUserAsEmote(MessageData originalMessageData, String text) throws FusionException {
      MessageDataIce messageIce = originalMessageData.toIceObject();
      if (text != null) {
         messageIce.messageText = text;
      }

      this.sendMessageBackToUserAsEmote(messageIce);
   }

   public void sendMessageBackToUserAsEmote(MessageDataIce message) throws FusionException {
      message.contentType = MessageData.ContentTypeEnum.EMOTE.value();
      if (message.messageDestinations[0].type == MessageDestinationData.TypeEnum.INDIVIDUAL.value()) {
         String tmp = message.source;
         message.source = message.messageDestinations[0].destination;
         message.messageDestinations[0].destination = tmp;
      }

      this.putMessage(message);
   }

   public boolean isMessageThumbnailSupportedByDevice() {
      return this.deviceType.value() == ClientType.ANDROID.value() && this.clientVersion >= 300 || this.deviceType.value() == ClientType.BLACKBERRY.value() || this.deviceType.value() == ClientType.MRE.value() || this.deviceType.value() == ClientType.IOS.value();
   }

   private void sendFusionMessage(MessageDataIce message) throws FusionException {
      long startMillis = System.currentTimeMillis();

      try {
         MessageDestinationData.TypeEnum messageDestinationType = this.user.sessionSendFusionMessage(this, message);
         if (messageDestinationType == MessageDestinationData.TypeEnum.INDIVIDUAL) {
            this.sendFusionMessageToIndividual(message);
         } else if (messageDestinationType == MessageDestinationData.TypeEnum.CHAT_ROOM) {
            this.sendFusionMessageToChatRoom(message);
         } else {
            if (messageDestinationType != MessageDestinationData.TypeEnum.GROUP) {
               FusionException fe = new FusionException("The destination type " + MessageDestinationData.TypeEnum.fromValue(message.messageDestinations[0].type) + " for FUSION messages is not supported");
               log.warn(fe.message);
               throw fe;
            }

            this.sendFusionMessageToGroupChat(message);
         }
      } finally {
         CoreChatStats.getInstance().addSendFusionMessageWallclockTime(System.currentTimeMillis() - startMillis);
      }

   }

   private boolean handlePrivateChatCommand(MessageData messageData, String destinationUsername, boolean sendAsOfflineMessage) throws Exception {
      String[] args = messageData.messageText.toLowerCase().split(" ");
      String command = args[0].substring(1);
      EmoteCommand ec = EmoteCommandFactory.getEmoteCommand(command, ChatSource.ChatType.fromDestinationType(((MessageDestinationData)messageData.messageDestinations.get(0)).type), this.objectManager.getIcePrxFinder());
      if (ec != null) {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.OFFLINE_V2_EMOTES_ENABLED)) {
            if (sendAsOfflineMessage && !ec.getEmoteCommandData().isOfflineEnabled()) {
               throw new FusionException("This type of emote not supported yet for offline messaging");
            }
         } else if (sendAsOfflineMessage) {
            throw new FusionException("This type of emote not supported yet for offline messaging");
         }

         EmoteCommand.ResultType resultType = ec.execute(messageData, this, (String)destinationUsername);
         if (resultType == EmoteCommand.ResultType.HANDLED_AND_STOP) {
            return true;
         }

         if (resultType == EmoteCommand.ResultType.HANDLED_AND_CONTINUE) {
            return false;
         }
      }

      messageData.contentType = MessageData.ContentTypeEnum.EMOTE;
      messageData.messageText = (new Emote(messageData.source, messageData.messageText, destinationUsername)).toString();
      this.sendMessageBackToUserAsEmote(messageData, messageData.messageText);
      return false;
   }

   private boolean handleChatRoomCommand(MessageData messageData, ChatRoomPrx chatRoomPrx) throws Exception {
      String[] args = messageData.messageText.toLowerCase().split(" ");
      String command = args[0].substring(1);
      EmoteCommand ec = EmoteCommandFactory.getEmoteCommand(command, ChatSource.ChatType.fromDestinationType(((MessageDestinationData)messageData.messageDestinations.get(0)).type), this.objectManager.getIcePrxFinder());
      if (ec != null) {
         EmoteCommand.ResultType resultType = ec.execute(messageData, this, (ChatRoomPrx)chatRoomPrx);
         if (resultType == EmoteCommand.ResultType.HANDLED_AND_STOP) {
            return true;
         }

         if (resultType == EmoteCommand.ResultType.HANDLED_AND_CONTINUE) {
            return false;
         }
      }

      messageData.contentType = MessageData.ContentTypeEnum.EMOTE;
      messageData.messageText = (new Emote(messageData.source, messageData.messageText, chatRoomPrx)).toString();
      this.putMessage(messageData.toIceObject());
      return false;
   }

   private boolean handleGroupChatCommand(MessageData messageData, GroupChatPrx groupChatPrx) throws Exception {
      String[] args = messageData.messageText.toLowerCase().split(" ");
      String command = args[0].substring(1);
      EmoteCommand ec = EmoteCommandFactory.getEmoteCommand(command, ChatSource.ChatType.fromDestinationType(((MessageDestinationData)messageData.messageDestinations.get(0)).type), this.objectManager.getIcePrxFinder());
      if (ec != null) {
         EmoteCommand.ResultType resultType = ec.execute(messageData, this, (GroupChatPrx)groupChatPrx);
         if (resultType == EmoteCommand.ResultType.HANDLED_AND_STOP) {
            return true;
         }

         if (resultType == EmoteCommand.ResultType.HANDLED_AND_CONTINUE) {
            return false;
         }
      }

      messageData.contentType = MessageData.ContentTypeEnum.EMOTE;
      messageData.messageText = (new Emote(messageData.source, messageData.messageText, groupChatPrx)).toString();
      this.putMessage(messageData.toIceObject());
      return false;
   }

   public void themeChanged(String themeLocation) throws FusionException {
      this.chatConnection.themeChanged(themeLocation);
   }

   public void setLanguage(String language) {
      this.language = language;
   }

   public String getLanguage() {
      return this.language;
   }

   private Enums.ConnectionEnum getConnectionType() {
      return this.connectionType;
   }

   private ImType getImType() {
      return this.imType;
   }

   private int getPort() {
      return this.port;
   }

   private int getRemotePort() {
      return this.remotePort;
   }

   public String getRemoteAddress() {
      return this.remoteAddress;
   }

   public String getMobileDevice() {
      return this.mobileDevice;
   }

   public String getUserAgent() {
      return this.userAgent;
   }

   private void setPresence(PresenceType presence) {
      this.presence = presence;
   }

   private long getTimeCreated() {
      return this.timeCreated;
   }

   public short getClientVersion() {
      return this.clientVersion;
   }

   public ClientType getDeviceType() {
      return this.deviceType;
   }

   public void setChatListVersion(int version) {
      this.chatListVersion = version;
   }

   public int getChatListVersion() {
      return this.chatListVersion;
   }

   public SessionIce getSessionLog() {
      SessionIce sessionIce = new SessionIce();
      sessionIce.clientVersion = this.getClientVersion();
      sessionIce.connectionType = this.getConnectionType().value();
      sessionIce.deviceType = this.getDeviceType().value();
      sessionIce.startDateTime = this.getTimeCreated();
      sessionIce.endDateTime = System.currentTimeMillis();
      sessionIce.ipAddress = this.getRemoteAddress();
      String sessionLanguage = this.getLanguage();
      if (sessionLanguage != null && sessionLanguage.length() > 5) {
         sessionLanguage = sessionLanguage.substring(0, 5);
      }

      sessionIce.language = sessionLanguage;
      sessionIce.mobileDevice = this.getMobileDevice();
      sessionIce.port = this.getPort();
      sessionIce.remotePort = this.getRemotePort();
      return sessionIce;
   }

   public void putWebCallNotification(String source, String destination, int gateway, String gatewayName, int protocol) throws FusionException {
      this.chatConnection.putWebCallNotification(source, destination, gateway, gatewayName, protocol);
   }

   public void silentlyDropIncomingPackets() {
      this.chatConnection.silentlyDropIncomingPackets();
   }

   public void metricsFriendInvitedByPhoneNumber() {
      this.metrics.inviteByPhoneNumber();
   }

   public void metricsFriendInvitedByUsername() {
      this.metrics.inviteByUsername();
   }

   public void metricsGroupChatJoined(String id) {
      this.metrics.groupChatsEntered(1);
   }

   public void metricsGroupChatJoined(String id, int increment) {
      this.metrics.groupChatsEntered(increment);
   }

   public void metricsPhotoUploaded() {
      this.metrics.photoUploaded();
   }

   public void metricsStatusMessageSet() {
      this.metrics.statusMessageSet();
   }

   public void metricsThemeUpdated() {
      if (log.isDebugEnabled()) {
         log.debug("incrementing theme updated count for [" + this.getUsername() + "]");
      }

      this.metrics.themeUpdated();
   }

   public int getUserID() {
      return this.user.getUserID();
   }

   public UserDataIce getUserData() {
      return this.user.getUserData();
   }

   public void logEmoteData(ChatRoomEmoteLogData data) {
      try {
         com.projectgoth.fusion.interfaces.Message messageEJB = (com.projectgoth.fusion.interfaces.Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         messageEJB.addChatRoomEmoteLog(data);
      } catch (Exception var3) {
         log.error("Failed to log emote action: " + data.toString() + ", " + var3.getMessage());
      }

   }

   public UserData.TypeEnum getUserType() {
      return this.user.getUserType();
   }

   public double getBalance() {
      return this.user.getBalance();
   }

   public ConnectionPrx getConnectionProxy() {
      return this.chatConnection.getConnectionProxy();
   }

   public SessionPrx findSessionPrx(String sessionID) {
      return this.objectManager.findSessionPrx(sessionID);
   }

   public UserPrx findUserPrx(String destinationUsername) throws FusionException {
      return this.objectManager.findUserPrx(destinationUsername);
   }

   public ChatRoomPrx findChatRoomPrx(String destination) throws FusionException {
      return this.objectManager.findChatRoomPrx(destination);
   }

   public GroupChatPrx findGroupChatPrx(String destination) throws FusionException {
      return this.objectManager.findGroupChatPrx(destination);
   }

   public IcePrxFinder getIcePrxFinder() {
      return this.objectManager.getIcePrxFinder();
   }

   public boolean isV3Device() {
      return this.deviceType.value() == ClientType.ANDROID.value() && this.clientVersion >= 300 || this.deviceType.value() == ClientType.BLACKBERRY.value() || this.deviceType.value() == ClientType.IOS.value();
   }

   private void sendFusionMessageToIndividual(MessageDataIce message) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("sendFusionMessageToIndividual");
      }

      String destinationUsername = message.messageDestinations[0].destination;
      String localUsername = this.user.getUsername();
      Integer localUserCountryID = this.user.getCountryID();
      if (ChatPrivacyController.validUsername(destinationUsername, message.source)) {
         UserPrx destUserPrx = null;
         MessageData messageData = new MessageData(message);
         boolean sendAsOfflineMessage = false;
         boolean senderOnRecipientContactList = false;
         Integer recipientId = null;
         Contact contactEJB = null;
         User userEJB = null;
         if (log.isDebugEnabled()) {
            log.debug("Checking if recipient " + destinationUsername + " has a UserPrx");
         }

         boolean userChatSyncEnabled;
         try {
            destUserPrx = this.objectManager.findUserPrx(destinationUsername);
            senderOnRecipientContactList = destUserPrx.isOnContactList(localUsername);
            if (log.isDebugEnabled() && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.SEND_ENABLED)) {
               log.debug("Recipient " + destinationUsername + " has a User entry in ObjectCache: " + destUserPrx);
            }
         } catch (FusionException var28) {
            if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.SEND_ENABLED) || message.contentType != MessageData.ContentTypeEnum.TEXT.value()) {
               throw var28;
            }

            if (log.isDebugEnabled()) {
               log.debug("UserPrx can't be got for recipient " + destinationUsername + ": " + var28);
            }

            try {
               ChatPrivacyController.doOfflineMessagingRateLimiting(localUsername, destinationUsername);
               contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
               ContactData cd = contactEJB.getContact(destinationUsername, localUsername);
               senderOnRecipientContactList = cd.id != null;
               userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
               recipientId = userEJB.getUserID(destinationUsername, (Connection)null);
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.OFFLINE_MESSAGE_GUARDSET_ENABLED)) {
                  sendAsOfflineMessage = ChatPrivacyController.checkOfflineMsgGuardset(this.user.getUserID(), this.user.getUsername(), messageData, destinationUsername, userEJB, recipientId);
               } else {
                  int minMigLevel = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.MIN_MIG_LEVEL_FOR_SENDING);
                  if (minMigLevel != 0) {
                     if (log.isDebugEnabled()) {
                        log.debug("OLM sending control by mig level enabled");
                     }

                     int senderMigLevel = this.user.getReputationDataLevel();
                     sendAsOfflineMessage = senderMigLevel >= minMigLevel;
                  } else {
                     if (log.isDebugEnabled()) {
                        log.debug("Can send offline messages: offline msging guardset is disabled and no control by mig level");
                     }

                     sendAsOfflineMessage = true;
                  }
               }

               if (sendAsOfflineMessage && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.CHECK_FOR_DEACTIVATED_RECIPIENT_ENABLED)) {
                  userChatSyncEnabled = userEJB.getAccountStatus(destinationUsername);
                  if (!userChatSyncEnabled) {
                     throw new FusionException(destinationUsername + "'s account is not active.");
                  }
               }
            } catch (FusionException var25) {
               throw var25;
            } catch (RemoteException var26) {
               if (var26.getCause() != null && var26.getCause().toString().contains("Invalid username")) {
                  ChatPrivacyController.onOfflineMessageToNonExistentRecipient(localUsername, destinationUsername);
               } else {
                  log.error("Exception handling offline recipient with offline msging feature switch on", var26);
               }

               throw new FusionException("The system was not able to store your offline message");
            } catch (Exception var27) {
               log.error("Exception handling offline recipient with offline msging feature switch on", var27);
               throw new FusionException("The system was not able to store your offline message");
            }
         }

         if (!ChatPrivacyController.dropMessageNotOnContactList(new UserData(this.user.getUserData()), senderOnRecipientContactList)) {
            ChatPrivacyController.mediaSharing(localUsername, destUserPrx, message.contentType);
            ChatPrivacyController.mediaSharing2(destUserPrx, localUsername, destinationUsername, message, contactEJB, userEJB);
            String messageText;
            if (destUserPrx != null) {
               int destinationPresence = destUserPrx.getOverallFusionPresence((String)null);
               if (PresenceType.BUSY.value() == destinationPresence) {
                  messageText = PresenceType.fromValue(destinationPresence).toString();
                  if (PresenceType.OFFLINE.value() == destinationPresence) {
                     messageData.messageText = destinationUsername + " is " + messageText + ". Your message was not delivered";
                  } else {
                     messageData.messageText = destinationUsername + " is " + messageText + ". You have to be in a chatroom / group chat with " + destinationUsername + " to chat with " + destinationUsername;
                  }

                  this.sendMessageBackToUserAsEmote(messageData, (String)null);
                  return;
               }
            }

            boolean isNewMimeMessage = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CoreChatSettings.MIME_TYPE_MESSAGES_ENABLED) && !StringUtil.iceIsBlank(message.mimeType);
            if (!isNewMimeMessage) {
               if (log.isDebugEnabled()) {
                  log.debug("Handling MIME message from sender=" + localUsername + " to recipient=" + destinationUsername);
               }

               if (destUserPrx != null && messageData.contentType != MessageData.ContentTypeEnum.TEXT) {
                  try {
                     if (!destUserPrx.supportsBinaryMessage()) {
                        throw new Exception(destinationUsername + " does not have picture sharing capability");
                     }

                     MIS misBean = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
                     if (messageData.contentType == MessageData.ContentTypeEnum.EXISTING_FILE) {
                        FileData fileData = misBean.getFile(messageData.messageText);
                        if (fileData == null) {
                           throw new Exception("Invalid file ID " + messageData.messageText);
                        }

                        messageData.binaryData = new byte[fileData.size];
                        messageData.contentType = MessageData.ContentTypeEnum.IMAGE;
                     } else {
                        messageData.messageText = this.storeImage(messageData.source, messageData.binaryData);
                     }

                     if (this.isV3Device()) {
                        this.putFileReceived(messageData.toIceObject());
                     }

                     destUserPrx.putFileReceived(messageData.toIceObject());
                     return;
                  } catch (LocalException var16) {
                     throw new UserNotOnlineException(destinationUsername);
                  } catch (Exception var17) {
                     throw new FusionException("Unable to send the picture. Reason: " + var17.getMessage());
                  }
               }

               try {
                  if (Emote.isEmote(messageData.messageText)) {
                     if (log.isDebugEnabled()) {
                        log.debug("Handling emote from sender=" + localUsername + " to recipient=" + destinationUsername);
                     }

                     if (this.handlePrivateChatCommand(messageData, destinationUsername, sendAsOfflineMessage)) {
                        return;
                     }
                  }
               } catch (FusionException var23) {
                  throw var23;
               } catch (Exception var24) {
                  throw new FusionException(var24.getMessage());
               }
            }

            message = messageData.toIceObject();
            this.user.applyMessageColor(message);

            try {
               if (destUserPrx != null) {
                  messageText = destUserPrx.getUserData().displayPicture;
               } else {
                  try {
                     messageText = userEJB.getDisplayPicture(destinationUsername);
                  } catch (Exception var21) {
                     throw new FusionException(var21.getMessage());
                  }
               }

               userChatSyncEnabled = this.switchBoardSendFusionMessageToIndividual(message, destinationUsername, this.metrics.getUniqueUsersPrivateChattedWith(), messageText);
               if (sendAsOfflineMessage) {
                  if (!userChatSyncEnabled && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.OLD_STYLE_OLM_STORAGE_ENABLED) && ChatPrivacyController.storeOfflineFusionMessageForIndividual(this.user.getUserID(), messageData, destinationUsername, recipientId)) {
                     this.sendOfflineMessageStoredConfirmation(messageData, destinationUsername);
                  }
               } else {
                  if (destUserPrx == null) {
                     throw new UserNotOnlineException(destinationUsername);
                  }

                  destUserPrx.putMessage(message);
               }

               this.metrics.privateMessageSent(destinationUsername);
            } catch (LocalException var22) {
               throw new UserNotOnlineException(destinationUsername);
            }

            if (ObjectCache.logMessagesToFile) {
               try {
                  this.objectManager.logMessage(MessageToLog.TypeEnum.PRIVATE, localUserCountryID, message.source, destinationUsername, 1, message.messageText);
               } catch (LocalException var20) {
                  log.warn("Unable to send a Fusion message to the MessageLogger application. Exception: " + var20.toString());
               }
            }

            if (message.contentType == MessageData.ContentTypeEnum.TEXT.value() && !this.metrics.hasPasswordWarning(destinationUsername)) {
               messageText = " " + message.messageText.toLowerCase() + " ";
               if (messageText.contains(" password ") || messageText.contains(" pwd ") || messageText.contains(" pass-word ") || messageText.contains(" pass word ") || messageText.contains(" passwd ")) {
                  message.contentType = MessageData.ContentTypeEnum.EMOTE.value();
                  message.messageText = "Warning do not give out passwords. migme does not ask for passwords through chat or IM";
                  if (ObjectCache.logMessagesToFile) {
                     try {
                        this.objectManager.logMessage(MessageToLog.TypeEnum.PRIVATE, localUserCountryID, message.source, destinationUsername, 1, message.messageText);
                     } catch (LocalException var19) {
                        log.warn("Unable to send a Fusion message to the MessageLogger application. Exception: " + var19.toString());
                     }
                  }

                  if (sendAsOfflineMessage) {
                     MessageData warningMsgData = new MessageData(message);
                     this.storeOfflineFusionMessageForIndividual(warningMsgData, destinationUsername, recipientId);
                  } else {
                     try {
                        destUserPrx.putMessage(message);
                     } catch (LocalException var18) {
                     }
                  }

                  this.metrics.passwordWarning(destinationUsername);
               }
            }

            this.user.rewardTriggerPrivateMessage();
         }
      }
   }

   public SessionPrx makeProxy() {
      return this.objectManager.makeSessionPrx(this.sessionID);
   }

   private void sendFusionMessageToChatRoom(MessageDataIce messageIce) throws FusionException {
      MessageSwitchboardDispatcher.getInstance().onSendFusionMessageToChatRoom(this.objectManager.getApplicationContext(), this.makeProxy(), this.user.makeProxy(), messageIce, messageIce.messageDestinations[0].destination, this.deviceType, this.clientVersion);
      if (messageIce.contentType != MessageData.ContentTypeEnum.TEXT.value()) {
         throw new FusionException("You cannot send a file to a chat room");
      } else {
         String chatRoomName = messageIce.messageDestinations[0].destination;
         ChatRoomPrx chatRoomPrx = this.objectManager.findChatRoomPrx(chatRoomName);
         if (!chatRoomPrx.isParticipant(this.user.getUsername())) {
            throw new FusionException("You are not in the chat room " + chatRoomName);
         } else {
            try {
               if (messageIce.messageText.startsWith("/")) {
                  MessageData messageData = new MessageData(messageIce);
                  if (this.handleChatRoomCommand(messageData, chatRoomPrx)) {
                     return;
                  }

                  messageIce = messageData.toIceObject();
               }
            } catch (FusionException var6) {
               throw var6;
            } catch (RemoteException var7) {
               throw new FusionException(RMIExceptionHelper.getRootMessage(var7));
            } catch (Exception var8) {
               throw new FusionException(var8.getMessage());
            }

            try {
               if (messageIce.messageText.startsWith("!")) {
                  if (SystemProperty.getBool("MerchantCannotPlayGame", false) && this.user.getUserType() == UserData.TypeEnum.MIG33_TOP_MERCHANT) {
                     throw new FusionException("Games are temporarily disabled for merchant accounts");
                  }

                  chatRoomPrx.sendMessageToBots(messageIce.source, messageIce.messageText, messageIce.requestReceivedTimestamp);
               } else {
                  chatRoomPrx.putMessage(messageIce, this.sessionID);
                  this.metrics.chatRoomMessageSent();
               }

            } catch (LocalException var5) {
               throw new FusionException("Unable to send message to the chat room '" + chatRoomName + "'");
            }
         }
      }
   }

   private void sendFusionMessageToGroupChat(MessageDataIce messageIce) throws FusionException {
      GroupChatPrx groupChatPrx = this.objectManager.findGroupChatPrx(messageIce.messageDestinations[0].destination);
      if (!groupChatPrx.isParticipant(this.user.getUsername())) {
         throw new GroupChatNotMemberException();
      } else {
         this.switchBoardSendFusionMessageToGroupChat(messageIce);
         MessageData messageData = new MessageData(messageIce);
         if (messageData.contentType != MessageData.ContentTypeEnum.TEXT) {
            try {
               if (!SystemProperty.getBool("SharingPhotosInGroupChatEnabled", false)) {
                  throw new Exception("Media sharing is not available right now...");
               } else if (!groupChatPrx.supportsBinaryMessage(messageData.source)) {
                  throw new Exception("There are no participants support picture sharing");
               } else {
                  MIS misBean = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
                  if (messageData.contentType == MessageData.ContentTypeEnum.EXISTING_FILE) {
                     FileData fileData = misBean.getFile(messageData.messageText);
                     if (fileData == null) {
                        throw new Exception("Invalid file ID " + messageData.messageText);
                     }

                     messageData.binaryData = new byte[fileData.size];
                     messageData.contentType = MessageData.ContentTypeEnum.IMAGE;
                  } else {
                     messageData.messageText = this.storeImage(messageData.source, messageData.binaryData);
                  }

                  groupChatPrx.putFileReceived(messageData.toIceObject());
                  if (this.isV3Device()) {
                     this.putFileReceived(messageData.toIceObject());
                  }

               }
            } catch (LocalException var6) {
               this.logIceLocalException("sendFusionMessageToGroupChat: Ice.LocalException processing binary data", messageData, var6);
               throw new FusionException("The group chat is no longer available");
            } catch (Exception var7) {
               FusionException fe = new FusionException("Unable to send the picture. Reason: " + var7.getMessage());
               log.warn(fe.message);
               throw fe;
            }
         } else {
            try {
               if (messageIce.messageText.startsWith("/")) {
                  if (this.handleGroupChatCommand(messageData, groupChatPrx)) {
                     return;
                  }

                  messageIce = messageData.toIceObject();
               }
            } catch (FusionException var9) {
               throw var9;
            } catch (Exception var10) {
               throw new FusionException(var10.getMessage());
            }

            try {
               if (messageIce.messageText.startsWith("!")) {
                  if (SystemProperty.getBool("MerchantCannotPlayGame", true) && this.user.getUserType() == UserData.TypeEnum.MIG33_TOP_MERCHANT) {
                     throw new FusionException("Games are temporarily disabled for merchant accounts");
                  }

                  groupChatPrx.sendMessageToBots(messageIce.source, messageIce.messageText, messageIce.requestReceivedTimestamp);
               } else {
                  groupChatPrx.putMessage(messageIce);
                  this.metrics.groupChatMessageSent();
               }

            } catch (LocalException var8) {
               this.logIceLocalException("sendFusionMessageToGroupChat: Ice.LocalException sending message", messageData, var8);
               throw new FusionException("Unable to send message to the group chat");
            }
         }
      }
   }

   private void storeOfflineFusionMessageForIndividual(MessageData messageData, String destinationUsername, int recipientId) throws FusionException {
      try {
         OfflineMessageHelper.StorageResult result = OfflineMessageHelper.getInstance().scheduleOfflineMessageStorageAndWait(messageData, this.user.getUserID(), recipientId);
         if (result.failed()) {
            throw new FusionException(result.getError());
         } else {
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.MSG_STORED_CONF_ENABLED)) {
               this.sendOfflineMessageStoredConfirmation(messageData, destinationUsername);
            }

         }
      } catch (FusionException var5) {
         throw var5;
      } catch (Exception var6) {
         log.error("Exception in storeOfflineFusionMessageForIndividual(): " + var6);
         throw new FusionException(var6.getMessage());
      }
   }

   private boolean switchBoardSendFusionMessageToIndividual(MessageDataIce message, String destinationUsername, Set<String> uniqueUsersPrivateChattedWith, String recipientDisplayPicture) throws FusionException {
      return MessageSwitchboardDispatcher.getInstance().onSendFusionMessageToIndividual(this.objectManager.getApplicationContext(), this.sessionID, this.findUserPrx(this.user.getUsername()), this.user.getSessionPrx(this.sessionID), this.user, message, destinationUsername, uniqueUsersPrivateChattedWith, this.deviceType, this.clientVersion, this.user.getUserData(), recipientDisplayPicture);
   }

   private void switchBoardSendFusionMessageToGroupChat(MessageDataIce messageIce) throws FusionException {
      MessageSwitchboardDispatcher.getInstance().onSendFusionMessageToGroupChat(this.objectManager.getApplicationContext(), this.makeProxy(), this.user.makeProxy(), messageIce, messageIce.messageDestinations[0].destination, this.deviceType, this.clientVersion);
   }

   public UserDataIce getUserDataIce() {
      return this.user.getUserData();
   }

   private void logIceLocalException(String msg, MessageData md, LocalException e) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.SESSIONI_LOG_ICELOCALEXCEPTIONS_ENABLED)) {
         try {
            if (md != null) {
               msg = msg + " messageData=" + md.toString();
            }

            msg = msg + " getCause=" + e.getCause();
            log.error(msg, e);
         } catch (Exception var5) {
         }
      }

   }

   public Set<String> checkOutstandingContactRequests(Set<String> userList) {
      Set<String> contactRequestSentList = new HashSet();
      Iterator i$ = userList.iterator();

      while(i$.hasNext()) {
         String username = (String)i$.next();

         try {
            this.contactRequest(username, userList.size());
            contactRequestSentList.add(username);
         } catch (Exception var6) {
         }
      }

      return contactRequestSentList;
   }

   public ChatSessionState getState() {
      ChatSessionState state = new ChatSessionState();
      state.sessionID = this.sessionID;
      state.mobileDevice = this.mobileDevice;
      state.userAgent = this.userAgent;
      state.port = this.port;
      state.remotePort = this.remotePort;
      state.remoteAddress = this.remoteAddress;
      state.clientVersion = this.clientVersion;
      state.userAgent = this.userAgent;
      if (this.connectionType != null) {
         state.connectionType = this.connectionType.value();
      }

      if (this.imType != null) {
         state.imType = this.imType.value();
      }

      if (this.deviceType != null) {
         state.deviceType = this.deviceType.value();
      }

      if (this.presence != null) {
         state.presence = this.presence.value();
      }

      return state;
   }

   public void setCurrentChatListGroupChatSubset(ChatList newCCL) {
      this.user.setCurrentChatListGroupChatSubset(newCCL);
   }

   public boolean isMessageStatusEventCapable() {
      return MessageStatusEvent.isClientMessageStatusEventCapable(this.deviceType, this.clientVersion);
   }

   public void putMessageStatusEvent(MessageStatusEvent event) throws FusionException {
      this.chatConnection.putMessageStatusEvent(event);
      if (log.isDebugEnabled()) {
         log.debug("Forwarded FusionPktMessageEvent to recipient=" + this.user.getUsername() + " on device=" + this.deviceType + " client ver=" + this.clientVersion);
      }

   }

   private class ChatSessionRooms {
      private int chatRoomRemovalIceTimeout;
      private HashMap<String, ChatRoomPrx> currentChatrooms = new HashMap();

      public ChatSessionRooms(ChatObjectManagerSession objectManager) {
         this.chatRoomRemovalIceTimeout = objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomRemovalIceTimeout", 2000);
      }

      public void endSession(String username) {
         synchronized(this.currentChatrooms) {
            Iterator i$ = this.currentChatrooms.keySet().iterator();

            while(i$.hasNext()) {
               String name = (String)i$.next();

               try {
                  ChatRoomPrx chatRoomProxy = (ChatRoomPrx)this.currentChatrooms.get(name);
                  ChatRoomPrx oneWay = ChatRoomPrxHelper.uncheckedCast(chatRoomProxy.ice_oneway());
                  oneWay = (ChatRoomPrx)oneWay.ice_connectionId("OneWayProxyGroup");
                  oneWay.removeParticipantOneWay(username, false);
                  ChatSession.this.user.removeFromCurrentChatroomList(name);
               } catch (Exception var8) {
               }
            }

         }
      }

      public int join(ChatRoomPrx roomProxy, String name) {
         synchronized(this.currentChatrooms) {
            this.currentChatrooms.put(name, roomProxy);
            return this.currentChatrooms.size();
         }
      }
   }
}
