package com.projectgoth.fusion.userevent.system;

import Ice.Current;
import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.slice.EventSystemStats;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice._EventSystemAdminDisp;

public class EventSystemAdminI extends _EventSystemAdminDisp {
   private EventSystemI eventSystem;

   public EventSystemAdminI(EventSystemI eventSystemI) {
      this.eventSystem = eventSystemI;
   }

   public EventSystemStats getStats(Current __current) throws FusionException {
      EventSystemStats stats = ServiceStatsFactory.getEventSystemStats(EventSystem.startTime);
      stats.addedFriendEvents = this.eventSystem.getAddedFriendCounter().getTotalRequests();
      stats.addedFriendRate = this.eventSystem.getAddedFriendCounter().getRequestsPerSecond();
      stats.createdPublicChatrooomStatusEvents = this.eventSystem.getCreatedPublicChatrooomStatusCounter().getTotalRequests();
      stats.createdPublicChatrooomStatusRate = this.eventSystem.getCreatedPublicChatrooomStatusCounter().getRequestsPerSecond();
      stats.madePhotoPublicEvents = this.eventSystem.getMadePhotoPublicCounter().getTotalRequests();
      stats.madePhotoPublicRate = this.eventSystem.getMadePhotoPublicCounter().getRequestsPerSecond();
      stats.purchasedVirtualGoodsEvents = this.eventSystem.getPurchasedVirtualGoodsCounter().getTotalRequests();
      stats.purchasedVirtualGoodsRate = this.eventSystem.getPurchasedVirtualGoodsCounter().getRequestsPerSecond();
      stats.setProfileStatusEvents = this.eventSystem.getSetProfileStatusCounter().getTotalRequests();
      stats.setProfileStatusRate = this.eventSystem.getSetProfileStatusCounter().getRequestsPerSecond();
      stats.updatedProfileEvents = this.eventSystem.getUpdatedProfileCounter().getTotalRequests();
      stats.updatedProfileRate = this.eventSystem.getUpdatedProfileCounter().getRequestsPerSecond();
      stats.virtualGiftsEvents = this.eventSystem.getVirtualGiftCounter().getTotalRequests();
      stats.virtualGiftsRate = this.eventSystem.getVirtualGiftCounter().getRequestsPerSecond();
      stats.userWallPostEvents = this.eventSystem.getUserWallPostCounter().getTotalRequests();
      stats.userWallPostRate = this.eventSystem.getUserWallPostCounter().getRequestsPerSecond();
      stats.totalEvents = this.eventSystem.getTotalCounter().getTotalRequests();
      stats.totalRate = this.eventSystem.getTotalCounter().getRequestsPerSecond();
      stats.maxTotalRate = this.eventSystem.getTotalCounter().getMaxRequestsPerSecond();
      stats.droppedEvents = this.eventSystem.getDroppedEventCounter().getTotalRequests();
      stats.droppedRate = this.eventSystem.getDroppedEventCounter().getRequestsPerSecond();
      stats.maxDroppedRate = this.eventSystem.getDroppedEventCounter().getMaxRequestsPerSecond();
      stats.streamedEvents = this.eventSystem.getStreamedEventsCounter().getTotalRequests();
      stats.streamedRate = this.eventSystem.getStreamedEventsCounter().getRequestsPerSecond();
      stats.maxStreamedRate = this.eventSystem.getStreamedEventsCounter().getMaxRequestsPerSecond();
      stats.distributedEvents = this.eventSystem.getDistributedEventsCounter().getTotalRequests();
      stats.distributedRate = this.eventSystem.getDistributedEventsCounter().getRequestsPerSecond();
      stats.maxDistributedRate = this.eventSystem.getDistributedEventsCounter().getMaxRequestsPerSecond();
      stats.genericApplicationEvents = this.eventSystem.getGenericApplicationEventCounter().getTotalRequests();
      stats.genericApplicationEventRate = this.eventSystem.getGenericApplicationEventCounter().getRequestsPerSecond();
      stats.giftShowerEvents = this.eventSystem.getGiftShowerEventCounter().getTotalRequests();
      stats.giftShowerEventRate = this.eventSystem.getGiftShowerEventCounter().getRequestsPerSecond();
      return stats;
   }
}
