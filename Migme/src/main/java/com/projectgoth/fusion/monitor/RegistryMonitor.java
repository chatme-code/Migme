package com.projectgoth.fusion.monitor;

import Ice.LocalException;
import Ice.ObjectPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.RegistryAdminPrx;
import com.projectgoth.fusion.slice.RegistryAdminPrxHelper;
import com.projectgoth.fusion.slice.RegistryStats;
import org.eclipse.swt.widgets.TreeItem;

public class RegistryMonitor extends BaseStatsMonitor {
   public RegistryStats latestStats = null;
   private Exception latestException = null;
   private boolean latestStatsLoaded = false;
   private RegistryAdminPrx registryAdminPrx;
   private TreeItem numUserProxiesTreeItem;
   private String numUserProxiesText = "No. User Proxies";
   private TreeItem maxUserProxiesTreeItem;
   private String maxUserProxiesText = "Max. User Proxies";
   private TreeItem numConnectionProxiesTreeItem;
   private String numConnectionProxiesText = "No. Connection Proxies";
   private TreeItem maxConnectionProxiesTreeItem;
   private String maxConnectionProxiesText = "Max. Connection Proxies";
   private TreeItem numChatRoomProxiesTreeItem;
   private String numChatRoomProxiesText = "No. ChatRoom Proxies";
   private TreeItem maxChatRoomProxiesTreeItem;
   private String maxChatRoomProxiesText = "Max. ChatRoom Proxies";
   private TreeItem objectCachesTreeItem;
   private String objectCachesText = "Object Caches";
   private TreeItem otherRegistriesTreeItem;
   private String otherRegistriesText = "Other Registries";
   private TreeItem requestsPerSecondTreeItem;
   private String requestsPerSecondText = "Requests Per Second";
   private TreeItem maxRequestsPerSecondTreeItem;
   private String maxRequestsPerSecondText = "Max. Requests Per Second";

   public RegistryMonitor(String hostName, Integer port, TreeItem parentTreeItem) {
      super(hostName, parentTreeItem);
      this.numUserProxiesTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.numUserProxiesTreeItem.setText(this.numUserProxiesText + ":");
      this.maxUserProxiesTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.maxUserProxiesTreeItem.setText(this.maxUserProxiesText + ":");
      this.numConnectionProxiesTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.numConnectionProxiesTreeItem.setText(this.numConnectionProxiesText + ":");
      this.maxConnectionProxiesTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.maxConnectionProxiesTreeItem.setText(this.maxConnectionProxiesText + ":");
      this.numChatRoomProxiesTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.numChatRoomProxiesTreeItem.setText(this.numChatRoomProxiesText + ":");
      this.maxChatRoomProxiesTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.maxChatRoomProxiesTreeItem.setText(this.maxChatRoomProxiesText + ":");
      this.objectCachesTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.objectCachesTreeItem.setText(this.objectCachesText + ":");
      this.otherRegistriesTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.otherRegistriesTreeItem.setText(this.otherRegistriesText + ":");
      this.requestsPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.requestsPerSecondTreeItem.setText(this.requestsPerSecondText + ":");
      this.maxRequestsPerSecondTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.maxRequestsPerSecondTreeItem.setText(this.maxRequestsPerSecondText + ":");
      String stringifiedProxy = "RegistryAdmin:tcp -h " + hostName + " -p " + (port == null ? "12000" : port);
      ObjectPrx basePrx = Monitor.communicator().stringToProxy(stringifiedProxy);
      this.registryAdminPrx = RegistryAdminPrxHelper.uncheckedCast(basePrx);
   }

   public void getStats() {
      try {
         this.latestStats = this.registryAdminPrx.getStats();
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
            this.sendAlert("Connection to Registry on " + this.hostName + " failed");
         }

         try {
            try {
               if (this.latestStats != null) {
                  if (!this.isOnline) {
                     this.isOnline = true;
                     this.sendAlert("Connection to Registry on " + this.hostName + " restored");
                  }

                  this.updateWithLatestStats(this.latestStats, this.isOnline);
                  this.numUserProxiesTreeItem.setText(this.numUserProxiesText + ": " + this.latestStats.numUserProxies);
                  this.maxUserProxiesTreeItem.setText(this.maxUserProxiesText + ": " + this.latestStats.maxUserProxies);
                  this.numConnectionProxiesTreeItem.setText(this.numConnectionProxiesText + ": " + this.latestStats.numConnectionProxies);
                  this.maxConnectionProxiesTreeItem.setText(this.maxConnectionProxiesText + ": " + this.latestStats.maxConnectionProxies);
                  this.numChatRoomProxiesTreeItem.setText(this.numChatRoomProxiesText + ": " + this.latestStats.numChatRoomProxies);
                  this.maxChatRoomProxiesTreeItem.setText(this.maxChatRoomProxiesText + ": " + this.latestStats.maxChatRoomProxies);
                  this.objectCachesTreeItem.setText(this.objectCachesText + ": " + this.latestStats.objectCaches);
                  this.otherRegistriesTreeItem.setText(this.otherRegistriesText + ": " + this.latestStats.otherRegistries);
                  this.requestsPerSecondTreeItem.setText(this.requestsPerSecondText + ": " + this.latestStats.requestsPerSecond);
                  this.maxRequestsPerSecondTreeItem.setText(this.maxRequestsPerSecondText + ": " + this.latestStats.maxRequestsPerSecond);
               }
            } catch (Exception var6) {
               System.err.println("WARNING: Unable to save stats for the Registry on " + this.hostName);
               var6.printStackTrace();
            }

         } finally {
            ;
         }
      }
   }
}
