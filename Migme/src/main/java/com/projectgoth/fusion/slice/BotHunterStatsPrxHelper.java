package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class BotHunterStatsPrxHelper extends ObjectPrxHelperBase implements BotHunterStatsPrx {
   public static BotHunterStatsPrx checkedCast(ObjectPrx __obj) {
      BotHunterStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (BotHunterStatsPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::BotHunterStats")) {
               BotHunterStatsPrxHelper __h = new BotHunterStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (BotHunterStatsPrx)__d;
   }

   public static BotHunterStatsPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      BotHunterStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (BotHunterStatsPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::BotHunterStats", __ctx)) {
               BotHunterStatsPrxHelper __h = new BotHunterStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (BotHunterStatsPrx)__d;
   }

   public static BotHunterStatsPrx checkedCast(ObjectPrx __obj, String __facet) {
      BotHunterStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::BotHunterStats")) {
               BotHunterStatsPrxHelper __h = new BotHunterStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static BotHunterStatsPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      BotHunterStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::BotHunterStats", __ctx)) {
               BotHunterStatsPrxHelper __h = new BotHunterStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static BotHunterStatsPrx uncheckedCast(ObjectPrx __obj) {
      BotHunterStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (BotHunterStatsPrx)__obj;
         } catch (ClassCastException var4) {
            BotHunterStatsPrxHelper __h = new BotHunterStatsPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (BotHunterStatsPrx)__d;
   }

   public static BotHunterStatsPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      BotHunterStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         BotHunterStatsPrxHelper __h = new BotHunterStatsPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _BotHunterStatsDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _BotHunterStatsDelD();
   }

   public static void __write(BasicStream __os, BotHunterStatsPrx v) {
      __os.writeProxy(v);
   }

   public static BotHunterStatsPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         BotHunterStatsPrxHelper result = new BotHunterStatsPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
