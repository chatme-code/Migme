package com.projectgoth.fusion.monitor;

import Ice.LocalException;
import Ice.ObjectPrx;
import com.projectgoth.fusion.slice.EmailAlertAdminPrx;
import com.projectgoth.fusion.slice.EmailAlertAdminPrxHelper;
import com.projectgoth.fusion.slice.EmailAlertStats;
import com.projectgoth.fusion.slice.FusionException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

public class EmailAlertMonitor extends BaseMonitor {
   public EmailAlertStats latestStats = null;
   private Exception latestException = null;
   private boolean latestStatsLoaded = false;
   private EmailAlertAdminPrx emailAlertAdminPrx;
   private TreeItem numNotificationsReceivedPerSecondTreeItem;
   private String numNotificationsReceivedPerSecondText = "No. Notifications Received Per Second";
   private TreeItem maxNotificationsReceivedPerSecondTreeItem;
   private String maxNotificationsReceivedPerSecondText = "Max. Notifications Received Per Second";
   private TreeItem numNotificationsProcessedPerSecondTreeItem;
   private String numNotificationsProcessedPerSecondText = "No. Notifications Processed Per Second";
   private TreeItem maxNotificationsProcessedPerSecondTreeItem;
   private String maxNotificationsProcessedPerSecondText = "Max. Notifications Processed Per Second";
   private TreeItem notificationsThreadPoolSizeTreeItem;
   private String notificationsThreadPoolSizeText = "Notifications Thread Pool Size (Active Threads)";
   private TreeItem notificationsMaxThreadPoolSizeTreeItem;
   private String notificationsMaxThreadPoolSizeText = "Notifications Max. Thread Pool Size";
   private TreeItem notificationsThreadPoolQueueSizeTreeItem;
   private String notificationsThreadPoolQueueSizeText = "Notifications Thread Pool Queue Size";
   private TreeItem gatewayQueriesThreadPoolSizeTreeItem;
   private String gatewayQueriesThreadPoolSizeText = "Gateway Queries Thread Pool Size (Active Threads)";
   private TreeItem gatewayQueriesMaxThreadPoolSizeTreeItem;
   private String gatewayQueriesMaxThreadPoolSizeText = "Gateway Queries Max. Thread Pool Size";
   private TreeItem gatewayQueriesThreadPoolQueueSizeTreeItem;
   private String gatewayQueriesThreadPoolQueueSizeText = "Gateway Queries Thread Pool Queue Size";
   private TreeItem numGatewayQueriesReceivedPerSecondTreeItem;
   private String numGatewayQueriesReceivedPerSecondText = "No. Gateway Queries Received Per Second";
   private TreeItem maxGatewayQueriesReceivedPerSecondTreeItem;
   private String maxGatewayQueriesReceivedPerSecondText = "Max. Gateway Queries Received Per Second";
   private TreeItem numGatewayQueriesProcessedPerSecondTreeItem;
   private String numGatewayQueriesProcessedPerSecondText = "No. Gateway Queries Processed Per Second";
   private TreeItem maxGatewayQueriesProcessedPerSecondTreeItem;
   private String maxGatewayQueriesProcessedPerSecondText = "Max. Gateway Queries Processed Per Second";
   private TreeItem numGatewayQueriesDiscardedPerSecondTreeItem;
   private String numGatewayQueriesDiscardedPerSecondText = "No. Gateway Queries Discarded Per Second";
   private TreeItem maxGatewayQueriesDiscardedPerSecondTreeItem;
   private String maxGatewayQueriesDiscardedPerSecondText = "Max. Gateway Queries Discarded Per Second";
   private TreeItem jvmMemoryUsageTreeItem;
   private String jvmMemoryUsageText = "JVM Memory Usage";
   private TreeItem uptimeTreeItem;
   private String uptimeText = "Uptime";

