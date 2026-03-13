package com.projectgoth.fusion.sessioncache;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.dao.SessionArchiveDAO;
import com.projectgoth.fusion.data.UserReputationScoreAndLevelData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.ReputationServicePrx;
import com.projectgoth.fusion.slice.ScoreAndLevel;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import org.apache.log4j.Logger;

public class ArchiveSessionThread extends Thread {
   private Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ArchiveSessionThread.class));
   private static final int MAX_BATCH_RETRY_COUNT = 10;
   private BlockingQueue<SessionArchiveDetail> sessions;
   private SessionArchiveDAO sessionArchiveDAO;
   private int batchSize = 20;
   private final int maxWaitTimeInSeconds;
   private boolean running = true;
   private ReputationServicePrx reputationServicePrx;
   private AtomicInteger abortedBatches;
   private int maxQueueSize = 0;

   public ArchiveSessionThread(SessionArchiveDAO sessionArchiveDAO, BlockingQueue<SessionArchiveDetail> sessions, int batchSize, int maxWaitTimeInSeconds, ReputationServicePrx reputationServicePrx, AtomicInteger abortedBatches, int maxQueueSize) {
      this.sessions = sessions;
      this.sessionArchiveDAO = sessionArchiveDAO;
      this.batchSize = batchSize;
      this.maxWaitTimeInSeconds = maxWaitTimeInSeconds;
      this.reputationServicePrx = reputationServicePrx;
      this.abortedBatches = abortedBatches;
      this.maxQueueSize = maxQueueSize;
   }

   public void setRunning(boolean running) {
      this.running = running;
   }

   void populateMigLevelForList(List<SessionArchiveDetail> list) {
      Iterator i$;
      SessionArchiveDetail listDetail;
      try {
         SessionArchiveDetail listDetail;
         Iterator i$;
         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SessionCacheSettings.USE_REPUTATION_SERVICE_FOR_SCORE_RETRIEVAL)) {
            try {
               Set<Integer> userIDSet = new TreeSet();
               i$ = list.iterator();

               while(i$.hasNext()) {
                  listDetail = (SessionArchiveDetail)i$.next();
                  userIDSet.add(listDetail.getUserID());
               }

               User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
               Map<Integer, UserReputationScoreAndLevelData> map = userBean.getReputationScoreAndLevelForUsers(new LinkedList(userIDSet), (Connection)null);
               i$ = list.iterator();

               while(i$.hasNext()) {
                  listDetail = (SessionArchiveDetail)i$.next();
                  UserReputationScoreAndLevelData scoreData = (UserReputationScoreAndLevelData)map.get(listDetail.getUserID());
                  listDetail.setMigLevel(scoreData == null ? 1 : scoreData.level);
                  listDetail.setMigLevelScore(scoreData == null ? 0 : scoreData.score);
               }
            } catch (CreateException var8) {
               throw new FusionException("Unable to create userBean for invocation of getReputationScoreAndLevelForUsers(): " + var8.getMessage());
            } catch (RemoteException var9) {
               throw new FusionException("Unable to invoke getReputationScoreAndLevelForUsers(): " + var9.getMessage());
            } catch (EJBException var10) {
               throw new FusionException("EJBException caught while invoking getReputationScoreAndLevelForUsers(): " + var10.getMessage());
            }
         } else {
            int[] userIDs = new int[list.size()];
            int index = 0;

            SessionArchiveDetail listDetail;
            for(Iterator i$ = list.iterator(); i$.hasNext(); userIDs[index++] = listDetail.getUserID()) {
               listDetail = (SessionArchiveDetail)i$.next();
            }

            if (this.log.isDebugEnabled()) {
               this.log.debug("sending " + userIDs.length + " user ids to reputation service");
            }

            ScoreAndLevel[] scoresAndLevels = this.reputationServicePrx.getUserScoreAndLevels(userIDs);
            if (this.log.isDebugEnabled()) {
               if (scoresAndLevels != null) {
                  this.log.debug("found " + scoresAndLevels.length + " ScoresAndLevel from reputationServicePrx");
               } else {
                  this.log.debug("received null ScoresAndLevel from reputationServicePrx");
               }
            }

            if (scoresAndLevels == null || scoresAndLevels.length != list.size()) {
               throw new FusionException("Unable to retrieve reputation score and levels from reputationService");
            }

            index = 0;

            for(i$ = list.iterator(); i$.hasNext(); ++index) {
               listDetail = (SessionArchiveDetail)i$.next();
               listDetail.setMigLevel(scoresAndLevels[index].level);
               listDetail.setMigLevelScore(scoresAndLevels[index].score);
            }
         }
      } catch (FusionException var11) {
         this.log.error("failed to find mig levels for batch, setting all to level 1 and score 0", var11);
         i$ = list.iterator();

         while(i$.hasNext()) {
            listDetail = (SessionArchiveDetail)i$.next();
            listDetail.setMigLevel(1);
            listDetail.setMigLevelScore(0);
         }
      }

   }

   private boolean addDetailToList(List<SessionArchiveDetail> list, SessionArchiveDetail detail) {
      List<String> errors = null;
      String truncatedValue;
      if (detail.getRemoteAddress() != null && detail.getRemoteAddress().length() > 45) {
         if (errors == null) {
            errors = new ArrayList();
         }

         truncatedValue = detail.getRemoteAddress().substring(0, 45);
         errors.add(String.format("remoteAddress longer than limit %d, value '%s' truncated to '%s'", 45, detail.getRemoteAddress(), truncatedValue));
         detail.setRemoteAddress(truncatedValue);
      } else if (detail.getMobileDevice() != null && detail.getMobileDevice().length() > 500) {
         if (errors == null) {
            errors = new ArrayList();
         }

         truncatedValue = detail.getMobileDevice().substring(0, 500);
         errors.add(String.format("mobileDevice longer than limit %d, value '%s' truncated to '%s'", 500, detail.getMobileDevice(), truncatedValue));
         detail.setMobileDevice(truncatedValue);
      }

      detail.setLanguage(detail.getLanguage().replaceAll("[\n\r|]", " "));
      detail.setMobileDevice(detail.getMobileDevice().replaceAll("[\n\r|]", " "));
      detail.setRemoteAddress(detail.getRemoteAddress().replaceAll("[\n\r|]", " "));
      list.add(detail);
      if (errors != null) {
         this.log.error(String.format("invalid session archive detail truncated before logging to sessionarchive, %d error, %s, truncated detail=%s", errors.size(), StringUtil.join((Collection)errors, ", "), detail));
         return false;
      } else {
         return true;
      }
   }

   public void run() {
      List<SessionArchiveDetail> list = new ArrayList(this.batchSize);
      SessionArchiveDetail detail = null;

      while(this.running) {
         try {
            boolean retry = false;

            while(list.size() < this.batchSize && (detail = (SessionArchiveDetail)this.sessions.poll()) != null) {
               this.addDetailToList(list, detail);
            }

            long localMaxWaitTime = (long)(this.maxWaitTimeInSeconds * 1000);

            while(list.size() < this.batchSize && localMaxWaitTime > 0L) {
               long startTime = System.currentTimeMillis();
               detail = (SessionArchiveDetail)this.sessions.poll(localMaxWaitTime, TimeUnit.MILLISECONDS);
               localMaxWaitTime -= System.currentTimeMillis() - startTime;
               if (detail != null) {
                  this.addDetailToList(list, detail);
               }
            }

            if (!list.isEmpty()) {
               if (this.log.isDebugEnabled()) {
                  this.log.debug("archive batch of size " + list.size());
               }

               this.populateMigLevelForList(list);
               int retryCount = 1;
               int waitCount = 1;

               do {
                  try {
                     this.sessionArchiveDAO.bulkArchiveSession(list);
                     list.clear();
                     retry = false;
                  } catch (Throwable var9) {
                     this.log.error(String.format("failed to persist session archive batch, we have a resource problem, retrying (%d) this batch until it succeeds", retryCount), var9);
                     ++retryCount;
                     sleep((long)(500 * waitCount));
                     retry = true;
                     if ((double)this.sessions.remainingCapacity() > 0.75D * (double)this.maxQueueSize) {
                        ++waitCount;
                     } else if ((double)this.sessions.remainingCapacity() < 0.25D * (double)this.maxQueueSize) {
                        waitCount = 1;
                     }
                  }
               } while(retry && retryCount < 10);

               if (retryCount >= 10) {
                  this.log.error("failed to persist batch after 10 tries, aborting batch of size " + list.size());

                  for(int i = 0; i < list.size(); ++i) {
                     this.log.error(String.format(" aborted entry %02d: %s", i, list.get(i)));
                  }

                  list.clear();
                  this.abortedBatches.incrementAndGet();
               }
            }
         } catch (Throwable var10) {
            this.log.error("failed to archive sessions", var10);
         }
      }

      this.log.warn("Exiting run() method...");
   }
}
