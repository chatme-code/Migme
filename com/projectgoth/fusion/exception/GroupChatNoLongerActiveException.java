package com.projectgoth.fusion.exception;

import com.projectgoth.fusion.slice.FusionException;

public class GroupChatNoLongerActiveException extends FusionException {
   private String chatID;

   public String getChatID() {
      return this.chatID;
   }

   public GroupChatNoLongerActiveException(String chatID) {
      super("The group chat is no longer active. Please start a new one");
      this.chatID = chatID;
   }
}
