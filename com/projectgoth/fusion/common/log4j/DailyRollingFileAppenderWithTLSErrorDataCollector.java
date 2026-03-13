package com.projectgoth.fusion.common.log4j;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.LoggingEvent;

public class DailyRollingFileAppenderWithTLSErrorDataCollector extends DailyRollingFileAppender {
   public DailyRollingFileAppenderWithTLSErrorDataCollector() {
      super.setErrorHandler(TLSErrorDataCollectorErrorHandler.create());
   }

   public synchronized void setErrorHandler(ErrorHandler eh) {
      super.setErrorHandler(TLSErrorDataCollectorErrorHandler.create(eh));
   }

   public synchronized void doAppend(LoggingEvent event) {
      if (TLSLoggingInvocationCtxStack.getInstance().ctxExists()) {
         super.doAppend(event);
      } else {
         throw new IllegalStateException("DailyRollingFileAppenderWithTLSErrorDataCollector requires callers to push context prior to writing logs.");
      }
   }
}
