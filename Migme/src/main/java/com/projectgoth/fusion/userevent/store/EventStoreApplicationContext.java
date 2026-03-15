package com.projectgoth.fusion.userevent.store;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class EventStoreApplicationContext {
   private static ApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/projectgoth/fusion/userevent/store/applicationContext-eventStore.xml");

   public static ApplicationContext getContext() {
      return context;
   }
}
