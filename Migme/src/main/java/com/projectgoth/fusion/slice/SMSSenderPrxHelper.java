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

public final class SMSSenderPrxHelper extends ObjectPrxHelperBase implements SMSSenderPrx {
   public void sendSMS(MessageDataIce message, long delay) throws FusionException {
      this.sendSMS(message, delay, (Map)null, false);
   }

   public void sendSMS(MessageDataIce message, long delay, Map<String, String> __ctx) throws FusionException {
      this.sendSMS(message, delay, __ctx, true);
   }

   private void sendSMS(MessageDataIce message, long delay, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("sendSMS");
            __delBase = this.__getDelegate(false);
            _SMSSenderDel __del = (_SMSSenderDel)__delBase;
            __del.sendSMS(message, delay, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendSystemSMS(SystemSMSDataIce message, long delay) throws FusionException {
      this.sendSystemSMS(message, delay, (Map)null, false);
   }

   public void sendSystemSMS(SystemSMSDataIce message, long delay, Map<String, String> __ctx) throws FusionException {
      this.sendSystemSMS(message, delay, __ctx, true);
   }

   private void sendSystemSMS(SystemSMSDataIce message, long delay, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("sendSystemSMS");
            __delBase = this.__getDelegate(false);
            _SMSSenderDel __del = (_SMSSenderDel)__delBase;
            __del.sendSystemSMS(message, delay, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static SMSSenderPrx checkedCast(ObjectPrx __obj) {
      SMSSenderPrx __d = null;
      if (__obj != null) {
         try {
            __d = (SMSSenderPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::SMSSender")) {
               SMSSenderPrxHelper __h = new SMSSenderPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (SMSSenderPrx)__d;
   }

   public static SMSSenderPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      SMSSenderPrx __d = null;
      if (__obj != null) {
         try {
            __d = (SMSSenderPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::SMSSender", __ctx)) {
               SMSSenderPrxHelper __h = new SMSSenderPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (SMSSenderPrx)__d;
   }

   public static SMSSenderPrx checkedCast(ObjectPrx __obj, String __facet) {
      SMSSenderPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::SMSSender")) {
               SMSSenderPrxHelper __h = new SMSSenderPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static SMSSenderPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      SMSSenderPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::SMSSender", __ctx)) {
               SMSSenderPrxHelper __h = new SMSSenderPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static SMSSenderPrx uncheckedCast(ObjectPrx __obj) {
      SMSSenderPrx __d = null;
      if (__obj != null) {
         try {
            __d = (SMSSenderPrx)__obj;
         } catch (ClassCastException var4) {
            SMSSenderPrxHelper __h = new SMSSenderPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (SMSSenderPrx)__d;
   }

   public static SMSSenderPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      SMSSenderPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         SMSSenderPrxHelper __h = new SMSSenderPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _SMSSenderDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _SMSSenderDelD();
   }

   public static void __write(BasicStream __os, SMSSenderPrx v) {
      __os.writeProxy(v);
   }

   public static SMSSenderPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         SMSSenderPrxHelper result = new SMSSenderPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
