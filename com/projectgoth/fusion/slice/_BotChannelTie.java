package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _BotChannelTie extends _BotChannelDisp implements TieBase {
   private _BotChannelOperations _ice_delegate;

   public _BotChannelTie() {
   }

   public _BotChannelTie(_BotChannelOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_BotChannelOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _BotChannelTie) ? false : this._ice_delegate.equals(((_BotChannelTie)rhs)._ice_delegate);
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
}
