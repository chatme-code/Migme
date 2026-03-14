package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.LocalException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDel;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import IceInternal.LocalExceptionWrapper;
import IceInternal.OutgoingAsync;
import java.util.Map;

public final class ChatRoomPrxHelper extends ObjectPrxHelperBase implements ChatRoomPrx {
   public void botKilled(String botInstanceID) throws FusionException {
      this.botKilled(botInstanceID, (Map)null, false);
   }

   public void botKilled(String botInstanceID, Map<String, String> __ctx) throws FusionException {
      this.botKilled(botInstanceID, __ctx, true);
   }

   private void botKilled(String botInstanceID, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("botKilled");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.botKilled(botInstanceID, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public String[] getParticipants(String requestingUsername) {
      return this.getParticipants(requestingUsername, (Map)null, false);
   }

   public String[] getParticipants(String requestingUsername, Map<String, String> __ctx) {
      return this.getParticipants(requestingUsername, __ctx, true);
   }

   private String[] getParticipants(String requestingUsername, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getParticipants");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            return __del.getParticipants(requestingUsername, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public boolean isParticipant(String username) throws FusionException {
      return this.isParticipant(username, (Map)null, false);
   }

   public boolean isParticipant(String username, Map<String, String> __ctx) throws FusionException {
      return this.isParticipant(username, __ctx, true);
   }

   private boolean isParticipant(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("isParticipant");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            return __del.isParticipant(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void putBotMessage(String botInstanceID, String username, String message, String[] emoticonHotKeys, boolean displayPopUp) throws FusionException {
      this.putBotMessage(botInstanceID, username, message, emoticonHotKeys, displayPopUp, (Map)null, false);
   }

   public void putBotMessage(String botInstanceID, String username, String message, String[] emoticonHotKeys, boolean displayPopUp, Map<String, String> __ctx) throws FusionException {
      this.putBotMessage(botInstanceID, username, message, emoticonHotKeys, displayPopUp, __ctx, true);
   }

   private void putBotMessage(String botInstanceID, String username, String message, String[] emoticonHotKeys, boolean displayPopUp, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("putBotMessage");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.putBotMessage(botInstanceID, username, message, emoticonHotKeys, displayPopUp, __ctx);
            return;
         } catch (LocalExceptionWrapper var11) {
            this.__handleExceptionWrapper(__delBase, var11, (OutgoingAsync)null);
         } catch (LocalException var12) {
            __cnt = this.__handleException(__delBase, var12, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void putBotMessageToAllUsers(String botInstanceID, String message, String[] emoticonHotKeys, boolean displayPopUp) throws FusionException {
      this.putBotMessageToAllUsers(botInstanceID, message, emoticonHotKeys, displayPopUp, (Map)null, false);
   }

   public void putBotMessageToAllUsers(String botInstanceID, String message, String[] emoticonHotKeys, boolean displayPopUp, Map<String, String> __ctx) throws FusionException {
      this.putBotMessageToAllUsers(botInstanceID, message, emoticonHotKeys, displayPopUp, __ctx, true);
   }

   private void putBotMessageToAllUsers(String botInstanceID, String message, String[] emoticonHotKeys, boolean displayPopUp, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("putBotMessageToAllUsers");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.putBotMessageToAllUsers(botInstanceID, message, emoticonHotKeys, displayPopUp, __ctx);
            return;
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void putBotMessageToUsers(String botInstanceID, String[] usernames, String message, String[] emoticonHotKeys, boolean displayPopUp) throws FusionException {
      this.putBotMessageToUsers(botInstanceID, usernames, message, emoticonHotKeys, displayPopUp, (Map)null, false);
   }

   public void putBotMessageToUsers(String botInstanceID, String[] usernames, String message, String[] emoticonHotKeys, boolean displayPopUp, Map<String, String> __ctx) throws FusionException {
      this.putBotMessageToUsers(botInstanceID, usernames, message, emoticonHotKeys, displayPopUp, __ctx, true);
   }

   private void putBotMessageToUsers(String botInstanceID, String[] usernames, String message, String[] emoticonHotKeys, boolean displayPopUp, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("putBotMessageToUsers");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.putBotMessageToUsers(botInstanceID, usernames, message, emoticonHotKeys, displayPopUp, __ctx);
            return;
         } catch (LocalExceptionWrapper var11) {
            this.__handleExceptionWrapper(__delBase, var11, (OutgoingAsync)null);
         } catch (LocalException var12) {
            __cnt = this.__handleException(__delBase, var12, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendGamesHelpToUser(String username) throws FusionException {
      this.sendGamesHelpToUser(username, (Map)null, false);
   }

   public void sendGamesHelpToUser(String username, Map<String, String> __ctx) throws FusionException {
      this.sendGamesHelpToUser(username, __ctx, true);
   }

   private void sendGamesHelpToUser(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("sendGamesHelpToUser");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.sendGamesHelpToUser(username, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendMessageToBots(String username, String message, long receivedTimestamp) throws FusionException {
      this.sendMessageToBots(username, message, receivedTimestamp, (Map)null, false);
   }

   public void sendMessageToBots(String username, String message, long receivedTimestamp, Map<String, String> __ctx) throws FusionException {
      this.sendMessageToBots(username, message, receivedTimestamp, __ctx, true);
   }

   private void sendMessageToBots(String username, String message, long receivedTimestamp, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("sendMessageToBots");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.sendMessageToBots(username, message, receivedTimestamp, __ctx);
            return;
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void startBot(String username, String botCommandName) throws FusionException {
      this.startBot(username, botCommandName, (Map)null, false);
   }

   public void startBot(String username, String botCommandName, Map<String, String> __ctx) throws FusionException {
      this.startBot(username, botCommandName, __ctx, true);
   }

   private void startBot(String username, String botCommandName, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("startBot");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.startBot(username, botCommandName, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void stopAllBots(String username, int timeout) throws FusionException {
      this.stopAllBots(username, timeout, (Map)null, false);
   }

   public void stopAllBots(String username, int timeout, Map<String, String> __ctx) throws FusionException {
      this.stopAllBots(username, timeout, __ctx, true);
   }

   private void stopAllBots(String username, int timeout, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("stopAllBots");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.stopAllBots(username, timeout, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void stopBot(String username, String botCommandName) throws FusionException {
      this.stopBot(username, botCommandName, (Map)null, false);
   }

   public void stopBot(String username, String botCommandName, Map<String, String> __ctx) throws FusionException {
      this.stopBot(username, botCommandName, __ctx, true);
   }

   private void stopBot(String username, String botCommandName, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("stopBot");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.stopBot(username, botCommandName, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void addGroupModerator(String instigator, String targetUser) throws FusionException {
      this.addGroupModerator(instigator, targetUser, (Map)null, false);
   }

   public void addGroupModerator(String instigator, String targetUser, Map<String, String> __ctx) throws FusionException {
      this.addGroupModerator(instigator, targetUser, __ctx, true);
   }

   private void addGroupModerator(String instigator, String targetUser, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("addGroupModerator");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.addGroupModerator(instigator, targetUser, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void addModerator(String username) {
      this.addModerator(username, (Map)null, false);
   }

   public void addModerator(String username, Map<String, String> __ctx) {
      this.addModerator(username, __ctx, true);
   }

   private void addModerator(String username, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.addModerator(username, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void addParticipant(UserPrx userProxy, UserDataIce userData, SessionPrx sessionProxy, String sessionID, String ipAddress, String mobileDevice, String userAgent, short clientVersion, int deviceType) throws FusionException {
      this.addParticipant(userProxy, userData, sessionProxy, sessionID, ipAddress, mobileDevice, userAgent, clientVersion, deviceType, (Map)null, false);
   }

   public void addParticipant(UserPrx userProxy, UserDataIce userData, SessionPrx sessionProxy, String sessionID, String ipAddress, String mobileDevice, String userAgent, short clientVersion, int deviceType, Map<String, String> __ctx) throws FusionException {
      this.addParticipant(userProxy, userData, sessionProxy, sessionID, ipAddress, mobileDevice, userAgent, clientVersion, deviceType, __ctx, true);
   }

   private void addParticipant(UserPrx userProxy, UserDataIce userData, SessionPrx sessionProxy, String sessionID, String ipAddress, String mobileDevice, String userAgent, short clientVersion, int deviceType, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("addParticipant");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.addParticipant(userProxy, userData, sessionProxy, sessionID, ipAddress, mobileDevice, userAgent, clientVersion, deviceType, __ctx);
            return;
         } catch (LocalExceptionWrapper var15) {
            this.__handleExceptionWrapper(__delBase, var15, (OutgoingAsync)null);
         } catch (LocalException var16) {
            __cnt = this.__handleException(__delBase, var16, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void addParticipantOld(UserPrx userProxy, UserDataIce userData, SessionPrx sessionProxy, String sessionID, String ipAddress, String mobileDevice, String userAgent) throws FusionException {
      this.addParticipantOld(userProxy, userData, sessionProxy, sessionID, ipAddress, mobileDevice, userAgent, (Map)null, false);
   }

   public void addParticipantOld(UserPrx userProxy, UserDataIce userData, SessionPrx sessionProxy, String sessionID, String ipAddress, String mobileDevice, String userAgent, Map<String, String> __ctx) throws FusionException {
      this.addParticipantOld(userProxy, userData, sessionProxy, sessionID, ipAddress, mobileDevice, userAgent, __ctx, true);
   }

   private void addParticipantOld(UserPrx userProxy, UserDataIce userData, SessionPrx sessionProxy, String sessionID, String ipAddress, String mobileDevice, String userAgent, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("addParticipantOld");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.addParticipantOld(userProxy, userData, sessionProxy, sessionID, ipAddress, mobileDevice, userAgent, __ctx);
            return;
         } catch (LocalExceptionWrapper var13) {
            this.__handleExceptionWrapper(__delBase, var13, (OutgoingAsync)null);
         } catch (LocalException var14) {
            __cnt = this.__handleException(__delBase, var14, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void adminAnnounce(String announceMessage, int waitTime) throws FusionException {
      this.adminAnnounce(announceMessage, waitTime, (Map)null, false);
   }

   public void adminAnnounce(String announceMessage, int waitTime, Map<String, String> __ctx) throws FusionException {
      this.adminAnnounce(announceMessage, waitTime, __ctx, true);
   }

   private void adminAnnounce(String announceMessage, int waitTime, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("adminAnnounce");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.adminAnnounce(announceMessage, waitTime, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void announceOff(String announcer) throws FusionException {
      this.announceOff(announcer, (Map)null, false);
   }

   public void announceOff(String announcer, Map<String, String> __ctx) throws FusionException {
      this.announceOff(announcer, __ctx, true);
   }

   private void announceOff(String announcer, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("announceOff");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.announceOff(announcer, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void announceOn(String announcer, String announceMessage, int waitTime) throws FusionException {
      this.announceOn(announcer, announceMessage, waitTime, (Map)null, false);
   }

   public void announceOn(String announcer, String announceMessage, int waitTime, Map<String, String> __ctx) throws FusionException {
      this.announceOn(announcer, announceMessage, waitTime, __ctx, true);
   }

   private void announceOn(String announcer, String announceMessage, int waitTime, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("announceOn");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.announceOn(announcer, announceMessage, waitTime, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void banGroupMembers(String[] banList, String instigator, int reasonCode) throws FusionException {
      this.banGroupMembers(banList, instigator, reasonCode, (Map)null, false);
   }

   public void banGroupMembers(String[] banList, String instigator, int reasonCode, Map<String, String> __ctx) throws FusionException {
      this.banGroupMembers(banList, instigator, reasonCode, __ctx, true);
   }

   private void banGroupMembers(String[] banList, String instigator, int reasonCode, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("banGroupMembers");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.banGroupMembers(banList, instigator, reasonCode, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void banIndexes(int[] indexes, String bannedBy, int reasonCode) throws FusionException {
      this.banIndexes(indexes, bannedBy, reasonCode, (Map)null, false);
   }

   public void banIndexes(int[] indexes, String bannedBy, int reasonCode, Map<String, String> __ctx) throws FusionException {
      this.banIndexes(indexes, bannedBy, reasonCode, __ctx, true);
   }

   private void banIndexes(int[] indexes, String bannedBy, int reasonCode, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("banIndexes");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.banIndexes(indexes, bannedBy, reasonCode, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void banMultiIds(String username) throws FusionException {
      this.banMultiIds(username, (Map)null, false);
   }

   public void banMultiIds(String username, Map<String, String> __ctx) throws FusionException {
      this.banMultiIds(username, __ctx, true);
   }

   private void banMultiIds(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("banMultiIds");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.banMultiIds(username, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void banUser(String username) {
      this.banUser(username, (Map)null, false);
   }

   public void banUser(String username, Map<String, String> __ctx) {
      this.banUser(username, __ctx, true);
   }

   private void banUser(String username, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.banUser(username, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void broadcastMessage(String instigator, String message) throws FusionException {
      this.broadcastMessage(instigator, message, (Map)null, false);
   }

   public void broadcastMessage(String instigator, String message, Map<String, String> __ctx) throws FusionException {
      this.broadcastMessage(instigator, message, __ctx, true);
   }

   private void broadcastMessage(String instigator, String message, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("broadcastMessage");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.broadcastMessage(instigator, message, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void bumpUser(String instigator, String target) throws FusionException {
      this.bumpUser(instigator, target, (Map)null, false);
   }

   public void bumpUser(String instigator, String target, Map<String, String> __ctx) throws FusionException {
      this.bumpUser(instigator, target, __ctx, true);
   }

   private void bumpUser(String instigator, String target, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("bumpUser");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.bumpUser(instigator, target, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void changeOwner(String oldOwnerUsername, String newOwnerUsername) {
      this.changeOwner(oldOwnerUsername, newOwnerUsername, (Map)null, false);
   }

   public void changeOwner(String oldOwnerUsername, String newOwnerUsername, Map<String, String> __ctx) {
      this.changeOwner(oldOwnerUsername, newOwnerUsername, __ctx, true);
   }

   private void changeOwner(String oldOwnerUsername, String newOwnerUsername, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.changeOwner(oldOwnerUsername, newOwnerUsername, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void clearUserKick(String instigator, String target) throws FusionException {
      this.clearUserKick(instigator, target, (Map)null, false);
   }

   public void clearUserKick(String instigator, String target, Map<String, String> __ctx) throws FusionException {
      this.clearUserKick(instigator, target, __ctx, true);
   }

   private void clearUserKick(String instigator, String target, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("clearUserKick");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.clearUserKick(instigator, target, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void convertIntoGroupChatRoom(int groupID, String groupName) throws FusionException {
      this.convertIntoGroupChatRoom(groupID, groupName, (Map)null, false);
   }

   public void convertIntoGroupChatRoom(int groupID, String groupName, Map<String, String> __ctx) throws FusionException {
      this.convertIntoGroupChatRoom(groupID, groupName, __ctx, true);
   }

   private void convertIntoGroupChatRoom(int groupID, String groupName, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("convertIntoGroupChatRoom");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.convertIntoGroupChatRoom(groupID, groupName, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void convertIntoUserOwnedChatRoom() throws FusionException {
      this.convertIntoUserOwnedChatRoom((Map)null, false);
   }

   public void convertIntoUserOwnedChatRoom(Map<String, String> __ctx) throws FusionException {
      this.convertIntoUserOwnedChatRoom(__ctx, true);
   }

   private void convertIntoUserOwnedChatRoom(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("convertIntoUserOwnedChatRoom");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.convertIntoUserOwnedChatRoom(__ctx);
            return;
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy) throws FusionException {
      return this.executeEmoteCommandWithState(emoteCommand, message, sessionProxy, (Map)null, false);
   }

   public int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy, Map<String, String> __ctx) throws FusionException {
      return this.executeEmoteCommandWithState(emoteCommand, message, sessionProxy, __ctx, true);
   }

   private int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("executeEmoteCommandWithState");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            return __del.executeEmoteCommandWithState(emoteCommand, message, sessionProxy, __ctx);
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public String[] getAdministrators(String requestingUsername) {
      return this.getAdministrators(requestingUsername, (Map)null, false);
   }

   public String[] getAdministrators(String requestingUsername, Map<String, String> __ctx) {
      return this.getAdministrators(requestingUsername, __ctx, true);
   }

   private String[] getAdministrators(String requestingUsername, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getAdministrators");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            return __del.getAdministrators(requestingUsername, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public String[] getAllParticipants(String requestingUsername) {
      return this.getAllParticipants(requestingUsername, (Map)null, false);
   }

   public String[] getAllParticipants(String requestingUsername, Map<String, String> __ctx) {
      return this.getAllParticipants(requestingUsername, __ctx, true);
   }

   private String[] getAllParticipants(String requestingUsername, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getAllParticipants");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            return __del.getAllParticipants(requestingUsername, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public String[] getGroupModerators(String instigator) throws FusionException {
      return this.getGroupModerators(instigator, (Map)null, false);
   }

   public String[] getGroupModerators(String instigator, Map<String, String> __ctx) throws FusionException {
      return this.getGroupModerators(instigator, __ctx, true);
   }

   private String[] getGroupModerators(String instigator, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getGroupModerators");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            return __del.getGroupModerators(instigator, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public int getMaximumMessageLength(String sender) {
      return this.getMaximumMessageLength(sender, (Map)null, false);
   }

   public int getMaximumMessageLength(String sender, Map<String, String> __ctx) {
      return this.getMaximumMessageLength(sender, __ctx, true);
   }

   private int getMaximumMessageLength(String sender, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getMaximumMessageLength");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            return __del.getMaximumMessageLength(sender, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public int getNumParticipants() {
      return this.getNumParticipants((Map)null, false);
   }

   public int getNumParticipants(Map<String, String> __ctx) {
      return this.getNumParticipants(__ctx, true);
   }

   private int getNumParticipants(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getNumParticipants");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            return __del.getNumParticipants(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public ChatRoomDataIce getRoomData() {
      return this.getRoomData((Map)null, false);
   }

   public ChatRoomDataIce getRoomData(Map<String, String> __ctx) {
      return this.getRoomData(__ctx, true);
   }

   private ChatRoomDataIce getRoomData(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getRoomData");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            return __del.getRoomData(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public Map<String, String> getTheme() {
      return this.getTheme((Map)null, false);
   }

   public Map<String, String> getTheme(Map<String, String> __ctx) {
      return this.getTheme(__ctx, true);
   }

   private Map<String, String> getTheme(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getTheme");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            return __del.getTheme(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void inviteUserToGroup(String invitee, String inviter) throws FusionException {
      this.inviteUserToGroup(invitee, inviter, (Map)null, false);
   }

   public void inviteUserToGroup(String invitee, String inviter, Map<String, String> __ctx) throws FusionException {
      this.inviteUserToGroup(invitee, inviter, __ctx, true);
   }

   private void inviteUserToGroup(String invitee, String inviter, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("inviteUserToGroup");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.inviteUserToGroup(invitee, inviter, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public boolean isLocked() {
      return this.isLocked((Map)null, false);
   }

   public boolean isLocked(Map<String, String> __ctx) {
      return this.isLocked(__ctx, true);
   }

   private boolean isLocked(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("isLocked");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            return __del.isLocked(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public boolean isVisibleParticipant(String username) throws FusionException {
      return this.isVisibleParticipant(username, (Map)null, false);
   }

   public boolean isVisibleParticipant(String username, Map<String, String> __ctx) throws FusionException {
      return this.isVisibleParticipant(username, __ctx, true);
   }

   private boolean isVisibleParticipant(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("isVisibleParticipant");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            return __del.isVisibleParticipant(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void kickIndexes(int[] indexes, String bannedBy) throws FusionException {
      this.kickIndexes(indexes, bannedBy, (Map)null, false);
   }

   public void kickIndexes(int[] indexes, String bannedBy, Map<String, String> __ctx) throws FusionException {
      this.kickIndexes(indexes, bannedBy, __ctx, true);
   }

   private void kickIndexes(int[] indexes, String bannedBy, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("kickIndexes");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.kickIndexes(indexes, bannedBy, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void listParticipants(String requestingUsername, int size, int startIndex) throws FusionException {
      this.listParticipants(requestingUsername, size, startIndex, (Map)null, false);
   }

   public void listParticipants(String requestingUsername, int size, int startIndex, Map<String, String> __ctx) throws FusionException {
      this.listParticipants(requestingUsername, size, startIndex, __ctx, true);
   }

   private void listParticipants(String requestingUsername, int size, int startIndex, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("listParticipants");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.listParticipants(requestingUsername, size, startIndex, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void lock(String locker) throws FusionException {
      this.lock(locker, (Map)null, false);
   }

   public void lock(String locker, Map<String, String> __ctx) throws FusionException {
      this.lock(locker, __ctx, true);
   }

   private void lock(String locker, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("lock");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.lock(locker, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void mute(String username, String target) throws FusionException {
      this.mute(username, target, (Map)null, false);
   }

   public void mute(String username, String target, Map<String, String> __ctx) throws FusionException {
      this.mute(username, target, __ctx, true);
   }

   private void mute(String username, String target, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("mute");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.mute(username, target, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void putMessage(MessageDataIce message, String sessionID) throws FusionException {
      this.putMessage(message, sessionID, (Map)null, false);
   }

   public void putMessage(MessageDataIce message, String sessionID, Map<String, String> __ctx) throws FusionException {
      this.putMessage(message, sessionID, __ctx, true);
   }

   private void putMessage(MessageDataIce message, String sessionID, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("putMessage");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.putMessage(message, sessionID, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void putSystemMessage(String messageText, String[] emoticonKeys) {
      this.putSystemMessage(messageText, emoticonKeys, (Map)null, false);
   }

   public void putSystemMessage(String messageText, String[] emoticonKeys, Map<String, String> __ctx) {
      this.putSystemMessage(messageText, emoticonKeys, __ctx, true);
   }

   private void putSystemMessage(String messageText, String[] emoticonKeys, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.putSystemMessage(messageText, emoticonKeys, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void putSystemMessageWithColour(String messageText, String[] emoticonKeys, int messageColour) {
      this.putSystemMessageWithColour(messageText, emoticonKeys, messageColour, (Map)null, false);
   }

   public void putSystemMessageWithColour(String messageText, String[] emoticonKeys, int messageColour, Map<String, String> __ctx) {
      this.putSystemMessageWithColour(messageText, emoticonKeys, messageColour, __ctx, true);
   }

   private void putSystemMessageWithColour(String messageText, String[] emoticonKeys, int messageColour, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.putSystemMessageWithColour(messageText, emoticonKeys, messageColour, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void removeGroupModerator(String instigator, String targetUser) throws FusionException {
      this.removeGroupModerator(instigator, targetUser, (Map)null, false);
   }

   public void removeGroupModerator(String instigator, String targetUser, Map<String, String> __ctx) throws FusionException {
      this.removeGroupModerator(instigator, targetUser, __ctx, true);
   }

   private void removeGroupModerator(String instigator, String targetUser, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("removeGroupModerator");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.removeGroupModerator(instigator, targetUser, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void removeModerator(String username) {
      this.removeModerator(username, (Map)null, false);
   }

   public void removeModerator(String username, Map<String, String> __ctx) {
      this.removeModerator(username, __ctx, true);
   }

   private void removeModerator(String username, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.removeModerator(username, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void removeParticipant(String username) throws FusionException {
      this.removeParticipant(username, (Map)null, false);
   }

   public void removeParticipant(String username, Map<String, String> __ctx) throws FusionException {
      this.removeParticipant(username, __ctx, true);
   }

   private void removeParticipant(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("removeParticipant");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.removeParticipant(username, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void removeParticipantOneWay(String username, boolean removeFromUsersChatRoomList) {
      this.removeParticipantOneWay(username, removeFromUsersChatRoomList, (Map)null, false);
   }

   public void removeParticipantOneWay(String username, boolean removeFromUsersChatRoomList, Map<String, String> __ctx) {
      this.removeParticipantOneWay(username, removeFromUsersChatRoomList, __ctx, true);
   }

   private void removeParticipantOneWay(String username, boolean removeFromUsersChatRoomList, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.removeParticipantOneWay(username, removeFromUsersChatRoomList, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void setAdultOnly(boolean adultOnly) {
      this.setAdultOnly(adultOnly, (Map)null, false);
   }

   public void setAdultOnly(boolean adultOnly, Map<String, String> __ctx) {
      this.setAdultOnly(adultOnly, __ctx, true);
   }

   private void setAdultOnly(boolean adultOnly, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.setAdultOnly(adultOnly, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void setAllowKicking(boolean allowKicking) {
      this.setAllowKicking(allowKicking, (Map)null, false);
   }

   public void setAllowKicking(boolean allowKicking, Map<String, String> __ctx) {
      this.setAllowKicking(allowKicking, __ctx, true);
   }

   private void setAllowKicking(boolean allowKicking, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.setAllowKicking(allowKicking, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void setDescription(String description) {
      this.setDescription(description, (Map)null, false);
   }

   public void setDescription(String description, Map<String, String> __ctx) {
      this.setDescription(description, __ctx, true);
   }

   private void setDescription(String description, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.setDescription(description, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void setMaximumSize(int maximumSize) {
      this.setMaximumSize(maximumSize, (Map)null, false);
   }

   public void setMaximumSize(int maximumSize, Map<String, String> __ctx) {
      this.setMaximumSize(maximumSize, __ctx, true);
   }

   private void setMaximumSize(int maximumSize, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.setMaximumSize(maximumSize, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void setNumberOfFakeParticipants(String username, int number) {
      this.setNumberOfFakeParticipants(username, number, (Map)null, false);
   }

   public void setNumberOfFakeParticipants(String username, int number, Map<String, String> __ctx) {
      this.setNumberOfFakeParticipants(username, number, __ctx, true);
   }

   private void setNumberOfFakeParticipants(String username, int number, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.setNumberOfFakeParticipants(username, number, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void silence(String username, int timeout) throws FusionException {
      this.silence(username, timeout, (Map)null, false);
   }

   public void silence(String username, int timeout, Map<String, String> __ctx) throws FusionException {
      this.silence(username, timeout, __ctx, true);
   }

   private void silence(String username, int timeout, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("silence");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.silence(username, timeout, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void silenceUser(String instigator, String target, int timeout) throws FusionException {
      this.silenceUser(instigator, target, timeout, (Map)null, false);
   }

   public void silenceUser(String instigator, String target, int timeout, Map<String, String> __ctx) throws FusionException {
      this.silenceUser(instigator, target, timeout, __ctx, true);
   }

   private void silenceUser(String instigator, String target, int timeout, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("silenceUser");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.silenceUser(instigator, target, timeout, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void submitGiftAllTask(int giftId, String giftMessage, MessageDataIce message) throws FusionException {
      this.submitGiftAllTask(giftId, giftMessage, message, (Map)null, false);
   }

   public void submitGiftAllTask(int giftId, String giftMessage, MessageDataIce message, Map<String, String> __ctx) throws FusionException {
      this.submitGiftAllTask(giftId, giftMessage, message, __ctx, true);
   }

   private void submitGiftAllTask(int giftId, String giftMessage, MessageDataIce message, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("submitGiftAllTask");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.submitGiftAllTask(giftId, giftMessage, message, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void unbanGroupMember(String target, String instigator, int reasonCode) throws FusionException {
      this.unbanGroupMember(target, instigator, reasonCode, (Map)null, false);
   }

   public void unbanGroupMember(String target, String instigator, int reasonCode, Map<String, String> __ctx) throws FusionException {
      this.unbanGroupMember(target, instigator, reasonCode, __ctx, true);
   }

   private void unbanGroupMember(String target, String instigator, int reasonCode, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("unbanGroupMember");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.unbanGroupMember(target, instigator, reasonCode, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void unbanUser(String username) {
      this.unbanUser(username, (Map)null, false);
   }

   public void unbanUser(String username, Map<String, String> __ctx) {
      this.unbanUser(username, __ctx, true);
   }

   private void unbanUser(String username, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.unbanUser(username, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void unlock(String unlocker) throws FusionException {
      this.unlock(unlocker, (Map)null, false);
   }

   public void unlock(String unlocker, Map<String, String> __ctx) throws FusionException {
      this.unlock(unlocker, __ctx, true);
   }

   private void unlock(String unlocker, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("unlock");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.unlock(unlocker, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void unmute(String username, String target) throws FusionException {
      this.unmute(username, target, (Map)null, false);
   }

   public void unmute(String username, String target, Map<String, String> __ctx) throws FusionException {
      this.unmute(username, target, __ctx, true);
   }

   private void unmute(String username, String target, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("unmute");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.unmute(username, target, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void unsilence(String username) throws FusionException {
      this.unsilence(username, (Map)null, false);
   }

   public void unsilence(String username, Map<String, String> __ctx) throws FusionException {
      this.unsilence(username, __ctx, true);
   }

   private void unsilence(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("unsilence");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.unsilence(username, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void unsilenceUser(String instigator, String target) throws FusionException {
      this.unsilenceUser(instigator, target, (Map)null, false);
   }

   public void unsilenceUser(String instigator, String target, Map<String, String> __ctx) throws FusionException {
      this.unsilenceUser(instigator, target, __ctx, true);
   }

   private void unsilenceUser(String instigator, String target, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("unsilenceUser");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.unsilenceUser(instigator, target, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void updateDescription(String instigator, String description) throws FusionException {
      this.updateDescription(instigator, description, (Map)null, false);
   }

   public void updateDescription(String instigator, String description, Map<String, String> __ctx) throws FusionException {
      this.updateDescription(instigator, description, __ctx, true);
   }

   private void updateDescription(String instigator, String description, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("updateDescription");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.updateDescription(instigator, description, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void updateExtraData(ChatRoomDataIce data) {
      this.updateExtraData(data, (Map)null, false);
   }

   public void updateExtraData(ChatRoomDataIce data, Map<String, String> __ctx) {
      this.updateExtraData(data, __ctx, true);
   }

   private void updateExtraData(ChatRoomDataIce data, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.updateExtraData(data, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void updateGroupModeratorStatus(String username, boolean promote) {
      this.updateGroupModeratorStatus(username, promote, (Map)null, false);
   }

   public void updateGroupModeratorStatus(String username, boolean promote, Map<String, String> __ctx) {
      this.updateGroupModeratorStatus(username, promote, __ctx, true);
   }

   private void updateGroupModeratorStatus(String username, boolean promote, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.updateGroupModeratorStatus(username, promote, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void voteToKickUser(String voter, String target) throws FusionException {
      this.voteToKickUser(voter, target, (Map)null, false);
   }

   public void voteToKickUser(String voter, String target, Map<String, String> __ctx) throws FusionException {
      this.voteToKickUser(voter, target, __ctx, true);
   }

   private void voteToKickUser(String voter, String target, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("voteToKickUser");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.voteToKickUser(voter, target, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void warnUser(String instigator, String target, String message) throws FusionException {
      this.warnUser(instigator, target, message, (Map)null, false);
   }

   public void warnUser(String instigator, String target, String message, Map<String, String> __ctx) throws FusionException {
      this.warnUser(instigator, target, message, __ctx, true);
   }

   private void warnUser(String instigator, String target, String message, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("warnUser");
            __delBase = this.__getDelegate(false);
            _ChatRoomDel __del = (_ChatRoomDel)__delBase;
            __del.warnUser(instigator, target, message, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static ChatRoomPrx checkedCast(ObjectPrx __obj) {
      ChatRoomPrx __d = null;
      if (__obj != null) {
         try {
            __d = (ChatRoomPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::ChatRoom")) {
               ChatRoomPrxHelper __h = new ChatRoomPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (ChatRoomPrx)__d;
   }

   public static ChatRoomPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      ChatRoomPrx __d = null;
      if (__obj != null) {
         try {
            __d = (ChatRoomPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::ChatRoom", __ctx)) {
               ChatRoomPrxHelper __h = new ChatRoomPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (ChatRoomPrx)__d;
   }

   public static ChatRoomPrx checkedCast(ObjectPrx __obj, String __facet) {
      ChatRoomPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::ChatRoom")) {
               ChatRoomPrxHelper __h = new ChatRoomPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static ChatRoomPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      ChatRoomPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::ChatRoom", __ctx)) {
               ChatRoomPrxHelper __h = new ChatRoomPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static ChatRoomPrx uncheckedCast(ObjectPrx __obj) {
      ChatRoomPrx __d = null;
      if (__obj != null) {
         try {
            __d = (ChatRoomPrx)__obj;
         } catch (ClassCastException var4) {
            ChatRoomPrxHelper __h = new ChatRoomPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (ChatRoomPrx)__d;
   }

   public static ChatRoomPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      ChatRoomPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         ChatRoomPrxHelper __h = new ChatRoomPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _ChatRoomDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _ChatRoomDelD();
   }

   public static void __write(BasicStream __os, ChatRoomPrx v) {
      __os.writeProxy(v);
   }

   public static ChatRoomPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         ChatRoomPrxHelper result = new ChatRoomPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
