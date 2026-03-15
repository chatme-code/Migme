package com.projectgoth.fusion.monitor;

import com.projectgoth.fusion.slice.BaseServiceStats;
import java.text.DecimalFormat;
import java.util.Map;
import org.eclipse.swt.widgets.TreeItem;

public class IceStatsTreeItems {
   protected static final String NEWLINE = "\n";
   protected static final DecimalFormat DF = new DecimalFormat("#.##");
   protected TreeItem iceRequestStatsTreeItem;
   protected TreeItem iceRequestStatsEnabledTreeItem;
   protected TreeItem requestCountTreeItem;
   protected IceStatsTreeItems.PromptAndContentPair requestCountByOriginTreeItems;
   protected IceStatsTreeItems.PromptAndContentPair requestCountByOpTreeItems;
   protected IceStatsTreeItems.PromptAndContentPair requestMeanProcessingTimeByOpTreeItems;
   protected IceStatsTreeItems.PromptAndContentPair requestMaxProcessingTimeByOpTreeItems;
   protected IceStatsTreeItems.PromptAndContentPair requestStdevProcessingTimeByOpTreeItems;
   protected IceStatsTreeItems.PromptAndContentPair request95thPercentileProcessingTimeByOpTreeItems;
   protected IceStatsTreeItems.PromptAndContentPair requestTotalProcessingTimeByOpTreeItems;
   protected TreeItem iceThreadStatsTreeItem;
   protected TreeItem iceThreadStatsContentTreeItem;

   public IceStatsTreeItems(TreeItem baseTreeItem) {
      this.iceRequestStatsTreeItem = new TreeItem(baseTreeItem, 0);
      this.iceRequestStatsTreeItem.setText("Ice connection stats (seconds):");
      this.iceRequestStatsEnabledTreeItem = new TreeItem(this.iceRequestStatsTreeItem, 0);
      this.requestCountTreeItem = new TreeItem(this.iceRequestStatsTreeItem, 0);
      this.requestCountByOriginTreeItems = new IceStatsTreeItems.PromptAndContentPair(this.iceRequestStatsTreeItem, "Request count by origin:");
      this.requestCountByOpTreeItems = new IceStatsTreeItems.PromptAndContentPair(this.iceRequestStatsTreeItem, "Request count by operation:");
      this.requestMeanProcessingTimeByOpTreeItems = new IceStatsTreeItems.PromptAndContentPair(this.iceRequestStatsTreeItem, "Request mean processing time by operation:");
      this.requestMaxProcessingTimeByOpTreeItems = new IceStatsTreeItems.PromptAndContentPair(this.iceRequestStatsTreeItem, "Request max processing time by operation:");
      this.requestStdevProcessingTimeByOpTreeItems = new IceStatsTreeItems.PromptAndContentPair(this.iceRequestStatsTreeItem, "Request stdev in processing time by operation:");
      this.request95thPercentileProcessingTimeByOpTreeItems = new IceStatsTreeItems.PromptAndContentPair(this.iceRequestStatsTreeItem, "95th percentile value (est) for request processing time by operation:");
      this.requestTotalProcessingTimeByOpTreeItems = new IceStatsTreeItems.PromptAndContentPair(this.iceRequestStatsTreeItem, "Request total processing time by operation:");
      this.iceThreadStatsTreeItem = new TreeItem(baseTreeItem, 0);
      this.iceThreadStatsTreeItem.setText("Ice thread stats:");
      this.iceThreadStatsContentTreeItem = new TreeItem(this.iceThreadStatsTreeItem, 0);
   }

   public void update(BaseServiceStats stats) {
      this.updateRequestStats(stats);
      this.updateThreadStats(stats);
   }

