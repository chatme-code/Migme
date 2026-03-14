package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class RecommendationGenerationServiceStatsHolder {
   public RecommendationGenerationServiceStats value;

   public RecommendationGenerationServiceStatsHolder() {
   }

   public RecommendationGenerationServiceStatsHolder(RecommendationGenerationServiceStats value) {
      this.value = value;
   }

   public RecommendationGenerationServiceStatsHolder.Patcher getPatcher() {
      return new RecommendationGenerationServiceStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            RecommendationGenerationServiceStatsHolder.this.value = (RecommendationGenerationServiceStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::RecommendationGenerationServiceStats";
      }
   }
}
