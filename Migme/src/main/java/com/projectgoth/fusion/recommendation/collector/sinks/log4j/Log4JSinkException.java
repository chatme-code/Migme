package com.projectgoth.fusion.recommendation.collector.sinks.log4j;

import com.projectgoth.fusion.common.log4j.AppenderErrorData;
import com.projectgoth.fusion.recommendation.collector.CollectorSinkException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Log4JSinkException extends CollectorSinkException {
   private final List<AppenderErrorData> appenderErrors = new ArrayList();

   public Log4JSinkException() {
   }

   public Log4JSinkException(String msg, Throwable t) {
      super(msg, t);
   }

   public Log4JSinkException(String msg) {
      super(msg);
   }

   public Log4JSinkException(Throwable t) {
      super(t);
   }

   public Log4JSinkException addAppenderErrors(Collection<AppenderErrorData> appenderErrors) {
      this.appenderErrors.addAll(appenderErrors);
      return this;
   }

   public String toString() {
      return super.toString() + ".AppenderErrors=[" + this.appenderErrors.toString() + "]";
   }
}
