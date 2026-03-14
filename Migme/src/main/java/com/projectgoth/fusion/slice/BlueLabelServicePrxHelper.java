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

public final class BlueLabelServicePrxHelper extends ObjectPrxHelperBase implements BlueLabelServicePrx {
   public WebServiceResponse authenticate(String username) throws FusionException {
      return this.authenticate(username, (Map)null, false);
   }

   public WebServiceResponse authenticate(String username, Map<String, String> __ctx) throws FusionException {
      return this.authenticate(username, __ctx, true);
   }

   private WebServiceResponse authenticate(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("authenticate");
            __delBase = this.__getDelegate(false);
            _BlueLabelServiceDel __del = (_BlueLabelServiceDel)__delBase;
            return __del.authenticate(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public WebServiceResponse fullVoucherRedemption(String migUsername, String userTicket, BlueLabelOneVoucher voucher) throws FusionException {
      return this.fullVoucherRedemption(migUsername, userTicket, voucher, (Map)null, false);
   }

   public WebServiceResponse fullVoucherRedemption(String migUsername, String userTicket, BlueLabelOneVoucher voucher, Map<String, String> __ctx) throws FusionException {
      return this.fullVoucherRedemption(migUsername, userTicket, voucher, __ctx, true);
   }

   private WebServiceResponse fullVoucherRedemption(String migUsername, String userTicket, BlueLabelOneVoucher voucher, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("fullVoucherRedemption");
            __delBase = this.__getDelegate(false);
            _BlueLabelServiceDel __del = (_BlueLabelServiceDel)__delBase;
            return __del.fullVoucherRedemption(migUsername, userTicket, voucher, __ctx);
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public WebServiceResponse getAccountStatus(String liveid) throws FusionException {
      return this.getAccountStatus(liveid, (Map)null, false);
   }

   public WebServiceResponse getAccountStatus(String liveid, Map<String, String> __ctx) throws FusionException {
      return this.getAccountStatus(liveid, __ctx, true);
   }

   private WebServiceResponse getAccountStatus(String liveid, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getAccountStatus");
            __delBase = this.__getDelegate(false);
            _BlueLabelServiceDel __del = (_BlueLabelServiceDel)__delBase;
            return __del.getAccountStatus(liveid, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public WebServiceResponse registerAccount(String username, String password, int countryCode, String mobileNumber, int secretQuestionCode, String secretQuestionAnswer, String firstName, String lastName, String nickName, String dateOfBirth, String sex, String emailAddress) throws FusionException {
      return this.registerAccount(username, password, countryCode, mobileNumber, secretQuestionCode, secretQuestionAnswer, firstName, lastName, nickName, dateOfBirth, sex, emailAddress, (Map)null, false);
   }

   public WebServiceResponse registerAccount(String username, String password, int countryCode, String mobileNumber, int secretQuestionCode, String secretQuestionAnswer, String firstName, String lastName, String nickName, String dateOfBirth, String sex, String emailAddress, Map<String, String> __ctx) throws FusionException {
      return this.registerAccount(username, password, countryCode, mobileNumber, secretQuestionCode, secretQuestionAnswer, firstName, lastName, nickName, dateOfBirth, sex, emailAddress, __ctx, true);
   }

   private WebServiceResponse registerAccount(String username, String password, int countryCode, String mobileNumber, int secretQuestionCode, String secretQuestionAnswer, String firstName, String lastName, String nickName, String dateOfBirth, String sex, String emailAddress, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("registerAccount");
            __delBase = this.__getDelegate(false);
            _BlueLabelServiceDel __del = (_BlueLabelServiceDel)__delBase;
            return __del.registerAccount(username, password, countryCode, mobileNumber, secretQuestionCode, secretQuestionAnswer, firstName, lastName, nickName, dateOfBirth, sex, emailAddress, __ctx);
         } catch (LocalExceptionWrapper var18) {
            this.__handleExceptionWrapper(__delBase, var18, (OutgoingAsync)null);
         } catch (LocalException var19) {
            __cnt = this.__handleException(__delBase, var19, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static BlueLabelServicePrx checkedCast(ObjectPrx __obj) {
      BlueLabelServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (BlueLabelServicePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::BlueLabelService")) {
               BlueLabelServicePrxHelper __h = new BlueLabelServicePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (BlueLabelServicePrx)__d;
   }

   public static BlueLabelServicePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      BlueLabelServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (BlueLabelServicePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::BlueLabelService", __ctx)) {
               BlueLabelServicePrxHelper __h = new BlueLabelServicePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (BlueLabelServicePrx)__d;
   }

   public static BlueLabelServicePrx checkedCast(ObjectPrx __obj, String __facet) {
      BlueLabelServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::BlueLabelService")) {
               BlueLabelServicePrxHelper __h = new BlueLabelServicePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static BlueLabelServicePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      BlueLabelServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::BlueLabelService", __ctx)) {
               BlueLabelServicePrxHelper __h = new BlueLabelServicePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static BlueLabelServicePrx uncheckedCast(ObjectPrx __obj) {
      BlueLabelServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (BlueLabelServicePrx)__obj;
         } catch (ClassCastException var4) {
            BlueLabelServicePrxHelper __h = new BlueLabelServicePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (BlueLabelServicePrx)__d;
   }

   public static BlueLabelServicePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      BlueLabelServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         BlueLabelServicePrxHelper __h = new BlueLabelServicePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _BlueLabelServiceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _BlueLabelServiceDelD();
   }

   public static void __write(BasicStream __os, BlueLabelServicePrx v) {
      __os.writeProxy(v);
   }

   public static BlueLabelServicePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         BlueLabelServicePrxHelper result = new BlueLabelServicePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
