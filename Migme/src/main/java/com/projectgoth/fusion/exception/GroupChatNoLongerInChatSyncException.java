package com.projectgoth.fusion.exception;

import com.projectgoth.fusion.slice.FusionException;

public class GroupChatNoLongerInChatSyncException extends FusionException {
   private String chatID;

   public String getChatID() {
      return this.chatID;
   }

   public GroupChatNoLongerInChatSyncException(String chatID) {
      super("The group chat is no longer in chat sync storage");
      this.chatID = chatID;
   }
}
