package com.projectgoth.fusion.mogilefs;

public class MogileException extends Exception {
   public MogileException() {
   }

   public MogileException(String message) {
      super(message);
   }

   public MogileException(String message, Throwable cause) {
      super(message, cause);
   }

   public MogileException(Throwable cause) {
      super(cause);
   }
}
