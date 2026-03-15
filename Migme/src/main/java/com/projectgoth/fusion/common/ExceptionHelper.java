package com.projectgoth.fusion.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Vector;

public class ExceptionHelper {
   public static String getRawRootMessage(Exception e) {
      Throwable exception = e;

      for(Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
         exception = cause;
      }

      String message = ((Throwable)exception).getMessage();
      return message == null ? e.getClass().getName() : message;
   }

   public static String getRootMessage(Exception e) {
      return "EJBException:" + getRawRootMessage(e);
   }

   public static Hashtable getRootMessageAsHashtable(Exception e) {
      Throwable exception = e;

      for(Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
         exception = cause;
      }

      String message = ((Throwable)exception).getMessage();
      String returnMessage = "";
      if (message == null) {
         message = e.getClass().getName();
      }

      Hashtable hash = new Hashtable();
      hash.put("EJBException", message);
      return hash;
   }

   public static Vector getRootMessageAsVector(Exception e) {
      Throwable exception = e;

      for(Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
         exception = cause;
      }

      String message = ((Throwable)exception).getMessage();
      String returnMessage = "";
      if (message == null) {
         message = e.getClass().getName();
      }

      Vector error = new Vector();
      Hashtable hash = new Hashtable();
      hash.put("EJBException", message);
      error.add(hash);
      return error;
   }

   public static String setErrorMessage(String errorString) {
      return "EJBException:" + errorString;
   }

   public static String removeErrorMessagePrefix(String errorString) {
      return errorString.indexOf("EJBException:") == 0 ? errorString.substring("EJBException:".length()) : errorString;
   }

   public static Hashtable setErrorMessageAsHashtable(String errorString) {
      Hashtable hash = new Hashtable();
      hash.put("EJBException", errorString);
      return hash;
   }

   public static Vector setErrorMessageAsVector(String errorString) {
      Vector error = new Vector();
      Hashtable hash = new Hashtable();
      hash.put("EJBException", errorString);
      error.add(hash);
      return error;
   }

   public static StringBuilder appendStackTrace(Throwable ex, StringBuilder target) {
      if (ex == null) {
         return target.append(ex);
      } else {
         StringWriter stringWriter = new StringWriter();
         PrintWriter pw = new PrintWriter(stringWriter);

         try {
            ex.printStackTrace(pw);
            pw.flush();
         } finally {
            pw.close();
         }

         target.append(stringWriter.getBuffer());
         return target;
      }
   }
}
