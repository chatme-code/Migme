package com.projectgoth.fusion.monitor;

import Ice.LocalException;
import Ice.ObjectPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.SessionCacheAdminPrx;
import com.projectgoth.fusion.slice.SessionCacheAdminPrxHelper;
import com.projectgoth.fusion.slice.SessionCacheStats;
import java.util.Date;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

public class SessionCacheMonitor extends BaseStatsMonitor {
   public SessionCacheStats latestStats = null;
   private Exception latestException = null;
   private boolean latestStatsLoaded = false;
   private SessionCacheAdminPrx sessionCacheAdminPrx;
   private TreeItem numSessionsReceivedPerSecondTreeItem;
   private String numSessionsReceivedPerSecondText = "Sessions Received Per Second";
   private TreeItem maxSessionsReceivedPerSecondTreeItem;
   private String maxSessionsReceivedPerSecondText = "Max. Sessions Received Per Second";
   private TreeItem dateOfMaxSessionsReceivedPerSecondTreeItem;
   private String dateOfMaxSessionsReceivedPerSecondText = "Date of Max. Sessions Received Per Second";
   private TreeItem numSessionsQueuedTreeItem;
   private String numSessionsQueuedText = "Sessions Queued to be Archived";
   private TreeItem abortedSessionsQueuedTreeItem;
   private String abortedSessionsQueuedText = "Aborted Sessions: ";

   public SessionCacheMonitor(String hostName, Integer port, TreeItem parentTreeItem) {
      super(hostName, parentTreeItem);
      this.numSessionsReceivedPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.numSessionsReceivedPerSecondTreeItem.setText(this.numSessionsReceivedPerSecondText + ":");
      this.maxSessionsReceivedPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.maxSessionsReceivedPerSecondTreeItem.setText(this.maxSessionsReceivedPerSecondText + ":");
      this.dateOfMaxSessionsReceivedPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.dateOfMaxSessionsReceivedPerSecondTreeItem.setText(this.dateOfMaxSessionsReceivedPerSecondText + ":");
      this.numSessionsQueuedTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.numSessionsQueuedTreeItem.setText(this.numSessionsQueuedText + ":");
      this.abortedSessionsQueuedTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.abortedSessionsQueuedTreeItem.setText(this.abortedSessionsQueuedText);
      System.out.println("Connecting to " + hostName + ":" + port);
      String stringifiedProxy = "SessionCacheAdmin:tcp -h " + hostName + " -p " + (port == null ? "9023" : port);
      ObjectPrx basePrx = Monitor.communicator().stringToProxy(stringifiedProxy);
      this.sessionCacheAdminPrx = SessionCacheAdminPrxHelper.uncheckedCast(basePrx);
   }

   public void getStats() {
      try {
         this.latestStats = this.sessionCacheAdminPrx.getStats();
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
            this.sendAlert("Connection to MessageLogger on " + this.hostName + " failed");
         }

         try {
            try {
               if (this.latestStats != null) {
                  if (!this.isOnline) {
                     this.isOnline = true;
                     this.sendAlert("Connection to MessageLogger on " + this.hostName + " restored");
                  }

                  this.updateWithLatestStats(this.latestStats, this.isOnline);
                  if (this.latestStats.sessionsQueuedToBeArchived <= 0 && this.latestStats.abortedBatches <= 0) {
                     if (this.latestStats.uniqueSummariesTaskRunning) {
                        this.baseTreeItem.setForeground(new Color(Display.getCurrent(), 0, 0, 200));
                        this.baseTreeItem.setText(this.baseTreeItem.getText() + " DAILY PROCESS RUNNING");
                     } else {
                        this.baseTreeItem.setForeground(new Color(Display.getCurrent(), 0, 160, 0));
                     }
                  } else {
                     this.baseTreeItem.setForeground(new Color(Display.getCurrent(), 255, 110, 0));
                  }

                  this.numSessionsReceivedPerSecondTreeItem.setText(this.numSessionsReceivedPerSecondText + ": " + this.latestStats.sessionsReceivedPerSecond);
                  this.maxSessionsReceivedPerSecondTreeItem.setText(this.maxSessionsReceivedPerSecondText + ": " + this.latestStats.maxSessionsReceivedPerSecond);
                  this.dateOfMaxSessionsReceivedPerSecondTreeItem.setText(this.dateOfMaxSessionsReceivedPerSecondText + ": " + new Date(this.latestStats.dateOfMaxSessionsReceivedPerSecond));
                  if (this.latestStats.sessionsQueuedToBeArchived > 0) {
                     this.numSessionsQueuedTreeItem.setForeground(new Color(Display.getCurrent(), 192, 0, 0));
                  } else {
                     this.numSessionsQueuedTreeItem.setForeground(new Color(Display.getCurrent(), 0, 0, 0));
                  }

                  if (this.latestStats.abortedBatches > 0) {
                     this.abortedSessionsQueuedTreeItem.setForeground(new Color(Display.getCurrent(), 192, 0, 0));
                  } else {
                     this.abortedSessionsQueuedTreeItem.setForeground(new Color(Display.getCurrent(), 0, 0, 0));
                  }

                  this.numSessionsQueuedTreeItem.setText(this.numSessionsQueuedText + ": " + this.latestStats.sessionsQueuedToBeArchived);
                  this.abortedSessionsQueuedTreeItem.setText(this.abortedSessionsQueuedText + this.latestStats.abortedBatches);
               }
            } catch (Exception var6) {
               System.err.println("WARNING: Unable to save stats for the MessageLogger on " + this.hostName);
               var6.printStackTrace();
            }

         } finally {
            ;
         }
      }
   }
}
