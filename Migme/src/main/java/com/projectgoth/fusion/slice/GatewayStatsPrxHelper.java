package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class GatewayStatsPrxHelper extends ObjectPrxHelperBase implements GatewayStatsPrx {
   public static GatewayStatsPrx checkedCast(ObjectPrx __obj) {
      GatewayStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (GatewayStatsPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::GatewayStats")) {
               GatewayStatsPrxHelper __h = new GatewayStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (GatewayStatsPrx)__d;
   }

   public static GatewayStatsPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      GatewayStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (GatewayStatsPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::GatewayStats", __ctx)) {
               GatewayStatsPrxHelper __h = new GatewayStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (GatewayStatsPrx)__d;
   }

   public static GatewayStatsPrx checkedCast(ObjectPrx __obj, String __facet) {
      GatewayStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::GatewayStats")) {
               GatewayStatsPrxHelper __h = new GatewayStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static GatewayStatsPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      GatewayStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::GatewayStats", __ctx)) {
               GatewayStatsPrxHelper __h = new GatewayStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static GatewayStatsPrx uncheckedCast(ObjectPrx __obj) {
      GatewayStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (GatewayStatsPrx)__obj;
         } catch (ClassCastException var4) {
            GatewayStatsPrxHelper __h = new GatewayStatsPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (GatewayStatsPrx)__d;
   }

   public static GatewayStatsPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      GatewayStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         GatewayStatsPrxHelper __h = new GatewayStatsPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _GatewayStatsDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _GatewayStatsDelD();
   }

   public static void __write(BasicStream __os, GatewayStatsPrx v) {
      __os.writeProxy(v);
   }

   public static GatewayStatsPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         GatewayStatsPrxHelper result = new GatewayStatsPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
