package com.projectgoth.fusion.userevent.system.domain;

import java.io.Serializable;

public class AllowListEntry implements Serializable {
   private String username;
   private boolean pending;

   public AllowListEntry(String username, boolean pending) {
      this.username = username;
      this.pending = pending;
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public boolean isPending() {
      return this.pending;
   }

   public void setPending(boolean pending) {
      this.pending = pending;
   }
}
