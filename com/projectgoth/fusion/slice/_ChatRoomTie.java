package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;
import java.util.Map;

public class _ChatRoomTie extends _ChatRoomDisp implements TieBase {
   private _ChatRoomOperations _ice_delegate;

   public _ChatRoomTie() {
   }

   public _ChatRoomTie(_ChatRoomOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_ChatRoomOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _ChatRoomTie) ? false : this._ice_delegate.equals(((_ChatRoomTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public void botKilled(String botInstanceID, Current __current) throws FusionException {
      this._ice_delegate.botKilled(botInstanceID, __current);
   }

   public String[] getParticipants(String requestingUsername, Current __current) {
      return this._ice_delegate.getParticipants(requestingUsername, __current);
   }

   public boolean isParticipant(String username, Current __current) throws FusionException {
      return this._ice_delegate.isParticipant(username, __current);
   }

   public void putBotMessage(String botInstanceID, String username, String message, String[] emoticonHotKeys, boolean displayPopUp, Current __current) throws FusionException {
      this._ice_delegate.putBotMessage(botInstanceID, username, message, emoticonHotKeys, displayPopUp, __current);
   }

   public void putBotMessageToAllUsers(String botInstanceID, String message, String[] emoticonHotKeys, boolean displayPopUp, Current __current) throws FusionException {
      this._ice_delegate.putBotMessageToAllUsers(botInstanceID, message, emoticonHotKeys, displayPopUp, __current);
   }

   public void putBotMessageToUsers(String botInstanceID, String[] usernames, String message, String[] emoticonHotKeys, boolean displayPopUp, Current __current) throws FusionException {
      this._ice_delegate.putBotMessageToUsers(botInstanceID, usernames, message, emoticonHotKeys, displayPopUp, __current);
   }

   public void sendGamesHelpToUser(String username, Current __current) throws FusionException {
      this._ice_delegate.sendGamesHelpToUser(username, __current);
   }

   public void sendMessageToBots(String username, String message, long receivedTimestamp, Current __current) throws FusionException {
      this._ice_delegate.sendMessageToBots(username, message, receivedTimestamp, __current);
   }

   public void startBot(String username, String botCommandName, Current __current) throws FusionException {
      this._ice_delegate.startBot(username, botCommandName, __current);
   }

   public void stopAllBots(String username, int timeout, Current __current) throws FusionException {
      this._ice_delegate.stopAllBots(username, timeout, __current);
   }

   public void stopBot(String username, String botCommandName, Current __current) throws FusionException {
      this._ice_delegate.stopBot(username, botCommandName, __current);
   }

   public void addGroupModerator(String instigator, String targetUser, Current __current) throws FusionException {
      this._ice_delegate.addGroupModerator(instigator, targetUser, __current);
   }

   public void addModerator(String username, Current __current) {
      this._ice_delegate.addModerator(username, __current);
   }

   public void addParticipant(UserPrx userProxy, UserDataIce userData, SessionPrx sessionProxy, String sessionID, String ipAddress, String mobileDevice, String userAgent, short clientVersion, int deviceType, Current __current) throws FusionException {
      this._ice_delegate.addParticipant(userProxy, userData, sessionProxy, sessionID, ipAddress, mobileDevice, userAgent, clientVersion, deviceType, __current);
   }

   public void addParticipantOld(UserPrx userProxy, UserDataIce userData, SessionPrx sessionProxy, String sessionID, String ipAddress, String mobileDevice, String userAgent, Current __current) throws FusionException {
      this._ice_delegate.addParticipantOld(userProxy, userData, sessionProxy, sessionID, ipAddress, mobileDevice, userAgent, __current);
   }

   public void adminAnnounce(String announceMessage, int waitTime, Current __current) throws FusionException {
      this._ice_delegate.adminAnnounce(announceMessage, waitTime, __current);
   }

   public void announceOff(String announcer, Current __current) throws FusionException {
      this._ice_delegate.announceOff(announcer, __current);
   }

   public void announceOn(String announcer, String announceMessage, int waitTime, Current __current) throws FusionException {
      this._ice_delegate.announceOn(announcer, announceMessage, waitTime, __current);
   }

   public void banGroupMembers(String[] banList, String instigator, int reasonCode, Current __current) throws FusionException {
      this._ice_delegate.banGroupMembers(banList, instigator, reasonCode, __current);
   }

   public void banIndexes(int[] indexes, String bannedBy, int reasonCode, Current __current) throws FusionException {
      this._ice_delegate.banIndexes(indexes, bannedBy, reasonCode, __current);
   }

   public void banMultiIds(String username, Current __current) throws FusionException {
      this._ice_delegate.banMultiIds(username, __current);
   }

   public void banUser(String username, Current __current) {
      this._ice_delegate.banUser(username, __current);
   }

   public void broadcastMessage(String instigator, String message, Current __current) throws FusionException {
      this._ice_delegate.broadcastMessage(instigator, message, __current);
   }

   public void bumpUser(String instigator, String target, Current __current) throws FusionException {
      this._ice_delegate.bumpUser(instigator, target, __current);
   }

   public void changeOwner(String oldOwnerUsername, String newOwnerUsername, Current __current) {
      this._ice_delegate.changeOwner(oldOwnerUsername, newOwnerUsername, __current);
   }

   public void clearUserKick(String instigator, String target, Current __current) throws FusionException {
      this._ice_delegate.clearUserKick(instigator, target, __current);
   }

   public void convertIntoGroupChatRoom(int groupID, String groupName, Current __current) throws FusionException {
      this._ice_delegate.convertIntoGroupChatRoom(groupID, groupName, __current);
   }

   public void convertIntoUserOwnedChatRoom(Current __current) throws FusionException {
      this._ice_delegate.convertIntoUserOwnedChatRoom(__current);
   }

   public int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy, Current __current) throws FusionException {
      return this._ice_delegate.executeEmoteCommandWithState(emoteCommand, message, sessionProxy, __current);
   }

   public String[] getAdministrators(String requestingUsername, Current __current) {
      return this._ice_delegate.getAdministrators(requestingUsername, __current);
   }

   public String[] getAllParticipants(String requestingUsername, Current __current) {
      return this._ice_delegate.getAllParticipants(requestingUsername, __current);
   }

   public String[] getGroupModerators(String instigator, Current __current) throws FusionException {
      return this._ice_delegate.getGroupModerators(instigator, __current);
   }

   public int getMaximumMessageLength(String sender, Current __current) {
      return this._ice_delegate.getMaximumMessageLength(sender, __current);
   }

   public int getNumParticipants(Current __current) {
      return this._ice_delegate.getNumParticipants(__current);
   }

   public ChatRoomDataIce getRoomData(Current __current) {
      return this._ice_delegate.getRoomData(__current);
   }

   public Map<String, String> getTheme(Current __current) {
      return this._ice_delegate.getTheme(__current);
   }

   public void inviteUserToGroup(String invitee, String inviter, Current __current) throws FusionException {
      this._ice_delegate.inviteUserToGroup(invitee, inviter, __current);
   }

   public boolean isLocked(Current __current) {
      return this._ice_delegate.isLocked(__current);
   }

   public boolean isVisibleParticipant(String username, Current __current) throws FusionException {
      return this._ice_delegate.isVisibleParticipant(username, __current);
   }

   public void kickIndexes(int[] indexes, String bannedBy, Current __current) throws FusionException {
      this._ice_delegate.kickIndexes(indexes, bannedBy, __current);
   }

   public void listParticipants(String requestingUsername, int size, int startIndex, Current __current) throws FusionException {
      this._ice_delegate.listParticipants(requestingUsername, size, startIndex, __current);
   }

   public void lock(String locker, Current __current) throws FusionException {
      this._ice_delegate.lock(locker, __current);
   }

   public void mute(String username, String target, Current __current) throws FusionException {
      this._ice_delegate.mute(username, target, __current);
   }

   public void putMessage(MessageDataIce message, String sessionID, Current __current) throws FusionException {
      this._ice_delegate.putMessage(message, sessionID, __current);
   }

   public void putSystemMessage(String messageText, String[] emoticonKeys, Current __current) {
      this._ice_delegate.putSystemMessage(messageText, emoticonKeys, __current);
   }

   public void putSystemMessageWithColour(String messageText, String[] emoticonKeys, int messageColour, Current __current) {
      this._ice_delegate.putSystemMessageWithColour(messageText, emoticonKeys, messageColour, __current);
   }

   public void removeGroupModerator(String instigator, String targetUser, Current __current) throws FusionException {
      this._ice_delegate.removeGroupModerator(instigator, targetUser, __current);
   }

   public void removeModerator(String username, Current __current) {
      this._ice_delegate.removeModerator(username, __current);
   }

   public void removeParticipant(String username, Current __current) throws FusionException {
      this._ice_delegate.removeParticipant(username, __current);
   }

   public void removeParticipantOneWay(String username, boolean removeFromUsersChatRoomList, Current __current) {
      this._ice_delegate.removeParticipantOneWay(username, removeFromUsersChatRoomList, __current);
   }

   public void setAdultOnly(boolean adultOnly, Current __current) {
      this._ice_delegate.setAdultOnly(adultOnly, __current);
   }

   public void setAllowKicking(boolean allowKicking, Current __current) {
      this._ice_delegate.setAllowKicking(allowKicking, __current);
   }

   public void setDescription(String description, Current __current) {
      this._ice_delegate.setDescription(description, __current);
   }

   public void setMaximumSize(int maximumSize, Current __current) {
      this._ice_delegate.setMaximumSize(maximumSize, __current);
   }

   public void setNumberOfFakeParticipants(String username, int number, Current __current) {
      this._ice_delegate.setNumberOfFakeParticipants(username, number, __current);
   }

   public void silence(String username, int timeout, Current __current) throws FusionException {
      this._ice_delegate.silence(username, timeout, __current);
   }

   public void silenceUser(String instigator, String target, int timeout, Current __current) throws FusionException {
      this._ice_delegate.silenceUser(instigator, target, timeout, __current);
   }

   public void submitGiftAllTask(int giftId, String giftMessage, MessageDataIce message, Current __current) throws FusionException {
      this._ice_delegate.submitGiftAllTask(giftId, giftMessage, message, __current);
   }

   public void unbanGroupMember(String target, String instigator, int reasonCode, Current __current) throws FusionException {
      this._ice_delegate.unbanGroupMember(target, instigator, reasonCode, __current);
   }

   public void unbanUser(String username, Current __current) {
      this._ice_delegate.unbanUser(username, __current);
   }

   public void unlock(String unlocker, Current __current) throws FusionException {
      this._ice_delegate.unlock(unlocker, __current);
   }

   public void unmute(String username, String target, Current __current) throws FusionException {
      this._ice_delegate.unmute(username, target, __current);
   }

   public void unsilence(String username, Current __current) throws FusionException {
      this._ice_delegate.unsilence(username, __current);
   }

   public void unsilenceUser(String instigator, String target, Current __current) throws FusionException {
      this._ice_delegate.unsilenceUser(instigator, target, __current);
   }

   public void updateDescription(String instigator, String description, Current __current) throws FusionException {
      this._ice_delegate.updateDescription(instigator, description, __current);
   }

   public void updateExtraData(ChatRoomDataIce data, Current __current) {
      this._ice_delegate.updateExtraData(data, __current);
   }

   public void updateGroupModeratorStatus(String username, boolean promote, Current __current) {
      this._ice_delegate.updateGroupModeratorStatus(username, promote, __current);
   }

   public void voteToKickUser(String voter, String target, Current __current) throws FusionException {
      this._ice_delegate.voteToKickUser(voter, target, __current);
   }

   public void warnUser(String instigator, String target, String message, Current __current) throws FusionException {
      this._ice_delegate.warnUser(instigator, target, message, __current);
   }
}
