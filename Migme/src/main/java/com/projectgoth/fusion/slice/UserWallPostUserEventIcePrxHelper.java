package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class UserWallPostUserEventIcePrxHelper extends ObjectPrxHelperBase implements UserWallPostUserEventIcePrx {
   public static UserWallPostUserEventIcePrx checkedCast(ObjectPrx __obj) {
      UserWallPostUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (UserWallPostUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::UserWallPostUserEventIce")) {
               UserWallPostUserEventIcePrxHelper __h = new UserWallPostUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (UserWallPostUserEventIcePrx)__d;
   }

   public static UserWallPostUserEventIcePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      UserWallPostUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (UserWallPostUserEventIcePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::UserWallPostUserEventIce", __ctx)) {
               UserWallPostUserEventIcePrxHelper __h = new UserWallPostUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (UserWallPostUserEventIcePrx)__d;
   }

   public static UserWallPostUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet) {
      UserWallPostUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::UserWallPostUserEventIce")) {
               UserWallPostUserEventIcePrxHelper __h = new UserWallPostUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static UserWallPostUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      UserWallPostUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::UserWallPostUserEventIce", __ctx)) {
               UserWallPostUserEventIcePrxHelper __h = new UserWallPostUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static UserWallPostUserEventIcePrx uncheckedCast(ObjectPrx __obj) {
      UserWallPostUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (UserWallPostUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            UserWallPostUserEventIcePrxHelper __h = new UserWallPostUserEventIcePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (UserWallPostUserEventIcePrx)__d;
   }

   public static UserWallPostUserEventIcePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      UserWallPostUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         UserWallPostUserEventIcePrxHelper __h = new UserWallPostUserEventIcePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _UserWallPostUserEventIceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _UserWallPostUserEventIceDelD();
   }

   public static void __write(BasicStream __os, UserWallPostUserEventIcePrx v) {
      __os.writeProxy(v);
   }

   public static UserWallPostUserEventIcePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         UserWallPostUserEventIcePrxHelper result = new UserWallPostUserEventIcePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
