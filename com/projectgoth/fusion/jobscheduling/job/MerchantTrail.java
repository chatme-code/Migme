package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.rewardsystem.RewardCentre;
import com.projectgoth.fusion.rewardsystem.triggers.MerchantTrailTrigger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MerchantTrail implements Job {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MerchantTrail.class));
   private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
   private static final Semaphore semaphore = new Semaphore(1);
   private Date runDate = null;

   public void setRunDate(Date runDate) {
      if (runDate != null) {
         this.runDate = runDate;
      }

   }

   public void execute(JobExecutionContext context) throws JobExecutionException {
      if (!semaphore.tryAcquire()) {
         log.warn("Another job is still processing merchant trail. Exiting...");
      } else {
         Calendar start = Calendar.getInstance();
         Calendar end = Calendar.getInstance();
         if (this.runDate != null) {
            start.setTime(this.runDate);
            end.setTime(this.runDate);
         }

         start.add(6, -1);
         Date startDate = start.getTime();
         Date endDate = end.getTime();

         try {
            this.triggerOtherMerchantTrails(startDate, endDate);
            this.triggerGamesMerchantTrails(startDate, endDate);
            this.triggerThirdPartyMerchantTrails(startDate, endDate);
         } finally {
            semaphore.release();
         }

      }
   }

   public void triggerOtherMerchantTrails(Date startDate, Date endDate) {
      try {
         log.info("Begin: merchant trails for other purchases");
         Account accountBean = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         log.info("Triggering MerchantTrail for period " + dateFormat.format(startDate) + " to " + dateFormat.format(endDate));
         Map<Integer, Double> report = accountBean.getMerchantRevenueTrailReport(startDate, endDate);
         log.info(report.size() + " entries found from getMerchantRevenueTrailReport()");
         Iterator i$ = report.entrySet().iterator();

         Entry e;
         while(i$.hasNext()) {
            e = (Entry)i$.next();
            this.giveMerchantRevenueTrail((Integer)e.getKey(), (Double)e.getValue(), startDate);
         }

         report = accountBean.getSuperMerchantRevenueTrailReport(startDate, endDate);
         log.info(report.size() + " entries found from getSuperMerchantRevenueTrailReport()");
         i$ = report.entrySet().iterator();

         while(i$.hasNext()) {
            e = (Entry)i$.next();
            this.giveMerchantRevenueTrail((Integer)e.getKey(), (Double)e.getValue(), startDate);
         }

         log.info("End: merchant trails for other purchases");
      } catch (Exception var7) {
         log.error("Error in giving Merchant Trails for other purchases.Exception:" + var7, var7);
      }

   }

   public void triggerGamesMerchantTrails(Date startDate, Date endDate) {
      try {
         log.info("Begin: merchant trails for games purchases");
         Account accountBean = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         log.info("Triggering MerchantTrails for games for period " + dateFormat.format(startDate) + " to " + dateFormat.format(endDate));
         Map<Integer, Double> report = accountBean.getMerchantRevenueGameTrailReport(startDate, endDate);
         log.info(report.size() + " entries found from getMerchantRevenueGameTrailReport()");
         Iterator i$ = report.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<Integer, Double> e = (Entry)i$.next();
            this.giveMerchantRevenueGameTrail((Integer)e.getKey(), (Double)e.getValue(), startDate);
         }

         log.info("End: merchant trails for games purchases");
      } catch (Exception var7) {
         log.error("Error in giving Merchant Trails for games.Exception:" + var7, var7);
      }

   }

   public void triggerThirdPartyMerchantTrails(Date startDate, Date endDate) {
      try {
         log.info("Begin: merchant trails for third party app purchases");
         Account accountBean = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         log.info("Triggering MerchantTrails for third party apps for period " + dateFormat.format(startDate) + " to " + dateFormat.format(endDate));
         Map<Integer, Double> report = accountBean.getMerchantRevenueThirdPartyTrailReport(startDate, endDate);
         log.info(report.size() + " entries found from getMerchantRevenueThirdPartyTrailReport()");
         Iterator i$ = report.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<Integer, Double> e = (Entry)i$.next();
            this.giveMerchantRevenueThirdPartyTrail((Integer)e.getKey(), (Double)e.getValue(), startDate);
         }

         log.info("End: merchant trails for third party app purchases");
      } catch (Exception var7) {
         log.error("Error in giving Merchant Trails for third party apps. Exception: " + var7, var7);
      }

   }

   private void giveMerchantRevenueTrail(Integer userID, Double amount, Date startDate) {
      try {
         if (amount == 0.0D) {
            log.info("Skip merchant revenue trail for merchant [" + userID + "]. Amount = 0");
            return;
         }

         User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         UserData userData = userBean.loadUserFromID(userID);
         if (userData == null) {
            log.error("Unable to give merchant revenue trail to merchant [" + userID + "]. Invalid user ID");
            return;
         }

         if (userData.type != UserData.TypeEnum.MIG33_TOP_MERCHANT) {
            log.error("Unable to give merchant revenue trail to merchant [" + userData.username + "]. User no longer top merchant.");
            return;
         }

         Account accountBean = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         accountBean.giveMerchantRevenueTrail(userData.username, userData.username, "Revenue Trail (customer purchases) " + dateFormat.format(startDate), amount, CurrencyData.baseCurrency, new AccountEntrySourceData(MerchantTrail.class));
         log.info("Gave " + amount + " " + CurrencyData.baseCurrency + " merchant trail to " + userData.username);

         try {
            double amountInUSD = accountBean.convertCurrency(amount, CurrencyData.baseCurrency, "USD");
            sendMerchantTrailRewardProgramTrigger(userData, amountInUSD, 0);
         } catch (Exception var9) {
            log.error("Unable to send reward program trigger for merchant trails (revenue) earned for user [" + userData.username + "].Exception:" + var9, var9);
         }
      } catch (Exception var10) {
         log.error("Unable to give merchant revenue trail to merchant [" + userID + "].Exception:" + var10, var10);
      }

   }

   private void giveMerchantRevenueGameTrail(Integer userID, Double amount, Date startDate) {
      try {
         if (amount == 0.0D) {
            log.info("Skip merchant revenue game trail for merchant [" + userID + "]. Amount = 0");
            return;
         }

         User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         UserData userData = userBean.loadUserFromID(userID);
         if (userData == null) {
            log.error("Unable to give merchant revenue game trail to merchant [" + userID + "]. Invalid user ID");
            return;
         }

         if (userData.type != UserData.TypeEnum.MIG33_TOP_MERCHANT) {
            log.error("Unable to give merchant revenue game trail to merchant [" + userData.username + "]. User no longer top merchant.");
            return;
         }

         Account accountBean = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         accountBean.giveMerchantRevenueGameTrail(userData.username, userData.username, "Revenue Trail (chatroom games) " + dateFormat.format(startDate), amount, CurrencyData.baseCurrency, new AccountEntrySourceData(MerchantTrail.class));
         log.info("Gave " + amount + " " + CurrencyData.baseCurrency + " merchant game trail to " + userData.username);

         try {
            double amountInUSD = accountBean.convertCurrency(amount, CurrencyData.baseCurrency, "USD");
            sendMerchantTrailRewardProgramTrigger(userData, amountInUSD, 0);
         } catch (Exception var9) {
            log.error("Unable to send reward program trigger for merchant trails (games) earned for user [" + userData.username + "].Exception:" + var9, var9);
         }
      } catch (Exception var10) {
         log.error("Unable to give merchant revenue game trail to merchant [" + userID + "].Exception:" + var10, var10);
      }

   }

   private void giveMerchantRevenueThirdPartyTrail(Integer userID, Double amount, Date startDate) {
      try {
         if (amount == 0.0D) {
            log.info("Skip merchant revenue third party trail for merchant [" + userID + "]. Amount = 0");
            return;
         }

         User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         UserData userData = userBean.loadUserFromID(userID);
         if (userData == null) {
            log.error("Unable to give merchant revenue third party trail to merchant [" + userID + "]. Invalid user ID");
            return;
         }

         if (userData.type != UserData.TypeEnum.MIG33_TOP_MERCHANT) {
            log.error("Unable to give merchant revenue third party trail to merchant [" + userData.username + "]. User no longer top merchant.");
            return;
         }

         Account accountBean = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         accountBean.giveMerchantRevenueThirdPartyTrail(userData.username, userData.username, "Revenue Trail (third party games) " + dateFormat.format(startDate), amount, CurrencyData.baseCurrency, new AccountEntrySourceData(MerchantTrail.class));
         log.info("Gave " + amount + " " + CurrencyData.baseCurrency + " merchant third party trail to " + userData.username);

         try {
            double amountInUSD = accountBean.convertCurrency(amount, CurrencyData.baseCurrency, "USD");
            sendMerchantTrailRewardProgramTrigger(userData, amountInUSD, 0);
         } catch (Exception var9) {
            log.error("Unable to send reward program trigger for merchant trails (third party) earned for user [" + userData.username + "].Exception:" + var9, var9);
         }
      } catch (Exception var10) {
         log.error("Unable to give merchant revenue third party trail to merchant [" + userID + "].Exception:" + var10, var10);
      }

   }

   public static boolean sendMerchantTrailRewardProgramTrigger(UserData userData, double amountInUSD, int waitInSeconds) throws Exception {
      try {
         MerchantTrailTrigger trigger = new MerchantTrailTrigger(userData);
         trigger.amountDelta = amountInUSD;
         trigger.quantityDelta = 1;
         trigger.currency = "USD";
         Future<Boolean> future = RewardCentre.getInstance().sendTrigger(trigger);
         if (future == null) {
            throw new Exception("Unable to submit trigger - future object is null.");
         } else {
            return waitInSeconds == 0 ? true : (Boolean)future.get((long)waitInSeconds, TimeUnit.SECONDS);
         }
      } catch (TimeoutException var6) {
         throw new Exception("Sending of credit recharge trigger timed out");
      }
   }

   public static void main(String[] args) {
      try {
         Date runDate = null;
         String type = "";
         if (args.length == 0) {
            System.out.println("Usage: [all | other | games | thirdparty] [dd-mm-yyyy]");
            System.exit(1);
         }

         if (args.length > 1) {
            runDate = dateFormat.parse(args[1]);
         }

         Calendar start = Calendar.getInstance();
         Calendar end = Calendar.getInstance();
         if (runDate != null) {
            start.setTime(runDate);
            end.setTime(runDate);
         }

         start.add(6, -1);
         Date startDate = start.getTime();
         Date endDate = end.getTime();
         MerchantTrail x = new MerchantTrail();
         if ("all".equalsIgnoreCase(args[0])) {
            x.setRunDate(runDate);
            x.execute((JobExecutionContext)null);
         } else if ("other".equalsIgnoreCase(args[0])) {
            x.triggerOtherMerchantTrails(startDate, endDate);
         } else if ("games".equalsIgnoreCase(args[0])) {
            x.triggerGamesMerchantTrails(startDate, endDate);
         } else if ("thirdparty".equalsIgnoreCase(args[0])) {
            x.triggerThirdPartyMerchantTrails(startDate, endDate);
         } else {
            System.out.println("Usage: [all | other | games | thirdparty] [dd-mm-yyyy]");
            System.exit(1);
         }
      } catch (Exception var8) {
         var8.printStackTrace();
         System.exit(1);
      }

      System.exit(0);
   }
}
