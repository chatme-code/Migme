package com.projectgoth.fusion.rewardsystem;

import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.FulfilledFuture;
import com.projectgoth.fusion.common.HostUtils;
import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.RedisQueue;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserReputationScoreAndLevelData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.Rewards;
import com.projectgoth.fusion.interfaces.RewardsHome;
import com.projectgoth.fusion.rewardsystem.mmv2.MMv2TriggerSender;
import com.projectgoth.fusion.rewardsystem.notification.TemplateDataProvider;
import com.projectgoth.fusion.rewardsystem.outcomes.RewardProgramOutcomeProcessorInstances;
import com.projectgoth.fusion.rewardsystem.outcomes.UserRewardOutcome;
import com.projectgoth.fusion.rewardsystem.processors.RewardProgramProcessor;
import com.projectgoth.fusion.rewardsystem.processors.RewardProgramProcessorFactory;
import com.projectgoth.fusion.rewardsystem.stateprocessors.UserProgramCASState;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.leto.common.event.userreward.UserMigLevelIncreaseEvent;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

public class RewardCentre {
   public static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RewardCentre.class));
   private static RewardCentre instance = new RewardCentre();
   private LazyLoader<HashMap<String, Integer>> rewardScoreCapCache;
   private RewardProgramOutcomeProcessorInstances outcomeProcessorInstances;
   private ThreadPoolExecutor executor;
   private static final FulfilledFuture<Boolean> PROCESSED_FUTURE;
   private static final String COMPLETED_FIELD = "Completed";
   private static final String QUANTITY_FIELD = "Quantity";
   private static final String AMOUNT_FIELD = "Amount";
   private static final long millisInDay = 86400000L;

   private RewardCentre() {
      this.rewardScoreCapCache = new LazyLoader<HashMap<String, Integer>>(SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.REWARD_CENTRE_SCORECAP_REFRESH_MILLIS)) {
         protected HashMap<String, Integer> fetchValue() throws Exception {
            Rewards rewardEJB = (Rewards)EJBHomeCache.getObject("ejb/Rewards", RewardsHome.class);
            return rewardEJB.getRewardScoreCap();
         }
      };
      this.outcomeProcessorInstances = new RewardProgramOutcomeProcessorInstances();
      this.executor = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
   }

   protected RewardPrograms getRewardPrograms() {
      return RewardPrograms.Instance.get();
   }

   protected RewardCentre(int dummy) {
      this.rewardScoreCapCache = new LazyLoader<HashMap<String, Integer>>(SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.REWARD_CENTRE_SCORECAP_REFRESH_MILLIS)) {
         protected HashMap<String, Integer> fetchValue() throws Exception {
            Rewards rewardEJB = (Rewards)EJBHomeCache.getObject("ejb/Rewards", RewardsHome.class);
            return rewardEJB.getRewardScoreCap();
         }
      };
      this.outcomeProcessorInstances = new RewardProgramOutcomeProcessorInstances();
      this.executor = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
   }

   public static RewardCentre getInstance() {
      return instance;
   }

   public static RewardCentre setInstance(RewardCentre newInstance) {
      RewardCentre oldInstance = instance;
      instance = newInstance;
      return oldInstance;
   }

   public RewardProgramData getRewardProgram(int rewardProgramID) {
      return this.getRewardPrograms().get(rewardProgramID);
   }

   public void resetCachedRewardPrograms() {
      this.getRewardPrograms().resetCachedProperties();
      RewardProgramProcessorFactory.resetProgramProcessorMap();
   }

   public final Future<Boolean> sendTrigger(RewardProgramTrigger trigger) throws Exception {
      this.sendToMMv2(trigger);
      return this.sendToMMv1(trigger);
   }

   private void sendToMMv2(RewardProgramTrigger trigger) {
      try {
         MMv2TriggerSender.getInstance().send(trigger);
      } catch (Exception var3) {
         log.error("Failed to send to MMv2.Exception:" + var3, var3);
      }

   }

   private Future<Boolean> sendToMMv1(RewardProgramTrigger trigger) throws Exception {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.ENABLE_MM_V1)) {
         if (log.isDebugEnabled()) {
            log.debug("Sending trigger [" + trigger + "] to MMv1");
         }

         TriggerProcessingContext ctx = new TriggerProcessingContext(trigger);
         ctx.received();
         return this.processTrigger(ctx, trigger);
      } else {
         if (log.isDebugEnabled()) {
            log.debug("Not sending trigger [" + trigger + "] to MMv1");
         }

         return PROCESSED_FUTURE;
      }
   }

   protected Future<Boolean> processTrigger(TriggerProcessingContext context, RewardProgramTrigger trigger) throws Exception {
      int queueSize = this.executor.getQueue().size();
      int maxQueueSize = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.REWARD_CENTRE_QUEUE_MAXSIZE);
      if (queueSize <= maxQueueSize) {
         if (log.isDebugEnabled()) {
            log.debug("Submitting trigger for async execution: " + trigger.toString());
         }

         return this.executor.submit(this.createWorker(context, trigger));
      } else {
         context.dropped();
         log.warn("RewardCentreWorker queue too busy (" + queueSize + "/" + maxQueueSize + "). Dropping Trigger: " + trigger.toString());
         return null;
      }
   }

   public UserReputationScoreAndLevelData getUserReputationScoreAndLevelData(UserData userData) throws RemoteException, CreateException {
      boolean fetchUserReputationFromMaster = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.FETCH_USER_REPUTATION_FROM_MASTER);
      UserReputationScoreAndLevelData reputation = MemCacheOrEJB.getUserReputationScoreAndLevelData(fetchUserReputationFromMaster, userData);
      return reputation;
   }

   private void checkPrograms(TriggerProcessingContext context, RewardProgramTrigger trigger) throws Throwable {
      if ((Boolean)SystemPropertyEntities.Temp.Cache.se218Enabled.getValue()) {
         this.checkProgramsMinimizeUserReputationCheck(context, trigger);
      } else {
         this.checkProgramsOld(context, trigger);
      }

   }

   /** @deprecated */
   @Deprecated
   private void checkProgramsOld(TriggerProcessingContext context, RewardProgramTrigger trigger) throws Throwable {
      RewardPrograms programs = this.getRewardPrograms();
      List<RewardProgramProcessor> processors = RewardProgramProcessorFactory.getProcessorsForProgramType(trigger.programType);
      if (log.isDebugEnabled()) {
         log.debug("Received trigger [" + trigger.programType + "]");
         log.debug("Found " + processors.size() + " processors tied to " + trigger.programType);
         log.debug("Found [" + programs.get(trigger.programType).size() + "] programs for type [" + trigger.programType + "]");
      }

      Jedis redisConnection = null;
      RedisQueue queue = null;

      try {
         redisConnection = Redis.getMasterInstanceForUserID(trigger.userData.userID);
         if (redisConnection == null) {
            throw new Exception("Unable to get master instance of user " + trigger.userData.userID);
         }

         queue = RedisQueue.getInstance();
         if (queue == null) {
            throw new Exception("Unable to get redis queue instance ");
         }

         if (AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.PARTICIPATE_IN_MARKETING_MECHANICS, trigger.userData)) {
            UserReputationScoreAndLevelData reputation = this.getUserReputationScoreAndLevelData(trigger.userData);
            Iterator i$ = programs.get(trigger.programType).iterator();

            while(true) {
               RewardProgramData program;
               do {
                  if (!i$.hasNext()) {
                     return;
                  }

                  program = (RewardProgramData)i$.next();
                  if (log.isDebugEnabled()) {
                     log.debug("  Checking trigger against program :" + program.id + ";name=" + program.name + ";type=" + program.type);
                     log.debug("  Program.isActive [" + program.isActive(trigger.userData.countryID, reputation.level, trigger.userData.type) + "]");
                  }
               } while(!program.isActive(trigger.userData.countryID, reputation.level, trigger.userData.type));

               boolean triggerIsValid = true;
               Iterator i$ = processors.iterator();

               while(i$.hasNext()) {
                  RewardProgramProcessor proc = (RewardProgramProcessor)i$.next();
                  triggerIsValid = triggerIsValid && proc.checkValidity(program, trigger);
                  if (log.isDebugEnabled()) {
                     log.debug("    Processor: " + proc.toString() + " triggerIsValid:" + triggerIsValid);
                  }

                  if (!triggerIsValid) {
                     break;
                  }
               }

               if (log.isDebugEnabled()) {
                  log.debug("Trigger:[" + trigger + "]  isValid:[" + triggerIsValid + "]");
               }

               if (triggerIsValid) {
                  this.checkProgram(program, trigger, redisConnection, queue);
               }
            }
         }
      } finally {
         Redis.disconnect(redisConnection, log);
         if (queue != null) {
            try {
               queue.disconnect();
            } catch (IOException var19) {
               log.warn("Failed to close RedisQueue connection, reason: " + var19.getLocalizedMessage());
            }
         }

      }

   }

   public boolean hasValidTrigger(RewardProgramData rewardProgramData, RewardProgramTrigger trigger) {
      UserData userData = trigger.userData;
      if (userData == null) {
         if (userData == null) {
            log.error("Trigger [" + trigger + "] has null userData");
         }

         return false;
      } else if (rewardProgramData.status != RewardProgramData.StatusEnum.ACTIVE) {
         if (log.isDebugEnabled()) {
            log.debug("Reward program [" + rewardProgramData.id + "]:rewardProgramData.status [" + rewardProgramData.status + "]");
         }

         return false;
      } else if (rewardProgramData.userType != null && rewardProgramData.userType != userData.type) {
         if (log.isDebugEnabled()) {
            log.debug("Reward program [" + rewardProgramData.id + "]:rewardProgramData.userType [" + rewardProgramData.userType + "] userData.type [" + userData.type + "]");
         }

         return false;
      } else if (rewardProgramData.countryID != null && rewardProgramData.countryID.equals(userData.countryID)) {
         if (log.isDebugEnabled()) {
            log.debug("Reward program [" + rewardProgramData.id + "]:rewardProgramData.userType [" + rewardProgramData.countryID + "] userData.countryID [" + userData.countryID + "]");
         }

         return false;
      } else {
         long now = System.currentTimeMillis();
         if (rewardProgramData.startDate != null && now < rewardProgramData.startDate.getTime()) {
            if (log.isDebugEnabled()) {
               log.debug("Reward program [" + rewardProgramData.id + "]:rewardProgramData.startDate [" + rewardProgramData.startDate + "] now [" + new Timestamp(now) + "]");
            }

            return false;
         } else if (rewardProgramData.endDate != null && now > rewardProgramData.endDate.getTime()) {
            if (log.isDebugEnabled()) {
               log.debug("Reward program [" + rewardProgramData.id + "]:rewardProgramData.endDate [" + rewardProgramData.endDate + "] now [" + new Timestamp(now) + "]");
            }

            return false;
         } else {
            return true;
         }
      }
   }

   public boolean hasValidReputation(RewardProgramData rewardProgramData, int userLevel) {
      if (rewardProgramData.minMigLevel != null && rewardProgramData.minMigLevel > 1 && userLevel < rewardProgramData.minMigLevel) {
         return false;
      } else {
         return rewardProgramData.maxMigLevel == null || userLevel <= rewardProgramData.maxMigLevel;
      }
   }

   private boolean isValidTriggerAndReputation(RewardProgramData rewardProgramData, RewardProgramTrigger trigger, Integer userMigLevelToCheck) {
      if (!this.hasValidTrigger(rewardProgramData, trigger)) {
         return false;
      } else {
         if (userMigLevelToCheck != null) {
            if (!this.hasValidReputation(rewardProgramData, userMigLevelToCheck)) {
               if (log.isDebugEnabled()) {
                  log.debug("RewardProgram:[" + rewardProgramData.id + "] user level upon trigger entry [" + userMigLevelToCheck + "] doesn't meet required mig level");
               }

               return false;
            }
         } else if (log.isDebugEnabled()) {
            log.debug("RewardProgram:[" + rewardProgramData.id + "] reputation level check not performed");
         }

         return true;
      }
   }

   private Integer getUserMigLevelUponTriggerEntry(RewardPrograms.RewardProgramDataList rewardProgramDataList, RewardProgramTrigger trigger) throws CreateException, RemoteException {
      if (rewardProgramDataList.needsToCheckUserReputation()) {
         if (trigger instanceof UserMigLevelIncreaseEvent) {
            return ((UserMigLevelIncreaseEvent)trigger).getNewMigLevel();
         } else {
            UserReputationScoreAndLevelData reputationLevelData = this.getUserReputationScoreAndLevelData(trigger.userData);
            if (reputationLevelData == null) {
               throw new IllegalArgumentException("null reputationLevelData for [" + trigger.userData.userID + "]");
            } else if (reputationLevelData.level == null) {
               throw new IllegalArgumentException("null 'reputationLevelData.level' for [" + trigger.userData.userID + "]");
            } else {
               return reputationLevelData.level;
            }
         }
      } else {
         return null;
      }
   }

   private void checkProgramsMinimizeUserReputationCheck(TriggerProcessingContext context, RewardProgramTrigger trigger) throws Throwable {
      if (trigger.userData == null) {
         throw new IllegalArgumentException("Null userData in trigger [" + trigger + "]");
      } else {
         RewardPrograms programs = this.getRewardPrograms();
         List<RewardProgramProcessor> processors = RewardProgramProcessorFactory.getProcessorsForProgramType(trigger.programType);
         RewardPrograms.RewardProgramDataList rewardProgramDataList = programs.getRewardPrograms(trigger.programType);
         if (log.isDebugEnabled()) {
            log.debug("Received trigger type [" + trigger.programType + "]." + "Reward programs[" + rewardProgramDataList.getRewardPrograms().size() + "],needsReputationLevelCheck:[" + rewardProgramDataList.needsToCheckUserReputation() + "],Reward Program Processors:[" + processors + "]");
         }

         Jedis redisConnection = null;
         RedisQueue queue = null;

         try {
            redisConnection = Redis.getMasterInstanceForUserID(trigger.userData.userID);
            if (redisConnection == null) {
               throw new Exception("Unable to get master instance of user " + trigger.userData.userID);
            }

            queue = RedisQueue.getInstance();
            if (queue == null) {
               throw new Exception("Unable to get redis queue instance ");
            }

            if (AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.PARTICIPATE_IN_MARKETING_MECHANICS, trigger.userData)) {
               Integer userMigLevelUponTrigger = this.getUserMigLevelUponTriggerEntry(rewardProgramDataList, trigger);
               Iterator i$ = rewardProgramDataList.getRewardPrograms().iterator();

               while(true) {
                  RewardProgramData program;
                  boolean passedCommonValidation;
                  do {
                     if (!i$.hasNext()) {
                        return;
                     }

                     program = (RewardProgramData)i$.next();
                     passedCommonValidation = this.isValidTriggerAndReputation(program, trigger, userMigLevelUponTrigger);
                     if (log.isDebugEnabled()) {
                        log.debug("Checking trigger against program :" + program.id + ";name=" + program.name + ";type=" + program.type + ";trigger=[" + trigger + "];passedCommonValidation:[" + passedCommonValidation + "]");
                     }
                  } while(!passedCommonValidation);

                  boolean triggerIsValid = true;
                  Iterator i$ = processors.iterator();

                  while(i$.hasNext()) {
                     RewardProgramProcessor proc = (RewardProgramProcessor)i$.next();
                     triggerIsValid = triggerIsValid && proc.checkValidity(program, trigger);
                     if (log.isDebugEnabled()) {
                        log.debug("Processor: " + proc.toString() + " triggerIsValid:" + triggerIsValid);
                     }

                     if (!triggerIsValid) {
                        break;
                     }
                  }

                  if (log.isDebugEnabled()) {
                     log.debug("Trigger:[" + trigger + "]  passed all validation:[" + triggerIsValid + "]");
                  }

                  if (triggerIsValid) {
                     this.checkProgram(program, trigger, redisConnection, queue);
                  }
               }
            }
         } finally {
            Redis.disconnect(redisConnection, log);
            if (queue != null) {
               try {
                  queue.disconnect();
               } catch (IOException var22) {
                  log.warn("Failed to close RedisQueue connection, reason: " + var22.getLocalizedMessage());
               }
            }

         }

      }
   }

   private void checkProgram(RewardProgramData program, RewardProgramTrigger trigger, Jedis r, RedisQueue queue) throws Exception {
      int userID = trigger.userData.userID;
      int quantityDelta = trigger.quantityDelta;
      double amountDelta = trigger.amountDelta;
      String currency = trigger.currency;
      String key = "RewardProgram:" + program.id + ":" + userID;
      String completionRateLimitKey = "" + program.id + ':' + userID;
      if (program.rewardFrequency == RewardProgramData.RewardFrequencyEnum.ONCE_OFF) {
         if (r.hexists(key, "Completed")) {
            return;
         }
      } else if (program.completionRateLimit != null && !MemCachedRateLimiter.checkWithoutHit(MemCachedRateLimiter.NameSpace.REWARD_PRG, completionRateLimitKey, program.completionRateLimit)) {
         if (log.isDebugEnabled()) {
            log.debug("RATE LIMIT HIT: " + key + " " + program.completionRateLimit);
         }

         return;
      }

      boolean checkScoreCapFirst = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.REWARD_CENTRE_SCORECAP_CHECKED_FIRST_ENABLED);
      if (!checkScoreCapFirst || !program.isReputationMechanic() || this.checkScoreCap(program, trigger, r, false)) {
         boolean requirementsMet = true;
         if (program.quantityRequired > 0) {
            if (r.hincrBy(key, "Quantity", (long)quantityDelta) < (long)program.quantityRequired) {
               requirementsMet = false;
            }

            if (log.isDebugEnabled()) {
               log.debug(" CheckProgram. Quantity Required [" + program.quantityRequired + "] Current Quantity [" + r.hget(key, "Quantity") + "]");
            }
         }

         double amountDeltaInProgramCurrency;
         if (program.amountRequired > 0.0D) {
            amountDeltaInProgramCurrency = this.convertCurrency(amountDelta, currency, program.amountRequiredCurrency) * 100.0D;
            if ((double)r.hincrBy(key, "Amount", (long)((int)amountDeltaInProgramCurrency)) < program.amountRequired * 100.0D) {
               requirementsMet = false;
            }

            if (log.isDebugEnabled()) {
               log.debug(" CheckProgram. Amount Required [" + program.amountRequired * 100.0D + "] Current Amount [" + r.hget(key, "Amount") + "]");
            }
         } else {
            amountDeltaInProgramCurrency = 0.0D;
         }

         if (checkScoreCapFirst && requirementsMet && program.isReputationMechanic()) {
            requirementsMet = this.checkScoreCap(program, trigger, r, true);
            if (!requirementsMet) {
               this.incrementReputationScoreAwardedCounter(r, -program.scoreReward, userID, program.category);
               r.hincrBy(key, "Quantity", (long)(-quantityDelta));
               if (amountDeltaInProgramCurrency > 0.0D) {
                  r.hincrBy(key, "Amount", (long)((int)(-amountDeltaInProgramCurrency)));
               }
            }
         }

         boolean fullfillUserProgramState = UserProgramCASState.apply(r, program, trigger, key);
         requirementsMet = requirementsMet && fullfillUserProgramState;
         if (!checkScoreCapFirst && requirementsMet && program.isReputationMechanic()) {
            requirementsMet = this.checkScoreCap(program, trigger, r, true);
         }

         if (requirementsMet) {
            if (log.isDebugEnabled()) {
               log.debug("requirements for rewardprogram[id=" + program.id + ";name=" + program.name + ";type=" + program.type + "] for userid[" + userID + "] is met.");
            }

            if (program.rewardFrequency == RewardProgramData.RewardFrequencyEnum.ONCE_OFF) {
               long completed = r.hincrBy(key, "Completed", 1L);
               if (completed == 1L) {
                  r.hdel(key, new String[]{"Quantity"});
                  r.hdel(key, new String[]{"Amount"});
                  this.dispatchReward(program, trigger, queue);
               }
            } else {
               r.hincrBy(key, "Quantity", (long)(-program.quantityRequired));
               r.hincrBy(key, "Amount", (long)((int)(-program.amountRequired * 100.0D)));
               boolean reachedMaxCompletion = false;
               if (program.completionRateLimit != null) {
                  try {
                     MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.REWARD_PRG, completionRateLimitKey, program.completionRateLimit);
                  } catch (MemCachedRateLimiter.LimitExceeded var19) {
                     reachedMaxCompletion = true;
                  }

                  if (log.isDebugEnabled()) {
                     log.debug("reachedMaxCompletion [" + reachedMaxCompletion + "]  programID [" + program.id + "] programType [" + program.type + "] rateLimit [" + program.completionRateLimit + "]");
                  }
               }

               if (log.isDebugEnabled() && program.completionRateLimit != null) {
                  log.debug("   currentHit [" + MemCachedRateLimiter.checkWithoutHit(MemCachedRateLimiter.NameSpace.REWARD_PRG, completionRateLimitKey, program.completionRateLimit));
               }

               if (!reachedMaxCompletion) {
                  this.dispatchReward(program, trigger, queue);
               }
            }
         }

      }
   }

   public void resetProgramScoreForUser(RewardProgramData program, int userID) throws Exception {
      Jedis r = null;
      String key = "RewardProgram:" + program.id + ":" + userID;

      try {
         r = Redis.getMasterInstanceForUserID(userID);
         r.del(key);
         if (program.isReputationMechanic()) {
            r.del(getRedisKeyForReputationScoreAwarded(userID));
         }
      } catch (Exception var10) {
         throw var10;
      } finally {
         Redis.disconnect(r, log);
      }

   }

   private boolean checkScoreCap(RewardProgramData program, RewardProgramTrigger trigger, Jedis r, boolean toIncrement) throws Exception {
      int userID = trigger.userData.userID;
      Integer categoryScoreMax = null;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.REWARD_CENTRE_LEVEL_SCORECAP_ENABLED)) {
         UserReputationScoreAndLevelData reputation = this.getUserReputationScoreAndLevelData(trigger.userData);
         String cacheKey = generateCacheKeyForRewardLevelScoreCap(reputation.level, program.category.value());
         categoryScoreMax = (Integer)((HashMap)this.rewardScoreCapCache.getValue()).get(cacheKey);
      }

      if (categoryScoreMax == null) {
         categoryScoreMax = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)(new SystemPropertyEntities.MechanicsEngineCategoryDailyMaximum(program.category)));
      }

      int currentScore = false;
      int currentScore;
      if (toIncrement) {
         currentScore = this.incrementReputationScoreAwardedCounter(r, program.scoreReward, userID, program.category);
      } else {
         currentScore = this.readReputationScoreAwardedCounter(r, userID, program.category);
      }

      if (log.isDebugEnabled()) {
         log.debug("  user: " + userID + " program: " + program.id + " trigger: " + program.type + " category: " + program.category + " max:" + categoryScoreMax + " current:" + currentScore);
      }

      return currentScore <= categoryScoreMax;
   }

   private void dispatchReward(RewardProgramData program, RewardProgramTrigger trigger, RedisQueue queue) throws Exception {
      int userID = trigger.userData.userID;
      String origin = HostUtils.getHostname();
      long currentTs = System.currentTimeMillis();
      String id = UUID.randomUUID().toString();
      RewardProgramOutcomes outcomes = RewardProgramOutcomes.getOutcomes(this.outcomeProcessorInstances, program, trigger);
      UserRewardOutcome userReward = new UserRewardOutcome(id, origin, currentTs);
      userReward.setUserid(userID);
      userReward.setOutcomeDataList(outcomes.getOutcomeList());
      userReward.setTemplateData(this.getTemplateData(program, trigger, outcomes));
      String userRewardJsonStr = userReward.toJSONObject().toString();
      if (log.isDebugEnabled()) {
         log.debug("dispatchReward():queue.push=key:[RewardProgram:" + program.id + ":Dispatch];value:[" + userRewardJsonStr + "];Trigger=[" + trigger + "]");
      }

      this.dispatchRewardData(queue, program.id, userRewardJsonStr);
   }

   public void dispatchRewardData(RedisQueue redisQueue, int programId, String userRewardJsonStr) throws Exception {
      redisQueue.push("RewardProgram:" + programId + ":Dispatch", userRewardJsonStr, true);
   }

   private Map<String, String> getTemplateData(RewardProgramData rewardProgramData, RewardProgramTrigger trigger, RewardProgramOutcomes outcomes) throws Exception {
      if (rewardProgramData.emailTemplateID == null && !outcomes.isTemplateDataRequired()) {
         return null;
      } else {
         TemplateDataProvider templateDataProvider;
         if (rewardProgramData.emailTemplateDataProviderClassName != null) {
            templateDataProvider = (TemplateDataProvider)Class.forName(rewardProgramData.emailTemplateDataProviderClassName).newInstance();
         } else {
            templateDataProvider = TemplateDataProvider.getDefaultInstance();
         }

         return templateDataProvider.getTemplateData(rewardProgramData, trigger, outcomes.getOutcomeList());
      }
   }

   private double convertCurrency(double amount, String fromCurrency, String toCurrency) throws CreateException, RemoteException {
      if (toCurrency.equalsIgnoreCase(fromCurrency)) {
         return amount;
      } else {
         Account accountBean = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         return accountBean.convertCurrency(amount, fromCurrency, toCurrency);
      }
   }

   protected final Callable<Boolean> createWorker(TriggerProcessingContext context, RewardProgramTrigger trigger) {
      return new RewardCentre.RewardCentreWorker(context, trigger);
   }

   private int incrementReputationScoreAwardedCounter(Jedis jedis, int score, int userID, RewardProgramData.CategoryEnum category) {
      try {
         String key = getRedisKeyForReputationScoreAwarded(userID);
         String field = Integer.toString(category.value());
         Long resultLong = jedis.hincrBy(key, field, (long)score);
         if (resultLong == null) {
            throw new FusionException("incrementReputationScoreAwardedCounter-hincrBy returns null");
         } else {
            int newScore = resultLong.intValue();
            if (newScore == score) {
               jedis.expireAt(key, this.getEndOfDayInUnixTime());
            }

            return newScore;
         }
      } catch (Exception var9) {
         log.warn("incrementReputationScoreAwardedCounter exception handle:" + var9.getMessage());
         return Integer.MAX_VALUE;
      }
   }

   private int readReputationScoreAwardedCounter(Jedis jedis, int userID, RewardProgramData.CategoryEnum category) {
      byte resultInt = 0;

      try {
         String key = getRedisKeyForReputationScoreAwarded(userID);
         String field = Integer.toString(category.value());
         String resultString = jedis.hget(key, field);
         if (resultString == null) {
            return resultInt;
         } else {
            int resultInt = Integer.parseInt(resultString);
            return resultInt;
         }
      } catch (Exception var8) {
         log.warn("readReputationScoreAwardedCounter exception handle:" + var8.getMessage());
         return Integer.MAX_VALUE;
      }
   }

   private long getEndOfDayInUnixTime() {
      long currentTime = Calendar.getInstance().getTimeInMillis();
      return (currentTime / 86400000L * 86400000L + 86400000L) / 1000L;
   }

   public static String getRedisKeyForReputationScoreAwarded(int userID) {
      return "U:" + userID + ":RPS";
   }

   public static String generateCacheKeyForRewardLevelScoreCap(int level, int category) {
      return Integer.toString(level) + ":" + Integer.toString(category);
   }

   static {
      PROCESSED_FUTURE = new FulfilledFuture(Boolean.TRUE);
   }

   private class RewardCentreWorker implements Callable<Boolean> {
      private final RewardProgramTrigger trigger;
      private final TriggerProcessingContext context;

      RewardCentreWorker(TriggerProcessingContext context, RewardProgramTrigger trigger) {
         this.trigger = trigger;
         this.context = context;
      }

      public Boolean call() {
         try {
            this.context.dequeued();
            RewardCentre.this.checkPrograms(this.context, this.trigger);
            this.context.successful();
            Boolean var1 = true;
            return var1;
         } catch (Throwable var6) {
            this.context.failed();
            RewardCentre.log.error("Unable to execute trigger:[" + this.trigger.programType + "] id:[" + this.trigger.userData.userID + "] Exception:[" + var6.getMessage() + "]", var6);
         } finally {
            ;
         }

         return false;
      }
   }
}
