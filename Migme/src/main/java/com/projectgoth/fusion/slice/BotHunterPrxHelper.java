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

public final class BotHunterPrxHelper extends ObjectPrxHelperBase implements BotHunterPrx {
   public SuspectGroupIce[] getLatestSuspects() throws FusionException {
      return this.getLatestSuspects((Map)null, false);
   }

   public SuspectGroupIce[] getLatestSuspects(Map<String, String> __ctx) throws FusionException {
      return this.getLatestSuspects(__ctx, true);
   }

   private SuspectGroupIce[] getLatestSuspects(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getLatestSuspects");
            __delBase = this.__getDelegate(false);
            _BotHunterDel __del = (_BotHunterDel)__delBase;
            return __del.getLatestSuspects(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static BotHunterPrx checkedCast(ObjectPrx __obj) {
      BotHunterPrx __d = null;
      if (__obj != null) {
         try {
            __d = (BotHunterPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::BotHunter")) {
               BotHunterPrxHelper __h = new BotHunterPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (BotHunterPrx)__d;
   }

   public static BotHunterPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      BotHunterPrx __d = null;
      if (__obj != null) {
         try {
            __d = (BotHunterPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::BotHunter", __ctx)) {
               BotHunterPrxHelper __h = new BotHunterPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (BotHunterPrx)__d;
   }

   public static BotHunterPrx checkedCast(ObjectPrx __obj, String __facet) {
      BotHunterPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::BotHunter")) {
               BotHunterPrxHelper __h = new BotHunterPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static BotHunterPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      BotHunterPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::BotHunter", __ctx)) {
               BotHunterPrxHelper __h = new BotHunterPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static BotHunterPrx uncheckedCast(ObjectPrx __obj) {
      BotHunterPrx __d = null;
      if (__obj != null) {
         try {
            __d = (BotHunterPrx)__obj;
         } catch (ClassCastException var4) {
            BotHunterPrxHelper __h = new BotHunterPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (BotHunterPrx)__d;
   }

   public static BotHunterPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      BotHunterPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         BotHunterPrxHelper __h = new BotHunterPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _BotHunterDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _BotHunterDelD();
   }

   public static void __write(BasicStream __os, BotHunterPrx v) {
      __os.writeProxy(v);
   }

   public static BotHunterPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         BotHunterPrxHelper result = new BotHunterPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
