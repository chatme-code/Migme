package com.projectgoth.fusion.monitor;

import Ice.LocalException;
import Ice.ObjectPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.SMSEngineAdminPrx;
import com.projectgoth.fusion.slice.SMSEngineAdminPrxHelper;
import com.projectgoth.fusion.slice.SMSEngineStats;
import org.eclipse.swt.widgets.TreeItem;

public class SMSEngineMonitor extends BaseStatsMonitor {
   public SMSEngineStats latestStats = null;
   private Exception latestException = null;
   private boolean latestStatsLoaded = false;
   private SMSEngineAdminPrx smsEngineAdminPrx;
   private TreeItem requestsReceivedTreeItem;
   private String requestsReceivedText = "Requests Received";
   private TreeItem requestsDispatchedTreeItem;
   private String requestsDispatchedText = "Requests Dispatched";
   private TreeItem requestsPerSecondTreeItem;
   private String requestsPerSecondText = "Requests Per Second";
   private TreeItem maxRequestsPerSecondTreeItem;
   private String maxRequestsPerSecondText = "Max. Requests Per Second";

   public SMSEngineMonitor(String hostName, Integer port, TreeItem parentTreeItem) {
      super(hostName, parentTreeItem);
      this.requestsReceivedTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.requestsReceivedTreeItem.setText(this.requestsReceivedText + ":");
      this.requestsDispatchedTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.requestsDispatchedTreeItem.setText(this.requestsDispatchedText + ":");
      this.requestsPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.requestsPerSecondTreeItem.setText(this.requestsPerSecondText + ":");
      this.maxRequestsPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.maxRequestsPerSecondTreeItem.setText(this.maxRequestsPerSecondText + ":");
      String stringifiedProxy = "SMSEngineAdmin:tcp -h " + hostName + " -p " + (port == null ? "9996" : port);
      ObjectPrx basePrx = Monitor.communicator().stringToProxy(stringifiedProxy);
      this.smsEngineAdminPrx = SMSEngineAdminPrxHelper.uncheckedCast(basePrx);
   }

   public void getStats() {
      try {
         this.latestStats = this.smsEngineAdminPrx.getStats();
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
            this.updateWithLatestStats(this.latestStats, this.isOnline);
            this.sendAlert("Connection to SMSEngine on " + this.hostName + " failed");
         }

         try {
            try {
               if (this.latestStats != null) {
                  if (!this.isOnline) {
                     this.isOnline = true;
                     this.sendAlert("Connection to SMSEngine on " + this.hostName + " restored");
                  }

                  this.updateWithLatestStats(this.latestStats, this.isOnline);
                  this.requestsReceivedTreeItem.setText(this.requestsReceivedText + ": " + this.latestStats.requestsReceived);
                  this.requestsDispatchedTreeItem.setText(this.requestsDispatchedText + ": " + this.latestStats.requestsDispatched);
                  this.requestsPerSecondTreeItem.setText(this.requestsPerSecondText + ": " + this.latestStats.requestsPerSecond);
                  this.maxRequestsPerSecondTreeItem.setText(this.maxRequestsPerSecondText + ": " + this.latestStats.maxRequestsPerSecond);
               }
            } catch (Exception var6) {
               System.err.println("WARNING: Unable to save stats for the SMSEngine on " + this.hostName);
               var6.printStackTrace();
            }

         } finally {
            ;
         }
      }
   }
}
