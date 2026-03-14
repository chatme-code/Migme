package com.projectgoth.fusion.common;

import java.util.Arrays;

public class ExceptionWithErrorCause extends Exception {
   private ErrorCause errorCause;
   private Object[] errorMsgArgs;

   public ExceptionWithErrorCause(ErrorCause reasonType, Object... errorMsgArgs) {
      super(formatErrorMessage(reasonType.getDefaultErrorMessage(), errorMsgArgs));
      this.errorCause = reasonType;
      this.errorMsgArgs = errorMsgArgs;
   }

   public ExceptionWithErrorCause(Throwable cause, ErrorCause reasonType, Object... errorMsgArgs) {
      super(formatErrorMessage(reasonType.getDefaultErrorMessage(), errorMsgArgs), cause);
      this.errorCause = reasonType;
      this.errorMsgArgs = errorMsgArgs;
   }

   private static String formatErrorMessage(String format, Object... args) {
      return args != null && args.length != 0 ? String.format(format, args) : format;
   }

   public ErrorCause getErrorCause() {
      return this.errorCause;
   }

   public Object[] getErrorMsgArgs() {
      return this.errorMsgArgs;
   }

   public String toString() {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(super.toString());
      if (this.errorCause != null) {
         stringBuilder.append("\n");
         stringBuilder.append("ReasonCode:").append(this.errorCause.getCode());
         if (this.errorMsgArgs != null) {
            stringBuilder.append("\n");
            stringBuilder.append("ReasonData:").append(Arrays.asList(this.errorMsgArgs));
         }

         stringBuilder.append("\n");
      }

      return stringBuilder.toString();
   }
}
