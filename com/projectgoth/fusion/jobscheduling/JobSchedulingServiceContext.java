package com.projectgoth.fusion.jobscheduling;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JobSchedulingServiceContext {
   private static ApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/projectgoth/fusion/jobscheduling/applicationContext-jobSchedulingService.xml");

   public static ApplicationContext getContext() {
      return context;
   }
}
