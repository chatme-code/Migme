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

public final class BotServicePrxHelper extends ObjectPrxHelperBase implements BotServicePrx {
   public BotInstance addBotToChannel(BotChannelPrx channelProxy, String botCommandName, String starterUsername, boolean purgeIfIdle) throws FusionException {
      return this.addBotToChannel(channelProxy, botCommandName, starterUsername, purgeIfIdle, (Map)null, false);
   }

   public BotInstance addBotToChannel(BotChannelPrx channelProxy, String botCommandName, String starterUsername, boolean purgeIfIdle, Map<String, String> __ctx) throws FusionException {
      return this.addBotToChannel(channelProxy, botCommandName, starterUsername, purgeIfIdle, __ctx, true);
   }

   private BotInstance addBotToChannel(BotChannelPrx channelProxy, String botCommandName, String starterUsername, boolean purgeIfIdle, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("addBotToChannel");
            __delBase = this.__getDelegate(false);
            _BotServiceDel __del = (_BotServiceDel)__delBase;
            return __del.addBotToChannel(channelProxy, botCommandName, starterUsername, purgeIfIdle, __ctx);
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void removeBot(String botInstanceID, boolean stopEvenIfGameInProgress) throws FusionException {
      this.removeBot(botInstanceID, stopEvenIfGameInProgress, (Map)null, false);
   }

   public void removeBot(String botInstanceID, boolean stopEvenIfGameInProgress, Map<String, String> __ctx) throws FusionException {
      this.removeBot(botInstanceID, stopEvenIfGameInProgress, __ctx, true);
   }

   private void removeBot(String botInstanceID, boolean stopEvenIfGameInProgress, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("removeBot");
            __delBase = this.__getDelegate(false);
            _BotServiceDel __del = (_BotServiceDel)__delBase;
            __del.removeBot(botInstanceID, stopEvenIfGameInProgress, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendMessageToBot(String botInstanceID, String username, String message, long receivedTimestamp) throws FusionException {
      this.sendMessageToBot(botInstanceID, username, message, receivedTimestamp, (Map)null, false);
   }

   public void sendMessageToBot(String botInstanceID, String username, String message, long receivedTimestamp, Map<String, String> __ctx) throws FusionException {
      this.sendMessageToBot(botInstanceID, username, message, receivedTimestamp, __ctx, true);
   }

   private void sendMessageToBot(String botInstanceID, String username, String message, long receivedTimestamp, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("sendMessageToBot");
            __delBase = this.__getDelegate(false);
            _BotServiceDel __del = (_BotServiceDel)__delBase;
            __del.sendMessageToBot(botInstanceID, username, message, receivedTimestamp, __ctx);
            return;
         } catch (LocalExceptionWrapper var11) {
            this.__handleExceptionWrapper(__delBase, var11, (OutgoingAsync)null);
         } catch (LocalException var12) {
            __cnt = this.__handleException(__delBase, var12, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendMessageToBotsInChannel(String channelID, String username, String message, long receivedTimestamp) throws FusionException {
      this.sendMessageToBotsInChannel(channelID, username, message, receivedTimestamp, (Map)null, false);
   }

   public void sendMessageToBotsInChannel(String channelID, String username, String message, long receivedTimestamp, Map<String, String> __ctx) throws FusionException {
      this.sendMessageToBotsInChannel(channelID, username, message, receivedTimestamp, __ctx, true);
   }

   private void sendMessageToBotsInChannel(String channelID, String username, String message, long receivedTimestamp, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("sendMessageToBotsInChannel");
            __delBase = this.__getDelegate(false);
            _BotServiceDel __del = (_BotServiceDel)__delBase;
            __del.sendMessageToBotsInChannel(channelID, username, message, receivedTimestamp, __ctx);
            return;
         } catch (LocalExceptionWrapper var11) {
            this.__handleExceptionWrapper(__delBase, var11, (OutgoingAsync)null);
         } catch (LocalException var12) {
            __cnt = this.__handleException(__delBase, var12, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendNotificationToBotsInChannel(String channelID, String username, int notification) throws FusionException {
      this.sendNotificationToBotsInChannel(channelID, username, notification, (Map)null, false);
   }

   public void sendNotificationToBotsInChannel(String channelID, String username, int notification, Map<String, String> __ctx) throws FusionException {
      this.sendNotificationToBotsInChannel(channelID, username, notification, __ctx, true);
   }

   private void sendNotificationToBotsInChannel(String channelID, String username, int notification, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("sendNotificationToBotsInChannel");
            __delBase = this.__getDelegate(false);
            _BotServiceDel __del = (_BotServiceDel)__delBase;
            __del.sendNotificationToBotsInChannel(channelID, username, notification, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static BotServicePrx checkedCast(ObjectPrx __obj) {
      BotServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (BotServicePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::BotService")) {
               BotServicePrxHelper __h = new BotServicePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (BotServicePrx)__d;
   }

   public static BotServicePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      BotServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (BotServicePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::BotService", __ctx)) {
               BotServicePrxHelper __h = new BotServicePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (BotServicePrx)__d;
   }

   public static BotServicePrx checkedCast(ObjectPrx __obj, String __facet) {
      BotServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::BotService")) {
               BotServicePrxHelper __h = new BotServicePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static BotServicePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      BotServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::BotService", __ctx)) {
               BotServicePrxHelper __h = new BotServicePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static BotServicePrx uncheckedCast(ObjectPrx __obj) {
      BotServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (BotServicePrx)__obj;
         } catch (ClassCastException var4) {
            BotServicePrxHelper __h = new BotServicePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (BotServicePrx)__d;
   }

   public static BotServicePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      BotServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         BotServicePrxHelper __h = new BotServicePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _BotServiceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _BotServiceDelD();
   }

   public static void __write(BasicStream __os, BotServicePrx v) {
      __os.writeProxy(v);
   }

   public static BotServicePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         BotServicePrxHelper result = new BotServicePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
