package com.projectgoth.fusion.uns.domain;

import java.util.HashSet;
import java.util.Set;

public class AlertNote extends Note {
   private Set<String> users = new HashSet();

   public AlertNote(String message) {
      super(message);
   }

   public Set<String> getUsers() {
      return this.users;
   }

   public String[] getUsersArray() {
      return (String[])((String[])this.users.toArray(new String[this.users.size()]));
   }

   public void addUser(String username) {
      this.users.add(username);
   }

   public boolean hasRecipients() {
      return !this.users.isEmpty();
   }
}
