package com.projectgoth.fusion.userevent.system;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.DisplayPictureAndStatusMessage;
import com.projectgoth.fusion.slice.GroupAnnouncementUserEventIce;
import com.projectgoth.fusion.slice.UserEventIce;
import org.apache.log4j.Logger;

public class EventTask {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(EventTask.class));
   protected EventSystemI eventSystemI;

   public EventTask(EventSystemI eventSystemI) {
      this.eventSystemI = eventSystemI;
   }

   protected void assignDisplayPicture(UserEventIce event) {
      if (event instanceof GroupAnnouncementUserEventIce) {
         String displayPicture = this.eventSystemI.getDisplayPictureAndStatusMessageDAO().getGroupDisplayPicture(((GroupAnnouncementUserEventIce)event).groupId);
         if (displayPicture != null) {
            event.generatingUserDisplayPicture = displayPicture;
         }
      } else {
         DisplayPictureAndStatusMessage displayPictureAndStatusMessage = this.eventSystemI.getDisplayPictureAndStatusMessageDAO().getDisplayPictureAndStatusMessage(event.generatingUsername);
         if (displayPictureAndStatusMessage != null) {
            event.generatingUserDisplayPicture = displayPictureAndStatusMessage.getDisplayPicture();
         }
      }

      if (log.isDebugEnabled()) {
         log.debug("display picture set to [" + event.generatingUserDisplayPicture + "] for user [" + event.generatingUsername + "]");
      }

   }

   protected void assignRuntimeValues(UserEventIce event) {
      this.eventSystemI.assignRuntimeValues(event);
   }
}
