package com.projectgoth.fusion.exception;

import com.projectgoth.fusion.slice.NoLongerInGroupChatExceptionIce;

public class NoLongerInGroupChatException extends NoLongerInGroupChatExceptionIce {
   public NoLongerInGroupChatException() {
      super("You are no longer in the group chat");
   }
}
