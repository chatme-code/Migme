package com.projectgoth.fusion.recommendation;

public class RecommendationTransformParam {
   private final String name;
   private final String value;

   public RecommendationTransformParam(String name, String value) {
      this.name = name;
      this.value = value;
   }

   public String getName() {
      return this.name;
   }

   public String getValue() {
      return this.value;
   }
}
