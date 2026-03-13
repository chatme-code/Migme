package com.projectgoth.fusion.sessioncache;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SessionCacheApplicationContext {
   private static ApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/projectgoth/fusion/sessioncache/applicationContext-sessionCache.xml");

   public static ApplicationContext getContext() {
      return context;
   }
}
