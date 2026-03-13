package com.projectgoth.fusion.botservice;

import Ice.Application;
import Ice.ObjectAdapter;
import Ice.ObjectPrx;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ConfigurableExecutor;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.slice.BotServiceAdminPrx;
import com.projectgoth.fusion.slice.BotServiceAdminPrxHelper;
import com.projectgoth.fusion.slice.BotServicePrx;
import com.projectgoth.fusion.slice.BotServicePrxHelper;
import com.projectgoth.fusion.slice.BotServiceStats;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.RegistryPrxHelper;
import com.projectgoth.fusion.stats.IceStats;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class BotService extends Application {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(BotService.class));
   private ScheduledExecutorService executor;
   private BotServiceI botServiceServant;
   private String registryLocation;
   private int maxThreadPoolSize;
   private int botPurgerInterval;

   private boolean getSettings() {
      Properties properties = communicator().getProperties();
      this.registryLocation = properties.getProperty("RegistryProxy");
      this.maxThreadPoolSize = properties.getPropertyAsIntWithDefault("MaxThreadPoolSize", Integer.MAX_VALUE);
      this.botPurgerInterval = properties.getPropertyAsIntWithDefault("BotPurgerInterval", 60) * 1000;
      return true;
   }

   public BotServiceI getBotServiceServant() {
      return this.botServiceServant;
   }

   public void setBotServiceServant(BotServiceI botServiceServant) {
      this.botServiceServant = botServiceServant;
   }

   public BotServiceStats getStats() {
      return this.botServiceServant.getStats();
   }

   public int run(String[] arg0) {
      try {
         if (!this.getSettings()) {
            byte var17 = -1;
            return var17;
         }

         this.executor = Executors.newScheduledThreadPool(this.maxThreadPoolSize > 0 ? this.maxThreadPoolSize : 1);
         this.cleanupPendingPots();
         log.info("Configured endpoint [" + communicator().getProperties().getProperty("BotServiceAdapter.Endpoints") + "]");
         ObjectAdapter serviceAdapter = communicator().createObjectAdapter("BotServiceAdapter");
         ObjectPrx baseServiceProxy = serviceAdapter.add(this.botServiceServant, Util.stringToIdentity("BotService"));
         BotServicePrx serviceProxy = BotServicePrxHelper.checkedCast(baseServiceProxy);
         this.botServiceServant.setProxy(serviceProxy);
         this.botServiceServant.setExecutor(this.executor);
         serviceAdapter.activate();
         IceStats.getInstance().setIceObjects(communicator(), serviceAdapter, (ConfigurableExecutor)null);
         this.startIdleBotPurger();
         ObjectAdapter adminAdapter = communicator().createObjectAdapter("BotServiceAdminAdapter");
         ObjectPrx baseAdminProxy = adminAdapter.add(new BotServiceAdminI(this), Util.stringToIdentity("BotServiceAdmin"));
         BotServiceAdminPrx adminProxy = BotServiceAdminPrxHelper.checkedCast(baseAdminProxy);
         adminAdapter.activate();
         String hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
         ObjectPrx baseRegistryProxy = communicator().stringToProxy(this.registryLocation);
         RegistryPrx registryProxy = RegistryPrxHelper.checkedCast(baseRegistryProxy);
         registryProxy.registerBotService(hostName, 0, serviceProxy, adminProxy);
         log.info("Registered with " + registryProxy + ". Service started");
         communicator().waitForShutdown();
         log.info("Terminating server");
         this.botServiceServant.shutdown();
      } catch (Exception var15) {
         log.error("BotService could not be initialized", var15);
      } finally {
         if (interrupted()) {
            log.fatal("BotService: terminating");
            this.botServiceServant.shutdown();
         }

      }

      return 0;
   }

   private void startIdleBotPurger() {
      this.executor.scheduleAtFixedRate(new BotService.IdleBotPurger(), 0L, (long)this.botPurgerInterval, TimeUnit.SECONDS);
   }

   private void cleanupPendingPots() {
      log.debug("Running PotCleanupHandler...");

      try {
         Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         accountEJB.cancelAllPots(new AccountEntrySourceData(BotService.class));
      } catch (Exception var2) {
         log.error("cleanupPendingPots threw an exception: ", var2);
      }

      log.debug("End running PotCleanupHandler...");
   }

   private class IdleBotPurger implements Runnable {
      private IdleBotPurger() {
      }

      public void run() {
         BotService.log.debug("Running IdleBotPurger...");
         BotService.this.botServiceServant.purgeIdleBots();
         BotService.log.debug("End running IdleBotPurger...");
      }

      // $FF: synthetic method
      IdleBotPurger(Object x1) {
         this();
      }
   }
}
