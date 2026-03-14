package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class RecommendationGenerationServiceAdminHolder {
   public RecommendationGenerationServiceAdmin value;

   public RecommendationGenerationServiceAdminHolder() {
   }

   public RecommendationGenerationServiceAdminHolder(RecommendationGenerationServiceAdmin value) {
      this.value = value;
   }

   public RecommendationGenerationServiceAdminHolder.Patcher getPatcher() {
      return new RecommendationGenerationServiceAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            RecommendationGenerationServiceAdminHolder.this.value = (RecommendationGenerationServiceAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::RecommendationGenerationServiceAdmin";
      }
   }
}
