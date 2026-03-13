package com.projectgoth.fusion.common;

import Ice.Logger;

public class IceLog4jLogger implements Logger {
   private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IceLog4jLogger.class);

   public void error(String message) {
      log.error(message);
   }

   public void print(String message) {
      log.info(message);
   }

   public void trace(String category, String message) {
      log.debug(category + ": " + message);
   }

   public void warning(String message) {
      log.warn(message);
   }
}
