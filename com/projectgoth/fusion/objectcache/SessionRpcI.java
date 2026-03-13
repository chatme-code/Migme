package com.projectgoth.fusion.objectcache;

import Ice.Current;
import com.projectgoth.fusion.chatsync.ChatList;
import com.projectgoth.fusion.chatsync.CurrentChatList;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.ice.IceAmdInvoker;
import com.projectgoth.fusion.slice.AMD_Session_endSession;
import com.projectgoth.fusion.slice.AMD_Session_putMessage;
import com.projectgoth.fusion.slice.AMD_Session_sendMessage;
import com.projectgoth.fusion.slice.ChatListIce;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.MessageSwitchboardPrx;
import com.projectgoth.fusion.slice.SessionMetricsIce;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.slice._SessionDisp;
import org.apache.log4j.Logger;

public class SessionRpcI extends _SessionDisp {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SessionRpcI.class));
   private ChatObjectManagerSession objectManager;
   private ChatSession session;

   public SessionRpcI(ChatObjectManagerSession objectManager, ChatSession session) {
      this.objectManager = objectManager;
      this.session = session;
   }

   public UserPrx getUserProxy(String username, Current __current) throws FusionException {
      return this.objectManager.findUserPrx(username);
   }

   public GroupChatPrx findGroupChatObject(String groupChatID, Current __current) throws FusionException {
      return this.objectManager.findGroupChatPrx(groupChatID);
   }

   public MessageSwitchboardPrx getMessageSwitchboard(Current __current) throws FusionException {
      return this.objectManager.getMessageSwitchboardPrx();
   }

   public String getParentUsername(Current __current) throws FusionException {
      return this.session.getUsername();
   }

   public void disconnect(String reason) throws FusionException {
      this.session.disconnect(reason);
   }

   public void contactChangedPresence(int contactID, int imType, int presence) throws Exception {
      this.session.contactChangedPresence(contactID, imType, presence);
   }

   public void contactChangedDisplayPicture(int contactID, String displayPicture, long timeStamp) throws Exception {
      this.session.contactChangedDisplayPicture(contactID, displayPicture, timeStamp);
   }

   public void contactChangedStatusMessage(int contactID, String statusMessage, long timeStamp) throws Exception {
      this.session.contactChangedStatusMessage(contactID, statusMessage, timeStamp);
   }

   public void contactRequest(String username, int outstandingRequests) throws Exception {
      this.session.contactRequest(username, outstandingRequests);
   }

   public void contactRequestAccepted(ContactData contact, int contactListVersion, int outstandingRequests) throws Exception {
      this.session.contactRequestAccepted(contact, contactListVersion, outstandingRequests);
   }

   public void contactRequestRejected(String username, int outstandingRequests) throws Exception {
      this.session.contactRequestRejected(username, outstandingRequests);
   }

   public void putWebCallNotification(String source, String destination, int gateway, String gatewayName, int protocol) throws FusionException {
      this.session.putWebCallNotification(source, destination, gateway, gatewayName, protocol);
   }

   public String getSessionID(Current __current) {
      return this.session.getSessionID();
   }

   public String getRemoteIPAddress(Current __current) {
      return this.session.getRemoteAddress();
   }

   public void touch(Current __current) throws FusionException {
      this.session.touch();
   }

   public void sendMessage_async(final AMD_Session_sendMessage cb, final MessageDataIce message, Current __current) throws FusionException {
      IceAmdInvoker ivk = new ObjectCacheIceAmdInvoker() {
         public void payload() throws Exception {
            SessionRpcI.this.session.sendMessage(message);
         }

         public void ice_response() {
            cb.ice_response();
         }

         public void ice_exception(Exception e) {
            cb.ice_exception(e);
         }

         public String getLogContext() {
            return "Session.sendMessage with message.source=" + message.source;
         }
      };
      ivk.invoke();
   }

   public void putMessage_async(final AMD_Session_putMessage cb, final MessageDataIce message, Current __current) throws FusionException {
      IceAmdInvoker ivk = new ObjectCacheIceAmdInvoker() {
         public void payload() throws Exception {
            SessionRpcI.this.session.putMessage(message);
         }

         public void ice_response() {
            cb.ice_response();
         }

         public void ice_exception(Exception e) {
            cb.ice_exception(e);
         }

         public String getLogContext() {
            return "Session.putMessage with message.source=" + message.source;
         }
      };
      ivk.invoke();
   }

   public void putMessageOneWay(MessageDataIce message, Current __current) {
      this.session.putMessageOneWay(message);
   }

   public void setPresence(int presence, Current __current) throws FusionException {
      this.session.setPresence(presence);
   }

   public void endSession_async(final AMD_Session_endSession cb, Current __current) throws FusionException {
      IceAmdInvoker ivk = new ObjectCacheIceAmdInvoker() {
         public void payload() throws Exception {
            SessionRpcI.this.session.endSession();
         }

         public void ice_response() {
            cb.ice_response();
         }

         public void ice_exception(Exception e) {
            cb.ice_exception(e);
         }

         public String getLogContext() {
            return "Session.endSession";
         }
      };
      ivk.invoke();
   }

   public void endSessionOneWay(Current __current) {
      try {
         this.session.endSession();
      } catch (Exception var3) {
      }

   }

   public void putAlertMessage(String message, String title, short timeout, Current __current) throws FusionException {
      this.session.putAlertMessage(message, title, timeout);
   }

   public void putAlertMessageOneWay(String message, String title, short timeout, Current __current) {
      this.session.putAlertMessageOneWay(message, title, timeout);
   }

   public short getClientVersionIce(Current __current) {
      return this.session.getClientVersion();
   }

   public void silentlyDropIncomingPackets(Current __current) {
      this.session.silentlyDropIncomingPackets();
   }

   public void setLanguage(String language, Current __current) {
      this.session.setLanguage(language);
   }

   public void profileEdited(Current __current) {
      this.session.metricsProfileEdited();
   }

   public void chatroomJoined(ChatRoomPrx roomProxy, String name, Current __current) {
      this.session.chatroomJoined(roomProxy, name);
   }

   public void friendInvitedByPhoneNumber(Current __current) {
      this.session.metricsFriendInvitedByPhoneNumber();
   }

   public void friendInvitedByUsername(Current __current) {
      this.session.metricsFriendInvitedByUsername();
   }

   public void groupChatJoined(String id, Current __current) {
      this.session.metricsGroupChatJoined(id);
   }

   public void groupChatJoinedMultiple(String id, int increment, Current __current) {
      this.session.metricsGroupChatJoined(id, increment);
   }

   public void photoUploaded(Current __current) {
      this.session.metricsPhotoUploaded();
   }

   public void statusMessageSet(Current __current) {
      this.session.metricsStatusMessageSet();
   }

   public void themeUpdated(Current __current) {
      this.session.metricsThemeUpdated();
   }

   public void sendMessageBackToUserAsEmote(MessageDataIce message, Current __current) throws FusionException {
      this.session.sendMessageBackToUserAsEmote(message);
   }

   public boolean privateChattedWith(String username, Current __current) {
      return this.session.hasPrivateChattedWith(username);
   }

   public String getMobileDeviceIce(Current __current) {
      return this.session.getMobileDevice();
   }

   public String getUserAgentIce(Current __current) {
      return this.session.getUserAgent();
   }

   public int getDeviceTypeAsInt(Current __current) {
      return this.session.getDeviceType().value();
   }

   public void notifyUserLeftChatRoomOneWay(String chatroomname, String username, Current __current) {
      this.session.notifyUserLeftChatRoomOneWay(chatroomname, username);
   }

   public void notifyUserJoinedChatRoomOneWay(String chatroomname, String username, boolean isAdministrator, boolean isMuted, Current __current) {
      this.session.notifyUserJoinedChatRoomOneWay(chatroomname, username, isAdministrator, isMuted);
   }

   public void notifyUserLeftGroupChat(String groupChatId, String username, Current __current) throws FusionException {
      this.session.notifyUserLeftGroupChat(groupChatId, username);
   }

   public void notifyUserJoinedGroupChat(String groupChatId, String username, boolean isMuted, Current __current) throws FusionException {
      this.session.notifyUserJoinedGroupChat(groupChatId, username, isMuted);
   }

   public void sendGroupChatParticipantArrays(String groupChatId, byte imType, String[] participants, String[] mutedParticipants, Current __current) throws FusionException {
      this.session.sendGroupChatParticipantArrays(groupChatId, imType, participants, mutedParticipants);
   }

   public void sendGroupChatParticipants(String groupChatId, byte imType, String participants, String mutedParticipants, Current __current) throws FusionException {
      this.session.sendGroupChatParticipants(groupChatId, imType, participants, mutedParticipants);
   }

   public int getChatListVersion(Current __current) throws FusionException {
      return this.session.getChatListVersion();
   }

   public void setChatListVersion(int version, Current __current) throws FusionException {
      this.session.setChatListVersion(version);
   }

   public void putSerializedPacket(byte[] packet, Current __current) throws FusionException {
      this.session.putSerializedPacket(packet);
   }

   public void putSerializedPacketOneWay(byte[] packet, Current __current) {
      this.session.putSerializedPacketOneWay(packet);
   }

   public SessionMetricsIce getSessionMetrics(Current __current) {
      return this.session.getSessionMetrics();
   }

   public void setCurrentChatListGroupChatSubset(ChatListIce cclSubsetIce, Current __current) {
      ChatList cclSubset = new CurrentChatList(cclSubsetIce);
      this.session.setCurrentChatListGroupChatSubset(cclSubset);
   }
}
