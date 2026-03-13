package com.projectgoth.fusion.common;

import javax.ejb.EJBException;

public class EJBExceptionWithErrorCause extends EJBException {
   private ErrorCause errorCause;
   private Object[] errorMsgArgs;

   public EJBExceptionWithErrorCause(ErrorCause reasonType, Object... errorMsgArgs) {
      super(formatErrorMessage(reasonType.getDefaultErrorMessage(), errorMsgArgs));
      this.errorCause = reasonType;
      this.errorMsgArgs = errorMsgArgs;
   }

   public EJBExceptionWithErrorCause(Throwable t, ErrorCause reasonType, Object... errorMsgArgs) {
      super(formatErrorMessage(reasonType.getDefaultErrorMessage(), errorMsgArgs));
      this.initCause(t);
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
}
