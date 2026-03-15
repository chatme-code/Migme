package com.projectgoth.fusion.userevent.domain;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.GroupDonationUserEventIce;
import com.sleepycat.persist.model.Persistent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Persistent
public class GroupDonationUserEvent extends GroupUserEvent {
   public static final String EVENT_NAME = "GROUP_DONATION";
   private static Logger log = LoggerFactory.getLogger(ConfigUtils.getLoggerName(GroupDonationUserEvent.class));

   public GroupDonationUserEvent() {
   }

   public GroupDonationUserEvent(GroupDonationUserEventIce event) {
      super(event);
   }

   public GroupDonationUserEventIce toIceEvent() {
      if (log.isDebugEnabled()) {
         log.debug("creating " + this.getClass().getName());
      }

      GroupDonationUserEventIce iceEvent = new GroupDonationUserEventIce(this.getTimestamp(), this.getGeneratingUsername(), (String)null, this.getText(), this.getGroupId(), this.getGroupName());
      return iceEvent;
   }
}
