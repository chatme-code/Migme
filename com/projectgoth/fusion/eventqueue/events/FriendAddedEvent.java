package com.projectgoth.fusion.eventqueue.events;

import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.eventqueue.Event;

public class FriendAddedEvent extends Event {
   public static final String FRIEND_PARAM_KEY = "friend_username";

   public FriendAddedEvent() {
      super(Enums.EventTypeEnum.FRIEND_ADDED);
   }

   public FriendAddedEvent(String username, String friend) {
      super(username, Enums.EventTypeEnum.FRIEND_ADDED);
      this.putParameter("friend_username", friend);
   }
}
