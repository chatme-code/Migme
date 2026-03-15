package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class RecommendationDataCollectionServiceStatsPrxHelper extends ObjectPrxHelperBase implements RecommendationDataCollectionServiceStatsPrx {
   public static RecommendationDataCollectionServiceStatsPrx checkedCast(ObjectPrx __obj) {
      RecommendationDataCollectionServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (RecommendationDataCollectionServiceStatsPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::RecommendationDataCollectionServiceStats")) {
               RecommendationDataCollectionServiceStatsPrxHelper __h = new RecommendationDataCollectionServiceStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (RecommendationDataCollectionServiceStatsPrx)__d;
   }

   public static RecommendationDataCollectionServiceStatsPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      RecommendationDataCollectionServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (RecommendationDataCollectionServiceStatsPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::RecommendationDataCollectionServiceStats", __ctx)) {
               RecommendationDataCollectionServiceStatsPrxHelper __h = new RecommendationDataCollectionServiceStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (RecommendationDataCollectionServiceStatsPrx)__d;
   }

   public static RecommendationDataCollectionServiceStatsPrx checkedCast(ObjectPrx __obj, String __facet) {
      RecommendationDataCollectionServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::RecommendationDataCollectionServiceStats")) {
               RecommendationDataCollectionServiceStatsPrxHelper __h = new RecommendationDataCollectionServiceStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static RecommendationDataCollectionServiceStatsPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      RecommendationDataCollectionServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::RecommendationDataCollectionServiceStats", __ctx)) {
               RecommendationDataCollectionServiceStatsPrxHelper __h = new RecommendationDataCollectionServiceStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static RecommendationDataCollectionServiceStatsPrx uncheckedCast(ObjectPrx __obj) {
      RecommendationDataCollectionServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (RecommendationDataCollectionServiceStatsPrx)__obj;
         } catch (ClassCastException var4) {
            RecommendationDataCollectionServiceStatsPrxHelper __h = new RecommendationDataCollectionServiceStatsPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (RecommendationDataCollectionServiceStatsPrx)__d;
   }

   public static RecommendationDataCollectionServiceStatsPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      RecommendationDataCollectionServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         RecommendationDataCollectionServiceStatsPrxHelper __h = new RecommendationDataCollectionServiceStatsPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _RecommendationDataCollectionServiceStatsDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _RecommendationDataCollectionServiceStatsDelD();
   }

   public static void __write(BasicStream __os, RecommendationDataCollectionServiceStatsPrx v) {
      __os.writeProxy(v);
   }

   public static RecommendationDataCollectionServiceStatsPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         RecommendationDataCollectionServiceStatsPrxHelper result = new RecommendationDataCollectionServiceStatsPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
