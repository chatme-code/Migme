package com.projectgoth.fusion.objectcache;

import Ice.Current;
import com.projectgoth.fusion.chatsync.ChatDefinition;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice._GroupChatDisp;

public class GroupChatRpcI extends _GroupChatDisp {
   private ChatGroup group;

   public GroupChatRpcI(ChatGroup group) {
      this.group = group;
   }

   public void sendInitialMessages(Current __current) {
      this.group.sendInitialMessages();
   }

   public String listOfParticipants(Current __current) {
      return this.group.listOfParticipants();
   }

   public void addParticipants(String inviterUsername, String[] inviteeUsernames, Current __current) throws FusionException {
      this.group.addParticipants(inviterUsername, inviteeUsernames);
   }

   public void addParticipant(String inviterUsername, String inviteeUsername, Current __current) throws FusionException {
      this.group.addParticipant(inviterUsername, inviteeUsername);
   }

   public void addParticipantInner(String inviterUsername, String inviteeUsername, boolean debugLogging, Current __current) throws FusionException {
      this.group.addParticipant(inviterUsername, inviteeUsername, debugLogging);
   }

   public boolean removeParticipant(String username, Current __current) throws FusionException {
      return this.group.removeParticipant(username);
   }

   public void putMessage(MessageDataIce messageIce, Current __current) throws FusionException {
      this.group.putMessage(messageIce);
   }

   public void putFileReceived(MessageDataIce messageIce, Current __current) throws FusionException {
      this.group.putFileReceived(messageIce);
   }

   public void startBot(String username, String botName, Current __current) throws FusionException {
      this.group.startBot(username, botName);
   }

   public void stopBot(String username, String botName, Current __current) throws FusionException {
      this.group.stopBot(username, botName);
   }

   public void stopAllBots(String username, int timeout, Current current) throws FusionException {
      this.group.stopAllBots(username, timeout);
   }

   public void botKilled(String botInstanceID, Current __current) throws FusionException {
      this.group.botKilled(botInstanceID);
   }

   public void sendMessageToBots(String username, String message, long receivedTimestamp, Current __current) throws FusionException {
      this.group.sendMessageToBots(username, message, receivedTimestamp);
   }

   public void putBotMessage(String botInstanceID, String username, String message, String[] emoticonHotKeys, boolean displayPopUp, Current __current) throws FusionException {
      this.group.putBotMessage(botInstanceID, username, message, emoticonHotKeys, displayPopUp);
   }

   public void putBotMessageToUsers(String botInstanceID, String[] usernames, String message, String[] emoticonHotKeys, boolean displayPopUp, Current current) throws FusionException {
      this.group.putBotMessageToUsers(botInstanceID, usernames, message, emoticonHotKeys, displayPopUp);
   }

   public void putBotMessageToAllUsers(String botInstanceID, String message, String[] emoticonHotKeys, boolean displayPopUp, Current __current) {
      this.group.putBotMessageToAllUsers(botInstanceID, message, emoticonHotKeys, displayPopUp);
   }

   public void sendGamesHelpToUser(String username, Current __current) throws FusionException {
      this.group.sendGamesHelpToUser(username);
   }

   public int getNumParticipants(Current __current) {
      return this.group.getNumParticipants();
   }

   public String[] getParticipants(String requestingUsername, Current __current) {
      return this.group.getParticipants(requestingUsername);
   }

   public int[] getParticipantUserIDs(Current __current) {
      return this.group.getParticipantUserIDs();
   }

   public boolean supportsBinaryMessage(String usernameToExclude, Current __current) {
      return this.group.supportsBinaryMessage(usernameToExclude);
   }

   public boolean isParticipant(String username, Current __current) throws FusionException {
      return this.group.isParticipant(username);
   }

   public int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy, Current __current) throws FusionException {
      return this.group.executeEmoteCommandWithState(emoteCommand, message, sessionProxy);
   }

   public String getCreatorUsername(Current __current) {
      return this.group.getCreatorUsername();
   }

   public int getCreatorUserID(Current __current) {
      return this.group.getCreatorUserID();
   }

   public int getPrivateChatPartnerUserID(Current __current) {
      return this.group.getPrivateChatPartnerUserID();
   }

   public String getId(Current __current) {
      return this.group.getId();
   }

   public void setGroupChatPrx(GroupChatPrx groupChatPrx) {
      this.group.setGroupChatPrx(groupChatPrx);
   }

   public void addInitialParticipants(String creator, String privateChatParticipant, String[] otherPartyList) throws FusionException {
      this.group.addInitialParticipants(creator, privateChatParticipant, otherPartyList);
   }

   public ChatDefinition toChatDefinition() throws FusionException {
      return this.group.toChatDefinition();
   }

   public void removeAllParticipants() {
      this.group.removeAllParticipants();
   }

   public void addUserToGroupChatDebug(String participant, boolean b, boolean c, Current __current) throws FusionException {
      this.group.addUserToGroupChat(participant, b, c, true);
   }

   public void addUserToGroupChat(String participant, boolean b, boolean c) throws FusionException {
      this.group.addUserToGroupChat(participant, b, c);
   }

   public void setCreatorParticipant(String creator) throws FusionException {
      this.group.setCreatorParticipant(creator);
   }

   public boolean isMarkedForRemoval() {
      return this.group.isMarkedForRemoval();
   }

   public long getTimeLastMessageSent() {
      return this.group.getTimeLastMessageSent();
   }

   public void sendAdminMessageToParticipants(String messageText, String usernameToExclude) {
      this.group.sendAdminMessageToParticipants(messageText, usernameToExclude);
   }
}
