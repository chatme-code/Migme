package com.projectgoth.fusion.sessioncache;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.dao.SessionSummaryDAO;
import com.projectgoth.fusion.dao.UserDAO;
import com.projectgoth.fusion.domain.CountryLogins;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class PersistUniqueSummariesTask extends TimerTask {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PersistUniqueSummariesTask.class));
   private SessionCacheI sessionCacheI;
   private SessionSummaryDAO sessionSummaryDAO;
   private UserDAO misUserDAO;
   private List<Integer> periods;
   private TransactionTemplate transactionTemplate;
   private boolean startupRun = false;

   public PersistUniqueSummariesTask(SessionCacheI sessionCacheI, SessionSummaryDAO sessionSummaryDAO, UserDAO userDAO, TransactionTemplate transactionTemplate, List<Integer> periods) {
      this.sessionCacheI = sessionCacheI;
      this.sessionSummaryDAO = sessionSummaryDAO;
      this.misUserDAO = userDAO;
      this.periods = periods;
      this.transactionTemplate = transactionTemplate;
   }

   public PersistUniqueSummariesTask(SessionCacheI sessionCacheI, SessionSummaryDAO sessionSummaryDAO, UserDAO userDAO, TransactionTemplate transactionTemplate, List<Integer> periods, boolean startupRun) {
      this.sessionCacheI = sessionCacheI;
      this.sessionSummaryDAO = sessionSummaryDAO;
      this.misUserDAO = userDAO;
      this.periods = periods;
      this.transactionTemplate = transactionTemplate;
      this.startupRun = startupRun;
   }

   private void persistSummaries() throws Exception {
      final Date midnightYesterday = DateTimeUtils.midnightYesterday();
      final Date now = new Date();
      final Date startDateSource;
      if (this.startupRun) {
         startDateSource = DateTimeUtils.midnightToday();
      } else {
         startDateSource = new Date();
      }

      if (this.startupRun && this.sessionSummaryDAO.yesterdaysTotalsExists(midnightYesterday)) {
         log.info("No need to run PersistUniqueSummariesTask on startup since we've already ran for yesterday, returning...");
      } else {
         final Map<Integer, List<CountryLogins>> authenticatedLogins = new HashMap();
         final Map<Integer, List<CountryLogins>> nonAuthenticatedLogins = new HashMap();
         log.info("Examing " + this.periods.size() + " periods");
         Iterator i$ = this.periods.iterator();

         while(i$.hasNext()) {
            Integer period = (Integer)i$.next();
            List<CountryLogins> list = this.misUserDAO.findRecentLoginsGroupedByCountry(startDateSource, now, period);
            log.info("Got " + (list == null ? 0 : list.size()) + " country login records for period " + period);
            authenticatedLogins.put(period, new ArrayList());
            nonAuthenticatedLogins.put(period, new ArrayList());
            Iterator i$ = list.iterator();

            while(i$.hasNext()) {
               CountryLogins countryLogin = (CountryLogins)i$.next();
               if (countryLogin.getMobileVerified()) {
                  ((List)authenticatedLogins.get(period)).add(countryLogin);
               } else {
                  ((List)nonAuthenticatedLogins.get(period)).add(countryLogin);
               }
            }
         }

         this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
               try {
                  if (!PersistUniqueSummariesTask.this.sessionSummaryDAO.rowsExist(midnightYesterday)) {
                     if (PersistUniqueSummariesTask.log.isDebugEnabled()) {
                        PersistUniqueSummariesTask.log.debug("creating new daily rows for [" + midnightYesterday + "]");
                     }

                     PersistUniqueSummariesTask.this.sessionSummaryDAO.createDailyRows(midnightYesterday);
                  }

                  Iterator i$ = PersistUniqueSummariesTask.this.periods.iterator();

                  while(i$.hasNext()) {
                     Integer period = (Integer)i$.next();
                     if (PersistUniqueSummariesTask.log.isDebugEnabled()) {
                        PersistUniqueSummariesTask.log.debug("calculating unique totals from [" + DateTimeUtils.minusDays(startDateSource, period) + "] until [" + now + "] " + "for period date starting [" + midnightYesterday + "] and period [" + period + "]");
                     }

                     PersistUniqueSummariesTask.this.sessionSummaryDAO.updateUniqueTotals(midnightYesterday, period, true, (List)authenticatedLogins.get(period));
                     PersistUniqueSummariesTask.this.sessionSummaryDAO.updateUniqueTotals(midnightYesterday, period, false, (List)nonAuthenticatedLogins.get(period));
                  }
               } catch (Exception var4) {
                  PersistUniqueSummariesTask.log.error("Unable to persist unique totals from SOURCE [" + startDateSource + "] to [" + now + "] for period [" + midnightYesterday + "]", var4);
                  status.setRollbackOnly();
               }

            }
         });
      }
   }

   public void run() {
      try {
         this.sessionCacheI.setUniqueSummariesTaskRunning(true);
         log.info("persisting summary uniques, startupRun [" + this.startupRun + "]");
         long now = System.currentTimeMillis();
         this.persistSummaries();
         log.info("done persisting summary uniques, it took " + (System.currentTimeMillis() - now) / 60000L + " minutes");
         this.sessionCacheI.setUniqueSummariesTaskRunning(false);
      } catch (Exception var3) {
         log.error("Unable to persist unique summary totals", var3);
      }

   }
}
