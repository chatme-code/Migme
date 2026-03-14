package com.projectgoth.fusion.exception;

public class GroupChatCannotBeLoadedException extends ExceptionWithDiagnosticCode {
   private static final String MSG = "We were unable to load the group chat from storage. Please try later";

   public GroupChatCannotBeLoadedException(Exception rootException, String contextInfo) {
      super("We were unable to load the group chat from storage. Please try later", rootException, contextInfo);
   }
}
