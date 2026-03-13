package com.projectgoth.fusion.eventqueue;

import Ice.Current;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.RequestAndRateLongCounter;
import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.slice.EventQueueWorkerServiceStats;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice._EventQueueWorkerServiceAdminDisp;
import java.util.Iterator;

public class WorkerAdminI extends _EventQueueWorkerServiceAdminDisp {
   private WorkerI worker;

   public WorkerAdminI(WorkerI workerServant) {
      this.worker = workerServant;
   }

   public EventQueueWorkerServiceStats getStats(Current __current) throws FusionException {
      EventQueueWorkerServiceStats stats = ServiceStatsFactory.getEventQueueWorkerServiceStats(Worker.hostName, Worker.startTime);
      RequestAndRateLongCounter total = this.worker.getTotalCounter();
      stats.eventsProcessed = total.getTotalRequests();
      stats.currentEventsProcessedRate = total.getRequestsPerSecond();
      stats.peakEventsProcessedRate = total.getMaxRequestsPerSecond();
      stats.queueSize = this.worker.getQueueSize();
      stats.maxQueueSize = this.worker.getMaxQueueSize();
      Iterator i$ = Enums.EventTypeEnum.getAllTypes().iterator();

      while(i$.hasNext()) {
         Enums.EventTypeEnum e = (Enums.EventTypeEnum)i$.next();
         RequestAndRateLongCounter c = this.worker.getEventCounter(e);
         if (c != null) {
            if (e.equals(Enums.EventTypeEnum.FRIEND_ADDED)) {
               stats.friendsAddedEvents = c.getTotalRequests();
               stats.currentFriendsAddedEventRate = c.getRequestsPerSecond();
               stats.peakFriendsAddedEventRate = c.getMaxRequestsPerSecond();
            } else if (e.equals(Enums.EventTypeEnum.VIRTUAL_GIFT_PURCHASED)) {
               stats.virtualGiftSentEvents = c.getTotalRequests();
               stats.currentVirtualGiftEventRate = c.getRequestsPerSecond();
               stats.peakVirtualGiftEventRate = c.getMaxRequestsPerSecond();
            }
         }
      }

      return stats;
   }
}
