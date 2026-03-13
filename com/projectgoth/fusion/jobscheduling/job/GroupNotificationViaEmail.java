package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.jobscheduling.domain.EmailNote;
import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class GroupNotificationViaEmail extends GroupNotificationJob {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GroupNotificationViaEmail.class));

   public void execute(JobExecutionContext context) throws JobExecutionException {
      JobDataMap jobDataMap = context.getMergedJobDataMap();
      int groupId = jobDataMap.getInt("groupId");
      EmailNote note = (EmailNote)jobDataMap.get("note");
      log.info("triggering GroupNotificationViaEmail for group [" + groupId + "] with message [" + (note == null ? null : note.getMessage()) + "]");

      try {
         this.findUserNotificationServicePrx(context.getScheduler().getContext());
         if (log.isDebugEnabled()) {
            log.debug("contacting UNS [" + this.userNotificationServiceProxy + "] about group [" + groupId + "] and note subject [" + note.getSubject() + "]");
         }

         this.userNotificationServiceProxy.notifyFusionGroupAnnouncementViaEmail(groupId, note.toEmailUserNotification());
      } catch (Exception var6) {
         log.error("failed to execute job ", var6);
         throw new JobExecutionException(var6);
      }
   }
}
