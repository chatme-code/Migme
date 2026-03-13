package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class UserNotificationServiceStatsPrxHelper extends ObjectPrxHelperBase implements UserNotificationServiceStatsPrx {
   public static UserNotificationServiceStatsPrx checkedCast(ObjectPrx __obj) {
      UserNotificationServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (UserNotificationServiceStatsPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::UserNotificationServiceStats")) {
               UserNotificationServiceStatsPrxHelper __h = new UserNotificationServiceStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (UserNotificationServiceStatsPrx)__d;
   }

   public static UserNotificationServiceStatsPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      UserNotificationServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (UserNotificationServiceStatsPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::UserNotificationServiceStats", __ctx)) {
               UserNotificationServiceStatsPrxHelper __h = new UserNotificationServiceStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (UserNotificationServiceStatsPrx)__d;
   }

   public static UserNotificationServiceStatsPrx checkedCast(ObjectPrx __obj, String __facet) {
      UserNotificationServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::UserNotificationServiceStats")) {
               UserNotificationServiceStatsPrxHelper __h = new UserNotificationServiceStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static UserNotificationServiceStatsPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      UserNotificationServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::UserNotificationServiceStats", __ctx)) {
               UserNotificationServiceStatsPrxHelper __h = new UserNotificationServiceStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static UserNotificationServiceStatsPrx uncheckedCast(ObjectPrx __obj) {
      UserNotificationServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (UserNotificationServiceStatsPrx)__obj;
         } catch (ClassCastException var4) {
            UserNotificationServiceStatsPrxHelper __h = new UserNotificationServiceStatsPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (UserNotificationServiceStatsPrx)__d;
   }

   public static UserNotificationServiceStatsPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      UserNotificationServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         UserNotificationServiceStatsPrxHelper __h = new UserNotificationServiceStatsPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _UserNotificationServiceStatsDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _UserNotificationServiceStatsDelD();
   }

   public static void __write(BasicStream __os, UserNotificationServiceStatsPrx v) {
      __os.writeProxy(v);
   }

   public static UserNotificationServiceStatsPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         UserNotificationServiceStatsPrxHelper result = new UserNotificationServiceStatsPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
