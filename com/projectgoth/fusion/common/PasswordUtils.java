package com.projectgoth.fusion.common;

import com.projectgoth.fusion.authentication.CredentialVersion;
import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.authentication.domain.PersistedCredential;
import com.projectgoth.fusion.data.ValidateCredentialResult;
import com.projectgoth.fusion.slice.Credential;
import com.projectgoth.fusion.slice.FusionCredential;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.keyczar.Crypter;
import org.keyczar.exceptions.KeyczarException;
import org.springframework.util.StringUtils;

public class PasswordUtils {
   private static final Logger logger = Logger.getLogger(ConfigUtils.getLoggerName(PasswordUtils.class));

   public static Pattern getPasswordRegex() {
      PasswordUtils.PasswordRegexLoader loader = PasswordUtils.SingletonHolder.getPasswordRegexLoader();
      return (Pattern)loader.getValue();
   }

   public static Set<String> getUppercasedBannedPasswords() {
      PasswordUtils.UppercasedBannedPasswordsLoader loader = PasswordUtils.SingletonHolder.getUppercasedBannedPasswordsLoader();
      return (Set)loader.getValue();
   }

   public static Set<Pattern> getBannedPasswordPatterns() {
      return (Set)PasswordUtils.SingletonHolder.getBannedPasswordPatternLoader().getValue();
   }

   public static void invalidateCachedConfig() {
      PasswordUtils.SingletonHolder.invalidateCachedConfig();
   }

   /** @deprecated */
   public static boolean isValidPassword(String password) {
      ValidateCredentialResult result = validatePassword((String)null, password, false);
      return null != result && result.valid;
   }

   public static ValidateCredentialResult validatePassword(String username, String password) {
      return validatePassword(username, password, true);
   }

