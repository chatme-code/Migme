package com.projectgoth.fusion.common;

import org.apache.log4j.Logger;

public class MemCachedKeyUtils {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MemCachedKeyUtils.class));
   public static final String KEY_SEPERATOR = "/";
   public static final String URL_ENCODED_SPACE = "%20";
   public static final String URL_ENCODED_LF = "%0A";
   public static final String URL_ENCODED_CR = "%0D";

   public static String getFullKeyFromStrings(String key1, String... keys) {
      StringBuilder builder = new StringBuilder();
      builder.append(key1);
      String key;
      if (keys.length > 0) {
         String[] arr$ = keys;
         int len$ = keys.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            key = arr$[i$];
            builder.append("/");
            builder.append(key);
         }
      }

      String rawKey = builder.toString();
      String rawKeyWithEncodedSpace = rawKey.replaceAll(" ", "%20");
      String encodedKey = rawKeyWithEncodedSpace.replaceAll("\n", "%0A").replaceAll("\r", "%0D");
      if (encodedKey.length() != rawKeyWithEncodedSpace.length()) {
         key = "Invalid characters found in memcache key. Encoding it to : " + encodedKey;
         log.warn(key, new InvalidMemCachedKeyException(key));
      }

      return encodedKey;
   }

   public static String getFullKeyForKeySpace(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String... keys) {
      return getFullKeyFromStrings(keySpace.getName(), keys);
   }

   public static String[] getFullKeysForKeySpace(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String[] keys) {
      String[] fullKeys = new String[keys.length];

      for(int i = 0; i < keys.length; ++i) {
         fullKeys[i] = getFullKeyFromStrings(keySpace.getName(), keys[i]);
      }

      return fullKeys;
   }
}
