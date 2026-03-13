package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.ExceptionWithErrorCause;

public class EmoteCommandException extends ExceptionWithErrorCause {
   public EmoteCommandException(ErrorCause reasonType, Object... errorMsgArgs) {
      super(reasonType, errorMsgArgs);
   }

   public EmoteCommandException(Throwable cause, ErrorCause reasonType, Object... errorMsgArgs) {
      super(cause, reasonType, errorMsgArgs);
   }

   public EmoteCommandException(Throwable throwable) {
      this(throwable, ErrorCause.EmoteCommandError.INTERNAL_ERROR, throwable.getMessage());
   }
}
