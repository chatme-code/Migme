package com.projectgoth.fusion.bl1;

public class BlueLabelError {
   private long time;
   private BlueLabelResponseCodes errorCode;

   public BlueLabelError(long time, BlueLabelResponseCodes errorCode) {
      this.time = time;
      this.errorCode = errorCode;
   }

   public long getTime() {
      return this.time;
   }

   public BlueLabelResponseCodes getErrorCode() {
      return this.errorCode;
   }

   public boolean isOlderThanHours(int hours) {
      return System.currentTimeMillis() - this.time > (long)(hours * 1000 * 60 * 60);
   }

   public static void main(String[] args) {
      BlueLabelError error = new BlueLabelError(System.currentTimeMillis() - 25200000L, BlueLabelResponseCodes.SERVICE_DOWN);
      System.out.println(error.isOlderThanHours(6));
   }
}
