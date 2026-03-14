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

public final class AuthenticationServicePrxHelper extends ObjectPrxHelperBase implements AuthenticationServicePrx {
   public AuthenticationServiceResponseCodeEnum authenticate(Credential userCredential, String clientIP) throws FusionException {
      return this.authenticate(userCredential, clientIP, (Map)null, false);
   }

   public AuthenticationServiceResponseCodeEnum authenticate(Credential userCredential, String clientIP, Map<String, String> __ctx) throws FusionException {
      return this.authenticate(userCredential, clientIP, __ctx, true);
   }

   private AuthenticationServiceResponseCodeEnum authenticate(Credential userCredential, String clientIP, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("authenticate");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            return __del.authenticate(userCredential, clientIP, __ctx);
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public byte[] availableCredentialTypes(int userid) throws FusionException {
      return this.availableCredentialTypes(userid, (Map)null, false);
   }

   public byte[] availableCredentialTypes(int userid, Map<String, String> __ctx) throws FusionException {
      return this.availableCredentialTypes(userid, __ctx, true);
   }

   private byte[] availableCredentialTypes(int userid, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("availableCredentialTypes");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            return __del.availableCredentialTypes(userid, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public AuthenticationServiceResponseCodeEnum checkCredential(Credential userCredential) {
      return this.checkCredential(userCredential, (Map)null, false);
   }

   public AuthenticationServiceResponseCodeEnum checkCredential(Credential userCredential, Map<String, String> __ctx) {
      return this.checkCredential(userCredential, __ctx, true);
   }

   private AuthenticationServiceResponseCodeEnum checkCredential(Credential userCredential, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("checkCredential");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            return __del.checkCredential(userCredential, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public AuthenticationServiceResponseCodeEnum checkCredentialByUserId(int userid, String password, byte passwordType) {
      return this.checkCredentialByUserId(userid, password, passwordType, (Map)null, false);
   }

   public AuthenticationServiceResponseCodeEnum checkCredentialByUserId(int userid, String password, byte passwordType, Map<String, String> __ctx) {
      return this.checkCredentialByUserId(userid, password, passwordType, __ctx, true);
   }

   private AuthenticationServiceResponseCodeEnum checkCredentialByUserId(int userid, String password, byte passwordType, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("checkCredentialByUserId");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            return __del.checkCredentialByUserId(userid, password, passwordType, __ctx);
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public AuthenticationServiceResponseCodeEnum checkCredentialByUsername(String username, String password, byte passwordType) {
      return this.checkCredentialByUsername(username, password, passwordType, (Map)null, false);
   }

   public AuthenticationServiceResponseCodeEnum checkCredentialByUsername(String username, String password, byte passwordType, Map<String, String> __ctx) {
      return this.checkCredentialByUsername(username, password, passwordType, __ctx, true);
   }

   private AuthenticationServiceResponseCodeEnum checkCredentialByUsername(String username, String password, byte passwordType, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("checkCredentialByUsername");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            return __del.checkCredentialByUsername(username, password, passwordType, __ctx);
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public AuthenticationServiceResponseCodeEnum createCredential(Credential userCredential) throws FusionException {
      return this.createCredential(userCredential, (Map)null, false);
   }

   public AuthenticationServiceResponseCodeEnum createCredential(Credential userCredential, Map<String, String> __ctx) throws FusionException {
      return this.createCredential(userCredential, __ctx, true);
   }

   private AuthenticationServiceResponseCodeEnum createCredential(Credential userCredential, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("createCredential");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            return __del.createCredential(userCredential, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public AuthenticationServiceResponseCodeEnum exists(int userid, byte passwordType) throws FusionException {
      return this.exists(userid, passwordType, (Map)null, false);
   }

   public AuthenticationServiceResponseCodeEnum exists(int userid, byte passwordType, Map<String, String> __ctx) throws FusionException {
      return this.exists(userid, passwordType, __ctx, true);
   }

   private AuthenticationServiceResponseCodeEnum exists(int userid, byte passwordType, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("exists");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            return __del.exists(userid, passwordType, __ctx);
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public Credential[] getAllCredentials(int userid) throws FusionException {
      return this.getAllCredentials(userid, (Map)null, false);
   }

   public Credential[] getAllCredentials(int userid, Map<String, String> __ctx) throws FusionException {
      return this.getAllCredentials(userid, __ctx, true);
   }

   private Credential[] getAllCredentials(int userid, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getAllCredentials");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            return __del.getAllCredentials(userid, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public Credential[] getAllCredentialsFromOldSource(int userid) throws FusionException {
      return this.getAllCredentialsFromOldSource(userid, (Map)null, false);
   }

   public Credential[] getAllCredentialsFromOldSource(int userid, Map<String, String> __ctx) throws FusionException {
      return this.getAllCredentialsFromOldSource(userid, __ctx, true);
   }

   private Credential[] getAllCredentialsFromOldSource(int userid, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getAllCredentialsFromOldSource");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            return __del.getAllCredentialsFromOldSource(userid, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public AuthenticationServiceCredentialResponse getCredential(int userid, byte passwordType) throws FusionException {
      return this.getCredential(userid, passwordType, (Map)null, false);
   }

   public AuthenticationServiceCredentialResponse getCredential(int userid, byte passwordType, Map<String, String> __ctx) throws FusionException {
      return this.getCredential(userid, passwordType, __ctx, true);
   }

   private AuthenticationServiceCredentialResponse getCredential(int userid, byte passwordType, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getCredential");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            return __del.getCredential(userid, passwordType, __ctx);
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public Credential[] getCredentialsForTypes(int userid, byte[] passwordTypes) throws FusionException {
      return this.getCredentialsForTypes(userid, passwordTypes, (Map)null, false);
   }

   public Credential[] getCredentialsForTypes(int userid, byte[] passwordTypes, Map<String, String> __ctx) throws FusionException {
      return this.getCredentialsForTypes(userid, passwordTypes, __ctx, true);
   }

   private Credential[] getCredentialsForTypes(int userid, byte[] passwordTypes, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getCredentialsForTypes");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            return __del.getCredentialsForTypes(userid, passwordTypes, __ctx);
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public AuthenticationServiceCredentialResponse getLatestCredentialByUsernameAndPasswordType(String username, byte passwordType) {
      return this.getLatestCredentialByUsernameAndPasswordType(username, passwordType, (Map)null, false);
   }

   public AuthenticationServiceCredentialResponse getLatestCredentialByUsernameAndPasswordType(String username, byte passwordType, Map<String, String> __ctx) {
      return this.getLatestCredentialByUsernameAndPasswordType(username, passwordType, __ctx, true);
   }

   private AuthenticationServiceCredentialResponse getLatestCredentialByUsernameAndPasswordType(String username, byte passwordType, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getLatestCredentialByUsernameAndPasswordType");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            return __del.getLatestCredentialByUsernameAndPasswordType(username, passwordType, __ctx);
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void migrateUserCredentials(int userid) throws FusionException {
      this.migrateUserCredentials(userid, (Map)null, false);
   }

   public void migrateUserCredentials(int userid, Map<String, String> __ctx) throws FusionException {
      this.migrateUserCredentials(userid, __ctx, true);
   }

   private void migrateUserCredentials(int userid, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("migrateUserCredentials");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            __del.migrateUserCredentials(userid, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public AuthenticationServiceResponseCodeEnum removeCredential(Credential userCredential) throws FusionException {
      return this.removeCredential(userCredential, (Map)null, false);
   }

   public AuthenticationServiceResponseCodeEnum removeCredential(Credential userCredential, Map<String, String> __ctx) throws FusionException {
      return this.removeCredential(userCredential, __ctx, true);
   }

   private AuthenticationServiceResponseCodeEnum removeCredential(Credential userCredential, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("removeCredential");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            return __del.removeCredential(userCredential, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public AuthenticationServiceResponseCodeEnum updateCredential(Credential userCredential) throws FusionException {
      return this.updateCredential(userCredential, (Map)null, false);
   }

   public AuthenticationServiceResponseCodeEnum updateCredential(Credential userCredential, Map<String, String> __ctx) throws FusionException {
      return this.updateCredential(userCredential, __ctx, true);
   }

   private AuthenticationServiceResponseCodeEnum updateCredential(Credential userCredential, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("updateCredential");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            return __del.updateCredential(userCredential, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public AuthenticationServiceResponseCodeEnum updateFusionCredential(Credential userCredential, String oldPassword) throws FusionException {
      return this.updateFusionCredential(userCredential, oldPassword, (Map)null, false);
   }

   public AuthenticationServiceResponseCodeEnum updateFusionCredential(Credential userCredential, String oldPassword, Map<String, String> __ctx) throws FusionException {
      return this.updateFusionCredential(userCredential, oldPassword, __ctx, true);
   }

   private AuthenticationServiceResponseCodeEnum updateFusionCredential(Credential userCredential, String oldPassword, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("updateFusionCredential");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            return __del.updateFusionCredential(userCredential, oldPassword, __ctx);
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public int userIDForFusionUsername(String username) throws FusionException {
      return this.userIDForFusionUsername(username, (Map)null, false);
   }

   public int userIDForFusionUsername(String username, Map<String, String> __ctx) throws FusionException {
      return this.userIDForFusionUsername(username, __ctx, true);
   }

   private int userIDForFusionUsername(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("userIDForFusionUsername");
            __delBase = this.__getDelegate(false);
            _AuthenticationServiceDel __del = (_AuthenticationServiceDel)__delBase;
            return __del.userIDForFusionUsername(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static AuthenticationServicePrx checkedCast(ObjectPrx __obj) {
      AuthenticationServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (AuthenticationServicePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::AuthenticationService")) {
               AuthenticationServicePrxHelper __h = new AuthenticationServicePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (AuthenticationServicePrx)__d;
   }

   public static AuthenticationServicePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      AuthenticationServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (AuthenticationServicePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::AuthenticationService", __ctx)) {
               AuthenticationServicePrxHelper __h = new AuthenticationServicePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (AuthenticationServicePrx)__d;
   }

   public static AuthenticationServicePrx checkedCast(ObjectPrx __obj, String __facet) {
      AuthenticationServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::AuthenticationService")) {
               AuthenticationServicePrxHelper __h = new AuthenticationServicePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static AuthenticationServicePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      AuthenticationServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::AuthenticationService", __ctx)) {
               AuthenticationServicePrxHelper __h = new AuthenticationServicePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static AuthenticationServicePrx uncheckedCast(ObjectPrx __obj) {
      AuthenticationServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (AuthenticationServicePrx)__obj;
         } catch (ClassCastException var4) {
            AuthenticationServicePrxHelper __h = new AuthenticationServicePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (AuthenticationServicePrx)__d;
   }

   public static AuthenticationServicePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      AuthenticationServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         AuthenticationServicePrxHelper __h = new AuthenticationServicePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _AuthenticationServiceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _AuthenticationServiceDelD();
   }

   public static void __write(BasicStream __os, AuthenticationServicePrx v) {
      __os.writeProxy(v);
   }

   public static AuthenticationServicePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         AuthenticationServicePrxHelper result = new AuthenticationServicePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
