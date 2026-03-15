package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class GiftShowerUserEventIcePrxHelper extends ObjectPrxHelperBase implements GiftShowerUserEventIcePrx {
   public static GiftShowerUserEventIcePrx checkedCast(ObjectPrx __obj) {
      GiftShowerUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (GiftShowerUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::GiftShowerUserEventIce")) {
               GiftShowerUserEventIcePrxHelper __h = new GiftShowerUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (GiftShowerUserEventIcePrx)__d;
   }

   public static GiftShowerUserEventIcePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      GiftShowerUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (GiftShowerUserEventIcePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::GiftShowerUserEventIce", __ctx)) {
               GiftShowerUserEventIcePrxHelper __h = new GiftShowerUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (GiftShowerUserEventIcePrx)__d;
   }

   public static GiftShowerUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet) {
      GiftShowerUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::GiftShowerUserEventIce")) {
               GiftShowerUserEventIcePrxHelper __h = new GiftShowerUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static GiftShowerUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      GiftShowerUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::GiftShowerUserEventIce", __ctx)) {
               GiftShowerUserEventIcePrxHelper __h = new GiftShowerUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static GiftShowerUserEventIcePrx uncheckedCast(ObjectPrx __obj) {
      GiftShowerUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (GiftShowerUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            GiftShowerUserEventIcePrxHelper __h = new GiftShowerUserEventIcePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (GiftShowerUserEventIcePrx)__d;
   }

   public static GiftShowerUserEventIcePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      GiftShowerUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         GiftShowerUserEventIcePrxHelper __h = new GiftShowerUserEventIcePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _GiftShowerUserEventIceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _GiftShowerUserEventIceDelD();
   }

   public static void __write(BasicStream __os, GiftShowerUserEventIcePrx v) {
      __os.writeProxy(v);
   }

   public static GiftShowerUserEventIcePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         GiftShowerUserEventIcePrxHelper result = new GiftShowerUserEventIcePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
