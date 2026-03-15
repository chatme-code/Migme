package com.projectgoth.fusion.recommendation.collector;

import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.ExceptionWithErrorCause;

public class DataCollectorException extends ExceptionWithErrorCause {
   public DataCollectorException(ErrorCause reasonType, Object... errorMsgArgs) {
      super(reasonType, errorMsgArgs);
   }

   public DataCollectorException(Throwable cause, ErrorCause reasonType, Object... errorMsgArgs) {
      super(cause, reasonType, errorMsgArgs);
   }
}
