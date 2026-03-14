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

public final class RecommendationDataCollectionServiceAdminPrxHelper extends ObjectPrxHelperBase implements RecommendationDataCollectionServiceAdminPrx {
   public RecommendationDataCollectionServiceStats getStats() throws FusionExceptionWithRefCode {
      return this.getStats((Map)null, false);
   }

   public RecommendationDataCollectionServiceStats getStats(Map<String, String> __ctx) throws FusionExceptionWithRefCode {
      return this.getStats(__ctx, true);
   }

   private RecommendationDataCollectionServiceStats getStats(Map<String, String> __ctx, boolean __explicitCtx) throws FusionExceptionWithRefCode {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getStats");
            __delBase = this.__getDelegate(false);
            _RecommendationDataCollectionServiceAdminDel __del = (_RecommendationDataCollectionServiceAdminDel)__delBase;
            return __del.getStats(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static RecommendationDataCollectionServiceAdminPrx checkedCast(ObjectPrx __obj) {
      RecommendationDataCollectionServiceAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (RecommendationDataCollectionServiceAdminPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::RecommendationDataCollectionServiceAdmin")) {
               RecommendationDataCollectionServiceAdminPrxHelper __h = new RecommendationDataCollectionServiceAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (RecommendationDataCollectionServiceAdminPrx)__d;
   }

   public static RecommendationDataCollectionServiceAdminPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      RecommendationDataCollectionServiceAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (RecommendationDataCollectionServiceAdminPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::RecommendationDataCollectionServiceAdmin", __ctx)) {
               RecommendationDataCollectionServiceAdminPrxHelper __h = new RecommendationDataCollectionServiceAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (RecommendationDataCollectionServiceAdminPrx)__d;
   }

   public static RecommendationDataCollectionServiceAdminPrx checkedCast(ObjectPrx __obj, String __facet) {
      RecommendationDataCollectionServiceAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::RecommendationDataCollectionServiceAdmin")) {
               RecommendationDataCollectionServiceAdminPrxHelper __h = new RecommendationDataCollectionServiceAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static RecommendationDataCollectionServiceAdminPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      RecommendationDataCollectionServiceAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::RecommendationDataCollectionServiceAdmin", __ctx)) {
               RecommendationDataCollectionServiceAdminPrxHelper __h = new RecommendationDataCollectionServiceAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static RecommendationDataCollectionServiceAdminPrx uncheckedCast(ObjectPrx __obj) {
      RecommendationDataCollectionServiceAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (RecommendationDataCollectionServiceAdminPrx)__obj;
         } catch (ClassCastException var4) {
            RecommendationDataCollectionServiceAdminPrxHelper __h = new RecommendationDataCollectionServiceAdminPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (RecommendationDataCollectionServiceAdminPrx)__d;
   }

   public static RecommendationDataCollectionServiceAdminPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      RecommendationDataCollectionServiceAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         RecommendationDataCollectionServiceAdminPrxHelper __h = new RecommendationDataCollectionServiceAdminPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _RecommendationDataCollectionServiceAdminDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _RecommendationDataCollectionServiceAdminDelD();
   }

   public static void __write(BasicStream __os, RecommendationDataCollectionServiceAdminPrx v) {
      __os.writeProxy(v);
   }

   public static RecommendationDataCollectionServiceAdminPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         RecommendationDataCollectionServiceAdminPrxHelper result = new RecommendationDataCollectionServiceAdminPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
