package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class CollectedDataIcePrxHelper extends ObjectPrxHelperBase implements CollectedDataIcePrx {
   public static CollectedDataIcePrx checkedCast(ObjectPrx __obj) {
      CollectedDataIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (CollectedDataIcePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::CollectedDataIce")) {
               CollectedDataIcePrxHelper __h = new CollectedDataIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (CollectedDataIcePrx)__d;
   }

   public static CollectedDataIcePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      CollectedDataIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (CollectedDataIcePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::CollectedDataIce", __ctx)) {
               CollectedDataIcePrxHelper __h = new CollectedDataIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (CollectedDataIcePrx)__d;
   }

   public static CollectedDataIcePrx checkedCast(ObjectPrx __obj, String __facet) {
      CollectedDataIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::CollectedDataIce")) {
               CollectedDataIcePrxHelper __h = new CollectedDataIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static CollectedDataIcePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      CollectedDataIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::CollectedDataIce", __ctx)) {
               CollectedDataIcePrxHelper __h = new CollectedDataIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static CollectedDataIcePrx uncheckedCast(ObjectPrx __obj) {
      CollectedDataIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (CollectedDataIcePrx)__obj;
         } catch (ClassCastException var4) {
            CollectedDataIcePrxHelper __h = new CollectedDataIcePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (CollectedDataIcePrx)__d;
   }

   public static CollectedDataIcePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      CollectedDataIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         CollectedDataIcePrxHelper __h = new CollectedDataIcePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _CollectedDataIceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _CollectedDataIceDelD();
   }

   public static void __write(BasicStream __os, CollectedDataIcePrx v) {
      __os.writeProxy(v);
   }

   public static CollectedDataIcePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         CollectedDataIcePrxHelper result = new CollectedDataIcePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