   public void updateRequestStats(BaseServiceStats stats) {
      this.iceRequestStatsEnabledTreeItem.setText(stats.izeRequestStatsEnabled ? "ENABLED" : "DISABLED");
      if (stats.izeRequestStatsEnabled) {
         this.requestCountTreeItem.setText("Request count: " + stats.izeRequestCount);
         this.displaySortedCount(this.requestCountByOriginTreeItems, stats.izeRequestCountByOrigin, stats.izeRequestCount);
         this.displaySortedCount(this.requestCountByOpTreeItems, stats.izeRequestCountByOperation, stats.izeRequestCount);
         this.displaySortedCount(this.requestMeanProcessingTimeByOpTreeItems, stats.izeRequestMeanProcessingTimeByOperation);
         this.displaySortedCount(this.requestMaxProcessingTimeByOpTreeItems, stats.izeRequestMaxProcessingTimeByOperation);
         this.displaySortedCount(this.requestStdevProcessingTimeByOpTreeItems, stats.izeRequestStdevProcessingTimeByOperation);
         this.displaySortedCount(this.request95thPercentileProcessingTimeByOpTreeItems, stats.izeRequest95thPercentileProcessingTimeByOperation);
         this.displaySortedCount(this.requestTotalProcessingTimeByOpTreeItems, stats.izeRequestTotalProcessingTimeByOperation);
      }
   }

   public void updateThreadStats(BaseServiceStats stats) {
      if (stats.izeThreadStatsEnabled) {
         this.iceThreadStatsContentTreeItem.setText("ENABLED \nObjectAdapterThreadPool: \nrunning=" + stats.izeObjectAdapterThreadPoolRunning + "\n" + "inUse=" + stats.izeObjectAdapterThreadPoolInUse + "\n" + "inUse high watermark=" + stats.izeObjectAdapterThreadPoolInUseHighWatermark + "\n" + "load=" + DF.format(stats.izeObjectAdapterThreadPoolLoad) + "\n" + "Config params: " + "\n" + "size=" + stats.izeObjectAdapterThreadPoolSize + "\n" + "sizeMax=" + stats.izeObjectAdapterThreadPoolSizeMax + "\n" + "sizeWarn=" + stats.izeObjectAdapterThreadPoolSizeWarn + "\n" + "\n" + "AMD dispatch thread pool:" + "running=" + stats.amdObjectAdapterThreadPoolRunning + "\n" + "inUse=" + stats.amdObjectAdapterThreadPoolInUse + "\n" + "inUse high watermark=" + stats.amdObjectAdapterThreadPoolInUseHighWatermark + "\n" + "size=" + stats.amdObjectAdapterThreadPoolSize + "\n" + "sizeMax=" + stats.amdObjectAdapterThreadPoolSizeMax + "queue length=" + stats.amdObjectAdapterThreadPoolQueueLength);
         if (stats.izeClientThreadPoolStatsEnabled) {
            this.iceThreadStatsContentTreeItem.setText(this.iceThreadStatsContentTreeItem.getText() + "\n" + "\n" + "Ice client ThreadPool: \n" + "running=" + stats.izeClientThreadPoolRunning + "\n" + "inUse=" + stats.izeClientThreadPoolInUse + "\n" + "inUse high watermark=" + stats.izeClientThreadPoolInUseHighWatermark + "\n" + "load=" + DF.format(stats.izeClientThreadPoolLoad) + "\n" + "Config params: " + "\n" + "size=" + stats.izeClientThreadPoolSize + "\n" + "sizeMax=" + stats.izeClientThreadPoolSizeMax + "\n" + "sizeWarn=" + stats.izeClientThreadPoolSizeWarn + "\n");
         }
      } else {
         this.iceThreadStatsContentTreeItem.setText("DISABLED");
      }

   }

   private void displaySortedCount(IceStatsTreeItems.PromptAndContentPair treeItems, Map<String, ? extends Number> counts) {
      this.displaySortedCount(treeItems, counts, (Long)null);
   }

   private void displaySortedCount(IceStatsTreeItems.PromptAndContentPair treeItems, Map<String, ? extends Number> counts, Long total) {
      Map<String, Long> sorted = MonitorHelpers.sortDesc(counts);
      String display = MonitorHelpers.countsMapToString(sorted, total, DF);
      treeItems.setContent(display);
   }

   private class PromptAndContentPair {
      private TreeItem promptItem;
      private TreeItem contentItem;

      public PromptAndContentPair(TreeItem baseTreeItem, String prompt) {
         this.promptItem = new TreeItem(baseTreeItem, 0);
         this.promptItem.setText(prompt);
         this.contentItem = new TreeItem(this.promptItem, 0);
      }

      public void setContent(String s) {
         this.contentItem.setText(s);
      }
   }
}
