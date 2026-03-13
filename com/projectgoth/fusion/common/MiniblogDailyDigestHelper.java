package com.projectgoth.fusion.common;

import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.invitation.InvitationUtils;
import com.projectgoth.fusion.restapi.util.MigboApiUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.ejb.CreateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.PipelineBlock;

public class MiniblogDailyDigestHelper {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MiniblogDailyDigestHelper.class));
   public static final String DAILY_DIGEST_TO_USERS_BEING_FOLLOWED = "DailyDigestToUserBeingFollowed";
   public static final String HASH_TABLE_FOR_EARLIEST_FOLLOWER = "HashTableForEarlistFollower";
   private static final String postData = "{}";

   public static MiniblogDailyDigestHelper.AddRecipientResultEnum addRecipientForRecentlyFollowedDailyDigest(final int followerUserid, int followeeUserid, long currentTimeStampInMS) {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JobSchedulerSettings.DAILY_DIGEST_FOR_USERS_BEING_FOLLOWED_ENABLED)) {
         return MiniblogDailyDigestHelper.AddRecipientResultEnum.DAILY_DIGEST_TO_USERS_BEING_FOLLOWED_IS_DISABLE;
      } else if (followerUserid <= 0) {
         log.error(String.format("Unknown follower userid:%s", followerUserid));
         return MiniblogDailyDigestHelper.AddRecipientResultEnum.UNKNOWN_FOLLOWER_USERID;
      } else if (followeeUserid <= 0) {
         log.error(String.format("Unknown followee userid:%s", followeeUserid));
         return MiniblogDailyDigestHelper.AddRecipientResultEnum.UNKNOWN_FOLLOWEE_USERID;
      } else if (followeeUserid == followerUserid) {
         log.error(String.format("follower and followee should not be the same user:%s", followeeUserid));
         return MiniblogDailyDigestHelper.AddRecipientResultEnum.ERROR_SAME_FOLLOWER_FOLOOWEE;
      } else {
         String domain;
         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData userData = userBean.loadUserFromID(followeeUserid);
            if (userData == null || !userData.emailVerified || StringUtils.isEmpty(userData.emailAddress)) {
               return MiniblogDailyDigestHelper.AddRecipientResultEnum.NO_AVAILABLE_EMAIL_ADDRESS_FOR_FOLLOWEE;
            }

            if (!userData.allowToSendNewFollowerEmail()) {
               return MiniblogDailyDigestHelper.AddRecipientResultEnum.USER_CHOOSE_NOT_TO_RECEIVE_EMAIL;
            }

            if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DailyDigest.SEND_DIGEST_TO_BLACK_LIST_DOMAIN_USER_ENABLED)) {
               String[] blacklistedEmailDomains = SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.BLACKLISTED_EXTERNAL_MAIL_DOMAINS);
               if (blacklistedEmailDomains != null && blacklistedEmailDomains.length != 0) {
                  String[] arr$ = blacklistedEmailDomains;
                  int len$ = blacklistedEmailDomains.length;

                  for(int i$ = 0; i$ < len$; ++i$) {
                     domain = arr$[i$];
                     if (userData.emailAddress.endsWith(domain)) {
                        log.warn(String.format("user:%s emailaddress:%s is from blacklisted email domain:%s", followeeUserid, userData.emailAddress, domain));
                        return MiniblogDailyDigestHelper.AddRecipientResultEnum.BLACKLISTED_EMAIL_ADDRESS_FOR_FOLLOWEE;
                     }
                  }
               }
            }
         } catch (CreateException var22) {
            log.error(String.format("Failed to verify user emailaddress for user:%s", followeeUserid), var22);
            return MiniblogDailyDigestHelper.AddRecipientResultEnum.INTERNAL_ERROR;
         }

         Jedis handle = null;

         try {
            handle = Redis.getGamesMasterInstance();
            int lookBackHours = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.TIME_SCOPE_FOR_DAILY_DIGEST_TO_USERS_BEING_FOLLOWERED_IN_HOUR, 24);
            final long score = currentTimeStampInMS / 1000L;
            long OneDayAgoScore = score - (long)(lookBackHours * 60 * 60);
            domain = "DailyDigestToUserBeingFollowed";
            String hashKey = "HashTableForEarlistFollower";
            final String member = String.valueOf(followeeUserid);
            Double timeStamp = handle.zscore("DailyDigestToUserBeingFollowed", member);
            MiniblogDailyDigestHelper.AddRecipientResultEnum var14;
            if (timeStamp == null || timeStamp < (double)OneDayAgoScore) {
               handle.pipelined(new PipelineBlock() {
                  public void execute() {
                     this.zadd("DailyDigestToUserBeingFollowed", (double)score, member);
                     this.hset("HashTableForEarlistFollower", member, String.valueOf(followerUserid));
                  }
               });
               if (log.isDebugEnabled()) {
                  log.debug(String.format("zadd member:%s to key:%s with score:%s for daily digest recipient:%s, and hset key:%s, value:%s to hash table:%s", member, "DailyDigestToUserBeingFollowed", score, followeeUserid, followeeUserid, followerUserid, "HashTableForEarlistFollower"));
               }

               var14 = MiniblogDailyDigestHelper.AddRecipientResultEnum.SUCCESS_ADD_RECEIPIENT;
               return var14;
            }

            var14 = MiniblogDailyDigestHelper.AddRecipientResultEnum.NO_NEED_TO_UPDATE;
            return var14;
         } catch (Exception var20) {
            log.error("Failed to add recipient due to: " + var20, var20);
         } finally {
            Redis.disconnect(handle, log);
         }

         return MiniblogDailyDigestHelper.AddRecipientResultEnum.INTERNAL_ERROR;
      }
   }

   public static String getRecipientValueForDailyDigest(int recipientUserId) {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JobSchedulerSettings.DAILY_DIGEST_FOR_USERS_BEING_FOLLOWED_ENABLED)) {
         return null;
      } else if (recipientUserId <= 0) {
         log.error(String.format("Unknown follower userid:%s", recipientUserId));
         return null;
      } else {
         Jedis handle = null;

         try {
            handle = Redis.getGamesMasterInstance();
            String hashKey = "HashTableForEarlistFollower";
            String field = String.valueOf(recipientUserId);
            String value = handle.hget(hashKey, field);
            if (log.isDebugEnabled()) {
               log.debug(String.format("Retrieve ealiest user:%s who follows recipient:%s within a certain time scope", value, recipientUserId));
            }

            String var5 = value;
            return var5;
         } catch (Exception var10) {
            log.error("Failed to get Redis handle", var10);
         } finally {
            Redis.disconnect(handle, log);
         }

         return null;
      }
   }

   public static List<String> getRecipientsForDailyDigest(long timeStampInMS, int offset, int count) {
      List<String> recipients = null;
      Jedis handle = null;

      try {
         handle = Redis.getGamesSlaveInstance();
         int lookBackHours = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.TIME_SCOPE_FOR_DAILY_DIGEST_TO_USERS_BEING_FOLLOWERED_IN_HOUR, 24);
         long maxScore = timeStampInMS / 1000L;
         long minScore = timeStampInMS / 1000L - (long)(lookBackHours * 60 * 60);
         String key = "DailyDigestToUserBeingFollowed";
         Set<String> recipientsSet = handle.zrangeByScore(key, (double)minScore, (double)maxScore, offset, count);
         recipients = new ArrayList();
         recipients.addAll(recipientsSet);
      } catch (Exception var17) {
         log.error("Failed to get Redis handle", var17);
      } finally {
         Redis.disconnect(handle, log);
      }

      return recipients;
   }

   public static List<String> getEarlistFollowersListForRecipients(List<String> recipients) {
      if (recipients != null && recipients.size() != 0) {
         List<String> followers = null;
         Jedis handle = null;

         try {
            handle = Redis.getGamesSlaveInstance();
            String key = "HashTableForEarlistFollower";
            followers = handle.hmget(key, (String[])recipients.toArray(new String[0]));
         } catch (Exception var8) {
            log.error("Failed to get Redis handle", var8);
         } finally {
            Redis.disconnect(handle, log);
         }

         return followers;
      } else {
         log.info("empty daily digest recipient list");
         return null;
      }
   }

   public static void truncateSortedSetAndHashForDailyDigest() {
      Jedis handle = null;

      try {
         handle = Redis.getGamesMasterInstance();
         String key = "DailyDigestToUserBeingFollowed";
         String hashKey = "HashTableForEarlistFollower";
         handle.del(new String[]{"DailyDigestToUserBeingFollowed", "HashTableForEarlistFollower"});
      } catch (Exception var7) {
         log.error("Failed to get Redis handle", var7);
      } finally {
         Redis.disconnect(handle, log);
      }

   }

   public static long sendRecentlyFollowedDailyDigest(long timeStamp) {
      int count = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.MAX_RETRIEVE_COUNT);
      int offset = 0;
      List<String> recipients = null;
      long total = 0L;

      do {
         recipients = getRecipientsForDailyDigest(timeStamp, offset, count);
         List<String> followers = getEarlistFollowersListForRecipients(recipients);
         if (followers == null || followers.size() == 0) {
            log.info("empty daily digest recipient list, exit sending daily digest");
            break;
         }

         if (recipients.size() != followers.size()) {
            log.error("Failed to get correct followers list for recipients, exit sending daily digest");
            break;
         }

         MigboApiUtil apiUtil = MigboApiUtil.getInstance();
         long totalProcessed = 0L;
         long totalSentOK = 0L;
         long totalSentFailed = 0L;
         long totalNoProcess = 0L;
         long startTime = System.currentTimeMillis();
         total += (long)recipients.size();

         for(int i = 0; i < recipients.size(); ++i) {
            String follower = (String)followers.get(i);
            if (follower == null) {
               ++totalNoProcess;
            } else {
               ++totalProcessed;
               String userid = (String)recipients.get(i);
               String pathPrefix = String.format("/user/%s/email/%s?follower=%s", userid, Enums.EmailTypeEnum.DAILY_DIGEST_FOR_USERS_BEING_FOLLOWED.value(), follower);

               try {
                  if (apiUtil.postAndCheckOk(pathPrefix, "{}")) {
                     ++totalSentOK;
                     if (log.isDebugEnabled()) {
                        log.debug(String.format("Successfully triggered daily digest email for [%s] via migbo-datasvc [%s]", userid, pathPrefix));
                     }
                  } else {
                     ++totalSentFailed;
                     if (log.isDebugEnabled()) {
                        log.debug(String.format("Faled to trigger email for [%s] via migbo-datasvc [%s]", userid, pathPrefix));
                     }
                  }
               } catch (Exception var24) {
                  ++totalSentFailed;
                  log.error(String.format("Unable to send daily digest email to userid[%s]", userid), var24);
               }
            }
         }

         long timeTaken = System.currentTimeMillis() - startTime;
         log.info(String.format("Processed [%d] users.  Sent OK [%d]. Sent Failed [%d], Non-process [%d]", totalProcessed, totalSentOK, totalSentFailed, totalNoProcess));
         log.info(String.format("Migbo Daily Digest Email Trigger [COMPLETE] Time taken: %d seconds", TimeUnit.MILLISECONDS.toSeconds(timeTaken)));
         offset += count;
      } while(recipients != null || recipients.size() == count);

      return total;
   }

   public static enum AddRecipientResultEnum implements EnumUtils.IEnumValueGetter<Integer> {
      SUCCESS_ADD_RECEIPIENT(1),
      NO_NEED_TO_UPDATE(2),
      USER_CHOOSE_NOT_TO_RECEIVE_EMAIL(3),
      UNKNOWN_FOLLOWER_USERID(-1),
      UNKNOWN_FOLLOWEE_USERID(-2),
      ERROR_SAME_FOLLOWER_FOLOOWEE(-3),
      DAILY_DIGEST_TO_USERS_BEING_FOLLOWED_IS_DISABLE(-4),
      INTERNAL_ERROR(-5),
      NO_AVAILABLE_EMAIL_ADDRESS_FOR_FOLLOWEE(-6),
      BLACKLISTED_EMAIL_ADDRESS_FOR_FOLLOWEE(-7);

      private static final Map<Integer, InvitationUtils.SendInvitationResultEnum> lookup = new HashMap();
      int value;

      private AddRecipientResultEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static InvitationUtils.SendInvitationResultEnum fromValue(int v) {
         return (InvitationUtils.SendInvitationResultEnum)lookup.get(v);
      }

      public Integer getEnumValue() {
         return this.value();
      }

      static {
         EnumUtils.populateLookUpMap(lookup, InvitationUtils.SendInvitationResultEnum.class);
      }
   }
}
