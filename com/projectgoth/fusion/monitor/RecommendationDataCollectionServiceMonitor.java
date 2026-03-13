package com.projectgoth.fusion.monitor;

import Ice.ObjectPrx;
import com.projectgoth.fusion.slice.RecommendationDataCollectionServiceAdminPrx;
import com.projectgoth.fusion.slice.RecommendationDataCollectionServiceAdminPrxHelper;
import com.projectgoth.fusion.slice.RecommendationDataCollectionServiceStats;
import com.projectgoth.fusion.slice.ServiceStatsLongFieldValue;
import java.util.Iterator;
import org.eclipse.swt.widgets.TreeItem;

public class RecommendationDataCollectionServiceMonitor extends BaseStatsMonitor {
   private static final int DEFAULT_PORT = 24650;
   private static final String NEWLINE = "\n";
   private final String offlinePrompt;
   private final String onlinePrompt;
   private final RecommendationDataCollectionServiceAdminPrx rdcsAdminPrx;
   private final TreeItemAndPrompt totalReceivedDataCountItem;
   private final TreeItemAndPrompt totalReceivedDataCountByDataTypeItem;
   private final TreeItemAndPrompt totalSuccessfullyProcessedDataCountItem;
   private final TreeItemAndPrompt totalSuccessfullyProcessedDataCountByDataTypeItem;
   private final TreeItemAndPrompt totalFailedProcessedDataCountItem;
   private final TreeItemAndPrompt totalFailedProcessedDataCountByErrorCauseCodeItem;
   private String serverType = "";
   public RecommendationDataCollectionServiceStats latestStats = null;
   private Exception latestException = null;
   private boolean latestStatsLoaded = false;

   public RecommendationDataCollectionServiceMonitor(String hostName, Integer port, TreeItem parentTreeItem) {
      super(hostName, parentTreeItem);
      this.offlinePrompt = hostName + " (" + this.serverType + ":" + port + ") OFFLINE";
      this.onlinePrompt = hostName + " (" + this.serverType + ":" + port + ") ONLINE";
      this.totalReceivedDataCountItem = new TreeItemAndPrompt(this.baseTreeItem, 0, "Total received data count");
      this.totalReceivedDataCountByDataTypeItem = new TreeItemAndPrompt(this.baseTreeItem, 0, "Total received data count by type");
      this.totalSuccessfullyProcessedDataCountItem = new TreeItemAndPrompt(this.baseTreeItem, 0, "Total successfully processed data count");
      this.totalSuccessfullyProcessedDataCountByDataTypeItem = new TreeItemAndPrompt(this.baseTreeItem, 0, "Total successfully processed data count by type");
      this.totalFailedProcessedDataCountItem = new TreeItemAndPrompt(this.baseTreeItem, 0, "Total failed processed data count");
      this.totalFailedProcessedDataCountByErrorCauseCodeItem = new TreeItemAndPrompt(this.baseTreeItem, 0, "Total failed processed data count by error cause code");
      String stringifiedProxy = "RecommendationDataCollectionServiceAdmin:tcp -h " + hostName + " -p " + (port == null ? Integer.toString(24650) : port);
      ObjectPrx basePrx = Monitor.communicator().stringToProxy(stringifiedProxy);
      this.rdcsAdminPrx = RecommendationDataCollectionServiceAdminPrxHelper.uncheckedCast(basePrx);
   }

   public void getStats() {
      try {
         this.latestStats = this.rdcsAdminPrx.getStats();
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

      this.updateWithLatestStats(this.latestStats, this.latestStats != null);
      if (null != this.latestStats) {
         this.totalReceivedDataCountItem.update((Number)this.latestStats.totalReceivedDataCount.value);
         StringBuilder receivedBDT = new StringBuilder();
         Iterator i$ = this.latestStats.totalReceivedDataCountByDataType.keySet().iterator();

         while(i$.hasNext()) {
            int type = (Integer)i$.next();
            receivedBDT.append("\n" + type + ": " + ((ServiceStatsLongFieldValue)this.latestStats.totalReceivedDataCountByDataType.get(type)).value);
         }

         this.totalReceivedDataCountByDataTypeItem.update(receivedBDT.toString());
         this.totalSuccessfullyProcessedDataCountItem.update((Number)this.latestStats.totalSuccessfullyProcessedDataCount.value);
         StringBuilder procBDT = new StringBuilder();
         Iterator i$ = this.latestStats.totalSuccessfullyProcessedDataCountByDataType.keySet().iterator();

         while(i$.hasNext()) {
            int type = (Integer)i$.next();
            procBDT.append("\n" + type + ": " + ((ServiceStatsLongFieldValue)this.latestStats.totalSuccessfullyProcessedDataCountByDataType.get(type)).value);
         }

         this.totalSuccessfullyProcessedDataCountByDataTypeItem.update(procBDT.toString());
         this.totalFailedProcessedDataCountItem.update((Number)this.latestStats.totalFailedProcessedDataCount.value);
         StringBuilder failedBECC = new StringBuilder();
         Iterator i$ = this.latestStats.totalFailedProcessedDataCountByErrorCauseCode.keySet().iterator();

         while(i$.hasNext()) {
            String error = (String)i$.next();
            failedBECC.append("\n" + error + ": " + ((ServiceStatsLongFieldValue)this.latestStats.totalFailedProcessedDataCountByErrorCauseCode.get(error)).value);
         }

         this.totalFailedProcessedDataCountByErrorCauseCodeItem.update(failedBECC.toString());
      }

      this.latestStatsLoaded = false;
   }
}
