package com.projectgoth.fusion.userevent.system.domain;

import com.projectgoth.fusion.slice.UserEventIce;

public class UsernameAndUserEvent {
   private String username;
   private UserEventIce userEvent;

   public UsernameAndUserEvent(String username, UserEventIce userEvent) {
      this.username = username;
      this.userEvent = userEvent;
   }

   public String getUsername() {
      return this.username;
   }

   public UserEventIce getUserEvent() {
      return this.userEvent;
   }
}
