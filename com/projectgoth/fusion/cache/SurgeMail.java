package com.projectgoth.fusion.cache;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.common.MemCachedUtils;

public class SurgeMail {
   public static final String SURGEMAIL_PASSWORD_NAMESPACE = "SMP";

   public static String getPasswordKey(String username) {
      return MemCachedUtils.getCacheKeyInNamespace("SMP", username);
   }

   public static String getPassword(MemCachedClient instance, String username) {
      return (String)instance.get(getPasswordKey(username));
   }

   public static boolean setPassword(MemCachedClient instance, String username, String password) {
      return instance.set(getPasswordKey(username), password);
   }

   public static boolean deleteSurgeMailPassword(MemCachedClient instance, String username) {
      return instance.delete(getPasswordKey(username));
   }
}
