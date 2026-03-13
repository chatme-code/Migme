package com.projectgoth.fusion.monitor;

import Ice.ObjectPrx;
import com.projectgoth.fusion.slice.EventQueueWorkerServiceAdminPrx;
import com.projectgoth.fusion.slice.EventQueueWorkerServiceAdminPrxHelper;
import com.projectgoth.fusion.slice.EventQueueWorkerServiceStats;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import org.eclipse.swt.widgets.TreeItem;

public class EventQueueMonitor extends BaseStatsMonitor {
   private EventQueueWorkerServiceStats latestStats = null;
   private Exception latestException = null;
   private boolean latestStatsLoaded = false;
   private EventQueueWorkerServiceAdminPrx adminPrx = null;
   private static final String FRIENDS_ADDED = "Friends Added";
   private static final String VG_SENT = "Virtual Gifts Sent";
   protected LinkedHashMap<String, TreeItem> eventsTreeItems;
   protected TreeItem VirtualGiftSentTreeItem;
   protected TreeItem eventsProcessedTreeItem;
   protected TreeItem queueSizeTreeItem;

   public EventQueueMonitor(String hostName, Integer port, TreeItem parentTreeItem) {
      super(hostName, parentTreeItem);
      this.eventsProcessedTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.queueSizeTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.eventsTreeItems = new LinkedHashMap();
      this.eventsTreeItems.put("Friends Added", new TreeItem(this.baseTreeItem, 0));
      this.eventsTreeItems.put("Virtual Gifts Sent", new TreeItem(this.baseTreeItem, 0));
      Iterator i$ = this.eventsTreeItems.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, TreeItem> entry = (Entry)i$.next();
         TreeItem t = (TreeItem)entry.getValue();
         String key = (String)entry.getKey();
         t.setText(String.format(" %s events processed: - current rate: - peak rate -", key));
      }

      this.eventsProcessedTreeItem.setText("Total Events processed: - current rate: - peak rate: -");
      this.queueSizeTreeItem.setText("Queue Size: - max: - ");
      if (port != null && port > 0) {
         this.port = port;
      } else {
         this.port = 7903;
      }

      String stringifiedProxy = "EventQueueWorkerAdmin:tcp -h " + hostName + " -p " + port;
      System.out.println("Obtaining EventQueueWorkerServiceAdminPrx via " + stringifiedProxy);
      ObjectPrx basePrx = Monitor.communicator().stringToProxy(stringifiedProxy);
      this.adminPrx = EventQueueWorkerServiceAdminPrxHelper.uncheckedCast(basePrx);
      if (this.adminPrx == null) {
         System.out.println("Obtaining EventQueueWorkerServiceAdminPrx via " + stringifiedProxy + "[FAILED]");
      }

   }

   public void run() {
      if (!this.latestStatsLoaded) {
         this.getStats();
      }

      this.latestStatsLoaded = false;
      if (this.latestException != null) {
         this.isOnline = false;
         this.updateWithLatestStats(this.latestStats, this.isOnline);
         this.sendAlert("Connection to EventQueueWorkerAdmin on " + this.hostName + " failed");
      }

      try {
         if (this.latestStats != null) {
            if (!this.isOnline) {
               this.isOnline = true;
               this.sendAlert("Connection to EventQueueWorkerAdmin on " + this.hostName + " restored");
            }

            this.updateWithLatestStats(this.latestStats, this.isOnline);
            this.eventsProcessedTreeItem.setText("Total Events Processed: " + millionFormatter.format(this.latestStats.eventsProcessed) + " current rate: " + millionFormatter.format((long)this.latestStats.currentEventsProcessedRate) + " peak rate: " + millionFormatter.format((long)this.latestStats.peakEventsProcessedRate));
            this.queueSizeTreeItem.setText("Queue Size: " + millionFormatter.format(this.latestStats.queueSize) + " max: " + millionFormatter.format(this.latestStats.maxQueueSize));

            TreeItem t;
            String key;
            String totalEvents;
            String currentRate;
            String peakRate;
            for(Iterator i$ = this.eventsTreeItems.entrySet().iterator(); i$.hasNext(); t.setText(String.format(" %s events received: %s current rate: %s peak rate: %s", key, totalEvents, currentRate, peakRate))) {
               Entry<String, TreeItem> entry = (Entry)i$.next();
               t = (TreeItem)entry.getValue();
               key = (String)entry.getKey();
               totalEvents = "-";
               currentRate = "-";
               peakRate = "-";
               if (key.equals("Friends Added")) {
                  totalEvents = millionFormatter.format(this.latestStats.friendsAddedEvents);
                  currentRate = millionFormatter.format((long)this.latestStats.currentFriendsAddedEventRate);
                  peakRate = millionFormatter.format((long)this.latestStats.peakFriendsAddedEventRate);
               } else if (key.equals("Virtual Gifts Sent")) {
                  totalEvents = millionFormatter.format(this.latestStats.virtualGiftSentEvents);
                  currentRate = millionFormatter.format((long)this.latestStats.currentVirtualGiftEventRate);
                  peakRate = millionFormatter.format((long)this.latestStats.peakVirtualGiftEventRate);
               }
            }
         }
      } catch (Exception var8) {
         System.err.println("WARNING: Unable to save stats for the EventQueueWorkerAdmin on " + this.hostName);
         var8.printStackTrace();
      }

   }

   public void getStats() {
      try {
         if (this.adminPrx != null) {
            this.latestStats = this.adminPrx.getStats();
            this.latestException = null;
         } else {
            System.err.println("WARNING: Unable to  retrieve EventQueueWorkerServiceAdminPrx for " + this.hostName);
         }
      } catch (Exception var2) {
         this.latestStats = null;
         this.latestException = var2;
         System.err.println("WARNING: Unable to save stats for the EventQueueWorkerAdmin on " + this.hostName + ": " + var2.getMessage());
         var2.printStackTrace();
      }

      this.latestStatsLoaded = true;
   }
}
