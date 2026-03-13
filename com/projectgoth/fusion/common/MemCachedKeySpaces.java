package com.projectgoth.fusion.common;

import java.util.Date;

public final class MemCachedKeySpaces {
   private static long convertTimeUnitToMilliSeconds(long cacheTime, MemCachedKeySpaces.TimeUnit cacheTimeUnit) throws RuntimeException {
      switch(cacheTimeUnit) {
      case MILLISECONDS:
         return cacheTime;
      case SECONDS:
         return cacheTime * 1000L;
      case MINUTES:
         return cacheTime * 60000L;
      case HOURS:
         return cacheTime * 3600000L;
      case DAYS:
         return cacheTime * 86400000L;
      default:
         throw new RuntimeException("Invalid TimeUnit value provided");
      }
   }

   public static enum TokenKeySpace implements MemCachedKeySpaces.MemCachedKeySpaceInterface {
      FORGOT_PASSWORD("FGT_PWD", 2L, MemCachedKeySpaces.TimeUnit.DAYS);

      private final String name;
      private final long cacheTime;
      private final Integer pageSize;
      private final MemCachedUtils.Instance memcachedInstance;
      private final String keyIdentifier;

      public String getName() {
         return this.name;
      }

      public long getCacheTime() {
         return this.cacheTime;
      }

      public Integer getPageSize() {
         return this.pageSize;
      }

      public Date getExpiryDate() {
         return new Date(System.currentTimeMillis() + this.cacheTime);
      }

      public MemCachedUtils.Instance getMemCachedInstance() {
         return this.memcachedInstance;
      }

      private TokenKeySpace(String name, long cacheTime, MemCachedKeySpaces.TimeUnit cacheTimeUnit) {
         this(name, cacheTime, cacheTimeUnit, (Integer)null);
      }

      private TokenKeySpace(String name, long cacheTime, MemCachedKeySpaces.TimeUnit cacheTimeUnit, Integer pageSize) {
         this.keyIdentifier = "TKN_ID";
         this.name = MemCachedKeyUtils.getFullKeyFromStrings("TKN_ID", name);
         this.cacheTime = MemCachedKeySpaces.convertTimeUnitToMilliSeconds(cacheTime, cacheTimeUnit);
         this.pageSize = pageSize;
         this.memcachedInstance = MemCachedUtils.Instance.rateLimit;
      }
   }

