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

public final class EmailAlertPrxHelper extends ObjectPrxHelperBase implements EmailAlertPrx {
   public void requestUnreadEmailCount(String username, String password, UserPrx userProxy) {
      this.requestUnreadEmailCount(username, password, userProxy, (Map)null, false);
   }

   public void requestUnreadEmailCount(String username, String password, UserPrx userProxy, Map<String, String> __ctx) {
      this.requestUnreadEmailCount(username, password, userProxy, __ctx, true);
   }

   private void requestUnreadEmailCount(String username, String password, UserPrx userProxy, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _EmailAlertDel __del = (_EmailAlertDel)__delBase;
            __del.requestUnreadEmailCount(username, password, userProxy, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static EmailAlertPrx checkedCast(ObjectPrx __obj) {
      EmailAlertPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EmailAlertPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::EmailAlert")) {
               EmailAlertPrxHelper __h = new EmailAlertPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (EmailAlertPrx)__d;
   }

   public static EmailAlertPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      EmailAlertPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EmailAlertPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::EmailAlert", __ctx)) {
               EmailAlertPrxHelper __h = new EmailAlertPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (EmailAlertPrx)__d;
   }

   public static EmailAlertPrx checkedCast(ObjectPrx __obj, String __facet) {
      EmailAlertPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::EmailAlert")) {
               EmailAlertPrxHelper __h = new EmailAlertPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static EmailAlertPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      EmailAlertPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::EmailAlert", __ctx)) {
               EmailAlertPrxHelper __h = new EmailAlertPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static EmailAlertPrx uncheckedCast(ObjectPrx __obj) {
      EmailAlertPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EmailAlertPrx)__obj;
         } catch (ClassCastException var4) {
            EmailAlertPrxHelper __h = new EmailAlertPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (EmailAlertPrx)__d;
   }

   public static EmailAlertPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      EmailAlertPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         EmailAlertPrxHelper __h = new EmailAlertPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _EmailAlertDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _EmailAlertDelD();
   }

   public static void __write(BasicStream __os, EmailAlertPrx v) {
      __os.writeProxy(v);
   }

   public static EmailAlertPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         EmailAlertPrxHelper result = new EmailAlertPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
