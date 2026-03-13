package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class VirtualGiftUserEventIcePrxHelper extends ObjectPrxHelperBase implements VirtualGiftUserEventIcePrx {
   public static VirtualGiftUserEventIcePrx checkedCast(ObjectPrx __obj) {
      VirtualGiftUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (VirtualGiftUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::VirtualGiftUserEventIce")) {
               VirtualGiftUserEventIcePrxHelper __h = new VirtualGiftUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (VirtualGiftUserEventIcePrx)__d;
   }

   public static VirtualGiftUserEventIcePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      VirtualGiftUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (VirtualGiftUserEventIcePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::VirtualGiftUserEventIce", __ctx)) {
               VirtualGiftUserEventIcePrxHelper __h = new VirtualGiftUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (VirtualGiftUserEventIcePrx)__d;
   }

   public static VirtualGiftUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet) {
      VirtualGiftUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::VirtualGiftUserEventIce")) {
               VirtualGiftUserEventIcePrxHelper __h = new VirtualGiftUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static VirtualGiftUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      VirtualGiftUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::VirtualGiftUserEventIce", __ctx)) {
               VirtualGiftUserEventIcePrxHelper __h = new VirtualGiftUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static VirtualGiftUserEventIcePrx uncheckedCast(ObjectPrx __obj) {
      VirtualGiftUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (VirtualGiftUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            VirtualGiftUserEventIcePrxHelper __h = new VirtualGiftUserEventIcePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (VirtualGiftUserEventIcePrx)__d;
   }

   public static VirtualGiftUserEventIcePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      VirtualGiftUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         VirtualGiftUserEventIcePrxHelper __h = new VirtualGiftUserEventIcePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _VirtualGiftUserEventIceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _VirtualGiftUserEventIceDelD();
   }

   public static void __write(BasicStream __os, VirtualGiftUserEventIcePrx v) {
      __os.writeProxy(v);
   }

   public static VirtualGiftUserEventIcePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         VirtualGiftUserEventIcePrxHelper result = new VirtualGiftUserEventIcePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
