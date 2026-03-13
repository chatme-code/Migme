package com.projectgoth.fusion.userevent.domain;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.GroupAnnouncementUserEventIce;
import com.sleepycat.persist.model.Persistent;
import java.util.Map;
import org.apache.log4j.Logger;

@Persistent
public class GroupAnnouncementUserEvent extends GroupUserEvent {
   public static final String EVENT_NAME = "GROUP_ANNOUNCEMENT";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GroupAnnouncementUserEvent.class));
   private int groupAnnouncementId;
   private String groupAnnouncementTitle;

   public GroupAnnouncementUserEvent() {
   }

   public GroupAnnouncementUserEvent(GroupAnnouncementUserEventIce event) {
      super(event);
      this.groupAnnouncementId = event.groupAnnouncementId;
      this.groupAnnouncementTitle = event.groupAnnouncementTitle;
   }

   public GroupAnnouncementUserEventIce toIceEvent() {
      if (log.isDebugEnabled()) {
         log.debug("creating " + this.getClass().getName());
      }

      GroupAnnouncementUserEventIce iceEvent = new GroupAnnouncementUserEventIce(this.getTimestamp(), this.getGeneratingUsername(), (String)null, this.getText(), this.getGroupId(), this.getGroupName(), this.groupAnnouncementId, this.groupAnnouncementTitle);
      return iceEvent;
   }

   public static Map<String, String> findSubstitutionParameters(GroupAnnouncementUserEventIce event) {
      Map<String, String> map = GroupUserEvent.findSubstitutionParameters(event);
      map.put("groupAnnouncementId", Integer.toString(event.groupAnnouncementId));
      map.put("groupAnnouncementTitle", event.groupAnnouncementTitle);
      return map;
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer(super.toString());
      buffer.append(" groupAnnouncementId [").append(this.groupAnnouncementId).append("]");
      buffer.append(" groupAnnouncementTitle [").append(this.groupAnnouncementTitle).append("]");
      return buffer.toString();
   }
}
