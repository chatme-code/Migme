package com.projectgoth.fusion.userevent.domain;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.AddingMultipleFriendsUserEventIce;
import com.sleepycat.persist.model.Persistent;
import java.util.Map;
import org.apache.log4j.Logger;

@Persistent
public class AddingMultipleFriendsUserEvent extends AddingTwoFriendsUserEvent {
   public static final String EVENT_NAME = "ADDING_MULTIPLE_FRIENDS";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(AddingMultipleFriendsUserEvent.class));
   private int additionalFriends;

   public AddingMultipleFriendsUserEvent() {
   }

   public AddingMultipleFriendsUserEvent(AddingTwoFriendsUserEvent event, int additionalFriends) {
      super(event, event.getFriend2());
      this.additionalFriends = additionalFriends;
   }

   public AddingMultipleFriendsUserEvent(AddingMultipleFriendsUserEventIce event) {
      super(event);
      this.additionalFriends = event.additionalFriends;
   }

   public int getAdditionalFriends() {
      return this.additionalFriends;
   }

   public void setAdditionalFriends(int additionalFriends) {
      this.additionalFriends = additionalFriends;
   }

   public AddingMultipleFriendsUserEventIce toIceEvent() {
      if (log.isDebugEnabled()) {
         log.debug("creating " + this.getClass().getName());
      }

      AddingMultipleFriendsUserEventIce iceEvent = new AddingMultipleFriendsUserEventIce(this.getTimestamp(), this.getGeneratingUsername(), (String)null, this.getText(), this.getFriend(), this.getFriend2(), this.additionalFriends);
      return iceEvent;
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer(super.toString());
      buffer.append(" additionalFriends [").append(this.additionalFriends).append("]");
      return buffer.toString();
   }

   public static Map<String, String> findSubstitutionParameters(AddingMultipleFriendsUserEventIce event) {
      Map<String, String> map = UserEvent.findSubstitutionParameters(event);
      map.put("friend1", event.friend1);
      map.put("friend2", event.friend2);
      map.put("additionalFriends", Integer.toString(event.additionalFriends));
      return map;
   }
}
