package com.projectgoth.fusion.recommendation.generation;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RecommendationGenerationServiceContext {
   private static ApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/projectgoth/fusion/recommendation/generation/applicationContext-recommendationGenerationService.xml");

   public static ApplicationContext getContext() {
      return context;
   }
}
