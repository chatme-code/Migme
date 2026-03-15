package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class AuthenticationServiceStatsPrxHelper extends ObjectPrxHelperBase implements AuthenticationServiceStatsPrx {
   public static AuthenticationServiceStatsPrx checkedCast(ObjectPrx __obj) {
      AuthenticationServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (AuthenticationServiceStatsPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::AuthenticationServiceStats")) {
               AuthenticationServiceStatsPrxHelper __h = new AuthenticationServiceStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (AuthenticationServiceStatsPrx)__d;
   }

   public static AuthenticationServiceStatsPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      AuthenticationServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (AuthenticationServiceStatsPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::AuthenticationServiceStats", __ctx)) {
               AuthenticationServiceStatsPrxHelper __h = new AuthenticationServiceStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (AuthenticationServiceStatsPrx)__d;
   }

   public static AuthenticationServiceStatsPrx checkedCast(ObjectPrx __obj, String __facet) {
      AuthenticationServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::AuthenticationServiceStats")) {
               AuthenticationServiceStatsPrxHelper __h = new AuthenticationServiceStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static AuthenticationServiceStatsPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      AuthenticationServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::AuthenticationServiceStats", __ctx)) {
               AuthenticationServiceStatsPrxHelper __h = new AuthenticationServiceStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static AuthenticationServiceStatsPrx uncheckedCast(ObjectPrx __obj) {
      AuthenticationServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (AuthenticationServiceStatsPrx)__obj;
         } catch (ClassCastException var4) {
            AuthenticationServiceStatsPrxHelper __h = new AuthenticationServiceStatsPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (AuthenticationServiceStatsPrx)__d;
   }

   public static AuthenticationServiceStatsPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      AuthenticationServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         AuthenticationServiceStatsPrxHelper __h = new AuthenticationServiceStatsPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _AuthenticationServiceStatsDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _AuthenticationServiceStatsDelD();
   }

   public static void __write(BasicStream __os, AuthenticationServiceStatsPrx v) {
      __os.writeProxy(v);
   }

   public static AuthenticationServiceStatsPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         AuthenticationServiceStatsPrxHelper result = new AuthenticationServiceStatsPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
