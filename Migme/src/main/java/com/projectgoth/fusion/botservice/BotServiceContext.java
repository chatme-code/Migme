package com.projectgoth.fusion.botservice;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BotServiceContext {
   private static ApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/projectgoth/fusion/botservice/applicationContext-botService.xml");

   public static ApplicationContext getContext() {
      return context;
   }
}
