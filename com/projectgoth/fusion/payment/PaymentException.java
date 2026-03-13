package com.projectgoth.fusion.payment;

import com.projectgoth.fusion.common.ErrorCause;
import java.util.Arrays;
import javax.ejb.EJBException;

public class PaymentException extends EJBException {
   private ErrorCause errorCause;
   private Object[] errorMsgArgs;

   public PaymentException(ErrorCause reasonType, Object... errorMsgArgs) {
      super(formatErrorMessage(reasonType.getDefaultErrorMessage(), errorMsgArgs));
      this.errorCause = reasonType;
      this.errorMsgArgs = errorMsgArgs;
   }

   public PaymentException(Throwable cause, ErrorCause reasonType, Object... errorMsgArgs) {
      super(formatErrorMessage(reasonType.getDefaultErrorMessage(), errorMsgArgs), castOrWrapToException(cause));
      this.errorCause = reasonType;
      this.errorMsgArgs = errorMsgArgs;
   }

   private static Exception castOrWrapToException(Throwable t) {
      if (t == null) {
         return null;
      } else {
         return t instanceof Exception ? (Exception)t : new Exception("Throwable exception caught", t);
      }
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
