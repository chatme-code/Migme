package com.projectgoth.fusion.jobscheduling.domain;

import java.io.Serializable;

public class NotificationNote implements Serializable {
   private String message;

   public NotificationNote(String username) {
      this.message = username;
   }

   public String getMessage() {
      return this.message;
   }
}
