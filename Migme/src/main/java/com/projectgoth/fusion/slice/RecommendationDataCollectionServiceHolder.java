package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class RecommendationDataCollectionServiceHolder {
   public RecommendationDataCollectionService value;

   public RecommendationDataCollectionServiceHolder() {
   }

   public RecommendationDataCollectionServiceHolder(RecommendationDataCollectionService value) {
      this.value = value;
   }

   public RecommendationDataCollectionServiceHolder.Patcher getPatcher() {
      return new RecommendationDataCollectionServiceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            RecommendationDataCollectionServiceHolder.this.value = (RecommendationDataCollectionService)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::RecommendationDataCollectionService";
      }
   }
}
