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

public abstract class _ChatRoomDisp extends ObjectImpl implements ChatRoom {
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::BotChannel", "::com::projectgoth::fusion::slice::ChatRoom"};
   private static final String[] __all = new String[]{"addGroupModerator", "addModerator", "addParticipant", "addParticipantOld", "adminAnnounce", "announceOff", "announceOn", "banGroupMembers", "banIndexes", "banMultiIds", "banUser", "botKilled", "broadcastMessage", "bumpUser", "changeOwner", "clearUserKick", "convertIntoGroupChatRoom", "convertIntoUserOwnedChatRoom", "executeEmoteCommandWithState", "getAdministrators", "getAllParticipants", "getGroupModerators", "getMaximumMessageLength", "getNumParticipants", "getParticipants", "getRoomData", "getTheme", "ice_id", "ice_ids", "ice_isA", "ice_ping", "inviteUserToGroup", "isLocked", "isParticipant", "isVisibleParticipant", "kickIndexes", "listParticipants", "lock", "mute", "putBotMessage", "putBotMessageToAllUsers", "putBotMessageToUsers", "putMessage", "putSystemMessage", "putSystemMessageWithColour", "removeGroupModerator", "removeModerator", "removeParticipant", "removeParticipantOneWay", "sendGamesHelpToUser", "sendMessageToBots", "setAdultOnly", "setAllowKicking", "setDescription", "setMaximumSize", "setNumberOfFakeParticipants", "silence", "silenceUser", "startBot", "stopAllBots", "stopBot", "submitGiftAllTask", "unbanGroupMember", "unbanUser", "unlock", "unmute", "unsilence", "unsilenceUser", "updateDescription", "updateExtraData", "updateGroupModeratorStatus", "voteToKickUser", "warnUser"};

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
      return __ids[2];
   }

   public String ice_id(Current __current) {
      return __ids[2];
   }

   public static String ice_staticId() {
      return __ids[2];
   }

   public final void botKilled(String botInstanceID) throws FusionException {
      this.botKilled(botInstanceID, (Current)null);
   }

   public final String[] getParticipants(String requestingUsername) {
      return this.getParticipants(requestingUsername, (Current)null);
   }

   public final boolean isParticipant(String username) throws FusionException {
      return this.isParticipant(username, (Current)null);
   }

   public final void putBotMessage(String botInstanceID, String username, String message, String[] emoticonHotKeys, boolean displayPopUp) throws FusionException {
      this.putBotMessage(botInstanceID, username, message, emoticonHotKeys, displayPopUp, (Current)null);
   }

   public final void putBotMessageToAllUsers(String botInstanceID, String message, String[] emoticonHotKeys, boolean displayPopUp) throws FusionException {
      this.putBotMessageToAllUsers(botInstanceID, message, emoticonHotKeys, displayPopUp, (Current)null);
   }

   public final void putBotMessageToUsers(String botInstanceID, String[] usernames, String message, String[] emoticonHotKeys, boolean displayPopUp) throws FusionException {
      this.putBotMessageToUsers(botInstanceID, usernames, message, emoticonHotKeys, displayPopUp, (Current)null);
   }

   public final void sendGamesHelpToUser(String username) throws FusionException {
      this.sendGamesHelpToUser(username, (Current)null);
   }

   public final void sendMessageToBots(String username, String message, long receivedTimestamp) throws FusionException {
      this.sendMessageToBots(username, message, receivedTimestamp, (Current)null);
   }

   public final void startBot(String username, String botCommandName) throws FusionException {
      this.startBot(username, botCommandName, (Current)null);
   }

   public final void stopAllBots(String username, int timeout) throws FusionException {
      this.stopAllBots(username, timeout, (Current)null);
   }

   public final void stopBot(String username, String botCommandName) throws FusionException {
      this.stopBot(username, botCommandName, (Current)null);
   }

   public final void addGroupModerator(String instigator, String targetUser) throws FusionException {
      this.addGroupModerator(instigator, targetUser, (Current)null);
   }

   public final void addModerator(String username) {
      this.addModerator(username, (Current)null);
   }

   public final void addParticipant(UserPrx userProxy, UserDataIce userData, SessionPrx sessionProxy, String sessionID, String ipAddress, String mobileDevice, String userAgent, short clientVersion, int deviceType) throws FusionException {
      this.addParticipant(userProxy, userData, sessionProxy, sessionID, ipAddress, mobileDevice, userAgent, clientVersion, deviceType, (Current)null);
   }

   public final void addParticipantOld(UserPrx userProxy, UserDataIce userData, SessionPrx sessionProxy, String sessionID, String ipAddress, String mobileDevice, String userAgent) throws FusionException {
      this.addParticipantOld(userProxy, userData, sessionProxy, sessionID, ipAddress, mobileDevice, userAgent, (Current)null);
   }

   public final void adminAnnounce(String announceMessage, int waitTime) throws FusionException {
      this.adminAnnounce(announceMessage, waitTime, (Current)null);
   }

   public final void announceOff(String announcer) throws FusionException {
      this.announceOff(announcer, (Current)null);
   }

   public final void announceOn(String announcer, String announceMessage, int waitTime) throws FusionException {
      this.announceOn(announcer, announceMessage, waitTime, (Current)null);
   }

   public final void banGroupMembers(String[] banList, String instigator, int reasonCode) throws FusionException {
      this.banGroupMembers(banList, instigator, reasonCode, (Current)null);
   }

   public final void banIndexes(int[] indexes, String bannedBy, int reasonCode) throws FusionException {
      this.banIndexes(indexes, bannedBy, reasonCode, (Current)null);
   }

   public final void banMultiIds(String username) throws FusionException {
      this.banMultiIds(username, (Current)null);
   }

   public final void banUser(String username) {
      this.banUser(username, (Current)null);
   }

   public final void broadcastMessage(String instigator, String message) throws FusionException {
      this.broadcastMessage(instigator, message, (Current)null);
   }

   public final void bumpUser(String instigator, String target) throws FusionException {
      this.bumpUser(instigator, target, (Current)null);
   }

   public final void changeOwner(String oldOwnerUsername, String newOwnerUsername) {
      this.changeOwner(oldOwnerUsername, newOwnerUsername, (Current)null);
   }

   public final void clearUserKick(String instigator, String target) throws FusionException {
      this.clearUserKick(instigator, target, (Current)null);
   }

   public final void convertIntoGroupChatRoom(int groupID, String groupName) throws FusionException {
      this.convertIntoGroupChatRoom(groupID, groupName, (Current)null);
   }

   public final void convertIntoUserOwnedChatRoom() throws FusionException {
      this.convertIntoUserOwnedChatRoom((Current)null);
   }

   public final int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy) throws FusionException {
      return this.executeEmoteCommandWithState(emoteCommand, message, sessionProxy, (Current)null);
   }

   public final String[] getAdministrators(String requestingUsername) {
      return this.getAdministrators(requestingUsername, (Current)null);
   }

   public final String[] getAllParticipants(String requestingUsername) {
      return this.getAllParticipants(requestingUsername, (Current)null);
   }

   public final String[] getGroupModerators(String instigator) throws FusionException {
      return this.getGroupModerators(instigator, (Current)null);
   }

   public final int getMaximumMessageLength(String sender) {
      return this.getMaximumMessageLength(sender, (Current)null);
   }

   public final int getNumParticipants() {
      return this.getNumParticipants((Current)null);
   }

   public final ChatRoomDataIce getRoomData() {
      return this.getRoomData((Current)null);
   }

   public final Map<String, String> getTheme() {
      return this.getTheme((Current)null);
   }

   public final void inviteUserToGroup(String invitee, String inviter) throws FusionException {
      this.inviteUserToGroup(invitee, inviter, (Current)null);
   }

   public final boolean isLocked() {
      return this.isLocked((Current)null);
   }

   public final boolean isVisibleParticipant(String username) throws FusionException {
      return this.isVisibleParticipant(username, (Current)null);
   }

   public final void kickIndexes(int[] indexes, String bannedBy) throws FusionException {
      this.kickIndexes(indexes, bannedBy, (Current)null);
   }

   public final void listParticipants(String requestingUsername, int size, int startIndex) throws FusionException {
      this.listParticipants(requestingUsername, size, startIndex, (Current)null);
   }

   public final void lock(String locker) throws FusionException {
      this.lock(locker, (Current)null);
   }

   public final void mute(String username, String target) throws FusionException {
      this.mute(username, target, (Current)null);
   }

   public final void putMessage(MessageDataIce message, String sessionID) throws FusionException {
      this.putMessage(message, sessionID, (Current)null);
   }

   public final void putSystemMessage(String messageText, String[] emoticonKeys) {
      this.putSystemMessage(messageText, emoticonKeys, (Current)null);
   }

   public final void putSystemMessageWithColour(String messageText, String[] emoticonKeys, int messageColour) {
      this.putSystemMessageWithColour(messageText, emoticonKeys, messageColour, (Current)null);
   }

   public final void removeGroupModerator(String instigator, String targetUser) throws FusionException {
      this.removeGroupModerator(instigator, targetUser, (Current)null);
   }

   public final void removeModerator(String username) {
      this.removeModerator(username, (Current)null);
   }

   public final void removeParticipant(String username) throws FusionException {
      this.removeParticipant(username, (Current)null);
   }

   public final void removeParticipantOneWay(String username, boolean removeFromUsersChatRoomList) {
      this.removeParticipantOneWay(username, removeFromUsersChatRoomList, (Current)null);
   }

   public final void setAdultOnly(boolean adultOnly) {
      this.setAdultOnly(adultOnly, (Current)null);
   }

   public final void setAllowKicking(boolean allowKicking) {
      this.setAllowKicking(allowKicking, (Current)null);
   }

   public final void setDescription(String description) {
      this.setDescription(description, (Current)null);
   }

   public final void setMaximumSize(int maximumSize) {
      this.setMaximumSize(maximumSize, (Current)null);
   }

   public final void setNumberOfFakeParticipants(String username, int number) {
      this.setNumberOfFakeParticipants(username, number, (Current)null);
   }

   public final void silence(String username, int timeout) throws FusionException {
      this.silence(username, timeout, (Current)null);
   }

   public final void silenceUser(String instigator, String target, int timeout) throws FusionException {
      this.silenceUser(instigator, target, timeout, (Current)null);
   }

   public final void submitGiftAllTask(int giftId, String giftMessage, MessageDataIce message) throws FusionException {
      this.submitGiftAllTask(giftId, giftMessage, message, (Current)null);
   }

   public final void unbanGroupMember(String target, String instigator, int reasonCode) throws FusionException {
      this.unbanGroupMember(target, instigator, reasonCode, (Current)null);
   }

   public final void unbanUser(String username) {
      this.unbanUser(username, (Current)null);
   }

   public final void unlock(String unlocker) throws FusionException {
      this.unlock(unlocker, (Current)null);
   }

   public final void unmute(String username, String target) throws FusionException {
      this.unmute(username, target, (Current)null);
   }

   public final void unsilence(String username) throws FusionException {
      this.unsilence(username, (Current)null);
   }

   public final void unsilenceUser(String instigator, String target) throws FusionException {
      this.unsilenceUser(instigator, target, (Current)null);
   }

   public final void updateDescription(String instigator, String description) throws FusionException {
      this.updateDescription(instigator, description, (Current)null);
   }

   public final void updateExtraData(ChatRoomDataIce data) {
      this.updateExtraData(data, (Current)null);
   }

   public final void updateGroupModeratorStatus(String username, boolean promote) {
      this.updateGroupModeratorStatus(username, promote, (Current)null);
   }

   public final void voteToKickUser(String voter, String target) throws FusionException {
      this.voteToKickUser(voter, target, (Current)null);
   }

   public final void warnUser(String instigator, String target, String message) throws FusionException {
      this.warnUser(instigator, target, message, (Current)null);
   }

   public static DispatchStatus ___addParticipantOld(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      UserPrx userProxy = UserPrxHelper.__read(__is);
      UserDataIce userData = new UserDataIce();
      userData.__read(__is);
      SessionPrx sessionProxy = SessionPrxHelper.__read(__is);
      String sessionID = __is.readString();
      String ipAddress = __is.readString();
      String mobileDevice = __is.readString();
      String userAgent = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.addParticipantOld(userProxy, userData, sessionProxy, sessionID, ipAddress, mobileDevice, userAgent, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var13) {
         __os.writeUserException(var13);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___addParticipant(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      UserPrx userProxy = UserPrxHelper.__read(__is);
      UserDataIce userData = new UserDataIce();
      userData.__read(__is);
      SessionPrx sessionProxy = SessionPrxHelper.__read(__is);
      String sessionID = __is.readString();
      String ipAddress = __is.readString();
      String mobileDevice = __is.readString();
      String userAgent = __is.readString();
      short clientVersion = __is.readShort();
      int deviceType = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.addParticipant(userProxy, userData, sessionProxy, sessionID, ipAddress, mobileDevice, userAgent, clientVersion, deviceType, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var15) {
         __os.writeUserException(var15);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___removeParticipant(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.removeParticipant(username, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___removeParticipantOneWay(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      boolean removeFromUsersChatRoomList = __is.readBool();
      __is.endReadEncaps();
      __obj.removeParticipantOneWay(username, removeFromUsersChatRoomList, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___addModerator(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      __obj.addModerator(username, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___removeModerator(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      __obj.removeModerator(username, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___banUser(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      __obj.banUser(username, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___unbanUser(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      __obj.unbanUser(username, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___banGroupMembers(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String[] banList = StringArrayHelper.read(__is);
      String instigator = __is.readString();
      int reasonCode = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.banGroupMembers(banList, instigator, reasonCode, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___unbanGroupMember(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String target = __is.readString();
      String instigator = __is.readString();
      int reasonCode = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.unbanGroupMember(target, instigator, reasonCode, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___banMultiIds(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.banMultiIds(username, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___inviteUserToGroup(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String invitee = __is.readString();
      String inviter = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.inviteUserToGroup(invitee, inviter, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___broadcastMessage(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String instigator = __is.readString();
      String message = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.broadcastMessage(instigator, message, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___setMaximumSize(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int maximumSize = __is.readInt();
      __is.endReadEncaps();
      __obj.setMaximumSize(maximumSize, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___setDescription(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String description = __is.readString();
      __is.endReadEncaps();
      __obj.setDescription(description, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___updateDescription(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String instigator = __is.readString();
      String description = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.updateDescription(instigator, description, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___setAllowKicking(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      boolean allowKicking = __is.readBool();
      __is.endReadEncaps();
      __obj.setAllowKicking(allowKicking, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___setAdultOnly(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      boolean adultOnly = __is.readBool();
      __is.endReadEncaps();
      __obj.setAdultOnly(adultOnly, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___changeOwner(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String oldOwnerUsername = __is.readString();
      String newOwnerUsername = __is.readString();
      __is.endReadEncaps();
      __obj.changeOwner(oldOwnerUsername, newOwnerUsername, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getRoomData(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      ChatRoomDataIce __ret = __obj.getRoomData(__current);
      __ret.__write(__os);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getAllParticipants(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String requestingUsername = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      String[] __ret = __obj.getAllParticipants(requestingUsername, __current);
      StringArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getAdministrators(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String requestingUsername = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      String[] __ret = __obj.getAdministrators(requestingUsername, __current);
      StringArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getNumParticipants(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      int __ret = __obj.getNumParticipants(__current);
      __os.writeInt(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___isVisibleParticipant(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         boolean __ret = __obj.isVisibleParticipant(username, __current);
         __os.writeBool(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___listParticipants(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String requestingUsername = __is.readString();
      int size = __is.readInt();
      int startIndex = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.listParticipants(requestingUsername, size, startIndex, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___banIndexes(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int[] indexes = IntArrayHelper.read(__is);
      String bannedBy = __is.readString();
      int reasonCode = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.banIndexes(indexes, bannedBy, reasonCode, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___kickIndexes(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int[] indexes = IntArrayHelper.read(__is);
      String bannedBy = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.kickIndexes(indexes, bannedBy, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___bumpUser(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String instigator = __is.readString();
      String target = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.bumpUser(instigator, target, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___warnUser(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String instigator = __is.readString();
      String target = __is.readString();
      String message = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.warnUser(instigator, target, message, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___voteToKickUser(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String voter = __is.readString();
      String target = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.voteToKickUser(voter, target, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___clearUserKick(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String instigator = __is.readString();
      String target = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.clearUserKick(instigator, target, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putMessage(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      MessageDataIce message = new MessageDataIce();
      message.__read(__is);
      String sessionID = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putMessage(message, sessionID, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putSystemMessage(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String messageText = __is.readString();
      String[] emoticonKeys = StringArrayHelper.read(__is);
      __is.endReadEncaps();
      __obj.putSystemMessage(messageText, emoticonKeys, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___putSystemMessageWithColour(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String messageText = __is.readString();
      String[] emoticonKeys = StringArrayHelper.read(__is);
      int messageColour = __is.readInt();
      __is.endReadEncaps();
      __obj.putSystemMessageWithColour(messageText, emoticonKeys, messageColour, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getMaximumMessageLength(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String sender = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      int __ret = __obj.getMaximumMessageLength(sender, __current);
      __os.writeInt(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___addGroupModerator(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String instigator = __is.readString();
      String targetUser = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.addGroupModerator(instigator, targetUser, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___removeGroupModerator(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String instigator = __is.readString();
      String targetUser = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.removeGroupModerator(instigator, targetUser, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getGroupModerators(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String instigator = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         String[] __ret = __obj.getGroupModerators(instigator, __current);
         StringArrayHelper.write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___mute(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      String target = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.mute(username, target, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___unmute(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      String target = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.unmute(username, target, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___unsilence(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.unsilence(username, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___unsilenceUser(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String instigator = __is.readString();
      String target = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.unsilenceUser(instigator, target, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___silence(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      int timeout = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.silence(username, timeout, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___silenceUser(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String instigator = __is.readString();
      String target = __is.readString();
      int timeout = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.silenceUser(instigator, target, timeout, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___setNumberOfFakeParticipants(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      int number = __is.readInt();
      __is.endReadEncaps();
      __obj.setNumberOfFakeParticipants(username, number, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___convertIntoUserOwnedChatRoom(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.convertIntoUserOwnedChatRoom(__current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var5) {
         __os.writeUserException(var5);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___convertIntoGroupChatRoom(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int groupID = __is.readInt();
      String groupName = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.convertIntoGroupChatRoom(groupID, groupName, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___isLocked(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      boolean __ret = __obj.isLocked(__current);
      __os.writeBool(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___lock(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String locker = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.lock(locker, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___unlock(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String unlocker = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.unlock(unlocker, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___announceOff(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String announcer = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.announceOff(announcer, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___announceOn(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String announcer = __is.readString();
      String announceMessage = __is.readString();
      int waitTime = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.announceOn(announcer, announceMessage, waitTime, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___adminAnnounce(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String announceMessage = __is.readString();
      int waitTime = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.adminAnnounce(announceMessage, waitTime, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getTheme(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      Map<String, String> __ret = __obj.getTheme(__current);
      ParamMapHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___executeEmoteCommandWithState(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String emoteCommand = __is.readString();
      MessageDataIce message = new MessageDataIce();
      message.__read(__is);
      SessionPrx sessionProxy = SessionPrxHelper.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         int __ret = __obj.executeEmoteCommandWithState(emoteCommand, message, sessionProxy, __current);
         __os.writeInt(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___submitGiftAllTask(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int giftId = __is.readInt();
      String giftMessage = __is.readString();
      MessageDataIce message = new MessageDataIce();
      message.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.submitGiftAllTask(giftId, giftMessage, message, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___updateExtraData(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ChatRoomDataIce data = new ChatRoomDataIce();
      data.__read(__is);
      __is.endReadEncaps();
      __obj.updateExtraData(data, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___updateGroupModeratorStatus(ChatRoom __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      boolean promote = __is.readBool();
      __is.endReadEncaps();
      __obj.updateGroupModeratorStatus(username, promote, __current);
      return DispatchStatus.DispatchOK;
   }

   public DispatchStatus __dispatch(Incoming in, Current __current) {
      int pos = Arrays.binarySearch(__all, __current.operation);
      if (pos < 0) {
         throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
      } else {
         switch(pos) {
         case 0:
            return ___addGroupModerator(this, in, __current);
         case 1:
            return ___addModerator(this, in, __current);
         case 2:
            return ___addParticipant(this, in, __current);
         case 3:
            return ___addParticipantOld(this, in, __current);
         case 4:
            return ___adminAnnounce(this, in, __current);
         case 5:
            return ___announceOff(this, in, __current);
         case 6:
            return ___announceOn(this, in, __current);
         case 7:
            return ___banGroupMembers(this, in, __current);
         case 8:
            return ___banIndexes(this, in, __current);
         case 9:
            return ___banMultiIds(this, in, __current);
         case 10:
            return ___banUser(this, in, __current);
         case 11:
            return _BotChannelDisp.___botKilled(this, in, __current);
         case 12:
            return ___broadcastMessage(this, in, __current);
         case 13:
            return ___bumpUser(this, in, __current);
         case 14:
            return ___changeOwner(this, in, __current);
         case 15:
            return ___clearUserKick(this, in, __current);
         case 16:
            return ___convertIntoGroupChatRoom(this, in, __current);
         case 17:
            return ___convertIntoUserOwnedChatRoom(this, in, __current);
         case 18:
            return ___executeEmoteCommandWithState(this, in, __current);
         case 19:
            return ___getAdministrators(this, in, __current);
         case 20:
            return ___getAllParticipants(this, in, __current);
         case 21:
            return ___getGroupModerators(this, in, __current);
         case 22:
            return ___getMaximumMessageLength(this, in, __current);
         case 23:
            return ___getNumParticipants(this, in, __current);
         case 24:
            return _BotChannelDisp.___getParticipants(this, in, __current);
         case 25:
            return ___getRoomData(this, in, __current);
         case 26:
            return ___getTheme(this, in, __current);
         case 27:
            return ___ice_id(this, in, __current);
         case 28:
            return ___ice_ids(this, in, __current);
         case 29:
            return ___ice_isA(this, in, __current);
         case 30:
            return ___ice_ping(this, in, __current);
         case 31:
            return ___inviteUserToGroup(this, in, __current);
         case 32:
            return ___isLocked(this, in, __current);
         case 33:
            return _BotChannelDisp.___isParticipant(this, in, __current);
         case 34:
            return ___isVisibleParticipant(this, in, __current);
         case 35:
            return ___kickIndexes(this, in, __current);
         case 36:
            return ___listParticipants(this, in, __current);
         case 37:
            return ___lock(this, in, __current);
         case 38:
            return ___mute(this, in, __current);
         case 39:
            return _BotChannelDisp.___putBotMessage(this, in, __current);
         case 40:
            return _BotChannelDisp.___putBotMessageToAllUsers(this, in, __current);
         case 41:
            return _BotChannelDisp.___putBotMessageToUsers(this, in, __current);
         case 42:
            return ___putMessage(this, in, __current);
         case 43:
            return ___putSystemMessage(this, in, __current);
         case 44:
            return ___putSystemMessageWithColour(this, in, __current);
         case 45:
            return ___removeGroupModerator(this, in, __current);
         case 46:
            return ___removeModerator(this, in, __current);
         case 47:
            return ___removeParticipant(this, in, __current);
         case 48:
            return ___removeParticipantOneWay(this, in, __current);
         case 49:
            return _BotChannelDisp.___sendGamesHelpToUser(this, in, __current);
         case 50:
            return _BotChannelDisp.___sendMessageToBots(this, in, __current);
         case 51:
            return ___setAdultOnly(this, in, __current);
         case 52:
            return ___setAllowKicking(this, in, __current);
         case 53:
            return ___setDescription(this, in, __current);
         case 54:
            return ___setMaximumSize(this, in, __current);
         case 55:
            return ___setNumberOfFakeParticipants(this, in, __current);
         case 56:
            return ___silence(this, in, __current);
         case 57:
            return ___silenceUser(this, in, __current);
         case 58:
            return _BotChannelDisp.___startBot(this, in, __current);
         case 59:
            return _BotChannelDisp.___stopAllBots(this, in, __current);
         case 60:
            return _BotChannelDisp.___stopBot(this, in, __current);
         case 61:
            return ___submitGiftAllTask(this, in, __current);
         case 62:
            return ___unbanGroupMember(this, in, __current);
         case 63:
            return ___unbanUser(this, in, __current);
         case 64:
            return ___unlock(this, in, __current);
         case 65:
            return ___unmute(this, in, __current);
         case 66:
            return ___unsilence(this, in, __current);
         case 67:
            return ___unsilenceUser(this, in, __current);
         case 68:
            return ___updateDescription(this, in, __current);
         case 69:
            return ___updateExtraData(this, in, __current);
         case 70:
            return ___updateGroupModeratorStatus(this, in, __current);
         case 71:
            return ___voteToKickUser(this, in, __current);
         case 72:
            return ___warnUser(this, in, __current);
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
      ex.reason = "type com::projectgoth::fusion::slice::ChatRoom was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::ChatRoom was not generated with stream support";
      throw ex;
   }
}
