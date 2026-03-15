package com.projectgoth.fusion.gateway.exceptions;

public class FusionRequestException extends Exception {
   private static final long serialVersionUID = 1L;
   FusionRequestException.ExceptionType type;

   public FusionRequestException(FusionRequestException.ExceptionType type, String s) {
      super(s);
      this.type = type;
   }

   public static enum ExceptionType {
      PREVALIDATION;
   }
}
