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

public final class RecommendationDataCollectionServicePrxHelper extends ObjectPrxHelperBase implements RecommendationDataCollectionServicePrx {
   public void logData(CollectedDataIce dataIce) throws FusionExceptionWithRefCode {
      this.logData(dataIce, (Map)null, false);
   }

   public void logData(CollectedDataIce dataIce, Map<String, String> __ctx) throws FusionExceptionWithRefCode {
      this.logData(dataIce, __ctx, true);
   }

   private void logData(CollectedDataIce dataIce, Map<String, String> __ctx, boolean __explicitCtx) throws FusionExceptionWithRefCode {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("logData");
            __delBase = this.__getDelegate(false);
            _RecommendationDataCollectionServiceDel __del = (_RecommendationDataCollectionServiceDel)__delBase;
            __del.logData(dataIce, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static RecommendationDataCollectionServicePrx checkedCast(ObjectPrx __obj) {
      RecommendationDataCollectionServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (RecommendationDataCollectionServicePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::RecommendationDataCollectionService")) {
               RecommendationDataCollectionServicePrxHelper __h = new RecommendationDataCollectionServicePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (RecommendationDataCollectionServicePrx)__d;
   }

   public static RecommendationDataCollectionServicePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      RecommendationDataCollectionServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (RecommendationDataCollectionServicePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::RecommendationDataCollectionService", __ctx)) {
               RecommendationDataCollectionServicePrxHelper __h = new RecommendationDataCollectionServicePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (RecommendationDataCollectionServicePrx)__d;
   }

   public static RecommendationDataCollectionServicePrx checkedCast(ObjectPrx __obj, String __facet) {
      RecommendationDataCollectionServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::RecommendationDataCollectionService")) {
               RecommendationDataCollectionServicePrxHelper __h = new RecommendationDataCollectionServicePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static RecommendationDataCollectionServicePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      RecommendationDataCollectionServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::RecommendationDataCollectionService", __ctx)) {
               RecommendationDataCollectionServicePrxHelper __h = new RecommendationDataCollectionServicePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static RecommendationDataCollectionServicePrx uncheckedCast(ObjectPrx __obj) {
      RecommendationDataCollectionServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (RecommendationDataCollectionServicePrx)__obj;
         } catch (ClassCastException var4) {
            RecommendationDataCollectionServicePrxHelper __h = new RecommendationDataCollectionServicePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (RecommendationDataCollectionServicePrx)__d;
   }

   public static RecommendationDataCollectionServicePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      RecommendationDataCollectionServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         RecommendationDataCollectionServicePrxHelper __h = new RecommendationDataCollectionServicePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _RecommendationDataCollectionServiceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _RecommendationDataCollectionServiceDelD();
   }

   public static void __write(BasicStream __os, RecommendationDataCollectionServicePrx v) {
      __os.writeProxy(v);
   }

   public static RecommendationDataCollectionServicePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         RecommendationDataCollectionServicePrxHelper result = new RecommendationDataCollectionServicePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
