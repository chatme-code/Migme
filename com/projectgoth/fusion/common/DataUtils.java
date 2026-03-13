package com.projectgoth.fusion.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class DataUtils {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DataUtils.class));
   public static final int MAX_MOBILE_DEVICE_LENGTH = 128;
   public static final int MAX_USERAGENT_LENGTH = 128;
   private static final Pattern USERAGENT_PATTERN = Pattern.compile("(.+)(mig33/.*)");

   public static String truncateMobileDevice(String mobileDevice) {
      return truncateMobileDevice(mobileDevice, false, (String)null);
   }

   public static String truncateMobileDevice(String mobileDevice, boolean logTruncation, String logContext) {
      if (mobileDevice != null && mobileDevice.length() > 128) {
         String newValue = mobileDevice.substring(0, 128);
         if (logTruncation) {
            String logMsg = String.format("%s: mobileDevice value longer than %d, truncating it from '%s' to '%s'", logContext, 128, mobileDevice, newValue);
            log.info(logMsg);
         }

         return newValue;
      } else {
         return mobileDevice;
      }
   }

   public static String truncateUserAgent(String userAgent) {
      return truncateUserAgent(userAgent, false, (String)null);
   }

   public static String truncateUserAgent(String userAgent, boolean logTruncation, String logContext) {
      if (userAgent != null && userAgent.length() > 128) {
         String newValue = userAgent.substring(0, 128);
         if (!userAgent.startsWith("mig33")) {
            Matcher m = USERAGENT_PATTERN.matcher(userAgent);
            if (m.matches()) {
               if (m.group(2).length() > 128) {
                  newValue = m.group(2).substring(0, 128);
               } else if (m.group(2).length() >= 123) {
                  newValue = " ... ".substring(5 - (128 - m.group(2).length()), 5) + m.group(2);
               } else {
                  newValue = m.group(1).substring(0, 123 - m.group(2).length()) + " ... " + m.group(2);
               }
            }
         }

         if (logTruncation) {
            String logMsg = String.format("%s: userAgent value longer than %d, truncating it from '%s' to '%s'", logContext, 128, userAgent, newValue);
            log.info(logMsg);
         }

         return newValue;
      } else {
         return userAgent;
      }
   }
}
