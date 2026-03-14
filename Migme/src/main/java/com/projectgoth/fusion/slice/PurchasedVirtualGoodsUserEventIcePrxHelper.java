package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class PurchasedVirtualGoodsUserEventIcePrxHelper extends ObjectPrxHelperBase implements PurchasedVirtualGoodsUserEventIcePrx {
   public static PurchasedVirtualGoodsUserEventIcePrx checkedCast(ObjectPrx __obj) {
      PurchasedVirtualGoodsUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (PurchasedVirtualGoodsUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::PurchasedVirtualGoodsUserEventIce")) {
               PurchasedVirtualGoodsUserEventIcePrxHelper __h = new PurchasedVirtualGoodsUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (PurchasedVirtualGoodsUserEventIcePrx)__d;
   }

   public static PurchasedVirtualGoodsUserEventIcePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      PurchasedVirtualGoodsUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (PurchasedVirtualGoodsUserEventIcePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::PurchasedVirtualGoodsUserEventIce", __ctx)) {
               PurchasedVirtualGoodsUserEventIcePrxHelper __h = new PurchasedVirtualGoodsUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (PurchasedVirtualGoodsUserEventIcePrx)__d;
   }

   public static PurchasedVirtualGoodsUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet) {
      PurchasedVirtualGoodsUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::PurchasedVirtualGoodsUserEventIce")) {
               PurchasedVirtualGoodsUserEventIcePrxHelper __h = new PurchasedVirtualGoodsUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static PurchasedVirtualGoodsUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      PurchasedVirtualGoodsUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::PurchasedVirtualGoodsUserEventIce", __ctx)) {
               PurchasedVirtualGoodsUserEventIcePrxHelper __h = new PurchasedVirtualGoodsUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static PurchasedVirtualGoodsUserEventIcePrx uncheckedCast(ObjectPrx __obj) {
      PurchasedVirtualGoodsUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (PurchasedVirtualGoodsUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            PurchasedVirtualGoodsUserEventIcePrxHelper __h = new PurchasedVirtualGoodsUserEventIcePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (PurchasedVirtualGoodsUserEventIcePrx)__d;
   }

   public static PurchasedVirtualGoodsUserEventIcePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      PurchasedVirtualGoodsUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         PurchasedVirtualGoodsUserEventIcePrxHelper __h = new PurchasedVirtualGoodsUserEventIcePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _PurchasedVirtualGoodsUserEventIceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _PurchasedVirtualGoodsUserEventIceDelD();
   }

   public static void __write(BasicStream __os, PurchasedVirtualGoodsUserEventIcePrx v) {
      __os.writeProxy(v);
   }

   public static PurchasedVirtualGoodsUserEventIcePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         PurchasedVirtualGoodsUserEventIcePrxHelper result = new PurchasedVirtualGoodsUserEventIcePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
