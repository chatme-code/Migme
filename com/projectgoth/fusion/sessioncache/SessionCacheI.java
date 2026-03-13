package com.projectgoth.fusion.sessioncache;

import Ice.Current;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.RequestCounter;
import com.projectgoth.fusion.dao.SessionArchiveDAO;
import com.projectgoth.fusion.dao.SessionSummaryDAO;
import com.projectgoth.fusion.dao.UserDAO;
import com.projectgoth.fusion.dao.impl.DailyVariableTableName;
import com.projectgoth.fusion.slice.ReputationServicePrx;
import com.projectgoth.fusion.slice.SessionIce;
import com.projectgoth.fusion.slice.SessionMetricsIce;
import com.projectgoth.fusion.slice._SessionCacheDisp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class SessionCacheI extends _SessionCacheDisp implements InitializingBean {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SessionCacheI.class));
   private Map<Integer, TotalCountrySessions> totalSessions = new HashMap();
   private BlockingQueue<SessionArchiveDetail> sessionQueue;
   private ArchiveSessionThread archiveSessionThread;
   private RequestCounter receivedSessionsCounter;
   private AtomicInteger abortedBatches = new AtomicInteger(0);
   private SessionArchiveDAO sessionArchiveDAO;
   private SessionSummaryDAO sessionSummaryDAO;
   private UserDAO userDAO;
   private int requestCounterInterval;
   private int archiveBatchSize = 20;
   private int archiveMaxWaitTimeInSeconds = 5;
   private List<Integer> periods;
   private int queueSize;
   private final TransactionTemplate transactionTemplate;
   private final DailyVariableTableName archiveTableName;
   private boolean uniqueSummariesTaskRunning = false;
   private boolean ssoSessionArchiverTaskRunning = false;
   private ReputationServicePrx reputationServicePrx;

   public SessionCacheI(PlatformTransactionManager transactionManager, DailyVariableTableName archiveTableName) {
      this.transactionTemplate = new TransactionTemplate(transactionManager);
      this.archiveTableName = archiveTableName;
      log.debug(this.getClass() + " INSTANTIATED!!");
   }

   public void afterPropertiesSet() throws Exception {
      this.archiveTableName.updateTableName();
      this.sessionQueue = new LinkedBlockingQueue(this.queueSize);
      this.receivedSessionsCounter = new RequestCounter((long)this.requestCounterInterval);
      Timer shortTaskTimer = new Timer(true);
      shortTaskTimer.schedule(new DailyTableTask(this.archiveTableName), DateTimeUtils.midnightTomorrow(), 86400000L);
      Date firstRunTime = DateTimeUtils.midnightTomorrow();
      Timer timer = new Timer(true);
      timer.schedule(new PersistUniqueSummariesTask(this, this.sessionSummaryDAO, this.userDAO, this.transactionTemplate, this.periods, true), DateTimeUtils.minutesFromNow(1));
      timer.schedule(new PersistUniqueSummariesTask(this, this.sessionSummaryDAO, this.userDAO, this.transactionTemplate, this.periods), firstRunTime, 86400000L);
      timer.schedule(new PersistTotalSummariesTask(this.totalSessions, this.sessionSummaryDAO, this.transactionTemplate), DateTimeUtils.minutesFromNow(5), 300000L);
      int batchSize = 10000;
      int frequencyInMinutes = 1;
      timer.schedule(new ArchiveSSOSessionsTask(this, this.sessionArchiveDAO, this.userDAO, batchSize, this.transactionTemplate), DateTimeUtils.minutesFromNow(frequencyInMinutes), (long)('\uea60' * frequencyInMinutes));
      log.info(DailyTableTask.class + " scheduled for [" + DateTimeUtils.midnightTomorrow() + "]");
      log.info(PersistUniqueSummariesTask.class + " scheduled for [" + firstRunTime + "]");
      log.info(PersistTotalSummariesTask.class + " scheduled for [" + DateTimeUtils.minutesFromNow(5) + "]");
      log.info(ArchiveSSOSessionsTask.class + " scheduled for [" + DateTimeUtils.minutesFromNow(frequencyInMinutes) + "]");
   }

   public void createArchiveThread() {
      this.archiveSessionThread = new ArchiveSessionThread(this.sessionArchiveDAO, this.sessionQueue, this.archiveBatchSize, this.archiveMaxWaitTimeInSeconds, this.reputationServicePrx, this.abortedBatches, this.queueSize);
      this.archiveSessionThread.start();
   }

   public void logSession(SessionIce session, SessionMetricsIce sessionMetrics, Current __current) {
      try {
         if (log.isDebugEnabled()) {
            log.debug("received session for user [" + session.username + "]");
         }

         TotalCountrySessions sessions = null;
         synchronized(this.totalSessions) {
            if (!this.totalSessions.containsKey(session.sourceCountryID)) {
               TotalCountrySessions var10000 = (TotalCountrySessions)this.totalSessions.put(session.sourceCountryID, new TotalCountrySessions());
            }

            sessions = (TotalCountrySessions)this.totalSessions.get(session.sourceCountryID);
         }

         if (session.authenticated) {
            sessions.getTotalAuthenticatedSessions().incrementAndGet();
         } else {
            sessions.getTotalNonAuthenticatedSessions().incrementAndGet();
         }

         SessionArchiveDetail detail = new SessionArchiveDetail(session, sessionMetrics);
         if (detail.getDeviceType() == null) {
            log.error(String.format("unrecognized deviceType value %d, set to null and storing it into sessionarchive, detail=%s", session.deviceType, detail));
         }

         if (detail.getConnectionType() == null) {
            log.error(String.format("unrecognized connectionType value %d, set to 1(TCP) and storing it into sessionarchive, detail=%s", session.connectionType, detail));
         }

         if (!this.sessionQueue.offer(detail)) {
            log.warn("unable to log session, the queue is full!");
         }

         this.receivedSessionsCounter.add();
      } catch (Exception var8) {
         log.error("Exception caught :" + var8.getMessage(), var8);
      }

   }

   public void terminating() {
      log.info("Termininating");
      this.archiveSessionThread.setRunning(false);
   }

   @Required
   public void setRequestCounterInterval(int requestCounterInterval) {
      this.requestCounterInterval = requestCounterInterval;
   }

   @Required
   public void setSessionArchiveDAO(SessionArchiveDAO sessionArchiveDAO) {
      this.sessionArchiveDAO = sessionArchiveDAO;
   }

   @Required
   public void setSessionSummaryDAO(SessionSummaryDAO sessionSummaryDAO) {
      this.sessionSummaryDAO = sessionSummaryDAO;
   }

   @Required
   public void setUserDAO(UserDAO userDAO) {
      this.userDAO = userDAO;
   }

   @Required
   public void setPeriods(List<Integer> periods) {
      this.periods = periods;
   }

   @Required
   public void setQueueSize(int queueSize) {
      this.queueSize = queueSize;
   }

   public void setArchiveBatchSize(int archiveBatchSize) {
      this.archiveBatchSize = archiveBatchSize;
   }

   public void setArchiveMaxWaitTimeInSeconds(int archiveMaxWaitTimeInSeconds) {
      this.archiveMaxWaitTimeInSeconds = archiveMaxWaitTimeInSeconds;
   }

   public RequestCounter getReceivedSessionsCounter() {
      return this.receivedSessionsCounter;
   }

   public int getSessionsWaitingToBeArchived() {
      return this.sessionQueue.size();
   }

   public boolean isUniqueSummariesTaskRunning() {
      return this.uniqueSummariesTaskRunning;
   }

   public void setUniqueSummariesTaskRunning(boolean uniqueSummariesTaskIsRunning) {
      this.uniqueSummariesTaskRunning = uniqueSummariesTaskIsRunning;
   }

   public boolean isSsoSessionArchiverTaskRunning() {
      return this.ssoSessionArchiverTaskRunning;
   }

   public void setSsoSessionArchiverTaskRunning(boolean ssoSessionArchiverTaskRunning) {
      this.ssoSessionArchiverTaskRunning = ssoSessionArchiverTaskRunning;
   }

   public void setReputationServicePrx(ReputationServicePrx reputationServicePrx) {
      this.reputationServicePrx = reputationServicePrx;
   }

   public AtomicInteger getAbortedBatches() {
      return this.abortedBatches;
   }
}
