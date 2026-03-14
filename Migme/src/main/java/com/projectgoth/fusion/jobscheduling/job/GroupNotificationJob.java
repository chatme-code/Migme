package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import org.slf4j.Logger;
import org.quartz.Job;
import org.quartz.SchedulerContext;

public abstract class GroupNotificationJob implements Job {
   private static final Logger log = LoggerFactory.getLogger(ConfigUtils.getLoggerName(GroupNotificationViaAlert.class));
   protected UserNotificationServicePrx userNotificationServiceProxy = null;

   protected synchronized UserNotificationServicePrx findUserNotificationServicePrx(SchedulerContext context) {
      if (this.userNotificationServiceProxy == null) {
         this.userNotificationServiceProxy = (UserNotificationServicePrx)context.get("userNotificationServiceProxy");
      }

      return this.userNotificationServiceProxy;
   }
}
