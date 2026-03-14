package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class RegistryStatsPrxHelper extends ObjectPrxHelperBase implements RegistryStatsPrx {
   public static RegistryStatsPrx checkedCast(ObjectPrx __obj) {
      RegistryStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (RegistryStatsPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::RegistryStats")) {
               RegistryStatsPrxHelper __h = new RegistryStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (RegistryStatsPrx)__d;
   }

   public static RegistryStatsPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      RegistryStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (RegistryStatsPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::RegistryStats", __ctx)) {
               RegistryStatsPrxHelper __h = new RegistryStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (RegistryStatsPrx)__d;
   }

   public static RegistryStatsPrx checkedCast(ObjectPrx __obj, String __facet) {
      RegistryStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::RegistryStats")) {
               RegistryStatsPrxHelper __h = new RegistryStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static RegistryStatsPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      RegistryStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::RegistryStats", __ctx)) {
               RegistryStatsPrxHelper __h = new RegistryStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static RegistryStatsPrx uncheckedCast(ObjectPrx __obj) {
      RegistryStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (RegistryStatsPrx)__obj;
         } catch (ClassCastException var4) {
            RegistryStatsPrxHelper __h = new RegistryStatsPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (RegistryStatsPrx)__d;
   }

   public static RegistryStatsPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      RegistryStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         RegistryStatsPrxHelper __h = new RegistryStatsPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _RegistryStatsDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _RegistryStatsDelD();
   }

   public static void __write(BasicStream __os, RegistryStatsPrx v) {
      __os.writeProxy(v);
   }

   public static RegistryStatsPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         RegistryStatsPrxHelper result = new RegistryStatsPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
