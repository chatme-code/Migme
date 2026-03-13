package com.projectgoth.fusion.userevent.system;

import Ice.LocalException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.EventStorePrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.userevent.domain.EventPrivacySetting;
import com.projectgoth.fusion.userevent.system.domain.UsernameAndUserEvent;
import org.apache.log4j.Logger;

public class DistributionTask extends EventTask implements Runnable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DistributionTask.class));
   private UsernameAndUserEvent userEvent;
   private EventStorePrx eventStoreProxy;

   public DistributionTask(UsernameAndUserEvent userEvent, EventSystemI eventSystemI, EventStorePrx eventStoreProxy) {
      super(eventSystemI);
      this.userEvent = userEvent;
      this.eventStoreProxy = eventStoreProxy;
   }

   public void run() {
      try {
         UserPrx userProxy = this.eventSystemI.getRegistryProxy().findUserObject(this.userEvent.getUsername());
         this.assignDisplayPicture(this.userEvent.getUserEvent());
         this.assignRuntimeValues(this.userEvent.getUserEvent());
         if (log.isDebugEnabled()) {
            log.debug("sending event for user [" + this.userEvent.getUsername() + "] to proxy [" + userProxy + "] with display picture [" + this.userEvent.getUserEvent().generatingUserDisplayPicture + "]");
         }

         EventPrivacySetting mask = EventPrivacySetting.fromEventPrivacySettingIce(this.eventStoreProxy.getReceivingPrivacyMask(this.userEvent.getUsername()));
         if (mask.applyMask(this.userEvent.getUserEvent())) {
            userProxy.putEvent(this.userEvent.getUserEvent());
            this.eventSystemI.getDistributedEventsCounter().add();
         }
      } catch (ObjectNotFoundException var3) {
         if (log.isDebugEnabled()) {
            log.debug("user [" + this.userEvent.getUsername() + "] is not online according to registry, doing nothing");
         }
      } catch (FusionException var4) {
         log.error("failed to distribute event for user [" + this.userEvent.getUsername() + "]", var4);
      } catch (LocalException var5) {
         log.error("could not send event to gateway for user [" + this.userEvent.getUsername() + "]", var5);
      } catch (Exception var6) {
         log.error("something really bad happened trying to distribute event for user [" + this.userEvent.getUsername() + "]", var6);
      }

      if (log.isDebugEnabled()) {
         log.debug("done distributing event");
      }

   }
}
