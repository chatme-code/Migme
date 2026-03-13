package com.projectgoth.fusion.objectcache;

import Ice.CommunicatorDestroyedException;
import Ice.Current;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.ObjectCacheStats;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice._ObjectCacheAdminDisp;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class ObjectCacheAdminI extends _ObjectCacheAdminDisp {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ObjectCacheAdminI.class));
   private ObjectCacheContext applicationContext;
   private ObjectCacheAdminI.RegistryNotificationTask registryNotificationTask;

   public ObjectCacheAdminI(ObjectCacheContext ctx) {
      this.applicationContext = ctx;
   }

   public void startTimer() {
      long registryNotificationInterval = (long)(this.applicationContext.getProperties().getPropertyAsIntWithDefault("RegistryNotificationInterval", 1) * 1000);
      this.registryNotificationTask = new ObjectCacheAdminI.RegistryNotificationTask();
      Timer timer = new Timer(true);
      timer.schedule(this.registryNotificationTask, registryNotificationInterval, registryNotificationInterval);
   }

   public int ping(Current __current) {
      int currentLoad = this.applicationContext.getObjectCache().getUserCount();
      return currentLoad;
   }

   public ObjectCacheStats getStats(Current __current) throws FusionException {
      ObjectCacheStats stats = ServiceStatsFactory.getObjectCacheStats(ObjectCache.hostName, ObjectCache.startTime);

      try {
         this.applicationContext.getObjectCache().getStats(stats);
         return stats;
      } catch (Exception var5) {
         log.warn("Invalid Stats: " + var5);
         FusionException fe = new FusionException();
         fe.message = "Initialisation incomplete";
         throw fe;
      }
   }

   public String[] getUsernames(Current __current) {
      return this.applicationContext.getObjectCache().getUsernames();
   }

   public void reloadEmotes(Current __current) {
      throw new UnsupportedOperationException("This operation has been deprecated");
   }

   public void setLoadWeightage(int weightage, Current __current) {
      this.applicationContext.getObjectCache().setLoadWeightage(weightage);
   }

   public int getLoadWeightage(Current __current) {
      return this.applicationContext.getObjectCache().getLoadWeightage();
   }

   private class RegistryNotificationTask extends TimerTask {
      private RegistryNotificationTask() {
      }

      public void run() {
         try {
            ObjectCacheAdminI.this.applicationContext.getRegistryPrx().registerObjectCacheStats(ObjectCacheAdminI.this.applicationContext.getUniqueID(), ObjectCacheAdminI.this.getStats());
         } catch (ObjectNotFoundException var2) {
            ObjectCacheAdminI.log.warn("ObjectCache not currently registered with Registry");
         } catch (FusionException var3) {
            ObjectCacheAdminI.log.warn("ObjectCacheStats not initialized");
         } catch (CommunicatorDestroyedException var4) {
            ObjectCacheAdminI.log.warn("Unable to register stats with registry due to communicator shutdown");
         } catch (Exception var5) {
            ObjectCacheAdminI.log.error("Unable to register stats with registry", var5);
         } catch (Throwable var6) {
            ObjectCacheAdminI.log.error("Unhandled timer exception:" + var6, var6);
         }

      }

      // $FF: synthetic method
      RegistryNotificationTask(Object x1) {
         this();
      }
   }
}
