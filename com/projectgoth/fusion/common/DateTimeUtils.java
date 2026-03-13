package com.projectgoth.fusion.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;

public abstract class DateTimeUtils {
   private static final Logger logger = Logger.getLogger(ConfigUtils.getLoggerName(DateTimeUtils.class));
   public static long ONE_MINUTE_IN_MS = 60000L;
   public static long FIVE_MINUTES_IN_MS;
   public static long ONE_HOUR_IN_MS;
   public static long TWENTY_FOUR_HOURS_IN_MS;
   public static int MINUTES_IN_DAY;
   public static int SECONDS_IN_DAY;
   private static final String MIGCORE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
   public static final ThreadLocal<SimpleDateFormat> OFFLINE_MSG_KEY_DATE_FORMAT;
   private static final ThreadLocal<SimpleDateFormat> TL_FORMAT_DATE_FOR_MIGBO;
   private static final ThreadLocal<SimpleDateFormat> TL_FORMAT_DATE_ISO8601_DATE_ONLY;
   private static final ThreadLocal<SimpleDateFormat> TL_FORMAT_DATE_FOR_MIGCORE;
   private static final ThreadLocal<SimpleDateFormat> TL_FORMAT_DATE_FOR_REGISTRATION_CONTEXT;
   private static final ThreadLocal<SimpleDateFormat> TL_FORMAT_DATE_FOR_PAYMENT_TRANSACTIONS;
   private static final ThreadLocal<SimpleDateFormat> TL_FORMAT_DATE_FOR_TIMESTAMP;
   private static final ThreadLocal<SimpleDateFormat> TL_FORMAT_UTC_DATE_TIME;
   private static final ThreadLocal<SimpleDateFormat> TL_FORMAT_UTC_DATE;
   private static final ThreadLocal<SimpleDateFormat> TL_FORMAT_DATE_FOR_INVITATIONS;
   private static final ThreadLocal<SimpleDateFormat> TL_FORMAT_DATE_FOR_PAYMENT_RATE_LIMIT_PREALLOCATION;

   public static Date midnightYesterday() {
      Calendar today = Calendar.getInstance();
      today.set(11, 0);
      today.set(12, 0);
      today.set(13, 0);
      today.set(14, 0);
      today.add(6, -1);
      return new Date(today.getTimeInMillis());
   }

   public static Date midnightToday() {
      Calendar today = Calendar.getInstance();
      today.set(11, 0);
      today.set(12, 0);
      today.set(13, 0);
      today.set(14, 0);
      return new Date(today.getTimeInMillis());
   }

   public static Date midnightTomorrow() {
      Calendar today = Calendar.getInstance();
      today.set(11, 0);
      today.set(12, 0);
      today.set(13, 0);
      today.set(14, 0);
      today.add(11, 24);
      return new Date(today.getTimeInMillis());
   }

   public static Date midnightTomorrow(int calendarField, int amount) {
      Calendar today = Calendar.getInstance();
      today.set(11, 0);
      today.set(12, 0);
      today.set(13, 0);
      today.set(14, 0);
      today.add(11, 24);
      today.add(calendarField, amount);
      return new Date(today.getTimeInMillis());
   }

   public static Date midnightOnDate(Date date) {
      Calendar day = Calendar.getInstance();
      day.setTime(date);
      day.set(11, 0);
      day.set(12, 0);
      day.set(13, 0);
      day.set(14, 0);
      return new Date(day.getTimeInMillis());
   }

   public static Date minutesFromNow(int minutes) {
      Calendar today = Calendar.getInstance();
      today.add(12, minutes);
      return new Date(today.getTimeInMillis());
   }

   public static Date daysFromNow(int days) {
      Calendar today = Calendar.getInstance();
      today.add(12, days * MINUTES_IN_DAY);
      return new Date(today.getTimeInMillis());
   }

   public static Date minusDays(Date date, int days) {
      Calendar today = Calendar.getInstance();
      today.setTime(date);
      today.add(6, -1 * days);
      return new Date(today.getTimeInMillis());
   }

   public static Date plusDays(Date date, int days) {
      Calendar today = Calendar.getInstance();
      today.setTime(date);
      today.add(6, days);
      return new Date(today.getTimeInMillis());
   }

