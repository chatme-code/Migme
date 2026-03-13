package com.projectgoth.fusion.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jboss.logging.Logger;

public class UsernameUtils {
   private static final Pattern CREATE_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z]{1}(\\.{0,1}[\\w-]){5,}$");
   public static final int MIN_LENGTH = 6;
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UsernameUtils.class));

   public static String normalizeUsername(String username) {
      return StringUtil.trimmedLowerCase(username);
   }

   public static int validateUsernameLength(String username) throws UsernameValidationException {
      if (username == null) {
         throw new UsernameValidationException(ErrorCause.InvalidUserName.VALUE_NOT_SPECIFIED, new Object[0]);
      } else {
         int usernameLength = username.length();
         if (usernameLength == 0) {
            throw new UsernameValidationException(ErrorCause.InvalidUserName.VALUE_IS_BLANK, new Object[0]);
         } else if (usernameLength > MemCachedClientWrapper.MAX_KEY_LENGTH) {
            throw new UsernameValidationException(ErrorCause.InvalidUserName.UNSUPPORTED_STRING_LENGTH, new Object[0]);
         } else {
            return usernameLength;
         }
      }
   }

   public static String validateUsernameCharacters(String username, boolean normalizeBeforeValidate) throws UsernameValidationException {
      if (normalizeBeforeValidate) {
         username = normalizeUsername(username);
      }

      int usernameLength = validateUsernameLength(username);
      Matcher matcher = CREATE_USERNAME_PATTERN.matcher(username);
      if (!matcher.matches()) {
         throw new UsernameValidationException(ErrorCause.InvalidUserName.CONTAINS_ILLEGAL_PATTERNS, new Object[]{6});
      } else {
         int maxUsernameLength = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_USERNAME_LENGTH);
         if (usernameLength > maxUsernameLength) {
            throw new UsernameValidationException(ErrorCause.InvalidUserName.STRING_LENGTH_BEYOND_CONFIGURED_LIMIT, new Object[]{maxUsernameLength});
         } else {
            return !normalizeBeforeValidate ? normalizeUsername(username) : username;
         }
      }
   }

   public static void validateAgainstBannedWords(String normalizedUsername, String[] bannedWords) throws UsernameValidationException {
      if (bannedWords != null && bannedWords.length != 0) {
         String[] arr$ = bannedWords;
         int len$ = bannedWords.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String bannedWord = arr$[i$];
            String normalizedBannedWord = normalizeUsername(bannedWord);
            if (!StringUtil.isBlank(normalizedBannedWord) && normalizedUsername.indexOf(normalizedBannedWord) != -1) {
               throw new UsernameValidationException(ErrorCause.InvalidUserName.DETECTED_BANNED_WORDS, new Object[]{normalizedBannedWord});
            }
         }

      }
   }

   public static void validateUsernameAsSearchKeyword(String usernameKeyword) throws UsernameValidationException {
      validateUsernameLength(usernameKeyword);
      Pattern pattern = (Pattern)UsernameUtils.SingletonHolder.getUsernameAsSearchKeywordRegexLoader().getValue();
      if (pattern != null) {
         Matcher matcher = pattern.matcher(usernameKeyword);
         if (!matcher.matches()) {
            throw new UsernameValidationException(ErrorCause.InvalidUserName.INVALID_FORMAT, new Object[]{usernameKeyword});
         }
      } else {
         log.warn("Character validation is not performed on username [" + usernameKeyword + "] since the compiled regex pattern is null.");
      }

   }

   public static void invalidateUsernameSearchKeywordRegex() {
      UsernameUtils.SingletonHolder.USERNAME_AS_SEARCH_KEYWORD_REGEX_LOADER.invalidateCache();
   }

   private static class SingletonHolder {
      private static final long CACHED_TTL_MILLIES = 60000L;
      private static final LazyLoader<Pattern> USERNAME_AS_SEARCH_KEYWORD_REGEX_LOADER = new LazyLoader<Pattern>("USERNAME_AS_SEARCH_KEYWORD_REGEX_LOADER", 60000L) {
         protected Pattern fetchValue() throws Exception {
            String regex = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.USERNAME_AS_SEARCH_KEYWORD_REGEX);
            Pattern pattern = null;

            try {
               pattern = Pattern.compile(regex);
               return pattern;
            } catch (Exception var4) {
               UsernameUtils.log.error("Unable to compile regex [" + regex + "] defined under System property [" + SystemPropertyEntities.Default.USERNAME_AS_SEARCH_KEYWORD_REGEX.getName() + "]", var4);
               throw var4;
            }
         }
      };

      public static LazyLoader<Pattern> getUsernameAsSearchKeywordRegexLoader() {
         return USERNAME_AS_SEARCH_KEYWORD_REGEX_LOADER;
      }
   }
}
