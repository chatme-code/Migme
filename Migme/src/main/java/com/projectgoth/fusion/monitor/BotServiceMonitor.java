package com.projectgoth.fusion.monitor;

import Ice.LocalException;
import Ice.ObjectPrx;
import com.projectgoth.fusion.slice.BotServiceAdminPrx;
import com.projectgoth.fusion.slice.BotServiceAdminPrxHelper;
import com.projectgoth.fusion.slice.BotServiceStats;
import com.projectgoth.fusion.slice.FusionException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

public class BotServiceMonitor extends BaseStatsMonitor {
   private BotServiceStats latestStats = null;
   private Exception latestException = null;
   private boolean latestStatsLoaded = false;
   private BotServiceAdminPrx botServiceAdminPrx;
   private TreeItem numBotObjectsTreeItem;
   private String numBotObjectsText = "No. Bot Objects";
   private TreeItem maxBotObjectsTreeItem;
   private String maxBotObjectsText = "Max. Bot Objects";
   private TreeItem numChannelBotsMappingsTreeItem;
   private String numChannelBotsMappingsText = "No. Channel-Bots Mappings";
   private TreeItem maxChannelBotsMappingsTreeItem;
   private String maxChannelBotsMappingsText = "Max. Channel-Bots Mappings";
   private TreeItem requestsPerSecondTreeItem;
   private String requestsPerSecondText = "Requests Per Second";
   private TreeItem maxRequestsPerSecondTreeItem;
   private String maxRequestsPerSecondText = "Max. Requests Per Second";
   private TreeItem threadPoolTreeItem;
   protected String threadPoolText = "Thread Pool";

   public BotServiceMonitor(String hostName, Integer port, TreeItem parentTreeItem) {
      super(hostName, parentTreeItem);
      this.numBotObjectsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.numBotObjectsTreeItem.setText(this.numBotObjectsText + ":");
      this.maxBotObjectsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.maxBotObjectsTreeItem.setText(this.maxBotObjectsText + ":");
      this.numChannelBotsMappingsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.numChannelBotsMappingsTreeItem.setText(this.numChannelBotsMappingsText + ":");
      this.maxChannelBotsMappingsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.maxChannelBotsMappingsTreeItem.setText(this.maxChannelBotsMappingsText + ":");
      this.requestsPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.requestsPerSecondTreeItem.setText(this.requestsPerSecondText + ":");
      this.maxRequestsPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.maxRequestsPerSecondTreeItem.setText(this.maxRequestsPerSecondText + ":");
      this.threadPoolTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.threadPoolTreeItem.setText(this.threadPoolText + ":");
      String stringifiedProxy = "BotServiceAdmin:tcp -h " + hostName + " -p " + (port == null ? "5992" : port);
      ObjectPrx basePrx = Monitor.communicator().stringToProxy(stringifiedProxy);
      this.botServiceAdminPrx = BotServiceAdminPrxHelper.uncheckedCast(basePrx);
   }

   public void getStats() {
      try {
         this.latestStats = this.botServiceAdminPrx.getStats();
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
            this.baseTreeItem.setText(this.hostName + " OFFLINE");
            this.baseTreeItem.setForeground(new Color(Display.getCurrent(), 192, 0, 0));
            this.sendAlert("Connection to Bot Service on " + this.hostName + " failed");
         }

         try {
            if (this.latestStats != null) {
               if (!this.isOnline) {
                  this.isOnline = true;
                  this.sendAlert("Connection to Bot Service on " + this.hostName + " restored");
               }

               this.updateWithLatestStats(this.latestStats, this.isOnline);
               this.baseTreeItem.setText(this.hostName + " ONLINE " + this.latestStats.version);
               this.numBotObjectsTreeItem.setText(this.numBotObjectsText + ": " + this.latestStats.numBotObjects);
               this.maxBotObjectsTreeItem.setText(this.maxBotObjectsText + ": " + this.latestStats.maxBotObjects);
               this.numChannelBotsMappingsTreeItem.setText(this.numChannelBotsMappingsText + ": " + this.latestStats.numBotChannelObjects);
               this.maxChannelBotsMappingsTreeItem.setText(this.maxChannelBotsMappingsText + ": " + this.latestStats.maxBotChannelObjects);
               this.requestsPerSecondTreeItem.setText(this.requestsPerSecondText + ": " + this.latestStats.requestsPerSecond);
               this.maxRequestsPerSecondTreeItem.setText(this.maxRequestsPerSecondText + ": " + this.latestStats.maxRequestsPerSecond);
               this.threadPoolTreeItem.setText(this.threadPoolText + ": " + this.latestStats.threadPoolSize + "/" + this.latestStats.maxThreadPoolSize + " threads active. " + this.latestStats.threadPoolQueueSize + " in queue");
            }
         } catch (Exception var2) {
            System.err.println("WARNING: Unable to save stats for Bot Service on " + this.hostName);
            var2.printStackTrace();
         }

      }
   }
}
