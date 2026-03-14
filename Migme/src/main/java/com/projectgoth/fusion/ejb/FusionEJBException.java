package com.projectgoth.fusion.ejb;

public class FusionEJBException extends Exception {
   public FusionEJBException(String message) {
      super(message);
   }

   public FusionEJBException(String message, Throwable cause) {
      super(message, cause);
   }
}
