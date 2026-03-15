package com.projectgoth.fusion.userevent.system.domain;

import com.projectgoth.fusion.slice.UserEventIce;

public class UsernameAndUserEvents {
   private String username;
   private UserEventIce[] userEvents;

   public UsernameAndUserEvents(String username, UserEventIce[] userEvents) {
      this.username = username;
      this.userEvents = userEvents;
   }

   public String getUsername() {
      return this.username;
   }

   public UserEventIce[] getUserEvents() {
      return this.userEvents;
   }
}
