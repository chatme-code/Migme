package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class AddingTwoFriendsUserEventIcePrxHelper extends ObjectPrxHelperBase implements AddingTwoFriendsUserEventIcePrx {
   public static AddingTwoFriendsUserEventIcePrx checkedCast(ObjectPrx __obj) {
      AddingTwoFriendsUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (AddingTwoFriendsUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::AddingTwoFriendsUserEventIce")) {
               AddingTwoFriendsUserEventIcePrxHelper __h = new AddingTwoFriendsUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (AddingTwoFriendsUserEventIcePrx)__d;
   }

   public static AddingTwoFriendsUserEventIcePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      AddingTwoFriendsUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (AddingTwoFriendsUserEventIcePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::AddingTwoFriendsUserEventIce", __ctx)) {
               AddingTwoFriendsUserEventIcePrxHelper __h = new AddingTwoFriendsUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (AddingTwoFriendsUserEventIcePrx)__d;
   }

   public static AddingTwoFriendsUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet) {
      AddingTwoFriendsUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::AddingTwoFriendsUserEventIce")) {
               AddingTwoFriendsUserEventIcePrxHelper __h = new AddingTwoFriendsUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static AddingTwoFriendsUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      AddingTwoFriendsUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::AddingTwoFriendsUserEventIce", __ctx)) {
               AddingTwoFriendsUserEventIcePrxHelper __h = new AddingTwoFriendsUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static AddingTwoFriendsUserEventIcePrx uncheckedCast(ObjectPrx __obj) {
      AddingTwoFriendsUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (AddingTwoFriendsUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            AddingTwoFriendsUserEventIcePrxHelper __h = new AddingTwoFriendsUserEventIcePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (AddingTwoFriendsUserEventIcePrx)__d;
   }

   public static AddingTwoFriendsUserEventIcePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      AddingTwoFriendsUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         AddingTwoFriendsUserEventIcePrxHelper __h = new AddingTwoFriendsUserEventIcePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _AddingTwoFriendsUserEventIceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _AddingTwoFriendsUserEventIceDelD();
   }

   public static void __write(BasicStream __os, AddingTwoFriendsUserEventIcePrx v) {
      __os.writeProxy(v);
   }

   public static AddingTwoFriendsUserEventIcePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         AddingTwoFriendsUserEventIcePrxHelper result = new AddingTwoFriendsUserEventIcePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
