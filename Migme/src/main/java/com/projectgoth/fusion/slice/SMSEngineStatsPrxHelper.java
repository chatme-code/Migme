package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class SMSEngineStatsPrxHelper extends ObjectPrxHelperBase implements SMSEngineStatsPrx {
   public static SMSEngineStatsPrx checkedCast(ObjectPrx __obj) {
      SMSEngineStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (SMSEngineStatsPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::SMSEngineStats")) {
               SMSEngineStatsPrxHelper __h = new SMSEngineStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (SMSEngineStatsPrx)__d;
   }

   public static SMSEngineStatsPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      SMSEngineStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (SMSEngineStatsPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::SMSEngineStats", __ctx)) {
               SMSEngineStatsPrxHelper __h = new SMSEngineStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (SMSEngineStatsPrx)__d;
   }

   public static SMSEngineStatsPrx checkedCast(ObjectPrx __obj, String __facet) {
      SMSEngineStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::SMSEngineStats")) {
               SMSEngineStatsPrxHelper __h = new SMSEngineStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static SMSEngineStatsPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      SMSEngineStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::SMSEngineStats", __ctx)) {
               SMSEngineStatsPrxHelper __h = new SMSEngineStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static SMSEngineStatsPrx uncheckedCast(ObjectPrx __obj) {
      SMSEngineStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (SMSEngineStatsPrx)__obj;
         } catch (ClassCastException var4) {
            SMSEngineStatsPrxHelper __h = new SMSEngineStatsPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (SMSEngineStatsPrx)__d;
   }

   public static SMSEngineStatsPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      SMSEngineStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         SMSEngineStatsPrxHelper __h = new SMSEngineStatsPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _SMSEngineStatsDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _SMSEngineStatsDelD();
   }

   public static void __write(BasicStream __os, SMSEngineStatsPrx v) {
      __os.writeProxy(v);
   }

   public static SMSEngineStatsPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         SMSEngineStatsPrxHelper result = new SMSEngineStatsPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
