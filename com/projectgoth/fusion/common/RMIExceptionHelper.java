package com.projectgoth.fusion.common;

import java.rmi.RemoteException;

public class RMIExceptionHelper {
   public static String getRootMessage(RemoteException e) {
      Throwable exception = e;

      for(Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
         exception = cause;
      }

      String message = ((Throwable)exception).getMessage();
      return message == null ? e.getClass().getName() : message;
   }
}