   public static enum CommonKeySpace implements MemCachedKeySpaces.MemCachedKeySpaceInterface {
      ACCOUNT_BALANCE("BAL", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      ALERT_MESSAGE("AM", 5L, MemCachedKeySpaces.TimeUnit.MINUTES),
      AVATAR_COMMENTS_COUNT("ACC", 5L, MemCachedKeySpaces.TimeUnit.MINUTES),
      APPMENU("APM", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      APPMENU_OPTIONS("APMO", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      BOT_WARRIORS_BERSERK("BCH_BZ", 1L, MemCachedKeySpaces.TimeUnit.HOURS),
      BOT_WARRIORS_FEVER("BCH_FV", 1L, MemCachedKeySpaces.TimeUnit.HOURS),
      BOT_CHALLENGE_GAME_DATA("BCH_DATA", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      BLOCK_LIST("BL", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      CAMPAIGN("CMPGN", 1L, MemCachedKeySpaces.TimeUnit.DAYS),
      CAMPAIGN_PARTICIPANT("CMPGNP", 1L, MemCachedKeySpaces.TimeUnit.DAYS),
      CAMPAIGN_MOBILE("CMPGNM", 1L, MemCachedKeySpaces.TimeUnit.DAYS),
      CHATROOM("CHR", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      CHATROOMS_OWNED_COUNT("CHROC", 5L, MemCachedKeySpaces.TimeUnit.MINUTES),
      CHATROOM_BAN("CHB", 24L, MemCachedKeySpaces.TimeUnit.HOURS),
      CHATROOM_BLOCK_IP("CHBI", 30L, MemCachedKeySpaces.TimeUnit.MINUTES),
      CHATROOM_MODERATORS("CRM", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      CHATROOM_BANNED_USERS("CRBU", 7L, MemCachedKeySpaces.TimeUnit.DAYS, 10000),
      CHATROOM_NEGATIVE_CACHE("CRNC", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      CHATROOM_THEME("CRT", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      CONTACT_GROUP("CGR", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      CONTACT_LIST_VERSION("CLVV", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      CURRENCY("CUR", 15L, MemCachedKeySpaces.TimeUnit.MINUTES),
      DID_NUMBER("DID", 5L, MemCachedKeySpaces.TimeUnit.MINUTES),
      DISTRIBUTED_LOCK("LOCK", 30L, MemCachedKeySpaces.TimeUnit.SECONDS),
      EMOTE_GETMYLUCK("EGML", 1L, MemCachedKeySpaces.TimeUnit.DAYS),
      EMOTICON_PACKS_OWNED("EPO", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      EXTERNAL_EMAIL_VERIFICATION_TOKEN("EMVT", 604800L, MemCachedKeySpaces.TimeUnit.SECONDS),
      GET_INITIAL_COUNTER_VALUE_LOCK("GET_ICTR_LCK", 2L, MemCachedKeySpaces.TimeUnit.MINUTES),
      GIFTS_RECEIVED_COUNT("GRC", 5L, MemCachedKeySpaces.TimeUnit.MINUTES),
      GLOBAL_COLLECT_REPORT_FEED("GCRF", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      GROUP("GRP", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      GROUP_ANNOUNCEMENT("GA", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      GROUP_COUNT("GRPC", 1L, MemCachedKeySpaces.TimeUnit.HOURS),
      GROUPS_JOINED_COUNT("GRPJC", 5L, MemCachedKeySpaces.TimeUnit.MINUTES),
      GROUP_INVITATION("GI", 30L, MemCachedKeySpaces.TimeUnit.DAYS),
      GROUP_SUSPENSION("GRP_S", 1L, MemCachedKeySpaces.TimeUnit.HOURS),
      LOGIN_BAN("LB", 1L, MemCachedKeySpaces.TimeUnit.HOURS),
      MAX_SYSTEM_MIGLEVEL("MSML", 1L, MemCachedKeySpaces.TimeUnit.DAYS),
      MERCHANT_TAG_COUNT("MTTC", 1L, MemCachedKeySpaces.TimeUnit.DAYS),
      MERCHANT_TAG("MTT", 1L, MemCachedKeySpaces.TimeUnit.DAYS),
      MERCHANT_DETAILS("MTD", 1L, MemCachedKeySpaces.TimeUnit.DAYS),
      MERCHANT_GAME_LIMIT("MGLT", 1L, MemCachedKeySpaces.TimeUnit.DAYS),
      NUM_VIRTUAL_GIFTS_RECEIVED("NVGR", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      PAYPAL_TXN("PAYPAL_TXN", 3L, MemCachedKeySpaces.TimeUnit.HOURS),
      PAYPAL_TXN_LOCK("PAYPAL_TXNLCK", 2L, MemCachedKeySpaces.TimeUnit.MINUTES),
      PAINT_WARS_ITEMS("PWI", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      PHOTOS_UPLOADED_COUNT("PUC", 5L, MemCachedKeySpaces.TimeUnit.MINUTES),
      RECENT_CHATROOM_COUNT("RCC", 20L, MemCachedKeySpaces.TimeUnit.MINUTES),
      REPUTATION_SCORE("RSC", 1L, MemCachedKeySpaces.TimeUnit.HOURS),
      REPUTATION_LEVEL_DATA("RLD", 1L, MemCachedKeySpaces.TimeUnit.DAYS),
      SCRAPBOOK("SB", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      SSO_LOGIN_CAPTCHA_MAPPING("SLCM", 5L, MemCachedKeySpaces.TimeUnit.MINUTES),
      SYSTEM_PROPERTY("SYS", 1L, MemCachedKeySpaces.TimeUnit.MINUTES),
      TEMP_LOGIN_SUSPENSION("TL_S", (long)((int)(Math.random() + 0.5D) * 30), MemCachedKeySpaces.TimeUnit.SECONDS),
      USER_ALIAS_BY_USERID("UA_ID", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      USER_ALIAS_BY_USERNAME("UA_N", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      USER_ID("UID", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      USER_ID_BY_ALIAS("UID_A", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      USER_NAME_BY_ID("UN", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      USER_NAME_BY_ALIAS("UN_A", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      USER_PROFILE("UP", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      USER_PROFILE_STATUS("UPS", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      USER_SETTING("US", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      USER_REFERRAL_SUCCESS_RATE("URS", 1L, MemCachedKeySpaces.TimeUnit.DAYS),
      VAS_BUILD("VB", 5L, MemCachedKeySpaces.TimeUnit.MINUTES),
      VIRTUAL_GIFT_FEATURED_POPULAR_NEW("VG_FPN", 10L, MemCachedKeySpaces.TimeUnit.MINUTES),
      SECURITY_QUESTION("SEC_QN", 10L, MemCachedKeySpaces.TimeUnit.MINUTES),
      LOGIN_DATA("LG_DTA", 20L, MemCachedKeySpaces.TimeUnit.MINUTES),
      MIMOPAY_LOCK_RELOADKEY("MIMO_L_RK", 2L, MemCachedKeySpaces.TimeUnit.MINUTES),
      MINIMUM_CLIENT_VERSION("MCV", 5L, MemCachedKeySpaces.TimeUnit.MINUTES),
      EMAIL_TEMPLATE("EMT", 10L, MemCachedKeySpaces.TimeUnit.MINUTES),
      CHATROOM_BY_ID("CHRI", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      CAPTCHA_REQUIRED("CAP_RE", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      FAUX_DATA_GRID("FDG", 1L, MemCachedKeySpaces.TimeUnit.HOURS);

      private final String name;
      private final long cacheTime;
      private final Integer pageSize;
      private final MemCachedUtils.Instance memcachedInstance;

      public String getName() {
         return this.name;
      }

      public long getCacheTime() {
         return this.cacheTime;
      }

      public Integer getPageSize() {
         return this.pageSize;
      }

      public Date getExpiryDate() {
         return new Date(System.currentTimeMillis() + this.cacheTime);
      }

      public MemCachedUtils.Instance getMemCachedInstance() {
         return this.memcachedInstance;
      }

      private CommonKeySpace(String name, long cacheTime, MemCachedKeySpaces.TimeUnit cacheTimeUnit) {
         this(name, cacheTime, cacheTimeUnit, (Integer)null);
      }

      private CommonKeySpace(String name, long cacheTime, MemCachedKeySpaces.TimeUnit cacheTimeUnit, Integer pageSize) {
         this.name = name;
         this.cacheTime = MemCachedKeySpaces.convertTimeUnitToMilliSeconds(cacheTime, cacheTimeUnit);
         this.pageSize = pageSize;
         this.memcachedInstance = MemCachedUtils.Instance.common;
      }
   }

   public static enum RateLimitKeySpace implements MemCachedKeySpaces.MemCachedKeySpaceInterface {
      FAILED_LOGIN_ATTEMPTS("FLA", 7L, MemCachedKeySpaces.TimeUnit.DAYS),
      FAILED_AUTHENTICATION_ATTEMPTS("FAA", 24L, MemCachedKeySpaces.TimeUnit.HOURS),
      FORGOT_PASSWORD_ATTEMPTS("FPA", 1440L, MemCachedKeySpaces.TimeUnit.MINUTES),
      GENERIC_EMOTE_RATE_LIMIT("GENERIC_EMOTE_RL", 1L, MemCachedKeySpaces.TimeUnit.MINUTES),
      GENERIC_FUSION_REQUEST_RATE_LIMIT("GENERIC_FUSION_REQUEST_RL", 10L, MemCachedKeySpaces.TimeUnit.MINUTES),
      MAX_CONCURRENT_SESSIONS_BREACHED("MCCSB", 1L, MemCachedKeySpaces.TimeUnit.HOURS),
      PAYMENT_RATE_LIMIT("PRL", 1L, MemCachedKeySpaces.TimeUnit.HOURS),
      RATE_LIMIT("RLT", 0L, MemCachedKeySpaces.TimeUnit.DAYS),
      USER_REFERRAL_RATE_LIMIT("UR_RL", 1L, MemCachedKeySpaces.TimeUnit.DAYS),
      VIRTUAL_GIFT_RATE_LIMIT("VG_RL", 1L, MemCachedKeySpaces.TimeUnit.MINUTES),
      CAMPAIGN_RATE_LIMIT("CAMPAIGN_RL", 1L, MemCachedKeySpaces.TimeUnit.MINUTES);

      private final String name;
      private final long cacheTime;
      private final Integer pageSize;
      private final MemCachedUtils.Instance memcachedInstance;

      public String getName() {
         return this.name;
      }

      public long getCacheTime() {
         return this.cacheTime;
      }

      public Integer getPageSize() {
         return this.pageSize;
      }

      public Date getExpiryDate() {
         return new Date(System.currentTimeMillis() + this.cacheTime);
      }

      public MemCachedUtils.Instance getMemCachedInstance() {
         return this.memcachedInstance;
      }

      private RateLimitKeySpace(String name, long cacheTime, MemCachedKeySpaces.TimeUnit cacheTimeUnit) {
         this(name, cacheTime, cacheTimeUnit, (Integer)null);
      }

      private RateLimitKeySpace(String name, long cacheTime, MemCachedKeySpaces.TimeUnit cacheTimeUnit, Integer pageSize) {
         this.name = name;
         this.cacheTime = MemCachedKeySpaces.convertTimeUnitToMilliSeconds(cacheTime, cacheTimeUnit);
         this.pageSize = pageSize;
         this.memcachedInstance = MemCachedUtils.Instance.rateLimit;
      }
   }

   public interface MemCachedKeySpaceInterface {
      String getName();

      long getCacheTime();

      Integer getPageSize();

      Date getExpiryDate();

      MemCachedUtils.Instance getMemCachedInstance();
   }

   public static enum TimeUnit {
      MILLISECONDS,
      SECONDS,
      MINUTES,
      HOURS,
      DAYS;
   }
}
