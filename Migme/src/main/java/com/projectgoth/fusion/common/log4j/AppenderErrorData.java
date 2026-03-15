package com.projectgoth.fusion.common.log4j;

import com.projectgoth.fusion.common.ExceptionHelper;
import org.apache.log4j.spi.LoggingEvent;

public final class AppenderErrorData {
   public String message;
   public Exception ex;
   public Integer errorCode;
   public LoggingEvent event;

   public AppenderErrorData(String message, Exception ex, Integer errorCode, LoggingEvent event) {
      this.message = message;
      this.ex = ex;
      this.errorCode = errorCode;
      this.event = event;
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("AppenderError [message={");
      builder.append(this.message);
      builder.append("}, errorCode={");
      builder.append(this.errorCode);
      builder.append("}, event={");
      builder.append(this.event);
      builder.append("}, ex={");
      ExceptionHelper.appendStackTrace(this.ex, builder);
      builder.append("}]");
      return builder.toString();
   }
}
