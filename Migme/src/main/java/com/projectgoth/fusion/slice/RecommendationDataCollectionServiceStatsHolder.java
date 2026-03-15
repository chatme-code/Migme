package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class RecommendationDataCollectionServiceStatsHolder {
   public RecommendationDataCollectionServiceStats value;

   public RecommendationDataCollectionServiceStatsHolder() {
   }

   public RecommendationDataCollectionServiceStatsHolder(RecommendationDataCollectionServiceStats value) {
      this.value = value;
   }

   public RecommendationDataCollectionServiceStatsHolder.Patcher getPatcher() {
      return new RecommendationDataCollectionServiceStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            RecommendationDataCollectionServiceStatsHolder.this.value = (RecommendationDataCollectionServiceStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::RecommendationDataCollectionServiceStats";
      }
   }
}
