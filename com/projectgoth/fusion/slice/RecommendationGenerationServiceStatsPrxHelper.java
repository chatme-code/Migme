package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class RecommendationGenerationServiceStatsPrxHelper extends ObjectPrxHelperBase implements RecommendationGenerationServiceStatsPrx {
   public static RecommendationGenerationServiceStatsPrx checkedCast(ObjectPrx __obj) {
      RecommendationGenerationServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (RecommendationGenerationServiceStatsPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::RecommendationGenerationServiceStats")) {
               RecommendationGenerationServiceStatsPrxHelper __h = new RecommendationGenerationServiceStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (RecommendationGenerationServiceStatsPrx)__d;
   }

   public static RecommendationGenerationServiceStatsPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      RecommendationGenerationServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (RecommendationGenerationServiceStatsPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::RecommendationGenerationServiceStats", __ctx)) {
               RecommendationGenerationServiceStatsPrxHelper __h = new RecommendationGenerationServiceStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (RecommendationGenerationServiceStatsPrx)__d;
   }

   public static RecommendationGenerationServiceStatsPrx checkedCast(ObjectPrx __obj, String __facet) {
      RecommendationGenerationServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::RecommendationGenerationServiceStats")) {
               RecommendationGenerationServiceStatsPrxHelper __h = new RecommendationGenerationServiceStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static RecommendationGenerationServiceStatsPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      RecommendationGenerationServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::RecommendationGenerationServiceStats", __ctx)) {
               RecommendationGenerationServiceStatsPrxHelper __h = new RecommendationGenerationServiceStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static RecommendationGenerationServiceStatsPrx uncheckedCast(ObjectPrx __obj) {
      RecommendationGenerationServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (RecommendationGenerationServiceStatsPrx)__obj;
         } catch (ClassCastException var4) {
            RecommendationGenerationServiceStatsPrxHelper __h = new RecommendationGenerationServiceStatsPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (RecommendationGenerationServiceStatsPrx)__d;
   }

   public static RecommendationGenerationServiceStatsPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      RecommendationGenerationServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         RecommendationGenerationServiceStatsPrxHelper __h = new RecommendationGenerationServiceStatsPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _RecommendationGenerationServiceStatsDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _RecommendationGenerationServiceStatsDelD();
   }

   public static void __write(BasicStream __os, RecommendationGenerationServiceStatsPrx v) {
      __os.writeProxy(v);
   }

   public static RecommendationGenerationServiceStatsPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         RecommendationGenerationServiceStatsPrxHelper result = new RecommendationGenerationServiceStatsPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
