package com.projectgoth.fusion.userevent.domain;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.GroupJoinedUserEventIce;
import com.sleepycat.persist.model.Persistent;
import org.apache.log4j.Logger;

@Persistent
public class GroupJoinedUserEvent extends GroupUserEvent {
   public static final String EVENT_NAME = "GROUP_JOINED";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GroupJoinedUserEvent.class));

   public GroupJoinedUserEvent() {
   }

   public GroupJoinedUserEvent(GroupJoinedUserEventIce event) {
      super(event);
   }

   public GroupJoinedUserEventIce toIceEvent() {
      if (log.isDebugEnabled()) {
         log.debug("creating " + this.getClass().getName());
      }

      GroupJoinedUserEventIce iceEvent = new GroupJoinedUserEventIce(this.getTimestamp(), this.getGeneratingUsername(), (String)null, this.getText(), this.getGroupId(), this.getGroupName());
      return iceEvent;
   }
}
