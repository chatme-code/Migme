package com.projectgoth.fusion.userevent.system;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class EventSystemApplicationContext {
   private static ApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/projectgoth/fusion/userevent/system/applicationContext-eventSystem.xml");

   public static ApplicationContext getContext() {
      return context;
   }
}