   public static Date minusMinutes(Date date, int minutes) {
      Calendar today = Calendar.getInstance();
      today.setTime(date);
      today.add(12, -1 * minutes);
      return new Date(today.getTimeInMillis());
   }

   public static Date plusMinutes(Date date, int minutes) {
      Calendar today = Calendar.getInstance();
      today.setTime(date);
      today.add(12, minutes);
      return new Date(today.getTimeInMillis());
   }

   public static Date stringToDate(String dateString, String pattern, String timeZoneID) throws Exception {
      Date date = null;
      DateFormat formatter = new SimpleDateFormat(pattern);
      if (!StringUtils.isEmpty(timeZoneID)) {
         formatter.setTimeZone(TimeZone.getTimeZone(timeZoneID));
      }

      date = formatter.parse(dateString);
      return date;
   }

   public static String dateToString(Date date, String pattern) {
      DateFormat formatter = new SimpleDateFormat(pattern);
      return formatter.format(date);
   }

   public static String timeInvertalInSecondsToTimeString(int seconds) {
      if (seconds >= SECONDS_IN_DAY) {
         throw new IllegalArgumentException(String.format("unable to format time interval that's more than a day: %d", seconds));
      } else {
         int sec = seconds % 60;
         int r = seconds / 60;
         int min = r % 60;
         r /= 60;
         return String.format("%02d:%02d:%02d", r, min, sec);
      }
   }

   public static String timeInvertalInSecondsToPrettyString(int seconds) {
      if (seconds >= SECONDS_IN_DAY) {
         throw new IllegalArgumentException(String.format("unable to format time interval that's more than a day: %d", seconds));
      } else if (seconds == 0) {
         return "0 second";
      } else {
         int sec = seconds % 60;
         int r = seconds / 60;
         int min = r % 60;
         r /= 60;
         StringBuffer sb = new StringBuffer(32);
         if (r > 0) {
            sb.append(r);
            sb.append(' ');
            sb.append(r == 1 ? "hour" : "hours");
         }

         if (min > 0) {
            if (sb.length() > 0) {
               sb.append(' ');
            }

            sb.append(min);
            sb.append(' ');
            sb.append(min == 1 ? "minute" : "minutes");
         }

         if (sec > 0) {
            if (sb.length() > 0) {
               sb.append(' ');
            }

            sb.append(sec);
            sb.append(' ');
            sb.append(sec == 1 ? "second" : "seconds");
         }

         return sb.toString();
      }
   }

   public static Date getDateCurrentTimeZone(Calendar calendar, String timeZoneID) {
      Date result = null;
      int month = calendar.get(2) + 1;
      int day = calendar.get(5);
      int year = calendar.get(1);
      int hours = calendar.get(11);
      int minutes = calendar.get(12);
      int seconds = calendar.get(13);
      String pattern = "MM/dd/yyyy HH:mm:ss";
      String dateAsString = month + "/" + day + "/" + year + " " + hours + ":" + minutes + ":" + seconds;

      try {
         result = stringToDate(dateAsString, pattern, timeZoneID);
      } catch (Exception var12) {
         logger.warn("Error converting date = " + dateAsString + ", pattern = '" + pattern + "', timeZoneID = " + timeZoneID, var12);
      }

      return result;
   }

   public static String getRemainingTime(Date targetDate) {
      long timeRemaining = targetDate.getTime() - (new Date()).getTime();
      if (timeRemaining < 0L) {
         logger.warn("Error calculating remaining time. Target date is in the past.");
         return null;
      } else {
         StringBuilder formattedString = new StringBuilder();

         try {
            long minutes = timeRemaining / 60000L % 60L;
            long hours = timeRemaining / 3600000L % 24L;
            long days = timeRemaining / 86400000L;
            if (days > 0L) {
               formattedString.append(days).append(days == 1L ? " day" : " days");
            }

            if (hours > 0L) {
               if (days > 0L) {
                  formattedString.append(", ");
               }

               formattedString.append(hours).append(hours == 1L ? " hour" : " hours");
            }

            if (minutes > 0L) {
               if (hours > 0L) {
                  formattedString.append(", ");
               }

               formattedString.append(minutes).append(minutes == 1L ? " minute" : " minutes");
            }
         } catch (ArithmeticException var10) {
            logger.error("Error calculating remaining time.", var10);
         }

         return formattedString.toString();
      }
   }

