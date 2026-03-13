package com.projectgoth.fusion.bl1;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BlueLabelServiceContext {
   private static ApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/projectgoth/fusion/bl1/applicationContext-blueLabelService.xml");

   public static ApplicationContext getContext() {
      return context;
   }
}
