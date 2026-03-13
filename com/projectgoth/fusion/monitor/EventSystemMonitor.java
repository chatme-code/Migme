package com.projectgoth.fusion.monitor;

import Ice.LocalException;
import Ice.ObjectPrx;
import com.projectgoth.fusion.slice.EventSystemAdminPrx;
import com.projectgoth.fusion.slice.EventSystemAdminPrxHelper;
import com.projectgoth.fusion.slice.EventSystemStats;
import com.projectgoth.fusion.slice.FusionException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

public class EventSystemMonitor extends BaseStatsMonitor {
   public EventSystemStats latestStats = null;
   private Exception latestException = null;
   private boolean latestStatsLoaded = false;
   private EventSystemAdminPrx eventSystemAdminPrx = null;
   private int port = 21550;
   private TreeItem addedFriendEventsTreeItem;
   private String addedFriendEventsText = "AddedFriend events received: ";
   private TreeItem createdPublicChatrooomStatusEventsTreeItem;
   private String createdPublicChatrooomStatusEventsText = "CreatedPublicChatroom events received: ";
   private TreeItem madePhotoPublicEventsTreeItem;
   private String madePhotoPublicEventsText = "MadePhotoPublic events received: ";
   private TreeItem purchasedVirtualGoodsEventsTreeItem;
   private String purchasedVirtualGoodsEventsText = "PurchasedVirtualGoods events received: ";
   private TreeItem setProfileStatusEventsTreeItem;
   private String setProfileStatusEventsText = "SetProfileStatus events received: ";
   private TreeItem updatedProfileEventsTreeItem;
   private String updatedProfileEventsText = "UpdatedProfile events received: ";
   private TreeItem virtualGiftsEventsTreeItem;
   private String virtualGiftsEventsText = "VirtualGift events received: ";
   private TreeItem genericApplicationEventsTreeItem;
   private String genericApplicationEventsText = "Generic application events received: ";
   private TreeItem giftShowerEventTreeItem;
   private String giftShowerEventText = "GiftShower events received: ";
   private TreeItem totalEventsTreeItem;
   private String totalEventsText = "Total events received: ";
   private TreeItem droppedEventsTreeItem;
   private String droppedEventsText = "Events dropped: ";
   private TreeItem streamedEventsTreeItem;
   private String streamedEventsText = "Events Streamed: ";
   private TreeItem distributedEventsTreeItem;
   private String distributedEventsText = "Events Distributed: ";
   private String addedFriendRateText = " rate: ";
   private String createdPublicChatrooomStatusRateText = " rate: ";
   private String madePhotoPublicRateText = " rate: ";
   private String purchasedVirtualGoodsRateText = " rate: ";
   private String setProfileStatusRateText = " rate: ";
   private String updatedProfileRateText = " rate: ";
   private String virtualGiftsRateText = " rate: ";
   private String genericApplicationRateText = " rate: ";
   private String giftShowerEventRateText = " rate: ";
   private String totalRateText = " rate: ";
   private String droppedRateText = " rate: ";
   private String streamedRateText = " rate: ";
   private String distributedRateText = " rate: ";
   private String maxTotalRateText = " peak rate: ";
   private String maxDroppedRateText = " peak rate: ";
   private String maxStreamedRateText = " peak rate: ";
   private String maxDistributedRateText = " peak rate: ";

   public EventSystemMonitor(String hostName, Integer port, TreeItem parentTreeItem) {
      super(hostName, parentTreeItem);
      this.addedFriendEventsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.addedFriendEventsTreeItem.setText(this.addedFriendEventsText);
      this.createdPublicChatrooomStatusEventsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.createdPublicChatrooomStatusEventsTreeItem.setText(this.createdPublicChatrooomStatusEventsText);
      this.madePhotoPublicEventsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.madePhotoPublicEventsTreeItem.setText(this.madePhotoPublicEventsText);
      this.purchasedVirtualGoodsEventsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.purchasedVirtualGoodsEventsTreeItem.setText(this.purchasedVirtualGoodsEventsText);
      this.setProfileStatusEventsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.setProfileStatusEventsTreeItem.setText(this.setProfileStatusEventsText);
      this.updatedProfileEventsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.updatedProfileEventsTreeItem.setText(this.updatedProfileEventsText);
      this.virtualGiftsEventsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.virtualGiftsEventsTreeItem.setText(this.virtualGiftsEventsText);
      this.genericApplicationEventsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.genericApplicationEventsTreeItem.setText(this.genericApplicationEventsText);
      this.giftShowerEventTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.giftShowerEventTreeItem.setText(this.giftShowerEventText);
      this.totalEventsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.totalEventsTreeItem.setText(this.totalEventsText);
      this.droppedEventsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.droppedEventsTreeItem.setText(this.droppedEventsText);
      this.streamedEventsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.streamedEventsTreeItem.setText(this.streamedEventsText);
      this.distributedEventsTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.distributedEventsTreeItem.setText(this.distributedEventsText);
      this.port = port;
   }

   private synchronized EventSystemAdminPrx getEventSystemProxy() {
      if (this.eventSystemAdminPrx == null) {
         String stringifiedProxy = "EventSystemAdmin:tcp -h " + this.hostName + " -p " + this.port + " -t 2000";
         ObjectPrx basePrx = Monitor.communicator().stringToProxy(stringifiedProxy);
         this.eventSystemAdminPrx = EventSystemAdminPrxHelper.checkedCast(basePrx);
      }

      return this.eventSystemAdminPrx;
   }

