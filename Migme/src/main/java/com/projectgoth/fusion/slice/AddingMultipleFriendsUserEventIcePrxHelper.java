package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class AddingMultipleFriendsUserEventIcePrxHelper extends ObjectPrxHelperBase implements AddingMultipleFriendsUserEventIcePrx {
   public static AddingMultipleFriendsUserEventIcePrx checkedCast(ObjectPrx __obj) {
      AddingMultipleFriendsUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (AddingMultipleFriendsUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::AddingMultipleFriendsUserEventIce")) {
               AddingMultipleFriendsUserEventIcePrxHelper __h = new AddingMultipleFriendsUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (AddingMultipleFriendsUserEventIcePrx)__d;
   }

   public static AddingMultipleFriendsUserEventIcePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      AddingMultipleFriendsUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (AddingMultipleFriendsUserEventIcePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::AddingMultipleFriendsUserEventIce", __ctx)) {
               AddingMultipleFriendsUserEventIcePrxHelper __h = new AddingMultipleFriendsUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (AddingMultipleFriendsUserEventIcePrx)__d;
   }

   public static AddingMultipleFriendsUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet) {
      AddingMultipleFriendsUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::AddingMultipleFriendsUserEventIce")) {
               AddingMultipleFriendsUserEventIcePrxHelper __h = new AddingMultipleFriendsUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static AddingMultipleFriendsUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      AddingMultipleFriendsUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::AddingMultipleFriendsUserEventIce", __ctx)) {
               AddingMultipleFriendsUserEventIcePrxHelper __h = new AddingMultipleFriendsUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static AddingMultipleFriendsUserEventIcePrx uncheckedCast(ObjectPrx __obj) {
      AddingMultipleFriendsUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (AddingMultipleFriendsUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            AddingMultipleFriendsUserEventIcePrxHelper __h = new AddingMultipleFriendsUserEventIcePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (AddingMultipleFriendsUserEventIcePrx)__d;
   }

   public static AddingMultipleFriendsUserEventIcePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      AddingMultipleFriendsUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         AddingMultipleFriendsUserEventIcePrxHelper __h = new AddingMultipleFriendsUserEventIcePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _AddingMultipleFriendsUserEventIceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _AddingMultipleFriendsUserEventIceDelD();
   }

   public static void __write(BasicStream __os, AddingMultipleFriendsUserEventIcePrx v) {
      __os.writeProxy(v);
   }

   public static AddingMultipleFriendsUserEventIcePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         AddingMultipleFriendsUserEventIcePrxHelper result = new AddingMultipleFriendsUserEventIcePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
