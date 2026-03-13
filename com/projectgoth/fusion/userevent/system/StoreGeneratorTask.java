package com.projectgoth.fusion.userevent.system;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.EventStorePrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.userevent.system.domain.UsernameAndUserEvent;
import org.apache.log4j.Logger;

public class StoreGeneratorTask implements Runnable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(StoreGeneratorTask.class));
   private UsernameAndUserEvent userEvent;
   private EventStorePrx eventStoreProxy;

   public StoreGeneratorTask(UsernameAndUserEvent userEvent, EventStorePrx eventStoreProxy) {
      this.userEvent = userEvent;
      this.eventStoreProxy = eventStoreProxy;
   }

   public void run() {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Sending generator event [" + this.userEvent.getUserEvent() + "] to proxy [" + this.eventStoreProxy + "]");
         }

         this.eventStoreProxy.storeGeneratorEvent(this.userEvent.getUsername(), this.userEvent.getUserEvent());
      } catch (FusionException var2) {
         log.error("failed to send generator event to store", var2);
      }

   }
}
