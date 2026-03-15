package com.projectgoth.fusion.common;

public class AppStartupInfo {
   private final long startTime;
   private final String[] programArgs;

   public AppStartupInfo(long startTime, String[] programArgs) {
      this.startTime = startTime;
      this.programArgs = programArgs;
   }

   public AppStartupInfo(long startTime) {
      this(startTime, StringUtil.EMPTY_STRING_ARRAY);
   }

   public long getStartTime() {
      return this.startTime;
   }

   public String[] getProgramArgs() {
      return this.programArgs;
   }
}