   public void getStats() {
      try {
         EventSystemAdminPrx localEventSystemAdmPrx = this.getEventSystemProxy();
         if (localEventSystemAdmPrx != null) {
            this.latestStats = localEventSystemAdmPrx.getStats();
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
            this.sendAlert("Connection to EventSystem on " + this.hostName + " failed");
         }

         try {
            try {
               if (this.latestStats != null) {
                  if (!this.isOnline) {
                     this.isOnline = true;
                     this.sendAlert("Connection to MessageLogger on " + this.hostName + " restored");
                  }

                  this.addedFriendEventsTreeItem.setText(this.addedFriendEventsText + millionFormatter.format(this.latestStats.addedFriendEvents) + this.addedFriendRateText + millionFormatter.format((long)this.latestStats.addedFriendRate));
                  this.createdPublicChatrooomStatusEventsTreeItem.setText(this.createdPublicChatrooomStatusEventsText + millionFormatter.format(this.latestStats.createdPublicChatrooomStatusEvents) + this.createdPublicChatrooomStatusRateText + millionFormatter.format((long)this.latestStats.createdPublicChatrooomStatusRate));
                  this.madePhotoPublicEventsTreeItem.setText(this.madePhotoPublicEventsText + millionFormatter.format(this.latestStats.madePhotoPublicEvents) + this.madePhotoPublicRateText + millionFormatter.format((long)this.latestStats.madePhotoPublicRate));
                  this.purchasedVirtualGoodsEventsTreeItem.setText(this.purchasedVirtualGoodsEventsText + millionFormatter.format(this.latestStats.purchasedVirtualGoodsEvents) + this.purchasedVirtualGoodsRateText + millionFormatter.format((long)this.latestStats.purchasedVirtualGoodsRate));
                  this.setProfileStatusEventsTreeItem.setText(this.setProfileStatusEventsText + millionFormatter.format(this.latestStats.setProfileStatusEvents) + this.setProfileStatusRateText + millionFormatter.format((long)this.latestStats.setProfileStatusRate));
                  this.updatedProfileEventsTreeItem.setText(this.updatedProfileEventsText + millionFormatter.format(this.latestStats.updatedProfileEvents) + this.updatedProfileRateText + millionFormatter.format((long)this.latestStats.updatedProfileRate));
                  this.virtualGiftsEventsTreeItem.setText(this.virtualGiftsEventsText + millionFormatter.format(this.latestStats.virtualGiftsEvents) + this.virtualGiftsRateText + millionFormatter.format((long)this.latestStats.virtualGiftsRate));
                  this.genericApplicationEventsTreeItem.setText(this.genericApplicationEventsText + millionFormatter.format(this.latestStats.genericApplicationEvents) + this.genericApplicationRateText + millionFormatter.format((long)this.latestStats.genericApplicationEventRate));
                  this.giftShowerEventTreeItem.setText(this.giftShowerEventText + millionFormatter.format(this.latestStats.giftShowerEvents) + this.giftShowerEventRateText + millionFormatter.format((long)this.latestStats.giftShowerEventRate));
                  this.totalEventsTreeItem.setText(this.totalEventsText + millionFormatter.format(this.latestStats.totalEvents) + this.totalRateText + millionFormatter.format((long)this.latestStats.totalRate) + this.maxTotalRateText + millionFormatter.format((long)this.latestStats.maxTotalRate));
                  this.droppedEventsTreeItem.setText(this.droppedEventsText + millionFormatter.format(this.latestStats.droppedEvents) + this.droppedRateText + millionFormatter.format((long)this.latestStats.droppedRate) + this.maxDroppedRateText + millionFormatter.format((long)this.latestStats.maxDroppedRate));
                  this.streamedEventsTreeItem.setText(this.streamedEventsText + millionFormatter.format(this.latestStats.streamedEvents) + this.streamedRateText + millionFormatter.format((long)this.latestStats.streamedRate) + this.maxStreamedRateText + millionFormatter.format((long)this.latestStats.maxStreamedRate));
                  this.distributedEventsTreeItem.setText(this.distributedEventsText + millionFormatter.format(this.latestStats.distributedEvents) + this.distributedRateText + millionFormatter.format((long)this.latestStats.distributedRate) + this.maxDistributedRateText + millionFormatter.format((long)this.latestStats.maxDistributedRate));
                  this.updateWithLatestStats(this.latestStats, this.isOnline);
                  if (this.latestStats.droppedRate > 0) {
                     this.baseTreeItem.setForeground(new Color(Display.getCurrent(), 255, 110, 0));
                     this.droppedEventsTreeItem.setForeground(new Color(Display.getCurrent(), 0, 0, 0));
                  } else {
                     this.baseTreeItem.setForeground(new Color(Display.getCurrent(), 0, 160, 0));
                     this.droppedEventsTreeItem.setForeground(new Color(Display.getCurrent(), 0, 160, 0));
                  }
               }
            } catch (Exception var6) {
               System.err.println("WARNING: Unable to save stats for the EventSystem on " + this.hostName);
               var6.printStackTrace();
            }

         } finally {
            ;
         }
      }
   }
}
