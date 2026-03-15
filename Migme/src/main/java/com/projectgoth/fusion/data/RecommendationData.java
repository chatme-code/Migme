package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecommendationData implements Serializable {
   private List<RecommendationItem> recommendations;

   public RecommendationData() {
      this.recommendations = new ArrayList();
   }

   public RecommendationData(List<RecommendationItem> recommendations) {
      this.recommendations = recommendations;
   }

   public List<RecommendationItem> getRecommendations() {
      return this.recommendations;
   }

   public void setRecommendations(List<RecommendationItem> recommendations) {
      this.recommendations = recommendations;
   }

   public void addRecommendationItem(RecommendationItem item) {
      this.recommendations.add(item);
   }
}
