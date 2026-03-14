package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.LocalException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDel;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import IceInternal.LocalExceptionWrapper;
import IceInternal.OutgoingAsync;
import java.util.Map;

public final class AuthenticationServiceAdminPrxHelper extends ObjectPrxHelperBase implements AuthenticationServiceAdminPrx {
   public AuthenticationServiceStats getStats() throws FusionException {
      return this.getStats((Map)null, false);
   }

   public AuthenticationServiceStats getStats(Map<String, String> __ctx) throws FusionException {
      return this.getStats(__ctx, true);
   }

   private AuthenticationServiceStats getStats(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getStats");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceAdminDel __del = (_AuthenticationServiceAdminDel)__delBase;
            return __del.getStats(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static AuthenticationServiceAdminPrx checkedCast(ObjectPrx __obj) {
      AuthenticationServiceAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (AuthenticationServiceAdminPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::AuthenticationServiceAdmin")) {
               AuthenticationServiceAdminPrxHelper __h = new AuthenticationServiceAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (AuthenticationServiceAdminPrx)__d;
   }

   public static AuthenticationServiceAdminPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      AuthenticationServiceAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (AuthenticationServiceAdminPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::AuthenticationServiceAdmin", __ctx)) {
               AuthenticationServiceAdminPrxHelper __h = new AuthenticationServiceAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (AuthenticationServiceAdminPrx)__d;
   }

   public static AuthenticationServiceAdminPrx checkedCast(ObjectPrx __obj, String __facet) {
      AuthenticationServiceAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::AuthenticationServiceAdmin")) {
               AuthenticationServiceAdminPrxHelper __h = new AuthenticationServiceAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static AuthenticationServiceAdminPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      AuthenticationServiceAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::AuthenticationServiceAdmin", __ctx)) {
               AuthenticationServiceAdminPrxHelper __h = new AuthenticationServiceAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static AuthenticationServiceAdminPrx uncheckedCast(ObjectPrx __obj) {
      AuthenticationServiceAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (AuthenticationServiceAdminPrx)__obj;
         } catch (ClassCastException var4) {
            AuthenticationServiceAdminPrxHelper __h = new AuthenticationServiceAdminPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (AuthenticationServiceAdminPrx)__d;
   }

   public static AuthenticationServiceAdminPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      AuthenticationServiceAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         AuthenticationServiceAdminPrxHelper __h = new AuthenticationServiceAdminPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _AuthenticationServiceAdminDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _AuthenticationServiceAdminDelD();
   }

   public static void __write(BasicStream __os, AuthenticationServiceAdminPrx v) {
      __os.writeProxy(v);
   }

   public static AuthenticationServiceAdminPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         AuthenticationServiceAdminPrxHelper result = new AuthenticationServiceAdminPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
