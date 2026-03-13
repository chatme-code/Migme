package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class RecommendationDataCollectionServiceAdminHolder {
   public RecommendationDataCollectionServiceAdmin value;

   public RecommendationDataCollectionServiceAdminHolder() {
   }

   public RecommendationDataCollectionServiceAdminHolder(RecommendationDataCollectionServiceAdmin value) {
      this.value = value;
   }

   public RecommendationDataCollectionServiceAdminHolder.Patcher getPatcher() {
      return new RecommendationDataCollectionServiceAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            RecommendationDataCollectionServiceAdminHolder.this.value = (RecommendationDataCollectionServiceAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::RecommendationDataCollectionServiceAdmin";
      }
   }
}
