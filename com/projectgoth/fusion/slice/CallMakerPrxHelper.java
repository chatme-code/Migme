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

public final class CallMakerPrxHelper extends ObjectPrxHelperBase implements CallMakerPrx {
   public CallDataIce requestCallback(CallDataIce call, int maxDuration, int retries) throws FusionException {
      return this.requestCallback(call, maxDuration, retries, (Map)null, false);
   }

   public CallDataIce requestCallback(CallDataIce call, int maxDuration, int retries, Map<String, String> __ctx) throws FusionException {
      return this.requestCallback(call, maxDuration, retries, __ctx, true);
   }

   private CallDataIce requestCallback(CallDataIce call, int maxDuration, int retries, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("requestCallback");
            __delBase = this.__getDelegate(false);
            _CallMakerDel __del = (_CallMakerDel)__delBase;
            return __del.requestCallback(call, maxDuration, retries, __ctx);
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static CallMakerPrx checkedCast(ObjectPrx __obj) {
      CallMakerPrx __d = null;
      if (__obj != null) {
         try {
            __d = (CallMakerPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::CallMaker")) {
               CallMakerPrxHelper __h = new CallMakerPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (CallMakerPrx)__d;
   }

   public static CallMakerPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      CallMakerPrx __d = null;
      if (__obj != null) {
         try {
            __d = (CallMakerPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::CallMaker", __ctx)) {
               CallMakerPrxHelper __h = new CallMakerPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (CallMakerPrx)__d;
   }

   public static CallMakerPrx checkedCast(ObjectPrx __obj, String __facet) {
      CallMakerPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::CallMaker")) {
               CallMakerPrxHelper __h = new CallMakerPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static CallMakerPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      CallMakerPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::CallMaker", __ctx)) {
               CallMakerPrxHelper __h = new CallMakerPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static CallMakerPrx uncheckedCast(ObjectPrx __obj) {
      CallMakerPrx __d = null;
      if (__obj != null) {
         try {
            __d = (CallMakerPrx)__obj;
         } catch (ClassCastException var4) {
            CallMakerPrxHelper __h = new CallMakerPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (CallMakerPrx)__d;
   }

   public static CallMakerPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      CallMakerPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         CallMakerPrxHelper __h = new CallMakerPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _CallMakerDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _CallMakerDelD();
   }

   public static void __write(BasicStream __os, CallMakerPrx v) {
      __os.writeProxy(v);
   }

   public static CallMakerPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         CallMakerPrxHelper result = new CallMakerPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
