package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.MerchantTagData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.Semaphore;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

public class MerchantTag implements Job {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MerchantTag.class));
   private static final Semaphore semaphore = new Semaphore(1);

   public void execute(JobExecutionContext arg0) throws JobExecutionException {
      if (!semaphore.tryAcquire()) {
         log.warn("Another job is processing merchant tags. Exiting...");
      } else {
         Jedis jedisConn = null;
         String accountEntryId = null;

         try {
            while(SystemProperty.getBool("MerchantTaggingEnabled", true)) {
               try {
                  if (jedisConn == null) {
                     jedisConn = Redis.getQueuesMasterInstance();
                  }

                  if (!jedisConn.isConnected()) {
                     jedisConn.connect();
                  }

                  Set<String> idToProcess = jedisConn.zrange(Redis.KeySpaceQueuesInstance.TOP_MERCHANT_TAG.toString(), 0L, 0L);
                  if (idToProcess != null && !idToProcess.isEmpty()) {
                     log.info("Retrieved set: " + idToProcess);
                     accountEntryId = (String)idToProcess.iterator().next();
                     Account accountBean = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
                     User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
                     AccountEntryData fromUserEntry = null;

                     try {
                        fromUserEntry = accountBean.getAccountEntryFromSlave(Long.valueOf(accountEntryId));
                     } catch (NumberFormatException var31) {
                        log.error("Failed to parse account entry id: [" + accountEntryId + "]" + var31.getMessage());
                        jedisConn.zrem(Redis.KeySpaceQueuesInstance.TOP_MERCHANT_TAG.toString(), new String[]{accountEntryId});
                        continue;
                     }

                     if (fromUserEntry != null) {
                        AccountEntryData toUserEntry = null;

                        try {
                           toUserEntry = accountBean.getAccountEntryFromSlave(Long.valueOf(fromUserEntry.reference));
                        } catch (NumberFormatException var30) {
                           log.error("Failed to parse account entry id: [" + accountEntryId + "]" + var30.getMessage());
                           jedisConn.zrem(Redis.KeySpaceQueuesInstance.TOP_MERCHANT_TAG.toString(), new String[]{accountEntryId});
                           continue;
                        }

                        if (toUserEntry != null) {
                           MerchantTagData tagData = accountBean.getMerchantTagFromUsername((Connection)null, toUserEntry.username, false);
                           int fromUserId = userBean.getUserID(fromUserEntry.username, (Connection)null);
                           int toUserId = userBean.getUserID(toUserEntry.username, (Connection)null);
                           double nettTransfer = 0.0D;
                           if (tagData == null) {
                              nettTransfer = accountBean.getNETTTransfer((Connection)null, fromUserEntry.username, toUserEntry.username, fromUserEntry.dateCreated, SystemProperty.getInt((String)"MerchantMerchantTagInterval", 43200));
                           } else if (tagData != null && fromUserId == tagData.merchantUserID) {
                              Calendar dt = Calendar.getInstance();
                              int cutoff = (int)((dt.getTimeInMillis() - tagData.getLastSalesDateInMiliSeconds()) / 1000L) / 60;
                              nettTransfer = accountBean.getNETTTransfer((Connection)null, fromUserEntry.username, toUserEntry.username, fromUserEntry.dateCreated, cutoff);
                           }

                           double minRequirementUSD = SystemProperty.getDouble("MinMerchantMerchantTagAmountUSD", 100.0D);
                           double minRequirement = accountBean.convertCurrency(minRequirementUSD, "USD", CurrencyData.baseCurrency);
                           log.info("NETT transfer for merchant [" + fromUserEntry.username + "] from [" + fromUserEntry.dateCreated + "]: " + nettTransfer + " to:" + toUserEntry.username);
                           if (nettTransfer >= minRequirement) {
                              log.info("Processing merchant tag: (" + toUserId + " --> " + fromUserId + ")");
                              accountBean.tagMerchant((Connection)null, toUserId, toUserEntry.username, fromUserId, fromUserEntry.username, tagData, fromUserEntry.dateCreated, (Long)null, MerchantTagData.StatusEnum.ACTIVE.value());
                           }

                           log.info("Removing redis key: " + accountEntryId);
                           jedisConn.zrem(Redis.KeySpaceQueuesInstance.TOP_MERCHANT_TAG.toString(), new String[]{accountEntryId});
                        } else {
                           log.warn("AccountEntry for reference [" + fromUserEntry.reference + "] not found. Not yet replicated to slave?");
                           Thread.sleep(1000L);
                        }
                     } else {
                        log.warn("AccountEntry [" + accountEntryId + "] not found. Not yet replicated to slave?");
                        Thread.sleep(1000L);
                     }
                  } else {
                     log.debug("Merchant Tag set is empty");
                     Thread.sleep(1000L);
                  }
               } catch (JedisException var32) {
                  log.error("Jedis connection error: ", var32);
                  Redis.disconnect(jedisConn, log);

                  try {
                     Thread.sleep(1000L);
                  } catch (InterruptedException var29) {
                  }
               } catch (CreateException var33) {
                  log.error("Failed to instantiate bean: " + var33.getMessage());

                  try {
                     Thread.sleep(1000L);
                  } catch (InterruptedException var28) {
                  }
               }
            }

            log.info("Merchant tagging disabled... dying to live another day!");
         } catch (Exception var34) {
            log.error("Unhandled exception while executing MerchantTag job: ", var34);
         } finally {
            Redis.disconnect(jedisConn, log);
            semaphore.release();
         }

      }
   }
}
