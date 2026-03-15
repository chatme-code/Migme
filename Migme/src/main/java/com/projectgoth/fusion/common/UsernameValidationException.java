package com.projectgoth.fusion.common;

public class UsernameValidationException extends ExceptionWithErrorCause {
   public UsernameValidationException(ErrorCause reasonType, Object... errorMsgArgs) {
      super(reasonType, errorMsgArgs);
   }
}
