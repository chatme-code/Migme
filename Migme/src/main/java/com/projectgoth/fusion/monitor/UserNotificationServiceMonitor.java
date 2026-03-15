package com.projectgoth.fusion.monitor;

import Ice.LocalException;
import Ice.ObjectPrx;
import com.projectgoth.fusion.common.PortRegistry;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.UserNotificationServiceAdminPrx;
import com.projectgoth.fusion.slice.UserNotificationServiceAdminPrxHelper;
import com.projectgoth.fusion.slice.UserNotificationServiceStats;
import org.eclipse.swt.widgets.TreeItem;

public class UserNotificationServiceMonitor extends BaseStatsMonitor {
   private UserNotificationServiceStats latestStats = null;
   private Exception latestException = null;
   private boolean latestStatsLoaded = false;
   private UserNotificationServiceAdminPrx userNotificationServiceAdminPrx = null;
   protected TreeItem sentTreeItem;
   protected TreeItem queueSizeTreeItem;

   public UserNotificationServiceMonitor(String hostName, Integer port, TreeItem parentTreeItem) {
      super(hostName, parentTreeItem);
      this.sentTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.queueSizeTreeItem = new TreeItem(this.baseTreeItem, 0);
      if (port != null && port > 0) {
         this.port = port;
      } else {
         this.port = PortRegistry.USER_NOTIFICATION_SERVICE_ADMIN.getPort();
      }

   }

   private synchronized UserNotificationServiceAdminPrx getUserNotificationServiceAdminProxy() {
      if (this.userNotificationServiceAdminPrx == null) {
         String stringifiedProxy = "UserNotificationServiceAdmin:tcp -h " + this.hostName + " -p " + this.port + " -t 2000";
         ObjectPrx basePrx = Monitor.communicator().stringToProxy(stringifiedProxy);
         this.userNotificationServiceAdminPrx = UserNotificationServiceAdminPrxHelper.checkedCast(basePrx);
      }

      return this.userNotificationServiceAdminPrx;
   }

   public void getStats() {
      try {
         UserNotificationServiceAdminPrx localUserNotificationServiceAdminPrx = this.getUserNotificationServiceAdminProxy();
         if (localUserNotificationServiceAdminPrx != null) {
            this.latestStats = localUserNotificationServiceAdminPrx.getStats();
            this.latestException = null;
         }
      } catch (Exception var2) {
         this.latestStats = null;
         this.latestException = var2;
      }

      this.latestStatsLoaded = true;
   }

   public void run() {
      if (!this.latestStatsLoaded) {
         this.getStats();
      }

      this.latestStatsLoaded = false;
      if (!(this.latestException instanceof FusionException)) {
         if (this.latestException instanceof LocalException && this.isOnline) {
            this.isOnline = false;
            this.updateWithLatestStats(this.latestStats, this.isOnline);
            this.sendAlert("Connection to UserNotificationService on " + this.hostName + " failed");
         }

         try {
            try {
               if (this.latestStats != null) {
                  if (!this.isOnline) {
                     this.isOnline = true;
                     this.sendAlert("Connection to UserNotificationService on " + this.hostName + " restored");
                  }

                  this.updateWithLatestStats(this.latestStats, this.isOnline);
                  this.sentTreeItem.setText("Messages Sent, Alerts: " + this.latestStats.alertsSent + ", Email: " + this.latestStats.emailsSent + ", SMS: " + this.latestStats.smsSent + ", Notifications: " + this.latestStats.notificationsSent);
                  this.queueSizeTreeItem.setText("Queue Sizes, Alerts: " + this.latestStats.alertQueueSize + ", Email: " + this.latestStats.emailQueueSize + ", SMS: " + this.latestStats.smsQueueSize + "" + ", Notifications: " + this.latestStats.notificationQueueSize);
               }
            } catch (Exception var6) {
               System.err.println("WARNING: Unable to save stats for the UserNotificationService on " + this.hostName);
               var6.printStackTrace();
            }

         } finally {
            ;
         }
      }
   }
}
