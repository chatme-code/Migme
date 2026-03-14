package com.projectgoth.fusion.monitor;

import Ice.LocalException;
import Ice.ObjectPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GatewayAdminPrx;
import com.projectgoth.fusion.slice.GatewayAdminPrxHelper;
import com.projectgoth.fusion.slice.GatewayStats;
import com.projectgoth.fusion.slice.GatewayThreadPoolStats;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

public class GatewayMonitor extends BaseStatsMonitor {
   public GatewayStats latestStats = null;
   private Exception latestException = null;
   private boolean latestStatsLoaded = false;
   private GatewayAdminPrx gatewayAdminPrx;
   private int port = 0;
   private TreeItemAndPrompt numConnectionObjectsTreeItem;
   private String maxConnectionObjectsText = "(Max: ";
   private TreeItemAndPrompt requestsPerSecondTreeItem;
   private String maxRequestsPerSecondText = "(Max: ";
   protected static final String threadPoolText = "Thread Pools";
   protected TreeItem threadPoolTreeItem;
   private TreeItemAndPrompt tooBusyTreeItem;
   private static final String timesTooBusyText = "Times Too Busy";
   private static final String lastTimeTooBusyText = "Last Time Too Busy";
   private TreeItemAndPrompt timesTooBusyTreeItem;
   private TreeItemAndPrompt cxnsPerRemoteIPTreeItem;
   private TreeItemAndPrompt openUrlAttemptsTreeItem;
   private TreeItemAndPrompt openUrlFailuresTreeItem;
   private TreeItemAndPrompt openUrlFailurePercentTreeItem;
   private TreeItemAndPrompt averageSuccessfulProcessingTimeSecondsTreeItem;
   private TreeItemAndPrompt topFailuresByURLTreeItem;
   private String serverType;

   public GatewayMonitor(String hostName, Integer port, TreeItem parentTreeItem) {
      super(hostName, parentTreeItem);
      this.numConnectionObjectsTreeItem = new TreeItemAndPrompt(this.baseTreeItem, 0, "No. Connection Objects");
      this.requestsPerSecondTreeItem = new TreeItemAndPrompt(this.baseTreeItem, 0, "Requests Per Second");
      this.threadPoolTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.threadPoolTreeItem.setText(this.threadPoolTreeItem + ":");
      this.threadPoolTreeItem.setExpanded(true);
      this.tooBusyTreeItem = new TreeItemAndPrompt(this.baseTreeItem, 0, "Too Busy");
      this.timesTooBusyTreeItem = new TreeItemAndPrompt(this.baseTreeItem, 0, "Times Too Busy");
      this.cxnsPerRemoteIPTreeItem = new TreeItemAndPrompt(this.baseTreeItem, 0, "Connections per remote IP");
      this.openUrlAttemptsTreeItem = new TreeItemAndPrompt(this.baseTreeItem, 0, "OPEN_URL attempts");
      this.openUrlFailuresTreeItem = new TreeItemAndPrompt(this.baseTreeItem, 0, "OPEN_URL failures");
      this.openUrlFailurePercentTreeItem = new TreeItemAndPrompt(this.baseTreeItem, 0, "OPEN_URL failed %");
      this.averageSuccessfulProcessingTimeSecondsTreeItem = new TreeItemAndPrompt(this.baseTreeItem, 0, "OPEN_URL average successful processing time / seconds");
      this.topFailuresByURLTreeItem = new TreeItemAndPrompt(this.baseTreeItem, 0, "Top failures by URL");
      String stringifiedProxy = "GatewayAdmin:tcp -h " + hostName + " -p " + (port == null ? "9998" : port);
      ObjectPrx basePrx = Monitor.communicator().stringToProxy(stringifiedProxy);
      this.gatewayAdminPrx = GatewayAdminPrxHelper.uncheckedCast(basePrx);
   }

