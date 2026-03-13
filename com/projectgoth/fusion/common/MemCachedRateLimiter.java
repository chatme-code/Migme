package com.projectgoth.fusion.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class MemCachedRateLimiter {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MemCachedRateLimiter.class));
   private static final MemCachedKeySpaces.MemCachedKeySpaceInterface RATE_LIMIT_KEYSPACE;
   private static final Pattern HIT_PER_DURATION_PATTERN;
   private static final String[] DURATION_TYPES;
   private static final String[] DURATION_NAMES;
   private static final long[] DURATION_VALUES;
   private static Map<String, String> durationType2Name;
   private static Map<String, Long> durationType2Value;

   public static boolean reset(String namespace, String key) {
      return MemCachedClientWrapper.delete(RATE_LIMIT_KEYSPACE, MemCachedKeyUtils.getFullKeyFromStrings(namespace, key));
   }

   public static boolean checkWithoutHit(MemCachedRateLimiter.NameSpace namespace, String key, long maxHit, long duration) {
      if (namespace == null) {
         log.error(String.format("null namespace for key='%s', maxHit=%d, duration=%d", key, maxHit, duration));
         return false;
      } else {
         return checkWithoutHit(namespace.toString(), key, maxHit, duration);
      }
   }

   public static boolean checkWithoutHit(String namespace, String key, long maxHit, long duration) {
      String mcKey = MemCachedKeyUtils.getFullKeyFromStrings(namespace, key);
      return MemCachedClientWrapper.getCounter(RATE_LIMIT_KEYSPACE, mcKey) < maxHit;
   }

   public static boolean hit(MemCachedRateLimiter.NameSpace namespace, String key, long maxHit, long duration) {
      if (namespace == null) {
         log.error(String.format("null namespace for key='%s', maxHit=%d, duration=%d", key, maxHit, duration));
         return false;
      } else {
         return hit(namespace.toString(), key, maxHit, duration);
      }
   }

   public static boolean hit(String namespace, String key, long maxHit, long duration) {
      String mcKey = MemCachedKeyUtils.getFullKeyFromStrings(namespace, key);
      long currentHit = MemCachedClientWrapper.getCounter(RATE_LIMIT_KEYSPACE, mcKey);
      if (currentHit == -1L && MemCachedClientWrapper.add(RATE_LIMIT_KEYSPACE, mcKey, 1, duration)) {
         return true;
      } else {
         return currentHit < maxHit && MemCachedClientWrapper.incr(RATE_LIMIT_KEYSPACE, mcKey) <= maxHit;
      }
   }

   public static void hit(MemCachedRateLimiter.NameSpace namespace, String key, String hitPerDuration) throws MemCachedRateLimiter.LimitExceeded, MemCachedRateLimiter.FormatError {
      if (namespace == null) {
         log.error(String.format("null namespace for key='%s', hit='%s'", key, hitPerDuration));
         throw new MemCachedRateLimiter.FormatError("Empty namespace");
      } else {
         hit(namespace.toString(), key, hitPerDuration);
      }
   }

   private static List<MemCachedRateLimiter.DurationEntry> parseDurationString(String hitPerDuration, String namespace, String key, boolean throwErrorOnAnyError) throws MemCachedRateLimiter.FormatError {
      List<MemCachedRateLimiter.DurationEntry> results = new LinkedList();
      Matcher m = HIT_PER_DURATION_PATTERN.matcher(hitPerDuration);
      if (m.find()) {
         m.reset();

         while(m.find()) {
            MemCachedRateLimiter.DurationEntry entry = new MemCachedRateLimiter.DurationEntry();
            entry.durationPattern = m.group(0);
            entry.maxHit = Long.parseLong(m.group(2));
            entry.duration = Long.parseLong(m.group(3));
            entry.durationMillis = -1L;
            entry.durationType = m.group(4);
            entry.durationTypeName = (String)durationType2Name.get(entry.durationType);
            entry.durationTypeValue = (Long)durationType2Value.get(entry.durationType);
            if (entry.durationTypeValue != null) {
               entry.durationMillis = entry.duration * entry.durationTypeValue;
               results.add(entry);
            } else {
               log.warn(String.format("Incorrect hitPerDuration string '%s', for ns='%s', key='%s': unrecognized duration type '%s'", hitPerDuration, namespace, key, entry.durationType));
            }
         }

         return results;
      } else {
         log.error(String.format("Incorrect hitPerDuration string '%s', for ns='%s', key='%s'", hitPerDuration, namespace, key));
         throw new MemCachedRateLimiter.FormatError("Incorrect hitPerDuration string " + hitPerDuration);
      }
   }

   public static void hit(String namespace, String key, String hitPerDuration) throws MemCachedRateLimiter.LimitExceeded, MemCachedRateLimiter.FormatError {
      Iterator i$ = parseDurationString(hitPerDuration, namespace, key, false).iterator();

      MemCachedRateLimiter.DurationEntry entry;
      do {
         if (!i$.hasNext()) {
            return;
         }

         entry = (MemCachedRateLimiter.DurationEntry)i$.next();
      } while(hit(namespace, MemCachedKeyUtils.getFullKeyFromStrings(key, entry.durationType), entry.maxHit, entry.durationMillis));

      throw new MemCachedRateLimiter.LimitExceeded("rate limit exceeded " + namespace + "/" + key + "/" + entry.durationType, entry.durationPattern);
   }

   public static boolean checkWithoutHit(String namespace, String key, String hitPerDuration) throws MemCachedRateLimiter.LimitExceeded, MemCachedRateLimiter.FormatError {
      boolean result = true;

      MemCachedRateLimiter.DurationEntry entry;
      for(Iterator i$ = parseDurationString(hitPerDuration, namespace, key, false).iterator(); i$.hasNext(); result = result && checkWithoutHit(namespace, MemCachedKeyUtils.getFullKeyFromStrings(key, entry.durationType), entry.maxHit, entry.durationMillis)) {
         entry = (MemCachedRateLimiter.DurationEntry)i$.next();
      }

      return result;
   }

   public static boolean checkWithoutHit(MemCachedRateLimiter.NameSpace namespace, String key, String hitPerDuration) throws MemCachedRateLimiter.LimitExceeded, MemCachedRateLimiter.FormatError {
      return checkWithoutHit(namespace.toString(), key, hitPerDuration);
   }

   public static void reset(MemCachedRateLimiter.NameSpace namespace, String key, String hitPerDuration) {
      try {
         Iterator i$ = parseDurationString(hitPerDuration, namespace.toString(), key, false).iterator();

         while(i$.hasNext()) {
            MemCachedRateLimiter.DurationEntry entry = (MemCachedRateLimiter.DurationEntry)i$.next();
            reset(namespace.toString(), MemCachedKeyUtils.getFullKeyFromStrings(key, entry.durationType));
         }
      } catch (MemCachedRateLimiter.FormatError var5) {
      }

   }

   static String getPrettyHitPerDuration(String hitPerDuration) {
      try {
         List<MemCachedRateLimiter.DurationEntry> entries = parseDurationString(hitPerDuration, (String)null, (String)null, false);
         if (entries.size() > 0) {
            MemCachedRateLimiter.DurationEntry entry = (MemCachedRateLimiter.DurationEntry)entries.get(0);
            return String.format("%d %%s within %d %s%s", entry.maxHit, entry.duration, entry.durationTypeName, entry.duration > 1L ? "s" : "");
         } else {
            return "";
         }
      } catch (MemCachedRateLimiter.FormatError var3) {
         return "";
      }
   }

   public static boolean isValidHitPerDuration(String hitPerDuration) {
      try {
         return parseDurationString(hitPerDuration, (String)null, (String)null, true).size() > 0;
      } catch (MemCachedRateLimiter.FormatError var2) {
         return false;
      }
   }

   public static boolean bypassRateLimit(String username) {
      return (Boolean)SystemPropertyEntities.GatewaySettings.Cache.loadTestUsersRateLimitBypassEnabled.getValue() && null != username && username.startsWith("loadtest");
   }

   static {
      RATE_LIMIT_KEYSPACE = MemCachedKeySpaces.RateLimitKeySpace.RATE_LIMIT;
      HIT_PER_DURATION_PATTERN = Pattern.compile("(([0-9]+)/([0-9]+)(D|H|M|S|MS)(;|$))");
      DURATION_TYPES = new String[]{"D", "H", "M", "S", "MS"};
      DURATION_NAMES = new String[]{"day", "hour", "minute", "second", "milli-second"};
      DURATION_VALUES = new long[]{86400000L, 3600000L, 60000L, 1000L, 1L};
      durationType2Name = new HashMap();
      durationType2Value = new HashMap();

      for(int i = 0; i < DURATION_TYPES.length; ++i) {
         durationType2Name.put(DURATION_TYPES[i], DURATION_NAMES[i]);
         durationType2Value.put(DURATION_TYPES[i], DURATION_VALUES[i]);
      }

   }

   public static class FormatError extends Exception {
      public FormatError(String message) {
         super(message);
      }
   }

   public static class LimitExceeded extends Exception {
      private String expr;

      LimitExceeded(String message, String expr) {
         super(message);
         this.expr = expr;
      }

      public String getPrettyMessage() {
         return MemCachedRateLimiter.getPrettyHitPerDuration(this.expr);
      }
   }

   public static enum NameSpace {
      VIRTUALGIFT,
      CONTACT,
      SENDSMS,
      PHONECALL,
      GENERIC_EMOTE,
      AUTH_AC,
      USER_REFERRAL,
      CHATROOM_ENTRY,
      EMAIL_REG_RATE_LIMIT_IP,
      EMAIL_REG_RATE_LIMIT_IP_STEP_2,
      EMAIL_REG_RATE_LIMIT_DOMAIN,
      EMAIL_REG_RATE_LIMIT_DOMAIN_STEP_2,
      EMAIL_REG_RATE_LIMIT_SANITIZED_EMAIL_LOCAL_AND_IP,
      EMAIL_REG_RATE_LIMIT_SANITIZED_EMAIL_LOCAL_AND_IP_STEP_2,
      EMAIL_REG_RATE_LIMIT_SANITIZED_EMAIL_LOCAL_DOMAIN,
      EMAIL_REG_RATE_LIMIT_SANITIZED_EMAIL_LOCAL_DOMAIN_STEP_2,
      EMAIL_REG_RATE_LIMIT_NON_WHITELISTED_DOMAIN_TOP_LEVEL_PART,
      EMAIL_REG_RATE_LIMIT_NON_WHITELISTED_DOMAIN_TOP_LEVEL_PART_STEP_2,
      REWARD_PRG,
      FUSIONREST_ALERTBATCH,
      OFFLINE_MESSAGE_SENDS_PER_USER,
      OFFLINE_MESSAGE_SENDS_PER_USER_PAIR,
      OFFLINE_MESSAGE_RECEIVES_PER_USER,
      INITIATE_PAYPAL_PAYMENT,
      OFFLINE_MESSAGE_SENDS_PER_SUSPECTED_ABUSER,
      FACEBOOK_MESSAGE_RECEIVE_PER_USER,
      FORGOT_PASSWORD_REQUEST,
      FORGOT_PASSWORD_RESPONSE,
      FORGOT_USERNAME_REQUEST,
      AWS_GLOBAL_RATE_LIMITS,
      AWS_LIVE_JOB_FLOWS_PER_TRANSFORMATION,
      AUTO_LOGIN,
      CHAT_SYNC_GLOBAL_RATE_LIMITS,
      CHAT_SYNC_MAX_GET_CHATS_PER_USER_PER_DAY,
      CHAT_SYNC_MAX_GET_MESSAGES_PER_USER_PER_DAY,
      CHAT_SYNC_MAX_CHATS_STORED_PER_USER_PER_DAY,
      CHAT_SYNC_MAX_MESSAGES_STORED_PER_USER_PER_DAY,
      UPLD_DATA,
      UPLD_TICKET_REQ,
      MANDATORY_RESET_UNREAD_ALERT_COUNT,
      CAMPAIGN;
   }

   private static class DurationEntry {
      String durationPattern;
      long maxHit;
      long duration;
      long durationMillis;
      Long durationTypeValue;
      String durationTypeName;
      String durationType;

      private DurationEntry() {
      }

      // $FF: synthetic method
      DurationEntry(Object x0) {
         this();
      }
   }
}
