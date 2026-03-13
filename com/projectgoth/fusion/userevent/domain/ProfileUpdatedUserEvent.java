package com.projectgoth.fusion.userevent.domain;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.ProfileUpdatedUserEventIce;
import com.projectgoth.fusion.slice.UserEventIce;
import com.sleepycat.persist.model.Persistent;
import java.util.Map;
import org.apache.log4j.Logger;

@Persistent
public class ProfileUpdatedUserEvent extends UserEvent {
   public static final String EVENT_NAME = "PROFILE_UPDATED";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ProfileUpdatedUserEvent.class));

   public ProfileUpdatedUserEvent() {
   }

   public ProfileUpdatedUserEvent(ProfileUpdatedUserEventIce event) {
      super((UserEventIce)event);
   }

   public ProfileUpdatedUserEventIce toIceEvent() {
      if (log.isDebugEnabled()) {
         log.debug("creating " + this.getClass().getName());
      }

      ProfileUpdatedUserEventIce iceEvent = new ProfileUpdatedUserEventIce(this.getTimestamp(), this.getGeneratingUsername(), (String)null, this.getText());
      return iceEvent;
   }

   public static Map<String, String> findSubstitutionParameters(ProfileUpdatedUserEventIce event) {
      Map<String, String> map = UserEvent.findSubstitutionParameters(event);
      return map;
   }
}
