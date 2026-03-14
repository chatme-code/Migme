package com.projectgoth.fusion.uns;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class UserNotificationServiceContext {
   private static ApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/projectgoth/fusion/uns/applicationContext-userNotificationService.xml");

   public static ApplicationContext getContext() {
      return context;
   }
}
