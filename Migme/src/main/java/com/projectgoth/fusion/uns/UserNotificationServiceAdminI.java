package com.projectgoth.fusion.uns;

import Ice.Current;
import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.UserNotificationServiceStats;
import com.projectgoth.fusion.slice._UserNotificationServiceAdminDisp;

public class UserNotificationServiceAdminI extends _UserNotificationServiceAdminDisp {
   private UserNotificationServiceI userNotificationService;

   public UserNotificationServiceAdminI(UserNotificationServiceI blueLabelService) {
      this.userNotificationService = blueLabelService;
   }

   public UserNotificationServiceStats getStats(Current __current) throws FusionException {
      UserNotificationServiceStats stats = ServiceStatsFactory.getUserNotificationServiceStats(UserNotificationService.startTime);
      stats.alertsSent = this.userNotificationService.getAlertsSent();
      stats.emailsSent = this.userNotificationService.getEmailsSent();
      stats.smsSent = this.userNotificationService.getSmsSent();
      stats.notificationsSent = this.userNotificationService.getNotificationsSent();
      stats.alertQueueSize = this.userNotificationService.getAlertQueueSize();
      stats.emailQueueSize = this.userNotificationService.getEmailQueueSize();
      stats.smsQueueSize = this.userNotificationService.getSmsQueueSize();
      stats.notificationQueueSize = this.userNotificationService.getNotificationQueueSize();
      return stats;
   }
}
