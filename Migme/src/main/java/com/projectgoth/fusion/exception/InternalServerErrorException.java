package com.projectgoth.fusion.exception;

public class InternalServerErrorException extends ExceptionWithDiagnosticCode {
   private static final String MSG = "Internal Server Error";

   public InternalServerErrorException(Exception rootException, String contextInfo) {
      super("Internal Server Error", rootException, contextInfo);
   }
}
