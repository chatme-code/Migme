package com.projectgoth.fusion.monitor;

import Ice.LocalException;
import Ice.ObjectPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.ImageServerAdminPrx;
import com.projectgoth.fusion.slice.ImageServerAdminPrxHelper;
import com.projectgoth.fusion.slice.ImageServerStats;
import org.eclipse.swt.widgets.TreeItem;

public class ImageServerMonitor extends BaseStatsMonitor {
   public ImageServerStats latestStats = null;
   private Exception latestException = null;
   private boolean latestStatsLoaded = false;
   private ImageServerAdminPrx imageServerAdminPrx;
   private TreeItem portTreeItem;
   private String portText = "Port";
   private TreeItem requestsPerSecondTreeItem;
   private String requestsPerSecondText = "Requests Per Second";
   private TreeItem maxRequestsPerSecondTreeItem;
   private String maxRequestsPerSecondText = "Max. Requests Per Second";
   private TreeItem cacheInfoTreeItem;
   private String cacheInfoText = "Image Cache";
   private String threadPoolSizeText = "Thread Pool Size (Active Threads)";
   private TreeItem threadPoolSizeTreeItem;
   private String maxThreadPoolSizeText = "Max. Thread Pool Size";
   private TreeItem maxThreadPoolSizeTreeItem;
   private String threadPoolQueueSizeText = "Thread Pool Queue Size";
   private TreeItem threadPoolQueueSizeTreeItem;

   public ImageServerMonitor(String hostName, Integer port, TreeItem parentTreeItem) {
      super(hostName, parentTreeItem);
      this.portTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.portTreeItem.setText(this.portText + ":");
      this.requestsPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.requestsPerSecondTreeItem.setText(this.requestsPerSecondText + ":");
      this.maxRequestsPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.maxRequestsPerSecondTreeItem.setText(this.maxRequestsPerSecondText + ":");
      this.cacheInfoTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.cacheInfoTreeItem.setText(this.cacheInfoText + ":");
      this.threadPoolSizeTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.threadPoolSizeTreeItem.setText(this.threadPoolSizeText + ":");
      this.maxThreadPoolSizeTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.maxThreadPoolSizeTreeItem.setText(this.maxThreadPoolSizeText + ":");
      this.threadPoolQueueSizeTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.threadPoolQueueSizeTreeItem.setText(this.threadPoolQueueSizeText + ":");
      String stringifiedProxy = "ImageServerAdmin:tcp -h " + hostName + " -p " + (port == null ? "39998" : port);
      ObjectPrx basePrx = Monitor.communicator().stringToProxy(stringifiedProxy);
      this.imageServerAdminPrx = ImageServerAdminPrxHelper.uncheckedCast(basePrx);
   }

   public void getStats() {
      try {
         this.latestStats = this.imageServerAdminPrx.getStats();
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
            this.sendAlert("Connection to ImageServer on " + this.hostName + " failed");
         }

         try {
            try {
               if (this.latestStats != null) {
                  if (!this.isOnline) {
                     this.isOnline = true;
                     this.sendAlert("Connection to ImageServer on " + this.hostName + " restored");
                  }

                  this.updateWithLatestStats(this.latestStats, this.isOnline);
                  this.portTreeItem.setText(this.portText + ": " + this.latestStats.port);
                  this.requestsPerSecondTreeItem.setText(this.requestsPerSecondText + ": " + this.latestStats.requestsPerSecond);
                  this.maxRequestsPerSecondTreeItem.setText(this.maxRequestsPerSecondText + ": " + this.latestStats.maxRequestsPerSecond);
                  this.cacheInfoTreeItem.setText(this.cacheInfoText + ": " + this.latestStats.cacheInfo);
                  this.threadPoolSizeTreeItem.setText(this.threadPoolSizeText + ": " + this.latestStats.threadPoolSize);
                  this.maxThreadPoolSizeTreeItem.setText(this.maxThreadPoolSizeText + ": " + this.latestStats.maxThreadPoolSize);
                  this.threadPoolQueueSizeTreeItem.setText(this.threadPoolQueueSizeText + ": " + this.latestStats.threadPoolQueueSize);
               }
            } catch (Exception var5) {
               System.err.println("WARNING: Unable to save stats for the ImageServer on " + this.hostName);
               var5.printStackTrace();
            }

         } finally {
            ;
         }
      }
   }
}
