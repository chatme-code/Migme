package com.projectgoth.fusion.common;

import java.security.SecureRandom;
import org.apache.log4j.Logger;

public class Tokenizer {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Tokenizer.class));
   private static final SecureRandom random = new SecureRandom();

   private static String generateRandomWord(String charset, int length) {
      StringBuilder builder = new StringBuilder();

      for(int i = 0; i < length; ++i) {
         builder.append(charset.charAt(random.nextInt(charset.length())));
      }

      return builder.toString();
   }

   private static void storeToken(Tokenizer.TokenIdentifier identifier, String token, String expectedVal, Long expirationInMillis) throws Exception {
      MemCachedClientWrapper.set(identifier.memcacheKeySpace(), token, expectedVal, expirationInMillis);
   }

   public static void deleteToken(Tokenizer.TokenIdentifier identifier, String token) {
      MemCachedClientWrapper.delete(identifier.memcacheKeySpace(), token);
   }

   private static String getToken(Tokenizer.TokenIdentifier identifier, String token) {
      return MemCachedClientWrapper.getString(identifier.memcacheKeySpace(), token);
   }

   public static String generateToken(Tokenizer.TokenIdentifier identifier, String expectedVal, long expirationInMillis) throws Exception {
      return generateToken(identifier, expectedVal, SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.TokenizerSettings.DEFAULT_CHARSET), SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.TokenizerSettings.MINIMUM_LENGTH), expirationInMillis);
   }

   public static String generateToken(Tokenizer.TokenIdentifier identifier, String expectedVal) throws Exception {
      return generateToken(identifier, expectedVal, SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.TokenizerSettings.DEFAULT_CHARSET), SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.TokenizerSettings.MINIMUM_LENGTH), SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.TokenizerSettings.DEFAULT_EXPIRATION_IN_MILLIS));
   }

   public static String generateToken(Tokenizer.TokenIdentifier identifier, String expectedVal, String charset, int length, Long expirationInMillis) throws Exception {
      if (StringUtil.isBlank(charset)) {
         charset = SystemProperty.get(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.TokenizerSettings.DEFAULT_CHARSET));
      }

      int minTokenLength = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.TokenizerSettings.MINIMUM_LENGTH);
      if (length < minTokenLength) {
         length = minTokenLength;
      }

      if (null == expirationInMillis) {
         expirationInMillis = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.TokenizerSettings.DEFAULT_EXPIRATION_IN_MILLIS);
      }

      String token = generateRandomWord(charset, length);
      storeToken(identifier, token, expectedVal, expirationInMillis);
      return token;
   }

   public static boolean validateToken(Tokenizer.TokenIdentifier identifier, String token, String expectedVal) {
      String value = getToken(identifier, token);
      if (null == value) {
         log.warn("Token requested may have expired already: token [" + token + "] identifier [" + identifier + "]");
      }

      return value != null && value.equals(expectedVal);
   }

   public static enum TokenIdentifier {
      FORGOT_PASSWORD("FGT_PWD", MemCachedKeySpaces.TokenKeySpace.FORGOT_PASSWORD);

      private String tokenIdentifier;
      private MemCachedKeySpaces.TokenKeySpace memcacheKeySpace;

      private TokenIdentifier(String tokenIdentifier, MemCachedKeySpaces.TokenKeySpace keySpace) {
         this.tokenIdentifier = tokenIdentifier;
         this.memcacheKeySpace = keySpace;
      }

      public String identifier() {
         return this.tokenIdentifier;
      }

      public MemCachedKeySpaces.TokenKeySpace memcacheKeySpace() {
         return this.memcacheKeySpace;
      }

      public static Tokenizer.TokenIdentifier fromValue(String tokenIdentifier) {
         Tokenizer.TokenIdentifier[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Tokenizer.TokenIdentifier e = arr$[i$];
            if (e.identifier().equals(tokenIdentifier)) {
               return e;
            }
         }

         return null;
      }
   }
}
