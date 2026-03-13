package com.projectgoth.fusion.userevent.domain;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.AddingFriendUserEventIce;
import com.projectgoth.fusion.slice.UserEventIce;
import com.sleepycat.persist.model.Persistent;
import java.util.Map;
import org.apache.log4j.Logger;

@Persistent
public class AddingFriendUserEvent extends UserEvent {
   public static final String EVENT_NAME = "ADDING_FRIEND";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(AddingFriendUserEvent.class));
   private String friend;

   public AddingFriendUserEvent() {
   }

   public AddingFriendUserEvent(UserEvent event, String friend) {
      super(event);
      this.friend = friend;
   }

   public AddingFriendUserEvent(AddingFriendUserEventIce event) {
      super((UserEventIce)event);
      this.friend = event.friend1;
   }

   public String getFriend() {
      return this.friend;
   }

   public void setFriend(String friend) {
      this.friend = friend;
   }

   public static boolean areTheSame(AddingFriendUserEvent lhs, AddingFriendUserEvent rhs) {
      return lhs.getGeneratingUsername().equals(rhs.getGeneratingUsername()) && lhs.friend.equals(rhs.friend);
   }

   public static boolean areTheSame(UserEvent lhs, UserEvent rhs) {
      return areTheSame((AddingFriendUserEvent)lhs, (AddingFriendUserEvent)rhs);
   }

   public static boolean areInvertedTheSame(AddingFriendUserEvent lhs, AddingFriendUserEvent rhs) {
      return lhs.getGeneratingUsername().equals(rhs.friend) && lhs.friend.equals(rhs.getGeneratingUsername());
   }

   public static boolean areInvertedTheSame(UserEvent lhs, UserEvent rhs) {
      return areInvertedTheSame((AddingFriendUserEvent)lhs, (AddingFriendUserEvent)rhs);
   }

   public AddingFriendUserEventIce toIceEvent() {
      if (log.isDebugEnabled()) {
         log.debug("creating " + this.getClass().getName());
      }

      AddingFriendUserEventIce iceEvent = new AddingFriendUserEventIce(this.getTimestamp(), this.getGeneratingUsername(), (String)null, this.getText(), this.friend);
      return iceEvent;
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer(super.toString());
      buffer.append(" friend [").append(this.friend).append("]");
      return buffer.toString();
   }

   public static Map<String, String> findSubstitutionParameters(AddingFriendUserEventIce event) {
      Map<String, String> map = UserEvent.findSubstitutionParameters(event);
      map.put("friend1", event.friend1);
      return map;
   }
}
