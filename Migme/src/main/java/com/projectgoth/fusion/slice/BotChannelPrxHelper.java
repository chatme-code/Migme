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

public final class BotChannelPrxHelper extends ObjectPrxHelperBase implements BotChannelPrx {
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
            _BotChannelDel __del = (_BotChannelDel)__delBase;
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
            _BotChannelDel __del = (_BotChannelDel)__delBase;
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
            _BotChannelDel __del = (_BotChannelDel)__delBase;
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
            _BotChannelDel __del = (_BotChannelDel)__delBase;
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
            _BotChannelDel __del = (_BotChannelDel)__delBase;
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
            _BotChannelDel __del = (_BotChannelDel)__delBase;
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
            _BotChannelDel __del = (_BotChannelDel)__delBase;
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
            _BotChannelDel __del = (_BotChannelDel)__delBase;
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
            _BotChannelDel __del = (_BotChannelDel)__delBase;
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
            _BotChannelDel __del = (_BotChannelDel)__delBase;
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
            _BotChannelDel __del = (_BotChannelDel)__delBase;
            __del.stopBot(username, botCommandName, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static BotChannelPrx checkedCast(ObjectPrx __obj) {
      BotChannelPrx __d = null;
      if (__obj != null) {
         try {
            __d = (BotChannelPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::BotChannel")) {
               BotChannelPrxHelper __h = new BotChannelPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (BotChannelPrx)__d;
   }

   public static BotChannelPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      BotChannelPrx __d = null;
      if (__obj != null) {
         try {
            __d = (BotChannelPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::BotChannel", __ctx)) {
               BotChannelPrxHelper __h = new BotChannelPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (BotChannelPrx)__d;
   }

   public static BotChannelPrx checkedCast(ObjectPrx __obj, String __facet) {
      BotChannelPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::BotChannel")) {
               BotChannelPrxHelper __h = new BotChannelPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static BotChannelPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      BotChannelPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::BotChannel", __ctx)) {
               BotChannelPrxHelper __h = new BotChannelPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static BotChannelPrx uncheckedCast(ObjectPrx __obj) {
      BotChannelPrx __d = null;
      if (__obj != null) {
         try {
            __d = (BotChannelPrx)__obj;
         } catch (ClassCastException var4) {
            BotChannelPrxHelper __h = new BotChannelPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (BotChannelPrx)__d;
   }

   public static BotChannelPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      BotChannelPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         BotChannelPrxHelper __h = new BotChannelPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _BotChannelDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _BotChannelDelD();
   }

   public static void __write(BasicStream __os, BotChannelPrx v) {
      __os.writeProxy(v);
   }

   public static BotChannelPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         BotChannelPrxHelper result = new BotChannelPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
