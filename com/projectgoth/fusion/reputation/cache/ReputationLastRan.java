package com.projectgoth.fusion.reputation.cache;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.common.MemCachedUtils;

public class ReputationLastRan {
   public static final String REPUTATION_NAMESPACE = "REP";
   public static final String SESSION_ARCHIVE_LAST_ID_KEY = "SAID";
   public static final String ACCOUNT_ENTRY_LAST_ID_KEY = "AEID";
   public static final String VIRTUAL_GIFT_LAST_ID_KEY = "VGID";
   public static final String PHONE_CALL_LAST_ID_KEY = "PCID";
   public static final String LAST_RUN_DATE_KEY = "LRD";
   public static final String SESSION_ARCHIVE_TABLE_NAME_KEY = "SATN";

   public static Integer getSessionArchiveLastId(MemCachedClient instance) {
      return (Integer)instance.get(MemCachedUtils.getCacheKeyInNamespace("REP", "SAID"));
   }

   public static boolean setSessionArchiveLastId(MemCachedClient instance, Integer id) {
      return instance.set(MemCachedUtils.getCacheKeyInNamespace("REP", "SAID"), id);
   }

   public static Integer getAccountEntryLastId(MemCachedClient instance) {
      return (Integer)instance.get(MemCachedUtils.getCacheKeyInNamespace("REP", "AEID"));
   }

   public static boolean setAccountEntryLastId(MemCachedClient instance, Integer id) {
      return instance.set(MemCachedUtils.getCacheKeyInNamespace("REP", "AEID"), id);
   }

   public static Integer getVirtualGiftLastId(MemCachedClient instance) {
      return (Integer)instance.get(MemCachedUtils.getCacheKeyInNamespace("REP", "VGID"));
   }

   public static boolean setVirtualGiftLastId(MemCachedClient instance, Integer id) {
      return instance.set(MemCachedUtils.getCacheKeyInNamespace("REP", "VGID"), id);
   }

   public static Integer getPhoneCallLastId(MemCachedClient instance) {
      return (Integer)instance.get(MemCachedUtils.getCacheKeyInNamespace("REP", "PCID"));
   }

   public static boolean setPhoneCallLastId(MemCachedClient instance, Integer id) {
      return instance.set(MemCachedUtils.getCacheKeyInNamespace("REP", "PCID"), id);
   }

   public static long getLastRunDate(MemCachedClient instance) {
      Object result = instance.get(MemCachedUtils.getCacheKeyInNamespace("REP", "LRD"));
      return result == null ? 0L : (Long)result;
   }

   public static boolean setLastRunDate(MemCachedClient instance, long lastRunDate) {
      return instance.set(MemCachedUtils.getCacheKeyInNamespace("REP", "LRD"), lastRunDate);
   }

   public static String getSessionArchiveTableName(MemCachedClient instance) {
      return (String)instance.get(MemCachedUtils.getCacheKeyInNamespace("REP", "SATN"));
   }

   public static boolean setSessionArchiveTableName(MemCachedClient instance, String tableName) {
      return instance.set(MemCachedUtils.getCacheKeyInNamespace("REP", "SATN"), tableName);
   }
}
