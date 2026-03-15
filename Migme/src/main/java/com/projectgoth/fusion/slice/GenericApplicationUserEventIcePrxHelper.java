package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class GenericApplicationUserEventIcePrxHelper extends ObjectPrxHelperBase implements GenericApplicationUserEventIcePrx {
   public static GenericApplicationUserEventIcePrx checkedCast(ObjectPrx __obj) {
      GenericApplicationUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (GenericApplicationUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::GenericApplicationUserEventIce")) {
               GenericApplicationUserEventIcePrxHelper __h = new GenericApplicationUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (GenericApplicationUserEventIcePrx)__d;
   }

   public static GenericApplicationUserEventIcePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      GenericApplicationUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (GenericApplicationUserEventIcePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::GenericApplicationUserEventIce", __ctx)) {
               GenericApplicationUserEventIcePrxHelper __h = new GenericApplicationUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (GenericApplicationUserEventIcePrx)__d;
   }

   public static GenericApplicationUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet) {
      GenericApplicationUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::GenericApplicationUserEventIce")) {
               GenericApplicationUserEventIcePrxHelper __h = new GenericApplicationUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static GenericApplicationUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      GenericApplicationUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::GenericApplicationUserEventIce", __ctx)) {
               GenericApplicationUserEventIcePrxHelper __h = new GenericApplicationUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static GenericApplicationUserEventIcePrx uncheckedCast(ObjectPrx __obj) {
      GenericApplicationUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (GenericApplicationUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            GenericApplicationUserEventIcePrxHelper __h = new GenericApplicationUserEventIcePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (GenericApplicationUserEventIcePrx)__d;
   }

   public static GenericApplicationUserEventIcePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      GenericApplicationUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         GenericApplicationUserEventIcePrxHelper __h = new GenericApplicationUserEventIcePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _GenericApplicationUserEventIceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _GenericApplicationUserEventIceDelD();
   }

   public static void __write(BasicStream __os, GenericApplicationUserEventIcePrx v) {
      __os.writeProxy(v);
   }

   public static GenericApplicationUserEventIcePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         GenericApplicationUserEventIcePrxHelper result = new GenericApplicationUserEventIcePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
