package com.projectgoth.fusion.registry;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ConfigurableExecutor;
import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import org.apache.log4j.Logger;

public abstract class RegistryThreadPoolExecutor {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RegistryThreadPoolExecutor.class));
   public static long AMD_ENABLED_REFRESH_INTERVAL = 60000L;
   private static LazyLoader<Boolean> amdEnabled;

   public static void schedule(Runnable cmd) throws Exception {
      RegistryThreadPoolExecutor.RegistryExecutor.INSTANCE.execute(cmd);
   }

   static {
      amdEnabled = new LazyLoader<Boolean>("REGISTRY_AMD_ENABLED", AMD_ENABLED_REFRESH_INTERVAL) {
         protected Boolean fetchValue() throws Exception {
            return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.IceAsyncSettings.REGISTRY_AMD_ENABLED);
         }
      };
   }

   private static class RegistryExecutor {
      public static final ConfigurableExecutor INSTANCE;

      static {
         INSTANCE = new ConfigurableExecutor(SystemPropertyEntities.IceAsyncSettings.REGISTRY_TPOOL_CORE_SIZE, SystemPropertyEntities.IceAsyncSettings.REGISTRY_TPOOL_MAX_SIZE, SystemPropertyEntities.IceAsyncSettings.REGISTRY_TPOOL_KEEP_ALIVE_SECONDS, SystemPropertyEntities.IceAsyncSettings.REGISTRY_REFRESH_THREAD_POOL_PROPERTIES_ENABLED, SystemPropertyEntities.IceAsyncSettings.REGISTRY_TPOOL_PROPS_REFRESH_INTERVAL_SECS);
      }
   }
}
