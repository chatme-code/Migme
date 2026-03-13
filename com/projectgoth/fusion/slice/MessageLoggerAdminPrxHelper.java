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

public final class MessageLoggerAdminPrxHelper extends ObjectPrxHelperBase implements MessageLoggerAdminPrx {
   public MessageLoggerStats getStats() throws FusionException {
      return this.getStats((Map)null, false);
   }

   public MessageLoggerStats getStats(Map<String, String> __ctx) throws FusionException {
      return this.getStats(__ctx, true);
   }

   private MessageLoggerStats getStats(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getStats");
            __delBase = this.__getDelegate(false);
            _MessageLoggerAdminDel __del = (_MessageLoggerAdminDel)__delBase;
            return __del.getStats(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static MessageLoggerAdminPrx checkedCast(ObjectPrx __obj) {
      MessageLoggerAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (MessageLoggerAdminPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::MessageLoggerAdmin")) {
               MessageLoggerAdminPrxHelper __h = new MessageLoggerAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (MessageLoggerAdminPrx)__d;
   }

   public static MessageLoggerAdminPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      MessageLoggerAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (MessageLoggerAdminPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::MessageLoggerAdmin", __ctx)) {
               MessageLoggerAdminPrxHelper __h = new MessageLoggerAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (MessageLoggerAdminPrx)__d;
   }

   public static MessageLoggerAdminPrx checkedCast(ObjectPrx __obj, String __facet) {
      MessageLoggerAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::MessageLoggerAdmin")) {
               MessageLoggerAdminPrxHelper __h = new MessageLoggerAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static MessageLoggerAdminPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      MessageLoggerAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::MessageLoggerAdmin", __ctx)) {
               MessageLoggerAdminPrxHelper __h = new MessageLoggerAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static MessageLoggerAdminPrx uncheckedCast(ObjectPrx __obj) {
      MessageLoggerAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (MessageLoggerAdminPrx)__obj;
         } catch (ClassCastException var4) {
            MessageLoggerAdminPrxHelper __h = new MessageLoggerAdminPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (MessageLoggerAdminPrx)__d;
   }

   public static MessageLoggerAdminPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      MessageLoggerAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         MessageLoggerAdminPrxHelper __h = new MessageLoggerAdminPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _MessageLoggerAdminDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _MessageLoggerAdminDelD();
   }

   public static void __write(BasicStream __os, MessageLoggerAdminPrx v) {
      __os.writeProxy(v);
   }

   public static MessageLoggerAdminPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         MessageLoggerAdminPrxHelper result = new MessageLoggerAdminPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