   public static String getTimeSince(Date date) {
      long timeSince = (new Date()).getTime() - date.getTime();
      if (timeSince < 0L) {
         logger.warn("Error calculating time since. Target date is in the future.");
         return null;
      } else {
         StringBuilder formattedString = new StringBuilder();

         try {
            long minutes = timeSince / 60000L % 60L;
            long hours = timeSince / 3600000L % 24L;
            long days = timeSince / 86400000L;
            if (days > 0L) {
               formattedString.append(days).append(days == 1L ? " day" : " days");
            } else if (hours > 0L) {
               if (days > 0L) {
                  formattedString.append(", ");
               }

               formattedString.append(hours).append(hours == 1L ? " hr" : " hrs");
            } else if (minutes > 0L) {
               if (hours > 0L) {
                  formattedString.append(", ");
               }

               formattedString.append(minutes).append(minutes == 1L ? " min" : " mins");
            } else {
               formattedString.append("A moment");
            }

            formattedString.append(" ago");
         } catch (ArithmeticException var10) {
            logger.error("Error calculating remaining time.", var10);
         }

         return formattedString.toString();
      }
   }

   public static String getStringFromEpocMsForMigbo(long timeMs) {
      return ((SimpleDateFormat)TL_FORMAT_DATE_FOR_MIGBO.get()).format(new Date(timeMs));
   }

   public static String getStringForMigbo(Date date) {
      return date == null ? null : ((SimpleDateFormat)TL_FORMAT_DATE_FOR_MIGBO.get()).format(date);
   }

   public static String getStringForTimestamp(Date ts) {
      return ts == null ? null : ((SimpleDateFormat)TL_FORMAT_DATE_FOR_TIMESTAMP.get()).format(ts);
   }

   public static String getStringForUTCDate(Date ts) {
      return ts == null ? null : ((SimpleDateFormat)TL_FORMAT_UTC_DATE.get()).format(ts);
   }

   public static String getStringForUTCDateTime(Date ts) {
      return ts == null ? null : ((SimpleDateFormat)TL_FORMAT_UTC_DATE_TIME.get()).format(ts);
   }

   public static Date getTimestamp(String tsString) throws ParseException {
      return tsString == null ? null : ((SimpleDateFormat)TL_FORMAT_DATE_FOR_TIMESTAMP.get()).parse(tsString);
   }

   public static Date getUTCDate(String tsString) throws ParseException {
      return tsString == null ? null : ((SimpleDateFormat)TL_FORMAT_UTC_DATE.get()).parse(tsString);
   }

   public static Date getUTCDateTime(String tsString) throws ParseException {
      return tsString == null ? null : ((SimpleDateFormat)TL_FORMAT_UTC_DATE_TIME.get()).parse(tsString);
   }

   public static Date getDateWithoutTimeFromISO8601DateString(String dateStr) throws ParseException {
      if (dateStr != null && dateStr.length() >= 10) {
         return ((SimpleDateFormat)TL_FORMAT_DATE_ISO8601_DATE_ONLY.get()).parse(dateStr.substring(0, 10));
      } else {
         throw new ParseException("Expecting date string of the format YYYY-MM-DD HH:mm:ss, found [" + dateStr + "] instead.", 0);
      }
   }

   public static String getStringDateWithoutTime(Date date) {
      return date == null ? null : ((SimpleDateFormat)TL_FORMAT_DATE_ISO8601_DATE_ONLY.get()).format(date);
   }

   public static String getStringForRegistrationContext(Date date) {
      return date == null ? null : ((SimpleDateFormat)TL_FORMAT_DATE_FOR_REGISTRATION_CONTEXT.get()).format(date);
   }

   public static Date getDateForRegistrationContext(String dateStr) throws ParseException {
      return dateStr == null ? null : ((SimpleDateFormat)TL_FORMAT_DATE_FOR_REGISTRATION_CONTEXT.get()).parse(dateStr);
   }

   public static String getStringForMigcore(Date date) {
      return date == null ? null : ((SimpleDateFormat)TL_FORMAT_DATE_FOR_MIGCORE.get()).format(date);
   }

   public static Long getTimeInMilisecondsFromString(String date) throws ParseException {
      if (date == null) {
         return null;
      } else {
         Date dt = ((SimpleDateFormat)TL_FORMAT_DATE_FOR_MIGCORE.get()).parse(date);
         return dt.getTime();
      }
   }

