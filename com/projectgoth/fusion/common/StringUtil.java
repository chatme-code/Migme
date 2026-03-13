package com.projectgoth.fusion.common;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class StringUtil {
   public static final StringUtil.ToStringConverter<Object> DEFAULT_TO_STRING_CONVERTER = new StringUtil.ToStringConverter<Object>() {
      public String convert(Object object) {
         return object == null ? "null" : object.toString();
      }
   };
   private static final Logger log = Logger.getLogger(StringUtil.class);
   public static final String EMPTY_STRING = "";
   public static final int[] VOWELS = new int[]{65, 69, 73, 79, 85, 97, 101, 105, 111, 117};
   public static final Pattern WORD_CHARACTER = Pattern.compile("[a-zA-Z]");
   public static final Pattern DIGIT_CHARACTER = Pattern.compile("\\d");
   public static final Pattern VALID_MIG33_USERNAME_OLD_STYLE = Pattern.compile("^[a-zA-Z]{1}(\\.{0,1}[\\w-]){1,}$");
   public static final String ALLOWED_CURRENCY_STRING = "^[0-9]+(\\.[0-9]{0,2})?$";
   public static final String[] EMPTY_STRING_ARRAY = new String[0];
   public static final Map<String, String> EMPTY_STRING_MAP = new HashMap();
   private static volatile String stripPatternStringForEnqueueLog = null;
   private static volatile Pattern compiledStripPatternForEnqueueLog = null;
   private static final String DEFAULT_STRIP_PATTERN_FOR_ENQUEUE_LOG = "\r|\n|\\|";
   private static final SecureRandom random = new SecureRandom();

   public static boolean equals(String str1, String str2) {
      return str1 == null && str2 == null || str1 != null && str1.equals(str2);
   }

   public static boolean equalsIgnoreCase(String str1, String str2) {
      return str1 == null && str2 == null || str1 != null && str1.equalsIgnoreCase(str2);
   }

   public static String join(Object[] objects, String seperator) {
      return objects == null ? "" : join((Collection)Arrays.asList(objects), seperator);
   }

   public static String join(Collection<? extends Object> objects, String seperator) {
      return join(objects, seperator, DEFAULT_TO_STRING_CONVERTER);
   }

   public static <T> String join(Collection<? extends T> objects, String seperator, StringUtil.ToStringConverter<T> converter) {
      StringBuilder builder = new StringBuilder();
      Object object;
      if (objects != null) {
         for(Iterator i$ = objects.iterator(); i$.hasNext(); builder.append(converter.convert(object))) {
            object = i$.next();
            if (builder.length() > 0) {
               builder.append(seperator);
            }
         }
      }

      return builder.toString();
   }

   public static String repeat(String string, int count) {
      StringBuilder builder = new StringBuilder();

      for(int i = 0; i < count; ++i) {
         builder.append(string);
      }

      return builder.toString();
   }

   public static String padLeft(String string, char paddingChar, int totalLength) {
      int missing = totalLength - string.length();
      return missing > 0 ? repeat(String.valueOf(paddingChar), missing) + string : string;
   }

   public static String stripHTML(String input) {
      if (input == null) {
         return null;
      } else {
         StringBuilder sb = new StringBuilder();
         int inTag = 0;

         for(int i = 0; i < input.length(); ++i) {
            if (input.charAt(i) == '<') {
               ++inTag;
            } else if (input.charAt(i) == '>') {
               --inTag;
            } else if (inTag <= 0) {
               sb.append(input.charAt(i));
            }
         }

         return sb.toString();
      }
   }

   public static boolean startsWithaVowel(String input) {
      if (!StringUtils.hasLength(input)) {
         return false;
      } else {
         char firstChar = input.charAt(0);
         return Arrays.binarySearch(VOWELS, firstChar) >= 0;
      }
   }

   public static String asString(Collection<String> strings) {
      if (strings != null && strings.size() != 0) {
         StringBuilder builder = new StringBuilder();
         Iterator i$ = strings.iterator();

         while(i$.hasNext()) {
            String string = (String)i$.next();
            builder.append("'").append(string).append("'").append(",");
         }

         String result = builder.toString();
         return result.substring(0, result.length() - 1);
      } else {
         return "";
      }
   }

   public static String asString(String[] strings) {
      if (strings != null && strings.length != 0) {
         StringBuilder builder = new StringBuilder();
         String[] arr$ = strings;
         int len$ = strings.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String string = arr$[i$];
            builder.append("'").append(string).append("'").append(",");
         }

         String result = builder.toString();
         return result.substring(0, result.length() - 1);
      } else {
         return "";
      }
   }

   public static String asStringWithoutQuotes(String[] strings) {
      if (strings != null && strings.length != 0) {
         StringBuilder builder = new StringBuilder();
         String[] arr$ = strings;
         int len$ = strings.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String string = arr$[i$];
            builder.append(string).append(",");
         }

         String result = builder.toString();
         return result.substring(0, result.length() - 1);
      } else {
         return "";
      }
   }

   public static String asString(Object[] objects) {
      if (objects != null && objects.length != 0) {
         StringBuilder builder = new StringBuilder();
         Object[] arr$ = objects;
         int len$ = objects.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Object object = arr$[i$];
            builder.append("'").append(object.toString()).append("'").append(",");
         }

         String result = builder.toString();
         return result.substring(0, result.length() - 1);
      } else {
         return "";
      }
   }

   public static String asStringWithoutQuotes(int[] ints) {
      if (ints != null && ints.length != 0) {
         StringBuilder builder = new StringBuilder();
         int[] arr$ = ints;
         int len$ = ints.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            int anint = arr$[i$];
            builder.append(anint).append(",");
         }

         String result = builder.toString();
         return result.substring(0, result.length() - 1);
      } else {
         return "";
      }
   }

   public static String[] asArray(String arrayString, String delimeter) {
      if (!StringUtils.hasLength(arrayString)) {
         return new String[0];
      } else {
         String[] results = arrayString.split(delimeter);

         for(int i = 0; i < results.length; ++i) {
            results[i] = results[i].substring(1, results[i].length() - 1);
         }

         return results;
      }
   }

   public static String[] asArray(String arrayString) {
      return asArray(arrayString, ",");
   }

   public static boolean containsWordCharacter(String input) {
      Matcher matcher = WORD_CHARACTER.matcher(input);
      return matcher.find();
   }

   public static boolean containsDigitCharacter(String input) {
      Matcher matcher = DIGIT_CHARACTER.matcher(input);
      return matcher.find();
   }

   public static List<String> split(String input, char delimeter) {
      List<String> split = new ArrayList();
      if (!StringUtils.hasLength(input)) {
         return split;
      } else {
         int start = 0;

         int nextDelimeterIndex;
         for(boolean var4 = false; start < input.length(); start = nextDelimeterIndex + 1) {
            nextDelimeterIndex = input.indexOf(delimeter, start);
            if (nextDelimeterIndex < 0) {
               split.add(input.substring(start, input.length()));
               break;
            }

            if (nextDelimeterIndex - start < 1) {
               split.add("");
            } else {
               split.add(input.substring(start, nextDelimeterIndex));
            }
         }

         return split;
      }
   }

   public static String maskString(String input, int numShown, char maskChar) {
      char[] cArray = input.toCharArray();

      for(int i = 0; i < cArray.length; ++i) {
         if (i > numShown) {
            cArray[i] = maskChar;
         }
      }

      return new String(cArray);
   }

   public static String[] splitIntoArray(String input, char delimeter) {
      return (String[])split(input, delimeter).toArray(new String[0]);
   }

   public static String scrubDoubleAmount(String value) {
      StringBuilder builder = new StringBuilder();
      int count = -1;

      while(true) {
         char charAt;
         do {
            ++count;
            if (count >= value.length()) {
               return builder.toString();
            }

            charAt = value.charAt(count);
         } while((charAt < '0' || charAt > '9') && charAt != '.');

         builder.append(charAt);
      }
   }

   public static boolean iceIsBlank(String str) {
      return isBlank(str) ? true : str.equals("\u0000");
   }

   public static boolean isBlank(String str) {
      int strLen;
      if (str != null && (strLen = str.length()) != 0) {
         for(int i = 0; i < strLen; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public static String truncateWithEllipsis(String input, int maxLength) {
      if (isBlank(input)) {
         return input;
      } else if (input.length() <= maxLength) {
         return input;
      } else {
         StringBuffer sb = new StringBuffer(maxLength);
         sb.append(input.substring(0, maxLength - 3));
         sb.append("...");
         return sb.toString();
      }
   }

   public static String customizeStringForLogging(String source, int maxLength) {
      if (isBlank(source)) {
         return source;
      } else {
         String tempStripStr = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.STRIP_PATTERN_FOR_ENQUEUE_LOG);
         if (isBlank(tempStripStr)) {
            return truncateWithEllipsis(source, maxLength);
         } else {
            if (!tempStripStr.equals(stripPatternStringForEnqueueLog)) {
               stripPatternStringForEnqueueLog = tempStripStr;

               try {
                  compiledStripPatternForEnqueueLog = Pattern.compile(stripPatternStringForEnqueueLog);
               } catch (Exception var4) {
                  log.warn(String.format("Failed to compile regex:%s, use default regex:%s instead", tempStripStr, "\r|\n|\\|"), var4);
                  compiledStripPatternForEnqueueLog = Pattern.compile("\r|\n|\\|");
               }
            }

            Matcher m = compiledStripPatternForEnqueueLog.matcher(source);
            return truncateWithEllipsis(m.replaceAll(""), maxLength);
         }
      }
   }

   public static String implodeUserList(List<String> userNames, int numToImplode) {
      if (userNames != null && !userNames.isEmpty()) {
         StringBuffer sb = new StringBuffer();

         for(int i = 0; i < numToImplode && i < userNames.size(); ++i) {
            if (i == 0) {
               sb.append((String)userNames.get(i));
            } else if (i == numToImplode - 1) {
               sb.append(" and ");
               sb.append(userNames.size() - numToImplode);
               sb.append(userNames.size() - numToImplode > 1 ? " other" : " others");
            } else if (i == userNames.size() - 1) {
               sb.append(" and ");
               sb.append((String)userNames.get(i));
            } else {
               sb.append(", ");
               sb.append((String)userNames.get(i));
            }
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   public static int toIntOrDefault(String numStr, int defaultValue) {
      if (numStr == null) {
         return defaultValue;
      } else {
         try {
            return Integer.parseInt(numStr);
         } catch (NumberFormatException var3) {
            return defaultValue;
         }
      }
   }

   public static double toDoubleOrDefault(String numStr, double defaultValue) {
      if (numStr == null) {
         return defaultValue;
      } else {
         try {
            return Double.parseDouble(numStr);
         } catch (NumberFormatException var4) {
            return defaultValue;
         }
      }
   }

   public static long toLongOrDefault(String numStr, long defaultValue) {
      if (numStr == null) {
         return defaultValue;
      } else {
         try {
            return Long.parseLong(numStr);
         } catch (NumberFormatException var4) {
            return defaultValue;
         }
      }
   }

   public static byte toByteOrDefault(String numStr, byte defaultValue) {
      if (numStr == null) {
         return defaultValue;
      } else {
         try {
            return Byte.parseByte(numStr);
         } catch (NumberFormatException var3) {
            return defaultValue;
         }
      }
   }

   public static boolean toBooleanOrDefault(String valueStr, boolean defaultValue) {
      if (valueStr == null) {
         return defaultValue;
      } else if ("true".equalsIgnoreCase(valueStr)) {
         return true;
      } else if ("false".equalsIgnoreCase(valueStr)) {
         return false;
      } else {
         try {
            return Integer.parseInt(valueStr) == 1;
         } catch (NumberFormatException var3) {
            return defaultValue;
         }
      }
   }

   public static boolean isValidNonMig33Email(String emailAddress) {
      return emailAddress != null && emailAddress.toLowerCase().indexOf("@mig33.com") < 0 && isValidEmail(emailAddress);
   }

   public static boolean isValidEmail(String string) {
      boolean result = false;
      if (string != null && string.trim().length() > 0) {
         int length = string.length();
         if (length >= 5) {
            int endIndex = string.length() - 1;
            int charIndex = string.indexOf(64);
            int charIndex2 = string.indexOf("@", charIndex + 1);
            if (charIndex2 > 1) {
               return false;
            }

            if (string.trim().indexOf(" ") > 0) {
               return false;
            }

            if (charIndex > 0 && charIndex < endIndex) {
               String user = string.substring(0, charIndex);
               String domain = string.substring(charIndex + 1, endIndex);
               charIndex = user.indexOf(40);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = user.indexOf(41);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = user.indexOf(60);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = user.indexOf(62);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = user.indexOf(44);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = user.indexOf(59);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = user.indexOf(58);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = user.indexOf(92);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = user.indexOf(34);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = user.indexOf(91);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = user.indexOf(93);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = domain.indexOf(40);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = domain.indexOf(41);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = domain.indexOf(60);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = domain.indexOf(62);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = domain.indexOf(44);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = domain.indexOf(59);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = domain.indexOf(58);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = domain.indexOf(92);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = domain.indexOf(34);
               if (charIndex > 0) {
                  return false;
               }

               charIndex = domain.indexOf(46);
               if (charIndex < 0) {
                  return false;
               }

               charIndex = domain.indexOf(91);
               if (charIndex > 0) {
                  return false;
               }

               int char2Index = domain.indexOf(93);
               if (char2Index > 0) {
                  return false;
               }

               if (charIndex == 0 && char2Index == endIndex) {
                  result = true;
               }

               result = true;
            }
         }
      }

      return result;
   }

   public static String getEmailDomain(String emailAddress) {
      return emailAddress == null ? null : emailAddress.substring(emailAddress.indexOf(64) + 1).toLowerCase();
   }

   public static String trimmedLowerCase(String str) {
      return str == null ? null : str.trim().toLowerCase();
   }

   public static String trimmedUpperCase(String str) {
      return str == null ? null : str.trim().toUpperCase();
   }

   /** @deprecated */
   public static String replaceNamedValues(String format, Map<String, String> namedValues) throws IllegalArgumentException {
      if (isBlank(format)) {
         throw new IllegalArgumentException("Empty format provided in replaceNamedValues()");
      } else {
         String result = format;

         Iterator i$;
         Entry e;
         String key;
         String value;
         for(i$ = namedValues.entrySet().iterator(); i$.hasNext(); result = result.replaceAll("##" + key + "##", value)) {
            e = (Entry)i$.next();
            key = (String)e.getKey();
            value = (String)e.getValue();
         }

         for(i$ = namedValues.entrySet().iterator(); i$.hasNext(); result = result.replaceAll("##\\^" + key + "##", StringEscapeUtils.escapeHtml3(value))) {
            e = (Entry)i$.next();
            key = (String)e.getKey();
            value = (String)e.getValue();
         }

         return result;
      }
   }

   public static String normalizeEmailAddress(String validEmailAddress) {
      if (validEmailAddress == null) {
         return null;
      } else {
         validEmailAddress = validEmailAddress.trim();
         String emailDomain = getEmailDomain(validEmailAddress);
         String emailUser = validEmailAddress.substring(0, validEmailAddress.indexOf(64));
         if (emailUser.equalsIgnoreCase("postmaster")) {
            emailUser = "postmaster";
         }

         return emailUser + "@" + emailDomain;
      }
   }

   public static String normalizeUsername(String username) {
      return username == null ? null : username.toLowerCase().trim();
   }

   public static StringBuilder repeat(StringBuilder target, String stringToRepeat, String separator, int repeatCount) {
      for(int i = 0; i < repeatCount; ++i) {
         if (i > 0) {
            target.append(separator);
         }

         target.append(stringToRepeat);
      }

      return target;
   }

   public static String generateRandomWord(String charset, int length) {
      StringBuilder builder = new StringBuilder();

      for(int i = 0; i < length; ++i) {
         builder.append(charset.charAt(random.nextInt(charset.length())));
      }

      return builder.toString();
   }

   public static boolean isValidMoneyFormat(String moneyInput) throws IllegalArgumentException {
      if (isBlank(moneyInput)) {
         throw new IllegalArgumentException("Empty amount entered.");
      } else {
         return moneyInput.replaceAll(",", "").replaceAll(" ", "").matches("^[0-9]+(\\.[0-9]{0,2})?$");
      }
   }

   public static byte[] hexStringToByteArray(String s) {
      if ((s.length() & 1) != 0) {
         throw new NumberFormatException("Invalid hex number format " + s);
      } else {
         int len = s.length();
         byte[] data = new byte[len >> 1];

         for(int i = 0; i < len; i += 2) {
            data[i >> 1] = (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
         }

         return data;
      }
   }

   public static String toStringOrDefault(Object obj, String defaultString) {
      return obj == null ? defaultString : obj.toString();
   }

   public static String generateQuestionMarksForSQLStatement(int count) {
      return generateStringSequence(count, ",", "?");
   }

   public static String generateStringSequence(int count, String delimiter, String repeatString) {
      return count <= 0 ? "" : repeatString + repeat(delimiter + repeatString, count - 1);
   }

   public interface ToStringConverter<T> {
      String convert(T var1);
   }
}
