package com.projectgoth.fusion.recommendation.collector.sinks.log4j;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.log4j.LoggingInvocationCtx;
import com.projectgoth.fusion.common.log4j.TLSLoggingInvocationCtxStack;
import com.projectgoth.fusion.recommendation.collector.ICollectorSink;
import java.util.Collection;
import java.util.Iterator;
import org.apache.log4j.Logger;

public class Log4JSink<TLoggable> implements ICollectorSink<TLoggable> {
   private final String name;
   private final Logger loggerSink;

   public Log4JSink(String name, String loggerSinkCategoryName) {
      this.name = name;
      String fullLoggerSinkCategory = "LOG4JSINK";
      if (!StringUtil.isBlank(loggerSinkCategoryName)) {
         fullLoggerSinkCategory = fullLoggerSinkCategory + ":" + loggerSinkCategoryName;
      }

      this.loggerSink = Logger.getLogger(fullLoggerSinkCategory);
   }

   public String getName() {
      return this.name;
   }

   public void write(Collection<TLoggable> loggableColl) throws Log4JSinkException {
      TLSLoggingInvocationCtxStack ctxStack = TLSLoggingInvocationCtxStack.getInstance();
      ctxStack.pushCtx();

      try {
         Iterator i$ = loggableColl.iterator();

         while(i$.hasNext()) {
            TLoggable loggable = i$.next();
            this.loggerSink.info(this.toString(loggable));
         }

         LoggingInvocationCtx ctx = ctxStack.getCurrentCtx();
         if (ctx.hasAppenderErrors()) {
            throw (new Log4JSinkException("Found some errors while appending logs")).addAppenderErrors(ctx.getAppenderErrorDataList());
         }
      } finally {
         ctxStack.popCtx();
      }

   }

   protected String toString(TLoggable loggable) {
      return loggable == null ? "" : loggable.toString();
   }
}
