package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class UserEventIcePrxHelper extends ObjectPrxHelperBase implements UserEventIcePrx {
   public static UserEventIcePrx checkedCast(ObjectPrx __obj) {
      UserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (UserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::UserEventIce")) {
               UserEventIcePrxHelper __h = new UserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (UserEventIcePrx)__d;
   }

   public static UserEventIcePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      UserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (UserEventIcePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::UserEventIce", __ctx)) {
               UserEventIcePrxHelper __h = new UserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (UserEventIcePrx)__d;
   }

   public static UserEventIcePrx checkedCast(ObjectPrx __obj, String __facet) {
      UserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::UserEventIce")) {
               UserEventIcePrxHelper __h = new UserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static UserEventIcePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      UserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::UserEventIce", __ctx)) {
               UserEventIcePrxHelper __h = new UserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static UserEventIcePrx uncheckedCast(ObjectPrx __obj) {
      UserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (UserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            UserEventIcePrxHelper __h = new UserEventIcePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (UserEventIcePrx)__d;
   }

   public static UserEventIcePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      UserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         UserEventIcePrxHelper __h = new UserEventIcePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _UserEventIceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _UserEventIceDelD();
   }

   public static void __write(BasicStream __os, UserEventIcePrx v) {
      __os.writeProxy(v);
   }

   public static UserEventIcePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         UserEventIcePrxHelper result = new UserEventIcePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
