package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class BlueLabelServiceStatsPrxHelper extends ObjectPrxHelperBase implements BlueLabelServiceStatsPrx {
   public static BlueLabelServiceStatsPrx checkedCast(ObjectPrx __obj) {
      BlueLabelServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (BlueLabelServiceStatsPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::BlueLabelServiceStats")) {
               BlueLabelServiceStatsPrxHelper __h = new BlueLabelServiceStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (BlueLabelServiceStatsPrx)__d;
   }

   public static BlueLabelServiceStatsPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      BlueLabelServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (BlueLabelServiceStatsPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::BlueLabelServiceStats", __ctx)) {
               BlueLabelServiceStatsPrxHelper __h = new BlueLabelServiceStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (BlueLabelServiceStatsPrx)__d;
   }

   public static BlueLabelServiceStatsPrx checkedCast(ObjectPrx __obj, String __facet) {
      BlueLabelServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::BlueLabelServiceStats")) {
               BlueLabelServiceStatsPrxHelper __h = new BlueLabelServiceStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static BlueLabelServiceStatsPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      BlueLabelServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::BlueLabelServiceStats", __ctx)) {
               BlueLabelServiceStatsPrxHelper __h = new BlueLabelServiceStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static BlueLabelServiceStatsPrx uncheckedCast(ObjectPrx __obj) {
      BlueLabelServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (BlueLabelServiceStatsPrx)__obj;
         } catch (ClassCastException var4) {
            BlueLabelServiceStatsPrxHelper __h = new BlueLabelServiceStatsPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (BlueLabelServiceStatsPrx)__d;
   }

   public static BlueLabelServiceStatsPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      BlueLabelServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         BlueLabelServiceStatsPrxHelper __h = new BlueLabelServiceStatsPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _BlueLabelServiceStatsDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _BlueLabelServiceStatsDelD();
   }

   public static void __write(BasicStream __os, BlueLabelServiceStatsPrx v) {
      __os.writeProxy(v);
   }

   public static BlueLabelServiceStatsPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         BlueLabelServiceStatsPrxHelper result = new BlueLabelServiceStatsPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