   public EmailAlertMonitor(String hostName, Integer port, TreeItem parentTreeItem) {
      super(hostName, parentTreeItem);
      this.numNotificationsReceivedPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.numNotificationsReceivedPerSecondTreeItem.setText(this.numNotificationsReceivedPerSecondText + ":");
      this.maxNotificationsReceivedPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.maxNotificationsReceivedPerSecondTreeItem.setText(this.maxNotificationsReceivedPerSecondText + ":");
      this.numNotificationsProcessedPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.numNotificationsProcessedPerSecondTreeItem.setText(this.numNotificationsProcessedPerSecondText + ":");
      this.maxNotificationsProcessedPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.maxNotificationsProcessedPerSecondTreeItem.setText(this.maxNotificationsProcessedPerSecondText + ":");
      this.notificationsThreadPoolSizeTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.notificationsThreadPoolSizeTreeItem.setText(this.notificationsThreadPoolSizeText + ":");
      this.notificationsMaxThreadPoolSizeTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.notificationsMaxThreadPoolSizeTreeItem.setText(this.notificationsMaxThreadPoolSizeText + ":");
      this.notificationsThreadPoolQueueSizeTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.notificationsThreadPoolQueueSizeTreeItem.setText(this.notificationsThreadPoolQueueSizeText + ":");
      this.gatewayQueriesThreadPoolSizeTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.gatewayQueriesThreadPoolSizeTreeItem.setText(this.gatewayQueriesThreadPoolSizeText + ":");
      this.gatewayQueriesMaxThreadPoolSizeTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.gatewayQueriesMaxThreadPoolSizeTreeItem.setText(this.gatewayQueriesMaxThreadPoolSizeText + ":");
      this.gatewayQueriesThreadPoolQueueSizeTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.gatewayQueriesThreadPoolQueueSizeTreeItem.setText(this.gatewayQueriesThreadPoolQueueSizeText + ":");
      this.numGatewayQueriesReceivedPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.numGatewayQueriesReceivedPerSecondTreeItem.setText(this.numGatewayQueriesReceivedPerSecondText + ":");
      this.maxGatewayQueriesReceivedPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.maxGatewayQueriesReceivedPerSecondTreeItem.setText(this.maxGatewayQueriesReceivedPerSecondText + ":");
      this.numGatewayQueriesProcessedPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.numGatewayQueriesProcessedPerSecondTreeItem.setText(this.numGatewayQueriesProcessedPerSecondText + ":");
      this.maxGatewayQueriesProcessedPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.maxGatewayQueriesProcessedPerSecondTreeItem.setText(this.maxGatewayQueriesProcessedPerSecondText + ":");
      this.numGatewayQueriesDiscardedPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.numGatewayQueriesDiscardedPerSecondTreeItem.setText(this.numGatewayQueriesDiscardedPerSecondText + ":");
      this.maxGatewayQueriesDiscardedPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.maxGatewayQueriesDiscardedPerSecondTreeItem.setText(this.maxGatewayQueriesDiscardedPerSecondText + ":");
      this.jvmMemoryUsageTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.jvmMemoryUsageTreeItem.setText(this.jvmMemoryUsageText + ":");
      this.uptimeTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.uptimeTreeItem.setText(this.uptimeText + ":");
      String stringifiedProxy = "EmailAlertAdmin:tcp -h " + hostName + " -p " + (port == null ? "9345" : port);
      ObjectPrx basePrx = Monitor.communicator().stringToProxy(stringifiedProxy);
      this.emailAlertAdminPrx = EmailAlertAdminPrxHelper.uncheckedCast(basePrx);
   }

