package com.projectgoth.fusion.eventqueue;

import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.eventqueue.events.FriendAddedEvent;
import com.projectgoth.fusion.eventqueue.events.GameEvent;
import com.projectgoth.fusion.eventqueue.events.StatusUpdateEvent;
import com.projectgoth.fusion.eventqueue.events.ThirdPartySiteCredentialUpdatedEvent;
import com.projectgoth.fusion.eventqueue.events.UserDataUpdatedEvent;
import com.projectgoth.fusion.eventqueue.events.VirtualGiftSentEvent;

public class EventFactory {
   public static Event createEvent(Enums.EventTypeEnum type) {
      Event e = null;
      switch(type) {
      case TEST_EVENT:
         e = new DummyEvent();
         break;
      case FRIEND_ADDED:
         e = new FriendAddedEvent();
         break;
      case VIRTUAL_GIFT_PURCHASED:
         e = new VirtualGiftSentEvent();
         break;
      case USERDATA_UPDATED:
         e = new UserDataUpdatedEvent();
         break;
      case GAME_EVENT:
         e = new GameEvent();
         break;
      case THIRD_PARTY_SITE_CREDENTIAL_UPDATED:
         e = new ThirdPartySiteCredentialUpdatedEvent();
         break;
      case STATUS_UPDATE_EVENT:
         e = new StatusUpdateEvent();
      }

      return (Event)e;
   }
}
