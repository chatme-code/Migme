package com.projectgoth.fusion.objectcache;

import Ice.Current;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.slice._ChatRoomDisp;
import java.util.Map;
import org.apache.log4j.Logger;

public class ChatRoomRpcI extends _ChatRoomDisp {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatRoomRpcI.class));
   private ChatRoom room;

   public ChatRoomRpcI(ChatRoom room) {
      this.room = room;
   }

   public ChatRoomDataIce getRoomData(Current current) {
      return this.room.getRoomData();
   }

   public void setMaximumSize(int maximumSize, Current current) {
      this.room.setMaximumSize(maximumSize);
   }

   public void setAllowKicking(boolean allowKicking, Current current) {
      this.room.setAllowKicking(allowKicking);
   }

   public void setDescription(String description, Current current) {
      this.room.setDescription(description);
   }

   public void updateDescription(String instigator, String description, Current __current) throws FusionException {
      this.room.updateDescription(instigator, description);
   }

   public void setAdultOnly(boolean adultOnly, Current current) {
      this.room.setAdultOnly(adultOnly);
   }

   public void mute(String username, String target, Current current) throws FusionException {
      this.room.mute(username, target);
   }

   public void unmute(String username, String target, Current current) throws FusionException {
      this.room.unmute(username, target);
   }

   public void silence(String username, int timeout, Current current) throws FusionException {
      this.room.silence(username, timeout);
   }

   public void silenceUser(String instigator, String target, int timeout, Current current) throws FusionException {
      this.room.silenceUser(instigator, target, timeout);
   }

   public void unsilence(String username, Current current) throws FusionException {
      this.room.unsilence(username);
   }

   public void unsilenceUser(String instigator, String target, Current current) throws FusionException {
      this.room.unsilenceUser(instigator, target);
   }

   public void convertIntoGroupChatRoom(int groupID, String groupName, Current current) throws FusionException {
      this.room.convertIntoGroupChatRoom(groupID, groupName);
   }

   public void convertIntoUserOwnedChatRoom(Current current) throws FusionException {
      this.room.convertIntoUserOwnedChatRoom();
   }

   public void changeOwner(String oldOwnerUsername, String newOwnerUsername, Current current) {
      this.room.changeOwner(oldOwnerUsername, newOwnerUsername);
   }

   public void addParticipantOld(UserPrx userPrx, UserDataIce userData, SessionPrx sessionPrx, String sessionID, String ipAddress, String mobileDevice, String userAgent, Current current) throws FusionException {
      UserData ud = new UserData(userData);
      this.room.addParticipant(userPrx, ud, sessionPrx, sessionID, ipAddress, mobileDevice, userAgent);
   }

   public void addParticipant(UserPrx userPrx, UserDataIce userData, SessionPrx sessionPrx, String sessionID, String ipAddress, String mobileDevice, String userAgent, short clientVersion, int deviceType, Current current) throws FusionException {
      UserData ud = new UserData(userData);
      this.room.addParticipant(userPrx, ud, sessionPrx, sessionID, ipAddress, mobileDevice, userAgent, clientVersion, deviceType);
   }

   public void removeParticipant(String username, Current current) throws FusionException {
      this.room.removeParticipant(username);
   }

   public void removeParticipantOneWay(String username, boolean removeFromUsersChatRoomList, Current current) {
      try {
         this.room.removeParticipant(username, removeFromUsersChatRoomList);
      } catch (Exception var5) {
         if (log.isDebugEnabled()) {
            log.debug("Exception caught when removing user: [" + username + "] from chat room: [" + this.room.getRoomData().name + "]: " + var5.getMessage(), var5);
         }
      }

   }

   public String[] getParticipants(String requestingUsername, Current current) {
      return this.room.getParticipants(requestingUsername);
   }

   public String[] getAllParticipants(String requestingUsername, Current current) {
      return this.room.getAllParticipants(requestingUsername);
   }

   public String[] getAdministrators(String requestingUsername, Current current) {
      return this.room.getAdministrators(requestingUsername);
   }

   public int getNumParticipants(Current current) {
      return this.room.getNumParticipants();
   }

   public void setNumberOfFakeParticipants(String username, int number, Current current) {
      this.room.setNumberOfFakeParticipants(username, number);
   }

   public boolean isParticipant(String username, Current current) throws FusionException {
      return this.room.isParticipant(username);
   }

   public boolean isVisibleParticipant(String username, Current current) throws FusionException {
      return this.room.isVisibleParticipant(username);
   }

   public void listParticipants(String requestingUsername, int size, int startIndex, Current current) throws FusionException {
      this.room.listParticipants(requestingUsername, size, startIndex);
   }

   public int getMaximumMessageLength(String sender, Current current) {
      return this.room.getMaximumMessageLength(sender);
   }

   public void putMessage(MessageDataIce message, String sessionID, Current current) throws FusionException {
      this.room.putMessage(message, sessionID);
   }

   public void putSystemMessage(String messageText, String[] emoticonKeys, Current current) {
      this.room.putSystemMessageWithColour(messageText, emoticonKeys, -1);
   }

   public void putSystemMessageWithColour(String messageText, String[] emoticonKeys, int messageColour, Current current) {
      this.room.putSystemMessageWithColour(messageText, emoticonKeys, messageColour);
   }

   public void addModerator(String username, Current current) {
      this.room.addModerator(username);
   }

   public void removeModerator(String username, Current current) {
      this.room.removeModerator(username);
   }

   public Map<String, String> getTheme(Current current) {
      return this.room.getTheme();
   }

   public void banUser(String username, Current current) {
      this.room.banUser(username);
   }

   public void unbanUser(String username, Current current) {
      this.room.unbanUser(username);
   }

   public void banGroupMembers(String[] bannedList, String bannedby, int reason, Current __current) throws FusionException {
      this.room.banGroupMembers(bannedList, bannedby, reason);
   }

   public void unbanGroupMember(String unbanned, String unbannedby, int reason, Current __current) throws FusionException {
      this.room.unbanGroupMember(unbanned, unbannedby, reason);
   }

   public void banIndexes(int[] indexes, String bannedBy, int reason, Current __current) throws FusionException {
      this.room.banIndexes(indexes, bannedBy, reason);
   }

   public void banMultiIds(String requestingUsername, Current __current) throws FusionException {
      this.room.banMultiIds(requestingUsername);
   }

   public void kickIndexes(int[] indexes, String kickedBy, Current __current) throws FusionException {
      this.room.kickIndexes(indexes, kickedBy);
   }

   public void inviteUserToGroup(String invitee, String inviter, Current __current) throws FusionException {
      this.room.inviteUserToGroup(invitee, inviter);
   }

   public void broadcastMessage(String instigator, String message, Current __current) throws FusionException {
      this.room.broadcastMessage(instigator, message);
   }

   public void voteToKickUser(String voter, String target, Current current) throws FusionException {
      this.room.voteToKickUser(voter, target);
   }

   public void clearUserKick(String instigator, String target, Current current) throws FusionException {
      this.room.clearUserKick(instigator, target);
   }

   public void startBot(String username, String botName, Current current) throws FusionException {
      this.room.startBot(username, botName);
   }

   public void stopBot(String username, String botName, Current current) throws FusionException {
      this.room.stopBot(username, botName);
   }

   public void stopAllBots(String username, int timeout, Current current) throws FusionException {
      this.room.stopAllBots(username, timeout);
   }

   public void botKilled(String botInstanceID, Current current) throws FusionException {
      this.room.botKilled(botInstanceID);
   }

   public void sendMessageToBots(String username, String message, long receivedTimestamp, Current current) throws FusionException {
      this.room.sendMessageToBots(username, message, receivedTimestamp);
   }

   public void putBotMessage(String botInstanceID, String username, String message, String[] emoticonHotKeys, boolean displayPopUp, Current current) throws FusionException {
      this.room.putBotMessage(botInstanceID, username, message, emoticonHotKeys, displayPopUp);
   }

   public void putBotMessageToUsers(String botInstanceID, String[] usernames, String message, String[] emoticonHotKeys, boolean displayPopUp, Current current) throws FusionException {
      this.room.putBotMessageToUsers(botInstanceID, usernames, message, emoticonHotKeys, displayPopUp);
   }

   public void putBotMessageToAllUsers(String botInstanceID, String message, String[] emoticonHotKeys, boolean displayPopUp, Current current) throws FusionException {
      this.room.putBotMessageToAllUsers(botInstanceID, message, emoticonHotKeys, displayPopUp);
   }

   public void sendGamesHelpToUser(String username, Current current) throws FusionException {
      this.room.sendGamesHelpToUser(username);
   }

   public boolean isLocked(Current __current) {
      return this.room.isLocked();
   }

   public void lock(String locker, Current __current) throws FusionException {
      this.room.lock(locker);
   }

   public void unlock(String unlocker, Current __current) throws FusionException {
      this.room.unlock(unlocker);
   }

   public void announceOff(String announcer, Current __current) throws FusionException {
      this.room.announceOff(announcer);
   }

   public void announceOn(String announcer, String announceMessage, int waitTime, Current __current) throws FusionException {
      this.room.announceOn(announcer, announceMessage, waitTime);
   }

   public void adminAnnounce(String announceMessage, int waitTime, Current __current) throws FusionException {
      this.room.adminAnnounce(announceMessage, waitTime);
   }

   public int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy, Current __current) throws FusionException {
      return this.room.executeEmoteCommandWithState(emoteCommand, message, sessionProxy);
   }

   public void submitGiftAllTask(int giftId, String giftMessage, MessageDataIce message, Current __current) throws FusionException {
      this.room.submitGiftAllTask(giftId, giftMessage, message);
   }

   public void updateExtraData(ChatRoomDataIce newChatRoomDataWithExtraData, Current current) {
      this.room.updateExtraData(newChatRoomDataWithExtraData);
   }

   public void updateGroupModeratorStatus(String username, boolean promote, Current __current) {
      this.room.updateGroupModeratorStatus(username, promote);
   }

   public void bumpUser(String instigator, String target, Current __current) throws FusionException {
      this.room.bumpUser(instigator, target);
   }

   public void warnUser(String instigator, String target, String message, Current __current) throws FusionException {
      this.room.warnUser(instigator, target, message);
   }

   public void addGroupModerator(String instigator, String target, Current current) throws FusionException {
      this.room.addGroupModerator(instigator, target);
   }

   public void removeGroupModerator(String instigator, String target, Current current) throws FusionException {
      this.room.removeGroupModerator(instigator, target);
   }

   public String[] getGroupModerators(String instigator, Current current) throws FusionException {
      return this.room.getGroupModerators(instigator);
   }

   public boolean isIdle() {
      return this.room.isIdle();
   }

   public void prepareForPurge() {
      this.room.prepareForPurge();
   }
}