   public void getStats() {
      try {
         this.latestStats = this.emailAlertAdminPrx.getStats();
         this.latestException = null;
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
            this.baseTreeItem.setText(this.hostName + " OFFLINE");
            this.baseTreeItem.setForeground(new Color(Display.getCurrent(), 192, 0, 0));
            this.sendAlert("Connection to EmailAlert on " + this.hostName + " failed");
         }

         try {
            try {
               if (this.latestStats != null) {
                  if (!this.isOnline) {
                     this.isOnline = true;
                     this.sendAlert("Connection to EmailAlert on " + this.hostName + " restored");
                  }

                  this.baseTreeItem.setText(this.hostName + " ONLINE");
                  if (this.latestStats.gatewayQueriesThreadPoolQueueSize > 0) {
                     this.baseTreeItem.setForeground(new Color(Display.getCurrent(), 255, 110, 0));
                  } else {
                     this.baseTreeItem.setForeground(new Color(Display.getCurrent(), 0, 160, 0));
                  }

                  this.numNotificationsReceivedPerSecondTreeItem.setText(this.numNotificationsReceivedPerSecondText + ": " + this.latestStats.numNotificationsReceivedPerSecond);
                  this.maxNotificationsReceivedPerSecondTreeItem.setText(this.maxNotificationsReceivedPerSecondText + ": " + this.latestStats.maxNotificationsReceivedPerSecond);
                  this.numNotificationsProcessedPerSecondTreeItem.setText(this.numNotificationsProcessedPerSecondText + ": " + this.latestStats.numNotificationsProcessedPerSecond);
                  this.maxNotificationsProcessedPerSecondTreeItem.setText(this.maxNotificationsProcessedPerSecondText + ": " + this.latestStats.maxNotificationsProcessedPerSecond);
                  this.notificationsThreadPoolSizeTreeItem.setText(this.notificationsThreadPoolSizeText + ": " + this.latestStats.notificationsThreadPoolSize);
                  this.notificationsMaxThreadPoolSizeTreeItem.setText(this.notificationsMaxThreadPoolSizeText + ": " + this.latestStats.notificationsMaxThreadPoolSize);
                  this.notificationsThreadPoolQueueSizeTreeItem.setText(this.notificationsThreadPoolQueueSizeText + ": " + this.latestStats.notificationsThreadPoolQueueSize);
                  this.gatewayQueriesThreadPoolSizeTreeItem.setText(this.gatewayQueriesThreadPoolSizeText + ": " + this.latestStats.gatewayQueriesThreadPoolSize);
                  this.gatewayQueriesMaxThreadPoolSizeTreeItem.setText(this.gatewayQueriesMaxThreadPoolSizeText + ": " + this.latestStats.gatewayQueriesMaxThreadPoolSize);
                  this.gatewayQueriesThreadPoolQueueSizeTreeItem.setText(this.gatewayQueriesThreadPoolQueueSizeText + ": " + this.latestStats.gatewayQueriesThreadPoolQueueSize);
                  this.numGatewayQueriesReceivedPerSecondTreeItem.setText(this.numGatewayQueriesReceivedPerSecondText + ": " + this.latestStats.numGatewayQueriesReceivedPerSecond);
                  this.maxGatewayQueriesReceivedPerSecondTreeItem.setText(this.maxGatewayQueriesReceivedPerSecondText + ": " + this.latestStats.maxGatewayQueriesReceivedPerSecond);
                  this.numGatewayQueriesProcessedPerSecondTreeItem.setText(this.numGatewayQueriesProcessedPerSecondText + ": " + this.latestStats.numGatewayQueriesProcessedPerSecond);
                  this.maxGatewayQueriesProcessedPerSecondTreeItem.setText(this.maxGatewayQueriesProcessedPerSecondText + ": " + this.latestStats.maxGatewayQueriesProcessedPerSecond);
                  this.numGatewayQueriesDiscardedPerSecondTreeItem.setText(this.numGatewayQueriesDiscardedPerSecondText + ": " + this.latestStats.numGatewayQueriesDiscardedPerSecond);
                  this.maxGatewayQueriesDiscardedPerSecondTreeItem.setText(this.maxGatewayQueriesDiscardedPerSecondText + ": " + this.latestStats.maxGatewayQueriesDiscardedPerSecond);
                  this.jvmMemoryUsageTreeItem.setText(this.jvmMemoryUsageText + ": " + this.toMegaBytes(this.latestStats.jvmTotalMemory) + " allocated; " + this.toMegaBytes(this.latestStats.jvmTotalMemory - this.latestStats.jvmFreeMemory) + " used");
                  this.uptimeTreeItem.setText(this.uptimeText + ": " + this.toNiceDuration(this.latestStats.uptime));
               }
            } catch (Exception var6) {
               System.err.println("WARNING: Unable to save stats for the EmailAlert on " + this.hostName);
               var6.printStackTrace();
            }

         } finally {
            ;
         }
      }
   }
}
