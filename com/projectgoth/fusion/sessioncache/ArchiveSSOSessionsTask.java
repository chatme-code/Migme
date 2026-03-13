package com.projectgoth.fusion.sessioncache;

import com.projectgoth.fusion.clientsession.SSOLogin;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.dao.SessionArchiveDAO;
import com.projectgoth.fusion.dao.UserDAO;
import com.projectgoth.fusion.restapi.data.SSOSessionMetrics;
import com.projectgoth.fusion.restapi.util.RedisDataUtil;
import com.projectgoth.fusion.slice.FusionException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.PipelineBlock;

public class ArchiveSSOSessionsTask extends TimerTask {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ArchiveSSOSessionsTask.class));
   private SessionCacheI sessionCacheI;
   private SessionArchiveDAO sessionArchiveDAO;
   private UserDAO userDAO;
   private int batchSize;
   private TransactionTemplate transactionTemplate;
   public static final String SSO_ARCHIVES_TABLE_NAME = "ssosessionarchives";

   public ArchiveSSOSessionsTask(SessionCacheI sessionCacheI, SessionArchiveDAO sessionArchiveDAO, UserDAO userDAO, int batchSize, TransactionTemplate transactionTemplate) {
      this.sessionCacheI = sessionCacheI;
      this.sessionArchiveDAO = sessionArchiveDAO;
      this.userDAO = userDAO;
      this.batchSize = batchSize;
      this.transactionTemplate = transactionTemplate;
   }

   public static String getDailyTableName() {
      Date date = new Date();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
      return "ssosessionarchives" + sdf.format(date);
   }

   public static Date addDays(Date date, int days) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      cal.add(5, days);
      return cal.getTime();
   }

   public static String getNextDayDailyTableName() {
      Date date = new Date();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
      return "ssosessionarchives" + sdf.format(addDays(date, 1));
   }

   private void createTableIfNotExists() {
      this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
         protected void doInTransactionWithoutResult(TransactionStatus status) {
            ArchiveSSOSessionsTask.this.sessionArchiveDAO.createSSOSessionMetricsTableIfNotExists();
         }
      });
   }

   private int archiveSessions() {
      int numSessionsArchived = 0;
      Jedis slaveInst = null;

      byte var5;
      try {
         slaveInst = Redis.getGamesSlaveInstance();
         Long resultLong = slaveInst.zcard("SSOM:INDEX");
         if (resultLong == null) {
            throw new FusionException("archiveSessions-zcard returns null");
         }

         int totalSessions = resultLong.intValue();
         log.info(String.format("Found %d entries in SSO sessions index [%s]", totalSessions, "SSOM:INDEX"));
         if (totalSessions != 0) {
            int numLiveSessions = 0;
            int numExpiredSessions = 0;
            int numPending = totalSessions;

            for(int index = 0; index < totalSessions; index += this.batchSize) {
               Set<String> ssoMetricKeys = slaveInst.zrange("SSOM:INDEX", (long)index, (long)(index + this.batchSize));
               LinkedList<String> sidOfScannedSessions = new LinkedList();
               Iterator it = ssoMetricKeys.iterator();

               while(it.hasNext()) {
                  sidOfScannedSessions.add(RedisDataUtil.getSessionIDFromSSOSessionMetricKey((String)it.next()));
               }

               Map<String, String> memcacheData = SSOLogin.getMultipleLoginDataStrFromMemcache((String[])sidOfScannedSessions.toArray(new String[ssoMetricKeys.size()]));
               String[] memcacheDataArr = (String[])memcacheData.keySet().toArray(new String[sidOfScannedSessions.size()]);

               for(int i = 0; i < memcacheDataArr.length; ++i) {
                  if (memcacheData.get(memcacheDataArr[i]) != null) {
                     sidOfScannedSessions.remove(memcacheDataArr[i]);
                  }
               }

               numLiveSessions += memcacheDataArr.length - sidOfScannedSessions.size();
               numExpiredSessions += sidOfScannedSessions.size();
               numPending -= ssoMetricKeys.size();
               log.info(String.format("SSO Sessions Breakdown: Total [%d], Live [%d], Expired [%d], Pending Scan [%d]", totalSessions, numLiveSessions, numExpiredSessions, numPending));
               final LinkedList<String> ssoMetricKeysForExpiredSessions = new LinkedList();
               it = ssoMetricKeys.iterator();

               while(it.hasNext()) {
                  String key = (String)it.next();
                  if (sidOfScannedSessions.contains(RedisDataUtil.getSessionIDFromSSOSessionMetricKey(key))) {
                     ssoMetricKeysForExpiredSessions.add(key);
                  }
               }

               final Iterator<String> iter = ssoMetricKeysForExpiredSessions.iterator();
               List<Object> pipelineResult = slaveInst.pipelined(new PipelineBlock() {
                  public void execute() {
                     while(iter.hasNext()) {
                        String redisKey = (String)iter.next();
                        this.hgetAll(redisKey);
                     }

                  }
               });
               final LinkedList<SSOSessionMetrics> metricsToArchive = new LinkedList();

               for(int i = 0; i < ssoMetricKeysForExpiredSessions.size(); ++i) {
                  String redisKey = (String)ssoMetricKeysForExpiredSessions.get(i);

                  try {
                     Map<String, String> map = (Map)pipelineResult.get(i);
                     Long sessionStartTime = Long.parseLong((String)map.get("sessionStartTime"));
                     SSOSessionMetrics metrics = SSOSessionMetrics.fromMap(RedisDataUtil.getSessionIDFromSSOSessionMetricKey(redisKey), RedisDataUtil.getViewFromSSOSessionMetricKey(redisKey), sessionStartTime, map);
                     metricsToArchive.add(metrics);
                  } catch (Exception var27) {
                     log.warn(String.format("Unable to archive session [%s]", redisKey), var27);
                  }
               }

               this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                  protected void doInTransactionWithoutResult(TransactionStatus status) {
                     Jedis masterInst = null;

                     try {
                        ArchiveSSOSessionsTask.this.sessionArchiveDAO.bulkArchiveSSOSessionMetrics(metricsToArchive);
                        masterInst = Redis.getGamesMasterInstance();
                        final Iterator<String> iterx = ssoMetricKeysForExpiredSessions.iterator();
                        masterInst.pipelined(new PipelineBlock() {
                           public void execute() {
                              while(iterx.hasNext()) {
                                 String redisKey = (String)iterx.next();
                                 this.del(redisKey);
                                 this.zrem("SSOM:INDEX", new String[]{redisKey});
                                 ArchiveSSOSessionsTask.log.debug(String.format("Session Expired [%s]", redisKey));
                              }

                           }
                        });
                        Redis.disconnect(masterInst, ArchiveSSOSessionsTask.log);
                     } catch (Exception var7) {
                        ArchiveSSOSessionsTask.log.error("Unable to persist SSO session metrics", var7);
                        status.setRollbackOnly();
                     } finally {
                        Redis.disconnect(masterInst, ArchiveSSOSessionsTask.log);
                     }

                  }
               });
               numSessionsArchived += numExpiredSessions;
            }

            return numSessionsArchived;
         }

         log.info("No session entries found in SSO sessions index");
         var5 = 0;
      } catch (Exception var28) {
         log.error(String.format("Unable to archive SSO session %s", var28.getMessage()), var28);
         return numSessionsArchived;
      } finally {
         Redis.disconnect(slaveInst, log);
      }

      return var5;
   }

   public void run() {
      try {
         if (this.sessionCacheI.isSsoSessionArchiverTaskRunning()) {
            log.warn("Another SSOSessionArchiverTask is already running.");
            return;
         }

         this.sessionCacheI.setSsoSessionArchiverTaskRunning(true);
         this.createTableIfNotExists();
         log.info("Archiving SSO sessions metrics...");
         long now = System.currentTimeMillis();
         int numSessionsArchived = this.archiveSessions();
         log.info(String.format("Done archiving [%d] SSO sessions in [%.2f] seconds", numSessionsArchived, (double)(System.currentTimeMillis() - now) / 1000.0D));
      } catch (Exception var7) {
         log.error("Unable to persist unique summary totals", var7);
      } finally {
         this.sessionCacheI.setSsoSessionArchiverTaskRunning(false);
      }

   }
}
