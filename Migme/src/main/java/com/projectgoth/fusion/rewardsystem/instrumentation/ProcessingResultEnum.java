package com.projectgoth.fusion.rewardsystem.instrumentation;

public enum ProcessingResultEnum {
   FAILED(-2),
   DROPPED(-1),
   SUCCESSFUL(1);

   private int code;

   private ProcessingResultEnum(int code) {
      this.code = code;
   }

   public int getCode() {
      return this.code;
   }
}
