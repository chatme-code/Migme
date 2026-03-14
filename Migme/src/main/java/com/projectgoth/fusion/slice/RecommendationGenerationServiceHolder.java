package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class RecommendationGenerationServiceHolder {
   public RecommendationGenerationService value;

   public RecommendationGenerationServiceHolder() {
   }

   public RecommendationGenerationServiceHolder(RecommendationGenerationService value) {
      this.value = value;
   }

   public RecommendationGenerationServiceHolder.Patcher getPatcher() {
      return new RecommendationGenerationServiceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            RecommendationGenerationServiceHolder.this.value = (RecommendationGenerationService)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::RecommendationGenerationService";
      }
   }
}
