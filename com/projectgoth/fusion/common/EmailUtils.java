package com.projectgoth.fusion.common;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.DAOFactory;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class EmailUtils {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(EmailUtils.class));
   /** @deprecated */
   private static final Pattern emailAddressRegex = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
   private static final LazyLoader<Pattern> emailAddressRegexLoader = new LazyLoader<Pattern>("EMAIL_ADDRESS_REGEX", 60000L) {
      protected Pattern fetchValue() {
         return Pattern.compile(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.EMAIL_BASIC_CHECKS_REGEX));
      }
   };
   private static final String EMAIL_ADDRESS_REGEXP_FILTERS_SEPARATOR = "##";
   public static final String INVALID_EMAIL_ADDRESS_EXCEPTION_MSG = "Your email address contains invalid characters. Please provide a valid email address.";
   private static volatile String currentEmailAddressRegexpFilters = "";
   private static volatile ConcurrentHashMap<String, Pattern> regexpToPatternMap = new ConcurrentHashMap();
   private static final LazyLoader<Set<String>> whitelistEmailAddress = new LazyLoader<Set<String>>("WHITELIST_EMAIL_ADDRESS", 300000L) {
      protected Set<String> fetchValue() throws Exception {
         return new HashSet(Arrays.asList(SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.INTERNAL_EMAIL_WHITELIST)));
      }
   };

   public static EmailUtils.EmailValidatationResult externalEmailIsValid(String emailAddress) {
      if ((Boolean)SystemPropertyEntities.Temp.Cache.se605EmailHyphensEnabled.getValue()) {
         if (StringUtil.isBlank(emailAddress) || !StringUtil.isValidNonMig33Email(emailAddress) || !((Pattern)emailAddressRegexLoader.getValue()).matcher(emailAddress).matches()) {
            return new EmailUtils.EmailValidatationResult(EmailUtils.EmailValidatationEnum.INVALID, "Please provide a valid email address for registration");
         }
      } else if (StringUtil.isBlank(emailAddress) || !StringUtil.isValidNonMig33Email(emailAddress) || !emailAddressRegex.matcher(emailAddress).matches()) {
         return new EmailUtils.EmailValidatationResult(EmailUtils.EmailValidatationEnum.INVALID, "Please provide a vaild email address for registration");
      }

      String emailDomain = StringUtil.getEmailDomain(emailAddress);
      boolean isBlackListedEmailDomain = Arrays.asList(SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.BLACKLISTED_EXTERNAL_MAIL_DOMAINS)).contains(emailDomain);
      if (isBlackListedEmailDomain) {
         return new EmailUtils.EmailValidatationResult(EmailUtils.EmailValidatationEnum.INVALID, "Please provide a valid email address for registration");
      } else {
         try {
            doRegexpBasedValidation(emailAddress);
         } catch (Exception var6) {
            return new EmailUtils.EmailValidatationResult(EmailUtils.EmailValidatationEnum.INVALID, var6.getMessage());
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.SPECIAL_VALIDATION_FOR_GMAIL_ENABLED) && isGmailAddress(emailAddress)) {
            String local = emailAddress.substring(0, emailAddress.indexOf(64)).toLowerCase();
            if (emailAddress.toLowerCase().endsWith("googlemail.com")) {
               log.info(String.format("Invalid email address:%s, googlemail.com addresses are not allowed", emailAddress));
               return new EmailUtils.EmailValidatationResult(EmailUtils.EmailValidatationEnum.INVALID, String.format("googlemail.com addresses are not allowed, please use %s@gmail.com instead.", local));
            }

            if (local.contains("+")) {
               return new EmailUtils.EmailValidatationResult(EmailUtils.EmailValidatationEnum.INVALID, "Invalid character '+' provided. Please provide a valid email address.");
            }

            int numberOfPeriods = StringUtils.countOccurrencesOf(local, ".");
            if (numberOfPeriods > SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.NUMBER_OF_PERIODS_ALLOWED_IN_GMAIL_ADDRESSES)) {
               String stripped = local.replace(".", "");
               log.info(String.format("Invalid email address:%s, more than %s periods", emailAddress, SystemPropertyEntities.Registration.NUMBER_OF_PERIODS_ALLOWED_IN_GMAIL_ADDRESSES));
               return new EmailUtils.EmailValidatationResult(EmailUtils.EmailValidatationEnum.PERIODS_EXCEED_IN_GMAIL, String.format("Invalid email address provided. Do you mean to use %s@%s?", stripped, emailAddress.substring(emailAddress.indexOf(64) + 1)));
            }
         }

         if (isBounceEmailAddress(emailAddress)) {
            return new EmailUtils.EmailValidatationResult(EmailUtils.EmailValidatationEnum.BOUNCED_EMAIL_ADDRESS, "Please provide a valid email address for registration");
         } else {
            return new EmailUtils.EmailValidatationResult(EmailUtils.EmailValidatationEnum.VALID, (String)null);
         }
      }
   }

   public static boolean isBounceEmailAddress(String email) {
      boolean result = false;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EMAIL_DAO)) {
         try {
            result = DAOFactory.getInstance().getEmailDAO().isBounceEmailAddress(email);
         } catch (DAOException var4) {
            log.warn(String.format("Failed to check isBounceEmailAddress for email:%s, will treat it as non-bounce email address", email), var4);
         }
      } else {
         try {
            User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            result = userBean.isBounceEmailAddress(email);
         } catch (Exception var3) {
            log.warn(String.format("Failed to check isBounceEmailAddress for email:%s, will treat it as non-bounce email address", email), var3);
         }
      }

      if (result) {
         log.info("Found email address in bouncedb:" + email);
      }

      return result;
   }

   public static boolean isGmailAddress(String emailAddress) {
      if (StringUtil.isBlank(emailAddress)) {
         return false;
      } else {
         String emailDomain = StringUtil.getEmailDomain(emailAddress);
         return StringUtil.isBlank(emailDomain) ? false : SystemProperty.isValueInArray(emailDomain, SystemPropertyEntities.Default.GMAIL_DOMAINS);
      }
   }

   public static void doRegexpBasedValidation(String emailAddress) throws Exception {
      String regexpsSysprop = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.EMAIL_EXCLUSION_REGULAR_EXPRESSIONS);
      if (!StringUtil.isBlank(regexpsSysprop)) {
         if (!regexpsSysprop.equals(currentEmailAddressRegexpFilters)) {
            Class var2 = EmailUtils.class;
            synchronized(EmailUtils.class) {
               if (!regexpsSysprop.equals(currentEmailAddressRegexpFilters)) {
                  if (log.isDebugEnabled()) {
                     log.debug("Reloading regular expressions from " + SystemPropertyEntities.Registration.EMAIL_EXCLUSION_REGULAR_EXPRESSIONS);
                  }

                  currentEmailAddressRegexpFilters = regexpsSysprop;
                  ConcurrentHashMap<String, Pattern> newMap = new ConcurrentHashMap();
                  String[] regexps = regexpsSysprop.split("##");
                  String[] arr$ = regexps;
                  int len$ = regexps.length;

                  for(int i$ = 0; i$ < len$; ++i$) {
                     String re = arr$[i$];
                     Pattern p = Pattern.compile(re);
                     newMap.put(re, p);
                  }

                  regexpToPatternMap = newMap;
               }
            }
         }

         Collection<Pattern> patterns = regexpToPatternMap.values();
         Iterator i$ = patterns.iterator();

         Pattern p;
         Matcher m;
         do {
            if (!i$.hasNext()) {
               return;
            }

            p = (Pattern)i$.next();
            m = p.matcher(emailAddress);
         } while(!m.matches());

         log.info("Rejecting email address: " + emailAddress + " as it matches regexp=" + p.pattern());
         throw new Exception("Your email address contains invalid characters. Please provide a valid email address.");
      }
   }

   public static String stripPeriodsFromGmailAddress(String emailAddress) {
      String local = emailAddress.substring(0, emailAddress.indexOf(64)).toLowerCase();
      local = local.replace(".", "");
      return local + "@gmail.com";
   }

   public static String getSanitizedEmailAddressLocalPart(String emailAddress) {
      String localEmail = emailAddress.substring(0, emailAddress.indexOf(64)).toLowerCase();
      return localEmail.contains("+") ? localEmail.substring(0, localEmail.indexOf("+")).replaceAll("\\W", "") : localEmail.replaceAll("\\W", "");
   }

   public static String getDomain(String emailAddress) throws Exception {
      if (StringUtil.isBlank(emailAddress)) {
         throw new Exception("Please provide an email address.");
      } else if (!StringUtil.isValidEmail(emailAddress)) {
         throw new Exception("Invalid email address provided.");
      } else {
         return emailAddress.substring(emailAddress.indexOf(64) + 1, emailAddress.length()).toLowerCase();
      }
   }

   public static String getTopLevelDomain(String emailAddress) throws Exception {
      String domain = getDomain(emailAddress);
      String[] parts = domain.split("\\.");
      if (parts.length < 2) {
         throw new Exception("Invalid email address provided.");
      } else {
         return String.format("%s.%s", parts[parts.length - 2], parts[parts.length - 1]);
      }
   }

   public static boolean isInternalEmailAddress(String email, String defaultMigEmailDomain) {
      if (!StringUtil.isBlank(email) && !StringUtil.isBlank(defaultMigEmailDomain)) {
         try {
            email = email.toLowerCase();
            return email.endsWith(defaultMigEmailDomain) && !((Set)whitelistEmailAddress.getValue()).contains(email);
         } catch (Exception var3) {
            return false;
         }
      } else {
         return false;
      }
   }

   public static class EmailValidatationResult {
      public String reason;
      public EmailUtils.EmailValidatationEnum result;

      public EmailValidatationResult(EmailUtils.EmailValidatationEnum result, String reason) {
         this.result = result;
         this.reason = reason;
      }
   }

   public static enum EmailValidatationEnum {
      VALID,
      INVALID,
      BOUNCED_EMAIL_ADDRESS,
      PERIODS_EXCEED_IN_GMAIL;
   }
}
