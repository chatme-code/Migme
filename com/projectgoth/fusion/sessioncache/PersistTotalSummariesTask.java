package com.projectgoth.fusion.sessioncache;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.dao.SessionSummaryDAO;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class PersistTotalSummariesTask extends TimerTask {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PersistTotalSummariesTask.class));
   private Map<Integer, TotalCountrySessions> totalSessions;
   private SessionSummaryDAO sessionSummaryDAO;
   private TransactionTemplate transactionTemplate;

   public PersistTotalSummariesTask(Map<Integer, TotalCountrySessions> totalSessions, SessionSummaryDAO sessionSummaryDAO, TransactionTemplate transactionTemplate) {
      this.totalSessions = totalSessions;
      this.sessionSummaryDAO = sessionSummaryDAO;
      this.transactionTemplate = transactionTemplate;
   }

   private void persistSummaries() throws Exception {
      final Date midnightToday = DateTimeUtils.midnightToday();
      this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
         private final Map<Integer, TotalCountrySessions> tempSessions;
         private final Map<Integer, TotalCountrySessions> completedSessions;
         private Integer country;
         private int totalAuthenticated;
         private int totalNonAuthenticated;

         {
            this.tempSessions = new HashMap(PersistTotalSummariesTask.this.totalSessions.size() + 1);
            this.completedSessions = new HashMap(PersistTotalSummariesTask.this.totalSessions.size() + 1);
            this.country = null;
            this.totalAuthenticated = 0;
            this.totalNonAuthenticated = 0;
         }

         private void persistData(TransactionStatus status) {
            try {
               Iterator i = this.tempSessions.keySet().iterator();

               while(true) {
                  do {
                     if (!i.hasNext()) {
                        return;
                     }

                     this.country = (Integer)i.next();
                     TotalCountrySessions previousTotalSessions = (TotalCountrySessions)this.tempSessions.get(this.country);
                     this.totalAuthenticated = previousTotalSessions.getTotalAuthenticatedSessions().intValue();
                     this.totalNonAuthenticated = previousTotalSessions.getTotalNonAuthenticatedSessions().intValue();
                  } while(this.totalAuthenticated <= 0 && this.totalNonAuthenticated <= 0);

                  PersistTotalSummariesTask.this.sessionSummaryDAO.updateTotalSession(midnightToday, this.country, this.totalAuthenticated, this.totalNonAuthenticated);
                  this.completedSessions.put(this.country, new TotalCountrySessions(this.totalAuthenticated, this.totalNonAuthenticated));
               }
            } catch (Exception var7) {
               PersistTotalSummariesTask.log.error("Unable to persist total sessions for today [" + midnightToday + "], rolling back", var7);
               status.setRollbackOnly();
               if (!this.completedSessions.isEmpty()) {
                  synchronized(PersistTotalSummariesTask.this.totalSessions) {
                     Iterator ix = this.completedSessions.keySet().iterator();

                     while(ix.hasNext()) {
                        this.country = (Integer)ix.next();
                        ((TotalCountrySessions)PersistTotalSummariesTask.this.totalSessions.get(this.country)).getTotalAuthenticatedSessions().addAndGet(((TotalCountrySessions)this.completedSessions.get(this.country)).getTotalAuthenticatedSessions().intValue());
                        ((TotalCountrySessions)PersistTotalSummariesTask.this.totalSessions.get(this.country)).getTotalNonAuthenticatedSessions().addAndGet(((TotalCountrySessions)this.completedSessions.get(this.country)).getTotalNonAuthenticatedSessions().intValue());
                     }
                  }
               }
            }

         }

         protected void doInTransactionWithoutResult(TransactionStatus status) {
            if (PersistTotalSummariesTask.log.isDebugEnabled()) {
               PersistTotalSummariesTask.log.debug("we have totals data for " + PersistTotalSummariesTask.this.totalSessions.keySet().size() + " countries");
            }

            if (!PersistTotalSummariesTask.this.sessionSummaryDAO.rowsExist(midnightToday)) {
               if (PersistTotalSummariesTask.log.isDebugEnabled()) {
                  PersistTotalSummariesTask.log.debug("creating new daily rows for [" + midnightToday + "]");
               }

               PersistTotalSummariesTask.this.sessionSummaryDAO.createDailyRows(midnightToday);
            }

            synchronized(PersistTotalSummariesTask.this.totalSessions) {
               Iterator i = PersistTotalSummariesTask.this.totalSessions.keySet().iterator();

               while(true) {
                  if (!i.hasNext()) {
                     break;
                  }

                  this.country = (Integer)i.next();
                  TotalCountrySessions previousTotalSessions = (TotalCountrySessions)PersistTotalSummariesTask.this.totalSessions.get(this.country);
                  this.totalAuthenticated = previousTotalSessions.getTotalAuthenticatedSessions().getAndSet(0);
                  this.totalNonAuthenticated = previousTotalSessions.getTotalNonAuthenticatedSessions().getAndSet(0);
                  this.tempSessions.put(this.country, new TotalCountrySessions(this.totalAuthenticated, this.totalNonAuthenticated));
               }
            }

            this.persistData(status);
         }
      });
   }

   public void run() {
      try {
         log.info("persisting totals summary");
         this.persistSummaries();
      } catch (Exception var2) {
         log.error("Unable to persist totals summary", var2);
      }

   }
}
