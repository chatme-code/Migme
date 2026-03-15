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

public final class GroupChatPrxHelper extends ObjectPrxHelperBase implements GroupChatPrx {
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
            _GroupChatDel __del = (_GroupChatDel)__delBase;
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
            _GroupChatDel __del = (_GroupChatDel)__delBase;
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
            _GroupChatDel __del = (_GroupChatDel)__delBase;
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
            _GroupChatDel __del = (_GroupChatDel)__delBase;
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
            _GroupChatDel __del = (_GroupChatDel)__delBase;
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
            _GroupChatDel __del = (_GroupChatDel)__delBase;
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
            _GroupChatDel __del = (_GroupChatDel)__delBase;
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
            _GroupChatDel __del = (_GroupChatDel)__delBase;
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
            _GroupChatDel __del = (_GroupChatDel)__delBase;
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
            _GroupChatDel __del = (_GroupChatDel)__delBase;
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
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            __del.stopBot(username, botCommandName, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void addParticipant(String inviterUsername, String inviteeUsername) throws FusionException {
      this.addParticipant(inviterUsername, inviteeUsername, (Map)null, false);
   }

   public void addParticipant(String inviterUsername, String inviteeUsername, Map<String, String> __ctx) throws FusionException {
      this.addParticipant(inviterUsername, inviteeUsername, __ctx, true);
   }

   private void addParticipant(String inviterUsername, String inviteeUsername, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("addParticipant");
            __delBase = this.__getDelegate(false);
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            __del.addParticipant(inviterUsername, inviteeUsername, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void addParticipantInner(String inviterUsername, String inviteeUsername, boolean debug) throws FusionException {
      this.addParticipantInner(inviterUsername, inviteeUsername, debug, (Map)null, false);
   }

   public void addParticipantInner(String inviterUsername, String inviteeUsername, boolean debug, Map<String, String> __ctx) throws FusionException {
      this.addParticipantInner(inviterUsername, inviteeUsername, debug, __ctx, true);
   }

   private void addParticipantInner(String inviterUsername, String inviteeUsername, boolean debug, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("addParticipantInner");
            __delBase = this.__getDelegate(false);
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            __del.addParticipantInner(inviterUsername, inviteeUsername, debug, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void addParticipants(String inviterUsername, String[] inviteeUsernames) throws FusionException {
      this.addParticipants(inviterUsername, inviteeUsernames, (Map)null, false);
   }

   public void addParticipants(String inviterUsername, String[] inviteeUsernames, Map<String, String> __ctx) throws FusionException {
      this.addParticipants(inviterUsername, inviteeUsernames, __ctx, true);
   }

   private void addParticipants(String inviterUsername, String[] inviteeUsernames, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("addParticipants");
            __delBase = this.__getDelegate(false);
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            __del.addParticipants(inviterUsername, inviteeUsernames, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void addUserToGroupChatDebug(String participant, boolean b, boolean c) throws FusionException {
      this.addUserToGroupChatDebug(participant, b, c, (Map)null, false);
   }

   public void addUserToGroupChatDebug(String participant, boolean b, boolean c, Map<String, String> __ctx) throws FusionException {
      this.addUserToGroupChatDebug(participant, b, c, __ctx, true);
   }

   private void addUserToGroupChatDebug(String participant, boolean b, boolean c, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("addUserToGroupChatDebug");
            __delBase = this.__getDelegate(false);
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            __del.addUserToGroupChatDebug(participant, b, c, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
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
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            return __del.executeEmoteCommandWithState(emoteCommand, message, sessionProxy, __ctx);
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public int getCreatorUserID() {
      return this.getCreatorUserID((Map)null, false);
   }

   public int getCreatorUserID(Map<String, String> __ctx) {
      return this.getCreatorUserID(__ctx, true);
   }

   private int getCreatorUserID(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getCreatorUserID");
            __delBase = this.__getDelegate(false);
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            return __del.getCreatorUserID(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public String getCreatorUsername() {
      return this.getCreatorUsername((Map)null, false);
   }

   public String getCreatorUsername(Map<String, String> __ctx) {
      return this.getCreatorUsername(__ctx, true);
   }

   private String getCreatorUsername(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getCreatorUsername");
            __delBase = this.__getDelegate(false);
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            return __del.getCreatorUsername(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public String getId() {
      return this.getId((Map)null, false);
   }

   public String getId(Map<String, String> __ctx) {
      return this.getId(__ctx, true);
   }

   private String getId(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getId");
            __delBase = this.__getDelegate(false);
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            return __del.getId(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
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
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            return __del.getNumParticipants(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public int[] getParticipantUserIDs() {
      return this.getParticipantUserIDs((Map)null, false);
   }

   public int[] getParticipantUserIDs(Map<String, String> __ctx) {
      return this.getParticipantUserIDs(__ctx, true);
   }

   private int[] getParticipantUserIDs(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getParticipantUserIDs");
            __delBase = this.__getDelegate(false);
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            return __del.getParticipantUserIDs(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public int getPrivateChatPartnerUserID() {
      return this.getPrivateChatPartnerUserID((Map)null, false);
   }

   public int getPrivateChatPartnerUserID(Map<String, String> __ctx) {
      return this.getPrivateChatPartnerUserID(__ctx, true);
   }

   private int getPrivateChatPartnerUserID(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getPrivateChatPartnerUserID");
            __delBase = this.__getDelegate(false);
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            return __del.getPrivateChatPartnerUserID(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public String listOfParticipants() {
      return this.listOfParticipants((Map)null, false);
   }

   public String listOfParticipants(Map<String, String> __ctx) {
      return this.listOfParticipants(__ctx, true);
   }

   private String listOfParticipants(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("listOfParticipants");
            __delBase = this.__getDelegate(false);
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            return __del.listOfParticipants(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void putFileReceived(MessageDataIce message) throws FusionException {
      this.putFileReceived(message, (Map)null, false);
   }

   public void putFileReceived(MessageDataIce message, Map<String, String> __ctx) throws FusionException {
      this.putFileReceived(message, __ctx, true);
   }

   private void putFileReceived(MessageDataIce message, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("putFileReceived");
            __delBase = this.__getDelegate(false);
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            __del.putFileReceived(message, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void putMessage(MessageDataIce message) throws FusionException {
      this.putMessage(message, (Map)null, false);
   }

   public void putMessage(MessageDataIce message, Map<String, String> __ctx) throws FusionException {
      this.putMessage(message, __ctx, true);
   }

   private void putMessage(MessageDataIce message, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("putMessage");
            __delBase = this.__getDelegate(false);
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            __del.putMessage(message, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public boolean removeParticipant(String username) throws FusionException {
      return this.removeParticipant(username, (Map)null, false);
   }

   public boolean removeParticipant(String username, Map<String, String> __ctx) throws FusionException {
      return this.removeParticipant(username, __ctx, true);
   }

   private boolean removeParticipant(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("removeParticipant");
            __delBase = this.__getDelegate(false);
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            return __del.removeParticipant(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendInitialMessages() {
      this.sendInitialMessages((Map)null, false);
   }

   public void sendInitialMessages(Map<String, String> __ctx) {
      this.sendInitialMessages(__ctx, true);
   }

   private void sendInitialMessages(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            __del.sendInitialMessages(__ctx);
            return;
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public boolean supportsBinaryMessage(String usernameToExclude) {
      return this.supportsBinaryMessage(usernameToExclude, (Map)null, false);
   }

   public boolean supportsBinaryMessage(String usernameToExclude, Map<String, String> __ctx) {
      return this.supportsBinaryMessage(usernameToExclude, __ctx, true);
   }

   private boolean supportsBinaryMessage(String usernameToExclude, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("supportsBinaryMessage");
            __delBase = this.__getDelegate(false);
            _GroupChatDel __del = (_GroupChatDel)__delBase;
            return __del.supportsBinaryMessage(usernameToExclude, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static GroupChatPrx checkedCast(ObjectPrx __obj) {
      GroupChatPrx __d = null;
      if (__obj != null) {
         try {
            __d = (GroupChatPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::GroupChat")) {
               GroupChatPrxHelper __h = new GroupChatPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (GroupChatPrx)__d;
   }

   public static GroupChatPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      GroupChatPrx __d = null;
      if (__obj != null) {
         try {
            __d = (GroupChatPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::GroupChat", __ctx)) {
               GroupChatPrxHelper __h = new GroupChatPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (GroupChatPrx)__d;
   }

   public static GroupChatPrx checkedCast(ObjectPrx __obj, String __facet) {
      GroupChatPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::GroupChat")) {
               GroupChatPrxHelper __h = new GroupChatPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static GroupChatPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      GroupChatPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::GroupChat", __ctx)) {
               GroupChatPrxHelper __h = new GroupChatPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static GroupChatPrx uncheckedCast(ObjectPrx __obj) {
      GroupChatPrx __d = null;
      if (__obj != null) {
         try {
            __d = (GroupChatPrx)__obj;
         } catch (ClassCastException var4) {
            GroupChatPrxHelper __h = new GroupChatPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (GroupChatPrx)__d;
   }

   public static GroupChatPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      GroupChatPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         GroupChatPrxHelper __h = new GroupChatPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _GroupChatDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _GroupChatDelD();
   }

   public static void __write(BasicStream __os, GroupChatPrx v) {
      __os.writeProxy(v);
   }

   public static GroupChatPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         GroupChatPrxHelper result = new GroupChatPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
