package com.projectgoth.fusion.userevent.system;

import Ice.LocalException;
import Ice.ObjectNotExistException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.UserEventIce;
import com.projectgoth.fusion.userevent.system.domain.UsernameAndUserEvents;
import org.apache.log4j.Logger;

public class StreamTask extends EventTask implements Runnable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(StreamTask.class));
   private UsernameAndUserEvents userEvents;
   private ConnectionPrx connectionProxy;

   public StreamTask(UsernameAndUserEvents userEvents, ConnectionPrx connectionProxy, EventSystemI eventSystemI) {
      super(eventSystemI);
      this.userEvents = userEvents;
      this.connectionProxy = connectionProxy;
   }

   public void run() {
      boolean streamed = false;

      try {
         if (log.isDebugEnabled()) {
            log.debug("sending event for user [" + this.userEvents.getUsername() + "] to proxy [" + this.connectionProxy + "]");
         }

         UserEventIce[] arr$ = this.userEvents.getUserEvents();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserEventIce event = arr$[i$];
            this.assignDisplayPicture(event);
            this.assignRuntimeValues(event);
            if (this.connectionProxy != null) {
               this.connectionProxy.putEvent(event);
               streamed = true;
            }
         }
      } catch (FusionException var13) {
         log.error("failed to distribute event for user [" + this.userEvents.getUsername() + "]", var13);
      } catch (ObjectNotExistException var14) {
         log.warn("failed to send event to user, the connectionProxy no longer exists, the user probably got disconnected during/after login");
      } catch (LocalException var15) {
         log.error("could not send event to gateway for user [" + this.userEvents.getUsername() + "]", var15);
      } catch (Exception var16) {
         log.error("something really bad happened trying to distribute event for user [" + this.userEvents.getUsername() + "]", var16);
      } finally {
         if (streamed) {
            this.eventSystemI.getStreamedEventsCounter().add();
         }

         if (log.isDebugEnabled()) {
            log.debug("done distributing event");
         }

      }

   }
}
