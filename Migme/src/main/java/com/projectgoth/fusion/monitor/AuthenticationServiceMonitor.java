package com.projectgoth.fusion.monitor;

import Ice.LocalException;
import Ice.ObjectPrx;
import com.projectgoth.fusion.common.PortRegistry;
import com.projectgoth.fusion.slice.AuthenticationServiceAdminPrx;
import com.projectgoth.fusion.slice.AuthenticationServiceAdminPrxHelper;
import com.projectgoth.fusion.slice.AuthenticationServiceStats;
import com.projectgoth.fusion.slice.FusionException;
import java.util.Date;
import org.eclipse.swt.widgets.TreeItem;

public class AuthenticationServiceMonitor extends BaseStatsMonitor {
   private AuthenticationServiceStats latestStats = null;
   private Exception latestException = null;
   private boolean latestStatsLoaded = false;
   private AuthenticationServiceAdminPrx authenticationServiceAdminPrx = null;
   protected TreeItem successesTreeItem;
   protected TreeItem failsTreeItem;

   public AuthenticationServiceMonitor(String hostName, Integer port, TreeItem parentTreeItem) {
      super(hostName, parentTreeItem);
      this.successesTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.successesTreeItem.setText("Successful Authentications: - Rate: - Max Rate: - Date of Max Rate: -");
      this.failsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.failsTreeItem.setText("Failed Authentications: - Rate: - Max Rate: - Date of Max Rate: -");
      if (port != null && port > 0) {
         this.port = port;
      } else {
         this.port = PortRegistry.AUTHENTICATION_SERVICE_ADMIN.getPort();
      }

   }

   private synchronized AuthenticationServiceAdminPrx getAuthenticationServiceAdminProxy() {
      if (this.authenticationServiceAdminPrx == null) {
         String stringifiedProxy = "AuthenticationServiceAdmin:tcp -h " + this.hostName + " -p " + this.port + " -t 2000";
         ObjectPrx basePrx = Monitor.communicator().stringToProxy(stringifiedProxy);
         this.authenticationServiceAdminPrx = AuthenticationServiceAdminPrxHelper.checkedCast(basePrx);
      }

      return this.authenticationServiceAdminPrx;
   }

   public void getStats() {
      try {
         AuthenticationServiceAdminPrx localAuthenticationServiceAdminPrx = this.getAuthenticationServiceAdminProxy();
         if (localAuthenticationServiceAdminPrx != null) {
            this.latestStats = localAuthenticationServiceAdminPrx.getStats();
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
            this.sendAlert("Connection to AuthenticationService on " + this.hostName + " failed");
         }

         try {
            try {
               if (this.latestStats != null) {
                  if (!this.isOnline) {
                     this.isOnline = true;
                     this.sendAlert("Connection to AuthenticationService on " + this.hostName + " restored");
                  }

                  this.updateWithLatestStats(this.latestStats, this.isOnline);
                  this.successesTreeItem.setText("Successful Authentications: " + millionFormatter.format(this.latestStats.successfulAuthentications) + " Rate: " + millionFormatter.format((long)this.latestStats.successfulAuthenticationRate) + " Max Rate: " + millionFormatter.format((long)this.latestStats.peakSuccessfulAuthenticationRate) + " Date of Max Rate: " + new Date(this.latestStats.peakSuccessfulAuthenticationRateDate));
                  this.failsTreeItem.setText("Failed Authentications: " + millionFormatter.format(this.latestStats.failedAuthentications) + " Rate: " + millionFormatter.format((long)this.latestStats.failedAuthenticationRate) + " Max Rate: " + millionFormatter.format((long)this.latestStats.peakFailedAuthenticationRate) + " Date of Max Rate: " + new Date(this.latestStats.peakFailedAuthenticationRateDate));
               }
            } catch (Exception var6) {
               System.err.println("WARNING: Unable to save stats for the AuthenticationService on " + this.hostName);
               var6.printStackTrace();
            }

         } finally {
            ;
         }
      }
   }
}