   public static String getStringForPaymentTransactionTime(Date date) {
      return date == null ? null : ((SimpleDateFormat)TL_FORMAT_DATE_FOR_PAYMENT_TRANSACTIONS.get()).format(date);
   }

   public static Date getPaymentTransactionTimeFromString(String str) throws ParseException {
      return str == null ? null : ((SimpleDateFormat)TL_FORMAT_DATE_FOR_PAYMENT_TRANSACTIONS.get()).parse(str);
   }

   public static long getElapsedTimeInSeconds(Date now, Date since) {
      return (now.getTime() - since.getTime()) / 1000L;
   }

   public static String getStringForInvitationTime(Date date) {
      return date == null ? null : ((SimpleDateFormat)TL_FORMAT_DATE_FOR_INVITATIONS.get()).format(date);
   }

   public static Date getInvitationTimeFromString(String str) throws ParseException {
      return str == null ? null : ((SimpleDateFormat)TL_FORMAT_DATE_FOR_INVITATIONS.get()).parse(str);
   }

   public static String getStringForPaymentRateLimitPreallocationTime(Date date) {
      return date == null ? null : ((SimpleDateFormat)TL_FORMAT_DATE_FOR_PAYMENT_RATE_LIMIT_PREALLOCATION.get()).format(date);
   }

   public static void main(String[] args) {
      try {
         String dateString = "4/18/2009 18:00";
         String pattern = "MM/dd/yyyy HH:mm";
         String timeZoneID = "GMT";
         Date date = stringToDate(dateString, pattern, timeZoneID);
         System.out.println("String: " + dateString + " converts to " + date);
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public static long getEpocMsFromDateString(String dateStr) {
      if (StringUtil.isBlank(dateStr)) {
         return -1L;
      } else {
         try {
            return ((SimpleDateFormat)TL_FORMAT_DATE_FOR_MIGBO.get()).parse(dateStr).getTime();
         } catch (ParseException var2) {
            return -1L;
         }
      }
   }

   static {
      FIVE_MINUTES_IN_MS = ONE_MINUTE_IN_MS * 5L;
      ONE_HOUR_IN_MS = ONE_MINUTE_IN_MS * 60L;
      TWENTY_FOUR_HOURS_IN_MS = ONE_HOUR_IN_MS * 24L;
      MINUTES_IN_DAY = 1440;
      SECONDS_IN_DAY = 86400;
      OFFLINE_MSG_KEY_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
         protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy.MM.dd");
         }
      };
      TL_FORMAT_DATE_FOR_MIGBO = new ThreadLocal<SimpleDateFormat>() {
         protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
         }
      };
      TL_FORMAT_DATE_ISO8601_DATE_ONLY = new ThreadLocal<SimpleDateFormat>() {
         protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
         }
      };
      TL_FORMAT_DATE_FOR_MIGCORE = new ThreadLocal<SimpleDateFormat>() {
         protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         }
      };
      TL_FORMAT_DATE_FOR_REGISTRATION_CONTEXT = new ThreadLocal<SimpleDateFormat>() {
         protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
         }
      };
      TL_FORMAT_DATE_FOR_PAYMENT_TRANSACTIONS = new ThreadLocal<SimpleDateFormat>() {
         protected SimpleDateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            return sdf;
         }
      };
      TL_FORMAT_DATE_FOR_TIMESTAMP = new ThreadLocal<SimpleDateFormat>() {
         protected SimpleDateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            return sdf;
         }
      };
      TL_FORMAT_UTC_DATE_TIME = new ThreadLocal<SimpleDateFormat>() {
         protected SimpleDateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            return sdf;
         }
      };
      TL_FORMAT_UTC_DATE = new ThreadLocal<SimpleDateFormat>() {
         protected SimpleDateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            return sdf;
         }
      };
      TL_FORMAT_DATE_FOR_INVITATIONS = new ThreadLocal<SimpleDateFormat>() {
         protected SimpleDateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            return sdf;
         }
      };
      TL_FORMAT_DATE_FOR_PAYMENT_RATE_LIMIT_PREALLOCATION = new ThreadLocal<SimpleDateFormat>() {
         protected SimpleDateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            return sdf;
         }
      };
   }
}
