package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class ShortTextStatusUserEventIcePrxHelper extends ObjectPrxHelperBase implements ShortTextStatusUserEventIcePrx {
   public static ShortTextStatusUserEventIcePrx checkedCast(ObjectPrx __obj) {
      ShortTextStatusUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (ShortTextStatusUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::ShortTextStatusUserEventIce")) {
               ShortTextStatusUserEventIcePrxHelper __h = new ShortTextStatusUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (ShortTextStatusUserEventIcePrx)__d;
   }

   public static ShortTextStatusUserEventIcePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      ShortTextStatusUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (ShortTextStatusUserEventIcePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::ShortTextStatusUserEventIce", __ctx)) {
               ShortTextStatusUserEventIcePrxHelper __h = new ShortTextStatusUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (ShortTextStatusUserEventIcePrx)__d;
   }

   public static ShortTextStatusUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet) {
      ShortTextStatusUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::ShortTextStatusUserEventIce")) {
               ShortTextStatusUserEventIcePrxHelper __h = new ShortTextStatusUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static ShortTextStatusUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      ShortTextStatusUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::ShortTextStatusUserEventIce", __ctx)) {
               ShortTextStatusUserEventIcePrxHelper __h = new ShortTextStatusUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static ShortTextStatusUserEventIcePrx uncheckedCast(ObjectPrx __obj) {
      ShortTextStatusUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (ShortTextStatusUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            ShortTextStatusUserEventIcePrxHelper __h = new ShortTextStatusUserEventIcePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (ShortTextStatusUserEventIcePrx)__d;
   }

   public static ShortTextStatusUserEventIcePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      ShortTextStatusUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         ShortTextStatusUserEventIcePrxHelper __h = new ShortTextStatusUserEventIcePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _ShortTextStatusUserEventIceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _ShortTextStatusUserEventIceDelD();
   }

   public static void __write(BasicStream __os, ShortTextStatusUserEventIcePrx v) {
      __os.writeProxy(v);
   }

   public static ShortTextStatusUserEventIcePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         ShortTextStatusUserEventIcePrxHelper result = new ShortTextStatusUserEventIcePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
