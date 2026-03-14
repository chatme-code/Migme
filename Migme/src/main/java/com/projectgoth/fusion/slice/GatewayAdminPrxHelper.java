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

public final class GatewayAdminPrxHelper extends ObjectPrxHelperBase implements GatewayAdminPrx {
   public GatewayStats getStats() throws FusionException {
      return this.getStats((Map)null, false);
   }

   public GatewayStats getStats(Map<String, String> __ctx) throws FusionException {
      return this.getStats(__ctx, true);
   }

   private GatewayStats getStats(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getStats");
            __delBase = this.__getDelegate(false);
            _GatewayAdminDel __del = (_GatewayAdminDel)__delBase;
            return __del.getStats(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendAlertToAllConnections(String message, String title) {
      this.sendAlertToAllConnections(message, title, (Map)null, false);
   }

   public void sendAlertToAllConnections(String message, String title, Map<String, String> __ctx) {
      this.sendAlertToAllConnections(message, title, __ctx, true);
   }

   private void sendAlertToAllConnections(String message, String title, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _GatewayAdminDel __del = (_GatewayAdminDel)__delBase;
            __del.sendAlertToAllConnections(message, title, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static GatewayAdminPrx checkedCast(ObjectPrx __obj) {
      GatewayAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (GatewayAdminPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::GatewayAdmin")) {
               GatewayAdminPrxHelper __h = new GatewayAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (GatewayAdminPrx)__d;
   }

   public static GatewayAdminPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      GatewayAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (GatewayAdminPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::GatewayAdmin", __ctx)) {
               GatewayAdminPrxHelper __h = new GatewayAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (GatewayAdminPrx)__d;
   }

   public static GatewayAdminPrx checkedCast(ObjectPrx __obj, String __facet) {
      GatewayAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::GatewayAdmin")) {
               GatewayAdminPrxHelper __h = new GatewayAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static GatewayAdminPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      GatewayAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::GatewayAdmin", __ctx)) {
               GatewayAdminPrxHelper __h = new GatewayAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static GatewayAdminPrx uncheckedCast(ObjectPrx __obj) {
      GatewayAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (GatewayAdminPrx)__obj;
         } catch (ClassCastException var4) {
            GatewayAdminPrxHelper __h = new GatewayAdminPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (GatewayAdminPrx)__d;
   }

   public static GatewayAdminPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      GatewayAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         GatewayAdminPrxHelper __h = new GatewayAdminPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _GatewayAdminDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _GatewayAdminDelD();
   }

   public static void __write(BasicStream __os, GatewayAdminPrx v) {
      __os.writeProxy(v);
   }

   public static GatewayAdminPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         GatewayAdminPrxHelper result = new GatewayAdminPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
