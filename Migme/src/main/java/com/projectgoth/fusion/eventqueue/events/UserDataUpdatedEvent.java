package com.projectgoth.fusion.eventqueue.events;

import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.eventqueue.Event;
import java.util.HashMap;
import java.util.Map;

public class UserDataUpdatedEvent extends Event {
   public static final String TYPE_KEY = "type";

   public UserDataUpdatedEvent() {
      super(Enums.EventTypeEnum.USERDATA_UPDATED);
   }

   public UserDataUpdatedEvent(String username, UserDataUpdatedEvent.TypeEnum type) {
      super(username, Enums.EventTypeEnum.USERDATA_UPDATED);
      this.putParameter("type", type.toString());
   }

   public static enum TypeEnum {
      PROFILE(1),
      SETTINGS(2),
      USER_DETAIL(3),
      DISPLAY_PICTURE(4),
      STATUS_MESSAGE(5);

      private static final Map<Integer, UserDataUpdatedEvent.TypeEnum> lookup = new HashMap();
      int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static UserDataUpdatedEvent.TypeEnum fromValue(int v) {
         return (UserDataUpdatedEvent.TypeEnum)lookup.get(v);
      }

      static {
         UserDataUpdatedEvent.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserDataUpdatedEvent.TypeEnum e = arr$[i$];
            lookup.put(e.value, e);
         }

      }
   }
}
