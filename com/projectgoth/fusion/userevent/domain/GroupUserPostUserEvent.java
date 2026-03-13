package com.projectgoth.fusion.userevent.domain;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.GroupUserPostUserEventIce;
import com.sleepycat.persist.model.Persistent;
import java.util.Map;
import org.apache.log4j.Logger;

@Persistent(
   version = 1
)
public class GroupUserPostUserEvent extends GroupUserEvent {
   public static final String EVENT_NAME = "GROUP_USER_POST";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GroupUserPostUserEvent.class));
   private int userPostId;
   private int topicId;

   public GroupUserPostUserEvent() {
   }

   public GroupUserPostUserEvent(GroupUserPostUserEventIce event) {
      super(event);
      this.userPostId = event.userPostId;
   }

   public GroupUserPostUserEventIce toIceEvent() {
      if (log.isDebugEnabled()) {
         log.debug("creating " + this.getClass().getName());
      }

      GroupUserPostUserEventIce iceEvent = new GroupUserPostUserEventIce(this.getTimestamp(), this.getGeneratingUsername(), (String)null, this.getText(), this.getGroupId(), this.getGroupName(), this.userPostId, this.topicId, (String)null);
      return iceEvent;
   }

   public static Map<String, String> findSubstitutionParameters(GroupUserPostUserEventIce event) {
      Map<String, String> map = GroupUserEvent.findSubstitutionParameters(event);
      map.put("userPostId", Integer.toString(event.userPostId));
      map.put("topicId", Integer.toString(event.topicId));
      map.put("topicText", event.topicText);
      return map;
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer(super.toString());
      buffer.append(" userPostId [").append(this.userPostId).append("]");
      buffer.append(" topicId [").append(this.topicId).append("]");
      return buffer.toString();
   }
}
