package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _BotServiceTie extends _BotServiceDisp implements TieBase {
   private _BotServiceOperations _ice_delegate;

   public _BotServiceTie() {
   }

   public _BotServiceTie(_BotServiceOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_BotServiceOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _BotServiceTie) ? false : this._ice_delegate.equals(((_BotServiceTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public BotInstance addBotToChannel(BotChannelPrx channelProxy, String botCommandName, String starterUsername, boolean purgeIfIdle, Current __current) throws FusionException {
      return this._ice_delegate.addBotToChannel(channelProxy, botCommandName, starterUsername, purgeIfIdle, __current);
   }

   public void removeBot(String botInstanceID, boolean stopEvenIfGameInProgress, Current __current) throws FusionException {
      this._ice_delegate.removeBot(botInstanceID, stopEvenIfGameInProgress, __current);
   }

   public void sendMessageToBot(String botInstanceID, String username, String message, long receivedTimestamp, Current __current) throws FusionException {
      this._ice_delegate.sendMessageToBot(botInstanceID, username, message, receivedTimestamp, __current);
   }

   public void sendMessageToBotsInChannel(String channelID, String username, String message, long receivedTimestamp, Current __current) throws FusionException {
      this._ice_delegate.sendMessageToBotsInChannel(channelID, username, message, receivedTimestamp, __current);
   }

   public void sendNotificationToBotsInChannel(String channelID, String username, int notification, Current __current) throws FusionException {
      this._ice_delegate.sendNotificationToBotsInChannel(channelID, username, notification, __current);
   }
}
