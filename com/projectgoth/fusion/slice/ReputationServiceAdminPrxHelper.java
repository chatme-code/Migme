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

public final class ReputationServiceAdminPrxHelper extends ObjectPrxHelperBase implements ReputationServiceAdminPrx {
   public ReputationServiceStats getStats() throws FusionException {
      return this.getStats((Map)null, false);
   }

   public ReputationServiceStats getStats(Map<String, String> __ctx) throws FusionException {
      return this.getStats(__ctx, true);
   }

   private ReputationServiceStats getStats(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getStats");
            __delBase = this.__getDelegate(false);
            _ReputationServiceAdminDel __del = (_ReputationServiceAdminDel)__delBase;
            return __del.getStats(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static ReputationServiceAdminPrx checkedCast(ObjectPrx __obj) {
      ReputationServiceAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (ReputationServiceAdminPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::ReputationServiceAdmin")) {
               ReputationServiceAdminPrxHelper __h = new ReputationServiceAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (ReputationServiceAdminPrx)__d;
   }

   public static ReputationServiceAdminPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      ReputationServiceAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (ReputationServiceAdminPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::ReputationServiceAdmin", __ctx)) {
               ReputationServiceAdminPrxHelper __h = new ReputationServiceAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (ReputationServiceAdminPrx)__d;
   }

   public static ReputationServiceAdminPrx checkedCast(ObjectPrx __obj, String __facet) {
      ReputationServiceAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::ReputationServiceAdmin")) {
               ReputationServiceAdminPrxHelper __h = new ReputationServiceAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static ReputationServiceAdminPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      ReputationServiceAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::ReputationServiceAdmin", __ctx)) {
               ReputationServiceAdminPrxHelper __h = new ReputationServiceAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static ReputationServiceAdminPrx uncheckedCast(ObjectPrx __obj) {
      ReputationServiceAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (ReputationServiceAdminPrx)__obj;
         } catch (ClassCastException var4) {
            ReputationServiceAdminPrxHelper __h = new ReputationServiceAdminPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (ReputationServiceAdminPrx)__d;
   }

   public static ReputationServiceAdminPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      ReputationServiceAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         ReputationServiceAdminPrxHelper __h = new ReputationServiceAdminPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _ReputationServiceAdminDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _ReputationServiceAdminDelD();
   }

   public static void __write(BasicStream __os, ReputationServiceAdminPrx v) {
      __os.writeProxy(v);
   }

   public static ReputationServiceAdminPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         ReputationServiceAdminPrxHelper result = new ReputationServiceAdminPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
