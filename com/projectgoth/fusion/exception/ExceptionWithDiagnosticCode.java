package com.projectgoth.fusion.exception;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.FusionException;
import org.apache.log4j.Logger;

public class ExceptionWithDiagnosticCode extends FusionException {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ExceptionWithDiagnosticCode.class));
   private static final String PREFIX = " (";
   private static final String SUFFIX = ")";

   public ExceptionWithDiagnosticCode(String message, Exception rootException, String contextInfo) {
      super(message + " (" + makeObfuscatedErrorCode(rootException, contextInfo) + ")");
   }

   public static String makeObfuscatedErrorCode(Exception rootException, String contextInfo) {
      return Integer.toString(Math.abs(makeErrorCode(rootException, contextInfo).hashCode()));
   }

   private static String makeErrorCode(Exception rootException, String contextInfo) {
      return stackTraceConcat(Thread.currentThread().getStackTrace()) + rootException + rootException.getMessage() + stackTraceConcat(rootException.getStackTrace()) + contextInfo;
   }

   private static String stackTraceConcat(StackTraceElement[] ste) {
      String concat = "";
      StackTraceElement[] arr$ = ste;
      int len$ = ste.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         StackTraceElement elem = arr$[i$];
         concat = concat + elem.toString();
      }

      return concat;
   }
}
