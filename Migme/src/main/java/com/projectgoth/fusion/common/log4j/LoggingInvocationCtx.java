package com.projectgoth.fusion.common.log4j;

import java.util.LinkedList;
import java.util.List;

public class LoggingInvocationCtx {
   private final List<AppenderErrorData> appenderErrorDataList = new LinkedList();

   public void add(AppenderErrorData appenderErrorData) {
      this.appenderErrorDataList.add(appenderErrorData);
   }

   public List<AppenderErrorData> getAppenderErrorDataList() {
      return this.appenderErrorDataList;
   }

   public boolean hasAppenderErrors() {
      return !this.appenderErrorDataList.isEmpty();
   }
}
