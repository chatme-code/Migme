package com.projectgoth.fusion.common.log4j;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.LoggingEvent;

public final class TLSErrorDataCollectorErrorHandler implements ErrorHandler {
   private final ErrorHandler nextErrorHandler;

   private TLSErrorDataCollectorErrorHandler() {
      this((ErrorHandler)null);
   }

   private TLSErrorDataCollectorErrorHandler(ErrorHandler nextErrorHandler) {
      this.nextErrorHandler = nextErrorHandler;
   }

   public static TLSErrorDataCollectorErrorHandler create() {
      return new TLSErrorDataCollectorErrorHandler();
   }

   public static TLSErrorDataCollectorErrorHandler create(ErrorHandler nextErrorHandler) {
      return new TLSErrorDataCollectorErrorHandler(nextErrorHandler);
   }

   private static LoggingInvocationCtx getCurrentCtx() {
      return TLSLoggingInvocationCtxStack.getInstance().getCurrentCtx();
   }

   public boolean hasEmptyAppenderErrors() {
      return getCurrentCtx().hasAppenderErrors();
   }

   public void activateOptions() {
      if (this.nextErrorHandler != null) {
         this.nextErrorHandler.activateOptions();
      }

   }

   public void setLogger(Logger logger) {
      if (this.nextErrorHandler != null) {
         this.nextErrorHandler.setLogger(logger);
      }

   }

   public void error(String message, Exception e, int errorCode) {
      LoggingInvocationCtx ctx = getCurrentCtx();
      ctx.add(new AppenderErrorData(message, (Exception)null, errorCode, (LoggingEvent)null));
      if (this.nextErrorHandler != null) {
         this.nextErrorHandler.error(message, e, errorCode);
      }

   }

   public void error(String message) {
      LoggingInvocationCtx ctx = getCurrentCtx();
      ctx.add(new AppenderErrorData(message, (Exception)null, (Integer)null, (LoggingEvent)null));
      if (this.nextErrorHandler != null) {
         this.nextErrorHandler.error(message);
      }

   }

   public void error(String message, Exception e, int errorCode, LoggingEvent event) {
      LoggingInvocationCtx ctx = getCurrentCtx();
      ctx.add(new AppenderErrorData(message, e, errorCode, event));
      if (this.nextErrorHandler != null) {
         this.nextErrorHandler.error(message, e, errorCode, event);
      }

   }

   public void setAppender(Appender appender) {
      if (this.nextErrorHandler != null) {
         this.nextErrorHandler.setAppender(appender);
      }

   }

   public void setBackupAppender(Appender appender) {
      if (this.nextErrorHandler != null) {
         this.nextErrorHandler.setBackupAppender(appender);
      }

   }
}
