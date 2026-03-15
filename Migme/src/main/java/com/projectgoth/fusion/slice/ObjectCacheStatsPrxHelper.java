package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class ObjectCacheStatsPrxHelper extends ObjectPrxHelperBase implements ObjectCacheStatsPrx {
   public static ObjectCacheStatsPrx checkedCast(ObjectPrx __obj) {
      ObjectCacheStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (ObjectCacheStatsPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::ObjectCacheStats")) {
               ObjectCacheStatsPrxHelper __h = new ObjectCacheStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (ObjectCacheStatsPrx)__d;
   }

   public static ObjectCacheStatsPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      ObjectCacheStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (ObjectCacheStatsPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::ObjectCacheStats", __ctx)) {
               ObjectCacheStatsPrxHelper __h = new ObjectCacheStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (ObjectCacheStatsPrx)__d;
   }

   public static ObjectCacheStatsPrx checkedCast(ObjectPrx __obj, String __facet) {
      ObjectCacheStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::ObjectCacheStats")) {
               ObjectCacheStatsPrxHelper __h = new ObjectCacheStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static ObjectCacheStatsPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      ObjectCacheStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::ObjectCacheStats", __ctx)) {
               ObjectCacheStatsPrxHelper __h = new ObjectCacheStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static ObjectCacheStatsPrx uncheckedCast(ObjectPrx __obj) {
      ObjectCacheStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (ObjectCacheStatsPrx)__obj;
         } catch (ClassCastException var4) {
            ObjectCacheStatsPrxHelper __h = new ObjectCacheStatsPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (ObjectCacheStatsPrx)__d;
   }

   public static ObjectCacheStatsPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      ObjectCacheStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         ObjectCacheStatsPrxHelper __h = new ObjectCacheStatsPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _ObjectCacheStatsDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _ObjectCacheStatsDelD();
   }

   public static void __write(BasicStream __os, ObjectCacheStatsPrx v) {
      __os.writeProxy(v);
   }

   public static ObjectCacheStatsPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         ObjectCacheStatsPrxHelper result = new ObjectCacheStatsPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
