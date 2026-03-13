package com.projectgoth.fusion.rewardsystem;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RedisQueue;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import com.projectgoth.fusion.rewardsystem.outcomes.RewardProgramOutcomeData;
import com.projectgoth.fusion.rewardsystem.outcomes.UserRewardOutcome;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.leto.common.impl.outcome.MMv2Outcomes;
import com.projectgoth.leto.common.outcome.Outcomes;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class RewardDispatcher implements Runnable {
   private static final int POLL_INTERVAL = 1000;
   private static final int POLL_ERROR_RETRY_INTERVAL = 1000;
   private static final int WAIT_OLD_ENGINE_SHUTDOWN_SECS = 60;
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RewardDispatcher.class));
   protected RewardProgramData program;
   private AtomicBoolean stop = new AtomicBoolean(false);
   private static RewardDispatcherFactory dispatcherFactory = null;
   private static RewardPrograms rewardPrograms = null;
   private static ThreadPoolExecutor dispatchExecutor = null;

   public static RewardDispatcherFactory getRewardDispatcherFactory() {
      if (null == dispatcherFactory) {
         setRewardDispatcherFactory(new RewardDispatcher.DefaultRewardDispatcherFactory());
      }

      return dispatcherFactory;
   }

   public static void setRewardDispatcherFactory(RewardDispatcherFactory dispatcherFactory) {
      RewardDispatcher.dispatcherFactory = dispatcherFactory;
   }

   private static RewardPrograms getRewardPrograms() {
      if (null == rewardPrograms) {
         rewardPrograms = RewardPrograms.Instance.get();
      }

      return rewardPrograms;
   }

   public static void setRewardPrograms(RewardPrograms rewardPrograms) {
      RewardDispatcher.rewardPrograms = rewardPrograms;
   }

   public RewardDispatcher(RewardProgramData program) {
      this.program = program;
   }

   public void stop() {
      this.stop.set(true);
   }

   public void run() {
      log.info("RewardDispatcher for program " + this.program.id + " (" + this.program.name + ") started");
      String dispatchKey = "RewardProgram:" + this.program.id + ":Dispatch";
      String dispatchedRewardDataStr = null;
      int userid = true;
      RedisQueue queue = null;

      try {
         while(!this.stop.get()) {
            try {
               if (queue == null) {
                  queue = RedisQueue.getInstance();
                  if (queue == null) {
                     throw new Exception("Unable to get redisQueue instance");
                  }
               }

               dispatchedRewardDataStr = queue.pop(dispatchKey, true);
            } catch (LinkageError var22) {
               log.fatal("Unexpected linkage error", var22);
               System.exit(-1);
            } catch (Exception var23) {
               log.error("Error while polling redis queue to give rewards for program ID " + this.program.id, var23);
               if (queue != null) {
                  try {
                     queue.disconnect();
                  } catch (Exception var21) {
                  }
               }

               queue = null;

               try {
                  Thread.sleep(1000L);
               } catch (InterruptedException var20) {
               }
               continue;
            }

            if (dispatchedRewardDataStr == null) {
               try {
                  Thread.sleep(1000L);
               } catch (InterruptedException var19) {
               }
            } else {
               this.doDispatch(dispatchKey, dispatchedRewardDataStr);
            }
         }
      } finally {
         if (queue != null) {
            try {
               queue.disconnect();
            } catch (Exception var18) {
               queue = null;
            }
         }

      }

      log.info("RewardDispatcher for program " + this.program.id + " (" + this.program.name + ") stopped");
   }

   protected void doDispatch(String redisQueueSubkey, String dispatchedRewardDataStr) {
      boolean processedAsUserIDString = false;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.ENABLE_PROCESSING_OUTCOME_AS_USERID_STRING)) {
         try {
            Integer.parseInt(dispatchedRewardDataStr);
            this.processRewardOutcomeAsUserIDString(dispatchedRewardDataStr);
            processedAsUserIDString = true;
         } catch (NumberFormatException var5) {
            processedAsUserIDString = false;
         } catch (Exception var6) {
            log.error("RedisQueue:[" + redisQueueSubkey + "];Unhandled exception processing data:[" + dispatchedRewardDataStr + "].Exception:" + var6, var6);
            processedAsUserIDString = true;
         }
      }

      if (!processedAsUserIDString) {
         this.processAsJSONString(redisQueueSubkey, dispatchedRewardDataStr);
      }

   }

   private void processAsJSONString(String redisQueueSubkey, String dispatchedRewardDataStr) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RewardDispatcherSettings.ENABLE_LOGGING_RECEIVED_OUTCOMES_FROM_QUEUE)) {
         log.info("RedisQueue:[" + redisQueueSubkey + "];Received:[" + dispatchedRewardDataStr + "]");
      }

      try {
         this.processAsOutcomesJSONString(redisQueueSubkey, dispatchedRewardDataStr);
         if (log.isDebugEnabled()) {
            log.debug("RedisQueue:[" + redisQueueSubkey + "];Done processing [" + dispatchedRewardDataStr + "]");
         }
      } catch (Exception var4) {
         log.error("RedisQueue:[" + redisQueueSubkey + "];Unhandled exception while processing data:[" + dispatchedRewardDataStr + "].Exception:" + var4, var4);
      }

   }

   private void processAsOutcomesJSONString(String redisQueueSubkey, String dispatchedRewardDataStr) throws CommonOutcomes.InvalidJSONException, CreateException, RemoteException {
      Outcomes outcomes = CommonOutcomes.deserialize(dispatchedRewardDataStr);
      switch(outcomes.getOutcomeType()) {
      case 1:
         this.processOutcomes(redisQueueSubkey, (UserRewardOutcome)outcomes);
         break;
      case 2:
         this.processOutcomes(redisQueueSubkey, (MMv2Outcomes)outcomes);
         break;
      default:
         throw new CommonOutcomes.InvalidJSONException("Unsupported outcome type:[" + outcomes.getOutcomeType() + "].");
      }

   }

   /** @deprecated */
   private void processRewardOutcomeAsUserIDString(String userIDStr) throws RemoteException, CreateException {
      log.info("[Deprecated]Dispatching rewards to user ID " + userIDStr + " for program ID " + this.program.id);
      int userid = StringUtil.toIntOrDefault(userIDStr, -1);
      if (userid == -1) {
         log.error(String.format("Error while giving rewards to user ID %s for program ID %d: incorrect userid '%s'", userIDStr, this.program.id, userIDStr));
      } else {
         this.giveRewards(this.program, userid, (List)null, (Map)null);
      }

   }

   private void processOutcomes(String redisQueueSubkey, UserRewardOutcome rewardOutcome) throws RemoteException, CreateException {
      log.info("RedisQueue:[" + redisQueueSubkey + "];Processing UserRewardOutcome id:[" + rewardOutcome.getId() + "] createdtime:[" + rewardOutcome.getCreateTs() + "] origin:[" + rewardOutcome.getOrigin() + "] userID:[" + rewardOutcome.getUserid() + "] programId:[" + this.program.id + "]");
      CommonOutcomes.processOutcomes(this.program.id, rewardOutcome, new AccountEntrySourceData(RewardDispatcher.class));
   }

   private void giveRewards(RewardProgramData program, int userid, List<RewardProgramOutcomeData> outcomeList, Map<String, String> templateData) throws RemoteException, CreateException {
      Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
      contentEJB.giveRewards(program.id, userid, new AccountEntrySourceData(RewardDispatcher.class), outcomeList, templateData);
   }

   private void processOutcomes(String redisQueueSubkey, MMv2Outcomes outcomes) throws RemoteException, CreateException {
      log.info("RedisQueue:[" + redisQueueSubkey + "];Processing MMv2Outcomes id:[" + outcomes.getId() + "] createdtime:[" + outcomes.getCreateTimestamp().getTime() + "] origin:[" + outcomes.getOrigin() + "] userID:[" + outcomes.getSubjectId() + "] programId:[" + outcomes.getRuleId() + "]");
      CommonOutcomes.processOutcomes(outcomes, new AccountEntrySourceData(RewardDispatcher.class));
   }

   public static String getQueueName(int id) {
      return "RewardProgram:" + id + ":Dispatch";
   }

   public static ExcecutorMetrics getExcecutorMetrics() {
      return null == dispatchExecutor ? new ExcecutorMetrics() : new ExcecutorMetrics(dispatchExecutor.getActiveCount(), dispatchExecutor.getQueue().size(), dispatchExecutor.getCorePoolSize(), dispatchExecutor.getMaximumPoolSize(), dispatchExecutor.getKeepAliveTime(TimeUnit.SECONDS), dispatchExecutor.getLargestPoolSize());
   }

   private static int calculateMaxThreads(int programSize) {
      return (int)(SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.TPOOL_MAX_THREAD_MULTIPLIER) * (double)programSize);
   }

   public static boolean newMain(RewardPrograms programs) throws FusionException {
      log.info("NEW Engine started.");
      boolean terminateSignalReceived = false;
      dispatchExecutor = new ThreadPoolExecutor(SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.TPOOL_CORE_SIZE), calculateMaxThreads(programs.getAll().size()), (long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.TPOOL_KEEPALIVE_SECS), TimeUnit.SECONDS, new LinkedBlockingQueue(SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.MAX_WORKERS_QUEUED)));
      dispatchExecutor.prestartAllCoreThreads();
      int pollIntervalTPoolSettingsMillis = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.POLL_INTERVAL_SETTINGS_SECS) * 1000;
      long timeLastSettingsChecked = System.currentTimeMillis();
      RewardDispatcher.ProgramsStateHelper programsIterator = new RewardDispatcher.ProgramsStateHelper(programs);

      while(true) {
         int maxSizeSetting;
         int keepaliveSetting;
         int pollIntervalSetting;
         for(RewardProgramData program = programsIterator.currentOrNext(); null != program; program = programsIterator.next()) {
            maxSizeSetting = dispatchExecutor.getActiveCount();
            keepaliveSetting = dispatchExecutor.getQueue().size();
            pollIntervalSetting = dispatchExecutor.getMaximumPoolSize();
            if (maxSizeSetting + keepaliveSetting >= pollIntervalSetting) {
               log.info("Reward Thread pool busy - Current size:" + maxSizeSetting + ", Queued:" + keepaliveSetting + ", Max Size:" + pollIntervalSetting + ".");
               break;
            }

            String dispatchedRewardDataStr = programsIterator.getFromQueue();
            if (null != dispatchedRewardDataStr) {
               dispatchExecutor.execute(new RewardDispatcherEx(program, dispatchedRewardDataStr));
            }
         }

         try {
            Thread.sleep(1000L);
         } catch (InterruptedException var12) {
            terminateSignalReceived = true;
            break;
         } catch (Exception var13) {
            log.error("Unknown exception occured during 'Thread.sleep':", var13);
         }

         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.ENABLE_MINIMISE_REDIS_THREADS_ENGINE)) {
            log.warn("Switching to old Engine.");
            break;
         }

         if ((long)pollIntervalTPoolSettingsMillis < System.currentTimeMillis() - timeLastSettingsChecked) {
            int coreSizeSetting = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.TPOOL_CORE_SIZE);
            if (coreSizeSetting != dispatchExecutor.getCorePoolSize()) {
               dispatchExecutor.setCorePoolSize(coreSizeSetting);
               log.info("New CorePoolSize set to " + coreSizeSetting);
            }

            maxSizeSetting = calculateMaxThreads(programs.getAll().size());
            if (maxSizeSetting != dispatchExecutor.getMaximumPoolSize()) {
               dispatchExecutor.setMaximumPoolSize(maxSizeSetting);
               log.info("New Maximum Pool Size adjusted to " + maxSizeSetting);
            }

            keepaliveSetting = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.TPOOL_KEEPALIVE_SECS);
            if ((long)keepaliveSetting != dispatchExecutor.getKeepAliveTime(TimeUnit.SECONDS)) {
               dispatchExecutor.setKeepAliveTime((long)keepaliveSetting, TimeUnit.SECONDS);
               log.info("New KeepAliveTime set to " + keepaliveSetting + " seconds.");
            }

            pollIntervalSetting = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.POLL_INTERVAL_SETTINGS_SECS) * 1000;
            if (pollIntervalSetting != pollIntervalTPoolSettingsMillis) {
               pollIntervalTPoolSettingsMillis = pollIntervalSetting;
               log.info("Polling interval to check if settings have changed changed to " + pollIntervalSetting + " seconds.");
            }

            timeLastSettingsChecked = System.currentTimeMillis();
         }
      }

      try {
         log.info("Shutting down new engine.");
         dispatchExecutor.shutdown();
         dispatchExecutor.awaitTermination((long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.WAIT_ENGINE_SHUTDOWN_SECS), TimeUnit.SECONDS);
         log.info("New engine shutdown gracefuly.");
      } catch (Exception var11) {
         log.warn("New Engine DID NOT shutdown gracefuly.", var11);
      }

      return terminateSignalReceived;
   }

   public static void main(String[] args) {
      Map<Integer, RewardDispatcher> dispatchers = null;
      ExecutorService pool = null;

      try {
         DOMConfigurator.configureAndWatch(ConfigUtils.getDefaultLog4jConfigFilename());
         RewardPrograms programs = getRewardPrograms();

         while(true) {
            if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.ENABLE_MINIMISE_REDIS_THREADS_ENGINE)) {
               log.warn("OLD Engine started. Will NOT switch back with SysProp change.");
               pool = Executors.newCachedThreadPool();
               dispatchers = new ConcurrentHashMap();

               while(true) {
                  Iterator i$ = dispatchers.keySet().iterator();

                  while(i$.hasNext()) {
                     Integer programID = (Integer)i$.next();
                     if (programs.get(programID) == null) {
                        ((RewardDispatcher)dispatchers.remove(programID)).stop();
                     }
                  }

                  i$ = programs.getAll().iterator();

                  while(i$.hasNext()) {
                     RewardProgramData program = (RewardProgramData)i$.next();
                     if (!dispatchers.containsKey(program.id)) {
                        RewardDispatcher dispatcher = getRewardDispatcherFactory().getNewInstance(program);
                        dispatchers.put(program.id, dispatcher);
                        pool.execute(dispatcher);
                     }
                  }

                  try {
                     Thread.sleep(1000L);
                  } catch (InterruptedException var19) {
                     log.fatal("Interrupted exception.", var19);
                     break;
                  } catch (Exception var20) {
                     log.fatal("Uncaught exception. continuing...", var20);
                  }
               }
            } else if (!newMain(programs)) {
               continue;
            }

            log.info("RewardDispatcher terminated.");
            break;
         }
      } catch (Exception var21) {
         log.fatal("Unexpected exception. Terminating RewardDispatcher", var21);
         System.exit(-1);
      } finally {
         if (null != dispatchers) {
            Iterator i$ = dispatchers.values().iterator();

            while(i$.hasNext()) {
               RewardDispatcher dispatcher = (RewardDispatcher)i$.next();
               dispatcher.stop();
            }

            if (null != pool) {
               pool.shutdown();
            }

            try {
               pool.awaitTermination(60L, TimeUnit.SECONDS);
            } catch (InterruptedException var18) {
            }
         }

      }

   }

   private static class ProgramsStateHelper {
      RewardDispatcher.ProgramsStateHelper.STATE state;
      private final RewardPrograms programs;
      private RedisQueue queue;
      private Iterator<RewardProgramData> iterPrograms;
      private RewardProgramData curProgram;

      public ProgramsStateHelper(RewardPrograms programs) {
         this.state = RewardDispatcher.ProgramsStateHelper.STATE.CLOSE;
         this.queue = null;
         this.iterPrograms = null;
         this.curProgram = null;
         this.programs = programs;
      }

      private void testQueue() {
         if (RewardDispatcher.ProgramsStateHelper.STATE.CLOSE == this.state) {
            this.queue = RedisQueue.getInstance();
         }

         this.state = null == this.queue ? RewardDispatcher.ProgramsStateHelper.STATE.CLOSE : RewardDispatcher.ProgramsStateHelper.STATE.OPEN;
      }

      public String getFromQueue() {
         if (RewardDispatcher.ProgramsStateHelper.STATE.CLOSE == this.state) {
            return null;
         } else {
            String dispatchKey = RewardDispatcher.getQueueName(this.curProgram.id);

            try {
               if (0 < this.queue.size(dispatchKey)) {
                  String dispatchedRewardDataStr = this.queue.pop(dispatchKey, true);
                  if (!StringUtil.isBlank(dispatchedRewardDataStr)) {
                     return dispatchedRewardDataStr;
                  }
               }
            } catch (LinkageError var5) {
               RewardDispatcher.log.fatal("Unexpected linkage error", var5);
               System.exit(-1);
            } catch (Exception var6) {
               RewardDispatcher.log.error("Error while polling redis queue to give rewards for program ID " + this.curProgram.id, var6);
               if (this.queue != null) {
                  try {
                     this.queue.disconnect();
                  } catch (Exception var4) {
                  }

                  this.queue = null;
                  this.iterPrograms = null;
                  this.curProgram = null;
                  this.state = RewardDispatcher.ProgramsStateHelper.STATE.CLOSE;
               }
            }

            return null;
         }
      }

      public RewardProgramData currentOrNext() throws FusionException {
         switch(this.state) {
         case CLOSE:
            this.testQueue();
            if (RewardDispatcher.ProgramsStateHelper.STATE.CLOSE == this.state) {
               return null;
            }
         case OPEN:
            this.iterPrograms = this.programs.getAll().iterator();
            this.state = RewardDispatcher.ProgramsStateHelper.STATE.ITERATING;
         case ITERATING:
            this.curProgram = (RewardProgramData)this.iterPrograms.next();
            this.state = RewardDispatcher.ProgramsStateHelper.STATE.HOLD;
         case HOLD:
            return this.curProgram;
         default:
            throw new FusionException("Unknown State in RewardDispatcher.ProgramState");
         }
      }

      public RewardProgramData next() throws FusionException {
         if (RewardDispatcher.ProgramsStateHelper.STATE.HOLD == this.state) {
            this.curProgram = null;
            this.state = RewardDispatcher.ProgramsStateHelper.STATE.ITERATING;
            if (!this.iterPrograms.hasNext()) {
               this.iterPrograms = null;
               this.state = RewardDispatcher.ProgramsStateHelper.STATE.OPEN;
               return null;
            }
         }

         return this.currentOrNext();
      }

      public static enum STATE {
         CLOSE,
         OPEN,
         ITERATING,
         HOLD;
      }
   }

   private static class DefaultRewardDispatcherFactory implements RewardDispatcherFactory {
      private DefaultRewardDispatcherFactory() {
      }

      public RewardDispatcher getNewInstance(RewardProgramData program) {
         return new RewardDispatcher(program);
      }

      // $FF: synthetic method
      DefaultRewardDispatcherFactory(Object x0) {
         this();
      }
   }
}