   public void getStats() {
      try {
         this.latestStats = this.gatewayAdminPrx.getStats();
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
            if (this.serverType != null) {
               this.baseTreeItem.setText(this.hostName + " (" + this.serverType + ":" + this.port + ") OFFLINE");
            } else {
               this.baseTreeItem.setText(this.hostName + " OFFLINE");
            }

            this.baseTreeItem.setForeground(new Color(Display.getCurrent(), 192, 0, 0));
            this.sendAlert("Connection to " + this.serverType + " Gateway on " + this.hostName + " failed");
         }

         Date lastTimeTooBusyDate = new Date();
         if (this.latestStats != null && this.latestStats.serverType != null) {
            this.serverType = this.latestStats.serverType;
            this.port = this.latestStats.port;
         }

         try {
            if (this.latestStats != null) {
               if (!this.isOnline) {
                  this.isOnline = true;
                  this.sendAlert("Connection to " + this.serverType + " Gateway on " + this.hostName + " restored");
               }

               this.updateWithLatestStats(this.latestStats, this.isOnline);
               this.baseTreeItem.setText(this.hostName + " (" + this.serverType + ":" + this.latestStats.port + ") ONLINE " + this.latestStats.version);
               if (this.latestStats.tooBusy) {
                  this.baseTreeItem.setForeground(new Color(Display.getCurrent(), 255, 110, 0));
               } else {
                  this.baseTreeItem.setForeground(new Color(Display.getCurrent(), 0, 160, 0));
               }

               this.numConnectionObjectsTreeItem.update(this.latestStats.numConnectionObjects + "   " + this.maxConnectionObjectsText + this.latestStats.maxConnectionObjects + ")");
               int numThreadPools = this.latestStats.threadPoolStats.length;
               int activeThreads = 0;
               int totalThreads = 0;
               int queueSize = 0;
               float requestsPerSecond = 0.0F;
               float maxRequestsPerSecond = 0.0F;
               int i;
               if (this.threadPoolTreeItem.getItemCount() != numThreadPools) {
                  this.threadPoolTreeItem.removeAll();

                  for(i = 0; i < numThreadPools; ++i) {
                     new TreeItem(this.threadPoolTreeItem, 0);
                  }
               }

               for(i = 0; i < numThreadPools; ++i) {
                  GatewayThreadPoolStats poolStats = this.latestStats.threadPoolStats[i];
                  this.threadPoolTreeItem.getItem(i).setText(poolStats.name + ": " + poolStats.threadPoolSize + "/" + poolStats.maxThreadPoolSize + " threads active. " + poolStats.threadPoolQueueSize + " in queue");
                  activeThreads += poolStats.threadPoolSize;
                  totalThreads += poolStats.maxThreadPoolSize;
                  queueSize += poolStats.threadPoolQueueSize;
                  requestsPerSecond += poolStats.requestsPerSecond;
                  maxRequestsPerSecond += poolStats.maxRequestsPerSecond;
               }

               this.requestsPerSecondTreeItem.update(requestsPerSecond + "   " + this.maxRequestsPerSecondText + maxRequestsPerSecond + ")");
               if (this.threadPoolTreeItem.getExpanded()) {
                  this.threadPoolTreeItem.setText("Thread Pools");
               } else {
                  this.threadPoolTreeItem.setText("Thread Pools: " + activeThreads + "/" + totalThreads + " threads active. " + queueSize + " in queue");
               }

               if (this.latestStats.tooBusy) {
                  this.tooBusyTreeItem.getTreeItem().setForeground(new Color(Display.getCurrent(), 192, 0, 0));
               } else {
                  this.tooBusyTreeItem.getTreeItem().setForeground(new Color(Display.getCurrent(), 0, 0, 0));
               }

               this.tooBusyTreeItem.update(this.latestStats.tooBusy);
               String timesTooBusyString = "" + this.latestStats.timesTooBusy;
               if (this.latestStats.timesTooBusy > 0) {
                  lastTimeTooBusyDate.setTime(this.latestStats.lastTimeTooBusy);
                  timesTooBusyString = timesTooBusyString + "   Last Time Too Busy: [" + lastTimeTooBusyDate + "]";
               }

               this.timesTooBusyTreeItem.update(timesTooBusyString);
               if (this.latestStats.connectionsPerRemoteIP != 0.0F) {
                  this.cxnsPerRemoteIPTreeItem.update((Number)this.latestStats.connectionsPerRemoteIP);
               } else {
                  this.cxnsPerRemoteIPTreeItem.update("");
               }

               if (this.latestStats.openUrlAttempts != 0) {
                  this.openUrlAttemptsTreeItem.update((Number)this.latestStats.openUrlAttempts);
                  this.openUrlFailuresTreeItem.update((Number)this.latestStats.openUrlFailures);
                  this.openUrlFailurePercentTreeItem.update((Number)this.latestStats.openUrlFailurePercent);
                  this.averageSuccessfulProcessingTimeSecondsTreeItem.update((Number)this.latestStats.averageSuccessfulProcessingTimeSeconds);
                  Map<String, Long> sorted = MonitorHelpers.sortDesc(this.latestStats.openUrlFailuresByUrl);
                  String topFailures = MonitorHelpers.countsMapToString(sorted, (long)this.latestStats.openUrlFailures, new DecimalFormat("#"));
                  this.topFailuresByURLTreeItem.update(topFailures);
               }
            }
         } catch (Exception var11) {
            System.err.println("WARNING: Unable to save stats for the " + this.serverType + " Gateway on " + this.hostName);
            var11.printStackTrace();
         }

      }
   }
}
