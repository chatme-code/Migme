package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class GroupNotificationViaAlert extends GroupNotificationJob {
   private static final Logger log = LoggerFactory.getLogger(ConfigUtils.getLoggerName(GroupNotificationViaAlert.class));

   public void execute(JobExecutionContext context) throws JobExecutionException {
      JobDataMap jobDataMap = context.getMergedJobDataMap();
      int groupId = jobDataMap.getInt("groupId");
      String message = (String)jobDataMap.get("message");
      log.info("triggering GroupNotificationViaAlert for group [" + groupId + "] with message [" + message + "]");

      try {
         this.findUserNotificationServicePrx(context.getScheduler().getContext());
         if (log.isDebugEnabled()) {
            log.debug("contacting UNS [" + this.userNotificationServiceProxy + "] about group [" + groupId + "] and message [" + message + "]");
         }

         this.userNotificationServiceProxy.notifyFusionGroupViaAlert(groupId, message);
      } catch (Exception var6) {
         log.error("failed to execute job ", var6);
         throw new JobExecutionException(var6);
      }
   }
}
