package com.projectgoth.fusion.app.dao.config;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public enum FusionConfigEnum {
   DAO(new FusionPropertiesFileConfigurationNamespace("dao.properties")),
   FUSION_DB_READ(new FusionPropertiesFileConfigurationNamespace("fusion_db_read.properties")),
   OLAP_DB_READ(new FusionPropertiesFileConfigurationNamespace("olap_db_read.properties")),
   FUSION_DB_WRITE(new FusionPropertiesFileConfigurationNamespace("fusion_db_write.properties"));

   private FusionPropertiesFileConfigurationNamespace configurationNamespace;

   private FusionConfigEnum(FusionPropertiesFileConfigurationNamespace configurationNamespace) {
      this.configurationNamespace = configurationNamespace;
   }

   public String getIdentifier() {
      return this.configurationNamespace.getIdentifier();
   }

   private static void setupPeriodicCheck() {
      ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
      scheduler.scheduleAtFixedRate(new FusionConfigEnum.PeriodicCheck(), 60L, 60L, TimeUnit.SECONDS);
   }

   static {
      setupPeriodicCheck();
   }

   private static class PeriodicCheck implements Runnable {
      private PeriodicCheck() {
      }

      public void run() {
         FusionConfigEnum[] arr$ = FusionConfigEnum.values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            FusionConfigEnum e = arr$[i$];
            e.configurationNamespace.reloadProperties();
         }

      }

      // $FF: synthetic method
      PeriodicCheck(Object x0) {
         this();
      }
   }
}
