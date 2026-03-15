package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _GroupChatTie extends _GroupChatDisp implements TieBase {
   private _GroupChatOperations _ice_delegate;

   public _GroupChatTie() {
   }

   public _GroupChatTie(_GroupChatOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_GroupChatOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _GroupChatTie) ? false : this._ice_delegate.equals(((_GroupChatTie)rhs)._ice_delegate);
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

   public void addParticipant(String inviterUsername, String inviteeUsername, Current __current) throws FusionException {
      this._ice_delegate.addParticipant(inviterUsername, inviteeUsername, __current);
   }

   public void addParticipantInner(String inviterUsername, String inviteeUsername, boolean debug, Current __current) throws FusionException {
      this._ice_delegate.addParticipantInner(inviterUsername, inviteeUsername, debug, __current);
   }

   public void addParticipants(String inviterUsername, String[] inviteeUsernames, Current __current) throws FusionException {
      this._ice_delegate.addParticipants(inviterUsername, inviteeUsernames, __current);
   }

   public void addUserToGroupChatDebug(String participant, boolean b, boolean c, Current __current) throws FusionException {
      this._ice_delegate.addUserToGroupChatDebug(participant, b, c, __current);
   }

   public int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy, Current __current) throws FusionException {
      return this._ice_delegate.executeEmoteCommandWithState(emoteCommand, message, sessionProxy, __current);
   }

   public int getCreatorUserID(Current __current) {
      return this._ice_delegate.getCreatorUserID(__current);
   }

   public String getCreatorUsername(Current __current) {
      return this._ice_delegate.getCreatorUsername(__current);
   }

   public String getId(Current __current) {
      return this._ice_delegate.getId(__current);
   }

   public int getNumParticipants(Current __current) {
      return this._ice_delegate.getNumParticipants(__current);
   }

   public int[] getParticipantUserIDs(Current __current) {
      return this._ice_delegate.getParticipantUserIDs(__current);
   }

   public int getPrivateChatPartnerUserID(Current __current) {
      return this._ice_delegate.getPrivateChatPartnerUserID(__current);
   }

   public String listOfParticipants(Current __current) {
      return this._ice_delegate.listOfParticipants(__current);
   }

   public void putFileReceived(MessageDataIce message, Current __current) throws FusionException {
      this._ice_delegate.putFileReceived(message, __current);
   }

   public void putMessage(MessageDataIce message, Current __current) throws FusionException {
      this._ice_delegate.putMessage(message, __current);
   }

   public boolean removeParticipant(String username, Current __current) throws FusionException {
      return this._ice_delegate.removeParticipant(username, __current);
   }

   public void sendInitialMessages(Current __current) {
      this._ice_delegate.sendInitialMessages(__current);
   }

   public boolean supportsBinaryMessage(String usernameToExclude, Current __current) {
      return this._ice_delegate.supportsBinaryMessage(usernameToExclude, __current);
   }
}
