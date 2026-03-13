package com.projectgoth.fusion.monitor;

import Ice.LocalException;
import Ice.ObjectPrx;
import com.projectgoth.fusion.common.PortRegistry;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.ReputationServiceAdminPrx;
import com.projectgoth.fusion.slice.ReputationServiceAdminPrxHelper;
import com.projectgoth.fusion.slice.ReputationServiceStats;
import java.util.Date;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

public class ReputationServiceMonitor extends BaseStatsMonitor {
   private ReputationServiceStats latestStats = null;
   private Exception latestException = null;
   private boolean latestStatsLoaded = false;
   private ReputationServiceAdminPrx reputationServiceAdminPrx = null;
   protected TreeItem lastTimeRunCompletedTreeItem;

   public ReputationServiceMonitor(String hostName, Integer port, TreeItem parentTreeItem) {
      super(hostName, parentTreeItem);
      this.lastTimeRunCompletedTreeItem = new TreeItem(this.baseTreeItem, 0);
      if (port != null && port > 0) {
         this.port = port;
      } else {
         this.port = PortRegistry.REPUTATION_SERVICE_ADMIN.getPort();
      }

   }

   private synchronized ReputationServiceAdminPrx getReputationServiceAdminProxy() {
      if (this.reputationServiceAdminPrx == null) {
         String stringifiedProxy = "ReputationServiceAdmin:tcp -h " + this.hostName + " -p " + this.port + " -t 2000";
         ObjectPrx basePrx = Monitor.communicator().stringToProxy(stringifiedProxy);
         this.reputationServiceAdminPrx = ReputationServiceAdminPrxHelper.checkedCast(basePrx);
      }

      return this.reputationServiceAdminPrx;
   }

   public void getStats() {
      try {
         ReputationServiceAdminPrx localReputationServiceAdminPrx = this.getReputationServiceAdminProxy();
         if (localReputationServiceAdminPrx != null) {
            this.latestStats = localReputationServiceAdminPrx.getStats();
            this.latestException = null;
         }
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
            this.sendAlert("Connection to ReputationService on " + this.hostName + " failed");
         }

         try {
            try {
               if (this.latestStats != null) {
                  if (!this.isOnline) {
                     this.isOnline = true;
                     this.sendAlert("Connection to ReputationService on " + this.hostName + " restored");
                  }

                  this.updateWithLatestStats(this.latestStats, this.isOnline);
                  if (this.latestStats.processing) {
                     this.baseTreeItem.setForeground(new Color(Display.getCurrent(), 0, 0, 200));
                     this.baseTreeItem.setText(this.baseTreeItem.getText() + " DAILY PROCESS RUNNING");
                  } else if (System.currentTimeMillis() - this.latestStats.lastTimeRunCompleted > 172800000L) {
                     this.baseTreeItem.setForeground(new Color(Display.getCurrent(), 255, 110, 0));
                     this.baseTreeItem.setText(this.baseTreeItem.getText() + " (daily process haven't completed successfully in " + (System.currentTimeMillis() - this.latestStats.lastTimeRunCompleted) / 86400000L + " days)");
                  }

                  this.lastTimeRunCompletedTreeItem.setText("Last Time Run Completed Successfully: " + (this.latestStats.lastTimeRunCompleted == 0L ? "-" : new Date(this.latestStats.lastTimeRunCompleted)));
               }
            } catch (Exception var6) {
               System.err.println("WARNING: Unable to save stats for the ReputationService on " + this.hostName);
               var6.printStackTrace();
            }

         } finally {
            ;
         }
      }
   }
}