   private static ValidateCredentialResult validatePassword(String username, String password, boolean checkAgainstUsername) {
      if (password != null && password.length() != 0) {
         if (checkAgainstUsername && password.trim().equalsIgnoreCase(username)) {
            return new ValidateCredentialResult(false, "Password must not be the same as the username");
         } else {
            int minPasswordLength = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MIN_PASSWORD_LENGTH);
            if (password.length() < minPasswordLength) {
               return new ValidateCredentialResult(false, "The password must contain at least " + minPasswordLength + " characters");
            } else {
               int maxPasswordLength = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_PASSWORD_LENGTH);
               if (password.length() > maxPasswordLength) {
                  return new ValidateCredentialResult(false, "The password must not contain more than " + maxPasswordLength + " characters");
               } else if (!getPasswordRegex().matcher(password).matches()) {
                  return new ValidateCredentialResult(false, SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Credentials.INVALID_PASSWORD_MESSAGE));
               } else {
                  return passwordContainsBannedWords(password) ? new ValidateCredentialResult(false, "You cannot use " + password + " as your password") : new ValidateCredentialResult(true, "valid");
               }
            }
         }
      } else {
         return new ValidateCredentialResult(false, "Password must not be blank");
      }
   }

   public static boolean passwordContainsBannedWords(String password) {
      String pwLowerCase = password.toLowerCase();
      Iterator i$ = getBannedPasswordPatterns().iterator();

      Matcher m;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         Pattern pattern = (Pattern)i$.next();
         m = pattern.matcher(pwLowerCase);
      } while(!m.matches());

      return true;
   }

   public static String decryptPassword(PersistedCredential credential, Crypter crypter) throws KeyczarException {
      if (!StringUtils.hasLength(credential.getPassword())) {
         return "";
      } else {
         return credential.getVersion() != CredentialVersion.UNENCRYPTED_MIGRATED && credential.getVersion() != CredentialVersion.UNENCRYPTED_UNMIGRATED ? decrypt(credential.getPassword(), crypter) : credential.getPassword();
      }
   }

   public static String decrypt(String toBeDecrypted, Crypter crypter) throws KeyczarException {
      return !StringUtils.hasLength(toBeDecrypted) ? "" : crypter.decrypt(toBeDecrypted);
   }

   public static String encryptPassword(String password, Crypter crypter) throws KeyczarException {
      return !StringUtils.hasLength(password) ? "" : encrypt(password, crypter);
   }

   public static String encrypt(String toBeEncrypted, Crypter crypter) throws KeyczarException {
      return !StringUtils.hasLength(toBeEncrypted) ? "" : crypter.encrypt(toBeEncrypted);
   }

   public static ValidateCredentialResult validateCredential(Credential credential) {
      if (credential != null && StringUtils.hasLength(credential.username)) {
         if (!(credential instanceof FusionCredential)) {
            if (!StringUtils.hasLength(credential.password) || credential.passwordType < 1) {
               return new ValidateCredentialResult(false, "Password must not be blank");
            }

            if (credential.passwordType == PasswordType.FUSION.value()) {
               ValidateCredentialResult validationResult = validatePassword(credential.username, credential.password);
               if (validationResult == null) {
                  return new ValidateCredentialResult(false, "Unable to validate credential");
               }

               if (!validationResult.valid) {
                  return validationResult;
               }
            }
         }

         return new ValidateCredentialResult(true, "valid");
      } else {
         return new ValidateCredentialResult(false, "Username is blank");
      }
   }

   public static PersistedCredential filterPersistedCredentialByPasswordType(List<PersistedCredential> credentials, PasswordType passwordType) {
      if (credentials != null && !credentials.isEmpty()) {
         Iterator i$ = credentials.iterator();

         PersistedCredential cred;
         do {
            if (!i$.hasNext()) {
               return null;
            }

            cred = (PersistedCredential)i$.next();
         } while(cred.getPasswordType() != passwordType.value());

         return cred;
      } else {
         return null;
      }
   }

   public static List<PersistedCredential> filterPersistedCredentialsByPasswordTypes(List<PersistedCredential> credentials, PasswordType[] passwordTypes) {
      List<PersistedCredential> results = new ArrayList(passwordTypes.length);
      if (credentials != null && !credentials.isEmpty()) {
         Arrays.sort(passwordTypes);
         Iterator i$ = credentials.iterator();

         while(i$.hasNext()) {
            PersistedCredential cred = (PersistedCredential)i$.next();
            if (Arrays.binarySearch(passwordTypes, PasswordType.fromValue(cred.getPasswordType())) >= 0) {
               results.add(cred);
            }
         }

         return results;
      } else {
         return results;
      }
   }

   public static void main(String[] args) throws KeyczarException {
      Crypter crypter = new Crypter("/usr/fusion/etc/aeskeys/");
      String enc = encryptPassword("01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", crypter);
      System.out.println(enc);
      System.out.println(enc.length());
   }

   private static class SingletonHolder {
      private static final PasswordUtils.PasswordRegexLoader passwordRegexLoader = new PasswordUtils.PasswordRegexLoader();
      private static final PasswordUtils.UppercasedBannedPasswordsLoader uppercaseBannedPasswordsLoader = new PasswordUtils.UppercasedBannedPasswordsLoader();
      private static final PasswordUtils.BannedPasswordPatternsLoader bannedPasswordPatterns = new PasswordUtils.BannedPasswordPatternsLoader();

      public static PasswordUtils.PasswordRegexLoader getPasswordRegexLoader() {
         return passwordRegexLoader;
      }

      public static PasswordUtils.UppercasedBannedPasswordsLoader getUppercasedBannedPasswordsLoader() {
         return uppercaseBannedPasswordsLoader;
      }

      public static PasswordUtils.BannedPasswordPatternsLoader getBannedPasswordPatternLoader() {
         return bannedPasswordPatterns;
      }

      public static void invalidateCachedConfig() {
         passwordRegexLoader.invalidateCache();
         uppercaseBannedPasswordsLoader.invalidateCache();
         bannedPasswordPatterns.invalidateCache();
      }
   }

   private static class BannedPasswordPatternsLoader extends LazyLoader<Set<Pattern>> {
      private static final int CACHE_TTL_MILLIES = 120000;

      public BannedPasswordPatternsLoader() {
         super("BannedPasswordPatternsLoader", 120000L);
      }

      protected Set<Pattern> fetchValue() throws Exception {
         Set<Pattern> patterns = new HashSet();
         String[] entries = SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.BANNEDPASSWORDS);
         String[] arr$ = entries;
         int len$ = entries.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String entry = arr$[i$];

            try {
               Pattern ptn = Pattern.compile(entry);
               patterns.add(ptn);
            } catch (Exception var8) {
               PasswordUtils.logger.error("Exception compiling " + SystemPropertyEntities.Default.BANNEDPASSWORDS.getName() + " pattern entry=" + entry + ": e=" + var8, var8);
            }
         }

         return Collections.unmodifiableSet(patterns);
      }
   }

   private static class UppercasedBannedPasswordsLoader extends LazyLoader<Set<String>> {
      private static final int CACHE_TTL_MILLIES = 120000;

      public UppercasedBannedPasswordsLoader() {
         super("UppercasedBannedPasswordsLoader", 120000L);
      }

      protected Set<String> fetchValue() throws Exception {
         HashSet<String> passwordSet = new HashSet();
         String[] bannedPasswords = SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.BANNEDPASSWORDS);
         String[] arr$ = bannedPasswords;
         int len$ = bannedPasswords.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String bannedPassword = arr$[i$];
            passwordSet.add(bannedPassword.toUpperCase());
         }

         return Collections.unmodifiableSet(passwordSet);
      }
   }

   private static class PasswordRegexLoader extends LazyLoader<Pattern> {
      private static final int CACHE_TTL_MILLIES = 120000;

      public PasswordRegexLoader() {
         super("PasswordRegexLoader", 120000L);
      }

      protected Pattern fetchValue() throws Exception {
         String passwordRegex = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Credentials.PASSWORD_REG_EX);

         try {
            return Pattern.compile(passwordRegex);
         } catch (Exception var3) {
            this.getLogger().error("System property:[" + SystemPropertyEntities.Credentials.PASSWORD_REG_EX.getName() + "] has an invalid regular expression[" + passwordRegex + "].Exception:" + var3, var3);
            throw var3;
         }
      }
   }
}
