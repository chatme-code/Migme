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

public final class BotServiceAdminPrxHelper extends ObjectPrxHelperBase implements BotServiceAdminPrx {
   public BotServiceStats getStats() throws FusionException {
      return this.getStats((Map)null, false);
   }

   public BotServiceStats getStats(Map<String, String> __ctx) throws FusionException {
      return this.getStats(__ctx, true);
   }

   private BotServiceStats getStats(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getStats");
            __delBase = this.__getDelegate(false);
            _BotServiceAdminDel __del = (_BotServiceAdminDel)__delBase;
            return __del.getStats(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public int ping() {
      return this.ping((Map)null, false);
   }

   public int ping(Map<String, String> __ctx) {
      return this.ping(__ctx, true);
   }

   private int ping(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("ping");
            __delBase = this.__getDelegate(false);
            _BotServiceAdminDel __del = (_BotServiceAdminDel)__delBase;
            return __del.ping(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static BotServiceAdminPrx checkedCast(ObjectPrx __obj) {
      BotServiceAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (BotServiceAdminPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::BotServiceAdmin")) {
               BotServiceAdminPrxHelper __h = new BotServiceAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (BotServiceAdminPrx)__d;
   }

   public static BotServiceAdminPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      BotServiceAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (BotServiceAdminPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::BotServiceAdmin", __ctx)) {
               BotServiceAdminPrxHelper __h = new BotServiceAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (BotServiceAdminPrx)__d;
   }

   public static BotServiceAdminPrx checkedCast(ObjectPrx __obj, String __facet) {
      BotServiceAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::BotServiceAdmin")) {
               BotServiceAdminPrxHelper __h = new BotServiceAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static BotServiceAdminPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      BotServiceAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::BotServiceAdmin", __ctx)) {
               BotServiceAdminPrxHelper __h = new BotServiceAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static BotServiceAdminPrx uncheckedCast(ObjectPrx __obj) {
      BotServiceAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (BotServiceAdminPrx)__obj;
         } catch (ClassCastException var4) {
            BotServiceAdminPrxHelper __h = new BotServiceAdminPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (BotServiceAdminPrx)__d;
   }

   public static BotServiceAdminPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      BotServiceAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         BotServiceAdminPrxHelper __h = new BotServiceAdminPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _BotServiceAdminDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _BotServiceAdminDelD();
   }

   public static void __write(BasicStream __os, BotServiceAdminPrx v) {
      __os.writeProxy(v);
   }

   public static BotServiceAdminPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         BotServiceAdminPrxHelper result = new BotServiceAdminPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
