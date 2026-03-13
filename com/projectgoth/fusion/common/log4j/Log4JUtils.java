package com.projectgoth.fusion.common.log4j;

import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Log4JUtils {
   public static void log(Logger logger, Level logLevel, Object logObject) {
      log(logger, logLevel, logObject, (Throwable)null);
   }

   public static void log(Logger logger, Level logLevel, Object logObject, Throwable t) {
      int logLevelInt = logLevel == null ? 20000 : logLevel.toInt();
      switch(logLevelInt) {
      case 10000:
         if (t != null) {
            logger.debug(logObject, t);
         } else {
            logger.debug(logObject);
         }
         break;
      case 20000:
      default:
         if (t != null) {
            logger.info(logObject, t);
         } else {
            logger.info(logObject);
         }
         break;
      case 30000:
         if (t != null) {
            logger.warn(logObject, t);
         } else {
            logger.warn(logObject);
         }
         break;
      case 40000:
         if (t != null) {
            logger.error(logObject, t);
         } else {
            logger.error(logObject);
         }
         break;
      case 50000:
         if (t != null) {
            logger.fatal(logObject, t);
         } else {
            logger.fatal(logObject);
         }
      }

   }

   public static Logger getLogger(Class<?> clazz) {
      return Logger.getLogger(ConfigUtils.getLoggerName(clazz));
   }
}
