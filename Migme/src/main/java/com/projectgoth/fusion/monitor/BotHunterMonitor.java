package com.projectgoth.fusion.monitor;

import Ice.ObjectPrx;
import com.projectgoth.fusion.slice.BotHunterAdminPrx;
import com.projectgoth.fusion.slice.BotHunterAdminPrxHelper;
import com.projectgoth.fusion.slice.BotHunterStats;
import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.swt.widgets.TreeItem;

public class BotHunterMonitor extends BaseStatsMonitor {
   public BotHunterStats latestStats = null;
   private Exception latestException = null;
   private boolean latestStatsLoaded = false;
   private BotHunterAdminPrx botHunterAdminPrx;
   private int port = 0;
   private ArrayList<BotHunterMonitor.TreeItemHolder> allItems = new ArrayList();
   private String offlinePrompt;
   private String onlinePrompt;
   private BotHunterMonitor.TreeItemHolder statsIntervalSecondsItem;
   private BotHunterMonitor.TreeItemHolder packetsCapturedItem;
   private BotHunterMonitor.TreeItemHolder averageProcessingTimeItem;
   private BotHunterMonitor.TreeItemHolder packetsPerSecondItem;
   private BotHunterMonitor.TreeItemHolder ipsCachedItem;
   private BotHunterMonitor.TreeItemHolder portsCachedItem;
   private BotHunterMonitor.TreeItemHolder packetsCachedItem;
   private BotHunterMonitor.TreeItemHolder totalSequencePairsAnalyzedItem;
   private BotHunterMonitor.TreeItemHolder averageSequenceTransitionsItem;
   private BotHunterMonitor.TreeItemHolder sequenceSuspectPairsItem;
   private BotHunterMonitor.TreeItemHolder ratioSuspectPairsItem;
   private BotHunterMonitor.TreeItemHolder suspectIPsReportedItem;
   private BotHunterMonitor.TreeItemHolder suspectPortsReportedItem;
   private String serverType = "";

   public BotHunterMonitor(String hostName, Integer port, TreeItem parentTreeItem) {
      super(hostName, parentTreeItem);
      this.offlinePrompt = hostName + " (" + this.serverType + ":" + port + ") OFFLINE";
      this.onlinePrompt = hostName + " (" + this.serverType + ":" + port + ") ONLINE";
      this.statsIntervalSecondsItem = this.createItem("Stats collection interval/seconds: ");
      this.packetsCapturedItem = this.createItem("Packets captured: ");
      this.averageProcessingTimeItem = this.createItem("Average processing time/packet (microsecs): ");
      this.packetsPerSecondItem = this.createItem("Packets per second: ");
      this.ipsCachedItem = this.createItem("IPs cached: ");
      this.portsCachedItem = this.createItem("Ports cached: ");
      this.packetsCachedItem = this.createItem("Packets cached: ");
      this.totalSequencePairsAnalyzedItem = this.createItem("Total seq pairs analyzed: ");
      this.averageSequenceTransitionsItem = this.createItem("Average transitions/sequence analysis: ");
      this.sequenceSuspectPairsItem = this.createItem("Suspect socket pairs by sequence analysis: ");
      this.ratioSuspectPairsItem = this.createItem("Suspect socket pairs by ratio analysis (may undercount): ");
      this.suspectIPsReportedItem = this.createItem("Total suspects reported by IP: ");
      this.suspectPortsReportedItem = this.createItem("Total suspects reported by client port: ");
      String stringifiedProxy = "BotHunterAdmin:tcp -h " + hostName + " -p " + (port == null ? "9998" : port);
      ObjectPrx basePrx = Monitor.communicator().stringToProxy(stringifiedProxy);
      this.botHunterAdminPrx = BotHunterAdminPrxHelper.uncheckedCast(basePrx);
   }

   private BotHunterMonitor.TreeItemHolder createItem(String prompt) {
      BotHunterMonitor.TreeItemHolder t = new BotHunterMonitor.TreeItemHolder(this.baseTreeItem, 0, prompt);
      this.allItems.add(t);
      return t;
   }

   public void getStats() {
      try {
         this.latestStats = this.botHunterAdminPrx.getStats();
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
      if (this.latestStats == null) {
         Iterator i$ = this.allItems.iterator();

         while(i$.hasNext()) {
            BotHunterMonitor.TreeItemHolder item = (BotHunterMonitor.TreeItemHolder)i$.next();
            item.offline();
         }
      } else {
         this.statsIntervalSecondsItem.update((Number)this.latestStats.statsIntervalSeconds);
         this.packetsCapturedItem.update((Number)this.latestStats.packetsCaptured);
         this.averageProcessingTimeItem.update((Number)this.latestStats.averageProcessingTimePerPacketMicrosec);
         this.packetsPerSecondItem.update((Number)this.latestStats.packetsPerSecond);
         this.ipsCachedItem.update((Number)this.latestStats.ipsCached);
         this.portsCachedItem.update((Number)this.latestStats.portsCached);
         this.packetsCachedItem.update((Number)this.latestStats.packetsCached);
         double averageSeqTransitions = (double)this.latestStats.totalSequenceTransitions / (double)this.latestStats.totalSequencePairsAnalyzed;
         this.totalSequencePairsAnalyzedItem.update((Number)this.latestStats.totalSequencePairsAnalyzed);
         this.averageSequenceTransitionsItem.update((Number)averageSeqTransitions);
         this.sequenceSuspectPairsItem.update((Number)this.latestStats.sequenceSuspectPairs);
         this.ratioSuspectPairsItem.update((Number)this.latestStats.ratioSuspectPairs);
         this.suspectIPsReportedItem.update((Number)this.latestStats.suspectIPsReported);
         this.suspectPortsReportedItem.update((Number)this.latestStats.suspectPortsReported);
      }

      this.latestStatsLoaded = false;
   }

   private class TreeItemHolder {
      private String prompt = "";
      private TreeItem ti;

      public TreeItemHolder(TreeItem parentItem, int style, String prompt) {
         this.prompt = prompt;
         this.ti = new TreeItem(parentItem, style);
         this.ti.setText(prompt);
      }

      public void update(String s) {
         this.ti.setText(this.prompt + s);
      }

      public void update(Number value) {
         this.update(value.toString());
      }

      public void offline() {
         this.update("Offline");
      }
   }
}
