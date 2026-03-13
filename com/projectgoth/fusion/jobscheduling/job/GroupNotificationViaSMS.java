package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.jobscheduling.domain.SMSNote;
import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class GroupNotificationViaSMS extends GroupNotificationJob {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GroupNotificationViaSMS.class));

   public void execute(JobExecutionContext context) throws JobExecutionException {
      JobDataMap jobDataMap = context.getMergedJobDataMap();
      int groupId = jobDataMap.getInt("groupId");
      SMSNote note = (SMSNote)jobDataMap.get("note");
      log.info("triggering GroupNotificationViaSMS for group [" + groupId + "] with message [" + (note == null ? null : note.getMessage()) + "]");

      try {
         this.findUserNotificationServicePrx(context.getScheduler().getContext());
         if (log.isDebugEnabled()) {
            log.debug("contacting UNS [" + this.userNotificationServiceProxy + "] about group [" + groupId + "] and note message [" + note.getMessage() + "]");
         }

         this.userNotificationServiceProxy.notifyFusionGroupEventViaSMS(groupId, note.toSMSUserNotification());
      } catch (Exception var6) {
         log.error("failed to execute job ", var6);
         throw new JobExecutionException(var6);
      }
   }
}
