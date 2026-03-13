package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.MerchantTagData;
import com.projectgoth.fusion.data.MerchantTagUserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;
import javax.ejb.EJBException;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MerchantTagExpiry implements Job {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MerchantTagExpiry.class));
   private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
   private static final Semaphore semaphore = new Semaphore(1);

   public void execute(JobExecutionContext jobContext) throws JobExecutionException {
      if (!semaphore.tryAcquire()) {
         log.warn("Another job is still processing merchant tag expiration. Exiting...");
      } else {
         JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
         int limit = jobDataMap.getInt("limit");
         long throttle = (long)jobDataMap.getInt("throttle");
         int[] defaultTagTypes = new int[]{MerchantTagData.TypeEnum.NON_TOP_MERCHANT_TAG.value(), MerchantTagData.TypeEnum.TOP_MERCHANT_TAG.value()};
         int[] merchantTagTypesToExpire = SystemProperty.getIntArray("MerchantTagTypesToExpire", defaultTagTypes);
         Object expiredTags = new ArrayList();

         try {
            Account accountBean = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
            int[] arr$ = merchantTagTypesToExpire;
            int len$ = merchantTagTypesToExpire.length;

            label161:
            for(int i$ = 0; i$ < len$; ++i$) {
               int tagTypeIdx = arr$[i$];
               MerchantTagData.TypeEnum tagType = MerchantTagData.TypeEnum.fromValue(tagTypeIdx);
               if (tagType == null) {
                  log.info("Undefined tag expiration type detected: " + tagTypeIdx);
               } else {
                  int offset = 0;
                  boolean dataAvailable = true;

                  try {
                     while(true) {
                        while(true) {
                           if (!dataAvailable) {
                              continue label161;
                           }

                           if (!SystemProperty.getBool("merchantTagExpirationEnabled", false)) {
                              log.warn("Merchant Tag Expiration job is disabled...");
                              continue label161;
                           }

                           log.info("Starting " + tagType.description() + " on " + dateFormat.format(new Date()) + " with limit: " + limit + " and throttle: " + throttle + " AND offset: " + offset);
                           ((List)expiredTags).clear();
                           expiredTags = this.getExpiredTags(tagType, accountBean, limit, offset);
                           if (((List)expiredTags).size() == 0) {
                              log.info("No data available for this range... finished processing");
                              dataAvailable = false;
                           } else {
                              log.info("Started processing merchant tag expiry: limit[" + limit + "] offset[" + offset + "] found [" + ((List)expiredTags).size() + "].");
                              offset = ((MerchantTagUserData)((List)expiredTags).get(((List)expiredTags).size() - 1)).id;

                              try {
                                 if (throttle > 0L) {
                                    try {
                                       Thread.sleep(throttle);
                                    } catch (InterruptedException var27) {
                                    }
                                 }

                                 accountBean.untagMerchant((Connection)null, (List)expiredTags);
                                 Iterator i$ = ((List)expiredTags).iterator();

                                 while(i$.hasNext()) {
                                    MerchantTagUserData tag = (MerchantTagUserData)i$.next();
                                    log.info("Cleared tag for: merchant[" + tag.merchantUserName + "] to user[" + tag.userName + "]");
                                 }
                              } catch (Exception var28) {
                                 Exception e = var28;
                                 Iterator i$ = ((List)expiredTags).iterator();

                                 while(i$.hasNext()) {
                                    MerchantTagUserData tag = (MerchantTagUserData)i$.next();
                                    log.warn("Failed to untag merchant for user: " + tag.userName + ", reason:" + e.getLocalizedMessage());
                                 }
                              }
                           }
                        }
                     }
                  } catch (Exception var29) {
                     log.info("An error occurred while retrieving expiring " + tagType.description() + ": " + var29.getMessage());
                  }
               }
            }
         } catch (Exception var30) {
            log.info("An error occurred while expiring merchant tags: " + var30.getMessage());
         } finally {
            semaphore.release();
         }

      }
   }

   private List<MerchantTagUserData> getExpiredTags(MerchantTagData.TypeEnum tagType, Account accountBean, int limit, int offset) throws Exception {
      try {
         if (tagType == MerchantTagData.TypeEnum.NON_TOP_MERCHANT_TAG) {
            return accountBean.getExpiredNonTopMerchantTags(limit, offset);
         } else if (tagType == MerchantTagData.TypeEnum.TOP_MERCHANT_TAG) {
            return accountBean.getExpiredTopMerchantTags(limit, offset);
         } else {
            log.error("Not expiring tags in type: [" + tagType + "]");
            return null;
         }
      } catch (EJBException var6) {
         throw new Exception(var6.getMessage());
      }
   }
}
