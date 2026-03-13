package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class ProfileUpdatedUserEventIcePrxHelper extends ObjectPrxHelperBase implements ProfileUpdatedUserEventIcePrx {
   public static ProfileUpdatedUserEventIcePrx checkedCast(ObjectPrx __obj) {
      ProfileUpdatedUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (ProfileUpdatedUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::ProfileUpdatedUserEventIce")) {
               ProfileUpdatedUserEventIcePrxHelper __h = new ProfileUpdatedUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (ProfileUpdatedUserEventIcePrx)__d;
   }

   public static ProfileUpdatedUserEventIcePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      ProfileUpdatedUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (ProfileUpdatedUserEventIcePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::ProfileUpdatedUserEventIce", __ctx)) {
               ProfileUpdatedUserEventIcePrxHelper __h = new ProfileUpdatedUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (ProfileUpdatedUserEventIcePrx)__d;
   }

   public static ProfileUpdatedUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet) {
      ProfileUpdatedUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::ProfileUpdatedUserEventIce")) {
               ProfileUpdatedUserEventIcePrxHelper __h = new ProfileUpdatedUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static ProfileUpdatedUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      ProfileUpdatedUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::ProfileUpdatedUserEventIce", __ctx)) {
               ProfileUpdatedUserEventIcePrxHelper __h = new ProfileUpdatedUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static ProfileUpdatedUserEventIcePrx uncheckedCast(ObjectPrx __obj) {
      ProfileUpdatedUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (ProfileUpdatedUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            ProfileUpdatedUserEventIcePrxHelper __h = new ProfileUpdatedUserEventIcePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (ProfileUpdatedUserEventIcePrx)__d;
   }

   public static ProfileUpdatedUserEventIcePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      ProfileUpdatedUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         ProfileUpdatedUserEventIcePrxHelper __h = new ProfileUpdatedUserEventIcePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _ProfileUpdatedUserEventIceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _ProfileUpdatedUserEventIceDelD();
   }

   public static void __write(BasicStream __os, ProfileUpdatedUserEventIcePrx v) {
      __os.writeProxy(v);
   }

   public static ProfileUpdatedUserEventIcePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         ProfileUpdatedUserEventIcePrxHelper result = new ProfileUpdatedUserEventIcePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
